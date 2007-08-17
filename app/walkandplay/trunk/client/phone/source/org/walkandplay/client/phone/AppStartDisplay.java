package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;

public class AppStartDisplay extends DefaultDisplay implements TCPClientListener {

    protected TCPClient tcpClient;

    public AppStartDisplay(WPMidlet aMIDlet, String aDisplayTitle) {
        super(aMIDlet, aDisplayTitle);
    }

    public void addTCPClientListener(TCPClientListener aListener) {
        tcpClient.addListener(aListener);        
    }

    public void removeTCPClientListener(TCPClientListener aListener) {
        tcpClient.removeListener(aListener);
    }

    public TCPClient getTCPClient() {
        return tcpClient;
    }

    public void exit(){
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append("Oops a fatal error. Please try again. If this error happens repeatedly, please re-install");
    }

    protected void connect() {
        try {
            tcpClient = TCPClient.getInstance();
            tcpClient.addListener(this);
            tcpClient.start(midlet);
        } catch (Throwable t) {
            deleteAll();
            addCommand(BACK_CMD);
            //#style alertinfo
            append("We can not connect. Please check your account settings.");
        }
    }

    public void onNetStatus(String aStatus){

    }

    public void onConnected(){

    }

    public void onError(String anErrorMessage){

    }

    public void onFatal(){
        
    }

    public void sendRequest(JXElement aRequest) {
        try {
            tcpClient.utopia(aRequest);
        } catch (Throwable t) {
            Log.log("Exception sending " + new String(aRequest.toBytes(false)));            
        }
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        
    }

}
