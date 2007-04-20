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
import org.postgis.Point;

import java.util.Vector;
import java.sql.Timestamp;

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

	private Log log = Logging.getLog("GamePlayHandler");
	private ContentHandlerConfig config;

	/**
	 * Processes the Client Request.
	 *
	 * @param anUtopiaRequest A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public UtopiaResponse processRequest(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		Log log = Logging.getLog(anUtopiaRequest);

		// Get the service name for the request
		String service = anUtopiaRequest.getServiceName();
		log.info("Handling request for service=" + service);
		log.info(new String(anUtopiaRequest.getRequestCommand().toBytes(false)));

		JXElement response;
		try {
			if (service.equals(PLAY_START_SERVICE)) {
				response = startReq(anUtopiaRequest);
			} else if (service.equals(PLAY_RESET_SERVICE)) {
				response = resetReq(anUtopiaRequest);
			} else if (service.equals(PLAY_LOCATION_SERVICE)) {
				response = locationReq(anUtopiaRequest);
			} else if (service.equals(PLAY_ANSWERTASK_SERVICE)) {
				response = answerTaskReq(anUtopiaRequest);
			} else if (service.equals(PLAY_ADD_MEDIUM_SERVICE)) {
				response = addMediumReq(anUtopiaRequest);
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

	public JXElement addMediumReq(UtopiaRequest anUtopiaRequest) throws OaseException, UtopiaException {
/*
        <play-add-medium-req id="[mediumid]" />
        <play-add-medium-rsp locationid="[locationid]" taskresultid="[taskresultid]" />
*/
		JXElement requestElement = anUtopiaRequest.getRequestCommand();
		String mediumId = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr(ID_FIELD, mediumId);

		Oase oase = HandlerUtil.getOase(anUtopiaRequest);
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();
		// Create Location for medium and relate to track and location tables
		Record mediumRecord = oase.getFinder().read(Integer.parseInt(mediumId));

		int personId = HandlerUtil.getUserId(anUtopiaRequest);

		// First determine location and add to track
		Location location;
		TrackLogic trackLogic = new TrackLogic(oase);

		if (requestElement.hasAttr(TIME_FIELD)) {
			// if a timestamp was provided we assume we already have the correct creation time
			long timestamp = requestElement.getLongAttr(TIME_FIELD);

			// Set medium creationdate to timestamp
			mediumRecord.setTimestampField(MediaFiler.FIELD_CREATIONDATE, new Timestamp(timestamp));
			oase.getModifier().update(mediumRecord);

			location = trackLogic.createLocation(personId, mediumRecord.getId(), timestamp, TrackLogic.REL_TAG_MEDIUM);
		} else {
			// Determines timestamp from medium (e.g. EXIF) to create location
			location = trackLogic.createMediumLocation(mediumRecord.getId());
		}

		// We either have location or an exception here

		JXElement rsp = createResponse(PLAY_ADD_MEDIUM_SERVICE);
		rsp.setAttr(LOCATION_ID_FIELD, location.getId());

		// We must have a running GamePlay record
		Record runningGamePlay = getRunningGamePlay(oase, personId);
		if (runningGamePlay == null) {
			throw new UtopiaException("No running GamePlay found for person=" + personId);
		}

		// Figure out if location is near a task in progress
		Record game = getGameForGamePlay(oase, runningGamePlay.getId());
		Point point = location.getPoint();
		Record[] locationsHit = getLocationsHitForGame(oase, point, game.getId());
		log.info("Hit locations for medium cnt=" + locationsHit.length);
		return rsp;
	}

	public JXElement answerTaskReq(UtopiaRequest anUtopiaRequest) throws OaseException, UtopiaException {
/*
        <play-answertask-req id="[taskid]" answer="blabla" />
        <play-answertask-rsp result="[boolean]" score="[nrofpoints] />
*/
		JXElement requestElement = anUtopiaRequest.getRequestCommand();
		String taskId = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr(ID_FIELD, taskId);
		String playerAnswer = requestElement.getAttr(ANSWER_FIELD);
		HandlerUtil.throwOnMissingAttr(ANSWER_FIELD, playerAnswer);

		int personId = HandlerUtil.getUserId(anUtopiaRequest);

		Oase oase = HandlerUtil.getOase(anUtopiaRequest);
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();

		// We must have a running GamePlay record
		Record runningGamePlay = getRunningGamePlay(oase, personId);
		if (runningGamePlay == null) {
			throw new UtopiaException("No running GamePlay found for person=" + personId);
		}

		Record task = finder.read(Integer.parseInt(taskId), TASK_TABLE);
		String[] answers = task.getStringField(ANSWER_FIELD).split(",");
		boolean result = false;
		int score = 0;
		for (int i = 0; i < answers.length; i++) {
			if (answers[i].equals(playerAnswer)) {
				result = true;
				score = task.getIntField(SCORE_FIELD);
				break;
			}
		}

		JXElement rsp = createResponse(PLAY_ANSWERTASK_SERVICE);
		rsp.setAttr(RESULT_FIELD, result);
		rsp.setAttr(SCORE_FIELD, score);
		return rsp;
	}

	public JXElement locationReq(UtopiaRequest anUtopiaRequest) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaRequest.getRequestCommand();
		int personId = HandlerUtil.getUserId(anUtopiaRequest);

		Oase oase = HandlerUtil.getOase(anUtopiaRequest);
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();

		// We must have a running GamePlay record
		Record runningGamePlay = getRunningGamePlay(oase, personId);
		if (runningGamePlay == null) {
			throw new UtopiaException("No running GamePlay found for person=" + personId);
		}

		// Record to track
		TrackLogic trackLogic = new TrackLogic(oase);
		Vector points = trackLogic.write(requestElement.getChildren(), personId);

		// Determine if any task or medium was hit
		Record game = getGameForGamePlay(oase, runningGamePlay.getId());

		JXElement response = createResponse(PLAY_LOCATION_SERVICE);

		Point point;
		JXElement pointElm;
		for (int i = 0; i < points.size(); i++) {
			pointElm = (JXElement) points.elementAt(i);
			point = PostGISUtil.createPoint(pointElm.getAttr(LON_FIELD), pointElm.getAttr(LAT_FIELD));
			Record[] locationsHit = getLocationsHitForGame(oase, point, game.getId());
			Record locationHit;
			Record locationItemHit, locationItemsHit[];
			String hitTag;

			// Go through all locations that were hit
			for (int j = 0; j < locationsHit.length; j++) {
				locationHit = finder.read(locationsHit[j].getId(), LOCATION_TABLE);
				log.info("HIT: id=" + locationHit.getId() + " " + locationHit.getStringField(NAME_FIELD));
				switch (locationHit.getIntField(TYPE_FIELD)) {

					case LOC_TYPE_GAME_TASK:
						locationItemsHit = relater.getRelated(locationHit, TASK_TABLE, null);
						if (locationItemsHit.length != 1) {
							log.warn("No task found for location id=" + locationHit.getId() + " (ignoring)");
							continue;
						}
						locationItemHit = locationItemsHit[0];
						hitTag = TAG_TASK_HIT;

						// Record taskResult = modifier.create(TASKRESULT_TABLE);
						// relater.relate(runningGamePlay, taskResult);
						break;

					case LOC_TYPE_GAME_MEDIUM:
						locationItemsHit = relater.getRelated(locationHit, MEDIUM_TABLE, null);
						if (locationItemsHit.length == 0) {
							log.warn("No medium found for location id=" + locationHit.getId() + " (ignoring)");
							continue;
						}
						locationItemHit = locationItemsHit[0];
						hitTag = TAG_MEDIUM_HIT;
						break;
					default:
						continue;
				}

				// Add to the response
				JXElement hit = new JXElement(hitTag);
				hit.setAttr(ID_FIELD, locationItemHit.getId());
				response.addChild(hit);
			}

		}

		return response;
	}

	public JXElement resetReq(UtopiaRequest anUtopiaRequest) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaRequest.getRequestCommand();
		String gamePlayId = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr("id", gamePlayId);

		int personId = HandlerUtil.getUserId(anUtopiaRequest);

		Oase oase = HandlerUtil.getOase(anUtopiaRequest);
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

	public JXElement startReq(UtopiaRequest anUtopiaRequest) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaRequest.getRequestCommand();
		String gamePlayId = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr(ID_FIELD, gamePlayId);

		int personId = HandlerUtil.getUserId(anUtopiaRequest);

		Oase oase = HandlerUtil.getOase(anUtopiaRequest);
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();

		Record gamePlay = finder.read(Integer.parseInt(gamePlayId), GAMEPLAY_TABLE);
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
		}

		// Relate track to gameplay
		relater.relate(gamePlay, track.getRecord());

		// Resume current Track for this user
		trackLogic.resume(personId, Track.VAL_NORMAL_TRACK, Sys.now());

		return createResponse(PLAY_START_SERVICE);
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
				result = records[0];
			} else if (records.length > 1) {
				throw new UtopiaException("More than one running gameplay for person=" + aPersonId);
			}
		} catch (Throwable t) {
			log.warn("Error query running game person=" + aPersonId, t);
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

	/*

	public JXElement playLocationDbgReq(UtopiaRequest anUtopiaRequest) throws OaseException, UtopiaException {
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
