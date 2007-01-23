package org.walkandplay.client.phone;

import org.geotracing.client.*;

import javax.microedition.midlet.MIDlet;

/**
 * Sends GPS data and ratings to server.
 *
 * @author Just van den Broecke
 * @version $Id: Tracer.java 251 2007-01-08 15:55:20Z mrtn $
 */
public class Tracer implements GPSFetcherListener, NetListener {

    /**
     * instance of GPSFetcher
     */
    private GPSFetcher gpsFetcher;

    /**
     * Instance of KeyWorx client.
     */
    private Net net;
    private MIDlet midlet;

    private int sampleCount;
    private boolean paused = true;
    private int VOLUME = 70;
    //public TraceScreen traceScreen;
    public TraceScreen traceScreen;
    private int roadRating = -1;

    /**
     * Starts GPS fetching and KW client.
     */
    //public Tracer(MIDlet aMIDlet, TraceScreen aTraceScreen) {
    public Tracer(MIDlet aMIDlet, TraceScreen aTraceScreen) {
        traceScreen = aTraceScreen;
        midlet = aMIDlet;
        net = Net.getInstance();
        net.setListener(this);
    }

    public Net getNet() {
        return net;
    }

    public boolean isPaused() {
        return paused;
    }

    /**
     * Set road rating.
     */
    public void setRoadRating(int aRating) {
        roadRating = aRating;
    }

    /**
     * Starts GPS fetching and KW client.
     */
    public void start() {
        net.setProperties(midlet);
        net.start();
        startGPSFetcher();
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
            net.stop();

        } catch (Throwable t) {
            // ignore
        }
    }

    public void resume() {
        if (!paused) {
            return;
        }
        net.resume();
        sampleCount = 0;
        paused = false;
    }

    public void suspend() {
        if (paused) {
            return;
        }
        net.suspend();
        paused = true;
    }

    public void suspendResume() {
        if (paused) {
            resume();
        } else {
            suspend();
        }
    }

    public void onGPSConnect() {
        System.out.println("GPS connected");
        traceScreen.onGPSStatus("connected");
        traceScreen.setStatus("GPS connected");

        Util.playTone(60, 250, VOLUME);
    }

    /**
     * From GPSFetcher: GPS meta NMEA sample received.
     */
    synchronized public void onGPSInfo(GPSInfo theInfo) {
        System.out.println("GPSInfo:" + theInfo.toString());
        traceScreen.setGPSInfo(theInfo);
    }

    /**
     * From GPSFetcher: GPS NMEA sample received.
     */
    synchronized public void onGPSLocation(GPSLocation aLocation) {
        System.out.println("GPSlocation:" + aLocation.data + " paused=" + paused);
        Util.playTone(84, 50, VOLUME);

        traceScreen.onGPSStatus("sample #" + (++sampleCount));
        traceScreen.setLocation(aLocation.lon.toString(), aLocation.lat.toString());

        if (paused) {
            traceScreen.onNetStatus("paused");
            traceScreen.setStatus("NOTE: not sending GPS !!\ndo ResumeTrack to send");
            return;
        }
        net.sendSample(aLocation.data, roadRating, aLocation.time, sampleCount);
    }

    /**
     * From GPSFetcher: disconnect
     */
    public void onGPSDisconnect() {
        System.out.println("GPS disconnected");
        traceScreen.onGPSStatus("disconnected");
        traceScreen.setStatus("GPS disconnected");
    }

    /**
     * From GPSFetcher: disconnect
     */
    public void onGPSError(String aReason, Throwable anException) {
        Log.log("GPS error: " + aReason + " e=" + anException);
        traceScreen.onGPSStatus("error");
        // traceScreen.setStatus(aReason);
        Util.playTone(72, 100, VOLUME);
        Util.playTone(71, 100, VOLUME);
        Util.playTone(70, 100, VOLUME);
    }

    /**
     * From GPSFetcher: status update
     */
    public void onGPSStatus(String aStatusMsg) {
        System.out.println("GPS status:" + aStatusMsg);
        traceScreen.onGPSStatus(aStatusMsg);
        traceScreen.setStatus("GPS " + aStatusMsg);
    }

    public void onGPSTimeout() {
        System.out.println("GPS timout");
        traceScreen.onGPSStatus("timeout");
        traceScreen.setStatus("GPS timeout");
    }

    public void onNetInfo(String theInfo) {
        System.out.println("Net info:" + theInfo);
        traceScreen.setStatus(theInfo);
    }

    public void onNetError(String aReason, Throwable anException) {
        traceScreen.setStatus("ERROR " + aReason + "\n" + anException);
    }

    public void onNetStatus(String aStatusMsg) {
        System.out.println("Net status:" + aStatusMsg);
        traceScreen.onNetStatus(aStatusMsg);

    }

    private void startGPSFetcher() {
        try {

            String gpsURL = GPSScreen.getGPSURL();
            if (gpsURL == null) {
                traceScreen.onGPSStatus("NO GPS");
                traceScreen.setStatus("choose SelectGPS in menu");
                return;
            }
            traceScreen.setStatus("gpsURL=\n" + gpsURL);
            long GPS_SAMPLE_INTERVAL = Long.parseLong(midlet.getAppProperty("gps-sample-interval"));
            gpsFetcher = GPSFetcher.getInstance();
            gpsFetcher.setListener(this);
            gpsFetcher.setURL(gpsURL);
            gpsFetcher.start(GPS_SAMPLE_INTERVAL);
        } catch (Throwable t) {
            traceScreen.onGPSStatus("start error t=" + t);
            gpsFetcher = null;
        }
    }

}