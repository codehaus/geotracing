package nl.diwi.logic;

import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Sys;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.utopia.core.util.Oase;

import java.util.Properties;
import java.util.Vector;
import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;

public class TrafficLogic implements Constants {
    private Oase oase;
    private Log log = Logging.getLog("TrafficLogic");
    private static final Properties properties = new Properties();
    Format formatter = new SimpleDateFormat("EEEE, dd MMM yyyy HH:mm:ss");

    public TrafficLogic(Oase o) {
        oase = o;
    }

    private Record createTrafficLog(String aPersonId) throws UtopiaException {
        try {
            // first check if there's already an active trip, if so return this one.

            Record person = oase.getFinder().read(Integer.parseInt(aPersonId));
            if(person == null){
                throw new UtopiaException("No person found with id " + aPersonId);
            }

            String name = person.getStringField(Person.FIRSTNAME_FIELD) + " "
                    + person.getStringField(Person.LASTNAME_FIELD)
                    + "'s trip on " + formatter.format(new Date());

            Record trip = oase.getModifier().create(TRIP_TABLE);
            trip.setStringField(NAME_FIELD, name);
            trip.setStringField(STATE_FIELD, TRIP_STATE_RUNNING);
            trip.setLongField(START_DATE_FIELD, Sys.now());

            File emptyFile;
            try {
                emptyFile = File.createTempFile("empty", ".txt");
            } catch (IOException ioe) {
                log.warn("Cannot create empty temp file", ioe);
                throw new UtopiaException("createTrip exception:" + ioe.getMessage());
            }
            trip.setFileField(EVENTS_FIELD, trip.createFileField(emptyFile));

            oase.getModifier().insert(trip);

            // relate person
            oase.getRelater().relate(trip, person, null);

            return trip;
        } catch (Throwable t) {
            log.error("Exception creating the traffic log: " + t.toString());
            throw new UtopiaException(t);
        }
    }

    public void storeTraffic(String aPersonId, JXElement aRequest, JXElement aResponse) throws UtopiaException {
        try {
            Record r = oase.getModifier().create(TRAFFIC_TABLE);
            r.setXMLField(REQUEST_FIELD, aRequest);
            r.setXMLField(RESPONSE_FIELD, aResponse);
            oase.getModifier().insert(r);
            // relate the traffic to the person            
            oase.getRelater().relate(oase.getFinder().read(Integer.parseInt(aPersonId)), r);
        } catch (Throwable t) {
            log.error("Exception storing the traffic: " + t.toString());
            throw new UtopiaException(t);
        }
    }

    public Vector getAllTraffic() throws UtopiaException {
        try {
            Record[] recs = oase.getFinder().readAll(TRAFFIC_TABLE);
            Vector result = new Vector(recs.length);
            for (int i = 0; i < recs.length; i++) {
                JXElement t = recs[i].toXML();
                t.setTag(TRAFFIC_ELM);
                result.add(t);
            }
            return result;
        } catch (Throwable t) {
            log.error("Exception reading the traffic: " + t.toString());
            throw new UtopiaException(t);
        }
    }

    public Vector getTrafficForPerson(String aPersonId) throws UtopiaException {
        try {
            Record[] recs = oase.getRelater().getRelated(oase.getFinder().read(Integer.parseInt(aPersonId)), TRAFFIC_TABLE, null);
            Vector result = new Vector(recs.length);
            for (int i = 0; i < recs.length; i++) {
                JXElement t = recs[i].toXML();
                t.setTag(TRAFFIC_ELM);
                result.add(t);
            }
            return result;
        } catch (Throwable t) {
            log.error("Exception reading the traffic: " + t.toString());
            throw new UtopiaException(t);
        }
    }

    /**
     * Properties passed on from Handler.
     */
    public static void setProperty(String propertyName, String propertyValue) {
        properties.put(propertyName, propertyValue);
    }
}
