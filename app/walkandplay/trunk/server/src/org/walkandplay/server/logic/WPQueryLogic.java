package org.walkandplay.server.logic;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.QueryLogic;
import org.geotracing.handler.Location;
import org.keyworx.amuse.core.Protocol;
import org.keyworx.oase.api.Record;
import org.walkandplay.server.util.Constants;
import org.postgis.Point;

import java.util.Map;

public class WPQueryLogic extends QueryLogic implements Constants {

	public JXElement doQuery(String aQueryName, Map theParms) {
		JXElement result;
		long t1, t2;

		try {
			if ("q-games-by-user".equals(aQueryName)) {
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
			}else if("q-task".equals(aQueryName)) {
                String taskId = (String)theParms.get("id");
                JXElement rsp = new JXElement("query-store-rsp");

                JXElement task = new JXElement("task");
                rsp.addChild(task);
                task.setAttr("id", taskId);
                JXElement name = new JXElement("name");
                name.setText("Fiets opdracht");
                JXElement description = new JXElement("description");
                description.setText("Haal een fiets uit de sloot");
                JXElement mediumid = new JXElement("mediumid");
                mediumid.setText("10");
                task.addChild(name);
                task.addChild(description);
                task.addChild(mediumid);

                return rsp;
            }else if("q-medium".equals(aQueryName)) {
                String mediumId = (String)theParms.get("id");
                JXElement rsp = new JXElement("query-store-rsp");

                JXElement task = new JXElement("medium");
                rsp.addChild(task);
                task.setAttr("id", mediumId);
                JXElement name = new JXElement("name");
                name.setText("Fiets plaatje");
                JXElement type = new JXElement("type");
                type.setText("image");
                task.addChild(name);
                task.addChild(type);

                return rsp;
		    } else if ("q-game-locations".equals(aQueryName)) {
				String idStr = getParameter(theParms, PAR_ID, null);
				throwOnMissingParm(PAR_ID, idStr);
				int id = Integer.parseInt(idStr);
				Record game = getOase().getFinder().read(id, GAME_TABLE);
				if (game == null) {
					throw new IllegalArgumentException("Cannot find game for id=" + id);
				}
				
				Record[] locations = getOase().getRelater().getRelated(game, LOCATION_TABLE, null);

				result = Protocol.createResponse(QueryLogic.QUERY_STORE_SERVICE);
				JXElement record;
				for (int i = 0; i < locations.length; i++) {
					record = new JXElement("record");
					int locType = locations[i].getIntField(Location.FIELD_TYPE);
					String itemTable;
					String itemType;
					Record item;
					switch (locType) {
						case LOC_TYPE_GAME_TASK:
							itemType = "task";
							itemTable = GAME_TABLE;
							break;
						case LOC_TYPE_GAME_MEDIUM:
							itemType = "medium";
							itemTable = MEDIUM_TABLE;
							break;
						default:
							throw new IllegalArgumentException("Illegal location type " + locType);
					}

					// Set id/name of related record (medium or task)
					item = getOase().getRelater().getRelated(locations[i], itemTable, null)[0];
					record.setChildText(ID_FIELD, item.getId()+"");
					record.setChildText(TYPE_FIELD, itemType);
					record.setChildText(NAME_FIELD, item.getStringField(NAME_FIELD));
					record.setChildText(LON_FIELD, locations[i].getRealField(LON_FIELD) + "");
					record.setChildText(LAT_FIELD, locations[i].getRealField(LAT_FIELD) + "");

					result.addChild(record);
				}


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
