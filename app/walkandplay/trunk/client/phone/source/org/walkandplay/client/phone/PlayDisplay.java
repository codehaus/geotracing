package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannelListener;
import nl.justobjects.mjox.XMLChannel;
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
/*public class PlayDisplay extends GameCanvas implements CommandListener, DownloadListener {*/
public class PlayDisplay extends GameCanvas implements CommandListener, XMLChannelListener {
    private GoogleMap.XY xy;
    private Image mapImage;
    private Displayable prevScreen;
    private String tileBaseURL;

    private GoogleMap.LonLat lonLat;
    private GoogleMap.BBox bbox;


    private int zoom = 12;
    private Image mediumDot1, mediumDot2, mediumDot3, playerDot1, playerDot2, playerDot3, taskDot1, taskDot2, taskDot3, bg;
    private String mapType = "map";
    private boolean active;

    private int OFF_MAP_TOLERANCE = 15;

    private WPMidlet midlet;
    private JXElement taskHit;
    private JXElement mediumHit;

    private Image transBar;
    private int maxScore;

    private Vector gameLocations = new Vector(3);

    private final static int SHOW_LOG = 1;
    private int SHOW_STATE = 0;

    Font f;
    int fh, w, h;

    String gpsStatus = "disconnected";
    String netStatus = "disconnected";
    String status = "OK";
    String errorMsg = "";

    private boolean showGPSInfo = true;
    private TracerEngine tracerEngine;

    private Command ADD_TEXT_CMD = new Command(Locale.get("play.AddText"), Command.ITEM, 2);
    private Command ADD_PHOTO_CMD = new Command(Locale.get("play.AddPhoto"), Command.ITEM, 2);
    private Command ADD_AUDIO_CMD = new Command(Locale.get("play.AddAudio"), Command.ITEM, 2);
    private Command ZOOM_IN_CMD = new Command(Locale.get("play.ZoomIn"), Command.ITEM, 2);
    private Command ZOOM_OUT_CMD = new Command(Locale.get("play.ZoomOut"), Command.ITEM, 2);
    private Command TOGGLE_MAP_CMD = new Command(Locale.get("play.ToggleMap"), Command.ITEM, 2);
    private Command SCORES_CMD = new Command(Locale.get("play.Scores"), Command.ITEM, 2);
    private Command SHOW_LOG_CMD = new Command(Locale.get("play.ShowLog"), Command.ITEM, 2);
    private Command HIDE_LOG_CMD = new Command(Locale.get("play.HideLog"), Command.ITEM, 2);
    private Command BACK_CMD = new Command(Locale.get("play.Back"), Command.ITEM, 2);    
    private Command SHOW_INTRO_CMD = new Command(Locale.get("play.ShowIntro"), Command.ITEM, 2);
    private Command IM_CMD = new Command(Locale.get("play.IM"), Command.ITEM, 2);

    public PlayDisplay(WPMidlet aMidlet) {
        super(false);
        setFullScreenMode(true);

        midlet = aMidlet;
        prevScreen = Display.getDisplay(midlet).getCurrent();
        midlet.setKWClientListener(this);

        // make sure we stop tracing when we go into play mode
        if (midlet.traceDisplay != null) midlet.traceDisplay.stop();
        try {
            String user = new Preferences(Net.RMS_STORE_NAME).get(Net.PROP_USER, midlet.getAppProperty(Net.PROP_USER));

            //#ifdef polish.images.directLoad
            if (user.indexOf("red") != -1) {
                playerDot1 = Image.createImage("/icon_player_r_1.png");
                playerDot2 = Image.createImage("/icon_player_r_2.png");
                playerDot3 = Image.createImage("/icon_player_r_3.png");
            } else if (user.indexOf("green") != -1) {
                playerDot1 = Image.createImage("/icon_player_g_1.png");
                playerDot2 = Image.createImage("/icon_player_g_2.png");
                playerDot3 = Image.createImage("/icon_player_g_3.png");
            } else if (user.indexOf("blue") != -1) {
                playerDot1 = Image.createImage("/icon_player_b_1.png");
                playerDot2 = Image.createImage("/icon_player_b_2.png");
                playerDot3 = Image.createImage("/icon_player_b_3.png");
            } else if (user.indexOf("yellow") != -1) {
                //playerDot = Image.createImage("/icon_player_y.png");
                playerDot1 = Image.createImage("/icon_player_y_1.png");
                playerDot2 = Image.createImage("/icon_player_y_2.png");
                playerDot3 = Image.createImage("/icon_player_y_3.png");
            }

            transBar = Image.createImage("/trans_bar.png");
            taskDot1 = Image.createImage("/task_dot_1.png");
            taskDot2 = Image.createImage("/task_dot_2.png");
            taskDot3 = Image.createImage("/task_dot_3.png");
            mediumDot1 = Image.createImage("/medium_dot_1.png");
            mediumDot2 = Image.createImage("/medium_dot_2.png");
            mediumDot3 = Image.createImage("/medium_dot_3.png");
            bg = Image.createImage("/bg.png");
            //#else
            if (user.indexOf("red") != -1) {
                playerDot1 = scheduleImage("/icon_player_r_1.png");
                playerDot2 = scheduleImage("/icon_player_r_2.png");
                playerDot3 = scheduleImage("/icon_player_r_3.png");
            } else if (user.indexOf("green") != -1) {
                playerDot1 = scheduleImage("/icon_player_g_1.png");
                playerDot2 = scheduleImage("/icon_player_g_2.png");
                playerDot3 = scheduleImage("/icon_player_g_3.png");
            } else if (user.indexOf("blue") != -1) {
                playerDot1 = scheduleImage("/icon_player_b_1.png");
                playerDot2 = scheduleImage("/icon_player_b_2.png");
                playerDot3 = scheduleImage("/icon_player_b_3.png");
            } else if (user.indexOf("yellow") != -1) {
                //playerDot = scheduleImage("/icon_player_y.png");
                playerDot1 = scheduleImage("/icon_player_y_1.png");
                playerDot2 = scheduleImage("/icon_player_y_2.png");
                playerDot3 = scheduleImage("/icon_player_y_3.png");
            }

            taskDot1 = scheduleImage("/task_dot_1.png");
            taskDot2 = scheduleImage("/task_dot_2.png");
            taskDot3 = scheduleImage("/task_dot_3.png");
            transBar = scheduleImage("/trans_bar.png");
            mediumDot1 = scheduleImage("/medium_dot_1.png");
            mediumDot2 = scheduleImage("/medium_dot_2.png");
            mediumDot3 = scheduleImage("/medium_dot_3.png");
            bg = scheduleImage("/bg.png");
            //#endif
        } catch (Throwable t) {
            log("Could not load the images on PlayDisplay", true);
        }

        addCommand(ZOOM_IN_CMD);
        addCommand(ZOOM_OUT_CMD);
        addCommand(TOGGLE_MAP_CMD);
        addCommand(ADD_TEXT_CMD);
        addCommand(ADD_PHOTO_CMD);
        addCommand(ADD_AUDIO_CMD);
        addCommand(IM_CMD);
        addCommand(SHOW_INTRO_CMD);
        addCommand(SCORES_CMD);
        addCommand(SHOW_LOG_CMD);
        addCommand(BACK_CMD);
        setCommandListener(this);

    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        Log.log("** received:" + new String(aResponse.toBytes(false)));
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("query-store-rsp")) {
                String cmd = rsp.getAttr("cmd");
                if(cmd!=null && cmd.equals("q-game-locations")){
                    gameLocations = rsp.getChildrenByTag("record");

                    // now determine the maximum attainable score
                    for (int i = 0; i < gameLocations.size(); i++) {
                        JXElement r = (JXElement) gameLocations.elementAt(i);
                        if (r.getChildText("type").equals("task")) {
                            maxScore += Integer.parseInt(r.getChildText("score"));
                        }
                    }
                }else if(cmd!=null && cmd.equals("q-game")){
                    midlet.setGame(rsp.getChildByTag("record"));
                }
            }
        }
    }

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        
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

            //tileBaseURL = Net.getInstance().getURL() + "/map/gmap-wms.jsp?";
            tileBaseURL = Net.getInstance().getURL() + "/map.srv?";
            Display.getDisplay(midlet).setCurrent(this);
            active = true;

            show();

        } catch (Throwable t) {
            log("Exception in start():\n" + t.getMessage(), true);
        }
    }

    private void getGameLocations() {
        JXElement req = new JXElement("query-store-req");
        req.setAttr("cmd", "q-game-locations");
        req.setAttr("id", midlet.getGameRound().getChildText("gameid"));
        midlet.sendRequest(req);
    }

    private void getGame() {
        JXElement req = new JXElement("query-store-req");
        req.setAttr("cmd", "q-game");
        req.setAttr("id", midlet.getGameRound().getChildText("gameid"));
        midlet.sendRequest(req);        
    }

    public void setLocation(String aLon, String aLat) {
        lonLat = new GoogleMap.LonLat(aLon, aLat);
        show();
    }

    private void show() {
        if (active) {
            repaint();
        }
    }

    public boolean hasLocation() {
        return lonLat != null;
    }

    void stop() {
        tracerEngine.stop();
    }

    public void setGPSInfo(GPSInfo theInfo) {
        setLocation(theInfo.lon.toString(), theInfo.lat.toString());
        if (!showGPSInfo) {
            return;
        }
        status = theInfo.toString();
    }

    public void setStatus(String s) {
        status = s;
    }

    public void onGPSStatus(String s) {
        gpsStatus = "GPS:" + s;
        if (s.indexOf("error") != -1 || s.indexOf("err") != -1 || s.indexOf("ERROR") != -1) {
            log(s, true);
            setError(s);
        }
        show();
    }

    //<task-hit id="22560" state="open|hit|done" answerstate="open" mediastate="open"/>
    public void setTaskHit(JXElement aTaskHit) {
        taskHit = aTaskHit;
        String state = taskHit.getAttr("state");
        if (!state.equals("done")) {
            log("we found a task!!", false);            
            new TaskDisplay(midlet, Integer.parseInt(taskHit.getAttr("id")), w, this);
        }
    }

    //<medium-hit id="22578" state="open|hit" />
    public void setMediumHit(JXElement aMediumHit) {
        mediumHit = aMediumHit;
        new MediumDisplay(midlet, Integer.parseInt(mediumHit.getAttr("id")), w, this);
    }

    public void onNetStatus(String s) {
        try {
            if (s.indexOf("error") != -1 || s.indexOf("err") != -1 || s.indexOf("ERROR") != -1) {
                log(s, true);
                setError(s);
            }

            netStatus = "NET:" + s;
            show();
        } catch (Throwable t) {
            log("Exception in onNetStatus:\n" + t.getMessage(), true);
        }
    }

    private void log(String aMsg, boolean isError) {
        Log.log(aMsg);
        if (isError) {
            setError(aMsg);
            show();
        }
    }

    private void setError(String aMsg){
        errorMsg = aMsg;
    }

    private void zoomIn() {
		zoom++;
		resetMap();
		show();
	}

	private void zoomOut() {
		zoom--;
		resetMap();
		show();
	}

    private void resetMap() {
		bbox = null;
		mapImage = null;
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
            // No use proceeding if we don't have location
			if (!hasLocation()) {
				g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
                g.drawImage(transBar, 0, h / 2 - transBar.getHeight() / 2, Graphics.TOP | Graphics.LEFT);
                String s = "Retrieving current location...";
                g.drawString(s, w / 2 - f.stringWidth(s) / 2, h / 2, Graphics.TOP | Graphics.LEFT);
                //repaint();
                return;
			}

			// ASSERT: we have a valid location

			// Create bbox if not present
			if (bbox == null) {
				resetMap();

				// Create bbox around our location for given zoom and w,h
				bbox = GoogleMap.createCenteredBBox(lonLat, zoom, w, h);
				if(mapImage == null) g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
                g.drawImage(transBar, 0, h / 2 - transBar.getHeight() / 2, Graphics.TOP | Graphics.LEFT);
                String loading = "Loading map...";
                g.drawString(loading, w / 2 - f.stringWidth(loading) / 2, h / 2, Graphics.TOP | Graphics.LEFT);
                //repaint();                
                return;
			}

            // Should we fetch new map image ?
			if (mapImage == null) {
				try {
					// Create WMS URL and fetch image
					String mapURL = GoogleMap.createWMSURL(tileBaseURL, bbox, mapType, w, h, "image/jpeg");
					Image wmsImage = Util.getImage(mapURL);

					// Create offscreen image
					mapImage = Image.createImage(wmsImage.getWidth(), wmsImage.getHeight());
					mapImage.getGraphics().drawImage(wmsImage, 0, 0, Graphics.TOP | Graphics.LEFT);

                    if (gameLocations != null) {
                        for (int i = 0; i < gameLocations.size(); i++) {
                            JXElement loc = (JXElement) gameLocations.elementAt(i);

                            GoogleMap.LonLat ll = new GoogleMap.LonLat(loc.getChildText("lon"), loc.getChildText("lat"));
                            GoogleMap.XY gameLocXY = bbox.getPixelXY(ll);

                            if (loc.getChildText("type").equals("task")) {
                                if(zoom >= 0 && zoom < 6){
                                    mapImage.getGraphics().drawImage(taskDot1, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
                                }else if(zoom >= 6 && zoom < 12){
                                    mapImage.getGraphics().drawImage(taskDot2, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
                                }else{
                                    mapImage.getGraphics().drawImage(taskDot3, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
                                }
                            } else if (loc.getChildText("type").equals("medium")) {
                                if(zoom >= 0 && zoom < 6){
                                    mapImage.getGraphics().drawImage(mediumDot1, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
                                }else if(zoom >= 6 && zoom < 12){
                                    mapImage.getGraphics().drawImage(mediumDot2, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
                                }else{
                                    mapImage.getGraphics().drawImage(mediumDot3, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
                                }
                            } else {
                                if(zoom >= 0 && zoom < 6){
                                    mapImage.getGraphics().drawImage(mediumDot1, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
                                }else if(zoom >= 6 && zoom < 12){
                                    mapImage.getGraphics().drawImage(mediumDot2, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
                                }else{
                                    mapImage.getGraphics().drawImage(mediumDot3, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
                                }
                            }
                        }
                    }
                } catch (Throwable t) {
					g.drawString("error: " + t.getMessage(), 10, 30, Graphics.TOP | Graphics.LEFT);
					return;
				}
			}

            // Draw location and trace.
            GoogleMap.XY prevXY = xy;
            xy = bbox.getPixelXY(lonLat);

            // System.out.println("xy=" + xy);
            // If we have previous point: draw line from there to current
            if (prevXY != null && prevXY.x < 1000) {
                // Draw trace
                Graphics mapGraphics = mapImage.getGraphics();
                mapGraphics.setColor(0, 0, 255);
                mapGraphics.drawLine(prevXY.x - 1, prevXY.y - 1, xy.x - 1, xy.y - 1);
                mapGraphics.drawLine(prevXY.x, prevXY.y, xy.x, xy.y);
            }

            // Draw background map
            g.drawImage(mapImage, 0, 0, Graphics.TOP | Graphics.LEFT);

            // draw the player            
            if(zoom >= 0 && zoom < 6){
                g.drawImage(playerDot1, xy.x - (playerDot1.getWidth()) / 2, xy.y - (playerDot1.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
            }else if(zoom >= 6 && zoom < 12){
                g.drawImage(playerDot2, xy.x - (playerDot2.getWidth()) / 2, xy.y - (playerDot2.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
            }else{
                g.drawImage(playerDot3, xy.x - (playerDot3.getWidth()) / 2, xy.y - (playerDot3.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
            }

            // If moving off map refresh
            if (xy.x < OFF_MAP_TOLERANCE || w - xy.x < OFF_MAP_TOLERANCE || xy.y < OFF_MAP_TOLERANCE || h - xy.y < OFF_MAP_TOLERANCE){
                resetMap();
            }

            switch (SHOW_STATE) {
                case SHOW_LOG:
                    g.drawImage(transBar, 0, h / 2 - transBar.getHeight() / 2, Graphics.TOP | Graphics.LEFT);
                    g.drawString(netStatus, w / 2 - (g.getFont().stringWidth(netStatus)) / 2, h / 2 - fh, Graphics.TOP | Graphics.LEFT);
                    g.drawString(gpsStatus, w / 2 - (g.getFont().stringWidth(gpsStatus)) / 2, h / 2, Graphics.TOP | Graphics.LEFT);
                    break;
            }

        } catch (Throwable t) {
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
            new ImageCaptureDisplay(midlet, this);
        } else if (cmd == ADD_AUDIO_CMD) {
            new AudioCaptureDisplay(midlet);
        } else if (cmd == ADD_TEXT_CMD) {
            new AddTextDisplay(midlet, this);            
        } else if (cmd == ZOOM_IN_CMD) {
            zoomIn();
        } else if (cmd == ZOOM_OUT_CMD) {
            zoomOut();
        } else if (cmd == TOGGLE_MAP_CMD) {
            mapType = mapType.equals("sat") ? "map" : "sat";
            resetMap();
            show();
        } else if (cmd == SCORES_CMD) {
            new ScoreDisplay(midlet, maxScore, this);
        } else if (cmd == SHOW_LOG_CMD) {
            removeCommand(SHOW_LOG_CMD);
            addCommand(HIDE_LOG_CMD);
            SHOW_STATE = SHOW_LOG;
        } else if (cmd == HIDE_LOG_CMD) {
            removeCommand(HIDE_LOG_CMD);
            addCommand(SHOW_LOG_CMD);
            SHOW_STATE = 0;
        } else if (cmd == IM_CMD) {
            new IMDisplay(midlet, this);
        } else if (cmd == SHOW_INTRO_CMD) {
            new IntroDisplay(midlet, this);
        }
    }

}
