package org.walkandplay.client.external;

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

/**
 * Form to show the realtime video got from the camera,
 * in order to let user decide when to take a snapshot.
 *
 * @author Song Yuan
 */
public class CameraForm extends Form implements CommandListener {

    private Command captureCommand;
    private Command backCommand;

    /**
     * Construct a CameraForm.
     *
     * @param name title of this form
     */
    public CameraForm(String name) {

//set the title of this form
        super(name);

//Configure the CommandListener
        captureCommand = new Command("Capture", Command.ITEM, 1);
        backCommand = new Command("Back", Command.BACK, 0);
        setCommandListener(this);
        addCommand(captureCommand);
        addCommand(backCommand);

    }

    /**
     * Implementation of commandAction interface.
     */
    public void commandAction(Command cmd, Displayable disp) {

//in case that "Back" button is pressed
        if (cmd.getCommandType() == Command.BACK) {
            CameraHandler.isFinished = true;
        }

//in case that "Capture" button is pressed
        else if (cmd == captureCommand) {
            CameraHandler.capturePhoto();
        }
    }

}
