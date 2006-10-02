package org.walkandplay.server.control;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Java;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.utopia.core.data.Medium;
import org.keyworx.utopia.core.logic.ContentLogic;
import org.keyworx.utopia.core.logic.RelateLogic;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.oase.api.Record;
import org.keyworx.plugin.tagging.logic.TagLogic;
import org.walkandplay.server.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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
    public final static String TOUR_CONFIRM_SERVICE = "tour-confirm";
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
            } else if (service.equals(TOUR_CONFIRM_SERVICE)) {
                response = confirmTour(anUtopiaRequest);
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
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            JXElement contentElement = requestElement.getChildAt(0);
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();

            // get and add unknown invited people
            Vector personElms1 = requestElement.getChildrenByTag(Person.TABLE_NAME);
            if(personElms1!=null){
                JXElement invited = new JXElement("invitees");
                for(int i=0;i<personElms1.size();i++){
                    JXElement personElm1 = (JXElement)personElms1.elementAt(i);
                    String email = personElm1.getChildText(Person.EMAIL_FIELD);
                    if(email!=null && email.length()>0){
                        JXElement p = new JXElement(Person.TABLE_NAME);
                        p.setAttr(Person.EMAIL_FIELD, email);
                        invited.addChild(p);
                    }
                }
                JXElement extra = new JXElement(Person.EXTRA_FIELD);
                extra.addChild(invited);
                contentElement.addChild(extra);
            }

            ContentLogic contentLogic = new ContentLogic(oase, null);
            int tourId = contentLogic.insertContent(contentElement);

            // add gameplay
            Vector gameplayElms = contentElement.getChildrenByTag(Constants.GAMEPLAY_TABLE);
            if(gameplayElms!=null){
                RelateLogic relateLogic = new RelateLogic(oase, null);
                for(int i=0;i<gameplayElms.size();i++){
                    JXElement gameplayElm = (JXElement)gameplayElms.elementAt(i);
                    String gameplayId = gameplayElm.getAttr(Constants.ID_FIELD);
                    if(gameplayId!=null && gameplayId.length()>0 && Java.isInt(gameplayId)){
                        relateLogic.relate(tourId, Integer.parseInt(gameplayId), null);
                    }
                }
            }

            // add tags
            Vector tagsElms = contentElement.getChildrenByTag(org.keyworx.plugin.tagging.util.Constants.TAG_ELEMENT);
            if(tagsElms!=null){
                TagLogic tagLogic = new TagLogic(oase.getOaseSession());
                int taggerId = Integer.parseInt(anUtopiaRequest.getUtopiaSession().getContext().getUserId());
                int[] items= {tourId};
                String[] tags= new String[tagsElms.size()];
                for(int i=0;i<tagsElms.size();i++){
                    JXElement tagElm = (JXElement)tagsElms.elementAt(i);
                    tags[i] = tagElm.getText();
                }
                tagLogic.tag(taggerId, items, tags, org.keyworx.plugin.tagging.util.Constants.MODE_ADD);
            }

            // relate known invited people
            Vector personElms2 = contentElement.getChildrenByTag(Person.TABLE_NAME);
            if(personElms2!=null){
                RelateLogic relateLogic = new RelateLogic(oase, null);
                for(int i=0;i<personElms2.size();i++){
                    JXElement personElm2 = (JXElement)personElms2.elementAt(i);
                    String personId = personElm2.getAttr(Person.ID_FIELD);
                    if(personId!=null && personId.length()>0 && Java.isInt(personId)){
                        relateLogic.relate(tourId, Integer.parseInt(personId), Constants.INVITATION_SEND);
                    }
                }
            }
            
            JXElement response = createResponse(TOUR_CREATE_SERVICE);
            response.setAttr("id", tourId);

            return response;
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    public JXElement getTour(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
            ContentLogic contentLogic = new ContentLogic(oase, null);
            String id = requestElement.getAttr(Constants.ID_FIELD);
            if(id == null || id.length() == 0 || !Java.isInt(id)) throw new UtopiaException("No tourid found");

            int tourId = Integer.parseInt(id);
            JXElement content = contentLogic.getContent(tourId);

            // get known invitees
            RelateLogic relateLogic = new RelateLogic(oase, null);
            List fields1 = new ArrayList(2);
            fields1.add(Constants.ID_FIELD);
            fields1.add(Constants.NAME_FIELD);
            JXElement[] inviteeElms = relateLogic.getRelated(tourId, Person.TABLE_NAME, Constants.INVITATION_SEND, fields1);
            if(inviteeElms!=null){
                for(int i=0;i<inviteeElms.length;i++){
                    JXElement inviteeElm = inviteeElms[i];
                    inviteeElm.setAttr(Constants.INVITATION, Constants.INVITATION_PENDING);
                    content.addChild(inviteeElm);
                }
            }

            // get tags
            TagLogic tagLogic = new TagLogic(oase.getOaseSession());
            int[] items = {tourId};
            Record[] tags = tagLogic.getTagsFor(items, -1, -1);
            if(tags!=null){
                for(int i=0;i<tags.length;i++){
                    JXElement tagElm = new JXElement(org.keyworx.plugin.tagging.util.Constants.TAG_ELEMENT);
                    tagElm.setText(tags[i].getStringField(org.keyworx.plugin.tagging.util.Constants.NAME_COLUMN));
                    content.addChild(tagElm);
                }
            }

            // get gameplay
            List fields2 = new ArrayList(2);
            fields2.add(Constants.ID_FIELD);
            fields2.add(Constants.NAME_FIELD);
            JXElement[] gameplayElms = relateLogic.getRelated(tourId, Constants.GAMEPLAY_TABLE, null, fields2);
            if(gameplayElms!=null){
                for(int i=0;i<gameplayElms.length;i++){
                    content.addChild(gameplayElms[i]);
                }
            }

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

    public JXElement confirmTour(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
            String tid = requestElement.getAttr("id");
            if(tid == null || tid.length() == 0 || !Java.isInt(tid)) throw new UtopiaException("No tourid found");
            String pid = requestElement.getAttr("personid");
            if(pid == null || pid.length() == 0 || !Java.isInt(pid)) throw new UtopiaException("No personid found");

            // we don't check if the person has been invited... we just confirm
            RelateLogic relateLogic = new RelateLogic(oase, null);
            relateLogic.relate(Integer.parseInt(tid), Integer.parseInt(pid), Constants.INVITATION_CONFIRMED);

            return createResponse(TOUR_CONFIRM_SERVICE);
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    public JXElement getTours(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
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
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            JXElement contentElement = requestElement.getChildAt(0);
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();

            String id = requestElement.getAttr(Constants.ID_FIELD);
            if(id == null || id.length() == 0 || !Java.isInt(id)) throw new UtopiaException("No tourid found");

            int tourId = Integer.parseInt(id);

            // get and add unknown invited people
            Vector personElms1 = requestElement.getChildrenByTag(Person.TABLE_NAME);
            if(personElms1!=null){
                JXElement invited = new JXElement("invitees");
                for(int i=0;i<personElms1.size();i++){
                    JXElement personElm1 = (JXElement)personElms1.elementAt(i);
                    String email = personElm1.getChildText(Person.EMAIL_FIELD);
                    if(email!=null && email.length()>0){
                        JXElement p = new JXElement(Person.TABLE_NAME);
                        p.setAttr(Person.EMAIL_FIELD, email);
                        invited.addChild(p);
                    }
                }
                JXElement extra = new JXElement(Person.EXTRA_FIELD);
                extra.addChild(invited);
                contentElement.addChild(extra);
            }


            ContentLogic contentLogic = new ContentLogic(oase, null);
            contentLogic.updateContent(tourId, requestElement);

            // add gameplay
            Vector gameplayElms = contentElement.getChildrenByTag(Constants.GAMEPLAY_TABLE);
            if(gameplayElms!=null){
                RelateLogic relateLogic = new RelateLogic(oase, null);
                // first unrelate the current gameplay
                relateLogic.unrelate(tourId, Constants.GAMEPLAY_TABLE, null);
                for(int i=0;i<gameplayElms.size();i++){
                    JXElement gameplayElm = (JXElement)gameplayElms.elementAt(i);
                    String gameplayId = gameplayElm.getAttr(Constants.ID_FIELD);
                    if(gameplayId!=null && gameplayId.length()>0 && Java.isInt(gameplayId)){
                        relateLogic.relate(tourId, Integer.parseInt(gameplayId), null);
                    }
                }
            }

            // add tags
            Vector tagsElms = contentElement.getChildrenByTag(org.keyworx.plugin.tagging.util.Constants.TAG_ELEMENT);
            if(tagsElms!=null){
                TagLogic tagLogic = new TagLogic(oase.getOaseSession());
                int taggerId = Integer.parseInt(anUtopiaRequest.getUtopiaSession().getContext().getUserId());
                int[] items= {tourId};
                String[] tags= new String[tagsElms.size()];
                for(int i=0;i<tagsElms.size();i++){
                    JXElement tagElm = (JXElement)tagsElms.elementAt(i);
                    tags[i] = tagElm.getText();
                }
                tagLogic.tag(taggerId, items, tags, org.keyworx.plugin.tagging.util.Constants.MODE_REPLACE);
            }

            // relate known invited people
            Vector personElms2 = contentElement.getChildrenByTag(Person.TABLE_NAME);
            if(personElms2!=null){
                RelateLogic relateLogic = new RelateLogic(oase, null);
                // first unrelate the current invitees
                relateLogic.unrelate(tourId, Person.TABLE_NAME, Constants.INVITATION_SEND);
                for(int i=0;i<personElms2.size();i++){
                    JXElement personElm2 = (JXElement)personElms2.elementAt(i);
                    String personId = personElm2.getAttr(Person.ID_FIELD);
                    if(personId!=null && personId.length()>0 && Java.isInt(personId)){
                        relateLogic.relate(tourId, Integer.parseInt(personId), Constants.INVITATION_SEND);
                    }
                }
            }

            // add poi's
            Vector poiElms = contentElement.getChildrenByTag(Constants.POI_TABLE);
            if(poiElms!=null){
                RelateLogic relateLogic = new RelateLogic(oase, null);
                // first unrelate the current poi's
                relateLogic.unrelate(tourId, Constants.POI_TABLE, null);
                for(int i=0;i<poiElms.size();i++){
                    JXElement poiElm = (JXElement)poiElms.elementAt(i);
                    String poiId = poiElm.getAttr(Constants.POI_TABLE);
                    if(poiId!=null && poiId.length()>0 && Java.isInt(poiId)){
                        relateLogic.relate(tourId, Integer.parseInt(poiId), null);
                    }
                }
            }

            // add media
            Vector mediumElms = contentElement.getChildrenByTag(Medium.TABLE_NAME);
            if(mediumElms!=null){
                RelateLogic relateLogic = new RelateLogic(oase, null);
                // first unrelate the current media
                relateLogic.unrelate(tourId, Medium.TABLE_NAME, null);
                for(int i=0;i<mediumElms.size();i++){
                    JXElement mediumElm = (JXElement)mediumElms.elementAt(i);
                    String mediumId = mediumElm.getAttr(Medium.ID_FIELD);
                    if(mediumId!=null && mediumId.length()>0 && Java.isInt(mediumId)){
                        relateLogic.relate(tourId, Integer.parseInt(mediumId), null);
                    }
                }
            }

            // add assignments
            Vector assignmentElms = contentElement.getChildrenByTag(Constants.ASSIGNMENT_TABLE);
            if(assignmentElms!=null){
                RelateLogic relateLogic = new RelateLogic(oase, null);
                // first unrelate the current assignments
                relateLogic.unrelate(tourId, Constants.ASSIGNMENT_TABLE, null);
                for(int i=0;i<assignmentElms.size();i++){
                    JXElement assignmentElm = (JXElement)assignmentElms.elementAt(i);
                    String assignmentId = assignmentElm.getAttr(Constants.ASSIGNMENT_TABLE);
                    if(assignmentId!=null && assignmentId.length()>0 && Java.isInt(assignmentId)){
                        relateLogic.relate(tourId, Integer.parseInt(assignmentId), null);
                    }
                }
            }

            return createResponse(TOUR_UPDATE_SERVICE);
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
		}
	}

}
