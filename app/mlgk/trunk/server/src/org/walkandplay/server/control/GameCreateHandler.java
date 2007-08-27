package org.walkandplay.server.control;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.HandlerUtil;
import org.geotracing.handler.Location;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.control.ThreadSafe;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.Medium;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.walkandplay.server.logic.GameCreateLogic;
import org.walkandplay.server.util.Constants;

/**
 * Manage Game content.
 * <p/>
 * Redirects the requests to the right logic method
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class GameCreateHandler extends DefaultHandler implements Constants, ThreadSafe {

	public final static String GAME_CREATE_SERVICE = "game-create";
	public final static String GAME_UPDATE_SERVICE = "game-update";
	public final static String GAME_DELETE_SERVICE = "game-delete";
	public final static String GAME_ADD_MEDIUM_SERVICE = "game-add-medium";
	public final static String GAME_DEL_MEDIUM_SERVICE = "game-delete-medium";
	public final static String GAME_ADD_TASK_SERVICE = "game-add-task";
	public final static String GAME_DEL_TASK_SERVICE = "game-delete-task";

	private Log log = Logging.getLog("GameCreateHandler");

	/**
	 * Processes the Client Request.
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public UtopiaResponse processRequest(UtopiaRequest anUtopiaReq) throws UtopiaException {

		// Get the service name for the request
		String service = anUtopiaReq.getServiceName();

		JXElement response;
		try {
			if (service.equals(GAME_CREATE_SERVICE)) {
				response = createGame(anUtopiaReq);
			} else if (service.equals(GAME_UPDATE_SERVICE)) {
				response = updateGame(anUtopiaReq);
			} else if (service.equals(GAME_DELETE_SERVICE)) {
				response = deleteGame(anUtopiaReq);
			} else if (service.equals(GAME_ADD_MEDIUM_SERVICE)) {
				response = addMediumReq(anUtopiaReq);
			} else if (service.equals(GAME_DEL_MEDIUM_SERVICE)) {
				response = deleteMediumReq(anUtopiaReq);
			} else if (service.equals(GAME_ADD_TASK_SERVICE)) {
				response = addTaskReq(anUtopiaReq);
			} else if (service.equals(GAME_DEL_TASK_SERVICE)) {
				response = deleteTaskReq(anUtopiaReq);
			} else {
				log.warn("Unknown service " + service);
				response = createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
			}
			return new UtopiaResponse(response);
		} catch (UtopiaException ue) {
			log.error("Negative response for service=" + service + "; exception:" + ue.getMessage());
			return new UtopiaResponse(createNegativeResponse(service, ue.getErrorCode(), "Error in request: " + ue.getMessage()));
		} catch (Throwable t) {
			log.error("Unexpected error in service : " + service, t);
			return new UtopiaResponse(createNegativeResponse(service, ErrorCode.__6005_Unexpected_error, "Unexpected error in request " + t));
		}
	}

	/**
	 * Adds an medium to the game based with its location.
	 *
	 * @param anUtopiaReq
	 * @return
	 * @throws OaseException
	 * @throws UtopiaException
	 */
	public JXElement addMediumReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		// Check required input data
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		HandlerUtil.throwOnNonNumAttr(requestElement, ID_FIELD);
		HandlerUtil.throwOnMissingChildElement(requestElement, Medium.XML_TAG);

		JXElement mediumElm = requestElement.getChildByTag(Medium.XML_TAG);

		// At least lon/lat required
		HandlerUtil.throwOnMissingChildElement(mediumElm, Location.FIELD_LON);
		HandlerUtil.throwOnMissingChildElement(mediumElm, Location.FIELD_LAT);

		// Input data ok: let logic add medium with location
		int gameId = requestElement.getIntAttr(ID_FIELD);
		int personId = HandlerUtil.getUserId(anUtopiaReq);
		Record medium = createLogic(anUtopiaReq).addGameMedium(personId, gameId, mediumElm);

		JXElement response = createResponse(GAME_ADD_MEDIUM_SERVICE);
		response.setAttr(ID_FIELD, medium.getId());

		return response;
	}

	/**
	 * Adds an item to the game based on its location. An item can be a medium or assignment object
	 *
	 * @param anUtopiaReq
	 * @return
	 * @throws OaseException
	 * @throws UtopiaException
	 */
	public JXElement addTaskReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaReq.getRequestCommand();

		// Id is required
		HandlerUtil.throwOnNonNumAttr(requestElement, ID_FIELD);
		HandlerUtil.throwOnMissingChildElement(requestElement, TAG_TASK);

		JXElement taskElm = requestElement.getChildByTag(TAG_TASK);
		HandlerUtil.throwOnMissingChildElement(taskElm, NAME_FIELD);
		HandlerUtil.throwOnMissingChildElement(taskElm, DESCRIPTION_FIELD);
		HandlerUtil.throwOnMissingChildElement(taskElm, SCORE_FIELD);
		HandlerUtil.throwOnMissingChildElement(taskElm, ANSWER_FIELD);
		HandlerUtil.throwOnMissingChildElement(taskElm, MEDIUM_ID_FIELD);
		HandlerUtil.throwOnMissingChildElement(taskElm, Location.FIELD_LON);
		HandlerUtil.throwOnMissingChildElement(taskElm, Location.FIELD_LAT);

		int gameId = requestElement.getIntAttr(ID_FIELD);
		int personId = HandlerUtil.getUserId(anUtopiaReq);
		Record task = createLogic(anUtopiaReq).addGameTask(personId, gameId, taskElm);


		JXElement response = createResponse(GAME_ADD_TASK_SERVICE);
		response.setAttr(ID_FIELD, task.getId());

		return response;
	}

	public JXElement createGame(UtopiaRequest anUtopiaReq) throws UtopiaException, OaseException {
		JXElement requestElm = anUtopiaReq.getRequestCommand();
		HandlerUtil.throwOnMissingChildElement(requestElm, TAG_GAME);

		JXElement gameElm = requestElm.getChildAt(0);
		HandlerUtil.throwOnMissingChildElement(gameElm, NAME_FIELD);
		// HandlerUtil.throwOnMissingChildElement(gameElm, DESCRIPTION_FIELD);

		// Use Logic to create game record
		GameCreateLogic logic = createLogic(anUtopiaReq);
		Record game = logic.createGame(HandlerUtil.getUserId(anUtopiaReq), gameElm);

		JXElement response = createResponse(GAME_CREATE_SERVICE);
		response.setAttr(ID_FIELD, game.getId());

		return response;
	}

	/**
	 * Deletes a game and related objects.
	 *
	 * @param anUtopiaReq
	 * @return
	 * @throws OaseException
	 * @throws UtopiaException
	 */
	public JXElement deleteGame(UtopiaRequest anUtopiaReq) throws UtopiaException, OaseException {
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		HandlerUtil.throwOnNonNumAttr(ID_FIELD, requestElement.getAttr(ID_FIELD));
		int gameId = requestElement.getIntAttr(ID_FIELD);
		createLogic(anUtopiaReq).deleteGame(HandlerUtil.getUserId(anUtopiaReq), gameId);

		JXElement response = createResponse(GAME_DELETE_SERVICE);
		response.setAttr(ID_FIELD, gameId);
		return response;
	}

	/**
	 * Deletes an medium from the game and its location.
	 *
	 * @param anUtopiaReq
	 * @return
	 * @throws OaseException
	 * @throws UtopiaException
	 */
	public JXElement deleteMediumReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		HandlerUtil.throwOnNonNumAttr(requestElement, ID_FIELD);
		int mediumId = requestElement.getIntAttr(ID_FIELD);
		createLogic(anUtopiaReq).deleteGameMedium(HandlerUtil.getUserId(anUtopiaReq), mediumId);

		JXElement response = createResponse(GAME_DEL_MEDIUM_SERVICE);
		response.setAttr(ID_FIELD, mediumId);

		return response;
	}

	/**
	 * Deletes an task from the game and its location.
	 *
	 * @param anUtopiaReq
	 * @return
	 * @throws OaseException
	 * @throws UtopiaException
	 */
	public JXElement deleteTaskReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		HandlerUtil.throwOnNonNumAttr(requestElement, ID_FIELD);
		int taskId = requestElement.getIntAttr(ID_FIELD);

		createLogic(anUtopiaReq).deleteGameTask(HandlerUtil.getUserId(anUtopiaReq), taskId);

		JXElement response = createResponse(GAME_DEL_TASK_SERVICE);
		response.setAttr(ID_FIELD, taskId);

		return response;
	}

	public JXElement updateGame(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		JXElement requestElm = anUtopiaReq.getRequestCommand();
		HandlerUtil.throwOnNonNumAttr(ID_FIELD, requestElm.getAttr(ID_FIELD));
		HandlerUtil.throwOnMissingChildElement(requestElm, TAG_GAME);

		JXElement gameElm = requestElm.getChildAt(0);
		createLogic(anUtopiaReq).updateGame(HandlerUtil.getUserId(anUtopiaReq), requestElm.getIntAttr(ID_FIELD), gameElm);
		return createResponse(GAME_UPDATE_SERVICE);
	}


	protected GameCreateLogic createLogic(UtopiaRequest anUtopiaReq) {
		return new GameCreateLogic(HandlerUtil.getOase(anUtopiaReq));
	}

}
