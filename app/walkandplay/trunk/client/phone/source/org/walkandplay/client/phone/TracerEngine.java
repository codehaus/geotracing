package org.walkandplay.client.phone;

import org.geotracing.client.*;

import javax.microedition.midlet.MIDlet;
import java.util.Calendar;
import java.util.TimeZone;

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
	public TraceDisplay traceDisplay;
	private int roadRating = -1;

	/**
	 * Starts GPS fetching and KW client.
	 */
	public TracerEngine(MIDlet aMIDlet, TraceDisplay aTraceDisplay) {
		traceDisplay = aTraceDisplay;
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
		traceDisplay.onGPSStatus("connected");
		traceDisplay.setStatus("GPS connected");

		Util.playTone(60, 250, VOLUME);
	}

	/** From GPSFetcher: GPS meta NMEA sample received. */
	synchronized public void onGPSInfo(GPSInfo theInfo) {
		traceDisplay.setGPSInfo(theInfo);
	}

	/** From GPSFetcher: GPS NMEA sample received. */
	synchronized public void onGPSLocation(GPSLocation aLocation) {
		Util.playTone(84, 50, VOLUME);

		traceDisplay.onGPSStatus("sample #" + (++sampleCount));
		if (paused) {
			traceDisplay.onNetStatus("paused");
			traceDisplay.setStatus("NOTE: not sending GPS !!\ndo ResumeTrack to send");
			return;
		}
		net.sendSample(aLocation.data, roadRating, aLocation.time, sampleCount);
	}

	/** From GPSFetcher: disconnect */
	public void onGPSDisconnect() {
		traceDisplay.onGPSStatus("disconnected");
		traceDisplay.setStatus("GPS disconnected");
	}

	/** From GPSFetcher: disconnect */
	public void onGPSError(String aReason, Throwable anException) {
		Log.log("GPS error: " + aReason + " e=" + anException);
		traceDisplay.onGPSStatus("error");
		// traceScreen.setStatus(aReason);
		Util.playTone(72, 100, VOLUME);
		Util.playTone(71, 100, VOLUME);
		Util.playTone(70, 100, VOLUME);
	}

	/** From GPSFetcher: status update */
	public void onGPSStatus(String aStatusMsg) {
		traceDisplay.onGPSStatus(aStatusMsg);
		traceDisplay.setStatus("GPS " + aStatusMsg);
	}

	public void onGPSTimeout() {
		traceDisplay.onGPSStatus("timeout");
		traceDisplay.setStatus("GPS timeout");
	}

	public void onNetInfo(String theInfo) {
		traceDisplay.setStatus(theInfo);
	}

	public void onNetError(String aReason, Throwable anException) {
		traceDisplay.setStatus("ERROR " + aReason + "\n" + anException);
	}

	public void onNetStatus(String aStatusMsg) {
		traceDisplay.onNetStatus(aStatusMsg);

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
				traceDisplay.onGPSStatus("NO GPS");
				traceDisplay.setStatus("choose SelectGPS in menu");
				return;
			}
			traceDisplay.setStatus("gpsURL=\n" + gpsURL);
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
