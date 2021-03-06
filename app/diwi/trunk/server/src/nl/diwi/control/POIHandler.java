package nl.diwi.control;

import nl.diwi.logic.LogLogic;
import nl.diwi.logic.POILogic;
import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Java;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.control.ThreadSafe;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.Medium;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;

import java.util.Vector;


public class POIHandler extends DefaultHandler implements ThreadSafe, Constants {
    public final static String POI_INSERT_SERVICE = "poi-insert";
    public final static String POI_GETLIST_SERVICE = "poi-getlist";
    public final static String POI_GET_SERVICE = "poi-get";
    public final static String POI_GET_STARTPOINTS_SERVICE = "poi-get-startpoints";
    public final static String POI_GET_ENDPOINTS_SERVICE = "poi-get-endpoints";
    public final static String POI_GET_STARTENDPOINTS_SERVICE = "poi-get-startendpoints";
    public final static String POI_UPDATE_SERVICE = "poi-update";
    public final static String POI_DELETE_SERVICE = "poi-delete";
    public final static String POI_RELATE_MEDIA_SERVICE = "poi-relate-media";
    public final static String POI_UNRELATE_MEDIA_SERVICE = "poi-unrelate-media";

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
            } else if (service.equals(POI_GET_STARTENDPOINTS_SERVICE)) {
                // get startendpoint type pois
                response = getStartEndPoints(anUtopiaReq);
            } else if (service.equals(POI_RELATE_MEDIA_SERVICE)) {
                // relate media to poi
                response = relateMediaToPoi(anUtopiaReq);
            } else if (service.equals(POI_UNRELATE_MEDIA_SERVICE)) {
                // unrelatye media to poi
                response = unrelateMediaToPoi(anUtopiaReq);
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

        // log the event
        LogLogic logLogic = new LogLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        JXElement req = anUtopiaReq.getRequestCommand();
        //req.addChild(response);
        logLogic.storeLogEvent(anUtopiaReq.getUtopiaSession().getContext().getUserId(), req, LOG_WEB_TYPE);

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
        POILogic logic = new POILogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
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
        POILogic logic = new POILogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
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
        POILogic logic = new POILogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        logic.delete(Integer.parseInt(id));

        return createResponse(POI_DELETE_SERVICE);
    }

    /**
     * relate media to a POI.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws org.keyworx.utopia.core.data.UtopiaException
     *          Standard Utopia exception
     */
    protected JXElement relateMediaToPoi(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement reqElm = anUtopiaReq.getRequestCommand();

        String id = reqElm.getAttr(ID_FIELD);
        throwOnMissingAttr(ID_FIELD, id);

        Vector media = reqElm.getChildrenByTag(Medium.XML_TAG);
        POILogic logic = new POILogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        logic.relateMedia(Integer.parseInt(id), media);

        return createResponse(POI_RELATE_MEDIA_SERVICE);
    }

    /**
     * unrelate media from a POI.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws org.keyworx.utopia.core.data.UtopiaException
     *          Standard Utopia exception
     */
    protected JXElement unrelateMediaToPoi(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement reqElm = anUtopiaReq.getRequestCommand();

        String id = reqElm.getAttr(ID_FIELD);
        throwOnMissingAttr(ID_FIELD, id);

        Vector media = reqElm.getChildrenByTag(Medium.XML_TAG);
        POILogic logic = new POILogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        logic.unrelateMedia(Integer.parseInt(id), media);

        return createResponse(POI_UNRELATE_MEDIA_SERVICE);
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

        int personId = Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());

        JXElement response = createResponse(POI_GET_SERVICE);
        String id = reqElm.getAttr(ID_FIELD);
        String kichid = reqElm.getAttr(KICHID_FIELD);
        POILogic logic = new POILogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        if (id != null && id.length() > 0 && Java.isInt(id)) {
            response.addChild(logic.get(personId, Integer.parseInt(id)));
        } else if (kichid != null && kichid.length() > 0) {
            response.addChild(logic.get(personId, kichid));
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
        JXElement reqElm = anUtopiaReq.getRequestCommand();
        String bbox = reqElm.getAttr(BBOX_FIELD);
        POILogic logic = new POILogic(anUtopiaReq.getUtopiaSession().getContext().getOase());

        response.addChildren(logic.getList(bbox));
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
        POILogic logic = new POILogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        response.addChildren(logic.getPoisByType(POI_STARTPOINT));
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
        POILogic logic = new POILogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        response.addChildren(logic.getPoisByType(POI_ENDPOINT));
        return response;
    }

    /**
     * Gets all endpoint pois.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws UtopiaException standard Utopia exception
     */
    protected JXElement getStartEndPoints(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement response = createResponse(POI_GET_STARTENDPOINTS_SERVICE);
        POILogic logic = new POILogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
        response.addChildren(logic.getPoisByType(POI_START_AND_ENDPOINT));
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
     * Throw exception when attribute empty or not present.
     */
    protected void throwOnMissingAttr(String aName, String aValue) throws UtopiaException {
        if (aValue == null || aValue.length() == 0) {
            throw new UtopiaException("Missing name=" + aName + " value=" + aValue, ErrorCode.__6002_Required_attribute_missing);
        }
    }




}
