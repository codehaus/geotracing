package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.SocketXMLChannel;
import nl.justobjects.mjox.XMLChannel;
import nl.justobjects.mjox.XMLChannelListener;
import org.keyworx.mclient.ClientException;
import org.keyworx.mclient.Protocol;
import org.keyworx.mclient.ProtocolException;

import java.util.Vector;

/**
 * Basic TCP Client.
 *
 * @author Ronald Lenz
 * @version $Id: TCPClient.java,v 1.3 2006/08/04 12:28:26 just Exp $
 * @see org.keyworx.mclient.HTTPMidlet
 */

public class TCPClient extends Protocol implements XMLChannelListener {

    /**
     * Default KW session timeout (minutes).
     */
    public static final int DEFAULT_TIMEOUT_MINS = 5;

    /**
     * Key gotten on login ack
     */
    private String agentKey;

    /**
     * Saved login request for session restore on timeout.
     */
    private JXElement loginRequest;

    /**
     * Saved selectApp request for session restore on timeout.
     */
    private JXElement selectAppRequest;

    private XMLChannel xmlChannel;
    private Vector listeners = new Vector(3);
    private static final TCPClient instance = new TCPClient();

    private TCPClient() {

    }

    public static TCPClient getInstance() {
        return instance;
    }

    public void start(String aServer, int aPort) throws ClientException {
        try {
            xmlChannel = new SocketXMLChannel(aServer, aPort);
            xmlChannel.start();
            xmlChannel.setListener(this);
        } catch (Throwable t) {
            throw new ClientException("Could not connect to " + aServer + " at port " + aPort);
        }
    }

    synchronized public void stop() {
        try {
            logout();
        } catch (Throwable t) {
            // nada - we stop anyway
        }

        if (xmlChannel != null) {
            xmlChannel.stop();
            xmlChannel = null;
        }
    }


    synchronized public void addListener(TCPClientListener aListener) {
        listeners.addElement(aListener);
        Log.log("Added TCPClientListener # " + listeners.size());
    }

    synchronized public void removeListener(TCPClientListener aListener) {
        listeners.removeElement(aListener);
        Log.log("Removed TCPClientListener # " + listeners.size());
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        Log.log("** received:" + new String(aResponse.toBytes(false)));
        for (int i = 0; i < listeners.size(); i++) {
            ((TCPClientListener) listeners.elementAt(i)).accept(anXMLChannel, aResponse);
        }
    }

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        for (int i = 0; i < listeners.size(); i++) {
            ((TCPClientListener) listeners.elementAt(i)).onStop(anXMLChannel, aReason);
        }
    }

    synchronized public void setAgentKey(JXElement aLoginResponse) {
        agentKey = aLoginResponse.getAttr("agentkey");
    }

    synchronized public String getAgentKey() {
        return agentKey;
    }

    /**
     * Login on portal with portalname (no longer required).
     */
    synchronized public void login(String aName, String aPassword, String aPortal) throws ClientException {
        login(aName, aPassword);
    }

    /**
     * Login on portal.
     */
    synchronized public void login(String aName, String aPassword) throws ClientException {
        agentKey = null;

        // Create XML request
        JXElement request = createRequest(SERVICE_LOGIN);
        request.setAttr(ATTR_NAME, aName);
        request.setAttr(ATTR_PASSWORD, aPassword);
        request.setAttr(ATTR_PROTOCOLVERSION, PROTOCOL_VERSION);

        // Save for later session restore
        loginRequest = request;

        // Execute request
        doRequest(request);
    }

    /**
     * Select application on portal.
     */
    synchronized public void selectApp(String anAppName, String aRole) throws ClientException {
        throwOnInvalidSession();

        // Create XML request
        JXElement request = createRequest(SERVICE_SELECT_APP);
        request.setAttr(ATTR_APPNAME, anAppName);
        request.setAttr(ATTR_ROLENAME, aRole);

        // Save for later session restore
        selectAppRequest = request;

        // Execute request
        doRequest(request);

    }

    /**
     * Utopia service.
     */
    synchronized public void utopia(JXElement aHandlerRequest) throws ClientException {
        throwOnInvalidSession();

        // Wrap Handler request with <utopia-req> tag
        JXElement request = createRequest(SERVICE_UTOPIA);
        request.addChild(aHandlerRequest);

        // Execute request
        doRequest(request);
    }

    /**
     * Logout from portal.
     */
    synchronized public void logout() throws ClientException {
        throwOnInvalidSession();

        // Create XML request
        JXElement request = createRequest(SERVICE_LOGOUT);

        // Execute request
        doRequest(request);
    }

    /**
     * Throw exception when not logged in.
     */
    private void throwOnInvalidSession() throws ClientException {
        if (agentKey == null) {
            throw new ClientException("Invalid keyworx session");
        }
    }

    /**
     * Do XML over HTTP request and retun response.
     */
    private void doRequest(JXElement anJXElement) throws ClientException {
        try {
            Log.log("** sending " + new String(anJXElement.toBytes(false)));
            xmlChannel.push(anJXElement);
        } catch (Throwable t) {
            Log.log("Exception sending " + new String(anJXElement.toBytes(false)) + ":" + t.getMessage());
        }
    }


}
