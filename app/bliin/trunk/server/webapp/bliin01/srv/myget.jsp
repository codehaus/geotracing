<%@ page import="nl.justobjects.jox.dom.JXElement" %>
<%@ page import="org.geotracing.server.QueryHandler"%>
<%!
	static JXElement allLocMedia = null;
	static Object semaphore = new Object();
	static boolean allLocMediaAvail;

	
	// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
	// Distributable under LGPL license. See terms of license at gnu.org.

	// This JSP is included in get.jsp and can be used to implement application-specific
	// commands (queries, exports). See get.jsp (method doCommand()) for
	// doing a command and returning a result.
	// doMyCommand() is called first from get.jsp to handle new or overridden commands.
	//
	// $Id$

	/** Performs application-specific command and returns XML result. */
	public JXElement doMyCommand(HttpServletRequest request, HttpServletResponse response) {
		JXElement result = null;
		String command = getParameter(request, PAR_CMD, CMD_DESCRIBE);
		try {
			// do application-specific command handling here
			// if not handled return null
			if (command.equals(CMD_QUERY_LOCATIVE_MEDIA)) {
				// See http://www.petefreitag.com/item/466.cfm
				// LAST N: select * from table where key > (select max(key) - n from table)
				String tables = "base_medium,g_location";
				String fields = "base_medium.id,base_medium.kind,base_medium.name,base_medium.creationdate,g_location.lon,g_location.lat";
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
					postCond = "ORDER BY RAND()";
				} else {
//                              postCond = "ORDER BY base_medium.creationdate";
					postCond = "";
				}

				// Limit
				String limitParm = getParameter(request, "max", null);
				if (limitParm != null) {
					postCond += " LIMIT " + Integer.parseInt(limitParm);
				}

				// If whole world is asked check if cached result can be sent.
				if (bboxParm == null && type == null && random.equals("false") && limitParm == null) {

				}
				// log.info("[bliin]: q-locative-media: bbox=" + bboxParm + " post=" + postCond);
				result = QueryHandler.queryStoreReq2(oase, tables, fields, where, relations, postCond);
				// Add account/person attrs to each record
				// addUserAttrs(rsp);
			}
		} catch (Throwable t) {
			result = new JXElement(TAG_ERROR);
			result.setText("Unexpected Error during query " + t);
			log.error("Unexpected Error during doMyCommand query", t);
		}
		return result;
	}
%>
