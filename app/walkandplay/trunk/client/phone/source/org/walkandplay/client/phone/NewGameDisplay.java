package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import nl.justobjects.mjox.XMLChannelListener;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextField;

public class NewGameDisplay extends DefaultDisplay implements XMLChannelListener {

    private Command OK_CMD = new Command("OK", Command.OK, 1);
    private TextField inputField;
    private String gameName;

    public NewGameDisplay(WPMidlet aMIDlet, Displayable aPrevScreen) {
        super(aMIDlet, "New game");
        prevScreen = aPrevScreen;
        midlet.getCreateApp().setKWClientListener(this);

        //#style labelinfo
        append("Create a new game");
        //#style textbox
        inputField = new TextField("", "", 48, TextField.ANY);
        append(inputField);

        addCommand(OK_CMD);
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        Log.log("** received:" + new String(aResponse.toBytes(false)));
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("game-create-rsp")) {
                midlet.getCreateApp().setGameId(rsp.getAttr("id"));
                midlet.getCreateApp().setGameName(gameName);
                deleteAll();
                addCommand(BACK_CMD);
                //#style alertinfo
                append("Created new game '" + gameName + "'");
            } else if (rsp.getTag().equals("game-create-nrsp")) {
                //#style alertinfo
                append("Could not create game '" + gameName + "'. Please try again.");
                gameName = "";
                inputField.setString("");
            }
        }
    }

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append("Oops, we lost our connection. Please go back and try again.");
    }

    private void createGame(String aGameName) {
        Log.log("create game");
        JXElement req = new JXElement("game-create-req");
        JXElement game = new JXElement("game");
        req.addChild(game);
        JXElement name = new JXElement("name");
        name.setText(aGameName);
        game.addChild(name);
        /*JXElement state = new JXElement("state");
        state.setText("0");
        game.addChild(state);*/
        midlet.getCreateApp().sendRequest(req);
    }

    public void commandAction(Command command, Displayable screen) {
        Log.log("submitting");
        if (command == OK_CMD) {
            gameName = inputField.getString();
            Log.log("game name:" + gameName);
            if (gameName.length() == 0) {
                //#style alertinfo
                append("Fill in a name.");
            } else {
                createGame(gameName);
            }
        } else if (command == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }
}