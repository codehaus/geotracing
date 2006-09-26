package org.walkandplay.server.control;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.logic.ContentLogic;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;
import org.walkandplay.server.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * RssHandler.
 * <p/>
 * Redirects the requests to the right logic method
 *
 * @author Ronald Lenz
 * @version $Id: RssHandler.java,v 1.1 2005/08/18 08:43:52 rlenz Exp $
 */
public class TourHandler extends DefaultHandler {

    public final static String TOUR_GET_SERVICE = "tour-get";
    public final static String TOUR_GETLIST_SERVICE = "tour-getlist";
    public final static String TOUR_CREATE_SERVICE = "tour-create";
    public final static String TOUR_UPDATE_SERVICE = "tour-update";
    public final static String TOUR_DELETE_SERVICE = "tour-delete";

    /**
     * Processes the Client Request.
     *
     * @param anUtopiaRequest A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws org.keyworx.utopia.core.data.UtopiaException
     *          Standard Utopia exception
     */
    public UtopiaResponse processRequest(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        Log log = Logging.getLog(anUtopiaRequest);

        // Get the service name for the request
        String service = anUtopiaRequest.getServiceName();
        log.info("Handling request for service=" + service);
        log.info(new String(anUtopiaRequest.getRequestCommand().toBytes(false)));

        JXElement response;
        try {
            if (service.equals(TOUR_GET_SERVICE)) {
                response = getTour(anUtopiaRequest);
            } else if (service.equals(TOUR_GETLIST_SERVICE)) {
                response = getTours(anUtopiaRequest);
            } else if (service.equals(TOUR_CREATE_SERVICE)) {
                response = createTour(anUtopiaRequest);
            } else if (service.equals(TOUR_UPDATE_SERVICE)) {
                response = updateTour(anUtopiaRequest);
            } else if (service.equals(TOUR_DELETE_SERVICE)) {
                response = deleteTour(anUtopiaRequest);
            } else {
                log.warn("Unknown service " + service);
                response = createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
            }

            log.info("Handled service=" + service + " response=" + response.getTag());
            log.info(new String(response.toBytes(false)));
            return new UtopiaResponse(response);
        } catch (UtopiaException ue) {
            log.error("Negative response for service=" + service + "; exception:" + ue.getMessage());
            return new UtopiaResponse(createNegativeResponse(service, ue.getErrorCode(), "Error in request: " + ue.getMessage()));
        } catch (Throwable t) {
            log.error("Unexpected error in service : " + service, t);
            return new UtopiaResponse(createNegativeResponse(service, ErrorCode.__6005_Unexpected_error, "Unexpected error in request"));
        }
    }


    public JXElement createTour(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand().getChildAt(0);
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();

            ContentLogic contentLogic = new ContentLogic(oase, null);
            int id = contentLogic.insertContent(requestElement);

            JXElement response = createResponse(TOUR_CREATE_SERVICE);
            response.setAttr("id", id);

            return response;
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    public JXElement getTour(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand().getChildAt(0);
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
            ContentLogic contentLogic = new ContentLogic(oase, null);
            JXElement content = contentLogic.getContent(Integer.parseInt(requestElement.getAttr(Constants.ID_FIELD)));

            JXElement response = createResponse(TOUR_GET_SERVICE);
            response.addChild(content);
            return response;
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    public JXElement deleteTour(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
            ContentLogic contentLogic = new ContentLogic(oase, null);
            contentLogic.deleteContent(Integer.parseInt(requestElement.getAttr(Constants.ID_FIELD)));

            return createResponse(TOUR_DELETE_SERVICE);
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    public JXElement getTours(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand().getChildAt(0);
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
            ContentLogic contentLogic = new ContentLogic(oase, null);
            List columns = new ArrayList(3);
            columns.add(Constants.ID_FIELD);
            columns.add(Constants.NAME_FIELD);
            columns.add(Constants.DESCRIPTION_FIELD);
            JXElement[] content = contentLogic.listContent(Constants.TOUR_TABLE, columns, null, -1, -1, -1);

            JXElement response = createResponse(TOUR_GETLIST_SERVICE);

            if (content != null) {
                for (int i = 0; i < content.length; i++) {
                    response.addChild(content[i]);
                }
            }

            return response;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    public JXElement updateTour(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand().getChildAt(0);
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
            ContentLogic contentLogic = new ContentLogic(oase, null);
            contentLogic.updateContent(Integer.parseInt(requestElement.getAttr(Constants.ID_FIELD)), requestElement);

            return createResponse(TOUR_UPDATE_SERVICE);
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
		}
	}

}
