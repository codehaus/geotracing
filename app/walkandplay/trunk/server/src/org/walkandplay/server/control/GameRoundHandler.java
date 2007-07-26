package org.walkandplay.server.control;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.HandlerUtil;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.control.ThreadSafe;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.walkandplay.server.logic.GameRoundLogic;
import org.walkandplay.server.util.Constants;

/**
 * Manage gamerounds.
 * <p/>
 * A GameRound ties players and gameplay t a specific game.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class GameRoundHandler extends DefaultHandler implements ThreadSafe, Constants {

	public final static String ROUND_CREATE_SERVICE = "round-create";
	public final static String ROUND_DELETE_SERVICE = "round-delete";
	public final static String ROUND_ADD_PLAYERS_SERVICE = "round-add-players";
	public final static String ROUND_REMOVE_PLAYERS_SERVICE = "round-remove-players";

	private Log log = Logging.getLog("GameRoundHandler");

	/**
	 * Processes the Client Request.
	 *
	 * @param anUtopiaRequest A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public UtopiaResponse processRequest(UtopiaRequest anUtopiaRequest) throws UtopiaException {

		// Get the service name for the request
		String service = anUtopiaRequest.getServiceName();

		JXElement response;
		try {
			if (service.equals(ROUND_CREATE_SERVICE)) {
				response = createRound(anUtopiaRequest);
			} else if (service.equals(ROUND_DELETE_SERVICE)) {
				response = deleteRound(anUtopiaRequest);
			} else if (service.equals(ROUND_ADD_PLAYERS_SERVICE)) {
				response = addPlayers(anUtopiaRequest);
			} else if (service.equals(ROUND_REMOVE_PLAYERS_SERVICE)) {
				response = removePlayers(anUtopiaRequest);
			} else {
				log.warn("Unknown service " + service);
				response = createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
			}

			log.trace("Handled service=" + service + " response=" + response.getTag());
			return new UtopiaResponse(response);
		} catch (UtopiaException ue) {
			log.error("Negative response for service=" + service + "; exception:" + ue.getMessage());
			return new UtopiaResponse(createNegativeResponse(service, ue.getErrorCode(), "Error in request: " + ue.getMessage()));
		} catch (Throwable t) {
			log.error("Unexpected error in service : " + service, t);
			return new UtopiaResponse(createNegativeResponse(service, ErrorCode.__6005_Unexpected_error, "Unexpected error in request"));
		}
	}

	/**
	 * Create a gameround.
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return a response.
	 * @throws UtopiaException Standard Utopia exception
	 */
	public JXElement createRound(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		JXElement requestElm = anUtopiaReq.getRequestCommand();

		HandlerUtil.throwOnNonNumAttr(requestElm, GAME_ID_FIELD);
		HandlerUtil.throwOnMissingAttr(requestElm, NAME_FIELD);
		GameRoundLogic logic = createLogic(anUtopiaReq);
		Record gameRound = logic.createRound(HandlerUtil.getUserId(anUtopiaReq), requestElm.getIntAttr(GAME_ID_FIELD), requestElm.getAttr(NAME_FIELD));

		// If players specified: add them to gameround
		if (requestElm.hasAttr(PLAYERS_FIELD)) {
			logic.addPlayers(gameRound, requestElm.getAttr(PLAYERS_FIELD));
		}


		JXElement response = createResponse(ROUND_CREATE_SERVICE);
		response.setAttr(ID_FIELD, gameRound.getId());
		return response;
	}

	/**
	 * Delete a gameround.
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return a response.
	 * @throws UtopiaException Standard Utopia exception
	 */
	public JXElement deleteRound(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		JXElement requestElm = anUtopiaReq.getRequestCommand();
		HandlerUtil.throwOnNonNumAttr(requestElm, ID_FIELD);
		createLogic(anUtopiaReq).deleteRound(HandlerUtil.getUserId(anUtopiaReq), requestElm.getIntAttr(ID_FIELD));
		return createResponse(ROUND_DELETE_SERVICE);
	}

	/**
	 * Add player (person) to gameround.
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return a response.
	 * @throws UtopiaException Standard Utopia exception
	 */
	public JXElement addPlayers(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		JXElement requestElm = anUtopiaReq.getRequestCommand();

		HandlerUtil.throwOnNonNumAttr(requestElm, ROUND_ID_FIELD);
		HandlerUtil.throwOnMissingAttr(requestElm, PLAYERS_FIELD);

		// Add players to round
		createLogic(anUtopiaReq).addPlayers(requestElm.getIntAttr(ROUND_ID_FIELD), requestElm.getAttr(PLAYERS_FIELD));

		return createResponse(ROUND_ADD_PLAYERS_SERVICE);
	}

	/**
	 * Remove players (persons) from gameround.
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return a response.
	 * @throws UtopiaException Standard Utopia exception
	 */
	public JXElement removePlayers(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		JXElement requestElm = anUtopiaReq.getRequestCommand();

		HandlerUtil.throwOnNonNumAttr(requestElm, ROUND_ID_FIELD);
		HandlerUtil.throwOnMissingAttr(requestElm, PLAYERS_FIELD);

		// Add players to round
		createLogic(anUtopiaReq).removePlayers(requestElm.getIntAttr(ROUND_ID_FIELD), requestElm.getAttr(PLAYERS_FIELD));
		return createResponse(ROUND_REMOVE_PLAYERS_SERVICE);
	}

	public GameRoundLogic createLogic(UtopiaRequest anUtopiaReq) {
		return new GameRoundLogic(HandlerUtil.getOase(anUtopiaReq));
	}

}
