package org.walkandplay.client.phone;

import javax.microedition.lcdui.*;

import org.walkandplay.client.phone.Log;
import de.enough.polish.util.Locale;


/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
public class HelpDisplay extends DefaultDisplay {
    private Command HELP1_CMD = new Command(Locale.get("help.Topic1"), Command.ITEM, 2);
    private Command HELP2_CMD = new Command(Locale.get("help.Topic2"), Command.ITEM, 2);
    private Command HELP3_CMD = new Command(Locale.get("help.Topic3"), Command.ITEM, 2);
    private Command HELP4_CMD = new Command(Locale.get("help.Topic4"), Command.ITEM, 2);
    private Image logo;

    StringItem title = new StringItem("", "Welcome to the help section");
    StringItem text = new StringItem("", "Select a help item from the menu");

    public HelpDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "Help");

        try {
            //#ifdef polish.images.directLoad
            logo = Image.createImage("/help_icon_small.png");
            //#else
            logo = scheduleImage("/help_icon_small.png");
            //#endif
        } catch (Throwable t) {
            Log.log("Could not load the images on HelpDisplay");
        }

        append(logo);

        //#style labelinfo
        append(title);

        //#style formbox
        append(text);

        addCommand(HELP1_CMD);
        addCommand(HELP2_CMD);
        addCommand(HELP3_CMD);
        addCommand(HELP4_CMD);
    }

    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == HELP1_CMD) {
            title.setText(HELP1_CMD.getLabel());
            //#style formbox
            text.setText(Locale.get("help.Topic1Text"));
        } else if (cmd == HELP2_CMD) {
            title.setText(HELP2_CMD.getLabel());
            //#style formbox
            text.setText(Locale.get("help.Topic2Text"));
        } else if (cmd == HELP3_CMD) {
            title.setText(HELP3_CMD.getLabel());
            //#style formbox
            text.setText(Locale.get("help.Topic3Text"));
        } else if (cmd == HELP4_CMD) {
            title.setText(HELP4_CMD.getLabel());
            //#style formbox
            text.setText(Locale.get("help.Topic4Text"));
        }
    }
}
