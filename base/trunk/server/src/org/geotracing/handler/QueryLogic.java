// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.handler;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.gis.GISCalc;
import org.geotracing.gis.PostGISUtil;
import org.keyworx.amuse.core.Amuse;
import org.keyworx.amuse.core.Protocol;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Sys;
import org.keyworx.oase.api.Finder;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.api.Relater;
import org.keyworx.plugin.tagging.logic.TagLogic;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.postgis.PGgeometryLW;
import org.postgis.Point;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Handles queries.
 * <p/>
 * Allows doing any query.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class QueryLogic {
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd'.'MM'.'yy-HH:mm:ss");

	public final static String QUERY_STORE_SERVICE = "query-store";
	public final static String ATTR_TABLES = "tables";
	public final static String ATTR_FIELDS = "fields";
	public final static String ATTR_RELATIONS = "relations";
	public final static String ATTR_POST_COND = "postcond";

	/**
	 * Clause template for relation queries.
	 */
	public static String IS_RELATED =
			"( REL_ALIAS.rec1 = TABLE1.id AND REL_ALIAS.rec2 = TABLE2.id ) ";


	/**
	 * Clause template for tagged relation queries.
	 */
	public static final String IS_RELATED_WITH_TAG =
			"(" + IS_RELATED +
					" AND REL_ALIAS.tag = 'TAG' )";

	/**
	 * To separate multiple relation specs.
	 */
	public static final String REL_SPEC_SEPARATOR = ";";

	public static final String REL_ALIAS_BASE = "rel";

	public static final String CMD_QUERY_STORE = "q-store";
	public static final String CMD_QUERY_ACTIVE_TRACKS = "q-active-tracks";
	public static final String CMD_QUERY_AROUND = "q-around";
	public static final String CMD_QUERY_AROUND2 = "q-around2";
	public static final String CMD_QUERY_ALL_TRACKS = "q-all-tracks";
	public static final String CMD_QUERY_BY_EXAMPLE = "q-by-example";
	public static final String CMD_QUERY_COMMENTS_FOR_TARGET = "q-comments-for-target";
	public static final String CMD_QUERY_COMMENTS_FOR_TARGET_PERSON = "q-comments-for-target-person";
	public static final String CMD_QUERY_COMMENTERS_FOR_TARGET = "q-commenters-for-target";
	public static final String CMD_QUERY_COMMENT_COUNT_FOR_TARGET = "q-comment-count-for-target";
	public static final String CMD_QUERY_ROW_COUNT = "q-row-count";
	public static final String CMD_QUERY_RECENT_TRACKS = "q-recent-tracks";
	public static final String CMD_QUERY_TRACKS_BY_USER = "q-tracks-by-user";
	public static final String CMD_QUERY_ALL_USERS = "q-all-users";
	public static final String CMD_QUERY_RANDOM_TRACK = "q-random-track";
	public static final String CMD_QUERY_LOCATIVE_MEDIA = "q-locative-media";
	public static final String CMD_QUERY_RECENT_MEDIA = "q-recent-media";
	public static final String CMD_QUERY_MEDIA_BY_USER = "q-media-by-user";
	public static final String CMD_QUERY_USER_IMAGE = "q-user-image";
	public static final String CMD_QUERY_USER_INFO = "q-user-info";
	public static final String CMD_QUERY_USER_BY_NAME = "q-user-by-name";
	public static final String CMD_QUERY_MEDIUM_INFO = "q-medium-info";
	public static final String CMD_QUERY_FEATURE_INFO = "q-feature-info";
	public static final String CMD_QUERY_TAGS = "q-tags";
	public static final String CMD_QUERY_TAGGED = "q-tagged";

	public static final String PAR_ID = "id";
	public static final String PAR_CMD = "cmd";
	public static final String PAR_BBOX = "bbox";
	public static final String PAR_EXCLUDE_OWNER = "excludeowner";
	public static final String PAR_LOC = "loc";
	public static final String PAR_MAX = "max";
	public static final String PAR_RADIUS = "radius";
	public static final String PAR_USER_NAME = "user";
	public static final String PAR_TABLE_NAME = "table";
	public static final String PAR_OWNER_INFO = "ownerinfo";
	public static final String PAR_TARGET = CommentLogic.FIELD_TARGET;
	public static final String PAR_TARGET_INFO = "targetinfo";
	public static final String PAR_TARGET_PERSON = CommentLogic.FIELD_TARGET_PERSON;
	public static final String PAR_ITEMS = "items";
	public static final String PAR_OFFSET = "offset";
	public static final String PAR_OUTPUT = "output";
	public static final String PAR_ROW_COUNT = "rowcount";
	public static final String PAR_STATE = "state";
	public static final String PAR_TAGGERS = "taggers";
	public static final String PAR_TAGS = "tags";
	public static final String PAR_TYPES = "types";
	public static final String PAR_TYPE = "type";
	public static final String TAG_ERROR = "error";

	public final static String OUTPUT_JSON = "json";
	public final static String OUTPUT_XML = "xml";
	public static String DB_RANDOM_FUN = "RANDOM()";

	private static Oase oase;
	public static Log log = Logging.getLog("QueryLogic");

	private static QueryLogic instance;

	private QueryLogic() {
	}

	public static QueryLogic create(String aClassName) throws Exception {
		// Get global Oase (DB) session.
		try {
			// Use one Oase session
			if (oase == null) {
				// First time: create and save in app context
				String oaseContextId = Amuse.server.getPortal().getId();
				oase = Oase.createOaseSession(oaseContextId);

			}
		} catch (Exception e) {
			log.error("error creating Oase session", e);
			throw e;
		}

		instance =  (QueryLogic) Class.forName(aClassName).newInstance();
		return instance;
	}

	public static QueryLogic getInstance()  {
		return instance;
	}


	public static Oase getOase()  {
		return oase;
	}

	/**
	 * Performs command and returns XML query-store-rsp.
	 */
	public JXElement doQuery(String aQueryName, Map theParms) {
		JXElement result;
		long t1, t2;
		try {

			/*		if (command.equals(CMD_QUERY_STORE)) {
							String tables = getParameter(request, "tables", null);
							String fields = getParameter(request, "fields", null);
							String where = getParameter(request, "where", null);
							String relations = getParameter(request, "rels", null);
							String postCond = getParameter(request, "postcond", null);
							result = QueryLogic.queryStoreReq(oase, tables, fields, where, relations, postCond);
						} else      */

			if (aQueryName.equals(CMD_QUERY_BY_EXAMPLE)) {
				// Query for single table by providing partial example record

				// Table is required
				String table = getParameter(theParms, PAR_TABLE_NAME, null);
				throwOnMissingParm(PAR_TABLE_NAME, table);

				// Create example Record from other parms
				Record exampleRecord = oase.getFinder().createExampleRecord(table);

				// Fill in all field name/values (except table)
				Set fields = theParms.keySet();
				String name, value;
				Iterator iter = fields.iterator();
				while (iter.hasNext()) {
					name = (String) iter.next();
					if (name.equals("table") || name.equals("cmd") || name.equals("t") || name.equals("output")) {
						// Skip
						continue;
					}
					value = getParameter(theParms, name, null);
					if (value != null) {
						exampleRecord.setField(name, value);
					}
				}

				result = createResponse(oase.getFinder().queryTable(exampleRecord));

			} else if (aQueryName.equals(CMD_QUERY_AROUND)) {
				// Look for items around a point with a radius
				String locString = getParameter(theParms, PAR_LOC, null);
				throwOnMissingParm(PAR_LOC, locString);
				String radiusString = getParameter(theParms, PAR_RADIUS, null);
				throwOnMissingParm(PAR_RADIUS, radiusString);


				String limitParm = getParameter(theParms, PAR_MAX, "51");
				String typesParm = getParameter(theParms, PAR_TYPES, null);
				String meParm = getParameter(theParms, "me", null);

				// WHERE clause
				// Optional object type
				// String type = getParameter(request, "type", null);
				// See http://www.petefreitag.com/item/466.cfm
				// LAST N: select * from table where key > (select max(key) - n from table)
				String where = "";
				if (typesParm != null) {
					String[] types = typesParm.split(",");
					for (int i = 0; i < types.length; i++) {
						if (i == 0) {
							where += " AND ( ";
						}
						if (i > 0) {
							where += " OR ";
						}
						if (types[i].equals("medium")) {
							where = where + "g_location.type = 1";
						}

						if (types[i].equals("track")) {
							where = where + "g_location.type = 2";
						}

						if (types[i].equals("user")) {
							where = where + "g_location.type = 3";
						}

						// Brackets
						if (i == types.length - 1) {
							where += " ) ";
						}
					}

				}

				// Calculate BBOX around point
				double radius = Double.parseDouble(radiusString);
				String[] lonLatString = locString.split(",");
				Point location = PostGISUtil.createPoint(lonLatString[0], lonLatString[1]);
				double metersPerDegLon = GISCalc.metersPerDegreeLon(location.y, location.x);
				double metersPerDegLat = GISCalc.metersPerDegreeLon(location.y, location.x);


				Point locSW = new Point(location.x - (radius / metersPerDegLon), location.y - (radius / metersPerDegLat));
				Point locNE = new Point(location.x + (radius / metersPerDegLon), location.y + (radius / metersPerDegLat));

				// Limit
				int limit = Integer.parseInt(limitParm);

				// Hard limit: don't blow up our DB (for now)
				if (limit > 51) {
					limit = 51;
				}
				String postCond = " LIMIT " + limit;

				// log.info("where=[" + where + "] postCond=[" + postCond +"]");
				String box = locSW.x + " " + locSW.y + "," + locNE.x + " " + locNE.y;
				String distanceClause = "distance_sphere(GeomFromText('POINT(" + location.x + " " + location.y + ")',4326),point)";
				// SELECT * FROM g_location WHERE distance_sphere(GeomFromText('POINT(5.1 4.1)',4326),point) < 100000
				String queryNearest =
						"SELECT *," + distanceClause + " AS distance FROM g_location where point && SetSRID('BOX3D(" + box + ")'::box3d,4326) " + where + " order by " + distanceClause + " asc limit " + limit;

				// String query= "SELECT * from g_location where point && SetSRID('BOX3D(" + box + ")'::box3d,4326) " + postCond;
				// String query= "SELECT * from g_location where point && SetSRID('BOX3D(" + box + ")'::box3d,4326) " + postCond;
				// Record[] records = QueryLogic.queryStore(oase, tables, fields, where, relations, postCond);
				t1 = Sys.now();
				Record[] records = oase.getFinder().freeQuery(queryNearest);
				t2 = Sys.now();
				log.info("spatial querytime=" + (t2 - t1));
				result = Protocol.createResponse(QueryLogic.QUERY_STORE_SERVICE);
				JXElement nextElm;
				Record nextRecord;
				Point nextPoint;
				double nextDistance;
				Relater relater = oase.getRelater();
				Record[] relatedRecs;
				for (int i = 0; i < records.length; i++) {
					nextRecord = records[i];
					nextPoint = (Point) ((PGgeometryLW) nextRecord.getObjectField("point")).getGeometry();
					nextDistance = nextRecord.getRealField("distance");
					if (nextDistance > radius) {
						continue;
					}
					nextDistance = Math.round(nextDistance);

					relatedRecs = relater.getRelated(nextRecord);
					if (relatedRecs.length != 1) {
						continue;
					}

					String table = relatedRecs[0].getTableName();
					String id = relatedRecs[0].getIdString();
					String name = "unknown";
					String type = "unknown";

					// Pperson either directly related or via related (e.g. medium)
					Record person;
					if (table.equals("utopia_person")) {
						person = relatedRecs[0];
					} else {
						person = relater.getRelated(relatedRecs[0], "utopia_person", null)[0];
					}

					String loginName = getLoginNameForPerson(oase, person);
					if (table.equals("utopia_person")) {
						// table = "utopia_person";
						id = person.getIdString();
						type = "user";
						name = loginName;

						// Skip my own location if specified
						if (meParm != null && meParm.equals(name)) {
							continue;
						}
					} else if (table.equals("g_track")) {
						type = "track";
						name = relatedRecs[0].getStringField("name");
					} else if (table.equals("base_medium")) {
						type = relatedRecs[0].getStringField("kind");
						name = relatedRecs[0].getStringField("name");
					}
					nextElm = new JXElement("record");
					nextElm.setChildText("id", id);
					nextElm.setChildText("name", name);
					nextElm.setChildText("user", loginName);
					nextElm.setChildText("type", type);
					nextElm.setChildText("time", nextRecord.getField("time").toString());
					nextElm.setChildText("lon", nextPoint.getX() + "");
					nextElm.setChildText("lat", nextPoint.getY() + "");
					nextElm.setChildText("distance", (int) nextDistance + "");
					result.addChild(nextElm);
				}
				return result;
			} else if (aQueryName.equals(CMD_QUERY_ACTIVE_TRACKS)) {
				// Niet mooi maar wel optimized!!
				// First get all active tracks
				t1 = Sys.now();
				String tables = "g_track,g_location";
				String fields = "g_track.id,g_track.name,g_location.lon,g_location.lat,g_location.time";
				String where = "g_track.state=1";
				String relations = "g_track,g_location,lastpt";
				String postCond = null;
				result = QueryLogic.queryStoreReq(oase, tables, fields, where, relations, postCond);
				t2 = Sys.now();
				log.info(CMD_QUERY_ACTIVE_TRACKS + " qtime=" + (t2 - t1));

				// Add account/person attrs to each record
				addUserAttrs(result, "g_track");

			} else if (aQueryName.equals(CMD_QUERY_ALL_TRACKS)) {
				String tables = "g_track";
				String fields = null;
				String where = null;
				String relations = null;
				String postCond = null;
				result = QueryLogic.queryStoreReq(oase, tables, fields, where, relations, postCond);
				addUserAttrs(result, "g_track");
			} else if (aQueryName.equals(CMD_QUERY_TRACKS_BY_USER)) {
				String userName = getParameter(theParms, PAR_USER_NAME, null);
				throwOnMissingParm(PAR_USER_NAME, userName);
				// This query split for optimization (from 7 seconds to 50 ms)

				// First get Person+Account
				String tables = "utopia_person,utopia_account,g_track";
				String fields = "utopia_account.loginname,utopia_person.id AS personid,utopia_person.extra,g_track.id,g_track.name";
				String where = "utopia_account.loginname = '" + userName + "'";
				String relations = "utopia_account,utopia_person;g_track,utopia_person";
				String postCond = "ORDER BY g_track.id";
				result = QueryLogic.queryStoreReq(oase, tables, fields, where, relations, postCond);
			} else if (aQueryName.equals(CMD_QUERY_ALL_USERS)) {
				String tables = "utopia_person,utopia_account,utopia_role";
				String fields = "utopia_person.id,utopia_account.id AS accountid, utopia_account.loginname";
				String where = "utopia_role.name = 'user' AND utopia_account.state = 1";
				String relations = "utopia_account,utopia_person;utopia_account,utopia_role";
				String postCond = "ORDER BY utopia_account.loginname";
				result = QueryLogic.queryStoreReq(oase, tables, fields, where, relations, postCond);
			} else if (aQueryName.equals(CMD_QUERY_RECENT_TRACKS)) {
				// Optional number
				String max = getParameter(theParms, "max", "5");
				Finder finder = oase.getFinder();
				Record[] trackRecords = finder.freeQuery("select * from g_track order by enddate desc limit " + max);
				result = Protocol.createResponse(QueryLogic.QUERY_STORE_SERVICE);
				for (int i = 0; i < trackRecords.length; i++) {
					result.addChild(trackRecords[i].toXML());
				}

				// Add account/person attrs to each record
				addUserAttrs(result, "g_track");

			} else if (aQueryName.equals(CMD_QUERY_RANDOM_TRACK)) {
				// (do this in two queries for performance reasons)

				// First get random track
				String tables = "g_track";
				String fields = "g_track.id";
				String where = null;
				String relations = null;
				String postCond = "ORDER BY " + DB_RANDOM_FUN + " LIMIT 1";
				result = QueryLogic.queryStoreReq(oase, tables, fields, where, relations, postCond);
				// log.info("result=" + result);
				String trackId = result.getChildAt(0).getAttr("id");

				// Now get all info for track
				tables = "g_track,utopia_person,utopia_account,g_location";
				fields = "g_track.id,g_track.name,g_track.state,utopia_person.extra,utopia_account.loginname,g_location.lon,g_location.lat,g_location.time";
				where = "g_track.id = " + trackId;
				relations = "g_track,g_location,lastpt;g_track,utopia_person;utopia_account,utopia_person";
				postCond = null;
				result = QueryLogic.queryStoreReq(oase, tables, fields, where, relations, postCond);

			} else if (aQueryName.equals(CMD_QUERY_ROW_COUNT)) {
				String tableName = getParameter(theParms, PAR_TABLE_NAME, null);
				throwOnMissingParm(PAR_TABLE_NAME, tableName);
				Record[] records = oase.getFinder().freeQuery("select count(*) from " + tableName);
				result = Protocol.createResponse(QueryLogic.QUERY_STORE_SERVICE);
				JXElement countElm = new JXElement("count");
				String count = records[0].getField("count(*)") + "";
				countElm.setText(count);
				result.addChild(countElm);
			} else if (aQueryName.equals(CMD_QUERY_COMMENTS_FOR_TARGET)) {
				String targetId = getParameter(theParms, PAR_TARGET, null);
				throwOnMissingParm(PAR_TARGET, targetId);

				result = createResponse(oase.getFinder().freeQuery("select * from " + CommentLogic.TABLE_COMMENT + " WHERE target = " + targetId + " ORDER BY id", CommentLogic.TABLE_COMMENT));

				String ownerInfo = getParameter(theParms, PAR_OWNER_INFO, "false");
				if (ownerInfo.equals("true")) {
					addOwnerFields(result);
				}

			} else if (aQueryName.equals(CMD_QUERY_COMMENTS_FOR_TARGET_PERSON)) {
				String targetPerson = getParameter(theParms, PAR_TARGET_PERSON, null);
				throwOnMissingParm(PAR_TARGET_PERSON, targetPerson);

				// Optional parms: state and exclude own comments (made by target person itself) and to include owner info
				String state = getParameter(theParms, PAR_STATE, null);
				String excludeOwner = getParameter(theParms, PAR_EXCLUDE_OWNER, "false");
				String addOwnerInfo = getParameter(theParms, PAR_OWNER_INFO, "false");
				String addTargetInfo = getParameter(theParms, PAR_TARGET_INFO, "false");

				String query = "select * from " + CommentLogic.TABLE_COMMENT + " WHERE targetperson = " + targetPerson;
				if (state != null) {
					query += " AND state =" + state;
				}

				if (excludeOwner.equals("true")) {
					query += " AND ( owner is null OR owner != " + targetPerson + ")";
				}

				// log.info("excludeowner=" + excludeOwner + " q=" + query);
				result = createResponse(oase.getFinder().freeQuery(query + " ORDER BY target,creationdate DESC"));

				if (addOwnerInfo.equals("true")) {
					addOwnerFields(result);
				}
			} else if (aQueryName.equals(CMD_QUERY_COMMENTERS_FOR_TARGET)) {
				String targetId = getParameter(theParms, PAR_TARGET, null);
				throwOnMissingParm(PAR_TARGET, targetId);

				int[] commenterIds = CommentLogic.getCommenterIds(oase, Integer.parseInt(targetId));
				result = Protocol.createResponse(QueryLogic.QUERY_STORE_SERVICE);
				for (int i = 0; i < commenterIds.length; i++) {
					JXElement elm = new JXElement("record");
					elm.setChildText("id", commenterIds[i] + "");
					result.addChild(elm);
				}
			} else if (aQueryName.equals(CMD_QUERY_COMMENT_COUNT_FOR_TARGET)) {
				String targetId = getParameter(theParms, PAR_TARGET, null);
				throwOnMissingParm(PAR_TARGET, targetId);
				String count = CommentLogic.getCommentCountForTarget(oase, Integer.parseInt(targetId)) + "";

				result = Protocol.createResponse(QueryLogic.QUERY_STORE_SERVICE);
				JXElement countElm = new JXElement("count");
				countElm.setText(count);
				result.addChild(countElm);
			} else if (aQueryName.equals(CMD_QUERY_LOCATIVE_MEDIA)) {
				// See http://www.petefreitag.com/item/466.cfm
				// LAST N: select * from table where key > (select max(key) - n from table)
				String tables = "base_medium,g_location";
				String fields = "base_medium.id,base_medium.kind,base_medium.mime,base_medium.name,base_medium.description,base_medium.creationdate,base_medium.extra,g_location.lon,g_location.lat";
				String where = null;
				String relations = "g_location,base_medium";
				String postCond;

				// WHERE clause

				// Optional bounding box
				String bboxParm = getParameter(theParms, PAR_BBOX, null);
				if (bboxParm != null) {
					where = addBBoxConstraint(bboxParm, where);
				}

				// Optional media type
				String type = getParameter(theParms, "type", null);
				if (type != null) {
					where = (where != null ? where + " AND " : "");
					where += ("base_medium.kind = '" + type + "'");
				}

				// POSTCONDITION
				String random = getParameter(theParms, "random", "false");
				if (random.equals("true")) {
					postCond = "ORDER BY " + DB_RANDOM_FUN;
				} else {
					postCond = "ORDER BY base_medium.creationdate DESC";
				}

				// Limit
				String limitParm = getParameter(theParms, "max", null);
				if (limitParm != null) {
					postCond += " LIMIT " + Integer.parseInt(limitParm);
				}
				// log.info("where=[" + where + "] postCond=[" + postCond +"]");
				result = QueryLogic.queryStoreReq(oase, tables, fields, where, relations, postCond);

				// Add account/person attrs to each record
				// addUserAttrs(result, "base_medium");

			} else if (aQueryName.equals(CMD_QUERY_RECENT_MEDIA)) {
				// Optional number
				String max = getParameter(theParms, "max", "10");
				Finder finder = oase.getFinder();
				result = createResponse(finder.freeQuery("select * from base_medium order by creationdate desc limit " + max));

				// Add lon/lat attrs to each record
				addLocationAttrs(result, "base_medium");

				// Add account/person attrs to each record
				addUserAttrs(result, "base_medium");
			} else if (aQueryName.equals(CMD_QUERY_MEDIA_BY_USER)) {
				String userName = getParameter(theParms, PAR_USER_NAME, null);
				throwOnMissingParm(PAR_USER_NAME, userName);
				// This query split for optimization (from 7 seconds to 50 ms)

				// First get Person+Account
				String tables = "utopia_person,utopia_account";
				String fields = "utopia_account.loginname,utopia_person.id,utopia_person.extra";
				String where = "utopia_account.loginname = '" + userName + "'";
				String relations = "utopia_account,utopia_person";
				String postCond = null;
				result = QueryLogic.queryStoreReq(oase, tables, fields, where, relations, postCond);
				String personId = ((JXElement) result.getChildren().get(0)).getChildText("id");

				// Now query media related to person id
				tables = "utopia_person,base_medium";
				fields = "base_medium.id,base_medium.kind,base_medium.mime,base_medium.name,base_medium.description,base_medium.creationdate";
				where = "utopia_person.id = " + personId;
				relations = "base_medium,utopia_person";
				postCond = "ORDER BY base_medium.creationdate DESC";
				result = QueryLogic.queryStoreReq(oase, tables, fields, where, relations, postCond);

				// Now add login name to each record element in response
				Vector records = result.getChildren();
				JXElement nextRecord;
				String creationDate;
				for (int i = 0; i < records.size(); i++) {
					nextRecord = (JXElement) records.get(i);
					creationDate = nextRecord.getChildText("creationdate");
					creationDate = DATE_FORMAT.format(new Date(Long.parseLong(creationDate)));
					nextRecord.setChildText("fcreationdate", creationDate);
					nextRecord.setChildText("loginname", userName);
				}

				t1 = Sys.now();
				addLocationAttrs(result, "base_medium");
				t2 = Sys.now();
				log.info(CMD_QUERY_MEDIA_BY_USER + " qtime=" + (t2 - t1));

			} else if (aQueryName.equals(CMD_QUERY_TAGS)) {
				// Get tag clouds
				// Parameters:
				// items (item ids), taggers (tagger ids), types (table names), offset, rowcount
				String parItems = getParameter(theParms, PAR_ITEMS, null);
				String parTaggers = getParameter(theParms, PAR_TAGGERS, null);
				String parTypes = getParameter(theParms, PAR_TYPES, null);
				String parOffset = getParameter(theParms, PAR_OFFSET, null);
				String parRowcount = getParameter(theParms, PAR_ROW_COUNT, null);

				int items[] = parItems == null ? null : string2IntArray(parItems);
				int taggers[] = parTaggers == null ? null : string2IntArray(parTaggers);
				String types[] = parTypes == null ? null : parTypes.split(",");
				int offset = parOffset == null ? -1 : Integer.parseInt(parOffset);
				int rowcount = parRowcount == null ? -1 : Integer.parseInt(parRowcount);

				TagLogic tagLogic = new TagLogic(oase.getOaseSession());
				result = createResponse(tagLogic.getTags(types, taggers, items, offset, rowcount));

			} else if (aQueryName.equals(CMD_QUERY_TAGGED)) {
				String parType = getParameter(theParms, PAR_TYPE, null);
				String parTags = getParameter(theParms, PAR_TAGS, null);
				String parOffset = getParameter(theParms, PAR_OFFSET, null);
				String parRowcount = getParameter(theParms, PAR_ROW_COUNT, null);

				String tags[] = parTags == null ? null : parTags.split(",");
				int offset = parOffset == null ? -1 : Integer.parseInt(parOffset);
				int rowcount = parRowcount == null ? -1 : Integer.parseInt(parRowcount);

				TagLogic tagLogic = new TagLogic(oase.getOaseSession());
				result = createResponse(tagLogic.getItemsTaggedWith(parType, tags, "AND", null, offset, rowcount));


			} else if (aQueryName.equals(CMD_QUERY_MEDIUM_INFO) || aQueryName.equals(CMD_QUERY_FEATURE_INFO)) {
				String id = getParameter(theParms, PAR_ID, null);
				throwOnMissingParm(PAR_ID, id);
				Record[] records = new Record[1];
				if (aQueryName.equals(CMD_QUERY_MEDIUM_INFO)) {
					records[0] = oase.getFinder().read(Integer.parseInt(id), "base_medium");
				} else {
					records[0] = oase.getFinder().read(Integer.parseInt(id));
				}

				// Normalize extra field (e.g. meta info for image)  if present
				JXElement extra = null;
				if (!records[0].isNull("extra")) {
					extra = records[0].getXMLField("extra");
					records[0].setXMLField("extra", null);
				}

				result = createResponse(records);
				if (extra != null && extra.hasChildren()) {
					result.getChildAt(0).removeChildByTag("extra");
					result.getChildAt(0).addChild(extra);
				}

				// Add account/person attrs to each record
				addUserAttrs(result, records[0].getTableName());

				// Add number of comments
				addTargetCommentCounts(result);
			} else if (aQueryName.equals(CMD_QUERY_USER_BY_NAME)) {
				String loginName = getParameter(theParms, PAR_USER_NAME, null);
				throwOnMissingParm(PAR_USER_NAME, loginName);

				// First get all active tracks
				String tables = "utopia_person,utopia_account";
				String fields = "utopia_person.id,utopia_person.extra,utopia_account.loginname";
				String where = "utopia_account.loginname = '" + loginName + "'";
				String relations = "utopia_account,utopia_person";
				String postCond = null;
				result = QueryLogic.queryStoreReq(oase, tables, fields, where, relations, postCond);
			} else if (aQueryName.equals(CMD_QUERY_USER_INFO)) {
				String loginName = getParameter(theParms, PAR_USER_NAME, null);
				throwOnMissingParm(PAR_USER_NAME, loginName);

				Record account = getAccount(oase, loginName);
				if (account == null || account.getIntField("state") != 1) {
					throw new IllegalArgumentException("No (active) account found for loginname=" + loginName);
				}

				// We must have a related person otherwise we fail
				Record person = oase.getRelater().getRelated(account, "utopia_person", null)[0];

				// We have valid person/account: create user info record custom
				JXElement userInfo = new JXElement("record");

				// Add account name
				userInfo.setChildText("id", person.getIdString());
				userInfo.setChildText("loginname", loginName);

				// Member since
				userInfo.setChildText("creationdate", person.getLongField("creationdate") + "");

				// Optional profile info
				if (!person.isNull("extra")) {
					userInfo.addChild(person.getXMLField("extra"));
				}

				// Add optional thumb (icon) id
				Record[] thumbRecords = oase.getRelater().getRelated(person, "base_medium", "thumb");
				if (thumbRecords.length > 0) {
					userInfo.setChildText("thumbid", thumbRecords[0].getIdString());
				}

				// Construct final result
				result = Protocol.createResponse(QueryLogic.QUERY_STORE_SERVICE);
				result.addChild(userInfo);

				// Add number of comments on person
				addTargetCommentCounts(result);

				// Add number of comments made by person
				int cmtMadeCnt = CommentLogic.getCommentCountForOwner(oase, person.getId());
				userInfo.setChildText("commentsmade", cmtMadeCnt + "");

				// Add media count
				// TODO: add Relater.getRelatedCount()
				int mediaCnt = oase.getRelater().getRelated(person, "base_medium", null).length;
				userInfo.setChildText("media", mediaCnt + "");

				// Add location
				// TODO: add g_location related to persons
				TrackLogic trackLogic = new TrackLogic(oase);
				Track activeTrack = trackLogic.getActiveTrack(person.getId());
				if (activeTrack != null) {
					Location loc = (Location) activeTrack.getRelatedObject(Location.class, "lastpt");
					if (loc != null) {
						Point pt = loc.getPoint();
						if (pt != null) {
							userInfo.setChildText("lon", pt.getX() + "");
							userInfo.setChildText("lat", pt.getY() + "");
							userInfo.setChildText("lonlattime", (long) pt.getM() + "");
						}
					}
				}

				// Add tags made on this person
				String tags = new TagLogic(oase.getOaseSession()).getTagsString(person.getId(), person.getId());
/*					Record[] tagRelations = new TagLogic(oase.getOaseSession()).getTags(null, new int[] {person.getId()}, null, -1, -1);

					StringBuffer res = new StringBuffer();
					for (int i = 0; i < tagRelations.length; i++) {
						String tag = tagRelations[i].getStringField(TagLogic.NAME_COLUMN);
						if (i != 0) {
							res.append(' ');
						}
						if (tag.indexOf(' ') > 0) {
							res.append("'").append(tag).append("'");
						}
						else {
							res.append(tag);
						}
					}

					String tags = res.toString();
 */
				userInfo.setChildText("tags", tags);
			} else if (aQueryName.equals(CMD_QUERY_USER_IMAGE)) {
				String loginName = getParameter(theParms, PAR_USER_NAME, null);
				throwOnMissingParm(PAR_USER_NAME, loginName);

				Record person = getPersonForLoginName(oase, loginName);
				if (person == null) {
					throw new IllegalArgumentException("No person found for loginname=" + loginName);
				}

				Record[] thumbRecords = oase.getRelater().getRelated(person, "base_medium", "thumb");

				result = Protocol.createResponse(QueryLogic.QUERY_STORE_SERVICE);

				if (thumbRecords.length > 0) {
					result.addChild(thumbRecords[0].toXML());
				}
			} else {
				result = new JXElement(TAG_ERROR);
				result.setText("unknown query " + aQueryName);
				log.warn("unknown query " + aQueryName);
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

	/**
	 * Executes an full sql query.
	 * <p/>
	 * Static method to allow usage from different contexts, like REST requests.
	 *
	 * @param oase	  Oase session object
	 * @param tables	one or more comma-separated tables
	 * @param fields	one or more comma-separated fields
	 * @param where	 the WHERE clause (without WHERE)
	 * @param relations one or more relations e.g. utopia_person,utopia_medium
	 * @param postCond  query post fix string (ORDER BY, LIMIT etc)
	 * @return XML &lt;query-store-rsp /&gt; response
	 * @throws UtopiaException Standard Utopia exception
	 */
	static public JXElement queryStoreReq(Oase oase, String tables, String fields, String where, String relations, String postCond) throws UtopiaException {
		JXElement rsp = Protocol.createResponse(QUERY_STORE_SERVICE);
		rsp.setAttr(ATTR_TABLES, tables);
		rsp.setAttr(ATTR_FIELDS, fields);
		try {

			// Do query with Record[] result
			Record[] result = queryStore(oase, tables, fields, where, relations, postCond);

			// Convert Record[] to XML
			JXElement nextRecord = null;
			for (int i = 0; i < result.length; i++) {
				nextRecord = result[i].toXML();

				// Fix multi-table query (TODO: move to Oase)
				nextRecord.removeAttr("table");
				if (tables.indexOf(",") > 0) {
					// Multi table query: remove id attr
					if (nextRecord.hasAttr("id")) {
						nextRecord.removeAttr("id");
					}
				} else {
					// Single table query: make id attr
					nextRecord.removeChildByTag("id");
				}

				rsp.addChild(nextRecord);
			}
		} catch (OaseException oe) {
			rsp = Protocol.createNegativeResponse(QUERY_STORE_SERVICE, ErrorCode.__6006_database_irregularity_error, oe.getMessage());
			// throw new UtopiaException("DB error " + oe, oe, ErrorCode.__6005_Unexpected_error);
		} catch (Throwable t) {
			rsp = Protocol.createNegativeResponse(QUERY_STORE_SERVICE, ErrorCode.__6005_Unexpected_error, t.getMessage());
		}

		return rsp;
	}

	/**
	 * Executes an full Oase sql query.
	 * <p/>
	 * Static method to allow usage from different contexts, like REST requests.
	 *
	 * @param oase	  Oase session object
	 * @param tables	one or more comma-separated tables
	 * @param fields	one or more comma-separated fields
	 * @param where	 the WHERE clause (without WHERE)
	 * @param relations one or more relations e.g. utopia_person,utopia_medium
	 * @param postCond  query post fix string (ORDER BY, LIMIT etc)
	 * @return Record[] size >= 0
	 * @throws OaseException Standard Oase exception
	 */
	static public Record[] queryStore(Oase oase, String tables, String fields, String where, String relations, String postCond) throws OaseException {

		try {
			Record[] result = null;

			// Init query constraints
			String constraints = null;
			StringBuffer constraintBuf = new StringBuffer();
			if (where != null || relations != null) {
				constraintBuf.append("WHERE ");
			}

			// Null means all fields.
			if (fields == null) {
				fields = "*";
			}

			if (tables.indexOf(",") > 0) {
				// Prepare query store parms
				if (relations != null) {

					// Prepare relation clause
					String allRelClause = "";

					// Get all relation specs e.g. (person,medium;account,person)
					String relSpecs[] = relations.trim().split(REL_SPEC_SEPARATOR);
					for (int i = 0; i < relSpecs.length; i++) {
						String relSpec[] = relSpecs[i].trim().split(",");
						String[] orderedTables = oase.getRelater().getRelationTableOrder(relSpec[0].trim(), relSpec[1].trim());
						String table1 = orderedTables[0];
						String table2 = orderedTables[1];

						// Create alias name (required for multi-relation specs)
						String relAlias = REL_ALIAS_BASE + i;

						// Assume no tag
						String relClause = IS_RELATED;

						// Optional tag as e.g. {base_medium,utopia_person,image}
						if (relSpec.length == 3) {
							relClause = IS_RELATED_WITH_TAG.replaceAll("TAG", relSpec[2]);
						}

						// Replace relation alias
						relClause = relClause.replaceAll("REL_ALIAS", relAlias);

						// Replace related tables
						relClause = relClause.replaceAll("TABLE1", table1).replaceAll("TABLE2", table2);

						// Determine if multiple relations need to be AND-ed
						if (allRelClause.length() == 0) {
							allRelClause = relClause;
						} else {
							allRelClause += (" AND " + relClause);
						}

						// Extend FROM with relation table alias
						tables += ",oase_relation " + relAlias;
					}

					// Make final WHERE dependent if there was a WHERE clause
					constraintBuf.append(where == null ? allRelClause : (where + " AND " + allRelClause));
				}

				if (postCond != null) {
					constraintBuf.append(" " + postCond);
				}

				constraints = (constraintBuf.length() > 0) ? constraintBuf.toString() : null;

				// Let oase do multi-table query
				result = oase.getFinder().queryStore(tables, fields, constraints);
			} else {
				// Query on single table
				if (where != null) {
					constraintBuf.append(where);
				}
				if (postCond != null) {
					constraintBuf.append(" " + postCond);
				}

				constraints = (constraintBuf.length() > 0) ? constraintBuf.toString() : null;

				// Simple one-table query
				result = oase.getFinder().queryTable(tables, constraints);
			}
			return result;
		} catch (OaseException oe) {
			throw oe;
		} catch (Throwable t) {
			throw new OaseException("Unexpected error in queryStore2()", t);
		}

	}

	String getParameter(Map parms, String name, String defaultValue) {
		Object value = parms.get(name);
		if (value == null) {
			return defaultValue;
		}

		String strValue;
		if (value instanceof String[] && ((String[]) value).length > 0) {
			// May come from Servlet parms Map
			strValue = ((String[]) value)[0];
		} else {
			strValue = (String) value;

		}
		if (strValue == null || strValue.length() == 0) {
			return defaultValue;
		}

		return strValue.trim();
	}

	/**
	 * Throw exception when parm empty or not present.
	 */
	public void throwOnMissingParm(String aName, String aValue) throws IllegalArgumentException {
		if (aValue == null || aValue.length() == 0) {
			throw new IllegalArgumentException("Missing parameter=" + aName);
		}
	}

	/**
	 * Adds Bounding box WHERE constraint.
	 */
	public String addBBoxConstraint(String bboxParm, String where) throws Exception {
		// point && SetSRID('BOX3D(2 2, 6 5)'::box3d,4326)
		String[] bbox = bboxParm.split(",");
		where = where == null ? "" : where + " AND";
		/* where = where
				+ " g_location.lon >= " + bbox[0]
				+ " AND g_location.lat >= " + bbox[1]
				+ " AND g_location.lon <= " + bbox[2]
				+ " AND g_location.lat <= " + bbox[3];   */

		where = where + "g_location.point && SetSRID('BOX3D("
				+ bbox[0] + " "
				+ bbox[1] + ","
				+ bbox[2] + " "
				+ bbox[3] + ")'::box3d,4326)";
		return where;
	}

	/**
	 * Create query-response XML from Record array.
	 */
	public JXElement createResponse(Record[] theRecords) throws Exception {
		JXElement result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
		for (int i = 0; i < theRecords.length; i++) {
			result.addChild(theRecords[i].toXML());
		}
		return result;
	}

	/**
	 * Adds g_location attrs to query response records.
	 */
	public void addLocationAttrs(JXElement rsp, String aTableName) throws Exception {
		Vector records = rsp.getChildren();
		Finder finder = oase.getFinder();
		Relater relater = oase.getRelater();

		// Start with empty
		rsp.removeChildren();
		JXElement nextRecordElm;
		String recordId;
		Record record;
		Record[] locationRecords;
		Point nextPoint;
		for (int i = 0; i < records.size(); i++) {
			nextRecordElm = (JXElement) records.get(i);

			// SIngle table responses have id in attr
			// Multi table in record elm
			// TODO FIX!!
			recordId = nextRecordElm.getChildText("id");
			if (recordId == null) {
				recordId = nextRecordElm.getAttr("id");
			}

			// Get related location
			if (aTableName != null && aTableName.equals("base_medium")) {
				String tables = "base_medium,g_location";
				String fields = "g_location.point";
				String where = "base_medium.id = " + recordId;
				String relations = "base_medium,g_location";
				locationRecords = QueryLogic.queryStore(oase, tables, fields, where, relations, null);
			} else {
				// Must have the real record to determine tablename
				record = finder.read(Integer.parseInt(recordId));

				if (record == null) {
					log.warn("Cannot read record for id=" + recordId);
					continue;
				}
				locationRecords = relater.getRelated(record, "g_location", null);
			}

			if (locationRecords.length == 0) {
				continue;
			}

			// Add the location attrs
			nextPoint = Location.getPoint(locationRecords[0]);
			if (nextPoint == null) {
				continue;
			}

			nextRecordElm.setChildText("lon", nextPoint.getX() + "");
			nextRecordElm.setChildText("lat", nextPoint.getY() + "");

			// Add to response
			rsp.addChild(nextRecordElm);
		}
	}


	/**
	 * Adds owner info (loginname and user icon id).
	 */
	public void addOwnerFields(JXElement rsp) throws Exception {
		// Add extra fields: user name (loginname) and user icon (thumb) id
		Vector records = rsp.getChildren();
		rsp.removeChildren();
		JXElement nextRecordElm;
		String personId;
		Record personRecord, accountRecord;
		String iconRecordId;
		HashMap personsForPersonId = new HashMap(3);
		HashMap accountsForPersonId = new HashMap(3);
		HashMap iconIdForPersonId = new HashMap(3);
		for (int i = 0; i < records.size(); i++) {
			nextRecordElm = (JXElement) records.get(i);

			// Get owner (person) id
			personId = nextRecordElm.getChildText(CommentLogic.FIELD_OWNER);
			if (personId == null || personId.length() == 0) {
				// No owner (anon comment) add to rsp and proceed
				rsp.addChild(nextRecordElm);
				continue;
			}

			// ASSERT: has owner: now get loginname and thumb (icon, optional) id
			// ASSERT : got valid person, now get logginname
			personRecord = (Record) personsForPersonId.get(personId);
			if (personRecord == null) {
				personRecord = oase.getFinder().read(Integer.parseInt(personId), "utopia_person");
				if (personRecord == null) {
					// Rare case: person non-existent
					log.warn("addOwnerFields: no person record for id=" + personId);
					rsp.addChild(nextRecordElm);
					continue;
				} else {
					// Add to cache
					personsForPersonId.put(personId, personRecord);
				}
			}

			// ASSERT : got valid person, now get logginname
			accountRecord = (Record) accountsForPersonId.get(personId);
			if (accountRecord == null) {
				accountRecord = oase.getRelater().getRelated(personRecord, "utopia_account", null)[0];
				// Add to cache
				accountsForPersonId.put(personId, accountRecord);
			}

			// Skip records for disabled account
			if (accountRecord.getIntField("state") != 1) {
				rsp.addChild(nextRecordElm);
				continue;
			}

			// Add user name (loginname)
			nextRecordElm.setChildText("ownername", accountRecord.getStringField("loginname"));

			iconRecordId = (String) iconIdForPersonId.get(personId);
			if (iconRecordId == null) {
				Record[] iconRecords = oase.getRelater().getRelated(personRecord, "base_medium", "thumb");
				if (iconRecords.length > 0) {
					// Add to cache
					iconRecordId = iconRecords[0].getIdString();
				} else {
					// Put empty string so we won't do query next time
					iconRecordId = "";
				}
				iconIdForPersonId.put(personId, iconRecordId);
			}

			if (iconRecordId != null && iconRecordId.length() > 0) {
				nextRecordElm.setChildText("ownericon", iconRecordId);
			}

			// Finally ad to rsp
			rsp.addChild(nextRecordElm);
		}
	}

	/**
	 * Adds account and person attrs to query response records.
	 */
	public void addUserAttrs(JXElement rsp, String aTableName) throws Exception {
		Finder finder = oase.getFinder();
		Relater relater = oase.getRelater();

		// Start with empty
		Vector records = rsp.getChildren();
		rsp.removeChildren();
		JXElement nextRecordElm;
		String recordId;
		Record record;
		Record[] personRecords, accountRecords, thumbRecords;
		HashMap cache = new HashMap(3);
		Vector userElms;
		for (int i = 0; i < records.size(); i++) {
			nextRecordElm = (JXElement) records.get(i);

			// Single table responses have id in attr
			// Multi table in record elm
			// TODO FIX!!
			recordId = nextRecordElm.getChildText("id");
			if (recordId == null) {
				recordId = nextRecordElm.getAttr("id");
			}

			// Must have the real record
			record = finder.read(Integer.parseInt(recordId), aTableName);

			// Get related person
			personRecords = relater.getRelated(record, "utopia_person", null);
			if (personRecords.length == 0) {
				log.warn("no person record found for record id=" + record.getId() + " table=" + record.getTableName());
				continue;
			}

			// See if already cached
			userElms = (Vector) cache.get(personRecords[0].getIdString());
			if (userElms == null) {
				// Get related account
				accountRecords = relater.getRelated(personRecords[0], "utopia_account", null);
				if (accountRecords.length == 0) {
					log.warn("no account record found for person id=" + record.getId());
					continue;
				}

				// Skip records for disabled account
				if (accountRecords[0].getIntField("state") != 1) {
					continue;
				}

				userElms = new Vector(2);

				// Add the user login nane
				JXElement loginNameElm = new JXElement("loginname");
				loginNameElm.setText(accountRecords[0].getStringField("loginname"));
				userElms.add(loginNameElm);

				// Extra field with profile info
				if (!personRecords[0].isNull("extra")) {
					userElms.add(personRecords[0].getXMLField("extra"));
				}

				// Add to cache
				cache.put(personRecords[0].getIdString(), userElms);
			} else {
				// log.info("cache hit for person=" + personRecords[0].getIdString());
			}

			// User user elms to add to result
			nextRecordElm.addChildren(userElms);

			// Add to response
			rsp.addChild(nextRecordElm);
		}
	}

	/**
	 * Adds account and person attrs to query response records.
	 */
	public void addTargetCommentCounts(JXElement rsp) throws Exception {
		Vector records = rsp.getChildren();
		JXElement nextRecordElm;
		String targetId;
		for (int i = 0; i < records.size(); i++) {
			nextRecordElm = (JXElement) records.get(i);

			// SIngle table responses have id in attr
			// Multi table in record elm
			// TODO FIX!!
			targetId = nextRecordElm.getChildText("id");
			if (targetId == null) {
				targetId = nextRecordElm.getAttr("id");
			}

			// Add the comment count
			nextRecordElm.setChildText("comments", CommentLogic.getCommentCountForTarget(oase, Integer.parseInt(targetId)) + "");
		}
	}

	Record getAccount(Oase oase, String aLoginName) throws Exception {
		Finder finder = oase.getFinder();
		Record[] result = finder.queryTable("utopia_account", "WHERE utopia_account.loginname = '" + aLoginName + "'");
		return result.length == 0 ? null : result[0];
	}

	Record getPersonForLoginName(Oase oase, String aLoginName) throws Exception {
		Record account = getAccount(oase, aLoginName);
		if (account == null) {
			return null;
		}
		Record[] result = oase.getRelater().getRelated(account, "utopia_person", null);
		return result.length == 0 ? null : result[0];
	}

	String getLoginNameForPerson(Oase oase, Record aPerson) throws Exception {
		Record[] result = oase.getRelater().getRelated(aPerson, "utopia_account", null);
		return result.length == 0 ? null : result[0].getStringField("loginname");
	}

	/**
	 * Convert string like "22,34,56" to int array.
	 */
	int[] string2IntArray(String anIntList) throws NumberFormatException {
		String[] ids = anIntList.split(",");
		int[] values = new int[ids.length];
		for (int i = 0; i < ids.length; i++) {
			values[i] = Integer.parseInt(ids[i]);
		}
		return values;
	}

}



