<%@ page import="nl.justobjects.jox.dom.JXElement"%>
<%@ page import="org.geotracing.server.QueryHandler"%>
<%@ page import="org.keyworx.oase.api.Record"%>
<%@ page import="org.keyworx.oase.api.Finder"%>
<%!

	// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
	// Distributable under LGPL license. See terms of license at gnu.org.

	// This JSP is included in get.jsp and can be used to implement application-specific
	// commands (queries, exports). See get.jsp (method doCommand()) for
	// doing a command and returning a result.
	// doMyCommand() is called first from get.jsp to handle new or overridden commands.
	//
	// $Id: myget.jsp,v 1.1 2006-08-28 09:35:19 just Exp $
	public static final String CMD_QUERY_TRACKS_BY_USER_SOTC = "q-tracks-by-user-sotc";
	public static final String CMD_QUERY_RANDOM_TRACK_SOTC = "q-random-track-sotc";
	public static final String CMD_QUERY_RANDOM_IMAGE_SOTC = "q-random-image-sotc";
	
	/** Performs application-specific command and returns XML result. */
	public JXElement doMyCommand(HttpServletRequest request, HttpServletResponse response)  {
		JXElement result=null;
		String command = getParameter(request, PAR_CMD, CMD_DESCRIBE);
		try {
		   // do application-specific command handling here
		   // if not handled return null
			if (command.equals(CMD_QUERY_TRACKS_BY_USER_SOTC)) {
				String userName = getParameter(request, PAR_USER_NAME, null);
				throwOnMissingParm(PAR_USER_NAME, userName);

				String tables = "utopia_person,utopia_account,g_track";
				String fields = "g_track.id,g_track.name,utopia_account.loginname,utopia_person.extra";
				String where = "utopia_account.loginname = '" + userName + "'";
				String relations = "g_track,utopia_person;utopia_person,utopia_account";
				String postCond = "ORDER BY g_track.name";
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);
			} else if (command.equals(CMD_QUERY_RANDOM_TRACK_SOTC)) {
				while (true) {
					// (do this in two queries for performance reasons)
					// First get random track
					String tables = "g_track";
					String fields = "g_track.id";
					String where = null;
					String relations = null;
					String postCond = "ORDER BY RAND() LIMIT 1";
					result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);
					// log.info("rsp=" + rsp);
					String trackId = result.getChildAt(0).getAttr("id");

					// Now get all info for track (note: account may not be active!)
					tables = "utopia_person,utopia_account,g_track,g_location";
					fields = "g_track.id,g_track.name,g_track.state,utopia_account.loginname,utopia_person.extra,g_location.lon,g_location.lat";
					where = "g_track.id = " + trackId + " AND utopia_account.state = 1";
					relations = "g_track,g_location,lastpt;g_track,utopia_person;utopia_person,utopia_account";
					postCond = null;
					result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);

					// Found random track for active account
					if (result.hasChildren()) {
						break;
					}
				}
			} else if (command.equals(CMD_QUERY_RANDOM_IMAGE_SOTC)) {
				String loginName = getParameter(request, PAR_USER_NAME, null);
				throwOnMissingParm(PAR_USER_NAME, loginName);
				Record person = getPersonForLoginName(oase, loginName);
				String tables = "utopia_person,base_medium";
				String fields = "base_medium.id,base_medium.name,base_medium.description,base_medium.creationdate";
				String where = "utopia_person.id = " + person.getId();
				String relations = "base_medium,utopia_person";
				String postCond = "ORDER BY RAND() LIMIT 1";
				result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);
				((JXElement) result.getChildren().get(0)).setChildText("loginname", loginName);
			}
		} catch (Throwable t) {
			result = new JXElement(TAG_ERROR);
			result.setText("Unexpected Error during query " + t);
			log.error("Unexpected Error during query cmd=" + command, t);
		}
		return result;
	}
%>
