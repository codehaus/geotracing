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
    private Command help1Cmd = new Command("Lost your GPS connection?", Command.ITEM, 2);
    private Command help2Cmd = new Command("Application 'hangs'?", Command.ITEM, 2);
    private Command help3Cmd = new Command("Login problems?", Command.ITEM, 2);
    private Command help4Cmd = new Command("Where is my application?", Command.ITEM, 2);
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
        
        //#style labelinfo
        append(logo);

        //#style formbox
        append(text);

        addCommand(help1Cmd);
        addCommand(help2Cmd);
        addCommand(help3Cmd);
        addCommand(help4Cmd);
    }

    /*
       * The commandAction method is implemented by this midlet to
       * satisfy the CommandListener interface and handle the Cancel action.
       */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == help1Cmd) {
            text.setLabel(help1Cmd.getLabel());
            //#style formbox
            text.setText("This can happen for several reasons. \n\n1)First the obvious make sure it's turned on and " +
                    "within bluetooth range (5-7 mtrs). \n\n2)If you have never used a GPS with this phone before then " +
                    "go to the GPS section and connect to the device first. After a reset the GPS is automatically " +
                    "found and connected. \n\n3)Sometimes the GPS-device can hang - just reset it.");
        } else if (cmd == help2Cmd) {
            text.setLabel(help2Cmd.getLabel());
            //#style formbox
            text.setText("Yes sometimes software hangs. Sometimes you can press the red 'cancel call' button to close the " +
                    "application. If that doesn't work just reboot your phone and simply start over again.");
        } else if (cmd == help3Cmd) {
            text.setLabel(help3Cmd.getLabel());
            //#style formbox
            text.setText("Make sure that your username and password correspond with those you can find under Settings/Account.");
        } else if (cmd == help4Cmd) {
            text.setLabel(help4Cmd.getLabel());
            //#style formbox
            text.setText("Sometimes you think your application has disappeared all of a sudden. This can happen because another " +
                    "application like a mediaplayer has started up and now has the focus. Or a call or sms came in between." +
                    "Most of the time when you close the 'upper' app this application appears again. Most Nokia's will " +
                    "also let you switch applications when holding the down the menu-button for a few secs.");
        }
    }


}
