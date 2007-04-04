// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.handler;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.gis.PostGISUtil;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Sys;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.Account;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.session.UtopiaSessionContext;
import org.keyworx.utopia.core.util.Oase;

/**
 * Handles all operations related to Tracks.
 * <p/>
 * Redirects the requests to TrackLogic methods.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class LocationHandler extends DefaultHandler {
	public final static String LOC_CREATE_SERVICE = "loc-create";
	public final static String LOC_DELETE_SERVICE = "loc-delete";

	public final static String ATTR_NAME = "name";
	public final static String ATTR_DESCRIPTION = "description";
	public final static String ATTR_LON = "lon";
	public final static String ATTR_LAT = "lat";
	public final static String ATTR_RELATE_IDS = "relateids";
	public final static String ATTR_ID = "id";
	public final static String ATTR_STATE = "state";

	protected TrackLogic trackLogic;

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

		JXElement response = null;
		try {
			if (service.equals(LOC_CREATE_SERVICE)) {
				response = createReq(anUtopiaReq);
			} else if (service.equals(LOC_DELETE_SERVICE)) {
				response = deleteReq(anUtopiaReq);
			} else {
				// To be overridden in subclass
				response = unknownReq(anUtopiaReq);
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
		trackLogic = null;
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
		location.saveInsert();

		// Create relation with Person
		location.createRelation(HandlerUtil.getUserId(anUtopiaReq), "location");
		
		if (reqElm.hasAttr(ATTR_RELATE_IDS)) {
			String relateIds[] = reqElm.getAttr(ATTR_RELATE_IDS).split(",");
			for (int i=0; i < relateIds.length; i++) {
				int nextId = Integer.parseInt(relateIds[i]);
				location.createRelation(nextId, "loctag");
			}
		}

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
	public JXElement deleteReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();
		JXElement response = createResponse(LOC_DELETE_SERVICE);

		return response;
	}

/**
	 * Default implementation for unknown service request.
	 * <p/>
	 * Override this method in extended class for handling additional
	 * requests.
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A negative UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement unknownReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		String service = anUtopiaReq.getServiceName();
		Logging.getLog(anUtopiaReq).warn("Unknown service " + service);
		return createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
	}
	/**
	 * Get user Account from request.
	 */
	protected Account getAccount(UtopiaRequest anUtopiaReq) throws UtopiaException {

		// Get account name for event subject
		// Expensive but we have to
		UtopiaSessionContext sc = anUtopiaReq.getUtopiaSession().getContext();
		Oase oase = sc.getOase();
		Person person = (Person) oase.get(Person.class, HandlerUtil.getUserId(anUtopiaReq) + "");
		return person.getAccount();
	}


}



