package org.geotracing.server;

import org.keyworx.client.KWClient;
import org.keyworx.amuse.core.Protocol;
import org.keyworx.oase.util.Net;
import org.geotracing.gis.GPSDecoder;
import org.geotracing.gis.GPSSample;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import nl.justobjects.pushlet.core.Event;
import nl.justobjects.pushlet.core.EventParser;

import java.util.Vector;
import java.util.ArrayList;
import java.util.Date;
import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;

public class MigrateTracks {
	private long startTime;
	private String routesDir;
	private String sourceURL = "http://test.geoskating.com/gs/";
	private String mediaURL = sourceURL + "media.srv";
	private KWClient kwClient;

	public static void main(String[] args) {
		new MigrateTracks().go();
	}

	public MigrateTracks() {
		startTime = System.currentTimeMillis();
		routesDir = "/var/keyworx/webapps/test.geoskating.com/rsc/routes/";

	}

	public void go() {
		startSession();
		processRouteFiles();
		processMedia();
		p("ALL DONE");

	}

	public void startSession() {
		try {
			kwClient = new KWClient();
			kwClient.setDebug(false);
			kwClient.connect("127.0.0.1", 4042);
			kwClient.login("just", "");
			kwClient.selectApp("geoapp", "user");

			JXElement req = Protocol.createRequest("query-store");
			req.setAttr("tables", "g_track");
			JXElement rsp = kwClient.performUtopiaRequest(req);
			if (Protocol.isNegativeResponse(rsp)) {
				p("Negative query response: " + rsp.toFormattedString());
				return;
			}

			Vector tracks = rsp.getChildren();
			for (int i = 0; i < tracks.size(); i++) {
				JXElement track = (JXElement) tracks.get(i);
				p("deleting " + track.getChildText("name"));
				req = Protocol.createRequest("t-trk-delete");
				req.setAttr("id", track.getChildText("id"));
				rsp = kwClient.performUtopiaRequest(req);
				if (Protocol.isNegativeResponse(rsp)) {
					p("Negative delete response: " + rsp.toFormattedString());
				}
			}
		} catch (Throwable t) {
			e("error creating kwClient", t);
		}
		p("CREATE SESSION DONE");
	}

	public void processMedia() {
		try {
			JXElement mediaElement = new JXBuilder().build(new URL(sourceURL + "media.jsp"));
			Vector media = mediaElement.getChildren();
			p("processing " + media.size() + " media files");
			for (int i = 0; i < media.size(); i++) {
				JXElement medium = (JXElement) media.get(i);
				String fileName = medium.getChildText("filename");
				int id = medium.getIntAttr("id");

			//	if (id >= 2095 && id <= 2128) {
					p("processing " + id);
					String fileURL = mediaURL + "?id=" + id;
					File file = new File("/tmp/" + fileName);
					Net.fetchURL(fileURL, file);
					p("file: " + file.length() + " bytes");
					JXElement req = Protocol.createRequest("t-trk-upload-medium");
					req.setAttr("type", medium.getChildText("kind"));
					req.setAttr("mime", medium.getChildText("mime"));
					req.setAttr("file", file.getAbsolutePath());
					req.setAttr("name", medium.getChildText("name"));
					if (medium.getChildText("description") != null) {
						req.setAttr("description", medium.getChildText("description"));
					}
					req.setAttr("t", medium.getChildText("creationdate"));
					p("utopia-req " + req.toFormattedString());
					JXElement rsp = kwClient.performUtopiaRequest(req);
					if (Protocol.isNegativeResponse(rsp)) {
						p("Negative response: " + rsp.toFormattedString());
					}
					Thread.sleep(50);

	//			}
			}
		} catch (Throwable t) {
			e("error processing media", t);
		}
		p("MEDIA DONE");
	}

	public void submitTrack(TrackEvent[] trackEvents) {
		p("submitting track eventCount=" + trackEvents.length);

		try {
			JXElement rsp;
			for (int i = 0; i < trackEvents.length; i++) {
				// p(trackEvents[i].toTracingReq().toFormattedString());
				rsp = kwClient.performUtopiaRequest(trackEvents[i].toTracingReq());
				if (Protocol.isNegativeResponse(rsp)) {
					p("Negative response: " + rsp.toFormattedString());
				}
				Thread.sleep(50);
			}

		} catch (Throwable t) {
			e("error submitting track", t);
		}

	}

	public void processRouteFiles() {
		File[] routeFiles = new File(routesDir).listFiles();
		p("processing " + routeFiles.length + " route files");
		double totalDist = 0d;
		TrackEvent[] trackEvents;
		for (int i = 0; i < routeFiles.length; i++) {

			String routeFileName = routeFiles[i].getName();
			if (!routeFileName.startsWith("georoute")) {
				p("skipping " + routeFileName);
				continue;
			}

		//	if (routeFileName.indexOf("050804") != -1) {
				submitTrack(processRouteFile(routeFiles[i]));
		//	}
		}

		p("ROUTES DONE");
	}

	public TrackEvent[] processRouteFile(File aRouteFile) {
		ArrayList trackEvents = new ArrayList(512);
		String routeFileName = aRouteFile.getName();

		p("processing " + routeFileName);

		try {
			FileInputStream fis = new FileInputStream(aRouteFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			DataInputStream dis = new DataInputStream(bis);
			String line = null;
			Event event = null;
			TrackEvent trackEvent = null;
			TrackEvent trackResumeEvent = null;
			TrackEvent trackCreateEvent = null;
			String gpsData = null;
			String dateStr = routeFileName.substring(routeFileName.indexOf('-') + 1, routeFileName.indexOf('.'));
			GPSDecoder.setStartDate(dateStr);
			String trackName = dateStr;
			p("trackName=" + trackName);
			while ((line = dis.readLine()) != null) {
				if (line.indexOf("<event") == -1) {
					continue;
				}
				event = EventParser.parse(line);
				if (event.getField("cmd") != null) {
					// p("cmd=" + event.getField("cmd"));
					trackResumeEvent = new TrackEvent("t-trk-resume-req", 0);
					continue;
				}
				gpsData = event.getField("data");
				if (gpsData == null) {
					continue;
				}
				if (gpsData.indexOf("START") != -1) {
					trackResumeEvent = new TrackEvent("t-trk-resume-req", 0);
					continue;
				}

				GPSSample sample = GPSDecoder.parseSample(gpsData);
				if (sample == null) {
					continue;
				}

				int roadRating = Integer.parseInt(event.getField("ar", "0"));
				int sceneryRating = Integer.parseInt(event.getField("sr", "0"));
				long time = sample.timestamp; // Integer.parseInt(event.getField("p_time", "0"));
				trackEvent = new TrackEvent(gpsData, time, roadRating, sceneryRating);
				if (trackCreateEvent == null) {
					trackCreateEvent = new TrackEvent("t-trk-create-req", trackName);
					trackCreateEvent.time = time - 11;
					trackEvents.add(trackCreateEvent);
					p(trackCreateEvent.toTracingReq().toFormattedString() + " start=" + new Date(trackCreateEvent.time));
				}
				if (trackResumeEvent != null) {
					trackResumeEvent.time = time - 10;
					trackEvents.add(trackResumeEvent);
					p(trackResumeEvent.toTracingReq().toFormattedString());
					trackResumeEvent = null;
				}
				// p(trackEvent.toTracingReq().toFormattedString());

				trackEvents.add(trackEvent);
			}


		} catch (Throwable t) {
			e("error processing route file", t);
		}
		p("DONE: processing " + routeFileName + " eventCount=" + trackEvents.size());
		return (TrackEvent[]) trackEvents.toArray(new TrackEvent[trackEvents.size()]);

	}

	public static void p(String s) {
		System.out.println(s);
	}

	public static void e(String s, Throwable t) {
		p("ERROR " + s + " t=" + t);
		t.printStackTrace();
	}

	private static class TrackEvent {
		public String req;
		public String name;
		public String nmeaData;
		public long time;
		public int roadRating;
		public int scenaryRating;

		public TrackEvent(String theNmeaData, long aTime, int aRoadRating, int aScenaryRating) {
			nmeaData = theNmeaData;
			time = aTime;
			roadRating = aRoadRating;
			scenaryRating = aScenaryRating;
		}

		public TrackEvent(String theReq, long aTime) {
			req = theReq;
			time = aTime;

		}

		public TrackEvent(String theReq, String aName) {
			req = theReq;
			name = aName;

		}

		public JXElement toTracingReq() {
			JXElement result = null;
			if (req == null) {
				result = new JXElement("t-trk-write-req");
				JXElement pt = new JXElement("pt");
				pt.setAttr("nmea", nmeaData);
				pt.setAttr("t", time);
				pt.setAttr("rr", roadRating);
				pt.setAttr("sr", scenaryRating);
				result.addChild(pt);
			} else {
				result = new JXElement(req);
				result.setAttr("t", time);
				if (name != null) {
					result.setAttr("type", 1);
					result.setAttr("name", name);
				}
				// result.setAttr("date", new Date(time).toString());
			}
			return result;
		}
	}
}
