package org.walkandplay.client.phone;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

public class OutroDisplay extends DefaultDisplay {

    private Command CONTINUE_CMD = new Command("Continue", Command.SCREEN, 1);

    public OutroDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "");
        midlet = aMIDlet;
        prevScreen = Display.getDisplay(midlet).getCurrent();

        //#style labelinfo
        append("Outro");

        //#style formbox
        append(midlet.getGame().getChildText("outro"));

        addCommand(CONTINUE_CMD);
        setCommandListener(this);
        Display.getDisplay(midlet).setCurrent(this);
    }

    /*
         * The commandAction method is implemented by this midlet to
         * satisfy the CommandListener interface and handle the Exit action.
         */
    public void commandAction(Command command, Displayable screen) {
        if (command == CONTINUE_CMD) {
            midlet.setHome();
        }
    }

}
