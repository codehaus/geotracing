package nl.diwi.control;

import nl.diwi.logic.MapLogic;
import nl.diwi.logic.NavigationLogic;
import nl.diwi.logic.TrafficLogic;
import nl.diwi.logic.TripLogic;
import nl.diwi.util.Constants;
import nl.diwi.util.ProjectionConversionUtil;
import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.HandlerUtil;
import org.geotracing.handler.Track;
import org.geotracing.handler.TrackLogic;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.postgis.Point;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Vector;

public class NavigationHandler extends DefaultHandler implements Constants {

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
            } else {
                // May be overridden in subclass
                response = unknownReq(anUtopiaReq);
            }

            // store the traffic
            TrafficLogic t = new TrafficLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
            t.storeTraffic(anUtopiaReq.getUtopiaSession().getContext().getUserId(), anUtopiaReq.getRequestCommand(), response);

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
        JXElement response = createResponse(NAV_DEACTIVATE_ROUTE);

        return response;
    }

    private JXElement activateRoute(UtopiaRequest anUtopiaReq) throws UtopiaException {
        NavigationLogic logic = createLogic(anUtopiaReq);
        JXElement reqElm = anUtopiaReq.getRequestCommand();
        int personId = Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());
        int routeId = Integer.parseInt(anUtopiaReq.getRequestCommand().getAttr(ID_FIELD));

        logic.activateRoute(routeId, personId);
        JXElement response = createResponse(NAV_ACTIVATE_ROUTE);

        return response;
    }

    private JXElement handlePoint(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement reqElm = anUtopiaReq.getRequestCommand();
        TrackLogic trackLogic = new TrackLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        NavigationLogic navLogic = new NavigationLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());

        //result contains 'pt' elements with everything filled out if an EMEA string was sent.
        Vector result = trackLogic.write(reqElm.getChildren(), HandlerUtil.getUserId(anUtopiaReq));

        //Get Point from pt elements
        JXElement ptElement = (JXElement) (reqElm.getChildren().get(0));
        Point point = new Point(Double.parseDouble(ptElement.getAttr(LAT_FIELD)),
                Double.parseDouble(ptElement.getAttr(LON_FIELD)), 0);
        point.setSrid(EPSG_WGS84);
        result.addAll(navLogic.checkPoint(point, HandlerUtil.getUserId(anUtopiaReq)));

        JXElement response = createResponse(NAV_POINT);
        response.addChildren(result);

        return response;
    }

    private JXElement stopNavigation(UtopiaRequest anUtopiaReq) throws UtopiaException {
        TrackLogic trackLogic = new TrackLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());

        // Resume current Track for this user
        trackLogic.suspend(HandlerUtil.getUserId(anUtopiaReq), System.currentTimeMillis());

        // Create and return response with open track id.
        return createResponse(NAV_STOP);
    }

    private JXElement startNavigation(UtopiaRequest anUtopiaReq) throws UtopiaException {
        TrackLogic trackLogic = new TrackLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());

        // Resume current Track for this user
        trackLogic.resume(HandlerUtil.getUserId(anUtopiaReq), Track.VAL_DAY_TRACK, System.currentTimeMillis());

        // create a trip
        TripLogic tripLogic = new TripLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        tripLogic.createTrip(anUtopiaReq.getUtopiaSession().getContext().getUserId());

        // Create and return response with open track id.
        return createResponse(NAV_START);
    }

    protected NavigationLogic createLogic(UtopiaRequest anUtopiaReq) throws UtopiaException {
        return new NavigationLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
    }

    private JXElement getMap(UtopiaRequest anUtopiaReq) throws UtopiaException {
        MapLogic logic = new MapLogic();
        JXElement reqElm = anUtopiaReq.getRequestCommand();

        int height = Integer.parseInt(reqElm.getAttr(HEIGHT_FIELD));
        int width = Integer.parseInt(reqElm.getAttr(WIDTH_FIELD));
        double llbLat = Double.parseDouble(reqElm.getAttr(LLB_LAT_ATTR));
        double llbLon = Double.parseDouble(reqElm.getAttr(LLB_LON_ATTR));
        double urtLat = Double.parseDouble(reqElm.getAttr(URL_LAT_ATTR));
        double urtLon = Double.parseDouble(reqElm.getAttr(URT_LON_ATTR));
//		"BOX(5.37404870986938 52.1408767700195,5.40924692153931 52.172737121582)"

//		double llbLon = 5.37404870986938;
//		double llbLat = 52.1408767700195;
//		double urtLon = 5.40924692153931;
//		double urtLat = 52.172737121582;


        Point llb = ProjectionConversionUtil.WGS842RD(new Point(llbLon, llbLat));
        Point urt = ProjectionConversionUtil.WGS842RD(new Point(urtLon, urtLat));

        String mapURL = logic.getMapURL(urt, llb, height, width);
        JXElement response = createResponse(NAV_GET_MAP);
        try {
            response.setAttr(URL_FIELD, URLEncoder.encode(mapURL, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new UtopiaException("Exception in getMap", e);
        }
        return response;
    }


    protected JXElement unknownReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
        String service = anUtopiaReq.getServiceName();
        Logging.getLog(anUtopiaReq).warn("Unknown service " + service);
        return createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
    }

}
