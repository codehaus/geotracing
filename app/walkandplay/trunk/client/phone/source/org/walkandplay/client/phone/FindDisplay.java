package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Net;
import org.geotracing.client.NetListener;
import org.geotracing.client.Preferences;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import java.util.Hashtable;
import java.util.Vector;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: FindDisplay.java 254 2007-01-11 17:13:03Z just $
 */
public class FindDisplay extends DefaultDisplay implements NetListener {
    private Command OK_CMD = new Command(Locale.get("find.Ok"), Command.ITEM, 2);
    private ChoiceGroup gamesGroup = new ChoiceGroup("", ChoiceGroup.EXCLUSIVE);
    private Hashtable games = new Hashtable(2);
    private MIDlet midlet;
    private Image logo;

    public FindDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "Find a game");
        try {
            //#ifdef polish.images.directLoad
            logo = Image.createImage("/find_icon_small.png");
            //#else
            logo = scheduleImage("/find_icon_small.png");
            //#endif
        } catch (Throwable t) {
            Log.log("Could not load the images on FindDisplay");
        }

        midlet = aMIDlet;

        Net net = Net.getInstance();
        if (!net.isConnected()) {
            net.setProperties(aMIDlet);
            net.setListener(this);
            net.start();
        }

        if (!net.isConnected()) {
            // login must have failed!!!!
            //#style formbox
            append("Logging in has failed!! Please check your username and password under Settings/Account and try again.");
        } else {
            // get the games
            try {
                JXElement req = new JXElement("query-store-req");
                req.setAttr("cmd", "q-schedule-by-user");
                req.setAttr("user", new Preferences(Net.RMS_STORE_NAME).get(Net.PROP_USER, midlet.getAppProperty(Net.PROP_USER)));
                JXElement rsp = net.utopiaReq(req);
                Log.log(new String(rsp.toBytes(false)));
                Vector elms = rsp.getChildrenByTag("record");
                for (int i = 0; i < elms.size(); i++) {
                    JXElement elm = (JXElement) elms.elementAt(i);
                    String name = elm.getChildText("name");
                    //#style formbox
                    gamesGroup.append(name, null);
                    games.put(name, elm);
                }
            } catch (Throwable t) {
                Log.log(t.getMessage());
            }

            append(logo);

            //#style labelinfo
            append("Select a game and press Ok in menu");
            //#style formbox
            append(gamesGroup);
            addCommand(OK_CMD);
        }
    }

    public void onNetInfo(String theInfo) {
        Log.log(theInfo);
    }

    public void onNetError(String aReason, Throwable anException) {
        Log.log(aReason);
    }

    public void onNetStatus(String aStatusMsg) {
        Log.log(aStatusMsg);
    }

    /*
       * The commandAction method is implemented by this midlet to
       * satisfy the CommandListener interface and handle the Cancel action.
       */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == OK_CMD) {
            String gameName = gamesGroup.getString(gamesGroup.getSelectedIndex());
            JXElement gameElm = (JXElement) games.get(gameName);
        }
    }


}
