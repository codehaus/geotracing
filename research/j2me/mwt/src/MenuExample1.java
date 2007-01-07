/*
 * MWT - Micro Window Toolkit
 * Copyright (C) 2006 Lucas Domanico - lucazd@gmail.com
 *
 * Licensed under the terms of the GNU Lesser General Public License:
 * 		http://www.opensource.org/licenses/lgpl-license.php
 *
 * For further information visit:
 * 		http://j2me-mwt.sourceforge.net/
 */

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import mwt.Label;
import mwt.Window;


public class MenuExample1 extends MIDlet {
	static MIDlet instance;

	protected void startApp() throws MIDletStateChangeException {
		instance = this;
		MenuCanvas1 c = new MenuCanvas1();
		Display.getDisplay(this).setCurrent(c);
		new Thread(c).start();
	}

	protected void pauseApp() {
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}
}

// Hello World
//

class MenuCanvas1 extends Canvas implements Runnable {
	Window win = new Window(0, 0, getWidth(), getHeight()); // the main window object
	boolean exit; // setted to true to finish the application

	// notify input
	protected void keyPressed(int keyCode) {
		win.setKeyState(keyCode, Window.KEYSTATE_PRESSED, true);
		if (keyCode == KEY_NUM5) exit = true; // exit the application
	}

	protected void keyReleased(int keyCode) {
		win.setKeyState(keyCode, Window.KEYSTATE_RELEASED, true);
	}

	public MenuCanvas1() {
		// create components
		win.add(new Label(10, 10, 120, 20, "Hello World!"));
	}

	public void run() {
		while (!exit) { // main loop
			win.repeatKeys(true);
			repaint();
			serviceRepaints();
			try {
				Thread.sleep(1);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		Example1.instance.notifyDestroyed();
	}

	protected void paint(Graphics g) {
		//g.setColor(0x000000);	// clear graphics
		//g.fillRect(0, 0, getWidth(), getHeight());
		// p("w=" + getWidth() + " h=" + getHeight());
		win.paint(g); // and paint the window...
	}

	public void p(String s) {
		System.out.println(s);
	}
}
