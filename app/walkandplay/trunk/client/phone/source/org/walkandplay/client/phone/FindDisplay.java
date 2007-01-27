package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import de.enough.polish.ui.StringItem;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
public class FindDisplay extends DefaultDisplay {
    Command SEARCH_CMD = new Command(Locale.get("find.Search"), Command.ITEM, 2);
    StringItem text = new StringItem("", "Press find from the menu to get tours close to you");

    public FindDisplay(MIDlet aMIDlet) {
        super(aMIDlet, "");

        //#style formbox
        append(text);
        addCommand(SEARCH_CMD);
    }

    /*
       * The commandAction method is implemented by this midlet to
       * satisfy the CommandListener interface and handle the Cancel action.
       */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == SEARCH_CMD) {
            
        } 
    }


}
