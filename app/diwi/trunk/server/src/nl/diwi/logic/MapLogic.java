package nl.diwi.logic;

import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.postgis.Point;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MapLogic {

    private static final String MAPPING_URL = "http://test.digitalewichelroede.nl/map/";
    private Log log = Logging.getLog("MapLogic");


    public MapLogic() {

    }

    public String getMapURL(Point urt, Point llb, int width, int height) throws UtopiaException {
        String boxString;
        try {
            boxString = URLEncoder.encode("" + llb.x + "," + llb.y + "," + urt.x + "," + urt.y, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Exception in deleteRoute: " + e.toString());
            throw new UtopiaException("Exception in getMapUrl", e, ErrorCode.__6006_database_irregularity_error);
        }

        return "" + MAPPING_URL + "?LAYERS=topnl_raster&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&FORMAT=image%2Fjpeg&SRS=EPSG%3A28992&BBOX=" + boxString + "&WIDTH=" + width + "&HEIGHT=" + height;
    }

    public String getMapURL(int routeId, Point urt, Point llb, double width, double height) throws UtopiaException {

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


        String boxString;
        try {
            boxString = URLEncoder.encode("" + llb.x + "," + llb.y + "," + urt.x + "," + urt.y, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Exception in deleteRoute: " + e.toString());
            throw new UtopiaException("Exception in getMapUrl", e, ErrorCode.__6006_database_irregularity_error);
        }

        return "" + MAPPING_URL + "?ID=" + routeId + "&LAYERS=topnl_raster,single_diwi_route&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&FORMAT=image%2Fjpeg&SRS=EPSG%3A28992&BBOX=" + boxString + "&WIDTH=" + width + "&HEIGHT=" + height;
    }


}
