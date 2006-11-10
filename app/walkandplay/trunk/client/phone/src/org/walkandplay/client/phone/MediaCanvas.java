package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;
import javax.microedition.media.control.VideoControl;
import java.util.Timer;
import java.util.TimerTask;

public class MediaCanvas extends DefaultCanvas {
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
    private String titleText = "";
    private String tagText = "";
    public static final String[] keys = {" 0", ".,-!?@:;1", "aAbBcC2", "dDeEfF3", "gGhHiI4", "jJkKlL5", "mMnNoO6", "pPqQrRsS7", "tTuUvV8", "wWxXyYzZ9"};
    Timer keyTimer;
    int keyMajor = -1;
    int keyMinor;

    boolean showMenu;
    boolean photoShot;
    boolean audioRecorded;

    // image objects
    private Image iconOverlay, inputBox, okBt, addTagBt, shootBt, recordBt, topWhiteArea, middleWhiteArea, bottomWhiteArea;

    // icon buttons
    private Image[] icons = new Image[3];

    // screenstates
    private int screenStat = 4;
    private final static int PHOTO_STAT = 0;
    private final static int PHOTO_TAG_STAT = 1;
    private final static int AUDIO_STAT = 2;
    private final static int AUDIO_TAG_STAT = 3;
    private final static int POI_STAT = 4;
    private final static int POI_INPUT_STAT = 5;
    private final static int TAG_STAT = 6;

    private int fontType = Font.FACE_MONOSPACE;

    // tag canvas states
    private boolean tagCloudSelected;
    private boolean titleBoxSelected;
    private boolean tagBoxSelected;

    private int selectedTag = -1;
    private String myTags = "";    
    private String[] tagCloud = {"tag1", "tag2", "tag3", "tag4"};

    public MediaCanvas(WP aMidlet) {
        super(aMidlet);
        try {
            setFullScreenMode(true);

            int rate = Integer.parseInt(midlet.getAppProperty("audio-rate"));
            int bits = Integer.parseInt(midlet.getAppProperty("audio-bits"));

            // load all images
            backBt = Image.createImage("/back_button.png");

            icons[0] = Image.createImage("/poi_icon_small.png");
            icons[1] = Image.createImage("/photo_icon_small.png");
            icons[2] = Image.createImage("/movie_icon_small.png");
            //icons[3] = Image.createImage("/movie_icon_small.png");
            //icons[4] = Image.createImage("/movie_icon_small.png");

            iconOverlay = Image.createImage("/icon_overlay_small.png");
            inputBox = Image.createImage("/inputbox.png");
            okBt = Image.createImage("/ok_button.png");
            shootBt = Image.createImage("/shoot_button.png");
            recordBt = Image.createImage("/record_button.png");
            addTagBt = Image.createImage("/addtag_button.png");
            topWhiteArea = Image.createImage("/whitearea_1.png");
            middleWhiteArea = Image.createImage("/whitearea_2.png");
            bottomWhiteArea = Image.createImage("/whitearea_3.png");

            photoCamera = new PhotoCamera(player, videoControl);
            audioRecorder = new AudioRecorder(player, recordControl, rate, bits);
        } catch (Throwable t) {
            log("could not load all images : " + t.toString());
        }
    }

    private void drawTags(Graphics aGraphics, int anXOffset, int anYOffset) {
        int lineLength = 0;
        int ypos = anYOffset;
        for (int i = 0; i < tagCloud.length; i++) {
            String txt = tagCloud[i];
            int length = lineLength + f.stringWidth(txt) + margin;
            if (length > w) {
                ypos += fh;
                // start at the beginning again
                lineLength = 0;
            }
            if (selectedTag == i) {
                f = Font.getFont(fontType, Font.STYLE_UNDERLINED, Font.SIZE_SMALL);
                aGraphics.setFont(f);
                //txt = "*" + txt + "*";
                aGraphics.drawString(txt, anXOffset + lineLength + 1, ypos, Graphics.TOP | Graphics.LEFT);
                f = Font.getFont(fontType, Font.STYLE_PLAIN, Font.SIZE_SMALL);
                aGraphics.setFont(f);
            } else {
                aGraphics.drawString(txt, anXOffset + lineLength, ypos, Graphics.TOP | Graphics.LEFT);
            }
            // after placing
            lineLength += f.stringWidth(txt);
        }
    }

    private void addTag(){
        // first check if it's already added
        if(tagText.indexOf(tagCloud[selectedTag])!=-1) return;

        if(tagText.length() == 0){
            tagText += tagCloud[selectedTag];
        }else{
            tagText += " " + tagCloud[selectedTag];
        }        
    }

    private void selectFirstTag(){
        selectedTag = 0;
    }

    private void deselectTagCloud(){
        selectedTag = -1;
    }

    private void selectNextTag() {
        if (tagCloud != null) {
            if (selectedTag == (tagCloud.length - 1)) {
                selectedTag = 0;
            } else {
                selectedTag++;
            }
        }
    }

    private void selectPrevTag() {
        if (tagCloud != null) {
            if (selectedTag == 0) {
                selectedTag = (tagCloud.length - 1);
            } else {
                selectedTag--;
            }
        }
    }

    /**
     * Draws the screen.
     *
     * @param g The graphics object.
     */
    public void paint(Graphics g) {
        super.paint(g);
        switch (screenStat) {
            case PHOTO_STAT:
                g.setColor(0, 0, 0);
                f = Font.getFont(fontType, Font.STYLE_PLAIN, Font.SIZE_SMALL);
                g.setFont(f);

                ScreenUtil.drawIcons(g, w, (w - 2*margin - middleTextArea.getWidth())/2, margin + logo.getHeight() + margin, icons, iconOverlay);

                if(photoShot){
                    g.drawImage(photoCamera.getPreview(), (w - 2*margin - middleTextArea.getWidth())/2, margin + logo.getHeight() + icons[0].getHeight() + 2 * margin, Graphics.TOP | Graphics.LEFT);
                    ScreenUtil.drawLeftSoftKey(g, h, okBt, margin);
                }else{
                    g.drawRect((w - 2*margin - middleTextArea.getWidth())/2, margin + logo.getHeight() + icons[0].getHeight()+ 2 * margin,160, 120);
                    photoCamera.show((w - 2*margin - middleTextArea.getWidth())/2, margin + logo.getHeight() + 2 * margin, 160, 120);
                    ScreenUtil.drawLeftSoftKey(g, h, shootBt, margin);
                }

                break;
            case TAG_STAT:
                ScreenUtil.drawTextArea(g, 100, (w - 2*margin - middleTextArea.getWidth())/2, 3*margin + logo.getHeight() + iconOverlay.getHeight(), topTextArea, middleTextArea, bottomTextArea);

                String keySelect = "";
                if (keyMajor != -1) {
                    String all = keys[keyMajor];
                    keySelect = all.substring(0, keyMinor) + "[" + all.charAt(keyMinor) + "]" + all.substring(keyMinor + 1);
                }

                g.drawString("add a title", (w - middleTextArea.getWidth())/2, 4*margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);
                g.drawImage(inputBox, (w - middleTextArea.getWidth())/2, fh + 4*margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);
                g.drawString(titleText, (w - middleTextArea.getWidth() + 4)/2, fh + 2 + 4*margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);
                g.drawString("and some tags", (w - middleTextArea.getWidth())/2, fh + inputBox.getHeight() + 4*margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);
                g.drawImage(inputBox, (w - middleTextArea.getWidth())/2, 2*fh + inputBox.getHeight() + 4*margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);
                g.drawString(tagText, (w - middleTextArea.getWidth() + 4)/2, 2*fh + 2 + inputBox.getHeight() + 4*margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);
                g.drawString(keySelect, (w - middleTextArea.getWidth())/2, 2*fh + 2*inputBox.getHeight() + 4*margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);

                drawTags(g, (w - middleTextArea.getWidth())/2, 4*fh + 2*inputBox.getHeight() + 4*margin + logo.getHeight() + iconOverlay.getHeight());

                if(tagCloudSelected){
                    ScreenUtil.drawLeftSoftKey(g, h, addTagBt, margin);
                }else{
                    ScreenUtil.drawLeftSoftKey(g, h, okBt, margin);
                }
                break;
            case AUDIO_STAT:
                ScreenUtil.drawIcons(g, w, (w - 2*margin - middleTextArea.getWidth())/2, 2* margin + logo.getHeight(), icons, iconOverlay);
                ScreenUtil.drawTextArea(g, 100, (w - 2*margin - middleTextArea.getWidth())/2, 3*margin + logo.getHeight() + iconOverlay.getHeight(), topTextArea, middleTextArea, bottomTextArea);
                audioRecorder.create();
                ScreenUtil.drawLeftSoftKey(g, h, recordBt, margin);
                break;
            case POI_STAT:
                ScreenUtil.drawIcons(g, w, (w - 2*margin - middleTextArea.getWidth())/2, 2*margin + logo.getHeight(), icons, iconOverlay);
                ScreenUtil.drawTextArea(g, 100, (w - 2*margin - middleTextArea.getWidth())/2, 3*margin + logo.getHeight() + iconOverlay.getHeight(), topTextArea, middleTextArea, bottomTextArea);
                ScreenUtil.drawText(g, "press OK to start writing your poi.", (w - middleTextArea.getWidth())/2, 4*margin + logo.getHeight() + iconOverlay.getHeight(), fh, 60);
                ScreenUtil.drawLeftSoftKey(g, h, okBt, margin);
                break;
            case POI_INPUT_STAT:
                ScreenUtil.drawIcons(g, w, (w - 2*margin - middleTextArea.getWidth())/2, 2*margin + logo.getHeight(), icons, iconOverlay);
                ScreenUtil.drawTextArea(g, 100, (w - 2*margin - middleTextArea.getWidth())/2, 3*margin + logo.getHeight() + iconOverlay.getHeight(), topTextArea, middleTextArea, bottomTextArea);
                ScreenUtil.drawTextArea(g, 80, (w - middleTextArea.getWidth())/2, 4*margin + logo.getHeight() + iconOverlay.getHeight(), topWhiteArea, middleWhiteArea, bottomWhiteArea);

                // the text
                keySelect = "";
                if (keyMajor != -1) {
                    String all = keys[keyMajor];
                    keySelect = all.substring(0, keyMinor) + "[" + all.charAt(keyMinor) + "]" + all.substring(keyMinor + 1);
                }

                String lin1 = ("" + (f.stringWidth(inputText) / (w - 4 * margin)));
                int nrOfLines = Integer.parseInt(lin1.substring(0, 1));

                if (nrOfLines < 8) {
                    for (int i = 0; i < (nrOfLines + 1); i++) {
                        String txt = inputText.substring(i * 32, inputText.length());
                        g.drawString(txt, (w - middleTextArea.getWidth())/2, 2 * margin + logo.getHeight() + iconOverlay.getHeight() + topTextArea.getHeight() + i * fh, Graphics.TOP | Graphics.LEFT);
                    }
                    g.drawString(keySelect, (w - middleTextArea.getWidth())/2, 80 + 4 * margin + logo.getHeight() + iconOverlay.getHeight() + middleTextArea.getHeight() + fh, Graphics.TOP | Graphics.LEFT);
                }

                ScreenUtil.drawLeftSoftKey(g, h, okBt, margin);
                break;
        }
    }

    /**
     * Handles all key actions.
     *
     * @param key The Key that was hit.
     */
    public void keyPressed(int key) {
        /*log("key: " + key);*/
        log("screenStat: " + screenStat);
        if(screenStat == POI_INPUT_STAT || screenStat == TAG_STAT){
            if(titleBoxSelected){
                // start clean
                if(titleText.equals("_")) titleText = "";
            }else if(tagBoxSelected){
                // start clean
                if(tagText.equals("_")) tagText = "";
            }else{
                if(inputText.equals("_")) inputText = "";
            }

            if (key == -8) {
                if(titleBoxSelected){
                    titleText = titleText.substring(0, titleText.length() - 1);
                }else if(tagBoxSelected){
                    tagText = tagText.substring(0, tagText.length() - 1);
                }else{
                    inputText = inputText.substring(0, inputText.length() - 1);
                }
                repaint();
                return;
            }else if(key == Canvas.KEY_NUM0 || key == Canvas.KEY_NUM1 || key == Canvas.KEY_NUM2 || key == Canvas.KEY_NUM3 ||
                    key == Canvas.KEY_NUM4 || key == Canvas.KEY_NUM5 || key == Canvas.KEY_NUM6 || key == Canvas.KEY_NUM7 ||
                    key == Canvas.KEY_NUM8 || key == Canvas.KEY_NUM9){
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
                repaint();
                return;
            }
        }

        // left soft key & fire
        if (key == -6 || getGameAction(key) == Canvas.FIRE) {
            log("LEFT SOFTKEY or FIRE pressed");
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
                case POI_STAT:
                    screenStat = POI_INPUT_STAT;
                    // indicate that the user can write...
                    if(inputText.length() == 0) inputText = "_";
                    break;
                case POI_INPUT_STAT:
                    if(inputText.length()>0){
                        poiDesc = inputText;
                        titleText = "_";
                        titleBoxSelected = true;
                        screenStat = TAG_STAT;
                    }
                    break;
                case TAG_STAT:
                    if(tagCloudSelected){
                        addTag();
                    }else{
                        // create the poi
                        poiName = titleText;
                        WP.traceCanvas.getTracer().getNet().addPOI(poiType, poiName, poiDesc);
                        // TODO: do the tagging
                        //clear fields
                        inputText = "";
                        poiName = "";
                        poiDesc = "";
                    }
            }
            // right softkey
        } else if (key == -7) {
            log("RIGHT SOFTKEY pressed");
            switch(screenStat){
                case PHOTO_TAG_STAT:
                    screenStat = PHOTO_STAT;
                    photoShot  = false;
                    break;
                case AUDIO_TAG_STAT:
                    screenStat = AUDIO_STAT;
                    audioRecorded = false;
                    break;
                case POI_INPUT_STAT:
                    screenStat = POI_STAT;
                    inputText = "";
                    titleText = "";
                    tagText = "";
                    break;
                case TAG_STAT:
                    screenStat = POI_INPUT_STAT;
                    inputText = poiDesc;
                    break;
                default:
                    midlet.setScreen(WP.TRACE_CANVAS);
            }

            // left
        } else if (getGameAction(key) == Canvas.LEFT) {
            log("LEFT pressed");
            if(screenStat==TAG_STAT){
                selectPrevTag();
            }else{
                switch (ScreenUtil.getSelectedIcon()) {
                    // poi
                    case 1:
                        screenStat = AUDIO_STAT;
                        break;
                    // photo
                    case 2:
                        screenStat = POI_STAT;
                        break;
                    // audio
                    case 3:
                        screenStat = PHOTO_STAT;
                        break;
                }
                ScreenUtil.selectPrevIcon();
            }
            // right
        } else if (getGameAction(key) == Canvas.RIGHT) {
            log("RIGHT pressed");
            if(screenStat==TAG_STAT){
                selectNextTag();
            }else{
                switch (ScreenUtil.getSelectedIcon()) {
                    // poi
                    case 1:
                        screenStat = PHOTO_STAT;
                        break;
                    // photo
                    case 2:
                        screenStat = AUDIO_STAT;
                        break;
                    // audio
                    case 3:
                        screenStat = POI_STAT;
                        break;
                }
                ScreenUtil.selectNextIcon();
            }
            // up
        } else if (getGameAction(key) == Canvas.UP) {
            log("UP pressed");
            if(screenStat == TAG_STAT){
                if(tagCloudSelected){
                    tagBoxSelected = true;
                    titleBoxSelected = false;
                    tagCloudSelected = false;
                    if(tagText.length() == 0) tagText = "_";
                    deselectTagCloud();
                }else if(tagBoxSelected){
                    titleBoxSelected = true;
                    tagBoxSelected = false;
                    tagCloudSelected = false;
                    if(titleText.length() == 0) titleText = "_";
                }
            }else{
                ScreenUtil.selectNextMenuItem();
            }
            // down
        } else if (getGameAction(key) == Canvas.DOWN) {
            log("DOWN pressed");
            if(screenStat == TAG_STAT){
                if(titleBoxSelected){
                    tagBoxSelected = true;
                    titleBoxSelected = false;
                    tagCloudSelected = false;
                    if(tagText.length() == 0) tagText = "_";
                }else if(tagBoxSelected){
                    tagCloudSelected = true;
                    tagBoxSelected = false;
                    titleBoxSelected = false;
                    selectFirstTag();
                }                
            }else{
                ScreenUtil.selectPrevMenuItem();
            }
        } 
        repaint();
    }

    synchronized void keyConfirmed() {
        if (keyMajor != -1) {
            if(titleBoxSelected){
                titleText += keys[keyMajor].charAt(keyMinor);
            }else if(tagBoxSelected){
                tagText += keys[keyMajor].charAt(keyMinor);
            }else{
                inputText += keys[keyMajor].charAt(keyMinor);
            }

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
