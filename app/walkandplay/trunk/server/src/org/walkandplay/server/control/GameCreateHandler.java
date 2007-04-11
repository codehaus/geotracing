package org.walkandplay.server.control;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.Location;
import org.geotracing.handler.HandlerUtil;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.plugin.tagging.logic.TagLogic;
import org.keyworx.utopia.core.config.ContentHandlerConfig;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.Medium;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.logic.ContentLogic;
import org.keyworx.utopia.core.logic.RelateLogic;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;
import org.walkandplay.server.util.Constants;

import java.util.List;

/**
 * Manage Game content.
 * <p/>
 * Redirects the requests to the right logic method
 *
 * @author Ronald Lenz
 * @version $Id$
 */
public class GameCreateHandler extends DefaultHandler implements Constants {

	public final static String GAME_CREATE_SERVICE = "game-create";
	public final static String GAME_UPDATE_SERVICE = "game-update";
	public final static String GAME_DELETE_SERVICE = "game-delete";
	public final static String GAME_ADD_MEDIUM_SERVICE = "game-add-medium";
	public final static String GAME_DEL_MEDIUM_SERVICE = "game-delete-medium";
	public final static String GAME_ADD_TASK_SERVICE = "game-add-task";
	public final static String GAME_DEL_TASK_SERVICE = "game-delete-task";

	private Log log = Logging.getLog("GameCreateHandler");
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
			if (service.equals(GAME_CREATE_SERVICE)) {
				response = createGame(anUtopiaRequest);
			} else if (service.equals(GAME_UPDATE_SERVICE)) {
				response = updateGame(anUtopiaRequest);
			} else if (service.equals(GAME_DELETE_SERVICE)) {
				response = deleteGame(anUtopiaRequest);
			} else if (service.equals(GAME_ADD_MEDIUM_SERVICE)) {
				response = addMediumReq(anUtopiaRequest);
			} else if (service.equals(GAME_DEL_MEDIUM_SERVICE)) {
				response = deleteMediumReq(anUtopiaRequest);
			} else if (service.equals(GAME_ADD_TASK_SERVICE)) {
				response = addTaskReq(anUtopiaRequest);
			} else {
				log.warn("Unknown service " + service);
				response = createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
			}

			log.info("Handled service=" + service + " response=" + response.getTag());
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
	 * Adds an medium to the game based on its location. An item can be a medium or assignment object
	 *
	 * @param anUtopiaRequest
	 * @return
	 * @throws OaseException
	 * @throws UtopiaException
	 */
	public JXElement addMediumReq(UtopiaRequest anUtopiaRequest) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaRequest.getRequestCommand();
		Oase oase = HandlerUtil.getOase(anUtopiaRequest);

		// Id is required
		String gameId = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr("game id", gameId);
		HandlerUtil.throwOnMissingChildElement(requestElement, Medium.XML_TAG);

		JXElement mediumElm = requestElement.getChildByTag(Medium.XML_TAG);
		HandlerUtil.throwOnMissingChildElement(mediumElm, "id");
		HandlerUtil.throwOnMissingChildElement(mediumElm, Location.FIELD_LON);
		HandlerUtil.throwOnMissingChildElement(mediumElm, Location.FIELD_LAT);

		Location location = Location.create(oase);
		String lon = mediumElm.getChildText(Location.FIELD_LON);
		String lat = mediumElm.getChildText(Location.FIELD_LAT);
		long time = System.currentTimeMillis();

		location.setPoint(Double.parseDouble(lon), Double.parseDouble(lat), 0.0D, time);
		location.saveInsert();

		int mediumId = Integer.parseInt(mediumElm.getChildText("id"));

		location.createRelation(mediumId, "medium");
		location.createRelation(Integer.parseInt(gameId), "medium");

		JXElement response = createResponse(GAME_ADD_MEDIUM_SERVICE);
		response.setAttr(ID_FIELD, mediumId);

		return response;
	}

	/**
	 * Adds an item to the game based on its location. An item can be a medium or assignment object
	 *
	 * @param anUtopiaRequest
	 * @return
	 * @throws OaseException
	 * @throws UtopiaException
	 */
	public JXElement addTaskReq(UtopiaRequest anUtopiaRequest) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaRequest.getRequestCommand();
		Oase oase = HandlerUtil.getOase(anUtopiaRequest);

		// Id is required
		String gameId = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr("game id", gameId);
		HandlerUtil.throwOnMissingChildElement(requestElement, "task");

		JXElement taskElm = requestElement.getChildByTag("task");
		HandlerUtil.throwOnMissingChildElement(taskElm, "name");
		HandlerUtil.throwOnMissingChildElement(taskElm, "description");
		HandlerUtil.throwOnMissingChildElement(taskElm, "score");
		HandlerUtil.throwOnMissingChildElement(taskElm, "answer");
		HandlerUtil.throwOnMissingChildElement(taskElm, "mediumid");
		HandlerUtil.throwOnMissingChildElement(taskElm, Location.FIELD_LON);
		HandlerUtil.throwOnMissingChildElement(taskElm, Location.FIELD_LAT);

		Record taskRecord = oase.getModifier().create("task");
		taskRecord.setField("name", taskElm.getChildText("name"));
		taskRecord.setField("description", taskElm.getChildText("description"));
		taskRecord.setField("score", taskElm.getChildText("score"));
		taskRecord.setField("name", taskElm.getChildText("name"));
		taskRecord.setField("answer", taskElm.getChildText("answer"));
		oase.getModifier().insert(taskRecord);

		// Couple to location
		Location location = Location.create(oase);
		String lon = taskElm.getChildText(Location.FIELD_LON);
		String lat = taskElm.getChildText(Location.FIELD_LAT);
		long time = System.currentTimeMillis();

		location.setPoint(Double.parseDouble(lon), Double.parseDouble(lat), 0.0D, time);
		location.saveInsert();

		int mediumId = Integer.parseInt(taskElm.getChildText("mediumid"));

		location.createRelation(taskRecord.getId(), "task");
		location.createRelation(Integer.parseInt(gameId), "medium");

		JXElement response = createResponse(GAME_ADD_TASK_SERVICE);
		response.setAttr(ID_FIELD, taskRecord.getId());

		return response;
	}

	public JXElement createGame(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		try {
			JXElement requestElement = anUtopiaRequest.getRequestCommand();
			JXElement contentElement = requestElement.getChildAt(0);
			Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();

			ContentLogic contentLogic = new ContentLogic(oase, config);

			// Set owner to person creating the game
			int personId = Integer.parseInt(anUtopiaRequest.getUtopiaSession().getContext().getUserId());
			contentElement.addTextChild(OWNER_FIELD, personId + "");

			// Inserts core game fields like name, description
			int gameId = contentLogic.insertContent(contentElement);

			// automagically create a game schedule for later
			/* JXElement gameScheduleElm = new JXElement(GAME_SCHEDULE_TABLE);
						gameScheduleElm.addTextChild(OWNER_FIELD, personId + "");
						int gameScheduleId = contentLogic.insertContent(gameScheduleElm);
						relateLogic.relate(gameId, gameScheduleId, null);  */

			JXElement response = createResponse(GAME_CREATE_SERVICE);
			response.setAttr(ID_FIELD, gameId);
			// response.setAttr("gamescheduleid", gameScheduleId);

			return response;
		} catch (UtopiaException ue) {
			throw ue;
		} catch (Throwable t) {
			throw new UtopiaException(t);
		}
	}

	public JXElement deleteGame(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		try {
			JXElement requestElement = anUtopiaRequest.getRequestCommand();
			ContentLogic contentLogic = createContentLogic(anUtopiaRequest);

			// Id is required
			HandlerUtil.throwOnNonNumAttr(ID_FIELD, requestElement.getAttr(ID_FIELD));
			int gameId = requestElement.getIntAttr(ID_FIELD);

			/* JXElement[] gameScheduleElms = relateLogic.getRelated(gameId, GAME_SCHEDULE_TABLE, null, null);
						for(int i=0;i<gameScheduleElms.length;i++){
							contentLogic.deleteContent(gameScheduleElms[i].getIntAttr(ID_FIELD));
						} */
			contentLogic.deleteContent(gameId);

			JXElement response = createResponse(GAME_DELETE_SERVICE);
			response.setAttr(ID_FIELD, gameId);
			return response;
		} catch (Throwable t) {
			throw new UtopiaException(t);
		}
	}

	/**
	 * Deletes an medium from the game and its location.
	 *
	 * @param anUtopiaRequest
	 * @return
	 * @throws OaseException
	 * @throws UtopiaException
	 */
	public JXElement deleteMediumReq(UtopiaRequest anUtopiaRequest) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaRequest.getRequestCommand();
		Oase oase = HandlerUtil.getOase(anUtopiaRequest);

		// Id is required
		String gameId = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr("game id", gameId);
		String mediumStrId = requestElement.getAttr("mediumid");
		HandlerUtil.throwOnNonNumAttr("medium id", mediumStrId);
        int mediumId = Integer.parseInt(mediumStrId);
		Record mediumRecord = oase.getFinder().read(mediumId, Medium.TABLE_NAME);

		Record locationRecord = oase.getRelater().getRelated(mediumRecord, Location.TABLE_NAME, null)[0];
		oase.getModifier().delete(locationRecord);
		oase.getModifier().delete(mediumRecord);

		JXElement response = createResponse(GAME_DEL_MEDIUM_SERVICE);
		response.setAttr(ID_FIELD, mediumId);

		return response;
	}

	public JXElement updateGame(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		try {
			JXElement requestElement = anUtopiaRequest.getRequestCommand();
			Oase oase = HandlerUtil.getOase(anUtopiaRequest);
			ContentLogic contentLogic = new ContentLogic(oase, config);
			RelateLogic relateLogic = createRelateLogic(anUtopiaRequest);

 			// Id is required
			HandlerUtil.throwOnNonNumAttr(ID_FIELD, requestElement.getAttr(ID_FIELD));

			JXElement gameElm = requestElement.getChildByTag(GAME_TABLE);
			if (gameElm == null)
				throw new UtopiaException("No game content found to update", ErrorCode.__7003_missing_XML_element);

			int id = requestElement.getIntAttr(ID_FIELD);
			contentLogic.updateContent(id, gameElm);

			// add intro and outro
			List mediumElms = requestElement.getChildrenByTag(Medium.TABLE_NAME);
			if (mediumElms != null) {
				for (int i = 0; i < mediumElms.size(); i++) {
					JXElement mediumElm = (JXElement) mediumElms.get(i);
					int mediumId = mediumElm.getIntAttr(Medium.ID_FIELD);
					String type = mediumElm.getAttr(TYPE_FIELD);
					if (mediumId != -1) {
						if (type != null && type.length() > 0) {
							if (type.equals(RELTAG_INTRO)) {
								relateLogic.unrelate(id, Medium.TABLE_NAME, RELTAG_INTRO);
								relateLogic.relate(id, mediumId, RELTAG_INTRO);
							} else if (type.equals(RELTAG_OUTRO)) {
								relateLogic.unrelate(id, Medium.TABLE_NAME, RELTAG_OUTRO);
								relateLogic.relate(id, mediumId, RELTAG_OUTRO);
							}
						}
					}
				}
			}

			// add tags
			List tagElms = requestElement.getChildrenByTag(TAG_FIELD);
			String[] tags = new String[tagElms.size()];
			for (int i = 0; i < tagElms.size(); i++) {
				tags[i] = ((JXElement) tagElms.get(i)).getText();
			}

			if (tags.length > 0) {
				TagLogic tagLogic = new TagLogic(HandlerUtil.getOase(anUtopiaRequest).getOaseSession());
				int taggerId = Integer.parseInt(anUtopiaRequest.getUtopiaSession().getContext().getUserId());
				int[] items = {id};
				tagLogic.tag(taggerId, items, tags, org.keyworx.plugin.tagging.util.Constants.MODE_REPLACE);
			}

			// add gameplay
			/*List gameplayElms = requestElement.getChildrenByTag(GAMEPLAY_TABLE);
						   if (gameplayElms != null) {
							   RelateLogic relateLogic = new RelateLogic(oase, null);
							   // first unrelate the current gameplay
							   relateLogic.unrelate(gameId, GAMEPLAY_TABLE, null);
							   for (int i = 0; i < gameplayElms.size(); i++) {
								   JXElement gameplayElm = (JXElement) gameplayElms.get(i);
								   String gameplayId = gameplayElm.getAttr(ID_FIELD);
								   if (gameplayId != null && gameplayId.length() > 0 && Java.isInt(gameplayId)) {
									   relateLogic.relate(gameId, Integer.parseInt(gameplayId), null);
								   }
							   }
						   }*/

			return createResponse(GAME_UPDATE_SERVICE);
		} catch (UtopiaException ue) {
			throw ue;
		} catch (Throwable t) {
			throw new UtopiaException(t);
		}
	}

	/**
	 * Overridden to have a hook to do the initialisation.
	 *
	 * @param aKey
	 * @param aValue
	 * @see org.keyworx.utopia.core.control.Handler#setProperty(String,String)
	 */
	public void setProperty(String aKey, String aValue) {
		if (aKey.equals("config")) {
			try {
				config = ContentHandlerConfig.getConfiguration(aValue);
			}
			catch (Exception e) {
				log.error("Exception while processing content handler configuration.", e);
				throw new RuntimeException("Exception while processing content handler configuration.", e);
			}

		}
		super.setProperty(aKey, aValue);
	}


	/**
	 * Create ContentLogic object from Utopia request.
	 *
	 * @param anUtopiaRequest the request object
	 * @return ContentLogic object
	 */
	protected ContentLogic createContentLogic(UtopiaRequest anUtopiaRequest) {
		return new ContentLogic(HandlerUtil.getOase(anUtopiaRequest), config);
	}

	/**
	 * Create RelateLogic object from Utopia request.
	 *
	 * @param anUtopiaRequest the request object
	 * @return RelateLogic object
	 */
	protected RelateLogic createRelateLogic(UtopiaRequest anUtopiaRequest) {
		return new RelateLogic(HandlerUtil.getOase(anUtopiaRequest), config);
	}

}
