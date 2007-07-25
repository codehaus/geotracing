package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import nl.justobjects.mjox.JXElement;
import org.geotracing.client.*;
import org.geotracing.client.Log;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import java.util.Vector;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
/*public class TraceDisplay extends DefaultDisplay  {*/
/*public class PlayDisplay extends GameCanvas implements CommandListener, DownloadListener {*/
public class PlayDisplay extends GameCanvas implements CommandListener {
    // =====
    private GoogleMap.XY xy, prevXY;
    private String tileRef = "";
    private Image mapImage;
    private Displayable prevScreen;
    private String tileBaseURL;
    private MFloat tileScale;

    private int zoom = 12;
    private Image mediumDot, playerDot, taskDot, bg;
    private String mapType = "map";
    private String lon = "0", lat = "0";
    private boolean active;
    // ====

    private WPMidlet midlet;
    private int taskId = -1;
    private JXElement task;
    private JXElement taskHit;
    private Image taskImage;
    private int mediumId = -1;
    private JXElement medium;
    private JXElement mediumHit;
    private Image mediumImage;
    private Image transBar;
    private int maxScore;
    private Vector scores;

    /*private Gauge progressBar = new Gauge("Download Progress", false, 100, 0);
private int progressCounter;
private int progressMax = 100;*/

    private Vector gameLocations = new Vector(3);

    private final static int SHOW_TASK = 1;
    private final static int SHOW_LOG = 2;
    private final static int SHOW_SCORES = 3;
    private final static int SHOW_INFO = 4;
    private final static int SHOW_ERROR = 5;
    private int SHOW_STATE = 0;

    Font f;
    int fh, w, h;

    String gpsStatus = "disconnected";
    String netStatus = "disconnected";
    String status = "OK";
    String errorMsg = "";

    private boolean showGPSInfo = true;
    private TracerEngine tracerEngine;
    private PlayDisplay playDisplay;

    /*private Command START_GAME_CMD = new Command(Locale.get("play.Start"), Command.ITEM, 2);
private Command STOP_GAME_CMD = new Command(Locale.get("play.Stop"), Command.ITEM, 2);*/
    private Command ADD_TEXT_CMD = new Command(Locale.get("play.AddText"), Command.ITEM, 2);
    private Command ADD_PHOTO_CMD = new Command(Locale.get("play.AddPhoto"), Command.ITEM, 2);
    private Command ADD_AUDIO_CMD = new Command(Locale.get("play.AddAudio"), Command.ITEM, 2);
    private Command ZOOM_IN_CMD = new Command(Locale.get("play.ZoomIn"), Command.ITEM, 2);
    private Command ZOOM_OUT_CMD = new Command(Locale.get("play.ZoomOut"), Command.ITEM, 2);
    private Command TOGGLE_MAP_CMD = new Command(Locale.get("play.ToggleMap"), Command.ITEM, 2);
    private Command SCORES_CMD = new Command(Locale.get("play.Scores"), Command.ITEM, 2);
    private Command SHOW_LOG_CMD = new Command(Locale.get("play.ShowLog"), Command.ITEM, 2);
    private Command HIDE_LOG_CMD = new Command(Locale.get("play.HideLog"), Command.ITEM, 2);
    private Command SHOW_INFO_CMD = new Command(Locale.get("play.ShowInfo"), Command.ITEM, 2);
    private Command HIDE_INFO_CMD = new Command(Locale.get("play.HideInfo"), Command.ITEM, 2);
    private Command BACK_CMD = new Command(Locale.get("play.Back"), Command.ITEM, 2);
    private Command HIDE_ERROR_CMD = new Command(Locale.get("play.HideError"), Command.ITEM, 2);
    private Command SHOW_INTRO_CMD = new Command(Locale.get("play.ShowIntro"), Command.ITEM, 2);
    private Command HIDE_INTRO_CMD = new Command(Locale.get("play.HideIntro"), Command.ITEM, 2);
    private Command IM_CMD = new Command(Locale.get("play.IM"), Command.ITEM, 2);

    public PlayDisplay(WPMidlet aMidlet) {
        super(false);
        setFullScreenMode(true);

        midlet = aMidlet;
        prevScreen = Display.getDisplay(midlet).getCurrent();

        // make sure we stop tracing when we go into play mode
        if (midlet.traceDisplay != null) midlet.traceDisplay.stop();
        try {
            String user = new Preferences(Net.RMS_STORE_NAME).get(Net.PROP_USER, midlet.getAppProperty(Net.PROP_USER));

            //#ifdef polish.images.directLoad
            transBar = Image.createImage("/trans_bar.png");

            if (user.indexOf("red") != -1) {
                playerDot = Image.createImage("/icon_player_r.png");
            } else if (user.indexOf("green") != -1) {
                playerDot = Image.createImage("/icon_player_g.png");
            } else if (user.indexOf("blue") != -1) {
                playerDot = Image.createImage("/icon_player_b.png");
            } else if (user.indexOf("yellow") != -1) {
                playerDot = Image.createImage("/icon_player_y.png");
            }

            taskDot = Image.createImage("/task_dot.png");
            mediumDot = Image.createImage("/medium_dot.png");
            bg = Image.createImage("/bg.png");
            //#else
            taskDot = scheduleImage("/task_dot.png");

            if (user.indexOf("red") != -1) {
                playerDot = scheduleImage("/icon_player_r.png");
            } else if (user.indexOf("green") != -1) {
                playerDot = scheduleImage("/icon_player_g.png");
            } else if (user.indexOf("blue") != -1) {
                playerDot = scheduleImage("/icon_player_b.png");
            } else if (user.indexOf("yellow") != -1) {
                playerDot = scheduleImage("/icon_player_y.png");
            }

            transBar = scheduleImage("/trans_bar.png");
            mediumDot = scheduleImage("/medium_dot.png");
            bg = scheduleImage("/bg.png");
            //#endif
        } catch (Throwable t) {
            log("Could not load the images on PlayDisplay", true);
        }

        playDisplay = this;

        setCommandListener(this);

    }

    /**
     * User is now ready to start playing
     */
    void start() {
        try {
            // start the traceEngine
            tracerEngine = new TracerEngine(midlet, this);
            tracerEngine.start();
            tracerEngine.suspendResume();

            // get the game and all game locations for this game
            getGame();
            getGameLocations();
            new IntroDisplay(midlet);

            tileBaseURL = Net.getInstance().getURL() + "/map/gmap.jsp?";
            Display.getDisplay(midlet).setCurrent(this);
            active = true;

            //addCommand(BACK_CMD);

            fetchTileInfo();
            show();

        } catch (Throwable t) {
            log("Exception in start():\n" + t.getMessage(), true);
        }
    }

    private void getGameLocations() {
        try {
            JXElement req = new JXElement("query-store-req");
            req.setAttr("cmd", "q-game-locations");
            req.setAttr("id", midlet.getGameRound().getChildText("gameid"));
            log(new String(req.toBytes(false)), false);
            JXElement rsp = tracerEngine.getNet().utopiaReq(req);
            gameLocations = rsp.getChildrenByTag("record");

            // now determine the maximum attainable score
            for (int i = 0; i < gameLocations.size(); i++) {
                JXElement r = (JXElement) gameLocations.elementAt(i);
                if (r.getChildText("type").equals("task")) {
                    maxScore += Integer.parseInt(r.getChildText("score"));
                }
            }
            log("maxscore: " + maxScore, false);

            log(new String(rsp.toBytes(false)), false);
        } catch (Throwable t) {
            log("Exception in getGameLocations():\n" + t.getMessage(), true);
        }
    }

    private void getGame() {
        try {
            JXElement req = new JXElement("query-store-req");
            req.setAttr("cmd", "q-game");
            req.setAttr("id", midlet.getGameRound().getChildText("gameid"));
            log(new String(req.toBytes(false)), false);
            JXElement rsp = tracerEngine.getNet().utopiaReq(req);
            log(new String(rsp.toBytes(false)), false);
            midlet.setGame(rsp.getChildByTag("record"));
        } catch (Throwable t) {
            log("Exception in getGame():\n" + t.getMessage(), true);
        }
    }

    public void setLocation(String aLon, String aLat) {
        lon = aLon;
        lat = aLat;
        fetchTileInfo();
        show();
    }

    protected void fetchTileInfo() {
        if (!hasLocation() || !active) {
            return;
        }

        try {
            // Get pixel offset into GMap 256x256 tile
            GoogleMap.XY newTileXY = GoogleMap.getPixelXY(lon, lat, zoom);

            // Get unique tile ref (keyhole string)
            String newTileRef = GoogleMap.getKeyholeRef(lon, lat, zoom);

            // System.out.println("MT: x=" + newTileXY.x + " y=" + newTileXY.y);

            // Force refresh of mapImage when
            // no tile info (initial)
            // OR map keyhole ref changed (when zoom or moving off tile)
            if (!tileRef.equals(newTileRef)) {
                // System.out.println("refresh");
                mapImage = null;
                xy = prevXY = null;
                tileRef = newTileRef;
            }

            // Scale x,y to scaled tile image
            if (tileScale != null) {
                // Correct pixel offset with tile scale
                // Scale x,y offset of our location in mapImage
                newTileXY.x = (int) new MFloat(newTileXY.x).Mul(tileScale).toLong();
                newTileXY.y = (int) new MFloat(newTileXY.y).Mul(tileScale).toLong();

                // Remember previous (scaled) location
                prevXY = xy;

                // Set current location
                xy = newTileXY;
            }

        } catch (Throwable t) {
            log("Exception in fetchTileInfo:\n" + t.getMessage(), true);
        }
    }

    private void show() {
        if (active) {
            repaint();
        }
    }

    public boolean hasLocation() {
        return !lon.equals(("0")) && !lat.equals("0");
    }

    void stop() {
        tracerEngine.stop();
    }

    TracerEngine getTracer() {
        return tracerEngine;
    }

    public void setGPSInfo(GPSInfo theInfo) {
        setLocation(theInfo.lon.toString(), theInfo.lat.toString());
        if (!showGPSInfo) {
            return;
        }
        status = theInfo.toString();
        //show();
    }

    public void setStatus(String s) {
        status = s;
        //show();
    }

    public void onGPSStatus(String s) {
        gpsStatus = "GPS:" + s;
        if (s.indexOf("error") != -1 || s.indexOf("err") != -1 || s.indexOf("ERROR") != -1) {
            log(s, true);
            /*SHOW_STATE = SHOW_ERROR;*/
        }
        show();
    }

    public void setTaskHit(JXElement aTaskHit) {
        //<task-hit id="54232" state="open|hit|done" answerstate="open" mediastate="open"/>
        taskHit = aTaskHit;
    }

    public void setMediumHit(JXElement aMediumHit) {
        //<medium-hit id="54232" state="open|hit" />
        mediumHit = aMediumHit;
    }

    public void onNetStatus(String s) {
        try {
            if (s.indexOf("task") != -1 && SHOW_STATE != SHOW_TASK) {
                //<play-location-rsp><task-hit id="54232" state="open|hit|done" answerstate="open|done" mediastate="open|done"/></play-location-rsp>
                String state = taskHit.getAttr("state");
                if (!state.equals("done")) {
                    log("we found a task!!", false);
                    int taskId = Integer.parseInt(s.substring(s.indexOf("-") + 1, s.length()));
                    new TaskDisplay(midlet, taskId, w);
                }
            } else if (s.indexOf("medium") != -1) {
                log("we found a medium!!", false);
                int mediumId = Integer.parseInt(s.substring(s.indexOf("-") + 1, s.length()));
                new MediumDisplay(midlet, mediumId, w);
            } else if (s.indexOf("error") != -1 || s.indexOf("err") != -1 || s.indexOf("ERROR") != -1) {
                log(s, true);
                /*SHOW_STATE = SHOW_ERROR;*/
            }

            netStatus = "NET:" + s;
            show();
        } catch (Throwable t) {
            log("Exception in onNetStatus:\n" + t.getMessage(), true);
        }
    }

    private void log(String aMsg, boolean isError) {
        //System.out.println(aMsg);
        Log.log(aMsg);
        if (isError) {
            /*errorMsg = aMsg;
            SHOW_STATE = SHOW_ERROR;
            removeAllCommands();
            addCommand(HIDE_ERROR_CMD);
            show();*/
        }
    }

    /**
     * Draws the map.
     *
     * @param g The graphics object.
     */
    public void paint(Graphics g) {
        if (f == null) {
            w = getWidth();
            h = getHeight();
            // Defeat Nokia bug ?
            if (w == 0) w = 240;
            if (h == 0) h = 320;
        }

        g.setColor(255, 255, 255);
        g.fillRect(0, 0, w, h);
        g.setColor(0, 0, 0);

        f = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        fh = f.getHeight();
        g.setFont(f);

        try {

            if (tileScale == null) {
                tileScale = new MFloat(h).Div(GoogleMap.I_GMAP_TILE_SIZE);
            }

            if (hasLocation() && mapImage == null) {
                try {
                    //String tileSize = w + "x" + w;
                    //String tileURL = tileBaseURL + "lon=" + lon + "&lat=" + lat + "&zoom=" + zoom + "&type=" + mapType + "&format=image&size=" + tileSize;
                    String tileURL = tileBaseURL + "lon=" + lon + "&lat=" + lat + "&zoom=" + zoom + "&type=" + mapType + "&format=image&size=320x320";

                    g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
                    g.drawImage(transBar, 0, h / 2 - transBar.getHeight() / 2, Graphics.TOP | Graphics.LEFT);
                    String loading = "Loading map...";
                    g.drawString(loading, w / 2 - f.stringWidth(loading) / 2, h / 2, Graphics.TOP | Graphics.LEFT);

                    // Get Google Tile image and draw on mapImage
                    Image tileImage = Util.getImage(tileURL);
                    mapImage = Image.createImage(tileImage.getWidth(), tileImage.getHeight());
                    mapImage.getGraphics().drawImage(tileImage, 0, 0, Graphics.TOP | Graphics.LEFT);

                    if (gameLocations != null) {
                        for (int i = 0; i < gameLocations.size(); i++) {
                            JXElement loc = (JXElement) gameLocations.elementAt(i);

                            String locKhRef = GoogleMap.getKeyholeRef(loc.getChildText("lon"), loc.getChildText("lat"), zoom);
                            // make sure the locations are in the same tile
                            if (locKhRef.equals(tileRef)) {
                                GoogleMap.XY Gxy = GoogleMap.getPixelXY(loc.getChildText("lon"), loc.getChildText("lat"), zoom);
                                Gxy.x = (int) new MFloat(Gxy.x).Mul(tileScale).toLong();
                                Gxy.y = (int) new MFloat(Gxy.y).Mul(tileScale).toLong();
                                // draw the location icons onto the map
                                if (loc.getChildText("type").equals("task")) {
                                    mapImage.getGraphics().drawImage(taskDot, Gxy.x, Gxy.y, Graphics.BOTTOM | Graphics.HCENTER);
                                } else if (loc.getChildText("type").equals("medium")) {
                                    mapImage.getGraphics().drawImage(mediumDot, Gxy.x, Gxy.y, Graphics.BOTTOM | Graphics.HCENTER);
                                } else {
                                    mapImage.getGraphics().drawImage(mediumDot, Gxy.x, Gxy.y, Graphics.BOTTOM | Graphics.HCENTER);
                                }
                            }
                        }
                    }
                } catch (Throwable t) {
                    log(t.getMessage(), true);
                    /*String err = "error: " + t.getMessage();
                    g.drawString(err, w/2 - f.stringWidth(err)/2, h/2, Graphics.TOP | Graphics.LEFT);*/
                    return;
                }
            }

            if (xy != null) {

                // If we have previous point: draw line from there to current
                if (prevXY != null) {
                    Graphics mapGraphics = mapImage.getGraphics();
                    mapGraphics.setColor(0, 0, 255);
                    mapGraphics.drawLine(prevXY.x - 1, prevXY.y - 1, xy.x - 1, xy.y - 1);
                    mapGraphics.drawLine(prevXY.x, prevXY.y, xy.x, xy.y);
                }

                // Draw the map and user location
                if (xy.x < 40) {
                    g.drawImage(mapImage, 0, 0, Graphics.TOP | Graphics.LEFT);
                    g.drawImage(playerDot, xy.x - (playerDot.getWidth()) / 2, xy.y - (playerDot.getHeight()) / 2, Graphics.VCENTER | Graphics.HCENTER);
                } else if (xy.x > 280) {
                    g.drawImage(mapImage, -80, 0, Graphics.TOP | Graphics.LEFT);
                    g.drawImage(playerDot, xy.x - (playerDot.getWidth()) / 2 - 80, xy.y - (playerDot.getHeight()) / 2, Graphics.VCENTER | Graphics.HCENTER);
                } else {
                    g.drawImage(mapImage, -40, 0, Graphics.TOP | Graphics.LEFT);
                    g.drawImage(playerDot, xy.x - (playerDot.getWidth()) / 2 - 40, xy.y - (playerDot.getHeight()) / 2, Graphics.VCENTER | Graphics.HCENTER);
                }
            } else {
                g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
                g.drawImage(transBar, 0, h / 2 - transBar.getHeight() / 2, Graphics.TOP | Graphics.LEFT);
                String s = "Retrieving current location...";
                g.drawString(s, w / 2 - f.stringWidth(s) / 2, h / 2, Graphics.TOP | Graphics.LEFT);
            }

            switch (SHOW_STATE) {
                case SHOW_SCORES:
                    new ScoreDisplay(midlet, maxScore);
                    break;
                case SHOW_LOG:
                    g.drawImage(transBar, 0, h / 2 - transBar.getHeight() / 2, Graphics.TOP | Graphics.LEFT);
                    g.drawString(netStatus, w / 2 - (g.getFont().stringWidth(netStatus)) / 2, h / 2 - fh, Graphics.TOP | Graphics.LEFT);
                    g.drawString(gpsStatus, w / 2 - (g.getFont().stringWidth(gpsStatus)) / 2, h / 2, Graphics.TOP | Graphics.LEFT);
                    break;
                case SHOW_ERROR:
                    g.drawImage(transBar, 0, h / 2 - transBar.getHeight() / 2, Graphics.TOP | Graphics.LEFT);
                    g.drawString(errorMsg, w / 2, h / 2, Graphics.TOP | Graphics.LEFT);
                    break;
                    /*case SHOW_INFO:
                    int tH = transBar.getHeight();
                    int margin = 5;
                    int imgMargin = 30;
                    g.drawImage(transBar, 0, h / 2 - 3 / 2 * tH, Graphics.TOP | Graphics.LEFT);
                    g.drawImage(transBar, 0, h / 2 - 1 / 2 * tH, Graphics.TOP | Graphics.LEFT);
                    g.drawImage(transBar, 0, h / 2 + 3 / 2 * tH, Graphics.TOP | Graphics.LEFT);
                    g.drawImage(playerDot, margin, h / 2 - 3 / 2 * tH + fh, Graphics.VCENTER | Graphics.LEFT);
                    g.drawString("this is you!", margin + imgMargin + margin, h / 2 - 3 / 2 * tH + fh, Graphics.VCENTER | Graphics.LEFT);
                    g.drawImage(taskDot, margin, h / 2 - 3 / 2 * tH + fh + 20, Graphics.VCENTER | Graphics.LEFT);
                    g.drawString("a task - complete to score points", margin + imgMargin + margin, h / 2 - 3 / 2 * tH + fh + 20, Graphics.VCENTER | Graphics.LEFT);
                    g.drawImage(mediumDot, margin, h / 2 - 3 / 2 * tH + fh + 40, Graphics.VCENTER | Graphics.LEFT);
                    g.drawString("media - text/video/photo/audio", margin + imgMargin + margin, h / 2 - 3 / 2 * tH + fh + 40, Graphics.VCENTER | Graphics.LEFT);
                    break;*/
            }

        } /*catch (IOException ioe) {
			g.drawString("cannot get image", 10, 10, Graphics.TOP | Graphics.LEFT);
			g.drawString("try zooming out", 10, 30, Graphics.TOP | Graphics.LEFT);
		} */ catch (Throwable t) {
            log("Paint exception:" + t.getMessage(), true);
            //g.drawString("ERROR", 10, 10, Graphics.TOP | Graphics.LEFT);
            //g.drawString(t.getMessage(), 10, 30, Graphics.TOP | Graphics.LEFT);
        }
    }

    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            tracerEngine.suspendResume();
            tracerEngine.stop();
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == ADD_PHOTO_CMD) {
            log("adding photo", false);
            new ImageCaptureDisplay(midlet);
        } else if (cmd == ADD_AUDIO_CMD) {
            log("adding audio", false);
            new AudioCaptureDisplay(midlet);
        } else if (cmd == ADD_TEXT_CMD) {
            log("adding text", false);
            new AddTextDisplay(midlet);
        } else if (cmd == ZOOM_IN_CMD) {
            log("zoom in", false);
            zoom++;
            fetchTileInfo();
            show();
        } else if (cmd == ZOOM_OUT_CMD) {
            log("zoom out", false);
            zoom--;
            fetchTileInfo();
            show();
        } else if (cmd == TOGGLE_MAP_CMD) {
            log("zoom out", false);
            mapType = mapType.equals("sat") ? "map" : "sat";
            fetchTileInfo();
            mapImage = null;
            show();
        } else if (cmd == SCORES_CMD) {
            log("scores", false);
            new ScoreDisplay(midlet, maxScore);
        } else if (cmd == SHOW_LOG_CMD) {
            log("show log", false);
            SHOW_STATE = SHOW_LOG;
            removeAllCommands();
            addCommand(HIDE_LOG_CMD);
        } else if (cmd == HIDE_LOG_CMD) {
            log("hide log", false);
            SHOW_STATE = 0;
            addAllCommands();
            removeCommand(HIDE_LOG_CMD);
        } /*else if (cmd == SHOW_INFO_CMD) {
            log("show info", false);
            SHOW_STATE = SHOW_INFO;
            removeAllCommands();
            addCommand(HIDE_INFO_CMD);
        } else if (cmd == HIDE_INFO_CMD) {
            log("hide info", false);
            SHOW_STATE = 0;
            addAllCommands();
            removeCommand(HIDE_LOG_CMD);
        }*/
        else if (cmd == HIDE_ERROR_CMD) {
            log("hide error", false);
            SHOW_STATE = 0;
            addAllCommands();
            removeCommand(HIDE_ERROR_CMD);
        } else if (cmd == IM_CMD) {
            log("instant messaging", false);
            new IMDisplay(midlet);
        } else if (cmd == SHOW_INTRO_CMD) {
            log("show intro", false);
            new IntroDisplay(midlet);
        }
    }

    private void removeAllCommands() {
        //removeCommand(STOP_GAME_CMD);
        removeCommand(ADD_TEXT_CMD);
        //removeCommand(ADD_PHOTO_CMD);
        removeCommand(ADD_AUDIO_CMD);
        removeCommand(ZOOM_IN_CMD);
        removeCommand(ZOOM_OUT_CMD);
        removeCommand(TOGGLE_MAP_CMD);
        removeCommand(SCORES_CMD);
        removeCommand(SHOW_LOG_CMD);
        //removeCommand(SHOW_INFO_CMD);
        removeCommand(HIDE_ERROR_CMD);
        //removeCommand(HIDE_INFO_CMD);
        removeCommand(HIDE_LOG_CMD);
        removeCommand(IM_CMD);
        removeCommand(SHOW_INTRO_CMD);
        removeCommand(BACK_CMD);
    }

    private void addAllCommands() {
        removeAllCommands();
        addCommand(ZOOM_IN_CMD);
        addCommand(ZOOM_OUT_CMD);
        addCommand(TOGGLE_MAP_CMD);
        //addCommand(STOP_GAME_CMD);
        addCommand(ADD_TEXT_CMD);
        //addCommand(ADD_PHOTO_CMD);
        addCommand(ADD_AUDIO_CMD);
        addCommand(IM_CMD);
        addCommand(SHOW_INTRO_CMD);
        addCommand(SCORES_CMD);
        addCommand(SHOW_LOG_CMD);
        addCommand(BACK_CMD);
        //addCommand(SHOW_INFO_CMD);
    }

}
