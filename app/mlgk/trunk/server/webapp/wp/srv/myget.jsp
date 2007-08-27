<%@ page import="nl.justobjects.jox.dom.JXElement"%>
<%@ page import="org.walkandplay.server.control.GamePlayHandler"%>
<%@ page import="javax.servlet.http.HttpServletRequest"%>
<%@ page import="javax.servlet.http.HttpServletResponse"%>
<%@ page import="org.keyworx.utopia.core.util.Oase"%>
<%@ page import="org.geotracing.handler.QueryLogic"%>
<%@ page import="org.walkandplay.server.logic.WPQueryLogic"%>
<%!

	// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
	// Distributable under LGPL license. See terms of license at gnu.org.

	// This JSP is included in get.jsp and can be used to implement application-specific
	// commands (queries, exports). See get.jsp (method doCommand()) for
	// doing a command and returning a result.
	// doMyCommand() is called first from get.jsp to handle new or overridden commands.
	//
	// $Id$

	/** Performs application-specific command and returns XML result. */
	public JXElement doMyCommand(HttpServletRequest request, HttpServletResponse response)  {
		JXElement result=null;
		String command = getParameter(request, PAR_CMD, CMD_DESCRIBE);
		try {
		   // do application-specific command handling here
		   // if not handled return null
			if (command.equals("get-gameplay-events")) {
				String gamePlayId = getParameter(request, PAR_ID, null);
				throwOnMissingParm(PAR_ID, gamePlayId);
				result = new JXElement("gameplay");
				result.setAttr("id", gamePlayId);

				Oase oase = QueryLogic.getOase();
				result.addChildren(WPQueryLogic.getGamePlayEvents(Integer.parseInt(gamePlayId)));

			}

		} catch (Throwable t) {
			result = new JXElement(TAG_ERROR);
			result.setText("Unexpected Error during query " + t);
			log.error("Unexpected Error during query cmd=" + command, t);
		}
		return result;
	}
%>
