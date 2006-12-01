package org.walkandplay.client.phone;


import org.geotracing.client.Log;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;


/**
 * Sends GPS data and ratings to server.
 *
 * @author Just van den Broecke
 * @version $Id: WP.java 8 2006-08-28 15:36:01Z just $
 */
public class WP extends MIDlet implements Runnable {
    private boolean GPS_CONNECTED;
    private boolean NET_CONNECTED;

    private static int CURRENT_CANVAS = -1;
    public final static int HOME_CANVAS = 0;
    public final static int TRACE_CANVAS = 1;
    public final static int FIND_TOURS_CANVAS = 2;
    public final static int PLAY_TOURS_CANVAS = 3;
    public final static int GPS_CANVAS = 4;
    public final static int SETTINGS_CANVAS = 5;
    public final static int HELP_CANVAS = 6;
    public final static int MEDIA_CANVAS = 7;
    public final static int ASSIGNMENT_CANVAS = 8;

    public static TraceCanvas traceCanvas = null;

    /**
     * Sets up the midlet and the items required for user interaction.
     */
    protected void startApp() {
        new Thread(this).start();
    }

    /**
     * Must be defined but no implementation required as the midlet only responds
     * to user interaction.
     */
    protected void pauseApp() {
    }

    /**
     * Must be defined but no implementation required as all the resources
     * are released when the thread terminates.
     */
    protected void destroyApp(boolean b) {

    }

    public boolean GPS_OK() {
        return GPS_CONNECTED;
    }

    public boolean NET_OK() {
        return NET_CONNECTED;
    }

    public void setGPSConnectionStat(boolean aValue) {
        GPS_CONNECTED = aValue;
    }

    public void setNetConnectionStat(boolean aValue) {
        NET_CONNECTED = aValue;
    }

    public void setScreen(int aScreenName) {
        switch (aScreenName) {
            case -1:
                log("going to the splashscreen");
                Display.getDisplay(this).setCurrent(new SplashCanvas(this, -1));
            case HOME_CANVAS:
                CURRENT_CANVAS = HOME_CANVAS;
                Display.getDisplay(this).setCurrent(new HomeCanvas(this));
                break;
            case TRACE_CANVAS:
                CURRENT_CANVAS = TRACE_CANVAS;
				if (traceCanvas == null) {
					traceCanvas = new TraceCanvas(this);
				}
				   Display.getDisplay(this).setCurrent(traceCanvas);
				traceCanvas.start();
 				break;
            case FIND_TOURS_CANVAS:
                CURRENT_CANVAS = FIND_TOURS_CANVAS;
                Display.getDisplay(this).setCurrent(new FindToursCanvas(this));
                break;
            case PLAY_TOURS_CANVAS:
                CURRENT_CANVAS = PLAY_TOURS_CANVAS;
                Display.getDisplay(this).setCurrent(new PlayToursCanvas(this));
                break;
            case GPS_CANVAS:
                CURRENT_CANVAS = GPS_CANVAS;
                Display.getDisplay(this).setCurrent(new GPSCanvas(this));
                break;
            case SETTINGS_CANVAS:
                CURRENT_CANVAS = SETTINGS_CANVAS;
                Display.getDisplay(this).setCurrent(new SettingsCanvas(this));
                break;
            case HELP_CANVAS:
                CURRENT_CANVAS = HELP_CANVAS;
                Display.getDisplay(this).setCurrent(new HelpCanvas(this));
                break;
            case MEDIA_CANVAS:
                CURRENT_CANVAS = MEDIA_CANVAS;
                Display.getDisplay(this).setCurrent(new MediaCanvas(this));
                break;
            case ASSIGNMENT_CANVAS:
                CURRENT_CANVAS = ASSIGNMENT_CANVAS;
                Display.getDisplay(this).setCurrent(new AssignmentCanvas(this));
                break;
        }
    }

    /**
     * Starts GPS fetching and KW client.
     */
    public void run() {
        Display.getDisplay(this).setCurrent(new SplashCanvas(this, HOME_CANVAS));
    }

    public void log(String aMsg) {
        System.out.println(aMsg);
        Log.log(aMsg);
    }


}
