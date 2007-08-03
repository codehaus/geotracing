package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.*;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Integrates GPS, Screen and Network interaction.
 *
 * @author Just van den Broecke
 * @version $Id: Tracer.java 222 2006-12-10 00:17:59Z just $
 */
public class TracerEngine implements GPSFetcherListener, NetListener {

    /**
     * instance of GPSFetcher
     */
    private GPSFetcher gpsFetcher;

    /**
     * Instance of KeyWorx client.
     */
    private Net net;
    private WPMidlet midlet;

    private int sampleCount;
    private boolean paused = true;
    private int VOLUME = 70;
    public TracerEngineListener listener;
    private Vector points = new Vector(3);
    public static final long DEFAULT_LOC_SEND_INTERVAL_MILLIS = 25000;
    private long locSendIntervalMillis = DEFAULT_LOC_SEND_INTERVAL_MILLIS;
    private long lastTimeLocSent;
    private boolean playing;

    /**
     * Starts GPS fetching and KW client.
     */
    public TracerEngine(WPMidlet aMIDlet, TracerEngineListener aListener, boolean isPlaying) {
        listener = aListener;
        playing = isPlaying;
        midlet = aMIDlet;
        net = Net.getInstance();
        net.setListener(this);
    }

    public Net getNet() {
        return net;
    }

    public void setListener(TracerEngineListener aListener) {
        listener = aListener;
    }

    public boolean isPaused() {
        return paused;
    }

    /**
     * Starts GPS fetching and KW client.
     */
    public void start() {
        net.setProperties(midlet);
        net.start();
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
            net.stop();

        } catch (Throwable t) {
            // ignore
        }
    }

    public void resume() {
        if (!paused) {
            return;
        }
        //net.resume();
        sampleCount = 0;
        paused = false;
    }

    public void suspend() {
        if (paused) {
            return;
        }
        //net.suspend();
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
        listener.onGPSStatus("connected");
        listener.setStatus("GPS connected");
        Util.playTone(60, 250, VOLUME);
    }

    /**
     * From GPSFetcher: GPS meta NMEA sample received.
     */
    synchronized public void onGPSInfo(GPSInfo theInfo) {
        listener.setGPSInfo(theInfo);
    }

    /**
     * From GPSFetcher: GPS NMEA sample received.
     */
    synchronized public void onGPSLocation(GPSLocation aLocation) {
        Util.playTone(84, 50, VOLUME);

        onGPSStatus("sample #" + (++sampleCount));
        if (paused) {
            listener.onNetStatus("paused");
            listener.setStatus("NOTE: not sending GPS !!\ndo ResumeTrack to send");
            return;
        }


        JXElement pt = new JXElement("pt");
        pt.setAttr("nmea", aLocation.data);

        pt.setAttr("t", aLocation.time);

        points.addElement(pt);

        // Send collected points to server if interval passed
        long now = Util.getTime();
        if (now - lastTimeLocSent > locSendIntervalMillis) {
            lastTimeLocSent = now;
            if (playing) {
                sendPoints(points, sampleCount, "play-location-req");
            } else {
                sendPoints(points, sampleCount, "t-trk-write-req");

            }
            points.removeAllElements();
        }
    }

    /**
     * Send GPS points.
     */
    public void sendPoints(Vector thePoints, int theCount, String aRequestName) {
        JXElement req = new JXElement(aRequestName);
        req.addChildren(thePoints);
        try {
            onNetStatus("sending #" + theCount);
            JXElement rsp = net.utopiaReq(req);
            // TODO: remove later - teskting purposes
            //rsp.removeChildren();

            /*if (System.currentTimeMillis() % 3 == 0) {
                JXElement hit = new JXElement("task-hit");
                hit.setAttr("id", 22560);
                rsp.addChild(hit);
            }*/

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

            //long lastCommandTime = Util.getTime();
            if (rsp != null) {
                onNetStatus("sent #" + theCount);
                Log.log(new String(rsp.toBytes(false)));
                Util.playTone(96, 75, VOLUME);
                JXElement e = rsp.getChildAt(0);
                if (e != null) {
                    listener.setHit(e);
                } else {
                    Log.log("No task or medium hit found");
                }
            } else {
                onNetStatus("send error");
            }

        } catch (Throwable pe) {
            onNetStatus("send error");
        }
    }

    /**
     * From GPSFetcher: disconnect
     */
    public void onGPSDisconnect() {
        listener.onGPSStatus("disconnected");
        listener.setStatus("GPS disconnected");
    }

    /**
     * From GPSFetcher: disconnect
     */
    public void onGPSError(String aReason, Throwable anException) {
        Log.log("GPS error: " + aReason + " e=" + anException);
        listener.onGPSStatus("error");
        // traceScreen.setStatus(aReason);
        Util.playTone(72, 100, VOLUME);
        Util.playTone(71, 100, VOLUME);
        Util.playTone(70, 100, VOLUME);
    }

    /**
     * From GPSFetcher: status update
     */
    public void onGPSStatus(String aStatusMsg) {
        listener.onGPSStatus(aStatusMsg);
        listener.setStatus("GPS " + aStatusMsg);
    }

    public void onGPSTimeout() {
        listener.onGPSStatus("timeout");
        listener.setStatus("GPS timeout");
    }

    public void onNetInfo(String theInfo) {
        listener.setStatus(theInfo);
    }

    public void onNetError(String aReason, Throwable anException) {
        listener.setStatus("ERROR " + aReason + "\n" + anException);
    }

    public void onNetStatus(String aStatusMsg) {
        listener.onNetStatus(aStatusMsg);
    }

    public GPSLocation getCurrentLocation() {
        return gpsFetcher.getCurrentLocation();
    }

    private void startGPSFetcher() {
        try {
            String gpsURL = GPSSelector.getGPSURL();
            if (gpsURL == null) {
                listener.onGPSStatus("NO GPS");
                listener.setStatus("choose SelectGPS in menu");
                return;
            }
            listener.setStatus("gpsURL=\n" + gpsURL);

            long GPS_SAMPLE_INTERVAL = Long.parseLong(midlet.getAppProperty("gps-sample-interval"));
            gpsFetcher = GPSFetcher.getInstance();
            gpsFetcher.setListener(this);
            gpsFetcher.setURL(gpsURL);
            gpsFetcher.start(GPS_SAMPLE_INTERVAL);
        } catch (Throwable t) {
            listener.onGPSStatus("start error");
            gpsFetcher = null;
        }
    }


}
