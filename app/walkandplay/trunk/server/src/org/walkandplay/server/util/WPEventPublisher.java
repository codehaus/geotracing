// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.walkandplay.server.util;

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
import org.geotracing.handler.Location;
import org.geotracing.handler.Track;
import org.geotracing.handler.CommentLogic;

import java.util.Iterator;
import java.util.Vector;

/**
 * Publishes all events to clients using Pushlet Publisher.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class WPEventPublisher {
	/**
	 * Pushlet topic (subject)
	 */
	public static final String PUSHLET_SUBJECT = "/wp";
	public static final String PERSON_SUBJECT = "/person/";
	public static final String GAME_PLAY_SUBJECT = PUSHLET_SUBJECT + "/play/";
	public static final String GAME_ROUND_SUBJECT = PUSHLET_SUBJECT + "/round/";
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

	public static final String FIELD_GAMEPLAY_ID = "gameplayid";
	public static final String FIELD_LON = "lon";
	public static final String FIELD_LAT = "lat";

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
	 * Publish new user location to Pushlet framework.
	 */
	public static void userMove(int aUserId, String aUserName, int aGameRoundId, int aGamePlayId, Point aPoint) {
		Log log = Logging.getLog(EVENT_USER_MOVE);

		try {

			// Pushlet subject (topic) is e.g. "/location/piet"
			Event event = Event.createDataEvent(GAME_ROUND_SUBJECT + aGameRoundId);
			event.setField(FIELD_EVENT, EVENT_USER_MOVE);
			event.setField(FIELD_USER_ID, aUserId);
			event.setField(FIELD_USER_NAME, aUserName);
			event.setField(FIELD_GAMEPLAY_ID, aGamePlayId);
			event.setField(FIELD_LON, aPoint.x+"");
			event.setField(FIELD_LAT, aPoint.y+"");

			multicast(event);
			log.info("USER MOVE: " + event);
		} catch (Throwable t) {
			log.warn("Cannot publish user-move", t);
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




