package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;
import javax.microedition.media.control.VideoControl;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.geotracing.client.Net;
import org.geotracing.client.Util;

public class MediaCanvas extends DefaultCanvas {

    // paint vars
    int w, h, fh;
    Font f;

    int x0, y0;
    int midx;

    private Player player;
    private VideoControl videoControl;
    private RecordControl recordControl;
    private PhotoCamera photoCamera;
    private AudioRecorder audioRecorder;

    // Text objects
    String poiType, poiName, poiDesc;
    private static final String textMime = "text/plain";

    private String errorMsg;

    private String inputText = "";

    public static final String[] keys = {" 0", ".,-!?@:;1", "aAbBcC2", "dDeEfF3", "gGhHiI4", "jJkKlL5", "mMnNoO6", "pPqQrRsS7", "tTuUvV8", "wWxXyYzZ9"};
    Timer keyTimer;
    int keyMajor = -1;
    int keyMinor;

    boolean showMenu;
    boolean photoShot;
    boolean audioRecorded;
    boolean textWritten;
    boolean poiWritten;

    // image objects
    private Image iconOverlay, inputBox, okBt, shootBt, recordBt;

    // icon buttons
    private Image[] icons = new Image[4];

    // screenstates
    private int screenStat = 0;
    private final static int PHOTO_STAT = 0;
    private final static int PHOTO_TAG_STAT = 1;
    private final static int AUDIO_STAT = 2;
    private final static int AUDIO_TAG_STAT = 3;
    private final static int TEXT_STAT = 4;
    private final static int TEXT_TAG_STAT = 5;
    private final static int POI_STAT = 6;
    private final static int POI_TAG_STAT = 7;

    private int fontType = Font.FACE_MONOSPACE;

    public MediaCanvas(WP aMidlet) {
        super(aMidlet);
        try {
            w = getWidth();
            h = getHeight();
            setFullScreenMode(true);

            int rate = Integer.parseInt(midlet.getAppProperty("audio-rate"));
            int bits = Integer.parseInt(midlet.getAppProperty("audio-bits"));

            // load all images
            backBt = Image.createImage("/back_button.png");

            icons[0] = Image.createImage("/poi_icon_small.png");
            icons[1] = Image.createImage("/assignment_icon_small.png");
            icons[2] = Image.createImage("/photo_icon_small.png");
            icons[3] = Image.createImage("/movie_icon_small.png");
            //icons[4] = Image.createImage("/movie_icon_small.png");

            iconOverlay = Image.createImage("/icon_overlay_small.png");
            inputBox = Image.createImage("/inputbox.png");
            okBt = Image.createImage("/ok_button.png");
            shootBt = Image.createImage("/shoot_button.png");
            recordBt = Image.createImage("/record_button.png");

            photoCamera = new PhotoCamera(player, videoControl);
            audioRecorder = new AudioRecorder(player, recordControl, rate, bits);
        } catch (Throwable t) {
            log("could not load all images : " + t.toString());
        }
    }

/*
    private boolean submitText(){
        JXElement rsp = Net.getInstance().uploadMedium(mediumName, "text", textMime, photoTime, photoData, false);
        if(rsp == null || rsp.getTag().indexOf("nrsp")!=-1){
            return false;
        }
        return true;
    }
*/


    /**
     * Draws the screen.
     *
     * @param g The graphics object.
     */
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(0, 0, 0);
        f = Font.getFont(fontType, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        g.setFont(f);
        fh = f.getHeight();

        ScreenUtil.drawIcons(g, w, margin, margin + logo.getHeight() + margin, icons, iconOverlay);
        switch (screenStat) {
            case PHOTO_STAT:
                g.setColor(0, 0, 0);
                f = Font.getFont(fontType, Font.STYLE_PLAIN, Font.SIZE_SMALL);
                g.setFont(f);
                ScreenUtil.drawTextArea(g, 100, margin, 3 * margin + logo.getHeight() + iconOverlay.getHeight(), topTextArea, middleTextArea, bottomTextArea);
                if(photoShot){
                    g.drawImage(photoCamera.getPreview(), 2*margin, margin + logo.getHeight() + 2 * margin, Graphics.TOP | Graphics.LEFT);
                    ScreenUtil.drawLeftSoftKey(g, h, okBt);
                }else{
                    g.drawRect(2*margin, margin + logo.getHeight() + 2 * margin,160, 120);
                photoCamera.show(2 * margin, margin + logo.getHeight() + 2 * margin, 160, 120);
                    ScreenUtil.drawLeftSoftKey(g, h, shootBt);
                }

                // the text
                String keySelect1 = "";
                if (keyMajor != -1) {
                    String all = keys[keyMajor];
                    keySelect1 = all.substring(0, keyMinor) + "[" + all.charAt(keyMinor) + "]" + all.substring(keyMinor + 1);
                }

                g.drawString("title", 2 * margin, 4 * margin + logo.getHeight() + iconOverlay.getHeight() + topTextArea.getHeight(), Graphics.TOP | Graphics.LEFT);
                g.drawImage(inputBox, 2 * margin, 5 * margin + logo.getHeight() + iconOverlay.getHeight() + topTextArea.getHeight() + fh, Graphics.TOP | Graphics.LEFT);
                g.drawString(inputText, 2 * margin, 5 * margin + logo.getHeight() + iconOverlay.getHeight() + topTextArea.getHeight() + fh + 2, Graphics.TOP | Graphics.LEFT);
                g.drawString(keySelect1, 2 * margin, 6 * margin + logo.getHeight() + iconOverlay.getHeight() + topTextArea.getHeight() + 2 * fh + 2, Graphics.TOP | Graphics.LEFT);

                break;
            case AUDIO_STAT:
                ScreenUtil.drawTextArea(g, 100, margin, margin + logo.getHeight() + margin + iconOverlay.getHeight() + margin, topTextArea, middleTextArea, bottomTextArea);                
                audioRecorder.create();
                ScreenUtil.drawLeftSoftKey(g, h, recordBt);
                break;
            case TEXT_STAT:
                ScreenUtil.drawTextArea(g, 100, margin, margin + logo.getHeight() + margin + iconOverlay.getHeight() + margin, topTextArea, middleTextArea, bottomTextArea);
                ScreenUtil.drawLeftSoftKey(g, h, okBt);
                break;
            case POI_STAT:
                ScreenUtil.drawTextArea(g, 100, margin, margin + logo.getHeight() + margin + iconOverlay.getHeight() + margin, topTextArea, middleTextArea, bottomTextArea);
                ScreenUtil.drawLeftSoftKey(g, h, okBt);
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
        log("key:" + key);
        if (key == -6 || key == -5 || getGameAction(key) == Canvas.FIRE) {
            /*if (key == -6 || key == -5) {*/
            switch(screenStat){
                case PHOTO_STAT:
                    if(photoShot){
                        screenStat= PHOTO_TAG_STAT;
                    }else{
                        photoCamera.capture();
                        photoShot = true;
                    }
                    break;
                case PHOTO_TAG_STAT:
                    photoCamera.upload();
                    photoShot = false;
                case AUDIO_STAT:
                    if(audioRecorded){
                        audioRecorder.play();
                        screenStat = AUDIO_TAG_STAT;
                    }else{
                        audioRecorder.start();
                        audioRecorded = true;
                    }
                    break;
                case AUDIO_TAG_STAT:
                    audioRecorder.upload();
                    audioRecorded = false;
                case TEXT_STAT:
                    break;
                case TEXT_TAG_STAT:
                    
                    textWritten = false;
                case POI_STAT:
                    break;
                case POI_TAG_STAT:

                    poiWritten = false;
            }

            // right softkey
        } else if (key == -7) {
            switch(screenStat){
                case PHOTO_TAG_STAT:
                    screenStat = PHOTO_STAT;
                    photoShot  = false;
                    break;
                case AUDIO_TAG_STAT:
                    screenStat = AUDIO_STAT;
                    audioRecorded = false;
                    break;
                case TEXT_TAG_STAT:
                    screenStat = TEXT_STAT;
                    break;
                case POI_TAG_STAT:
                    screenStat = POI_STAT;
                    break;
                default:
                    midlet.setScreen(WP.TRACE_CANVAS);
            }

            // left
        } else if (key == -3 || getGameAction(key) == Canvas.LEFT) {
            /*} else if (key == -3) {*/
            ScreenUtil.selectPrevIcon();
            switch (ScreenUtil.getSelectedIcon()) {
                case 1:
                    screenStat = AUDIO_STAT;
                    showMenu = false;
                    ScreenUtil.resetMenu();
                    break;
                case 2:
                    screenStat = POI_STAT;
                    showMenu = false;
                    ScreenUtil.resetMenu();
                    break;
                case 3:
                    screenStat = TEXT_STAT;
                    showMenu = false;
                    ScreenUtil.resetMenu();
                    break;
                case 4:
                    screenStat = PHOTO_STAT;
                    showMenu = false;
                    ScreenUtil.resetMenu();
                    break;
            }
            // right
        } else if (key == -4 || getGameAction(key) == Canvas.RIGHT) {
/*        } else if (key == -4) {*/
            ScreenUtil.selectNextIcon();
            switch (ScreenUtil.getSelectedIcon()) {
                case 1:
                    screenStat = TEXT_STAT;
                    showMenu = false;
                    break;
                case 2:
                    screenStat = PHOTO_STAT;
                    showMenu = false;
                    break;
                case 3:
                    screenStat = AUDIO_STAT;
                    showMenu = false;
                    break;
                case 4:
                    screenStat = POI_STAT;
                    showMenu = false;
                    break;
            }
            // up
            /*} else if (key == -1 || getGameAction(key) == Canvas.UP) {*/
        } else if (key == -1) {
            ScreenUtil.selectNextMenuItem();
            // down
            /*} else if (key == -2 || getGameAction(key) == Canvas.DOWN) {*/
        } else if (key == -2) {
            ScreenUtil.selectPrevMenuItem();
        } else if (getGameAction(key) == Canvas.KEY_STAR || key == Canvas.KEY_STAR) {
        } else if (getGameAction(key) == Canvas.KEY_POUND || key == Canvas.KEY_POUND) {
            midlet.setScreen(-1);
        } else if (key == -8) {
            inputText = inputText.substring(0, inputText.length() - 1);
        } else {
            if (keyTimer != null) keyTimer.cancel();

            int index = key - KEY_NUM0;

            if (index < 0 || index > keys.length)
                keyMajor = -1;
            else {
                if (index != keyMajor) {
                    keyMinor = 0;
                    keyMajor = index;
                } else {
                    keyMinor++;
                    if (keyMinor >= keys[keyMajor].length())
                        keyMinor = 0;
                }

                keyTimer = new Timer();
                keyTimer.schedule(new KeyConfirmer(this), 1000);
            }
        }
        repaint();
    }

    synchronized void keyConfirmed() {
        if (keyMajor != -1) {
            inputText += keys[keyMajor].charAt(keyMinor);
            keyMajor = -1;
            repaint();
        }
    }

    class KeyConfirmer extends TimerTask {
        MediaCanvas mainCanvas;

        private KeyConfirmer(MediaCanvas aCanvas) {
            mainCanvas = aCanvas;
        }

        public void run() {
            mainCanvas.keyConfirmed();
        }
    }

}
