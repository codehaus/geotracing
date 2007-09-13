package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;

import javax.microedition.lcdui.*;
import java.util.Hashtable;
import java.util.Vector;

public class SelectGameDisplay extends AppStartDisplay {
    private ChoiceGroup gamesGroup;
    private Hashtable gameRounds;
    private int gamePlayId;
    private JXElement game;
    private JXElement gameRound;
    private String color;

    private SelectGameDisplay instance;
    private ErrorHandler errorHandler;

    private Image logo;
    private PlayDisplay playDisplay;


    Command PLAY_CMD = new Command(Locale.get("selectGame.Play"), Command.SCREEN, 2);
    Command DESCRIPTION_CMD = new Command(Locale.get("selectGame.Description"), Command.SCREEN, 2);

    public SelectGameDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "Play a game!");
        instance = this;

        try {
            //#ifdef polish.images.directLoad
            logo = Image.createImage("/play_icon_small.png");
            //#else
            logo = scheduleImage("/play_icon_small.png");
            //#endif

        } catch (Throwable t) {
            //#style alertinfo
            append("Oops, could not start you up. \n " + t.getMessage());
        }
    }

    public void start(){
        connect();        
    }

    public void onConnected(){
        getGameRoundsByUser();
    }

    public void onError(String anErrorMessage){
        //#style alertinfo
        append(anErrorMessage);
    }

    public void onFatal(){
        exit();
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("query-store-rsp")) {
                String cmd = rsp.getAttr("cmd");
                if(cmd.equals("q-play-status-by-user")){

                    // always start clean
                    deleteAll();
                    gamesGroup = new ChoiceGroup("", ChoiceGroup.EXCLUSIVE);
                    gameRounds = new Hashtable(2);

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
                        String roundName = elm.getChildText("roundname");
                        String gameplayState = elm.getChildText("gameplaystate");
                        String displayName = name + " | " + roundName;
                        if(gameplayState.equals("running")){
                            displayName += " *";
                        }
                        
                        if(!gameplayState.equals("done")){
                            //#style formbox
                            gamesGroup.append(displayName, null);
                            gameRounds.put(displayName, elm);
                        }
                    }
                    // select the first
                    gamesGroup.setSelectedIndex(0, true);
                    
                    // now show the screen
                    Display.getDisplay(midlet).setCurrent(this);
                }
            } else if (rsp.getTag().equals("play-start-rsp")) {

                // start the playdisplay
                if(playDisplay == null){
                    playDisplay = new PlayDisplay(midlet);
                }
                Display.getDisplay(midlet).setCurrent(playDisplay);
                playDisplay.start(color);
            } else if (rsp.getTag().equals("play-start-nrsp")) {
                getErrorHandler().showGoBack(rsp.getAttr("details"));
            }
        }
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
            tcpClient.stop();
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == PLAY_CMD) {
            gameRound = (JXElement) gameRounds.get(gamesGroup.getString(gamesGroup.getSelectedIndex()));
            gamePlayId = Integer.parseInt(gameRound.getChildText("gameplayid"));
            color = gameRound.getChildText("color");

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

    private ErrorHandler getErrorHandler(){
        if(errorHandler == null){
            errorHandler = new ErrorHandler();
        }
        return errorHandler;
    }

    private class ErrorHandler implements CommandListener {
		private Command BACK_CMD = new Command("Back", Command.CANCEL, 1);

        private Form form;

        public void showGoBack(String aMsg) {
            //#style defaultscreen
			form = new Form("TaskDisplay");

            //#style alertinfo
            form.append(aMsg);
			form.addCommand(BACK_CMD);

			form.setCommandListener(this);
            Display.getDisplay(midlet).setCurrent(form);
		}

        public void commandAction(Command command, Displayable screen) {
            midlet.getActiveApp().removeTCPClientListener(instance);
            Display.getDisplay(midlet).setCurrent(prevScreen);            
		}
	}


}
