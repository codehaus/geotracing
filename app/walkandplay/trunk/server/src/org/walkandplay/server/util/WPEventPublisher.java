// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.walkandplay.server.util;

import nl.justobjects.pushlet.core.Dispatcher;
import nl.justobjects.pushlet.core.Event;
import org.geotracing.handler.CommentLogic;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.utopia.core.util.Oase;
import org.postgis.Point;

import java.util.List;
import java.util.ArrayList;

/**
 * Publishes all events to clients using Pushlet Publisher.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class WPEventPublisher {
	/**
	 * Event types.
	 */
	//public static final String EVENT_USER_HEARTBEAT = "user-hb";
	public static final String EVENT_PLAY_START = "play-start";
	public static final String EVENT_PLAY_FINISH = "play-finish";
	public static final String EVENT_USER_MOVE = "user-move";
	public static final String EVENT_TASK_DONE = "task-done";
	public static final String EVENT_TASK_HIT = "task-hit";
	public static final String EVENT_MEDIUM_HIT = "medium-hit";
	public static final String EVENT_COMMENT_ADD = "comment-add";
	public static final String EVENT_MEDIUM_ADD = "medium-add";
	public static final String EVENT_ANSWER_SUBMIT = "answer-submit";

	/**
	 * Pushlet topic (subject)
	 */
	public static final String PUSHLET_SUBJECT = "/wp";
	public static final String PERSON_SUBJECT = "/person/";
	public static final String GAME_PLAY_SUBJECT = PUSHLET_SUBJECT + "/play/";
	public static final String GAME_ROUND_SUBJECT = PUSHLET_SUBJECT + "/round/";
	public static final String FIELD_EVENT = "event";
	public static final String FIELD_ANSWER = "answer";
	public static final String FIELD_ANSWER_STATE= "answerstate";
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
	public static final String FIELD_GAMEROUND_ID = "roundid";
	public static final String FIELD_TASK_ID = "taskid";
	public static final String FIELD_TASKRESULT_ID = "taskresultid";
	public static final String FIELD_MEDIUM_ID = "mediumid";
	public static final String FIELD_MEDIUMRESULT_ID = "mediumresultid";
	public static final String FIELD_LON = "lon";
	public static final String FIELD_LAT = "lat";
	public static final String FIELD_SCORE = "score";


	protected static Log log;

	static {
		log = Logging.getLog("eventpublisher");
	}

	/**
	 * Publish answer submit event to Pushlet framework.
	 */
	public static Event answerSubmit(int aUserId, String aUserName, int aGameRoundId, int aGamePlayId, int aTaskId, int aTaskResultId, String theAnswer, String theAnswerState) {

		// Pushlet subject (topic) is e.g. "/location/piet"
		Event event = Event.createDataEvent(GAME_PLAY_SUBJECT + aGamePlayId);
		try {

			event.setField(FIELD_EVENT, EVENT_ANSWER_SUBMIT);
			event.setField(FIELD_USER_ID, aUserId);
			event.setField(FIELD_USER_NAME, aUserName);
			event.setField(FIELD_GAMEROUND_ID, aGameRoundId);
			event.setField(FIELD_GAMEPLAY_ID, aGamePlayId);
			event.setField(FIELD_TASK_ID, aTaskId);
			event.setField(FIELD_TASKRESULT_ID, aTaskResultId);
			event.setField(FIELD_ANSWER, theAnswer);
			event.setField(FIELD_ANSWER_STATE, theAnswerState);

			multicast(event);
		} catch (Throwable t) {
			log.warn("Cannot publish answerSubmit", t);
		}
		return event;
	}

	/**
	 * Publish comment added event.
	 */
	public static Event[] commentAdd(Record aCommentRecord, Oase anOase) {
		int commentId = aCommentRecord.getId();
		List events = new ArrayList(2);

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
				events.add(event);
			}

		} catch (Throwable t) {
			log.warn("Cannot publish event " + EVENT_COMMENT_ADD + " for commentId=" + commentId, t);
		}
		return (Event[]) events.toArray(new Event[events.size()]);
	}


	/**
	 * Publish medium submit for trace to Pushlet framework.
	 */
	public static Event mediumAdd(int aUserId, String aUserName, int aGameRoundId, int aGamePlayId, int theMediumId, String theMediumType) {
		 return mediumAdd(aUserId, aUserName, aGameRoundId, aGamePlayId, theMediumId, theMediumType, -1, -1);
	}

	/**
	 * Publish medium submit to Pushlet framework, optional task.
	 */
	public static Event mediumAdd(int aUserId, String aUserName, int aGameRoundId, int aGamePlayId, int theMediumId, String theMediumType, int aTaskId, int aTaskResultId) {

		// Pushlet subject (topic) is e.g. "/location/piet"
		Event event = Event.createDataEvent(GAME_PLAY_SUBJECT + aGamePlayId);
		try {

			event.setField(FIELD_EVENT, EVENT_MEDIUM_ADD);
			event.setField(FIELD_USER_ID, aUserId);
			event.setField(FIELD_USER_NAME, aUserName);
			event.setField(FIELD_GAMEROUND_ID, aGameRoundId);
			event.setField(FIELD_GAMEPLAY_ID, aGamePlayId);
			event.setField(FIELD_MEDIUM_ID, theMediumId);
			event.setField(FIELD_TYPE, theMediumType);

			// Optional if medium was also part of answering task
			if (aTaskId > 0) {
				event.setField(FIELD_TASK_ID, aTaskId);
				event.setField(FIELD_TASKRESULT_ID, aTaskResultId);
			}

			multicast(event);
		} catch (Throwable t) {
			log.warn("Cannot publish mediumAdd", t);
		}
		return event;
	}

	/**
	 * Publish task hit event to Pushlet framework.
	 */
	public static Event mediumHit(int aUserId, String aUserName, int aGameRoundId, int aGamePlayId, int aMediumId, int aMediumResultId) {
		// Pushlet subject (topic) is e.g. "/location/piet"
		Event event = Event.createDataEvent(GAME_PLAY_SUBJECT + aGamePlayId);

		try {

			event.setField(FIELD_EVENT, EVENT_MEDIUM_HIT);
			event.setField(FIELD_USER_ID, aUserId);
			event.setField(FIELD_USER_NAME, aUserName);
			event.setField(FIELD_GAMEROUND_ID, aGameRoundId);
			event.setField(FIELD_GAMEPLAY_ID, aGamePlayId);
			event.setField(FIELD_MEDIUM_ID, aMediumId);
			event.setField(FIELD_MEDIUMRESULT_ID, aMediumResultId);

			multicast(event);
		} catch (Throwable t) {
			log.warn("Cannot publish taskHit", t);
		}
		return event;
	}

	/**
	 * Publish user starts gameplay.
	 */
	public static Event playFinish(int aUserId, String aUserName, int aGameRoundId, int aGamePlayId) {

		// Pushlet subject (topic) is e.g. "/location/piet"
		Event event = Event.createDataEvent(GAME_ROUND_SUBJECT + aGameRoundId);

		try {

			event.setField(FIELD_EVENT, EVENT_PLAY_FINISH);
			event.setField(FIELD_USER_ID, aUserId);
			event.setField(FIELD_USER_NAME, aUserName);
			event.setField(FIELD_GAMEROUND_ID, aGameRoundId);
			event.setField(FIELD_GAMEPLAY_ID, aGamePlayId);

			multicast(event);
		} catch (Throwable t) {
			log.warn("Cannot publish playFinish", t);
		}
		return event;
	}

	/**
	 * Publish user starts gameplay.
	 */
	public static Event playStart(int aUserId, String aUserName, int aGameRoundId, int aGamePlayId) {
		// Pushlet subject (topic) is e.g. "/location/piet"
		Event event = Event.createDataEvent(GAME_ROUND_SUBJECT + aGameRoundId);

		try {

			event.setField(FIELD_EVENT, EVENT_PLAY_START);
			event.setField(FIELD_USER_ID, aUserId);
			event.setField(FIELD_USER_NAME, aUserName);
			event.setField(FIELD_GAMEROUND_ID, aGameRoundId);
			event.setField(FIELD_GAMEPLAY_ID, aGamePlayId);

			multicast(event);
		} catch (Throwable t) {
			log.warn("Cannot publish playStart", t);
		}
		return event;
	}

	/**
	 * Publish new user location to Pushlet framework.
	 */
	public static Event taskDone(int aUserId, String aUserName, int aGameRoundId, int aGamePlayId, int aTaskId, int aTaskResultId, int aScore) {
		// Pushlet subject (topic) is e.g. "/location/piet"
		Event event = Event.createDataEvent(GAME_ROUND_SUBJECT + aGameRoundId);

		try {

			event.setField(FIELD_EVENT, EVENT_TASK_DONE);
			event.setField(FIELD_USER_ID, aUserId);
			event.setField(FIELD_USER_NAME, aUserName);
			event.setField(FIELD_GAMEROUND_ID, aGameRoundId);
			event.setField(FIELD_GAMEPLAY_ID, aGamePlayId);
			event.setField(FIELD_TASK_ID, aTaskId);
			event.setField(FIELD_TASKRESULT_ID, aTaskResultId);
			event.setField(FIELD_SCORE, aScore);

			multicast(event);
		} catch (Throwable t) {
			log.warn("Cannot publish taskDone", t);
		}
		return event;
	}


	/**
	 * Publish task hit event to Pushlet framework.
	 */
	public static Event taskHit(int aUserId, String aUserName, int aGameRoundId, int aGamePlayId, int aTaskId, int aTaskResultId) {
		// Pushlet subject (topic) is e.g. "/location/piet"
		Event event = Event.createDataEvent(GAME_PLAY_SUBJECT + aGamePlayId);

		try {

			event.setField(FIELD_EVENT, EVENT_TASK_HIT);
			event.setField(FIELD_USER_ID, aUserId);
			event.setField(FIELD_USER_NAME, aUserName);
			event.setField(FIELD_GAMEROUND_ID, aGameRoundId);
			event.setField(FIELD_GAMEPLAY_ID, aGamePlayId);
			event.setField(FIELD_TASK_ID, aTaskId);
			event.setField(FIELD_TASKRESULT_ID, aTaskResultId);

			multicast(event);
		} catch (Throwable t) {
			log.warn("Cannot publish taskHit", t);
		}
		return event;
	}

	/**
	 * Publish new user location to Pushlet framework.
	 */
	public static Event userMove(int aUserId, String aUserName, int aGameRoundId, int aGamePlayId, Point aPoint) {

		Event event = Event.createDataEvent(GAME_ROUND_SUBJECT + aGameRoundId);

		try {

			// Pushlet subject (topic) is e.g. "/location/piet"
			event.setField(FIELD_EVENT, EVENT_USER_MOVE);
			event.setField(FIELD_USER_ID, aUserId);
			event.setField(FIELD_USER_NAME, aUserName);
			event.setField(FIELD_GAMEROUND_ID, aGameRoundId);
			event.setField(FIELD_GAMEPLAY_ID, aGamePlayId);
			event.setField(FIELD_LON, aPoint.x + "");
			event.setField(FIELD_LAT, aPoint.y + "");

			multicast(event);
		} catch (Throwable t) {
			log.warn("Cannot publish user-move", t);
		}
		return event;
	}

	/**
	 * Publish Event to Pushlet framework.
	 */
	private static void multicast(Event anEvent) {
		// log.info("EVENT: " + anEvent.getSubject() + " " + anEvent.getField(FIELD_EVENT));

		// Add time attr if not present
		if (anEvent.getField(FIELD_TIME) == null) {
			anEvent.setField(FIELD_TIME, System.currentTimeMillis());
		}
		Dispatcher.getInstance().multicast(anEvent);

	}
}




