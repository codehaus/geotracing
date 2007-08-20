package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;

import javax.microedition.lcdui.*;

import org.walkandplay.client.phone.Log;
import org.walkandplay.client.phone.TCPClientListener;

public class AddRoundDisplay extends DefaultDisplay implements TCPClientListener {

    private Command OK_CMD = new Command("OK", Command.OK, 1);

    private TextField nameField;
    private StringItem alertField = new StringItem("", "");
    private String gameRoundName;

    public AddRoundDisplay(WPMidlet aMIDlet, Displayable aPrevScreen) {
        super(aMIDlet, "Add game round");
        prevScreen = aPrevScreen;
        midlet.getActiveApp().addTCPClientListener(this);

        //#style labelinfo
        append("Enter Title");

        //#style textbox
        nameField = new TextField("", "", 32, TextField.ANY);
        append(nameField);

        addCommand(OK_CMD);
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("round-create-rsp")) {
                clearScreen();
                alertField.setText("Game round '" + gameRoundName + "' added");
            } else if (rsp.getTag().equals("round-create-nrsp")) {
                clearScreen();
                alertField.setText("Error adding game round '" + gameRoundName + "'. Please try again.");
            }
        }
    }

    private void clearScreen(){
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append(alertField);
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
            midlet.getActiveApp().removeTCPClientListener(this);
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }

}
