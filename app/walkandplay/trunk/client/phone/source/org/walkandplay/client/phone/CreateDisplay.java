package org.walkandplay.client.phone;


import de.enough.polish.ui.StringItem;
import de.enough.polish.util.Locale;
import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import nl.justobjects.mjox.XMLChannelListener;
import org.geotracing.client.GPSInfo;

import javax.microedition.lcdui.*;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
/*public class CreateDisplay extends DefaultDisplay  {*/
public class CreateDisplay extends DefaultDisplay implements XMLChannelListener, TracerEngineListener {
    private String gpsStatus = "disconnected";
    private String netStatus = "disconnected";
    private String status = "OK";
    private StringItem gpsStatusBT;
    private StringItem netStatusBT;
    private StringItem gameLabel = new StringItem("", "Create an new game or select one to edit");

    private TracerEngine tracerEngine;
    private TCPClient kwClient;
    private String gameId;
    private String gameName;

    private int msgNum;
    private int gpsNum;
    private int netNum;

    private boolean showGPSInfo = true;

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

        tracerEngine = new TracerEngine(aMidlet, this, false);

        connect();

        addCommand(NEW_GAME_CMD);
        addCommand(EDIT_GAME_CMD);
        addCommand(SHOW_MAP_CMD);

        append(logo);
        //#style labelinfo
        append(gameLabel);

        //#style gpsstat
        gpsStatusBT = new StringItem("gps", gpsStatus, Item.BUTTON);
        //#style netstat
        netStatusBT = new StringItem("net", netStatus, Item.BUTTON);
        gpsNum = append(gpsStatusBT);
        netNum = append(netStatusBT);
    }

    private void connect() {
        try {
            if (kwClient != null) {
                kwClient.restart();
            } else {
                kwClient = new TCPClient(midlet.getKWServer(), Integer.parseInt(midlet.getKWPort()));
                setKWClientListener(this);
                kwClient.login(midlet.getKWUser(), midlet.getKWPassword());
            }
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
        gameName = aGameName;
        gameLabel.setText("Working on game '" + aGameName + "'");
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        Log.log("** received:" + new String(aResponse.toBytes(false)));
        String tag = aResponse.getTag();
        if (tag.equals("login-rsp")) {
            try {
                Log.log("send select app");
                kwClient.setAgentKey(aResponse);
                kwClient.selectApp("geoapp", "user");
            } catch (Throwable t) {
                Log.log("Selectapp failed:" + t.getMessage());
            }
        }
    }

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append("Oops, we lost our connection. Please go back and try again.");
        connect();
    }

    public void sendRequest(JXElement aRequest) {
        try {
            Log.log("** sent: " + new String(aRequest.toBytes(false)));
            kwClient.utopia(aRequest);
        } catch (Throwable t) {
            Log.log("Exception sending " + new String(aRequest.toBytes(false)));
            // we need to reconnect!!!!
            connect();
        }
    }

    public void setKWClientListener(XMLChannelListener aListener) {
        kwClient.setListener(aListener);
    }

    void start() {
        tracerEngine.start();
    }

    void stop() {
        tracerEngine.stop();
    }

    TracerEngine getTracer() {
        return tracerEngine;
    }

    public void setGPSInfo(GPSInfo theInfo) {
        //mapViewer.setLocation(theInfo.lon.toString(), theInfo.lat.toString());
        if (!showGPSInfo) {
            return;
        }
        status = theInfo.toString();
        log(status);
    }

    public void setStatus(String s) {
        status = s;
        log(s);
    }

    public void onGPSStatus(String s) {
        gpsStatus = s;
        gpsStatusBT.setText(gpsStatus);
        //log(s);
    }


    public void onNetStatus(String s) {
        netStatus = s;
        netStatusBT.setText(netStatus);
        //log(s);
    }

    public void setHit(JXElement aHitElement) {
        
    }

    public void cls() {
        Log.log("# items: " + size());
        Log.log("gps: " + gpsNum);
        Log.log("net: " + netNum);
        Log.log("msg: " + msgNum);
        delete(msgNum);
    }

    public void log(String message) {
        cls();
        //#style formbox
        msgNum = append(message + "\n");
        //msgNum = append(new StringItem("", message + "\n"));
        Log.log(msgNum + ":" + message);
    }

    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == NEW_GAME_CMD) {
            log("creating new game");
            new NewGameDisplay(midlet, this);
        } else if (cmd == EDIT_GAME_CMD) {
            log("editing new game");
            new EditGameDisplay(midlet, this);
        } else if (cmd == ADD_ROUND_CMD) {
            log("creating new round");
            new AddRoundDisplay(midlet, this);
        } else if (cmd == ADD_PHOTO_CMD) {
            log("adding photo");
            new ImageCaptureDisplay(midlet, this, false);
        } else if (cmd == ADD_AUDIO_CMD) {
            log("adding audio");
            new AudioCaptureDisplay(midlet, this, false);
        } else if (cmd == ADD_TEXT_CMD) {
            log("adding text");
            new AddTextDisplay(midlet, this, false);
        } else if (cmd == SHOW_MAP_CMD) {
            log("show map");
            MapDisplay md = new MapDisplay(midlet, this);
            Display.getDisplay(midlet).setCurrent(md);
            md.start();
        }
    }

}

