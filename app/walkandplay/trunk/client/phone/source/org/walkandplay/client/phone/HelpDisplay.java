package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import de.enough.polish.ui.StringItem;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;


/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
public class HelpDisplay extends DefaultDisplay {
    List menuScreen;
    Command help1Cmd = new Command(Locale.get("help.Topic1"), Command.ITEM, 2);
    Command help2Cmd = new Command(Locale.get("help.Topic2"), Command.ITEM, 2);
    Command help3Cmd = new Command(Locale.get("help.Topic3"), Command.ITEM, 2);

    StringItem label = new StringItem("", "Help");
    StringItem text = new StringItem("", "Welcome to the help section");

    public HelpDisplay(MIDlet aMIDlet) {
        super(aMIDlet, "");

        //#style titlebox
        append(label);
        //#style formbox
        append(text);

        addCommand(help1Cmd);
        addCommand(help2Cmd);
        addCommand(help3Cmd);
    }

    /*
       * The commandAction method is implemented by this midlet to
       * satisfy the CommandListener interface and handle the Cancel action.
       */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == help1Cmd) {
            label.setText(Locale.get("help.Topic1"));
            text.setText(Locale.get("help.Topic1Text"));
        } else if (cmd == help2Cmd) {
            label.setText(Locale.get("help.Topic2"));
            text.setText(Locale.get("help.Topic2Text"));
        } else if (cmd == help3Cmd) {
            label.setText(Locale.get("help.Topic3"));
            text.setText(Locale.get("help.Topic3Text"));
        }
    }


}
