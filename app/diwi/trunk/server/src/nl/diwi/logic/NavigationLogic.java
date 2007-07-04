package nl.diwi.logic;

import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.QueryLogic;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.Medium;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.utopia.core.data.UtopiaException;
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
        if (isUserContentEnabled(aPersonId)) {
            result.addAll(checkUGCHits(aPoint));
        }
        result.addAll(roamAlert(aPersonId, aPoint));

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
                throw new UtopiaException("No active route set!");
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
                logLogic.closeLogs("" + aPersonId, LOG_TRIP_TYPE);
            }

            //Relate route to person as active route
            oase.getRelater().relate(person, route, ACTIVE_TAG);

            // explicitely relate the route to the trip
            Record trip = logLogic.getOpenLog("" + aPersonId, LOG_TRIP_TYPE);
            oase.getRelater().relate(trip, route);

        } catch (OaseException oe) {
            throw new UtopiaException("Cannot set active route", oe);
        }
    }

    // TODO: complete/change this query
    private Vector checkPoiHits(int aPersonId, Point aPoint) throws UtopiaException {
        try {
            // first get the active route
            Record route = getActiveRoute(aPersonId);

            Vector result = new Vector();
            Record[] recs = null;

            String distanceClause = "distance(GeomFromText('POINT(" + aPoint.x + " " + aPoint.y + ")'," + EPSG_DUTCH_RD + "), " + RDPOINT_FIELD + ")";
            if(route == null) {
                // struinen!!!
                String tables = "diwi_poi";
                String fields = "id";
                String where = distanceClause + " < " + HIT_DISTANCE;
                String relations = null;
                String postCond = null;
                recs = QueryLogic.queryStore(oase, tables, fields, where, relations, postCond);
            }else{
                String tables = "diwi_poi,diwi_route";
                String fields = "diwi_poi.id";
                String where = distanceClause + " < " + HIT_DISTANCE + " AND diwi_route.id=" + route.getId();
                String relations = "diwi_route,diwi_poi";
                String postCond = null;
                recs = QueryLogic.queryStore(oase, tables, fields, where, relations, postCond);
            }

            for (int i = 0; i < recs.length; i++) {
                JXElement hit = new JXElement(POI_HIT_ELM);
                hit.setAttr(ID_FIELD, recs[i].getIntField(ID_FIELD));
                result.add(hit);
            }
            return result;
        } catch (Throwable t) {
			throw new UtopiaException(t);
		}        
    }

    private Vector roamAlert(int aPersonId, Point aPoint) throws UtopiaException {
        Vector result = new Vector();
        try {
            // first get the active route
            Record route = getActiveRoute(aPersonId);
            if(route == null) return new Vector(0);

            String queryString = "distance_sphere(GeomFromText('" + aPoint + "') , path) " +
                    "as distance from diwi_route where distance_sphere(GeomFromText('" + aPoint + "'), " +
                    "path) > " + ROAM_DISTANCE + " AND id=" + route.getId();
            Record[] recs = oase.getFinder().freeQuery(queryString);

            for (int i = 0; i < recs.length; i++) {
                JXElement msg = new JXElement(MSG_ELM);
                msg.setText("roam");
                result.add(msg);
            }
        } catch (Throwable t) {
            return new Vector(0);
            //throw new UtopiaException(t);
        }

        return result;
    }

    private Vector checkUGCHits(Point aPoint) throws UtopiaException {
        try {
            /*Record[] ugcHits = QueryLogic.queryStore(oase, UGC_TABLE, "id, distance_sphere(GeomFromEWKT('" + point + "') , point) as distance",
                    "distance_sphere(GeomFromEWKT('" + point + "') ,  point) < " + HIT_DISTANCE, Medium.TABLE_NAME, null);
*/
            Vector result = new Vector();
            String distanceClause = "distance_sphere(GeomFromText('POINT(" + aPoint.x + " " + aPoint.y + ")',4326),point)";
			String tables = UGC_TABLE;
			String fields = ID_FIELD;
			//String fields = ID_FIELD + "," + distanceClause + " as distance";
			String where = distanceClause + " < " + HIT_DISTANCE;
			String relations = null;
			String postCond = null;
			Record[] recs = QueryLogic.queryStore(oase, tables, fields, where, relations, postCond);
            for (int i = 0; i < recs.length; i++) {
                JXElement hit = new JXElement(UGC_HIT_ELM);
                hit.setAttr(ID_FIELD, recs[i].getIntField(ID_FIELD));
                //hit.setAttr(DISTANCE_ATTR, poiHits[i].getField(DISTANCE_FIELD).toString());
                result.add(hit);
            }
            return result;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

}
