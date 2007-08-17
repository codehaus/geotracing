package org.walkandplay.client.phone.util;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.*;
import org.geotracing.client.Log;
import org.walkandplay.client.phone.WPMidlet;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Integrates GPS, Screen and Network interaction.
 *
 * @author Just van den Broecke
 * @version $Id: Tracer.java 222 2006-12-10 00:17:59Z just $
 */
public class GPSEngine implements GPSFetcherListener{

    /**
     * instance of GPSFetcher
     */
    private GPSFetcher gpsFetcher;
    private WPMidlet midlet;
    private int sampleCount;
    private Vector listeners = new Vector(3);

    private Vector points = new Vector(3);
    public static final long DEFAULT_LOC_SEND_INTERVAL_MILLIS = 25000;
    private long locSendIntervalMillis = DEFAULT_LOC_SEND_INTERVAL_MILLIS;
    private long lastTimeLocSent;

    private static final GPSEngine instance = new GPSEngine();

    /**
     * Starts GPS fetching and KW client.
     */

    private GPSEngine() {

    }

    public static GPSEngine getInstance() {
        return instance;
    }

    synchronized public void addListener(GPSEngineListener aListener) {
        listeners.addElement(aListener);
        Log.log("Added GPSEngineListener # " + listeners.size());
    }

    synchronized public void removeListener(GPSEngineListener aListener) {
        listeners.removeElement(aListener);
        Log.log("Removed GPSEngineListener # " + listeners.size());
    }

    /**
     * Starts GPS fetching and KW client.
     * @param aMIDlet the main midlet
     */
    public void start(WPMidlet aMIDlet) {
        midlet = aMIDlet;
        startGPSFetcher();
        Log.log("time=" + Calendar.getInstance().getTime() + " timezone=" + TimeZone.getDefault().getID());
    }

    /**
     * Disconnect
     */
    public void stop() {
        try {

            if (gpsFetcher != null) {
                gpsFetcher.stop();
                gpsFetcher = null;
            }                     
        } catch (Throwable t) {
            Log.log("Exception stopping GPS:" + t.toString());            
        }
    }

    public void onGPSConnect() {
        for (int i = 0; i < listeners.size(); i++) {
            ((GPSEngineListener)listeners.elementAt(i)).onGPSStatus("GPS connected");
        }
        Util.playTone(60, 250, midlet.getVolume());
    }

    /**
     * From GPSFetcher: GPS meta NMEA sample received.
     */
    synchronized public void onGPSInfo(GPSInfo theInfo) {
        for (int i = 0; i < listeners.size(); i++) {
            ((GPSEngineListener)listeners.elementAt(i)).onGPSInfo(theInfo);
        }
    }

    /**
     * From GPSFetcher: GPS NMEA sample received.
     */
    synchronized public void onGPSLocation(GPSLocation aLocation) {
        Util.playTone(84, 50, midlet.getVolume());

        onGPSStatus("sample #" + (++sampleCount));

        JXElement pt = new JXElement("pt");
        pt.setAttr("nmea", aLocation.data);

        pt.setAttr("t", aLocation.time);

        points.addElement(pt);

        // Send collected points to server if interval passed
        long now = Util.getTime();
        if (now - lastTimeLocSent > locSendIntervalMillis) {
            lastTimeLocSent = now;

            for (int i = 0; i < listeners.size(); i++) {
                ((GPSEngineListener)listeners.elementAt(i)).onGPSLocation(points);
            }
            
            points.removeAllElements();
        }
    }

    /**
     * From GPSFetcher: disconnect
     */
    public void onGPSDisconnect() {
        for (int i = 0; i < listeners.size(); i++) {
            ((GPSEngineListener)listeners.elementAt(i)).onGPSStatus("GPS disconnected");
        }
    }

    /**
     * From GPSFetcher: disconnect
     */
    public void onGPSError(String aReason, Throwable anException) {
        Log.log("GPS error: " + aReason + " e=" + anException);
        for (int i = 0; i < listeners.size(); i++) {
            ((GPSEngineListener)listeners.elementAt(i)).onGPSStatus("error");
        }
        Util.playTone(72, 100, midlet.getVolume());
        Util.playTone(71, 100, midlet.getVolume());
        Util.playTone(70, 100, midlet.getVolume());
    }

    /**
     * From GPSFetcher: status update
     */
    public void onGPSStatus(String aStatusMsg) {
        for (int i = 0; i < listeners.size(); i++) {
            ((GPSEngineListener)listeners.elementAt(i)).onGPSStatus(aStatusMsg);
        }
    }

    public void onGPSTimeout() {
        for (int i = 0; i < listeners.size(); i++) {
            ((GPSEngineListener)listeners.elementAt(i)).onGPSStatus("GPS timeout");
        }
    }

    public GPSLocation getCurrentLocation() {
        return gpsFetcher.getCurrentLocation();
    }

    private void startGPSFetcher() {
        try {
            String gpsURL = GPSSelector.getGPSURL();
            if (gpsURL == null) {
                for (int i = 0; i < listeners.size(); i++) {
                    ((GPSEngineListener)listeners.elementAt(i)).onGPSStatus("No GPS");
                }
                return;
            }

            for (int i = 0; i < listeners.size(); i++) {
                    ((GPSEngineListener)listeners.elementAt(i)).onGPSStatus("gpsURL=\n" + gpsURL);
            }

            long GPS_SAMPLE_INTERVAL = Long.parseLong(midlet.getAppProperty("gps-sample-interval"));
            gpsFetcher = GPSFetcher.getInstance();
            gpsFetcher.setListener(this);
            gpsFetcher.setURL(gpsURL);
            gpsFetcher.start(GPS_SAMPLE_INTERVAL);            
        } catch (Throwable t) {
             for (int i = 0; i < listeners.size(); i++) {
                    ((GPSEngineListener)listeners.elementAt(i)).onGPSStatus("start error");
             }
            gpsFetcher = null;
        }
    }


}
