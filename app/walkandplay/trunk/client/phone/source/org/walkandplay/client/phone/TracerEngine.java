package org.walkandplay.client.phone;

import org.geotracing.client.*;

import javax.microedition.midlet.MIDlet;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;

import nl.justobjects.mjox.JXElement;

/**
 * Integrates GPS, Screen and Network interaction.
 *
 * @author  Just van den Broecke
 * @version $Id: Tracer.java 222 2006-12-10 00:17:59Z just $
 */
public class TracerEngine implements GPSFetcherListener, NetListener {

	/** instance of GPSFetcher */
	private GPSFetcher gpsFetcher;

	/** Instance of KeyWorx client. */
	private Net net;
	private MIDlet midlet;

	private int sampleCount;
	private boolean paused = true;
	private int VOLUME = 70;
	public DefaultTraceDisplay traceDisplay;
	public PlayDisplay playDisplay;
	private int roadRating = -1;
    private Vector points = new Vector(3);
    public static final long DEFAULT_LOC_SEND_INTERVAL_MILLIS = 25000;
    private long locSendIntervalMillis = DEFAULT_LOC_SEND_INTERVAL_MILLIS;
    private long lastTimeLocSent;


    /**
	 * Starts GPS fetching and KW client.
	 */
	public TracerEngine(MIDlet aMIDlet, DefaultTraceDisplay aDisplay) {
		traceDisplay = aDisplay;
		midlet = aMIDlet;
		net = Net.getInstance();
		net.setListener(this);
	}

    public TracerEngine(MIDlet aMIDlet, PlayDisplay aDisplay) {
		playDisplay = aDisplay;
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
		if(traceDisplay!=null) traceDisplay.onGPSStatus("connected");
		if(playDisplay!=null) playDisplay.onGPSStatus("connected");
		if(traceDisplay!=null) traceDisplay.setStatus("GPS connected");
		if(playDisplay!=null) playDisplay.setStatus("GPS connected");

		Util.playTone(60, 250, VOLUME);
	}

	/** From GPSFetcher: GPS meta NMEA sample received. */
	synchronized public void onGPSInfo(GPSInfo theInfo) {
		if(traceDisplay!=null) traceDisplay.setGPSInfo(theInfo);
		if(playDisplay!=null) playDisplay.setGPSInfo(theInfo);
	}

	/** From GPSFetcher: GPS NMEA sample received. */
	synchronized public void onGPSLocation(GPSLocation aLocation) {
		Util.playTone(84, 50, VOLUME);

		onGPSStatus("sample #" + (++sampleCount));
		if (paused) {
			if(traceDisplay!=null) traceDisplay.onNetStatus("paused");
			if(playDisplay!=null) playDisplay.onNetStatus("paused");
			if(traceDisplay!=null) traceDisplay.setStatus("NOTE: not sending GPS !!\ndo ResumeTrack to send");
			if(playDisplay!=null) playDisplay.setStatus("NOTE: not sending GPS !!\ndo ResumeTrack to send");
			return;
		}


		JXElement pt = new JXElement("pt");
		pt.setAttr("nmea", aLocation.data);

		pt.setAttr("t", aLocation.time);

		if (roadRating != -1) {
			pt.setAttr("rr", roadRating);
		}

		points.addElement(pt);

		// Send collected points to server if interval passed
		long now = Util.getTime();
		if (now - lastTimeLocSent > locSendIntervalMillis) {
			lastTimeLocSent = now;
			net.sendPoints(points, sampleCount);
			sendPoints(points, sampleCount, "play-location-req");
			sendPoints(points, sampleCount, "t-trk-write-req");
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
            rsp.removeChildren();
            if (System.currentTimeMillis() % 3 == 0) {
                JXElement hit = new JXElement("cmt-hit");
                hit.setText("bericht van webspeler");
                rsp.addChild(hit);
            }

            if (System.currentTimeMillis() % 3 == 0) {
                JXElement hit = new JXElement("task-hit");
                hit.setAttr("id", 22560);
                rsp.addChild(hit);
            }

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
                System.out.println(new String(rsp.toBytes(false)));
                Util.playTone(96, 75, VOLUME);
                JXElement e = rsp.getChildAt(0);
                if(e!=null){
                    if(e.getTag().equals("task-hit")){
                        onNetStatus("task-" + e.getAttr("id"));
                    }else if(e.getTag().equals("medium-hit")){
                        onNetStatus("medium-" + e.getAttr("id"));
                    }else if(e.getTag().equals("cmt-hit")){
                        onNetStatus("cmt-" + e.getText());
                    }
                }else{
                    System.out.println("No task or medium hit found");                    
                }
            } else {
				onNetStatus("send error");
			}

		} catch (Throwable pe) {
			onNetStatus("send error");
		}
	}

    /** From GPSFetcher: disconnect */
	public void onGPSDisconnect() {
		if(traceDisplay!=null) traceDisplay.onGPSStatus("disconnected");
		if(playDisplay!=null) playDisplay.onGPSStatus("disconnected");
		if(traceDisplay!=null) traceDisplay.setStatus("GPS disconnected");
		if(playDisplay!=null) playDisplay.setStatus("GPS disconnected");
	}

	/** From GPSFetcher: disconnect */
	public void onGPSError(String aReason, Throwable anException) {
		Log.log("GPS error: " + aReason + " e=" + anException);
		if(traceDisplay!=null) traceDisplay.onGPSStatus("error");
		if(playDisplay!=null) playDisplay.onGPSStatus("error");
		// traceScreen.setStatus(aReason);
		Util.playTone(72, 100, VOLUME);
		Util.playTone(71, 100, VOLUME);
		Util.playTone(70, 100, VOLUME);
	}

	/** From GPSFetcher: status update */
	public void onGPSStatus(String aStatusMsg) {
		if(traceDisplay!=null) traceDisplay.onGPSStatus(aStatusMsg);
		if(playDisplay!=null) playDisplay.onGPSStatus(aStatusMsg);
		if(traceDisplay!=null) traceDisplay.setStatus("GPS " + aStatusMsg);
		if(playDisplay!=null) playDisplay.setStatus("GPS " + aStatusMsg);
	}

	public void onGPSTimeout() {
		if(traceDisplay!=null) traceDisplay.onGPSStatus("timeout");
		if(playDisplay!=null) playDisplay.onGPSStatus("timeout");
		if(traceDisplay!=null) traceDisplay.setStatus("GPS timeout");
		if(playDisplay!=null) playDisplay.setStatus("GPS timeout");
	}

	public void onNetInfo(String theInfo) {
        if(traceDisplay!=null) traceDisplay.setStatus(theInfo);
		if(playDisplay!=null) playDisplay.setStatus(theInfo);
	}

	public void onNetError(String aReason, Throwable anException) {
		if(traceDisplay!=null) traceDisplay.setStatus("ERROR " + aReason + "\n" + anException);
		if(playDisplay!=null) playDisplay.setStatus("ERROR " + aReason + "\n" + anException);
	}

	public void onNetStatus(String aStatusMsg) {
        if(traceDisplay!=null) traceDisplay.onNetStatus(aStatusMsg);
		if(playDisplay!=null) playDisplay.onNetStatus(aStatusMsg);
	}

	/** Check local midlet version with the one from server. */
	public String versionCheck() {
		String myVersion = midlet.getAppProperty("MIDlet-Version");

		String myName = midlet.getAppProperty("MIDlet-Name");
		String versionURL = Net.getInstance().getURL() + "/ota/version.html";
		String result = null;
		String theirVersion = null;
		try {
			theirVersion = Util.getPage(versionURL);
			if (theirVersion != null && !theirVersion.trim().equals(myVersion)) {
				result = "Your " + myName + " version (" + myVersion + ") differs from the version (" + theirVersion + ") available for download. \nYou may want to upgrade to " + theirVersion;
			}
		} catch (Throwable t) {
			result = "error fetching version from " + versionURL;
		}

		Log.log("versionCheck mine=" + myVersion + " theirs=" + theirVersion);
		return result;
	}

	private void startGPSFetcher() {
		try {
			String gpsURL = GPSSelector.getGPSURL();
			if (gpsURL == null) {
				if(traceDisplay!=null) traceDisplay.onGPSStatus("NO GPS");
				if(playDisplay!=null) playDisplay.onGPSStatus("NO GPS");
				if(traceDisplay!=null) traceDisplay.setStatus("choose SelectGPS in menu");
				if(playDisplay!=null) playDisplay.setStatus("choose SelectGPS in menu");
				return;
			}
			if(traceDisplay!=null) traceDisplay.setStatus("gpsURL=\n" + gpsURL);
			if(playDisplay!=null) playDisplay.setStatus("gpsURL=\n" + gpsURL);
			long GPS_SAMPLE_INTERVAL = Long.parseLong(midlet.getAppProperty("gps-sample-interval"));
			gpsFetcher = GPSFetcher.getInstance();
			gpsFetcher.setListener(this);
			gpsFetcher.setURL(gpsURL);
			gpsFetcher.start(GPS_SAMPLE_INTERVAL);
		} catch (Throwable t) {
			traceDisplay.onGPSStatus("start error");
			gpsFetcher = null;
		}
	}


}
