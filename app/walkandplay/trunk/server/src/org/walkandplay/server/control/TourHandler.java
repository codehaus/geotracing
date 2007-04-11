package org.walkandplay.server.control;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.Location;
import org.geotracing.handler.HandlerUtil;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.OaseException;
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
 * Manage Tour content.
 * <p/>
 * Redirects the requests to the right logic method
 *
 * @author Ronald Lenz
 * @version $Id$
 */
public class TourHandler extends DefaultHandler implements Constants {

	public final static String TOUR_CREATE_SERVICE = "tour-create";
	public final static String TOUR_UPDATE_SERVICE = "tour-update";
	public final static String TOUR_DELETE_SERVICE = "tour-delete";
	public final static String TOUR_ADD_ITEM_SERVICE = "tour-add-item";

	private Log log = Logging.getLog("TourHandler");
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
			if (service.equals(TOUR_CREATE_SERVICE)) {
				response = createTour(anUtopiaRequest);
			} else if (service.equals(TOUR_UPDATE_SERVICE)) {
				response = updateTour(anUtopiaRequest);
			} else if (service.equals(TOUR_DELETE_SERVICE)) {
				response = deleteTour(anUtopiaRequest);
			} else if (service.equals(TOUR_ADD_ITEM_SERVICE)) {
				response = addItem(anUtopiaRequest);
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


	public JXElement createTour(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		try {
			JXElement requestElement = anUtopiaRequest.getRequestCommand();
			JXElement contentElement = requestElement.getChildAt(0);
			Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();

			ContentLogic contentLogic = new ContentLogic(oase, config);

			// Set owner to person creating the tour
			int personId = Integer.parseInt(anUtopiaRequest.getUtopiaSession().getContext().getUserId());
			contentElement.addTextChild(OWNER_FIELD, personId + "");

			// Inserts core tour fields like name, description
			int tourId = contentLogic.insertContent(contentElement);

			// automagically create a tour schedule for later
			/* JXElement tourScheduleElm = new JXElement(TOUR_SCHEDULE_TABLE);
						tourScheduleElm.addTextChild(OWNER_FIELD, personId + "");
						int tourScheduleId = contentLogic.insertContent(tourScheduleElm);
						relateLogic.relate(tourId, tourScheduleId, null);  */

			JXElement response = createResponse(TOUR_CREATE_SERVICE);
			response.setAttr(ID_FIELD, tourId);
			// response.setAttr("tourscheduleid", tourScheduleId);

			return response;
		} catch (UtopiaException ue) {
			throw ue;
		} catch (Throwable t) {
			throw new UtopiaException(t);
		}
	}

	public JXElement deleteTour(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		try {
			JXElement requestElement = anUtopiaRequest.getRequestCommand();
			ContentLogic contentLogic = createContentLogic(anUtopiaRequest);

			// Id is required
			HandlerUtil.throwOnNonNumAttr(ID_FIELD, requestElement.getAttr(ID_FIELD));
			int tourId = requestElement.getIntAttr(ID_FIELD);

			/* JXElement[] tourScheduleElms = relateLogic.getRelated(tourId, TOUR_SCHEDULE_TABLE, null, null);
						for(int i=0;i<tourScheduleElms.length;i++){
							contentLogic.deleteContent(tourScheduleElms[i].getIntAttr(ID_FIELD));
						} */
			contentLogic.deleteContent(tourId);

			JXElement response = createResponse(TOUR_DELETE_SERVICE);
			response.setAttr(ID_FIELD, tourId);
			return response;
		} catch (Throwable t) {
			throw new UtopiaException(t);
		}
	}

	/**
	 * Adds an item to the tour based on its location. An item can be a medium or assignment object
	 *
	 * @param anUtopiaRequest
	 * @return
	 * @throws OaseException
	 * @throws UtopiaException
	 */
	public JXElement addItem(UtopiaRequest anUtopiaRequest) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaRequest.getRequestCommand();
		Oase oase = HandlerUtil.getOase(anUtopiaRequest);
		ContentLogic contentLogic = new ContentLogic(oase, config);
		RelateLogic relateLogic = createRelateLogic(anUtopiaRequest);

		// Id is required
		String tourId = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr("tour id", tourId);
		// HandlerUtil.throwOnMissingChildElement(requestElement, ASSIGNMENT_TABLE);

		JXElement item = requestElement.getChildByTag(Medium.XML_TAG);
		if (item == null) {
			item = requestElement.getChildByTag(ASSIGNMENT_TABLE);
		}
		if (item == null) {
			throw new UtopiaException("No item found to add.", ErrorCode.__7003_missing_XML_element);
		}

		// get the location element
		JXElement locationElm = requestElement.getChildByTag(LOCATION_FIELD);
		if (locationElm == null)
			throw new UtopiaException("No location found for item to add.", ErrorCode.__7003_missing_XML_element);

		Location location = Location.create(oase);
		String lon = locationElm.getChildText(Location.FIELD_LON);
		String lat = locationElm.getChildText(Location.FIELD_LAT);
		String ele = locationElm.getChildText(Location.FIELD_ELE);
		//String time = locationElm.getChildText(Location.FIELD_TIME);
		long time = System.currentTimeMillis();
		HandlerUtil.throwOnMissingAttr("lon", lon);
		HandlerUtil.throwOnMissingAttr("lat", lat);
		HandlerUtil.throwOnMissingAttr("ele", ele);
		//throwOnMissingAttr("time", time);

		location.setPoint(Double.parseDouble(lon), Double.parseDouble(lat), Double.parseDouble(ele), time);
		location.saveInsert();
		relateLogic.relate(Integer.parseInt(tourId), location.getId(), null);
		String id = "";

		if (item.getTag().equals(Medium.XML_TAG)) {
			id = item.getAttr(Medium.ID_FIELD);
			HandlerUtil.throwOnMissingAttr("medium id", id);

			relateLogic.relate(Integer.parseInt(id), location.getId(), null);

		} else if (item.getTag().equals(ASSIGNMENT_TABLE)) {
			List media = requestElement.getChildrenByTag(Medium.TABLE_NAME);
			// remove media for contentlogic to work
			log.info(new String(item.toBytes(false)));
			while (item.getChildByTag(Medium.XML_TAG) != null) {
				item.removeChildByTag(Medium.XML_TAG);
			}
			log.info(new String(item.toBytes(false)));

			id = "" + contentLogic.insertContent(item);
			if (media != null) {
				for (int i = 0; i < media.size(); i++) {
					JXElement mediumElm = (JXElement) media.get(i);
					String mediumId = mediumElm.getAttr(Medium.ID_FIELD);
					if (mediumId != null && mediumId.length() > 0) {
						relateLogic.relate(Integer.parseInt(id), Integer.parseInt(mediumId), null);
					}
				}
			}

			relateLogic.relate(Integer.parseInt(id), location.getId(), null);
		}

		JXElement response = createResponse(TOUR_ADD_ITEM_SERVICE);
		response.setAttr(ID_FIELD, id);

		return response;
	}


	public JXElement updateTour(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		try {
			JXElement requestElement = anUtopiaRequest.getRequestCommand();
			Oase oase = HandlerUtil.getOase(anUtopiaRequest);
			ContentLogic contentLogic = new ContentLogic(oase, config);
			RelateLogic relateLogic = createRelateLogic(anUtopiaRequest);

 			// Id is required
			HandlerUtil.throwOnNonNumAttr(ID_FIELD, requestElement.getAttr(ID_FIELD));

			JXElement tourElm = requestElement.getChildByTag(TOUR_TABLE);
			if (tourElm == null)
				throw new UtopiaException("No tour content found to update", ErrorCode.__7003_missing_XML_element);

			int id = requestElement.getIntAttr(ID_FIELD);
			contentLogic.updateContent(id, tourElm);

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
							   relateLogic.unrelate(tourId, GAMEPLAY_TABLE, null);
							   for (int i = 0; i < gameplayElms.size(); i++) {
								   JXElement gameplayElm = (JXElement) gameplayElms.get(i);
								   String gameplayId = gameplayElm.getAttr(ID_FIELD);
								   if (gameplayId != null && gameplayId.length() > 0 && Java.isInt(gameplayId)) {
									   relateLogic.relate(tourId, Integer.parseInt(gameplayId), null);
								   }
							   }
						   }*/

			return createResponse(TOUR_UPDATE_SERVICE);
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
