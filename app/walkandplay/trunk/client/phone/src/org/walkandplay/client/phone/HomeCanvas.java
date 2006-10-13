package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class HomeCanvas extends DefaultCanvas {

    // paint vars
    int w, h, fh;
    Font f;

    int x0, y0;
    int midx;

    // image objects
    private Image logo, textArea, bg, menuBt, backBt, msgBar;
    // icon buttons
    private Image traceBtOn, traceBtOff, findBtOn, findBtOff, playBtOn, playBtOff;
    private Image helpBtOn, helpBtOff, settingsBtOn, settingsBtOff, gpsBtOn, gpsBtOff;
    private Image[] theOffIcons = new Image[6];
    private Image[] theOnIcons = new Image[6];


    // screenstates
    private int screenStat = 0;
    private final static int HOME_STAT = 0;
    private final static int MENU_STAT = 1;
    private final static int EXIT_STAT = 18;

    private int fontType = Font.FACE_MONOSPACE;

    public HomeCanvas(WP aMidlet) {
        super(aMidlet);
        try {
            w = getWidth();
            h = getHeight();
            setFullScreenMode(true);

            // load all images
            logo = Image.createImage("/logo.png");
            textArea = Image.createImage("/text_area.png");
            menuBt = Image.createImage("/menu_button.png");
            backBt = Image.createImage("/back_button.png");
            msgBar = Image.createImage("/msg_bar.png");
            traceBtOff = Image.createImage("/trace_button_off.png");
            traceBtOn = Image.createImage("/trace_button_on.png");
            findBtOff = Image.createImage("/find_button_off.png");
            findBtOn = Image.createImage("/find_button_on.png");
            playBtOff = Image.createImage("/play_button_off.png");
            playBtOn = Image.createImage("/play_button_on.png");

            helpBtOff = Image.createImage("/help_button_off.png");
            helpBtOn = Image.createImage("/help_button_on.png");
            settingsBtOff = Image.createImage("/settings_button_off.png");
            settingsBtOn = Image.createImage("/settings_button_on.png");
            gpsBtOff = Image.createImage("/gps_button_off.png");
            gpsBtOn = Image.createImage("/gps_button_on.png");

            bg = Image.createImage("/bg.png");

            theOffIcons[0] = traceBtOff;
            theOffIcons[1] = findBtOff;
            theOffIcons[2] = playBtOff;
            theOffIcons[3] = gpsBtOff;
            theOffIcons[4] = settingsBtOff;
            theOffIcons[5] = helpBtOff;
            theOnIcons[0] = traceBtOn;
            theOnIcons[1] = findBtOn;
            theOnIcons[2] = playBtOn;
            theOnIcons[3] = gpsBtOn;
            theOnIcons[4] = settingsBtOn;
            theOnIcons[5] = helpBtOn;

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
        if (f == null) {
            g.setColor(0, 0, 0);
            f = Font.getFont(fontType, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            g.setFont(f);
            fh = f.getHeight();
        }

        switch (screenStat) {
            case HOME_STAT:
                g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
                g.drawImage(logo, 5, 5, Graphics.TOP | Graphics.LEFT);
                //g.drawImage(textArea, 5, logo.getHeight() + 10, Graphics.TOP | Graphics.LEFT);
                //String text = "Press menu to see the options";
                //ScreenUtil.drawText(g, text, 10, logo.getHeight() + 15, fh);
                ScreenUtil.createIcons(g, 5, 30, theOffIcons, theOnIcons);
                String msg = "";
                switch (ScreenUtil.getSelectedIcon()) {
                    case 1:
                        msg = "start a trace";
                        break;
                    case 2:
                        msg = "find a tour";
                        break;
                    case 3:
                        msg = "play a game";
                        break;
                    case 4:
                        msg = "select a gps";
                        break;
                    case 5:
                        msg = "change settings";
                        break;
                    case 6:
                        msg = "get help";
                        break;
                }

                ScreenUtil.placeMsgBar(g, fh, msg, msgBar, h);

                //ScreenUtil.setLeftBt(g, h, menuBt);
                //ScreenUtil.setRightBt(g, h, w, backBt);
                break;
            case MENU_STAT:
                g.setColor(0, 0, 0);
                g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
                g.drawImage(logo, 5, 5, Graphics.TOP | Graphics.LEFT);
                g.drawImage(textArea, 5, logo.getHeight() + 10, Graphics.TOP | Graphics.LEFT);
                String text = "Press menu to see the options";
                ScreenUtil.drawText(g, text, 10, logo.getHeight() + 15, fh);
                if (midlet.GPS_OK()) {
                    String[] options = {"help", "settings", "change gps"};
                    ScreenUtil.createMenu(g, f, h, fh, options, menuTop, menuMiddle, menuBottom);
                } else {
                    String[] options = {"help", "settings", "select gps"};
                    ScreenUtil.createMenu(g, f, h, fh, options, menuTop, menuMiddle, menuBottom);
                }
                ScreenUtil.setRightBt(g, h, w, backBt);
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
                    midlet.setScreen(WP.TRACE_CANVAS);
                    break;
                case 2:
                    midlet.setScreen(WP.FIND_TOURS_CANVAS);
                    break;
                case 3:
                    midlet.setScreen(WP.PLAY_TOURS_CANVAS);
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
