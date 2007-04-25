package org.walkandplay.server.control;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.*;
import org.geotracing.gis.PostGISUtil;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Sys;
import org.keyworx.utopia.core.config.ContentHandlerConfig;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.oase.api.*;
import org.walkandplay.server.util.Constants;
import org.walkandplay.server.util.WPEventPublisher;
import org.postgis.Point;

import java.util.Vector;

/**
 * GamePlayHandler.
 * <p/>
 * Redirects the requests to the right logic method
 *
 * @author Ronald Lenz
 * @version $Id: GameScheduleHandler.java 327 2007-01-25 16:54:39Z just $
 */
public class GamePlayHandler extends DefaultHandler implements Constants {

	public final static String PLAY_RESET_SERVICE = "play-reset";
	public final static String PLAY_START_SERVICE = "play-start";
	public final static String PLAY_LOCATION_SERVICE = "play-location";
	public final static String PLAY_ANSWERTASK_SERVICE = "play-answertask";
	public final static String PLAY_ADD_MEDIUM_SERVICE = "play-add-medium";
	public final static String PLAY_GET_TEAM_RESULT_SERVICE = "play-get-gameplay";
	public final static String PLAY_HEARTBEAT_SERVICE = "play-hb";

	private Log log = Logging.getLog("GamePlayHandler");
	private ContentHandlerConfig config;

	/**
	 * Processes the Client Request.
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public UtopiaResponse processRequest(UtopiaRequest anUtopiaReq) throws UtopiaException {
		Log log = Logging.getLog(anUtopiaReq);

		// Get the service name for the request
		String service = anUtopiaReq.getServiceName();
		log.info("Handling request for service=" + service);
		log.info(new String(anUtopiaReq.getRequestCommand().toBytes(false)));

		JXElement response;
		try {
			if (service.equals(PLAY_START_SERVICE)) {
				response = startReq(anUtopiaReq);
			} else if (service.equals(PLAY_RESET_SERVICE)) {
				response = resetReq(anUtopiaReq);
			} else if (service.equals(PLAY_LOCATION_SERVICE)) {
				response = locationReq(anUtopiaReq);
			} else if (service.equals(PLAY_ANSWERTASK_SERVICE)) {
				response = answerTaskReq(anUtopiaReq);
			} else if (service.equals(PLAY_ADD_MEDIUM_SERVICE)) {
				response = addMediumReq(anUtopiaReq);
			} else if (service.equals(PLAY_GET_TEAM_RESULT_SERVICE)) {
				response = getTeamResultReq(anUtopiaReq);
			} else if (service.equals(PLAY_HEARTBEAT_SERVICE)) {
				response = heartbeatReq(anUtopiaReq);
			} else {
				log.warn("Unknown service " + service);
				response = createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
			}

			log.info("Handled service=" + service + " response=" + response.getTag());
			log.info(new String(response.toBytes(false)));
			return new UtopiaResponse(response);
		} catch (UtopiaException ue) {
			log.error("Negative response for service=" + service + "; exception:" + ue.getMessage());
			return new UtopiaResponse(createNegativeResponse(service, ue.getErrorCode(), "Error in request: " + ue.getMessage()));
		} catch (Throwable t) {
			log.error("Unexpected error in service : " + service, t);
			return new UtopiaResponse(createNegativeResponse(service, ErrorCode.__6005_Unexpected_error, "Unexpected error in request"));
		}
	}

	public JXElement addMediumReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
/*
        <play-add-medium-req id="[mediumid]" />
        <play-add-medium-rsp locationid="[locationid]" taskresultid="[taskresultid]" />
*/
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		String mediumIdStr = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr(ID_FIELD, mediumIdStr);

		Oase oase = HandlerUtil.getOase(anUtopiaReq);
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();

		// Create Location for medium and relate to track and location tables
		Record medium = oase.getFinder().read(Integer.parseInt(mediumIdStr));
		int mediumId = medium.getId();

		// Person is person related to medium
		// not neccessarily the person logged in (e.g. admin for email upload)
		Record person = relater.getRelated(medium, PERSON_TABLE, null)[0];
		int personId = person.getId();

		// Determine timestamp: we use the time that the medium was sent
		long timestamp = Sys.now();
		if (requestElement.hasAttr(TIME_FIELD)) {
			// if a timestamp was provided we assume we already have the correct creation time
			timestamp = requestElement.getLongAttr(TIME_FIELD);
		}

		// First determine medium location and add to track
		TrackLogic trackLogic = new TrackLogic(oase);

		// Adds medium to track and creates location object for timestamp
		// (where the player was at that time)
		Location location = trackLogic.createLocation(personId, mediumId, timestamp, TrackLogic.REL_TAG_MEDIUM);

		// We either have location or an exception here
		JXElement rsp = createResponse(PLAY_ADD_MEDIUM_SERVICE);
		rsp.setAttr(LOCATION_ID_FIELD, location.getId());

		// We must have a running GamePlay record
		Record gamePlay = getRunningGamePlay(oase, personId);
		if (gamePlay == null) {
			throw new UtopiaException("No running GamePlay found for person=" + personId);
		}

		// Figure out if location is near a task
		Record game = getGameForGamePlay(oase, gamePlay.getId());
		Point point = location.getPoint();
		Record task = getTaskHitForGame(oase, point, game.getId());
		if (task != null) {
			log.info("HIT task for medium add taskid=" + task.getId());

			// Store result of this task
			Record taskResult = getTaskResultForTask(oase, task.getId(), gamePlay.getId());
			if (taskResult == null) {
				throw new UtopiaException("No taskResult found for task=" + task.getId());
			}

			String totalState = taskResult.getStringField(STATE_FIELD);
			String answerState = taskResult.getStringField(ANSWER_STATE_FIELD);
			taskResult.setStringField(MEDIA_STATE_FIELD, VAL_DONE);

			// Unrelate possible previous medium
			Record[] mediaResults = relater.getRelated(taskResult, MEDIUM_TABLE, RELTAG_RESULT);
			for (int i=0; i < mediaResults.length; i++) {
				relater.unrelate(taskResult,mediaResults[i]);
			}

			// Relate added medium
			relater.relate(taskResult, medium, RELTAG_RESULT);

			// Set scores if we are totally done and task result was not already done
			if (answerState.equals(VAL_DONE) && !totalState.equals(VAL_DONE)) {
				// ALL DONE
				int score = task.getIntField(SCORE_FIELD);

				// Update task result and total game play score
				taskResult.setIntField(SCORE_FIELD, score);
				int totalScore = gamePlay.getIntField(SCORE_FIELD) + score;
				gamePlay.setIntField(SCORE_FIELD, totalScore);
				modifier.update(gamePlay);
				totalState = VAL_DONE;
				taskResult.setStringField(STATE_FIELD, totalState);
			}


			modifier.update(taskResult);
			rsp.setAttr(TASK_ID_FIELD, task.getId());
			rsp.setAttr(TASK_STATE_FIELD, totalState);
		}
		return rsp;
	}

	public JXElement answerTaskReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
/*
        <play-answertask-req id="[taskid]" answer="blabla" />
        <play-answertask-rsp result="[boolean]" score="[nrofpoints] />
*/
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		String taskId = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr(ID_FIELD, taskId);
		String playerAnswer = requestElement.getAttr(ANSWER_FIELD);
		HandlerUtil.throwOnMissingAttr(ANSWER_FIELD, playerAnswer);

		int personId = HandlerUtil.getUserId(anUtopiaReq);

		Oase oase = HandlerUtil.getOase(anUtopiaReq);
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();

		// We must have a running GamePlay record
		Record gamePlay = getRunningGamePlay(oase, personId);
		if (gamePlay == null) {
			throw new UtopiaException("No running GamePlay found for person=" + personId);
		}

		Record task = finder.read(Integer.parseInt(taskId), TASK_TABLE);
		String[] answers = task.getStringField(ANSWER_FIELD).split(",");
		int score = 0;
		for (int i = 0; i < answers.length; i++) {
			if (answers[i].equals(playerAnswer)) {
				score = task.getIntField(SCORE_FIELD);
				break;
			}
		}

		// Store result of this task
		Record taskResult = getTaskResultForTask(oase, task.getId(), gamePlay.getId());
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

		// Ok, set score only if also media was done and this task was not yet completed
		if (score > 0 && mediaState.equals(VAL_DONE) && !totalState.equals(VAL_DONE)) {
			taskResult.setIntField(SCORE_FIELD, score);

			// Update total game play score
			int totalScore = gamePlay.getIntField(SCORE_FIELD) + score;
			gamePlay.setIntField(SCORE_FIELD, totalScore);
			modifier.update(gamePlay);

			// ALL DONE
			totalState = VAL_DONE;
			taskResult.setStringField(STATE_FIELD, totalState);
		}

		// Store result
		modifier.update(taskResult);

		// Send response
		JXElement rsp = createResponse(PLAY_ANSWERTASK_SERVICE);
		rsp.setAttr(STATE_FIELD, totalState);
		rsp.setAttr(MEDIA_STATE_FIELD, mediaState);
		rsp.setAttr(ANSWER_STATE_FIELD, answerState);
		rsp.setAttr(SCORE_FIELD, score);
		return rsp;
	}


	public JXElement getTeamResultReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
/*
        <get-team-result-req id="[gameplayid]"  />

        <gameplay team="red2" score="123" state="running" trackid="456">
        	  <taskresult taskid="8787" state="done" answerstate="ok" mediastate="done" answer="fjkdaf" />
    	      <taskresult taskid="8890" state="hit" answerstate="notok" mediastate="done" answer="wrong" />
	          <mediumresult mediumid="341" state="open" />
              <mediumresult mediumid="879" state="hit" />
          	  <taskresult taskid="3256" state="open" answerstate="open" mediastate="open" />
	          <taskresult taskid="3256" state="open" answerstate="open" mediastate="open" />
        </gameplay>
*/
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		String gamePlayIdStr = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr(ID_FIELD, gamePlayIdStr);
		int gamePlayId = Integer.parseInt(gamePlayIdStr);

		JXElement gamePlayElm = getResultForTeam(HandlerUtil.getOase(anUtopiaReq), HandlerUtil.getAccountName(anUtopiaReq), gamePlayId);

		JXElement rsp = createResponse(PLAY_GET_TEAM_RESULT_SERVICE);
		rsp.addChild(gamePlayElm);
		return rsp;
	}

	/**
	 * heartbeat.
	 * <p/>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement heartbeatReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		// EventPublisher.heartbeat(track, reqElm.getLongAttr(ATTR_T), anUtopiaReq);

		return createResponse(PLAY_HEARTBEAT_SERVICE);
	}

	public JXElement locationReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		int personId = HandlerUtil.getUserId(anUtopiaReq);

		Oase oase = HandlerUtil.getOase(anUtopiaReq);
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();

		// We must have a running GamePlay record
		Record gamePlay = getRunningGamePlay(oase, personId);
		if (gamePlay == null) {
			throw new UtopiaException("No running GamePlay found for person=" + personId);
		}

		// Record to track
		TrackLogic trackLogic = new TrackLogic(oase);
		Vector points = trackLogic.write(requestElement.getChildren(), personId);

		// Determine if any task or medium was hit
		Record game = getGameForGamePlay(oase, gamePlay.getId());

		JXElement response = createResponse(PLAY_LOCATION_SERVICE);

		Point point=null;
		JXElement pointElm;
		for (int i = 0; i < points.size(); i++) {
			pointElm = (JXElement) points.elementAt(i);
			point = PostGISUtil.createPoint(pointElm.getAttr(LON_FIELD), pointElm.getAttr(LAT_FIELD));
			Record[] locationsHit = getLocationsHitForGame(oase, point, game.getId());
			Record locationHit;
			int lastTaskId=-1,lastMediumId=-1;

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

						log.info("HIT TASK: id=" + task.getId() + " name=" + task.getStringField(NAME_FIELD));

						// Store result of this task
						Record taskResult = getTaskResultForTask(oase, task.getId(), gamePlay.getId());
						if (taskResult == null) {
							log.warn("No taskResult found for task=" + task.getId());
							break;
						}

						// Update task result state when first hit
						// state: open-->hit
						String taskResultState = taskResult.getStringField(STATE_FIELD);
						if (taskResultState.equals(VAL_OPEN)) {
							taskResultState = VAL_HIT;
							taskResult.setStringField(STATE_FIELD, taskResultState);
							modifier.update(taskResult);
						}

						// Set task-states in response
						hit = new JXElement(TAG_TASK_HIT);
						hit.setAttr(ID_FIELD, task.getId());
						hit.setAttr(STATE_FIELD, taskResultState);
						hit.setAttr(ANSWER_STATE_FIELD, taskResult.getStringField(ANSWER_STATE_FIELD));
						hit.setAttr(MEDIA_STATE_FIELD, taskResult.getStringField(MEDIA_STATE_FIELD));

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
						log.info("HIT MEDIUM: id=" + medium.getId() + " name=" + medium.getStringField(NAME_FIELD));

						// TODO update result of media hit

						// Set media state in response
						hit = new JXElement(TAG_MEDIUM_HIT);
						hit.setAttr(ID_FIELD, medium.getId());
						hit.setAttr(STATE_FIELD, VAL_HIT);

						break;
					default:
						continue;
				}

				// Add to the response if valid hit
				if (hit != null) {
					response.addChild(hit);
				}
			}

		}

		// Send out event for last point
		if (point != null) {
			WPEventPublisher.userMove(personId, HandlerUtil.getAccountName(anUtopiaReq), getGameRoundForGamePlay(oase, gamePlay).getId(), gamePlay.getId(), point);
		}

		return response;
	}

	public JXElement resetReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		String gamePlayId = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr("id", gamePlayId);

		int personId = HandlerUtil.getUserId(anUtopiaReq);

		Oase oase = HandlerUtil.getOase(anUtopiaReq);
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();

		Record gamePlay = finder.read(Integer.parseInt(gamePlayId), GAMEPLAY_TABLE);

		// Delete trace if avail
		TrackLogic trackLogic = new TrackLogic(oase);
		Record[] tracks = relater.getRelated(gamePlay, TRACK_TABLE, null);
		for (int i = 0; i < tracks.length; i++) {
			// This also deletes related media!!
			trackLogic.delete(personId, tracks[i].getId() + "");
		}

		// Delete task results
		Record[] taskResults = relater.getRelated(gamePlay, TASKRESULT_TABLE, null);
		for (int i = 0; i < taskResults.length; i++) {
			modifier.delete(taskResults[i]);
		}

		// Game state is scheduled or running
		gamePlay.setIntField(SCORE_FIELD, 0);
		gamePlay.setStringField(STATE_FIELD, PLAY_STATE_SCHEDULED);
		modifier.update(gamePlay);

		return createResponse(PLAY_RESET_SERVICE);
	}

	public JXElement startReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		String gamePlayIdStr = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr(ID_FIELD, gamePlayIdStr);
		int gamePlayId = Integer.parseInt(gamePlayIdStr);

		int personId = HandlerUtil.getUserId(anUtopiaReq);

		Oase oase = HandlerUtil.getOase(anUtopiaReq);
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();

		Record gamePlay = finder.read(gamePlayId, GAMEPLAY_TABLE);
		String gamePlayState = gamePlay.getStringField(STATE_FIELD);
		if (gamePlayState.equals(PLAY_STATE_DONE)) {
			throw new UtopiaException("Cannot play game that is already done");
		}

		// TODO Find any running games
		Record runningGamePlay = getRunningGamePlay(oase, personId);
		if (runningGamePlay != null) {

		}
		Record person = finder.read(personId, PERSON_TABLE);

		// Game state is scheduled or running
		gamePlay.setStringField(STATE_FIELD, PLAY_STATE_RUNNING);
		modifier.update(gamePlay);

		// Start any track if not already active
		TrackLogic trackLogic = new TrackLogic(oase);
		Track track = trackLogic.getActiveTrack(personId);
		if (track == null) {
			track = trackLogic.create(personId, gamePlay.getStringField(NAME_FIELD), Track.VAL_NORMAL_TRACK, Sys.now());

			// Relate track to gameplay
			relater.relate(gamePlay, track.getRecord());
		}

		// Resume current Track for this user
		trackLogic.resume(personId, Track.VAL_NORMAL_TRACK, Sys.now());

		// Initialize results if not present
		Record[] taskResults = getTaskResultsForGamePlay(oase, gamePlayId);
		if (taskResults.length == 0) {
			Record[] tasks = getTasksForGamePlay(oase, gamePlayId);
			Record taskResult;
			for (int i = 0; i < tasks.length; i++) {
				taskResult = modifier.create(TASKRESULT_TABLE);
				modifier.insert(taskResult);
				relater.relate(gamePlay, taskResult, RELTAG_RESULT);
				relater.relate(tasks[i], taskResult, RELTAG_RESULT);
			}
		}

		return createResponse(PLAY_START_SERVICE);
	}

	/**************** Data Queries ************************/

	protected Record getGameRoundForGamePlay(Oase anOase, Record aGamePlay) throws OaseException, UtopiaException {
		try {
			return anOase.getRelater().getRelated(aGamePlay, SCHEDULE_TABLE, null)[0];
		} catch (Throwable t) {
			log.warn("Error query getTaskResultsForGamePlay gamePlayId=" + aGamePlay.getId(), t);
			throw new UtopiaException("Error in getTaskResultsForGamePlay aGamePlayId=" + aGamePlay.getId(), t);
		}
	}

	protected JXElement getResultForTeam(Oase anOase, String aTeamName, int aGamePlayId) throws UtopiaException {
		JXElement result = null;
		try {
			Finder finder = anOase.getFinder();
			Relater relater = anOase.getRelater();
			Record gamePlay = finder.read(aGamePlayId, GAMEPLAY_TABLE);

			// Gameplay attrs
			result = new JXElement(TAG_GAMEPLAY);
			result.setAttr(ATTR_TEAM, aTeamName);
			result.setAttr(SCORE_FIELD, gamePlay.getIntField(SCORE_FIELD));
			result.setAttr(STATE_FIELD, gamePlay.getStringField(STATE_FIELD));
			Record[] tracks = relater.getRelated(gamePlay, TRACK_TABLE, null);
			if (tracks.length > 0) {
				result.setAttr(ATTR_TRACKID, tracks[0].getId());
			}

			String tables = "wp_gameplay,wp_task,wp_taskresult";
			String fields = "wp_task.id AS taskid,wp_taskresult.state,wp_taskresult.answerstate,wp_taskresult.mediastate,wp_taskresult.answer,wp_taskresult.score";
			String where = "wp_gameplay.id = " + aGamePlayId;
			String relations = "wp_gameplay,wp_taskresult;wp_taskresult,wp_task";
			String postCond = null;
			Record[] records = QueryLogic.queryStore(anOase, tables, fields, where, relations, postCond);
			Record taskResult;
			JXElement taskResultElm;
			for (int i=0; i < records.length; i++) {
				taskResult = records[i];
				taskResultElm = new JXElement(TAG_TASK_RESULT);
				taskResultElm.setAttr(TASK_ID_FIELD, taskResult.getIntField(TASK_ID_FIELD));
				taskResultElm.setAttr(STATE_FIELD, taskResult.getStringField(STATE_FIELD));
				taskResultElm.setAttr(ANSWER_STATE_FIELD, taskResult.getStringField(ANSWER_STATE_FIELD));
				taskResultElm.setAttr(MEDIA_STATE_FIELD, taskResult.getStringField(MEDIA_STATE_FIELD));
				taskResultElm.setAttr(ANSWER_FIELD, taskResult.getStringField(ANSWER_FIELD));
				taskResultElm.setAttr(SCORE_FIELD, taskResult.getIntField(SCORE_FIELD));

				result.addChild(taskResultElm);
			}
		} catch (Throwable t) {
			log.warn("Error getResultForTeam gamplayid=" + aGamePlayId, t);
			throw new UtopiaException("Error getResultForTeam gamplayid=" + aGamePlayId, t);
		}

		return result;
	}

	protected Record getGameForGamePlay(Oase anOase, int aGamePlayId) throws OaseException, UtopiaException {
		Record game = null;
		try {
			Record gamePlay = anOase.getFinder().read(aGamePlayId, GAMEPLAY_TABLE);
			Record schedule = anOase.getRelater().getRelated(gamePlay, SCHEDULE_TABLE, null)[0];
			game = anOase.getRelater().getRelated(schedule, GAME_TABLE, null)[0];
		} catch (Throwable t) {
			log.warn("Error query getGameForGamePlay gamePlayId=" + aGamePlayId, t);
		}

		return game;
	}

	protected Record getRunningGamePlay(Oase anOase, int aPersonId) throws UtopiaException {
		Record result = null;
		try {
			String tables = "utopia_person,wp_gameplay";
			String fields = "wp_gameplay.id,wp_gameplay.name,wp_gameplay.state,wp_gameplay.score";
			String where = "utopia_person.id = " + aPersonId + " AND wp_gameplay.state = '" + PLAY_STATE_RUNNING + "'";
			String relations = "utopia_person,wp_gameplay";
			String postCond = null;
			Record[] records = QueryLogic.queryStore(anOase, tables, fields, where, relations, postCond);
			if (records.length == 1) {
				result = anOase.getFinder().read(records[0].getId(), GAMEPLAY_TABLE);
			} else if (records.length > 1) {
				throw new UtopiaException("More than one running gameplay for person=" + aPersonId);
			}
		} catch (Throwable t) {
			log.warn("Error get running gameplay for person=" + aPersonId, t);
		}

		return result;
	}

	protected Record[] getLocationsHitForGame(Oase anOase, Point aPoint, int aGameId) throws UtopiaException {
		try {
			String distanceClause = "distance_sphere(GeomFromText('POINT(" + aPoint.x + " " + aPoint.y + ")',4326),point)";
			String tables = "g_location,wp_game";
			String fields = "g_location.id,g_location.name,g_location.type,g_location.point";
			String where = distanceClause + " < " + HIT_RADIUS_METERS + " AND wp_game.id = " + aGameId;
			String relations = "g_location,wp_game";
			String postCond = null;
			return QueryLogic.queryStore(anOase, tables, fields, where, relations, postCond);
		} catch (Throwable t) {
			log.warn("Error query locations for game=" + aGameId, t);
			throw new UtopiaException("Error in getLocationsHit game=" + aGameId, t);
		}
	}

	protected Record getTaskHitForGame(Oase anOase, Point aPoint, int aGameId) throws UtopiaException {
		Record result = null;
		try {
			String distanceClause = "distance_sphere(GeomFromText('POINT(" + aPoint.x + " " + aPoint.y + ")',4326),point)";
			String tables = "g_location,wp_game,wp_task";
			String fields = "wp_task.id";
			String where = distanceClause + " < " + HIT_RADIUS_METERS + " AND wp_game.id = " + aGameId + " AND g_location.type = " + LOC_TYPE_GAME_TASK;
			;
			String relations = "g_location,wp_game;g_location,wp_task";
			String postCond = null;
			Record[] tasksHit = QueryLogic.queryStore(anOase, tables, fields, where, relations, postCond);
			if (tasksHit.length == 1) {
				result = anOase.getFinder().read(tasksHit[0].getId(), TASK_TABLE);
			} else if (tasksHit.length > 1) {
				log.warn("More than task hit for aGameId=" + aGameId + " pt=" + aPoint);
			}
		} catch (Throwable t) {
			log.warn("Error getTaskHitForGame for game=" + aGameId, t);
			throw new UtopiaException("Error in getTaskHitForGame game=" + aGameId, t);
		}
		return result;
	}

	protected Record[] getTasksForGame(Oase anOase, int aGameId) throws OaseException, UtopiaException {
		try {
			String tables = "wp_game,g_location,wp_task";
			String fields = "wp_task.id";
			String where = "wp_game.id = " + aGameId + " AND g_location.type = " + LOC_TYPE_GAME_TASK;
			String relations = "wp_game,g_location;g_location,wp_task";
			String postCond = null;
			Record[] taskIds = QueryLogic.queryStore(anOase, tables, fields, where, relations, postCond);

			Record[] tasks = new Record[taskIds.length];
			for (int i = 0; i < tasks.length; i++) {
				tasks[i] = anOase.getFinder().read(taskIds[i].getId(), TASK_TABLE);
			}
			return tasks;

		} catch (Throwable t) {
			log.warn("Error query getTasksForGame gamePlayId=" + aGameId, t);
			throw new UtopiaException("Error in getTasksForGame aGamePlayId=" + aGameId, t);
		}
	}

	protected Record[] getTasksForGamePlay(Oase anOase, int aGamePlayId) throws OaseException, UtopiaException {
		try {
			Record game = getGameForGamePlay(anOase, aGamePlayId);
			return getTasksForGame(anOase, game.getId());
		} catch (Throwable t) {
			log.warn("Error query getTasksForGamePlay gamePlayId=" + aGamePlayId, t);
			throw new UtopiaException("Error in getTasksForGamePlay aGamePlayId=" + aGamePlayId, t);
		}
	}

	protected Record[] getTaskResultsForGamePlay(Oase anOase, int aGamePlayId) throws OaseException, UtopiaException {
		try {
			Record gamePlay = anOase.getFinder().read(aGamePlayId, GAMEPLAY_TABLE);
			return anOase.getRelater().getRelated(gamePlay, TASKRESULT_TABLE, null);
		} catch (Throwable t) {
			log.warn("Error query getTaskResultsForGamePlay gamePlayId=" + aGamePlayId, t);
			throw new UtopiaException("Error in getTaskResultsForGamePlay aGamePlayId=" + aGamePlayId, t);
		}
	}


	protected Record getTaskResultForTask(Oase anOase, int aTaskId, int aGamePlayId) throws UtopiaException {
		Record result = null;
		try {
			String tables = "wp_gameplay,wp_task,wp_taskresult";
			String fields = "wp_taskresult.id";
			String where = "wp_gameplay.id = " + aGamePlayId + " AND wp_task.id = " + aTaskId;
			String relations = "wp_gameplay,wp_taskresult;wp_taskresult,wp_task";
			String postCond = null;
			Record[] records = QueryLogic.queryStore(anOase, tables, fields, where, relations, postCond);
			if (records.length == 1) {
				result = anOase.getFinder().read(records[0].getId(), TASKRESULT_TABLE);
			} else if (records.length > 1) {
				throw new UtopiaException("More than taskresult for gamplayid=" + aGamePlayId);
			}
		} catch (Throwable t) {
			log.warn("Error query TaskResultForTask taskId=" + aTaskId + " gamplayid=" + aGamePlayId, t);
		}

		return result;
	}

	/*

	public JXElement playLocationDbgReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		JXElement response = createResponse(PLAY_LOCATION_SERVICE);

		if (Rand.randomInt(0, 2) == 1) {
			JXElement hit = new JXElement(TAG_TASK_HIT);
			hit.setAttr(ID_FIELD, 22560);
			response.addChild(hit);
		}

		if (Rand.randomInt(0, 4) == 1 && !response.hasChildren()) {
			JXElement hit = new JXElement(TAG_MEDIUM_HIT);
			hit.setAttr(ID_FIELD, 22629);
			response.addChild(hit);
		}

		if (Rand.randomInt(0, 4) == 1 && !response.hasChildren()) {
			JXElement hit = new JXElement(TAG_MEDIUM_HIT);
			hit.setAttr(ID_FIELD, 4497);
			response.addChild(hit);
		}

		if (Rand.randomInt(0, 4) == 1 && !response.hasChildren()) {
			JXElement hit = new JXElement(TAG_MEDIUM_HIT);
			hit.setAttr(ID_FIELD, 26527);
			response.addChild(hit);
		}

		return response;
	}

	*/
}
