package nl.diwi.logic;

import nl.diwi.util.Constants;
import nl.diwi.control.NavigationHandler;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import nl.justobjects.jox.parser.JXBuilderListener;
import org.geotracing.handler.QueryLogic;
import org.geotracing.handler.TrackLogic;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Sys;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.store.record.FileFieldImpl;
import org.keyworx.server.ServerConfig;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.data.Medium;
import org.keyworx.utopia.core.util.Oase;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class LogLogic implements Constants {
    private Oase oase;
    private Log log = Logging.getLog("LogLogic");
    private static final Properties properties = new Properties();
    Format formatter = new SimpleDateFormat("EEEE, dd MMM yyyy HH:mm:ss");

    public LogLogic(Oase o) {
        oase = o;
    }

    /**
     * Creates a new log
     *
     * @param aPersonId log for this person
     * @param aType 'mobile' or 'web' is used
     * @return log record
     * @throws UtopiaException standard exception
     */
    private Record createLog(String aPersonId, String aType) throws UtopiaException {
        try {
            // first check if there's already an open log, if so return this one.
            Record person = oase.getFinder().read(Integer.parseInt(aPersonId));
            if (person == null) {
                throw new UtopiaException("No person found with id " + aPersonId);
            }

            // close all other logs before opening a new one
            closeLogs(aPersonId, aType);

            String name = person.getStringField(Person.FIRSTNAME_FIELD) + " "
                    + person.getStringField(Person.LASTNAME_FIELD)
                    + "'s " + aType + " on " + formatter.format(new Date());

            Record record = oase.getModifier().create(LOG_TABLE);
            record.setStringField(NAME_FIELD, name);
            record.setStringField(STATE_FIELD, LOG_STATE_OPEN);
            record.setStringField(TYPE_FIELD, aType);
            record.setLongField(START_DATE_FIELD, Sys.now());

            File emptyFile;
            try {
                emptyFile = File.createTempFile("empty", ".txt");
            } catch (IOException ioe) {
                log.warn("Cannot create empty temp file", ioe);
                throw new UtopiaException("createLog exception:" + ioe.getMessage());
            }
            record.setFileField(EVENTS_FIELD, record.createFileField(emptyFile));

            oase.getModifier().insert(record);

            // relate person
            oase.getRelater().relate(record, person, aType);

            return record;
        } catch (Throwable t) {
            log.error("Exception creating the log: " + t.toString());
            throw new UtopiaException(t);
        }
    }

    /**
     * Closes the logs after a certain period set in server.properties
     *
     * @param aPersonId log for this person
     * @param aType     'mobile' or 'web' is used
     * @return log record
     * @throws UtopiaException standard exception
     */
    public void closeLogByTime(String aPersonId, String aType) throws UtopiaException {
        try {
            long timeout = Long.parseLong(ServerConfig.getProperty("keyworx.log.timeout"));
            long now = System.currentTimeMillis();

            Record[] records = queryOpenLogs(aPersonId, aType);

            for (int i = 0; i < records.length; i++) {
                Record record = oase.getFinder().read(records[i].getId(), LOG_TABLE);

                long startDate = record.getLongField(START_DATE_FIELD);
                // we time out after 12 hrs
                if (now - startDate > timeout) {
                    record.setStringField(STATE_FIELD, LOG_STATE_CLOSED);
                    record.setLongField(END_DATE_FIELD, now);
                    oase.getModifier().update(record);
                }
            }
        } catch (Throwable t) {
            log.error("Exception closing the log: " + t.toString());
            throw new UtopiaException(t);
        }
    }

    /**
     * Explicitely closes all open logs
     *
     * @param aPersonId log for this person
     * @param aType     'mobile' or 'web' is used
     * @return log record
     * @throws UtopiaException standard exception
     */
    public void closeLogs(String aPersonId, String aType) throws UtopiaException {
        try {
            Record[] records = queryOpenLogs(aPersonId, aType);

            for (int i = 0; i < records.length; i++) {
                Record record = oase.getFinder().read(records[i].getId(), LOG_TABLE);
                record.setStringField(STATE_FIELD, LOG_STATE_CLOSED);
                record.setLongField(END_DATE_FIELD, System.currentTimeMillis());
                oase.getModifier().update(record);

            }
        } catch (Throwable t) {
            log.error("Exception closing the log: " + t.toString());
            throw new UtopiaException(t);
        }
    }

    /**
     * Stores an event into the open log
     *
     * @param aPersonId log for this person
     * @param aType     'mobile' or 'web' is used
     * @param anEvent   the event to store
     * @throws UtopiaException standard exception
     */
    public void storeLogEvent(String aPersonId, JXElement anEvent, String aType) throws UtopiaException {
        try {
            log.info("Storing event " + anEvent);
            Record person = oase.getFinder().read(Integer.parseInt(aPersonId));
            if (person == null) {
                throw new UtopiaException("No person found with id " + aPersonId);
            }

            Record record = getOpenLog(aPersonId, aType);

            // explicitely put a timestamp in
            anEvent.setAttr("time", System.currentTimeMillis());
            anEvent.setAttr("date", formatter.format(new Date()));

            String eventStr = new String(anEvent.toBytes(false)) + "\n";
            record.getFileField(EVENTS_FIELD).append(eventStr.getBytes());
            oase.getModifier().update(record);
        } catch (Throwable t) {
            log.error("Exception storing the event: " + t.toString());
            throw new UtopiaException(t);
        }
    }

    /**
     * Gets the open log to write the events to.
     *
     * @param aPersonId log for this person
     * @param aType     'mobile' or 'web' is used
     * @return log record
     * @throws UtopiaException standard exception
     */
    public Record getOpenLog(String aPersonId, String aType) throws UtopiaException {
        try {
            Record[] recs = queryOpenLogs(aPersonId, aType);
            if (recs == null || recs.length == 0) {
                return createLog(aPersonId, aType);
            }
            return oase.getFinder().read(recs[0].getId(), LOG_TABLE);
        } catch (Throwable t) {
            log.error("Exception in getOpenLog: " + t.toString());
            throw new UtopiaException(t);

        }
    }

    /**
     * retrieves all open logs.
     *
     * @param aPersonId
     * @param aType
     * @return
     * @throws UtopiaException
     */
    private Record[] queryOpenLogs(String aPersonId, String aType) throws UtopiaException {
        try {
            String tables = LOG_TABLE + "," + Person.TABLE_NAME;
            String fields = LOG_TABLE + "." + ID_FIELD;
            String where = LOG_TABLE + "." + STATE_FIELD + " = '" + LOG_STATE_OPEN + "' " +
                    "AND " + LOG_TABLE + "." + TYPE_FIELD + "= '" + aType + "'  " +
                    "AND " + Person.TABLE_NAME + "." + ID_FIELD + " = " + aPersonId;
            String relations = LOG_TABLE + "," + Person.TABLE_NAME;
            String postCond = null;
            return QueryLogic.queryStore(oase, tables, fields, where, relations, postCond);
        } catch (Throwable t) {
            log.error("Exception in queryOpenLogs: " + t.toString());
            throw new UtopiaException(t);

        }
    }

    private Record[] queryLogs(String aPersonId, String aType) throws UtopiaException {
        try {
            Record person = oase.getFinder().read(Integer.parseInt(aPersonId));
            if (person == null) {
                throw new UtopiaException("No person found with id " + aPersonId);
            }

            return oase.getRelater().getRelated(person, LOG_TABLE, aType);
        } catch (Throwable t) {
            log.error("Exception getting the logs: " + t.toString());
            throw new UtopiaException(t);
        }
    }

    public Vector getLogs(String aPersonId, String aType) throws UtopiaException {
        Record[] recs = queryLogs(aPersonId, aType);
        Vector v = new Vector(recs.length);
        for (int i = 0; i < recs.length; i++) {
            JXElement elm = recs[i].toXML();
            elm.setTag(aType);
            elm.removeAttr("table");
            v.add(elm);
        }
        return v;
    }

    /*public void relatePoiToTrip(int aPersonId, int aPoiId, String aState) throws UtopiaException{
        try{
            Record trip = getOpenLog("" + aPersonId, LOG_MOBILE_TYPE);
            Record poi = oase.getFinder().read(aPoiId);

            // check if it's already related
            if(oase.getRelater().isRelated(trip, poi)){
                String tag = oase.getRelater().getTag(trip, poi);
                if(tag!=null && !tag.equals(aState)){
                    oase.getRelater().setTag(trip, poi, aState);
                }                
            }else{
                oase.getRelater().relate(trip, oase.getFinder().read(aPoiId), aState);    
            }
        }catch(Throwable t){
            throw new UtopiaException(t);
        }
    }*/

    public JXElement getLog(String aLogId) throws UtopiaException {
        final JXElement logElm;
        Record record;
        String type;

        try {
            record = oase.getFinder().read(Integer.parseInt(aLogId), LOG_TABLE);
            type = record.getStringField(TYPE_FIELD);
        } catch (Throwable t) {
            throw new UtopiaException("No log found for id:" + aLogId);
        }

        if (type.equals(LOG_MOBILE_TYPE)) {
            logElm = new JXElement(MOBILE_ELM);
        } else {
            logElm = new JXElement(WEB_ELM);
        }

        try {

            JXBuilder builder = new JXBuilder(
                    new JXBuilderListener() {
                        /**
                         * Called by XmlElementParser when it parsed and created an JXElement.
                         */
                        public void element(JXElement e) {
                            logElm.addChild(e);
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
            FileFieldImpl fileField = (FileFieldImpl) record.getFileField(EVENTS_FIELD);
            File file = fileField.getStoredFile();

            // Only makes sense to parse a file with content
            if (file != null && file.exists() && file.length() > 0) {
                builder.setMultiDoc(true);
                builder.build(fileField.getFileInputStream());
            }

            if (type.equals(LOG_MOBILE_TYPE)) {
                JXElement tripElm = new JXElement(LOG_MOBILE_TYPE);

                // get the related track
                Record trackRec = oase.getRelater().getRelated(record, "g_track", null)[0];

                // now also add the track to the log output
                TrackLogic trackLogic = new TrackLogic(oase);
                JXElement trackElm = trackLogic.export("" + trackRec.getId(), "gtx", "x,y,t", true, 20, 1000);
                log.info(new String(trackElm.toBytes(false)));

                // add hit poi's, ugc and routes
                // first the contents of the gtx
                tripElm.addChildren(trackElm.getChildren());

                // now add poi en ugc hits
                JXElement hits = new JXElement("hits");
                Vector poiHitElms = logElm.getChildrenByTag(POI_HIT_ELM);
                for(int i=0;i<poiHitElms.size();i++){
                    JXElement poiHitElm = (JXElement)poiHitElms.elementAt(i);
                    String id = poiHitElm.getAttr(ID_FIELD);
                    String query = "SELECT * from " + POI_TABLE + " WHERE " + ID_FIELD + "=" + id;
                    Record[] records = oase.getFinder().freeQuery(query);
                    JXElement e = records[0].toXML();
                    e.setAttr("time", poiHitElm.getAttr("time"));
                    e.setAttr("date", poiHitElm.getAttr("date"));
                    e.addChild(e);
                }
                Vector ugcHitElms = logElm.getChildrenByTag(UGC_HIT_ELM);
                for(int i=0;i<ugcHitElms.size();i++){
                    JXElement ugcHitElm = (JXElement)ugcHitElms.elementAt(i);
                    String id = ugcHitElm.getAttr(ID_FIELD);
                    String tables = UGC_TABLE + "," + Medium.TABLE_NAME;
                    String fields = Medium.TABLE_NAME + "." + ID_FIELD + "," + Medium.TABLE_NAME + "." + NAME_FIELD  + "," + Medium.TABLE_NAME + "." + DESCRIPTION_FIELD;
                    String where = UGC_TABLE + "." + ID_FIELD + "=" + id;
                    String relations = UGC_TABLE + "," + Medium.TABLE_NAME + ",medium";
                    String postCond = null;
                    Record[] records = QueryLogic.queryStore(oase, tables, fields, where, relations, postCond);
                    JXElement e = records[0].toXML();
                    e.setAttr("time", ugcHitElm.getAttr("time"));
                    e.setAttr("date", ugcHitElm.getAttr("date"));
                    e.addChild(e);
                }
                tripElm.addChild(hits);

                // now add routes
                // <nav-activate-route-req id="685"/>
                JXElement routes = new JXElement("routes");
                Vector routeMsgs = logElm.getChildrenByTag(NavigationHandler.NAV_ACTIVATE_ROUTE_SERVICE + "-req");
                for(int i=0;i<routeMsgs.size();i++){
                    JXElement routeMsg = (JXElement)routeMsgs.elementAt(i);
                    String id = routeMsg.getAttr(ID_FIELD);
                    String query = "SELECT id,name,description from " + ROUTE_TABLE + " WHERE " + ID_FIELD + "=" + id;
                    Record[] records = oase.getFinder().freeQuery(query);
                    JXElement e = records[0].toXML();
                    e.setAttr("time", routeMsg.getAttr("time"));
                    e.setAttr("date", routeMsg.getAttr("date"));
                    routes.addChild(e);
                }
                tripElm.addChild(routes);

                return tripElm;
            }
        } catch (Throwable t) {
            new UtopiaException("Error query getLogEvents logId=" + aLogId, t);
        }

        return logElm;
    }

    /**
     * Properties passed on from Handler.
     */
    public static void setProperty(String propertyName, String propertyValue) {
        properties.put(propertyName, propertyValue);
    }
}
