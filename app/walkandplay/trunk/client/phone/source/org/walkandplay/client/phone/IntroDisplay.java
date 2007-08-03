package org.walkandplay.client.phone;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

public class IntroDisplay extends DefaultDisplay {

    private Command CONTINUE_CMD = new Command("Continue", Command.SCREEN, 1);

    public IntroDisplay(WPMidlet aMIDlet, Displayable aPrevScreen) {
        super(aMIDlet, "Intro");
        prevScreen = aPrevScreen;
        
        //#style formbox
        append(midlet.getPlayApp().getGame().getChildText("intro"));

        addCommand(CONTINUE_CMD);        
    }

    /*
         * The commandAction method is implemented by this midlet to
         * satisfy the CommandListener interface and handle the Exit action.
         */
    public void commandAction(Command command, Displayable screen) {
        if (command == CONTINUE_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }

}
