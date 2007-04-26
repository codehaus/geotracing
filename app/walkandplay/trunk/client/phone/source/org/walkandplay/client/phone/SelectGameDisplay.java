package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;

import javax.microedition.lcdui.*;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Net;
import org.geotracing.client.NetListener;
import org.geotracing.client.Preferences;

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

    Command PLAY_CMD = new Command(Locale.get("selectGame.Play"), Command.SCREEN, 2);
    Command DESCRIPTION_CMD = new Command(Locale.get("selectGame.Description"), Command.SCREEN, 2);

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
        
        midlet = aMIDlet;
        
        net = Net.getInstance();
        if(!net.isConnected()){
            net.setProperties(aMIDlet);
            net.setListener(this);
            net.start();
        }

        if(!net.isConnected()){
            // login must have failed!!!!
            //#style formbox
            append("Logging in has failed!! Please check your username and password under Settings/Account and try again.");            
        }else{

            // get the games
            try{
                JXElement req = new JXElement("query-store-req");
                req.setAttr("cmd", "q-play-status-by-user");
                req.setAttr("user", new Preferences(Net.RMS_STORE_NAME).get(Net.PROP_USER, midlet.getAppProperty(Net.PROP_USER)));
                JXElement rsp = net.utopiaReq(req);
                System.out.println(new String(rsp.toBytes(false)));
                if(rsp!=null) {
                    Vector elms = rsp.getChildrenByTag("record");
                    for(int i=0;i<elms.size();i++){
                        JXElement elm = (JXElement)elms.elementAt(i);
                        String name = elm.getChildText("name");
                        //String description = elm.getChildText("description");
                        String gameplayState = elm.getChildText("gameplaystate");
                        String displayName = name + " | " + gameplayState;
                        //String displayName = name + " - '" + description + "' | state: " + gameplayState;
                        //#style formbox
                        gamesGroup.append(displayName, null);
                        games.put(displayName, elm);
                    }
                    // select the first on
                    gamesGroup.setSelectedIndex(0, true);

                }
            }catch(Throwable t){
                System.out.println(t.getMessage());
            }
            
            append(logo);
            //#style formbox
            append("Select a game and press PLAY in the menu");
            append(gamesGroup);
            addCommand(PLAY_CMD);
            addCommand(DESCRIPTION_CMD);
        }
    }

    private void startGame(){
        JXElement req = new JXElement("play-reset-req");
        req.setAttr("id", midlet.getGamePlayId());
        System.out.println(new String(req.toBytes(false)));
        JXElement rsp = net.utopiaReq(req);
        System.out.println(new String(rsp.toBytes(false)));

        req = new JXElement("play-start-req");
        req.setAttr("id", midlet.getGamePlayId());
        System.out.println(new String(req.toBytes(false)));
        rsp = net.utopiaReq(req);
        System.out.println(new String(rsp.toBytes(false)));
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
            midlet.setGameRound(gameElm);
            midlet.setGamePlayId(Integer.parseInt(gameElm.getChildText("gameplayid")));
            // now start the game
            startGame();
            midlet.setPlayMode(true);

            PlayDisplay d = new PlayDisplay(midlet);
            midlet.playDisplay = d;                        
            Display.getDisplay(midlet).setCurrent(d);
            d.start();
        }else if (cmd == DESCRIPTION_CMD) {
            gameName = gamesGroup.getString(gamesGroup.getSelectedIndex());
            gameElm = (JXElement) games.get(gameName);
            String desc = gameElm.getChildText("description");

            //#style labelinfo
            append("description");

            //#style formbox
            append(desc);
        }
    }


}
