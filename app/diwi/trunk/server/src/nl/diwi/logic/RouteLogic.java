// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package nl.diwi.logic;

import nl.diwi.external.RouteGenerator;
import nl.diwi.util.Constants;
import nl.diwi.util.GPXUtil;
import nl.diwi.util.ProjectionConversionUtil;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.api.Relater;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.utopia.core.util.XML;
import org.postgis.LineString;
import org.postgis.PGbox2d;
import org.postgis.PGgeometryLW;
import org.geotracing.handler.QueryLogic;

import java.util.*;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;

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
    public JXElement generateRoute(JXElement reqElm, int personId) throws UtopiaException {
        try {
            Record person = oase.getFinder().read(personId);

            // first delete existing prefs
            Record[] prefs = oase.getRelater().getRelated(person, PREFS_TABLE, null);
            for (int i = 0; i < prefs.length; i++) {
                oase.getModifier().delete(prefs[i]);
            }

            // now store the prefs
            String prefString = "";
            Vector prefElms = reqElm.getChildrenByTag(PREF_ELM);
            prefs = new Record[prefElms.size()];
            for (int i = 0; i < prefElms.size(); i++) {
                JXElement prefElm = (JXElement) prefElms.elementAt(i);

                // create the pref
                Record pref = oase.getModifier().create(PREFS_TABLE);
                pref.setIntField(OWNER_FIELD, personId);
                pref.setStringField(NAME_FIELD, prefElm.getAttr(NAME_FIELD));
                pref.setStringField(VALUE_FIELD, prefElm.getAttr(VALUE_FIELD));
                pref.setIntField(TYPE_FIELD, prefElm.getIntAttr(TYPE_FIELD));

                if(i==0){
                    prefString +=prefElm.getAttr(NAME_FIELD) + "=" + prefElm.getAttr(VALUE_FIELD);
                }else{
                    prefString +=", " + prefElm.getAttr(NAME_FIELD) + "=" + prefElm.getAttr(VALUE_FIELD);
                }

                prefs[i] = pref;
                oase.getModifier().insert(pref);

                // relate pref to person
                oase.getRelater().relate(person, pref);
            }

            //JXElement generated = RouteGenerator.generateRoute(prefs);
            //log.info("dbg 4 " + new String(generated.toBytes(false)));
            URL url = new URL("http://local.diwi.nl/diwi/testresponse/generateroute1.xml");
            JXElement generated = new JXBuilder().build(url);
            Record route = null;

            if(generated != null && generated.hasChildren() && generated.getChildByTag("rte")!=null && generated.getChildByTag("rte").hasChildren()){
                //make sure all other generated routes are set to 'inactive'
                String tables = "diwi_route,utopia_person";
                String fields = "diwi_route.id";
                String where = "diwi_route.type=" + ROUTE_TYPE_GENERATED + " AND utopia_person.id=" + personId;
                String relations = "diwi_route,utopia_person";
                String postCond = null;
                Record[] gens = QueryLogic.queryStore(oase, tables, fields, where, relations, postCond);
                for(int i=0;i<gens.length;i++){
                    Record gen = oase.getFinder().read(gens[i].getIntField(ID_FIELD));
                    gen.setIntField(STATE_FIELD, INACTIVE_STATE);
                    oase.getModifier().update(gen);
                }
                log.info("now inserting route.");
                //Process the GPX into datastructures
                try {
                    route = oase.getModifier().create(ROUTE_TABLE);
                    Format formatter = new SimpleDateFormat("EEEE, dd MMM yyyy HH:mm:ss");

                    String name = person.getStringField(Person.FIRSTNAME_FIELD) + " "
                            + person.getStringField(Person.LASTNAME_FIELD)
                            + "'s personal route generated at " + formatter.format(new Date())
                            + " -  " + prefString;

                    String gpxName = generated.getChildText(NAME_ELM);
                    if(gpxName != null && gpxName.length()>0){
                        name = gpxName + name;
                    }
                    //route.setStringField(NAME_FIELD, new String(generated.getChildByTag(NAME_ELM).getCDATA()));
                    route.setStringField(NAME_FIELD, name);
                    //route.setStringField(DESCRIPTION_FIELD, new String(generated.getChildByTag(DESCRIPTION_ELM).getCDATA()));
                    //route.setStringField(DESCRIPTION_FIELD, generated.getChildText(DESCRIPTION_ELM));
                    route.setIntField(TYPE_FIELD, ROUTE_TYPE_GENERATED);
                    route.setIntField(STATE_FIELD, ACTIVE_STATE);

                    LineString lineString = GPXUtil.GPXRoute2LineString(generated);
                    log.info(lineString.toString());
                    // Convert if routing API is in RD
                    //if (SRID_ROUTING_API == EPSG_DUTCH_RD) {
                        lineString = ProjectionConversionUtil.RD2WGS84(lineString);
                    //}
                    log.info(lineString.toString());

                    PGgeometryLW geom = new PGgeometryLW(lineString);
                    route.setObjectField(PATH_FIELD, geom);
                    oase.getModifier().insert(route);

                    // now relate the route to the person
                    oase.getRelater().relate(person, route);
                } catch (OaseException e) {
                    e.printStackTrace();
                }
            }

            if(route == null){
                return new JXElement(ROUTE_ELM);
            }else{
                //Convert Route record to XML and add to result
                return getRoute(route);
            }
        } catch (Throwable t) {
            log.error("Exception in generateRoute: " + t.toString());
            throw new UtopiaException("Error in generateRoute", t, ErrorCode.__6006_database_irregularity_error);
        }
    }

    public int insertRoute(JXElement aRouteElement, int aRouteType) throws UtopiaException {
        Record route;
        try {
            // fixed routes have unique names so check first
            String name = new String(aRouteElement.getChildByTag(NAME_ELM).getCDATA());
            Record[] recs = oase.getFinder().queryTable(ROUTE_TABLE, NAME_FIELD + "='" + name + "'", null, null);
            if (recs.length > 0) {
                // do an update
                route = oase.getFinder().read(recs[0].getId());
                route.setStringField(NAME_FIELD, new String(aRouteElement.getChildByTag(NAME_ELM).getCDATA()));
                route.setStringField(DESCRIPTION_FIELD, new String(aRouteElement.getChildByTag(DESCRIPTION_ELM).getCDATA()));
                route.setIntField(TYPE_FIELD, aRouteType);

                LineString lineString = GPXUtil.GPXRoute2LineString(aRouteElement);
                // Convert if routing API is in RD
                if (SRID_ROUTING_API == EPSG_DUTCH_RD) {
                    lineString = ProjectionConversionUtil.RD2WGS84(lineString);
                }
                PGgeometryLW geom = new PGgeometryLW(lineString);
                route.setObjectField(PATH_FIELD, geom);
                oase.getModifier().update(route);

            } else {
                // do an insert
                route = oase.getModifier().create(ROUTE_TABLE);
                route.setStringField(NAME_FIELD, new String(aRouteElement.getChildByTag(NAME_ELM).getCDATA()));
                route.setStringField(DESCRIPTION_FIELD, new String(aRouteElement.getChildByTag(DESCRIPTION_ELM).getCDATA()));
                route.setIntField(TYPE_FIELD, aRouteType);

                LineString lineString = GPXUtil.GPXRoute2LineString(aRouteElement);
                // Convert if routing API is in RD
                if (SRID_ROUTING_API == EPSG_DUTCH_RD) {
                    lineString = ProjectionConversionUtil.RD2WGS84(lineString);
                }

                PGgeometryLW geom = new PGgeometryLW(lineString);
                route.setObjectField(PATH_FIELD, geom);
                oase.getModifier().insert(route);
            }
        } catch (Throwable t) {
            log.error("Exception in insertRoute: " + t.toString());
            throw new UtopiaException("Error in insertRoute", t, ErrorCode.__6006_database_irregularity_error);
        }
        return route.getId();
    }

    public JXElement getRoute(int routeId) throws UtopiaException {
        JXElement routeElm;
        try {
            routeElm = getRoute(oase.getFinder().read(routeId));
        } catch (Throwable t) {
            log.error("Exception in getRoute: " + t.toString());
            throw new UtopiaException("Error in getRoute", t, ErrorCode.__6006_database_irregularity_error);
        }
        return routeElm;
    }

    public JXElement getRoute(Record aRoute) throws UtopiaException {
        JXElement routeElm;
        try {
            routeElm = XML.createElementFromRecord(ROUTE_ELM, aRoute);
            routeElm.removeChildByTag(PATH_FIELD);

            JXElement lengthElm = new JXElement(DISTANCE_FIELD);
            lengthElm.setText("" + getDistance(aRoute));
            routeElm.addChild(lengthElm);
        } catch (Throwable t) {
            log.error("Exception in getRoute: " + t.toString());
            throw new UtopiaException("Error in getRoute", t, ErrorCode.__6006_database_irregularity_error);
        }
        return routeElm;
    }

    public Vector getRoutes(int aRouteType, String aPersonId) throws UtopiaException {
        Vector results;
        try {
            Record[] routes;
            if (aRouteType == ROUTE_TYPE_GENERATED) {
                /*Record[] recs = oase.getFinder().queryTable(ROUTE_TABLE, "diwi_route.type=" + ROUTE_TYPE_GENERATED + " AND diwi_route.state=" + ACTIVE_STATE, null, null);
                Relater relater = oase.getRelater();
                Record person = oase.getFinder().read(Integer.parseInt(aPersonId));
                int c = 0;
                for(int i=0;i<recs.length;i++){
                    if(relater.isRelated(recs[i], person)){
                        routes[c] = recs[i];
                        c++;
                    }

                }*/
                String tables = "diwi_route,utopia_person";
                String fields = "diwi_route.id,diwi_route.name,diwi_route.description";
                String where = "diwi_route.type=" + ROUTE_TYPE_GENERATED + " AND diwi_route.state=" + ACTIVE_STATE + " AND utopia_person.id=" + aPersonId;
                String relations = "diwi_route,utopia_person";
                routes = QueryLogic.queryStore(oase, tables, fields, where, relations, null);
            } else {
                routes = oase.getFinder().queryTable(ROUTE_TABLE, TYPE_FIELD + "=" + aRouteType, null, null);
            }

            results = new Vector(routes.length);
            for (int i = 0; i < routes.length; i++) {
                results.add(getRoute(routes[i]));
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

    private int getDistance(Record route) throws OaseException {
        Record distance = oase.getFinder().freeQuery(
                "select length2d(path) AS distance from diwi_route where id="
                        + route.getId())[0];

        return (int) Float.parseFloat(distance.getField(DISTANCE_FIELD).toString());
    }


    public String getMapUrl(int routeId, double width, double height) throws UtopiaException {
        Record bounds;
        try {
            bounds = oase.getFinder().freeQuery(
                    "select box2d(path) AS bbox from diwi_route where id="
                            + routeId)[0];

        } catch (OaseException e) {
            log.error("Exception in deleteRoute: " + e.toString());
            throw new UtopiaException("Exception in getMapUrl", e, ErrorCode.__6006_database_irregularity_error);
        }
        PGbox2d bbox = (PGbox2d) bounds.getObjectField("bbox");

        MapLogic mapLogic = new MapLogic();

        return mapLogic.getMapURL(routeId, ProjectionConversionUtil.WGS842RD(bbox.getURT()), ProjectionConversionUtil.WGS842RD(bbox.getLLB()), width, height);
    }
}

