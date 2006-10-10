package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class HomeCanvas extends Canvas {

    // paint vars
    int w, h, fh;
    Font f;

    int x0, y0;
    int midx;

    private WP midlet;

    // image objects
    private Image logo, textArea, bg, menuBt, backBt, msgBar;
    // icon buttons
    private Image traceBtOn, traceBtOff, findBtOn, findBtOff, playBtOn, playBtOff;
    private Image[] theOffIcons = new Image[3];
    private Image[] theOnIcons = new Image[3];


    // screenstates
    private int screenStat = 0;
    private final static int HOME_STAT = 0;
    private final static int MENU_STAT = 1;
    private final static int EXIT_STAT = 18;

    private int fontType = Font.FACE_MONOSPACE;

    public HomeCanvas(WP aMidlet) {
        try {
            midlet = aMidlet;
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
            bg = Image.createImage("/bg.png");

            theOffIcons[0] = traceBtOff;
            theOffIcons[1] = findBtOff;
            theOffIcons[2] = playBtOff;
            theOnIcons[0] = traceBtOn;
            theOnIcons[1] = findBtOn;
            theOnIcons[2] = playBtOn;
        } catch (Throwable t) {
            log("could not load all images : " + t.toString());
        }
    }

    // passes log msg to the main log method
    private void log(String aMsg) {
        midlet.log(aMsg);
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

                ScreenUtil.createIcons(g, 5, 20, theOffIcons, theOnIcons);
                ScreenUtil.placeMsgBar(g, msgBar, h);
                ScreenUtil.setLeftBt(g, h, menuBt);
                ScreenUtil.setRightBt(g, h, w, backBt);
                break;
            case MENU_STAT:
                g.setColor(0, 0, 0);
                g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
                g.drawImage(logo, 5, 5, Graphics.TOP | Graphics.LEFT);
                g.drawImage(textArea, 5, logo.getHeight() + 10, Graphics.TOP | Graphics.LEFT);
                String text = "Press menu to see the options";
                ScreenUtil.drawText(g, text, 10, logo.getHeight() + 15, fh);                
                if(midlet.GPS_OK()){
                    String[] options = {"help", "settings", "change gps"};
                    ScreenUtil.createMenu(g, f, h, fh, options);
                }else{
                    String[] options = {"help", "settings", "select gps"};
                    ScreenUtil.createMenu(g, f, h, fh, options);
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
//        log("screenstat: " + screenStat);
//        log("key: " + key);
//        log("getGameAction(key): " + getGameAction(key));
        // left soft key & fire
        if (key == -6 || key == -5 || getGameAction(key) == Canvas.FIRE) {
            switch (screenStat) {
                case HOME_STAT:
                    screenStat = MENU_STAT;
                    break;
                case MENU_STAT:
                    if(ScreenUtil.getSelectedMenuItem() == 2){
                        log("going to gps screen!!");
                        midlet.setScreen(WP.GPS_CANVAS);
                    }else if(ScreenUtil.getSelectedMenuItem() == 5){
                        log("going to map screen!!");
                        midlet.setScreen(WP.MAP_CANVAS);
                    }
                    break;
            }
            // right softkey
        } else if (key == -7) {
            switch (screenStat) {
                case HOME_STAT:
                    break;
                case MENU_STAT:
                    screenStat = HOME_STAT;
                    break;
        }
        // left
        }else if (key == -3 || getGameAction(key) == Canvas.LEFT) {
            switch (screenStat) {
                case HOME_STAT:
                    break;
            }
            // right
        } else if (key == -4 || getGameAction(key) == Canvas.RIGHT) {
            switch (screenStat) {
                case HOME_STAT:
                    break;
            }
            // up
        } else if (key == -1 || getGameAction(key) == Canvas.UP) {
            switch (screenStat) {
                case MENU_STAT:
                    ScreenUtil.nextMenuItem();
                    break;
            }
            // down
        } else if (key == -2 || getGameAction(key) == Canvas.DOWN) {
            switch (screenStat) {
                case MENU_STAT:
                    ScreenUtil.prevMenuItem();
                    break;
            }
        } else if (getGameAction(key) == Canvas.KEY_STAR || key == Canvas.KEY_STAR) {

        } else if (getGameAction(key) == Canvas.KEY_POUND || key == Canvas.KEY_POUND) {
            midlet.setScreen(-1);
        } else if (key == -8) {

        } else {

        }

        repaint();
    }

}
