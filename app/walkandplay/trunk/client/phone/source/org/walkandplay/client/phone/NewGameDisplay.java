package org.walkandplay.client.phone;

import javax.microedition.lcdui.*;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannelListener;
import nl.justobjects.mjox.XMLChannel;

public class NewGameDisplay extends DefaultDisplay implements XMLChannelListener {

    private Command OK_CMD = new Command("OK", Command.OK, 1);
    private TextField inputField;
    private TracerEngine tracerEngine;
    private String gameName;

    public NewGameDisplay(WPMidlet aMIDlet, Displayable aPrevScreen, TracerEngine aTracerEngine) {
        super(aMIDlet, "New game");
        prevScreen = aPrevScreen;
        tracerEngine = aTracerEngine;
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
        if(tag.equals("utopia-rsp")){
            JXElement rsp = aResponse.getChildAt(0);
            if(rsp.getTag().equals("game-create-rsp")){
                // now automatically create a gameround
                String gameId = rsp.getAttr("id");
                createGameRound(gameId, "test");
            }else if(rsp.getTag().equals("round-create-rsp")){
                // now start the game round
                String gameRoundId = rsp.getAttr("id");
                startGameRound(gameRoundId);
                deleteAll();
                addCommand(BACK_CMD);
                //#style alertinfo
                append("Created new game '" + gameName + "'");
            }else if(rsp.getTag().equals("game-create-nrsp")){
                //#style alertinfo
                append("Could not create game '" + gameName + "'. Please try again.");
                gameName = "";
                inputField.setString("");
            }else if(rsp.getTag().equals("round-create-nrsp")){
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

    private void createGame(String aGameName){
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

    private void createGameRound(String aGameId, String aGameRoundName){
        Log.log("create game round");
        JXElement req = new JXElement("round-create-req");
        req.setAttr("gameid", aGameId);
        req.setAttr("name", aGameRoundName);
        req.setAttr("players", midlet.getKWUser());
        midlet.getCreateApp().sendRequest(req);
    }

    private void startGameRound(String aGamePlayId) {
        JXElement req = new JXElement("play-start-req");
        req.setAttr("id", aGamePlayId);
        midlet.getCreateApp().sendRequest(req);
    }

    public void commandAction(Command command, Displayable screen) {
        Log.log("submitting");
        if (command == OK_CMD) {
            String gameName = inputField.getString();
            Log.log("game name:" + gameName);
            if(gameName.length() == 0){
                //#style alertinfo
                append("Fill in a name.");
            }else{
                tracerEngine.suspend();
                createGame(gameName);
            }
        }else if(command == BACK_CMD){
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }
}
