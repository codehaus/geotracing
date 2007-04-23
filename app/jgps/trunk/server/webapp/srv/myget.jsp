<%@ page import="nl.justobjects.jox.dom.JXElement" %>
<%@ page import="org.keyworx.oase.api.Record" %>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import="javax.servlet.http.HttpServletResponse" %>
<%@ page import="org.geotracing.handler.QueryLogic" %>
<%@ page import="org.keyworx.utopia.core.util.Oase" %>
<%!

	// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
	// Distributable under LGPL license. See terms of license at gnu.org.

	// This JSP is included in get.jsp and can be used to implement application-specific
	// commands (queries, exports). See get.jsp (method doCommand()) for
	// doing a command and returning a result.
	// doMyCommand() is called first from get.jsp to handle new or overridden commands.
	//
	// $Id: myget.jsp,v 1.1 2006-08-28 09:35:19 just Exp $

	/** Performs application-specific command and returns XML result. */
	public JXElement doMyCommand(HttpServletRequest request, HttpServletResponse response) {
		JXElement result = null;
		String command = getParameter(request, PAR_CMD, CMD_DESCRIBE);
		try {
			// do application-specific command handling here
			// if not handled return null
			// http://localhost/jgps/srv/get.jsp?cmd=q-locations-by-user&user=dirk
			// JS: SRV.get('q-locations-by-user', 'user', 'dirk');
			if (command.equals("q-locations-by-user")) {
				Oase oase = QueryLogic.getOase();
				String loginName = getParameter(request, "user", null);
				// We must have a related person otherwise we fail
					// QueryLogic.throwOnMissingParm(PAR_USER_NAME, loginName);
					Record person = QueryLogic.getPersonForLoginName(oase, loginName);
					String tables = "utopia_person,base_medium,g_location";
					String fields = "g_location.id,g_location.name,g_location.lon,g_location.lat,g_location.type,g_location.subtype,base_medium.id AS mediumid,base_medium.kind,base_medium.mime,base_medium.name AS mediumname,base_medium.description,base_medium.creationdate,base_medium.extra";
					String where = "utopia_person.id = " + person.getId();
					String relations = "utopia_person,g_location;g_location,base_medium";
					String postCond = null;

					result = QueryLogic.queryStoreReq(oase, tables, fields, where, relations, postCond);

					log.info("result=" + result);
			}
		} catch (Throwable t) {
			result = new JXElement(TAG_ERROR);
			result.setText("Unexpected Error during myget " + t);
			log.error("Unexpected Error during myget cmd=" + command, t);
		}
		return result;
	}
%>
