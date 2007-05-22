// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.handler;

import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.pushlet.core.Dispatcher;
import nl.justobjects.pushlet.core.Event;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.Account;
import org.keyworx.utopia.core.data.Medium;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaSessionContext;
import org.keyworx.utopia.core.util.Oase;
import org.postgis.Point;

import java.util.Iterator;
import java.util.Vector;

/**
 * Publishes all events to clients using Pushlet Publisher.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class EventPublisher {
	/**
	 * Pushlet topic (subject)
	 */
	public static final String PUSHLET_SUBJECT = "/gt";
	public static final String PERSON_SUBJECT = "/person/";
	public static final String FIELD_EVENT = "event";
	public static final String FIELD_ID = "id";
	public static final String FIELD_USER_ID = "userid";
	public static final String FIELD_TARGET = "target";
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

	/**
	 * Event types.
	 */
	public static final String EVENT_USER_HEARTBEAT = "user-hb";
	public static final String EVENT_USER_MOVE = "user-move";
	public static final String EVENT_COMMENT_ADD = "comment-add";
	public static final String EVENT_MEDIUM_ADD = "medium-add";
	public static final String EVENT_TRACK_CREATE = "track-create";
	public static final String EVENT_TRACK_DELETE = "track-delete";
	public static final String EVENT_TRACK_SUSPEND = "track-suspend";
	public static final String EVENT_TRACK_RESUME = "track-resume";

	/**
	 * Publish comment added event.
	 */
	public static void commentAdd(Record aCommentRecord, Oase anOase) {
		Log log = Logging.getLog(EVENT_COMMENT_ADD);
		int commentId = aCommentRecord.getId();

		try {
			// Get account name for event subject
			// Expensive but we have to
			int ownerId = -1;
			String accountName = null;
			if (!aCommentRecord.isNull(CommentLogic.FIELD_OWNER)) {
				ownerId = aCommentRecord.getIntField(CommentLogic.FIELD_OWNER);
				Person ownerPerson = (Person) anOase.get(Person.class, ownerId + "");
				accountName = ownerPerson.getAccount().getLoginName();
			}

			// Send to
			// - owner of target (targetperson)
			// - to all users that have commented on this target (owners)
			int targetId = aCommentRecord.getIntField(CommentLogic.FIELD_TARGET);
			int targetPersonId = aCommentRecord.getIntField(CommentLogic.FIELD_TARGET_PERSON);
			int[] commenterIds = CommentLogic.getCommenterIds(anOase, targetId);

			for (int i = -1; i < commenterIds.length; i++) {
				// Include sending to owner of item that is commented
				int personId = (i == -1) ? targetPersonId : commenterIds[i];

				// Skip if owner and target person are the same
				// since owner will be in commenterIds[i] later on.
				if (i == -1 && ownerId == personId) {
					continue;
				}

				// Create and send Pushlet event

				// Subject is /person/id, i.e. only to specific person (if online)
				Event event = Event.createDataEvent(PERSON_SUBJECT + personId);
				event.setField(FIELD_EVENT, EVENT_COMMENT_ADD);
				event.setField(FIELD_ID, commentId);
				event.setField(FIELD_TARGET, targetId);

				// Only include owner if not anonymous commenting
				if (ownerId != -1) {
					event.setField(FIELD_USER_NAME, accountName);
					event.setField(FIELD_USER_ID, ownerId);
				}

				// Send pushlet event
				multicast(event);
				log.trace("Published: " + event);
			}

		} catch (Throwable t) {
			log.warn("Cannot publish event " + EVENT_COMMENT_ADD + " for commentId=" + commentId, t);
		}
	}

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

			// Send track info if medium is related to track
			Track track = (Track) medium.getRelatedObject(Track.class);
			if (track != null) {
				event.setField(FIELD_TRACK_ID, track.getId());
				event.setField(FIELD_TRACK_NAME, track.getName());
			}

			// Only if location supplied
			Point point = aLocation.getPoint();
			event.setField(Location.FIELD_LON, point.x + "");
			event.setField(Location.FIELD_LAT, point.y + "");
			event.setField(Location.FIELD_ELE, point.z + "");

			// Send pushlet event
			multicast(event);
			log.trace("Published: " + event);

		} catch (Throwable t) {
			log.warn("Cannot publish event " + EVENT_MEDIUM_ADD + " for mediumId=" + aMediumId, t);
		}
	}


	/**
	 * Medium has been deleted.
	 */
	public static void mediumDelete(int aMediumId, int aTrackId, UtopiaRequest anUtopiaRequest) {
		// TODO implement
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

				// Publish point for person/track
				tracerMove(person.getId(), accountName, trackId, trackName, nextElement);
			}
		} catch (Throwable t) {
			log.warn("Cannot publish geo elements", t);
		}
	}

	/**
	 * Publish new tracer location to Pushlet framework.
	 */
	public static void tracerMove(int aPersonId, String anAccountName, int aTrackId, String aTrackName, JXElement aPoint) {
		// Pushlet subject (topic) is e.g. "/location/piet"
		Event event = Event.createDataEvent(PUSHLET_SUBJECT);
		event.setField(FIELD_EVENT, EVENT_USER_MOVE);
		event.setField(FIELD_ID, aPersonId);
		event.setField(FIELD_USER_NAME, anAccountName);
		event.setField(FIELD_TRACK_ID, aTrackId);
		event.setField(FIELD_TRACK_NAME, aTrackName);
		// For now just copy (almost) all attrs
		String nextField = null;
		for (Iterator iter = aPoint.getAttrs().keys(); iter.hasNext();) {
			nextField = (String) iter.next();

			// Skip raw GPS data
			if (nextField.equals(Track.ATTR_NMEA)) {
				continue;
			}

			event.setField(nextField, aPoint.getAttr(nextField));
		}

		multicast(event);
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



