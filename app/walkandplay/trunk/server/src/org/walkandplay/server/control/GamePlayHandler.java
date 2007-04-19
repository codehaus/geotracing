package org.walkandplay.server.control;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.HandlerUtil;
import org.geotracing.handler.TrackLogic;
import org.geotracing.handler.Track;
import org.geotracing.handler.QueryLogic;
import org.geotracing.gis.PostGISUtil;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Rand;
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

/**
 * GamePlayHandler.
 * <p/>
 * Redirects the requests to the right logic method
 *
 * @author Ronald Lenz
 * @version $Id: GameScheduleHandler.java 327 2007-01-25 16:54:39Z just $
 */
public class GamePlayHandler extends DefaultHandler implements Constants {

	public final static String PLAY_START_SERVICE = "play-start";
	public final static String PLAY_LOCATION_SERVICE = "play-location";
	public final static String PLAY_ANSWERTASK_SERVICE = "play-answertask";

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
				response = playStartReq(anUtopiaRequest);
			} else if (service.equals(PLAY_LOCATION_SERVICE)) {
				response = playLocationReq(anUtopiaRequest);
			} else if (service.equals(PLAY_ANSWERTASK_SERVICE)) {
				response = playAnswerTaskReq(anUtopiaRequest);
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

	public JXElement playLocationReq(UtopiaRequest anUtopiaRequest) throws OaseException, UtopiaException {
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

			for (int j=0; locationsHit.length > 0; j++) {
				log.info("HIT:" + locationsHit[j].getStringField(NAME_FIELD));
			}
		}


		return response;
	}

	public JXElement playStartReq(UtopiaRequest anUtopiaRequest) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaRequest.getRequestCommand();
		String gamePlayId = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr("id", gamePlayId);

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
			trackLogic.create(personId, gamePlay.getStringField(NAME_FIELD), Track.VAL_NORMAL_TRACK, Sys.now());
		}

		// Resume current Track for this user
		trackLogic.resume(personId, Track.VAL_NORMAL_TRACK, Sys.now());

		return createResponse(PLAY_START_SERVICE);
	}

	public JXElement playAnswerTaskReq(UtopiaRequest anUtopiaRequest) throws UtopiaException {
/*
        <play-answertask-req id="[taskid]" answer="blabla" />
        <play-answertask-rsp answer="[boolean]" score="[nrofpoints] />
*/

		JXElement rsp = createResponse(PLAY_ANSWERTASK_SERVICE);
		rsp.setAttr("answer", "true");
		rsp.setAttr("score", "10");
		return rsp;
	}

	public Record getGameForGamePlay(Oase anOase, int aGamePlayId) throws OaseException, UtopiaException {
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

	public Record getRunningGamePlay(Oase anOase, int aPersonId) throws UtopiaException {
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

	public Record[] getLocationsHitForGame(Oase anOase, Point aPoint, int aGameId) throws UtopiaException {
		try {
			String distanceClause = "distance_sphere(GeomFromText('POINT(" + aPoint.x + " " + aPoint.y + ")',4326),point)";
			String tables = "g_location,wp_game";
			String fields = "g_location.id,g_location.name,g_location.type,g_location.point";
			String where = distanceClause + " < 40 AND wp_game.id = " + aGameId;
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
