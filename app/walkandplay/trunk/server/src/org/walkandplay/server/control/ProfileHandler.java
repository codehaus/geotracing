/*
*	cd:	Thu May 06 16:33:32 CEST 2004
*	author:	Ronald Lenz
*
*	$Id: RssHandler.java,v 1.1 2005/08/18 08:43:52 rlenz Exp $
*************************************************************/

package org.walkandplay.server.control;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Java;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.Account;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;
import org.walkandplay.server.logic.ProfileLogic;

/**
 * RssHandler.
 * <p/>
 * Redirects the requests to the right logic method
 *
 * @author Ronald Lenz
 * @version $Id: RssHandler.java,v 1.1 2005/08/18 08:43:52 rlenz Exp $
 */
public class ProfileHandler extends DefaultHandler {

    public final static String PROFILE_GET_SERVICE = "profile-get";
    public final static String PROFILE_CREATE_SERVICE = "profile-create";
    public final static String PROFILE_UPDATE_SERVICE = "profile-update";
    public final static String PROFILE_ACTIVATE_SERVICE = "profile-activate";
    public final static String PROFILE_RESETPASSWORD_SERVICE = "profile-resetpassword";
    public final static String PROFILE_SENDJAD_SERVICE = "profile-sendjad";

    /**
     * Processes the Client Request.
     *
     * @param anUtopiaRequest A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws UtopiaException Standard Utopia exception
     */
    public UtopiaResponse processRequest(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        Log log = Logging.getLog(anUtopiaRequest);

        // Get the service name for the request
        String service = anUtopiaRequest.getServiceName();
        log.info("Handling request for service=" + service);
        log.info(new String(anUtopiaRequest.getRequestCommand().toBytes(false)));

        JXElement response;
        try {
            if (service.equals(PROFILE_CREATE_SERVICE)) {
                response = createProfile(anUtopiaRequest);
            } else if (service.equals(PROFILE_GET_SERVICE)) {
                response = getProfile(anUtopiaRequest);
            } else if (service.equals(PROFILE_UPDATE_SERVICE)) {
                response = updateProfile(anUtopiaRequest);
            } else if (service.equals(PROFILE_ACTIVATE_SERVICE)) {
                response = activateProfile(anUtopiaRequest);
            } else if (service.equals(PROFILE_RESETPASSWORD_SERVICE)) {
                response = resetPassword(anUtopiaRequest);
            } else if (service.equals(PROFILE_SENDJAD_SERVICE)) {
                response = sendJad(anUtopiaRequest);
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
     * Creates a new profile. Client signup functionality. Sends a confirmation email.
     *
     * @param anUtopiaRequest The request holding all needed parameters
     * @return a utopia response
     * @throws UtopiaException
     */
    public JXElement createProfile(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();

            String portalId = anUtopiaRequest.getUtopiaSession().getContext().getPortalId();
            String applicationId = anUtopiaRequest.getUtopiaSession().getContext().getApplicationId();

            String nickName = requestElement.getAttr("nickname");
            String firstName = requestElement.getAttr(Person.FIRSTNAME_FIELD);
            String lastName = requestElement.getAttr(Person.LASTNAME_FIELD);
            String street = requestElement.getAttr(Person.STREET_FIELD);
            String streetNr = requestElement.getAttr(Person.STREETNR_FIELD);
            String zipCode = requestElement.getAttr(Person.ZIPCODE_FIELD);
            String mobileNr = requestElement.getAttr(Person.MOBILENR_FIELD);
            String city = requestElement.getAttr(Person.CITY_FIELD);
            String country = requestElement.getAttr(Person.COUNTRY_FIELD);
            String tagString = requestElement.getAttr("tags");
            String[] tags = null;
            if (tagString != null && tagString.length() > 0) {
                tags = Java.stripFromDelimeterToArray(tagString, ",");
            }

            String photoId = requestElement.getAttr("mediumid");
            String licenseId = requestElement.getAttr("licenseId");
            boolean profilePublic = Java.StringToBoolean(requestElement.getAttr("profilepublic"));
            boolean emailPublic = Java.StringToBoolean(requestElement.getAttr("emailpublic"));
            String email = requestElement.getAttr(Person.EMAIL_FIELD);
            String password = requestElement.getAttr(Account.PASSWORD_FIELD);
            String confirmationUrl = requestElement.getAttr("confirmationurl");

            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
            ProfileLogic logic = new ProfileLogic(oase);

            int personId = logic.createProfile(portalId, applicationId, nickName, firstName, lastName,
                    street, streetNr, zipCode, city, country, mobileNr, photoId,
                    tags, profilePublic, licenseId, email, emailPublic, password, confirmationUrl);

            JXElement response = createResponse(PROFILE_CREATE_SERVICE);
            response.setAttr("id", personId);
            return response;
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    /**
     * Returns a profile.
     *
     * @param anUtopiaRequest The request holding all needed parameters
     * @return a utopia response
     * @throws UtopiaException
     */
    public JXElement getProfile(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            String personId = requestElement.getAttr(Person.ID_FIELD);

            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
            ProfileLogic logic = new ProfileLogic(oase);
            JXElement profile = logic.getProfile(personId);

            JXElement response = createResponse(PROFILE_GET_SERVICE);
            response.addChild(profile);
            return response;
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    /**
     * Activates the profile upon confirmation (by email).
     *
     * @param anUtopiaRequest The request holding all needed parameters
     * @return a utopia response
     * @throws UtopiaException
     */
    public JXElement activateProfile(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
            String code = requestElement.getAttr("code");
            String key = requestElement.getAttr("key");

            ProfileLogic logic = new ProfileLogic(oase);
            logic.activateProfile(code);

            return createResponse(PROFILE_ACTIVATE_SERVICE);
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    /**
     * Sends a personlized JAD file so auto login from the mobile client can be supported.
     *
     * @param anUtopiaRequest The request holding all needed parameters
     * @return a utopia response
     * @throws UtopiaException
     */
    public JXElement sendJad(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
            String personId = requestElement.getAttr("personid");

            ProfileLogic logic = new ProfileLogic(oase);
            logic.sendJAD(personId);

            return createResponse(PROFILE_SENDJAD_SERVICE);
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    /**
     * Updates a user profile
     *
     * @param anUtopiaRequest The request holding all needed parameters
     * @return a utopia response
     * @throws UtopiaException
     */
    public JXElement updateProfile(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();

            String personId = requestElement.getAttr("personid");
            String nickName = requestElement.getAttr("nickname");
            String firstName = requestElement.getAttr(Person.FIRSTNAME_FIELD);
            String lastName = requestElement.getAttr(Person.LASTNAME_FIELD);
            String street = requestElement.getAttr(Person.STREET_FIELD);
            String streetNr = requestElement.getAttr(Person.STREETNR_FIELD);
            String zipCode = requestElement.getAttr(Person.ZIPCODE_FIELD);
            String mobileNr = requestElement.getAttr(Person.MOBILENR_FIELD);
            String city = requestElement.getAttr(Person.CITY_FIELD);
            String country = requestElement.getAttr(Person.COUNTRY_FIELD);
            String tagString = requestElement.getAttr("tags");
            String[] tags = null;
            if (tagString != null && tagString.length() > 0) {
                tags = Java.stripFromDelimeterToArray(tagString, ",");
            }

            String photoId = requestElement.getAttr("mediumid");
            String licenseId = requestElement.getAttr("licenseId");
            boolean profilePublic = Java.StringToBoolean(requestElement.getAttr("profilepublic"));
            boolean emailPublic = Java.StringToBoolean(requestElement.getAttr("emailpublic"));
            String email = requestElement.getAttr(Person.EMAIL_FIELD);
            String password = requestElement.getAttr(Account.PASSWORD_FIELD);

            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
            ProfileLogic logic = new ProfileLogic(oase);

            int id = logic.updateProfile(personId, nickName, firstName, lastName,
                    street, streetNr, zipCode, city, country, mobileNr, photoId,
                    tags, profilePublic, licenseId, email, emailPublic, password);

            JXElement response = createResponse(PROFILE_UPDATE_SERVICE);
            response.setAttr("id", id);
            return response;
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    public JXElement resetPassword(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
            String email = requestElement.getAttr(Person.EMAIL_FIELD);

            ProfileLogic logic = new ProfileLogic(oase);
            logic.resetPassword(email);

            return createResponse(PROFILE_RESETPASSWORD_SERVICE);
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }


}

/*
* $Log:
*
*/