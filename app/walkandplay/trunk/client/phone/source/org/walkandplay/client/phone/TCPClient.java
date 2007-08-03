package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.SocketXMLChannel;
import nl.justobjects.mjox.XMLChannel;
import nl.justobjects.mjox.XMLChannelListener;
import org.keyworx.mclient.ClientException;
import org.keyworx.mclient.Protocol;
import org.keyworx.mclient.ProtocolException;

/**
 * Basic KeyWorx client using XML over HTTP.
 * <p/>
 * Use this class within J2ME MIDlets. Should work at least
 * for MIDP2, and possibly MIDP1.
 *
 * @author Just van den Broecke
 * @version $Id: TCPClient.java,v 1.3 2006/08/04 12:28:26 just Exp $
 * @see org.keyworx.mclient.HTTPMidlet
 */

public class TCPClient extends Protocol {

    /**
     * Default KW session timeout (minutes).
     */
    public static final int DEFAULT_TIMEOUT_MINS = 5;

    /**
     * Debug flag for verbose output.
     */
    private boolean debug;

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

    /**
     * Constructor with full protocol URL e.g. http://www.bla.com/proto.srv.
     */
    public TCPClient(String aServer, int aPort) throws ClientException {
        try {
            xmlChannel = new SocketXMLChannel(aServer, aPort);
            xmlChannel.start();
        } catch (Throwable t) {
            throw new ClientException("Could not connect to " + aServer + " at port " + aPort);
        }
    }

    synchronized public void setListener(XMLChannelListener aListener) {
        xmlChannel.setListener(aListener);
    }

    synchronized public void setAgentKey(JXElement aLoginResponse) {
        agentKey = aLoginResponse.getAttr("agentkey");
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

    public void setDebug(boolean b) {
        debug = b;
    }

    synchronized public void restart() throws ClientException {
        if (xmlChannel != null) {
            xmlChannel.stop();
            xmlChannel = null;
        }
        doRequest(loginRequest);
    }

    /**
     * Throw exception on negative protocol response.
     */
    private void throwOnNrsp(JXElement anJXElement) throws ProtocolException {
        if (isNegativeResponse(anJXElement)) {
            String details = "no details";
            if (anJXElement.hasAttr(ATTR_DETAILS)) {
                details = anJXElement.getAttr(ATTR_DETAILS);
            }
            throw new ProtocolException(anJXElement.getIntAttr(ATTR_ERRORID),
                    anJXElement.getAttr(ATTR_ERROR), details);
        }
    }

    /**
     * Throw exception when not logged in.
     */
    private void throwOnInvalidSession() throws ClientException {
        if (agentKey == null) {
            /*throw new ClientException("Invalid keyworx session");*/
            restart();
        }
    }

    /**
     * Do XML over HTTP request and retun response.
     */
    private void doRequest(JXElement anJXElement) throws ClientException {
        try {
            System.out.println("Sending " + new String(anJXElement.toBytes(false)));
            xmlChannel.push(anJXElement);
        } catch (Throwable t) {
            System.out.println("Exception sending " +
                    new String(anJXElement.toBytes(false)) + ":" + t.getMessage());
            restart();
        }
    }


    /**
     * Util: print.
     */
    private void p(String s) {
        if (debug) {
            System.out.println("[TCPClient] " + s);
        }
    }

    /**
     * Util: warn.
     */
    private void warn(String s) {
        warn(s, null);
    }

    /**
     * Util: warn with exception.
     */
    private void warn(String s, Throwable t) {
        System.err.println("[TCPClient] - WARN - " + s + " ex=" + t);

        if (t != null) {
            t.printStackTrace();
        }
    }

}
