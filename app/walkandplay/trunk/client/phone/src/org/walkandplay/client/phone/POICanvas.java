package org.walkandplay.client.phone;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Canvas;

public class POICanvas extends DefaultCanvas {

    // paint vars
    int w, h, fh;
    Font f;

    int x0, y0;
    int midx;

    String gpsMsg;

    // image objects
    private Image logo, textArea, bg, backBt;
    // icon buttons
    private Image[] theOffIcons = new Image[6];
    private Image[] theOnIcons = new Image[6];

    // screenstates
    private int screenStat = 0;
    private final static int HOME_STAT = 0;
    private final static int MENU_STAT = 1;

    private int fontType = Font.FACE_MONOSPACE;

    public POICanvas(WP aMidlet) {
        super(aMidlet);
        try {
            w = getWidth();
            h = getHeight();
            setFullScreenMode(true);

            // load all images
            logo = Image.createImage("/logo.png");
            textArea = Image.createImage("/text_area.png");
            backBt = Image.createImage("/back_button.png");
            bg = Image.createImage("/bg.png");

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
