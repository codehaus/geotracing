package org.walkandplay.server.logic;

import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import nl.justobjects.jox.parser.JXBuilderListener;
import org.geotracing.handler.QueryLogic;
import org.geotracing.handler.Track;
import org.keyworx.amuse.core.Protocol;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.Finder;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.api.Relater;
import org.keyworx.oase.store.record.FileFieldImpl;
import org.keyworx.utopia.core.data.UtopiaException;
import org.postgis.Point;
import org.walkandplay.server.util.Constants;

import java.io.File;
import java.util.Map;
import java.util.Vector;

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

				Record person = getPersonForLoginName(loginName);
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

				Record[] gameRounds = getOase().getRelater().getRelated(game, GAMEROUND_TABLE, null);
				result = createResponse(gameRounds);
			} else if ("q-gameplays".equals(aQueryName)) {
				String id = getParameter(theParms, "roundid", null);
				throwOnMissingParm("roundid", id);
				String tables = "utopia_person,wp_gameplay,wp_gameround";
				String fields = "wp_gameplay.id,wp_gameplay.state";
				String where = "wp_gameround.id = " + id;
				String relations = "utopia_person,wp_gameplay;wp_gameplay,wp_gameround;wp_gameround,utopia_person";
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

				Record person = getPersonForLoginName(loginName);
				String tables = "utopia_person,wp_gameplay,wp_gameround,wp_game";
				String fields = "wp_game.name AS name,wp_game.description AS description,wp_game.id AS gameid,wp_gameround.id AS  roundid,wp_gameplay.id AS gameplayid,wp_gameplay.state AS gameplaystate";
				String where = "utopia_person.id = " + person.getId();
				String relations = "utopia_person,wp_gameplay;wp_gameplay,wp_gameround;wp_gameround,wp_game";
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
        result.setAttr("cmd", aQueryName);
        return result;
	}

	/**
	 * ************* Data Queries ***********************
	 */
	static public Record getGamePlayForTrack(Track aTrack) throws OaseException, UtopiaException {
		try {
			return getOase().getRelater().getRelated(aTrack.getRecord(), GAMEPLAY_TABLE, null)[0];
		} catch (Throwable t) {
			// log.warn("Cannot find gameplay for aTrack.id=" + aTrack.getId(), t);
			return null;
		}
	}

	public static Record getGameRoundForGamePlay(Record aGamePlay) throws OaseException, UtopiaException {
		try {
			return getOase().getRelater().getRelated(aGamePlay, GAMEROUND_TABLE, null)[0];
		} catch (Throwable t) {
			log.warn("Error query getTaskResultsForGamePlay gamePlayId=" + aGamePlay.getId(), t);
			throw new UtopiaException("Error in getTaskResultsForGamePlay aGamePlayId=" + aGamePlay.getId(), t);
		}
	}

	public static JXElement getResultForTeam(String aTeamName, int aGamePlayId) throws UtopiaException {
		JXElement result = null;
		try {
			Finder finder = getOase().getFinder();
			Relater relater = getOase().getRelater();
			Record gamePlay = finder.read(aGamePlayId, GAMEPLAY_TABLE);

			// Gameplay attrs
			result = new JXElement(TAG_GAMEPLAY);
			result.setAttr(ATTR_TEAM, aTeamName);
			result.setAttr(SCORE_FIELD, gamePlay.getIntField(SCORE_FIELD));
			result.setAttr(STATE_FIELD, gamePlay.getStringField(STATE_FIELD));
			Record[] tracks = relater.getRelated(gamePlay, TRACK_TABLE, null);
			if (tracks.length > 0) {
				result.setAttr(ATTR_TRACKID, tracks[0].getId());
			}

			// Get all result records
			Record[] results = relater.getRelated(gamePlay, null, null);
			Record nextResult;
			for (int i = 0; i < results.length; i++) {
				nextResult = results[i];
				JXElement resultElm = null;

				// Determine result type from tablename and create element
				// for each result
				String tableName = nextResult.getTableName();
				if (tableName.equals(TASKRESULT_TABLE)) {
					// Add task result element
					resultElm = new JXElement(TAG_TASK_RESULT);
					resultElm.setAttr(TASK_RESULT_ID_FIELD, nextResult.getId());
					resultElm.setAttr(TIME_FIELD, nextResult.getLongField(TIME_FIELD));
					resultElm.setAttr(STATE_FIELD, nextResult.getStringField(STATE_FIELD));
					resultElm.setAttr(ANSWER_STATE_FIELD, nextResult.getStringField(ANSWER_STATE_FIELD));
					resultElm.setAttr(MEDIA_STATE_FIELD, nextResult.getStringField(MEDIA_STATE_FIELD));
					resultElm.setAttr(ANSWER_FIELD, nextResult.getStringField(ANSWER_FIELD));
					resultElm.setAttr(SCORE_FIELD, nextResult.getIntField(SCORE_FIELD));

					// If media submitted set medium id in medium result
					if (nextResult.getStringField(MEDIA_STATE_FIELD).equals(VAL_DONE)) {
						Record media[] = relater.getRelated(nextResult, MEDIUM_TABLE, null);
						if (media.length > 0) {
							resultElm.setAttr(MEDIUM_ID_FIELD, media[0].getId());
						} else {
							log.warn("Wrong number of media: (" + media.length + ") related to taskresult id=" + nextResult.getId());
						}
					}

					Record taskAndLocactionId = getTaskAndLocationIdForTaskResult(nextResult.getId());
					resultElm.setAttr(TASK_ID_FIELD, taskAndLocactionId.getIntField(TASK_ID_FIELD));
					resultElm.setAttr(LOCATION_ID_FIELD, taskAndLocactionId.getIntField(LOCATION_ID_FIELD));
				} else if (tableName.equals(MEDIUMRESULT_TABLE)) {
					// Add medium result element
					resultElm = new JXElement(TAG_MEDIUM_RESULT);
					resultElm.setAttr(MEDIUM_RESULT_ID_FIELD, nextResult.getId());
					resultElm.setAttr(STATE_FIELD, nextResult.getStringField(STATE_FIELD));
					resultElm.setAttr(TIME_FIELD, nextResult.getLongField(TIME_FIELD));
					Record mediumAndLocactionId = getMediumAndLocationIdForMediumResult(nextResult.getId());
					resultElm.setAttr(MEDIUM_ID_FIELD, mediumAndLocactionId.getIntField(MEDIUM_ID_FIELD));
					resultElm.setAttr(LOCATION_ID_FIELD, mediumAndLocactionId.getIntField(LOCATION_ID_FIELD));
				}

				// Add to total result
				if (resultElm != null) {
					result.addChild(resultElm);
				}
			}

		} catch (Throwable t) {
			log.warn("Error getResultForTeam gamplayid=" + aGamePlayId, t);
			throw new UtopiaException("Error getResultForTeam gamplayid=" + aGamePlayId, t);
		}

		return result;
	}

	public static Record getGameForGamePlay(int aGamePlayId) throws OaseException, UtopiaException {
		Record game = null;
		try {
			Record gamePlay = getOase().getFinder().read(aGamePlayId, GAMEPLAY_TABLE);
			Record schedule = getOase().getRelater().getRelated(gamePlay, GAMEROUND_TABLE, null)[0];
			game = getOase().getRelater().getRelated(schedule, GAME_TABLE, null)[0];
		} catch (Throwable t) {
			log.warn("Error query getGameForGamePlay gamePlayId=" + aGamePlayId, t);
		}

		return game;
	}

	public static Vector getGamePlayEvents(int aGamePlayId) throws UtopiaException {
		final Vector result = new Vector();
		try {
			Record gamePlay = getOase().getFinder().read(aGamePlayId, GAMEPLAY_TABLE);
			JXBuilder builder = new JXBuilder(
					new JXBuilderListener() {
						/**
						 * Called by XmlElementParser when it parsed and created an JXElement.
						 */
						public void element(JXElement e) {
							result.add(e);
						}

						/**
						 * Called when parser encounters an error.
						 */
						public void error(String msg) {
							Logging.getLog().warn("getGamePlayEvents() error: " + msg);
						}

						/**
						 * End of input stream is reached.
						 * <p/>
						 * This may occur when listening for multiple documents on a stream.
						 *
						 * @param message	 text message
						 * @param anException optional exception that caused the stream end
						 */
						public void endInputStream(String message, Throwable anException) {
							Logging.getLog().trace("getGamePlayEvents() EOF reached: " + message + " e=" + anException);
						}

					}
			);

			// Event data file
			FileFieldImpl fileField = (FileFieldImpl) gamePlay.getFileField(EVENTS_FIELD);
			File file = fileField.getStoredFile();

			// Only makes sense to parse a file with content
			if (file != null && file.exists() && file.length() > 0) {
				builder.setMultiDoc(true);
				builder.build(fileField.getFileInputStream());
			}
		} catch (Throwable t) {
			new UtopiaException("Error query getGamePlayEvents gamePlayId=" + aGamePlayId, t);
		}

		return result;
	}

	public static Record getRunningGamePlay(int aPersonId) throws UtopiaException {
		Record result = null;
		try {
			String tables = "utopia_person,wp_gameplay";
			String fields = "wp_gameplay.id,wp_gameplay.name,wp_gameplay.state,wp_gameplay.score";
			String where = "utopia_person.id = " + aPersonId + " AND wp_gameplay.state = '" + PLAY_STATE_RUNNING + "'";
			String relations = "utopia_person,wp_gameplay";
			String postCond = null;
			Record[] records = QueryLogic.queryStore(getOase(), tables, fields, where, relations, postCond);
			if (records.length == 1) {
				result = getOase().getFinder().read(records[0].getId(), GAMEPLAY_TABLE);
			} else if (records.length > 1) {
				throw new UtopiaException("More than one running gameplay for person=" + aPersonId);
			}
		} catch (Throwable t) {
			log.warn("Error get running gameplay for person=" + aPersonId, t);
		}

		return result;
	}

	public static Record[] getLocationsHitForGame(Point aPoint, int aGameId) throws UtopiaException {
		try {
			String distanceClause = "distance_sphere(GeomFromText('POINT(" + aPoint.x + " " + aPoint.y + ")',4326),point)";
			String tables = "g_location,wp_game";
			String fields = "g_location.id,g_location.name,g_location.type,g_location.point";
			String where = distanceClause + " < " + HIT_RADIUS_METERS + " AND wp_game.id = " + aGameId;
			String relations = "g_location,wp_game";
			String postCond = null;
			return QueryLogic.queryStore(tables, fields, where, relations, postCond);
		} catch (Throwable t) {
			log.warn("Error query locations for game=" + aGameId, t);
			throw new UtopiaException("Error in getLocationsHit game=" + aGameId, t);
		}
	}

	public static Record[] getMediaForGame(int aGameId) throws OaseException, UtopiaException {
		try {
			String tables = "wp_game,g_location,base_medium";
			String fields = "base_medium.id";
			String where = "wp_game.id = " + aGameId + " AND g_location.type = " + LOC_TYPE_GAME_MEDIUM;
			String relations = "wp_game,g_location;g_location,base_medium";
			String postCond = null;
			Record[] mediumIds = QueryLogic.queryStore(tables, fields, where, relations, postCond);

			Record[] media = new Record[mediumIds.length];
			for (int i = 0; i < media.length; i++) {
				media[i] = getOase().getFinder().read(mediumIds[i].getId(), MEDIUM_TABLE);
			}
			return media;

		} catch (Throwable t) {
			log.warn("Error query getMediaForGame gamePlayId=" + aGameId, t);
			throw new UtopiaException("Error in getMediaForGame aGamePlayId=" + aGameId, t);
		}
	}

	public static Record[] getMediaForGamePlay(Record aGamePlay) throws OaseException, UtopiaException {
		try {
			Record game = getGameForGamePlay(aGamePlay.getId());
			return getMediaForGame(game.getId());
		} catch (Throwable t) {
			log.warn("Error query getMediaForGamePlay gamePlayId=" + aGamePlay.getId(), t);
			throw new UtopiaException("Error in getMediaForGamePlay aGamePlayId=" + aGamePlay.getId(), t);
		}
	}

	public static Record[] getMediaResultsForGamePlay(int aGamePlayId) throws OaseException, UtopiaException {
		try {
			Record gamePlay = getOase().getFinder().read(aGamePlayId, GAMEPLAY_TABLE);
			return getOase().getRelater().getRelated(gamePlay, MEDIUMRESULT_TABLE, null);
		} catch (Throwable t) {
			log.warn("Error query getMediaResultsForGamePlay gamePlayId=" + aGamePlayId, t);
			throw new UtopiaException("Error in getMediaResultsForGamePlay aGamePlayId=" + aGamePlayId, t);
		}
	}

	public static Record[] getMediaResultsForGamePlay(Record aGamePlay) throws OaseException, UtopiaException {
		try {
			return getOase().getRelater().getRelated(aGamePlay, MEDIUMRESULT_TABLE, null);
		} catch (Throwable t) {
			log.warn("Error query getMediaResultsForGamePlay gamePlayId=" + aGamePlay.getId(), t);
			throw new UtopiaException("Error in getMediaResultsForGamePlay aGamePlayId=" + aGamePlay.getId(), t);
		}
	}

	public static Record getMediumAndLocationIdForMediumResult(int aMediumResultId) throws OaseException, UtopiaException {
		try {
			String tables = "g_location,base_medium,wp_mediumresult";
			String fields = "g_location.id AS locationid,base_medium.id AS mediumid";
			String where = "wp_mediumresult.id = " + aMediumResultId;
			String relations = "wp_mediumresult,base_medium;g_location,base_medium";
			String postCond = null;
			return QueryLogic.queryStore(tables, fields, where, relations, postCond)[0];
		} catch (Throwable t) {
			log.warn("Error query getMediumAndLocationIdForMediumResult aMediumResultId=" + aMediumResultId, t);
			throw new UtopiaException("Error in getMediumAndLocationIdForMediumResult aMediumResultId=" + aMediumResultId, t);
		}
	}

	public static Record getMediumResultForMedium(int aMediumId, int aGamePlayId) throws UtopiaException {
		Record result = null;
		try {
			String tables = "wp_gameplay,base_medium,wp_mediumresult";
			String fields = "wp_mediumresult.id";
			String where = "wp_gameplay.id = " + aGamePlayId + " AND base_medium.id = " + aMediumId;
			String relations = "wp_gameplay,wp_mediumresult;wp_mediumresult,base_medium";
			String postCond = null;
			Record[] records = QueryLogic.queryStore(tables, fields, where, relations, postCond);
			if (records.length == 1) {
				result = getOase().getFinder().read(records[0].getId(), MEDIUMRESULT_TABLE);
			} else if (records.length > 1) {
				throw new UtopiaException("More than mediumresult for aMediumId=" + aGamePlayId);
			}
		} catch (Throwable t) {
			log.warn("Error query getMediumResultForMedium mediumId=" + aMediumId + " gamplayid=" + aGamePlayId, t);
		}

		return result;
	}

	public static Record getTaskHitForGame(Point aPoint, int aGameId) throws UtopiaException {
		Record result = null;
		try {
			String distanceClause = "distance_sphere(GeomFromText('POINT(" + aPoint.x + " " + aPoint.y + ")',4326),point)";
			String tables = "g_location,wp_game,wp_task";
			String fields = "wp_task.id";
			String where = distanceClause + " < " + HIT_RADIUS_METERS + " AND wp_game.id = " + aGameId + " AND g_location.type = " + LOC_TYPE_GAME_TASK;

			String relations = "g_location,wp_game;g_location,wp_task";
			String postCond = null;
			Record[] tasksHit = QueryLogic.queryStore(tables, fields, where, relations, postCond);
			if (tasksHit.length == 1) {
				result = getOase().getFinder().read(tasksHit[0].getId(), TASK_TABLE);
			} else if (tasksHit.length > 1) {
				log.warn("More than task hit for aGameId=" + aGameId + " pt=" + aPoint);
			}
		} catch (Throwable t) {
			log.warn("Error getTaskHitForGame for game=" + aGameId, t);
			throw new UtopiaException("Error in getTaskHitForGame game=" + aGameId, t);
		}
		return result;
	}

	public static Record[] getTasksForGame(int aGameId) throws OaseException, UtopiaException {
		try {
			String tables = "wp_game,g_location,wp_task";
			String fields = "wp_task.id";
			String where = "wp_game.id = " + aGameId + " AND g_location.type = " + LOC_TYPE_GAME_TASK;
			String relations = "wp_game,g_location;g_location,wp_task";
			String postCond = null;
			Record[] taskIds = QueryLogic.queryStore(tables, fields, where, relations, postCond);

			Record[] tasks = new Record[taskIds.length];
			for (int i = 0; i < tasks.length; i++) {
				tasks[i] = getOase().getFinder().read(taskIds[i].getId(), TASK_TABLE);
			}
			return tasks;

		} catch (Throwable t) {
			log.warn("Error query getTasksForGame gamePlayId=" + aGameId, t);
			throw new UtopiaException("Error in getTasksForGame aGamePlayId=" + aGameId, t);
		}
	}

	public static Record getTaskAndLocationIdForTaskResult(int aTaskResultId) throws OaseException, UtopiaException {
		try {
			String tables = "g_location,wp_task,wp_taskresult";
			String fields = "g_location.id AS locationid,wp_task.id AS taskid";
			String where = "wp_taskresult.id = " + aTaskResultId;
			String relations = "wp_taskresult,wp_task;g_location,wp_task";
			String postCond = null;
			return QueryLogic.queryStore(tables, fields, where, relations, postCond)[0];
		} catch (Throwable t) {
			log.warn("Error query getTaskAndLocationIdForTaskResult aTaskResultId=" + aTaskResultId, t);
			throw new UtopiaException("Error in getTaskAndLocationIdForTaskResult aTaskResultId=" + aTaskResultId, t);
		}
	}

	public static Record[] getTasksForGamePlay(int aGamePlayId) throws OaseException, UtopiaException {
		try {
			Record game = getGameForGamePlay(aGamePlayId);
			return getTasksForGame(game.getId());
		} catch (Throwable t) {
			log.warn("Error query getTasksForGamePlay gamePlayId=" + aGamePlayId, t);
			throw new UtopiaException("Error in getTasksForGamePlay aGamePlayId=" + aGamePlayId, t);
		}
	}

	public static Record[] getTaskResultsForGamePlay(int aGamePlayId) throws OaseException, UtopiaException {
		try {
			Record gamePlay = getOase().getFinder().read(aGamePlayId, GAMEPLAY_TABLE);
			return getOase().getRelater().getRelated(gamePlay, TASKRESULT_TABLE, null);
		} catch (Throwable t) {
			log.warn("Error query getTaskResultsForGamePlay gamePlayId=" + aGamePlayId, t);
			throw new UtopiaException("Error in getTaskResultsForGamePlay aGamePlayId=" + aGamePlayId, t);
		}
	}


	public static Record[] getTaskResultsForGamePlay(Record aGamePlay) throws OaseException, UtopiaException {
		try {
			return getOase().getRelater().getRelated(aGamePlay, TASKRESULT_TABLE, null);
		} catch (Throwable t) {
			log.warn("Error query getTaskResultsForGamePlay gamePlayId=" + aGamePlay.getId(), t);
			throw new UtopiaException("Error in getTaskResultsForGamePlay aGamePlayId=" + aGamePlay.getId(), t);
		}
	}

	public static Record getTaskResultForTask(int aTaskId, int aGamePlayId) throws UtopiaException {
		Record result = null;
		try {
			String tables = "wp_gameplay,wp_task,wp_taskresult";
			String fields = "wp_taskresult.id";
			String where = "wp_gameplay.id = " + aGamePlayId + " AND wp_task.id = " + aTaskId;
			String relations = "wp_gameplay,wp_taskresult;wp_taskresult,wp_task";
			String postCond = null;
			Record[] records = QueryLogic.queryStore(tables, fields, where, relations, postCond);
			if (records.length == 1) {
				result = getOase().getFinder().read(records[0].getId(), TASKRESULT_TABLE);
			} else if (records.length > 1) {
				throw new UtopiaException("More than taskresult for gamplayid=" + aGamePlayId);
			}
		} catch (Throwable t) {
			log.warn("Error query TaskResultForTask taskId=" + aTaskId + " gamplayid=" + aGamePlayId, t);
		}

		return result;
	}

	/*

	public JXElement playLocationDbgReq(UtopiaRequest anUtopiaReq) throws OaseException, UtopiaException {
		JXElement response = createResponse(PLAY_LOCATION_SERVICE);

		if (Rand.randomInt(0, 2) == 1) {
			JXElement hit = new JXElement(TAG_TASK_HIT);
			hit.setAttr(ID_FIELD, 22560);
			response.addChild(hit);
		}

		if (Rand.randomInt(0, 4) == 1 && !response.hasChildren()) {
			JXElement hit = new JXElement(TAG_MEDIUM_HIT);
			hit.setAttr(ID_FIELD, 22629);
			response.addChild(hit);
		}

		if (Rand.randomInt(0, 4) == 1 && !response.hasChildren()) {
			JXElement hit = new JXElement(TAG_MEDIUM_HIT);
			hit.setAttr(ID_FIELD, 4497);
			response.addChild(hit);
		}

		if (Rand.randomInt(0, 4) == 1 && !response.hasChildren()) {
			JXElement hit = new JXElement(TAG_MEDIUM_HIT);
			hit.setAttr(ID_FIELD, 26527);
			response.addChild(hit);
		}

		return response;
	}

	*/
}
