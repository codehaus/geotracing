/**
 *
 *	cd:	Fri May 07 15:55:07 CEST 2004
 *	author:	ronald
 *
 *	$Id$
 *************************************************************/

package org.walkandplay.server.logic;

import nl.justobjects.jox.dom.JXElement;

import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.IO;
import org.keyworx.common.util.Java;
import org.keyworx.common.util.MD5;
import org.keyworx.common.util.MailClient;
import org.keyworx.oase.api.Record;
import org.keyworx.plugin.metadata.MetadataPlugin;
import org.keyworx.plugin.metadata.core.LicenseLogic;
import org.keyworx.plugin.tagging.logic.TagLogic;
import org.keyworx.plugin.tagging.util.Constants;
import org.keyworx.server.ServerConfig;
import org.keyworx.utopia.core.config.ContentHandlerConfig;
import org.keyworx.utopia.core.data.Account;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.Medium;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.utopia.core.data.Role;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.logic.AccountLogic;
import org.keyworx.utopia.core.logic.PersonLogic;
import org.keyworx.utopia.core.util.Core;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.utopia.core.util.Translator;

/**
 * Performs account actions.
 *
 *
 */
public class ProfileLogic {
	private Oase oase;
	private Log log = Logging.getLog("ProfileLogic");
	private String mailServer;
	private String mailSender;
	//private String host;
    private ContentHandlerConfig config;

    public ProfileLogic(Oase o, ContentHandlerConfig aConfig) {
		oase = o;
        config = aConfig;
        mailServer = ServerConfig.getProperty("keyworx.mail.server");
        mailSender = ServerConfig.getProperty("keyworx.mail.sender");
        //host = ServerConfig.getProperty("host.name");
        log.info("Using mailsever: " + mailServer);
        //log.info("Using host: " + host);
    }

	/**
	 * Activates a user account.
	 *
	 * @param aCode The registration code.
	 * @throws UtopiaException
	 */
	public void activateProfile(String aCode) throws UtopiaException {
		try {
			Record[] accounts = oase.getFinder().queryTable(Account.TABLE_NAME, Account.SESSIONKEY_FIELD + " = '" + aCode + "'", null, null);
			if (accounts == null || accounts.length == 0) {
				throw new UtopiaException("This code is not valid.", ErrorCode.__6005_Unexpected_error);
			}

			AccountLogic accountLogic = new AccountLogic(oase);
			accountLogic.updateAccount("" + accounts[0].getId(), null, null, null, Account.ACTIVE_STATE_VALUE, null, null, false);
		} catch (UtopiaException ue) {
			throw ue;
		} catch (Throwable t) {
			throw new UtopiaException(t);
		}
	}

    /**
     * Creates a user profile
     * @param aPortalId
     * @param anApplicationId
     * @param aNickName
     * @param aFirstName
     * @param aLastName
     * @param aStreet
     * @param aStreetNr
     * @param aZipcode
     * @param aCity
     * @param aCountry
     * @param aMobileNr
     * @param aPhotoId
     * @param theTags
     * @param profilePublic
     * @param anEmail
     * @param emailPublic
     * @param aPassword
     * @param aConfirmationUrl
     * @return
     * @throws UtopiaException
     */
    public int createProfile(String aPortalId, String anApplicationId, String aNickName, String aFirstName, String aLastName,
                              String aStreet, String aStreetNr, String aZipcode, String aCity, String aCountry, String aMobileNr,
                              String aPhotoId, String[] theTags, boolean profilePublic, String aLicense, String anEmail,
                              boolean emailPublic, String aPassword, String aConfirmationUrl) throws UtopiaException {
		try {
            // first check required params
            if (aPortalId == null || aPortalId.length() == 0 || !Java.isInt(aPortalId) || oase.get(Core.PORTAL, aPortalId) == null) throw new UtopiaException("No portalid or portal could not be found.");
            if (anApplicationId == null || anApplicationId.length() == 0 || !Java.isInt(anApplicationId)|| oase.get(Core.APPLICATION, anApplicationId) == null) throw new UtopiaException("No applicationid or application could not be found.");
            if (aNickName == null || aNickName.length() == 0) throw new UtopiaException("No nickName found.");
            if (anEmail == null || anEmail.length() == 0) throw new UtopiaException("No email found.");
            if (aPassword == null || aPassword.length() == 0) throw new UtopiaException("No password found.");
            if (aLicense == null || aLicense.length() == 0) throw new UtopiaException("No license found.");
            if (aConfirmationUrl == null || aConfirmationUrl.length() == 0) throw new UtopiaException("No confirmation url found.");

            // check if email address already exists
			Record[] people = oase.getFinder().queryTable(Person.TABLE_NAME, Person.EMAIL_FIELD + "='" + anEmail + "'", null, null);
			if (people != null && people.length > 0) throw new UtopiaException("This email address is already registered.", ErrorCode.__6207_Value_already_in_use);

            // first create the person
            // first and last name are not mandatory
            //if(aFirstName == null || aFirstName.length() == 0) aFirstName = "John";
            //if(aLastName == null || aLastName.length() == 0) aLastName = "Doe";

            // set privacy params
            JXElement extra = new JXElement("extra");
            extra.setAttr("nickname", aNickName);
            extra.setAttr("emailpublic", emailPublic);
            extra.setAttr("profilepublic", profilePublic);

            Person person = (Person) oase.get(Core.PERSON);
			person = person.insert(aFirstName, aLastName, null, aStreet, aStreetNr, aZipcode, aCity, aCountry, null, aMobileNr, anEmail, extra);

            // create the account
            AccountLogic accountLogic = new AccountLogic(oase);
            Record[] roles = oase.getFinder().queryTable(Role.TABLE_NAME, Role.NAME_FIELD + "='" + Role.USER_ROLE_VALUE + "'", null, null);
            String[] rolesIdList = {"" + roles[0].getId()};

            // create the confirmation code and set it
            String code = createProfileCode(anEmail);
            log.info("confirmationcode:" + code);
            accountLogic.insertAccount(aPortalId, "" + person.getId(), rolesIdList, anEmail, aPassword, code, Account.INACTIVE_STATE_VALUE, null);

            // set the default license
            LicenseLogic licenseLogic = new LicenseLogic(oase);
            Record license = licenseLogic.getLicense(-1, aLicense, null);
            licenseLogic.attachLicenseToPerson("" + person.getId(), "" + license.getId());

            // attach a photo
            if(aPhotoId!=null && aPhotoId.length() > 0 && Java.isInt(aPhotoId)){
                Record photo = oase.getFinder().read(Integer.parseInt(aPhotoId));
                if(photo!=null && photo.getTableName().equals(Medium.TABLE_NAME)){
                    oase.getRelater().relate(person.getRecord(), photo, "profile");
                }
            }
            
            // add the tags to the person
            if(theTags!=null){
                TagLogic taglogic = new TagLogic(oase.getOaseSession());
                int[] items = {person.getId()};
                taglogic.tag(person.getId(), items, theTags, Constants.MODE_ADD);
            }
            
            // now create a url for confirmation and send it by mail.
             if(aConfirmationUrl.indexOf("?")!=-1){
                aConfirmationUrl += "&code=" + code;
            }else{
                aConfirmationUrl += "?code=" + code;
            }
            String body = "Thanks for registering!\n";
			body += "To confirm your account please click on the link below!\n";
			//body += host + aConfirmationUrl;
			body += aConfirmationUrl;
            try{
                MailClient.sendMail(mailServer, mailSender, anEmail, "WalkAndPlay account confirmation", body, null, null, null);
            }catch(Throwable t){
                log.error("************** Mail error!!!! Mail not sent!: " + t.toString());
            }

            return person.getId();
		} catch (UtopiaException ue) {
			throw ue;
		} catch (Throwable t) {
			throw new UtopiaException("Exception in createProfile()" + t.toString());
		}
	}

    /**
     * Updates a user profile
     * @param aPersonId
     * @param aNickName
     * @param aFirstName
     * @param aLastName
     * @param aStreet
     * @param aStreetNr
     * @param aZipcode
     * @param aCity
     * @param aCountry
     * @param aMobileNr
     * @param aPhotoId
     * @param theTags
     * @param profilePublic
     * @param anEmail
     * @param emailPublic
     * @param aPassword
     * @throws UtopiaException
     */
	public void updateProfile(String aPersonId, String aNickName, String aFirstName, String aLastName,
                              String aStreet, String aStreetNr, String aZipcode, String aCity, String aCountry, String aMobileNr,
                              String aPhotoId, String[] theTags, boolean profilePublic, String aLicense, String anEmail,
                              boolean emailPublic, String aPassword) throws UtopiaException {
		try {
			// first check required params
            if (aPersonId == null || aPersonId.length() == 0 || !Java.isInt(aPersonId)) throw new UtopiaException("No personId found.");

            JXElement extra = null;
            if(aNickName!=null && aNickName.length()>0){
                // set privacy params
                extra = new JXElement("extra");
                extra.setAttr("nickname", aNickName);
                extra.setAttr("emailpublic", emailPublic);
                extra.setAttr("profilepublic", profilePublic);
            }

            Person person = (Person) oase.get(Core.PERSON, aPersonId);
            if(person == null) throw new UtopiaException("No person found with id " + aPersonId, ErrorCode.__6004_Invalid_attribute_value);
            person = person.update(aFirstName, aLastName, null, aStreet, aStreetNr, aZipcode, aCity, aCountry, null, aMobileNr, anEmail, extra);

            // update the account
            if((anEmail !=null && anEmail.length()>0) || (aPassword!=null && aPassword.length()>0)){
                // check if email address already exists
                Record[] people = oase.getFinder().queryTable(Person.TABLE_NAME, Person.EMAIL_FIELD + "='" + anEmail + "'", null, null);
                    if (people != null && people.length > 0 && Integer.parseInt(aPersonId)!=people[0].getId()) throw new UtopiaException("This email address is already registered.", ErrorCode.__6207_Value_already_in_use);
                person.getAccount().update(anEmail, aPassword, null, createProfileCode(anEmail), null);
            }

            // set the default license
            if (aLicense != null && aLicense.length() != 0) {
	            LicenseLogic licenseLogic = new LicenseLogic(oase);
	            Record license = licenseLogic.getLicense(-1, aLicense, null);
	            licenseLogic.attachLicenseToPerson("" + person.getId(), "" + license.getId());
            }

            // attach a photo
            if(aPhotoId!=null && aPhotoId.length() > 0 && Java.isInt(aPhotoId)){
                Record photo = oase.getFinder().read(Integer.parseInt(aPhotoId));
                if(photo!=null && photo.getTableName().equals(Medium.TABLE_NAME)){
                    // first remove the previous photo if one was attached
                    Record[] media = oase.getRelater().getRelated(person.getRecord(), Medium.TABLE_NAME, "profile");
                    for(int i=0;i<media.length;i++){
                        if(media[i]!=null){
                            oase.getMediaFiler().delete(media[i]);
                        }
                    }

                    // now relate the new photo
                    oase.getRelater().relate(person.getRecord(), photo, "profile");
                }
            }
            
            // add the tags to the person
            if(theTags!=null){
                TagLogic taglogic = new TagLogic(oase.getOaseSession());
                int[] items = {person.getId()};
                taglogic.tag(person.getId(), items, theTags, Constants.MODE_REPLACE);
            }            
		} catch (UtopiaException ue) {
			throw ue;
		} catch (Throwable t) {
			throw new UtopiaException("Exception in updateProfile()" + t.toString());
		}
	}

    public JXElement getProfile(String aPersonId) throws UtopiaException {
		try {
            if(aPersonId == null || aPersonId.length() == 0 || !Java.isInt(aPersonId)) throw new UtopiaException("No valid personid found", ErrorCode.__6002_Required_attribute_missing);
            
            PersonLogic personLogic = new PersonLogic(oase);
            JXElement personElement = personLogic.getPerson(null, aPersonId, false, null, false, null, false, null, null, true, "profile", Medium.IMAGE_KIND);

            // not get the license
            Record person = oase.getFinder().read(Integer.parseInt(aPersonId));
            if(!person.getTableName().equals(Person.TABLE_NAME)){
                throw new UtopiaException("Provided id is NOT a person but was found in table " + person.getTableName(), ErrorCode.__6004_Invalid_attribute_value);
            }
            Record[] licenses = oase.getRelater().getRelated(person, MetadataPlugin.LICENSE_TABLE_NAME, null);
            if(licenses==null){
                throw new UtopiaException("This should not have happened! Profile found without a default license");
            }

            personElement.addChild(Translator.toProtocolNames(licenses[0].toXML(), null, config));

            // and get the tags
            TagLogic tagLogic = new TagLogic(oase.getOaseSession());
            int[] taggers = {person.getId()};
            int[] items = {person.getId()};
            Record[] tags = tagLogic.getTags(null, taggers, items, -1, -1);
            if(tags!=null && tags.length>0){
                JXElement tagsElememt = new JXElement("tags");
                String s = "";
                for(int i=0;i<tags.length;i++){
                    if(i==0){
                        s = tags[i].getStringField("name");
                    }else{
                        s += "," + tags[i].getStringField("name");
                    }
                }
                tagsElememt.setText(s);
                personElement.addChild(tagsElememt);                        
            }

            return personElement;
        } catch (UtopiaException ue) {
			throw ue;
		} catch (Throwable t) {
			throw new UtopiaException(t);
		}
	}

    /**
	 * Sends the jad file to an email address.
	 *
	 * @param aPersonId The person id.
	 * @throws UtopiaException
	 */
	public void sendJAD(String aPersonId) throws UtopiaException {
		try {
			Person person = (Person) oase.get(Core.PERSON, aPersonId);
			String email = person.getEmail();
            if(email == null || email.length() == 0){
                throw new UtopiaException("This should not have happened!! Required field email not found");
            }
            String nickName = person.getExtra().getAttr("nickname");
			if(nickName == null || nickName.length() == 0){
                throw new UtopiaException("This should not have happened!! Required field nickname not found");
            }

            VersionLogic versionLogic = new VersionLogic(oase);
            JXElement version = versionLogic.getVersion();


            String fileName = "/var/keyworx/data/wp/" + nickName + ".jad";
            IO.cp("/var/keyworx/webapps/test.walkandplay.com/wp/WEB-INF/cfg/wp.jad", fileName);

			IO.replaceTokensInFile(fileName, "$[VERSION]", version.getAttr("version"));
			IO.replaceTokensInFile(fileName, "$[KW_USER]", person.getAccount().getLoginName());
			IO.replaceTokensInFile(fileName, "$[KW_PASSWORD]", person.getAccount().getPassword());

			String body = "Here's the jadfile for WalkAndPlay!\n";
			body += "Send this file to your phone by bluetooth and your installation should start automatically.\n";
			body += "Enjoy!\n";

            MailClient.sendMail(mailServer, mailSender, email, "WalkAndPlay jad file", body, null, null, fileName);

        } catch (UtopiaException ue) {
            throw ue;
		} catch (Throwable t) {
            throw new UtopiaException("Exception in sendJAD()" + t.toString());
		}
	}

	/**
	 * Request to reset a password and sends a confirm email.
	 *
	 * @param anEmail The email address
     * @param aConfirmationUrl forward url
	 * @throws UtopiaException
	 */
	public void resetPassword(String anEmail, String aConfirmationUrl) throws UtopiaException {
		try {
			Record[] people = oase.getFinder().queryTable(Person.TABLE_NAME, Person.EMAIL_FIELD + "='" + anEmail + "'", null, null);
			if (people == null || people.length == 0) {
				throw new UtopiaException("No account found for " + anEmail, ErrorCode.__6004_Invalid_attribute_value);
			}

			String code = createProfileCode(anEmail);

            if(aConfirmationUrl.indexOf("?")!=-1){
                aConfirmationUrl += "&code=" + code;
            }else{
                aConfirmationUrl += "?code=" + code;
            }
                
            String body = "You requested an password reset at WalkAndPlay.\n";
			body += "To confirm please click on the link below!\n";
			body += aConfirmationUrl;
            // client sends host info
            //body += host + aConfirmationUrl;

            MailClient.sendMail(mailServer, mailSender, anEmail, "WalkAndPlay account reset request", body, null, null, null);

        } catch (UtopiaException ue) {
			throw ue;
		} catch (Throwable t) {
			throw new UtopiaException("Exception in requestResetProfile()" + t.toString());
		}
	}

    private String createProfileCode(String anEmail) throws UtopiaException{
        try{
        return MD5.createStringDigest(anEmail + anEmail + anEmail);
        } catch (Throwable t) {
			throw new UtopiaException("Exception in creatProfileCode()" + t.toString());
		}
    }
    
}

/*
* $Log:
*/
