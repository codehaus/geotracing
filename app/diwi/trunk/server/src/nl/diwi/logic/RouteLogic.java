// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package nl.diwi.logic;

import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.util.XML;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.postgis.PGgeometry;
import org.postgis.PGgeometryLW;

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
    public Record generateRoute(UtopiaRequest anUtopiaReq) throws UtopiaException {
		try {
            Vector results = new Vector(3);

            JXElement reqElm = anUtopiaReq.getRequestCommand();
            // ok so this person is the one generating the routes!!
            String personId  = anUtopiaReq.getUtopiaSession().getContext().getUserId();
            Record person = oase.getFinder().read(Integer.parseInt(personId));

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
                pref.setIntField(OWNER_FIELD, Integer.parseInt(personId));
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

				PGgeometryLW geom = new PGgeometryLW(PostGISUtil.GPX2LineString(generated));
				route.setObjectField(PATH_FIELD, geom);
    			oase.getModifier().insert(route);    			    			

    			// now relate the route to the person
             	oase.getRelater().relate(person, route);
    		} catch (OaseException e) {
    			e.printStackTrace();
    		}
            
         	return route;
            
        } catch (OaseException oe) {
			throw new UtopiaException("Error in generateRoute", oe, ErrorCode.__6006_database_irregularity_error);
		}
	}

    /**
	 * Delete a route.
	 *
	 * @param aRouteId a comment id
	 * @throws UtopiaException Standard exception
	 */
	public void delete(int aRouteId) throws UtopiaException {
		try {
			oase.getModifier().delete(aRouteId);
		} catch (OaseException oe) {
			throw new UtopiaException("Cannot delete route record with id=" + aRouteId, oe, ErrorCode.__6006_database_irregularity_error);
		}
	}

	/** Properties passed on from Handler. */
	public static String getProperty(String propertyName) {
		return (String) properties.get(propertyName);
	}

	/** Properties passed on from Handler. */
	protected static void setProperty(String propertyName, String propertyValue) {
		properties.put(propertyName, propertyValue);
	}

	/** Trim content for empty begin/end and maximum chars allowed. */
	protected String trimContent(String aContentString) {
		String result = aContentString.trim();

		// Check for maximum allowed content size, trim if necessary
		int maxContentSize = Integer.parseInt(properties.getProperty(PROP_MAX_CONTENT_CHARS, "512"));
		if (aContentString.length() > maxContentSize) {
			result = result.substring(0, maxContentSize);
		}
		return result;
	}

}

