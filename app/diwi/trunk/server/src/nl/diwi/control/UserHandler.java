package nl.diwi.control;

import nl.diwi.logic.LogLogic;
import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import nl.justobjects.jox.parser.JXBuilderListener;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.store.record.FileFieldImpl;
import org.keyworx.server.ServerConfig;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.control.ThreadSafe;
import org.keyworx.utopia.core.data.*;
import org.keyworx.utopia.core.logic.PersonLogic;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Core;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.amuse.core.Protocol;
import org.geotracing.handler.QueryLogic;
import org.geotracing.handler.QueryHandler;
import org.geotracing.handler.TrackLogic;
import org.postgis.Point;
import org.postgis.PGgeometryLW;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;
import java.util.Map;
import java.io.File;

public class UserHandler extends DefaultHandler implements ThreadSafe, Constants {

    public final static String USER_GET_PREFERENCES_SERVICE = "user-get-preferences";
    public final static String USER_GET_STATS_SERVICE = "user-get-stats";
    public final static String USER_GET_ALLSTATS_SERVICE = "user-get-allstats";
    public final static String USER_GETLIST_SERVICE = "user-getlist";
    public final static String USER_REGISTER_SERVICE = "user-register";

    /**
     * Processes the Client Request.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws UtopiaException standard Utopia exception
     */
    public UtopiaResponse processRequest(UtopiaRequest anUtopiaReq) throws UtopiaException {
        String service = anUtopiaReq.getServiceName();
        Log log = Logging.getLog(anUtopiaReq);
        log.trace("Handling request for service=" + service);

        JXElement response;
        try {
            if (service.equals(USER_GET_PREFERENCES_SERVICE)) {
                response = getPreferences(anUtopiaReq);
            } else if (service.equals(USER_REGISTER_SERVICE)) {
                response = register(anUtopiaReq);
            } else if (service.equals(USER_GET_STATS_SERVICE)) {
                response = getStats(anUtopiaReq);
            } else if (service.equals(USER_GET_ALLSTATS_SERVICE)) {
                response = getAllStats(anUtopiaReq);
            } else if (service.equals(USER_GETLIST_SERVICE)) {
                response = getUsers(anUtopiaReq);
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

        // Always return a response
        log.trace("Handled service=" + service + " response=" + response.getTag());
        return new UtopiaResponse(response);
    }

    public JXElement register(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        Log log = Logging.getLog("UserHandler.register");
        try {
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

                if (i == 0) {
                    prefsString = name + "=" + value;
                } else {
                    prefsString += ", " + name + "=" + value;
                }

                oase.getModifier().insert(pref);

                // relate pref to person
                oase.getRelater().relate(person.getRecord(), pref, "register");
            }

            /*String subject = "Digitale Wichelroede registration for " + firstName + " " + lastName;
            String body = "Digitale Wichelroede registration for: \n";
            body += "+++++++++++++++++++++++++++++++++++++++++++++\n\n";
            body += firstName + " " + lastName + "\n\n";
            body += "birthdate: \n";
            if (birthDate != null && birthDate.length() > 0) {
                body += birthDate + "\n";
            } else {
                body += "-\n";
            }
            body += "address: \n";
            if (street != null && street.length() > 0) {
                body += street;
            } else {
                body += "-";
            }
            if (streetNr != null && streetNr.length() > 0) {
                body += " " + streetNr;
            } else {
                body += " -";
            }
            if (zipcode != null && zipcode.length() > 0) {
                body += "\n" + zipcode;
            } else {
                body += "\n-";
            }
            if (city != null && city.length() > 0) {
                body += " " + city + "\n";
            } else {
                body += " -\n";
            }
            body += "email: \n";
            if (email != null && email.length() > 0) {
                body += email + "\n";
            } else {
                body += "-\n";
            }
            body += "phonenr: \n";
            if (phoneNr != null && phoneNr.length() > 0) {
                body += phoneNr + "\n";
            } else {
                body += "-\n";
            }
            body += "mobilenr: \n";
            if (mobileNr != null && mobileNr.length() > 0) {
                body += mobileNr + "\n";
            } else {
                body += "-\n";
            }
            body += "loginname: \n";
            if (loginName != null && loginName.length() > 0) body += loginName + "\n";
            body += "password: \n";
            if (password != null && password.length() > 0) body += password;

            body += "\n\n";
            body += "preferences:\n";
            body += prefsString;

            body += "\n\n+++++++++++++++++++++++++++++++++++++++++++++";

            String mailHost = ServerConfig.getProperty("mail.host");
            String mailRecipient = ServerConfig.getProperty("mail.recipient");
            String mailUser = ServerConfig.getProperty("mail.user");
            String mailPassword = ServerConfig.getProperty("mail.password");

            sendMail(mailHost, mailUser, mailPassword, mailRecipient, "DigitaleWichelroede", subject, body);*/

            JXElement responseElement = createResponse(USER_REGISTER_SERVICE);
            responseElement.setAttr("id", "" + person.getId());

            return responseElement;
        } catch (Throwable t) {
            log.error("Exception in register user: " + t.getMessage());
            throw new UtopiaException(t);
        }
    }

    private void sendMail(String aHost, String aUser, String aPassword, String aRecipient, String aSender, String aSubject, String aBody) throws UtopiaException {
        Log log = Logging.getLog("UserHandler.sendMail");
        try {
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

        } catch (Throwable t) {
            log.error("Exception in sendMail:" + t.getMessage());
            throw new UtopiaException(t);
        }
    }

    private JXElement getUsers(UtopiaRequest anUtopiaReq) throws UtopiaException {
        Oase oase = anUtopiaReq.getUtopiaSession().getContext().getOase();

        JXElement response = createResponse(USER_GETLIST_SERVICE);

        try {
            String query = "SELECT * from " + Person.TABLE_NAME + " WHERE firstname NOT LIKE '%geoapp%' AND firstname NOT LIKE '%admin%'";
            Record[] recs = oase.getFinder().freeQuery(query);
            for(int i=0;i<recs.length;i++){
                JXElement e = recs[i].toXML();
                e.setTag(Person.XML_TAG);
                response.addChild(e);
            } 
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
        return response;
    }

    private JXElement processStat(Oase anOase, String aPersonId) throws UtopiaException{
        try {
            Log log = Logging.getLog("UserHandler.processStat");
            Record person = anOase.getFinder().read(Integer.parseInt(aPersonId));
            JXElement personElm = person.toXML();
            personElm.setTag(Person.XML_TAG);
            personElm.removeChildByTag(Person.CREATION_DATE_FIELD);
            personElm.removeChildByTag(Person.MODIFICATION_DATE_FIELD);
            personElm.removeChildByTag(Person.EXTRA_FIELD);
            personElm.removeChildByTag("owner");

            LogLogic logLogic = new LogLogic(anOase);

            Record[] prefs = anOase.getRelater().getRelated(person, PREFS_TABLE, "register");
            for (int j = 0; j < prefs.length; j++) {
                Record record = prefs[j];
                JXElement prefElm = new JXElement(PREF_ELM);
                prefElm.setAttr(NAME_FIELD, record.getStringField(NAME_FIELD));
                prefElm.setAttr(VALUE_FIELD, record.getStringField(VALUE_FIELD));
                personElm.addChild(prefElm);
            }

            // add the trips
            Vector tripLogs = logLogic.getLogs("" + person.getId(), LOG_MOBILE_TYPE);
            for (int j = 0; j < tripLogs.size(); j++) {
                personElm.addChild(logLogic.getLog(((JXElement) tripLogs.elementAt(j)).getAttr(ID_FIELD)));
            }

            // add the weblog
            Vector webLogs = logLogic.getLogs("" + person.getId(), LOG_WEB_TYPE);
            for (int h = 0; h < webLogs.size(); h++) {
                log.info("" + h);
                personElm.addChild(logLogic.getLog(((JXElement) webLogs.elementAt(h)).getAttr(ID_FIELD)));
            }

            return personElm;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    private JXElement getStats(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement response = createResponse(USER_GET_STATS_SERVICE);
        String personId = anUtopiaReq.getRequestCommand().getAttr("id");
        response.addChild(processStat(anUtopiaReq.getUtopiaSession().getContext().getOase(), personId));

        return response;
    }

    private JXElement getAllStats(UtopiaRequest anUtopiaReq) throws UtopiaException, OaseException {
        /*
        Log log = Logging.getLog("UserHandler.getAllStats");
        Oase oase = anUtopiaReq.getUtopiaSession().getContext().getOase();

        String start = anUtopiaReq.getRequestCommand().getAttr("start");
        String end = anUtopiaReq.getRequestCommand().getAttr("end");

        LogLogic logLogic = new LogLogic(oase);
        JXElement response = createResponse(USER_GET_ALLSTATS_SERVICE);

        try {
            Record[] people = new Record[0];
            if(start!=null && start.length()>0 && end!=null & end.length()>0){
                String query = "SELECT * from " + Person.TABLE_NAME + " WHERE firstname NOT LIKE '%geoapp%' AND firstname NOT LIKE '%admin%'";
                people = oase.getFinder().freeQuery(query);
            }else{
                // get all
                String query = "SELECT * from " + Person.TABLE_NAME + " WHERE firstname NOT LIKE '%geoapp%' AND firstname NOT LIKE '%admin%'";
                people = oase.getFinder().freeQuery(query);
            }


            for (int i = 0; i < people.length; i++) {
                Record person = people[i];
                response.addChild(processStat(anUtopiaReq.getUtopiaSession().getContext().getOase(), "" + person.getId()));
            }
            //System.out.println(new String(response.toBytes(false)));
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
        return response;
        */

        JXElement response = createResponse(USER_GET_ALLSTATS_SERVICE);
        Oase oase = anUtopiaReq.getUtopiaSession().getContext().getOase();

        // first get all the people
        Record[] people = oase.getFinder().readAll(Person.TABLE_NAME);
        for (int i = 0; i < people.length; i++) {
            Record person = people[i];
            JXElement personElm = new JXElement(Person.XML_TAG);
            personElm.setAttr(ID_FIELD, person.getId());
            personElm.setChildText(Person.FIRSTNAME_FIELD, person.getStringField(Person.FIRSTNAME_FIELD));
            personElm.setChildText(Person.LASTNAME_FIELD, person.getStringField(Person.LASTNAME_FIELD));

            // get register prefs
            Record[] prefs = oase.getRelater().getRelated(person, "diwi_prefs", "register");
            for (int j = 0; j < prefs.length; j++) {
                Record pref = prefs[j];
                JXElement e = new JXElement("pref");
                e.setAttr(pref.getStringField("name"), pref.getStringField("value"));
                personElm.addChild(e);
            }

            // get tracks
            Record[] tracks = oase.getRelater().getRelated(person, "g_track", null);
            for (int j = 0; j < tracks.length; j++) {
                Record track = tracks[j];

                JXElement tripElm = new JXElement("trip");
                tripElm.setChildText(STARTDATE_FIELD, "" + track.getTimestampField(STARTDATE_FIELD));
                tripElm.setChildText(ENDDATE_FIELD, "" + track.getTimestampField(END_DATE_FIELD));
                tripElm.setChildText(DISTANCE_FIELD, "" + track.getRealField(DISTANCE_FIELD));

                personElm.addChild(tripElm);

                // get the related ugs
                Record[] ugcs = oase.getRelater().getRelated(track, Medium.TABLE_NAME, "medium");
                for (int k = 0; k < ugcs.length; k++) {
                    Record ugc = ugcs[k];

                    JXElement e = new JXElement(Medium.XML_TAG);
                    e.setAttr(ID_FIELD, ugc.getId());
                    e.setChildText(Medium.NAME_FIELD, ugc.getStringField(Medium.NAME_FIELD));
                    e.setChildText(Medium.KIND_FIELD, ugc.getStringField(Medium.KIND_FIELD));
                    e.setChildText(Medium.FILENAME_FIELD, ugc.getStringField(Medium.FILENAME_FIELD));
                    e.setChildText(Medium.FILESIZE_FIELD, "" + ugc.getLongField(Medium.FILESIZE_FIELD));
                    e.setChildText(Medium.CREATION_DATE_FIELD, "" + ugc.getTimestampField(Medium.CREATION_DATE_FIELD));

                    tripElm.addChild(e);
                }

                // get the trip log
                Record[] logs = oase.getRelater().getRelated(track, LOG_TABLE, null);
                for (int k = 0; k < logs.length; k++) {
                    Record log = logs[k];

                    JXElement events = getEvents(log);
                    tripElm.setChildText("nrOfRoamActions", "" + events.getChildrenByTag("msg").size());

                    tripElm.setChildText("nrOfRouteActivations", "" + events.getChildrenByTag("nav-activate-route-req").size());
                    // <nav-activate-route-req init="True" id="14330" time="1187518523486" date="Sunday, 19 Aug 2007 12:15:23"/>
                    Vector v = events.getChildrenByTag("nav-activate-route-req");
                    for(int l = 0; l < v.size(); l++){
                        JXElement elm = (JXElement)v.elementAt(l);
                        Record rec = oase.getFinder().read(Integer.parseInt(elm.getAttr(ID_FIELD)));
                        elm.setAttr(NAME_FIELD, rec.getStringField(NAME_FIELD));
                    }

                    tripElm.setChildText("nrOfRouteDeactivations", "" + events.getChildrenByTag("nav-deactivate-route-req").size());
                    tripElm.setChildText("nrOfMediaUploads", "" + events.getChildrenByTag("nav-add-medium-req").size());
                    tripElm.setChildText("nrOfMapLoads", "" + events.getChildrenByTag("nav-get-map-req").size());
                    tripElm.setChildText("nrOfApplicationStarts", "" + events.getChildrenByTag("nav-start-req").size());
                    tripElm.setChildText("nrOfApplicationShutdowns", "" + events.getChildrenByTag("nav-stop-req").size());
                    tripElm.setChildText("nrOfUGCOnActions", "" + events.getChildrenByTag("nav-ugc-on-req").size());
                    tripElm.setChildText("nrOfPOIHits", "" + events.getChildrenByTag("poi-hit").size());
                    Vector w = events.getChildrenByTag("poi-hit");
                    for(int l = 0; l < w.size(); l++){
                        JXElement elm = (JXElement)w.elementAt(l);
                        Record rec = oase.getFinder().read(Integer.parseInt(elm.getAttr(ID_FIELD)));
                        elm.setAttr(NAME_FIELD, rec.getStringField(NAME_FIELD));
                    }

                    tripElm.setChildText("nrOfPOIsRequested", "" + events.getChildrenByTag("nav-poi-get-req").size());
                    tripElm.setChildText("nrOfRouteListRequested", "" + events.getChildrenByTag("nav-route-getlist-req").size());
                    tripElm.setChildText("nrOfRouteDetailsRequested", "" + events.getChildrenByTag("nav-route-get-req").size());
                    tripElm.setChildText("nrOfRoutesHomeRequested", "" + events.getChildrenByTag("nav-route-home-req").size());

                    if(events.hasChildren()){
                        tripElm.addChild(events);
                    }
                }

            }
            response.addChild(personElm);
        }

        return response;
    }

    private JXElement getEvents(Record aLogRecord) throws UtopiaException {
        final JXElement events = new JXElement("events");

        try {
            JXBuilder builder = new JXBuilder(
                    new JXBuilderListener() {
                        /**
                         * Called by XmlElementParser when it parsed and created an JXElement.
                         */
                        public void element(JXElement e) {
                            events.addChild(e);
                        }

                        /**
                         * Called when parser encounters an error.
                         */
                        public void error(String msg) {
                            Logging.getLog().warn("getLog() error: " + msg);
                        }

                        /**
                         * End of input stream is reached.
                         * <p/>
                         * This may occur when listening for multiple documents on a stream.
                         *
                         * @param message     text message
                         * @param anException optional exception that caused the stream end
                         */
                        public void endInputStream(String message, Throwable anException) {
                            Logging.getLog().trace("getLog() EOF reached: " + message + " e=" + anException);
                        }

                    }
            );

            // Event data file
            FileFieldImpl fileField = (FileFieldImpl) aLogRecord.getFileField(EVENTS_FIELD);
            File file = fileField.getStoredFile();

            // Only makes sense to parse a file with content
            if (file != null && file.exists() && file.length() > 0) {
                builder.setMultiDoc(true);
                builder.build(fileField.getFileInputStream());
            }

        } catch (Throwable t) {
            new UtopiaException("Error query getLogEvents logId=" + aLogRecord.getId(), t);
        }

        return events;
    }

    private JXElement getPreferences(UtopiaRequest anUtopiaReq) throws UtopiaException {
        Oase oase = anUtopiaReq.getUtopiaSession().getContext().getOase();
        String personId = anUtopiaReq.getUtopiaSession().getContext().getUserId();
        Record person;
        JXElement response = createResponse(USER_GET_PREFERENCES_SERVICE);

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


