// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.client;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;

/**
 * MobiTracer main GUI.
 *
 * @author  Just van den Broecke
 * @version $Id$
 */
public class TraceScreen extends GameCanvas {
	String gpsStatus = "disconnected";
	String netStatus = "disconnected";
	String status = "OK";
	private static final int BLACK = 0x000000;
	private static final int WHITE = 0xffffff;
	private static final int BLUE = 0x000066;
	private static final int YELLOW = 0xffff33;

	private static final int CMD_SUSPEND_RESUME = 0;
	private static final int CMD_NEW_TRK = 1;
	private static final int CMD_ADD_TEXT = 2;
	private static final int CMD_ADD_PHOTO = 3;
	private static final int CMD_ADD_AUDIO = 4;
	private static final int CMD_SOUND_TOGGLE = 5;
	private static final int CMD_GPS_TOGGLE = 6;
	private static final int CMD_KB_LOCK = 7;
	private static final int CMD_SELECT_GPS = 8;
	private static final int CMD_VIEW_LOG = 9;
	private static final int CMD_SHOW_MAP = 10;
	private static final int CMD_RADAR = 11;
	private static final int CMD_ACCOUNT = 12;
	private static final int CMD_QUIT = 13;
	private static final int[] DEF_CMDS = {CMD_SUSPEND_RESUME, CMD_NEW_TRK, CMD_ADD_TEXT, CMD_ADD_PHOTO, CMD_ADD_AUDIO, CMD_SOUND_TOGGLE, CMD_GPS_TOGGLE, CMD_KB_LOCK, CMD_SELECT_GPS, CMD_VIEW_LOG, CMD_SHOW_MAP, CMD_RADAR, CMD_ACCOUNT, CMD_QUIT};
	private static final int[] MIN_CMDS = {CMD_QUIT};
	private static final String[] DEF_CMD_LABELS = {"Resume Track", "New Track", "Send Text", "Send Photo", "Send Audio", "Sound Off", "Show GPS Info", "Lock KeyBoard", "SelectGPS", "View Log", "Show Map",  "GeoRadar", "Account", "Exit"};
	private static final String[] MIN_CMD_LABELS = {"Afsluiten"};
	private int[] CMDS = DEF_CMDS;
	private String[] CMD_LABELS = DEF_CMD_LABELS;
	private int cmdIndex = 0;
	private boolean poundPress = false;
	private boolean keyLock = false;
	private boolean showGPSInfo = false;
	private int roadRating = -1;
	private MapCanvas mapViewer;
	private String options;
	// screen width, height and font height
	private int w, h;
	// font to write message on screen
	Font f, fb;

	private Tracer tracer;
	private TraceScreen traceScreen;
	private MobiTracer midlet;
	private boolean cmdShow = true;
	private CustomLabel netLabel, gpsLabel, netStatusLabel, gpsStatusLabel, statusLabel, buttonLabel;

	public TraceScreen(MobiTracer aMidlet) {
		super(false);
		setFullScreenMode(true);
		tracer = new Tracer(aMidlet, this);
		midlet = aMidlet;
		traceScreen = this;
	}

	void start() {
		hideCommands();
		mapViewer = new MapCanvas();
		tracer.start();
		options = midlet.getAppProperty("mt-options");
		if (options.indexOf("minimal") != -1) {
			CMDS = MIN_CMDS;
			CMD_LABELS = MIN_CMD_LABELS;
			showGPSInfo = false;
			tracer.resume();
			String gpsURL = GPSSelector.getGPSURL();
			if (gpsURL == null) {
				tracer.stop();
				Display.getDisplay(midlet).setCurrent(new GPSSelector(midlet));
			}
		}
		showCommands();

		String versionMsg = tracer.versionCheck();
		if (versionMsg != null) {
			Util.showAlert(midlet, "Version Check", versionMsg);
		}
	}

	Tracer getTracer() {
		return tracer;
	}

	void redraw() {
		if (isShown()) {
			repaint();
		}
	}

	void hideCommands() {
		cmdShow = false;
		redraw();
	}

	void showCommands() {
		cmdShow = true;
		redraw();
	}

	/**
	 * Draws the screen.
	 * @param g The graphics object.
	 */
	public void paint(Graphics g) {
		if (f == null) {
			f = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
			fb = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
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

			gpsLabel = new CustomLabel("GPS", fb, 1, 1, (w - 2) / 2, 24, WHITE, BLUE);
			gpsLabel.setBorders(0, 0, 0, WHITE);
			netLabel = new CustomLabel("NET", fb, ((w - 2) / 2) + 1, 1, (w - 2) / 2, 24, WHITE, BLUE);
			netLabel.setBorders(0, 0, WHITE, 0);
			gpsStatusLabel = new CustomLabel("disconnected", f, 1, 25, (w - 2) / 2, 32, BLACK, WHITE);
			gpsStatusLabel.setBorders(0, 0, 0, BLUE);
			netStatusLabel = new CustomLabel("disconnected", f, ((w - 2) / 2) + 1, 25, (w - 2) / 2, 32, BLACK, WHITE);
			netStatusLabel.setBorders(0, 0, BLUE, 0);
			statusLabel = new CustomLabel("WAIT...", fb, 6, h - h / 5, w - 6, 24, YELLOW, BLUE);
			buttonLabel = new ButtonLabel("button", fb, w / 6, h - h / 5, w * 2 / 3, 24, BLACK, WHITE);
		}

		// Draw outline
		g.setColor(BLUE);
		g.fillRect(0, 0, w, h);
		g.setColor(WHITE);
		g.drawRect(0, 0, w - 1, h - 1);

		// Draw net and GPS status labels
		gpsLabel.paint(g);
		netLabel.paint(g);
		gpsStatusLabel.set(gpsStatus);
		netStatusLabel.set(netStatus);
		gpsStatusLabel.paint(g);
		netStatusLabel.paint(g);

		// Draw status message
		g.setFont(f);
		g.setColor(YELLOW);
		String[] statusLines = Util.split(status, '\n');
		for (int i = 0; i < statusLines.length; i++) {
			g.drawString(statusLines[i], 10, 70 + (i * 12), Graphics.TOP | Graphics.LEFT);
		}

		// Draw button
		if (cmdShow) {
			buttonLabel.set(CMD_LABELS[cmdIndex]);
			buttonLabel.paint(g);
		} else {
			statusLabel.paint(g);
		}
	}

	/**
	 * Handles all key actions.
	 * @param key The Key that was hit.
	 */
	public void keyPressed(int key) {

		switch (key) {

			case KEY_POUND:
				poundPress = true;
				return;

			case KEY_NUM1:
				setRoadRating(1);

				// Cheat code to switch to full mode
				if (poundPress) {
					CMDS = DEF_CMDS;
					CMD_LABELS = DEF_CMD_LABELS;
					poundPress = false;
					showGPSInfo = true;
				}
				return;

			case KEY_NUM2:
				setRoadRating(2);
				return;

			case KEY_NUM3:
				setRoadRating(3);
				return;

			case KEY_NUM4:
				setRoadRating(4);
				return;

			case KEY_NUM5:
				if (poundPress && keyLock) {
					keyLock = false;
					poundPress = false;
					setStatus("Keyboard is unlocked");
					return;
				} else {
					setRoadRating(5);
					return;
				}

			default:
				poundPress = false;
		}

		switch (getGameAction(key)) {
			case UP:
			case LEFT:
				cmdIndex = (cmdIndex == 0) ? CMDS.length - 1 : cmdIndex - 1;
				repaint();
				break;
			case DOWN:
			case RIGHT:
				cmdIndex = (cmdIndex == CMDS.length - 1) ? 0 : cmdIndex + 1;
				repaint();
				break;
			case FIRE:
				if (keyLock) {
					setStatus("Keyboard locked\n press #5 to unlock");
					return;
				}
				doCommand();
				break;
		}
	}

	public void setGPSInfo(GPSInfo theInfo) {
		mapViewer.setLocation(theInfo.lon.toString(), theInfo.lat.toString());
		if (!showGPSInfo) {
			return;
		}
		status = theInfo.toString();
		redraw();
	}

	public void setStatus(String s) {
		status = s;
		Log.log(s);
		redraw();
	}

	public void onGPSStatus(String s) {
		gpsStatus = s;
		redraw();
	}


	public void onNetStatus(String s) {
		netStatus = s;
		redraw();
	}

	public void setRoadRating(int r) {
		roadRating = r;
		tracer.setRoadRating(r);
		setStatus("RoadRating = " + roadRating);
	}

	public void doCommand() {
		hideCommands();
		switch (CMDS[cmdIndex]) {
			case CMD_SUSPEND_RESUME:
				tracer.suspendResume();
				CMD_LABELS[cmdIndex] = tracer.isPaused() ? "Resume Track" : "Suspend Track";
				break;

			case CMD_GPS_TOGGLE:
				showGPSInfo = !showGPSInfo;
				CMD_LABELS[cmdIndex] = showGPSInfo ? "Hide GPS Info" : "Show GPS Info";
				setStatus("GPS info is " + (showGPSInfo ? "ON" : "OFF"));
				if (!showGPSInfo) {
					setStatus("RoadRating = " + roadRating);
				}
				break;

			case CMD_SOUND_TOGGLE:
				Util.toggleSound();
				CMD_LABELS[cmdIndex] = Util.hasSound() ? "Sound Off" : "Sound On";
				setStatus("Sound is " + (Util.hasSound() ? "ON" : "OFF"));
				break;

			case CMD_NEW_TRK:
				new CreateTrackHandler().createTrack();
				break;

			case CMD_ADD_TEXT:
				new AddTextHandler().addText();
				break;

			case CMD_ADD_PHOTO:
				Display.getDisplay(midlet).setCurrent(new ImageCapture(midlet));
				break;

			case CMD_ADD_AUDIO:
				Display.getDisplay(midlet).setCurrent(new AudioCapture(midlet));
				break;

			case CMD_KB_LOCK:
				keyLock = true;
				setStatus("Keyboard locked\n press #5 to unlock");
				break;

			case CMD_SELECT_GPS:
				tracer.stop();
				Display.getDisplay(midlet).setCurrent(new GPSSelector(midlet));
				break;

			case CMD_VIEW_LOG:
				Log.view(midlet);
				break;

			case CMD_SHOW_MAP:
				// setStatus("Showing map...");
				mapViewer.activate(midlet);
				break;

			case CMD_RADAR:
				setStatus("Showing Radar");
				Display.getDisplay(midlet).setCurrent(new RadarScreen(midlet));
				break;

			case CMD_ACCOUNT:
				// tracer.stop();
				setStatus("Account settings: do Exit when changing");
				Display.getDisplay(midlet).setCurrent(new AccountScreen(midlet));
				break;
			case CMD_QUIT:
				tracer.stop();
				midlet.notifyDestroyed();
				break;
		}

		showCommands();
		cmdIndex = 0;
	}

	private class CustomLabel {
		protected String string;
		protected Font font;
		protected int topX, topY, width, height, x, y;
		protected int fgColor, bgColor, borderN, borderS, borderW, borderE;

		public CustomLabel(String aString, Font aFont, int aTopX, int aTopY, int aWidth, int aHeight, int anFGColor, int aBGColor) {
			font = aFont;
			topX = aTopX;
			topY = aTopY;
			width = aWidth;
			height = aHeight;
			fgColor = anFGColor;
			bgColor = aBGColor;

			set(aString);
		}

		public void setBorders(int n, int s, int w, int e) {
			borderN = n;
			borderS = s;
			borderW = w;
			borderE = e;
		}

		public void set(String aString) {
			string = aString;
			x = topX + width / 2 - font.stringWidth(aString) / 2;
			y = topY + height / 2 - font.getHeight() / 2;
		}

		public void paint(Graphics g) {
			g.setFont(font);
			g.setColor(bgColor);
			g.fillRect(topX, topY, width, height);
			drawBorder(g, borderN, topX, topY, topX + width, topY);
			drawBorder(g, borderS, topX, topY + height, topX + width, topY + height);
			drawBorder(g, borderW, topX, topY, topX, topY + height);
			drawBorder(g, borderE, topX + width, topY, topX + width, topY + height);
			g.drawRect(topX, topY, width, height);

			g.setColor(fgColor);
			g.drawString(string, x, y, Graphics.TOP | Graphics.LEFT);
		}

		protected void drawBorder(Graphics g, int aColor, int x1, int y1, int x2, int y2) {
			if (aColor == 0) {
				return;
			}
			g.setColor(aColor);
			g.drawLine(x1, y1, x2, y2);
		}
	}

	private class ButtonLabel extends CustomLabel {
		public ButtonLabel(String aString, Font aFont, int aTopX, int aTopY, int aWidth, int aHeight, int anFGColor, int aBGColor) {
			super(aString, aFont, aTopX, aTopY, aWidth, aHeight, anFGColor, aBGColor);

		}

		public void paint(Graphics g) {
			g.setColor(bgColor);
			g.fillRoundRect(topX, topY, width, height, 8, 8);
			g.setColor(fgColor);
			g.drawRoundRect(topX + 2, topY + 2, width - 4, height - 4, 8, 8);
			g.setFont(font);

			g.drawString(string, x, y, Graphics.TOP | Graphics.LEFT);

		}
	}

	private class CreateTrackHandler implements CommandListener {
		private Form form;
		private TextField textField;
		private Command okCmd = new Command("OK", Command.OK, 1);
		private Command cancelCmd = new Command("Back", Command.CANCEL, 1);

		/*
		* Create the first TextBox and associate
		* the exit command and listener.
		*/
		public void createTrack() {

			form = new Form("Create New Track");
			textField = new TextField("Enter Track Name", "", 48, TextField.ANY);
			form.append(textField);
			// Add the Exit Command to the TextBox
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
				String trackName = textField.getString();
				if (trackName != null && trackName.length() > 0) {
					tracer.suspend();
					tracer.getNet().newTrack(trackName);
				} else {
					onNetStatus("No trk name");
				}
			} else {
				onNetStatus("Create cancel");
			}


			// Set the current display of the midlet to the textBox screen
			Display.getDisplay(midlet).setCurrent(traceScreen);
		}
	}

	private class AddTextHandler implements CommandListener {
		private TextField tagsField;
		private TextField nameField;
		private TextBox textBox;
		private Command textOkCmd = new Command("OK", Command.OK, 1);
		private Command submitCmd = new Command("OK", Command.OK, 1);
		private Command cancelCmd = new Command("Back", Command.CANCEL, 1);

		/*
		* Create the first TextBox and associate
		* the exit command and listener.
		*/
		public void addText() {
			// Create the TextBox containing the "Hello,World!" message
			textBox = new TextBox("Enter Text", "", 1024, TextField.ANY);


			// Add the Exit Command to the TextBox
			textBox.addCommand(textOkCmd);
			textBox.addCommand(cancelCmd);

			// Set the command listener for the textbox to the current midlet
			textBox.setCommandListener(this);

			// Set the current display of the midlet to the textBox screen
			Display.getDisplay(midlet).setCurrent(textBox);
		}

		/*
		* Create the first TextBox and associate
		* the exit command and listener.
		*/
		public void addMeta() {
			// Create the TextBox containing the "Hello,World!" message
			Form form = new Form("Add Info");
			nameField = new TextField("Enter Title", "", 32, TextField.ANY);
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
				String text = textBox.getString();
				String tags = tagsField.getString();
				if (name != null && name.length() > 0 && text != null && text.length() > 0) {
					tracer.getNet().uploadMedium(name, "text", "text/plain", Util.getTime(), text.getBytes(), true, tags);
				} else {
					setStatus("Type title and tags");
				}
			} else if (command == textOkCmd) {
				if (textBox.getString() == null) {
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
			Display.getDisplay(midlet).setCurrent(traceScreen);
		}
	}

}
