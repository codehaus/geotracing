package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import org.geotracing.client.*;
import org.geotracing.client.Log;
import org.walkandplay.client.phone.GPSEngineListener;
import org.walkandplay.client.phone.TCPClientListener;
import org.walkandplay.client.phone.GPSEngine;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import java.util.Vector;

/**
 * SHows moving dot on map.
 */
public class MapDisplay extends GameCanvas implements CommandListener, TCPClientListener, GPSEngineListener {
    private Image mapImage;
    private Displayable prevScreen;
    private String tileBaseURL;

    private GoogleMap.LonLat lonLat;
    private GoogleMap.BBox bbox;

    private GoogleMap.XY xy;

    private int zoom = 12;
    private Image mediumDot1, mediumDot2, mediumDot3, playerDot1, playerDot2, playerDot3, taskDot1, taskDot2, taskDot3;
    private String mapType = "map";
    private boolean active;

    private int OFF_MAP_TOLERANCE = 15;

    private WPMidlet midlet;

    private Vector gameLocations;

    Font f;
    int fh, w, h;

    String gpsStatus = "disconnected";
    String netStatus = "disconnected";
    String status = "OK";
    String errorMsg = "";

    private boolean showGPSInfo = true;

    private Command ZOOM_IN_CMD = new Command(Locale.get("play.ZoomIn"), Command.ITEM, 2);
    private Command ZOOM_OUT_CMD = new Command(Locale.get("play.ZoomOut"), Command.ITEM, 2);
    private Command TOGGLE_MAP_CMD = new Command(Locale.get("play.ToggleMap"), Command.ITEM, 2);
    private Command BACK_CMD = new Command("Back", Command.ITEM, 2);

    public MapDisplay(WPMidlet aMidlet) {
        super(false);
        setFullScreenMode(true);

        midlet = aMidlet;

        try {
            //#ifdef polish.images.directLoad
            playerDot1 = Image.createImage("/icon_player_r_1.png");
            playerDot2 = Image.createImage("/icon_player_r_2.png");
            playerDot3 = Image.createImage("/icon_player_r_3.png");
            taskDot1 = Image.createImage("/task_dot_1.png");
            taskDot2 = Image.createImage("/task_dot_2.png");
            taskDot3 = Image.createImage("/task_dot_3.png");
            mediumDot1 = Image.createImage("/medium_dot_1.png");
            mediumDot2 = Image.createImage("/medium_dot_2.png");
            mediumDot3 = Image.createImage("/medium_dot_3.png");

            //#else
            playerDot1 = scheduleImage("/icon_player_r_1.png");
            playerDot2 = scheduleImage("/icon_player_r_2.png");
            playerDot3 = scheduleImage("/icon_player_r_3.png");
            taskDot1 = scheduleImage("/task_dot_1.png");
            taskDot2 = scheduleImage("/task_dot_2.png");
            taskDot3 = scheduleImage("/task_dot_3.png");
            mediumDot1 = scheduleImage("/medium_dot_1.png");
            mediumDot2 = scheduleImage("/medium_dot_2.png");
            mediumDot3 = scheduleImage("/medium_dot_3.png");

            //#endif
        } catch (Throwable t) {
            log("Could not load the images on PlayDisplay", true);
        }

        addCommand(ZOOM_IN_CMD);
        addCommand(ZOOM_OUT_CMD);
        addCommand(TOGGLE_MAP_CMD);
        addCommand(BACK_CMD);
        setCommandListener(this);

    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        Log.log("MapDisplay accept: " + new String(aResponse.toBytes(false)));
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("query-store-rsp")) {
                String cmd = rsp.getAttr("cmd");
                if(cmd.equals("q-game-locations")){
                    Log.log("setting the gamelocations");
                    gameLocations = rsp.getChildrenByTag("record");
                    // only now show
                    Display.getDisplay(midlet).setCurrent(this);
                    active = true;
                    show();
                }
            }
        }
    }


    public void onConnected(){

    }

    public void onError(String anErrorMessage){
        
    }

    public void onFatal(){
        midlet.getActiveApp().exit();
        Display.getDisplay(midlet).setCurrent(midlet.getActiveApp());
    }

    /**
     * User is now ready to start playing
     */
    void start(Displayable aPrevScreen) {
        try {
            prevScreen = aPrevScreen;
            midlet.getActiveApp().addTCPClientListener(this);
            Display.getDisplay(midlet).setCurrent(this);
            GPSEngine.getInstance().addListener(this);

            // get all game locations for this game
            String gameId = midlet.getCreateApp().getGameId();
            if(gameId !=null && gameId.length() >0){
                if (gameId != null && gameId.length() > 0) {
                    getGameLocations(gameId);
                }
            }

            tileBaseURL = midlet.getWMSUrl();
        } catch (Throwable t) {
            log("Exception in start():\n" + t.getMessage(), true);
        }
    }

    private void getGameLocations(String aGameId) {
        JXElement req = new JXElement("query-store-req");
        req.setAttr("cmd", "q-game-locations");
        req.setAttr("id", aGameId);
        midlet.getActiveApp().sendRequest(req);
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

    }

    public void onGPSLocation(Vector thePoints) {

    }

    public void onGPSInfo(GPSInfo theInfo) {
        setLocation(theInfo.lon.toString(), theInfo.lat.toString());
        if (!showGPSInfo) {
            return;
        }
        status = theInfo.toString();
    }

    public void onStatus(String s) {
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

    private void setError(String aMsg) {
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
				bbox = GoogleMap.createCenteredBBox(lonLat, zoom, w, h);
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
                        s = "Could not get a map image - please zoom in or out.";
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

            drawBar(g);

        } catch (Throwable t) {
            log("Paint exception:" + t.getMessage(), true);
        }
    }

    private void drawMessage(Graphics aGraphics, String aMsg, int aHeight){
        aGraphics.setColor(238, 238, 238);
        aGraphics.fillRect(0, h/2 - aHeight/2, w, h/2 + aHeight/2);
        aGraphics.setColor(0, 0, 0);
        aGraphics.drawString(aMsg, w / 2 - f.stringWidth(aMsg) / 2, h / 2, Graphics.TOP | Graphics.LEFT);
    }

    private void drawBar(Graphics aGraphics){
        aGraphics.setColor(255, 255, 255);
        aGraphics.fillRect(0, h - 22, w, h);
        aGraphics.setColor(0, 0, 0);
        aGraphics.drawString("options", 2, h - fh - 2, Graphics.TOP | Graphics.LEFT);
    }

    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            midlet.getActiveApp().removeTCPClientListener(this);
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == ZOOM_IN_CMD) {
            zoomIn();
        } else if (cmd == ZOOM_OUT_CMD) {
            zoomOut();
        } else if (cmd == TOGGLE_MAP_CMD) {
            mapType = mapType.equals("sat") ? "map" : "sat";
            resetMap();
            show();
        }
    }

}
