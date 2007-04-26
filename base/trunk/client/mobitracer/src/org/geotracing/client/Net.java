// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.client;


import nl.justobjects.mjox.JXElement;
import org.keyworx.mclient.HTTPClient;
import org.keyworx.mclient.Protocol;

import javax.microedition.midlet.MIDlet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;


/**
 * KeyWorx network client for GeoTracing protocol.
 * <p/>
 * Singleton class that provides an API wrapper to the
 * GeoTracing protocol over HTTP. Uses KWClient and MJOX (XML)
 * from KeyWorx.
 * </p>
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class Net {

	public static final int DISCONNECTED = 1;
	public static final int CONNECTED = 2;
	public static final int SENDING = 3;

	/**
	 * Instance of KeyWorx client.
	 */
	private HTTPClient kwClient;
	private boolean sending;
	private int VOLUME = 70;
	private static long HB_INTERVAL = 60000L;
	private static String trackId;
	private Timer heartbeatTimer;
	private long lastCommandTime = -1;
	private String agentKey;
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

	public int getState() {
		if (!isConnected()) {
			return DISCONNECTED;
		} else {
			return isSending() ? SENDING : CONNECTED;
		}
	}

	public String getUserName() {
		return user;
	}


	public long getLastCommandTime() {
		return lastCommandTime;
	}

	public String getURL() {
		return url;
	}

	public boolean isConnected() {
		return kwClient != null;
	}

	public boolean isSending() {
		return sending;
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

	/**
	 * Send GPS sample.
	 */
	public void sendPoint(JXElement aPoint, int theCount) {
		JXElement req = new JXElement("t-trk-write-req");

		req.addChild(aPoint);

		try {
			listener.onNetStatus("sending #" + theCount);
			JXElement rsp = kwClient.utopia(req);
			lastCommandTime = Util.getTime();
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

	/**
	 * Send GPS points.
	 */
	public void sendPoints(Vector thePoints, int theCount) {
		JXElement req = new JXElement("t-trk-write-req");
		req.addChildren(thePoints);

		try {
			listener.onNetStatus("sending #" + theCount);
			JXElement rsp = kwClient.utopia(req);
			lastCommandTime = Util.getTime();
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
			listener.onNetInfo("paused trkid=" + trackId);
		} catch (Throwable pe) {
			listener.onNetStatus("error");
		} finally {
			trackId = null;
		}
	}

	public JXElement uploadMedium(String aName, String aType, String aMime, long aTime, byte[] theData, boolean encode) {
		return uploadMedium(aName, aType, aMime, aTime, theData, encode, null);
	}

	/* public JXElement uploadMediumOld(String aName, String aType, String aMime, long aTime, byte[] theData, boolean encode, String theTags) {

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

		// Optional tags
		if (theTags != null) {
			uploadReq.setAttr("tags", theTags);
		}

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
	}  */

	public JXElement uploadMedium(String aName, String aType, String aMime, long aTime, byte[] theData, boolean encode, String theTags) {
		HTTPUploader uploader = new HTTPUploader();
		JXElement rsp = null;
		try {
			uploader.connect(url + "/media.srv");
			if (aName == null || aName.length() == 0) {
				aName = "unnamed " + aType;
			}

			uploader.writeField("agentkey", agentKey);
			uploader.writeField("name", aName);
			uploader.writeFile(aName, aMime, "mt-upload", theData);

			rsp = uploader.getResponse();
			if (Protocol.isNegativeResponse(rsp)) {
				return rsp;
			}

			// Upload OK, now add medium to track
			JXElement req = new JXElement("t-trk-add-medium-req");
			req.setAttr("id", rsp.getAttr("id"));
			req.setAttr("t", aTime);

			// Optional tags
			if (theTags != null && theTags.length() > 0) {
				req.setAttr("tags", theTags);
			}

			utopiaReq(req);

		} catch (Throwable t) {
			Log.log("Upload err: " + t);
		}
		return rsp;
	}

    public JXElement uploadMedium(String aName, String aDescription, String aType, String aMime, long aTime, byte[] theData, boolean encode) {
		HTTPUploader uploader = new HTTPUploader();
		JXElement rsp = null;
		try {
			uploader.connect(url + "/media.srv");
			if (aName == null || aName.length() == 0) {
				aName = "unnamed " + aType;
			}

			uploader.writeField("agentkey", agentKey);
			uploader.writeField("name", aName);
			uploader.writeField("description", aDescription);
			uploader.writeFile(aName, aMime, "mt-upload", theData);

			rsp = uploader.getResponse();

        } catch (Throwable t) {
			Log.log("Upload err: " + t);
		}
		return rsp;
	}


    public JXElement utopiaReq(JXElement aReq) {
		if (kwClient == null) {
			start();
		}

		JXElement rsp = null;
		try {
			sending = true;
			rsp = kwClient.utopia(aReq);
			lastCommandTime = Util.getTime();
		} catch (Throwable pe) {
			kwClient = null;
		} finally {
			sending = false;
		}

		return rsp;
	}


	public void start() {
		try {
			// traceScreen.hideCommands();
			kwClient = new HTTPClient(url + "/proto.srv");
			//kwClient = new TCPClient();
			//kwClient.connect("localhost", 4414);
			JXElement rsp = kwClient.login(user, password);
			if (rsp.hasAttr("time")) {
				Util.setTime(rsp.getLongAttr("time"));
			}

			if (rsp.hasAttr(HTTPClient.ATTR_AGENTKEY)) {
				agentKey = rsp.getAttr(HTTPClient.ATTR_AGENTKEY);
			}
			kwClient.selectApp(app, role);
			lastCommandTime = Util.getTime();
			listener.onNetStatus("login OK");
			listener.onNetInfo("login OK user=" + user + "\n" + "server=" + url + "\n" + "timeoffset=" + (Util.getTimeOffset() / 1000) + " sec");
			startHeartbeat();
		} catch (Throwable pe) {
			listener.onNetStatus("cannot login");
			listener.onNetInfo("LOGIN FAILED\nreason: \n" + pe.getMessage());
			kwClient = null;
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
