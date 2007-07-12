package nl.diwi.external;

import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import org.keyworx.amuse.core.Amuse;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.common.log.Logging;
import org.keyworx.common.log.Log;
import org.keyworx.common.util.Sys;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

public class RouteGenerator implements Constants {

	private static String GENERATOR_URL = Amuse.server.getPortal().getProperty(ROUTING_SERVLET_URL) + "?request=createroute";
	private static Log log = Logging.getLog("RouteGenerator");

	/** Generate route from preferences using external generator. */
	public static JXElement generateRoute(Record[] prefs) throws UtopiaException {
		JXElement resultRoute;

		//Get the GPX
		String url=null;
		try {
			url = buildUrl(prefs, GENERATOR_URL);
			long t1;
			t1= Sys.now();
			log.info("Generating route: url=" + url);
			resultRoute = new JXBuilder().build(new URL(url));
			log.info("Generated route dT=" + (Sys.now() - t1)/1000 + " sec");
		} catch (Throwable t) {
			throw new UtopiaException("Exception generating route", t);
		}
		return resultRoute;
	}

    /** Generate route from preferences using external generator. */
	public static JXElement generateRoute(String theNameValuePairs) throws UtopiaException {
		JXElement resultRoute;

		//Get the GPX
		String url=null;
		try {
			resultRoute = new JXBuilder().build(new URL(GENERATOR_URL + "?" + theNameValuePairs));
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
			urlBuffer.append(URLEncoder.encode(thePrefs[i].getStringField(NAME_FIELD), "UTF-8"));
			urlBuffer.append('=');
			urlBuffer.append(URLEncoder.encode(thePrefs[i].getStringField(VALUE_FIELD), "UTF-8"));
			if (i < thePrefs.length - 1) {
				urlBuffer.append('&');
			}
		}
		return urlBuffer.toString();
	}

}