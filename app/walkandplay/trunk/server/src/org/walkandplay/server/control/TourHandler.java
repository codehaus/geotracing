package org.walkandplay.server.control;

import nl.justobjects.jox.dom.JXElement;

import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Java;
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
import org.geotracing.server.Location;

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
			contentElement.addTextChild(OWNER_FIELD, personId+"");

			// Inserts core tour fields like name, description
			int tourId = contentLogic.insertContent(contentElement);

			JXElement response = createResponse(TOUR_CREATE_SERVICE);
			response.setAttr(ID_FIELD, tourId);

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

			// Id is required
			throwOnNonNumAttr(ID_FIELD, requestElement.getAttr(ID_FIELD));
			int tourId = requestElement.getIntAttr(ID_FIELD);

			Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
			ContentLogic contentLogic = new ContentLogic(oase, null);
			contentLogic.deleteContent(tourId);

			JXElement response = createResponse(TOUR_DELETE_SERVICE);
			response.setAttr(ID_FIELD, tourId);
			return response;
		} catch (Throwable t) {
			throw new UtopiaException(t);
		}
	}



	public JXElement updateTour(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		try {
			JXElement requestElement = anUtopiaRequest.getRequestCommand();
			// Id is required
			throwOnNonNumAttr(ID_FIELD, requestElement.getAttr(ID_FIELD));

			JXElement contentElement = requestElement.getChildAt(0);
			Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();

			// Id is required
			throwOnNonNumAttr(ID_FIELD, requestElement.getAttr(ID_FIELD));

			int tourId = requestElement.getIntAttr(ID_FIELD);

			// add media
			List mediumElms = contentElement.getChildrenByTag(Medium.TABLE_NAME);
			if (mediumElms != null) {
				RelateLogic relateLogic = new RelateLogic(oase, null);
				// first unrelate the current media
				relateLogic.unrelate(tourId, Medium.TABLE_NAME, null);
				for (int i = 0; i < mediumElms.size(); i++) {
					JXElement mediumElm = (JXElement) mediumElms.get(i);
					int mediumId = mediumElm.getIntAttr(Medium.ID_FIELD);
					String type = mediumElm.getAttr(TYPE_FIELD);
					if (mediumId != -1) {
						if (type != null && type.length() > 0) {
							if (type.equals(RELTAG_INTRO)) {
								relateLogic.relate(tourId, mediumId, RELTAG_INTRO);
							} else if (type.equals(RELTAG_OUTRO)) {
								relateLogic.relate(tourId, mediumId, RELTAG_OUTRO);
							}
						} else {
							// We also must have location
							String lon = mediumElm.getAttr("lon");
							String lat = mediumElm.getAttr("lon");
							// Add location and relate to Medium AND Tour
							Location location;
							relateLogic.relate(tourId, mediumId, RELTAG_MEDIUM);
						}
					}
				}
			}

			ContentLogic contentLogic = new ContentLogic(oase, null);
			contentLogic.updateContent(tourId, requestElement);

			// add gameplay
/*			List gameplayElms = contentElement.getChildrenByTag(GAMEPLAY_TABLE);
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
			}   */

			// add tags
			String tagsField = contentElement.getChildText(TAGS_FIELD);
			if (tagsField != null) {
				TagLogic tagLogic = new TagLogic(oase.getOaseSession());
				int taggerId = Integer.parseInt(anUtopiaRequest.getUtopiaSession().getContext().getUserId());
				int[] items = {tourId};
				String[] tags = tagsField.split(",");
				tagLogic.tag(taggerId, items, tags, org.keyworx.plugin.tagging.util.Constants.MODE_REPLACE);
			}



			// add assignments
/*			List assignmentElms = contentElement.getChildrenByTag(ASSIGNMENT_TABLE);
			if (assignmentElms != null) {
				RelateLogic relateLogic = new RelateLogic(oase, null);
				// first unrelate the current assignments
				relateLogic.unrelate(tourId, ASSIGNMENT_TABLE, null);
				for (int i = 0; i < assignmentElms.size(); i++) {
					JXElement assignmentElm = (JXElement) assignmentElms.get(i);
					String assignmentId = assignmentElm.getAttr(ASSIGNMENT_TABLE);
					if (assignmentId != null && assignmentId.length() > 0 && Java.isInt(assignmentId)) {
						relateLogic.relate(tourId, Integer.parseInt(assignmentId), null);
					}
				}
			}  */

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
	 * @see org.keyworx.utopia.core.control.Handler#setProperty(String, String)
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
	 * Throw exception when attribute empty or not present.
	 */
	protected void throwOnMissingAttr(String aName, String aValue) throws UtopiaException {
		if (aValue == null || aValue.length() == 0) {
			throw new UtopiaException("Missing name=" + aName + " value=" + aValue, ErrorCode.__6002_Required_attribute_missing);
		}
	}

	/**
	 * Throw exception when numeric attribute empty or not present.
	 */
	protected void throwOnNonNumAttr(String aName, String aValue) throws UtopiaException {
		throwOnMissingAttr(aName, aValue);
		try {
			Long.parseLong(aValue);
		} catch (Throwable t) {
			throw new UtopiaException("Invalid numvalue name=" + aName + " value=" + aValue, ErrorCode.__6004_Invalid_attribute_value);
		}
	}
}
