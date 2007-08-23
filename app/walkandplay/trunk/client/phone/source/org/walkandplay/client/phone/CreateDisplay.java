package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import org.geotracing.client.*;

import javax.microedition.lcdui.*;
import java.util.Vector;

public class CreateDisplay extends AppStartDisplay implements TCPClientListener, GPSEngineListener {
    private String gpsStatus = "disconnected";
    private String netStatus = "disconnected";
    private StringItem netStatusBT = new StringItem("", netStatus, Item.BUTTON);
    private StringItem gpsStatusBT = new StringItem("", netStatus, Item.BUTTON);
    private StringItem gameLabel = new StringItem("", "Create an new game or select one to edit");

    private GPSEngine gpsEngine;
    private String gameId;

    private int netId;
    private int gpsId;

    private Command NEW_GAME_CMD = new Command(Locale.get("create.New"), Command.ITEM, 2);
    private Command EDIT_GAME_CMD = new Command(Locale.get("create.Edit"), Command.ITEM, 2);
    private Command ADD_ROUND_CMD = new Command(Locale.get("create.AddRound"), Command.ITEM, 2);
    private Command ADD_TEXT_CMD = new Command(Locale.get("create.AddText"), Command.ITEM, 2);
    private Command ADD_PHOTO_CMD = new Command(Locale.get("create.AddPhoto"), Command.ITEM, 2);
    private Command ADD_AUDIO_CMD = new Command(Locale.get("create.AddAudio"), Command.ITEM, 2);
    private Command SHOW_MAP_CMD = new Command(Locale.get("create.ShowMap"), Command.ITEM, 2);
    private Command SHOW_STATE_CMD = new Command(Locale.get("create.ShowState"), Command.ITEM, 2);
    private Command HIDE_STATE_CMD = new Command(Locale.get("create.HideState"), Command.ITEM, 2);

    private Image logo;

    public CreateDisplay(WPMidlet aMidlet) {
        super(aMidlet, "Create a game");
        try {
            //#ifdef polish.images.directLoad
            logo = Image.createImage("/trace_icon_small.png");
            //#else
            logo = scheduleImage("/trace_icon_small.png");
            //#endif
        } catch (Throwable t) {
            Log.log("Could not load the images on CreateDisplay");
        }

        addCommand(NEW_GAME_CMD);
        addCommand(EDIT_GAME_CMD);
        addCommand(SHOW_STATE_CMD);

        append(logo);
        //#style labelinfo
        append(gameLabel);

        connect();
    }

    public void setGameId(String aGameId) {
        gameId = aGameId;
        addCommand(ADD_ROUND_CMD);
        addCommand(ADD_TEXT_CMD);
        addCommand(ADD_PHOTO_CMD);
        addCommand(ADD_AUDIO_CMD);
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameName(String aGameName) {
        gameLabel.setText("Working on game '" + aGameName + "'");
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        
    }

    public void onGPSLocation(Vector thePoints) {
        addCommand(SHOW_MAP_CMD);
    }

    public void onConnected(){
        gpsEngine = GPSEngine.getInstance();
        gpsEngine.addListener(this);
        gpsEngine.start(midlet);        
    }

    public void onError(String anErrorMessage){
        //#style alertinfo
        append(anErrorMessage);
    }

    public void onFatal(String anErrorMessage){
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append(anErrorMessage);
    }

    public void onGPSStatus(String s) {
        gpsStatus = s;
        gpsStatusBT.setText("GPS:" + gpsStatus);
    }

    public void onNetStatus(String aStatus){
        netStatus = aStatus;
        netStatusBT.setText("NET:" + netStatus);
    }

    public void onStatus(String aStatus){
        onNetStatus(aStatus);
    }

    public void onGPSInfo(GPSInfo theInfo) {
        gpsStatusBT.setText(theInfo.toString());
    }

    public void commandAction(Command cmd, Displayable screen) {
        try{
            if (cmd == BACK_CMD) {
                gpsEngine.stop();
                tcpClient.stop();
                Display.getDisplay(midlet).setCurrent(prevScreen);
            } else if (cmd == NEW_GAME_CMD) {
                new NewGameDisplay(midlet, this);
            } else if (cmd == EDIT_GAME_CMD) {
                new EditGameDisplay(midlet, this);
            } else if (cmd == ADD_ROUND_CMD) {
                new AddRoundDisplay(midlet, this);
            } else if (cmd == ADD_PHOTO_CMD) {
                Display.getDisplay(midlet).setCurrent(new ImageCaptureDisplay(midlet));
                /*new ImageCaptureDisplay(midlet, this, false);*/
            } else if (cmd == ADD_AUDIO_CMD) {
                new AudioCaptureDisplay(midlet, this, false);
            } else if (cmd == ADD_TEXT_CMD) {
                new AddTextDisplay(midlet, this, false);
            } else if (cmd == SHOW_MAP_CMD) {
                MapDisplay md = new MapDisplay(midlet, this);
                Display.getDisplay(midlet).setCurrent(md);
                md.start();
            }else if (cmd == SHOW_STATE_CMD) {
                new StateHandler().showState();
            }
        }catch(Throwable t){
            Log.log("damn: " + t.toString());
            t.printStackTrace();
        }
    }

    private class StateHandler implements CommandListener {
		private Command cancelCmd = new Command("Back", Command.CANCEL, 1);

		public void showState() {
            //#style defaultscreen
			Form form = new Form("Net & GPS State");

            //#style formbox
            form.append(netStatusBT);
			//#style formbox
            form.append(gpsStatusBT);
			form.addCommand(cancelCmd);

			form.setCommandListener(this);
            Display.getDisplay(midlet).setCurrent(form);
		}

		public void commandAction(Command command, Displayable screen) {
			Display.getDisplay(midlet).setCurrent(midlet.getActiveApp());
		}
	}
}

