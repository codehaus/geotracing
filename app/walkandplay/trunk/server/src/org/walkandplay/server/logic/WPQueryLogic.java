package org.walkandplay.server.logic;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.QueryLogic;
import org.geotracing.handler.Location;
import org.geotracing.gis.PostGISUtil;
import org.keyworx.amuse.core.Protocol;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.api.Relater;
import org.keyworx.oase.api.Finder;
import org.walkandplay.server.util.Constants;
import org.postgis.Point;

import java.util.Map;

public class WPQueryLogic extends QueryLogic implements Constants {

	public JXElement doQuery(String aQueryName, Map theParms) {
		JXElement result;
		long t1, t2;

		// WnP specific queries: alphabetalically
		try {

			if ("q-games-by-user".equals(aQueryName)) {
				// Games created by user
				String loginName = getParameter(theParms, PAR_USER_NAME, null);
				throwOnMissingParm(PAR_USER_NAME, loginName);

				Record person = getPersonForLoginName(getOase(), loginName);
				if (person == null) {
					throw new IllegalArgumentException("No person found for loginname=" + loginName);
				}

				Record[] records = getOase().getRelater().getRelated(person, GAME_TABLE, null);

				result = Protocol.createResponse(QueryLogic.QUERY_STORE_SERVICE);
				for (int i = 0; i < records.length; i++) {
					result.addChild(records[i].toXML());
				}
			} else if ("q-game-locations".equals(aQueryName)) {
				// All locations within game
				// Game id
				String id = getParameter(theParms, PAR_ID, null);
				throwOnMissingParm(PAR_ID, id);

				Finder finder = getOase().getFinder();
				Relater relater = getOase().getRelater();

				Record game = finder.read(Integer.parseInt(id), GAME_TABLE);
				Record[] locations = relater.getRelated(game, LOCATION_TABLE, null);
				result = Protocol.createResponse(QueryLogic.QUERY_STORE_SERVICE);
				Record locationItem;
				for (int i = 0; i < locations.length; i++) {
					JXElement rec = new JXElement("record");
					switch (locations[i].getIntField(TYPE_FIELD)) {

						case LOC_TYPE_GAME_TASK:
							locationItem = relater.getRelated(locations[i], TASK_TABLE, null)[0];
							rec.setChildText(TYPE_FIELD, "task");
							break;

						case LOC_TYPE_GAME_MEDIUM:
							locationItem = relater.getRelated(locations[i], MEDIUM_TABLE, null)[0];
							rec.setChildText(TYPE_FIELD, "medium");
							break;
						default:
							continue;
					}
					rec.setChildText(ID_FIELD, locationItem.getId() + "");
					rec.setChildText(NAME_FIELD, locationItem.getStringField(NAME_FIELD));
					Point point = new Point(locations[i].getObjectField(POINT_FIELD).toString());
					rec.setChildText(LAT_FIELD, point.x + "");
					rec.setChildText(LON_FIELD, point.y + "");
					result.addChild(rec);

				}

			} else if ("q-medium".equals(aQueryName)) {
				String id = getParameter(theParms, PAR_ID, null);
				throwOnMissingParm(PAR_ID, id);

				String tables = "base_medium";
				String fields = "id,name,description,kind AS type";
				String where = "id = " + id;
				String relations = null;
				String postCond = null;
				result = QueryLogic.queryStoreReq(getOase(), tables, fields, where, relations, postCond);

			} else if ("q-schedule-by-user".equals(aQueryName)) {
				String loginName = getParameter(theParms, PAR_USER_NAME, null);
				throwOnMissingParm(PAR_USER_NAME, loginName);

				Record person = getPersonForLoginName(getOase(), loginName);
				String tables = "utopia_person,wp_schedule,wp_game";
				String fields = "wp_schedule.id,wp_game.id AS gameid,wp_game.name,wp_game.description";
				String where = "utopia_person.id = " + person.getId();
				String relations = "utopia_person,wp_schedule,player;wp_schedule,wp_game";
				String postCond = null;
				result = QueryLogic.queryStoreReq(getOase(), tables, fields, where, relations, postCond);

			} else if ("q-scores".equals(aQueryName)) {
				result = Protocol.createResponse(QueryLogic.QUERY_STORE_SERVICE);
				// gameplay id
				String gameId = (String) theParms.get("gameid");

				JXElement s1 = new JXElement("record");
				s1.setChildText("team", "red1");
				s1.setChildText("points", "60");
				result.addChild(s1);
				JXElement s2 = new JXElement("record");
				s2.setChildText("team", "blue1");
				s2.setChildText("points", "80");
				result.addChild(s2);

			} else if ("q-task".equals(aQueryName)) {
				String id = getParameter(theParms, PAR_ID, null);
				throwOnMissingParm(PAR_ID, id);

				String tables = "wp_task,base_medium";
				String fields = "wp_task.id,base_medium.id AS mediumid,wp_task.name,wp_task.description";
				String where = "wp_task.id = " + id;
				String relations = "wp_task,base_medium";
				String postCond = null;
				result = QueryLogic.queryStoreReq(getOase(), tables, fields, where, relations, postCond);
			} else {
				// Query not handled by us: let superclass do query and any errors if query non-existing
				result = super.doQuery(aQueryName, theParms);
			}
		} catch (IllegalArgumentException iae) {
			result = new JXElement(TAG_ERROR);
			result.setText("Error in parameter: " + iae.getMessage());
			log.error("Unexpected Error during query", iae);
		} catch (Throwable t) {
			result = new JXElement(TAG_ERROR);
			result.setText("Unexpected Error during query " + t);
			log.error("Unexpected Error during query", t);
		}
		return result;
	}

}
