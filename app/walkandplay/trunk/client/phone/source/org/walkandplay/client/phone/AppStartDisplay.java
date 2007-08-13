package org.walkandplay.client.phone;

import de.enough.polish.ui.Form;

import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Display;

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

    protected void connect() {
        try {
            tcpClient = TCPClient.getInstance();
            tcpClient.start(midlet.getKWServer(), midlet.getKWPort());
            tcpClient.addListener(this);
            tcpClient.login(midlet.getKWUser(), midlet.getKWPassword());
        } catch (Throwable t) {
            deleteAll();
            addCommand(BACK_CMD);
            //#style alertinfo
            append("We can not connect. Please check your account settings.");
        }
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

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append("Oops, we lost our connection. Please go back and try again.");
    }
}
