package org.walkandplay.client.phone;

import org.geotracing.client.Preferences;
import org.geotracing.client.Net;
import org.geotracing.client.Util;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStoreException;

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
	protected MIDlet midlet;
	protected Displayable prevScreen;
    protected int logoNum;

    public DefaultDisplay(MIDlet aMIDlet, String aDisplayTitle) {
        //#style defaultscreen
        super(aDisplayTitle);
		midlet = aMIDlet;
		prevScreen = Display.getDisplay(midlet).getCurrent();
        System.out.println("prevscreen: " + prevScreen);
        if(prevScreen!=null){
            System.out.println(prevScreen.getTitle());
        }

        try{
            Image logo;
            //#ifdef polish.images.directLoad
            logo = Image.createImage("/gt_logo.png");
            //#else
            logo = scheduleImage("/gt_logo.png");
            //#endif

            //#style logo
            ImageItem logoItem = new ImageItem("", logo, ImageItem.LAYOUT_DEFAULT, "logo");
            logoNum = append(logoItem);
        }catch(Throwable t){
            Log.log("Exception getting logo:" + t.toString());    
        }

        addCommand(BACK_CMD);
		setCommandListener(this);
		Display.getDisplay(midlet).setCurrent(this);
	}

	public void commandAction(Command command, Displayable screen) {

	}
}
