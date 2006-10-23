package org.walkandplay.client.phone;

import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;
import javax.microedition.media.control.RecordControl;
import java.io.ByteArrayOutputStream;

public class MediaCanvas extends DefaultCanvas {

    // paint vars
    int w, h, fh;
    Font f;

    int x0, y0;
    int midx;

    private Player player;
    private VideoControl videoControl;
    private byte[] imageBytes;

    private RecordControl recordControl;
    private ByteArrayOutputStream output;
    private int rate, bits;
    private int kbPerSec;


    // image objects
    private Image logo, textArea, bg, backBt, iconOverlay;

    // icon buttons
    private Image[] icons = new Image[4];

    // screenstates
    private int screenStat = 0;
    private final static int HOME_STAT = 0;
    private final static int MENU_STAT = 1;

    private int fontType = Font.FACE_MONOSPACE;

    public MediaCanvas(WP aMidlet) {
        super(aMidlet);
        try {
            w = getWidth();
            h = getHeight();
            setFullScreenMode(true);

            rate = Integer.parseInt(midlet.getAppProperty("audio-rate"));
            bits = Integer.parseInt(midlet.getAppProperty("audio-bits"));
            kbPerSec = (rate * bits / 8) / 1000;

            // load all images
            logo = Image.createImage("/logo.png");
            textArea = Image.createImage("/text_area.png");
            backBt = Image.createImage("/back_button.png");
            bg = Image.createImage("/bg.png");

            icons[0] = Image.createImage("/assignment_icon.png");
            icons[1] = Image.createImage("/poi_icon.png");
            icons[2] = Image.createImage("/photo_icon.png");
            icons[3] = Image.createImage("/movie_icon.png");

            iconOverlay = Image.createImage("/icon_overlay.png");

        } catch (Throwable t) {
            log("could not load all images : " + t.toString());
        }
    }

    private void showAudioRecorder(){
        try {
            player = Manager.createPlayer("capture://audio?rate=" + rate + "&bits=" + bits);
            player.realize();
            recordControl =
                    (RecordControl) player.getControl("RecordControl");
            output = new ByteArrayOutputStream();
            recordControl.setRecordStream(output);
        } catch (Throwable t) {
            midlet.log("Exception initialising audiorecorder : " + t);
        }
    }

    private void closeAudioRecorder(){
         try {
            // close the player and videocontrol
            if (player != null) {
                player.stop();
                player.close();
                player = null;
            }

            if (recordControl != null) {
                recordControl = null;
            }
        } catch (MediaException me) {
            midlet.log("Exception closing the audiorecorder: " + me.toString());
        }
    }

    private void showCamera(int anX, int anY, int aWidth, int aHeight) {
        try {
            // create the player if it does not exist
            if (player == null) {
                player = Manager.createPlayer("capture://video");
                player.realize();
            }

            // create the video control if it does not exist
            if (videoControl == null) {
                videoControl = (VideoControl) player.getControl("VideoControl");
                videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
            }

            // place the camera screen
            videoControl.setDisplayLocation(anX, anY);
            videoControl.setDisplaySize(aWidth, aHeight);
            videoControl.setVisible(true);

            // show the player input
            player.start();

        } catch (Throwable ioe) {
            midlet.log("Exception initialising camera : " + ioe);
        }
    }

    private void closeCamera() {
        try {
            // close the player and videocontrol
            if (player != null) {
                player.stop();
                player.close();
                player = null;
            }

            if (videoControl != null) {
                videoControl = null;
            }
        } catch (MediaException me) {
            midlet.log("Exception closing the camera: " + me.toString());
        }
    }

    public void photoCapture() {
        try {

            // create the captured image
            imageBytes = videoControl.getSnapshot("encoding=png&width=160&height=120");

            // Shut down the player.
            closeCamera();

        } catch (Throwable me) {
            midlet.log("Exception trying to capture : " + me);
        }
    }

    public Image getCapturedImage() {
        return Image.createImage(imageBytes, 0, imageBytes.length);
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }


    /**
     * Draws the screen.
     *
     * @param g The graphics object.
     */
    public void paint(Graphics g) {
        super.paint(g);
        if (f == null) {
            g.setColor(0, 0, 0);
            f = Font.getFont(fontType, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            g.setFont(f);
            fh = f.getHeight();
        }

        ScreenUtil.createIcons(g, 5, 30, icons, iconOverlay);

        switch (screenStat) {
            case HOME_STAT:
                g.drawImage(textArea, margin, margin + logo.getHeight() + margin, Graphics.TOP | Graphics.LEFT);
                //showCamera(2*margin, margin + logo.getHeight() + 2*margin, 160, 120);
                break;
            case MENU_STAT:

                break;
        }
    }

    /**
     * Handles all key actions.
     *
     * @param key The Key that was hit.
     */
    public void keyPressed(int key) {
        // left soft key & fire
        if (key == -6 || key == -5 || getGameAction(key) == Canvas.FIRE) {

            // right softkey
        } else if (key == -7) {
            midlet.setScreen(-1);
            // left
        } else if (key == -3 || getGameAction(key) == Canvas.LEFT) {
            ScreenUtil.prevIcon();
            // right
        } else if (key == -4 || getGameAction(key) == Canvas.RIGHT) {
            ScreenUtil.nextIcon();
            // up
        } else if (key == -1 || getGameAction(key) == Canvas.UP) {
            ScreenUtil.upIcon();
            // down
        } else if (key == -2 || getGameAction(key) == Canvas.DOWN) {
            ScreenUtil.downIcon();
        } else if (getGameAction(key) == Canvas.KEY_STAR || key == Canvas.KEY_STAR) {
        } else if (getGameAction(key) == Canvas.KEY_POUND || key == Canvas.KEY_POUND) {
            midlet.setScreen(-1);
        } else if (key == -8) {
        } else {
        }

        repaint();
    }

}
