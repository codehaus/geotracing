package nl.diwi.logic;

import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import nl.justobjects.jox.parser.JXBuilderListener;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Sys;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.api.FileField;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.store.record.FileFieldImpl;

import java.util.Properties;
import java.util.Vector;
import java.util.Iterator;
import java.util.Date;
import java.util.logging.Formatter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.Format;

public class TripLogic implements Constants {
    private Oase oase;
    private Log log = Logging.getLog("TripLogic");
    private static final Properties properties = new Properties();

    public TripLogic(Oase o) {
        oase = o;
    }

    public Record createTrip(String aPersonId) throws UtopiaException {
        try {
            Format formatter = new SimpleDateFormat("EEEE, dd MMM yyyy HH:mm:ss");

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
            oase.getRelater().relate(trip, oase.getFinder().read(Integer.parseInt(aPersonId)));
            return trip;
        } catch (Throwable t) {
            log.error("Exception creating the trip: " + t.toString());
            throw new UtopiaException(t);
        }
    }

    // Store event into trip table.
	protected void storeEvent(String aPersonId, JXElement anEvent) throws OaseException, UtopiaException {
        try{
            Record person = oase.getFinder().read(Integer.parseInt(aPersonId));
            if(person == null){
                throw new UtopiaException("No person found with id " + aPersonId);
            }

            Record[] trips = oase.getRelater().getRelated(person, TRIP_TABLE, null);
            Record trip = null;
            if(trips.length == 0){
                // create a trip if we don't already have one
                trip = createTrip(aPersonId);
            }else{
                trip = trips[0];
            }

            FileField fileField = trip.getFileField(EVENTS_FIELD);
            String eventStr = new String(anEvent.toBytes(false)) + "\n";
            fileField.append(eventStr.getBytes());
            oase.getModifier().update(trip);
        }catch(Throwable t){
            log.error("Exception storing the event: " + t.toString());
            throw new UtopiaException(t);
        }
    }

    public Vector getTrips(String aPersonId) throws UtopiaException {
        try{
            Record[] recs = oase.getRelater().getRelated(oase.getFinder().read(Integer.parseInt(aPersonId)), TRIP_TABLE, null);
            Vector trips = new Vector(recs.length);
            for(int i=0;i<recs.length;i++){
                trips.add(recs[i].toXML());
            }
            return trips;
        }catch(Throwable t){
            log.error("Exception retrieving trips: " + t.toString());
            throw new UtopiaException(t);
        }
    }

    public JXElement getTrip(String aTripId) throws UtopiaException {
		final JXElement tripElm = new JXElement(TRIP_ELM);
		try {
			Record trip = oase.getFinder().read(Integer.parseInt(aTripId), TRIP_TABLE);
			JXBuilder builder = new JXBuilder(
					new JXBuilderListener() {
						/**
						 * Called by XmlElementParser when it parsed and created an JXElement.
						 */
						public void element(JXElement e) {
							tripElm.addChild(e);
						}

						/**
						 * Called when parser encounters an error.
						 */
						public void error(String msg) {
							Logging.getLog().warn("getTrip() error: " + msg);
						}

						/**
						 * End of input stream is reached.
						 * <p/>
						 * This may occur when listening for multiple documents on a stream.
						 *
						 * @param message	 text message
						 * @param anException optional exception that caused the stream end
						 */
						public void endInputStream(String message, Throwable anException) {
							Logging.getLog().trace("getTrip() EOF reached: " + message + " e=" + anException);
						}

					}
			);

			// Event data file
			FileFieldImpl fileField = (FileFieldImpl) trip.getFileField(EVENTS_FIELD);
			File file = fileField.getStoredFile();

			// Only makes sense to parse a file with content
			if (file != null && file.exists() && file.length() > 0) {
				builder.setMultiDoc(true);
				builder.build(fileField.getFileInputStream());
			}
		} catch (Throwable t) {
			new UtopiaException("Error query getTripEvents tripId=" + aTripId, t);
		}

		return tripElm;
	}

    /**
     * Properties passed on from Handler.
     */
    public static void setProperty(String propertyName, String propertyValue) {
        properties.put(propertyName, propertyValue);
    }
}
