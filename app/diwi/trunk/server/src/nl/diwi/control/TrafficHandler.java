package nl.diwi.control;

import nl.diwi.logic.TrafficLogic;
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

public class TrafficHandler extends DefaultHandler implements Constants {
    public final static String TRAFFIC_GETALL_SERVICE = "traffic-getall";
    public final static String TRAFFIC_GETFORPERSON_SERVICE = "traffic-getforperson";

    private TrafficLogic logic;

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

        logic = createLogic(anUtopiaReq);

        // Get the service name for the request
        String service = anUtopiaReq.getServiceName();
        log.trace("Handling request for service=" + service);

        JXElement response;
        try {
            if (service.equals(TRAFFIC_GETALL_SERVICE)) {
                // get all
                response = getAllReq(anUtopiaReq);
            } else if (service.equals(TRAFFIC_GETFORPERSON_SERVICE)) {
                // get for all for a specific person
                response = getForPersonReq(anUtopiaReq);
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


    /**
     * Get all.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws org.keyworx.utopia.core.data.UtopiaException
     *          Standard Utopia exception
     */
    protected JXElement getAllReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
        Vector result = logic.getAllTraffic();
        JXElement response = createResponse(TRAFFIC_GETALL_SERVICE);
        response.addChildren(result);
        return response;
    }

    /**
     * Get for person.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws org.keyworx.utopia.core.data.UtopiaException
     *          Standard Utopia exception
     */
    protected JXElement getForPersonReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement reqElm = anUtopiaReq.getRequestCommand();
        String personId = reqElm.getAttr("personid");
        Vector result = logic.getTrafficForPerson(personId);

        JXElement response = createResponse(TRAFFIC_GETFORPERSON_SERVICE);
        response.addChildren(result);
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

    /** Utility methods. */

    /**
     * Intercept and pass properties to CommentLogic.
     * <p/>
     * Since the Handler has no init() we do it this way
     * for the time being....
     */
    public void setProperty(String propertyName, String propertyValue) {
        super.setProperty(propertyName, propertyValue);
        TrafficLogic.setProperty(propertyName, propertyValue);
    }

    /**
     * Get user (Person) id from request.
     */
    protected TrafficLogic createLogic(UtopiaRequest anUtopiaReq) throws UtopiaException {
        return new TrafficLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
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

    /**
     * Throw exception when numeric attribute empty or not present.
     */
    protected void throwNegNumAttr(String aName, long aValue) throws UtopiaException {
        if (aValue == -1) {
            throw new UtopiaException("Invalid numvalue name=" + aName + " value=" + aValue, ErrorCode.__6004_Invalid_attribute_value);
		}
	}
}
