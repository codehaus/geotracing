package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import java.util.Timer;
import java.util.TimerTask;

public class SplashDisplay extends Canvas {
    private WPMidlet midlet;
    int w = -1, h = -1;
    private Delayer delayer;
    // image objects
    private Image logoBanner;

    // screenstates
    private int screenName;


    public SplashDisplay(WPMidlet aMidlet, int aScreenName) {
        try {
            midlet = aMidlet;
            // load all images
            //#ifdef polish.images.directLoad
            logoBanner = Image.createImage("/logo.png");
            //#else
            logoBanner = scheduleImage("/logo.png");
            //#endif

            screenName = aScreenName;
        } catch (Throwable t) {
            System.out.println("could not load all images : " + t.toString());
        }
    }

    /**
     * Draws the screen.
     *
     * @param g The graphics object.
     */
    public void paint(Graphics g) {
        if (w == -1) {
            setFullScreenMode(true);
            w = getWidth();
            h = getHeight();
        }
        g.setColor(221, 221, 221);
        g.fillRect(0, 0, w, h);
        //g.drawImage(bg, (w - bg.getWidth()) / 2, (h - bg.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);        
        g.drawImage(logoBanner, (w - logoBanner.getWidth()) / 2, (h - logoBanner.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);

        if (delayer == null) {
            delayer = new Delayer(2);
        }
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
                    midlet.setHome();
                    repaint();
                } else {
                    try {
                        midlet.destroyApp(true);
                    } catch (Throwable t) {

                    }
                    midlet.notifyDestroyed();
                }
                timer.cancel(); //Terminate the timer thread
            }
        }
    }
}
