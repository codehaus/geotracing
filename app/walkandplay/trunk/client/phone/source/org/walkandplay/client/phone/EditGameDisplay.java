package org.walkandplay.client.phone;

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
public class EditGameDisplay extends DefaultDisplay implements XMLChannelListener {
    private ChoiceGroup gamesGroup = new ChoiceGroup("", ChoiceGroup.EXCLUSIVE);
    private Hashtable games = new Hashtable(2);

    private Image logo;

    Command OK_CMD = new Command("Ok", Command.SCREEN, 2);

    public EditGameDisplay(WPMidlet aMIDlet, Displayable aPrevScreen) {
        super(aMIDlet, "Edit a game");
        prevScreen = aPrevScreen;

        try {
            //#ifdef polish.images.directLoad
            logo = Image.createImage("/play_icon_small.png");
            //#else
            logo = scheduleImage("/play_icon_small.png");
            //#endif

            midlet.getCreateApp().setKWClientListener(this);
            addCommand(OK_CMD);

            getGamesByUser();
        } catch (Throwable t) {
            //#style alertinfo
            append("Oops, could not start you up. \n " + t.getMessage());
        }
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        Log.log("** received:" + new String(aResponse.toBytes(false)));
        String tag = aResponse.getTag();

        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("query-store-rsp")) {

                // draw the screen
                append(logo);
                //#style labelinfo
                append("Select a game and press OK to edit");
                append(gamesGroup);

                Vector elms = rsp.getChildrenByTag("record");
                for (int i = 0; i < elms.size(); i++) {
                    JXElement elm = (JXElement) elms.elementAt(i);
                    String name = elm.getChildText("name");
                    //#style formbox
                    gamesGroup.append(name, null);
                    games.put(name, elm);
                }
                // select the first
                gamesGroup.setSelectedIndex(0, true);

            }
        }
    }

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append("Oops, we lost our connection. Please go back and try again.");
    }

    private void getGamesByUser() {
        JXElement req = new JXElement("query-store-req");
        req.setAttr("cmd", "q-games-by-user");
        req.setAttr("user", midlet.getKWUser());
        midlet.getCreateApp().sendRequest(req);
    }

    /*
       * The commandAction method is implemented by this midlet to
       * satisfy the CommandListener interface and handle the Cancel action.
       */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == OK_CMD) {
            JXElement game = (JXElement) games.get(gamesGroup.getString(gamesGroup.getSelectedIndex()));
            midlet.getCreateApp().setGameId(game.getAttr("id"));
            midlet.getCreateApp().setGameName(game.getChildText("name"));
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }


}
