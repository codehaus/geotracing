package org.walkandplay.client.phone;

import de.enough.polish.ui.Form;
import de.enough.polish.ui.ImageItem;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Display;


/**
 * Allows changing account preferences.
 *
 * Account preferences (user,password,server URL) can be configured in
 * JAD/JAR file, but may be changed and stored in RMS. The values in the RMS
 * will always prevail.
 *
 * In this Form a user can change account settings and store these in RMS.
 * Currently it is required to restart the Midlet after such change.
 *
 * @author  Just van den Broecke
 * @version $Id: AccountScreen.java 128 2006-10-30 16:17:38Z just $
 */
public class DefaultDisplay extends Form implements CommandListener {
	protected Command BACK_CMD = new Command("Back", Command.BACK, 1);
	protected WPMidlet midlet;
	protected Displayable prevScreen;
    protected int logoNum;
    private Image logo;

    public DefaultDisplay(WPMidlet aMIDlet, String aDisplayTitle) {
        //#style defaultscreen
        super(aDisplayTitle);
        midlet = aMIDlet;
		prevScreen = Display.getDisplay(midlet).getCurrent();
        try{

            //#ifdef polish.images.directLoad
            logo = Image.createImage("/gt_logo.png");
            //#else
            logo = scheduleImage("/gt_logo.png");
            //#endif

            //#style logo
            ImageItem logoItem = new ImageItem("", logo, ImageItem.LAYOUT_DEFAULT, "logo");
            //logoNum = append(logoItem);
        }catch(Throwable t){
            Log.log("Exception getting logo:" + t.toString());    
        }

        addCommand(BACK_CMD);
		setCommandListener(this);
		Display.getDisplay(midlet).setCurrent(this);
	}

    /*public void paint(Graphics g) {
        g.drawImage(logo, 0, 0, Graphics.TOP | Graphics.LEFT);
    }*/

    public void commandAction(Command command, Displayable screen) {

	}
}
