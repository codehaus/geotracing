package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import nl.justobjects.mjox.XMLChannelListener;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Gauge;
import java.util.Vector;

public class ScoreDisplay extends DefaultDisplay implements XMLChannelListener {

    private int maxScore;

    public ScoreDisplay(WPMidlet aMIDlet, int aMaxScore, Displayable aPrevScreen) {
        super(aMIDlet, "Scores");
        maxScore = aMaxScore;
        prevScreen = aPrevScreen;
        midlet.getPlayApp().setKWClientListener(this);

        // get the scores
        getScores();
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        Log.log("** received:" + new String(aResponse.toBytes(false)));
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("query-store-rsp")) {
                Vector elms = rsp.getChildrenByTag("record");
                for (int i = 0; i < elms.size(); i++) {
                    JXElement e = (JXElement) elms.elementAt(i);
                    String team = e.getChildText("team");
                    String points = e.getChildText("points");

                    //#style labelinfo
                    append(team);
                    //#style formbox
                    append(new Gauge("", false, maxScore, Integer.parseInt(points)));
                }
            }
        }
    }

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append("Oops, we lost our connection. Please go back and try again.");
    }

    private void getScores() {
        JXElement req = new JXElement("query-store-req");
        req.setAttr("cmd", "q-scores");
        req.setAttr("gameid", midlet.getPlayApp().getGamePlayId());
        midlet.getPlayApp().sendRequest(req);
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
