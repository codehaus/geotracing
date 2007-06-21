package nl.diwi.external;

import nl.diwi.util.Constants;
import nl.diwi.util.NetConnection;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import org.keyworx.amuse.core.Amuse;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.UtopiaException;
import org.postgis.Point;

import java.net.URL;

public class RouteGenerator implements Constants {

    private static String generatorUrl = Amuse.server.getPortal().getProperty(ROUTING_SERVLET_URL);

    //Query by example? non filled values are 'don't care'
    public static JXElement generateRoute(Record[] prefs) throws UtopiaException {
        JXElement resultRoute;
        String url = Amuse.server.getPortal().getProperty(ROUTING_SERVLET_URL);
        url += "?request=createroute";
        url = buildUrl(prefs, url);
        JXElement routeElm = NetConnection.getXMLFromREST(url);
        if (routeElm == null) {
            url = buildUrl(prefs, generatorUrl);
        }
        //Get the GPX
        try {
            resultRoute = new JXBuilder().build(new URL(url));
        } catch (Throwable t) {
            throw new UtopiaException("Exception building generated route from " + url, t);
        }
        return resultRoute;
    }

    private static String buildUrl(Record[] thePrefs, String anUrl) {
        //build the url
        StringBuffer urlBuffer = new StringBuffer();
        urlBuffer.append(anUrl);
        if (anUrl.indexOf("?") != -1) {
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
        return urlBuffer.toString();
    }

    public JXElement generateShortestRoute(Point from, Point to) {
        JXElement route = null;

        //JXBuilder().build(new URL());

        return route;
    }

}