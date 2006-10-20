// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.client;


import nl.justobjects.mjox.JXElement;
import org.keyworx.mclient.HTTPClient;

import javax.microedition.midlet.MIDlet;
import java.util.Timer;
import java.util.TimerTask;


/**
 * KeyWorx network client for GeoTracing protocol.
 * <p>
 * Singleton class that provides an API wrapper to the
 * GeoTracing protocol over HTTP. Uses KWClient and MJOX (XML)
 * from KeyWorx.
 * </p>
 *
 * @author  Just van den Broecke
 * @version $Id$
 */
public class Net {

	/** Instance of KeyWorx client. */
	private HTTPClient kwClient;
	private int VOLUME = 70;
	private static long HB_INTERVAL = 60000L;
	private static String trackId;
	private Timer heartbeatTimer;
	private long lastCommandTime = -1;

	private static final Net instance = new Net();
	private String url, user, password, app, role;
	private boolean minimal;
	private NetListener listener;
	public static final String RMS_STORE_NAME = "Net";
	public static final String PROP_USER = "kw-user";
	public static final String PROP_PASSWORD = "kw-password";
	public static final String PROP_URL = "kw-url";

	private Net() {
	}

	public static Net getInstance() {
		return instance;
	}

	public void setListener(NetListener aNetListener) {
		listener = aNetListener;
	}

	public void setProperties(MIDlet aMIDlet) {
		try {
			Preferences prefs = new Preferences(RMS_STORE_NAME);

			// Login name: value in RMS prevails
			user = prefs.get(PROP_USER, aMIDlet.getAppProperty(PROP_USER));

			password = prefs.get(PROP_PASSWORD, aMIDlet.getAppProperty(PROP_PASSWORD));

			url = prefs.get(PROP_URL, aMIDlet.getAppProperty(PROP_URL));

			app = aMIDlet.getAppProperty("kw-app");
			role = aMIDlet.getAppProperty("kw-role");
			minimal = aMIDlet.getAppProperty("mt-options").indexOf("minimal") != -1;
		} catch (Throwable t) {
            listener.onNetError("Cannot read RMS", t);
		}
	}

	public void addPOI(String aType, String aName, String aDescription) {
		try {
			// Adding POI
			listener.onNetStatus("POI add...");
			JXElement poiAddReq = new JXElement("t-trk-add-poi-req");
			poiAddReq.setAttr("type", aType);
			poiAddReq.setAttr("name", aName);
			poiAddReq.setAttr("t", Util.getTime());
			if (aDescription != null) {
				poiAddReq.setAttr("description", aDescription);
			}
			JXElement rsp = kwClient.utopia(poiAddReq);
			listener.onNetInfo("new POI type=" + aType + " id=" + rsp.getAttr("id"));
			listener.onNetStatus("POI add OK");

		} catch (Throwable pe) {
			listener.onNetStatus("POI add error");
		}
	}

	public void delTrack() {
		try {
			listener.onNetStatus("deltrack");
			JXElement req = new JXElement("t-trk-delete-req");
			req.setAttr("t", Util.getTime());
			JXElement rsp = kwClient.utopia(req);

			trackId = rsp.getAttr("id", null);
			listener.onNetStatus("deleted");
		} catch (Throwable pe) {
			listener.onNetStatus("error");
			trackId = null;
		}
	}

	public void newTrack(String aName) {
		try {
			listener.onNetStatus("creating trk");
			JXElement req = new JXElement("t-trk-create-req");
			req.setAttr("name", aName);
			req.setAttr("t", Util.getTime());
			if (minimal) {
				// Minimal mode: tracks are made daily (Track type 2)
				req.setAttr("type", 2);
			}

			JXElement rsp = kwClient.utopia(req);

			trackId = rsp.getAttr("id", null);
			listener.onNetStatus("trk created");
			listener.onNetInfo("newtrack: " + aName + " (" + trackId + ")");
		} catch (Throwable pe) {
			listener.onNetStatus("error");
			trackId = null;
		}
	}

	public void resume() {
		try {
			JXElement req = new JXElement("t-trk-resume-req");
			req.setAttr("t", Util.getTime());
			if (minimal) {
				// Minimal mode: tracks are made daily (Track type 2)
				req.setAttr("type", 2);
			}
			listener.onNetStatus("resuming..");
			JXElement rsp = kwClient.utopia(req);

			trackId = rsp.getAttr("id", null);
			listener.onNetStatus("resumed");
			Util.playTone(96, 75, VOLUME);
			listener.onNetInfo("resumed: trkid=" + trackId);
		} catch (Throwable pe) {
			listener.onNetStatus("resume error");
			trackId = null;
		}
	}

	/** Send GPS sample. */
	public void sendSample(String theData, int aRoadRating, long theTime, int theCount) {
		JXElement req = new JXElement("t-trk-write-req");

		JXElement pt = new JXElement("pt");
		pt.setAttr("nmea", theData);

		req.addChild(pt);
		pt.setAttr("t", theTime);

		if (aRoadRating != -1) {
			pt.setAttr("rr", aRoadRating);
		}

		try {
			listener.onNetStatus("sending #" + theCount);
			JXElement rsp = kwClient.utopia(req);
			if (rsp != null) {
				listener.onNetStatus("sent #" + theCount);
				Util.playTone(96, 75, VOLUME);
			} else {
				listener.onNetStatus("send error");
			}

		} catch (Throwable pe) {
			listener.onNetStatus("send error");
			kwClient = null;
		}
	}

	public void suspend() {

		try {
			listener.onNetStatus("pausing..");
			JXElement req = new JXElement("t-trk-suspend-req");
			req.setAttr("t", Util.getTime());
			JXElement rsp = kwClient.utopia(req);
			trackId = rsp.getAttr("id", null);
			listener.onNetStatus("paused");
			listener.onNetInfo("paused: trkid=" + trackId);
		} catch (Throwable pe) {
			listener.onNetStatus("error");
		} finally {
			trackId = null;
		}
	}

	public JXElement uploadMedium(String aName, String aType, String aMime, long aTime, byte[] theData, boolean encode) {

		// get the current image bytes
		// byte[] imageBytes = getImgFromRecStore(aKey);

		// create the request
		JXElement uploadReq = new JXElement("t-trk-upload-medium-req");
		uploadReq.setAttr("type", aType);
		uploadReq.setAttr("mime", aMime);
		uploadReq.setAttr("t", aTime);
		if (aName == null) {
			aName = "mt-upload";
		}
		uploadReq.setAttr("name", aName);

		// either encode the img in hexasc or send as raw
		JXElement data = new JXElement("data");
		if (encode) {
			data.setAttr("encoding", "hexasc");
			String img = Util.encode(theData, 0, theData.length);
			data.setCDATA(img.getBytes());
		} else {
			data.setAttr("encoding", "raw");
			data.setAttr("length", theData.length);
			data.setCDATA(theData);
		}
		uploadReq.addChild(data);

		// send the request
		return utopiaReq(uploadReq);
	}

	public JXElement utopiaReq(JXElement aReq) {
		if (kwClient == null) {
			start();
		}

		JXElement rsp = null;
		try {
			rsp = kwClient.utopia(aReq);
			lastCommandTime = Util.getTime();
		} catch (Throwable pe) {
			kwClient = null;
		}

		return rsp;
	}


	public void start() {
		try {
			// traceScreen.hideCommands();
			kwClient = new HTTPClient(url + "/proto.srv");
			JXElement rsp = kwClient.login(user, password);
			if (rsp.hasAttr("time")) {
				Util.setTime(rsp.getLongAttr("time"));
			}
			kwClient.selectApp(app, role);
			listener.onNetStatus("login OK");
			listener.onNetInfo("login OK user=" + user + "\n" + "server=" + url + "\n" + "timeoffset=" + (Util.getTimeOffset()/1000) + " sec");
			startHeartbeat();
		} catch (Throwable pe) {
			listener.onNetStatus("cannot login");
			listener.onNetInfo("LOGIN FAILED\nreason: \n" + pe.getMessage());
			kwClient = null;
		} finally {
			// traceScreen.showCommands();
		}
	}

	/**
	 * Disconnect
	 */
	public void stop() {
		stopHeartbeat();

		if (kwClient == null) {
			return;
		}

		try {
			listener.onNetStatus("logging out");
			kwClient.logout();
		} catch (Throwable t) {
			listener.onNetStatus("logout error");
		}
		kwClient = null;

	}


	private void sendHeartbeat() {
		JXElement req = new JXElement("t-hb-req");

		try {
			listener.onNetStatus("heartbeat..");
			req.setAttr("t", Util.getTime());
			utopiaReq(req);
			listener.onNetStatus("heartbeat ok");
		} catch (Throwable pe) {
			listener.onNetStatus("hb error");
			kwClient = null;
		}
	}

	private void stopHeartbeat() {
		// Already running
		if (heartbeatTimer != null) {
			heartbeatTimer.cancel();
			heartbeatTimer = null;
		}
	}

	private void startHeartbeat() {
		// Already running
		if (heartbeatTimer != null) {
			return;
		}

		heartbeatTimer = new Timer();
		TimerTask task = new Heartbeat();

		// wait five seconds before executing, then
		// execute every ten seconds
		heartbeatTimer.schedule(task, 5000, HB_INTERVAL);
	}

	private class Heartbeat extends TimerTask {
		public void run() {
			if (Util.getTime() - lastCommandTime > HB_INTERVAL || lastCommandTime < 0) {
				sendHeartbeat();
			}
		}
	}
}
