package org.walkandplay.client.phone;


import org.geotracing.client.*;

import javax.microedition.midlet.MIDlet;


/**
 * Sends GPS data and ratings to server.
 *
 * @author Just van den Broecke
 * @version $Id$
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
    //public TraceScreen traceCanvas;
    public TraceCanvas traceCanvas;
    private int roadRating = -1;

    /**
     * Starts GPS fetching and KW client.
     */
    //public Tracer(MIDlet aMIDlet, TraceScreen aTraceScreen) {
    public Tracer(MIDlet aMIDlet, TraceCanvas aTraceCanvas) {
        traceCanvas = aTraceCanvas;
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
        traceCanvas.onGPSStatus("connected");
        traceCanvas.setStatus("GPS connected");

        Util.playTone(60, 250, VOLUME);
    }

    /**
     * From GPSFetcher: GPS meta NMEA sample received.
     */
    synchronized public void onGPSInfo(GPSInfo theInfo) {
        System.out.println("GPSInfo:" + theInfo.toString());
        traceCanvas.setGPSInfo(theInfo);
    }

    /**
     * From GPSFetcher: GPS NMEA sample received.
     */
    synchronized public void onGPSLocation(GPSLocation aLocation) {
        System.out.println("GPSlocation:" + aLocation.data + " paused=" + paused);
        Util.playTone(84, 50, VOLUME);

        traceCanvas.onGPSStatus("sample #" + (++sampleCount));
		traceCanvas.setLocation(aLocation.lon.toString(),  aLocation.lat.toString());

		if (paused) {
            traceCanvas.onNetStatus("paused");
            traceCanvas.setStatus("NOTE: not sending GPS !!\ndo ResumeTrack to send");
            return;
        }
        net.sendSample(aLocation.data, roadRating, aLocation.time, sampleCount);
    }

    /**
     * From GPSFetcher: disconnect
     */
    public void onGPSDisconnect() {
        System.out.println("GPS disconnected");
        traceCanvas.onGPSStatus("disconnected");
        traceCanvas.setStatus("GPS disconnected");
    }

    /**
     * From GPSFetcher: disconnect
     */
    public void onGPSError(String aReason, Throwable anException) {
        Log.log("GPS error: " + aReason + " e=" + anException);
        traceCanvas.onGPSStatus("error");
        // traceCanvas.setStatus(aReason);
        Util.playTone(72, 100, VOLUME);
        Util.playTone(71, 100, VOLUME);
        Util.playTone(70, 100, VOLUME);
    }

    /**
     * From GPSFetcher: status update
     */
    public void onGPSStatus(String aStatusMsg) {
        System.out.println("GPS status:" + aStatusMsg);
        traceCanvas.onGPSStatus(aStatusMsg);
        traceCanvas.setStatus("GPS " + aStatusMsg);
    }

    public void onGPSTimeout() {
        System.out.println("GPS timout");
        traceCanvas.onGPSStatus("timeout");
        traceCanvas.setStatus("GPS timeout");
    }

    public void onNetInfo(String theInfo) {
        System.out.println("Net info:" + theInfo);
        traceCanvas.setStatus(theInfo);
    }

    public void onNetError(String aReason, Throwable anException) {
        traceCanvas.setStatus("ERROR " + aReason + "\n" + anException);
    }

    public void onNetStatus(String aStatusMsg) {
        System.out.println("Net status:" + aStatusMsg);
        traceCanvas.onNetStatus(aStatusMsg);

    }

    private void startGPSFetcher() {
        try {

            String gpsURL = GPSCanvas.getGPSURL();
            if (gpsURL == null) {
                traceCanvas.onGPSStatus("NO GPS");
                traceCanvas.setStatus("choose SelectGPS in menu");
                return;
            }
            traceCanvas.setStatus("gpsURL=\n" + gpsURL);
            long GPS_SAMPLE_INTERVAL = Long.parseLong(midlet.getAppProperty("gps-sample-interval"));
			gpsFetcher = GPSFetcher.getInstance();
			gpsFetcher.setListener(this);
			gpsFetcher.setURL(gpsURL);
            gpsFetcher.start(GPS_SAMPLE_INTERVAL);
        } catch (Throwable t) {
            traceCanvas.onGPSStatus("start error t=" + t);
            gpsFetcher = null;
        }
    }

}
