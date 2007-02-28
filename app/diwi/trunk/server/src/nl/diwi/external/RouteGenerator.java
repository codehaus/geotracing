package nl.diwi.external;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;

import org.geotracing.gis.GeoPoint;

import org.keyworx.server.ServerConfig;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.utopia.core.util.XML;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;

public class RouteGenerator implements Constants {
	Oase oase;
	
	private static String generatorUrl = ServerConfig.getProperty(GENERATOR_URL);
	
	public RouteGenerator(Oase oase) {
		this.oase = oase;
	}
	
	//Query by example? non filled values are 'don't care'
	public Vector generateRoutes(Record [] prefs) {
		Vector results = new Vector();
		JXElement resultRoutes = null;
		
		//build the url
		StringBuffer urlBuffer = new StringBuffer();
		urlBuffer.append(generatorUrl);
		urlBuffer.append("?");
		for(int i = 0; i < prefs.length; i++) {
			urlBuffer.append(prefs[i].getStringField(NAME_FIELD));
			urlBuffer.append('=');
			urlBuffer.append(prefs[i].getStringField(VALUE_FIELD));
			if(i < prefs.length - 1) {
				urlBuffer.append('&');			
			}
		}

		//Get the GPX
		try {
			resultRoutes = new JXBuilder().build(new URL(urlBuffer.toString()));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Vector gpxElms = resultRoutes.getChildren();
		//Process the GPX into datastructures
        for(int i=0;i<gpxElms.size();i++){
            JXElement gpxElm = (JXElement)gpxElms.elementAt(i);
            Record route = null;
			try {
				route = oase.getModifier().create(ROUTE_TABLE);
				route.setStringField(NAME_FIELD, gpxElm.getChildText(NAME_FIELD));
				route.setStringField(DESCRIPTION_FIELD, gpxElm.getChildText(NAME_FIELD));
				route.setIntField(TYPE_FIELD, ROUTE_TYPE_TEMP);
				route.setXMLField(PATH_FIELD, gpxElm);
				oase.getModifier().insert(route);
			} catch (OaseException e) {
				e.printStackTrace();
			}

			//Convert Route record to XML and add to result
            JXElement routeElm = null;
			try {
				routeElm = XML.createElementFromRecord(ROUTE_ELM, route);
				routeElm.removeChildByTag(PATH_FIELD);
	            results.add(routeElm);
			} catch (UtopiaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}            

            // now relate the route to the person
            //oase.getRelater().relate(person, route);
        }
		
		return results;
	}
	
	public Record generateShortestRoute(GeoPoint from, GeoPoint to) {
        Record route = null;
		
		//JXBuilder().build(new URL());
        
        try {
			route = oase.getModifier().create(ROUTE_TABLE);
		} catch (OaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return route;		
	}
	
}