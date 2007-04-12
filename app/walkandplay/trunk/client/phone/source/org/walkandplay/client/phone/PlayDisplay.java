package org.walkandplay.client.phone;

import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.ImageItem;
import de.enough.polish.util.Locale;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.midlet.MIDlet;

import org.geotracing.client.*;
import nl.justobjects.mjox.JXElement;

import java.util.Vector;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
/*public class TraceDisplay extends DefaultDisplay  {*/
public class PlayDisplay extends GameCanvas implements CommandListener {
    // =====
    private Displayable prevScreen;
    private String tileBaseURL;
    private JXElement tileInfo, prevTileInfo;
    private static final long REFRESH_INTERVAL_MILLIS = 10000L;
    private long lastRefreshMillis;
    private Image tileImage;
    private MFloat tileScale;
    private int zoom = 12;
    private Image redDot;
    private String mapType = "map";
    private String lon = "0", lat = "0";
    private boolean active;
    // ====

    private WPMidlet midlet;
    private int taskId = -1;
    private int mediumId = -1;
    private Vector gameLocations = new Vector(3);

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
    private Command BACK_CMD = new Command(Locale.get("play.Back"), Command.BACK, 1);

    public PlayDisplay(WPMidlet aMidlet) {
        super(false);
        setFullScreenMode(true);
        
        midlet = aMidlet;
        // make sure we stop tracing when we go into play mode
        if(midlet.traceDisplay!=null) midlet.traceDisplay.stop();

		playDisplay = this;

        addCommand(ADD_TEXT_CMD);
        addCommand(ADD_PHOTO_CMD);
        addCommand(ADD_AUDIO_CMD);
        addCommand(ZOOM_IN_CMD);
        addCommand(ZOOM_OUT_CMD);
        addCommand(TOGGLE_MAP_CMD);
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
        JXElement rsp = tracerEngine.getNet().utopiaReq(req);
        gameLocations = rsp.getChildrenByTag("record");
        log(new String(rsp.toBytes(false)));
    }

    public void setLocation(String aLon, String aLat) {
        // Don't refresh too often (save network overhead)
        long now = Util.getTime();
        if (now - lastRefreshMillis < REFRESH_INTERVAL_MILLIS) {
            return;
        }

        lon = aLon;
        lat = aLat;
        lastRefreshMillis = now;
        fetchTileInfo();
        show();
    }

    protected void fetchTileInfo() {
        if (!hasLocation() || !active) {
            return;
        }

        try {
            // Get information on tile
            String tileInfoURL = tileBaseURL + "lon=" + lon + "&lat=" + lat + "&zoom=" + zoom + "&type=" + mapType + "&format=xml";
            JXElement newTileInfo = Util.getXML(tileInfoURL);

            // Reset tileImage if first tile info or if keyhole ref changed (we moved to new tile).
            if (tileInfo == null || !tileInfo.getAttr("khref").equals(newTileInfo.getAttr("khref"))) {
                tileImage = null;
                prevTileInfo = null;
            }

            // Remember last point/tile if still on same tile
            if (tileImage != null) {
                prevTileInfo = tileInfo;
            }
            tileInfo = newTileInfo;
            // System.out.println("khref=" + tileInfo.getAttr("khref"));
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
		gpsStatus = s;
        show();
    }


	public void onNetStatus(String s) {
        if(s.indexOf("task")!=-1){
            taskId = Integer.parseInt(s.substring(s.indexOf("-") + 1, s.length()));
            new TaskHandler().showTask();
        }else if(s.indexOf("medium")!=-1){
            mediumId = Integer.parseInt(s.substring(s.indexOf("-") + 1, s.length()));
            new MediumHandler().showMedium();
        }
        netStatus = s;
        show();
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
        int w = getWidth();
        // Defeat Nokia bug ?
        if (w == 0) {
            w = 176;
        }
        int h = getHeight();
        // Defeat Nokia bug ?
        if (h == 0) {
            h = 208;
        }

        Font f = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        g.setFont(f);
            
        try {
            g.setColor(255, 255, 255);
            g.fillRect(0, 0, w, h);
            if (tileInfo != null && tileImage == null) {
                try {
                    String tileSize = w + "x" + w;
                    //String tileURL = tileBaseURL + "lon=" + lon + "&lat=" + lat + "&zoom=" + zoom + "&type=" + mapType + "&format=image&size=" + tileSize;
                    String tileURL = tileBaseURL + "lon=" + lon + "&lat=" + lat + "&zoom=" + zoom + "&type=" + mapType + "&format=image&size=320x320";
                    g.drawString("fetching tileImage...", 10, 10, Graphics.TOP | Graphics.LEFT);
                    Image mapImage = Util.getImage(tileURL);
                    tileImage = Image.createImage(mapImage.getWidth(), mapImage.getHeight());
                    tileImage.getGraphics().drawImage(mapImage, 0, 0, Graphics.TOP | Graphics.LEFT);
                } catch (Throwable t) {
                    g.drawString("error: " + t.getMessage(), 10, 30, Graphics.TOP | Graphics.LEFT);
                    return;
                }
            }

            if (tileImage != null) {

                // Correct pixel offset with tile scale
                if (tileScale == null) {
                    tileScale = new MFloat(320).Div(256L);
                }

                // x,y offset of our location in tile tileImage
                String myX = tileInfo.getAttr("x");
                String myY = tileInfo.getAttr("y");
                int x = (int) new MFloat(Integer.parseInt(myX)).Mul(tileScale).toLong();
                int y = (int) new MFloat(Integer.parseInt(myY)).Mul(tileScale).toLong();


                if (prevTileInfo != null) {
                    String lmyX = prevTileInfo.getAttr("x");
                    String lmyY = prevTileInfo.getAttr("y");
                    int lx = (int) new MFloat(Integer.parseInt(lmyX)).Mul(tileScale).toLong();
                    int ly = (int) new MFloat(Integer.parseInt(lmyY)).Mul(tileScale).toLong();
                    Graphics tg = tileImage.getGraphics();
                    tg.setColor(0, 0, 255);
                    tg.drawLine(lx, ly, x, y);
                }

                System.out.println("x:"+x);
                System.out.println("y:"+y);
                // Draw map
                if(x < 40){
                    g.drawImage(tileImage, 0, 0, Graphics.TOP | Graphics.LEFT);
                }else if (x > 280){
                    g.drawImage(tileImage, 80, 0, Graphics.TOP | Graphics.LEFT);
                }else{
                    g.drawImage(tileImage, -40, 0, Graphics.TOP | Graphics.LEFT);
                }

                // Draw current location
                if (redDot == null) {
                    redDot = Image.createImage("/red_dot.png");
                }
                g.drawImage(redDot, x, y, Graphics.TOP | Graphics.LEFT);
                
            } else {
                g.setColor(0, 0, 0);
                g.drawString("No location", w/2, h/2, Graphics.TOP | Graphics.LEFT);
            }

            // draw the gps & net status
            g.setColor(0, 0, 0);
            g.drawString(netStatus, 5, 5, Graphics.TOP | Graphics.LEFT);
            g.drawString(gpsStatus, 50, 5, Graphics.TOP | Graphics.LEFT);

        } catch (Throwable t) {
            g.drawString("cannot get image", 10, 10, Graphics.TOP | Graphics.LEFT);
            g.drawString("try zooming out", 10, 30, Graphics.TOP | Graphics.LEFT);
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
            addCommand(STOP_GAME_CMD);
        } else if (cmd == STOP_GAME_CMD) {
            log("suspending track");
            tracerEngine.suspendResume();
            removeCommand(STOP_GAME_CMD);
            this.addCommand(START_GAME_CMD);
        } else if (cmd == ADD_PHOTO_CMD) {
            log("adding photo");
            Display.getDisplay(midlet).setCurrent(new ImageCapture(midlet));
        } else if (cmd == ADD_AUDIO_CMD) {
            log("adding audio");
            Display.getDisplay(midlet).setCurrent(new AudioCapture(midlet));
        } else if (cmd == ADD_TEXT_CMD) {
            log("adding text");
            new PlayDisplay.AddTextHandler().addText();
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
        }

    }

    private class TaskHandler implements CommandListener {
		private TextField textField;
		private Command okCmd = new Command("OK", Command.OK, 1);
		private Command cancelCmd = new Command("Back", Command.CANCEL, 1);

		/*
		* Create the first TextBox and associate
		* the exit command and listener.
		*/
		public void showTask() {
            // retrieve the task
            JXElement req = new JXElement("query-store-req");
            req.setAttr("cmd", "q-task");
            req.setAttr("id", taskId);
            JXElement rsp = tracerEngine.getNet().utopiaReq(req);
            JXElement task = rsp.getChildByTag("record");
            String name = task.getChildText("name");
            String description = task.getChildText("description");
            String mediumId = task.getChildText("mediumid");

            //#style defaultscreen
            Form form = new Form("Task");
            //#style formbox
            form.append(name);
            form.append(description);
            try{
                form.append(Util.getImage(mediumBaseURL + mediumId + "&resize=120"));
            }catch(Throwable t){
                System.out.println("Exception showing task image:" + t.getMessage());
            }
            //#style textbox
            textField = new TextField("Enter Answer", "", 1024, TextField.ANY);
            form.append(textField);

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

			} else {
				onNetStatus("Create cancel");
			}


			// Set the current display of the midlet to the textBox screen
			Display.getDisplay(midlet).setCurrent(playDisplay);
		}
	}

    private class MediumHandler implements CommandListener {
		private Command cancelCmd = new Command("Back", Command.CANCEL, 1);

		/*
		* Create the first TextBox and associate
		* the exit command and listener.
		*/
		public void showMedium() {
            // retrieve the task
            JXElement req = new JXElement("query-store-req");
            req.setAttr("cmd", "q-medium");
            req.setAttr("id", mediumId);
            JXElement rsp = tracerEngine.getNet().utopiaReq(req);
            JXElement task = rsp.getChildByTag("record");
            String name = task.getChildText("name");
            String type = task.getChildText("type");

            //#style defaultscreen
            Form form = new Form("Medium");
            //#style formbox
            form.append("all the task info");
			// Add the Exit Command to the TextBox
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
			// Set the current display of the midlet to the textBox screen
			Display.getDisplay(midlet).setCurrent(playDisplay);
		}
	}

	private class AddTextHandler implements CommandListener {
		private TextField tagsField;
		private TextField nameField;
		private TextField textField;
		private Command textOkCmd = new Command("OK", Command.OK, 1);
		private Command submitCmd = new Command("OK", Command.OK, 1);
		private Command cancelCmd = new Command("Back", Command.CANCEL, 1);

		/*
		* Create the first TextBox and associate
		* the exit command and listener.
		*/
		public void addText() {
            //#style defaultscreen
            Form form = new Form("Add Text");
            // Create the TextBox containing the "Hello,World!" message
            //#style textbox
            textField = new TextField("Enter Text", "", 1024, TextField.ANY);
            form.append(textField);

            form.addCommand(textOkCmd);
            form.addCommand(cancelCmd);

            form.setCommandListener(this);

            Display.getDisplay(midlet).setCurrent(form);
		}

		/*
		* Create the first TextBox and associate
		* the exit command and listener.
		*/
		public void addMeta() {
			// Create the TextBox containing the "Hello,World!" message
            //#style defaultscreen
            Form form = new Form("Add Info");
            //#style textbox
            nameField = new TextField("Enter Title", "", 32, TextField.ANY);
            //#style textbox
            tagsField = new TextField("Enter Tags (opt)", "", 32, TextField.ANY);

			form.append(nameField);
			form.append(tagsField);

			// Add the Exit Command to the TextBox
			form.addCommand(submitCmd);
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
			if (command == submitCmd) {
				String name = nameField.getString();
				String text = textField.getString();
				String tags = tagsField.getString();
				if (name != null && name.length() > 0 && text != null && text.length() > 0) {
					tracerEngine.getNet().uploadMedium(name, "text", "text/plain", Util.getTime(), text.getBytes(), false, tags);
				} else {
					setStatus("Type title and tags");
				}
			} else if (command == textOkCmd) {
				if (textField.getString() == null) {
					setStatus("No text typed");
				} else {
					// text entered, now enter other stuff
					addMeta();
					return;
				}
			} else {
				onNetStatus("Add Text cancel");
			}

			// Set the current display of the midlet to the textBox screen
			Display.getDisplay(midlet).setCurrent(playDisplay);
		}
	}

}
