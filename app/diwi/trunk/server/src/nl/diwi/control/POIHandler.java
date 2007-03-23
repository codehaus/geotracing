package nl.diwi.control;

import nl.diwi.logic.POILogic;
import nl.diwi.logic.TrafficLogic;
import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Java;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;


public class POIHandler extends DefaultHandler implements Constants {
    public final static String POI_INSERT_SERVICE = "poi-insert";
    public final static String POI_GETLIST_SERVICE = "poi-getlist";
    public final static String POI_GET_SERVICE = "poi-get";
    public final static String POI_GET_STARTPOINTS_SERVICE = "poi-get-startpoints";
    public final static String POI_GET_ENDPOINTS_SERVICE = "poi-get-endpoints";
    public final static String POI_UPDATE_SERVICE = "poi-update";
    public final static String POI_DELETE_SERVICE = "poi-delete";

    private POILogic logic;

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
            if (service.equals(POI_INSERT_SERVICE)) {
                // Add a new poi
                response = insertPoi(anUtopiaReq);
            } else if (service.equals(POI_GET_SERVICE)) {
                // get poi
                response = getPoi(anUtopiaReq);
            } else if (service.equals(POI_GETLIST_SERVICE)) {
                // get all pois
                response = getPoiList(anUtopiaReq);
            } else if (service.equals(POI_UPDATE_SERVICE)) {
                // Update poi
                response = updatePoi(anUtopiaReq);
            } else if (service.equals(POI_DELETE_SERVICE)) {
                // Delete a poi by id
                response = deletePoi(anUtopiaReq);
            } else if (service.equals(POI_GET_STARTPOINTS_SERVICE)) {
                // get startpoint type pois
                response = getStartPoints(anUtopiaReq);
            } else if (service.equals(POI_GET_ENDPOINTS_SERVICE)) {
                // get endpoint type pois
                response = getEndPoints(anUtopiaReq);
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
     * Insert new POI.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws org.keyworx.utopia.core.data.UtopiaException
     *          Standard Utopia exception
     */
    protected JXElement insertPoi(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement reqElm = anUtopiaReq.getRequestCommand();

        // Insert poi object
        JXElement poiElement = reqElm.getChildByTag(Constants.POI_ELM);

        int id = logic.insert(poiElement);

        // Create and return response with poi id.
        JXElement response = createResponse(POI_INSERT_SERVICE);
        response.setAttr(ID_FIELD, id);

        return response;
    }

    /**
     * Updates a POI.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws org.keyworx.utopia.core.data.UtopiaException
     *          Standard Utopia exception
     */
    protected JXElement updatePoi(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement reqElm = anUtopiaReq.getRequestCommand();

        // Update poi object
        JXElement poiElement = reqElm.getChildByTag(POI_ELM);
        String id = reqElm.getAttr(ID_FIELD);
        throwOnMissingAttr(ID_FIELD, id);
        logic.update(Integer.parseInt(id), poiElement);

        return createResponse(POI_UPDATE_SERVICE);
    }

    /**
     * Delete a POI.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws org.keyworx.utopia.core.data.UtopiaException
     *          Standard Utopia exception
     */
    protected JXElement deletePoi(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement reqElm = anUtopiaReq.getRequestCommand();

        // delete poi object
        String id = reqElm.getAttr(ID_FIELD);
        throwOnMissingAttr(ID_FIELD, id);
        logic.delete(Integer.parseInt(id));

        return createResponse(POI_DELETE_SERVICE);
    }

    /**
     * Gets a poi.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws UtopiaException standard Utopia exception
     */
    protected JXElement getPoi(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement response = createResponse(POI_GET_SERVICE);
        JXElement reqElm = anUtopiaReq.getRequestCommand();
        String id = reqElm.getAttr(ID_FIELD);
        String kichid = reqElm.getAttr(KICHID_FIELD);
        if(id!=null && id.length()>0 && Java.isInt(id)){
            response.addChild(logic.get(Integer.parseInt(id)));
        }else if(kichid!=null && kichid.length()>0){
            response.addChild(logic.get(kichid));
        }
        return response;
    }
    
    /**
     * Gets all pois.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws UtopiaException standard Utopia exception
     */
    protected JXElement getPoiList(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement response = createResponse(POI_GETLIST_SERVICE);
        response.addChildren(logic.getList());
        return response;
    }

    /**
     * Gets all startpoint pois.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws UtopiaException standard Utopia exception
     */
    protected JXElement getStartPoints(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement response = createResponse(POI_GET_STARTPOINTS_SERVICE);
        response.addChildren(logic.getStartPoints());
        return response;
    }

    /**
     * Gets all endpoint pois.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws UtopiaException standard Utopia exception
     */
    protected JXElement getEndPoints(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement response = createResponse(POI_GET_ENDPOINTS_SERVICE);
        response.addChildren(logic.getEndPoints());
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
        POILogic.setProperty(propertyName, propertyValue);
    }

    /**
     * Get user (Person) id from request.
     */
    protected POILogic createLogic(UtopiaRequest anUtopiaReq) throws UtopiaException {
        return new POILogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
    }

    /**
     * Get user (Person) id from request.
     */
    protected int getPersonId(UtopiaRequest anUtopiaReq) throws UtopiaException {
        return Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());
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
