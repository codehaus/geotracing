package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class MainScreen extends Canvas {

    // paint vars
    int w, h, fh;
    Font f;

    int x0, y0;
    int midx;

    private WP midlet;

    // image objects
    private Image splashLogo;

    // screenstates
    private int screenStat;
    private final static int SPLASH_STAT = 0;
    private final static int HOME_STAT = 1;
    private final static int EXIT_STAT = 18;


    private int fontType = Font.FACE_MONOSPACE;

    public MainScreen(WP aMidlet) {
        try {
            midlet = aMidlet;
            setFullScreenMode(true);

            // load all images
            splashLogo = Image.createImage("/splash.png");

            log("initialisation ok!");
        } catch (Throwable t) {
            log("could not load all images : " + t.toString());
        }
    }

    // passes log msg to the main log method
    private void log(String aMsg) {
        midlet.log(aMsg);
    }

    private void setLeftBt(Graphics aGraphics, Image anImage) {
        aGraphics.drawImage(anImage, 0, h - anImage.getHeight(), Graphics.TOP | Graphics.LEFT);
    }

    private void setRightBt(Graphics aGraphics, Image anImage) {
        aGraphics.drawImage(anImage, w - anImage.getWidth(), h - anImage.getHeight(), Graphics.TOP | Graphics.LEFT);
    }

    private void createBackground(Graphics aGraphics) {
        if (f == null) {
            aGraphics.setColor(0, 0, 0);
            f = Font.getFont(fontType, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            aGraphics.setFont(f);
            w = getWidth();
            h = getHeight();
            fh = f.getHeight();
        }
        aGraphics.setColor(0, 0, 0);
        f = Font.getFont(fontType, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        aGraphics.setFont(f);

        // draw background: 193, 209, 125
        aGraphics.setColor(193, 209, 125);
        aGraphics.fillRect(0, 0, w, h);

        // draw head
        aGraphics.setColor(0, 90, 33);
//        aGraphics.fillRect(0, 0, w, headHeight);
//        // draw bottom
//        aGraphics.fillRect(0, h - bottomHeight, w, bottomHeight);
//        aGraphics.drawImage(headlogo, 0, 0, Graphics.TOP | Graphics.LEFT);
//        if (midlet.isConnected() && screenStat != HOME_STAT) {
//            aGraphics.drawImage(connectedlogo, w - connectedlogo.getWidth(), 0, Graphics.TOP | Graphics.LEFT);
//            aGraphics.setColor(255, 255, 255);
//            aGraphics.drawString(hotspot.getName(), w - connectedlogo.getWidth() - 3 - f.stringWidth(hotspot.getName()), padding, Graphics.TOP | Graphics.LEFT);
//        }
    }

    private void createMenu(Graphics aGraphics) {
        // white bg
        aGraphics.setColor(255, 255, 255);
//        aGraphics.fillRect(0, headHeight, w, menuHeight);
        // green line
        aGraphics.setColor(0, 90, 33);
//        aGraphics.fillRect(0, headHeight + menuHeight, w, 1);
    }

    /**
     * Draws the screen.
     *
     * @param g The graphics object.
     */
    public void paint(Graphics g) {
        switch (screenStat) {
            case SPLASH_STAT:
                g.drawImage(splashLogo, 0, 0, Graphics.TOP | Graphics.LEFT);
                new Delayer(4, HOME_STAT);
                break;
            case HOME_STAT:
                createBackground(g);
//                g.drawImage(welcomeScreen, 0, headHeight, Graphics.TOP | Graphics.LEFT);
//                setRightBt(g, btExit);
                break;
            case EXIT_STAT:
                // create a white bg
                g.setColor(255, 255, 255);
                g.fillRect(0, 0, w, h);
                g.drawImage(splashLogo, 0, 0, Graphics.TOP | Graphics.LEFT);

                new Delayer(2, -1);

                break;
        }
    }

    // creates a delay for the splashscreen
    private class Delayer {
        Timer timer;

        public Delayer(int seconds, int aScreen) {
            timer = new Timer();
            timer.schedule(new RemindTask(aScreen), seconds * 1000);
        }

        class RemindTask extends TimerTask {

            int screen = -1;

            public RemindTask(int aScreen) {
                screen = aScreen;
            }

            public void run() {
                if (screen != -1) {
                    screenStat = screen;
                    repaint();
                } else {
                    midlet.destroyApp(true);
                    midlet.notifyDestroyed();
                }
                timer.cancel(); //Terminate the timer thread
            }
        }
    }

    private void closeApp() {
        new Delayer(4, -1);
    }

    /**
     * Handles all key actions.
     *
     * @param key The Key that was hit.
     */
    public void keyPressed(int key) {
        /*log("screenstat: " + screenStat);
        log("key: " + key);*/
        // left soft key & fire
        // if (getGameAction(key) == Canvas.FIRE) {
        if (key == -6 || key == -5) {
            switch (screenStat) {
                case HOME_STAT:

                    break;
            }
            // right softkey
        } else if (key == -7) {
            switch (screenStat) {
                case HOME_STAT:
                    screenStat = EXIT_STAT;
                    break;
        }
        // left
        //} else if (getGameAction(key) == Canvas.LEFT) {
        }else if (key == -3) {
            switch (screenStat) {
                case HOME_STAT:
                    break;
            }
            // right
            //} else if (getGameAction(key) == Canvas.RIGHT) {
        } else if (key == -4) {
            switch (screenStat) {
                case HOME_STAT:
                    closeApp();
                    break;
            }
            // up
            //} else if (getGameAction(key) == Canvas.UP) {
        } else if (key == -1) {
            switch (screenStat) {
                case HOME_STAT:
                    break;
            }
            // down
            //} else if (getGameAction(key) == Canvas.DOWN) {
        } else if (key == -2) {
            switch (screenStat) {
                case HOME_STAT:
                    break;
            }
        } else if (getGameAction(key) == Canvas.KEY_STAR || key == Canvas.KEY_STAR) {

        } else if (getGameAction(key) == Canvas.KEY_POUND || key == Canvas.KEY_POUND) {
            screenStat = EXIT_STAT;
        } else if (key == -8) {

        } else {

        }

        repaint();
    }    

}
