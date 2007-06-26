package nl.diwi.control;

import nl.diwi.logic.TrafficLogic;
import nl.diwi.logic.TripLogic;
import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.MailClient;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.*;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.utopia.core.util.Core;
import org.keyworx.utopia.core.logic.PersonLogic;
import org.keyworx.server.ServerConfig;

import java.util.Vector;

public class UserHandler extends DefaultHandler implements Constants {

    public final static String USER_GET_PREFERENCES = "user-get-preferences";
    public final static String USER_GET_STATS = "user-get-stats";
    public final static String USER_REGISTER = "user-register";


    /**
     * Processes the Client Request.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws UtopiaException standard Utopia exception
     */
    public UtopiaResponse processRequest(UtopiaRequest anUtopiaReq) throws UtopiaException {
        Log log = Logging.getLog(anUtopiaReq);
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
            Vector prefElms = requestElement.getChildrenByTag(PREF_ELM);
            for (int i = 0; i < prefElms.size(); i++) {
                JXElement prefElm = (JXElement) prefElms.elementAt(i);

                // create the pref
                Record pref = oase.getModifier().create(PREFS_TABLE);
                pref.setIntField(OWNER_FIELD, person.getId());
                pref.setStringField(NAME_FIELD, prefElm.getAttr(NAME_FIELD));
                pref.setStringField(VALUE_FIELD, prefElm.getAttr(VALUE_FIELD));
                pref.setIntField(TYPE_FIELD, prefElm.getIntAttr(TYPE_FIELD));
    
                oase.getModifier().insert(pref);

                // relate pref to person
                oase.getRelater().relate(person.getRecord(), pref, "register");
            }

            String subject = "Digitale Wichelroede registration for " + firstName + " " + lastName;
            String body = "Digitale Wichelroede registration for: \n" +
                    firstName + " " + lastName + "\n" +
                    birthDate + "\n" +
                    street + " " + streetNr + "\n" +
                    zipcode + " " + city + "\n" +
                    country + "\n" +
                    phoneNr + "\n" +
                    mobileNr + "\n" +
                    email + "\n" +
                    loginName + "\n" +
                    password + "\n";

            String host = ServerConfig.getProperty("keyworx.mail.server");
            String recipient = ServerConfig.getProperty("keyworx.mail.recipient");

            MailClient.sendMail(host, "Digitale Wichelroede", recipient, subject, body, null, null, null);

            JXElement responseElement = createResponse(USER_REGISTER);
            responseElement.setAttr("id", "" + person.getId());

            return responseElement;
        }catch(Throwable t){
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

                // add the trips
                personElm.addChildren(tripLogic.getTrips("" + person.getId()));
                
                // add the traffic
                personElm.addChildren(trafficLogic.getTrafficForPerson("" + person.getId()));

            }
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


