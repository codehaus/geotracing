package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import org.geotracing.client.*;
import org.geotracing.client.Log;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Integrates GPS, Screen and Network interaction.
 *
 * @author Just van den Broecke
 * @version $Id: Tracer.java 222 2006-12-10 00:17:59Z just $
 */
public class GPSEngine implements GPSFetcherListener, TCPClientListener {

    /**
     * instance of GPSFetcher
     */
    private GPSFetcher gpsFetcher;
    private WPMidlet midlet;

    private int sampleCount;
    public int VOLUME = 70;
    public GPSEngineListener gpsEngineListener;
    private Vector points = new Vector(3);
    public static final long DEFAULT_LOC_SEND_INTERVAL_MILLIS = 25000;
    private long locSendIntervalMillis = DEFAULT_LOC_SEND_INTERVAL_MILLIS;
    private long lastTimeLocSent;

    /**
     * Starts GPS fetching and KW client.
     */
    public GPSEngine(WPMidlet aMIDlet, GPSEngineListener aListener) {
        gpsEngineListener = aListener;        
        midlet = aMIDlet;
    }

    public void setGpsEngineListener(GPSEngineListener aListener) {
        gpsEngineListener = aListener;
    }

    /**
     * Starts GPS fetching and KW client.
     */
    public void start() {
        startGPSFetcher();
        Log.log("time=" + Calendar.getInstance().getTime() + " timezone=" + TimeZone.getDefault().getID());
    }

    /**
     * Disconnect
     */
    public void stop() {
        try {
            midlet.getActiveApp().removeTCPClientListener(this);

            if (gpsFetcher != null) {
                gpsFetcher.stop();
                gpsFetcher = null;
            }                     
        } catch (Throwable t) {
            // ignore
        }
    }

    public void onGPSConnect() {
        gpsEngineListener.onGPSStatus("connected");
        gpsEngineListener.onStatus("GPS connected");
        Util.playTone(60, 250, VOLUME);
    }

    /**
     * From GPSFetcher: GPS meta NMEA sample received.
     */
    synchronized public void onGPSInfo(GPSInfo theInfo) {
        gpsEngineListener.onGPSInfo(theInfo);
    }

    /**
     * From GPSFetcher: GPS NMEA sample received.
     */
    synchronized public void onGPSLocation(GPSLocation aLocation) {
        Util.playTone(84, 50, VOLUME);

        onGPSStatus("sample #" + (++sampleCount));

        JXElement pt = new JXElement("pt");
        pt.setAttr("nmea", aLocation.data);

        pt.setAttr("t", aLocation.time);

        points.addElement(pt);

        // Send collected points to server if interval passed
        long now = Util.getTime();
        if (now - lastTimeLocSent > locSendIntervalMillis) {
            lastTimeLocSent = now;

            // send the request
            JXElement req = new JXElement("play-location-req");
            req.addChildren(points);
            midlet.getActiveApp().sendRequest(req);

            points.removeAllElements();
        }
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        onNetStatus("Net ok");
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("play-location-rsp")) {
                onNetStatus("GPS sample sent");
                
                if (System.currentTimeMillis() % 3 == 0 && !rsp.hasChildren()) {
                    JXElement hit = new JXElement("medium-hit");
                    hit.setAttr("id", 26527);
                    rsp.addChild(hit);
                }

                if (System.currentTimeMillis() % 3 == 0 && !rsp.hasChildren()) {
                    JXElement hit = new JXElement("medium-hit");
                    hit.setAttr("id", 22629);
                    rsp.addChild(hit);
                }
                
                if (System.currentTimeMillis() % 3 == 0 && !rsp.hasChildren()) {
                    JXElement hit = new JXElement("task-hit");
                    hit.setAttr("id", 22560);
                    rsp.addChild(hit);
                }

                Log.log(new String(rsp.toBytes(false)));
                Util.playTone(96, 75, VOLUME);
                JXElement e = rsp.getChildAt(0);
                if (e != null) {
                    gpsEngineListener.onHit(e);
                }
            }
        }
    }

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        onNetStatus("Net stopped");
    }


    /**
     * From GPSFetcher: disconnect
     */
    public void onGPSDisconnect() {
        gpsEngineListener.onGPSStatus("disconnected");
        gpsEngineListener.onStatus("GPS disconnected");
    }

    /**
     * From GPSFetcher: disconnect
     */
    public void onGPSError(String aReason, Throwable anException) {
        Log.log("GPS error: " + aReason + " e=" + anException);
        gpsEngineListener.onGPSStatus("error");
        Util.playTone(72, 100, VOLUME);
        Util.playTone(71, 100, VOLUME);
        Util.playTone(70, 100, VOLUME);
    }

    /**
     * From GPSFetcher: status update
     */
    public void onGPSStatus(String aStatusMsg) {
        gpsEngineListener.onGPSStatus(aStatusMsg);
        gpsEngineListener.onStatus("GPS " + aStatusMsg);
    }

    public void onGPSTimeout() {
        gpsEngineListener.onGPSStatus("timeout");
        gpsEngineListener.onStatus("GPS timeout");
    }

    public void onNetInfo(String theInfo) {
        gpsEngineListener.onStatus(theInfo);
    }

    public void onNetError(String aReason, Throwable anException) {
        gpsEngineListener.onStatus("ERROR " + aReason + "\n" + anException);
    }

    public void onNetStatus(String aStatusMsg) {
        gpsEngineListener.onNetStatus(aStatusMsg);
    }

    public GPSLocation getCurrentLocation() {
        return gpsFetcher.getCurrentLocation();
    }

    private void startGPSFetcher() {
        try {
            String gpsURL = GPSSelector.getGPSURL();
            if (gpsURL == null) {
                gpsEngineListener.onGPSStatus("NO GPS");
                gpsEngineListener.onStatus("choose SelectGPS in menu");
                return;
            }
            gpsEngineListener.onStatus("gpsURL=\n" + gpsURL);

            long GPS_SAMPLE_INTERVAL = Long.parseLong(midlet.getAppProperty("gps-sample-interval"));
            gpsFetcher = GPSFetcher.getInstance();
            gpsFetcher.setListener(this);
            gpsFetcher.setURL(gpsURL);
            gpsFetcher.start(GPS_SAMPLE_INTERVAL);            
        } catch (Throwable t) {
            gpsEngineListener.onGPSStatus("start error");
            gpsFetcher = null;
        }
    }


}
