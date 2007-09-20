package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import org.geotracing.client.GPSInfo;

import javax.microedition.lcdui.*;
import java.util.Vector;

public class CreateDisplay extends AppStartDisplay implements TCPClientListener, GPSEngineListener {
    private String gpsStatus = "disconnected";
    private String netStatus = "disconnected";
    private StringItem netStatusBT = new StringItem("", netStatus, Item.BUTTON);
    private StringItem gpsStatusBT = new StringItem("", netStatus, Item.BUTTON);
    private StringItem gameLabel = new StringItem("", "loading...");

    private GPSEngine gpsEngine;
    private String gameId;

    private Command NEW_GAME_CMD = new Command(Locale.get("create.New"), Command.ITEM, 2);
    private Command EDIT_GAME_CMD = new Command(Locale.get("create.Edit"), Command.ITEM, 2);
    private Command ADD_ROUND_CMD = new Command(Locale.get("create.AddRound"), Command.ITEM, 2);
    private Command ADD_TEXT_CMD = new Command(Locale.get("create.AddText"), Command.ITEM, 2);
    private Command ADD_PHOTO_CMD = new Command(Locale.get("create.AddPhoto"), Command.ITEM, 2);
    private Command ADD_AUDIO_CMD = new Command(Locale.get("create.AddAudio"), Command.ITEM, 2);
    private Command SHOW_MAP_CMD = new Command(Locale.get("create.ShowMap"), Command.ITEM, 2);
    private Command SHOW_STATE_CMD = new Command(Locale.get("create.ShowState"), Command.ITEM, 2);
    
    private AddTextDisplay addTextDisplay;
    private AudioCaptureDisplay audioCaptureDisplay;
    private AddRoundDisplay addRoundDisplay;
    private NewGameDisplay newGameDisplay;
    private EditGameDisplay editGameDisplay;
    private ImageCaptureDisplay imageCaptureDisplay;
    private MapDisplay mapDisplay;

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

        append(logo);
        //#style labelinfo
        append(gameLabel);
    }

    public void start() {
        gameLabel.setText("Connecting to GPS...");
        connect();
    }

    public void setGameId(String aGameId) {
        gameId = aGameId;
        removeAllCommands();
        addCommand(NEW_GAME_CMD);
        addCommand(EDIT_GAME_CMD);
        addCommand(SHOW_STATE_CMD);
        addCommand(ADD_ROUND_CMD);
        addCommand(ADD_TEXT_CMD);
        addCommand(ADD_PHOTO_CMD);
        addCommand(ADD_AUDIO_CMD);
        addCommand(BACK_CMD);
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameName(String aGameName) {
        gameLabel.setText("Working on game '" + aGameName + "'");
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("query-store-rsp")) {
                String cmd = rsp.getAttr("cmd");
                if (cmd.equals("q-game-locations")) {
                    if (mapDisplay != null) {
                        mapDisplay.handleGetGameLocationsRsp(rsp);
                    }
                }
            } else if (rsp.getTag().equals("query-store-nrsp")) {
                String cmd = rsp.getAttr("cmd");
                if (cmd.equals("q-game-locations")) {
                    if (mapDisplay != null) {
                        mapDisplay.handleGetGameLocationsNrsp(rsp);
                    }
                }
            } else if (rsp.getTag().equals("round-create-rsp")) {
                if (addRoundDisplay != null) {
                    addRoundDisplay.handleRoundCreateRsp(rsp);
                }
            } else if (rsp.getTag().equals("round-create-nrsp")) {
                if (addRoundDisplay != null) {
                    addRoundDisplay.handleRoundCreateNrsp(rsp);
                }
            } else if (rsp.getTag().equals("game-create-rsp")) {
                if (newGameDisplay != null) {
                    newGameDisplay.handleGameCreateRsp(rsp);
                }
            } else if (rsp.getTag().equals("game-create-nrsp")) {
                if (newGameDisplay != null) {
                    newGameDisplay.handleGameCreateNrsp(rsp);
                }
            } else if (rsp.getTag().equals("play-add-medium-rsp") || rsp.getTag().equals("game-add-medium-rsp")) {
                if (addTextDisplay != null && addTextDisplay.isActive()) {
                    addTextDisplay.handleAddMediumRsp(rsp);
                } else if (imageCaptureDisplay != null && imageCaptureDisplay.isActive()) {
                    imageCaptureDisplay.handleAddImageRsp(rsp, "Image sent successfully.");
                } else if (audioCaptureDisplay != null && audioCaptureDisplay.isActive()) {
                    audioCaptureDisplay.handleAddAudioRsp(rsp, "Audio sent successfully");
                }
            } else if (rsp.getTag().equals("play-add-medium-nrsp") || rsp.getTag().equals("game-add-medium-nrsp")) {
                if (addTextDisplay != null && addTextDisplay.isActive()) {
                    addTextDisplay.handleAddMediumNrsp(rsp);
                } else if (imageCaptureDisplay != null && imageCaptureDisplay.isActive()) {
                    imageCaptureDisplay.handleAddImageNrsp(rsp);
                } else if (audioCaptureDisplay != null && audioCaptureDisplay.isActive()) {
                    audioCaptureDisplay.handleAddMediumNrsp(rsp);
                }
            }
        }
    }

    public void onGPSLocation(Vector thePoints) {
        addCommand(SHOW_MAP_CMD);
    }

    public void onConnected() {
        gpsEngine = GPSEngine.getInstance();
        gpsEngine.addListener(this);
        gpsEngine.start(midlet);
    }

    public void onError(String anErrorMessage) {
        deleteAll();
        //#style alertinfo
        append(anErrorMessage);
    }

    public void onFatal(String anErrorMessage) {
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append(anErrorMessage);
    }

    public void onGPSStatus(String s) {
        gpsStatus = s;
        gpsStatusBT.setText("GPS:" + gpsStatus);
        Log.log("gps status:" + s);
        if (s.equals("GPS connected")) {
            addCommand(NEW_GAME_CMD);
            addCommand(EDIT_GAME_CMD);
            addCommand(SHOW_STATE_CMD);
            addCommand(BACK_CMD);
            gameLabel.setText("Create an new game or select one to edit");
        } else if (s.equals("No GPS") || s.indexOf("error") != -1) {
            //#style alertinfo
            append("No GPS signal - please go back and setup your GPS (again).");
        }
    }

    public void onNetStatus(String aStatus) {
        netStatus = aStatus;
        netStatusBT.setText("NET:" + netStatus);
        if (mapDisplay != null) {
            mapDisplay.setNetStatus(aStatus);
        }
    }

    public void onStatus(String aStatus) {
        onNetStatus(aStatus);
    }

    public void onGPSInfo(GPSInfo theInfo) {
        gpsStatusBT.setText(theInfo.toString());
    }

    public void commandAction(Command cmd, Displayable screen) {
        try {
            if (cmd == BACK_CMD) {
                if (gpsEngine != null) gpsEngine.stop();
                if (tcpClient != null) tcpClient.stop();
                Display.getDisplay(midlet).setCurrent(prevScreen);
            } else if (cmd == NEW_GAME_CMD) {
                if (newGameDisplay == null) {
                    newGameDisplay = new NewGameDisplay(midlet);
                }
                newGameDisplay.start(this);
            } else if (cmd == EDIT_GAME_CMD) {
                if (editGameDisplay == null) {
                    editGameDisplay = new EditGameDisplay(midlet);
                }
                editGameDisplay.start(this);
            } else if (cmd == ADD_ROUND_CMD) {
                if (addRoundDisplay == null) {
                    addRoundDisplay = new AddRoundDisplay(midlet);
                }
                addRoundDisplay.start(this);
            } else if (cmd == ADD_PHOTO_CMD) {
                if (imageCaptureDisplay == null) {
                    imageCaptureDisplay = new ImageCaptureDisplay(midlet);
                }
                imageCaptureDisplay.start(this, false);
            } else if (cmd == ADD_AUDIO_CMD) {
                if (audioCaptureDisplay == null) {
                    audioCaptureDisplay = new AudioCaptureDisplay(midlet);
                }
                audioCaptureDisplay.start(this, false);
            } else if (cmd == ADD_TEXT_CMD) {
                if (addTextDisplay == null) {
                    addTextDisplay = new AddTextDisplay(midlet);
                }
                addTextDisplay.start(this, false);
            } else if (cmd == SHOW_MAP_CMD) {
                if (mapDisplay == null) {
                    mapDisplay = new MapDisplay(midlet);
                }
                mapDisplay.start(this);
            } else if (cmd == SHOW_STATE_CMD) {
                new StateHandler().showState();
            }
        } catch (Throwable t) {
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

