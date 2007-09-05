package org.walkandplay.server.logic;

import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.pushlet.core.Event;
import org.geotracing.gis.PostGISUtil;
import org.geotracing.handler.Location;
import org.geotracing.handler.Track;
import org.geotracing.handler.TrackLogic;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Sys;
import org.keyworx.oase.api.*;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.postgis.Point;
import org.walkandplay.server.util.Constants;
import org.walkandplay.server.util.WPEventPublisher;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

/**
 * Manage gamerounds.
 * <p/>
 * A GameRound ties players and gameplay t a specific game.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class GamePlayLogic implements Constants {
	private Oase oase;
	static private Log log = Logging.getLog("GamePlayLogic");

	public GamePlayLogic(Oase anOase) {
		oase = anOase;
	}

	public void delete(int aGamePlayId) throws OaseException, UtopiaException {
		delete(oase.getFinder().read(aGamePlayId, GAMEPLAY_TABLE));
	}

	public void delete(Record aGamePlay) throws OaseException, UtopiaException {
		reset(aGamePlay);
		oase.getModifier().delete(aGamePlay);
	}

	public JXElement addMedium(int aMediumId, long aTime) throws OaseException, UtopiaException {
/*
        <play-add-medium-req id="[mediumid]" />
        <play-add-medium-rsp locationid="[locationid]" taskresultid="[taskresultid]" />
*/

		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();

		// Create Location for medium and relate to track and location tables
		Record medium = oase.getFinder().read(aMediumId, MEDIUM_TABLE);

		// Person is person related to medium
		// not neccessarily the person logged in (e.g. admin for email upload)
		Record person = relater.getRelated(medium, PERSON_TABLE, null)[0];
		int personId = person.getId();

		// Determine timestamp: we use the time that the medium was sent
		long timestamp = Sys.now();
		if (aTime != -1) {
			// if a timestamp was provided we assume we already have the correct creation time
			timestamp = aTime;
		}

		// First determine medium location and add to track
		TrackLogic trackLogic = new TrackLogic(oase);

		// Adds medium to track and creates location object for timestamp
		// (where the player was at that time)
		Location location = trackLogic.createLocation(personId, aMediumId, timestamp, TrackLogic.REL_TAG_MEDIUM);

		// We either have location or an exception here
		JXElement result = new JXElement("medium-add");
		result.setAttr(LOCATION_ID_FIELD, location.getId());

		// We must have a running GamePlay record
		Record gamePlay = WPQueryLogic.getRunningGamePlay(personId);
		if (gamePlay == null) {
			throw new UtopiaException("No running GamePlay found for person=" + personId);
		}

		// Figure out if location is near a task
		Record game = WPQueryLogic.getGameForGamePlay(gamePlay.getId());
		Point point = location.getPoint();
		Record task = WPQueryLogic.getTaskHitForGame(point, game.getId());
		Record round = relater.getRelated(gamePlay, GAMEROUND_TABLE, null)[0];

		// Use account related to medium (request may come from admin when email medium upload)
		String accountName = relater.getRelated(person, "utopia_account", null)[0].getStringField("loginname"); //HandlerUtil.getAccountName(anUtopiaReq);
		String mediumType = medium.getStringField(KIND_FIELD);

		if (task != null) {
			log.trace("HIT task for medium add taskid=" + task.getId());

			// Store result of this task
			Record taskResult = WPQueryLogic.getTaskResultForTask(task.getId(), gamePlay.getId());
			if (taskResult == null) {
				throw new UtopiaException("No taskResult found for task=" + task.getId());
			}

			String totalState = taskResult.getStringField(STATE_FIELD);
			String answerState = taskResult.getStringField(ANSWER_STATE_FIELD);
			taskResult.setStringField(MEDIA_STATE_FIELD, VAL_DONE);

			// Unrelate possible previous medium
			Record[] mediaResults = relater.getRelated(taskResult, MEDIUM_TABLE, RELTAG_RESULT);
			for (int i = 0; i < mediaResults.length; i++) {
				relater.unrelate(taskResult, mediaResults[i]);
			}

			// Relate added medium
			relater.relate(taskResult, medium, RELTAG_RESULT);

			// Publish event with related task/taskresult
			storeEvent(oase, gamePlay, WPEventPublisher.mediumAdd(personId, accountName, round.getId(), gamePlay.getId(), aMediumId, mediumType, task.getId(), taskResult.getId()));

			// Set scores if we are totally done and task result was not already done
			if (answerState.equals(VAL_OK) && !totalState.equals(VAL_DONE)) {
				// ALL DONE
				boolean gameDone = finishTaskResult(oase, gamePlay, task, taskResult);

				// int aUserId, String aUserName, int aGameRoundId, int aGamePlayId, int aTaskId, int aTaskResultId, int aScore
				storeEvent(oase, gamePlay, WPEventPublisher.taskDone(personId, accountName, round.getId(), gamePlay.getId(), task.getId(), taskResult.getId(), task.getIntField(SCORE_FIELD)));
				totalState = VAL_DONE;
				if (gameDone) {
					// playFinish(int aUserId, String aUserName, int aGameRoundId, int aGamePlayId)
					storeEvent(oase, gamePlay, WPEventPublisher.playFinish(personId, accountName, round.getId(), gamePlay.getId()));
				}

			} else {
				modifier.update(taskResult);
			}


			result.setAttr(TASK_ID_FIELD, task.getId());
			result.setAttr(TASK_STATE_FIELD, totalState);
			result.setAttr(PLAY_STATE_FIELD, gamePlay.getStringField(STATE_FIELD));
		} else {
			// Medium is not part of answering task: just publish id's
			storeEvent(oase, gamePlay, WPEventPublisher.mediumAdd(personId, accountName, round.getId(), gamePlay.getId(), aMediumId, mediumType));
		}
		return result;
	}

	public JXElement answerTask(int aPersonId, int aTaskId, String anAnswer) throws OaseException, UtopiaException {
/*
        <play-answertask-req id="[taskid]" answer="blabla" />
        <play-answertask-rsp result="[boolean]" score="[nrofpoints] />
*/

		String playerAnswer = anAnswer.trim().toLowerCase();

		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();

		// We must have a running GamePlay record
		Record gamePlay = WPQueryLogic.getRunningGamePlay(aPersonId);
		if (gamePlay == null) {
			throw new UtopiaException("No running GamePlay found for person=" + aPersonId);
		}

		Record task = finder.read(aTaskId, TASK_TABLE);
		String[] answers = task.getStringField(ANSWER_FIELD).split(",");
		int score = 0;
		for (int i = 0; i < answers.length; i++) {
			if (answers[i].trim().toLowerCase().equals(playerAnswer)) {
				score = task.getIntField(SCORE_FIELD);
				break;
			}
		}

		// Store result of this task
		Record taskResult = WPQueryLogic.getTaskResultForTask(task.getId(), gamePlay.getId());
		if (taskResult == null) {
			throw new UtopiaException("No taskResult found for task=" + task.getId());
		}

		// Assemble results (3 states: total (totalState), answer and medium states)
		// Set answerstate from result
		String totalState = taskResult.getStringField(STATE_FIELD);
		String answerState = score > 0 ? VAL_OK : VAL_NOTOK;
		String mediaState = taskResult.getStringField(MEDIA_STATE_FIELD);

		// Always set outcome of answer
		taskResult.setField(ANSWER_FIELD, playerAnswer);
		taskResult.setField(ANSWER_STATE_FIELD, answerState);
		Record round = relater.getRelated(gamePlay, GAMEROUND_TABLE, null)[0];

		// Send event: answer submit
		Record person = finder.read(aPersonId, PERSON_TABLE);

		String accountName = relater.getRelated(person, ACCOUNT_TABLE, null)[0].getStringField(LOGINNAME_FIELD);
		storeEvent(oase, gamePlay, WPEventPublisher.answerSubmit(aPersonId, accountName, round.getId(), gamePlay.getId(), task.getId(), taskResult.getId(), playerAnswer, answerState));

		// Ok, set score only if also media was done and this task was not yet completed
		if (score > 0 && mediaState.equals(VAL_DONE) && !totalState.equals(VAL_DONE)) {
			// ALL DONE
			boolean gameDone = finishTaskResult(oase, gamePlay, task, taskResult);

			// Send event: task done
			// int aUserId, String aUserName, int aGameRoundId, int aGamePlayId, int aTaskId, int aTaskResultId, int aScore
			storeEvent(oase, gamePlay, WPEventPublisher.taskDone(aPersonId, accountName, round.getId(), gamePlay.getId(), task.getId(), taskResult.getId(), score));
			totalState = VAL_DONE;

			if (gameDone) {
				// Send event: gameplay done
				// playFinish(int aUserId, String aUserName, int aGameRoundId, int aGamePlayId)
				storeEvent(oase, gamePlay, WPEventPublisher.playFinish(aPersonId, accountName, round.getId(), gamePlay.getId()));
			}
		} else {
			// Store result
			modifier.update(taskResult);
		}

		// If scored but media state not done don't return score
		if (!mediaState.equals(VAL_DONE)) {
			score = 0;
		}

		// Send response
		JXElement result = new JXElement("answer-task");
		result.setAttr(STATE_FIELD, totalState);
		result.setAttr(MEDIA_STATE_FIELD, mediaState);
		result.setAttr(ANSWER_STATE_FIELD, answerState);
		result.setAttr(SCORE_FIELD, score);
		result.setAttr(PLAY_STATE_FIELD, gamePlay.getStringField(STATE_FIELD));
		return result;
	}

	public JXElement doLocation(int aPersonId, Vector thePointsIn) throws OaseException, UtopiaException {
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();
		Record person = finder.read(aPersonId, PERSON_TABLE);

		String accountName = relater.getRelated(person, ACCOUNT_TABLE, null)[0].getStringField(LOGINNAME_FIELD);

		// We must have a running GamePlay record
		// Record to track
		TrackLogic trackLogic = new TrackLogic(oase);

		Record gamePlay = WPQueryLogic.getRunningGamePlay(aPersonId);
		if (gamePlay == null) {
			return new JXElement("result");
			// throw new UtopiaException("No running GamePlay found for person=" + personId);
			/* Track track = trackLogic.getActiveTrack(personId);
			if (track == null) {
				return createResponse(PLAY_LOCATION_SERVICE);
			}
			gamePlay = getGamePlayForTrack(track);
			if (gamePlay == null) {
				return createResponse(PLAY_LOCATION_SERVICE);
			}  */
		}

		Vector points = trackLogic.write(thePointsIn, aPersonId);

		// Determine if any task or medium was hit
		Record game = WPQueryLogic.getGameForGamePlay(gamePlay.getId());

		JXElement result = new JXElement("result");

		Point point = null;
		JXElement pointElm;
		for (int i = 0; i < points.size(); i++) {
			pointElm = (JXElement) points.elementAt(i);
			point = PostGISUtil.createPoint(pointElm.getAttr(LON_FIELD), pointElm.getAttr(LAT_FIELD));
			Record[] locationsHit = WPQueryLogic.getLocationsHitForGame(point, game.getId());
			Record locationHit, round;
			int lastTaskId = -1, lastMediumId = -1;

			// Go through all locations that were hit
			for (int j = 0; j < locationsHit.length; j++) {
				locationHit = finder.read(locationsHit[j].getId(), LOCATION_TABLE);

				JXElement hit = null;

				switch (locationHit.getIntField(TYPE_FIELD)) {

					case LOC_TYPE_GAME_TASK:

						// Get (single) Task bound to location
						Record[] tasks = relater.getRelated(locationHit, TASK_TABLE, null);
						if (tasks.length != 1) {
							log.warn("No task found for location id=" + locationHit.getId() + " (ignoring)");
							break;
						}

						Record task = tasks[0];
						if (lastTaskId == task.getId()) {
							continue;
						}

						lastTaskId = task.getId();

						log.trace("HIT TASK: id=" + task.getId() + " name=" + task.getStringField(NAME_FIELD));

						// Store result of this task
						Record taskResult = WPQueryLogic.getTaskResultForTask(task.getId(), gamePlay.getId());
						if (taskResult == null) {
							log.warn("No taskResult found for task=" + task.getId());
							break;
						}

						// Update task result state when first hit
						// state: open-->hit
						String taskResultState = taskResult.getStringField(STATE_FIELD);
						if (taskResultState.equals(VAL_OPEN)) {
							taskResultState = VAL_HIT;
							taskResult.setLongField(TIME_FIELD, Sys.now());
							taskResult.setStringField(STATE_FIELD, taskResultState);
							modifier.update(taskResult);
						}

						// Set task-states in response
						hit = new JXElement(TAG_TASK_HIT);
						hit.setAttr(ID_FIELD, task.getId());
						hit.setAttr(STATE_FIELD, taskResultState);
						hit.setAttr(ANSWER_STATE_FIELD, taskResult.getStringField(ANSWER_STATE_FIELD));
						hit.setAttr(MEDIA_STATE_FIELD, taskResult.getStringField(MEDIA_STATE_FIELD));

						// int aUserId, String aUserName, int aGameRoundId, int aGamePlayId, int aTaskId, int aTaskResultId
						round = relater.getRelated(gamePlay, GAMEROUND_TABLE, null)[0];
						storeEvent(oase, gamePlay, WPEventPublisher.taskHit(aPersonId, accountName, round.getId(), gamePlay.getId(), task.getId(), taskResult.getId()));
						break;

					case LOC_TYPE_GAME_MEDIUM:
						Record[] media = relater.getRelated(locationHit, MEDIUM_TABLE, null);
						if (media.length == 0) {
							log.warn("No medium found for location id=" + locationHit.getId() + " (ignoring)");
							break;
						}

						Record medium = media[0];
						if (lastMediumId == medium.getId()) {
							continue;
						}
						lastMediumId = medium.getId();
						log.trace("HIT MEDIUM: id=" + medium.getId() + " name=" + medium.getStringField(NAME_FIELD));

						// Store result of this medium
						Record mediumResult = WPQueryLogic.getMediumResultForMedium(medium.getId(), gamePlay.getId());
						if (mediumResult == null) {
							log.warn("No mediumResult found for medium=" + medium.getId());
							break;
						}

						// Update medium result state when first hit
						// state: open-->hit
						String mediumResultState = mediumResult.getStringField(STATE_FIELD);
						if (mediumResultState.equals(VAL_OPEN)) {
							mediumResult.setStringField(STATE_FIELD, VAL_HIT);
							mediumResult.setLongField(TIME_FIELD, Sys.now());
							modifier.update(mediumResult);
						}

						// Set media state in response
						hit = new JXElement(TAG_MEDIUM_HIT);
						hit.setAttr(ID_FIELD, medium.getId());
						hit.setAttr(STATE_FIELD, VAL_HIT);
						// int aUserId, String aUserName, int aGameRoundId, int aGamePlayId, int aMediumId, int aMediumResultId
						round = relater.getRelated(gamePlay, GAMEROUND_TABLE, null)[0];
						storeEvent(oase, gamePlay, WPEventPublisher.mediumHit(aPersonId, accountName, round.getId(), gamePlay.getId(), lastMediumId, mediumResult.getId()));
						break;
					default:
						continue;
				}

				// Add to the response if valid hit
				if (hit != null) {
					result.addChild(hit);
				}
			}

		}

		// Send out event for last point
		if (point != null) {
			storeEvent(oase, gamePlay, WPEventPublisher.userMove(aPersonId, accountName, WPQueryLogic.getGameRoundForGamePlay(gamePlay).getId(), gamePlay.getId(), point));
		}

		return result;
	}

	public void start(int aPersonId, int aGamePlayId) throws OaseException, UtopiaException {
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();

		Record gamePlay = finder.read(aGamePlayId, GAMEPLAY_TABLE);
		String gamePlayState = gamePlay.getStringField(STATE_FIELD);
		if (gamePlayState.equals(PLAY_STATE_DONE)) {
			throw new UtopiaException("Cannot play game that is already done");
		}

		// If already a running game: pause
		Record runningGamePlay = WPQueryLogic.getRunningGamePlay(aPersonId);
		if (runningGamePlay != null) {
			runningGamePlay.setStringField(STATE_FIELD, PLAY_STATE_PAUSED);
			modifier.update(runningGamePlay);
		}

		// Game state is scheduled or running
		if (gamePlay.getStringField(STATE_FIELD).equals(PLAY_STATE_SCHEDULED)) {
			gamePlay.setLongField(START_DATE_FIELD, Sys.now());
		}

		gamePlay.setStringField(STATE_FIELD, PLAY_STATE_RUNNING);

		FileField fileField = gamePlay.getFileField(EVENTS_FIELD);

		if (fileField == null) {
			File emptyFile;

			try {
				emptyFile = File.createTempFile("empty", ".txt");
			} catch (IOException ioe) {
				log.warn("Cannot create empty temp file", ioe);
				throw new UtopiaException("Cannot create temp file", ioe);
			}
			gamePlay.createFileField(emptyFile);
			gamePlay.setFileField(EVENTS_FIELD, fileField);

		}

		modifier.update(gamePlay);

		// Start any track if not already active
		TrackLogic trackLogic = new TrackLogic(oase);
		Track track = trackLogic.getActiveTrack(aPersonId);
		if (track == null) {
			track = trackLogic.create(aPersonId, gamePlay.getStringField(NAME_FIELD), Track.VAL_NORMAL_TRACK, Sys.now());

			// Relate track to gameplay
			relater.relate(gamePlay, track.getRecord());
		}

		// Resume current Track for this user
		trackLogic.resume(aPersonId, Track.VAL_NORMAL_TRACK, Sys.now());

		// Initialize results if not present
		initGamePlayResults(oase, gamePlay);

		// playStart(int aUserId, String aUserName, int aGameRoundId, int aGamePlayId)
		Record round = relater.getRelated(gamePlay, GAMEROUND_TABLE, null)[0];
		Record person = finder.read(aPersonId, PERSON_TABLE);

		String accountName = relater.getRelated(person, ACCOUNT_TABLE, null)[0].getStringField(LOGINNAME_FIELD);

		storeEvent(oase, gamePlay, WPEventPublisher.playStart(aPersonId, accountName, round.getId(), gamePlay.getId()));
	}

	public void reset(Record aGamePlay) throws OaseException, UtopiaException {
		Record person = oase.getRelater().getRelated(aGamePlay, PERSON_TABLE, null)[0];
		reset(person, aGamePlay);
	}

	public void reset(int aGamePlayId) throws OaseException, UtopiaException {
		Finder finder = oase.getFinder();
		Record gamePlay = finder.read(aGamePlayId, GAMEPLAY_TABLE);
		Record person = oase.getRelater().getRelated(gamePlay, PERSON_TABLE, null)[0];
		reset(person, gamePlay);
	}

	public void reset(int aPersonId, int aGamePlayId) throws OaseException, UtopiaException {
		Finder finder = oase.getFinder();
		reset(finder.read(aPersonId, PERSON_TABLE), finder.read(aGamePlayId, GAMEPLAY_TABLE));

	}

	public void reset(Record aPerson, Record aGamePlay) throws OaseException, UtopiaException {

		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();

		// Delete trace if avail
		TrackLogic trackLogic = new TrackLogic(oase);
		Record[] tracks = relater.getRelated(aGamePlay, TRACK_TABLE, null);
		for (int i = 0; i < tracks.length; i++) {
			// This also deletes related media!!
			trackLogic.delete(aPerson.getId(), tracks[i].getId() + "");
			log.info("RESET: deleted track id=" + tracks[i].getId());
		}

		// Delete task and medium results
		Record[] results = relater.getRelated(aGamePlay, null, RELTAG_RESULT);
		for (int i = 0; i < results.length; i++) {
			modifier.delete(results[i]);
		}
		log.info("RESET: deleted " + results.length + " results");

		// Game state is scheduled or running
		aGamePlay.setIntField(SCORE_FIELD, 0);
		aGamePlay.setStringField(STATE_FIELD, PLAY_STATE_SCHEDULED);

		// re-init events field
		FileField fileField = aGamePlay.getFileField(EVENTS_FIELD);

		if (fileField != null) {
			File emptyFile;

			try {
				emptyFile = File.createTempFile("empty", ".txt");
			} catch (IOException ioe) {
				log.warn("Cannot create empty temp file", ioe);
				throw new UtopiaException("Cannot create temp file", ioe);
			}
			aGamePlay.setFileField(EVENTS_FIELD, aGamePlay.createFileField(emptyFile));
		}

		modifier.update(aGamePlay);

	}


	/**
	 * ************* Data Updates ***********************
	 */

	// Initialize results if not present
	protected boolean finishTaskResult(Oase anOase, Record aGamePlay, Record aTask, Record aTaskResult) throws OaseException, UtopiaException {
		// ALL DONE
		int score = aTask.getIntField(SCORE_FIELD);

		// Update task result and total game play score
		aTaskResult.setStringField(STATE_FIELD, VAL_DONE);
		aTaskResult.setIntField(SCORE_FIELD, score);
		int totalScore = aGamePlay.getIntField(SCORE_FIELD) + score;
		aGamePlay.setIntField(SCORE_FIELD, totalScore);

		// Update task result and gameplay
		anOase.getModifier().update(aTaskResult);

		// Check if all tasks/media done
		Record[] taskResults = WPQueryLogic.getTaskResultsForGamePlay(aGamePlay);

		// Assume all done until we find a task-result not yet completed
		boolean gameDone = true;
		for (int i = 0; i < taskResults.length; i++) {
			if (!taskResults[i].getStringField(STATE_FIELD).equals(VAL_DONE)) {
				gameDone = false;
				break;
			}
		}

		if (gameDone) {
			aGamePlay.setLongField(END_DATE_FIELD, Sys.now());
			aGamePlay.setStringField(STATE_FIELD, VAL_DONE);
		}

		// Always update gameplay state
		anOase.getModifier().update(aGamePlay);

		return gameDone;
	}

	// Initialize results if not present
	protected void initGamePlayResults(Oase anOase, Record aGamePlay) throws OaseException, UtopiaException {
		Modifier modifier = anOase.getModifier();
		Relater relater = anOase.getRelater();
		Record[] taskResults = WPQueryLogic.getTaskResultsForGamePlay(aGamePlay);

		// Check if results already present
		if (taskResults.length > 0) {
			// Already there: nothing to do
			return;
		}

		// Create results for each task and medium related to game

		// The task-results
		Record[] tasks = WPQueryLogic.getTasksForGamePlay(aGamePlay.getId());
		Record taskResult;
		for (int i = 0; i < tasks.length; i++) {
			taskResult = modifier.create(TASKRESULT_TABLE);
			modifier.insert(taskResult);
			relater.relate(aGamePlay, taskResult, RELTAG_RESULT);
			relater.relate(tasks[i], taskResult, RELTAG_RESULT);
		}

		// The medium-results
		Record[] media = WPQueryLogic.getMediaForGamePlay(aGamePlay);
		Record mediumResult;
		for (int i = 0; i < media.length; i++) {
			mediumResult = modifier.create(MEDIUMRESULT_TABLE);
			modifier.insert(mediumResult);
			relater.relate(aGamePlay, mediumResult, RELTAG_RESULT);
			relater.relate(media[i], mediumResult, RELTAG_RESULT);
		}


	}

	// Store (pushlet) event into gameplay table.
	protected void storeEvent(Oase anOase, Record aGamePlay, Event anEvent) throws OaseException, UtopiaException {
//		if (getProperty("verbose").equals("true")) {
//			log.info("EVENT: " + anEvent.getSubject() + " " + anEvent.getField(WPEventPublisher.FIELD_EVENT));
//		}

		Iterator iter = anEvent.getFieldNames();
		JXElement storedEvent = new JXElement("event");
		String name;
		while (iter.hasNext()) {
			name = (String) iter.next();

			// Skip Pushlet internal fields
			if (name.startsWith("p_")) {
				continue;
			}

			storedEvent.setAttr(name, anEvent.getField(name));
		}
		FileField fileField = aGamePlay.getFileField(EVENTS_FIELD);
		if (fileField == null) {
			File emptyFile;

			try {
				emptyFile = File.createTempFile("empty", ".txt");
			} catch (IOException ioe) {
				log.warn("Cannot create empty temp file", ioe);
				throw new UtopiaException("Cannot create temp file", ioe);
			}
			aGamePlay.setFileField(EVENTS_FIELD, aGamePlay.createFileField(emptyFile));
		}
		fileField = aGamePlay.getFileField(EVENTS_FIELD);
		String eventStr = storedEvent.toString() + "\n";
		fileField.append(eventStr.getBytes());
		anOase.getModifier().update(aGamePlay);
	}


}
