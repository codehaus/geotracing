// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.client;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

/**
 * Very basic rotating log.
 *
 * @author  Just van den Broecke
 * @version $Id$
 */
public class Log {

	private static final int LOG_SZ = 16;
	private static String[] logMsgs = new String[LOG_SZ];
	private static int logIndex = 0;

	private Log() {
	}

	public static void log(String aMsg) {
		synchronized (logMsgs) {
			logMsgs[logIndex++] = aMsg;

			// Rotate if full
			if (logIndex == LOG_SZ) {
				logIndex = 0;
			}
		}
	}


	/*
	* Create the first TextBox and associate
	* the exit command and listener.
	*/
	public static void view(MIDlet aMidlet) {
		final Displayable prevScreen = Display.getDisplay(aMidlet).getCurrent();
		final MIDlet midlet = aMidlet;

		Form form = new Form("Log Viewer");

		// Single command to go back to prev screen
		form.addCommand(new Command("Back", Command.BACK, 1));

		// Set the command listener for the textbox to the current midlet
		form.setCommandListener(new CommandListener() {
			public void commandAction(Command command, Displayable screen) {
				// Go back to caller screen
				Display.getDisplay(midlet).setCurrent(prevScreen);
			}
		});

		// Append all log messages
		synchronized (logMsgs) {
			int idx = logIndex;
			String nextMsg = null;
			for (int i = 0; i < LOG_SZ; i++) {
				nextMsg = logMsgs[idx++];
				if (nextMsg != null) {
					form.append("\n" + i + "=====\n" + nextMsg);
				}
				if (idx == LOG_SZ) {
					idx = 0;
				}
			}
		}

		// Display log messages
		Display.getDisplay(midlet).setCurrent(form);
	}
}
