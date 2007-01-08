package org.walkandplay.server.control;

import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.Medium;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.utopia.core.logic.ContentLogic;
import org.keyworx.utopia.core.logic.RelateLogic;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Java;
import org.walkandplay.server.util.Constants;
import nl.justobjects.jox.dom.JXElement;

import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

/**
 * AssignmentHandler.
 * <p/>
 * Redirects the requests to the right logic method
 *
 * @author Ronald Lenz
 * @version $Id$
 */
public class AssignmentHandler extends DefaultHandler {

    public final static String ASSIGNMENT_GET_SERVICE = "assignment-get";
    public final static String ASSIGNMENT_CREATE_SERVICE = "assignment-create";
    public final static String ASSIGNMENT_UPDATE_SERVICE = "assignment-update";
    public final static String ASSIGNMENT_DELETE_SERVICE = "assignment-delete";

    /**
     * Processes the Client Request.
     *
     * @param anUtopiaRequest A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws org.keyworx.utopia.core.data.UtopiaException Standard Utopia exception
     */
    public UtopiaResponse processRequest(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        Log log = Logging.getLog(anUtopiaRequest);

        // Get the service name for the request
        String service = anUtopiaRequest.getServiceName();
        log.info("Handling request for service=" + service);
        log.info(new String(anUtopiaRequest.getRequestCommand().toBytes(false)));

        JXElement response;
        try {
            if (service.equals(ASSIGNMENT_GET_SERVICE)) {
                response = getAssignment(anUtopiaRequest);
            } else if (service.equals(ASSIGNMENT_CREATE_SERVICE)) {
                response = createAssignment(anUtopiaRequest);
            } else if (service.equals(ASSIGNMENT_UPDATE_SERVICE)) {
                response = updateAssignment(anUtopiaRequest);
            } else if (service.equals(ASSIGNMENT_DELETE_SERVICE)) {
                response = deleteAssignment(anUtopiaRequest);
            } else {
                log.warn("Unknown service " + service);
                response = createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
            }

            log.info("Handled service=" + service + " response=" + response.getTag());
            log.info(new String(response.toBytes(false)));
            return new UtopiaResponse(response);
        } catch (UtopiaException ue) {
            log.error("Negative response for service: " + service, ue);
            return new UtopiaResponse(createNegativeResponse(service, ue.getErrorCode(), "Error in request: " + ue.getMessage()));
        } catch (Throwable t) {
            log.error("Unexpected error in service : " + service, t);
            return new UtopiaResponse(createNegativeResponse(service, ErrorCode.__6005_Unexpected_error, "Unexpected error in request"));
        }
    }


    public JXElement createAssignment(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            JXElement contentElement = requestElement.getChildByTag(Constants.ASSIGNMENT_TABLE);
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();

            String tourId = requestElement.getAttr("tourid");
            if(tourId == null || tourId.length()  == 0 || !Java.isInt(tourId)){
                throw new UtopiaException("No tourid found", ErrorCode.__6002_Required_attribute_missing);
            }

            ContentLogic contentLogic = new ContentLogic(oase, null);
            int assignmentId = contentLogic.insertContent(contentElement);

            RelateLogic relateLogic = new RelateLogic(oase, null);
            relateLogic.relate(Integer.parseInt(tourId), assignmentId, null);

            Vector media = requestElement.getChildrenByTag(Medium.TABLE_NAME);
            if(media!=null){
                for(int i=0;i<media.size();i++){
                    JXElement medium = (JXElement)media.elementAt(i);
                    relateLogic.relate(assignmentId, Integer.parseInt(medium.getAttr(Constants.ID_FIELD)), null);
                }
            }

            JXElement response = createResponse(ASSIGNMENT_CREATE_SERVICE);
            response.setAttr("id", assignmentId);

            return response;
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    public JXElement getAssignment(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
            ContentLogic contentLogic = new ContentLogic(oase, null);
            String id = requestElement.getAttr(Constants.ID_FIELD);
            if(id == null || id.length() == 0 || !Java.isInt(id)) {
                throw new UtopiaException("No valid assignmentid found", ErrorCode.__6002_Required_attribute_missing);
            }

            int assignmentId = Integer.parseInt(id);
            JXElement content = contentLogic.getContent(assignmentId);

            JXElement response = createResponse(ASSIGNMENT_GET_SERVICE);
            response.addChild(content);

            RelateLogic relateLogic = new RelateLogic(oase, null);
            List fields = new ArrayList(2);
            fields.add(Medium.ID_FIELD);
            fields.add(Medium.NAME_FIELD);
            JXElement[] media = relateLogic.getRelated(assignmentId,Medium.TABLE_NAME,null, fields);
            if(media!=null){
                for(int i=0;i<media.length;i++){
                    JXElement medium = media[i];
                    response.addChild(medium);
                }
            }
            
            return response;
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    public JXElement deleteAssignment(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
            String id = requestElement.getAttr(Constants.ID_FIELD);
            if(id == null || id.length() == 0 || !Java.isInt(id)) {
                throw new UtopiaException("No valid assignmentid found", ErrorCode.__6002_Required_attribute_missing);
            }

            ContentLogic contentLogic = new ContentLogic(oase, null);
            contentLogic.deleteContent(Integer.parseInt(id));

            return createResponse(ASSIGNMENT_DELETE_SERVICE);
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    public JXElement updateAssignment(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            JXElement contentElement = requestElement.getChildByTag(Constants.ASSIGNMENT_TABLE);
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();

            String id = requestElement.getAttr(Constants.ID_FIELD);
            if(id == null || id.length() == 0 || !Java.isInt(id)) {
                throw new UtopiaException("No valid assignmentid found", ErrorCode.__6002_Required_attribute_missing);
            }
            int assignmentId = Integer.parseInt(id);

            ContentLogic contentLogic = new ContentLogic(oase, null);
            contentLogic.updateContent(assignmentId, contentElement);

            RelateLogic relateLogic = new RelateLogic(oase, null);

            Vector media = requestElement.getChildrenByTag(Medium.TABLE_NAME);
            if(media!=null){
                relateLogic.unrelate(assignmentId, Medium.TABLE_NAME, null);
                for(int i=0;i<media.size();i++){
                    JXElement medium = (JXElement)media.elementAt(i);
                    relateLogic.relate(assignmentId, Integer.parseInt(medium.getAttr(Constants.ID_FIELD)), null);
                }
            }

            return createResponse(ASSIGNMENT_UPDATE_SERVICE);
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
		}
	}

}
