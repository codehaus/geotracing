package nl.diwi.control;

import nl.diwi.logic.*;
import nl.diwi.util.Constants;
import nl.diwi.util.ProjectionConversionUtil;
import nl.justobjects.jox.dom.JXElement;
import org.geotracing.gis.Transform;
import org.geotracing.gis.PostGISUtil;
import org.geotracing.handler.HandlerUtil;
import org.geotracing.handler.Location;
import org.geotracing.handler.Track;
import org.geotracing.handler.TrackLogic;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.api.Relater;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.control.ThreadSafe;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;
import org.postgis.Point;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Vector;

public class NavigationHandler extends DefaultHandler implements ThreadSafe, Constants {

    // Keyworx services
    public final static String NAV_GET_STATE_SERVICE = "nav-get-state";
    public final static String NAV_GET_MAP_SERVICE = "nav-get-map";
    public final static String NAV_POINT_SERVICE = "nav-point";
    public final static String NAV_START_SERVICE = "nav-start";
    public final static String NAV_STOP_SERVICE = "nav-stop";
    public final static String NAV_ACTIVATE_ROUTE_SERVICE = "nav-activate-route";
    public final static String NAV_DEACTIVATE_ROUTE_SERVICE = "nav-deactivate-route";
    public final static String NAV_ADD_MEDIUM_SERVICE = "nav-add-medium";
    public final static String NAV_TOGGLE_UGC_SERVICE = "nav-toggle-ugc";
    public final static String NAV_UGC_ON_SERVICE = "nav-ugc-on";
    public final static String NAV_UGC_OFF_SERVICE = "nav-ugc-off";
    public final static String NAV_POI_GET_SERVICE = "nav-poi-get";
    public final static String NAV_ROUTE_GET_SERVICE = "nav-route-get";
    public final static String NAV_ROUTE_GETLIST_SERVICE = "nav-route-getlist";
    public final static String NAV_ROUTE_HOME_SERVICE = "nav-route-home";

    /**
	 * Processes the Client Request.
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws UtopiaException standard Utopia exception
	 */
	public UtopiaResponse processRequest(UtopiaRequest anUtopiaReq) throws UtopiaException {
		Log log = Logging.getLog(anUtopiaReq);
		String service = anUtopiaReq.getServiceName();
		log.trace("Handling request for service=" + service);
		log.info("###### NAV request:" + new String(anUtopiaReq.getRequestCommand().toBytes(false)));

		JXElement response;
		try {
			if (service.equals(NAV_GET_MAP_SERVICE)) {
				response = getMap(anUtopiaReq);
			} else if (service.equals(NAV_START_SERVICE)) {
				response = startNavigation(anUtopiaReq);
			} else if (service.equals(NAV_STOP_SERVICE)) {
				response = stopNavigation(anUtopiaReq);
			} else if (service.equals(NAV_POINT_SERVICE)) {
				response = handlePoint(anUtopiaReq);
			} else if (service.equals(NAV_ACTIVATE_ROUTE_SERVICE)) {
				response = activateRoute(anUtopiaReq);
			} else if (service.equals(NAV_DEACTIVATE_ROUTE_SERVICE)) {
				response = deactivateRoute(anUtopiaReq);
			} else if (service.equals(NAV_ADD_MEDIUM_SERVICE)) {
				response = addMedium(anUtopiaReq);
			} else if (service.equals(NAV_GET_STATE_SERVICE)) {
				response = getState(anUtopiaReq);
			} else if (service.equals(NAV_TOGGLE_UGC_SERVICE)) {
				response = toggleUGC(anUtopiaReq);
			} else if (service.equals(NAV_UGC_ON_SERVICE)) {
				response = UGCOn(anUtopiaReq);
			} else if (service.equals(NAV_UGC_OFF_SERVICE)) {
				response = UGCOff(anUtopiaReq);
			} else if (service.equals(NAV_POI_GET_SERVICE)) {
				response = getPoi(anUtopiaReq);
			} else if (service.equals(NAV_ROUTE_GET_SERVICE)) {
				response = getRoute(anUtopiaReq);
			} else if (service.equals(NAV_ROUTE_GETLIST_SERVICE)) {
				response = getRoutes(anUtopiaReq);
			} else if (service.equals(NAV_ROUTE_HOME_SERVICE)) {
				response = getRouteHome(anUtopiaReq);
			} else {
				// May be overridden in subclass
				response = unknownReq(anUtopiaReq);
			}

            log.info("###### NAV response:" + new String(response.toBytes(false)));

		} catch (UtopiaException ue) {
			log.warn("Negative response service=" + service, ue);
			response = createNegativeResponse(service, ue.getErrorCode(), ue.getMessage());
		} catch (Throwable t) {
			log.error("Unexpected error service=" + service, t);
			response = createNegativeResponse(service, ErrorCode.__6005_Unexpected_error, "Unexpected error in request " + t);
		}

        LogLogic logLogic = new LogLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        if(!service.equals(NAV_POINT_SERVICE)){
            JXElement req = anUtopiaReq.getRequestCommand();
            //req.addChild(response);
            logLogic.storeLogEvent(anUtopiaReq.getUtopiaSession().getContext().getUserId(), req, LOG_MOBILE_TYPE);
        }

        // Always return a response
		log.trace("Handled service=" + service + " response=" + response.getTag());
		return new UtopiaResponse(response);
	}

    protected JXElement getRouteHome(UtopiaRequest anUtopiaReq) throws UtopiaException {
        RouteLogic logic = new RouteLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        JXElement reqElm = anUtopiaReq.getRequestCommand();
        // ok so this person is the one generating the routes!!
        int personId = Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());

        JXElement route = logic.generateRoute(reqElm, personId, GENERATE_HOME_ROUTE);
        JXElement response = createResponse(NAV_ROUTE_HOME_SERVICE);
        response.addChild(route);

        return response;
    }

    /**
     * Gets a poi.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws UtopiaException standard Utopia exception
     */
    protected JXElement getPoi(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement reqElm = anUtopiaReq.getRequestCommand();
        POILogic poiLogic = new POILogic(anUtopiaReq.getUtopiaSession().getContext().getOase());

        int personId = Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());

        JXElement response = createResponse(NAV_POI_GET_SERVICE);
        String id = reqElm.getAttr(ID_FIELD);
        response.addChild(poiLogic.get(personId, Integer.parseInt(id)));
        return response;
    }

    private JXElement getRoute(UtopiaRequest anUtopiaReq) throws UtopiaException {
        RouteLogic logic = new RouteLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        JXElement routeElm = logic.getRoute(Integer.parseInt(anUtopiaReq.getRequestCommand().getAttr(ID_FIELD)));

        JXElement response = createResponse(NAV_ROUTE_GET_SERVICE);
        response.addChild(routeElm);

        return response;
    }

    private JXElement getRoutes(UtopiaRequest anUtopiaReq) throws UtopiaException {
        RouteLogic logic = new RouteLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        String type = anUtopiaReq.getRequestCommand().getAttr(TYPE_FIELD);
        String personId = anUtopiaReq.getUtopiaSession().getContext().getUserId();
        Vector routes = logic.getRoutes(type, personId);

        JXElement response = createResponse(NAV_ROUTE_GETLIST_SERVICE);
        response.addChildren(routes);

        return response;
    }

    private JXElement deactivateRoute(UtopiaRequest anUtopiaReq) throws UtopiaException {
        NavigationLogic logic = createLogic(anUtopiaReq);
		int personId = Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());

		logic.deactivateRoute(personId);

		return createResponse(NAV_DEACTIVATE_ROUTE_SERVICE);
	}

	private JXElement toggleUGC(UtopiaRequest anUtopiaReq) throws UtopiaException {
		NavigationLogic logic = createLogic(anUtopiaReq);
		logic.toggleUGC(anUtopiaReq.getUtopiaSession().getContext().getUserId());
		return createResponse(NAV_TOGGLE_UGC_SERVICE);
	}

    private JXElement UGCOn(UtopiaRequest anUtopiaReq) throws UtopiaException {
		NavigationLogic logic = createLogic(anUtopiaReq);
		logic.setUGC(anUtopiaReq.getUtopiaSession().getContext().getUserId(), true);
		return createResponse(NAV_UGC_ON_SERVICE);
	}

    private JXElement UGCOff(UtopiaRequest anUtopiaReq) throws UtopiaException {
		NavigationLogic logic = createLogic(anUtopiaReq);
		logic.setUGC(anUtopiaReq.getUtopiaSession().getContext().getUserId(), false);
		return createResponse(NAV_UGC_OFF_SERVICE);
	}

    private JXElement activateRoute(UtopiaRequest anUtopiaReq) throws UtopiaException {
		NavigationLogic logic = createLogic(anUtopiaReq);
		JXElement reqElm = anUtopiaReq.getRequestCommand();
		int personId = Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());
		int routeId = Integer.parseInt(anUtopiaReq.getRequestCommand().getAttr(ID_FIELD));
		String initString = anUtopiaReq.getRequestCommand().getAttr(INIT_FIELD);
		boolean init = false;
		if (initString != null && initString.toLowerCase().equals("true")) {
			init = true;
		}

		logic.activateRoute(routeId, personId, init);

		return createResponse(NAV_ACTIVATE_ROUTE_SERVICE);
	}

	private JXElement getState(UtopiaRequest anUtopiaReq) throws UtopiaException {
		NavigationLogic logic = createLogic(anUtopiaReq);
		int personId = Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());

		JXElement response = createResponse(NAV_GET_STATE_SERVICE);
		Record route = logic.getActiveRoute(personId);
		if (route != null) {
			response.setAttr("routeid", route.getId());
		}

        return response;
	}

	private JXElement handlePoint(UtopiaRequest anUtopiaReq) throws UtopiaException {
        LogLogic logLogic = new LogLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());

        JXElement reqElm = anUtopiaReq.getRequestCommand();
        Oase oase = anUtopiaReq.getUtopiaSession().getContext().getOase();
        TrackLogic trackLogic = new TrackLogic(oase);
		NavigationLogic navLogic = new NavigationLogic(oase);

		//result contains 'pt' elements with everything filled out if an EMEA string was sent.
		// add x and y field before sending it to TrackLogic
		JXElement ptElement = (JXElement) (reqElm.getChildren().get(0));
		double lon = Double.parseDouble(ptElement.getAttr(LON_FIELD));
		double lat = Double.parseDouble(ptElement.getAttr(LAT_FIELD));
		double xy[];
		try {
			xy = Transform.WGS84toRD(lon, lat);
		} catch (Exception e) {
			throw new UtopiaException("No valid lat and lon coordinates found");
		}

		double x = xy[0];
		double y = xy[1];
		ptElement.setAttr(X_FIELD, x);
		ptElement.setAttr(Y_FIELD, y);

		// write to track
        trackLogic.write(reqElm.getChildren(), HandlerUtil.getUserId(anUtopiaReq));

        Vector result = new Vector(3);

		Point point = new Point(x, y);
		point.setSrid(EPSG_DUTCH_RD);
		result.addAll(navLogic.checkPoint(point, HandlerUtil.getUserId(anUtopiaReq)));

        // store all poi-hit, ugc-hit and roam messages
        for(int i=0;i<result.size();i++){
            JXElement elm = (JXElement)result.elementAt(i);
            logLogic.storeLogEvent(anUtopiaReq.getUtopiaSession().getContext().getUserId(), elm, LOG_MOBILE_TYPE);
        }

        JXElement response = createResponse(NAV_POINT_SERVICE);
		response.addChildren(result);

		return response;
	}

	private JXElement stopNavigation(UtopiaRequest anUtopiaReq) throws UtopiaException {
        // LogLogic logLogic = new LogLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());

        TrackLogic trackLogic = new TrackLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());

		// Resume current Track for this user
		trackLogic.suspend(HandlerUtil.getUserId(anUtopiaReq), System.currentTimeMillis());
        
        // close this trip
		// logLogic.closeLogs(anUtopiaReq.getUtopiaSession().getContext().getUserId(), LOG_MOBILE_TYPE);

		// Create and return response with open track id.
		return createResponse(NAV_STOP_SERVICE);
	}

	private JXElement startNavigation(UtopiaRequest anUtopiaReq) throws UtopiaException {
        LogLogic logLogic = new LogLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        
        Oase oase = anUtopiaReq.getUtopiaSession().getContext().getOase();
		TrackLogic trackLogic = new TrackLogic(oase);

		// Resume current Track for this user
		Track track = trackLogic.resume(HandlerUtil.getUserId(anUtopiaReq), Track.VAL_DAY_TRACK, System.currentTimeMillis());

		// close previous trip
		logLogic.closeLogByTime(anUtopiaReq.getUtopiaSession().getContext().getUserId(), LOG_MOBILE_TYPE);

		// relate the track to the trip
		Record trip = logLogic.getOpenLog(anUtopiaReq.getUtopiaSession().getContext().getUserId(), LOG_MOBILE_TYPE);
        
        // relate the track to the trip
		try {
			oase.getRelater().relate(trip, track.getRecord());
		} catch (Throwable t) {
			throw new UtopiaException(t);
		}

		// Create and return response with open track id.
		return createResponse(NAV_START_SERVICE);
	}

	protected NavigationLogic createLogic(UtopiaRequest anUtopiaReq) throws UtopiaException {
		return new NavigationLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
	}

	private JXElement getMap(UtopiaRequest anUtopiaReq) throws UtopiaException, OaseException {
		MapLogic mapLogic = new MapLogic();
		NavigationLogic navLogic = createLogic(anUtopiaReq);

		JXElement reqElm = anUtopiaReq.getRequestCommand();
        int layerId = reqElm.getIntAttr(LAYER_ID);

        int height = reqElm.getIntAttr(HEIGHT_FIELD);
		int width = reqElm.getIntAttr(WIDTH_FIELD);
		double llbLat = reqElm.getDoubleAttr(LLB_LAT_ATTR);
		double llbLon = reqElm.getDoubleAttr(LLB_LON_ATTR);
		double urtLat = reqElm.getDoubleAttr(URL_LAT_ATTR);
		double urtLon = reqElm.getDoubleAttr(URT_LON_ATTR);

		Point llb = ProjectionConversionUtil.WGS842RD(new Point(llbLon, llbLat));
		Point urt = ProjectionConversionUtil.WGS842RD(new Point(urtLon, urtLat));

		String mapURL;
		int personId = HandlerUtil.getUserId(anUtopiaReq);
		Record route = navLogic.getActiveRoute(personId);
		if (route != null) {
			// Following a route
			String routePOIs = DIWIQueryLogic.getRoutePOIs(route.getId());
			mapURL = mapLogic.getMapURL(layerId, route.getId(), routePOIs, navLogic.isUserContentEnabled(personId), llb, urt, width, height);
		} else {
            // Roaming
            String layers = "";
            switch (layerId){
                case MAP_GREBBELINIE:
                    layers += "topnl_diwiwms";
                    break;
                case MAP_DOMPLEIN1:
                    layers += "topnl_domplein1";
                    break;
                case MAP_DOMPLEIN2:
                    layers += "topnl_domplein2";
                    break;
                case MAP_DOMPLEIN3:
                    layers += "topnl_domplein3";
                    break;
                default:
                    layers += "topnl_diwiwms";

            }

            layers += ",diwi_pois";

			if (navLogic.isUserContentEnabled(personId)) {
				layers += ",diwi_ugc";
			}
			mapURL = mapLogic.getMapURL(layers, llb, urt, width, height);
		}

		JXElement response = createResponse(NAV_GET_MAP_SERVICE);
		try {
			response.setAttr(URL_FIELD, URLEncoder.encode(mapURL, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new UtopiaException("Exception in getMap", e);
		}

		return response;
	}

    /*
    <nav-add-medium-req id="[mediumid]" lon="" lat="" />
    <play-add-medium-rsp locationid="[locationid]" />
    */
	public JXElement addMedium(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		JXElement requestElement = anUtopiaReq.getRequestCommand();
		String mediumIdStr = requestElement.getAttr(ID_FIELD);
		HandlerUtil.throwOnNonNumAttr(ID_FIELD, mediumIdStr);
		int mediumId = Integer.parseInt(mediumIdStr);
		String lon = requestElement.getAttr(LON_FIELD);
		String lat = requestElement.getAttr(LAT_FIELD);

		Oase oase = HandlerUtil.getOase(anUtopiaReq);
		Relater relater = oase.getRelater();

		int personId = HandlerUtil.getUserId(anUtopiaReq);

		// First determine medium location and add to track
		TrackLogic trackLogic = new TrackLogic(oase);

		// Adds medium to track
		Point wgsPoint = PostGISUtil.createPoint(lon, lat);
		Track track = trackLogic.getActiveTrack(personId);
		Location location = trackLogic.addLocation(track, wgsPoint, mediumId, "medium");

        /*Record r = setRDPoint(location.getRecord(), lon, lat);
        oase.getModifier().update(r);*/

		// We either have location or an exception here
		JXElement rsp = createResponse(NAV_ADD_MEDIUM_SERVICE);
		rsp.setAttr(LOCATION_ID_FIELD, location.getId());

		return rsp;
	}

    protected JXElement unknownReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		String service = anUtopiaReq.getServiceName();
		Logging.getLog(anUtopiaReq).warn("Unknown service " + service);
		return createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
	}

}
