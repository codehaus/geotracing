// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.client;


import nl.justobjects.mjox.JXElement;

import javax.microedition.midlet.MIDlet;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;


/**
 * Integrates GPS, Screen and Network interaction.
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
	public TraceScreen traceScreen;
	private int roadRating = -1;
	public static final long DEFAULT_LOC_SEND_INTERVAL_MILLIS = 25000;
	private long locSendIntervalMillis = DEFAULT_LOC_SEND_INTERVAL_MILLIS;
	private long lastTimeLocSent;
	private Vector points = new Vector(3);

	/**
	 * Starts GPS fetching and KW client.
	 */
	public Tracer(MIDlet aMIDlet, TraceScreen aTraceScreen) {
		traceScreen = aTraceScreen;
		midlet = aMIDlet;
		net = Net.getInstance();
		net.setListener(this);
		// Log.log("Tracer cons");
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
		locSendIntervalMillis = Long.parseLong(midlet.getAppProperty("gps-send-interval"));
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
		traceScreen.onGPSStatus("connected");
		traceScreen.setStatus("GPS connected");

		Util.playTone(60, 250, VOLUME);
	}

	/**
	 * From GPSFetcher: GPS meta NMEA sample received.
	 */
	synchronized public void onGPSInfo(GPSInfo theInfo) {
		traceScreen.setGPSInfo(theInfo);
	}

	/**
	 * From GPSFetcher: GPS NMEA sample received.
	 */
	synchronized public void onGPSLocation(GPSLocation aLocation) {
		Util.playTone(84, 50, VOLUME);

		traceScreen.onGPSStatus("sample #" + (++sampleCount));
		if (paused) {
			traceScreen.onNetStatus("paused");
			traceScreen.setStatus("NOTE: not sending GPS !!\ndo ResumeTrack to send");
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
			points.removeAllElements();
		}
	}

	/**
	 * From GPSFetcher: disconnect
	 */
	public void onGPSDisconnect() {
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
		traceScreen.onGPSStatus(aStatusMsg);
		traceScreen.setStatus("GPS " + aStatusMsg);
	}

	public void onGPSTimeout() {
		traceScreen.onGPSStatus("timeout");
		traceScreen.setStatus("GPS timeout");
	}

	public void onNetInfo(String theInfo) {
		traceScreen.setStatus(theInfo);
	}

	public void onNetError(String aReason, Throwable anException) {
		traceScreen.setStatus("ERROR " + aReason + "\n" + anException);
	}

	public void onNetStatus(String aStatusMsg) {
		traceScreen.onNetStatus(aStatusMsg);

	}

	/**
	 * Check local midlet version with the one from server.
	 */
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
			traceScreen.onGPSStatus("start error");
			gpsFetcher = null;
		}
	}


}
