package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import org.geotracing.client.*;
import org.geotracing.client.Log;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class PlayDisplay extends GameCanvas implements CommandListener, GPSEngineListener {
	private GoogleMap.XY xy;
	private Image mapImage;
	private Displayable prevScreen;
	private String tileBaseURL;

	private GoogleMap.LonLat lonLat;
	private GoogleMap.BBox bbox;

	private String errorMsg = "";

	private int zoom = 12;
	private Image mediumDot1, mediumDot2, mediumDot3, playerDot1, playerDot2, playerDot3, taskDot1, taskDot2, taskDot3;
	private String mapType = "streets";
	private boolean active;

	private int OFF_MAP_TOLERANCE = 15;

	private WPMidlet midlet;

	private Timer pollTimer;
	private long lastRetrievalTime = -1;
	private static long POLL_INTERVAL = 10000L;

	private int maxScore;

	private Vector gameLocations = new Vector(3);

	private final static int SHOW_LOG = 1;
	private int SHOW_STATE = 0;

	Font f;
	int fh, w, h;

	private String gpsStatus = "disconnected";
	private String netStatus = "disconnected";

	private boolean showGPSInfo = true;
	private GPSEngine gpsEngine;

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
	private Command LAST_HIT_CMD = new Command(Locale.get("play.LastHit"), Command.ITEM, 2);

	private boolean newIMMMessage;
    private String imMessage = "";
    private boolean hasCommands;

    protected TaskDisplay taskDisplay;
    protected IMDisplay imDisplay;
    protected MediumDisplay mediumDisplay;
    protected ScoreDisplay scoreDisplay;
    protected IntroDisplay introDisplay;
    protected AudioCaptureDisplay audioCaptureDisplay;
    protected AddTextDisplay addTextDisplay;
    protected ImageCaptureDisplay imageCaptureDisplay;


    private JXElement lastObject;
    private String lastObjectType;

    private boolean demoTaskSent;
    private boolean firstTime;

    public PlayDisplay(WPMidlet aMidlet) {
		super(false);

		midlet = aMidlet;
		prevScreen = Display.getDisplay(midlet).getCurrent();

		try {
			//#ifdef polish.images.directLoad
			taskDot1 = Image.createImage("/task_dot_1.png");
			taskDot2 = Image.createImage("/task_dot_2.png");
			taskDot3 = Image.createImage("/task_dot_3.png");
			mediumDot1 = Image.createImage("/medium_dot_1.png");
			mediumDot2 = Image.createImage("/medium_dot_2.png");
			mediumDot3 = Image.createImage("/medium_dot_3.png");
			//#else
			taskDot1 = scheduleImage("/task_dot_1.png");
			taskDot2 = scheduleImage("/task_dot_2.png");
			taskDot3 = scheduleImage("/task_dot_3.png");
			mediumDot1 = scheduleImage("/medium_dot_1.png");
			mediumDot2 = scheduleImage("/medium_dot_2.png");
			mediumDot3 = scheduleImage("/medium_dot_3.png");
			//#endif
		} catch (Throwable t) {
            Log.log("Could not load the images on PlayDisplay");
		}

		addCommand(BACK_CMD);
		setCommandListener(this);

	}

    public boolean isActive(){
        return active;
    }

    private void setCommands() {
		removeCommand(BACK_CMD);

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
		addCommand(LAST_HIT_CMD);
		addCommand(BACK_CMD);
		hasCommands = true;
	}

	private void removeCommands() {
		removeCommand(ZOOM_IN_CMD);
		removeCommand(ZOOM_OUT_CMD);
		removeCommand(TOGGLE_MAP_CMD);
		removeCommand(ADD_TEXT_CMD);
		removeCommand(ADD_PHOTO_CMD);
		removeCommand(ADD_AUDIO_CMD);
        addCommand(LAST_HIT_CMD);
        hasCommands = false;
	}

    private boolean hasActiveDisplays(){
        if((taskDisplay!=null && taskDisplay.isActive())
                || (mediumDisplay!=null && mediumDisplay.isActive())
                || (scoreDisplay!=null && scoreDisplay.isActive())
                || (introDisplay!=null && introDisplay.isActive())
                || (audioCaptureDisplay!=null && audioCaptureDisplay.isActive())
                || (addTextDisplay!=null && addTextDisplay.isActive())
                || (imageCaptureDisplay!=null && imageCaptureDisplay.isActive())){
            return true;
        }
        return false;
    }

    public void handlePlayLocationRsp(JXElement aResponse){
        JXElement hitElm = null;
        if (midlet.isInDemoMode()) {
            // video
            /*if (System.currentTimeMillis() % 3 == 0 && !rsp.hasChildren()) {
                Log.log("add a hit!!!!");
                JXElement hit = new JXElement("medium-hit");
                hit.setAttr("id", 831882);
                rsp.addChild(hit);
            }

            // audio
            if (System.currentTimeMillis() % 3 == 0 && !rsp.hasChildren()) {
                Log.log("add a hit!!!!");
                JXElement hit = new JXElement("medium-hit");
                hit.setAttr("id", 831815);
                rsp.addChild(hit);
            }*/

            // image
            /*if (System.currentTimeMillis() % 3 == 0 && !rsp.hasChildren()) {
                Log.log("add a hit!!!!");
                JXElement hit = new JXElement("medium-hit");
                hit.setAttr("id", 831905);
                rsp.addChild(hit);
            }

            // text
            if (System.currentTimeMillis() % 3 == 0 && !rsp.hasChildren()) {
                Log.log("add a hit!!!!");
                JXElement hit = new JXElement("medium-hit");
                hit.setAttr("id", 831820);
                rsp.addChild(hit);
            }*/

            // task
            //if (System.currentTimeMillis() % 3 == 0 && !rsp.hasChildren()) {
            if (!demoTaskSent) {
                JXElement hit = new JXElement("medium-hit");
                hit.setAttr("id", 831815);
                aResponse.addChild(hit);

                Log.log("add a hit!!!!");
                /*JXElement hit = new JXElement("task-hit");
                hit.setAttr("id", 831651);
                // open | done
                hit.setAttr("state", "open");
                // open | notok | ok
                hit.setAttr("answerstate", "open");
                // open | done
                hit.setAttr("mediastate", "open");
                aResponse.addChild(hit);*/

                demoTaskSent = true;
                hitElm = aResponse.getChildAt(0);
            }
        }else{
            hitElm = aResponse.getChildAt(0);
        }

        if (hitElm != null && (lastObject == null || (!hitElm.getAttr("id").equals(lastObject.getAttr("id"))))) {
            lastObject = hitElm;
            String t = lastObject.getTag();
            if (t.equals("task-hit")) {
                lastObjectType = "task";
                String state = lastObject.getAttr("state");
                String answerState = lastObject.getAttr("answerstate");
                String mediaState = lastObject.getAttr("mediastate");

                Util.playTone(80, 50, midlet.getVolume());
                Util.playTone(90, 250, midlet.getVolume() );

                Log.log("we found a task!!");
                if(taskDisplay == null){
                    taskDisplay = new TaskDisplay(midlet, w, this);
                }
                taskDisplay.start(lastObject.getAttr("id"), state, answerState, mediaState);
            } else if (t.equals("medium-hit")) {
                lastObjectType = "medium";
                Util.playTone(80, 50, midlet.getVolume());
                Util.playTone(90, 250, midlet.getVolume() );
                if(mediumDisplay == null){
                    mediumDisplay = new MediumDisplay(midlet, w);
                }
                mediumDisplay.start(lastObject.getAttr("id"), this);
            }
        }
    }

    public void handlePlayLocationNrsp(JXElement aResponse){

    }

    public void handleGetGameLocationsRsp(JXElement aResponse){
        gameLocations = aResponse.getChildrenByTag("record");
        // now determine the maximum attainable score
        for (int i = 0; i < gameLocations.size(); i++) {
            JXElement r = (JXElement) gameLocations.elementAt(i);
            if (r.getChildText("type").equals("task")) {
                maxScore += Integer.parseInt(r.getChildText("score"));
            }
        }
    }

    public void handleGetGameLocationsNrsp(JXElement aResponse){

    }

    public void handleCommentsForTargetRsp(JXElement aResponse){
        Vector recs = aResponse.getChildrenByTag("record");
        // if we have one or more messages and the last one is NOT sent by the mobile
        if(recs.size() >= 1){
            String msg = ((JXElement)recs.elementAt(0)).getChildText("content");
            if(imDisplay == null){
                imDisplay = new IMDisplay(midlet);
            }

            // if we have a new message and make sure it's not the last message you send yourself
            if(!imMessage.equals(msg) && !msg.equals(imDisplay.getMyMessage())){
                imMessage = msg;
                if(imDisplay.isActive()){
                    // ok so show it on the screen
                    imDisplay.start(this, imMessage);
                }else{
                    // if there are no other displays active
                    if(hasActiveDisplays()){
                        // now show that there's a new message
                        newIMMMessage = true;
                    }else if(!firstTime){
                        // we show the IM message
                        imDisplay.start(this, imMessage);
                    }
                }
            }

            firstTime = false;
        }
    }

    public void handleCommentsForTargetNrsp(JXElement aResponse){

    }

    /**
	 * User is now ready to start playing
	 */
	void start(String aColor) {
		try {
            firstTime = true;
            setFullScreenMode(true);
            
            // make sure we don't show any locations from previous games
            gameLocations = null;
            repaint();

            setColor(aColor);

            // start the traceEngine
            gpsEngine = GPSEngine.getInstance();
            gpsEngine.addListener(this);
            gpsEngine.start(midlet);

			// get the game and all game locations for this game
			getGame();
            getGameLocations();
			// start polling for IM messages
			startPoll();

			tileBaseURL = midlet.getWMSUrl();
			Display.getDisplay(midlet).setCurrent(this);
			active = true;

            show();

		} catch (Throwable t) {
			Log.log("Exception in start():" + t.getMessage());
		}
	}

    private void setColor(String aColor){
        Log.log("#############Color:" + aColor);
        try{
        //#ifdef polish.images.directLoad
        if (aColor.indexOf("red") != -1) {
            playerDot1 = Image.createImage("/icon_player_r_1.png");
            playerDot2 = Image.createImage("/icon_player_r_2.png");
            playerDot3 = Image.createImage("/icon_player_r_3.png");
        } else if (aColor.indexOf("green") != -1) {
            playerDot1 = Image.createImage("/icon_player_g_1.png");
            playerDot2 = Image.createImage("/icon_player_g_2.png");
            playerDot3 = Image.createImage("/icon_player_g_3.png");
        } else if (aColor.indexOf("blue") != -1) {
            playerDot1 = Image.createImage("/icon_player_b_1.png");
            playerDot2 = Image.createImage("/icon_player_b_2.png");
            playerDot3 = Image.createImage("/icon_player_b_3.png");
        } else if (aColor.indexOf("yellow") != -1) {
            playerDot1 = Image.createImage("/icon_player_y_1.png");
            playerDot2 = Image.createImage("/icon_player_y_2.png");
            playerDot3 = Image.createImage("/icon_player_y_3.png");
        }else if (aColor.indexOf("orange") != -1) {
            playerDot1 = Image.createImage("/icon_player_o_1.png");
            playerDot2 = Image.createImage("/icon_player_o_2.png");
            playerDot3 = Image.createImage("/icon_player_o_3.png");
        } else if (aColor.indexOf("purple") != -1) {
            playerDot1 = Image.createImage("/icon_player_p_1.png");
            playerDot2 = Image.createImage("/icon_player_p_2.png");
            playerDot3 = Image.createImage("/icon_player_p_3.png");
        }
        //#else
        if (aColor.indexOf("red") != -1) {
            playerDot1 = scheduleImage("/icon_player_r_1.png");
            playerDot2 = scheduleImage("/icon_player_r_2.png");
            playerDot3 = scheduleImage("/icon_player_r_3.png");
        } else if (aColor.indexOf("green") != -1) {
            playerDot1 = scheduleImage("/icon_player_g_1.png");
            playerDot2 = scheduleImage("/icon_player_g_2.png");
            playerDot3 = scheduleImage("/icon_player_g_3.png");
        } else if (aColor.indexOf("blue") != -1) {
            playerDot1 = scheduleImage("/icon_player_b_1.png");
            playerDot2 = scheduleImage("/icon_player_b_2.png");
            playerDot3 = scheduleImage("/icon_player_b_3.png");
        } else if (aColor.indexOf("yellow") != -1) {
            playerDot1 = scheduleImage("/icon_player_y_1.png");
            playerDot2 = scheduleImage("/icon_player_y_2.png");
            playerDot3 = scheduleImage("/icon_player_y_3.png");
        }else if (aColor.indexOf("orange") != -1) {
            playerDot1 = scheduleImage("/icon_player_o_1.png");
            playerDot2 = scheduleImage("/icon_player_o_2.png");
            playerDot3 = scheduleImage("/icon_player_o_3.png");
        } else if (aColor.indexOf("purple") != -1) {
            playerDot1 = scheduleImage("/icon_player_p_1.png");
            playerDot2 = scheduleImage("/icon_player_p_2.png");
            playerDot3 = scheduleImage("/icon_player_p_3.png");
        }
        //#endif
        }catch(Throwable t){
            Log.log("Exception setting the color");
        }
    }

    private void getGameLocations() {
		JXElement req = new JXElement("query-store-req");
		req.setAttr("cmd", "q-game-locations");
		req.setAttr("id", midlet.getPlayApp().getGameRound().getChildText("gameid"));
		midlet.getActiveApp().sendRequest(req);
	}

	private void getGame() {
		JXElement req = new JXElement("query-store-req");
		req.setAttr("cmd", "q-game");
		req.setAttr("id", midlet.getPlayApp().getGameRound().getChildText("gameid"));
		midlet.getActiveApp().sendRequest(req);
	}

	public void setLocation(String aLon, String aLat) {
		lonLat = new GoogleMap.LonLat(aLon, aLat);
		if (!hasCommands) {
			setCommands();
		}
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

	public void onGPSLocation(Vector thePoints) {
        if(thePoints == null || thePoints.size() == 0) return;

        // send the request
		JXElement req = new JXElement("play-location-req");

        req.addChild((JXElement)thePoints.elementAt(0));
        //req.addChildren(thePoints);

		midlet.getActiveApp().sendRequest(req);
	}

	public void onGPSInfo(GPSInfo theInfo) {
		//Log.log("onGPSInfo:" + theInfo.toString());
		// only start when we have good gps data!!
		/*if(gpsStatus.equals("no signal") || gpsStatus.equals("fixing") || gpsStatus.equals("bad signal")){
					return;
				}*/

		// Check for valid GPS location
		if (theInfo.lon == GPSInfo.NULL || theInfo.lat == GPSInfo.NULL) {
			// No location
			return;
		}
		setLocation(theInfo.lon.toString(), theInfo.lat.toString());
		if (!showGPSInfo) {
			return;
		}
		// TODO: usefull info but needs to be formatted better
		//gpsStatus = theInfo.toString();
	}

	public void onGPSStatus(String s) {
		Log.log("onGPSStatus:" + s);
		gpsStatus = "GPS:" + s;
		/*if(s.equals("No GPS")){
            errorMsg = "No GPS signal - go back and setup your GPS (again).";
            removeCommands();
            lonLat = null;
        }else if(s.equals("conn error")){
            errorMsg = "Problems connecting to GPS!";
            removeCommands();
            lonLat = null;
        }else if(s.equals("connecting")){
            errorMsg = "(Re)connecting to GPS...";
            removeCommands();
            lonLat = null;
        }else if(s.equals("no signal")){
            errorMsg = "Waiting for GPS signal...";
            removeCommands();
            lonLat = null;
            mapImage = null;
				}*/
		//setLocation("4.92", "52.35");
		show();
	}

    public void setNetStatus(String aNetStatus){
        netStatus = "NET:" + aNetStatus;
		show();
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

	/* <utopia-req>
       <query-store-req cmd="q-comments-for-target" target="219881" max="2"  last="true"/>
    </utopia-req> */
	private void getIMMessages() {
		JXElement req = new JXElement("query-store-req");
		req.setAttr("cmd", "q-comments-for-target");
		req.setAttr("target", midlet.getPlayApp().getGamePlayId());
		req.setAttr("max", 2);
		req.setAttr("last", true);

		lastRetrievalTime = Util.getTime();
		midlet.getActiveApp().sendRequest(req);
	}

	private void stopPoll() {
		if (pollTimer != null) {
			pollTimer.cancel();
			pollTimer = null;
		}
	}

	private void startPoll() {
		if (pollTimer != null) {
			return;
		}

		pollTimer = new Timer();
		TimerTask task = new Poller();

		// wait five seconds before executing, then
		// execute every ten seconds
		pollTimer.schedule(task, 5000, POLL_INTERVAL);
	}

	private class Poller extends TimerTask {
		public void run() {
			if (Util.getTime() - lastRetrievalTime > POLL_INTERVAL || lastRetrievalTime < 0) {
				getIMMessages();
			}
		}
	}

	public void paint(Graphics g) {
		if (f == null) {
			w = getWidth();
			h = getHeight();
			// Defeat Nokia bug ?
			if (w == 0) {
				w = 240;
			}
			if (h == 0) {
				h = 320;
			}
		}

		g.setColor(204, 204, 204);
		g.fillRect(0, 0, w, h);
		g.setColor(0, 0, 0);

		f = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
		fh = f.getHeight();
		g.setFont(f);

		try {
			// No use proceeding if we don't have location
			if (!hasLocation()) {
				String s = "Waiting for GPS location...";
				if (errorMsg.length() > 0) {
					s = errorMsg;
				}
                drawMessage(g, s, 50);
                drawBar(g);
                //repaint();
				return;
			}

			// ASSERT: we have a valid location

			// Create bbox if not present
			if (bbox == null) {
				resetMap();

				// Create bbox around our location for given zoom and w,h
				bbox = GoogleMap.createCenteredBBox(lonLat, zoom, w, h, true);

				drawMessage(g, "Loading map...", 50);
                drawBar(g);
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
								if (zoom >= 0 && zoom < 6) {
									mapImage.getGraphics().drawImage(taskDot1, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
								} else if (zoom >= 6 && zoom < 12) {
									mapImage.getGraphics().drawImage(taskDot2, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
								} else {
									mapImage.getGraphics().drawImage(taskDot3, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
								}
							} else if (loc.getChildText("type").equals("medium")) {
								if (zoom >= 0 && zoom < 6) {
									mapImage.getGraphics().drawImage(mediumDot1, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
								} else if (zoom >= 6 && zoom < 12) {
									mapImage.getGraphics().drawImage(mediumDot2, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
								} else {
									mapImage.getGraphics().drawImage(mediumDot3, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
								}
							} else {
								if (zoom >= 0 && zoom < 6) {
									mapImage.getGraphics().drawImage(mediumDot1, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
								} else if (zoom >= 6 && zoom < 12) {
									mapImage.getGraphics().drawImage(mediumDot2, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
								} else {
									mapImage.getGraphics().drawImage(mediumDot3, gameLocXY.x, gameLocXY.y, Graphics.BOTTOM | Graphics.HCENTER);
								}
							}
						}
					}
				} catch (Throwable t) {
					String s = t.getMessage();
                    if(s == null || s.equals("null")){
                        s = "Error " + t;
                    }
                    drawMessage(g, s, 50);
                    drawBar(g);
                    return;
				}
			}

			// Draw location and trace.
			//GoogleMap.XY prevXY = xy;
			xy = bbox.getPixelXY(lonLat);

			// System.out.println("xy=" + xy);
			// If we have previous point: draw line from there to current
			/*if (prevXY != null && prevXY.x < 1000) {
                // Draw trace
                Graphics mapGraphics = mapImage.getGraphics();
                mapGraphics.setColor(0, 0, 255);
                mapGraphics.drawLine(prevXY.x - 1, prevXY.y - 1, xy.x - 1, xy.y - 1);
                mapGraphics.drawLine(prevXY.x, prevXY.y, xy.x, xy.y);
            }*/

			// Draw background map
			g.drawImage(mapImage, 0, 0, Graphics.TOP | Graphics.LEFT);

			// draw the player
			if (zoom >= 0 && zoom < 6) {
				g.drawImage(playerDot1, xy.x - (playerDot1.getWidth()) / 2, xy.y - (playerDot1.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
			} else if (zoom >= 6 && zoom < 12) {
				g.drawImage(playerDot2, xy.x - (playerDot2.getWidth()) / 2, xy.y - (playerDot2.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
			} else {
				g.drawImage(playerDot3, xy.x - (playerDot3.getWidth()) / 2, xy.y - (playerDot3.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
			}

			// If moving off map refresh
			if (xy.x < OFF_MAP_TOLERANCE || w - xy.x < OFF_MAP_TOLERANCE || xy.y < OFF_MAP_TOLERANCE || h - xy.y < OFF_MAP_TOLERANCE)
			{
				resetMap();
			}

			switch (SHOW_STATE) {
				case SHOW_LOG:
                    drawMessage(g, netStatus + " | " + gpsStatus, 50);
                    /*g.drawImage(transBar, 0, h / 2 - transBar.getHeight() / 2, Graphics.TOP | Graphics.LEFT);
					g.drawString(netStatus, w / 2 - (g.getFont().stringWidth(netStatus)) / 2, h / 2 - fh, Graphics.TOP | Graphics.LEFT);
					g.drawString(gpsStatus, w / 2 - (g.getFont().stringWidth(gpsStatus)) / 2, h / 2, Graphics.TOP | Graphics.LEFT);*/
					break;
			}

            drawBar(g);

        } catch (Throwable t) {
            drawMessage(g, "Could not get a map image - please zoom in or out.", 50);
            drawBar(g);
        }
	}

    private void drawMessage(Graphics aGraphics, String aMsg, int aHeight){
        aGraphics.setColor(238, 238, 238);
        aGraphics.fillRect(0, (h/2 - aHeight/2), w, aHeight);
        aGraphics.setColor(51, 51, 51);
        aGraphics.fillRect(0, (h/2 - aHeight/2), w, 1);
        aGraphics.fillRect(0, (h/2 + aHeight/2), w, 1);
        aGraphics.setColor(0, 0, 0);
        aGraphics.drawString(aMsg, w / 2 - f.stringWidth(aMsg) / 2, h / 2, Graphics.TOP | Graphics.LEFT);
    }

    private void drawBar(Graphics aGraphics){
        aGraphics.setColor(255, 255, 255);
        aGraphics.fillRect(0, h - 22, w, h);
        aGraphics.setColor(0, 0, 0);
        aGraphics.drawString("options", 2, h - fh - 2, Graphics.TOP | Graphics.LEFT);
        if(newIMMMessage){
            String m = "*msg*";
            aGraphics.drawString(m, w - 2 - f.stringWidth(m)  , h - fh - 2, Graphics.TOP | Graphics.LEFT);
        }
    }

    public void keyPressed(int key) {
		switch (key) {
			case KEY_NUM0:
				if (hasCommands) {
					mapType = mapType.equals("sat") ? "streets" : "sat";
					resetMap();
					show();
					return;
				}
			case KEY_STAR:
				if (hasCommands) {
					zoomIn();
					return;
				}
			case KEY_POUND:
				if (hasCommands) {
					zoomOut();
				}
		}
	}

	public void commandAction(Command cmd, Displayable screen) {
		if (cmd == BACK_CMD) {
			gpsEngine.stop();
			stopPoll();						
			Display.getDisplay(midlet).setCurrent(prevScreen);
		} else if (cmd == ADD_PHOTO_CMD) {
            if(imageCaptureDisplay == null){
                imageCaptureDisplay = new ImageCaptureDisplay(midlet);
            }
            imageCaptureDisplay.start(this, true);            
		} else if (cmd == ADD_AUDIO_CMD) {
            if(audioCaptureDisplay == null){
                audioCaptureDisplay = new AudioCaptureDisplay(midlet);
            }
            audioCaptureDisplay.start(this, true);
        } else if (cmd == ADD_TEXT_CMD) {
            if(addTextDisplay == null){
                addTextDisplay = new AddTextDisplay(midlet);
            }
            addTextDisplay.start(this, true);
		} else if (cmd == ZOOM_IN_CMD) {
			zoomIn();
		} else if (cmd == ZOOM_OUT_CMD) {
			zoomOut();
		} else if (cmd == LAST_HIT_CMD) {
            Log.log("lastobject:" + lastObject);
            Log.log("lastobjecttype:" + lastObjectType);
            if(lastObject == null) return;
            
            if(lastObjectType.equals("task")){
                taskDisplay.start(lastObject.getAttr("id"), lastObject.getAttr("state"), lastObject.getAttr("answerstate"), lastObject.getAttr("mediastate"));
            }else if(lastObjectType.equals("medium")){
                mediumDisplay.start(lastObject.getAttr("id"), this);
            }
		} else if (cmd == TOGGLE_MAP_CMD) {
			mapType = mapType.equals("sat") ? "streets" : "sat";
			resetMap();
			show();
		} else if (cmd == SCORES_CMD) {
            if(scoreDisplay == null){
                scoreDisplay = new ScoreDisplay(midlet, maxScore, this);
            }
            scoreDisplay.start();
		} else if (cmd == SHOW_LOG_CMD) {
			removeCommand(SHOW_LOG_CMD);
			addCommand(HIDE_LOG_CMD);
			SHOW_STATE = SHOW_LOG;
		} else if (cmd == HIDE_LOG_CMD) {
			removeCommand(HIDE_LOG_CMD);
			addCommand(SHOW_LOG_CMD);
			SHOW_STATE = 0;
		} else if (cmd == IM_CMD) {
            if(imDisplay == null){
                imDisplay = new IMDisplay(midlet);
            }
            // once we go to the IMDisplay clear the newMessage boolean
            newIMMMessage = false;
            imDisplay.start(this, imMessage);
		} else if (cmd == SHOW_INTRO_CMD) {
            if(introDisplay == null){
                introDisplay = new IntroDisplay(midlet);
            }
            introDisplay.start(this);
		}
	}

}
