package nl.diwi.control;

import nl.diwi.logic.LogLogic;
import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.control.ThreadSafe;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.api.OaseException;
import org.geotracing.handler.QueryLogic;

public class UGCHandler extends DefaultHandler implements ThreadSafe, Constants {
    public final static String UGC_GETLIST_SERVICE = "ugc-getlist";
    public final static String UGC_DELETE_SERVICE = "ugc-delete";

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
            if (service.equals(UGC_GETLIST_SERVICE)) {
                response = getUGCList(anUtopiaReq);
            } else if (service.equals(UGC_DELETE_SERVICE)) {
                response = deleteUGC(anUtopiaReq);
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
     * Gets all ugcs.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws org.keyworx.utopia.core.data.UtopiaException standard Utopia exception
     */
    protected JXElement getUGCList(UtopiaRequest anUtopiaReq) throws UtopiaException {
        String tables = "base_medium,g_track";
        String fields = "base_medium.id,base_medium.kind,base_medium.mime,base_medium.name,base_medium.description,base_medium.creationdate,base_medium.extra";
        String where = null;
        String relations = "g_track,base_medium,medium";
        String postCond = null;
        JXElement response = QueryLogic.queryStoreReq(anUtopiaReq.getUtopiaSession().getContext().getOase(), tables, fields, where, relations, postCond);
        response.setTag(UGC_GETLIST_SERVICE + "-rsp");
        return response;
    }

    /**
     * Delete a UGC.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws org.keyworx.utopia.core.data.UtopiaException
     *          Standard Utopia exception
     */
    protected JXElement deleteUGC(UtopiaRequest anUtopiaReq) throws UtopiaException, OaseException {
        JXElement reqElm = anUtopiaReq.getRequestCommand();

        String id = reqElm.getAttr(ID_FIELD);
        throwOnMissingAttr(ID_FIELD, id);

        Oase oase = anUtopiaReq.getUtopiaSession().getContext().getOase();
        Record medium = oase.getFinder().read(Integer.parseInt(id));
        Record[] locations = oase.getRelater().getRelated(medium, "g_location", "medium");

        // also delete the location of the ugc
        if(locations.length>0){
            oase.getModifier().delete(medium);
            oase.getModifier().delete(locations[0]);
        }

        return createResponse(UGC_DELETE_SERVICE);
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

    /**
     * Throw exception when attribute empty or not present.
     */
    protected void throwOnMissingAttr(String aName, String aValue) throws UtopiaException {
        if (aValue == null || aValue.length() == 0) {
            throw new UtopiaException("Missing name=" + aName + " value=" + aValue, ErrorCode.__6002_Required_attribute_missing);
        }
    }




}