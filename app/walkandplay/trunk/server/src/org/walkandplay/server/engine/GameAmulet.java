/****************************************************************
 * Copyright 2004 - Waag Society - www.waag.org - See license below *
 ****************************************************************/

package org.walkandplay.server.engine;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.amuse.amulet.DefaultAmulet;
import org.keyworx.amuse.core.Agent;
import org.keyworx.amuse.core.AmuletContext;
import org.keyworx.amuse.core.AmuseException;
import org.keyworx.common.log.Log;
import org.keyworx.common.util.Sys;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * generic Game amulet.
 *
 * @version $Id: GameAmulet.java,v 1.1.1.1 2006/04/03 09:21:35 rlenz Exp $
 * @author Just van den Broecke
 */
public class GameAmulet extends DefaultAmulet implements GameProtocol, IndicationListener {
	private GameEngine gameEngine;
	private Log log;
	private volatile boolean accepting;
	private List indications = new ArrayList(1);
	private HashMap observers = new HashMap(1);
	private DayLogger messageLogger;
	private long now = Sys.now();
	private long then = Sys.now();

	/** Interprete command sent by agent. */
	public void accept(Agent anAgentFrom, JXElement aMessage) throws AmuseException {
		// setDebug(true);
		// log.trace("accept: received command=" + aMessage);
		GameMessage gameMessage = GameMessage.createRequest(aMessage, anAgentFrom.getAgentContext().getId());
		GameMessage responseMessage = null;

		try {
			accepting = true;

			// Log incoming message
			logMessage(gameMessage);

			// Let the gameEngine handle all incoming messages.
			// Handle message, optinal response is returned
			responseMessage = gameEngine.doMessage(gameMessage);

			// If message returned send back to agent
			if (responseMessage != null) {
				logMessage(responseMessage);
				unicast(anAgentFrom, responseMessage.getData());
			}

			// If any indications received from GameEngine while
			// processing, send them _after_ response is sent.
			for (int i = 0; i < indications.size(); i++) {
				sendIndication((GameMessage) indications.get(i));
			}
		} catch (Throwable t) {
			log.warn("Error processing " + aMessage.getTag(), t);
			// Only Send negative response with request messages
			String msgTag = aMessage.getTag();
			if (msgTag.equals(MSG_JOIN_REQ) || msgTag.equals(MSG_LEAVE_REQ)) {
				throw new AmuseException("Error processing " + msgTag, t);
			} else if (msgTag.endsWith(POSTFIX_REQ)) {
				responseMessage = GameMessage.createNegativeResponse(gameMessage, "error processing request " + t);
				logMessage(responseMessage);
				unicast(anAgentFrom, responseMessage.getData());
			} else {
				// ignore
			}
		} finally {
			// Reset flag that we are in accepting mode
			accepting = false;

			// Always clear indication list
			indications.clear();
		}
	}

	/** Callback from GameEngine. */
	public void onIndication(GameMessage aMsg) {
		if (accepting) {
			// When processing a message we keep indications
			// until message handling finished.
			indications.add(aMsg);
		} else {
			// Spontaneous indication or from other trigger
			// send now.
			sendIndication(aMsg);
		}
	}

	/** Init the data structures. */
	public void activate() throws AmuseException {
		log.info("activated");
	}

	public void init(AmuletContext theContext) throws AmuseException {
		try {
            super.init(theContext);
            String space = theContext.getSpaceId();
            gameEngine = gameEngine.getInstance(space);
            gameEngine.init();
            gameEngine.addIndicationListener(this);
            log = gameEngine.getLog();
            messageLogger = new DayLogger(gameEngine.getDataDirPath() + File.separator + "messages", "msg", log);            
        } catch (Throwable t) {
			logWarning(null, "Error in init GameAmulet t=" + t);
			throw new AmuseException("Cannot init GameAmulet", t);
		}
	}

	public void stop() throws AmuseException {
		super.stop();
	}

	/** Save objects for next session. */
	public void passivate() throws AmuseException {
		super.passivate();
		log.info("passivated");
	}

	public void afterJoin(Agent agent) {
		log.info("afterJoin: " + agent.getAgentContext().getId());
	}

	public void beforeJoin(Agent agent, JXElement joinRequest) throws AmuseException {
		log.info("beforeJoin: " + agent.getAgentContext().getId() + " " + joinRequest);
		try {
			JXElement options = joinRequest.getChildByTag(GameProtocol.TAG_OPTIONS);
			if (options.getAttr(ATTR_TYPE).equals(VAL_GAME_OBSERVER)) {
				String agentId = agent.getAgentContext().getId();
				log.info("adding observer: " + agentId);
				observers.put(agentId, agent);
				return;
			}
			accept(agent, joinRequest);
		} catch (Throwable t) {
			// Hmm, what to do ???
			log.warn("Error in joining game", t);
			throw new AmuseException("Error in joining game", t);
		}
	}

	public void leave(Agent agent) throws AmuseException {
		log.info("leave: " + agent.getAgentContext().getId());
		try {
			String agentId = agent.getAgentContext().getId();
			if (observers.remove(agent.getAgentContext().getId()) != null) {
				log.info("removing observer: " + agentId);
				return;
			}
			JXElement leaveReq = new JXElement(MSG_LEAVE_REQ);
			accept(agent, leaveReq);
		} catch (Throwable t) {
			// Hmm, what to do ???
			log.warn("Error in leaving game", t);
		}

	}


	protected void broadcast(Agent anAgentFrom, JXElement aMessage, boolean excludeSender) {
		getContext().broadcast(anAgentFrom, aMessage, excludeSender);
	}

	/** Send message to agent. */
	protected void unicast(Agent agent, JXElement aMessage) {
		getContext().unicast(agent, agent, aMessage);
	}

	/** Send message to agent by id. */
	protected void unicast(String anAgentId, JXElement aMessage) {
		if (anAgentId == null || anAgentId.length() == 0) {
			return;
		}

		Agent toAgent = getContext().getAgent(anAgentId);
		if (toAgent == null) {
			logWarning(null, "Cannot send message to agent id=" + anAgentId);
			return;
		}

		unicast(toAgent, aMessage);
	}

	/** Send indication message. */
	private void sendIndication(GameMessage aMsg) {
		try {
			logMessage(aMsg);

			JXElement messageData = aMsg.getData();

			// Look at destination type
			if (aMsg.toType.equals(TYPE_SINGLE)) {
				// Send to single agent
				unicast(aMsg.to, messageData);

			} else if (aMsg.toType.equals(TYPE_ALL)) {
				// Send to all
				broadcast(null, messageData, false);
			} else if (aMsg.toType.equals(TYPE_MULTI)) {
				// Send to all players

				// The "to" field is a comma-separated list of agent id's
				String[] agentToIds = aMsg.to.split(",");
				for (int i = 0; i < agentToIds.length; i++) {
					unicast(agentToIds[i], messageData);
				}
			} else if (aMsg.toType.equals(TYPE_ALL_BUT_SENDER)) {
				// Send to all except sender
				if (aMsg.from == null) {
					log.warn("Cannot send indication: " + messageData.getTag() + " from==null");
					return;
				}

				Agent agent = getContext().getAgent(aMsg.from);
				if (agent == null) {
					log.warn("Cannot send indication: agent==null");
					return;
				}
				broadcast(agent, messageData, true);
			}
		} catch (Throwable t) {
			log.warn("Error in sending indication: " + aMsg, t);
		}
	}

	private void logMessage(GameMessage aGameMessage) {
		JXElement xmlMessage = aGameMessage.getData();

		// Hack for medium raw upload of image or video
		// Convert bytes to readable ASCII such that
		// log file is not corrupted
		if (xmlMessage.getTag().equals("medium-raw-upload-req")) {
			String type = xmlMessage.getAttr("type");
			JXElement data = xmlMessage.getChildByTag("data");
			if ((type.equals("image") || type.equals("video")) && data.getAttr("encoding").equals("raw")) {
				byte[] cdata = data.getCDATA();
				byte[] encodedCDATA = encode(cdata, 0, cdata.length);

				JXElement newXMLMessage = new JXElement("medium-raw-upload-req");
				newXMLMessage.setAttrs(xmlMessage.getAttrs());
				JXElement newData = new JXElement("data");
				newData.setAttr("encoding", "hexasc");
				newData.setCDATA(encodedCDATA);
				newXMLMessage.addChild(newData);
				aGameMessage = GameMessage.createRequest(newXMLMessage, aGameMessage.from);
			}
		}

		JXElement gameMessageElement = aGameMessage.toXML();
		now = Sys.now();
		gameMessageElement.setAttr("delta", now - then);
		messageLogger.log(gameMessageElement);
		then = now;
	}

	public static byte[] encode(byte[] data, int off, int len) {
		byte[] ch;
		int i;

		// Convert bytes to hex digits
		ch = new byte[data.length * 2];
		i = 0;

		while (len-- > 0) {
			int b;
			int d;

			// Convert next byte into a hex digit pair
			b = data[off++] & 0xFF;

			d = b >> 4;
			d = (d < 0xA ? d + '0' : d - 0xA + 'A');
			ch[i++] = (byte) d;

			d = b & 0xF;
			d = (d < 0xA ? d + '0' : d - 0xA + 'A');
			ch[i++] = (byte) d;
		}

		return ch;
	}


}
