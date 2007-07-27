package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Net;
import org.geotracing.client.NetListener;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Gauge;
import java.util.Vector;

public class ScoreDisplay extends DefaultDisplay {

    private Vector scores;
    private int maxScore;
    private Net net;

    public ScoreDisplay(WPMidlet aMIDlet, int aMaxScore, Displayable aPrevScreen) {
        super(aMIDlet, "Scores");
        maxScore = aMaxScore;
        prevScreen = aPrevScreen;
        
        net = Net.getInstance();
        if (!net.isConnected()) {
            net.setProperties(midlet);
            net.start();
        }

        // get the scores
        getScores();
    }

    private void drawScores(){
        // Create the TextBox containing the "Hello,World!" message
        for (int i = 0; i < scores.size(); i++) {
            JXElement r = (JXElement) scores.elementAt(i);
            String team = r.getChildText("team");
            String points = r.getChildText("points");

            //#style labelinfo
            append(team);
            //#style formbox
            append(new Gauge("", false, maxScore, Integer.parseInt(points)));
        }
    }

    private void getScores() {
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
                    
                    // niow do the rest
                    drawScores();
                }
            }).start();
        } catch (Throwable t) {
            Log.log("Exception in getScores:\n" + t.getMessage());
        }
    }

    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command command, Displayable screen) {
        if (command == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }
}
