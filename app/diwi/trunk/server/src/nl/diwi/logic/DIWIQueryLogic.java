package nl.diwi.logic;

import nl.diwi.util.Constants;
import nl.diwi.external.DataSource;
import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.QueryLogic;
import org.geotracing.handler.QueryHandler;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.oase.api.OaseException;
import org.keyworx.amuse.core.Protocol;

import java.util.Map;

public class DIWIQueryLogic extends QueryLogic implements Constants {

    public static final String CMD_QUERY_ROUTES = "q-diwi-routes";
    public static final String CMD_QUERY_THEMES = "q-diwi-themes";
    public static final String CMD_QUERY_STARTPOINTS = "q-diwi-startpoints";
    public static final String CMD_QUERY_ENDPOINTS = "q-diwi-endpoints";
    public static final String CMD_QUERY_TRIP = "q-diwi-trip";
    public static final String CMD_QUERY_TRIPS = "q-diwi-trips";

    public JXElement doQuery(String aQueryName, Map theParms) {
        JXElement result;

        try {
            if (aQueryName.equals(CMD_QUERY_ROUTES)) {
                return queryRoutes(theParms);
            }else if (aQueryName.equals(CMD_QUERY_THEMES)) {
                return queryThemes(theParms);
            }else if (aQueryName.equals(CMD_QUERY_STARTPOINTS)) {
                return queryStartPoints(theParms);
            }else if (aQueryName.equals(CMD_QUERY_ENDPOINTS)) {
                return queryEndPoints(theParms);
            }else if (aQueryName.equals(CMD_QUERY_TRIP)) {
                return queryTrip(theParms);
            }else if (aQueryName.equals(CMD_QUERY_TRIPS)) {
                return queryTrips(theParms);
            }

            return super.doQuery(aQueryName, theParms);
        } catch (Throwable ue) {
            result = new JXElement(TAG_ERROR);
            result.setText("Unexpected Error during query " + ue);
            log.error("Unexpected Error during query", ue);
        }

        return result;
    }

    private JXElement queryThemes(Map theParms) throws UtopiaException {
        JXElement result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
        DataSource ds = new DataSource(getOase());
        result.addChildren(ds.getKICHThemes());
        return result;
    }

    private JXElement queryStartPoints(Map theParms) throws Exception {
        JXElement result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
        POILogic logic = new POILogic(getOase());
        result.addChildren(logic.getStartPoints());
        return result;
    }

    private JXElement queryEndPoints(Map theParms) throws Exception {
        JXElement result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
        POILogic logic = new POILogic(getOase());
        result.addChildren(logic.getEndPoints());
        return result;
    }

    private JXElement queryRoutes(Map theParms) throws Exception {
        JXElement result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
        String personId = (String)theParms.get("personid");
        int type = Integer.parseInt((String)theParms.get("type"));
        RouteLogic logic = new RouteLogic(getOase());
        result.addChildren(logic.getRoutes(type, personId));
        return result;        
    }

    private JXElement queryTrip(Map theParms) throws Exception {
        JXElement result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
        String id = (String)theParms.get("id");
        LogLogic logic = new LogLogic(getOase());
        result.addChild(logic.getLog(id));
        return result;
    }

    private JXElement queryTrips(Map theParms) throws Exception {
        JXElement result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
        String personId = (String)theParms.get("personid");
        String type = (String)theParms.get("type");
        LogLogic logic = new LogLogic(getOase());
        result.addChildren(logic.getLogs(personId, type));
        return result;
    }


}
