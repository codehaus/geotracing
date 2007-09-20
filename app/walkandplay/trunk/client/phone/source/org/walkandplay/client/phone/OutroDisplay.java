package org.walkandplay.client.phone;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

public class OutroDisplay extends DefaultDisplay {

    private Command FINISH_CMD = new Command("Finish", Command.SCREEN, 1);
    
    public OutroDisplay(WPMidlet aMIDlet, int theMaxScore) {
        super(aMIDlet, "Outro");

        //#style labelinfo
        append("You completed all your tasks! Congratulations!");
        //#style alertinfo
        append("You scored " + theMaxScore + " points");

        //#style formbox
        append(midlet.getPlayApp().getGame().getChildText("outro"));

        addCommand(FINISH_CMD);
        removeCommand(BACK_CMD);
        Display.getDisplay(midlet).setCurrent(this);
    }

    public void commandAction(Command command, Displayable screen) {
        //midlet.setHome();
        midlet.getPlayApp().finishGame();
    }

}
