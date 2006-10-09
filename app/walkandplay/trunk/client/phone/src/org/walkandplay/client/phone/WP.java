package org.walkandplay.client.phone;


import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;


/**
 *  Sends GPS data and ratings to server.
 *
 * @author  Just van den Broecke
 * @version $Id: WP.java 8 2006-08-28 15:36:01Z just $
 */
public class WP extends MIDlet implements Runnable {
    private boolean GPS_OK;

    private static int CURRENT_SCREEN = -1;
    public final static int HOME_SCREEN = 0;
    public final static int GPS_SCREEN = 2;
    public final static int MAP_SCREEN = 3;
    public final static int TRACE_SCREEN = 4;

    private SplashScreen splashScreen;
    private HomeScreen homeScreen;
    private LogScreen logScreen;
    private TraceScreen traceScreen;

    /**
	 *  Sets up the midlet and the items required for user interaction.
	 */
	protected void startApp() {
		new Thread(this).start();
	}

	/**
	 *  Must be defined but no implementation required as the midlet only responds
	 *  to user interaction.
	 */
	protected void pauseApp() {
	}

	/**
	 *  Must be defined but no implementation required as all the resources
	 *  are released when the thread terminates.
	 */
	protected void destroyApp(boolean b) {

	}

    public boolean GPS_OK(){
        return GPS_OK;
    }
    
    public void setGPSStat(boolean aValue){
        GPS_OK = aValue;
    }

    public void setScreen(int aScreenName){
        switch(aScreenName){
            case -1:
               Display.getDisplay(this).setCurrent(new SplashScreen(this, -1));
            case HOME_SCREEN:
                CURRENT_SCREEN = HOME_SCREEN;
                Display.getDisplay(this).setCurrent(new HomeScreen(this));
                break;
            case MAP_SCREEN:
                CURRENT_SCREEN = MAP_SCREEN;
                Display.getDisplay(this).setCurrent(new MapScreen(this));
                break;
            case GPS_SCREEN:
                CURRENT_SCREEN = GPS_SCREEN;
                Display.getDisplay(this).setCurrent(new GPSScreen(this));
                break;
            case TRACE_SCREEN:
                CURRENT_SCREEN = TRACE_SCREEN;
                TraceScreen traceScreen = new TraceScreen(this);
                traceScreen.start();
                Display.getDisplay(this).setCurrent(traceScreen);
                break;
        }
    }

    /**
	 * Starts GPS fetching and KW client.
	 */
	public void run() {
        splashScreen = new SplashScreen(this, HOME_SCREEN);
        //TraceScreen traceScreen = new TraceScreen(this);
		logScreen = new LogScreen();
		Display.getDisplay(this).setCurrent(splashScreen);
		//traceScreen.start();
	}

    public void log(String aMsg) {
        System.out.println(aMsg);
        //logScreen.add(aMsg);        
    }


}
