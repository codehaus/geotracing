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
    private Image iconOverlay;
    // icon buttons
    private Image[] icons = new Image[6];

    public HomeCanvas(WP aMidlet) {
        super(aMidlet);
        try {
            w = getWidth();
            h = getHeight();
            setFullScreenMode(true);

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
        ScreenUtil.placeIcons(g, w, 5, 30, icons, iconOverlay);
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
                    /*if(midlet.GPS_OK()){
                        midlet.setScreen(WP.FIND_TOURS_CANVAS);
                    }else{
                        gpsMsg = "select a gps first";
                    }*/
                    midlet.setScreen(WP.FIND_TOURS_CANVAS);
                    break;
                case 3:
                    /*if(midlet.GPS_OK()){
                        midlet.setScreen(WP.PLAY_TOURS_CANVAS);
                    }else{
                        gpsMsg = "select a gps first";
                    }*/
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
