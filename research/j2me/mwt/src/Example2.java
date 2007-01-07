import mwt.*;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

// Click Counter
//
class Canvas2 extends Canvas implements Runnable, EventListener {
	Window win = new Window(0,0,getWidth(),getHeight()); // the main window object
	boolean exit;	// setted to true to finish the application
	int clicks;		// counts the number of buttons clicks
	Label label = new Label(10,10,80,20,"Press the button!"); // this label will be added in the constructor
	Button counter = new Button(10,34,110,20,"I'm a MicroWT button",this,0);
	Button close = new Button(10,64,50,20,"Close",this,0);

	// notify input
	protected void keyPressed(int keyCode) 	{ win.setKeyState(keyCode,Window.KEYSTATE_PRESSED,true); }
	protected void keyReleased(int keyCode) { win.setKeyState(keyCode,Window.KEYSTATE_RELEASED,true); }

	// event listener implementation
	public void processEvent(int eventType, Component sender, Object[] args) {
		if(sender == counter) label.setText("N¼ of clicks:" + ++clicks + "!");
		else if(sender == close) exit = true;
	}

	public Canvas2() {
		// add components
		win.add(label);
		win.add(counter);
		win.add(close);
		win.setFocusFirst();
	}

	public void run() {
		while(!exit) { // main loop
			win.repeatKeys(true);
			repaint();
			serviceRepaints();
			try { Thread.sleep(1); }
			catch(Exception e) { e.printStackTrace(); }
		}
		Example2.instance.notifyDestroyed();
	}

	protected void paint(Graphics g) {
		win.paint(g); // and paint the window...
	}
}


public class  Example2 extends MIDlet {
	static MIDlet instance;
	protected void startApp() throws MIDletStateChangeException {
		instance = this;
		Canvas2 c = new Canvas2();
		Display.getDisplay(this).setCurrent(c);
		new Thread(c).start();
	}
	protected void pauseApp() { }
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException { }
}
