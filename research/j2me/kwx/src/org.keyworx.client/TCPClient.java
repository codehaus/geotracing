// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.client.kwx;


import nl.justobjects.mjox.*;
import org.geotracing.client.Log;
import org.keyworx.mclient.ClientException;
import org.keyworx.mclient.Protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Basic implementation of KeyWorx client protocol lib.
 *
 * */
public class TCPClient implements XMLChannelListener {
	public static final long RESPONSE_TIMEOUT = 40000;
	XMLChannel xmlChannel;
	JXElement lastResponse;
	volatile boolean responseReceived = false;
	String serverHost;
	int serverPort;
	String spaceName;
	String agentKey;
	JXElement myAgent;
	Hashtable agents = new Hashtable(5);
	SpaceListener spaceListener;
	SubscriptionListener subscriptionListener;
	private boolean debug = true;

	public TCPClient() {
	}

	public void setDebug(boolean b) {
		debug = b;
	}

	/** Connect to server using TCP host/port. */
	public void connect(String aHost, int aPort) throws ClientException {
		serverHost = aHost;
		serverPort = aPort;

		try {
			xmlChannel = new SocketXMLChannel(serverHost, serverPort);
		} catch (IOException e) {
			throw new ClientException(e);
		}

		startXMLChannel();
	}

	/** Connect with arbitrary XML channel. */
	public void connectXMLChannel(InputStream anInputStream, OutputStream anOutputStream) throws ClientException {
		xmlChannel = new XMLChannelImpl(anInputStream, anOutputStream);
		startXMLChannel();
	}


	public boolean isConnected() {
		return xmlChannel != null;
	}

	/** Check if session is valid by submitting an echo to the server. */
	public boolean isSessionValid() {
		// No use if we are not even connected.
		if (!isConnected()) {
			return false;
		}

		boolean result = false;

		try {
			// Submit echo-req and expect echo-rsp
			JXElement echoReq = Protocol.createRequest("echo");
			JXElement response = performRequest(echoReq);
			lastResponse = response;
			result = response.getTag().equals("echo-rsp");
		} catch (Throwable t) {
			Log.log("Error on sending echo-req " + t);
			result = false;
		}

		// Disconnect in all cases where we could not do an echo
		if (!result) {
			disconnect();
		}

		return result;
	}

	public JXElement enterSpace(String aSpaceName, SpaceListener theSpaceListener) throws ClientException {
		spaceListener = theSpaceListener;
		if (spaceListener == null) {
			spaceListener = new DefaultSpaceListener();
		}

		JXElement enterCommand = new JXElement(Protocol.TAG_ENTER_REQ);
		enterCommand.setAttr("spacename", aSpaceName);

		JXElement response = performRequest(enterCommand);
		String tag = response.getTag();
		lastResponse = response;
		if (tag.equals(Protocol.TAG_ENTER_RSP)) {
			myAgent.setAttr("spacename", aSpaceName);
			spaceName = aSpaceName;
			JXElement spaceState = response.getChildByTag("space-state");
			JXElement agentList = spaceState.getChildByTag("agentlist");
			Vector agentsInSpace = agentList.getChildren();
			for (int i = 0; i < agentsInSpace.size(); i++) {
				JXElement nextAgent = (JXElement) agentsInSpace.elementAt(i);
				agents.put(nextAgent.getId(), nextAgent);
			}

			return response;
		} else if (tag.equals(Protocol.TAG_ENTER_NRSP)) {
			return response;
		} else {
			throw new ClientException("unexpected response received " +  response);
		}
	}

	public JXElement exitSpace() throws ClientException {
		JXElement exitCommand = new JXElement(Protocol.TAG_EXIT_REQ);

		JXElement response = performRequest(exitCommand);
		String tag = response.getTag();

		if (tag.equals(Protocol.TAG_EXIT_RSP)) {
			myAgent.removeAttr("spaceid");
			return response;
		} else if (tag.equals(Protocol.TAG_EXIT_NRSP)) {
			return response;
		} else {
			throw new ClientException("unexpected response received " + response);
		}
	}

	public JXElement getMyAgent() {
		return myAgent;
	}

	public String getMyAgentId() {
		return myAgent.getId();
	}

	public String getMyAgentKey() {
		return agentKey;
	}

	public JXElement[] getAgents() {
		return null; // (JXElement[]) agents.values().toArray(new JXElement[agents.size()]);
	}

	public JXElement joinAmulet(String amuletId) throws ClientException {
		return joinAmulet(amuletId, null);
	}

	public JXElement joinAmulet(String amuletId, JXElement joinOptions) throws ClientException {
		JXElement joinCommand = Protocol.createRequest(Protocol.SERVICE_JOIN);
		joinCommand.setId(amuletId);

		// Add optional extra attributes
		if (joinOptions != null) {
			joinCommand.addChild(joinOptions);
		}
		JXElement response = performRequest(joinCommand);

		String tag = response.getTag();

		if (tag.equals(Protocol.TAG_JOIN_RSP)) {
			// Set participation status for this amulet in the agent
			JXElement amuletElement = new JXElement("amulet");
			amuletElement.setId(amuletId);
			myAgent.addChild(amuletElement);
			return response.getChildAt(0);
		} else if (tag.equals(Protocol.TAG_JOIN_NRSP)) {
			Log.log(Protocol.TAG_JOIN_NRSP + response.toString());
			return response;
		} else {
			Log.log("unexpected response: " + response.toString());
			throw new ClientException("unexpected response received " + response);
		}
	}

	public JXElement leaveAmulet(String amuletId) throws ClientException {
		JXElement leaveCommand = Protocol.createRequest(Protocol.SERVICE_LEAVE);
		leaveCommand.setId(amuletId);

		JXElement response = performRequest(leaveCommand);

		String tag = response.getTag();

		if (tag.equals(Protocol.TAG_LEAVE_RSP)) {
			// Set participation status for this amulet in the agent
			JXElement amuletElement = new JXElement("amulet");
			amuletElement.setId(amuletId);
			// myAgent.removeChildByExample(amuletElement);
			return response;
		} else if (tag.equals(Protocol.TAG_LEAVE_NRSP)) {
			Log.log(Protocol.TAG_LEAVE_NRSP + response.toString());
			return response;
		} else {
			Log.log("unexpected response: " + response.toString());
			throw new ClientException("unexpected response received " +  response);
		}
	}


	/** Login using agent key from existing session. */
	public JXElement loginByAgentKey(String anAgentKey) throws ClientException {
		JXElement loginCommand = new JXElement(Protocol.TAG_LOGIN_REQ);
		loginCommand.setAttr("agentkey", anAgentKey);
		loginCommand.setAttr("protocolversion", "4.0");

		//System.out.println("$$$$$$$ clienthelper login start");
		JXElement response = performRequest(loginCommand);
		//System.out.println("$$$$$$$ clienthelper login done : " + response.toString());
		String tag = response.getTag();
		lastResponse = response;
		if (tag.equals(Protocol.TAG_LOGIN_NRSP)) {
			return response;
		} else if (tag.equals(Protocol.TAG_LOGIN_RSP)) {
			myAgent = response.getChildByTag("agent");
			// This is a new agent key
			agentKey = response.getAttr("agentkey");
			return response;
		} else {
			throw new ClientException("unexpected response received  " + response);
		}
	}

	/** Login using account session key . */
	public JXElement loginBySessionKey(String aSessionKey) throws ClientException {
		JXElement loginCommand = new JXElement(Protocol.TAG_LOGIN_REQ);
		loginCommand.setAttr("key", aSessionKey);
		loginCommand.setAttr("protocolversion", "4.0");

		//System.out.println("$$$$$$$ clienthelper login start");
		JXElement response = performRequest(loginCommand);
		//System.out.println("$$$$$$$ clienthelper login done : " + response.toString());
		String tag = response.getTag();
		lastResponse = response;
		if (tag.equals(Protocol.TAG_LOGIN_NRSP)) {
			return response;
		} else if (tag.equals(Protocol.TAG_LOGIN_RSP)) {
			myAgent = response.getChildByTag("agent");
			agentKey = response.getAttr("agentkey");
			return response;
		} else {
			throw new ClientException("unexpected response received " + response);
		}
	}

	/** Login using username/password. */
	public JXElement login(String aName, String aPassword) throws ClientException {
		JXElement loginCommand = new JXElement(Protocol.TAG_LOGIN_REQ);
		loginCommand.setAttr("name", aName);
		loginCommand.setAttr("password", aPassword);
		loginCommand.setAttr("protocolversion", "4.0");

		//System.out.println("$$$$$$$ clienthelper login start");
		JXElement response = performRequest(loginCommand);
		//System.out.println("$$$$$$$ clienthelper login done : " + response.toString());
		String tag = response.getTag();
		lastResponse = response;
		if (tag.equals(Protocol.TAG_LOGIN_NRSP)) {
			return response;
		} else if (tag.equals(Protocol.TAG_LOGIN_RSP)) {
			myAgent = response.getChildByTag("agent");
			agentKey = response.getAttr("agentkey");
			return response;
		} else {
			throw new ClientException("unexpected response received " + response);
		}
	}


	public JXElement selectApp(String anApplicationName, String aRoleName) throws ClientException {
		JXElement selectAppCommand = new JXElement(Protocol.TAG_SELECT_APP_REQ);
		selectAppCommand.setAttr("appname", anApplicationName);
		if (aRoleName != null) {
			selectAppCommand.setAttr("rolename", aRoleName);
		}

		//System.out.println("$$$$$$$ clienthelper select app for " + anApplicationName + ";" + aRoleName);
		JXElement response = performRequest(selectAppCommand);
		String tag = response.getTag();
		lastResponse = response;
		if (tag.equals(Protocol.TAG_SELECT_APP_NRSP)) {
			throw new ClientException("negative response received error " +  response);
		} else if (tag.equals(Protocol.TAG_SELECT_APP_RSP)) {
			myAgent.addChild(response.getChildByTag("app-context"));
			return response;
		} else {
			throw new ClientException("unexpected response received " + response);
		}
	}

	public JXElement selectApp(String anApplicationName) throws ClientException {
		return selectApp(anApplicationName, null);
	}

	/**

	 <!-- Subscribe for events (e.g. insert/update/delete of Utopia Base objects) -->
	 <element tag="subscribe-object-req" comment="subscribe for events">
	 <element tag="object" comment="data element">
	 <attribute name="type" comment="the object type, e.g. Person or Medium"/>
	 <attribute name="id" optional="true" type="int" comment="unique data object id, e.g. 42"/>
	 </element>
	 </element>
	 */
	public String subscribeObject(String anObjectType, String anObjectId, SubscriptionListener aSubscriptionListener) throws ClientException {

		subscriptionListener = aSubscriptionListener;
		JXElement subscribeObjReq = Protocol.createRequest(Protocol.SERVICE_SUBSCRIBE_OBJECT);

		JXElement objElement = new JXElement("object");

		objElement.setAttr("type", anObjectType);
		if (anObjectId != null) {
			objElement.setAttr("id", anObjectId);
		}
		subscribeObjReq.addChild(objElement);

		JXElement response = performRequest(subscribeObjReq);
		String tag = response.getTag();
		lastResponse = response;
		if (tag.equals(Protocol.TAG_SUBSCRIBE_OBJECT_NRSP)) {
			throw new ClientException("negative response received error " +  response);
		} else if (tag.equals(Protocol.TAG_SUBSCRIBE_OBJECT_RSP)) {
			return response.getAttr("subscriptionid");
		} else {
			throw new ClientException("unexpected response received " +  response);
		}

	}

	/*
	<!-- Subscribe for events (e.g. insert/update/delete of relations between Utopia Base objects) -->
	<element tag="subscribe-relation-req" comment="subscribe for events">
		<element tag="relation" comment="relation between two objects">
			<attribute name="type1" optional="true" comment="the object type, e.g. Person or Medium"/>
			<attribute name="type2" optional="true" comment="the object type, e.g. Person or Medium"/>
			<attribute name="id1" type="int" optional="true" comment="unique data object id, e.g. 42"/>
			<attribute name="id2" type="int" optional="true" comment="unique data object id, e.g. 43"/>
			<attribute name="tag" optional="true" comment="relation tag"/>
		</element>
	</element>

	*/
	public String doSubscribeRelation(String anObjectType1, String anObjectType2, String anObjectId1, String anObjectId2, String aRelTag, SubscriptionListener aSubscriptionListener) throws ClientException {
		subscriptionListener = aSubscriptionListener;

		JXElement subscribeRelReq = Protocol.createRequest(Protocol.SERVICE_SUBSCRIBE_RELATION);

		JXElement relElement = new JXElement("relation");

		// All attrs are optional.
		if (anObjectType1 != null) {
			relElement.setAttr("type1", anObjectType1);
		}
		if (anObjectType2 != null) {
			relElement.setAttr("type2", anObjectType2);
		}
		if (anObjectId1 != null) {
			relElement.setAttr("id1", anObjectId1);
		}
		if (anObjectId2 != null) {
			relElement.setAttr("id2", anObjectId2);
		}
		if (aRelTag != null) {
			relElement.setAttr("tag", aRelTag);
		}

		subscribeRelReq.addChild(relElement);

		JXElement response = performRequest(subscribeRelReq);
		String tag = response.getTag();
		lastResponse = response;
		if (tag.equals(Protocol.TAG_SUBSCRIBE_RELATION_NRSP)) {
			throw new ClientException("negative response received error " +  response);
		} else if (tag.equals(Protocol.TAG_SUBSCRIBE_RELATION_RSP)) {
			return response.getAttr("subscriptionid");
		} else {
			throw new ClientException("unexpected response received " +  response);
		}
	}

	/** Unsubscribe from all. */
	public void unsubscribe() throws ClientException {
		unsubscribe(null);
	}

	public void unsubscribe(String aSubscriptionId) throws ClientException {
		JXElement unsubscribeReq = Protocol.createRequest(Protocol.SERVICE_UNSUBSCRIBE);

		// All attrs are optional.
		if (aSubscriptionId != null) {
			unsubscribeReq.setAttr("subscriptionid", aSubscriptionId);
		}

		JXElement response = performRequest(unsubscribeReq);
		String tag = response.getTag();
		lastResponse = response;
		if (tag.equals(Protocol.TAG_UNSUBSCRIBE_NRSP)) {
			throw new ClientException("negative response received error " +  response);
		} else if (tag.equals(Protocol.TAG_UNSUBSCRIBE_RSP)) {
			// OK
		} else {
			throw new ClientException("unexpected response received " + response);
		}
	}

	public JXElement getLastResponse() {
		return lastResponse;
	}

	public JXElement logout() throws ClientException {
		// If already logged out or never logged in
		if (myAgent == null) {
			throw new ClientException("client is not logged in");
		}

		JXElement logoutCommand = new JXElement(Protocol.TAG_LOGOUT_REQ);
		JXElement response = performRequest(logoutCommand);

		String tag = response.getTag();
		if (tag.equals(Protocol.TAG_LOGOUT_NRSP)) {
			throw new ClientException("negative response received error " +  response);
		} else if (tag.equals(Protocol.TAG_LOGOUT_RSP)) {
			myAgent = null;
			return response;
		} else {
			throw new ClientException("unexpected response received " + response);
		}
	}

	public void sendCommand(JXElement command) throws ClientException {
		if (!isConnected()) {
			throw new ClientException("client is not connected");
		}

		try {
			xmlChannel.push(command);
		} catch (XMLChannelException xce) {
			xmlChannel = null;
			throw new ClientException(xce);
		}
	}

	public void sendAmuletCommand(String amuletId, JXElement command) throws ClientException {
		JXElement amuletCommand = Protocol.createAmuletMessage(amuletId);
		amuletCommand.addChild(command);
		sendCommand(amuletCommand);
	}

	/** Incoming message from XML channel. */
	synchronized public void accept(XMLChannel anXMLChannel, JXElement command) {
		// p("received: " + command);
		if (command == null) {
			Log.log("command == null");
			return;
		}

		if (Protocol.isResponse(command) || Protocol.isAmuletResponse(command)) {
			notifyResponse(command);
		} else if (Protocol.isIndication(command) || Protocol.isAmuletIndication(command)) {
			handleIndication(command);
		} else if (Protocol.isAmuletMessage(command)) {
			handleIndication(command);
		} else {
			Log.log("unhandled message from server tag=" + command.getTag() + " cmd=" + command);
		}
	}

	synchronized public void onStop(XMLChannel anXMLChannel, String msg) {
		// spaceListener.onError(msg);

		xmlChannel = null;
	}

	/** Do synchronous request/response. */
	public JXElement performRequest(JXElement aRequestElement) throws ClientException {
		sendCommand(aRequestElement);
		return waitForResponse();
	}

	/** Do synchronous request/response to amulet. */
	public JXElement performAmuletRequest(String anAmuletId, JXElement aRequestElement) throws ClientException {
		sendAmuletCommand(anAmuletId, aRequestElement);
		JXElement response = waitForResponse();
		if (Protocol.isAmuletResponse(response)) {
			return response.getChildAt(0);
		} else {
			throw new ClientException("No real response from amulet (maybe old protocol?) " + response);
		}
	}

	/** Do synchronous request/response to Utopia. */
	public JXElement utopia(JXElement aRequestElement) throws ClientException {
		JXElement utopiaAmuseRequest = Protocol.createRequest(Protocol.SERVICE_UTOPIA);
		utopiaAmuseRequest.addChild(aRequestElement);
		sendCommand(utopiaAmuseRequest);
		JXElement response = waitForResponse();
		String responseTag = response.getTag();

		if (responseTag.equals(Protocol.TAG_UTOPIA_RSP)) {
			// Set participation status for this amulet in the agent
			return response.getChildAt(0);
		} else if (responseTag.equals(Protocol.TAG_UTOPIA_NRSP)) {
			return response.getChildAt(0);
		} else {
			throw new ClientException("unexpected response received " + response);
		}
	}

	private void startXMLChannel() throws ClientException {
		xmlChannel.setListener(this);

		try {
			xmlChannel.start();
		} catch (Exception e) {
			Log.log("Could not start xmlChannel e=" + e);
			disconnect();
			throw new ClientException(e);
		}

		p("KWClient connected to " + serverHost + ":" + serverPort);
	}

	public void disconnect() {
		if (xmlChannel == null) {
			return;
		}

		try {
			xmlChannel.setListener(null);
			xmlChannel.stop();
			p("KWClient disconnected ");
		} catch (Throwable t) {
			p("exception during disconnect()" + t);
		} finally {
			myAgent = null;
			xmlChannel = null;
		}
	}

	/** Handle incoming requests. */
	synchronized protected void handleRequest(JXElement aRequest) {
		if (aRequest.equals(Protocol.TAG_AMULET)) {
			String agentId = aRequest.getAttr("agentid");
			JXElement agent = (JXElement) agents.get(agentId);
			spaceListener.onAmuletRequest(agent, aRequest.getId(), aRequest.getChildAt(0));
		} else {
			p("handleRequest: unknown tag=" + aRequest.getTag());
		}
	}

	synchronized protected void handleIndication(JXElement anIndication) {
		try {
			String tag = anIndication.getTag();

			if (tag.equals(Protocol.TAG_ENTER_IND)) {
				JXElement agent = anIndication.getChildByTag("agent");

				agents.put(agent.getId(), agent);
				spaceListener.onEnterIndication(agent);
			} else if (tag.equals(Protocol.TAG_EXIT_IND)) {
				JXElement agent = (JXElement) agents.remove(anIndication.getAttr("agentid"));
				spaceListener.onExitIndication(agent, anIndication.getAttr("reason"));
			} else if (tag.equals(Protocol.TAG_JOIN_IND)) {
				String agentId = anIndication.getAttr("agentId");
				JXElement agent = (JXElement) agents.get(agentId);
				if (agent == null) {
					Log.log("agent not found uid=" + agentId);
				} else {
					agent.addChild(anIndication);
					spaceListener.onJoinIndication(agent, anIndication);
				}
			} else if (tag.equals(Protocol.TAG_LEAVE_IND)) {
				String amuletId = anIndication.getId();
				String agentId = anIndication.getAttr("agentId");
				JXElement agent = (JXElement) agents.get(agentId);
				if (agent != null) {
					JXElement joinCommand = new JXElement(Protocol.SERVICE_JOIN);
					joinCommand.setAttr("amulet", amuletId);
					// agent.removeChildByExample(joinCommand);
					spaceListener.onLeaveIndication(agent, amuletId);

				} else {
					p("agent not found id=" + agentId);
				}
			} else if (tag.equals(Protocol.TAG_AMULET)) {
				String agentId = anIndication.getAttr("agentid");
				String amuletId = anIndication.getAttr("id");
				JXElement agent = (JXElement) agents.get(agentId);
				spaceListener.onAmuletIndication(agent, amuletId, anIndication.getChildAt(0));
			} else if (tag.equals(Protocol.TAG_OBJECT_EVENT_IND)) {
				subscriptionListener.onObjectIndication(anIndication);
			} else if (tag.equals(Protocol.TAG_RELATION_EVENT_IND)) {
				subscriptionListener.onRelationIndication(anIndication);
			} else {
				p("Unknown indication");
			}
		} catch (Throwable t) {
			Log.log("Error on handling indication: " + anIndication.getTag() + " " + t);
		}
	}

	synchronized protected void notifyResponse(JXElement aResponse) {
		synchronized (this) {
			lastResponse = aResponse;
			responseReceived = true;
		}
		notifyAll();
	}

	synchronized protected JXElement waitForResponse() throws ClientException {
		p("waiting for response...");
		if (!responseReceived) {
			try {
				wait(RESPONSE_TIMEOUT);
			} catch (InterruptedException ie) {
			}
		}

		// Still no response received ?
		if (!responseReceived) {
			throw new ClientException("no response received");
		}

		// Assume response received.
		p("got response [" + lastResponse.getTag() + "]");
		synchronized (this) {
			JXElement response = lastResponse;
			lastResponse = null;
			responseReceived = false;
			return response;
		}
	}

	protected void p(String s) {
		if (debug) {
			System.out.println("KWClient: " + s);
		}
	}

}
