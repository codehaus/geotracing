package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;
import de.enough.polish.ui.StringItem;

import nl.justobjects.mjox.XMLChannel;
import nl.justobjects.mjox.SocketXMLChannel;
import nl.justobjects.mjox.XMLChannelListener;
import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Net;
import org.geotracing.client.Preferences;
import org.geotracing.client.NetListener;

import java.util.Hashtable;
import java.util.Vector;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
public class PlayDisplay extends DefaultDisplay implements NetListener {
    private ChoiceGroup toursGroup = new ChoiceGroup("Select a game and press PLAY in the menu", ChoiceGroup.EXCLUSIVE);
    private Hashtable tours = new Hashtable(2);
    private String tourName;
    private JXElement tourElm;
    private WPMidlet midlet;
    private Net net;

    Command PLAY_CMD = new Command(Locale.get("play.Play"), Command.ITEM, 2);

    public PlayDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "Play");

        net = Net.getInstance();
        if(!net.isConnected()){
            net.setProperties(aMIDlet);
            net.setListener(this);
            net.start();
        }

        // get the play state
        JXElement req = new JXElement("play-getstate-req");
        JXElement rsp = net.utopiaReq(req);
        if(rsp!=null) {
            Vector toursElms = rsp.getChildrenByTag("tour");
            for(int i=0;i<toursElms.size();i++){
                JXElement t = (JXElement)toursElms.elementAt(i);
                String id = t.getAttr("id");
                String name = t.getAttr("name");
                String state = t.getAttr("state");
                String displayName = name + " | " + state;
                toursGroup.append(displayName, null);
                tours.put(displayName, t);
            }
        }

        //#style formbox
        append(toursGroup);
        addCommand(PLAY_CMD);
    }

    private void startTour(){
        JXElement req = new JXElement("play-start-req");
        req.setAttr("id", midlet.getCurrentTour().getAttr("id"));
        JXElement rsp = net.utopiaReq(req);
        Log.log(new String(rsp.toBytes(false)));
    }

    public void onNetInfo(String theInfo){
        Log.log(theInfo);
    }

	public void onNetError(String aReason, Throwable anException){
        Log.log(aReason);
    }

	public void onNetStatus(String aStatusMsg){
        Log.log(aStatusMsg);
    }

    /*
       * The commandAction method is implemented by this midlet to
       * satisfy the CommandListener interface and handle the Cancel action.
       */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == PLAY_CMD) {
            tourName = toursGroup.getString(toursGroup.getSelectedIndex());
            midlet.setCurrentTour((JXElement) tours.get(tourName));
            // now start the tour
            startTour();
            // now go to the MapRadarDisplay
            MapRadarDisplay d = new MapRadarDisplay(midlet);
            Display.getDisplay(midlet).setCurrent(d);
        }
    }


}
