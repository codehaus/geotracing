package nl.diwi.control;

// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$


import nl.diwi.logic.LogLogic;
import nl.diwi.logic.RouteLogic;
import nl.diwi.logic.MapLogic;
import nl.diwi.logic.NavigationLogic;
import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.postgis.PGbox2d;
import org.geotracing.handler.HandlerUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    public final static String ROUTE_GET_SERVICE = "route-get";
    public final static String ROUTE_GETLIST_SERVICE = "route-getlist";
    public final static String ROUTE_GET_MAP_SERVICE = "route-get-map";

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
            } else if (service.equals(ROUTE_GET_SERVICE)) {
                response = getRoute(anUtopiaReq);
            } else if (service.equals(ROUTE_GETLIST_SERVICE)) {
                response = getRoutes(anUtopiaReq);
            } else if (service.equals(ROUTE_GET_MAP_SERVICE)) {
                response = getMap(anUtopiaReq);
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


        LogLogic logLogic = new LogLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        JXElement req = anUtopiaReq.getRequestCommand();
        req.addChild(response);
        logLogic.storeLogEvent(anUtopiaReq.getUtopiaSession().getContext().getUserId(), req, LOG_WEB_TYPE);

        // Always return a response
        log.trace("Handled service=" + service + " response=" + response.getTag());
        return new UtopiaResponse(response);
    }

    private JXElement getMap(UtopiaRequest anUtopiaReq) throws UtopiaException {
        RouteLogic logic = createLogic(anUtopiaReq);
		int routeId = Integer.parseInt(anUtopiaReq.getRequestCommand().getAttr(ID_FIELD));
		int height = Integer.parseInt(anUtopiaReq.getRequestCommand().getAttr(HEIGHT_FIELD));
		int width = Integer.parseInt(anUtopiaReq.getRequestCommand().getAttr(WIDTH_FIELD));

		JXElement response = createResponse(ROUTE_GET_MAP_SERVICE);
		MapLogic mapLogic = new MapLogic();
		PGbox2d bbox = logic.getBBox(routeId);
		int personId = HandlerUtil.getUserId(anUtopiaReq);

		NavigationLogic navLogic = new NavigationLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
		String url = mapLogic.getMapURL(routeId, navLogic.isUserContentEnabled(personId), bbox.getLLB(), bbox.getURT(), width, height);

		try {
			response.setAttr(URL_FIELD, URLEncoder.encode(url, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new UtopiaException("Exception in getMap", e);
		}
        return response;
    }

    private JXElement getRoute(UtopiaRequest anUtopiaReq) throws UtopiaException {
        RouteLogic logic = createLogic(anUtopiaReq);
        JXElement routeElm = logic.getRoute(Integer.parseInt(anUtopiaReq.getRequestCommand().getAttr(ID_FIELD)));

        JXElement response = createResponse(ROUTE_GET_SERVICE);
        response.addChild(routeElm);

        return response;
    }

    private JXElement getRoutes(UtopiaRequest anUtopiaReq) throws UtopiaException {
        RouteLogic logic = createLogic(anUtopiaReq);
        String type = anUtopiaReq.getRequestCommand().getAttr(TYPE_FIELD);
        String personId = anUtopiaReq.getUtopiaSession().getContext().getUserId();
        Vector routes = logic.getRoutes(type, personId);

        JXElement response = createResponse(ROUTE_GETLIST_SERVICE);
        response.addChildren(routes);

        return response;
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
        JXElement reqElm = anUtopiaReq.getRequestCommand();
        // ok so this person is the one generating the routes!!
        int personId = Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());

        JXElement route = logic.generateRoute(reqElm, personId, GENERATE_PREFS_ROUTE);
        JXElement response = createResponse(ROUTE_GENERATE_SERVICE);
        response.addChild(route);

        return response;
    }

    /**
     * Get user (Person) id from request.
     *
     * @param anUtopiaReq
     * @return
     * @throws UtopiaException
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
