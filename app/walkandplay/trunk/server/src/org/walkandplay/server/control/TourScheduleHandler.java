package org.walkandplay.server.control;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.MailClient;
import org.keyworx.server.ServerConfig;
import org.keyworx.utopia.core.config.ContentHandlerConfig;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.logic.ContentLogic;
import org.keyworx.utopia.core.logic.RelateLogic;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;
import org.walkandplay.server.util.Constants;

import java.util.List;

/**
 * RssHandler.
 * <p/>
 * Redirects the requests to the right logic method
 *
 * @author Ronald Lenz
 * @version $Id$
 */
public class TourScheduleHandler extends DefaultHandler implements Constants {

    public final static String TOUR_SEND_INVITATION_SERVICE = "tour-send-invitation";
    public final static String TOUR_SCHEDULE_SERVICE = "tour-schedule";
    public final static String TOUR_CREATE_TEAM_SERVICE = "tour-create-team";
    public final static String TOUR_UPDATE_TEAM_SERVICE = "tour-update-teams";

    private Log log = Logging.getLog("TourScheduleHandler");
    private ContentHandlerConfig config;
    private RelateLogic relateLogic;
    private ContentLogic contentLogic;
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

        Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
        relateLogic = new RelateLogic(oase, config);
        contentLogic = new ContentLogic(oase, config);

        JXElement response;
        try {
            if (service.equals(TOUR_SEND_INVITATION_SERVICE)) {
                response = sendInvitation(anUtopiaRequest);
            } else if (service.equals(TOUR_SCHEDULE_SERVICE)) {
                response = schedule(anUtopiaRequest);
            } else if (service.equals(TOUR_CREATE_TEAM_SERVICE)) {
                response = createTeams(anUtopiaRequest);
            } else if (service.equals(TOUR_UPDATE_TEAM_SERVICE)) {
                response = updateTeams(anUtopiaRequest);
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


    /**
     * Sends an invitation to known and unkown users to play a tour.
     *
     * @param anUtopiaRequest
     * @return
     * @throws UtopiaException
     */
    public JXElement sendInvitation(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            // Id is required
            throwOnNonNumAttr("schedule id", requestElement.getAttr(SCHEDULE_ID_FIELD));
            int tourScheduleId = requestElement.getIntAttr(SCHEDULE_ID_FIELD);

            JXElement tourScheduleElm = requestElement.getChildByTag(TOUR_SCHEDULE_TABLE);
            throwOnMissingElement("tourschedule", tourScheduleElm);

            String invitation = tourScheduleElm.getChildText(INVITATION_FIELD);
            throwOnMissingAttribute("invitation", invitation);

            List peopleElms = requestElement.getChildrenByTag(Person.XML_TAG);
            if (peopleElms.size() == 0)
                throw new UtopiaException("No people found to send the invitation to", ErrorCode.__7003_missing_XML_element);

            // first update the tourschedule entry with this new invitation
            contentLogic.updateContent(tourScheduleId, tourScheduleElm);

            for (int i = 0; i < peopleElms.size(); i++) {
                // support both known and unknown participants
                JXElement personElm = (JXElement) peopleElms.get(i);
                String email = "";
                if (personElm.hasAttr(ID_FIELD)) {
                    // known user
                    JXElement p = contentLogic.getContent(Integer.parseInt(personElm.getAttr(ID_FIELD)));
                    if (p != null) {
                        email = p.getChildText(Person.EMAIL_FIELD);
                    }
                } else if (personElm.hasAttr(Person.EMAIL_FIELD)) {
                    // unknown user
                    email = personElm.getAttr(Person.EMAIL_FIELD);
                }

                String mailServer = ServerConfig.getProperty("keyworx.mail.server");
                String mailSender = ServerConfig.getProperty("keyworx.mail.sender");
                try {
                    MailClient.sendMail(mailServer, mailSender, email, "WalkAndPlay invitation", invitation, null, null, null);
                } catch (Throwable t) {
                    log.error("************** Mail error!!!! Invitation mail not sent!: " + t.toString());
                }

            }

            return createResponse(TOUR_SEND_INVITATION_SERVICE);
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    /**
     * Schedules the start and enddate for a tour.
     *
     * @param anUtopiaRequest
     * @return
     * @throws UtopiaException
     */
    public JXElement schedule(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            // Id is required
            throwOnNonNumAttr("schedule id", requestElement.getAttr(SCHEDULE_ID_FIELD));
            int tourScheduleId = requestElement.getIntAttr(SCHEDULE_ID_FIELD);

            JXElement tourScheduleElm = requestElement.getChildByTag(TOUR_SCHEDULE_TABLE);
            throwOnMissingElement("tourschedule", tourScheduleElm);

            String startDate = tourScheduleElm.getChildText(STARTDATE_FIELD);
            String endDate = tourScheduleElm.getChildText(ENDDATE_FIELD);
            throwOnMissingAttribute("startdate", startDate);
            throwOnMissingAttribute("enddate", endDate);

            contentLogic.updateContent(tourScheduleId, tourScheduleElm);

            JXElement response = createResponse(TOUR_SCHEDULE_SERVICE);
            response.setAttr("id", tourScheduleId);

            return response;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    public JXElement createTeams(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            // Id is required
            throwOnNonNumAttr("schedule id", requestElement.getAttr(SCHEDULE_ID_FIELD));
            int tourScheduleId = requestElement.getIntAttr(SCHEDULE_ID_FIELD);

            String ids = "";
            List teamElms = requestElement.getChildrenByTag(TEAM_TABLE);
            for (int i = 0; i < teamElms.size(); i++) {
                JXElement teamElm = (JXElement) teamElms.get(i);
                List peopleElms = teamElm.getChildrenByTag(Person.XML_TAG);

                // remove the children because of contentlogic xml handling
                teamElm.removeChildren();

                // for now even create a team without people
                int teamId = contentLogic.insertContent(teamElm);
                if (i == 0) {
                    ids += "" + teamId;
                } else {
                    ids += "," + teamId;
                }
                relateLogic.relate(tourScheduleId, teamId, null);

                for (int j = 0; j < peopleElms.size(); j++) {
                    int personId = ((JXElement) peopleElms.get(j)).getIntAttr(ID_FIELD);
                    relateLogic.relate(teamId, personId, null);
                }
            }

            JXElement response = createResponse(TOUR_CREATE_TEAM_SERVICE);
            response.setAttr("ids", ids);

            return response;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    public JXElement updateTeams(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();

            List teamElms = requestElement.getChildrenByTag(TEAM_TABLE);
            for (int i = 0; i < teamElms.size(); i++) {
                JXElement teamElm = (JXElement) teamElms.get(i);
                int teamId = teamElm.getIntAttr(ID_FIELD);
                List peopleElms = teamElm.getChildrenByTag(Person.XML_TAG);

                // remove the children because of contentlogic xml handling
                teamElm.removeChildren();

                // for now even create a team without people
                contentLogic.updateContent(teamId, teamElm);

                // first unrelate the players
                relateLogic.unrelate(teamId, Person.TABLE_NAME, null);

                for (int j = 0; j < peopleElms.size(); j++) {
                    int personId = ((JXElement) peopleElms.get(j)).getIntAttr(ID_FIELD);
                    relateLogic.relate(teamId, personId, null);
                }
            }

            return createResponse(TOUR_CREATE_TEAM_SERVICE);
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    /**
     * Overridden to have a hook to do the initialisation.
     *
     * @param aKey
     * @param aValue
     * @see org.keyworx.utopia.core.control.Handler#setProperty(String,String)
     */
    public void setProperty(String aKey, String aValue) {
        if (aKey.equals("config")) {
            try {
                config = ContentHandlerConfig.getConfiguration(aValue);
            }
            catch (Exception e) {
                log.error("Exception while processing content handler configuration.", e);
                throw new RuntimeException("Exception while processing content handler configuration.", e);
            }

        }
        super.setProperty(aKey, aValue);
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
     * Throw exception when attribute empty or not present.
     */
    protected void throwOnMissingElement(String aName, JXElement aValue) throws UtopiaException {
        if (aValue == null) {
            throw new UtopiaException("Missing name=" + aName + " element", ErrorCode.__7003_missing_XML_element);
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

}
