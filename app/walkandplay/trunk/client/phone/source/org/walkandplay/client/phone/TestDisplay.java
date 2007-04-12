package org.walkandplay.client.phone;

import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.TextEffect;
import de.enough.polish.ui.FramedForm;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Display;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
public class TestDisplay extends DefaultDisplay {
    Image logo;
    private int w = -1, h = -1;
	private Font f, fb;
    public TestDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "");
        try{

            //#ifdef polish.images.directLoad
            logo = Image.createImage("/gt_logo.png");
            //#else
            logo = scheduleImage("/gt_logo.png");
            //#endif
            
        }catch(Throwable t){
            Log.log("Exception getting logo:" + t.toString());
        }
    }

    

    public void paint(Graphics g) {
        if (f == null) {
			w = getWidth();
			h = getHeight();
			if (w == 0 || h == 0) {
				w = 176;
				h = 208;
			}
			f = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
			fb = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
		}


        // Clear screen
        g.setFont(f);
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, w, h);

        g.drawImage(logo, 3, 3, Graphics.TOP | Graphics.LEFT);
        /*Form f = new Form("");
        f.append(new StringItem("label", "text"));
        Display.getDisplay(midlet).setCurrent(f);
        f.setCommandListener(this);*/
        /*StringItem si = new StringItem("label", "text");
        append(si);*/

        //#style formbox
        FramedForm ff = new FramedForm("");
        ff.addCommand(BACK_CMD);
        StringItem si = new StringItem("label", "text");
        ff.append(si);        
        Display.getDisplay(midlet).setCurrent(ff);
        //TextEffect te = new TextEffect();

    }



}
