package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import nl.justobjects.mjox.XMLChannelListener;

import javax.microedition.lcdui.*;

public class AddRoundDisplay extends DefaultDisplay implements XMLChannelListener {

    private Command OK_CMD = new Command("OK", Command.OK, 1);

    private TextField nameField;
    private StringItem alertField = new StringItem("", "");
    private String gameRoundName;

    public AddRoundDisplay(WPMidlet aMIDlet, Displayable aPrevScreen) {
        super(aMIDlet, "Add game round");
        prevScreen = aPrevScreen;

        midlet.getCreateApp().setKWClientListener(this);

        //#style labelinfo
        append("Enter Title");

        //#style textbox
        nameField = new TextField("", "", 32, TextField.ANY);
        append(nameField);

        addCommand(OK_CMD);
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        Log.log("** received:" + new String(aResponse.toBytes(false)));
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            deleteAll();
            addCommand(BACK_CMD);
            //#style alertinfo
            append(alertField);
            if (rsp.getTag().equals("round-create-rsp")) {
                alertField.setText("Game round '" + gameRoundName + "' added");
            } else if (rsp.getTag().equals("round-create-nrsp")) {
                alertField.setText("Error adding game round '" + gameRoundName + "'. Please try again.");
            }
        }
    }

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append("Oops, we lost our connection. Please go back and try again.");
    }

    private void createGameRound(String aGameRoundName) {
        Log.log("create game round");
        JXElement req = new JXElement("round-create-req");
        req.setAttr("gameid", midlet.getCreateApp().getGameId());
        req.setAttr("name", aGameRoundName);
        req.setAttr("players", midlet.getKWUser());
        midlet.getCreateApp().sendRequest(req);
    }

    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
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
