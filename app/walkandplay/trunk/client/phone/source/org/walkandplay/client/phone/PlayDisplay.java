package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import de.enough.polish.ui.Form;

import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.TextField;
import javax.microedition.media.Player;

import org.geotracing.client.*;
import org.geotracing.client.Log;
import nl.justobjects.mjox.JXElement;

import java.util.Vector;
import java.io.IOException;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
/*public class TraceDisplay extends DefaultDisplay  {*/
public class PlayDisplay extends GameCanvas implements CommandListener {
    // =====
    private GoogleMap.XY xy, prevXY;
    private String tileRef = "";
    private Image mapImage;
    private Displayable prevScreen;
    private String tileBaseURL;
    private Image tileImage;
    private MFloat tileScale;

    private int zoom = 12;
    private Image textDot, movieDot, photoDot, redDot, traceDot, taskDot, bg, locationDot;
    private String mapType = "map";
    private String lon = "0", lat = "0";
    private boolean active;
    // ====

    private WPMidlet midlet;
    private int taskId = -1;
    private JXElement task;
    private Image taskImage;
    private int mediumId = -1;
    private JXElement medium;
    private Image mediumImage;
    private Image transBar;
    private int maxScore;
    private Vector scores;
    private String comment = "";

    private Vector gameLocations = new Vector(3);

    private final static int RETRIEVING_MEDIUM = 1;
    private final static int RETRIEVING_TASK = 2;
    private final static int SHOW_MEDIUM = 3;
    private final static int SHOW_TASK = 4;
    private final static int SHOW_LOG = 5;
    private final static int SHOW_SCORES = 6;
    private final static int SHOW_INFO = 7;
    private final static int SHOW_ERROR = 8;
    private final static int RETRIEVING_MAP = 9;
    private final static int SHOW_COMMENT = 10;    
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
    private String mediumBaseURL = Net.getInstance().getURL() + "/media.srv?id=";

    private Command START_GAME_CMD = new Command(Locale.get("play.Start"), Command.ITEM, 2);
    private Command STOP_GAME_CMD = new Command(Locale.get("play.Stop"), Command.ITEM, 2);
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
    private Command IM_CMD = new Command(Locale.get("play.IM"), Command.ITEM, 2);

    public PlayDisplay(WPMidlet aMidlet) {
        super(false);
        setFullScreenMode(true);

        midlet = aMidlet;
        // make sure we stop tracing when we go into play mode
        if (midlet.traceDisplay != null) midlet.traceDisplay.stop();
        try {
            //#ifdef polish.images.directLoad
            transBar = Image.createImage("/trans_bar.png");
            redDot = Image.createImage("/red_dot.png");
            taskDot = Image.createImage("/task_dot.png");
            textDot = Image.createImage("/text_dot.png");
            movieDot = Image.createImage("/movie_dot.png");
            photoDot = Image.createImage("/photo_dot.png");
            traceDot = Image.createImage("/trace_dot.png");
            locationDot = Image.createImage("/location_dot.png");
            bg = Image.createImage("/bg.png");
            //#else
            taskDot = scheduleImage("/task_dot.png");
            redDot = scheduleImage("/red_dot.png");
            transBar = scheduleImage("/trans_bar.png");
            textDot = scheduleImage("/text_dot.png");            
            movieDot = scheduleImage("/movie_dot.png");
            photoDot = scheduleImage("/photo_dot.png");
            traceDot = scheduleImage("/trace_dot.png");
            bg = scheduleImage("/bg.png");
            locationDot = scheduleImage("/location_dot.png");
            //#endif
        } catch (Throwable t) {
            log("Could not load the images on PlayDisplay", true);
        }

        playDisplay = this;

        addCommand(START_GAME_CMD);
        addCommand(BACK_CMD);
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

            // get all game locations for this game
            getGameLocations();

            tileBaseURL = Net.getInstance().getURL() + "/map/gmap.jsp?";
            prevScreen = Display.getDisplay(midlet).getCurrent();
            Display.getDisplay(midlet).setCurrent(this);
            active = true;

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
            req.setAttr("id", midlet.getGameSchedule().getChildText("gameid"));
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
            SHOW_STATE = SHOW_ERROR;
        }
        show();
    }

    public void onNetStatus(String s) {
        try {
            if (s.indexOf("task") != -1 && SHOW_STATE != SHOW_TASK) {
                log("we found a task!!", false);
                taskId = Integer.parseInt(s.substring(s.indexOf("-") + 1, s.length()));
                log("taskid:" + taskId, false);
                SHOW_STATE = RETRIEVING_TASK;

            } else if (s.indexOf("medium") != -1 && SHOW_STATE != SHOW_MEDIUM) {
                log("we found a medium!!", false);
                mediumId = Integer.parseInt(s.substring(s.indexOf("-") + 1, s.length()));
                log("mediumid:" + mediumId, false);
                SHOW_STATE = RETRIEVING_MEDIUM;

            } else if (s.indexOf("cmt") != -1) {
                log("received a comment!!", false);
                comment = s.substring(s.indexOf("-") + 1, s.length());
                log("comment:" + comment, false);
                new IMHandler().showIM();                
            } else if (s.indexOf("error") != -1 || s.indexOf("err") != -1 || s.indexOf("ERROR") != -1) {
                log(s, true);
                SHOW_STATE = SHOW_ERROR;
            }

            netStatus = "NET:" + s;
            show();
        } catch (Throwable t) {
            log("Exception in onNetStatus:\n" + t.getMessage(), true);
        }
    }

    private void retrieveTask() {
        try {
            // retrieve the task
            new Thread(new Runnable() {
                public void run() {
                    log("retrieving the task: " + taskId, false);
                    JXElement req = new JXElement("query-store-req");
                    req.setAttr("cmd", "q-task");
                    req.setAttr("id", taskId);
                    log(new String(req.toBytes(false)), false);
                    JXElement rsp = tracerEngine.getNet().utopiaReq(req);
                    log(new String(rsp.toBytes(false)), false);
                    task = rsp.getChildByTag("record");
                    if (task != null) {
                        String mediumId = task.getChildText("mediumid");
                        String url = mediumBaseURL + mediumId + "&resize=" + (w - 10);
                        log(url, false);
                        try {
                            taskImage = Util.getImage(url);
                        } catch (Throwable t) {
                            log("Error fetching task image url: " + url, false);
                        }

                        SHOW_STATE = SHOW_TASK;
                    } else {
                        log("No task found with id " + taskId, false);
                    }
                }
            }).start();
        } catch (Throwable t) {
            log("Exception in retrieveTask:\n" + t.getMessage(), true);
        }
    }

    private void retrieveScores() {
        try {
            new Thread(new Runnable() {
                public void run() {
                    JXElement req = new JXElement("query-store-req");
                    req.setAttr("cmd", "q-scores");
                    req.setAttr("gameid", midlet.getGamePlayId());
                    log(new String(req.toBytes(false)), false);
                    JXElement rsp = tracerEngine.getNet().utopiaReq(req);
                    scores = rsp.getChildrenByTag("record");
                    log(new String(rsp.toBytes(false)), false);

                    SHOW_STATE = SHOW_SCORES;
                }
            }).start();
        } catch (Throwable t) {
            log("Exception in retrieveScores:\n" + t.getMessage(), true);
        }
    }

    private void retrieveMedium() {
        try {
            // retrieve the medium
            new Thread(new Runnable() {
                public void run() {
                    log("retrieving the medium: " + mediumId, false);
                    JXElement req = new JXElement("query-store-req");
                    req.setAttr("cmd", "q-medium");
                    req.setAttr("id", mediumId);
                    JXElement rsp = tracerEngine.getNet().utopiaReq(req);
                    medium = rsp.getChildByTag("record");
                    String type = medium.getChildText("type");
                    String url = mediumBaseURL + mediumId;
                    log(url, false);
                    if (type.equals("image")) {
                        try {
                            url += "&resize=" + (w - 10);
                            mediumImage = Util.getImage(url);
                        } catch (Throwable t) {
                            log("Error fetching image url", false);
                        }
                    } else if (type.equals("audio")) {
                        try {
                            Util.playStream(url);
                            // open up real player!!!
                            //midlet.platformRequest(url);
                        } catch (Throwable t) {
                            log("Error playing audio url", false);
                        }
                    } else if (type.equals("video")) {
                        try {
                            // open up real player!!!
                            //midlet.platformRequest(url);
                        } catch (Throwable t) {
                            log("Error fetching text url=", false);
                        }
                    } else if (type.equals("text")) {
                        try {
                            medium.setChildText("description", Util.getPage(url));
                        } catch (Throwable t) {
                            log("Error fetching text url=", false);
                        }
                    } else if (type.equals("user")) {
                        //showObject.setChildText("text", "last location of " + showObject.getChildText("name"));
                    } else {
                        //showObject.setChildText("text", type + " is not supported (yet)");
                    }

                    SHOW_STATE = SHOW_MEDIUM;
                }
            }).start();
        } catch (Throwable t) {
            log("Exception in retrieveMedium:\n" + t.getMessage(), true);
        }
    }

    private void log(String aMsg, boolean isError) {
        //System.out.println(aMsg);
        Log.log(aMsg);
        if (isError) {
            errorMsg = aMsg;
            SHOW_STATE = SHOW_ERROR;
            removeAllCommands();
            addCommand(HIDE_ERROR_CMD);
            show();
        }
    }

    private void drawAlert(Graphics aGraphics, String aMsg, boolean useBG){
        //if(useBG) aGraphics.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
        //aGraphics.drawImage(transBar, 0, h / 2 - transBar.getHeight() / 2, Graphics.TOP | Graphics.LEFT);
        aGraphics.setColor(255, 255, 255);
        aGraphics.fillRect(0, 0, w, h);
        aGraphics.setColor(0, 0, 0);
        aGraphics.drawString(aMsg, w / 2 - (aGraphics.getFont().stringWidth(aMsg)) / 2, h / 2, Graphics.VCENTER | Graphics.LEFT);
    }

    /**
     * Draws the map.
     *
     * @param g The graphics object.
     */
    public void paint(Graphics g) {
        if(f == null){
            w = getWidth();
            h = getHeight();
            // Defeat Nokia bug ?
            if (w == 0) w = 240;
            if (h == 0) h = 320;
        }
        
        f = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        fh = f.getHeight();
        g.setFont(f);

        try {
            // draw white background
            g.setColor(255, 255, 255);
            g.fillRect(0, 0, w, h);

            // calculate the tileScale only once
            if (tileScale == null) {
                tileScale = new MFloat(h).Div(GoogleMap.I_GMAP_TILE_SIZE);
            }

            if (hasLocation() && mapImage == null) {
                try {
                    //String tileSize = w + "x" + w;
                    //String tileURL = tileBaseURL + "lon=" + lon + "&lat=" + lat + "&zoom=" + zoom + "&type=" + mapType + "&format=image&size=" + tileSize;
                    String tileURL = tileBaseURL + "lon=" + lon + "&lat=" + lat + "&zoom=" + zoom + "&type=" + mapType + "&format=image&size=320x320";

                    drawAlert(g, "Fetching map image...", true);

                    // Get Google Tile image and draw on mapImage
                    Image tileImage = Util.getImage(tileURL);
                    mapImage = Image.createImage(tileImage.getWidth(), tileImage.getHeight());
                    mapImage.getGraphics().drawImage(tileImage, 0, 0, Graphics.TOP | Graphics.LEFT);

                    /*if (gameLocations != null) {
                        for (int i = 0; i < gameLocations.size(); i++) {
                            JXElement loc = (JXElement) gameLocations.elementAt(i);

                            *//*Image img;
                            String type = loc.getChildText("type");
                            if (type.equals("task")) {
                                img = locationDot;
                            } else if (type.equals("medium")) {
                                img = textDot;
                            } else {
                                img = textDot;
                            }*//*

                            String locKhRef = GoogleMap.getKeyholeRef(loc.getChildText("lon"), loc.getChildText("lat"), zoom);
                            // make sure the locations are in the same tile
                            if(locKhRef.equals(tileRef)){
                                GoogleMap.XY Gxy = GoogleMap.getPixelXY(loc.getChildText("lon"), loc.getChildText("lat"), zoom);
                                Gxy.x = (int) new MFloat(Gxy.x).Mul(tileScale).toLong();
                                Gxy.y = (int) new MFloat(Gxy.y).Mul(tileScale).toLong();
                                // draw the location icons onto the map
                                mapImage.getGraphics().drawImage(locationDot, Gxy.x, Gxy.y, Graphics.VCENTER | Graphics.HCENTER);
                            }
                        }
                    }*/

                    fetchTileInfo();
                    show();
                    return;
                } catch (Throwable t) {
                    throw new IOException(t.getMessage());
                }
            }else{
                drawAlert(g, "Starting up...", true);
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
                    g.drawImage(redDot, xy.x, xy.y, Graphics.VCENTER | Graphics.HCENTER);
                } else if (xy.x > 280) {
                    g.drawImage(mapImage, -80, 0, Graphics.TOP | Graphics.LEFT);
                    g.drawImage(redDot, xy.x - 80, xy.y, Graphics.VCENTER | Graphics.HCENTER);
                } else {
                    g.drawImage(mapImage, -40, 0, Graphics.TOP | Graphics.LEFT);
                    g.drawImage(redDot, xy.x - 40, xy.y, Graphics.VCENTER | Graphics.HCENTER);
                }

            } else {
                drawAlert(g, "Retrieving location...", true);
            }

            switch (SHOW_STATE) {
                case RETRIEVING_MEDIUM:
                    drawAlert(g, "Hit media - retrieving...", false);
                    retrieveMedium();
                    break;
                case RETRIEVING_TASK:
                    drawAlert(g, "Hit media - retrieving...", false);
                    retrieveTask();
                    break;
                case SHOW_MEDIUM:
                    new MediumHandler().showMedium();
                    break;
                case SHOW_TASK:
                    new TaskHandler().showTask();
                    break;
                case SHOW_SCORES:
                    new ScoreHandler().showScores();
                    break;
                case SHOW_LOG:
                    g.drawImage(transBar, 0, h / 2 - transBar.getHeight() / 2, Graphics.TOP | Graphics.LEFT);
                    g.drawString(netStatus, w / 2 - (g.getFont().stringWidth(netStatus)) / 2, h / 2 - fh, Graphics.TOP | Graphics.LEFT);
                    g.drawString(gpsStatus, w / 2 - (g.getFont().stringWidth(gpsStatus)) / 2, h / 2, Graphics.TOP | Graphics.LEFT);
                    break;
                case SHOW_ERROR:
                    drawAlert(g, errorMsg, false);
                    break;
                case SHOW_INFO:
                    int tH = transBar.getHeight();
                    int margin = 5;
                    int imgMargin = 30;
                    g.drawImage(transBar, 0, h / 2 - 3 / 2 * tH, Graphics.TOP | Graphics.LEFT);
                    g.drawImage(transBar, 0, h / 2 - 1 / 2 * tH, Graphics.TOP | Graphics.LEFT);
                    g.drawImage(transBar, 0, h / 2 + 3 / 2 * tH, Graphics.TOP | Graphics.LEFT);
                    g.drawImage(redDot, margin, h / 2 - 3 / 2 * tH + fh, Graphics.VCENTER | Graphics.LEFT);
                    g.drawString("this is you!", margin + imgMargin + margin, h / 2 - 3 / 2 * tH + fh, Graphics.VCENTER | Graphics.LEFT);
                    g.drawImage(taskDot, margin, h / 2 - 3 / 2 * tH + fh + 20, Graphics.VCENTER | Graphics.LEFT);
                    g.drawString("a task - complete to score points", margin + imgMargin + margin, h / 2 - 3 / 2 * tH + fh + 20, Graphics.VCENTER | Graphics.LEFT);
                    g.drawImage(textDot, margin, h / 2 - 3 / 2 * tH + fh + 40, Graphics.VCENTER | Graphics.LEFT);
                    g.drawString("media - text/video/photo/audio", margin + imgMargin + margin, h / 2 - 3 / 2 * tH + fh + 40, Graphics.VCENTER | Graphics.LEFT);
                    break;
            }
        } catch (IOException ioe) {
            drawAlert(g, "Error showing map\n" + ioe.getMessage() + "\nCannot get image. Try zooming out", true);
        } catch (Throwable t) {
            drawAlert(g, "Error showing map\n" + t.getMessage() + "\nCannot get image. Try zooming out", true);
        }
    }

    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            tracerEngine.suspendResume();
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == START_GAME_CMD) {
            log("resuming track", false);
            tracerEngine.suspendResume();
            removeCommand(START_GAME_CMD);
            addAllCommands();
        } else if (cmd == STOP_GAME_CMD) {
            log("suspending track", false);
            tracerEngine.suspendResume();
            removeAllCommands();
            addCommand(START_GAME_CMD);
        } else if (cmd == ADD_PHOTO_CMD) {
            log("adding photo", false);
            Display.getDisplay(midlet).setCurrent(new ImageCaptureDisplay(midlet));
        } else if (cmd == ADD_AUDIO_CMD) {
            log("adding audio", false);
            Display.getDisplay(midlet).setCurrent(new AudioCaptureDisplay(midlet));
        } else if (cmd == ADD_TEXT_CMD) {
            log("adding text", false);
            new AddTextHandler().addText();
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
            tileImage = null;
            show();
        } else if (cmd == SCORES_CMD) {
            log("scores", false);
            retrieveScores();
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
        } else if (cmd == SHOW_INFO_CMD) {
            log("show info", false);
            SHOW_STATE = SHOW_INFO;
            removeAllCommands();
            addCommand(HIDE_INFO_CMD);
        } else if (cmd == HIDE_INFO_CMD) {
            log("hide info", false);
            SHOW_STATE = 0;
            addAllCommands();
            removeCommand(HIDE_LOG_CMD);
        } else if (cmd == HIDE_ERROR_CMD) {
            log("hide error", false);
            SHOW_STATE = 0;
            addAllCommands();
            removeCommand(HIDE_ERROR_CMD);
        } else if (cmd == IM_CMD) {
            log("instant messaging", false);
            SHOW_STATE = SHOW_COMMENT;
            new IMHandler().showIM();
        }
    }

    private void removeAllCommands() {
        removeCommand(STOP_GAME_CMD);
        removeCommand(ADD_TEXT_CMD);
        removeCommand(ADD_PHOTO_CMD);
        removeCommand(ADD_AUDIO_CMD);
        removeCommand(ZOOM_IN_CMD);
        removeCommand(ZOOM_OUT_CMD);
        removeCommand(TOGGLE_MAP_CMD);
        removeCommand(SCORES_CMD);
        removeCommand(SHOW_LOG_CMD);
        removeCommand(SHOW_INFO_CMD);
        removeCommand(HIDE_ERROR_CMD);
        removeCommand(HIDE_INFO_CMD);
        removeCommand(HIDE_LOG_CMD);
        removeCommand(IM_CMD);
    }

    private void addAllCommands() {
        removeAllCommands();
        addCommand(STOP_GAME_CMD);
        addCommand(ADD_TEXT_CMD);
        addCommand(ADD_PHOTO_CMD);
        addCommand(ADD_AUDIO_CMD);
        addCommand(ZOOM_IN_CMD);
        addCommand(ZOOM_OUT_CMD);
        addCommand(TOGGLE_MAP_CMD);
        addCommand(SCORES_CMD);
        addCommand(SHOW_LOG_CMD);
        addCommand(SHOW_INFO_CMD);
        addCommand(IM_CMD);
    }

    private class TaskHandler implements CommandListener {
        private TextField textField;
        private String alert = "";
        private Command okCmd = new Command("OK", Command.OK, 1);
        private Command cancelCmd = new Command("Back", Command.CANCEL, 1);

        /*
          * Create the first TextBox and associate
          * the exit command and listener.
          */
        public void showTask() {
            log("now get the task!", false);

            //#style defaultscreen
            Form form = new Form("");
            //#style formbox
            form.append(task.getChildText("name"));
            //#style formbox
            form.append(task.getChildText("description"));

            form.append(taskImage);

            //#style labelinfo
            form.append("answer");
            //#style textbox
            textField = new TextField("", "", 1024, TextField.ANY);
            form.append(textField);
            if (alert.length() > 0) form.append(alert);

            form.addCommand(okCmd);
            form.addCommand(cancelCmd);
            form.setCommandListener(this);
            Display.getDisplay(midlet).setCurrent(form);
        }

        /*
          * The commandAction method is implemented by this midlet to
          * satisfy the CommandListener interface and handle the Exit action.
          */
        public void commandAction(Command command, Displayable screen) {
            if (command == okCmd) {
                if (textField.getString() == null) {
                    alert = "No text typed";
                } else {
                    // send the answer!!
                    log("retrieving the task: " + taskId, false);
                    JXElement req = new JXElement("play-answertask-req");
                    req.setAttr("id", taskId);
                    req.setAttr("answer", textField.getString());
                    log(new String(req.toBytes(false)), false);
                    JXElement rsp = tracerEngine.getNet().utopiaReq(req);
                    log(new String(rsp.toBytes(false)), false);
                    if (rsp.getTag().indexOf("-rsp") != -1) {
                        String result = rsp.getAttr("result");
                        String score = rsp.getAttr("score");
                        if (result.equals("true")) {
                            log("we've got the right answer", false);
                            alert = "Right answer! You scored " + score + " points";
                        } else {
                            log("oops wrong answer", false);
                            alert = "Wrong answer! Try again...";
                        }
                    } else {
                        alert = "something went wrong when sending the answer.\n Please try again.";
                    }
                    showTask();
                }
            } else {
                onNetStatus("Create cancel");
                task = null;
                taskId = -1;
                SHOW_STATE = 0;
                addAllCommands();
                // Set the current display of the midlet to the textBox screen
                Display.getDisplay(midlet).setCurrent(playDisplay);
            }
        }
    }

    private class MediumHandler implements CommandListener {
        private Command CANCEL_CMD = new Command("Back", Command.CANCEL, 1);
        private Command VIEW_VIDEO_CMD = new Command("View video", Command.SCREEN, 2);
        Player player;

        /*
		* Create the first TextBox and associate
		* the exit command and listener.
		*/
        public void showMedium() {
            //#style defaultscreen
            Form form = new Form("");
            String type = medium.getChildText("type");
            //#style formbox
            form.append(medium.getChildText("name"));

            if (type.equals("image")) {
                form.append(mediumImage);
            } else if (type.equals("video")) {
                //#style formbox
                form.append(medium.getChildText("description"));
                form.addCommand(VIEW_VIDEO_CMD);
            }

            form.addCommand(CANCEL_CMD);
            form.setCommandListener(this);
            Display.getDisplay(midlet).setCurrent(form);
        }

        /*
		* The commandAction method is implemented by this midlet to
		* satisfy the CommandListener interface and handle the Exit action.
		*/
        public void commandAction(Command command, Displayable screen) {
            if (command == CANCEL_CMD) {
                // Set the current display of the midlet to the textBox screen
                medium = null;
                mediumId = -1;
                SHOW_STATE = 0;
                addAllCommands();
                Display.getDisplay(midlet).setCurrent(playDisplay);
            } else if (command == VIEW_VIDEO_CMD) {
                try {
                    midlet.platformRequest(mediumBaseURL + mediumId);
                }
                catch (Throwable t) {
                    log("Exception launching the video:" + t.getMessage(), true);
                }
            }
        }

    }

    private class AddTextHandler implements CommandListener {
        private TextField tagsField;
        private TextField nameField;
        private TextField textField;
        private StringItem alertField = new StringItem("", "");
        private Command submitCmd = new Command("OK", Command.OK, 1);
        private Command cancelCmd = new Command("Back", Command.CANCEL, 1);

        /*
          * Create the first TextBox and associate
          * the exit command and listener.
          */
        public void addText() {
            //#style defaultscreen
            Form form = new Form("");
            
            //#style labelinfo
            form.append("Enter Title (opt)");

            //#style textbox
            nameField = new TextField("", "", 32, TextField.ANY);
            form.append(nameField);

            //#style labelinfo
            form.append("Enter Text");

            //#style textbox
            textField = new TextField("", "", 1024, TextField.ANY);
            form.append(textField);

            //#style labelinfo
            form.append("Enter Tags (opt)");
            //#style textbox
            tagsField = new TextField("", "", 32, TextField.ANY);
            form.append(tagsField);

            form.append(alertField);

            form.addCommand(submitCmd);
            form.addCommand(cancelCmd);

            form.setCommandListener(this);

            Display.getDisplay(midlet).setCurrent(form);
        }

        /*
          * The commandAction method is implemented by this midlet to
          * satisfy the CommandListener interface and handle the Exit action.
          */
        public void commandAction(Command command, Displayable screen) {
            if (command == submitCmd) {
                if (textField.getString() == null) {
                    alertField.setText("No text typed");
                } else {
                    String name = nameField.getString();
                    String text = textField.getString();
                    String tags = tagsField.getString();
                    if (name != null && name.length() > 0 && text != null && text.length() > 0) {
                        tracerEngine.getNet().uploadMedium(name, "text", "text/plain", Util.getTime(), text.getBytes(), false, tags);
                    } else {
                        alertField.setText("Type title and tags");
                    }
                }
            } else {
                onNetStatus("Add Text cancel");
            }

            addAllCommands();
            // Set the current display of the midlet to the textBox screen
            Display.getDisplay(midlet).setCurrent(playDisplay);
        }
    }

    private class ScoreHandler implements CommandListener {
        private Command cancelCmd = new Command("Back", Command.CANCEL, 1);

        public void showScores() {
            //#style defaultscreen
            Form form = new Form("");
            // Create the TextBox containing the "Hello,World!" message
            for (int i = 0; i < scores.size(); i++) {
                JXElement r = (JXElement) scores.elementAt(i);
                String team = r.getChildText("team");
                String points = r.getChildText("points");

                //#style formbox
                form.append(new Gauge(team, false, maxScore, Integer.parseInt(points)));
            }
            
            form.addCommand(cancelCmd);
            form.setCommandListener(this);
            Display.getDisplay(midlet).setCurrent(form);
        }

        /*
          * The commandAction method is implemented by this midlet to
          * satisfy the CommandListener interface and handle the Exit action.
          */
        public void commandAction(Command command, Displayable screen) {
            SHOW_STATE = 0;
            addAllCommands();
            Display.getDisplay(midlet).setCurrent(playDisplay);
        }
    }

    private class IMHandler implements CommandListener, NetListener {
        private StringItem inputField = new StringItem("", "");
        private TextField outputField = new TextField("", "", 32, TextField.ANY);;
        private StringItem alertField = new StringItem("", "");
        private Command submitCmd = new Command("OK", Command.OK, 1);
        private Command cancelCmd = new Command("Back", Command.CANCEL, 1);
        private Net net;

        public void showIM() {
            //#style defaultscreen
            Form form = new Form("");
            //#style labelinfo
            form.append("last message from webplayer");
            //#style formbox
            form.append(inputField);
            
            if(comment.length()>0){
                inputField.setText(comment);
            }
            //#style labelinfo
            form.append("send message to webplayer");
            //#style textbox
            form.append(outputField);
            form.append(alertField);

            net = Net.getInstance();
            if(!net.isConnected()){
                net.setProperties(midlet);
                net.setListener(this);
                net.start();
            }

            form.addCommand(submitCmd);
            form.addCommand(cancelCmd);
            form.setCommandListener(this);
            Display.getDisplay(midlet).setCurrent(form);
        }

        public void onNetInfo(String theInfo){
            System.out.println(theInfo);
        }

        public void onNetError(String aReason, Throwable anException){
            System.out.println(aReason);
        }

        public void onNetStatus(String aStatusMsg){
            System.out.println(aStatusMsg);
        }

        private void sendMsg() {
            try {
                new Thread(new Runnable() {
                    public void run() {
                        try{
                            String user = new Preferences(Net.RMS_STORE_NAME).get(Net.PROP_USER, midlet.getAppProperty(Net.PROP_USER));

                            JXElement req = new JXElement("cmt-insert-req");
                            JXElement comment = new JXElement("comment");
                            req.addChild(comment);
                            JXElement targetPerson = new JXElement("targetperson");
                            JXElement author = new JXElement("author");
                            author.setText(user);
                            comment.addChild(author);
                            JXElement content = new JXElement("content");
                            content.setText(outputField.getString());
                            comment.addChild(content);
                            log(new String(req.toBytes(false)), false);
                            JXElement rsp = tracerEngine.getNet().utopiaReq(req);
                            //JXElement rsp = net.utopiaReq(req);
                            log(new String(rsp.toBytes(false)), false);
                            if(rsp.getTag().indexOf("-rsp")!=-1){
                                alertField.setText("msg sent!");
                            }else{
                                alertField.setText("error sending msg!");
                            }
                            log(new String(rsp.toBytes(false)), false);
                        }catch(Throwable t){
                            alertField.setText(t.getMessage());
                        }
                    }
                }).start();
            } catch (Throwable t) {
                log("Exception in sendMsg:\n" + t.getMessage(), true);
            }
        }


        /*
          * The commandAction method is implemented by this midlet to
          * satisfy the CommandListener interface and handle the Exit action.
          */
        public void commandAction(Command command, Displayable screen) {
            if (command == submitCmd) {
                if (outputField.getString() == null) {
                    alertField.setText("No text typed");
                } else {
                    sendMsg();
                }
            } else if (command == cancelCmd) {
                removeAllCommands();
                addAllCommands();
                SHOW_STATE = 0;
                // Set the current display of the midlet to the textBox screen
                Display.getDisplay(midlet).setCurrent(playDisplay);
            }
        }
    }

}
