package org.walkandplay.client.external;

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

/**
 * Form to display the taken photo to let the user confirm his action.
 *
 * @author Song Yuan
 */
public class PhotoForm extends Form implements CommandListener {

    private Command okCommand;
    private Command cancelCommand;

    /**
     * Construct a PhotoForm.
     *
     * @param name title of the form
     */
    public PhotoForm(String name) {

//set the title of the form
        super(name);

//configure the CommandListener
        okCommand = new Command("OK", Command.ITEM, 1);
        cancelCommand = new Command("Cancel", Command.BACK, 0);
        setCommandListener(this);
        addCommand(okCommand);
        addCommand(cancelCommand);
    }

    /**
     * Implementation of commandAction interface.
     */
    public void commandAction(Command cmd, Displayable disp) {

//in case that "Back" button is pressed
        if (cmd.getCommandType() == Command.BACK) {
            try {
                org.walkandplay.client.external.CameraHandler.showVideo();
                CameraHandler.setPhotoNull();
            } catch (Exception e) {
            }
        }

//in case that "OK" button is pressed
        else if (cmd == okCommand) {
            CameraHandler.isFinished = true;
        }
    }

}
