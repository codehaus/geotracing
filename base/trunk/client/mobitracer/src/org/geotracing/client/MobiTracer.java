// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.client;


import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;


/**
 *  Main program for MobiTracer, starts all.
 *
 * @author  Just van den Broecke
 * @version $Id$
 */
public class MobiTracer extends MIDlet implements Runnable {

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
		Display.getDisplay(this).setCurrent(traceScreen);
		traceScreen.start();
	}


}
