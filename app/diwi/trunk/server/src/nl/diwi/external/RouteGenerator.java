package nl.diwi.external;

import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import org.keyworx.amuse.core.Amuse;
import org.keyworx.oase.api.Record;
import org.postgis.Point;

import java.net.MalformedURLException;
import java.net.URL;

public class RouteGenerator implements Constants {
	
	private static String generatorUrl = Amuse.server.getPortal().getProperty(GENERATOR_URL);
		
	//Query by example? non filled values are 'don't care'
	public static JXElement generateRoute(Record [] prefs) {
		JXElement resultRoute = null;
		
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
			resultRoute = new JXBuilder().build(new URL(urlBuffer.toString()));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return resultRoute;
	}
	
	public JXElement generateShortestRoute(Point from, Point to) {
		JXElement route = null;
		
		//JXBuilder().build(new URL());
		
		return route;
	}
	
}