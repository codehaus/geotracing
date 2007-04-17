package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;

import javax.microedition.lcdui.*;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Net;
import org.geotracing.client.NetListener;

import java.util.Hashtable;
import java.util.Vector;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
public class SelectGameDisplay extends DefaultDisplay implements NetListener {
    private ChoiceGroup gamesGroup = new ChoiceGroup("", ChoiceGroup.EXCLUSIVE);
    private Hashtable games = new Hashtable(2);
    private String gameName;
    private JXElement gameElm;
    private WPMidlet midlet;
    private Net net;
    private Image logo;

    Command PLAY_CMD = new Command(Locale.get("play.Play"), Command.OK, 1
    );

    public SelectGameDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "");
        try{
            //#ifdef polish.images.directLoad
            logo = Image.createImage("/play_icon_small.png");
            //#else
            logo = scheduleImage("/play_icon_small.png");
            //#endif
        }catch(Throwable t){
            Log.log("Could not load the images on PlayDisplay");
        }
        
        append(logo);

        midlet = aMIDlet;
        
        net = Net.getInstance();
        if(!net.isConnected()){
            net.setProperties(aMIDlet);
            net.setListener(this);
            net.start();
        }

        /*JXElement req = new JXElement("query-store-req");
        req.setAttr("cmd", "q-games-by-user");
        req.setAttr("user", net.getUserName());
        JXElement rsp = net.utopiaReq(req);
        //Vector gameLocations = rsp.getChildrenByTag("record");
        System.out.println(new String(rsp.toBytes(false)));*/

        // get the play state
        JXElement req = new JXElement("play-getstate-req");
        JXElement rsp = net.utopiaReq(req);
        if(rsp!=null) {
            Vector gamesElms = rsp.getChildrenByTag("game");
            for(int i=0;i<gamesElms.size();i++){
                JXElement t = (JXElement)gamesElms.elementAt(i);
                String id = t.getAttr("id");
                String name = t.getAttr("name");
                String state = t.getAttr("state");
                String displayName = name + " | " + state;
                //#style formbox
                gamesGroup.append(displayName, null);
                games.put(displayName, t);
            }
        }
        //#style formbox
        append("Select a game and press PLAY in the menu");        
        append(gamesGroup);
        addCommand(PLAY_CMD);
    }

    private void startGame(){
        JXElement req = new JXElement("play-start-req");
        req.setAttr("id", midlet.getGameSchedule().getAttr("id"));
        JXElement rsp = net.utopiaReq(req);
        // store the gameplay id
        midlet.setGamePlayId(Integer.parseInt(rsp.getAttr("id")));

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
            gameName = gamesGroup.getString(gamesGroup.getSelectedIndex());
            gameElm = (JXElement) games.get(gameName);
            midlet.setGameSchedule(gameElm);
            // now start the game
            startGame();
            
            midlet.setPlayMode(true);
            PlayDisplay d = new PlayDisplay(midlet);
            d.start();
            
            Display.getDisplay(midlet).setCurrent(d);

            
        }
    }


}
