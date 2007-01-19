package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

//#ifdef polish.debugEnabled
	import de.enough.polish.util.Debug;
//#endif

/**
 * MobiTracer main GUI.
 *
 * @author  Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
//public class HomeScreen extends Form implements CommandListener {
public class HomeScreen extends Form implements CommandListener {
    MIDlet midlet;
    List menuScreen;
	private Command okCmd = new Command("OK", Command.OK, 1);
	private Command cancelCmd = new Command("Cancel", Command.CANCEL, 1);
	//#ifdef polish.debugEnabled
		Command showLogCmd = new Command( Locale.get("cmd.ShowLog"), Command.ITEM, 9 );
	//#endif
	Display display;

    public HomeScreen(MIDlet aMIDlet) {
        super("HomeScreen");
        midlet = aMIDlet;
        //#ifdef title:defined
            //#= String title = "Walk&Play";
        //#else
            String title = "Walk&Play";
        //#endif
        
        //#style mainScreen
        menuScreen = new List(title, List.IMPLICIT);

        //#style mainTraceCommand
        menuScreen.append(Locale.get( "menu.Trace"), null);
        //#style mainFindCommand
        menuScreen.append(Locale.get( "menu.Find"), null);
        //#style mainPlayCommand
        menuScreen.append(Locale.get( "menu.Play"), null);
        //#style mainGPSCommand
        menuScreen.append(Locale.get( "menu.GPS"), null);
        //#style mainSettingsCommand
        menuScreen.append(Locale.get( "menu.Settings"), null);
        //#style mainHelpCommand
        menuScreen.append(Locale.get( "menu.Help"), null);
        //#style mainQuitCommand
        menuScreen.append(Locale.get( "menu.Quit"), null);

        menuScreen.setCommandListener(this);

        //#ifdef polish.debugEnabled
            menuScreen.addCommand( this.showLogCmd );
        //#endif


        //TextField userField = new TextField("User", "Ronald", 16, TextField.ANY);
        //append(userField);
        

        addCommand(okCmd);
		addCommand(cancelCmd);
		setCommandListener(this);

        Display.getDisplay(midlet).setCurrent(this);

    }

    

    private void goToScreen(int aScreenNr) {
		switch(aScreenNr){
            case 0:
                // Trace
                TraceScreen traceScreen = new TraceScreen(midlet);
		        Display.getDisplay(midlet).setCurrent(traceScreen);
                break;
            case 1:
                // Find
                break;
            case 2:
                // Play
                break;
            case 3:
                // GPS
                break;
            case 4:
                // Settings
                break;
            case 5:
                // Help
                break;
            case 6:
                // Quit
                quit();
                break;
        }
	}


    /*
       * The commandAction method is implemented by this midlet to
       * satisfy the CommandListener interface and handle the Cancel action.
       */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == List.SELECT_COMMAND) {
                goToScreen(this.menuScreen.getSelectedIndex());
        }

        /*// Set the current display of the midlet to the textBox screen
        Display.getDisplay(midlet).setCurrent(prevScreen);*/
    }

    private void quit() {
		midlet.notifyDestroyed();
	}

}
