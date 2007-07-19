package org.walkandplay.server.control;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.HandlerUtil;
import org.keyworx.amuse.core.Protocol;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Sys;
import org.keyworx.oase.api.OaseException;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.walkandplay.server.logic.GamePlayLogic;
import org.walkandplay.server.logic.WPQueryLogic;
import org.walkandplay.server.util.Constants;

import java.util.Vector;

/**
 * GamePlayHandler.
 * <p/>
 * Redirects the requests to the right logic method
 *
 * @author Ronald Lenz
 * @version $Id: GameScheduleHandler.java 327 2007-01-25 16:54:39Z just $
 */
public class GamePlayHandler extends DefaultHandler implements Constants {

	public final static String PLAY_RESET_SERVICE = "play-reset";
	public final static String PLAY_START_SERVICE = "play-start";
	public final static String PLAY_LOCATION_SERVICE = "play-location";
	public final static String PLAY_ANSWERTASK_SERVICE = "play-answertask";
	public final static String PLAY_ADD_MEDIUM_SERVICE = "play-add-medium";
	public final static String PLAY_GET_GAMEPLAY_SERVICE = "play-get-gameplay";
	public final static String PLAY_GET_EVENTS_SERVICE = "play-get-events";
	public final static String PLAY_HEARTBEAT_SERVICE = "play-hb";

	private Log log = Logging.getLog("GamePlayHandler");

	/**
	 * Processes the Client Request.
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public UtopiaResponse processRequest(UtopiaRequest anUtopiaReq) throws UtopiaException {
		Log log = Logging.getLog(anUtopiaReq);

		// Get the service name for the request
		String service = anUtopiaReq.getServiceName();

		if (getProperty("verbose").equals("true")) {
			log.info("Handling request for service=" + service);
			log.info(new String(anUtopiaReq.getRequestCommand().toBytes(false)));
		}

		long t1;
		JXElement response;
		t1 = Sys.now();
		try {
			if (service.equals(PLAY_START_SERVICE)) {
				response = startReq(anUtopiaReq);
			} else if (service.equals(PLAY_RESET_SERVICE)) {
				response = resetReq(anUtopiaReq);
			} else if (service.equals(PLAY_LOCATION_SERVICE)) {
				response = locationReq(anUtopiaReq);
			} else if (service.equals(PLAY_ANSWERTASK_SERVICE)) {
				response = answerTaskReq(anUtopiaReq);
			} else if (service.equals(PLAY_ADD_MEDIUM_SERVICE)) {
				response = addMediumReq(anUtopiaReq);
			} else if (service.equals(PLAY_GET_GAMEPLAY_SERVICE)) {
				response = getGamePlayReq(anUtopiaReq);
			} else if (service.equals(PLAY_HEARTBEAT_SERVICE)) {
				response = heartbeatReq(anUtopiaReq);
			} else if (service.equals(PLAY_GET_EVENTS_SERVICE)) {
				response = getEventsReq(anUtopiaReq);
			} else {
				log.warn("Unknown service " + service);
				response = createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
			}
		} catch (UtopiaException ue) {
			log.error("Negative response for service=" + service + "; exception:" + ue.getMessage());
			response = createNegativeResponse(service, ue.getErrorCode(), "Error in request: " + ue.getMessage());
		} catch (Throwable t) {
			log.error("Unexpected error in service : " + service, t);
			response = createNegativeResponse(service, ErrorCode.__6005_Unexpected_error, "error " + t);
		}

		if (getProperty("verbose").equals("true")) {
			log.info("Handled service=" + service + " response=" + response.getTag() + " in " + (Sys.now() - t1) + " ms");
			log.info(new String(response.toBytes(false)));
		}
		return new UtopiaResponse(response);
	}

	public JXElement addMediumReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
/*
        <play-add-medium-req id="[mediumid]" />
        <play-add-medium-rsp locationid="[locationid]" taskresultid="[taskresultid]" />
*/
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		HandlerUtil.throwOnNonNumAttr(requestElement, ID_FIELD);
		int mediumId = requestElement.getIntAttr(ID_FIELD);
		long timestamp = -1;
		if (requestElement.hasAttr(TIME_FIELD)) {
			// if a timestamp was provided we assume we already have the correct creation time
			timestamp = requestElement.getLongAttr(TIME_FIELD);
		}
		JXElement response = createLogic(anUtopiaReq).addMedium(mediumId, timestamp);
		response.setTag(Protocol.createResponse(PLAY_ADD_MEDIUM_SERVICE).getTag());
		return response;
	}

	public JXElement answerTaskReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
/*
        <play-answertask-req id="[taskid]" answer="blabla" />
        <play-answertask-rsp result="[boolean]" score="[nrofpoints] />
*/
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		HandlerUtil.throwOnNonNumAttr(requestElement, ID_FIELD);
		HandlerUtil.throwOnMissingAttr(requestElement, ANSWER_FIELD);

		// Assemble required vars
		int taskId = requestElement.getIntAttr(ID_FIELD);
		String playerAnswer = requestElement.getAttr(ANSWER_FIELD);
		int personId = HandlerUtil.getUserId(anUtopiaReq);

		JXElement response = createLogic(anUtopiaReq).answerTask(personId, taskId, playerAnswer);
		response.setTag(Protocol.createResponse(PLAY_ANSWERTASK_SERVICE).getTag());
		return response;

	}

	/**
	 * Get gameplay events.
	 * <p/>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement getEventsReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		String gamePlayIdStr = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr(ID_FIELD, gamePlayIdStr);
		int gamePlayId = Integer.parseInt(gamePlayIdStr);

		Vector events = WPQueryLogic.getGamePlayEvents(gamePlayId);
		JXElement response = createResponse(PLAY_GET_EVENTS_SERVICE);
		response.addChildren(events);
		return response;
	}

	public JXElement getGamePlayReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
/*
        <get-team-result-req id="[gameplayid]"  />

        <gameplay team="red2" score="123" state="running" trackid="456">
        	  <taskresult taskid="8787" state="done" answerstate="ok" mediastate="done" answer="fjkdaf" />
    	      <taskresult taskid="8890" state="hit" answerstate="notok" mediastate="done" answer="wrong" />
	          <mediumresult mediumid="341" state="open" />
              <mediumresult mediumid="879" state="hit" />
          	  <taskresult taskid="3256" state="open" answerstate="open" mediastate="open" />
	          <taskresult taskid="3256" state="open" answerstate="open" mediastate="open" />
        </gameplay>
*/
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		String gamePlayIdStr = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr(ID_FIELD, gamePlayIdStr);
		int gamePlayId = Integer.parseInt(gamePlayIdStr);

		JXElement gamePlayElm = WPQueryLogic.getResultForTeam(HandlerUtil.getAccountName(anUtopiaReq), gamePlayId);

		JXElement rsp = createResponse(PLAY_GET_GAMEPLAY_SERVICE);
		rsp.addChild(gamePlayElm);
		return rsp;
	}

	/**
	 * heartbeat.
	 * <p/>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement heartbeatReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		// EventPublisher.heartbeat(track, reqElm.getLongAttr(ATTR_T), anUtopiaReq);

		return createResponse(PLAY_HEARTBEAT_SERVICE);
	}

	public JXElement locationReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		int personId = HandlerUtil.getUserId(anUtopiaReq);
		Vector points = anUtopiaReq.getRequestCommand().getChildren();
		JXElement response = createLogic(anUtopiaReq).doLocation(personId, points);
		response.setTag(Protocol.createResponse(PLAY_LOCATION_SERVICE).getTag());
		return response;
	}

	public JXElement resetReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		HandlerUtil.throwOnNonNumAttr(requestElement, ID_FIELD);
		createLogic(anUtopiaReq).reset(HandlerUtil.getUserId(anUtopiaReq), requestElement.getIntAttr(ID_FIELD));
		return createResponse(PLAY_RESET_SERVICE);
	}

	public JXElement startReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		HandlerUtil.throwOnNonNumAttr(requestElement, ID_FIELD);
		int gamePlayId = requestElement.getIntAttr(ID_FIELD);

		createLogic(anUtopiaReq).start(HandlerUtil.getUserId(anUtopiaReq), gamePlayId);

		return createResponse(PLAY_START_SERVICE);
	}

	protected GamePlayLogic createLogic(UtopiaRequest anUtopiaReq) {
		return new GamePlayLogic(HandlerUtil.getOase(anUtopiaReq));
	}

}
