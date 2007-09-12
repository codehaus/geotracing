package org.walkandplay.client.phone;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

public class IntroDisplay extends DefaultDisplay {

    private boolean active;

    public IntroDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "Intro");

        //#style formbox
        append(midlet.getPlayApp().getGame().getChildText("intro"));
    }

    public void start(Displayable aPrevScreen){
        active = true;
        prevScreen = aPrevScreen;
        Display.getDisplay(midlet).setCurrent(this);
    }

    public boolean isActive(){
        return active;
    }

    public void commandAction(Command command, Displayable screen) {
        if (command == BACK_CMD) {
            active = false;
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }

}
