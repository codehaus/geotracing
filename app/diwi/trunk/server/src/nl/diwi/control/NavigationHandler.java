package nl.diwi.control;

import nl.diwi.logic.LogLogic;
import nl.diwi.logic.MapLogic;
import nl.diwi.logic.NavigationLogic;
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
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;
import org.postgis.Point;
import org.postgis.PGgeometryLW;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Vector;

public class NavigationHandler extends DefaultHandler implements Constants {

	LogLogic logLogic;

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
			if (service.equals(NAV_GET_MAP)) {
				response = getMap(anUtopiaReq);
			} else if (service.equals(NAV_START)) {
				response = startNavigation(anUtopiaReq);
			} else if (service.equals(NAV_STOP)) {
				response = stopNavigation(anUtopiaReq);
			} else if (service.equals(NAV_POINT)) {
				response = handlePoint(anUtopiaReq);
			} else if (service.equals(NAV_ACTIVATE_ROUTE)) {
				response = activateRoute(anUtopiaReq);
			} else if (service.equals(NAV_DEACTIVATE_ROUTE)) {
				response = deactivateRoute(anUtopiaReq);
			} else if (service.equals(NAV_ADD_MEDIUM)) {
				response = addMedium(anUtopiaReq);
			} else if (service.equals(NAV_GET_STATE)) {
				response = getState(anUtopiaReq);
			} else if (service.equals(NAV_TOGGLE_UGC)) {
				response = toggleUGC(anUtopiaReq);
			} else {
				// May be overridden in subclass
				response = unknownReq(anUtopiaReq);
			}

			// store the traffic
			LogLogic l = new LogLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
			l.storeLogEvent(anUtopiaReq.getUtopiaSession().getContext().getUserId(), anUtopiaReq.getRequestCommand(), LOG_TRAFFIC_TYPE);

			logLogic = new LogLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
			log.info("###### NAV response:" + new String(response.toBytes(false)));

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

	private JXElement deactivateRoute(UtopiaRequest anUtopiaReq) throws UtopiaException {
		NavigationLogic logic = createLogic(anUtopiaReq);
		int personId = Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());

		logic.deactivateRoute(personId);

		logLogic.storeLogEvent(anUtopiaReq.getUtopiaSession().getContext().getUserId(), anUtopiaReq.getRequestCommand(), LOG_TRIP_TYPE);

		return createResponse(NAV_DEACTIVATE_ROUTE);
	}

	private JXElement toggleUGC(UtopiaRequest anUtopiaReq) throws UtopiaException {
		NavigationLogic logic = createLogic(anUtopiaReq);
		logic.toggleUGC(anUtopiaReq.getUtopiaSession().getContext().getUserId());

		logLogic.storeLogEvent(anUtopiaReq.getUtopiaSession().getContext().getUserId(), anUtopiaReq.getRequestCommand(), LOG_TRIP_TYPE);

		return createResponse(NAV_TOGGLE_UGC);
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

		logLogic.storeLogEvent(anUtopiaReq.getUtopiaSession().getContext().getUserId(), reqElm, LOG_TRIP_TYPE);

		return createResponse(NAV_ACTIVATE_ROUTE);
	}

	private JXElement getState(UtopiaRequest anUtopiaReq) throws UtopiaException {
		NavigationLogic logic = createLogic(anUtopiaReq);
		int personId = Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());

		JXElement response = createResponse(NAV_GET_STATE);
		Record route = logic.getActiveRoute(personId);
		if (route != null) {
			response.setAttr("routeid", route.getId());
		}

		return response;
	}

	private JXElement handlePoint(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();
		TrackLogic trackLogic = new TrackLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
		NavigationLogic navLogic = new NavigationLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());

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
            logLogic.storeLogEvent(anUtopiaReq.getUtopiaSession().getContext().getUserId(), elm, LOG_TRIP_TYPE);            
        }
        
        JXElement response = createResponse(NAV_POINT);
		response.addChildren(result);

		return response;
	}

	private JXElement stopNavigation(UtopiaRequest anUtopiaReq) throws UtopiaException {
		TrackLogic trackLogic = new TrackLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());

		// Resume current Track for this user
		trackLogic.suspend(HandlerUtil.getUserId(anUtopiaReq), System.currentTimeMillis());
		// store the event
		logLogic.storeLogEvent(anUtopiaReq.getUtopiaSession().getContext().getUserId(), anUtopiaReq.getRequestCommand(), LOG_TRIP_TYPE);
		// close this trip
		logLogic.closeLogs(anUtopiaReq.getUtopiaSession().getContext().getUserId(), LOG_TRIP_TYPE);

		// Create and return response with open track id.
		return createResponse(NAV_STOP);
	}

	private JXElement startNavigation(UtopiaRequest anUtopiaReq) throws UtopiaException {
		Oase oase = anUtopiaReq.getUtopiaSession().getContext().getOase();
		TrackLogic trackLogic = new TrackLogic(oase);

		// Resume current Track for this user
		Track track = trackLogic.resume(HandlerUtil.getUserId(anUtopiaReq), Track.VAL_DAY_TRACK, System.currentTimeMillis());

		// close previous trip
		logLogic.closeLogByTime(anUtopiaReq.getUtopiaSession().getContext().getUserId(), LOG_TRIP_TYPE);

		// and store the request
		logLogic.storeLogEvent(anUtopiaReq.getUtopiaSession().getContext().getUserId(), anUtopiaReq.getRequestCommand(), LOG_TRIP_TYPE);

		// relate the track to the trip
		Record trip = logLogic.getOpenLog(anUtopiaReq.getUtopiaSession().getContext().getUserId(), LOG_TRIP_TYPE);
		// relate the track to the trip
		try {
			oase.getRelater().relate(trip, track.getRecord());
		} catch (Throwable t) {
			throw new UtopiaException(t);
		}

		// Create and return response with open track id.
		return createResponse(NAV_START);
	}

	protected NavigationLogic createLogic(UtopiaRequest anUtopiaReq) throws UtopiaException {
		return new NavigationLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
	}

	private JXElement getMap(UtopiaRequest anUtopiaReq) throws UtopiaException {
		MapLogic mapLogic = new MapLogic();
		NavigationLogic navLogic = createLogic(anUtopiaReq);

		JXElement reqElm = anUtopiaReq.getRequestCommand();

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
			mapURL = mapLogic.getMapURL(route.getId(), urt, llb, height, width);
		} else {
			// Roaming
			mapURL = mapLogic.getMapURL(urt, llb, height, width);
		}

		JXElement response = createResponse(NAV_GET_MAP);
		try {
			response.setAttr(URL_FIELD, URLEncoder.encode(mapURL, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new UtopiaException("Exception in getMap", e);
		}

		// and store the request
		logLogic.storeLogEvent(personId+"", anUtopiaReq.getRequestCommand(), LOG_TRIP_TYPE);

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

		double xy[];
		try {
			xy = Transform.WGS84toRD(Double.parseDouble(lon), Double.parseDouble(lat));
		} catch (Exception e) {
			throw new UtopiaException("No valid lat and lon coordinates found");
		}

		double x = xy[0];
		double y = xy[1];

		// now store the rdpoint
		Point rdPoint = PostGISUtil.createPoint(EPSG_DUTCH_RD, x, y, 0.0, System.currentTimeMillis());
		location.getRecord().setObjectField(RDPOINT_FIELD, new PGgeometryLW(rdPoint));
		location.saveUpdate();

		// We either have location or an exception here
		JXElement rsp = createResponse(NAV_ADD_MEDIUM);
		rsp.setAttr(LOCATION_ID_FIELD, location.getId());

		// store the event
		logLogic.storeLogEvent(anUtopiaReq.getUtopiaSession().getContext().getUserId(), requestElement, LOG_TRIP_TYPE);

		return rsp;
	}

	protected JXElement unknownReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		String service = anUtopiaReq.getServiceName();
		Logging.getLog(anUtopiaReq).warn("Unknown service " + service);
		return createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
	}

}
