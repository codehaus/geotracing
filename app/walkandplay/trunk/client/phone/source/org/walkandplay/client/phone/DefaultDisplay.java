package org.walkandplay.client.phone;

import de.enough.polish.ui.Form;

import javax.microedition.lcdui.*;

public class DefaultDisplay extends Form implements CommandListener {
    protected Command BACK_CMD = new Command("Back", Command.BACK, 1);
    protected WPMidlet midlet;
    protected Displayable prevScreen;

    public DefaultDisplay(WPMidlet aMIDlet, String aDisplayTitle) {
        //#style defaultscreen
        super(aDisplayTitle);
        midlet = aMIDlet;
        prevScreen = Display.getDisplay(midlet).getCurrent();

        addCommand(BACK_CMD);
        setCommandListener(this);
        Display.getDisplay(midlet).setCurrent(this);
    }

    public void commandAction(Command command, Displayable screen) {

    }
}
