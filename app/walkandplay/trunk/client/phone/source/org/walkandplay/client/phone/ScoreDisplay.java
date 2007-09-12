package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Gauge;
import java.util.Vector;

import org.walkandplay.client.phone.TCPClientListener;

public class ScoreDisplay extends DefaultDisplay implements TCPClientListener {

    private int maxScore;
    private boolean active;

    public ScoreDisplay(WPMidlet aMIDlet, int aMaxScore, Displayable aPrevScreen) {
        super(aMIDlet, "Scores");
        maxScore = aMaxScore;
        prevScreen = aPrevScreen;
        midlet.getPlayApp().addTCPClientListener(this);
    }

    public void start(){
        active = true;
        getScores();
        Display.getDisplay(midlet).setCurrent(this);
    }

    public boolean isActive(){
        return active;
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("query-store-rsp")) {
                String cmd = rsp.getAttr("cmd");
                if(cmd.equals("q-scores")){
                    Vector elms = rsp.getChildrenByTag("record");
                    for (int i = 0; i < elms.size(); i++) {
                        JXElement e = (JXElement) elms.elementAt(i);
                        String team = e.getChildText("loginname");
                        String points = e.getChildText("score");

                        //#style labelinfo
                        append(team);
                        //#style formbox
                        append(new Gauge("", false, maxScore, Integer.parseInt(points)));
                    }
                }
            }
        }
    }

    public void onNetStatus(String aStatus){

    }
    
    public void onConnected(){

    }

    public void onError(String anErrorMessage){
        //#style alertinfo
        append(anErrorMessage);
    }

    public void onFatal(){
        midlet.getActiveApp().exit();
        Display.getDisplay(midlet).setCurrent(midlet.getActiveApp());
    }

    private void getScores() {
        JXElement req = new JXElement("query-store-req");
        req.setAttr("cmd", "q-scores");
        req.setAttr("roundid", midlet.getPlayApp().getGameRound().getChildText("roundid"));
        midlet.getPlayApp().sendRequest(req);
    }

    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command command, Displayable screen) {
        if (command == BACK_CMD) {
            active = false;
            midlet.getPlayApp().removeTCPClientListener(this);
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }
}
