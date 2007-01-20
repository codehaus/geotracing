package org.walkandplay.client.phone;


import org.geotracing.client.GPSInfo;
import org.geotracing.client.MFloat;
import org.geotracing.client.Util;
import org.geotracing.client.Log;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

import nl.justobjects.mjox.JXElement;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
public class TraceScreen extends Form implements CommandListener {
    private Command okCmd = new Command("OK", Command.OK, 1);
    private Command cancelCmd = new Command("Cancel", Command.CANCEL, 1);
    private MIDlet midlet;
    private Displayable prevScreen;

    private Tracer tracer;
    private String tileBaseURL;
	private JXElement tileInfo;
	private Image tileImage;
	private int zoom = 12;
	private MFloat tileScale;
	private String mapType = "map";
	private String lon = "0", lat = "0";
	private String msg = "";
	private String[] statMsgs = new String[3];
	private String gpsStatus = "disconnected";
	private String netStatus = "disconnected";
	private String status = "OK";
    int tileWidth, tileHeight;

    public TraceScreen(MIDlet aMIDlet) {
        super("TraceScreen");
        midlet = aMIDlet;
        prevScreen = Display.getDisplay(midlet).getCurrent();

        addCommand(okCmd);
        addCommand(cancelCmd);
        setCommandListener(this);

        // Set our Form as  current display of the midlet
        Display.getDisplay(midlet).setCurrent(this);
    }

    void start() {
        if (tracer == null) {
            tracer = new Tracer(midlet, this);
            tracer.start();
        }
    }

    void stop() {
        tracer.stop();
        tracer = null;
    }

    Tracer getTracer() {
        return tracer;
    }

    void fetchTileInfo() {
        if (lon.equals(("0")) || lon.equals("0")) {
            return;
        }
        try {

            // Get information on tile
            String tileInfoURL = tileBaseURL + "lon=" + lon + "&lat=" + lat + "&zoom=" + zoom + "&type=" + mapType + "&format=xml";
            JXElement newTileInfo = Util.getXML(tileInfoURL);

            // Reset tileImage if first tile info or if keyhole ref changed (we moved to new tile).
            if (tileInfo == null || !tileInfo.getAttr("khref").equals(newTileInfo.getAttr("khref"))) {
                tileImage = null;
            }
            tileInfo = newTileInfo;
            // System.out.println("khref=" + tileInfo.getAttr("khref"));
        } catch (Throwable t) {
            Log.log("error: TraceCanvas: t=" + t + " m=" + t.getMessage());
        }
    }

    void setLocation(String aLon, String aLat) {
        if (aLon.equals(("0")) || aLat.equals("0")) {
            statMsgs[2] = "No Location";
            return;
        }
        lon = aLon;
        lat = aLat;
        fetchTileInfo();
    }

    public void setGPSInfo(GPSInfo theInfo) {
        setLocation(theInfo.lon.toString(), theInfo.lat.toString());
        status = theInfo.toString();
    }

    public void setStatus(String s) {
        status = s;
        Log.log(s);
    }

    public void onGPSStatus(String s) {
        gpsStatus = s;
        Log.log(s);
    }


    public void onNetStatus(String s) {
        netStatus = s;
        Log.log(s);        
    }

    public boolean hasLocation() {
        return !lon.equals(("0")) && !lat.equals("0");
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

