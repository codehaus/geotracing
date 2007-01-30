package org.walkandplay.client.phone;

import org.geotracing.client.Preferences;
import org.geotracing.client.Net;
import org.geotracing.client.Util;
import de.enough.polish.ui.TextField;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStoreException;

/**
 * Allows changing account preferences.
 *
 * Account preferences (user,password,server URL) can be configured in
 * JAD/JAR file, but may be changed and stored in RMS. The values in the RMS
 * will always prevail.
 *
 * In this Form a user can change account settings and store these in RMS.
 * Currently it is required to restart the Midlet after such change.
 *
 * @author  Just van den Broecke
 * @version $Id: AccountScreen.java 128 2006-10-30 16:17:38Z just $
 */
public class AccountDisplay extends DefaultDisplay {
	private TextField urlField;
	private TextField userField;
	private TextField passwordField;
	private Command OK_CMD = new Command("OK", Command.OK, 1);
	private static Preferences preferences;

	public AccountDisplay(MIDlet aMIDlet) {
        super(aMIDlet, "AccountScreen");

		addCommand(OK_CMD);

		String user = getPreferences().get(Net.PROP_USER, midlet.getAppProperty(Net.PROP_USER));

		String password = getPreferences().get(Net.PROP_PASSWORD, midlet.getAppProperty(Net.PROP_PASSWORD));

		String url = getPreferences().get(Net.PROP_URL, midlet.getAppProperty(Net.PROP_URL));

		// Create input fields for user/password/url
        //#style formbox
        userField = new TextField("User", user, 16, TextField.ANY);
        //#style textbox
        passwordField = new TextField("Password", password, 16, TextField.ANY);
        //#style textbox
        urlField = new TextField("Server", url, 512, TextField.ANY);
		append(userField);
		append(passwordField);
		append(urlField);

		// OK/cancel midlet menu
		addCommand(OK_CMD);

		// Set the command listener for callback
		setCommandListener(this);

		// Set our Form as  current display of the midlet
		Display.getDisplay(midlet).setCurrent(this);
	}

	/*
	   * The commandAction method is implemented by this midlet to
	   * satisfy the CommandListener interface and handle the Cancel action.
	   */
	public void commandAction(Command command, Displayable screen) {
		if (command == OK_CMD) {
			String user = userField.getString();
			String password = passwordField.getString();
			String url = urlField.getString();

			try {
				if (user != null && user.length() > 0) {
					getPreferences().put(Net.PROP_USER, user);
					getPreferences().save();
				}

				if (password != null && password.length() > 0) {
					getPreferences().put(Net.PROP_PASSWORD, password);
					getPreferences().save();
				}

				if (url != null && url.length() > 0) {
					getPreferences().put(Net.PROP_URL, url);
					getPreferences().save();
				}
				Util.showAlert(midlet, "OK", "Account settings saved, please Exit and restart");
			} catch (Throwable t) {
				Util.showAlert(midlet, "Error", "Error saving account preference");
			}
		}

		// Set the current display of the midlet to the textBox screen
		Display.getDisplay(midlet).setCurrent(prevScreen);
	}

	public static Preferences getPreferences() {
		try {
			if (preferences == null) {
				preferences = new Preferences(Net.RMS_STORE_NAME);
			}
			return preferences;
		} catch (RecordStoreException e) {
			return null;
		}
	}
}
