package org.walkandplay.server.control;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Sys;
import org.keyworx.common.util.Rand;
import org.keyworx.utopia.core.config.ContentHandlerConfig;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.oase.api.*;
import org.walkandplay.server.util.Constants;
import org.geotracing.handler.HandlerUtil;
import org.geotracing.handler.QueryLogic;
import org.geotracing.handler.TrackLogic;
import org.geotracing.handler.Track;

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

	public JXElement playStartReq(UtopiaRequest anUtopiaRequest) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaRequest.getRequestCommand();
		String gamePlayId = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr("id", gamePlayId);

		int personId = HandlerUtil.getUserId(anUtopiaRequest);

		Finder finder = HandlerUtil.getOase(anUtopiaRequest).getFinder();
		Modifier modifier = HandlerUtil.getOase(anUtopiaRequest).getModifier();
		Relater relater = HandlerUtil.getOase(anUtopiaRequest).getRelater();

		Record gamePlay =  finder.read(Integer.parseInt(gamePlayId), GAMEPLAY_TABLE);
		String gamePlayState = gamePlay.getStringField(STATE_FIELD);
		if (gamePlayState.equals(PLAY_STATE_DONE)) {
			throw new UtopiaException("Cannot play game that is already done");
		}

		// TODO Suspend any running games
		Record person = finder.read(personId, PERSON_TABLE);

		// Game state is scheduled or running
		gamePlay.setStringField(STATE_FIELD, PLAY_STATE_RUNNING);
		modifier.update(gamePlay);

		// Start any track if not already active
		//TrackLogic trackLogic = new TrackLogic(anUtopiaRequest.getUtopiaSession().getContext().getOase());

		// Resume current Track for this user
		//trackLogic.resume(HandlerUtil.getUserId(anUtopiaRequest), Track.VAL_DAY_TRACK, System.currentTimeMillis());

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

}
