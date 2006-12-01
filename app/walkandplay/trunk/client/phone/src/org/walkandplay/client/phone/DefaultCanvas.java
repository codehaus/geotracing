package org.walkandplay.client.phone;

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

	protected boolean gpsBlinking;

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

	// passes log msg to the main log method
	protected void log(String aMsg) {
		midlet.log(aMsg);
	}

	protected void blinkGPS() {
		gpsBlinking = true;
		repaint();
	}

	protected void placeDot(Graphics aGraphics, Image aDotImage, boolean isGPS) {
		aGraphics.drawImage(aDotImage, w - gpsNetBar.getWidth() - margin + (isGPS ? 4 : 41), 10, Graphics.TOP | Graphics.LEFT);

	}

	protected void placeMainLogo(Graphics aGraphics) {
		aGraphics.drawImage(logo, margin, margin, Graphics.TOP | Graphics.LEFT);
	}

	protected void placeGPSNetBar(Graphics aGraphics) {
		aGraphics.drawImage(gpsNetBar, w - gpsNetBar.getWidth() - margin, margin, Graphics.TOP | Graphics.LEFT);

		if (gpsBlinking) {
			placeDot(aGraphics, greenDot, true);
			// gpsBlinking = false;
		} else if (midlet.GPS_OK()) {
			placeDot(aGraphics, blueDot, true);
		} else {
			placeDot(aGraphics, redDot, true);
		}

		if (midlet.NET_OK()) {
			placeDot(aGraphics, blueDot, false);
		} else {
			placeDot(aGraphics, redDot, false);
		}
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
