package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import java.util.Timer;
import java.util.TimerTask;

public class SplashCanvas extends Canvas {
    private WPMidlet midlet;
    int w=-1, h=-1;
	private Delayer delayer;
    // image objects
    private Image bg, gtLogo, kwxLogo;

    // screenstates
    private int screenName;


    public SplashCanvas(WPMidlet aMidlet, int aScreenName) {
        try {
            midlet = aMidlet;            
            // load all images
            bg = Image.createImage("/bg.png");
            gtLogo = Image.createImage("/gt_logo.png");
            kwxLogo = Image.createImage("/kwx_logo.png");
            screenName = aScreenName;
            midlet.log("new splashscreen!! : " + screenName);            
        } catch (Throwable t) {
            midlet.log("could not load all images : " + t.toString());
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
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, w, h);
        g.drawImage(bg, (w - bg.getWidth())/2, (h - bg.getHeight())/2, Graphics.TOP | Graphics.LEFT);
        g.drawImage(gtLogo, (w - gtLogo.getWidth()) / 2, (h - gtLogo.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
        g.drawImage(kwxLogo, 3, h - kwxLogo.getHeight() - 3, Graphics.TOP | Graphics.LEFT);
        if (delayer == null) {
			delayer = new Delayer(2);
		}
    }

    // creates a delay for the splashscreen
    private class Delayer {
        Timer timer;

        public Delayer(int seconds) {
            midlet.log("SplashScreen: new delayer screenName=" + screenName);
            timer = new Timer();
            timer.schedule(new RemindTask(), seconds * 1000);
        }

        class RemindTask extends TimerTask {
            public void run() {
                if (screenName != -1) {
                    midlet.log("going to next screen!");
                    midlet.setScreen(screenName);
                    repaint();
                } else {
                    midlet.log("say bye bye");
                    midlet.destroyApp(true);
                    midlet.notifyDestroyed();
                }
                timer.cancel(); //Terminate the timer thread
            }
        }
    }
}
