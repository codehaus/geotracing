package org.walkandplay.client.phone;

import org.geotracing.client.GPSFetcher;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;
import java.util.Timer;
import java.util.TimerTask;

public class DefaultGameCanvas extends GameCanvas {

    // paint vars
    int w, h, fh;
    Font f;

    int x0, y0;
    int midx;
    int margin = 3;

    protected WP midlet;

    // image objects
    protected Image logo, bg, backBt, gpsNetBar, redDot, greenDot;
    protected Image menuTop, menuMiddle, menuBottom, menuSel;
    protected Image menuBt, topTextArea, middleTextArea, bottomTextArea;

    protected int fontType = Font.FACE_MONOSPACE;

    public DefaultGameCanvas(WP aMidlet) {
        super(false);
        try {
            midlet = aMidlet;
            w = getWidth();
            h = getHeight();
            setFullScreenMode(true);

            // load all images
            logo = Image.createImage("/logo.png");
            backBt = Image.createImage("/back_button.png");
            bg = Image.createImage("/bg.png");
            gpsNetBar = Image.createImage("/gpsnet_bg.png");
            redDot = Image.createImage("/red_dot.png");
            greenDot = Image.createImage("/green_dot.png");
            menuTop = Image.createImage("/menu_top.png");
            menuMiddle = Image.createImage("/menu_middle.png");
            menuBottom = Image.createImage("/menu_bottom.png");
            menuSel = Image.createImage("/menu_sel.png");
            menuBt = Image.createImage("/menu_button.png");
            topTextArea = Image.createImage("/textarea_1.png");
            middleTextArea = Image.createImage("/textarea_2.png");
            bottomTextArea = Image.createImage("/textarea_3.png");

            // reset the menu's
            ScreenUtil.resetMenu();
        } catch (Throwable t) {
            log("could not load all images : " + t.toString());
        }
    }

    // passes log msg to the main log method
    protected void log(String aMsg) {
        midlet.log(aMsg);
    }

    protected void placeMainLogo(Graphics aGraphics) {
        aGraphics.drawImage(logo, margin, margin, Graphics.TOP | Graphics.LEFT);
    }

    protected void placeGPSNetBar(Graphics aGraphics) {
        aGraphics.drawImage(gpsNetBar, margin + logo.getWidth() + margin, margin, Graphics.TOP | Graphics.LEFT);
        if (GPSFetcher.getInstance().getState() == GPSFetcher.CONNECTED) {
            aGraphics.drawImage(greenDot, margin + logo.getWidth() + margin + 4, 10, Graphics.TOP | Graphics.LEFT);
        } else {
            aGraphics.drawImage(redDot, margin + logo.getWidth() + margin + 4, 10, Graphics.TOP | Graphics.LEFT);
        }
        if (GPSFetcher.getInstance().getState() == GPSFetcher.CONNECTED) {
            aGraphics.drawImage(greenDot, margin + logo.getWidth() + margin + 41, 10, Graphics.TOP | Graphics.LEFT);
        } else {
            aGraphics.drawImage(redDot, margin + logo.getWidth() + margin + 41, 10, Graphics.TOP | Graphics.LEFT);
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
        g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
        placeMainLogo(g);
        placeGPSNetBar(g);
        ScreenUtil.drawRightSoftKey(g, h, w, backBt, margin);
    }

    // creates a delay for the splashscreen
    protected class Forwarder {
        Timer timer;
        int screenName;

        public Forwarder(int aScreenName, int seconds) {
            timer = new Timer();
            screenName = aScreenName;
            timer.schedule(new DefaultGameCanvas.Forwarder.RemindTask(), seconds * 1000);
        }

        class RemindTask extends TimerTask {
            public void run() {
                if (screenName != -1) {
                    midlet.setScreen(screenName);
                    repaint();
                } else {
                    midlet.destroyApp(true);
                    midlet.notifyDestroyed();
                }
                timer.cancel(); //Terminate the timer thread
            }
        }
    }

    // creates a delay for the splashscreen
    protected class Delayer {
        Timer timer;

        public Delayer(int seconds) {
            timer = new Timer();
            timer.schedule(new DefaultGameCanvas.Delayer.RemindTask(), seconds * 1000);
        }

        class RemindTask extends TimerTask {
            public void run() {
                repaint();
                timer.cancel(); //Terminate the timer thread
            }
        }
    }



}
