package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import java.util.Timer;
import java.util.TimerTask;

public class SplashCanvas extends Canvas {
    private WP midlet;

    int w, h;
    // image objects
    private Image bg, gtLogo, kwxLogo;

    // screenstates
    private int screenName;


    public SplashCanvas(WP aMidlet, int aScreenName) {
        try {
            midlet = aMidlet;
            setFullScreenMode(true);
            w = getWidth();
            h = getHeight();
            // load all images
            bg = Image.createImage("/bg.png");
            gtLogo = Image.createImage("/gt_logo.png");
            kwxLogo = Image.createImage("/kwx_logo.png");
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
        g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
        g.drawImage(gtLogo, (w - gtLogo.getWidth())/2, (h - gtLogo.getHeight())/2, Graphics.TOP | Graphics.LEFT);
        g.drawImage(kwxLogo, 3, h - kwxLogo.getHeight() - 3, Graphics.TOP | Graphics.LEFT);
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
                    System.out.println("bye bye");
                    midlet.destroyApp(true);
                    midlet.notifyDestroyed();
                }
                timer.cancel(); //Terminate the timer thread
            }
        }
    }
}
