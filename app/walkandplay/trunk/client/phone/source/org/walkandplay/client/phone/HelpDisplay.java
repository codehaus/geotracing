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
    private List menuScreen;
    private Command help1Cmd = new Command(Locale.get("help.Topic1"), Command.ITEM, 2);
    private Command help2Cmd = new Command(Locale.get("help.Topic2"), Command.ITEM, 2);
    private Command help3Cmd = new Command(Locale.get("help.Topic3"), Command.ITEM, 2);
    private Image logo;

    //StringItem label = new StringItem("", "Help");
    StringItem text = new StringItem("", "Welcome to the help section");

    public HelpDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "");

        try{
            //#ifdef polish.images.directLoad
            logo = Image.createImage("/help_icon_small.png");            
            //#else
            logo = scheduleImage("/help_icon_small.png");
            //#endif
        }catch(Throwable t){
            Log.log("Could not load the images on HelpDisplay");
        }
        
        //#style smallstring
        append(logo);

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
            //label.setText(Locale.get("help.Topic1"));
            text.setText(Locale.get("help.Topic1Text"));
        } else if (cmd == help2Cmd) {
            //label.setText(Locale.get("help.Topic2"));
            text.setText(Locale.get("help.Topic2Text"));
        } else if (cmd == help3Cmd) {
            //label.setText(Locale.get("help.Topic3"));
            text.setText(Locale.get("help.Topic3Text"));
        }
    }


}
