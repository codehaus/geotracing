package org.walkandplay.client.phone;

import de.enough.polish.ui.List;
import de.enough.polish.ui.StringItem;
import de.enough.polish.util.Locale;
import org.geotracing.client.Util;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */

/*public class SettingsDisplay extends Form implements CommandListener {*/
public class SettingsDisplay extends DefaultDisplay {
    List menuScreen;
    Command SOUND_CMD;
    Command ACCOUNT_CMD = new Command(Locale.get("settings.Account"), Command.ITEM, 2);

    //StringItem label = new StringItem("", "Settings");
    StringItem text = new StringItem("", "Choose settings from the menu to change");
    private Image logo;

    public SettingsDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "");

        try {
            //#ifdef polish.images.directLoad
            logo = Image.createImage("/settings_icon_small.png");
            //#else
            logo = scheduleImage("/settings_icon_small.png");
            //#endif
        } catch (Throwable t) {
            Log.log("Could not load the images on SettingsDisplay");
        }
        append(logo);

        if (Util.hasSound()) {
            SOUND_CMD = new Command(Locale.get("settings.SoundOff"), Command.ITEM, 2);
        } else {
            SOUND_CMD = new Command(Locale.get("settings.SoundOn"), Command.ITEM, 2);
        }

        /*//#style titlebox
        append(label);*/
        //#style formbox
        append(text);

        /*DigitalClockItem clock = new DigitalClockItem("");
        append(clock);*/
        addCommand(SOUND_CMD);
        addCommand(ACCOUNT_CMD);

    }

    /*
       * The commandAction method is implemented by this midlet to
       * satisfy the CommandListener interface and handle the Cancel action.
       */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == SOUND_CMD) {
            Util.toggleSound();
        } else if (cmd == ACCOUNT_CMD) {
            Display.getDisplay(midlet).setCurrent(new AccountDisplay(midlet));
        }
    }


}
