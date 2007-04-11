package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Net;
import org.geotracing.client.NetListener;

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
    private ChoiceGroup toursGroup = new ChoiceGroup("Select a tour and press Ok in menu", ChoiceGroup.EXCLUSIVE);
    private Hashtable tours = new Hashtable(2);
    private String tourName;
    private JXElement tourElm;
    private MIDlet midlet;

    public FindDisplay(MIDlet aMIDlet) {
        super(aMIDlet, "Find a game!!");
        midlet = aMIDlet;

        Net net = Net.getInstance();
        if(!net.isConnected()){
            net.setProperties(aMIDlet);
            net.setListener(this);
            net.start();
        }

        // get the tours
        JXElement getMyToursReq = new JXElement("schedule-getlist-req");
        JXElement getMyToursRsp = net.utopiaReq(getMyToursReq);
        if(getMyToursRsp!=null) {
            Vector toursElms = getMyToursRsp.getChildrenByTag("schedule");
            for(int i=0;i<toursElms.size();i++){
                JXElement t = (JXElement)toursElms.elementAt(i);
                String name = t.getChildText("name");
                toursGroup.append(name, null);
                tours.put(name, t);
            }
        }

        //#style formbox
        append(toursGroup);
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
            tourName = toursGroup.getString(toursGroup.getSelectedIndex());
            tourElm = (JXElement) tours.get(tourName);
		}
    }


}
