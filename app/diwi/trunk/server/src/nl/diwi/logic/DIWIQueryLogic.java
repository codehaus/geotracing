package nl.diwi.logic;

import nl.diwi.external.DataSource;
import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.QueryHandler;
import org.geotracing.handler.QueryLogic;
import org.keyworx.amuse.core.Protocol;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;

import java.util.Map;
import java.util.Vector;

public class DIWIQueryLogic extends QueryLogic implements Constants {

    public static final String CMD_QUERY_ROUTES = "q-diwi-routes";
	public static final String CMD_QUERY_ROUTE_INFO = "q-diwi-route-info";
    public static final String CMD_QUERY_THEMES = "q-diwi-themes";
    public static final String CMD_QUERY_STARTPOINTS = "q-diwi-startpoints";
    public static final String CMD_QUERY_ENDPOINTS = "q-diwi-endpoints";
    public static final String CMD_QUERY_STARTENDPOINTS = "q-diwi-startendpoints";
    public static final String CMD_QUERY_TRIP = "q-diwi-trip";
    public static final String CMD_QUERY_TRIPS = "q-diwi-trips";

    public JXElement doQuery(String aQueryName, Map theParms) {
        JXElement result;

        try {
            if (aQueryName.equals(CMD_QUERY_ROUTES)) {
                result = queryRoutes(theParms);
			} else if (aQueryName.equals(CMD_QUERY_ROUTE_INFO)) {
				String id = getParameter(theParms, PAR_ID, null);
				throwOnMissingParm(PAR_ID, id);
				Record route = getOase().getFinder().freeQuery(
		//				"select id,name,description,type,distance,extent(rdpath) AS bbox from diwi_route where id=" + id)[0];
				"select id,astext(extent(rdpath)) AS bbox from diwi_route where id=" + id)[0];
				JXElement routeElm = route.toXML();

				result = Protocol.createResponse(CMD_QUERY_ROUTE_INFO);
				result.addChild(routeElm);
			} else if (aQueryName.equals(CMD_QUERY_THEMES)) {
                result = queryThemes(theParms);
            } else if (aQueryName.equals(CMD_QUERY_STARTPOINTS)) {
                result = queryStartPoints(theParms);
            } else if (aQueryName.equals(CMD_QUERY_ENDPOINTS)) {
                result = queryEndPoints(theParms);
            } else if (aQueryName.equals(CMD_QUERY_STARTENDPOINTS)) {
                result = queryStartEndPoints(theParms);
            } else if (aQueryName.equals(CMD_QUERY_TRIP)) {
                result = queryTrip(theParms);
            } else if (aQueryName.equals(CMD_QUERY_TRIPS)) {
                result = queryTrips(theParms);
            } else {
				result = super.doQuery(aQueryName, theParms);
			}
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
        result.addChildren(convertToRecordElms(ds.getKICHThemes()));
        return result;
    }

    private JXElement queryStartPoints(Map theParms) throws Exception {
        JXElement result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
        POILogic logic = new POILogic(getOase());
        result.addChildren(convertToRecordElms(logic.getPoisByType(POI_STARTPOINT)));
        return result;
    }

    private JXElement queryEndPoints(Map theParms) throws Exception {
        JXElement result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
        POILogic logic = new POILogic(getOase());
        result.addChildren(convertToRecordElms(logic.getPoisByType(POI_ENDPOINT)));
        return result;
    }

    private JXElement queryStartEndPoints(Map theParms) throws Exception {
        JXElement result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
        POILogic logic = new POILogic(getOase());
        result.addChildren(convertToRecordElms(logic.getPoisByType(POI_START_AND_ENDPOINT)));
        return result;
    }

    private JXElement queryRoutes(Map theParms) throws Exception {
        JXElement result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
        String personId = getParameter(theParms, "personid", null);
        String type = getParameter(theParms, "type", null);
        RouteLogic logic = new RouteLogic(getOase());
        result.addChildren(convertToRecordElms(logic.getRoutes(type, personId)));
        return result;
    }

    private JXElement queryTrip(Map theParms) throws Exception {
        JXElement result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
        String id = (String) theParms.get("id");
        LogLogic logic = new LogLogic(getOase());
        JXElement elm = logic.getLog(id);
        elm.setTag("record");
        result.addChild(elm);
        return result;
    }

    private JXElement queryTrips(Map theParms) throws Exception {
        JXElement result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
        String personId = (String) theParms.get("personid");
        String type = (String) theParms.get("type");
        LogLogic logic = new LogLogic(getOase());
        result.addChildren(convertToRecordElms(logic.getLogs(personId, type)));
        return result;
    }

    private Vector convertToRecordElms(Vector theElements){
        for(int i=0;i<theElements.size();i++){
            ((JXElement)theElements.elementAt(i)).setTag("record");
        }
        return theElements;
    }    

}
