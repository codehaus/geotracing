package org.walkandplay.client.phone;


import de.enough.polish.ui.StringItem;
import de.enough.polish.util.Locale;
import org.geotracing.client.*;

import javax.microedition.lcdui.*;

import nl.justobjects.mjox.XMLChannel;
import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannelListener;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
/*public class CreateDisplay extends DefaultDisplay  {*/
public class CreateDisplay extends DefaultDisplay implements XMLChannelListener {
    private String gpsStatus = "disconnected";
    private String netStatus = "disconnected";
    private String status = "OK";
    private StringItem gpsStatusBT;
    private StringItem netStatusBT;

    private TracerEngine tracerEngine;
    private TCPClient kwClient;

    private int msgNum;
    private int gpsNum;
    private int netNum;

    private boolean showGPSInfo = true;
    private MapDisplay mapViewer;

    private Command NEW_TRK_CMD = new Command(Locale.get("trace.New"), Command.ITEM, 2);
    private Command SUSPEND_TRK_CMD = new Command(Locale.get("trace.Suspend"), Command.ITEM, 2);
    private Command RESUME_TRK_CMD = new Command(Locale.get("trace.Resume"), Command.ITEM, 2);
    private Command ADD_TEXT_CMD = new Command(Locale.get("trace.AddText"), Command.ITEM, 2);
    private Command ADD_PHOTO_CMD = new Command(Locale.get("trace.AddPhoto"), Command.ITEM, 2);
    private Command ADD_AUDIO_CMD = new Command(Locale.get("trace.AddAudio"), Command.ITEM, 2);
    private Command SHOW_MAP_CMD = new Command(Locale.get("trace.ShowMap"), Command.ITEM, 2);
    private Command RADAR_CMD = new Command(Locale.get("trace.Radar"), Command.ITEM, 2);

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

        tracerEngine = new TracerEngine(aMidlet, this);

        connect();

        addCommand(ADD_TEXT_CMD);
        addCommand(ADD_PHOTO_CMD);
        addCommand(ADD_AUDIO_CMD);
        addCommand(SHOW_MAP_CMD);
        addCommand(RADAR_CMD);
        addCommand(NEW_TRK_CMD);
        if (tracerEngine.isPaused()) {
            addCommand(RESUME_TRK_CMD);
        } else {
            addCommand(SUSPEND_TRK_CMD);
        }

        append(logo);

        //#style gpsstat
        gpsStatusBT = new StringItem("gps", gpsStatus, Item.BUTTON);
        //#style netstat
        netStatusBT = new StringItem("net", netStatus, Item.BUTTON);
        gpsNum = append(gpsStatusBT);
        netNum = append(netStatusBT);
    }

    private void connect(){
        try{
            if(kwClient!=null){
                kwClient.restart();
            }else{
                kwClient = new TCPClient(midlet.getKWServer(), Integer.parseInt(midlet.getKWPort()));
                setKWClientListener(this);
                kwClient.login(midlet.getKWUser(), midlet.getKWPassword());
            }
        }catch(Throwable t){
            deleteAll();
            addCommand(BACK_CMD);
            //#style alertinfo
            append("We can not connect. Please check your account settings.");
        }
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        Log.log("** received:" + new String(aResponse.toBytes(false)));
        String tag = aResponse.getTag();
        if(tag.equals("login-rsp")){
            try{
                Log.log("send select app");
                kwClient.setAgentKey(aResponse);
                kwClient.selectApp("geoapp", "user");
            }catch(Throwable t){
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

    public void sendRequest(JXElement aRequest){
        try{
            Log.log("** sent: " + new String(aRequest.toBytes(false)));
            kwClient.utopia(aRequest);
        }catch(Throwable t){
            Log.log("Exception sending " + new String(aRequest.toBytes(false)));
            // we need to reconnect!!!!
            connect();
        }
    }

    public void setKWClientListener(XMLChannelListener aListener){
        kwClient.setListener(aListener);
    }

    void start() {
        mapViewer = new MapDisplay();
        tracerEngine.start();        
    }

    void stop() {
        tracerEngine.stop();
    }

    TracerEngine getTracer() {
        return tracerEngine;
    }

    public void setGPSInfo(GPSInfo theInfo) {
        mapViewer.setLocation(theInfo.lon.toString(), theInfo.lat.toString());
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
        } else if (cmd == NEW_TRK_CMD) {
            log("creating new track");
            new NewGameDisplay(midlet, this, tracerEngine);
        } else if (cmd == SUSPEND_TRK_CMD) {
            log("suspending track");
            tracerEngine.suspendResume();
            removeCommand(SUSPEND_TRK_CMD);
            addCommand(RESUME_TRK_CMD);
        } else if (cmd == RESUME_TRK_CMD) {
            log("resuming track");
            tracerEngine.suspendResume();
            removeCommand(RESUME_TRK_CMD);
            this.addCommand(SUSPEND_TRK_CMD);
        } else if (cmd == ADD_PHOTO_CMD) {
            log("adding photo");
            new ImageCaptureDisplay(midlet, this);
        } else if (cmd == ADD_AUDIO_CMD) {
            log("adding audio");
            new AudioCaptureDisplay(midlet, this);
        } else if (cmd == ADD_TEXT_CMD) {
            log("adding text");
            new AddTextDisplay(midlet, this);
        } else if (cmd == SHOW_MAP_CMD) {
            log("show map");
            mapViewer.activate(midlet);
        } else if (cmd == RADAR_CMD) {
            log("show radar");
            Display.getDisplay(midlet).setCurrent(new RadarScreen(midlet));
        }

    }

}

