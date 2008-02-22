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
import org.postgis.PGbox2d;

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
	public static final String CMD_QUERY_UGCS = "q-diwi-ugcs";

	public JXElement doQuery(String aQueryName, Map theParms) {
		JXElement result;

		try {
			if (aQueryName.equals(CMD_QUERY_ROUTES)) {
				result = queryRoutes(theParms);
			} else if (aQueryName.equals(CMD_QUERY_ROUTE_INFO)) {
				String id = getParameter(theParms, PAR_ID, null);
				throwOnMissingParm(PAR_ID, id);
				Record bboxRec = getOase().getFinder().freeQuery(
						"select extent(rdpath) AS bbox from diwi_route where id=" + id)[0];
				PGbox2d pgBox = (PGbox2d) bboxRec.getObjectField("bbox");
				String bbox = (int) pgBox.getLLB().x + "," + (int) pgBox.getLLB().y + "," + (int) pgBox.getURT().x + "," + (int) pgBox.getURT().y;

				Record[] routeRec = getOase().getFinder().freeQuery(
						"select id,name,description,type,distance from diwi_route where id=" + id);

				result = createResponse(routeRec);

				// Add bounding box
				result.getChildAt(0).addTextChild("bbox", bbox);
				String poiIds = getRoutePOIs(Integer.parseInt(id));
				if (poiIds != null) {
					result.getChildAt(0).addTextChild("pois", poiIds);
				}

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
			} else if (aQueryName.equals(CMD_QUERY_UGCS)) {
				result = queryUGCs(theParms);
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

	public static String getRoutePOIs(int aRouteId) throws OaseException {
		// Add comma-separated list of poi ids
		String poiIds = null;
		String tables = "diwi_route,diwi_poi";
		String fields = "diwi_poi.id";
		String where = "diwi_route.id=" + aRouteId;
		String relations = "diwi_route,diwi_poi";
		String postCond = null;
		Record[] poiRecs = queryStore(getOase(), tables, fields, where, relations, postCond);
		log.info("poirecs.length=" + poiRecs.length);
		if (poiRecs.length > 0) {
			poiIds = "";
			String nextPrefix;
			for (int i = 0; i < poiRecs.length; i++) {
				nextPrefix = i == 0 ? "" : ",";
				poiIds = poiIds + nextPrefix + poiRecs[i].getId();
			}
		}
		return poiIds;
	}

	private JXElement queryThemes(Map theParms) throws UtopiaException {
		JXElement result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
		DataSource ds = new DataSource(getOase());
		result.addChildren(convertToRecordElms(ds.getKICHThemes()));
		return result;
	}

	private JXElement queryUGCs(Map theParms) throws UtopiaException {
		String tables = "base_medium,g_track";
        String fields = "base_medium.id,base_medium.kind,base_medium.mime,base_medium.name,base_medium.description,base_medium.creationdate,base_medium.extra";
        String where = null;
        String relations = "g_track,base_medium,medium";
        String postCond = null;        
        return QueryLogic.queryStoreReq(getOase(), tables, fields, where, relations, postCond);
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

	private Vector convertToRecordElms(Vector theElements) {
		for (int i = 0; i < theElements.size(); i++) {
			((JXElement) theElements.elementAt(i)).setTag("record");
		}
		return theElements;
	}

}
