package nl.diwi.logic;

import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.QueryLogic;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.data.Medium;
import org.keyworx.utopia.core.util.Oase;
import org.postgis.Point;

import java.util.Vector;

public class NavigationLogic implements Constants {

    private Oase oase;
    private Log log = Logging.getLog("NavigationLogic");

    public NavigationLogic(Oase oase) {
        this.oase = oase;
    }


    public Vector checkPoint(Point aPoint, int aPersonId) throws UtopiaException {
        Vector result = new Vector();

        result.addAll(checkPoiHits(aPersonId, aPoint));
        if(result.size() == 0){
            // no poi hit - so let's check if we're roaming
            JXElement roam = roamAlert(aPersonId, aPoint);
            if(roam != null) result.add(roam);
        }
        if (isUserContentEnabled(aPersonId)) {
            result.addAll(checkUGCHits(aPersonId, aPoint));
        }

        return result;
    }

    public void toggleUGC(String aPersonId) throws UtopiaException {
        try {
            Record person = oase.getFinder().read(Integer.parseInt(aPersonId), Person.TABLE_NAME);
            if (person == null) {
                throw new UtopiaException("No person found with id " + aPersonId);
            }

            Record[] prefs = oase.getRelater().getRelated(person, PREFS_TABLE, "ugc");
            if (prefs != null && prefs.length > 0) {
                Record pref = prefs[0];
                if (pref.getStringField(VALUE_FIELD).equals("ON")) {
                    pref.setStringField(VALUE_FIELD, "OFF");
                } else {
                    pref.setStringField(VALUE_FIELD, "ON");
                }
                oase.getModifier().update(pref);
            } else {
                Record pref = oase.getModifier().create(PREFS_TABLE);
                pref.setIntField(OWNER_FIELD, Integer.parseInt(aPersonId));
                pref.setStringField(NAME_FIELD, "UGC");
                pref.setStringField(VALUE_FIELD, "OFF");
                oase.getModifier().insert(pref);

                oase.getRelater().relate(person, pref, "ugc");
            }
        } catch (Throwable t) {
            log.error("Exception in toggleUGC: " + t.getMessage());
            throw new UtopiaException(t);
        }
    }

    public void setUGC(String aPersonId, boolean turnOn) throws UtopiaException {
        try {
            Record person = oase.getFinder().read(Integer.parseInt(aPersonId), Person.TABLE_NAME);
            if (person == null) {
                throw new UtopiaException("No person found with id " + aPersonId);
            }

            Record[] prefs = oase.getRelater().getRelated(person, PREFS_TABLE, "ugc");
            if (prefs != null && prefs.length > 0) {
                Record pref = prefs[0];
                if(turnOn){
                    pref.setStringField(VALUE_FIELD, "ON");
                }else{
                    pref.setStringField(VALUE_FIELD, "OFF");
                }
                oase.getModifier().update(pref);
            } else {
                Record pref = oase.getModifier().create(PREFS_TABLE);
                pref.setIntField(OWNER_FIELD, Integer.parseInt(aPersonId));
                pref.setStringField(NAME_FIELD, "UGC");
                if(turnOn){
                    pref.setStringField(VALUE_FIELD, "ON");
                }else{
                    pref.setStringField(VALUE_FIELD, "OFF");
                }                                
                oase.getModifier().insert(pref);

                oase.getRelater().relate(person, pref, "ugc");
            }
        } catch (Throwable t) {
            log.error("Exception in setUGC: " + t.getMessage());
            throw new UtopiaException(t);
        }
    }

    public boolean isUserContentEnabled(int aPersonId) throws UtopiaException {
        try {
            Record person = oase.getFinder().read(aPersonId, Person.TABLE_NAME);
            if (person == null) {
                throw new UtopiaException("No person found with id " + aPersonId);
            }

            Record[] prefs = oase.getRelater().getRelated(person, PREFS_TABLE, "ugc");
            if (prefs != null && prefs.length > 0) {
                return prefs[0].getStringField(VALUE_FIELD).equals("ON");
            } else {
                Record pref = oase.getModifier().create(PREFS_TABLE);
                pref.setIntField(OWNER_FIELD, aPersonId);
                pref.setStringField(NAME_FIELD, "UGC");
                pref.setStringField(VALUE_FIELD, "OFF");
                oase.getModifier().insert(pref);

                oase.getRelater().relate(person, pref, "ugc");
                return false;
            }
        } catch (Throwable t) {
            log.error("Exception in isUserContentEnabled: " + t.getMessage());
            throw new UtopiaException(t);
        }
    }

    public void deactivateRoute(int personId) throws UtopiaException {
        try {
            //Find the person
            Record person = oase.getFinder().read(personId);
            //Find the 'active' route.
            Record activeRoute = getActiveRoute(personId);

            if (activeRoute == null) {
                //throw new UtopiaException("No active route set!");
                return;
            }
            //Unrelate
            oase.getRelater().unrelate(person, activeRoute);
        } catch (OaseException oe) {
            throw new UtopiaException("Cannot set active route", oe);
        }
    }

    public Record getActiveRoute(int personId) throws UtopiaException {
        try {
            //Find the person
            Record person = oase.getFinder().read(personId);
            Record[] activeRoute = oase.getRelater().getRelated(person, null, ACTIVE_TAG);

            if (activeRoute.length == 0) {
                return null;
            } else {
                return activeRoute[0];
            }
        } catch (OaseException oe) {
            throw new UtopiaException("Cannot set active route", oe);
        }

    }

    public void activateRoute(int aRouteId, int aPersonId, boolean init) throws UtopiaException {
        try {
            //Find the person
            Record person = oase.getFinder().read(aPersonId);
            //Find the Route
            Record route = oase.getFinder().read(aRouteId);

            //If an active route is allready set, deactivate first
            if (getActiveRoute(aPersonId) != null) {
                deactivateRoute(aPersonId);
            }

            LogLogic logLogic = new LogLogic(oase);
            if (init) {
                // now create explicitely close the previous trip
                logLogic.closeLogs("" + aPersonId, LOG_MOBILE_TYPE);
            }

            //Relate route to person as active route
            oase.getRelater().relate(person, route, ACTIVE_TAG);

            // explicitely relate the route to the trip
            Record trip = logLogic.getOpenLog("" + aPersonId, LOG_MOBILE_TYPE);
            oase.getRelater().relate(trip, route);

        } catch (OaseException oe) {
            throw new UtopiaException("Cannot set active route", oe);
        }
    }

    private Vector checkPoiHits(int aPersonId, Point aPoint) throws UtopiaException {
        try {
            LogLogic logLogic = new LogLogic(oase);

            // first get the active route
            Record route = getActiveRoute(aPersonId);

            Vector result = new Vector();
            Record[] recs;

            String distanceClause = "distance(GeomFromText('POINT(" + aPoint.x + " " + aPoint.y + ")'," + EPSG_DUTCH_RD + "), " + RDPOINT_FIELD + ")";
            if (route == null) {
                // struinen!!!
                String tables = POI_TABLE;
                String fields = ID_FIELD;
                String where = distanceClause + " < " + HIT_DISTANCE;
                String relations = null;
                String postCond = null;
                recs = QueryLogic.queryStore(oase, tables, fields, where, relations, postCond);
            } else {
                String tables = POI_TABLE + "," + ROUTE_TABLE;
                String fields = POI_TABLE + "." + ID_FIELD;
                String where = distanceClause + " < " + HIT_DISTANCE + " AND " + ROUTE_TABLE + "." + ID_FIELD + "=" + route.getId();
                String relations = ROUTE_TABLE + "," + POI_TABLE;
                String postCond = null;
                recs = QueryLogic.queryStore(oase, tables, fields, where, relations, postCond);
            }

            for (int i = 0; i < recs.length; i++) {
                JXElement hit = new JXElement(POI_HIT_ELM);
                int id = recs[i].getIntField(ID_FIELD);
                hit.setAttr(ID_FIELD, id);
                result.add(hit);

                // relate poi to trip
                //logLogic.relatePoiToTrip(aPersonId, id, POI_TRIP_STATE_HIT);

            }
            return result;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    private JXElement roamAlert(int aPersonId, Point aPoint) throws UtopiaException {
        try {
            // first get the active route
            Record route = getActiveRoute(aPersonId);
            if (route == null) return null;

            String query = "SELECT * from " + ROUTE_TABLE + " where " + ID_FIELD + "=" + route.getId();
            query += " AND distance(GeomFromText('POINT(" + aPoint.x + " " + aPoint.y + ")'," + EPSG_DUTCH_RD + ")," + RDPATH_FIELD + ")  < " + ROAM_DISTANCE;
            Record[] records = oase.getFinder().freeQuery(query);

            if (records.length == 0) {
                JXElement msg = new JXElement(MSG_ELM);
                msg.setText("roam");
                return msg;
            }
            return null;

        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    private Vector checkUGCHits(int aPersonId, Point aPoint) throws UtopiaException {
        try {
            Vector result = new Vector();

            String distanceClause = "distance(GeomFromText('POINT(" + aPoint.x + " " + aPoint.y + ")'," + EPSG_DUTCH_RD + "), " + RDPOINT_FIELD + ")";
            String tables = UGC_TABLE;
            String fields = ID_FIELD;
            String where = TYPE_FIELD + "=1 AND " + distanceClause + " < " + HIT_DISTANCE;
            String relations = null;
            String postCond = null;
            Record[] recs = QueryLogic.queryStore(oase, tables, fields, where, relations, postCond);

            for (int i = 0; i < recs.length; i++) {
                // make sure we don't find our own media!!!
                Record rec = recs[i];
                Record[] media = oase.getRelater().getRelated(oase.getFinder().read(rec.getId()), Medium.TABLE_NAME, "medium");
                for(int j=0;j<media.length;j++){
                    Record medium = media[j];
                    Record[] people = oase.getRelater().getRelated(medium, Person.TABLE_NAME, null);
                    for(int k=0;k<people.length;k++){
                        if(people[k].getId() != aPersonId){
                            JXElement hit = new JXElement(UGC_HIT_ELM);
                            hit.setAttr(ID_FIELD, medium.getIntField(ID_FIELD));
                            hit.setAttr(Medium.FILENAME_FIELD, medium.getStringField(Medium.FILENAME_FIELD));
                            hit.setAttr(Medium.KIND_FIELD, medium.getStringField(Medium.KIND_FIELD));
                            result.add(hit);
                        }
                    }
                }
            }
            return result;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

}
