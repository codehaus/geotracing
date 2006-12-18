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

import java.util.Random;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import mwt.Button;
import mwt.Component;
import mwt.EventListener;
import mwt.Label;
import mwt.Window;

// Plug In
//
class Canvas3 extends Canvas implements Runnable, EventListener {
	Window win = null;	// the main window -reference-
	boolean exit;		// setted to true to finish the application
	int x; 				// the sprite's x and y
	int y;
	boolean keyUp;		// these fields contains the key states
	boolean keyDown;
	boolean keyRight;
	boolean keyLeft;
	boolean keyMenu;
	static final int ACTION_RESUME = 0;	// button's action ids
	static final int ACTION_EXIT = 1;

	// event listener implementation
	public void processEvent(int eventType, Component sender, Object[] args) {
		switch(eventType) {
			case EVENT_ACTION: // when a button is pressed an event action is triggered
				switch(((Button)sender).getActionType()) {
					case ACTION_RESUME: win = null; break;
					case ACTION_EXIT: exit = true; break;
				}
				break;
			default: break;
		}
	}

	protected void keyPressed(int keyCode) {
		switch(keyCode) { // update keystates
			case KEY_NUM0:	keyMenu = true; break;
			case KEY_NUM2:	keyUp = true; break;
			case KEY_NUM8:	keyDown = true; break;
			case KEY_NUM4:	keyLeft = true; break;
			case KEY_NUM6:	keyRight = true; break;
		}
		if(win != null) win.setKeyState(keyCode,Window.KEYSTATE_PRESSED,true); // notify input
	}
	protected void keyReleased(int keyCode) {
		switch(keyCode) { // update keystates
			case KEY_NUM0:	keyMenu = false; break;
			case KEY_NUM2:	keyUp = false; break;
			case KEY_NUM8:	keyDown = false; break;
			case KEY_NUM4:	keyLeft = false; break;
			case KEY_NUM6:	keyRight = false; break;
		}
		if(win != null) win.setKeyState(keyCode,Window.KEYSTATE_RELEASED,true); // notify input
	}

	public void run() {
		while(!exit) { // main loop
			if(win == null) {
				// move the sprite
				if(keyLeft)	 x--;
				if(keyRight) x++;
				if(keyUp)	 y--;
				if(keyDown)	 y++;
				if(keyMenu) { // set the window
					// create components and the window
					win = new Window(8,8,100,100);
					win.add(new Label(4,4,40,30,"Menu"));
					win.add(new Button(4,20,50,20,"Resume",this,ACTION_RESUME));
					win.add(new Button(4,50,30,20,"Exit",this,ACTION_EXIT));
					win.setFocusFirst();
				}
			}
			else win.repeatKeys(true);
			repaint();
			serviceRepaints();
			try { Thread.sleep(1); }
			catch(Exception e) { e.printStackTrace(); }
		}
		Example3.instance.notifyDestroyed();
	}

	protected void paint(Graphics g) {
		g.setColor(0xFFFFFFFF);	// clear graphics
		g.fillRect(0,0,getWidth(),getHeight());

		Random r = new Random(); // paint the "background"
		for(int i=0; i<10 ;i++) {
			g.setColor(Math.abs(r.nextInt()));
			g.drawRect(Math.abs(r.nextInt())%128,Math.abs(r.nextInt())%128,Math.abs(r.nextInt())%128,Math.abs(r.nextInt())%128);
		}

		g.setColor(0,0,255); // paint the sprite
		g.fillRect(x,y,20,20);

		if(win != null) win.paint(g); // and paint the window...
	}
}


public class Example3 extends MIDlet {
	static MIDlet instance;
	protected void startApp() throws MIDletStateChangeException {
		instance = this;
		Canvas3 c = new Canvas3();
		Display.getDisplay(this).setCurrent(c);
		new Thread(c).start();
	}
	protected void pauseApp() { }
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException { }
}
