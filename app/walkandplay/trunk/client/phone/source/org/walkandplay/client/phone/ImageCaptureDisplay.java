package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.GPSFetcher;
import org.geotracing.client.Util;
import org.walkandplay.client.external.CameraHandler;
import org.walkandplay.client.external.CameraListener;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextField;

/**
 * Capture image from phone camera.
 *
 * @author Just van den Broecke
 * @version $Id: ImageCapture.java 254 2007-01-11 17:13:03Z just $
 */
public class ImageCaptureDisplay extends DefaultDisplay implements CameraListener {

    private Command SEND_CMD = new Command("Send", Command.OK, 1);

    private Displayable prevScreen;
    private boolean playing;
    private TextField name;
    private boolean active;
    private Display display;

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

    public void onFinish() {
        display.setCurrent(this);
        drawScreen();
        CameraHandler.end();
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

    private void sendPhoto(byte[] theBytes) {
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
            active = false;
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (c == SEND_CMD) {
            sendPhoto(CameraHandler.getPhotoBytes());
        }
    }

    public void handleAddImageRsp(JXElement aResponse, String aText) {
        if (aResponse.getTag().equals("play-add-medium-rsp") || aResponse.getTag().equals("game-add-medium-rsp")) {
            deleteAll();
            removeCommand(SEND_CMD);

            //#style alertinfo
            append(aText);
        }
    }

    public void handleAddImageNrsp(JXElement aResponse) {
        if (aResponse.getTag().equals("play-add-medium-nrsp") || aResponse.getTag().equals("game-add-medium-nrsp")) {
            deleteAll();
            //#style alertinfo
            append("Error sending Image - please try again.");
        }
    }

}
