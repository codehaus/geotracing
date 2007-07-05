package nl.diwi.external;

import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import org.keyworx.amuse.core.Amuse;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.UtopiaException;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

public class RouteGenerator implements Constants {

	private static String GENERATOR_URL = Amuse.server.getPortal().getProperty(ROUTING_SERVLET_URL) + "?request=createroute";

	/** Generate route from preferences using external generator. */
	public static JXElement generateRoute(Record[] prefs) throws UtopiaException {
		JXElement resultRoute;

		//Get the GPX
		String url=null;
		try {
			url = buildUrl(prefs, GENERATOR_URL);
			resultRoute = new JXBuilder().build(new URL(url));
		} catch (Throwable t) {
			throw new UtopiaException("Exception generating route from " + url, t);
		}
		return resultRoute;
	}

	/** Build URL from prefs */
	private static String buildUrl(Record[] thePrefs, String anUrl) throws UnsupportedEncodingException {
		//build the url
		StringBuffer urlBuffer = new StringBuffer();
		urlBuffer.append(anUrl);
		if (anUrl.indexOf("?") == -1) {
			urlBuffer.append("?");
		} else {
			urlBuffer.append("&");
		}
		for (int i = 0; i < thePrefs.length; i++) {
			urlBuffer.append(thePrefs[i].getStringField(NAME_FIELD));
			urlBuffer.append('=');
			urlBuffer.append(thePrefs[i].getStringField(VALUE_FIELD));
			if (i < thePrefs.length - 1) {
				urlBuffer.append('&');
			}
		}
		return URLEncoder.encode(urlBuffer.toString(), "UTF-8");
	}

}