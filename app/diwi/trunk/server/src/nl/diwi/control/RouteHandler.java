package nl.diwi.control;

// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$



import nl.justobjects.jox.dom.JXElement;
import nl.diwi.logic.RouteLogic;
import nl.diwi.util.Constants;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.XML;

import java.util.Vector;

/**
 * Handles all operations related to commenting.
 * <p/>
 * Redirects the requests to CommentLogic methods.
 *
 * @author Just van den Broecke
 * @version $Id: CommentHandler.java 361 2007-02-05 21:34:58Z just $
 */
public class RouteHandler extends DefaultHandler implements Constants {

    public final static String ROUTE_GENERATE_SERVICE = "route-generate";
    public final static String ROUTE_SAVE_SERVICE = "route-save";
    

    /**
	 * Processes the Client Request.
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws UtopiaException standard Utopia exception
	 */
	public UtopiaResponse processRequest(UtopiaRequest anUtopiaReq) throws UtopiaException {
		Log log = Logging.getLog(anUtopiaReq);

		// Get the service name for the request
		String service = anUtopiaReq.getServiceName();
		log.trace("Handling request for service=" + service);

		JXElement response;
		try {
			if (service.equals(ROUTE_GENERATE_SERVICE)) {
				response = generateRoute(anUtopiaReq);
			} else if (service.equals(ROUTE_SAVE_SERVICE)) {
				response = saveRoute(anUtopiaReq);
			} else {
				// May be overridden in subclass
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
		return new UtopiaResponse(response);
	}

	/*
        <route-generate-req >
             <pref name="bos" value="40" type="outdoor-params" />
             <pref name="heide" value="20" type="outdoor-params" />
             <pref name="bebouwing" value="10" type="outdoor-params" />
             <pref name="theme" value="forts" type="theme" />
             <pref name="activity" value="wandelaar" type="activity" />

        </route-generate-req>
     */
    protected JXElement generateRoute(UtopiaRequest anUtopiaReq) throws UtopiaException {
        RouteLogic logic = createLogic(anUtopiaReq);
        Record route = logic.generateRoute(anUtopiaReq);

		//Convert Route record to XML and add to result
        JXElement routeElm = null;
		try {
			routeElm = XML.createElementFromRecord(ROUTE_ELM, route);
			
			//replace path data with calculated distance
			routeElm.removeChildByTag(PATH_FIELD);
			// calculate length of route
			//routeElm.setAttr(DISTANCE_ATTR, (()route.getObjectField(PATH_FIELD).)
					
		} catch (UtopiaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}            
        
        JXElement response = createResponse(ROUTE_GENERATE_SERVICE);
        response.addChild(routeElm);

        return response;
	}

    private JXElement saveRoute(UtopiaRequest anUtopiaReq) throws UtopiaException {
        // Create and return response with genera
		JXElement response = createResponse(ROUTE_SAVE_SERVICE);

        return response;
	}
    
    /**
	 * Get user (Person) id from request.
	 */
	protected RouteLogic createLogic(UtopiaRequest anUtopiaReq) throws UtopiaException {
		return new RouteLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
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
	protected JXElement unknownReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		String service = anUtopiaReq.getServiceName();
		Logging.getLog(anUtopiaReq).warn("Unknown service " + service);
		return createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
	}

}
