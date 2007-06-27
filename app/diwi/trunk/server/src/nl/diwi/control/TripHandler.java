package nl.diwi.control;

import nl.diwi.logic.LogLogic;
import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;

import java.util.Vector;

/**
 * Handles all operations related to commenting.
 * <p/>
 * Redirects the requests to CommentLogic methods.
 *
 * @author Just van den Broecke
 * @version $Id: CommentHandler.java 361 2007-02-05 21:34:58Z just $
 */
public class TripHandler extends DefaultHandler implements Constants {

    public final static String TRIP_GET_SERVICE = "trip-get";
    public final static String TRIP_GETLIST_SERVICE = "trip-getlist";

    /**
     * Processes the Client Request.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws org.keyworx.utopia.core.data.UtopiaException
     *          standard Utopia exception
     */
    public UtopiaResponse processRequest(UtopiaRequest anUtopiaReq) throws UtopiaException {
        Log log = Logging.getLog(anUtopiaReq);

        // Get the service name for the request
        String service = anUtopiaReq.getServiceName();
        log.trace("Handling request for service=" + service);

        JXElement response;
        try {
            if (service.equals(TRIP_GET_SERVICE)) {
                response = getTrip(anUtopiaReq);
            } else if (service.equals(TRIP_GETLIST_SERVICE)) {
                response = getTripList(anUtopiaReq);
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

    private JXElement getTrip(UtopiaRequest anUtopiaReq) throws UtopiaException {
        LogLogic l = new LogLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        l.storeLogEvent(anUtopiaReq.getUtopiaSession().getContext().getUserId(), anUtopiaReq.getRequestCommand(), LOG_TRAFFIC_TYPE);

        LogLogic logic = new LogLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        JXElement tripElm = logic.getLog(anUtopiaReq.getRequestCommand().getAttr(ID_FIELD));

        JXElement response = createResponse(TRIP_GET_SERVICE);
        response.addChild(tripElm);

        return response;
    }

    private JXElement getTripList(UtopiaRequest anUtopiaReq) throws UtopiaException {
        LogLogic l = new LogLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        l.storeLogEvent(anUtopiaReq.getUtopiaSession().getContext().getUserId(), anUtopiaReq.getRequestCommand(), LOG_TRAFFIC_TYPE);

        // in case we are on somebody else's page we want to use their personid to retrieve the trips.
        String personId = anUtopiaReq.getRequestCommand().getAttr("personid");
        if (personId == null || personId.length() == 0) {
            personId = anUtopiaReq.getUtopiaSession().getContext().getUserId();
        }

        LogLogic logic = new LogLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        Vector trips = logic.getLogs(personId, LOG_TRIP_TYPE);

        JXElement response = createResponse(TRIP_GETLIST_SERVICE);
        response.addChildren(trips);

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
    protected JXElement unknownReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
        String service = anUtopiaReq.getServiceName();
        Logging.getLog(anUtopiaReq).warn("Unknown service " + service);
        return createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
    }

}
