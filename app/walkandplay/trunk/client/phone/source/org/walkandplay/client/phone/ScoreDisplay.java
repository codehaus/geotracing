package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Gauge;
import java.util.Vector;

import org.walkandplay.client.phone.TCPClientListener;

public class ScoreDisplay extends DefaultDisplay {

    private int maxScore;
    private boolean active;

    public ScoreDisplay(WPMidlet aMIDlet, int aMaxScore, Displayable aPrevScreen) {
        super(aMIDlet, "Scores");
        maxScore = aMaxScore;
        prevScreen = aPrevScreen;
    }

    public void start(){
        active = true;
        // start fresh
        deleteAll();
        getScores();
        Display.getDisplay(midlet).setCurrent(this);
    }

    public boolean isActive(){
        return active;
    }

    public void handleGetScoresRsp(JXElement aResponse){
        Vector elms = aResponse.getChildrenByTag("record");
        for (int i = 0; i < elms.size(); i++) {
            JXElement e = (JXElement) elms.elementAt(i);
            String team = e.getChildText("loginname");
            String points = e.getChildText("score");
            try{
                //#style labelinfo
                append(team);
                //#style formbox
                append(new Gauge("", false, maxScore, Integer.parseInt(points)));
            }catch(Throwable t){
                //#style alertinfo
                append("Error:" + t.toString() + ":" + t.getMessage());
            }
        }
    }

    public void handleGetScoresNrsp(JXElement aResponse){
        //#style alertinfo
        append("Could not get the scores:" + aResponse.getAttr("details"));
    }

    private void getScores() {
        JXElement req = new JXElement("query-store-req");
        req.setAttr("cmd", "q-scores");
        req.setAttr("roundid", midlet.getPlayApp().getGameRound().getChildText("roundid"));
        midlet.getPlayApp().sendRequest(req);
    }

    public void commandAction(Command command, Displayable screen) {
        if (command == BACK_CMD) {
            active = false;
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }
}
