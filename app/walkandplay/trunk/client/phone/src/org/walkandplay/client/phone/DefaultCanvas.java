package org.walkandplay.client.phone;

import org.geotracing.client.GPSFetcher;
import org.geotracing.client.Net;
import org.geotracing.client.Util;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import java.util.Timer;
import java.util.TimerTask;

public class DefaultCanvas extends Canvas {

	// paint vars
	protected int w, h, fh;
	protected Font f;

	protected int margin = 3;

	protected WP midlet;

	// image objects
	protected Image logo, bg, backBt, gpsNetBar, redDot, blueDot, greenDot;
	protected Image menuTop, menuMiddle, menuBottom, menuSel;
	protected Image menuBt, topTextArea, middleTextArea, bottomTextArea;

	protected int fontType = Font.FACE_MONOSPACE;
    protected Menu menu;

    protected CanvasElement activeElement;

    public DefaultCanvas(WP aMidlet) {
		try {
			midlet = aMidlet;

			// load all images
			logo = Image.createImage("/logo.png");
			backBt = Image.createImage("/back_button.png");
			bg = Image.createImage("/bg.png");
			gpsNetBar = Image.createImage("/gpsnet_bg.png");
			redDot = Image.createImage("/red_dot.png");
			blueDot = Image.createImage("/blue_dot.png");
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

    protected void setActiveElement(CanvasElement aCanvasElement){
        activeElement = aCanvasElement;
    }

    // passes log msg to the main log method
	protected void log(String aMsg) {
		midlet.log(aMsg);
	}

	protected void placeMainLogo(Graphics aGraphics) {
		aGraphics.drawImage(logo, margin, margin, Graphics.TOP | Graphics.LEFT);
	}

	protected void placeGPSNetBar(Graphics aGraphics) {
		aGraphics.drawImage(gpsNetBar, w - gpsNetBar.getWidth() - margin, margin, Graphics.TOP | Graphics.LEFT);

		// Init Net/GPS status dots to DISCONNECTED (red)
		Image gpsDot = redDot, netDot = redDot;

		long now = Util.getTime();

		// TODO make green lights blinking in real-time (need timer)

		// GPS status: connected(idle) or connected(data)
		if (GPSFetcher.getInstance().getState() == GPSFetcher.CONNECTED) {
			// Make green if a valid location was sampled in last second
			//System.out.println("gpsDiff=" + (now - GPSFetcher.getInstance().getLastLocationTime()));
			gpsDot = now - GPSFetcher.getInstance().getLastLocationTime() < 15000 ? greenDot : blueDot;
		}

		// Net status: connected(idle) or sending data
		if (Net.getInstance().getState() == Net.CONNECTED) {
			//System.out.println("netDiff=" + (now - Net.getInstance().getLastCommandTime()));
			netDot = now - Net.getInstance().getLastCommandTime() < 15000 ? greenDot : blueDot;
		}

		aGraphics.drawImage(gpsDot, w - gpsNetBar.getWidth() - margin + 4, 10, Graphics.TOP | Graphics.LEFT);
		aGraphics.drawImage(netDot, w - gpsNetBar.getWidth() - margin + 41, 10, Graphics.TOP | Graphics.LEFT);
	}

	/**
	 * Draws the screen.
	 *
	 * @param g The graphics object.
	 */
	public void paint(Graphics g) {
		if (f == null) {
			setFullScreenMode(true);
			w = getWidth();
			h = getHeight();
			f = Font.getFont(fontType, Font.STYLE_PLAIN, Font.SIZE_SMALL);
			fh = f.getHeight();
            menu = new Menu(menuTop, menuMiddle, menuBottom, menuSel, h);
            setActiveElement(menu);
        }

		g.setFont(f);

		//g.setColor(153, 179, 204);
		g.setColor(255, 255, 255);
		g.fillRect(0, 0, w, h);

		g.drawImage(bg, (w - bg.getWidth()) / 2, (h - bg.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
		placeMainLogo(g);
		placeGPSNetBar(g);
		ScreenUtil.drawRightSoftKey(g, h, w, backBt, margin);

		g.setColor(0, 0, 0);

        activeElement.draw(g);
    }

    public void keyPressed(int key) {
        activeElement.keyPressed(key, getGameAction(key));
    }

    // creates a delay for the splashscreen
	protected class Forwarder {
		Timer timer;
		int screenName;

		public Forwarder(int aScreenName, int seconds) {
			timer = new Timer();
			screenName = aScreenName;
			timer.schedule(new DefaultCanvas.Forwarder.RemindTask(), seconds * 1000);
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
			timer.schedule(new DefaultCanvas.Delayer.RemindTask(), seconds * 1000);
		}

		class RemindTask extends TimerTask {
			public void run() {
				repaint();
				timer.cancel(); //Terminate the timer thread
			}
		}
	}


}
