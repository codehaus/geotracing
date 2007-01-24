package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import de.enough.polish.ui.TabbedForm;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import java.io.IOException;


/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
//public class HomeScreen extends Form implements CommandListener {
public class HelpDisplay extends Form implements CommandListener {
    MIDlet midlet;
    private Displayable prevScreen;
    List menuScreen;
    Command help1Cmd = new Command(Locale.get("help.Topic1"), Command.ITEM, 2);
    Command help2Cmd = new Command(Locale.get("help.Topic2"), Command.ITEM, 2);
    Command help3Cmd = new Command(Locale.get("help.Topic3"), Command.ITEM, 2);
    Command backCmd = new Command("Back", Command.BACK, 1);

    StringItem label = new StringItem("", "Help");
    StringItem text = new StringItem("", "Welcome to the help section");

    public HelpDisplay(MIDlet aMIDlet) {
        //#style defaultscreen
        super("");
        midlet = aMIDlet;
        prevScreen = Display.getDisplay(midlet).getCurrent();

        try {
            Image logo;
            //#ifdef polish.images.directLoad
            logo = Image.createImage("/gt_logo.png");
            //#else
            logo = scheduleImage("/gt_logo.png");
            //#endif

            //#style logo
            ImageItem logoItem = new ImageItem("", logo, ImageItem.LAYOUT_DEFAULT, "logo");
            append(logoItem);
        } catch (IOException e) {
            e.printStackTrace();
        }

        append(label);
        append(text);

        addCommand(help1Cmd);
        addCommand(help2Cmd);
        addCommand(help3Cmd);

        addCommand(backCmd);
        setCommandListener(this);

        Display.getDisplay(midlet).setCurrent(this);
    }

    /*
       * The commandAction method is implemented by this midlet to
       * satisfy the CommandListener interface and handle the Cancel action.
       */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == backCmd) {
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
