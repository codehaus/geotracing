package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;

import javax.microedition.lcdui.*;

public class AddRoundDisplay extends DefaultDisplay {

    private Command OK_CMD = new Command("OK", Command.OK, 1);

    private TextField nameField;
    private StringItem alertField = new StringItem("", "");
    private String gameRoundName;

    public AddRoundDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "Add game round");

        //#style labelinfo
        append("Enter Title");

        //#style textbox
        nameField = new TextField("", "", 32, TextField.ANY);
        append(nameField);

        addCommand(OK_CMD);

    }

    public void start(Displayable aPrevScreen) {
        prevScreen = aPrevScreen;
        Display.getDisplay(midlet).setCurrent(this);
    }

    public void handleRoundCreateRsp(JXElement aResponse) {
        clearScreen();
        alertField.setText("Game round '" + gameRoundName + "' added");
    }

    public void handleRoundCreateNrsp(JXElement aResponse) {
        clearScreen();
        alertField.setText("Error adding game round '" + gameRoundName + "'. Please try again.");
    }

    private void clearScreen() {
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append(alertField);
    }

    private void createGameRound(String aGameRoundName) {
        Log.log("create game round");
        JXElement req = new JXElement("round-create-req");
        req.setAttr("gameid", midlet.getCreateApp().getGameId());
        req.setAttr("name", aGameRoundName);
        req.setAttr("players", midlet.getKWUser());
        midlet.getActiveApp().sendRequest(req);
    }

    public void commandAction(Command command, Displayable screen) {
        if (command == OK_CMD) {
            gameRoundName = nameField.getString();
            if (gameRoundName == null || gameRoundName.length() == 0) {
                //#style alertinfo
                append(alertField);
                alertField.setText("Please fill in the game round name...");
            } else {
                createGameRound(gameRoundName);
            }
        } else if (command == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }

}
