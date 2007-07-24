// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package nl.diwi.logic;

import nl.diwi.external.RouteGenerator;
import nl.diwi.util.Constants;
import nl.diwi.util.GPXUtil;
import nl.diwi.util.ProjectionConversionUtil;
import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.QueryLogic;
import org.geotracing.handler.TrackLogic;
import org.geotracing.handler.Track;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.utopia.core.util.XML;
import org.postgis.LineString;
import org.postgis.PGbox2d;
import org.postgis.PGgeometryLW;
import org.postgis.Point;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

/**
 * Handles all logic related to commenting.
 * <p/>
 * Uses Oase directly for DB updates.
 *
 * @author Just van den Broecke
 * @version $Id: CommentLogic.java 353 2007-02-02 12:04:11Z just $
 */
public class RouteLogic implements Constants {
	private static final Properties properties = new Properties();
	public static final String PROP_MAX_CONTENT_CHARS = "max-content-chars";

	private Oase oase;
	private Log log = Logging.getLog("RouteLogic");

	public RouteLogic(Oase o) {
		oase = o;
	}

	/*
     <route-generate-req >
          <pref name="bos" value="40" type="outdoor-params" />
          <pref name="heide" value="20" type="outdoor-params" />
          <pref name="bebouwing" value="10" type="outdoor-params" />
          <pref name="thema" value="forts" type="theme" />
          <pref name="activiteit" value="wandelaar" type="activity" />
          <pref name="startpunt" value="nijevelt" type="route" />
          <pref name="eindpunt" value="groningen" type="route" />
          <pref name="lengte" value="130" type="route" />
     </route-generate-req>

     return vector of Route record converted to JXElement.
    */
	public JXElement generateRoute(JXElement reqElm, int aPersonId, String aType) throws UtopiaException {
		try {
			Record person = oase.getFinder().read(aPersonId);

			// first delete existing prefs
			Record[] prefs = oase.getRelater().getRelated(person, PREFS_TABLE, aType);
			for (int i = 0; i < prefs.length; i++) {
				oase.getModifier().delete(prefs[i]);
			}

			if (aType.equals(GENERATE_HOME_ROUTE)) {
				// now add the start and end point coordinates
				TrackLogic trackLogic = new TrackLogic(oase);
				Track track = trackLogic.getActiveTrack(aPersonId);
				if (track == null) return new JXElement("route");

				Record[] firstRecs = track.getRelatedRecords("g_location", "firstpt");
				Point rdPointStart = (Point) ((PGgeometryLW) firstRecs[0].getObjectField(RDPOINT_FIELD)).getGeometry();

				JXElement pref1 = new JXElement(PREF_ELM);
				pref1.setAttr(NAME_FIELD, "startx");
				pref1.setAttr(VALUE_FIELD, Math.round(rdPointStart.x));
				reqElm.addChild(pref1);

				JXElement pref2 = new JXElement(PREF_ELM);
				pref2.setAttr(NAME_FIELD, "starty");
				pref2.setAttr(VALUE_FIELD, Math.round(rdPointStart.y));
				reqElm.addChild(pref2);

				Record[] lastRecs = oase.getRelater().getRelated(person, "g_location", "lastloc");
				Point rdPointEnd = (Point) ((PGgeometryLW) lastRecs[0].getObjectField(RDPOINT_FIELD)).getGeometry();

				JXElement pref3 = new JXElement(PREF_ELM);
				pref3.setAttr(NAME_FIELD, "endx");
				pref3.setAttr(VALUE_FIELD, Math.round(rdPointEnd.x));
				reqElm.addChild(pref3);

				JXElement pref4 = new JXElement(PREF_ELM);
				pref4.setAttr(NAME_FIELD, "endy");
				pref4.setAttr(VALUE_FIELD, Math.round(rdPointEnd.y));
				reqElm.addChild(pref4);

                //determine what the user is doing: as all predefined routes are walking routes
                String type = "cycling";
                // first get the trip
                LogLogic logLogic = new LogLogic(oase);
                Record trip = logLogic.getOpenLog("" + aPersonId, LOG_MOBILE_TYPE);
                if(trip!=null){
                    Record[] routes = oase.getRelater().getRelated(trip, ROUTE_TABLE, null);
                    // now check if we have related routes of type FIXED
                    for(int i=0;i<routes.length;i++){
                        if(routes[i].getIntField(STATE_FIELD) == ROUTE_TYPE_FIXED){
                            type = "walking";
                        }
                    }
                }

                JXElement pref5 = new JXElement(PREF_ELM);
				pref5.setAttr(NAME_FIELD, "type");
				pref5.setAttr(VALUE_FIELD, type);
				reqElm.addChild(pref5);
            }

			// now store the prefs
			String prefString = "";
			Vector prefElms = reqElm.getChildrenByTag(PREF_ELM);
			prefs = new Record[prefElms.size()];
			for (int i = 0; i < prefElms.size(); i++) {
				JXElement prefElm = (JXElement) prefElms.elementAt(i);

				// create the pref
				Record pref = oase.getModifier().create(PREFS_TABLE);
				pref.setIntField(OWNER_FIELD, aPersonId);
				pref.setStringField(NAME_FIELD, prefElm.getAttr(NAME_FIELD));
				pref.setStringField(VALUE_FIELD, prefElm.getAttr(VALUE_FIELD));
				pref.setIntField(TYPE_FIELD, prefElm.getIntAttr(TYPE_FIELD));

				if (i == 0) {
					prefString += prefElm.getAttr(NAME_FIELD) + "=" + prefElm.getAttr(VALUE_FIELD);
				} else {
					prefString += ", " + prefElm.getAttr(NAME_FIELD) + "=" + prefElm.getAttr(VALUE_FIELD);
				}

				prefs[i] = pref;
				oase.getModifier().insert(pref);

				// relate pref to person
				oase.getRelater().relate(person, pref, aType);
			}

			JXElement generated = RouteGenerator.generateRoute(prefs);
			Record route = null;

			if (generated != null && generated.hasChildren() && generated.getChildByTag("rte") != null && generated.getChildByTag("rte").hasChildren())
			{
				//make sure all other generated routes are set to 'inactive'
				String tables = ROUTE_TABLE + "," + Person.TABLE_NAME;
				String fields = ROUTE_TABLE + "." + ID_FIELD;
				String where = ROUTE_TABLE + "." + TYPE_FIELD + "=" + ROUTE_TYPE_GENERATED + " AND " + Person.TABLE_NAME + "." + ID_FIELD + "=" + aPersonId;
				String relations = ROUTE_TABLE + "," + Person.TABLE_NAME;
				String postCond = null;
				Record[] gens = QueryLogic.queryStore(oase, tables, fields, where, relations, postCond);
				for (int i = 0; i < gens.length; i++) {
					Record gen = oase.getFinder().read(gens[i].getIntField(ID_FIELD));
					gen.setIntField(STATE_FIELD, INACTIVE_STATE);
					oase.getModifier().update(gen);
				}
				log.info("now inserting route.");
				//Process the GPX into datastructures

                route = oase.getModifier().create(ROUTE_TABLE);
                Format formatter = new SimpleDateFormat("EEEE, dd MMM yyyy HH:mm:ss");

                String description = person.getStringField(Person.FIRSTNAME_FIELD) + " "
                        + person.getStringField(Person.LASTNAME_FIELD)
                        + "'s " + aType + " generated at " + formatter.format(new Date())
                        + " -  " + prefString;

                String gpxName = generated.getChildText(NAME_ELM);
                if (gpxName != null && gpxName.length() > 0) {
                    description = gpxName + description;
                }

                if (aType.equals(GENERATE_PREFS_ROUTE)) {
                    route.setStringField(NAME_FIELD, "persoonlijke route");
                    route.setIntField(TYPE_FIELD, ROUTE_TYPE_GENERATED);
                } else {
                    route.setStringField(NAME_FIELD, "route terug");
                    route.setIntField(TYPE_FIELD, ROUTE_TYPE_DIRECT);
                }
                route.setStringField(DESCRIPTION_FIELD, description);
                route.setIntField(STATE_FIELD, ACTIVE_STATE);

                // we receive the gpx file in RD
                LineString rdLineString = GPXUtil.GPXRoute2LineString(generated);
                PGgeometryLW rdGeom = new PGgeometryLW(rdLineString);
                route.setObjectField(RDPATH_FIELD, rdGeom);

                // but also store it in WGS84
                LineString wgsLineString = GPXUtil.GPXRoute2LineString(generated);
                wgsLineString = ProjectionConversionUtil.RD2WGS84(wgsLineString);
                PGgeometryLW wgsGeom = new PGgeometryLW(wgsLineString);
                route.setObjectField(WGSPATH_FIELD, wgsGeom);

                oase.getModifier().insert(route);

                // now relate the route to the person
                oase.getRelater().relate(person, route);

                // now update the distance
                route.setIntField(DISTANCE_FIELD, getDistance(route.getId()));
                oase.getModifier().update(route);

                // now relate all poi's to the route
                relatePois(route, generated);                
			}

			if (route == null) {
				return new JXElement(ROUTE_ELM);
			} else {
				//Convert Route record to XML and add to result
				return route.toXML();
			}
		} catch (Throwable t) {
			log.error("Exception in generateRoute: " + t.toString());
			throw new UtopiaException("Error in generateRoute", t, ErrorCode.__6006_database_irregularity_error);
		}
	}

	public int insertRoute(JXElement aRouteElement, int aRouteType) throws UtopiaException {
		Record route;
		try {
			boolean insert = true;
			// fixed routes have unique names so check first
			String name = aRouteElement.getChildText(DESCRIPTION_FIELD);
			Record[] recs = oase.getFinder().queryTable(ROUTE_TABLE, NAME_FIELD + "='" + name + "'", null, null);

			// get or create the record
			if (recs.length > 0) {
				// do an update
				route = oase.getFinder().read(recs[0].getId());
				insert = false;
			} else {
				route = oase.getModifier().create(ROUTE_TABLE);
			}

			// set the fields
			route.setStringField(NAME_FIELD, aRouteElement.getChildText(DESCRIPTION_FIELD));
			route.setStringField(DESCRIPTION_FIELD, aRouteElement.getChildText(DESCRIPTION_FIELD));
			route.setIntField(TYPE_FIELD, aRouteType);

			LineString rdLineString = GPXUtil.GPXRoute2LineString(aRouteElement.getChildByTag("gpx"));
			PGgeometryLW rdGeom = new PGgeometryLW(rdLineString);
			route.setObjectField(RDPATH_FIELD, rdGeom);

			LineString wgsLineString = GPXUtil.GPXRoute2LineString(aRouteElement.getChildByTag("gpx"));
			wgsLineString = ProjectionConversionUtil.RD2WGS84(wgsLineString);
			PGgeometryLW wgsGeom = new PGgeometryLW(wgsLineString);
			route.setObjectField(WGSPATH_FIELD, wgsGeom);

			// do the actual insert or update
			if (insert) {
				oase.getModifier().insert(route);
			} else {
				oase.getModifier().update(route);
			}

			// now update the distance
			route.setIntField(DISTANCE_FIELD, getDistance(route.getId()));
			oase.getModifier().update(route);

			// now relate all poi's to the route
            relatePois(route, aRouteElement.getChildByTag("gpx"));
            
		} catch (Throwable t) {
			log.error("Exception in insertRoute: " + t.toString());
			throw new UtopiaException("Error in insertRoute", t, ErrorCode.__6006_database_irregularity_error);
		}
		return route.getId();
	}

    private void relatePois(Record aRoute, JXElement aGPX)throws UtopiaException{
        try{
            Vector wpts = aGPX.getChildrenByTag("wpt");
			for (int i = 0; i < wpts.size(); i++) {
				JXElement wpt = (JXElement) wpts.elementAt(i);
				String kichId = wpt.getChildText(NAME_FIELD);
				Record[] pois = oase.getFinder().queryTable(POI_TABLE, KICHID_FIELD + "='" + kichId.trim() + "'", null, null);
				if (pois.length == 1) {
					// create relation
					oase.getRelater().relate(aRoute, pois[0]);
				}
			}
        } catch (Throwable t) {
			log.error("Exception in insertRoute: " + t.toString());
			throw new UtopiaException("Error in insertRoute", t, ErrorCode.__6006_database_irregularity_error);
		}
    }

    public JXElement getRoute(int aRouteId) throws UtopiaException {
		try {
            String tables = ROUTE_TABLE;
            String fields = ID_FIELD + "," + NAME_FIELD + "," + DESCRIPTION_FIELD + "," + DISTANCE_FIELD;
            String where = ID_FIELD + "=" + aRouteId;
            Record[] routes = QueryLogic.queryStore(oase, tables, fields, where, null, null);
            if(routes.length == 0) throw new UtopiaException("No route found with id:" + aRouteId);

            return routes[0].toXML();
		} catch (Throwable t) {
			log.error("Exception in getRoute: " + t.toString());
			throw new UtopiaException("Error in getRoute", t, ErrorCode.__6006_database_irregularity_error);
		}
	}

    /**
     * Retrieves routes by type: either fixed, direct or generated
     * @param aRouteType the route type
     * @param aPersonId the person id
     * @return a vector with route elements
     * @throws UtopiaException the standard exception
     */
    public Vector getRoutes(String aRouteType, String aPersonId) throws UtopiaException {
		Vector results;
		try {
			int type = -1;
			if (aRouteType.equals("fixed")) {
				type = ROUTE_TYPE_FIXED;
			} else if (aRouteType.equals("direct")) {
				type = ROUTE_TYPE_DIRECT;
			} else if (aRouteType.equals("generated")) {
				type = ROUTE_TYPE_GENERATED;
			}
			Record[] routes;
			if (type == ROUTE_TYPE_GENERATED) {
				String tables = ROUTE_TABLE + "," + Person.TABLE_NAME;
				String fields = ROUTE_TABLE + "." + ID_FIELD + "," + ROUTE_TABLE + "." + NAME_FIELD + "," +
                        ROUTE_TABLE + "." + DESCRIPTION_FIELD + "," + ROUTE_TABLE + "." + DISTANCE_FIELD;
				String where = ROUTE_TABLE + "." + TYPE_FIELD + "=" + type + " AND " + ROUTE_TABLE + "." +
                        STATE_FIELD + "=" + ACTIVE_STATE + " AND " + Person.TABLE_NAME + "." + ID_FIELD + "=" + aPersonId;
				String relations = "diwi_route,utopia_person";
				routes = QueryLogic.queryStore(oase, tables, fields, where, relations, null);
			} else {
				routes = oase.getFinder().queryTable(ROUTE_TABLE, TYPE_FIELD + "=" + type, null, null);
			}

			results = new Vector(routes.length);
			for (int i = 0; i < routes.length; i++) {
				results.add(routes[i].toXML());
			}
		} catch (Throwable t) {
			log.error("Exception in getRoutes: " + t.toString());
			throw new UtopiaException("Error in getRoutes", t, ErrorCode.__6006_database_irregularity_error);
		}
		return results;
	}

	/**
	 * Delete a route.
	 *
	 * @param aRouteId a comment id
	 * @throws UtopiaException Standard exception
	 */
	public void deleteRoute(int aRouteId) throws UtopiaException {
		try {
			oase.getModifier().delete(aRouteId);
		} catch (Throwable t) {
			log.error("Exception in deleteRoute: " + t.toString());
			throw new UtopiaException("Cannot delete route record with id=" + aRouteId, t, ErrorCode.__6006_database_irregularity_error);
		}
	}

    /**
     * Calculate the distance of a route
     * @param aRouteId the route id
     * @return the sitance
     * @throws UtopiaException the standard exception
     */
    private int getDistance(int aRouteId) throws UtopiaException {
        try{
            Record distance = oase.getFinder().freeQuery(
                    "select length2d_spheroid(wgspath , 'SPHEROID[\"WGS_1984\", 6378137, 298.257223563]' ) as distance from diwi_route where id ="
                            + aRouteId)[0];

            return (int) Float.parseFloat(distance.getField(DISTANCE_FIELD).toString());
        }catch(Throwable t){
            throw new UtopiaException(t.toString());
        }
    }

    /**
     * Gets the bounding box for a route
     * @param routeId the route
     * @return
     * @throws UtopiaException
     */
    public PGbox2d getBBox(int routeId) throws UtopiaException {
		Record bounds;
		try {
			bounds = oase.getFinder().freeQuery("select extent(rdpath) AS bbox from diwi_route where id=" + routeId)[0];
		} catch (OaseException e) {
			log.error("Exception in deleteRoute: " + e.toString());
			throw new UtopiaException("Exception in getMapUrl", e, ErrorCode.__6006_database_irregularity_error);
		}

		return (PGbox2d) bounds.getObjectField("bbox");
	}
}

