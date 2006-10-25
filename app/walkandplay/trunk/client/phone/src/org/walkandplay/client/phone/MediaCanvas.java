package org.walkandplay.client.phone;

import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;
import javax.microedition.media.control.RecordControl;
import java.io.ByteArrayOutputStream;
import java.util.TimerTask;
import java.util.Timer;

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

    private String inputText = "";

    public static final String[] keys = {" 0", ".,-!?@:;1", "aAbBcC2", "dDeEfF3", "gGhHiI4", "jJkKlL5", "mMnNoO6", "pPqQrRsS7", "tTuUvV8", "wWxXyYzZ9"};
    Timer keyTimer;
    int keyMajor = -1;
    int keyMinor;

    boolean showMenu;

    // image objects
    private Image logo, textArea, bg, backBt, iconOverlay, menuBt, topTextArea, middleTextArea, bottomTextArea, inputBox;

    // icon buttons
    private Image[] icons = new Image[4];

    // screenstates
    private int screenStat = 0;
    private final static int PHOTO_STAT = 0;
    //private final static int VIDEO_STAT = 1;
    private final static int AUDIO_STAT = 2;
    private final static int TEXT_STAT = 3;
    private final static int POI_STAT = 4;

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
            menuBt = Image.createImage("/menu_button.png");
            topTextArea = Image.createImage("/textarea_1.png");
            middleTextArea = Image.createImage("/textarea_2.png");
            bottomTextArea = Image.createImage("/textarea_3.png");

            icons[0] = Image.createImage("/poi_icon_small.png");
            icons[1] = Image.createImage("/assignment_icon_small.png");
            icons[2] = Image.createImage("/photo_icon_small.png");
            icons[3] = Image.createImage("/movie_icon_small.png");
            //icons[4] = Image.createImage("/movie_icon_small.png");

            iconOverlay = Image.createImage("/icon_overlay_small.png");
            inputBox = Image.createImage("/inputbox.png");

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

        ScreenUtil.placeIcons(g, w, margin, margin + logo.getHeight() + margin, icons, iconOverlay);
        ScreenUtil.setLeftBt(g, h, menuBt);
        switch (screenStat) {
            case PHOTO_STAT:
                g.setColor(0, 0, 0);
                f = Font.getFont(fontType, Font.STYLE_PLAIN, Font.SIZE_SMALL);
                g.setFont(f);
                ScreenUtil.drawTextArea(g, 100, margin, 3*margin + logo.getHeight() + iconOverlay.getHeight(), topTextArea, middleTextArea, bottomTextArea);
                showCamera(2*margin, margin + logo.getHeight() + 2*margin, 160, 120);
                
                // the text
                String keySelect1 = "";
                if (keyMajor != -1) {
                    String all = keys[keyMajor];
                    keySelect1 = all.substring(0, keyMinor) + "[" + all.charAt(keyMinor) + "]" + all.substring(keyMinor + 1);
                }

                g.drawString("title", 2*margin, 4*margin + logo.getHeight() + iconOverlay.getHeight() + topTextArea.getHeight(), Graphics.TOP | Graphics.LEFT);
                g.drawImage(inputBox, 2*margin, 5*margin + logo.getHeight() + iconOverlay.getHeight() + topTextArea.getHeight() + fh, Graphics.TOP | Graphics.LEFT);
                g.drawString(inputText, 2*margin, 5*margin + logo.getHeight() + iconOverlay.getHeight() + topTextArea.getHeight() + fh + 2, Graphics.TOP | Graphics.LEFT);
                g.drawString(keySelect1, 2*margin, 6*margin + logo.getHeight() + iconOverlay.getHeight() + topTextArea.getHeight() + 2*fh + 2, Graphics.TOP | Graphics.LEFT);

                if(showMenu){
                    String[] options = {"capture"};
                    ScreenUtil.createMenu(g, f, h, fh, options, menuTop, menuMiddle, menuBottom);
                }
                break;
            /*case VIDEO_STAT:
                ScreenUtil.drawTextArea(g, 100, margin, 3*margin + logo.getHeight() + iconOverlay.getHeight(), topTextArea, middleTextArea, bottomTextArea);
                g.drawString("press 'record' from the menu", 2*margin, 4*margin + logo.getHeight() + iconOverlay.getHeight() + topTextArea.getHeight(), Graphics.TOP | Graphics.LEFT);
                showCamera(2*margin, margin + logo.getHeight() + 2*margin, 160, 120);
                if(showMenu){
                    String[] options = {"record"};
                    ScreenUtil.createMenu(g, f, h, fh, options, menuTop, menuMiddle, menuBottom);
                }
                break;
            */case AUDIO_STAT:
                ScreenUtil.drawTextArea(g, 100, margin, margin + logo.getHeight() + margin + iconOverlay.getHeight() + margin, topTextArea, middleTextArea, bottomTextArea);
                g.drawString("press 'record' from the menu", 2*margin, margin + logo.getHeight() + margin + iconOverlay.getHeight() + margin + topTextArea.getHeight(), Graphics.TOP | Graphics.LEFT);
                showAudioRecorder();
                if(showMenu){
                    String[] options = {"record"};
                    ScreenUtil.createMenu(g, f, h, fh, options, menuTop, menuMiddle, menuBottom);
                }
                break;
            case TEXT_STAT:
                g.drawImage(textArea, margin, margin + logo.getHeight() + 3*margin, Graphics.TOP | Graphics.LEFT);
                //showCamera(2*margin, margin + logo.getHeight() + 2*margin, 160, 120);
                if(showMenu){
                    String[] options = {"place"};
                    ScreenUtil.createMenu(g, f, h, fh, options, menuTop, menuMiddle, menuBottom);
                }
                break;
            case POI_STAT:
                g.drawImage(textArea, margin, margin + logo.getHeight() + 3*margin, Graphics.TOP | Graphics.LEFT);
                //showCamera(2*margin, margin + logo.getHeight() + 2*margin, 160, 120);
                if(showMenu){
                    String[] options = {"place"};
                    ScreenUtil.createMenu(g, f, h, fh, options, menuTop, menuMiddle, menuBottom);
                }
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
        /*if (key == -6 || key == -5 || getGameAction(key) == Canvas.FIRE) {*/
        if (key == -6 || key == -5) {
            if(showMenu){
                showMenu = false;
            }else{
                showMenu = true;
            }
            // right softkey
        } else if (key == -7) {
            midlet.setScreen(WP.TRACE_CANVAS);
            // left
        /*} else if (key == -3 || getGameAction(key) == Canvas.LEFT) {*/
        } else if (key == -3) {
            ScreenUtil.prevIcon();
            switch(ScreenUtil.getSelectedIcon()){
                case 1:
                    screenStat = AUDIO_STAT;
                    showMenu= false;
                    ScreenUtil.resetMenu();
                    break;
                case 2:
                    screenStat = POI_STAT;
                    showMenu= false;
                    ScreenUtil.resetMenu();
                    break;
                case 3:
                    screenStat = TEXT_STAT;
                    showMenu= false;
                    ScreenUtil.resetMenu();
                    break;
                case 4:
                    screenStat = PHOTO_STAT;
                    showMenu= false;
                    ScreenUtil.resetMenu();
                    break;
                /*case 5:
                    screenStat = VIDEO_STAT;
                    showMenu= false;
                    break;*/
            }
            // right
        /*} else if (key == -4 || getGameAction(key) == Canvas.RIGHT) {*/
        } else if (key == -4) {
            ScreenUtil.nextIcon();
            switch(ScreenUtil.getSelectedIcon()){
                case 1:
                    screenStat = TEXT_STAT;
                    showMenu= false;
                    break;
                case 2:
                    screenStat = PHOTO_STAT;
                    showMenu= false;
                    break;
                /*case 3:
                    screenStat = VIDEO_STAT;
                    showMenu= false;
                    break;
                */case 4:
                    screenStat = AUDIO_STAT;
                    showMenu= false;
                    break;
                case 5:
                    screenStat = POI_STAT;
                    showMenu= false;
                    break;
            }
            // up
        /*} else if (key == -1 || getGameAction(key) == Canvas.UP) {*/
        } else if (key == -1) {
            // down
        /*} else if (key == -2 || getGameAction(key) == Canvas.DOWN) {*/
        } else if (key == -2) {            
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
