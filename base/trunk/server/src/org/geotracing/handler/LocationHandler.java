// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.handler;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.gis.PostGISUtil;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Sys;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.oase.api.*;

/**
 * Handles services related to Locations.
 * <p/>
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class LocationHandler extends DefaultHandler {
	public final static String LOC_CREATE_SERVICE = "loc-create";
	public final static String LOC_DELETE_SERVICE = "loc-delete";
	// public final static String LOC_UPDATE_SERVICE = "loc-update";

	public final static String ATTR_NAME = "name";
	public final static String ATTR_DESCRIPTION = "description";
	public final static String ATTR_LON = "lon";
	public final static String ATTR_LAT = "lat";
	public final static String ATTR_RELATE_IDS = "relateids";
	public final static String ATTR_ID = "id";
	public final static String ATTR_TAGS = "tags";
	public final static String ATTR_STATE = "state";
	public final static String ATTR_TYPE = "type";
	public final static String ATTR_SUBTYPE = "subtype";

	public final static String RELTAG_LOC = "loctag";

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
		log.trace("Handling request for service=" + service);

		JXElement response;
		try {
			if (service.equals(LOC_CREATE_SERVICE)) {
				response = createReq(anUtopiaReq);
			} else if (service.equals(LOC_DELETE_SERVICE)) {
				response = deleteReq(anUtopiaReq);
			} else {
				// Unknown request
				response = HandlerUtil.unknownReq(anUtopiaReq);
			}
		} catch (UtopiaException ue) {
			log.warn("Negative response service=" + service, ue);
			response = createNegativeResponse(service, ue.getErrorCode(), ue.getMessage());
		} catch (Throwable t) {
			log.error("Unexpected error service=" + service, t);
			response = createNegativeResponse(service, ErrorCode.__6005_Unexpected_error, "Unexpected error in request " + t);
		}

		// Always return a response
		log.trace("Handled service=" + service + " response=" + response.getTag());
		return new UtopiaResponse(response);
	}

	/**
	 * Create new Location.
	 * <p/>
	 * Examples
	 * <code>
	 * &lt;loc-create-req &gt; &lt;
	 * <p/>
	 * &lt;loc-create-rsp id="locationid" &gt; &lt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement createReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();

		// Create and return response with open track id.
		JXElement response = createResponse(LOC_CREATE_SERVICE);
		String name = reqElm.getAttr(ATTR_NAME);
		if (name == null || name.length() == 0) {
			name = "unnamed";
		}

		double lon = reqElm.getDoubleAttr(ATTR_LON);
		double lat = reqElm.getDoubleAttr(ATTR_LAT);
		long time = Sys.now();

		// Create and insert location
		Location location = Location.create(anUtopiaReq.getUtopiaSession().getContext().getOase());
		location.setPoint(PostGISUtil.createPoint(lon, lat, 0.0d, time));
		location.setStringValue(Location.FIELD_NAME, name);

		// Add optional type to location
		if (reqElm.hasAttr(ATTR_TYPE)) {
			location.setIntValue(Location.FIELD_TYPE, reqElm.getIntAttr(ATTR_TYPE));
		}

		// Add optional subtype to location
		if (reqElm.hasAttr(ATTR_SUBTYPE)) {
			location.setIntValue(Location.FIELD_SUBTYPE, reqElm.getIntAttr(ATTR_SUBTYPE));
		}

		location.saveInsert();

		// Create relation with Person
		location.createRelation(HandlerUtil.getUserId(anUtopiaReq), "location");

		// Optional: relate medium to other records
		if (reqElm.hasAttr(ATTR_RELATE_IDS)) {
			String relateIds[] = reqElm.getAttr(ATTR_RELATE_IDS).split(",");
			for (int i=0; i < relateIds.length; i++) {
				int nextId = Integer.parseInt(relateIds[i]);
				location.createRelation(nextId, RELTAG_LOC);
			}
		}

		response.setAttr(ATTR_ID, location.getId());
		return response;
	}

	/**
	 * Delete location.
	 * <p/>
	 * <p/>
	 * Example
	 * <code>
	 * &lt;loc-delete-req [id="loc-id"] /&gt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement deleteReq(UtopiaRequest anUtopiaReq) throws UtopiaException, OaseException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();
		int id = reqElm.getIntAttr(ATTR_ID);
		HandlerUtil.throwOnNegNumAttr(ATTR_ID, id);

		Oase oase = HandlerUtil.getOase(anUtopiaReq);
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();

		// Get location record
		Record location = finder.read(id, Location.TABLE_NAME);
		if (location == null) {
			throw new UtopiaException("Invalid location id: " + id, ErrorCode.__6004_Invalid_attribute_value);
		}

		// Get record of person in session
		// and check if location is related to logged in user
		Record person = HandlerUtil.getPersonRecord(anUtopiaReq);
		HandlerUtil.throwIfNotOwner(oase, person, location);

		// All ok, delete related first
		Record[] related = relater.getRelated(location, null, RELTAG_LOC);
		for (int i=0; i < related.length; i++) {
			modifier.delete(related[i]);
		}

		// Delete location itself
		modifier.delete(location);

		JXElement response = createResponse(LOC_DELETE_SERVICE);
		response.setAttr(ATTR_ID, id);

		return response;
	}

}



