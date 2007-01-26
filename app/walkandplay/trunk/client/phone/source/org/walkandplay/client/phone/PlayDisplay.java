package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;

import nl.justobjects.mjox.XMLChannel;
import nl.justobjects.mjox.SocketXMLChannel;
import nl.justobjects.mjox.XMLChannelListener;
import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Net;
import org.geotracing.client.Preferences;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
public class PlayDisplay extends DefaultDisplay implements XMLChannelListener, Runnable {
    MIDlet midlet;
    List menuScreen;
    private XMLChannel xmlChannel;
    public String space, url, port;
    private String loginReq, enterSpaceReq;

    Command PLAY_CMD = new Command(Locale.get("play.Play"), Command.ITEM, 2);

    StringItem text = new StringItem("", "Press 'Play' to start your tour");
        
    public PlayDisplay(MIDlet aMIDlet) {
        super(aMIDlet, "");

        //#style formbox
        append(text);
        addCommand(PLAY_CMD);

        try{
        Preferences prefs = new Preferences(Net.RMS_STORE_NAME);

        String user = prefs.get(Net.PROP_USER, midlet.getAppProperty(Net.PROP_USER));
        String password = prefs.get(Net.PROP_PASSWORD, midlet.getAppProperty(Net.PROP_PASSWORD));
        url = prefs.get(Net.PROP_URL, midlet.getAppProperty(Net.PROP_URL));
        port = midlet.getAppProperty("kw-port");
        String app = midlet.getAppProperty("kw-app");
        String amulet = midlet.getAppProperty("kw-amulet");
		String role = midlet.getAppProperty("kw-role");

        loginReq = "<login-req protocolversion=\"4.0\" name=\"" + user + "\" password=\"" + password + "\" />";
        loginReq += "<select-app-req appname=\"" + app + "\"" + role + "=\"user\" />";
        enterSpaceReq += "<enter-space-req spacename=\"" + space + "\" />";
        enterSpaceReq += "<join-amulet-req id=\"" + amulet + "\" ></join-amulet-req>";

        }catch(Throwable t){

        }
    }

    /**
     * Sets up a XML channel and logs in.
     * login-req, select-app, enter-space and join-amulet all in one go.
     */
    void connect() {
        try {
            //log("connecting to " + server + ":" + port);
            xmlChannel = new SocketXMLChannel(url, Integer.parseInt(port));
            xmlChannel.setListener(this);
            xmlChannel.start();

            sendXMLRequest(loginReq);

        } catch (Throwable t) {
            Log.log("Connect exception : " + t.toString());
        }
    }

    /**
     * Notifies if the XML channel has stopped. Now we automaticallt reconnect.
     * @param anXMLChannel
     * @param aReason
     */
    public void onStop(XMLChannel anXMLChannel, String aReason) {
        connect();
    }
    /**
     * Handles incoming xml messages from the xml channel.
     * @param anXMLChannel The xml socket connection.
     * @param element The xml message.
     */
    public void accept(XMLChannel anXMLChannel, JXElement element) {
        //log("received:" + new String(element.toBytes(false)));
        if (element != null) {
            String tag = element.getTag();
            if (tag.equals("game-getspace-rsp")) {
                space = element.getAttr("name");
                sendXMLRequest(enterSpaceReq);
            }
        }
    }

    /**
     * Sends a XML request to the server.
     * @param aRequest The request element.
     */
    public void sendXMLRequest(JXElement aRequest) {
        try {
            Log.log("send:" + new String(aRequest.toBytes(false)));
            xmlChannel.push(aRequest);
        } catch (Throwable t) {
            Log.log("sendXMLRequest exception : " + t.toString());
            connect();
        }
    }

    /**
     * Sends a XML request to the server.
     * @param aRequest The request string.
     */
    public void sendXMLRequest(String aRequest) {
        try {
            xmlChannel.push(aRequest.getBytes());
        } catch (Throwable t) {
            Log.log("sendXMLRequest exception : " + t.toString());
            connect();
        }
    }

    /**
     * The midlet run method. Not implemented.
     */
    public void run() {

    }



    /*
       * The commandAction method is implemented by this midlet to
       * satisfy the CommandListener interface and handle the Cancel action.
       */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == PLAY_CMD) {

        }
    }


}
