package org.walkandplay.client.phone;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;

/**
 * Very basic rotating log.
 *
 * @author  Just van den Broecke
 * @version $Id: Log.java 22 2006-08-31 10:48:03Z just $
 */
public class Log {

	private static final int LOG_SZ = 16;
	private static String[] logMsgs = new String[LOG_SZ];
	private static int logIndex = 0;

	private Log() {
	}

	public static void log(String aMsg) {
        System.out.println(aMsg);
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
        //#style defaultscreen
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
                    //#style formbox
                    form.append(new StringItem("","\n" + i + "=====\n" + nextMsg));
                    /*form.append("\n" + i + "=====\n" + nextMsg);*/
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
