package org.walkandplay.client.phone;

import org.geotracing.client.Util;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

public class VersionDisplay extends DefaultDisplay {
    private String serverURL;

    private Command GET_CMD = new Command("Get new version", Command.SCREEN, 1);

    public VersionDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "Version check");
    }

    public void start(Displayable aPrevScreen) {
        prevScreen = aPrevScreen;
        String myVersion = midlet.getAppProperty("MIDlet-Version");

        //#style labelinfo
        append("Current version: " + myVersion);

        String myName = midlet.getAppProperty("MIDlet-Name");
        serverURL = "http://" + midlet.getKWServer();
        String versionURL = serverURL + "/dist/version.html";
        String theirVersion = null;
        try {
            theirVersion = Util.getPage(versionURL);
            if (theirVersion != null) {
                if (!theirVersion.trim().equals(myVersion)) {
                    //#style alertinfo
                    append("Your " + myName + " version (" + myVersion + ") differs from the version (" + theirVersion + ") available for download. \nYou may want to upgrade to " + theirVersion);
                    addCommand(GET_CMD);
                } else {
                    //#style alertinfo
                    append("Your have the latest version");
                }
            }
        } catch (Throwable t) {
            //#style alertinfo
            append("Error fetching version from " + versionURL);
        }

        Log.log("versionCheck mine=" + myVersion + " theirs=" + theirVersion);
        Display.getDisplay(midlet).setCurrent(this);
    }

    public void commandAction(Command command, Displayable screen) {
        if (command == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (command == GET_CMD) {
            try {
                midlet.platformRequest(serverURL);
                // and exit
                try {
                    midlet.destroyApp(true);
                    midlet.notifyDestroyed();
                } catch (Throwable t) {
                    //
                }
            } catch (Throwable t) {
                //#style alertinfo
                append("Could not get new version:" + t.getMessage());
            }
        }
    }

}
