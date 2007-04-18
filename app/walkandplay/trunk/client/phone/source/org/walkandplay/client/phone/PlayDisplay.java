package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.Gauge;
import de.enough.polish.ui.ItemCommandListener;


import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.TextField;
import javax.microedition.media.control.VideoControl;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.MediaException;

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
    private String tileRef="";
    private Image mapImage;
    private Displayable prevScreen;
    private String tileBaseURL;
    private JXElement tileInfo, prevTileInfo;
    private static final long REFRESH_INTERVAL_MILLIS = 10000L;
    private long lastRefreshMillis;
    private Image tileImage;
    private MFloat tileScale;

    private int zoom = 12;
    private Image textDot, movieDot, photoDot, redDot, traceDot, taskDot, bg;
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

    private Vector gameLocations = new Vector(3);

    private final static int RETRIEVING_MEDIUM = 1;
    private final static int RETRIEVING_TASK = 2;
    private final static int SHOW_MEDIUM = 3;
    private final static int SHOW_TASK = 4;
    private final static int SHOW_LOG = 5;
    private final static int SHOW_SCORES = 6;
    private int SHOW_STATE = 0;

    int w, h;

    String gpsStatus = "disconnected";
	String netStatus = "disconnected";
	String status = "OK";

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
    private Command BACK_CMD = new Command(Locale.get("play.Back"), Command.BACK, 1);

    public PlayDisplay(WPMidlet aMidlet) {
        super(false);
        setFullScreenMode(true);

        midlet = aMidlet;
        // make sure we stop tracing when we go into play mode
        if(midlet.traceDisplay!=null) midlet.traceDisplay.stop();
        try{
            //#ifdef polish.images.directLoad
            transBar = Image.createImage("/trans_bar.png");
            redDot = Image.createImage("/red_dot.png");
            taskDot = Image.createImage("/task_dot.png");
            textDot = Image.createImage("/text_dot.png");
            movieDot = Image.createImage("/movie_dot.png");
            photoDot = Image.createImage("/photo_dot.png");
            traceDot = Image.createImage("/trace_dot.png");
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
            //#endif
        }catch(Throwable t){
            Log.log("Could not load the images on PlayDisplay");
        }

        playDisplay = this;

        addCommand(START_GAME_CMD);
        addCommand(BACK_CMD);
        setCommandListener(this);
    }

    void start() {
        tracerEngine = new TracerEngine(midlet, this);
        tracerEngine.start();

        getGameLocations();

        tileBaseURL = Net.getInstance().getURL() + "/map/gmap.jsp?";
        prevScreen = Display.getDisplay(midlet).getCurrent();
        Display.getDisplay(midlet).setCurrent(this);
        active = true;

        fetchTileInfo();
        show();
    }

    private void getGameLocations(){
        JXElement req = new JXElement("query-store-req");
        req.setAttr("cmd", "q-game-locations");
        req.setAttr("id", midlet.getGameSchedule().getChildText("gameid"));
        log(new String(req.toBytes(false)));
        JXElement rsp = tracerEngine.getNet().utopiaReq(req);
        gameLocations = rsp.getChildrenByTag("record");

        // now determine the maximum attainable score
        for(int i=0;i<gameLocations.size();i++){
            JXElement r = (JXElement)gameLocations.elementAt(i);
            if(r.getChildText("type").equals("task")){
                maxScore += Integer.parseInt(r.getChildText("score"));
            }
        }
        log("maxscore: " + maxScore);

        log(new String(rsp.toBytes(false)));
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
			Log.log("error: MapCanvas: t=" + t + " m=" + t.getMessage());
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
        show();
    }

    public void onNetStatus(String s) {
        try{
            if(s.indexOf("task")!=-1 && SHOW_STATE!=SHOW_TASK){
                log("we found a task!!");
                taskId = Integer.parseInt(s.substring(s.indexOf("-") + 1, s.length()));
                log("taskid:" + taskId);
                SHOW_STATE = RETRIEVING_TASK;

                // make sure the commands are right
                removeCommand(HIDE_LOG_CMD);
                addCommand(SHOW_LOG_CMD);

            }else if(s.indexOf("medium")!=-1 && SHOW_STATE!=SHOW_MEDIUM){
                log("we found a medium!!");
                mediumId = Integer.parseInt(s.substring(s.indexOf("-") + 1, s.length()));
                log("mediumid:" + mediumId);
                SHOW_STATE = RETRIEVING_MEDIUM;

                // make sure the commands are right
                removeCommand(HIDE_LOG_CMD);
                addCommand(SHOW_LOG_CMD);
            }
        }catch(Throwable t){
            log("OnNetStatus exception:" + t.getMessage());
        }
        netStatus = "NET:" + s;
        show();
    }

    private void retrieveTask(){
        // retrieve the task
        new Thread(new Runnable() {
            public void run() {
                log("retrieving the task: " + taskId);
                JXElement req = new JXElement("query-store-req");
                req.setAttr("cmd", "q-task");
                req.setAttr("id", taskId);
                log(new String(req.toBytes(false)));
                JXElement rsp = tracerEngine.getNet().utopiaReq(req);
                log(new String(rsp.toBytes(false)));
                task = rsp.getChildByTag("record");
                if(task!=null){
                    String mediumId = task.getChildText("mediumid");
                    String url = mediumBaseURL + mediumId + "&resize=" + (w - 10);
                    log(url);
                    try {
                        taskImage = Util.getImage(url);
                    } catch (Throwable t) {
                        log("Error fetching task image url: " + url);
                    }

                    SHOW_STATE = SHOW_TASK;
                }else{
                    log("No task found with id " + taskId);
                }
            }
        }).start();
    }

    private void retrieveScores(){
        /*<query-store-rsp cnt="(record count)" >
           <record>
             <id>23045</id>
             <team>[teamname]</team>
             <points>fietsvissen</points>
           </record>
        </query-store-rsp>*/
        new Thread(new Runnable() {
            public void run() {
                JXElement req = new JXElement("query-store-req");
                req.setAttr("cmd", "q-scores");
                req.setAttr("gameid", midlet.getGamePlayId());
                JXElement rsp = tracerEngine.getNet().utopiaReq(req);
                scores = rsp.getChildrenByTag("record");
                log(new String(rsp.toBytes(false)));

                SHOW_STATE = SHOW_SCORES;
            }
        }).start();
    }

    private void retrieveMedium(){
        // retrieve the medium
        new Thread(new Runnable() {
            public void run() {
                log("retrieving the medium: " + mediumId);
                JXElement req = new JXElement("query-store-req");
                req.setAttr("cmd", "q-medium");
                req.setAttr("id", mediumId);
                JXElement rsp = tracerEngine.getNet().utopiaReq(req);
                medium = rsp.getChildByTag("record");
                String type = medium.getChildText("type");
                String url = mediumBaseURL + mediumId;
                log(url);
                if (type.equals("image")) {
                    try {
                        url += "&resize=" + (w - 10);
                        mediumImage = Util.getImage(url);
                    } catch (Throwable t) {
                        log("Error fetching image url");
                    }
                } else if (type.equals("audio")) {
                    try {
                        Util.playStream(url);
                        // open up real player!!!
                        //midlet.platformRequest(url);
                    } catch (Throwable t) {
                        log("Error playing audio url");
                    }
                } else if (type.equals("video")) {
                    try {
                        // open up real player!!!
                        midlet.platformRequest(url);
                    } catch (Throwable t) {
                        log("Error fetching text url=");
                    }
                } else if (type.equals("text")) {
                    try {
                        medium.setChildText("description", Util.getPage(url));
                    } catch (Throwable t) {
                        log("Error fetching text url=");
                    }
                } else if (type.equals("user")) {
                    //showObject.setChildText("text", "last location of " + showObject.getChildText("name"));
                } else {
                    //showObject.setChildText("text", type + " is not supported (yet)");
                }

                SHOW_STATE = SHOW_MEDIUM;
            }
        }).start();
    }


    private void log(String aMsg){
        System.out.println(aMsg);
    }

        /**
     * Draws the map.
     *
     * @param g The graphics object.
     */
    public void paint(Graphics g) {
        w = getWidth();
        // Defeat Nokia bug ?
        if (w == 0) {
            w = 176;
        }
        h = getHeight();
        // Defeat Nokia bug ?
        if (h == 0) {
            h = 208;
        }

        //log("dbg 1");
        Font f = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        int fh = f.getHeight();
        g.setFont(f);
        g.setColor(0, 0, 0);

        try {
            g.setColor(255, 255, 255);
            g.fillRect(0, 0, w, h);
            g.setColor(0, 0, 0);

            if (tileScale == null) {
                tileScale = new MFloat(h).Div(GoogleMap.I_GMAP_TILE_SIZE);
            }

            if (hasLocation() && mapImage == null) {
                try {
                    String tileSize = w + "x" + w;
                    //String tileURL = tileBaseURL + "lon=" + lon + "&lat=" + lat + "&zoom=" + zoom + "&type=" + mapType + "&format=image&size=" + tileSize;
                    String tileURL = tileBaseURL + "lon=" + lon + "&lat=" + lat + "&zoom=" + zoom + "&type=" + mapType + "&format=image&size=320x320";
                    g.drawImage(transBar, 0, h/2 - transBar.getHeight()/2, Graphics.TOP | Graphics.LEFT);
                    String s1 = "Fetching map image...";
                    g.drawString(s1, w/2 - (g.getFont().stringWidth(s1))/2, h/2 - fh, Graphics.TOP | Graphics.LEFT);

                    // Get Google Tile image and draw on mapImage
                    Image tileImage = Util.getImage(tileURL);
                    mapImage = Image.createImage(tileImage.getWidth(), tileImage.getHeight());
                    mapImage.getGraphics().drawImage(tileImage, 0, 0, Graphics.TOP | Graphics.LEFT);
                    fetchTileInfo();
                    repaint();
                    return;
                } catch (Throwable t) {
                    //g.drawString("error: " + t.getMessage(), 10, 30, Graphics.TOP | Graphics.LEFT);
                    throw new IOException(t.getMessage());
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

                // Draw background map

                if(xy.x < 40){
                    g.drawImage(mapImage, 0, 0, Graphics.TOP | Graphics.LEFT);
                    g.drawImage(redDot, xy.x, xy.y, Graphics.HCENTER | Graphics.VCENTER);
                }else if (xy.x > 280){
                    g.drawImage(mapImage, - 80, 0, Graphics.TOP | Graphics.LEFT);
                    g.drawImage(redDot, xy.x - 80, xy.y, Graphics.HCENTER | Graphics.VCENTER);
                }else{
                    g.drawImage(mapImage, -40, 0, Graphics.TOP | Graphics.LEFT);
                    g.drawImage(redDot, xy.x - 40, xy.y, Graphics.HCENTER | Graphics.VCENTER);
                }

            } else {
                g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
                g.drawImage(transBar, 0, h/2 - transBar.getHeight()/2, Graphics.TOP | Graphics.LEFT);
                String s = "Retrieving location...";
                g.drawString(s, w/2 - (g.getFont().stringWidth(s))/2, h/2, Graphics.TOP | Graphics.LEFT);
            }

            // now draw the gamelocations
            if(gameLocations!=null){
                for(int i=0;i<gameLocations.size();i++){
                    JXElement loc = (JXElement)gameLocations.elementAt(i);
                    String khref = GoogleMap.getKeyholeRef(loc.getChildText("lon"),loc.getChildText("lat"), zoom);
                    Image img;
                    String type = loc.getChildText("type");
                    if(type.equals("task")){
                        img = taskDot;
                    }else if(type.equals("medium")){
                        img = textDot;
                    }else{
                        img = textDot;
                    }
                    if(tileRef!=null && khref.equals(tileRef)){
                        GoogleMap.XY Gxy = GoogleMap.getPixelXY(loc.getChildText("lon"),loc.getChildText("lat"), zoom);
                        if(Gxy.x < 40){
                            g.drawImage(img, Gxy.x, Gxy.y, Graphics.HCENTER | Graphics.VCENTER);
                        }else if (Gxy.x > 280){
                            g.drawImage(img, Gxy.x - 80, Gxy.y, Graphics.HCENTER | Graphics.VCENTER);
                        }else{
                            g.drawImage(img, Gxy.x - 40, Gxy.y, Graphics.HCENTER | Graphics.VCENTER);
                        }
                    }
                }
            }

            switch(SHOW_STATE){
                case RETRIEVING_MEDIUM:
                    g.drawImage(transBar, 0, h/2 - transBar.getHeight()/2, Graphics.TOP | Graphics.LEFT);
                    String s1 = "Hit media - retrieving...";
                    g.drawString(s1, w/2 - (g.getFont().stringWidth(s1))/2, h/2 - fh, Graphics.TOP | Graphics.LEFT);
                    retrieveMedium();
                    break;
                case RETRIEVING_TASK:
                    g.drawImage(transBar, 0, h/2 - transBar.getHeight()/2, Graphics.TOP | Graphics.LEFT);
                    String s2 = "Hit a task - retrieving...";
                    g.drawString(s2, w/2 - (g.getFont().stringWidth(s2))/2, h/2 - fh, Graphics.TOP | Graphics.LEFT);
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
                    g.drawImage(transBar, 0, h/2 - transBar.getHeight()/2, Graphics.TOP | Graphics.LEFT);
                    g.drawString(netStatus, w/2 - (g.getFont().stringWidth(netStatus))/2, h/2 - fh, Graphics.TOP | Graphics.LEFT);
                    g.drawString(gpsStatus, w/2 - (g.getFont().stringWidth(gpsStatus))/2, h/2, Graphics.TOP | Graphics.LEFT);
                    break;
            }
        } catch (IOException ioe) {
            g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
            String s = "Error\n" + ioe.getMessage() + "\nCannot get image. Try zooming out";
            g.drawString(s, w/2 - (g.getFont().stringWidth(s))/2, h/2, Graphics.TOP | Graphics.LEFT);
		} catch (Throwable t) {
            g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
            String s = "Error\n" + t.getMessage();
            g.drawString(s, w/2 - (g.getFont().stringWidth(s))/2, h/2, Graphics.TOP | Graphics.LEFT);            
		}
    }

    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == START_GAME_CMD) {
            log("resuming track");
            tracerEngine.suspendResume();
            removeCommand(START_GAME_CMD);
            addCommand(ADD_TEXT_CMD);
            addCommand(ADD_PHOTO_CMD);
            addCommand(ADD_AUDIO_CMD);
            addCommand(ZOOM_IN_CMD);
            addCommand(ZOOM_OUT_CMD);
            addCommand(TOGGLE_MAP_CMD);
            addCommand(SCORES_CMD);
            addCommand(SHOW_LOG_CMD);
            addCommand(STOP_GAME_CMD);
        } else if (cmd == STOP_GAME_CMD) {
            log("suspending track");
            tracerEngine.suspendResume();
            removeCommand(STOP_GAME_CMD);
            removeCommand(ADD_TEXT_CMD);
            removeCommand(ADD_PHOTO_CMD);
            removeCommand(ADD_AUDIO_CMD);
            removeCommand(ZOOM_IN_CMD);
            removeCommand(ZOOM_OUT_CMD);
            removeCommand(TOGGLE_MAP_CMD);
            removeCommand(SCORES_CMD);
            removeCommand(SHOW_LOG_CMD);
            this.addCommand(START_GAME_CMD);
        } else if (cmd == ADD_PHOTO_CMD) {
            log("adding photo");
            Display.getDisplay(midlet).setCurrent(new ImageCapture(midlet));
        } else if (cmd == ADD_AUDIO_CMD) {
            log("adding audio");
            Display.getDisplay(midlet).setCurrent(new AudioCapture(midlet));
        } else if (cmd == ADD_TEXT_CMD) {
            log("adding text");
            new AddTextHandler().addText();
        } else if (cmd == ZOOM_IN_CMD) {
            log("zoom in");
            zoom++;
            fetchTileInfo();
            show();
        } else if (cmd == ZOOM_OUT_CMD) {
            log("zoom out");
            zoom--;
            fetchTileInfo();
            show();
        } else if (cmd == TOGGLE_MAP_CMD) {
            log("zoom out");
            mapType = mapType.equals("sat") ? "map" : "sat";
            fetchTileInfo();
            tileImage = null;
            show();
        } else if (cmd == SCORES_CMD) {
            log("scores");
            retrieveScores();
        } else if (cmd == SHOW_LOG_CMD) {
            log("show log");
            SHOW_STATE = SHOW_LOG;
            removeCommand(SHOW_LOG_CMD);
            addCommand(HIDE_LOG_CMD);
        } else if (cmd == HIDE_LOG_CMD) {
            log("hide log");
            SHOW_STATE = 0;
            addCommand(SHOW_LOG_CMD);
            removeCommand(HIDE_LOG_CMD);
        }

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
            log("now get the task!");

            //#style defaultscreen
            Form form = new Form("");
            //#style formbox
            form.append(task.getChildText("name"));
            //#style formbox
            form.append(task.getChildText("description"));

            form.append(taskImage);

            //#style smallstring
            form.append("answer");
            //#style textbox
            textField = new TextField("", "", 1024, TextField.ANY);
            form.append(textField);
            if(alert.length()>0) form.append(alert);

            form.addCommand(okCmd);
			form.addCommand(cancelCmd);

			// Set the command listener for the textbox to the current midlet
			form.setCommandListener(this);

			// Set the current display of the midlet to the textBox screen
			Display.getDisplay(midlet).setCurrent(form);
		}

		/*
		* The commandAction method is implemented by this midlet to
		* satisfy the CommandListener interface and handle the Exit action.
		*/
		public void commandAction(Command command, Displayable screen) {
			if (command == okCmd) {
                if (textField.getString() == null) {
                    alert  = "No text typed";
                }else{
                    // send the answer!!
                    log("retrieving the task: " + taskId);
                    JXElement req = new JXElement("play-answertask-req");
                    req.setAttr("id", taskId);
                    req.setAttr("answer", textField.getString());
                    log(new String(req.toBytes(false)));
                    JXElement rsp = tracerEngine.getNet().utopiaReq(req);
                    log(new String(rsp.toBytes(false)));
                    if(rsp.getTag().indexOf("-rsp")!=-1){
                        String rightAnswer = rsp.getAttr("answer");
                        String score = rsp.getAttr("score");
                        if(rightAnswer.equals("true")){
                            log("we've got the right answer");
                            alert = "Right answer! You scored " + score + " points";
                        }else{
                            log("oops wrong answer");
                            alert = "Wrong answer! Try again...";
                        }
                    }else{
                        alert = "something went wrong when sending the answer.\n Please try again.";
                    }
                    showTask();
                }
            } else {
				onNetStatus("Create cancel");
                task = null;
                taskId = -1;
                SHOW_STATE = 0;
                // Set the current display of the midlet to the textBox screen
			    Display.getDisplay(midlet).setCurrent(playDisplay);
            }
		}
	}

    private class MediumHandler implements CommandListener, ItemCommandListener {
		private Command cancelCmd = new Command("Back", Command.CANCEL, 1);
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
            /*//#style formbox*/
            //form.append(medium.getChildText("description"));

            if (type.equals("image")) {
                form.append(mediumImage);
            }else if (type.equals("video")) {
                try {
                    play(mediumBaseURL + mediumId);
                }
                catch(Throwable t) {
                    log("Exception playing the video:" + t.getMessage());
                    StringItem si = new StringItem("", mediumBaseURL + mediumId);
                    si.setAppearanceMode(Item.HYPERLINK);
                    si.setDefaultCommand(new Command("View", Command.ITEM, 1));
                    si.setItemCommandListener(this);
                    form.append(si);
                }
            }

            // Add the Exit Command to the TextBox
			form.addCommand(cancelCmd);

			// Set the command listener for the textbox to the current midlet
			form.setCommandListener(this);

			// Set the current display of the midlet to the textBox screen
			Display.getDisplay(midlet).setCurrent(form);
		}

       void play(String url) throws Exception{
          try {
             VideoControl vc;
             defplayer();
             // create a player instance
             player = Manager.createPlayer(url);
             //player.addPlayerListener(this);
             // realize the player
             player.realize();
             vc = (VideoControl)player.getControl("VideoControl");
             if(vc != null) {
                Item video = (Item)vc.initDisplayMode(vc.USE_GUI_PRIMITIVE, null);
                Form v = new Form("Playing Video...");
                StringItem si = new StringItem("Status: ", "Playing...");
                v.append(si);
                v.append(video);
                Display.getDisplay(midlet).setCurrent(v);
             }
             player.prefetch();
             player.start();
          }
          catch(Throwable t) {
             reset();
             throw new Exception("inline playback failed");
          }
       }

       void defplayer() throws MediaException {
          if (player != null) {
             if(player.getState() == Player.STARTED) {
                player.stop();
             }
             if(player.getState() == Player.PREFETCHED) {
                player.deallocate();
             }
             if(player.getState() == Player.REALIZED ||
                player.getState() == Player.UNREALIZED) {
                player.close();
             }
          }
          player = null;
       }
       void reset() {
          player = null;
       }

       void stopPlayer() {
          try {
             defplayer();
          }
          catch(MediaException me) {
          }
          reset();
       }

        /*
		* The commandAction method is implemented by this midlet to
		* satisfy the CommandListener interface and handle the Exit action.
		*/
		public void commandAction(Command command, Displayable screen) {
			// Set the current display of the midlet to the textBox screen
            medium = null;
            mediumId = -1;
            SHOW_STATE = 0;
            Display.getDisplay(midlet).setCurrent(playDisplay);
		}

        /*
		* The commandAction method is implemented by this midlet to
		* satisfy the CommandListener interface and handle the Exit action.
		*/
		public void commandAction(Command command, Item anItem) {
			log("Hit the video!!!!");
		}
    }

	private class AddTextHandler implements CommandListener {
		private TextField tagsField;
		private TextField nameField;
		private TextField textField;
		private String alert = "";
		private Command submitCmd = new Command("OK", Command.OK, 1);
		private Command cancelCmd = new Command("Back", Command.CANCEL, 1);

		/*
		* Create the first TextBox and associate
		* the exit command and listener.
		*/
		public void addText() {
            //#style defaultscreen
            Form form = new Form("");
            //#style formbox
            form.append("Enter Title (opt)");
            //#style textbox
            nameField = new TextField("", "", 32, TextField.ANY);
            //#style formbox
            form.append("Enter Text");
            //#style textbox
            textField = new TextField("", "", 1024, TextField.ANY);
            //#style formbox
            form.append("Enter Tags (opt)");
            //#style textbox
            tagsField = new TextField("", "", 32, TextField.ANY);

            form.append(nameField);
            form.append(tagsField);
            form.append(textField);
            if(alert.length()>0) form.append(alert);

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
                    alert  = "No text typed";
                }else{
                    String name = nameField.getString();
                    String text = textField.getString();
                    String tags = tagsField.getString();
                    if (name != null && name.length() > 0 && text != null && text.length() > 0) {
                        tracerEngine.getNet().uploadMedium(name, "text", "text/plain", Util.getTime(), text.getBytes(), false, tags);
                    } else {
                        setStatus("Type title and tags");
                    }
                }
            } else {
				onNetStatus("Add Text cancel");
			}

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
            for(int i=0;i<scores.size();i++){
                JXElement r = (JXElement)scores.elementAt(i);
                String team = r.getChildText("team");
                String points = r.getChildText("points");

                //#style formbox
                form.append(new Gauge(team, false, maxScore, Integer.parseInt(points)));
            }

            /*int[][] dataSequences = new int[][] {
                    new int[]{ 12, 0, 5, 20, 25, 40 },
                    new int[]{ 0, 2, 4, 8, 16, 32 },
                    new int[]{ 1, 42, 7, 12, 16, 1 }
            };

            int[] colors = new int[]{ 0xFF0000, 0x00FF00, 0x0000FF };

            //#style formbox
            ChartItem ci = new ChartItem("scores", dataSequences, colors);
            form.append(ci);
            */

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
            Display.getDisplay(midlet).setCurrent(playDisplay);
		}
	}

}
