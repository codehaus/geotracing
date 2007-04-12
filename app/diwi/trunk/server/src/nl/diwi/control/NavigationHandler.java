package nl.diwi.control;

import java.util.Vector;

import nl.diwi.logic.NavigationLogic;
import nl.diwi.logic.RouteLogic;
import nl.diwi.logic.TrafficLogic;
import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;

import org.geotracing.handler.EventPublisher;
import org.geotracing.handler.HandlerUtil;
import org.geotracing.handler.Track;
import org.geotracing.handler.TrackLogic;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;

public class NavigationHandler extends DefaultHandler implements Constants {

    public final static String NAV_GET_MAP = "nav-get-map";	
    public final static String NAV_POINT = "nav-point";	
    public final static String NAV_START = "nav-start";	
    public final static String NAV_STOP = "nav-stop";	    
    public final static String NAV_ACTIVATE_ROUTE = "nav-activate-route";	    
    public final static String NAV_DEACTIVATE_ROUTE = "nav-deactivate-route";	    
    
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
			}
			else {
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
        int personId  = Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());
        	
        logic.deactivateRoute(personId);        
        JXElement response = createResponse(NAV_DEACTIVATE_ROUTE);

        return response;	
	}

	private JXElement activateRoute(UtopiaRequest anUtopiaReq) throws UtopiaException {
        NavigationLogic logic = createLogic(anUtopiaReq);
        JXElement reqElm = anUtopiaReq.getRequestCommand();
        int personId  = Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());
        int routeId = Integer.parseInt(anUtopiaReq.getRequestCommand().getAttr(ID_FIELD)); 
        	
        logic.activateRoute(routeId, personId);        
        JXElement response = createResponse(NAV_ACTIVATE_ROUTE);

        return response;	
    }

	private JXElement handlePoint(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();
		TrackLogic trackLogic = new TrackLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());

		Vector result = trackLogic.write(reqElm.getChildren(), HandlerUtil.getUserId(anUtopiaReq));
		
		//result contains 'pt' elements with everything filled out if an EMEA string was sent.
				
		return createResponse(NAV_POINT);
		
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

		// Create and return response with open track id.
		return createResponse(NAV_START);
	}

	protected NavigationLogic createLogic(UtopiaRequest anUtopiaReq) throws UtopiaException {
		return new NavigationLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
	}
	
	private JXElement getMap(UtopiaRequest anUtopiaReq) throws UtopiaException {
        NavigationLogic logic = createLogic(anUtopiaReq);
        JXElement reqElm = anUtopiaReq.getRequestCommand();
        int personId  = Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());
        	
        String mapURL = logic.getActiveMap(personId);        
        JXElement response = createResponse(NAV_ACTIVATE_ROUTE);

        return response;	
	}


	protected JXElement unknownReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		String service = anUtopiaReq.getServiceName();
		Logging.getLog(anUtopiaReq).warn("Unknown service " + service);
		return createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
	}

}
