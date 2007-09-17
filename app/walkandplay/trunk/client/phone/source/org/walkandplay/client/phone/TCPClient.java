package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.SocketXMLChannel;
import nl.justobjects.mjox.XMLChannel;
import nl.justobjects.mjox.XMLChannelListener;
import org.keyworx.mclient.ClientException;
import org.keyworx.mclient.Protocol;
import org.walkandplay.client.phone.Log;
import org.walkandplay.client.phone.WPMidlet;

import java.util.Vector;

/**
 * Basic TCP Client.
 *
 * @author Ronald Lenz
 * @version $Id: TCPClient.java,v 1.3 2006/08/04 12:28:26 just Exp $
 * @see org.keyworx.mclient.HTTPMidlet
 */

public class TCPClient extends Protocol implements XMLChannelListener {

    public final static String DISCONNECTED = "disconnected";
    public final static String CONNECTING = "connecting";
    public final static String CONNECTED = "connected";
    private String STATE = DISCONNECTED;

    private WPMidlet midlet;

    private JXElement lastRequest;

    /**
     * Key gotten on login ack
     */
    private String agentKey;


    private XMLChannel xmlChannel;
    private Vector listeners = new Vector(3);
    private static final TCPClient instance = new TCPClient();

    private int panicCounter;
    private int MAX_PANIC = 3;

    private TCPClient() {

    }

    public static TCPClient getInstance() {
        return instance;
    }

    public void start(WPMidlet aMIDlet) throws ClientException {
        try {
            midlet = aMIDlet;
            panicCounter = 0;

            if(xmlChannel!=null){
                xmlChannel.stop();
                xmlChannel = null;
            }
            xmlChannel = new SocketXMLChannel(midlet.getKWServer(), midlet.getKWPort());
            xmlChannel.start();
            xmlChannel.setListener(this);

            login();
            
        } catch (Throwable t) {
            throw new ClientException("Could not connect to " + midlet.getKWServer() + " at port " + midlet.getKWPort());
        }
    }

    synchronized public void stop() {
        // first let all listeners know we have are disconnecting
        broadCastNetStatus(DISCONNECTED);

        // remove all listeners
        listeners = new Vector(3);

        /*try {
            logout();
        } catch (Throwable t) {
            // nada - we stop anyway
        }*/

        exit();
    }

    synchronized private void exit() {
        STATE = DISCONNECTED;

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
        String tag = aResponse.getTag();
        if (tag.equals("login-rsp")) {
            try {
                setAgentKey(aResponse);
                selectApp();
            } catch (Throwable t) {
                Log.log("Selectapp failed:" + t.getMessage());
                broadCastFatal();
            }
        }else if(aResponse.getTag().equals("select-app-rsp")){
            STATE = CONNECTED;
            // no (more) panic - reset the counter
            panicCounter = 0;

            try{
                if(lastRequest!=null){
                    // first re-issue previous request
                    doRequest(lastRequest);
                    lastRequest = null;
                }else{
                    // only broadcast connected state when there is NO pending request
                    broadCastConnected();
                    broadCastNetStatus(CONNECTED);
                }
            }catch(Throwable t){

            }
        }else if (tag.equals("login-nrsp")) {
            broadCastError("Invalid username and/or password. Please check your settings.");
        }else if (tag.equals("select-app-nrsp")) {
            broadCastFatal();
        }else{
            for (int i = 0; i < listeners.size(); i++) {
                ((TCPClientListener) listeners.elementAt(i)).accept(anXMLChannel, aResponse);
            }
        }
    }

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        STATE = DISCONNECTED;

        try{
            restart();
        }catch(Throwable t){
            //
        }
    }

    synchronized public void setAgentKey(JXElement aLoginResponse) {
        agentKey = aLoginResponse.getAttr("agentkey");
    }

    synchronized public String getAgentKey() {
        return agentKey;
    }

    synchronized public String getState() {
        return STATE;
    }

    private void broadCastConnected(){
        for (int i = 0; i < listeners.size(); i++) {
            ((TCPClientListener) listeners.elementAt(i)).onConnected();
        }
    }

    private void broadCastNetStatus(String aNetStatus){
        for (int i = 0; i < listeners.size(); i++) {
            ((TCPClientListener) listeners.elementAt(i)).onNetStatus(aNetStatus);
        }
    }

    private void broadCastError(String anErrorMessage){
        for (int i = 0; i < listeners.size(); i++) {
            ((TCPClientListener) listeners.elementAt(i)).onError(anErrorMessage);
        }
    }

    private void broadCastFatal(){
        for (int i = 0; i < listeners.size(); i++) {
            ((TCPClientListener) listeners.elementAt(i)).onFatal();
        }
    }

    /**
     * Login on portal.
     */
    synchronized public void login() throws ClientException {
        agentKey = null;
        STATE = CONNECTING;

        // Create XML request
        JXElement request = createRequest(SERVICE_LOGIN);
        request.setAttr(ATTR_NAME, midlet.getKWUser());
        request.setAttr(ATTR_PASSWORD, midlet.getKWPassword());
        request.setAttr(ATTR_PROTOCOLVERSION, PROTOCOL_VERSION);

        // Execute request
        doRequest(request);
    }

    /**
     * Select application on portal.
     */
    synchronized public void selectApp() throws ClientException {
        if(!STATE.equals(CONNECTING)){
            throw new ClientException("Start TCPClient first");
        }

        // Create XML request
        JXElement request = createRequest(SERVICE_SELECT_APP);
        request.setAttr(ATTR_APPNAME, midlet.getKWApp());
        request.setAttr(ATTR_ROLENAME, midlet.getKWRole());

        // Execute request
        doRequest(request);
    }

    /**
     * Utopia service.
     */
    synchronized public void utopia(JXElement aHandlerRequest) throws ClientException {
        if(STATE.equals(DISCONNECTED)){
            throw new ClientException("Start TCPClient first");
        }

        // Wrap Handler request with <utopia-req> tag
        JXElement request = createRequest(SERVICE_UTOPIA);
        request.addChild(aHandlerRequest);

        // Execute request
        doRequest(request);
    }

    synchronized private void restart() throws ClientException{
        if(panicCounter == MAX_PANIC){
            broadCastFatal();
            exit();
        }else{
            panicCounter++;
            stop();
            start(midlet);
        }
    }

    /**
     * Logout from portal.
     */
    synchronized private void logout() throws ClientException {
        // Create XML request
        JXElement request = createRequest(SERVICE_LOGOUT);

        // Execute request
        doRequest(request);
    }

    /**
     * Do XML over HTTP request and retun response.
     */
    private void doRequest(JXElement aRequest) throws ClientException {
        try {
            Log.log("** sending " + new String(aRequest.toBytes(false)));
            xmlChannel.push(aRequest);
            broadCastNetStatus(CONNECTED);
        } catch (Throwable t) {
            // store the request
            lastRequest = aRequest;

            // restart the tcpclient
            restart();

            Log.log("Exception sending " + new String(aRequest.toBytes(false)) + ":" + t.getMessage());
        }
    }


}
