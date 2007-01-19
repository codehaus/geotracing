package org.walkandplay.client.phone;


import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
public class TraceScreen extends Form implements CommandListener {
    private TextField userField;
    private Command okCmd = new Command("OK", Command.OK, 1);
    private Command cancelCmd = new Command("Cancel", Command.CANCEL, 1);
    private MIDlet midlet;
    private Displayable prevScreen;

    public TraceScreen(MIDlet aMIDlet) {
        super("TraceScreen");
        midlet = aMIDlet;
        prevScreen = Display.getDisplay(midlet).getCurrent();

        addCommand(okCmd);
        addCommand(cancelCmd);
        setCommandListener(this);

        userField = new TextField("Inputfield", "Ronald Lenz", 16, TextField.ANY);
        append(userField);

        // Set our Form as  current display of the midlet
        Display.getDisplay(midlet).setCurrent(this);
    }

    /*
       * The commandAction method is implemented by this midlet to
       * satisfy the CommandListener interface and handle the Cancel action.
       */
    public void commandAction(Command command, Displayable screen) {
        if (command == okCmd) {

        }

        // Set the current display of the midlet to the textBox screen
        Display.getDisplay(midlet).setCurrent(prevScreen);
    }

}

