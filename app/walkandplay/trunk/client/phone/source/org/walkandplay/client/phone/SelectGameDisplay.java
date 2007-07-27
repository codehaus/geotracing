package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannelListener;
import nl.justobjects.mjox.XMLChannel;
import org.geotracing.client.Net;
import org.geotracing.client.NetListener;
import org.geotracing.client.Preferences;

import javax.microedition.lcdui.*;
import java.util.Hashtable;
import java.util.Vector;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
public class SelectGameDisplay extends DefaultDisplay implements XMLChannelListener {
    private ChoiceGroup gamesGroup = new ChoiceGroup("", ChoiceGroup.EXCLUSIVE);
    private Hashtable games = new Hashtable(2);
    private int gamePlayId;
    private JXElement gameElm;
    private Image logo;

    Command PLAY_CMD = new Command(Locale.get("selectGame.Play"), Command.SCREEN, 2);
    Command DESCRIPTION_CMD = new Command(Locale.get("selectGame.Description"), Command.SCREEN, 2);

    public SelectGameDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "Play a game!");
        try {
            //#ifdef polish.images.directLoad
            logo = Image.createImage("/play_icon_small.png");
            //#else
            logo = scheduleImage("/play_icon_small.png");
            //#endif
        } catch (Throwable t) {
            Log.log("Could not load the images on PlayDisplay");
        }

        midlet.setListener(this);

        append(logo);
        //#style labelinfo
        append("Select a game and press PLAY from the options");
        append(gamesGroup);
        addCommand(PLAY_CMD);
        addCommand(DESCRIPTION_CMD);

        getGames();
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        Log.log("** received:" + new String(aResponse.toBytes(false)));
        String tag = aResponse.getTag();
        if(tag.equals("utopia-rsp")){
            JXElement rsp = aResponse.getChildAt(0);
            if(rsp.getTag().equals("query-store-rsp")){
                Vector elms = rsp.getChildrenByTag("record");
                for (int i = 0; i < elms.size(); i++) {
                    JXElement elm = (JXElement) elms.elementAt(i);
                    String name = elm.getChildText("name");
                    String gameplayState = elm.getChildText("gameplaystate");
                    String displayName = name + " | " + gameplayState;
                    //#style formbox
                    gamesGroup.append(displayName, null);
                    games.put(displayName, elm);
                }
                // select the first on
                gamesGroup.setSelectedIndex(0, true);
            }else if(rsp.getTag().equals("play-start-rsp")){
                midlet.setGameRound(gameElm);
                midlet.setGamePlayId(gamePlayId);

                midlet.setPlayMode(true);
                PlayDisplay d = new PlayDisplay(midlet);
                midlet.playDisplay = d;
                Display.getDisplay(midlet).setCurrent(d);
                d.start();
            }
        }
    }

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        Log.log("XMLChannel stopped");
    }

    private void getGames(){
        try {
            JXElement req = new JXElement("query-store-req");
            req.setAttr("cmd", "q-play-status-by-user");
            req.setAttr("user", new Preferences(Net.RMS_STORE_NAME).get(Net.PROP_USER, midlet.getAppProperty(Net.PROP_USER)));
            midlet.sendRequest(req);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }

    }

    private void startGame() {
        JXElement req = new JXElement("play-start-req");
        req.setAttr("id", gamePlayId);
        midlet.sendRequest(req);
    }

    /*
       * The commandAction method is implemented by this midlet to
       * satisfy the CommandListener interface and handle the Cancel action.
       */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == PLAY_CMD) {
            gameElm = (JXElement) games.get(gamesGroup.getString(gamesGroup.getSelectedIndex()));
            gamePlayId = Integer.parseInt(gameElm.getChildText("gameplayid"));

            // now start the game
            startGame();
        } else if (cmd == DESCRIPTION_CMD) {
            JXElement gameElm = (JXElement) games.get(gamesGroup.getString(gamesGroup.getSelectedIndex()));
            String desc = gameElm.getChildText("description");

            //#style labelinfo
            append("description");

            //#style formbox
            append(desc);
        }
    }


}
