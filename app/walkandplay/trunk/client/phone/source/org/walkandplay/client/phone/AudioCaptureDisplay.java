package org.walkandplay.client.phone;

import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.TextField;
import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import nl.justobjects.mjox.XMLChannelListener;
import org.geotracing.client.GPSFetcher;
import org.geotracing.client.Net;
import org.geotracing.client.Util;
import org.keyworx.mclient.Protocol;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Record and submit audio.
 *
 * @author Just van den Broecke
 * @version $Id: AudioCapture.java 222 2006-12-10 00:17:59Z just $
 */
public class AudioCaptureDisplay extends DefaultDisplay implements XMLChannelListener {

    private Player player;
    private RecordControl recordcontrol;
    private ByteArrayOutputStream output;
    private byte[] audioData;
    private TextField name = new TextField("", null, 24, TextField.ANY);
    private static final String MIME = "audio/x-wav";
    private long startTime;
    final int kbPerSec;
    private boolean playing;
    private StringItem alertField = new StringItem("", "");

    private Command START_CMD = new Command("Start", Command.OK, 1);
    private Command STOP_CMD = new Command("Stop", Command.OK, 1);
    private Command SUBMIT_CMD = new Command("Submit", Command.OK, 1);
    private Command PLAY_CMD = new Command("Play", Command.SCREEN, 1);

    public AudioCaptureDisplay(WPMidlet aMIDlet, Displayable aPrevScreen, boolean isPlaying) {
        super(aMIDlet, "Record and send audio");
        prevScreen = aPrevScreen;
        playing = isPlaying;

        if (isPlaying) {
            midlet.getPlayApp().setKWClientListener(this);
        } else {
            midlet.getCreateApp().setKWClientListener(this);
        }

        int rate = Integer.parseInt(midlet.getAppProperty("audio-rate"));
        int bits = Integer.parseInt(midlet.getAppProperty("audio-bits"));
        kbPerSec = (rate * bits / 8) / 1000;

        try {
            player = Manager.createPlayer("capture://audio?rate=" + rate + "&bits=" + bits);
            player.realize();
            recordcontrol = (RecordControl) player.getControl("RecordControl");
            output = new ByteArrayOutputStream();
            recordcontrol.setRecordStream(output);

            //#style labelinfo
            append("Use the menu to start and stop recording.");
            //#style formbox
            append("Settings: " + rate / 1000 + "kHz " + bits + " bits " + kbPerSec + " kb/sec");
        } catch (Exception e) {
            Util.showAlert(midlet, "Error", "Cannot create player. Maybe audio (MMAPI) is not supported.");
            back();
        }

        addCommand(START_CMD);
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        Log.log("** received:" + new String(aResponse.toBytes(false)));
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            deleteAll();
            addCommand(BACK_CMD);
            //#style alertinfo
            append(alertField);
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("play-add-medium-rsp") || rsp.getTag().equals("game-add-medium-rsp")) {
                alertField.setText("Audio sent successfully");
            } else if (rsp.getTag().equals("play-add-medium-nrsp")) {
                alertField.setText("Error sending audio - please try again.");
            }
        }
    }

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append("Oops, we lost our connection. Please go back and try again.");
    }

    public int write(String s) {
        //#style formbox
        return append(s);
    }

    public int write(String label, String s) {
        StringItem si = new StringItem(label, s);
        si.setLayout(Item.LAYOUT_NEWLINE_BEFORE);
        //#style formbox
        return super.append(si);
    }

    public void commandAction(Command c, Displayable d) {
        deleteAll();
        if (c == BACK_CMD) {
            audioData = null;
            back();

        } else if (c == START_CMD) {
            removeCommand(START_CMD);
            record();
            addCommand(STOP_CMD);

        } else if (c == STOP_CMD) {
            removeCommand(STOP_CMD);
            stop();
            addCommand(PLAY_CMD);
            addCommand(SUBMIT_CMD);

            //#style labelinfo
            append("Recorded: " + (Util.getTime() - startTime) / 1000 + " secs - " + audioData.length / 1024 + "kb");
            //#style labelinfo
            append("Enter Recording Name");
            //#style textbox
            append(name);
            write("press Play to hear recording or Submit to upload");
        } else if (c == SUBMIT_CMD) {

            if (audioData == null) {
                write("no audio data recorded");
                return;
            }

            write("", "Uploading... (takes a while)");

            JXElement rsp = Net.getInstance().uploadMedium(name.getString(), null, "audio", MIME, startTime, audioData, false);
            if (rsp == null) {
                write("cannot submit audio!");
            } else if (Protocol.isPositiveResponse(rsp)) {
                if (playing) {
                    JXElement addMediumReq = new JXElement("play-add-medium-req");
                    addMediumReq.setAttr("id", rsp.getAttr("id"));
                    midlet.getPlayApp().sendRequest(addMediumReq);
                } else {
                    JXElement addMediumReq = new JXElement("game-add-medium-req");
                    addMediumReq.setAttr("id", midlet.getCreateApp().getGameId());
                    JXElement medium = new JXElement("medium");
                    addMediumReq.addChild(medium);

                    JXElement id = new JXElement("id");
                    id.setText(rsp.getAttr("id"));
                    medium.addChild(id);

                    JXElement lat = new JXElement("lat");
                    lat.setText("" + GPSFetcher.getInstance().getCurrentLocation().lat);
                    medium.addChild(lat);

                    JXElement lon = new JXElement("lon");
                    lon.setText("" + GPSFetcher.getInstance().getCurrentLocation().lon);
                    medium.addChild(lon);

                    midlet.getCreateApp().sendRequest(addMediumReq);
                }
            } else {
                //#style alertinfo
                append("Upload failed: error is " + rsp.getAttr("error") + " press Back");
            }

            removeCommand(SUBMIT_CMD);
        } else if (c == PLAY_CMD) {
            play();
        }
    }


    private void back() {
        Display.getDisplay(midlet).setCurrent(prevScreen);
    }


    private void play() {
        if (audioData == null) {
            //#style alertinfo
            append("No audio recorded");
            return;
        }

        write("", "Playing audio...");

        try {
            Manager.createPlayer(new ByteArrayInputStream(audioData), MIME).start();
        } catch (Throwable t) {
            Util.showAlert(midlet, "Error", "Cannot play audio");
        }
    }


    private void record() {
        try {
            recordcontrol.startRecord();
            player.start();
            startTime = Util.getTime();
            write("", "Press STOP to stop recording");

            new Thread(new Runnable() {
                public void run() {
                    int seconds = 0;

                    //#style formbox
                    StringItem status = new StringItem("", "Recording: 0 secs - 0 kb");
                    status.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_VCENTER);
                    append(status);
                    try {
                        while (player != null) {
                            Thread.sleep(1000);
                            seconds++;
                            status.setText("Recording: " + seconds + " secs - " + (seconds * kbPerSec) + " kb");
                        }
                    } catch (Throwable t) {
                        Log.log(t.getMessage());
                    }
                }
            }).start();
        } catch (Exception e) {
            Util.showAlert(midlet, "Error", "Cannot start the player. Maybe audio recording is not supported.");
            back();
        }
    }

    private void stop() {
        try {
            recordcontrol.commit();
            player.close();
            audioData = output.toByteArray();
        } catch (Exception e) {
            Util.showAlert(midlet, "Error", "Cannot stop recording");
            back();
        } finally {
            player = null;
        }
    }
}
