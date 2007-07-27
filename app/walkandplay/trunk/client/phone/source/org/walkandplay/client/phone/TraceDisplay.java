package org.walkandplay.client.phone;


import de.enough.polish.ui.StringItem;
import de.enough.polish.util.Locale;
import org.geotracing.client.*;

import javax.microedition.lcdui.*;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
/*public class TraceDisplay extends DefaultDisplay  {*/
public class TraceDisplay extends DefaultTraceDisplay {
    String gpsStatus = "disconnected";
    String netStatus = "disconnected";
    String status = "OK";
    StringItem gpsStatusBT;
    StringItem netStatusBT;

    private int msgNum;
    private int gpsNum;
    private int netNum;

    private boolean showGPSInfo = true;
    private MapDisplay mapViewer;
    //private TracerEngine tracerEngine;
    private TraceDisplay traceDisplay;

    private Command NEW_TRK_CMD = new Command(Locale.get("trace.New"), Command.ITEM, 2);
    private Command SUSPEND_TRK_CMD = new Command(Locale.get("trace.Suspend"), Command.ITEM, 2);
    private Command RESUME_TRK_CMD = new Command(Locale.get("trace.Resume"), Command.ITEM, 2);
    private Command ADD_TEXT_CMD = new Command(Locale.get("trace.AddText"), Command.ITEM, 2);
    private Command ADD_PHOTO_CMD = new Command(Locale.get("trace.AddPhoto"), Command.ITEM, 2);
    private Command ADD_AUDIO_CMD = new Command(Locale.get("trace.AddAudio"), Command.ITEM, 2);
    private Command SHOW_MAP_CMD = new Command(Locale.get("trace.ShowMap"), Command.ITEM, 2);
    private Command RADAR_CMD = new Command(Locale.get("trace.Radar"), Command.ITEM, 2);

    private Image logo;

    public TraceDisplay(WPMidlet aMidlet) {
        super(aMidlet, "Trace");
        try {
            //#ifdef polish.images.directLoad
            logo = Image.createImage("/trace_icon_small.png");
            //#else
            logo = scheduleImage("/trace_icon_small.png");
            //#endif
        } catch (Throwable t) {
            Log.log("Could not load the images on TraceDisplay");
        }

        tracerEngine = new TracerEngine(aMidlet, this);
        traceDisplay = this;

        addCommand(ADD_TEXT_CMD);
        addCommand(ADD_PHOTO_CMD);
        addCommand(ADD_AUDIO_CMD);
        addCommand(SHOW_MAP_CMD);
        addCommand(RADAR_CMD);
        addCommand(NEW_TRK_CMD);
        if (tracerEngine.isPaused()) {
            addCommand(RESUME_TRK_CMD);
        } else {
            addCommand(SUSPEND_TRK_CMD);
        }

        append(logo);

        //#style gpsstat
        gpsStatusBT = new StringItem("gps", gpsStatus, Item.BUTTON);
        //#style netstat
        netStatusBT = new StringItem("net", netStatus, Item.BUTTON);
        gpsNum = append(gpsStatusBT);
        netNum = append(netStatusBT);
    }

    void start() {
        mapViewer = new MapDisplay();
        tracerEngine.start();
        // directly go to the map if in play mode.
        if (midlet.getPlayMode() == true) {
            mapViewer.activate(midlet);
        }
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
        gpsStatusBT.setText(gpsStatus);
        //log(s);
    }


    public void onNetStatus(String s) {
        netStatus = s;
        netStatusBT.setText(netStatus);
        //log(s);
    }

    public void cls() {
        Log.log("# items: " + size());
        Log.log("gps: " + gpsNum);
        Log.log("net: " + netNum);
        Log.log("msg: " + msgNum);
        delete(msgNum);
    }

    public void log(String message) {
        cls();
        //#style formbox
        msgNum = append(message + "\n");
        //msgNum = append(new StringItem("", message + "\n"));
        Log.log(msgNum + ":" + message);
    }


    private class CreateTrackHandler implements CommandListener {
        private TextField textField;
        private Command okCmd = new Command("OK", Command.OK, 1);
        private Command cancelCmd = new Command("Back", Command.CANCEL, 1);

        /*
          * Create the first TextBox and associate
          * the exit command and listener.
          */
        public void createTrack() {
            //#style defaultscreen
            Form form = new Form("Create New Track");
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
                    tracerEngine.getNet().uploadMedium(name, null, "text", "text/plain", Util.getTime(), text.getBytes(), false);
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
        } else if (cmd == NEW_TRK_CMD) {
            log("creating new track");
            new CreateTrackHandler().createTrack();
        } else if (cmd == SUSPEND_TRK_CMD) {
            log("suspending track");
            tracerEngine.suspendResume();
            removeCommand(SUSPEND_TRK_CMD);
            addCommand(RESUME_TRK_CMD);
        } else if (cmd == RESUME_TRK_CMD) {
            log("resuming track");
            tracerEngine.suspendResume();
            removeCommand(RESUME_TRK_CMD);
            this.addCommand(SUSPEND_TRK_CMD);
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

