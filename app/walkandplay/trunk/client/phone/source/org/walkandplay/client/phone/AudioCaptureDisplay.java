package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.GPSFetcher;
import org.geotracing.client.Util;
import org.keyworx.mclient.Protocol;

import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class AudioCaptureDisplay extends DefaultDisplay implements ProgressListener {

    private Player player;
    private RecordControl recordcontrol;
    private ByteArrayOutputStream output;
    private byte[] audioData;
    private TextField name = new TextField("", null, 24, TextField.ANY);
    private static final String MIME = "audio/x-wav";
    private long startTime;
    final int rate;
    final int bits;
    final int kbPerSec;
    private boolean playing;
    private boolean active;

    private StringItem alertField = new StringItem("", "");

    private Command START_CMD = new Command("Start", Command.OK, 1);
    private Command STOP_CMD = new Command("Stop", Command.OK, 1);
    private Command SUBMIT_CMD = new Command("Submit", Command.OK, 1);
    private Command PLAY_CMD = new Command("Play", Command.SCREEN, 1);

    private Gauge progressBar = new Gauge("", false, 100, 0);
    //private Gauge progressBar = new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);

    private int progressCounter;
    private int progressMax = 100;

    public AudioCaptureDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "Record and send audio");

        rate = Integer.parseInt(midlet.getAppProperty("audio-rate"));
        bits = Integer.parseInt(midlet.getAppProperty("audio-bits"));
        kbPerSec = (rate * bits / 8) / 1000;
    }

    public void start(Displayable aPrevScreen, boolean isPlaying) {
        try {
            prevScreen = aPrevScreen;
            playing = isPlaying;
            active = true;
            player = Manager.createPlayer("capture://audio?rate=" + rate + "&bits=" + bits);
            player.realize();
            recordcontrol = (RecordControl) player.getControl("RecordControl");
            output = new ByteArrayOutputStream();
            recordcontrol.setRecordStream(output);

            //#style labelinfo
            append("Use the menu to start and stop recording.");
            //#style formbox
            append("Settings: " + rate / 1000 + "kHz " + bits + " bits " + kbPerSec + " kb/sec");
            Display.getDisplay(midlet).setCurrent(this);
        } catch (Exception e) {
            Util.showAlert(midlet, "Error", "Cannot create player. Maybe audio (MMAPI) is not supported.");
            back();
        }

        addCommand(START_CMD);
        Display.getDisplay(midlet).setCurrent(this);
    }

    public boolean isActive() {
        return active;
    }

    public void handleAddMediumRsp(JXElement aResponse) {
        clearScreen();
        removeCommand(PLAY_CMD);
        alertField.setText("Audio sent successfully");
    }

    public void handleAddMediumNrsp(JXElement aResponse) {
        clearScreen();
        alertField.setText("Error sending audio - please try again.");
    }

    private void clearScreen() {
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append(alertField);
    }

    public void prStart() {
        progressBar.setMaxValue(progressMax);
    }

    public void prProgress(int anAmount) {
        progressBar.setValue(anAmount);
    }

    public void prStop() {
        progressBar.setLabel("Upload finished!");
    }

    public void prError(String aMessage) {
        write(aMessage);
    }

    public void prSetContentLength(int aContentLength) {
        progressBar.setLabel("Downloading " + aContentLength + " bytes");
    }

    private class Progress {
        public int state = 0;

        private void start(ProgressListener aListener) {
            final ProgressListener listener = aListener;
            try {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            try {
                                listener.prStart();
                                Uploader uploader = new Uploader();
                                listener.prProgress(progressMax / 4);
                                JXElement rsp = uploader.uploadMedium(TCPClient.getInstance().getAgentKey(), midlet.getKWUrl(), name.getString(), null, "audio", MIME, startTime, audioData, false);
                                listener.prProgress(progressMax / 2);
                                if (rsp == null) {
                                    write("cannot submit audio!");
                                } else if (Protocol.isPositiveResponse(rsp)) {
                                    JXElement addMediumReq;
                                    if (playing) {
                                        addMediumReq = new JXElement("play-add-medium-req");
                                        addMediumReq.setAttr("id", rsp.getAttr("id"));
                                    } else {
                                        addMediumReq = new JXElement("game-add-medium-req");
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
                                    }

                                    listener.prProgress(progressMax * 3 / 4);
                                    midlet.getActiveApp().sendRequest(addMediumReq);
                                    listener.prProgress(progressMax);
                                } else {
                                    //#style alertinfo
                                    append("Upload failed: error is " + rsp.getAttr("error") + " press Back");
                                }
                            } finally {
                                listener.prStop();
                            }
                        } catch (Throwable t) {
                            listener.prError(t.getMessage());
                        }
                    }
                }).start();
            } catch (Throwable t) {
                //#style alertinfo
                append("Exception in Downloader:" + t.getMessage());
            }
        }
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
            if(recordcontrol!=null){
                recordcontrol = null;
            }
            if(player!=null){
                player.deallocate();
                player.close();
                player = null;
            }
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

            //#style formbox
            append(progressBar);

            new Progress().start(this);

            removeCommand(SUBMIT_CMD);
        } else if (c == PLAY_CMD) {
            play();
        }
    }


    private void back() {
        active = false;
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
