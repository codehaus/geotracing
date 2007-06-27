package nl.diwi.control;

import nl.diwi.logic.TrafficLogic;
import nl.diwi.logic.TripLogic;
import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.*;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.utopia.core.util.Core;
import org.keyworx.utopia.core.logic.PersonLogic;
import org.keyworx.server.ServerConfig;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.Session;
import java.util.Vector;
import java.util.Properties;
import java.util.Date;

public class UserHandler extends DefaultHandler implements Constants {

    public final static String USER_GET_PREFERENCES = "user-get-preferences";
    public final static String USER_GET_STATS = "user-get-stats";
    public final static String USER_REGISTER = "user-register";
    Log log = Logging.getLog("UserHandler");


    /**
     * Processes the Client Request.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws UtopiaException standard Utopia exception
     */
    public UtopiaResponse processRequest(UtopiaRequest anUtopiaReq) throws UtopiaException {
        String service = anUtopiaReq.getServiceName();
        log.trace("Handling request for service=" + service);

        JXElement response;
        try {
            if (service.equals(USER_GET_PREFERENCES)) {
                response = getPreferences(anUtopiaReq);
            } else if (service.equals(USER_REGISTER)) {
                response = register(anUtopiaReq);
            } else if (service.equals(USER_GET_STATS)) {
                response = getStats(anUtopiaReq);
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

    public JXElement register(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try{
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
            Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
            Application application = (Application) oase.get(Core.APPLICATION, anUtopiaRequest.getUtopiaSession().getContext().getApplicationId());
            Role role = application.getRole(Role.USER_ROLE_VALUE);


            JXElement personElement = requestElement.getChildByTag(Person.XML_TAG);
            String firstName = personElement.getChildText(Person.FIRSTNAME_FIELD);
            String lastName = personElement.getChildText(Person.LASTNAME_FIELD);
            String birthDate = personElement.getChildText(Person.BIRTHDATE_FIELD);
            String street = personElement.getChildText(Person.STREET_FIELD);
            String streetNr = personElement.getChildText(Person.STREETNR_FIELD);
            String zipcode = personElement.getChildText(Person.ZIPCODE_FIELD);
            String city = personElement.getChildText(Person.CITY_FIELD);
            String country = personElement.getChildText(Person.COUNTRY_FIELD);
            String phoneNr = personElement.getChildText(Person.PHONENR_FIELD);
            String mobileNr = personElement.getChildText(Person.MOBILENR_FIELD);
            String email = personElement.getChildText(Person.EMAIL_FIELD);
            String loginName = personElement.getChildText(Account.LOGINNAME_FIELD);
            String password = personElement.getChildText(Account.PASSWORD_FIELD);

            String[] roleIdList = {"" + role.getId()};

            PersonLogic personLogic = new PersonLogic(oase);
            Person person = personLogic.insertPerson(anUtopiaRequest.getUtopiaSession().getContext().getPortalId(), null, null,
                    firstName, lastName, birthDate, street, streetNr, zipcode, city, country, phoneNr, mobileNr, email, null,
                    true, loginName, password, null, roleIdList);

            // now store the prefs
            String prefsString = "";
            Vector prefElms = requestElement.getChildrenByTag(PREF_ELM);
            for (int i = 0; i < prefElms.size(); i++) {
                JXElement prefElm = (JXElement) prefElms.elementAt(i);
                String name = prefElm.getAttr(NAME_FIELD);
                String value = prefElm.getAttr(VALUE_FIELD);

                // create the pref
                Record pref = oase.getModifier().create(PREFS_TABLE);
                pref.setIntField(OWNER_FIELD, person.getId());
                pref.setStringField(NAME_FIELD, name);
                pref.setStringField(VALUE_FIELD, value);

                if(i==0){
                    prefsString = name + "=" + value;
                }else{
                    prefsString += ", " + name + "=" + value;
                }

                oase.getModifier().insert(pref);

                // relate pref to person
                oase.getRelater().relate(person.getRecord(), pref, "register");
            }

            String subject = "Digitale Wichelroede registration for " + firstName + " " + lastName;
            String body = "Digitale Wichelroede registration for: \n";
            body +="+++++++++++++++++++++++++++++++++++++++++++++\n\n";
            body += firstName + " " + lastName + "\n\n";
            body += "birthdate: \n";
            if(birthDate!=null && birthDate.length()>0){
                body += birthDate + "\n";
            }else{
                body += "-\n";
            }
            body += "address: \n";
            if(street!=null && street.length()>0) {
                body += street;
            }else{
                body += "-";
            }
            if(streetNr!=null && streetNr.length()>0){
                body += " " + streetNr;
            }else{
                body += " -";
            }
            if(zipcode!=null && zipcode.length()>0){
                body += "\n" + zipcode;
            }else{
                body += "\n-";
            }
            if(city!=null && city.length()>0){
                body += " " + city + "\n";
            }else{
                body += " -\n";
            }
            body += "email: \n";
            if(email!=null && email.length()>0){
                body += email + "\n";
            }else{
                body += "-\n";
            }
            body += "phonenr: \n";
            if(phoneNr!=null && phoneNr.length()>0){
                body += phoneNr + "\n";
            }else{
                body += "-\n";
            }
            body += "mobilenr: \n";
            if(mobileNr!=null && mobileNr.length()>0){
                body += mobileNr + "\n";
            }else{
                body += "-\n";
            }
            body += "loginname: \n";
            if(loginName!=null && loginName.length()>0) body += loginName  + "\n";
            body += "password: \n";
            if(password!=null && password.length()>0) body += password;

            body += "\n\n";
            body += "preferences:\n";
            body += prefsString;

            body +="\n\n+++++++++++++++++++++++++++++++++++++++++++++";

            String mailHost = ServerConfig.getProperty("keyworx.mail.host");
            String mailRecipient = ServerConfig.getProperty("keyworx.mail.recipient");
            String mailUser = ServerConfig.getProperty("keyworx.mail.user");
            String mailPassword = ServerConfig.getProperty("keyworx.mail.password");

            sendMail(mailHost, mailUser, mailPassword, mailRecipient, "DigitaleWichelroede", subject, body);

            JXElement responseElement = createResponse(USER_REGISTER);
            responseElement.setAttr("id", "" + person.getId());

            return responseElement;
        }catch(Throwable t){
            log.error("Exception in register user: " + t.getMessage());
            throw new UtopiaException(t);
        }
    }

    private void sendMail(String aHost, String aUser, String aPassword, String aRecipient, String aSender, String aSubject, String aBody) throws UtopiaException{
        try{
            Properties props = System.getProperties();
            props.put("mail.host", aHost);
            props.put("mail.user", aUser);
            props.put("mail.password", aPassword);

            Session mailSession = Session.getDefaultInstance(props, null);
            Message msg = new MimeMessage(mailSession);

            msg.setFrom(new InternetAddress(aSender));
            InternetAddress[] address = {new InternetAddress(aRecipient)};
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(aSubject);
            msg.setSentDate(new Date());
            msg.setText(aBody);

            Transport.send(msg);
            
        }catch(Throwable t){
            log.error("Exception in sendMail:" + t.getMessage());
            throw new UtopiaException(t);
        }
    }

    private JXElement getStats(UtopiaRequest anUtopiaReq) throws UtopiaException {
        Oase oase = anUtopiaReq.getUtopiaSession().getContext().getOase();

            TripLogic tripLogic = new TripLogic(oase);
        TrafficLogic trafficLogic = new TrafficLogic(oase);
        JXElement response = createResponse(USER_GET_STATS);

        try {
            Record[] people = oase.getFinder().readAll(Person.TABLE_NAME);
            for(int i=0;i<people.length;i++){
                Record person = people[i];
                JXElement personElm = person.toXML();
                personElm.setTag(Person.XML_TAG);
                personElm.removeChildByTag(Person.CREATION_DATE_FIELD);
                personElm.removeChildByTag(Person.MODIFICATION_DATE_FIELD);
                personElm.removeChildByTag(Person.EXTRA_FIELD);
                personElm.removeChildByTag("owner");

                Record[] prefs = oase.getRelater().getRelated(person, PREFS_TABLE, "register");
                for(int j=0;j<prefs.length;j++){
                    Record pref = prefs[j];
                    JXElement prefElm = pref.toXML();
                    prefElm.removeChildByTag(Person.CREATION_DATE_FIELD);
                    prefElm.removeChildByTag(Person.MODIFICATION_DATE_FIELD);
                    prefElm.removeChildByTag(Person.EXTRA_FIELD);
                    prefElm.removeChildByTag("owner");

                    personElm.addChild(prefElm);
                }
                //log.info(new String(personElm.toBytes(false)));

                // add the trips
                Vector trips = tripLogic.getTrips("" + person.getId());
                for(int j=0;j<trips.size();j++){
                    JXElement trip = (JXElement)trips.elementAt(j);
                    personElm.addChild(tripLogic.getTrip(trip.getAttr(ID_FIELD)));
                }
                //log.info(new String(personElm.toBytes(false)));
                
                // add the traffic
                personElm.addChildren(trafficLogic.getTrafficForPerson("" + person.getId()));
                log.info(new String(personElm.toBytes(false)));
                response.addChild(personElm);

            }
            System.out.println(new String(response.toBytes(false)));
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
        return response;
    }

    private JXElement getPreferences(UtopiaRequest anUtopiaReq) throws UtopiaException {
        Oase oase = anUtopiaReq.getUtopiaSession().getContext().getOase();
        String personId = anUtopiaReq.getUtopiaSession().getContext().getUserId();
        Record person;
        JXElement response = createResponse(USER_GET_PREFERENCES);

        try {
            person = oase.getFinder().read(Integer.parseInt(personId));
            Record[] prefs = oase.getRelater().getRelated(person, PREFS_TABLE, "route");
            for (int i = 0; i < prefs.length; i++) {
                JXElement pref = new JXElement(PREF_ELM);
                pref.setAttr(NAME_FIELD, prefs[i].getStringField(NAME_FIELD));
                pref.setAttr(VALUE_FIELD, prefs[i].getStringField(VALUE_FIELD));
                response.addChild(pref);
            }
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
        return response;
    }

    protected JXElement unknownReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
        String service = anUtopiaReq.getServiceName();
        Logging.getLog(anUtopiaReq).warn("Unknown service " + service);
        return createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
    }

}


