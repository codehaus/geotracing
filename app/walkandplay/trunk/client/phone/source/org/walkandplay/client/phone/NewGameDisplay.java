package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextField;

import org.walkandplay.client.phone.Log;
import org.walkandplay.client.phone.TCPClientListener;

public class NewGameDisplay extends DefaultDisplay implements TCPClientListener {

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

    public void start(Displayable aPrevScreen){
        midlet.getActiveApp().addTCPClientListener(this);
        prevScreen = aPrevScreen;
        Display.getDisplay(midlet).setCurrent(this);                
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("game-create-rsp")) {
                midlet.getCreateApp().setGameId(rsp.getAttr("id"));
                midlet.getCreateApp().setGameName(gameName);
                deleteAll();
                removeCommand(OK_CMD);
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