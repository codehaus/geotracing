package nl.diwi.logic;

import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.amuse.core.Amuse;
import org.postgis.Point;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import nl.diwi.util.Constants;

public class MapLogic implements Constants {

	private Log log = Logging.getLog("MapLogic");

    public MapLogic() {
	}

	public String getMapURL(String theLayers, Point llb, Point urt, int width, int height) throws UtopiaException {
		String boxString;
		try {
			boxString = URLEncoder.encode("" + llb.x + "," + llb.y + "," + urt.x + "," + urt.y, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("Exception in getMapURL: " + e.toString());
			throw new UtopiaException("Exception in getMapUrl", e, ErrorCode.__6006_database_irregularity_error);
		}

		return Amuse.server.getPortal().getProperty(SITE_URL) + "/map?LAYERS=" + theLayers + "&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&FORMAT=image%2Fjpeg&SRS=EPSG%3A28992&BBOX=" + boxString + "&WIDTH=" + width + "&HEIGHT=" + height;
	}

	public String getMapURL(int aLayerId, int routeId, String poiIds, boolean showUGC, Point llb, Point urt, double width, double height) throws UtopiaException {

		double boundsHeight = urt.y - llb.y;
		double boundsWidth = urt.x - llb.x;

		if (width / height > boundsWidth / boundsHeight) {
			//pad x
			double padWidth = ((width / height) * boundsHeight) - boundsWidth;
			llb.x -= (padWidth / 2);
			urt.x += (padWidth / 2);
		} else {
			//pad y
			double padHeight = (boundsWidth * (height / width)) - boundsHeight;
			llb.y -= (padHeight / 2);
			urt.y += (padHeight / 2);
		}

		//add a 10% border
		boundsHeight = urt.y - llb.y;
		boundsWidth = urt.x - llb.x;
		double padHeight = 0.1 * boundsHeight;
		double padWidth = 0.1 * boundsWidth;

		llb.x -= padWidth / 2;
		urt.x += padWidth / 2;
		llb.y -= padHeight / 2;
		urt.y += padHeight / 2;

        String layers = "";
        if(aLayerId == MAP_GREBBELINIE){
            layers += "topnl_diwiwms";
        }else if(aLayerId == MAP_DOMPLEIN1){
            layers += "topnl_domplein1";
        }else if(aLayerId == MAP_DOMPLEIN2){
            layers += "topnl_domplein2";
        }else if(aLayerId == MAP_DOMPLEIN3){
            layers += "topnl_domplein3";
        }else{
            layers += "topnl_diwiwms";
        }

        layers += ",diwi_routes_sel";
        
        if (poiIds != null) {
			layers = layers + ",diwi_pois_sel";
		}

		if (showUGC) {
			layers += ",diwi_ugc";
		}

		// Route id is really part of the layers
		layers = layers + "&ID=" + routeId;
		if (poiIds != null) {
			// Append , separated list of poi ids
			layers = layers + "&POIIDS=" + poiIds;
		}
		
		return getMapURL(layers, llb, urt, (int) width, (int) height);
	}
}
