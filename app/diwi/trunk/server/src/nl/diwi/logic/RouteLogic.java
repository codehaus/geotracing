// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package nl.diwi.logic;

import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.utopia.core.session.UtopiaRequest;

import java.util.Properties;
import java.util.Vector;

import nl.diwi.util.Constants;
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
     */
    public Vector generateRoute(UtopiaRequest anUtopiaReq) throws UtopiaException {
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
            for(int i=0;i<prefElms.size();i++){
                JXElement prefElm = (JXElement) prefElms.elementAt(i);

                // create the pref
                Record pref = oase.getModifier().create(PREFS_TABLE);
                pref.setStringField(NAME_FIELD, prefElm.getAttr(NAME_FIELD));
                pref.setStringField(VALUE_FIELD, prefElm.getAttr(VALUE_FIELD));
                pref.setIntField(TYPE_FIELD, prefElm.getIntAttr(TYPE_FIELD));
                oase.getModifier().insert(pref);

                // relate pref to person
                oase.getRelater().relate(person, pref);
            }

            // now access the WUR route-generator
            // but for now just generate something ourselves
            Vector gpxElms = WURRouteGenerator(prefElms);

            for(int i=0;i<gpxElms.size();i++){
                JXElement gpxElm = (JXElement)gpxElms.elementAt(i);
                Record route = oase.getModifier().create(ROUTE_TABLE);
                route.setStringField(NAME_FIELD, gpxElm.getChildText(NAME_FIELD));
                route.setStringField(DESCRIPTION_FIELD, gpxElm.getChildText(DESCRIPTION_FIELD));
                route.setXMLField(PATH_FIELD, gpxElm);
                oase.getModifier().insert(route);

                // let's not add the gpx file....the client will have to wait to freakin long
                JXElement routeElm = route.toXML();
                routeElm.removeChildByTag(PATH_FIELD);
                results.add(routeElm);

                // now relate the route to the person
                oase.getRelater().relate(person, route);
            }

            return results;

        } catch (OaseException oe) {
			throw new UtopiaException("Error in generateRoute", oe, ErrorCode.__6006_database_irregularity_error);
		}
	}

    private Vector WURRouteGenerator(Vector thePrefs){
        Vector routes = new Vector(3);
        routes.add(WURGenRoute(thePrefs));
        routes.add(WURGenRoute(thePrefs));
        routes.add(WURGenRoute(thePrefs));
        return routes;
    }

    private JXElement WURGenRoute(Vector thePrefs){
        JXElement gpx = null;
        try{
            gpx = new JXBuilder().build("<gpx version=\"1.1\" creator=\"www.geotracing.org\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                    "     xmlns=\"http://www.topografix.com/GPX/1/1\"" +
                    "     xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" cnt=\"11\">" +
                    "    <time>2007-02-18T16:09:57Z</time>" +
                    "    <name>track #4721</name>" +
                    "    <number>4721</number>" +
                    "    <wpt lon=\"4.9193733\" lat=\"52.158635\">" +
                    "        <ele>0.0</ele>" +
                    "        <time>2007-02-18T14:28:08Z</time>" +
                    "        <name>Breukelen-Kockengen</name>" +
                    "        <desc/>" +
                    "        <type>image/jpeg</type>" +
                    "        <link>http://www.geoskating.com/gs/media.srv?id=4736</link>" +
                    "    </wpt>" +
                    "    <wpt lon=\"4.912625\" lat=\"52.129565\">" +
                    "        <ele>0.0</ele>" +
                    "        <time>2007-02-18T14:58:47Z</time>" +
                    "        <name>Breukelen-Kockengen</name>" +
                    "        <desc/>" +
                    "        <type>image/jpeg</type>" +
                    "        <link>http://www.geoskating.com/gs/media.srv?id=4756</link>" +
                    "    </wpt>" +
                    "    <wpt lon=\"4.9222317\" lat=\"52.1063783\">" +
                    "        <ele>0.0</ele>" +
                    "        <time>2007-02-18T15:11:33Z</time>" +
                    "        <name>Breukelen-Kockengen</name>" +
                    "        <desc/>" +
                    "        <type>image/jpeg</type>" +
                    "        <link>http://www.geoskating.com/gs/media.srv?id=4751</link>" +
                    "    </wpt>" +
                    "    <wpt lon=\"4.9323433\" lat=\"52.1054317\">" +
                    "        <ele>0.0</ele>" +
                    "        <time>2007-02-18T15:16:05Z</time>" +
                    "        <name>Breukelen-Kockengen</name>" +
                    "        <desc/>" +
                    "        <type>image/jpeg</type>" +
                    "        <link>http://www.geoskating.com/gs/media.srv?id=4746</link>" +
                    "    </wpt>" +
                    "    <wpt lon=\"4.9390117\" lat=\"52.10326\">" +
                    "        <ele>0.0</ele>" +
                    "        <time>2007-02-18T15:18:46Z</time>" +
                    "        <name>Breukelen-Kockengen</name>" +
                    "        <desc/>" +
                    "        <type>image/jpeg</type>" +
                    "        <link>http://www.geoskating.com/gs/media.srv?id=4761</link>" +
                    "    </wpt>" +
                    "    <wpt lon=\"4.96059\" lat=\"52.1340267\">" +
                    "        <ele>0.0</ele>" +
                    "        <time>2007-02-18T15:39:42Z</time>" +
                    "        <name>Breukelen-Kockengen</name>" +
                    "        <desc/>" +
                    "        <type>image/jpeg</type>" +
                    "        <link>http://www.geoskating.com/gs/media.srv?id=4741</link>" +
                    "    </wpt>" +
                    "    <wpt lon=\"4.951125\" lat=\"52.14467\">" +
                    "        <ele>0.0</ele>" +
                    "        <time>2007-02-18T15:45:07Z</time>" +
                    "        <name>Breukelen-Kockengen</name>" +
                    "        <desc/>" +
                    "        <type>image/jpeg</type>" +
                    "        <link>http://www.geoskating.com/gs/media.srv?id=4731</link>" +
                    "    </wpt>" +
                    "    <trk>" +
                    "        <trkseg>" +
                    "            <trkpt lon=\"4.9841483\" lat=\"52.1717417\">" +
                    "                <time>2007-02-18T13:52:28Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9839717\" lat=\"52.1719667\">" +
                    "                <time>2007-02-18T13:52:41Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9840550\" lat=\"52.1724783\">" +
                    "                <time>2007-02-18T13:52:56Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9843150\" lat=\"52.1729917\">" +
                    "                <time>2007-02-18T13:53:11Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9842033\" lat=\"52.1736617\">" +
                    "                <time>2007-02-18T13:53:26Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9841650\" lat=\"52.1742983\">" +
                    "                <time>2007-02-18T13:53:41Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9846467\" lat=\"52.1748867\">" +
                    "                <time>2007-02-18T13:53:56Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9823333\" lat=\"52.1756833\">" +
                    "                <time>2007-02-18T13:54:45Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9812783\" lat=\"52.1758183\">" +
                    "                <time>2007-02-18T13:55:00Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9802050\" lat=\"52.1759583\">" +
                    "                <time>2007-02-18T13:55:15Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9791350\" lat=\"52.1761017\">" +
                    "                <time>2007-02-18T13:55:30Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9780817\" lat=\"52.1760817\">" +
                    "                <time>2007-02-18T13:55:46Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9771633\" lat=\"52.1756917\">" +
                    "                <time>2007-02-18T13:56:01Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9765050\" lat=\"52.1754317\">" +
                    "                <time>2007-02-18T13:56:16Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9766300\" lat=\"52.1752933\">" +
                    "                <time>2007-02-18T13:56:31Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9767950\" lat=\"52.1751683\">" +
                    "                <time>2007-02-18T13:56:46Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9770650\" lat=\"52.1749550\">" +
                    "                <time>2007-02-18T13:57:01Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9776000\" lat=\"52.1744850\">" +
                    "                <time>2007-02-18T13:57:17Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9780433\" lat=\"52.1741033\">" +
                    "                <time>2007-02-18T13:57:32Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9786283\" lat=\"52.1735967\">" +
                    "                <time>2007-02-18T13:57:47Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9792400\" lat=\"52.1730100\">" +
                    "                <time>2007-02-18T13:58:02Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9797633\" lat=\"52.1724317\">" +
                    "                <time>2007-02-18T13:58:17Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9803467\" lat=\"52.1718250\">" +
                    "                <time>2007-02-18T13:58:32Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9809583\" lat=\"52.1711567\">" +
                    "                <time>2007-02-18T13:58:48Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>" +
                    "            <trkpt lon=\"4.9815350\" lat=\"52.1705917\">" +
                    "                <time>2007-02-18T13:59:03Z</time>" +
                    "                <ele>0.0</ele>" +
                    "            </trkpt>            " +
                    "        </trkseg>" +
                    "    </trk>" +
                    "</gpx>");
        }catch(Throwable t){
            System.out.println(t.toString());
        }
        return gpx;
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

