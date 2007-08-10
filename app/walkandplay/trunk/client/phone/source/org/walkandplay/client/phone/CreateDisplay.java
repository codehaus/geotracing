package org.walkandplay.client.phone;


import de.enough.polish.ui.StringItem;
import de.enough.polish.util.Locale;
import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import org.geotracing.client.*;

import javax.microedition.lcdui.*;

public class CreateDisplay extends DefaultAppDisplay implements TCPClientListener, GPSEngineListener {
    private String gpsStatus = "disconnected";
    private String netStatus = "disconnected";
    private StringItem gpsStatusBT;
    private StringItem netStatusBT;
    private StringItem gameLabel = new StringItem("", "Create an new game or select one to edit");

    private GPSEngine gpsEngine;
    private String gameId;


    private Command NEW_GAME_CMD = new Command(Locale.get("create.New"), Command.ITEM, 2);
    private Command EDIT_GAME_CMD = new Command(Locale.get("create.Edit"), Command.ITEM, 2);
    private Command ADD_ROUND_CMD = new Command(Locale.get("create.AddRound"), Command.ITEM, 2);
    private Command ADD_TEXT_CMD = new Command(Locale.get("create.AddText"), Command.ITEM, 2);
    private Command ADD_PHOTO_CMD = new Command(Locale.get("create.AddPhoto"), Command.ITEM, 2);
    private Command ADD_AUDIO_CMD = new Command(Locale.get("create.AddAudio"), Command.ITEM, 2);
    private Command SHOW_MAP_CMD = new Command(Locale.get("create.ShowMap"), Command.ITEM, 2);

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
        addCommand(SHOW_MAP_CMD);

        append(logo);
        //#style labelinfo
        append(gameLabel);

        //#style netstat
        netStatusBT = new StringItem("", netStatus, Item.BUTTON);
        append(netStatusBT);

        //#style gpsstat
        gpsStatusBT = new StringItem("", gpsStatus, Item.BUTTON);
        append(gpsStatusBT);

        connect();
    }

    private void connect() {
        try {
            tcpClient = TCPClient.getInstance();
            tcpClient.start(midlet.getKWServer(), midlet.getKWPort());
            tcpClient.addListener(this);
            tcpClient.login(midlet.getKWUser(), midlet.getKWPassword());
        } catch (Throwable t) {
            deleteAll();
            addCommand(BACK_CMD);
            //#style alertinfo
            append("We can not connect. Please check your account settings.");
        }
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
        String tag = aResponse.getTag();
        if (tag.equals("login-rsp")) {
            try {
                Log.log("send select app");
                tcpClient.setAgentKey(aResponse);
                tcpClient.selectApp(midlet.getKWApp(), midlet.getKWRole());
            } catch (Throwable t) {
                Log.log("Selectapp failed:" + t.getMessage());
            }
        }else if (tag.equals("select-app-rsp")) {
            Log.log("Now startup the gps engine: " + midlet);
            gpsEngine = new GPSEngine(midlet, this);
            Log.log("starting...");
            gpsEngine.start();
            Log.log("started...");
            addTCPClientListener(gpsEngine);
        }
    }

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append("Oops, we lost our connection. Please go back and try again.");
        //connect();
    }


    public void onGPSStatus(String s) {
        gpsStatus = s;
        gpsStatusBT.setText(gpsStatus);
    }

    public void onNetStatus(String aStatus){
        netStatus = aStatus;
        netStatusBT.setText(netStatus);
    }

    public void onStatus(String aStatus){
        onNetStatus(aStatus);
    }

    public void onHit(JXElement anElement){

    }

    public void onGPSInfo(GPSInfo theInfo) {
        gpsStatusBT.setText(theInfo.toString());
    }

    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command cmd, Displayable screen) {
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
            new ImageCaptureDisplay(midlet, this, false);
        } else if (cmd == ADD_AUDIO_CMD) {
            new AudioCaptureDisplay(midlet, this, false);
        } else if (cmd == ADD_TEXT_CMD) {
            new AddTextDisplay(midlet, this, false);
        } else if (cmd == SHOW_MAP_CMD) {
            MapDisplay md = new MapDisplay(midlet, this);
            Display.getDisplay(midlet).setCurrent(md);
            md.start();
        }
    }

}

