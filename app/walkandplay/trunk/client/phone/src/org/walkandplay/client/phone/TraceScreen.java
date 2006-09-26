package org.walkandplay.client.phone;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.game.GameCanvas;

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
	private static final int CMD_ADD_POI = 2;
	private static final int CMD_ADD_PHOTO = 3;
	private static final int CMD_ADD_AUDIO = 4;
	private static final int CMD_SOUND_TOGGLE = 5;
	private static final int CMD_GPS_TOGGLE = 6;
	private static final int CMD_KB_LOCK = 7;
	private static final int CMD_SELECT_GPS = 8;
	private static final int CMD_VIEW_LOG = 9;
	private static final int CMD_SHOW_MAP = 10;
	private static final int CMD_QUIT = 11;
	private static final int[] DEF_CMDS = {CMD_SUSPEND_RESUME, CMD_NEW_TRK, CMD_ADD_POI, CMD_ADD_PHOTO, CMD_ADD_AUDIO, CMD_SOUND_TOGGLE, CMD_GPS_TOGGLE, CMD_KB_LOCK, CMD_SELECT_GPS, CMD_VIEW_LOG, CMD_SHOW_MAP, CMD_QUIT};
	private static final int[] MIN_CMDS = {CMD_QUIT};
	private static final String[] DEF_CMD_LABELS = {"Resume Track", "New Track", "Add POI", "Send Photo", "Send Audio", "Sound Off", "Hide GPS Info", "Lock KeyBoard", "SelectGPS", "View Log", "Show Map", "Exit"};
	private static final String[] MIN_CMD_LABELS = {"Stoppen"};
	private int[] CMDS = DEF_CMDS;
	private String[] CMD_LABELS = DEF_CMD_LABELS;
	private int cmdIndex = 0;
	private boolean poundPress = false;
	private boolean keyLock = false;
	private boolean showGPSInfo = true;
	private int roadRating = -1;
	private MapScreen mapScreen;
	private String options;

	// screen width, height and font height
	private int w, h;
	// font to write message on screen
	Font f, fb;

	private Tracer tracer;
	private TraceScreen traceScreen;
	private WP midlet;
	private boolean cmdShow = true;
	private CustomLabel netLabel, gpsLabel, netStatusLabel, gpsStatusLabel, statusLabel, buttonLabel;

	public TraceScreen(WP aMidlet) {
		super(false);
		setFullScreenMode(true);
		tracer = new Tracer(aMidlet, this);
		midlet = aMidlet;
		traceScreen = this;
	}

	void start() {
		hideCommands();
		mapScreen = new MapScreen(midlet);
		tracer.start();
		options = midlet.getAppProperty("wp-options");
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
		mapScreen.setLocation(theInfo.lon.toString(), theInfo.lat.toString());
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

			case CMD_ADD_POI:
				new AddPOIHandler().addPOI();
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
				setStatus("Showing map...");
				Display.getDisplay(midlet).setCurrent(mapScreen);
				mapScreen.show();
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

	private class AddPOIHandler implements CommandListener {
		private Form form;
		private TextField typeField;
		private TextField nameField;
		private TextField descrField;
		private Command okCmd = new Command("OK", Command.OK, 1);
		private Command cancelCmd = new Command("Back", Command.CANCEL, 1);

		/*
		* Create the first TextBox and associate
		* the exit command and listener.
		*/
		public void addPOI() {
			// Create the TextBox containing the "Hello,World!" message
			form = new Form("Add New Point of Interest (POI)");
			typeField = new TextField("Enter Type", "", 16, TextField.ANY);
			nameField = new TextField("Enter Name", "", 32, TextField.ANY);
			descrField = new TextField("Enter Description (optional)", "", 512, TextField.ANY);
			form.append(typeField);
			form.append(nameField);
			form.append(descrField);
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
				String poiName = nameField.getString();
				String poiType = typeField.getString();
				if (poiName != null && poiName.length() > 0 && poiType != null && poiType.length() > 0) {
					tracer.getNet().addPOI(poiType, poiName, descrField.getString());
				} else {
					setStatus("No name or type");
				}
			} else {
				onNetStatus("Add POI cancel");
			}


			// Set the current display of the midlet to the textBox screen
			Display.getDisplay(midlet).setCurrent(traceScreen);
		}
	}

}
