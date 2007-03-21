// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package nl.diwi.logic;

import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.util.XML;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.postgis.PGgeometry;
import org.postgis.PGgeometryLW;
import org.postgis.PGbox2d;

import java.sql.SQLException;
import java.util.Properties;
import java.util.Vector;

import nl.diwi.external.RouteGenerator;
import nl.diwi.util.Constants;
import nl.diwi.util.PostGISUtil;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;

/**
  * Handles all logic related to commenting.
  * <p/>
  * Uses Oase directly for DB updates.
  *
  * @author Just van den Broecke
  * @version $Id: CommentLogic.java 353 2007-02-02 12:04:11Z just $
 */
public class RouteLogic implements Constants {
	public static final String TABLE_PERSON = "utopia_person";

    private static final Properties properties = new Properties();
    public static final String PROP_MAX_CONTENT_CHARS = "max-content-chars";

    private Oase oase;

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
            for(int i=0;i<prefs.length;i++){
                oase.getModifier().delete(prefs[i]);
            }

            // now store the prefs            
            Vector prefElms = reqElm.getChildrenByTag(PREF_ELM);
            prefs = new Record[prefElms.size()];
            for(int i=0;i<prefElms.size();i++){
                JXElement prefElm = (JXElement) prefElms.elementAt(i);

                // create the pref
                Record pref = oase.getModifier().create(PREFS_TABLE);
                pref.setIntField(OWNER_FIELD, personId);
                pref.setStringField(NAME_FIELD, prefElm.getAttr(NAME_FIELD));
                pref.setStringField(VALUE_FIELD, prefElm.getAttr(VALUE_FIELD));
                pref.setIntField(TYPE_FIELD, prefElm.getIntAttr(TYPE_FIELD));
                prefs[i] = pref;
				oase.getModifier().insert(pref);

                // relate pref to person
                oase.getRelater().relate(person, pref);
            }

            JXElement generated = RouteGenerator.generateRoute(prefs);

    		//Process the GPX into datastructures
            Record route = null;
    		try {
    			route = oase.getModifier().create(ROUTE_TABLE);
    			route.setStringField(NAME_FIELD, new String(generated.getChildByTag(NAME_ELM).getCDATA()));
    			route.setStringField(DESCRIPTION_FIELD, new String(generated.getChildByTag(DESCRIPTION_ELM).getCDATA()));
    			route.setIntField(TYPE_FIELD, ROUTE_TYPE_GENERATED);

				PGgeometryLW geom = new PGgeometryLW(PostGISUtil.GPXRoute2LineString(generated));
				route.setObjectField(PATH_FIELD, geom);
    			oase.getModifier().insert(route);    			    			

    			// now relate the route to the person
             	oase.getRelater().relate(person, route);
    		} catch (OaseException e) {
    			e.printStackTrace();
    		}
        
    		//Convert Route record to XML and add to result
            JXElement routeElm = getRoute(route.getId());          

    		return routeElm;
        } catch (OaseException oe) {
			throw new UtopiaException("Error in generateRoute", oe, ErrorCode.__6006_database_irregularity_error);
		}
	}

	public JXElement getRoute(int routeId) {
        JXElement routeElm = null;
		try {
			Record route = oase.getFinder().read(routeId);
			routeElm = XML.createElementFromRecord(ROUTE_ELM, route);			
			routeElm.removeChildByTag(PATH_FIELD);
			
			JXElement lengthElm = new JXElement(DISTANCE_FIELD);
			lengthElm.setText("" + getDistance(route));
			routeElm.addChild(lengthElm);
		} catch (OaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UtopiaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return routeElm;
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
		} catch (OaseException oe) {
			throw new UtopiaException("Cannot delete route record with id=" + aRouteId, oe, ErrorCode.__6006_database_irregularity_error);
		}
	}
	
	private int getDistance(Record route) throws OaseException {
		Record distance = oase.getFinder().freeQuery(
				"select length2d(path) AS distance from diwi_route where id="
				+ route.getId())[0];
		
		return (int)Float.parseFloat(distance.getField(DISTANCE_FIELD).toString());	
	}


	public String getMapUrl(int routeId, int width, int height) {
		Record bounds = null;
		try {
			bounds = oase.getFinder().freeQuery(
					"select box2d(path) AS bbox from diwi_route where id="
					+ routeId)[0];

		} catch (OaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PGgeometryLW geom = (PGgeometryLW)bounds.getObjectField("bbox");
		
		
		/*
		PGbox2d box = (PGbox2d)geom.getGeometry();
		try {
			box = new PGbox2d(bounds.getField("bbox").toString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		String boxString = "153929.028192%2C459842.063363%2C158851.133074%2C463123.466618";
		String url = "http://test.digitalewichelroede.nl/map/?ID=" + routeId + "&LAYERS=topnl_raster,single_diwi_route&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&FORMAT=image%2Fjpeg&SRS=EPSG%3A28992&BBOX=" + boxString + "&WIDTH=" + width + "&HEIGHT=" + height;
		return url;
	}	
}

