package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextField;

public class NewGameDisplay extends DefaultDisplay {

    private Command OK_CMD = new Command("OK", Command.OK, 1);
    private TextField inputField;
    private String gameName;

    public NewGameDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "New game");

        //#style labelinfo
        append("Create a new game");
        //#style textbox
        inputField = new TextField("", "", 48, TextField.ANY);
        append(inputField);

        addCommand(OK_CMD);
    }

    public void start(Displayable aPrevScreen) {
        prevScreen = aPrevScreen;
        Display.getDisplay(midlet).setCurrent(this);
    }

    public void handleGameCreateRsp(JXElement aResponse) {
        midlet.getCreateApp().setGameId(aResponse.getAttr("id"));
        midlet.getCreateApp().setGameName(gameName);
        deleteAll();
        removeCommand(OK_CMD);
        addCommand(BACK_CMD);
        //#style alertinfo
        append("Created new game '" + gameName + "'");
    }

    public void handleGameCreateNrsp(JXElement aResponse) {
        //#style alertinfo
        append("Could not create game '" + gameName + "'. Please try again.");
        gameName = "";
        inputField.setString("");
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
        midlet.getActiveApp().sendRequest(req);
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