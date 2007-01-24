/*
*	cd:	Thu May 06 16:33:32 CEST 2004
*	author:	Ronald Lenz
*
*	$Id$
*************************************************************/

package org.walkandplay.server.control;

import nl.justobjects.jox.dom.JXElement;

import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Java;
import org.keyworx.utopia.core.config.ContentHandlerConfig;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.control.QueryHandler;
import org.keyworx.utopia.core.data.*;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.logic.PersonLogic;
import org.walkandplay.server.logic.ProfileLogic;

import java.util.List;

/**
 * RssHandler.
 * <p/>
 * Redirects the requests to the right logic method
 *
 * @author Ronald Lenz
 * @version $Id$
 */
public class ProfileHandler extends DefaultHandler {

    public final static String PROFILE_GET_SERVICE = "profile-get";
    public final static String PROFILE_CREATE_SERVICE = "profile-create";
    public final static String PROFILE_UPDATE_SERVICE = "profile-update";
    public final static String PROFILE_ACTIVATE_SERVICE = "profile-activate";
    public final static String PROFILE_RESETPASSWORD_SERVICE = "profile-resetpassword";
    public final static String PROFILE_SENDJAD_SERVICE = "profile-sendjad";

    private Log log = Logging.getLog("ProfileHandler");
    private ContentHandlerConfig config;
    private ProfileLogic logic;

    /**
     * Processes the Client Request.
     *
     * @param anUtopiaRequest A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws UtopiaException Standard Utopia exception
     */
    public UtopiaResponse processRequest(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        // Get the service name for the request
        String service = anUtopiaRequest.getServiceName();
        log.info("Handling request for service=" + service);
        log.info(new String(anUtopiaRequest.getRequestCommand().toBytes(false)));

        if(logic == null) logic = new ProfileLogic(anUtopiaRequest.getUtopiaSession().getContext().getOase(), config);
        
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
            log.error("Negative response for service: " + service, ue);
            return new UtopiaResponse(createNegativeResponse(service, ue.getErrorCode(), "Error in request: " + ue.getMessage()));
        } catch (Throwable t) {
            log.error("Unexpected error in service: " + service, t);
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

            JXElement person = requestElement.getChildByTag(Person.XML_TAG);
            String nickName = person.getChildText("nickname");
            String firstName = person.getChildText(Person.FIRSTNAME_FIELD);
            String lastName = person.getChildText(Person.LASTNAME_FIELD);
            String street = person.getChildText(Person.STREET_FIELD);
            String streetNr = person.getChildText(Person.STREETNR_FIELD);
            String zipCode = person.getChildText(Person.ZIPCODE_FIELD);
            String mobileNr = person.getChildText(Person.MOBILENR_FIELD);
            String city = person.getChildText(Person.CITY_FIELD);
            String country = person.getChildText(Person.COUNTRY_FIELD);

            List tagElms = requestElement.getChildrenByTag(org.keyworx.plugin.tagging.util.Constants.TAG_ELEMENT);
            String[] tags = null;
            if (tagElms != null) {
                tags = new String[tagElms.size()];
                for(int i=0;i<tagElms.size();i++){
                    tags[i] = ((JXElement)tagElms.get(i)).getText();
                }                
            }

            String photoId = requestElement.getChildText("photoid");
            String license = requestElement.getChildText("license");
            boolean profilePublic = Java.StringToBoolean(person.getChildText("profilepublic"));
            boolean emailPublic = Java.StringToBoolean(person.getChildText("emailpublic"));
            String email = person.getChildText(Person.EMAIL_FIELD);
            String password = person.getChildText(Account.PASSWORD_FIELD);
            String confirmationUrl = requestElement.getChildText("confirmationurl");

            int personId = logic.createProfile(portalId, applicationId, nickName, firstName, lastName,
                    street, streetNr, zipCode, city, country, mobileNr, photoId,
                    tags, profilePublic, license, email, emailPublic, password, confirmationUrl);

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
            String code = requestElement.getAttr("code");

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
            String personId = requestElement.getAttr(Person.ID_FIELD);

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
            String personId = requestElement.getAttr(Person.ID_FIELD);
            PersonLogic personLogic = new PersonLogic(anUtopiaRequest.getUtopiaSession().getContext().getOase());
            JXElement personElement = personLogic.getPerson(null, personId, false, null, false, null, false, null, null, false, null, null);
            if(personElement == null) throw new UtopiaException("person with id " + personId + " does not exist!");

            JXElement person = requestElement.getChildByTag(Person.XML_TAG);
            String nickName = null;
            String firstName = null;
            String lastName = null;
            String street = null;
            String streetNr = null;
            String zipCode = null;
            String mobileNr = null;
            String city = null;
            String country = null;
            String email = null;
            String password = null;
            if(person!=null){
                nickName = person.getChildText("nickname");
                firstName = person.getChildText(Person.FIRSTNAME_FIELD);
                lastName = person.getChildText(Person.LASTNAME_FIELD);
                street = person.getChildText(Person.STREET_FIELD);
                streetNr = person.getChildText(Person.STREETNR_FIELD);
                zipCode = person.getChildText(Person.ZIPCODE_FIELD);
                mobileNr = person.getChildText(Person.MOBILENR_FIELD);
                city = person.getChildText(Person.CITY_FIELD);
                country = person.getChildText(Person.COUNTRY_FIELD);
                email = person.getChildText(Person.EMAIL_FIELD);
                password = person.getChildText(Account.PASSWORD_FIELD);
            }
            List tagElms = requestElement.getChildrenByTag(org.keyworx.plugin.tagging.util.Constants.TAG_ELEMENT);
            String[] tags = null;
            if (tagElms != null && tagElms.size()>0) {
                tags = new String[tagElms.size()];
                for(int i=0;i<tagElms.size();i++){
                    tags[i] = ((JXElement)tagElms.get(i)).getText();
                }
            }

            // check fields

            /*String tables = "utopia_person,utopia_account,g_track";
            String fields = "utopia_account.loginname,utopia_person.id AS personid,utopia_person.extra,g_track.id,g_track.name";
            String where = "utopia_account.loginname = '" + userName + "'";
            String relations = "utopia_account,utopia_person;g_track,utopia_person";
            String postCond = "ORDER BY g_track.id";
            JXElement result = QueryHandler.queryStoreReq2(oase, tables, fields, where, relations, postCond);*/

            boolean profilePublic = false;
            JXElement extra = personElement.getChildByTag("extra");
            if(extra!=null && extra.getAttr("profilepublic").equals("true")){
                profilePublic = true;
            }
            if(person!=null){
                String pp = person.getChildText("profilepublic");
                if(pp!=null && pp.length()>0){
                    profilePublic = Java.StringToBoolean(pp);
                }
            }

            boolean emailPublic = false;
            if(extra!=null && extra.getAttr("profilepublic").equals("true")){
                emailPublic = true;
            }
            if(person!=null){
                String ep = person.getChildText("emailpublic");
                if(ep!=null && ep.length()>0){
                    emailPublic = Java.StringToBoolean(ep);
                }
            }

            String photoId = requestElement.getChildText("photoid");
            String license = requestElement.getChildText("license");

            logic.updateProfile(personId, nickName, firstName, lastName,
                    street, streetNr, zipCode, city, country, mobileNr, photoId,
                    tags, profilePublic, license, email, emailPublic, password);

            return createResponse(PROFILE_UPDATE_SERVICE);
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    public JXElement resetPassword(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            String email = requestElement.getAttr(Person.EMAIL_FIELD);
            String confirmationUrl = requestElement.getAttr("confirmationurl");

            logic.resetPassword(email, confirmationUrl);

            return createResponse(PROFILE_RESETPASSWORD_SERVICE);
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    /**
	 * Overridden to have a hook to do the initialisation.
	 * @param aKey
	 * @param aValue
	 * @see org.keyworx.utopia.core.control.Handler#setProperty(java.lang.String, java.lang.String)
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

}

/*
* $Log:
*
*/