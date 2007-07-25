package org.walkandplay.client.phone;

import de.enough.polish.ui.Form;
import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Net;
import org.geotracing.client.NetListener;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Gauge;
import java.util.Vector;

public class ScoreDisplay extends DefaultDisplay implements NetListener {

    private Net net;
    private Vector scores;
    private Command CANCEL_CMD = new Command("Back", Command.CANCEL, 1);

    public ScoreDisplay(WPMidlet aMIDlet, int aMaxScore) {
        super(aMIDlet, "");

        midlet = aMIDlet;
        prevScreen = Display.getDisplay(midlet).getCurrent();

        // get the scores
        retrieveScores();

        //#style defaultscreen
        Form form = new Form("");
        // Create the TextBox containing the "Hello,World!" message
        for (int i = 0; i < scores.size(); i++) {
            JXElement r = (JXElement) scores.elementAt(i);
            String team = r.getChildText("team");
            String points = r.getChildText("points");

            //#style labelinfo
            form.append(team);
            //#style formbox
            form.append(new Gauge("", false, aMaxScore, Integer.parseInt(points)));
        }

        net = Net.getInstance();
        if (!net.isConnected()) {
            net.setProperties(midlet);
            net.setListener(this);
            net.start();
        }

        form.addCommand(CANCEL_CMD);
        form.setCommandListener(this);
        Display.getDisplay(midlet).setCurrent(form);
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

    private void retrieveScores() {
        try {
            new Thread(new Runnable() {
                public void run() {
                    JXElement req = new JXElement("query-store-req");
                    req.setAttr("cmd", "q-scores");
                    req.setAttr("gameid", midlet.getGamePlayId());
                    Log.log(new String(req.toBytes(false)));
                    JXElement rsp = net.utopiaReq(req);
                    scores = rsp.getChildrenByTag("record");
                    Log.log(new String(rsp.toBytes(false)));
                }
            }).start();
        } catch (Throwable t) {
            Log.log("Exception in retrieveScores:\n" + t.getMessage());
        }
    }

    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command command, Displayable screen) {
        if (command == CANCEL_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }
}
