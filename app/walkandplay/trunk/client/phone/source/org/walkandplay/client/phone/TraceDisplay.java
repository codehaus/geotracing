package org.walkandplay.client.phone;


import org.geotracing.client.*;

import javax.microedition.lcdui.*;

import de.enough.polish.util.Locale;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
/*public class TraceDisplay extends GameCanvas{*/
public class TraceDisplay extends DefaultDisplay  {
	String gpsStatus = "disconnected";
	String netStatus = "disconnected";
	String status = "OK";

	private boolean showGPSInfo = true;
	private MapDisplay mapViewer;
	Font f, fb;

    private TracerEngine tracerEngine;
	private TraceDisplay traceDisplay;

    private Command NEW_TRK_CMD = new Command(Locale.get("trace.New"), Command.ITEM, 2);
    private Command SUSPEND_TRK_CMD = new Command(Locale.get("trace.Suspend"), Command.ITEM, 2);
    private Command RESUME_TRK_CMD = new Command(Locale.get("trace.Resume"), Command.ITEM, 2);
    private Command ADD_TEXT_CMD = new Command(Locale.get("trace.AddText"), Command.ITEM, 2);
    private Command ADD_PHOTO_CMD = new Command(Locale.get("trace.AddPhoto"), Command.ITEM, 2);
    private Command ADD_AUDIO_CMD = new Command(Locale.get("trace.AddAudio"), Command.ITEM, 2);
    private Command SHOW_MAP_CMD = new Command(Locale.get("trace.ShowMap"), Command.ITEM, 2);
    private Command RADAR_CMD = new Command(Locale.get("trace.Radar"), Command.ITEM, 2);

    Command BACK_CMD = new Command("Back", Command.BACK, 1);


    public TraceDisplay(WPMidlet aMidlet) {
        super(aMidlet, "Trace");

		tracerEngine = new TracerEngine(aMidlet, this);
		traceDisplay = this;

        addCommand(NEW_TRK_CMD);
        addCommand(SUSPEND_TRK_CMD);
        addCommand(RESUME_TRK_CMD);
        addCommand(ADD_TEXT_CMD);
        addCommand(ADD_PHOTO_CMD);
        addCommand(ADD_AUDIO_CMD);
        addCommand(SHOW_MAP_CMD);
        addCommand(RADAR_CMD);
    }

	void start() {
		mapViewer = new MapDisplay();
		tracerEngine.start();
	}

    void stop() {
		tracerEngine.stop();
	}

    TracerEngine getTracer() {
		return tracerEngine;
	}

	public void setGPSInfo(GPSInfo theInfo) {
		mapViewer.setLocation(theInfo.lon.toString(), theInfo.lat.toString());
		if (!showGPSInfo) {
			return;
		}
		status = theInfo.toString();
        log(status);
    }

	public void setStatus(String s) {
		status = s;
		log(s);
	}

	public void onGPSStatus(String s) {
		gpsStatus = s;
        log(s);
    }


	public void onNetStatus(String s) {
		netStatus = s;
        log(s);
    }

    public void cls() {
		deleteAll();
	}

    public void log(String message) {
        cls();
        //#style formbox
        append(new StringItem("", message + "\n"));
		System.out.println(message);
	}


    private class MainHandler implements CommandListener {
		/*
		* Create the first TextBox and associate
		* the exit command and listener.
		*/
        private List menuScreen;
        public void start() {

            //#style mainScreen
            menuScreen = new List("", List.IMPLICIT);
            //#style traceNewCommand
            menuScreen.append(Locale.get("trace.New"), null);
            if(tracerEngine.isPaused()){
                //#style traceResumeCommand
                menuScreen.append(Locale.get("trace.Resume"), null);
            }else{
                //#style traceSuspendCommand
                menuScreen.append(Locale.get("trace.Suspend"), null);
            }
            //#style traceAddPhotoCommand
            menuScreen.append(Locale.get("trace.AddPhoto"), null);
            //#style traceAddTextCommand
            menuScreen.append(Locale.get("trace.AddText"), null);
            //#style traceAddAudioCommand
            menuScreen.append(Locale.get("trace.AddAudio"), null);
            //#style traceShowMapCommand
            menuScreen.append(Locale.get("trace.ShowMap"), null);
            //#style traceRadarCommand
            menuScreen.append(Locale.get("trace.Radar"), null);

            menuScreen.setCommandListener(this);

            Display.getDisplay(midlet).setCurrent(menuScreen);

		}

		/*
		* The commandAction method is implemented by this midlet to
		* satisfy the CommandListener interface and handle the Exit action.
		*/
		public void commandAction(Command cmd, Displayable screen) {
			if (cmd == List.SELECT_COMMAND) {
                switch (menuScreen.getSelectedIndex()) {
                    case 0:
                        //new
                        new CreateTrackHandler().createTrack();
                        break;
                    case 1:
                        // suspend/resume
                        tracerEngine.suspendResume();
                        break;
                    case 2:
                        // add photo
                        Display.getDisplay(midlet).setCurrent(new ImageCapture(midlet));
                        break;
                    case 3:
                        // add text
                        new AddTextHandler().addText();
                        break;
                    case 4:
                        // add audio
                        Display.getDisplay(midlet).setCurrent(new AudioCapture(midlet));
                        break;
                    case 5:
                        // show map
                        mapViewer.activate(midlet);
                        break;
                    case 6:
                        // radar
                        Display.getDisplay(midlet).setCurrent(new RadarScreen(midlet));
                        break;
                }
            }
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
            //#style defaultscreen
            form = new Form("Create New Track");
            //#style textbox
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
					tracerEngine.suspend();
					tracerEngine.getNet().newTrack(trackName);
				} else {
					onNetStatus("No trk name");
				}
			} else {
				onNetStatus("Create cancel");
			}


			// Set the current display of the midlet to the textBox screen
			Display.getDisplay(midlet).setCurrent(traceDisplay);
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
            //#style textbox
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
				String text = textBox.getString();
				String tags = tagsField.getString();
				if (name != null && name.length() > 0 && text != null && text.length() > 0) {
					tracerEngine.getNet().uploadMedium(name, "text", "text/plain", Util.getTime(), text.getBytes(), false, tags);
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
			Display.getDisplay(midlet).setCurrent(traceDisplay);
		}
	}

    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }else if (cmd == NEW_TRK_CMD) {
            log("creating new track");
            new CreateTrackHandler().createTrack();
        } else if (cmd == SUSPEND_TRK_CMD) {
            log("suspending track");
            tracerEngine.suspendResume();
        } else if (cmd == RESUME_TRK_CMD) {
            log("resuming track");
            tracerEngine.suspendResume();
        } else if (cmd == ADD_PHOTO_CMD) {
            log("adding photo");
            Display.getDisplay(midlet).setCurrent(new ImageCapture(midlet));
        } else if (cmd == ADD_AUDIO_CMD) {
            log("adding audio");
            Display.getDisplay(midlet).setCurrent(new AudioCapture(midlet));
        } else if (cmd == ADD_TEXT_CMD) {
            log("adding text");
            new AddTextHandler().addText();
        } else if (cmd == SHOW_MAP_CMD) {
            log("show map");
            mapViewer.activate(midlet);
        } else if (cmd == RADAR_CMD) {
            log("show radar");
            Display.getDisplay(midlet).setCurrent(new RadarScreen(midlet));
        }

    }

}

