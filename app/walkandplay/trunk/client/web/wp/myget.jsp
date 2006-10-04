<%@ page import="nl.justobjects.jox.dom.JXElement"%>
<%@ page import="org.geotracing.server.QueryHandler" %>
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

    public static final String CMD_LICENSE_GETLIST = "wp-license-getlist";

    public JXElement doMyCommand(HttpServletRequest request, HttpServletResponse response) {
        JXElement result = null;
        String command = getParameter(request, PAR_CMD, CMD_DESCRIBE);
        try {
            // do application-specific command handling here
            // if not handled return null
            if (command.equals(CMD_LICENSE_GETLIST)) {
                // Generic query
                String tables = "cc_license";
                String fields = null;
                String where = null;
                String relations = null;
                String postCond = null;
                result = QueryHandler.queryStoreReq(oase, tables, fields, where, relations, postCond);
            } else {

            }
        } catch (Throwable t) {
            result = new JXElement(TAG_ERROR);
            result.setText("Unexpected Error during query " + t);
            log.error("Unexpected Error during query", t);
        }
        return result;
    }
%>