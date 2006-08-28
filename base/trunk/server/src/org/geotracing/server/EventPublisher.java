// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.server;

import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.pushlet.core.Dispatcher;
import nl.justobjects.pushlet.core.Event;
import org.geotracing.gis.proj.WGS84toRD;
import org.geotracing.gis.proj.XY;
import org.geotracing.gis.GeoPoint;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.utopia.core.data.Account;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.utopia.core.data.Medium;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaSessionContext;
import org.keyworx.utopia.core.util.Oase;

import java.util.Iterator;
import java.util.Vector;

/**
 * Publishes all events to clients using Pushlet Publisher.
 *
 * Redirects the requests to the right logic method
 *
 * @author Just van den Broecke
 * @version $Id: EventPublisher.java,v 1.15 2006-08-10 16:13:17 just Exp $
 */
public class EventPublisher {
	/** Pushlet topic (subject) */
	public static final String PUSHLET_SUBJECT = "/gt";
	public static final String FIELD_EVENT = "event";
	public static final String FIELD_ID = "id";
	public static final String FIELD_USER_ID = "userid";
	public static final String FIELD_TRACK_ID = "trackid";
	public static final String FIELD_TRACK_NAME = "trackname";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_KIND = "kind";
	public static final String FIELD_MIME = "mime";
	public static final String FIELD_STATE = "state";
	public static final String FIELD_TIME = "time";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_USER_NAME = "username";
	public static final String FIELD_OWNER_NAME = "ownername";
	public static final String FIELD_OWNER_ID = "ownerid";

	/** Event types. */
	public static final String EVENT_USER_HEARTBEAT = "user-hb";
	public static final String EVENT_USER_MOVE = "user-move";
	public static final String EVENT_MEDIUM_ADD = "medium-add";
	public static final String EVENT_POI_ADD = "poi-add";
	public static final String EVENT_POI_DELETE = "poi-delete";
	public static final String EVENT_POI_HIT = "poi-hit";
	public static final String EVENT_POI_UPDATE = "poi-update";
	public static final String EVENT_TRACK_CREATE = "track-create";
	public static final String EVENT_TRACK_DELETE = "track-delete";
	public static final String EVENT_TRACK_SUSPEND = "track-suspend";
	public static final String EVENT_TRACK_RESUME = "track-resume";

	/**
	 * Publish new medium with location added.
	 */
	public static void heartbeat(Track aTrack, long aTime, UtopiaRequest anUtopiaRequest) {
		Log log = Logging.getLog(anUtopiaRequest);
		String accountName = null;
		try {
			// Get account name for event subject
			// Expensive but we have to
			UtopiaSessionContext sc = anUtopiaRequest.getUtopiaSession().getContext();
			Oase oase = sc.getOase();
			Person person = (Person) oase.get(Person.class, sc.getUserId());
			Account account = person.getAccount();
			accountName = account.getLoginName();

			// Pushlet event
			Event event = Event.createDataEvent(PUSHLET_SUBJECT);
			event.setField(FIELD_EVENT, EVENT_USER_HEARTBEAT);
			event.setField(FIELD_ID, person.getId());
			event.setField(FIELD_USER_NAME, accountName);
			event.setField(FIELD_TIME, aTime);
            if (aTrack != null) {
				event.setField(FIELD_TRACK_NAME, aTrack.getName());
			}
			multicast(event);
			log.trace("Published: " + event);

		} catch (Throwable t) {
			log.warn("Cannot publish event " + EVENT_USER_HEARTBEAT + " for account=" + accountName, t);
		}
	}

	/**
	 * Publish new medium with location added.
	 */
	public static void mediumAdd(int aMediumId, Location aLocation, UtopiaRequest anUtopiaRequest) {
		Log log = Logging.getLog(anUtopiaRequest);

		try {
			// Get account name for event subject
			// Expensive but we have to
			UtopiaSessionContext sc = anUtopiaRequest.getUtopiaSession().getContext();
			Oase oase = sc.getOase();
			Medium medium = (Medium) oase.get(Medium.class, aMediumId + "");
			Person person = medium.getPerson(null);
			Account account = person.getAccount();
			String accountName = account.getLoginName();
			Track track = (Track) medium.getRelatedObject(Track.class);

			// Pushlet event
			Event event = Event.createDataEvent(PUSHLET_SUBJECT);
			event.setField(FIELD_EVENT, EVENT_MEDIUM_ADD);
			event.setField(FIELD_ID, aMediumId);
			event.setField(FIELD_NAME, medium.getName());
			event.setField(FIELD_KIND, medium.getKind());
			event.setField(FIELD_MIME, medium.getMime());
			event.setField(FIELD_TIME, medium.getCreationDate());
			event.setField(FIELD_USER_ID, person.getId());
			event.setField(FIELD_USER_NAME, accountName);

			event.setField(FIELD_TRACK_ID, track.getId());
			event.setField(FIELD_TRACK_NAME, track.getName());

			// Only if location supplied
			GeoPoint point = aLocation.getLocation();
			event.setField(Location.FIELD_LON, point.lon + "");
			event.setField(Location.FIELD_LAT, point.lat + "");
			event.setField(Location.FIELD_ELE, point.elevation + "");
			// Send pushlet event
			multicast(event);
			log.trace("Published: " + event);

		} catch (Throwable t) {
			log.warn("Cannot publish event " + EVENT_MEDIUM_ADD + " for mediumId=" + aMediumId, t);
		}
	}


	/**
	 * Publish new POI with location added.
	 */
	public static void poiAdd(POI aPOI, Location aLocation, UtopiaRequest anUtopiaRequest) {
		Log log = Logging.getLog(anUtopiaRequest);

		try {
			// Get account name for event subject
			// Expensive but we have to
			UtopiaSessionContext sc = anUtopiaRequest.getUtopiaSession().getContext();
			Oase oase = sc.getOase();
			Person person = (Person) oase.get(Person.class, sc.getUserId());
			Account account = person.getAccount();
			String accountName = account.getLoginName();
			Track track = (Track) aPOI.getRelatedObject(Track.class);

			// Pushlet event
			Event event = Event.createDataEvent(PUSHLET_SUBJECT);
			event.setField(FIELD_EVENT, EVENT_POI_ADD);
			event.setField(FIELD_ID, aPOI.getId());
			event.setField(FIELD_NAME, aPOI.getStringValue(POI.FIELD_NAME));
			event.setField(FIELD_TYPE, aPOI.getStringValue(POI.FIELD_TYPE));
			event.setField(FIELD_STATE, aPOI.getIntValue(POI.FIELD_STATE));
			event.setField(FIELD_TIME, aPOI.getLongValue(POI.FIELD_TIME));
			event.setField(FIELD_USER_ID, person.getId());
			event.setField(FIELD_USER_NAME, accountName);

			event.setField(FIELD_TRACK_ID, track.getId());
			event.setField(FIELD_TRACK_NAME, track.getName());

			// Only if location supplied
			GeoPoint point = aLocation.getLocation();
			event.setField(Location.FIELD_LON, point.lon + "");
			event.setField(Location.FIELD_LAT, point.lat + "");
			event.setField(Location.FIELD_ELE, point.elevation + "");
			// Send pushlet event
			multicast(event);
			log.trace("Published: " + event);

		} catch (Throwable t) {
			log.warn("Cannot publish event " + EVENT_POI_ADD + " for poiId=" + aPOI.getId(), t);
		}
	}


	/**
	 * POI has been deleted.
	 */
	public static void mediumDelete(int aMediumId, int aTrackId, UtopiaRequest anUtopiaRequest) {
		// TODO implement
	}

	/**
	 * POI has been deleted.
	 */
	public static void poiDelete(int aPOIId, int aTrackId, UtopiaRequest anUtopiaRequest) {
		Log log = Logging.getLog(anUtopiaRequest);

		try {
			// Get account name for event subject
			// Expensive but we have to
			UtopiaSessionContext sc = anUtopiaRequest.getUtopiaSession().getContext();
			Oase oase = sc.getOase();
			Person person = (Person) oase.get(Person.class, sc.getUserId());
			Account account = person.getAccount();
			String accountName = account.getLoginName();

			// Pushlet event
			Event event = Event.createDataEvent(PUSHLET_SUBJECT);
			event.setField(FIELD_EVENT, EVENT_POI_DELETE);
			event.setField(FIELD_ID, aPOIId);
			Track track = (Track) oase.get(Track.class, aTrackId + "");
			event.setField(FIELD_TRACK_ID, track.getId());
			event.setField(FIELD_TRACK_NAME, track.getName());

			event.setField(FIELD_USER_ID, person.getId());
			event.setField(FIELD_USER_NAME, accountName);

			multicast(event);
			log.trace("Published: " + event);

		} catch (Throwable t) {
			log.warn("Cannot publish event " + EVENT_POI_DELETE + " for poi=" + aPOIId + " trackId=" + aTrackId, t);
		}
	}

	/**
	 * Publish new POI with location added.
	 */
	public static void poiHit(int aPOIid, UtopiaRequest anUtopiaRequest) {
		Log log = Logging.getLog(anUtopiaRequest);
        POI poi = null;
		try {
			// Get account name for event subject
			// Expensive but we have to
			UtopiaSessionContext sc = anUtopiaRequest.getUtopiaSession().getContext();
			Oase oase = sc.getOase();
			Person person = (Person) oase.get(Person.class, sc.getUserId());
			Account account = person.getAccount();
			poi = (POI) oase.get(POI.class, aPOIid + "");

			Location location = (Location) poi.getRelatedObject(Location.class);
			Track track = (Track) poi.getRelatedObject(Track.class);
			Person poiOwnerPerson = (Person) track.getRelatedObject(Person.class);
			Account poiOwnerAccount = poiOwnerPerson.getAccount();

			// Pushlet event
			Event event = Event.createDataEvent(PUSHLET_SUBJECT);
			event.setField(FIELD_EVENT, EVENT_POI_HIT);
			event.setField(FIELD_ID, poi.getId());
			event.setField(FIELD_NAME, poi.getStringValue(POI.FIELD_NAME));
			event.setField(FIELD_TYPE, poi.getStringValue(POI.FIELD_TYPE));
			event.setField(FIELD_STATE, poi.getIntValue(POI.FIELD_STATE));
			event.setField(FIELD_TIME, poi.getLongValue(POI.FIELD_TIME));
			event.setField(FIELD_USER_ID, person.getId());
			event.setField(FIELD_USER_NAME, account.getLoginName());
			event.setField(FIELD_OWNER_ID, poiOwnerPerson.getId());
			event.setField(FIELD_OWNER_NAME, poiOwnerAccount.getLoginName());

			event.setField(FIELD_TRACK_ID, track.getId());
			event.setField(FIELD_TRACK_NAME, track.getName());

			// Only if location supplied
			GeoPoint point = location.getLocation();
			event.setField(Location.FIELD_LON, point.lon + "");
			event.setField(Location.FIELD_LAT, point.lat + "");
			event.setField(Location.FIELD_ELE, point.elevation + "");
			// Send pushlet event
			multicast(event);
			log.trace("Published: " + event);

		} catch (Throwable t) {
			log.warn("Cannot publish event " + EVENT_POI_HIT + " for poiId=" + poi.getId(), t);
		}
	}

	/**
	 * Publish new Track added.
	 */
	public static void trackCreate(int aTrackId, UtopiaRequest anUtopiaRequest) {
		trackEvent(EVENT_TRACK_CREATE, aTrackId, anUtopiaRequest);
	}

	/**
	 * Publish Track deleted.
	 */
	public static void trackDelete(int aTrackId, UtopiaRequest anUtopiaRequest) {
		trackEvent(EVENT_TRACK_DELETE, aTrackId, anUtopiaRequest);

	}

	/**
	 * Publish Track suspended.
	 */
	public static void trackSuspend(int aTrackId, UtopiaRequest anUtopiaRequest) {
		trackEvent(EVENT_TRACK_SUSPEND, aTrackId, anUtopiaRequest);
	}


	/**
	 * Publish Track resumed.
	 */
	public static void trackResume(int aTrackId, UtopiaRequest anUtopiaRequest) {
		trackEvent(EVENT_TRACK_RESUME, aTrackId, anUtopiaRequest);
	}

	/**
	 * General track event.
	 */
	public static void trackEvent(String anEvent, int aTrackId, UtopiaRequest anUtopiaRequest) {
		Log log = Logging.getLog(anUtopiaRequest);

		try {
			// Get account name for event subject
			// Expensive but we have to
			UtopiaSessionContext sc = anUtopiaRequest.getUtopiaSession().getContext();
			Oase oase = sc.getOase();
			Person person = (Person) oase.get(Person.class, sc.getUserId());
			Account account = person.getAccount();
			String accountName = account.getLoginName();

			// Pushlet event
			Event event = Event.createDataEvent(PUSHLET_SUBJECT);
			event.setField(FIELD_EVENT, anEvent);
			event.setField(FIELD_ID, aTrackId);

			// Track name only available if not deleted
			if (!anEvent.equals(EVENT_TRACK_DELETE)) {
				Track track = (Track) oase.get(Track.class, aTrackId + "");
				event.setField(FIELD_NAME, track.getName());
			}

			event.setField(FIELD_USER_ID, person.getId());
			event.setField(FIELD_USER_NAME, accountName);

			multicast(event);
			log.trace("Published: " + event);

		} catch (Throwable t) {
			log.warn("Cannot publish event " + anEvent + " for trackId=" + aTrackId, t);
		}
	}

	/**
	 * Publish new tracer location to Pushlet framework.
	 */
	public static void tracerMove(Track aTrack, Vector theElements, UtopiaRequest anUtopiaRequest) {
		Log log = Logging.getLog(anUtopiaRequest);

		try {
			// Get account name for event subject
			// Expensive but we have to
			UtopiaSessionContext sc = anUtopiaRequest.getUtopiaSession().getContext();
			Oase oase = sc.getOase();
			Person person = (Person) oase.get(Person.class, sc.getUserId());
			Account account = person.getAccount();
			String accountName = account.getLoginName();
            String trackName = aTrack.getName();
		    int trackId = aTrack.getId();

			JXElement nextElement = null;
			for (int i = 0; i < theElements.size(); i++) {
				nextElement = (JXElement) theElements.get(i);

				// We want to publish only points
				if (!nextElement.getTag().equals(Track.TAG_PT)) {
					continue;
				}

				// Pushlet subject (topic) is e.g. "/location/piet"
				Event event = Event.createDataEvent(PUSHLET_SUBJECT);
				event.setField(FIELD_EVENT, EVENT_USER_MOVE);
				event.setField(FIELD_ID, person.getId());
				event.setField(FIELD_USER_NAME, accountName);
                event.setField(FIELD_TRACK_ID, trackId);
                event.setField(FIELD_TRACK_NAME, trackName);

				// For now just copy (almost) all attrs
				String nextField = null;
				for (Iterator iter = nextElement.getAttrs().keys(); iter.hasNext();) {
					nextField = (String) iter.next();

					// Skip raw GPS data
					if (nextField.equals(Track.ATTR_NMEA)) {
						continue;
					}

					event.setField(nextField, nextElement.getAttr(nextField));

					// Calculate RD coord X,Y for backward compat
					// XY rd = WGS84toRD.calculate(nextElement.getAttr(Track.ATTR_LAT), nextElement.getAttr(Track.ATTR_LON));
					// event.setField("x", rd.x);
					// event.setField("y", rd.y);
				}

				multicast(event);
				log.trace("Published: " + event);
			}
		} catch (Throwable t) {
			log.warn("Cannot publish geo elements", t);
		}
	}

	/**
	 * Publish Event to Pushlet framework.
	 */
	private static void multicast(Event anEvent) {
		// Add time attr if not present
		if (anEvent.getField(FIELD_TIME) == null) {
			anEvent.setField(FIELD_TIME, System.currentTimeMillis());		
		}
		Dispatcher.getInstance().multicast(anEvent);
	}
}

/*
* $Log: EventPublisher.java,v $
* Revision 1.15  2006-08-10 16:13:17  just
* added timnestamp always
*
* Revision 1.14  2006-04-18 14:38:45  just
* added delete medium service
*
* Revision 1.13  2006-04-04 15:15:33  just
* fix invalid gps sample writing
*
* Revision 1.12  2006/01/06 22:26:22  just
* *** empty log message ***
*
* Revision 1.11  2005/11/21 14:11:16  just
* *** empty log message ***
*
* Revision 1.10  2005/10/31 23:15:50  just
* *** empty log message ***
*
* Revision 1.9  2005/10/21 20:49:20  just
* *** empty log message ***
*
* Revision 1.8  2005/10/20 09:12:04  just
* *** empty log message ***
*
* Revision 1.7  2005/10/19 09:39:22  just
* *** empty log message ***
*
* Revision 1.6  2005/10/18 15:23:50  just
* *** empty log message ***
*
* Revision 1.5  2005/10/18 12:54:44  just
* *** empty log message ***
*
* Revision 1.4  2005/10/18 07:38:00  just
* *** empty log message ***
*
* Revision 1.3  2005/10/13 14:19:21  just
* *** empty log message ***
*
* Revision 1.2  2005/10/09 14:34:16  just
* *** empty log message ***
*
* Revision 1.1  2005/10/07 15:23:09  just
* *** empty log message ***
*

*
*/



