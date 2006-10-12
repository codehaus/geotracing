 <%@ page import="nl.justobjects.jox.dom.JXElement,
				 org.geotracing.server.QueryHandler,
				 org.geotracing.server.TrackLogic,
				 org.keyworx.amuse.core.Amuse,
				 org.keyworx.amuse.core.Protocol,
				 org.keyworx.common.log.Log,
				 org.keyworx.common.log.Logging,
				 org.keyworx.common.util.Sys,
				 org.keyworx.oase.api.Finder,
				 org.keyworx.oase.api.Record,
				  org.keyworx.oase.api.Relater"%>
 <%@ page import="org.keyworx.utopia.core.util.Oase"%>
 <%@ page import="javax.servlet.ServletRequest"%>
 <%@ page import="java.io.Writer"%>
 <%@ page import="java.text.SimpleDateFormat"%>
 <%@ page import="java.util.Date"%>
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
	public static final String CMD_QUERY_ALL_TRACKS = "q-all-tracks";
	public static final String CMD_QUERY_RECENT_TRACKS = "q-recent-tracks";
	public static final String CMD_QUERY_TRACKS_BY_USER = "q-tracks-by-user";
	public static final String CMD_QUERY_ALL_USERS = "q-all-users";
    public static final String CMD_QUERY_RANDOM_TRACK = "q-random-track";
	public static final String CMD_QUERY_LOCATIVE_MEDIA = "q-locative-media";
	public static final String CMD_QUERY_RECENT_MEDIA = "q-recent-media";
	public static final String CMD_QUERY_MEDIA_BY_USER = "q-media-by-user";
	public static final String CMD_QUERY_USER_IMAGE = "q-user-image";
	public static final String CMD_QUERY_USER_BY_NAME = "q-user-by-name";
	public static final String CMD_QUERY_MEDIUM_INFO = "q-medium-info";
	public static final String CMD_QUERY_POIS = "q-pois";
	public static final String CMD_GET_TRACK = "get-track";
	public static final String CMD_DESCRIBE = "describe";
	public static final String PAR_ID = "id";
	public static final String PAR_CMD = "cmd";
	public static final String PAR_BBOX= "bbox";
	public static final String PAR_USER_NAME= "user";
	public static final String TAG_ERROR = "error";

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
	public void throwOnMissingParm(String aName, String aValue) throws IllegalArgumentException  {
		if (aValue == null || aValue.length() == 0) {
			throw new IllegalArgumentException("Missing parameter=" + aName);
		}
	}

	/** Adds Bounding box WHERE constraint. */
	public String addBBoxConstraint(String bboxParm, String where) throws Exception  {
		String[] bbox = bboxParm.split(",");
		where = where == null ? "" : where + " AND";
		where = where
			+ " g_location.lon >= " + bbox[0]
			+ " AND g_location.lat >= " + bbox[1]
			+ " AND g_location.lon <= " + bbox[2]
			+ " AND g_location.lat <= " + bbox[3];
		return where;
	}

	 /** Adds g_location attrs to query response records. */
	public void addLocationAttrs(JXElement rsp) throws Exception  {
		Vector records = rsp.getChildren();
		Finder finder = oase.getFinder();
		Relater relater = oase.getRelater();

		// Start with empty
		rsp.removeChildren();
		JXElement nextRecordElm;
		String recordId;
		Record record;
		Record[] locationRecords;
		for (int i=0; i < records.size(); i++) {
			nextRecordElm = (JXElement) records.get(i);

			// SIngle table responses have id in attr
			// Multi table in record elm
			// TODO FIX!!
			recordId = nextRecordElm.getChildText("id");
			if (recordId == null) {
				recordId = nextRecordElm.getAttr("id");
			}

			// Must have the real record
			record = finder.read(Integer.parseInt(recordId));

			// Get related person
			locationRecords = relater.getRelated(record, "g_location", null);

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

	/** Adds account and person attrs to query response records. */
	public void addUserAttrs(JXElement rsp) throws Exception  {
		Vector records = rsp.getChildren();
		Finder finder = oase.getFinder();
		Relater relater = oase.getRelater();

		// Start with empty
		rsp.removeChildren();
		JXElement nextRecordElm;
		String recordId;
		Record record;
		Record[] personRecords, accountRecords, thumbRecords;
		for (int i=0; i < records.size(); i++) {
			nextRecordElm = (JXElement) records.get(i);

			// SIngle table responses have id in attr
			// Multi table in record elm
			// TODO FIX!!
			recordId = nextRecordElm.getChildText("id");
			if (recordId == null) {
				recordId = nextRecordElm.getAttr("id");
			}

			// Must have the real record
			record = finder.read(Integer.parseInt(recordId));

			// Get related person
			personRecords = relater.getRelated(record, "utopia_person", null);
			if (personRecords.length == 0) {
				log.warn("no person record found for record id=" + record.getId() + " table=" + record.getTableName());
				continue;
			}

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

			// Add the user attrs
			nextRecordElm.setChildText("loginname", accountRecords[0].getStringField("loginname"));
			if (personRecords[0].getStringField("extra") != null) {
				nextRecordElm.setChildText("extra", personRecords[0].getStringField("extra"));
			}

			/* thumbRecords = relater.getRelated(personRecords[0], "base_medium", "thumb");
			if (thumbRecords.length > 0) {
				nextRecordElm.setChildText("thumbid", thumbRecords[0].getId() + "");
			} */

			// Add to response
			rsp.addChild(nextRecordElm);
		}
	}

	Record getAccount(Oase oase, String aLoginName) throws Exception  {
		Finder finder = oase.getFinder();
		Record[] result = finder.queryTable("utopia_account", "WHERE utopia_account.loginname = '" + aLoginName + "'");
		return result.length == 0 ? null : result[0];
	}

	Record getPersonForLoginName(Oase oase, String aLoginName) throws Exception  {
		Record account = getAccount(oase, aLoginName);
		if (account == null) {
			return null;
		}
		Record[] result =  oase.getRelater().getRelated(account, "utopia_person", null);
		return result.length == 0 ? null : result[0];
	}

	/** Performs command and returns XML result. */
	public JXElement doCommand(HttpServletRequest request, HttpServletResponse response)  {
		JXElement result;
		String command = getParameter(request, PAR_CMD, CMD_DESCRIBE);
		try {

			if (command.equals(CMD_QUERY_STORE)) {
				// Generic query
				String tables = getParameter(request, "tables", null);
				String fields = getParameter(request, "fields", null);
				String where = getParameter(request, "where", null);
				String relations = getParameter(request, "rels", null);
				String postCond = getParameter(request, "postcond", null);
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);
			} else if (command.equals(CMD_QUERY_ACTIVE_TRACKS)) {
				// Niet mooi maar wel optimized!!

				// First get all active tracks
				String tables = "g_track,g_location";
				String fields = "g_track.id,g_track.name,g_location.lon,g_location.lat,g_location.time";
				String where = "g_track.state=1";
				String relations = "g_track,g_location,lastpt";
				String postCond = null;
				result = QueryHandler.queryStoreReq2(oase, tables, fields, where, relations, postCond);

				// Add account/person attrs to each record
				addUserAttrs(result);

				// Originele query (duurde heeeeel lang)

	/*			String tables = "utopia_person,utopia_account,g_track,g_location";
				String fields = "g_track.id,g_track.name,utopia_person.extra,utopia_account.loginname,g_location.lon,g_location.lat";
				String where = "g_track.state=1";
				String relations = "g_track,g_location,lastpt;g_track,utopia_person;utopia_account,utopia_person";
				String postCond = "ORDER BY utopia_account.loginname";
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);   */
			} else if (command.equals(CMD_QUERY_ALL_TRACKS)) {
				String tables = "utopia_person,utopia_account,g_track,g_location";
				String fields = "g_track.id,g_track.name,g_track.state,utopia_account.loginname,g_location.lon,g_location.lat,g_location.time";
				String where = null;
				String relations = "g_track,g_location,lastpt;g_track,utopia_person;utopia_person,utopia_account";
				String postCond = "ORDER BY utopia_account.loginname";
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);
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
				result = QueryHandler.queryStoreReq2(oase, tables, fields, where, relations, postCond);
			} else if (command.equals(CMD_QUERY_ALL_USERS)) {
				String tables = "utopia_person,utopia_account,utopia_role";
				String fields = "utopia_person.id,utopia_account.id AS accountid, utopia_account.loginname";
				String where = "utopia_role.name = 'user' AND utopia_account.state = 1";
				String relations = "utopia_account,utopia_person;utopia_account,utopia_role";
				String postCond = "ORDER BY utopia_account.loginname";
				result = QueryHandler.queryStoreReq2(oase, tables, fields, where, relations, postCond);
			} else if (command.equals(CMD_QUERY_RECENT_TRACKS)) {
				// Optional number
				String max = getParameter(request, "max", "5");
				Finder finder = oase.getFinder();
				Record[] trackRecords = finder.freeQuery("select * from g_track order by enddate desc limit " + max);
				result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
				for (int i=0; i < trackRecords.length; i++) {
					result.addChild(trackRecords[i].toXML());
				}

				// Add account/person attrs to each record
				addUserAttrs(result);

			} else if (command.equals(CMD_QUERY_RANDOM_TRACK)) {
				// (do this in two queries for performance reasons)

				// First get random track
				String tables = "g_track";
				String fields = "g_track.id";
				String where = null;
				String relations = null;
				String postCond = "ORDER BY RAND() LIMIT 1";
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);
				// log.info("result=" + result);
				String trackId = result.getChildAt(0).getAttr("id");

				// Now get all info for track
				tables = "g_track,utopia_person,utopia_account,g_location";
				fields = "g_track.id,g_track.name,g_track.state,utopia_person.extra,utopia_account.loginname,g_location.lon,g_location.lat,g_location.time";
				where = "g_track.id = " + trackId;
				relations = "g_track,g_location,lastpt;g_track,utopia_person;utopia_account,utopia_person";
				postCond = null;
				result = QueryHandler.queryStoreReq2(oase, tables, fields, where, relations, postCond);

			} else if (command.equals(CMD_QUERY_LOCATIVE_MEDIA)) {
				// See http://www.petefreitag.com/item/466.cfm
				// LAST N: select * from table where key > (select max(key) - n from table)
				String tables = "base_medium,g_location";
				String fields = "base_medium.id,base_medium.kind,base_medium.mime,base_medium.name,base_medium.description,base_medium.creationdate,g_location.lon,g_location.lat";
				String where=null;
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
					postCond = "ORDER BY RAND()";
				} else {
					postCond = "ORDER BY base_medium.creationdate DESC";
				}

				// Limit
				String limitParm = getParameter(request, "max", null);
				if (limitParm != null) {
					postCond += " LIMIT " + Integer.parseInt(limitParm);
				}
				// log.info("where=[" + where + "] postCond=[" + postCond +"]");
				result = QueryHandler.queryStoreReq2(oase, tables, fields, where, relations, postCond);

				// Add account/person attrs to each record
				addUserAttrs(result);

			} else if (command.equals(CMD_QUERY_RECENT_MEDIA)) {
				// Optional number
				String max = getParameter(request, "max", "10");
				Finder finder = oase.getFinder();
				Record[] mediumRecords = finder.freeQuery("select * from base_medium order by creationdate desc limit " + max);
				result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
				for (int i=0; i < mediumRecords.length; i++) {
					result.addChild(mediumRecords[i].toXML());
				}

				// Add lon/lat attrs to each record
				addLocationAttrs(result);

				// Add account/person attrs to each record
				addUserAttrs(result);
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
				result = QueryHandler.queryStoreReq2(oase, tables, fields, where, relations, postCond);
				String personId = ((JXElement)result.getChildren().get(0)).getChildText("id");

				// Now query tracks related to person id
				tables = "utopia_person,base_medium";
				fields = "base_medium.id,base_medium.kind,base_medium.mime,base_medium.name,base_medium.description,base_medium.creationdate";
				where = "utopia_person.id = " + personId;
				relations = "base_medium,utopia_person";
				postCond = "ORDER BY base_medium.id DESC";
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);

				// Now add login name to each record element in response
				Vector records = result.getChildren();
				JXElement nextRecord;
				String creationDate;
				for (int i=0; i < records.size(); i++) {
					nextRecord = (JXElement)records.get(i);
					creationDate = nextRecord.getChildText("creationdate");
					creationDate = DATE_FORMAT.format(new Date(Long.parseLong(creationDate)));
					nextRecord.setChildText("fcreationdate", creationDate);
					nextRecord.setChildText("loginname", userName);
				}

				addLocationAttrs(result);
			} else if (command.equals(CMD_QUERY_POIS)) {
				// See http://www.petefreitag.com/item/466.cfm
				String tables = "g_poi,g_location";
				String fields = "g_location.lon,g_location.lat,g_poi.id,g_poi.name,g_poi.description,g_poi.type,g_poi.time";
				String where=null;
				String relations = "g_location,g_poi";
				String postCond;

				// WHERE clause
				// Optional media type
				String type = getParameter(request, "type", null);
				if (type != null) {
					where = "g_poi.type = '" + type + "'";
				}

				String bboxParm = getParameter(request, PAR_BBOX, null);
				if (bboxParm != null) {
					where = addBBoxConstraint(bboxParm, where);
				}

				// POSTCONDITION
				String random = getParameter(request, "random", "false");
				if (random.equals("true")) {
					postCond = "ORDER BY RAND()";
				} else {
					postCond = "ORDER BY g_poi.id DESC";
				}

				// Limit
				String limitParm = getParameter(request, "max", null);
				if (limitParm != null) {
					postCond += " LIMIT " + Integer.parseInt(limitParm);
				}
				// log.info("where=[" + where + "] postCond=[" + postCond +"]");
				result = QueryHandler.queryStoreReq2(oase, tables, fields, where, relations, postCond);

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

			} else if (command.equals(CMD_QUERY_MEDIUM_INFO)) {
				String id = getParameter(request, PAR_ID, null);
				throwOnMissingParm(PAR_ID, id);

				// First get all active tracks
				String tables = "base_medium";
				String fields = null;
				String where = "base_medium.id = " + id;
				String relations = null;
				String postCond = null;
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);

				// Add account/person attrs to each record
				addUserAttrs(result);

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
				boolean pois = getParameter(request, "pois", "true").equals("true");
				long minPtDist = Long.parseLong(getParameter(request, "mindist", "0"));
				result = trackLogic.export(id, format, attrs, media, pois, minPtDist);
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

	response.setContentType("text/xml;charset=utf-8");

	// Start performance timing
	long t1 = Sys.now();
	JXElement result=null;

    // Get global Oase (DB) session.
 	try {
		// Use one Oase session
		 if (oase == null) {
			oase = (Oase) application.getAttribute("oase");
			if (oase == null) {
				// First time: create and save in app context
				oase = Oase.createOaseSession(Amuse.server.getPortal().getId());
				application.setAttribute("oase", oase);
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

		 try {
			 result.setAttr("cnt", result.getChildCount());
			 Writer writer = response.getWriter();
			 writer.write(result.toFormattedString());
			 writer.flush();
			 writer.close();
			 log.info("[" + oase.getOaseSession().getContextId() + "] cmd=" + command + " rsp=" + result.getTag() + " childcount=" + result.getChildCount() + " dt=" + (Sys.now() -t1) + " ms");
		 } catch (Throwable th) {
	  		 log.info("error " + command + " writing response");
		 }
	 }
%>