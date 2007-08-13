package org.walkandplay.client.phone;

import de.enough.polish.ui.TextField;
import org.geotracing.client.Net;
import org.geotracing.client.Preferences;
import org.geotracing.client.Util;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.rms.RecordStoreException;

public class AccountDisplay extends DefaultDisplay {
    private TextField urlField;
    private TextField userField;
    private TextField passwordField;
    private Command OK_CMD = new Command("OK", Command.OK, 1);
    private static Preferences preferences;

    public AccountDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "Account");

        String user = getPreferences().get(WPMidlet.KW_URL, midlet.getKWUser());
        String password = getPreferences().get(WPMidlet.KW_PASSWORD, midlet.getKWPassword());
        String url = getPreferences().get(WPMidlet.KW_URL, midlet.getKWUrl());

        userField = new TextField("", user, 16, TextField.ANY);
        passwordField = new TextField("", password, 16, TextField.PASSWORD);
        urlField = new TextField("", url, 512, TextField.ANY);

        //#style labelinfo
        append("user");
        //#style textbox
        append(userField);
        //#style labelinfo
        append("password");
        //#style textbox
        append(passwordField);
        //#style labelinfo
        append("server");
        //#style textbox
        append(urlField);

        addCommand(OK_CMD);
        setCommandListener(this);
        Display.getDisplay(midlet).setCurrent(this);
    }

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
