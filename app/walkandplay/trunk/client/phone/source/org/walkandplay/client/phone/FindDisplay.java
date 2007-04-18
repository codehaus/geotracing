package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Net;
import org.geotracing.client.NetListener;
import org.geotracing.client.Preferences;

import java.util.Vector;
import java.util.Hashtable;

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
    private String gameName;
    private JXElement gameElm;
    private MIDlet midlet;
    private Image logo;

    public FindDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "");
        try{
            //#ifdef polish.images.directLoad
            logo = Image.createImage("/find_icon_small.png");
            //#else
            logo = scheduleImage("/find_icon_small.png");
            //#endif
        }catch(Throwable t){
            Log.log("Could not load the images on FindDisplay");
        }

        append(logo);

        midlet = aMIDlet;

        Net net = Net.getInstance();
        if(!net.isConnected()){
            net.setProperties(aMIDlet);
            net.setListener(this);
            net.start();
        }

        // get the games
        try{
            JXElement req = new JXElement("query-store-req");
            req.setAttr("cmd", "q-schedule-by-user");
            //req.setAttr("user", new Preferences(Net.RMS_STORE_NAME).get(Net.PROP_USER, midlet.getAppProperty(Net.PROP_USER)));
            req.setAttr("user", new Preferences(Net.RMS_STORE_NAME).get(Net.PROP_USER, "red2"));
            JXElement rsp = net.utopiaReq(req);
            System.out.println(new String(rsp.toBytes(false)));
            if(rsp!=null) {
                Vector elms = rsp.getChildrenByTag("record");
                for(int i=0;i<elms.size();i++){
                    JXElement elm = (JXElement)elms.elementAt(i);
                    String name = elm.getChildText("name");
                    //#style formbox
                    gamesGroup.append(name, null);
                    games.put(name, elm);
                }
            }
        }catch(Throwable t){
            System.out.println(t.getMessage());
        }


        //#style smallstring
        append("Select a game and press Ok in menu");
        //#style formbox
        append(gamesGroup);
        addCommand(OK_CMD);
    }

    public void onNetInfo(String theInfo){
        System.out.println(theInfo);
    }

	public void onNetError(String aReason, Throwable anException){
        System.out.println(aReason);
    }

	public void onNetStatus(String aStatusMsg){
        System.out.println(aStatusMsg);
    }

    /*
       * The commandAction method is implemented by this midlet to
       * satisfy the CommandListener interface and handle the Cancel action.
       */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == OK_CMD) {
            gameName = gamesGroup.getString(gamesGroup.getSelectedIndex());
            gameElm = (JXElement) games.get(gameName);
		}
    }


}
