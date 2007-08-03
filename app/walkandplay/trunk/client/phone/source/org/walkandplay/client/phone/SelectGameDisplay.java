package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import nl.justobjects.mjox.XMLChannelListener;

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
    private TCPClient kwClient;
    private ChoiceGroup gamesGroup = new ChoiceGroup("", ChoiceGroup.EXCLUSIVE);
    private Hashtable gameRounds = new Hashtable(2);
    private int gamePlayId;
    private JXElement game;
    private JXElement gameRound;

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

            connect();
        } catch (Throwable t) {
            //#style alertinfo
            append("Oops, could not start you up. \n " + t.getMessage());
        }
    }

    private void connect() {
        try {
            if (kwClient != null) {
                kwClient.restart();
            } else {
                kwClient = new TCPClient(midlet.getKWServer(), Integer.parseInt(midlet.getKWPort()));
                setKWClientListener(this);
                kwClient.login(midlet.getKWUser(), midlet.getKWPassword());
            }
        } catch (Throwable t) {
            deleteAll();
            addCommand(BACK_CMD);
            //#style alertinfo
            append("We can not connect. Please check your account settings.");
        }
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        Log.log("** received:" + new String(aResponse.toBytes(false)));
        String tag = aResponse.getTag();
        if (tag.equals("login-rsp")) {
            try {
                Log.log("send select app");
                kwClient.setAgentKey(aResponse);
                kwClient.selectApp("geoapp", "user");
            } catch (Throwable t) {
                Log.log("Selectapp failed:" + t.getMessage());
            }
        } else if (tag.equals("select-app-rsp")) {
            getGameRoundsByUser();
        } else if (tag.indexOf("-nrsp") != -1) {
            //#style alertinfo
            append("Oops, could not log in. Check your username and password in SETTINGS.");
        } else {
            if (tag.equals("utopia-rsp")) {
                JXElement rsp = aResponse.getChildAt(0);
                if (rsp.getTag().equals("query-store-rsp")) {

                    // draw the screen
                    append(logo);
                    //#style labelinfo
                    append("Select a game and press PLAY from the options");
                    append(gamesGroup);
                    addCommand(PLAY_CMD);
                    addCommand(DESCRIPTION_CMD);

                    Vector elms = rsp.getChildrenByTag("record");
                    for (int i = 0; i < elms.size(); i++) {
                        JXElement elm = (JXElement) elms.elementAt(i);
                        String name = elm.getChildText("name");
                        String gameplayState = elm.getChildText("gameplaystate");
                        String displayName = name + " | " + gameplayState;
                        //#style formbox
                        gamesGroup.append(displayName, null);
                        gameRounds.put(displayName, elm);
                    }
                    // select the first
                    gamesGroup.setSelectedIndex(0, true);

                } else if (rsp.getTag().equals("play-start-rsp")) {
                    // start the playdisplay
                    PlayDisplay d = new PlayDisplay(midlet);
                    Display.getDisplay(midlet).setCurrent(d);
                    d.start();
                }
            }
        }
    }

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append("Oops, we lost our connection. Please go back and try again.");
        connect();
    }

    public void sendRequest(JXElement aRequest) {
        try {
            Log.log("** sent: " + new String(aRequest.toBytes(false)));
            kwClient.utopia(aRequest);
        } catch (Throwable t) {
            Log.log("Exception sending " + new String(aRequest.toBytes(false)));
            // we need to reconnect!!!!
            connect();
        }
    }

    public void setKWClientListener(XMLChannelListener aListener) {
        kwClient.setListener(aListener);
    }

    public void setGame(JXElement aGame) {
        game = aGame;
    }

    public JXElement getGame() {
        return game;
    }

    public void setGameRound(JXElement aGameRound) {
        gameRound = aGameRound;
    }

    public JXElement getGameRound() {
        return gameRound;
    }

    public void setGamePlayId(int anId) {
        gamePlayId = anId;
    }

    public int getGamePlayId() {
        return gamePlayId;
    }

    private void getGameRoundsByUser() {
        JXElement req = new JXElement("query-store-req");
        req.setAttr("cmd", "q-play-status-by-user");
        req.setAttr("user", midlet.getKWUser());
        sendRequest(req);
    }

    private void startGameRound() {
        JXElement req = new JXElement("play-start-req");
        req.setAttr("id", gamePlayId);
        sendRequest(req);
    }

    /*
       * The commandAction method is implemented by this midlet to
       * satisfy the CommandListener interface and handle the Cancel action.
       */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == PLAY_CMD) {
            gameRound = (JXElement) gameRounds.get(gamesGroup.getString(gamesGroup.getSelectedIndex()));
            gamePlayId = Integer.parseInt(gameRound.getChildText("gameplayid"));

            // now start the game
            startGameRound();
        } else if (cmd == DESCRIPTION_CMD) {
            JXElement gameElm = (JXElement) gameRounds.get(gamesGroup.getString(gamesGroup.getSelectedIndex()));
            String desc = gameElm.getChildText("description");

            //#style labelinfo
            append("description");

            //#style formbox
            append(desc);
        }
    }


}
