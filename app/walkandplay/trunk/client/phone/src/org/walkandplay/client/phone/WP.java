package org.walkandplay.client.phone;


import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;


/**
 *  Sends GPS data and ratings to server.
 *
 * @author  Just van den Broecke
 * @version $Id: WP.java 8 2006-08-28 15:36:01Z just $
 */
public class WP extends MIDlet implements Runnable {
    private boolean GPS_CONNECTED;

    private static int CURRENT_CANVAS = -1;
    public final static int HOME_CANVAS = 0;
    public final static int GPS_CANVAS = 1;
    public final static int MAP_CANVAS = 2;
    public final static int TRACE_CANVAS = 3;

    private SplashCanvas splashCanvas;
    private HomeCanvas homeCanvas;
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
        return GPS_CONNECTED;
    }
    
    public void setGPSConnectionStat(boolean aValue){
        GPS_CONNECTED = aValue;
    }

    public void setScreen(int aScreenName){
        switch(aScreenName){
            case -1:
               Display.getDisplay(this).setCurrent(new SplashCanvas(this, -1));
            case HOME_CANVAS:
                CURRENT_CANVAS = HOME_CANVAS;
                Display.getDisplay(this).setCurrent(new HomeCanvas(this));
                break;
            case MAP_CANVAS:
                CURRENT_CANVAS = MAP_CANVAS;
                log("creating a new mapcanvas");
                Display.getDisplay(this).setCurrent(new MapCanvas(this));
                break;
            case GPS_CANVAS:
                CURRENT_CANVAS = GPS_CANVAS;
                Display.getDisplay(this).setCurrent(new GPSCanvas(this));
                break;            
        }
    }

    /**
	 * Starts GPS fetching and KW client.
	 */
	public void run() {
        splashCanvas = new SplashCanvas(this, HOME_CANVAS);
        //TraceScreen traceScreen = new TraceScreen(this);
		logScreen = new LogScreen();
		Display.getDisplay(this).setCurrent(splashCanvas);
		//traceScreen.start();
	}

    public void log(String aMsg) {
        System.out.println(aMsg);
        //logScreen.add(aMsg);        
    }


}
