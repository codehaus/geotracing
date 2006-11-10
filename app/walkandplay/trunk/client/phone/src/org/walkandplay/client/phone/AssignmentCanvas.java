package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class AssignmentCanvas extends DefaultCanvas {

    // paint vars
    int w, h, fh;
    Font f;

    int x0, y0;
    int midx;

    String gpsMsg;

    // image objects
    private Image logo, textArea, bg, backBt, msgBar, iconOverlay;
    // icon buttons
    private Image[] icons = new Image[6];

    // screenstates
    private int screenStat = 0;
    private final static int HOME_STAT = 0;
    private final static int MENU_STAT = 1;

    private int fontType = Font.FACE_MONOSPACE;

    public AssignmentCanvas(WP aMidlet) {
        super(aMidlet);
        try {
            w = getWidth();
            h = getHeight();
            setFullScreenMode(true);

            // load all images
            logo = Image.createImage("/logo.png");
            textArea = Image.createImage("/text_area.png");
            backBt = Image.createImage("/back_button.png");
            msgBar = Image.createImage("/msg_bar.png");
            bg = Image.createImage("/bg.png");

            icons[0] = Image.createImage("/trace_icon.png");
            icons[1] = Image.createImage("/find_icon.png");
            icons[2] = Image.createImage("/play_icon.png");
            icons[3] = Image.createImage("/gps_icon.png");
            icons[4] = Image.createImage("/settings_icon.png");
            icons[5] = Image.createImage("/help_icon.png");

            iconOverlay = Image.createImage("/icon_overlay.png");

            ScreenUtil.resetIcons();

        } catch (Throwable t) {
            log("could not load all images : " + t.toString());
        }
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

        switch (screenStat) {
            case HOME_STAT:
                /*g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
                g.drawImage(logo, 5, 5, Graphics.TOP | Graphics.LEFT);*/
                ScreenUtil.drawIcons(g, w, 5, 30, icons, iconOverlay);
//                String msg = "";
//                switch (ScreenUtil.getSelectedIcon()) {
//                    case 1:
//                        msg = "start a trace";
//                        break;
//                    case 2:
//                        msg = "find a tour";
//                        break;
//                    case 3:
//                        msg = "play a game";
//                        break;
//                    case 4:
//                        msg = "select a gps";
//                        break;
//                    case 5:
//                        msg = "change settings";
//                        break;
//                    case 6:
//                        msg = "get help";
//                        break;
//                }

//                if(gpsMsg!=null && gpsMsg.length()>0) msg = gpsMsg;

                //ScreenUtil.drawMessageBar(g, fh, msg, msgBar, h);
                break;
            case MENU_STAT:
                g.setColor(0, 0, 0);
                g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
                g.drawImage(logo, 5, 5, Graphics.TOP | Graphics.LEFT);
                g.drawImage(textArea, 5, logo.getHeight() + 10, Graphics.TOP | Graphics.LEFT);
                String text = "Press menu to see the options";
                ScreenUtil.drawText(g, text, 10, logo.getHeight() + 15, fh, 100);
                if (midlet.GPS_OK()) {
                    String[] options = {"help", "settings", "change gps"};
                    ScreenUtil.drawMenu(g, h, options, menuTop, menuMiddle, menuBottom, menuSel);
                } else {
                    String[] options = {"help", "settings", "select gps"};
                    ScreenUtil.drawMenu(g, h, options, menuTop, menuMiddle, menuBottom, menuSel);
                }
                ScreenUtil.drawRightSoftKey(g, h, w, backBt, margin);
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
            switch (ScreenUtil.getSelectedIcon()) {
                case 1:
                    /*if(midlet.GPS_OK()){
                        midlet.setScreen(WP.TRACE_CANVAS);
                    }else{
                        gpsMsg = "select a gps first";
                    }*/
                    midlet.setScreen(WP.TRACE_CANVAS);
                    break;
                case 2:
                    if (midlet.GPS_OK()) {
                        midlet.setScreen(WP.FIND_TOURS_CANVAS);
                    } else {
                        gpsMsg = "select a gps first";
                    }
                    break;
                case 3:
                    if (midlet.GPS_OK()) {
                        midlet.setScreen(WP.PLAY_TOURS_CANVAS);
                    } else {
                        gpsMsg = "select a gps first";
                    }
                    break;
                case 4:
                    midlet.setScreen(WP.GPS_CANVAS);
                    break;
                case 5:
                    midlet.setScreen(WP.SETTINGS_CANVAS);
                    break;
                case 6:
                    midlet.setScreen(WP.HELP_CANVAS);
                    break;
            }
            // right softkey
        } else if (key == -7) {
            midlet.setScreen(-1);
            // left
        } else if (key == -3 || getGameAction(key) == Canvas.LEFT) {
            ScreenUtil.selectPrevIcon();
            // right
        } else if (key == -4 || getGameAction(key) == Canvas.RIGHT) {
            ScreenUtil.selectNextIcon();
            // up
        } else if (key == -1 || getGameAction(key) == Canvas.UP) {
            ScreenUtil.selectUpperIcon();
            // down
        } else if (key == -2 || getGameAction(key) == Canvas.DOWN) {
            ScreenUtil.selectLowerIcon();
        } else if (getGameAction(key) == Canvas.KEY_STAR || key == Canvas.KEY_STAR) {
        } else if (getGameAction(key) == Canvas.KEY_POUND || key == Canvas.KEY_POUND) {
            midlet.setScreen(-1);
        } else if (key == -8) {
        } else {
        }

        repaint();
    }

}
