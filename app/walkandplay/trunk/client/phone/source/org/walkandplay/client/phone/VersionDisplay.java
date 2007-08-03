package org.walkandplay.client.phone;

import org.geotracing.client.Net;
import org.geotracing.client.Util;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

public class VersionDisplay extends DefaultDisplay {

    private Command FETCH_CMD = new Command("Fetch new version", Command.SCREEN, 1);

    public VersionDisplay(WPMidlet aMIDlet, Displayable aPrevScreen) {
        super(aMIDlet, "Version check");
        prevScreen = aPrevScreen;


        String myVersion = midlet.getAppProperty("MIDlet-Version");

        //#style labelinfo
        append("Current version: " + myVersion);

        String myName = midlet.getAppProperty("MIDlet-Name");
        String versionURL = midlet.getKWUrl() + "/ota/version.html";
        String theirVersion = null;
        try {
            theirVersion = Util.getPage(versionURL);
            if (theirVersion != null && !theirVersion.trim().equals(myVersion)) {
                //#style alertinfo
                append("Your " + myName + " version (" + myVersion + ") differs from the version (" + theirVersion + ") available for download. \nYou may want to upgrade to " + theirVersion);
            }
        } catch (Throwable t) {
            //#style alertinfo
            append("Error fetching version from " + versionURL);
        }

        Log.log("versionCheck mine=" + myVersion + " theirs=" + theirVersion);
    }

    /*
     * The commandAction method is implemented by this midlet to
     * satisfy the CommandListener interface and handle the Exit action.
     */
    public void commandAction(Command command, Displayable screen) {
        if (command == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (command == FETCH_CMD) {
            try {
                midlet.platformRequest(Net.getInstance().getURL() + "/ota/version.html");
            } catch (Throwable t) {
                //#style alertinfo
                append("Could not get new version:" + t.getMessage());
            }
        }
    }

}
