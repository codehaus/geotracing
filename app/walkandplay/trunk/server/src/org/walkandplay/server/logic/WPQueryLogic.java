package org.walkandplay.server.logic;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.QueryLogic;
import org.keyworx.amuse.core.Protocol;
import org.keyworx.oase.api.Finder;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.api.Relater;
import org.postgis.Point;
import org.walkandplay.server.util.Constants;

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
			} else if ("q-games".equals(aQueryName)) {
				result = createResponse(getOase().getFinder().readAll(GAME_TABLE));
			} else if ("q-game".equals(aQueryName)) {
				String id = getParameter(theParms, PAR_ID, null);
				throwOnMissingParm(PAR_ID, id);

				Finder finder = getOase().getFinder();

				Record game = finder.read(Integer.parseInt(id), GAME_TABLE);
				if (game == null) {
					throw new IllegalArgumentException("Cannot find game with id=" + id);
				}

				result = Protocol.createResponse(QueryLogic.QUERY_STORE_SERVICE);
				result.addChild(game.toXML());
			} else if ("q-gamerounds".equals(aQueryName)) {
				String id = getParameter(theParms, "gameid", null);
				throwOnMissingParm("gameid", id);
				Finder finder = getOase().getFinder();

				Record game = finder.read(Integer.parseInt(id), GAME_TABLE);
				if (game == null) {
					throw new IllegalArgumentException("Cannot find game with id=" + id);
				}

				Record[] gameRounds = getOase().getRelater().getRelated(game, SCHEDULE_TABLE, null);
				result = createResponse(gameRounds);
			} else if ("q-gameplays".equals(aQueryName)) {
				String id = getParameter(theParms, "roundid", null);
				throwOnMissingParm("roundid", id);
				String tables = "utopia_person,wp_gameplay,wp_schedule";
				String fields = "wp_gameplay.id,wp_gameplay.state";
				String where = "wp_schedule.id = " + id;
				String relations = "utopia_person,wp_gameplay;wp_gameplay,wp_schedule;wp_schedule,utopia_person";
				String postCond = null;
				result = QueryLogic.queryStoreReq(getOase(), tables, fields, where, relations, postCond);
				addUserAttrs(result, GAMEPLAY_TABLE);
			} else if ("q-game-locations".equals(aQueryName)) {
				// All locations within game
				// Game id
				String id = getParameter(theParms, PAR_ID, null);
				throwOnMissingParm(PAR_ID, id);

				Finder finder = getOase().getFinder();
				Relater relater = getOase().getRelater();

				Record game = finder.read(Integer.parseInt(id), GAME_TABLE);
				if (game == null) {
					throw new IllegalArgumentException("Cannot find game with id=" + id);
				}

				Record[] locations = relater.getRelated(game, LOCATION_TABLE, null);
				result = Protocol.createResponse(QueryLogic.QUERY_STORE_SERVICE);
				Record locationItem, locationItems[];
				for (int i = 0; i < locations.length; i++) {
					JXElement rec = new JXElement("record");
					switch (locations[i].getIntField(TYPE_FIELD)) {

						case LOC_TYPE_GAME_TASK:
							locationItems = relater.getRelated(locations[i], TASK_TABLE, null);
							if (locationItems.length != 1) {
								log.warn("No task found for location id=" + locations[i].getId() + " (ignoring)");
								continue;
							}
							locationItem = locationItems[0];
							rec.setChildText(SCORE_FIELD, locationItem.getIntField(SCORE_FIELD) + "");
							rec.setChildText(TYPE_FIELD, "task");
							break;

						case LOC_TYPE_GAME_MEDIUM:
							locationItems = relater.getRelated(locations[i], MEDIUM_TABLE, null);
							if (locationItems.length != 1) {
								log.warn("No medium found for location id=" + locations[i].getId() + " (ignoring)");
								continue;
							}
							locationItem = locationItems[0];
							rec.setChildText(TYPE_FIELD, "medium");
							break;
						default:
							continue;
					}

					rec.setChildText(ID_FIELD, locationItem.getId() + "");
					rec.setChildText(NAME_FIELD, locationItem.getStringField(NAME_FIELD));
					Point point = new Point(locations[i].getObjectField(POINT_FIELD).toString());
					rec.setChildText(LON_FIELD, point.x + "");
					rec.setChildText(LAT_FIELD, point.y + "");
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

			} else if ("q-play-status-by-user".equals(aQueryName)) {
				String loginName = getParameter(theParms, PAR_USER_NAME, null);
				throwOnMissingParm(PAR_USER_NAME, loginName);

				Record person = getPersonForLoginName(getOase(), loginName);
				String tables = "utopia_person,wp_gameplay,wp_schedule,wp_game";
				String fields = "wp_game.name AS name,wp_game.description AS description,wp_game.id AS gameid,wp_schedule.id AS  roundid,wp_gameplay.id AS gameplayid,wp_gameplay.state AS gameplaystate";
				String where = "utopia_person.id = " + person.getId();
				String relations = "utopia_person,wp_gameplay;wp_gameplay,wp_schedule;wp_schedule,wp_game";
				String postCond = null;
				result = QueryLogic.queryStoreReq(getOase(), tables, fields, where, relations, postCond);

			} else if ("q-scores".equals(aQueryName)) {
				result = Protocol.createResponse(QueryLogic.QUERY_STORE_SERVICE);
				// gameplay id
				String gameId = (String) theParms.get("gameid");

				JXElement s1 = new JXElement("record");
				s1.setChildText("team", "red1");
				s1.setChildText("points", "5");
				result.addChild(s1);

				JXElement s2 = new JXElement("record");
				s2.setChildText("team", "blue1");
				s2.setChildText("points", "10");
				result.addChild(s2);

			} else if ("q-task".equals(aQueryName)) {
				String id = getParameter(theParms, PAR_ID, null);
				throwOnMissingParm(PAR_ID, id);

				String tables = "wp_task,base_medium";
				String fields = "wp_task.id,base_medium.id AS mediumid,base_medium.kind AS mediumtype,wp_task.name,wp_task.description,wp_task.answer,wp_task.score";
				String where = "wp_task.id = " + id;
				String relations = "wp_task,base_medium";
				String postCond = null;
				result = QueryLogic.queryStoreReq(getOase(), tables, fields, where, relations, postCond);
			} else {
				// Query not handled by us: let superclass do query and any errors if query non-existing
				result = super.doQuery(aQueryName, theParms);
			}
		} catch (IllegalArgumentException iae) {
			result = Protocol.createNegativeResponse(QUERY_STORE_SERVICE, Protocol.__4002_Required_attribute_missing, iae.getMessage());
			log.warn("IllegalArgumentException during query", iae);
		} catch (Throwable t) {
			result = Protocol.createNegativeResponse(QUERY_STORE_SERVICE, Protocol.__4005_Unexpected_error, t.getMessage());
			log.error("Unexpected Error during query", t);
		}
		return result;
	}

}
