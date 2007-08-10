package org.walkandplay.client.phone;

import de.enough.polish.ui.Form;

import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Display;

import nl.justobjects.mjox.JXElement;

public class DefaultAppDisplay extends DefaultDisplay {

    protected TCPClient tcpClient;

    public DefaultAppDisplay(WPMidlet aMIDlet, String aDisplayTitle) {
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

    public void sendRequest(JXElement aRequest) {
        try {
            tcpClient.utopia(aRequest);
        } catch (Throwable t) {
            Log.log("Exception sending " + new String(aRequest.toBytes(false)));            
        }
    }
}
