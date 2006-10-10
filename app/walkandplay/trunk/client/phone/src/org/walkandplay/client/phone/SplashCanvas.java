package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import java.util.Timer;
import java.util.TimerTask;

public class SplashCanvas extends Canvas {
    private WP midlet;

    // image objects
    private Image splashLogo;

    // screenstates
    private int screenName;


    public SplashCanvas(WP aMidlet, int aScreenName) {
        try {
            midlet = aMidlet;
            setFullScreenMode(true);

            // load all images
            splashLogo = Image.createImage("/splash.png");
            screenName = aScreenName;
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
        g.drawImage(splashLogo, 0, 0, Graphics.TOP | Graphics.LEFT);
        new Delayer(4);

    }

    // creates a delay for the splashscreen
    private class Delayer {
        Timer timer;

        public Delayer(int seconds) {
            timer = new Timer();
            timer.schedule(new RemindTask(), seconds * 1000);
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
}
