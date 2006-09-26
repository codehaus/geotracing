package org.walkandplay.client.phone;


import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;


/**
 *  Sends GPS data and ratings to server.
 *
 * @author  Just van den Broecke
 * @version $Id: WP.java 8 2006-08-28 15:36:01Z just $
 */
public class WP extends MIDlet implements Runnable {

    private LogScreen logScreen;

    /**
	 *  Sets up the midlet and the items required for user interaction.
	 */
	protected void startApp() {
		new Thread(this).start();
	}

	/**
	 *  Must be defined but no implementation required as the midlet only responds
	 *  to user interaction.
	 */
	protected void pauseApp() {
	}

	/**
	 *  Must be defined but no implementation required as all the resources
	 *  are released when the thread terminates.
	 */
	protected void destroyApp(boolean b) {

	}


	/**
	 * Starts GPS fetching and KW client.
	 */
	public void run() {
		TraceScreen traceScreen = new TraceScreen(this);
		logScreen = new LogScreen();
		Display.getDisplay(this).setCurrent(traceScreen);
		traceScreen.start();
	}

    public void log(String aMsg) {
        System.out.println(aMsg);
        logScreen.add(aMsg);        
    }


}
