package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.GPSFetcher;
import org.geotracing.client.Util;
import org.walkandplay.client.external.CameraHandler;
import org.walkandplay.client.external.CameraListener;
import org.keyworx.mclient.Protocol;

import javax.microedition.lcdui.*;

/**
 * Capture image from phone camera.
 *
 * @author Just van den Broecke
 * @version $Id: ImageCapture.java 254 2007-01-11 17:13:03Z just $
 */
public class ImageCaptureDisplay extends DefaultDisplay implements CameraListener, ProgressListener {

    private Command SEND_CMD = new Command("Send", Command.OK, 1);
    private Command OUTRO_CMD = new Command("Outro", Command.CANCEL, 1);

    private Displayable prevScreen;
    private boolean playing;
    private TextField name;
    private boolean active;
    private Display display;
    private boolean lastTaskComplete;

    private Gauge progressBar = new Gauge("", false, 100, 0);
    private int progressMax = 100;

    public ImageCaptureDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "Image Capture");
        display = Display.getDisplay(midlet);
    }

    public void camera() {
        try {
            CameraHandler.addListener(this);
            CameraHandler.takeSnapshot(display);
        } catch (Exception e) {
            //#style alertinfo
            append("Error:" + e.getMessage());
        }
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
        //#style formbox
        append(aMessage);
    }

    public void prSetContentLength(int aContentLength) {
        progressBar.setLabel("Downloading " + aContentLength + " bytes");
    }

    public void onFinish() {
        display.setCurrent(this);
        drawScreen();
    }

    public void onCancel() {
        active = false;
        Display.getDisplay(midlet).setCurrent(prevScreen);
    }

    private void drawScreen() {
        display.setCurrent(this);
        deleteAll();
        name = new TextField("", null, 24, TextField.ANY);
        //#style labelinfo
        append("Name your photo");
        //#style textbox
        append(name);

        addCommand(SEND_CMD);
    }

    private class PhotoUploader {
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
                                long photoTime = Util.getTime();
                                JXElement rsp = uploader.uploadMedium(TCPClient.getInstance().getAgentKey(), midlet.getKWUrl(), name.getString(), null, "image", "image/jpeg", photoTime, CameraHandler.getPhotoBytes(), false);
                                listener.prProgress(progressMax / 2);
                                if (rsp == null) {
                                    //#style formbox
                                    append("cannot submit audio!");
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

    /*private void sendPhoto(byte[] theBytes) {
        try {
            Uploader uploader = new Uploader();
            long photoTime = Util.getTime();
            JXElement rsp = uploader.uploadMedium(TCPClient.getInstance().getAgentKey(), midlet.getKWUrl(), name.getString(), null, "image", "image/jpeg", photoTime, theBytes, false);
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
            midlet.getActiveApp().sendRequest(addMediumReq);
        } catch (Throwable t) {
            //#style alertinfo
            append("Error:" + t.toString() + ":" + t.getMessage());
        }
    }
*/
    public void start(Displayable aPrevScreen, boolean isPlaying) {
        prevScreen = aPrevScreen;
        playing = isPlaying;
        active = true;
        display.setCurrent(this);
        camera();
    }

    public boolean isActive() {
        return active;
    }

    public void commandAction(Command c, Displayable d) {
        if (c == BACK_CMD) {
            CameraHandler.end();
            active = false;
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (c == SEND_CMD) {
            //#style labelinfo
            append("Uploading... (takes a while)");

            //#style formbox
            append(progressBar);

            new PhotoUploader().start(this);
            
            //sendPhoto(CameraHandler.getPhotoBytes());
        } else if (c == OUTRO_CMD) {
            active = false;
            new OutroDisplay(midlet);
        }
    }

    public void handleAddImageRsp(JXElement aResponse, String aText) {
        if (aResponse.getTag().equals("play-add-medium-rsp") || aResponse.getTag().equals("game-add-medium-rsp")) {
            deleteAll();
            removeCommand(SEND_CMD);

            //#style alertinfo
            append(aText);

            if(lastTaskComplete){
                removeCommand(BACK_CMD);
                addCommand(OUTRO_CMD);
            }
        }
    }

    public void completedLastTask(){
        lastTaskComplete = true;
    }

    public void handleAddImageNrsp(JXElement aResponse) {
        if (aResponse.getTag().equals("play-add-medium-nrsp") || aResponse.getTag().equals("game-add-medium-nrsp")) {
            deleteAll();
            //#style alertinfo
            append("Error sending Image - please try again.");
        }
    }

}
