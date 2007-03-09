<%@ page import="nl.justobjects.jox.parser.JXBuilder,
				 org.geotracing.gis.GeoPoint,
				 org.geotracing.gis.XYDouble,
				 org.geotracing.handler.*,
				 org.keyworx.amuse.core.Amuse,
				 org.keyworx.amuse.core.Protocol,
				 org.keyworx.common.log.Log,
				 org.keyworx.common.log.Logging" %>
<%@ page import="org.keyworx.common.util.Sys" %>
<%@ page import="org.keyworx.oase.api.Finder" %>
<%@ page import="org.keyworx.oase.api.Record" %>
<%@ page import="org.keyworx.oase.api.Relater" %>
<%@ page import="org.keyworx.oase.config.ComponentDef" %>
<%@ page import="org.keyworx.oase.config.OaseConfig" %>
<%@ page import="org.keyworx.oase.config.StoreContextConfig" %>
<%@ page import="org.keyworx.plugin.tagging.logic.TagLogic"%>
<%@ page import="org.keyworx.utopia.core.util.Oase"%>
<%@ page import="javax.servlet.ServletRequest"%>
<%@ page import="javax.servlet.http.HttpServletRequest"%>
<%@ page import="javax.servlet.http.HttpServletResponse"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.Writer"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.Enumeration"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Vector"%>
<%!
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd'.'MM'.'yy-HH:mm:ss");

	// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
	// Distributable under LGPL license. See terms of license at gnu.org.

	// This JSP implements a REST-like service to obtain
	// data (in XML) from the server DB.
	// $Id: get.jsp,v 1.25 2006-08-28 09:43:23 just Exp $

	public static final String CMD_QUERY_STORE = "q-store";
	public static final String CMD_QUERY_ACTIVE_TRACKS = "q-active-tracks";
	public static final String CMD_QUERY_AROUND = "q-around";
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
	public static final String CMD_GET_TRACK = "get-track";
	public static final String CMD_DESCRIBE = "describe";

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
	public static final String PAR_TARGET_PERSON= CommentLogic.FIELD_TARGET_PERSON;
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

	public static Oase oase;
	public static Log log = Logging.getLog("get.jsp");

	String getParameter(ServletRequest req, String name, String defaultValue) {
		String value = req.getParameter(name);
		if (value == null || value.length() == 0) {
			return defaultValue;
		}

		return value.trim();
	}


	/**
	 * Throw exception when parm empty or not present.
	 */
	public void throwOnMissingParm(String aName, String aValue) throws IllegalArgumentException {
		if (aValue == null || aValue.length() == 0) {
			throw new IllegalArgumentException("Missing parameter=" + aName);
		}
	}

	/** Adds Bounding box WHERE constraint. */
	public String addBBoxConstraint(String bboxParm, String where) throws Exception {
		String[] bbox = bboxParm.split(",");
		where = where == null ? "" : where + " AND";
		where = where
				+ " g_location.lon >= " + bbox[0]
				+ " AND g_location.lat >= " + bbox[1]
				+ " AND g_location.lon <= " + bbox[2]
				+ " AND g_location.lat <= " + bbox[3];
		return where;
	}

	/** Create query-response XML from Record array. */
	public JXElement createResponse(Record[] theRecords) throws Exception {
		JXElement result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
		for (int i = 0; i < theRecords.length; i++) {
			result.addChild(theRecords[i].toXML());
		}
		return result;
	}

	/** Adds g_location attrs to query response records. */
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
				String fields = "g_location.lon,g_location.lat";
				String where = "base_medium.id = " + recordId;
				String relations = "base_medium,g_location";
				locationRecords = QueryHandler.queryStore(oase, tables, fields, where, relations, null);
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
			nextRecordElm.setChildText("lon", locationRecords[0].getField("lon").toString());
			nextRecordElm.setChildText("lat", locationRecords[0].getField("lat").toString());

			// Add to response
			rsp.addChild(nextRecordElm);
		}
	}


	/** Adds owner info (loginname and user icon id). */
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

	/** Adds account and person attrs to query response records. */
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

	/** Adds account and person attrs to query response records. */
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

	/** Convert string like "22,34,56" to int array. */
	int[] string2IntArray(String anIntList) throws NumberFormatException {
		String[] ids = anIntList.split(",");
		int[] values = new int[ids.length];
		for (int i = 0; i < ids.length; i++) {
			values[i] = Integer.parseInt(ids[i]);
		}
		return values;
	}


	class JSONQueryEncoder  {
		private Writer writer;
		private boolean debug = false;

		public JSONQueryEncoder() {
		}

		public void encode(JXElement aQueryResult, Writer aWriter) throws Exception {
			writer = aWriter;
			outputD("<pre>");
			output("{");
			output("query_rsp");


			output(": { cnt: ");
			outputString(aQueryResult.getAttr("cnt"));
			output(", records: [");
			Vector records = aQueryResult.getChildren();
			int cnt = records.size();
			for (int i=0; i < cnt; i++) {
				outputRecord((JXElement) records.get(i));
				if (i < cnt -1) {
					output(",\n");
				}
			}
			output("]");
			output("}");
			output("}");
			outputD("</pre>");
		}

		private void outputRecord(JXElement aRec) throws Exception {
			Vector fields = aRec.getChildren();
			if (fields.size() == 0) {
				return;
			}

			StringBuffer sb=new StringBuffer();
			sb.append("{");

			JXElement field;
			String encodedField= null;
			for (int i=0; i < fields.size(); i++) {
				field = (JXElement) fields.get(i);
				if (field.getTag().equals("extra")) {
					if (field.hasChildren()) {
						fields.addAll(field.getChildAt(0).getChildren());
					} else if (field.hasText() && field.getText().trim().length() != 0) {
						fields.addAll(new JXBuilder().build(field.getText()).getChildren());
					}
					continue;
				}

				encodedField = encodeField(field);
				if (encodedField != null) {
					sb.append(encodedField);
					sb.append(",\n");
				}

				// outputD("\n");
			}

			// Crude but simplest way to deal with last "," (all kinds of exceptions,
			// empty fields etc.
			sb.replace(sb.length()-2, sb.length()-1, " ");
			sb.append("}");
			output(sb.toString());
			outputD("\n");
		}

		private String encodeField(JXElement field) throws Exception {
			if (!field.hasText()) {
				return null;
			}

			String text = field.getText().trim();
			if (text.length() == 0) {
				return null;
			}
			String name = field.getTag().replaceAll("-", "");
			return name + ": '" + text + "'";
		}

		private void outputD(String s) {
			if (!debug) {
				return;
			}
			output(s);
		}

		private void output(String s) {
			try {
				writer.write(s);
			} catch (IOException ioe) {
				System.out.println("error: " + ioe);
			}
		}

		private void outputString(String s) {
			try {
				writer.write("'" + s + "'");
			} catch (IOException ioe) {
				System.out.println("error: " + ioe);
			}
		}
	}

	/** Performs command and returns XML result. */
	public JXElement doCommand(HttpServletRequest request, HttpServletResponse response) {
		JXElement result;
		String command = getParameter(request, PAR_CMD, CMD_DESCRIBE);
		long t1,t2;
		try {

			/*		if (command.equals(CMD_QUERY_STORE)) {
							String tables = getParameter(request, "tables", null);
							String fields = getParameter(request, "fields", null);
							String where = getParameter(request, "where", null);
							String relations = getParameter(request, "rels", null);
							String postCond = getParameter(request, "postcond", null);
							result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);
						} else      */

			if (command.equals(CMD_QUERY_BY_EXAMPLE)) {
				// Query for single table by providing partial example record

				// Table is required
				String table = getParameter(request, PAR_TABLE_NAME, null);
				throwOnMissingParm(PAR_TABLE_NAME, table);

				// Create example Record from other parms
				Record exampleRecord = oase.getFinder().createExampleRecord(table);

				// Fill in all field name/values (except table)
				Enumeration fields = request.getParameterNames();
				String name, value;
				while (fields.hasMoreElements()) {
					name = (String) fields.nextElement();
					if (name.equals("table") || name.equals("cmd") || name.equals("t") || name.equals("output")) {
						// Skip
						continue;
					}
					value = getParameter(request, name, null);
					if (value != null) {
						exampleRecord.setField(name, value);
					}
				}

				result = createResponse(oase.getFinder().queryTable(exampleRecord));
			} else if (command.equals(CMD_QUERY_AROUND)) {
				// Look for items around a point with a radius
				String locString = getParameter(request, PAR_LOC, null);
				throwOnMissingParm(PAR_LOC, locString);
				String radiusString = getParameter(request, PAR_RADIUS, null);
				throwOnMissingParm(PAR_RADIUS, radiusString);


				String limitParm = getParameter(request, PAR_MAX, "51");
				String typesParm = getParameter(request, PAR_TYPES, null);
				String meParm = getParameter(request, "me", null);

				// WHERE clause
				// Optional object type
				// String type = getParameter(request, "type", null);
				// See http://www.petefreitag.com/item/466.cfm
				// LAST N: select * from table where key > (select max(key) - n from table)
				String tables = "g_location";
				String fields = null;
				String where = null;
				if (typesParm != null) {
					String[] types = typesParm.split(",");
					where  = "";
					for (int i=0; i < types.length; i++) {
						if (i == 0) where += " ( ";
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
						if (i==types.length-1) where += " ) ";
					}

				}

				String relations = null;

				// Calculate BBOX
				double radius = Double.parseDouble(radiusString);
				String[] lonLatString = locString.split(",");
				GeoPoint location = new GeoPoint(lonLatString[0], lonLatString[1]);
				XYDouble metersPerDeg = location.metersPerDegree();

				GeoPoint locSW = new GeoPoint(location.lon - (radius / metersPerDeg.x), location.lat - (radius / metersPerDeg.y));
				GeoPoint locNE = new GeoPoint(location.lon + (radius / metersPerDeg.x), location.lat + (radius / metersPerDeg.y));

				String bbox = locSW.lon + "," + locSW.lat + "," + locNE.lon + "," + locNE.lat;
				where = addBBoxConstraint(bbox, where);
				// log.info("mperdeg=" + metersPerDeg.x + "," + metersPerDeg.y + " bbox=" + bbox);

				// Limit
				int limit = Integer.parseInt(limitParm);

				// Hard limit: don't blow up our DB (for now)
				if (limit > 51) {
					limit = 51;
				}
				String postCond = " LIMIT " + limit;

				// log.info("where=[" + where + "] postCond=[" + postCond +"]");
				Record[] records = QueryHandler.queryStore(oase, tables, fields, where, relations, postCond);
				result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
				JXElement nextElm;
				Record nextRecord;
				GeoPoint nextPoint;
				double nextDistance;
				Relater relater = oase.getRelater();
				Record[] relatedRecs;
				for (int i = 0; i < records.length; i++) {
					nextRecord = records[i];
					nextPoint = new GeoPoint(nextRecord.getRealField("lon"), nextRecord.getRealField("lat"));
					nextDistance = location.distance(nextPoint) * 1000;
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
					nextElm.setChildText("lon", nextRecord.getField("lon").toString());
					nextElm.setChildText("lat", nextRecord.getField("lat").toString());
					nextElm.setChildText("distance", (int) nextDistance + "");
					result.addChild(nextElm);
				}
				return result;

			} else if (command.equals(CMD_QUERY_ACTIVE_TRACKS)) {
				// Niet mooi maar wel optimized!!
				// First get all active tracks
				t1 = Sys.now();
				String tables = "g_track,g_location";
				String fields = "g_track.id,g_track.name,g_location.lon,g_location.lat,g_location.time";
				String where = "g_track.state=1";
				String relations = "g_track,g_location,lastpt";
				String postCond = null;
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);
				t2=Sys.now();
				log.info(CMD_QUERY_ACTIVE_TRACKS + " qtime=" + (t2-t1));

				// Add account/person attrs to each record
				addUserAttrs(result, "g_track");

			} else if (command.equals(CMD_QUERY_ALL_TRACKS)) {
				String tables = "g_track";
				String fields = null;
				String where = null;
				String relations = null;
				String postCond = null;
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);
				addUserAttrs(result, "g_track");
			} else if (command.equals(CMD_QUERY_TRACKS_BY_USER)) {
				String userName = getParameter(request, PAR_USER_NAME, null);
				throwOnMissingParm(PAR_USER_NAME, userName);
				// This query split for optimization (from 7 seconds to 50 ms)

				// First get Person+Account
				String tables = "utopia_person,utopia_account,g_track";
				String fields = "utopia_account.loginname,utopia_person.id AS personid,utopia_person.extra,g_track.id,g_track.name";
				String where = "utopia_account.loginname = '" + userName + "'";
				String relations = "utopia_account,utopia_person;g_track,utopia_person";
				String postCond = "ORDER BY g_track.id";
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);
			} else if (command.equals(CMD_QUERY_ALL_USERS)) {
				String tables = "utopia_person,utopia_account,utopia_role";
				String fields = "utopia_person.id,utopia_account.id AS accountid, utopia_account.loginname";
				String where = "utopia_role.name = 'user' AND utopia_account.state = 1";
				String relations = "utopia_account,utopia_person;utopia_account,utopia_role";
				String postCond = "ORDER BY utopia_account.loginname";
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);
			} else if (command.equals(CMD_QUERY_RECENT_TRACKS)) {
				// Optional number
				String max = getParameter(request, "max", "5");
				Finder finder = oase.getFinder();
				Record[] trackRecords = finder.freeQuery("select * from g_track order by enddate desc limit " + max);
				result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
				for (int i = 0; i < trackRecords.length; i++) {
					result.addChild(trackRecords[i].toXML());
				}

				// Add account/person attrs to each record
				addUserAttrs(result, "g_track");

			} else if (command.equals(CMD_QUERY_RANDOM_TRACK)) {
				// (do this in two queries for performance reasons)

				// First get random track
				String tables = "g_track";
				String fields = "g_track.id";
				String where = null;
				String relations = null;
				String postCond = "ORDER BY " + DB_RANDOM_FUN + " LIMIT 1";
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);
				// log.info("result=" + result);
				String trackId = result.getChildAt(0).getAttr("id");

				// Now get all info for track
				tables = "g_track,utopia_person,utopia_account,g_location";
				fields = "g_track.id,g_track.name,g_track.state,utopia_person.extra,utopia_account.loginname,g_location.lon,g_location.lat,g_location.time";
				where = "g_track.id = " + trackId;
				relations = "g_track,g_location,lastpt;g_track,utopia_person;utopia_account,utopia_person";
				postCond = null;
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);

			} else if (command.equals(CMD_QUERY_ROW_COUNT)) {
				String tableName = getParameter(request, PAR_TABLE_NAME, null);
				throwOnMissingParm(PAR_TABLE_NAME, tableName);
				Record[] records = oase.getFinder().freeQuery("select count(*) from " + tableName);
				result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
				JXElement countElm = new JXElement("count");
				String count = records[0].getField("count(*)") + "";
				countElm.setText(count);
				result.addChild(countElm);
			} else if (command.equals(CMD_QUERY_COMMENTS_FOR_TARGET)) {
				String targetId = getParameter(request, PAR_TARGET, null);
				throwOnMissingParm(PAR_TARGET, targetId);

				result = createResponse(oase.getFinder().freeQuery("select * from " + CommentLogic.TABLE_COMMENT + " WHERE target = " + targetId + " ORDER BY id", CommentLogic.TABLE_COMMENT));

				String ownerInfo = getParameter(request, PAR_OWNER_INFO, "false");
				if (ownerInfo.equals("true")) {
					addOwnerFields(result);
				}

			} else if (command.equals(CMD_QUERY_COMMENTS_FOR_TARGET_PERSON)) {
				String targetPerson = getParameter(request, PAR_TARGET_PERSON, null);
				throwOnMissingParm(PAR_TARGET_PERSON, targetPerson);

				// Optional parms: state and exclude own comments (made by target person itself) and to include owner info
				String state = getParameter(request, PAR_STATE, null);
				String excludeOwner = getParameter(request, PAR_EXCLUDE_OWNER, "false");
				String addOwnerInfo = getParameter(request, PAR_OWNER_INFO, "false");
				String addTargetInfo = getParameter(request, PAR_TARGET_INFO, "false");

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
			} else if (command.equals(CMD_QUERY_COMMENTERS_FOR_TARGET)) {
				String targetId = getParameter(request, PAR_TARGET, null);
				throwOnMissingParm(PAR_TARGET, targetId);

				int[] commenterIds = CommentLogic.getCommenterIds(oase, Integer.parseInt(targetId));
				result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
				for (int i = 0; i < commenterIds.length; i++) {
					JXElement elm = new JXElement("record");
					elm.setChildText("id", commenterIds[i] + "");
					result.addChild(elm);
				}
			} else if (command.equals(CMD_QUERY_COMMENT_COUNT_FOR_TARGET)) {
				String targetId = getParameter(request, PAR_TARGET, null);
				throwOnMissingParm(PAR_TARGET, targetId);
				String count = CommentLogic.getCommentCountForTarget(oase, Integer.parseInt(targetId)) + "";

				result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
				JXElement countElm = new JXElement("count");
				countElm.setText(count);
				result.addChild(countElm);
			} else if (command.equals(CMD_QUERY_LOCATIVE_MEDIA)) {
				// See http://www.petefreitag.com/item/466.cfm
				// LAST N: select * from table where key > (select max(key) - n from table)
				String tables = "base_medium,g_location";
				String fields = "base_medium.id,base_medium.kind,base_medium.mime,base_medium.name,base_medium.description,base_medium.creationdate,base_medium.extra,g_location.lon,g_location.lat";
				String where = null;
				String relations = "g_location,base_medium";
				String postCond;

				// WHERE clause
				// Optional media type
				String type = getParameter(request, "type", null);
				if (type != null) {
					where = "base_medium.kind = '" + type + "'";
				}

				String bboxParm = getParameter(request, PAR_BBOX, null);
				if (bboxParm != null) {
					where = addBBoxConstraint(bboxParm, where);
				}

				// POSTCONDITION
				String random = getParameter(request, "random", "false");
				if (random.equals("true")) {
					postCond = "ORDER BY " + DB_RANDOM_FUN;
				} else {
					postCond = "ORDER BY base_medium.creationdate DESC";
				}

				// Limit
				String limitParm = getParameter(request, "max", null);
				if (limitParm != null) {
					postCond += " LIMIT " + Integer.parseInt(limitParm);
				}
				// log.info("where=[" + where + "] postCond=[" + postCond +"]");
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);

				// Add account/person attrs to each record
				// addUserAttrs(result, "base_medium");

			} else if (command.equals(CMD_QUERY_RECENT_MEDIA)) {
				// Optional number
				String max = getParameter(request, "max", "10");
				Finder finder = oase.getFinder();
				result = createResponse(finder.freeQuery("select * from base_medium order by creationdate desc limit " + max));

				// Add lon/lat attrs to each record
				addLocationAttrs(result, "base_medium");

				// Add account/person attrs to each record
				addUserAttrs(result, "base_medium");
			} else if (command.equals(CMD_QUERY_MEDIA_BY_USER)) {
				String userName = getParameter(request, PAR_USER_NAME, null);
				throwOnMissingParm(PAR_USER_NAME, userName);
				// This query split for optimization (from 7 seconds to 50 ms)

				// First get Person+Account
				String tables = "utopia_person,utopia_account";
				String fields = "utopia_account.loginname,utopia_person.id,utopia_person.extra";
				String where = "utopia_account.loginname = '" + userName + "'";
				String relations = "utopia_account,utopia_person";
				String postCond = null;
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);
				String personId = ((JXElement) result.getChildren().get(0)).getChildText("id");

				// Now query media related to person id
				tables = "utopia_person,base_medium";
				fields = "base_medium.id,base_medium.kind,base_medium.mime,base_medium.name,base_medium.description,base_medium.creationdate";
				where = "utopia_person.id = " + personId;
				relations = "base_medium,utopia_person";
				postCond = "ORDER BY base_medium.creationdate DESC";
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);

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
				log.info(CMD_QUERY_MEDIA_BY_USER + " qtime=" + (t2-t1));

			} else if (command.equals(CMD_QUERY_TAGS)) {
				// Get tag clouds
				// Parameters:
				// items (item ids), taggers (tagger ids), types (table names), offset, rowcount
				String parItems = getParameter(request, PAR_ITEMS, null);
				String parTaggers = getParameter(request, PAR_TAGGERS, null);
				String parTypes = getParameter(request, PAR_TYPES, null);
				String parOffset = getParameter(request, PAR_OFFSET, null);
				String parRowcount = getParameter(request, PAR_ROW_COUNT, null);

				int items[] = parItems == null ? null : string2IntArray(parItems);
				int taggers[] = parTaggers == null ? null : string2IntArray(parTaggers);
				String types[] = parTypes == null ? null : parTypes.split(",");
				int offset = parOffset == null ? -1 : Integer.parseInt(parOffset);
				int rowcount = parRowcount == null ? -1 : Integer.parseInt(parRowcount);

				TagLogic tagLogic = new TagLogic(oase.getOaseSession());
				result = createResponse(tagLogic.getTags(types, taggers, items, offset, rowcount));

			} else if (command.equals(CMD_QUERY_TAGGED)) {
				String parType = getParameter(request, PAR_TYPE, null);
				String parTags = getParameter(request, PAR_TAGS, null);
				String parOffset = getParameter(request, PAR_OFFSET, null);
				String parRowcount = getParameter(request, PAR_ROW_COUNT, null);

				String tags[] = parTags == null ? null : parTags.split(",");
				int offset = parOffset == null ? -1 : Integer.parseInt(parOffset);
				int rowcount = parRowcount == null ? -1 : Integer.parseInt(parRowcount);

				TagLogic tagLogic = new TagLogic(oase.getOaseSession());
				result = createResponse(tagLogic.getItemsTaggedWith(parType, tags, "AND", null, offset, rowcount));


			} else if (command.equals(CMD_QUERY_MEDIUM_INFO) || command.equals(CMD_QUERY_FEATURE_INFO)) {
				String id = getParameter(request, PAR_ID, null);
				throwOnMissingParm(PAR_ID, id);
				Record[] records = new Record[1];
				if (command.equals(CMD_QUERY_MEDIUM_INFO)) {
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
			} else if (command.equals(CMD_QUERY_USER_BY_NAME)) {
				String loginName = getParameter(request, PAR_USER_NAME, null);
				throwOnMissingParm(PAR_USER_NAME, loginName);

				// First get all active tracks
				String tables = "utopia_person,utopia_account";
				String fields = "utopia_person.id,utopia_person.extra,utopia_account.loginname";
				String where = "utopia_account.loginname = '" + loginName + "'";
				String relations = "utopia_account,utopia_person";
				String postCond = null;
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);
			} else if (command.equals(CMD_QUERY_USER_INFO)) {
				String loginName = getParameter(request, PAR_USER_NAME, null);
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
				result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
				result.addChild(userInfo);

				// Add number of comments on person
				addTargetCommentCounts(result);

				// Add number of comments made by person
				int cmtMadeCnt = CommentLogic.getCommentCountForOwner(oase, person.getId());
				userInfo.setChildText("commentsmade", cmtMadeCnt+"");

				// Add media count
				// TODO: add Relater.getRelatedCount()
				int mediaCnt = oase.getRelater().getRelated(person, "base_medium", null).length;
				userInfo.setChildText("media", mediaCnt+"");

				// Add location
				// TODO: add g_location related to persons
				TrackLogic trackLogic = new TrackLogic(oase);
				Track activeTrack = trackLogic.getActiveTrack(person.getId());
				if (activeTrack != null) {
					Location loc = (Location) activeTrack.getRelatedObject(Location.class, "lastpt");
					if (loc != null) {
						GeoPoint pt = loc.getLocation();
						userInfo.setChildText("lon", pt.lon + "");
						userInfo.setChildText("lat", pt.lat + "");
						userInfo.setChildText("lonlattime", pt.timestamp + "");
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
			} else if (command.equals(CMD_QUERY_USER_IMAGE)) {
				String loginName = getParameter(request, PAR_USER_NAME, null);
				throwOnMissingParm(PAR_USER_NAME, loginName);

				Record person = getPersonForLoginName(oase, loginName);
				if (person == null) {
					throw new IllegalArgumentException("No person found for loginname=" + loginName);
				}

				Record[] thumbRecords = oase.getRelater().getRelated(person, "base_medium", "thumb");

				result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);

				if (thumbRecords.length > 0) {
					result.addChild(thumbRecords[0].toXML());
				}
			} else if (command.equals(CMD_GET_TRACK)) {
				TrackLogic trackLogic = new TrackLogic(oase);
				String id = getParameter(request, "id", null);
				throwOnMissingParm("id", id);
				String format = getParameter(request, "format", "gtx");
				String attrs = getParameter(request, "attrs", null);
				boolean media = getParameter(request, "media", "true").equals("true");
				long minPtDist = Long.parseLong(getParameter(request, "mindist", "0"));
				int maxPoint = Integer.parseInt(getParameter(request, "maxpoints", "-1"));
				result = trackLogic.export(id, format, attrs, media, minPtDist, maxPoint);
			} else if (command.equals(CMD_DESCRIBE)) {
				// Return documentation file
				result = null;
				response.sendRedirect("get-usage.txt");
			} else {
				result = new JXElement(TAG_ERROR);
				result.setText("unknown command " + command);
				log.warn("unknown command " + command);
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

	// Defines optional app-specific command processing
%>
<%@ include file="myget.jsp" %>
<%
	// Main handling below


	// Start performance timing
	long t1 = Sys.now();
	JXElement result = null;

	// Get global Oase (DB) session.
	try {
		// Use one Oase session
		if (oase == null) {
			oase = (Oase) application.getAttribute("oaseq");
			if (oase == null) {
				// First time: create and save in app context
				String oaseContextId = Amuse.server.getPortal().getId();
				oase = Oase.createOaseSession(oaseContextId);
				application.setAttribute("oaseq", oase);
				ComponentDef[] cDefs = OaseConfig.getOaseContextConfig(oaseContextId).getStoreContextConfig().getSourceDefs();
				for (int i=0; i < cDefs.length; i++) {
					if (cDefs[i].getId().equals(StoreContextConfig.SOURCE_DB) && cDefs[i].hasProperty("randomfun")) {
						DB_RANDOM_FUN = cDefs[i].getProperty("randomfun");
					}
				}
			}
		}
	} catch (Throwable th) {
		result = new JXElement(TAG_ERROR);
		result.setText("error creating oase session" + th);
		log.error("error creating oase session", th);
	}

	// Try optional app-specific command handling (see myget.jsp)
	if (result == null) {
		result = doMyCommand(request, response);
	}

	// perform base command and return XML result
	if (result == null) {
		result = doCommand(request, response);
	}

	// Send XML response to client (is null when redirected)
	if (result != null) {
		// Get command parameter
		String command = getParameter(request, PAR_CMD, CMD_DESCRIBE);
		// Get command parameter
		String output = getParameter(request, PAR_OUTPUT, OUTPUT_XML);
		result.setAttr("cnt", result.getChildCount());

		if (output.equals(OUTPUT_XML)) {
			response.setContentType("text/xml;charset=utf-8");
			try {
				Writer writer = response.getWriter();
				writer.write(result.toFormattedString());
				writer.flush();
				writer.close();
				} catch (Throwable th) {
				log.info("error " + command + " writing response");
			}
/*		} else if (output.equals(OUTPUT_JSON)) {
			response.setContentType("text/plain;charset=utf-8");
			try {
				Writer writer = response.getWriter();
				JSONWriter jsonWriter = new JSONWriter(writer);
				jsonWriter.object();
				jsonWriter.key(result.getTag());

				jsonWriter.object();
				jsonWriter.key("count");
				jsonWriter.value(result.getChildCount());
				jsonWriter.key("records");
				jsonWriter.array();
				Vector records = result.getChildren();
				JXElement rec;
				for (int i=0; i < records.size(); i ++) {
					jsonWriter.object();
					rec = (JXElement) records.get(i);
					Vector fields = rec.getChildren();
					JXElement field;
					for (int j=0; j < fields.size(); j++) {
						field = (JXElement) fields.get(j);
						jsonWriter.key(field.getTag());
						jsonWriter.value(field.getText());
					}
					jsonWriter.endObject();
				}
				jsonWriter.endArray();
				jsonWriter.endObject();

				jsonWriter.endObject();
				//writer.write(result.toFormattedString());
				writer.flush();
				writer.close();
			} catch (Throwable th) {
				log.info("error " + command + " writing response th=" + th);
			}  */
		} else if (output.equals(OUTPUT_JSON)) {
			// response.setContentType("text/plain;charset=utf-8");
			try {
				Writer writer = response.getWriter();
				new JSONQueryEncoder().encode(result, writer);
				Vector records = result.getChildren();

				writer.flush();
				writer.close();
			} catch (Throwable th) {
				log.info("error " + command + " writing response th=" + th);
			}
		}
		if (command.indexOf("info") == -1) {
			log.info("[" + oase.getOaseSession().getContextId() + "] cmd=" + command + " rsp=" + result.getTag() + " childcount=" + result.getChildCount() + " dt=" + (Sys.now() - t1) + " ms");
		}

	}
%>