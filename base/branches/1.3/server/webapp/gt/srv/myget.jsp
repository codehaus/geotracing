<%@ page import="nl.justobjects.jox.dom.JXElement"%>
<%@ page import="javax.servlet.http.HttpServletRequest"%>
<%@ page import="javax.servlet.http.HttpServletResponse"%>
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
	public JXElement doMyCommand(HttpServletRequest request, HttpServletResponse response)  {
		JXElement result=null;
		try {
		   // do application-specific command handling here
		   // if not handled return null
		} catch (Throwable t) {
			result = new JXElement(TAG_ERROR);
			result.setText("Unexpected Error during query " + t);
			log.error("Unexpected Error during query", t);
		}
		return result;
	}
%>