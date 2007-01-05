<%@ page import="
				com.oreilly.servlet.MultipartRequest,
                 nl.justobjects.jox.dom.JXElement,
                 org.geotracing.server.TrackLogic,
                 org.keyworx.amuse.client.web.HttpConnector,
                 org.keyworx.amuse.core.Amuse,
                 org.keyworx.amuse.core.Protocol,
                 org.keyworx.oase.util.Log,
                 org.keyworx.utopia.core.data.Application,
                 org.keyworx.utopia.core.data.Person,
                 org.keyworx.utopia.core.data.Portal,
                 org.keyworx.utopia.core.logic.PersonLogic,
                 org.keyworx.utopia.core.util.Core" %>
<%@ page import="org.keyworx.utopia.core.util.Oase" %>
<%@ page import="javax.servlet.ServletRequest" %>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import="javax.servlet.http.HttpServletResponse" %>
<%@ page import="javax.servlet.http.HttpSession" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.geotracing.server.QueryHandler" %>
<%@ page import="org.keyworx.utopia.core.data.Role" %>
<%@ page import="org.keyworx.oase.api.Record" %>
<%@ page import="java.text.StringCharacterIterator" %>
<%@ page import="java.text.CharacterIterator" %>

<%@ include file="model.jsp" %>

<%!
    Oase oase = null;

    /**
     * Replace characters having special meaning <em>inside</em> HTML tags
     * with their escaped equivalents, using character entities such as <tt>'&amp;'</tt>.
     *
     * <P>The escaped characters are :
     * <ul>
     * <li> <
     * <li> >
     * <li> "
     * <li> '
     * <li> \
     * <li> &
     * </ul>
     *
     * <P>This method ensures that arbitrary text appearing inside a tag does not "confuse"
     * the tag. For example, <tt>HREF='Blah.do?Page=1&Sort=ASC'</tt>
     * does not comply with strict HTML because of the ampersand, and should be changed to
     * <tt>HREF='Blah.do?Page=1&amp;Sort=ASC'</tt>. This is commonly seen in building
     * query strings. (In JSTL, the c:url tag performs this task automatically.)
     */
    public String forHTMLTag(String aTagFragment) {
        final StringBuffer result = new StringBuffer();

        final StringCharacterIterator iterator = new StringCharacterIterator(aTagFragment);
        char character = iterator.current();
        while (character != CharacterIterator.DONE) {
            if (character == '<') {
                result.append("&lt;");
            } else if (character == '>') {
                result.append("&gt;");
            } else if (character == '\"') {
                result.append("&quot;");
            } else if (character == '\'') {
                result.append("&#039;");
            } else if (character == '\\') {
                result.append("&#092;");
            } else if (character == '&') {
                result.append("&amp;");
            } else {
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }

    String getParameter(ServletRequest req, String name, String defaultValue) {
        String value = req.getParameter(name);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }

        return value.trim();
    }

    String getParameter(MultipartRequest req, String name, String defaultValue) {
        String value = req.getParameter(name);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }

        return value.trim();
    }


    private void doCommand(String command, HttpServletRequest request, HttpServletResponse response, HttpSession session, Model model) {
        try {
            model.setResultMsg("");
			if ((command.indexOf("delete") != -1 || command.indexOf("edit") != -1 || command.indexOf("update") != -1) && !model.isLoggedIn())
            {
                command = "nav-login";
            }

            model.set(ATTR_PAGE_URL, "edit.jsp");
            if ("login".equals(command)) {
                String userName = request.getParameter("username");
                String password = request.getParameter("password");
                model.set(ATTR_STATUS_MSG, STATUS_MSG_NULL);

                HttpConnector.logout(session);
                JXElement rspElm = HttpConnector.login(session, Amuse.server.getPortal().getId(), "geoapp", Role.USER_ROLE_VALUE, userName, password, null);
                if (Protocol.isNegativeResponse(rspElm)) {
                    model.setResultMsg("naam en/of wachtwoord onbekend");
                    return;
                }
                model.set(ATTR_USER_NAME, userName);
                model.set(ATTR_PASSWORD, password);
                model.set(ATTR_STATUS_MSG, STATUS_MSG_LOGGED_IN + userName);

                model.set(ATTR_CONTENT_URL, "inc-edit-tracks.jsp");
                model.setResultMsg("login ok");
            } else if ("logout".equals(command)) {
                HttpConnector.logout(session);
                model.reset();
                model.set(ATTR_PAGE_URL, "deelnemers.jsp");
                model.setResultMsg("logout ok");
            } else if ("register".equals(command)) {
                Portal portal = null;
                ArrayList portalList = oase.getByNameAndValue(Core.PORTAL, Portal.NAME_FIELD, Amuse.server.getPortal().getId());
                if (portalList != null && portalList.size() > 0) {
                    portal = (Portal) portalList.get(0);
                }
                PersonLogic personLogic = new PersonLogic(oase);
                /*public Person insertUser(String portalId, String applicationId, String organisationId, String organisationTag, String firstName, String lastName,
                                                             String birthDate, String street, String streetNr, String zipCode,
                                                             String city, String country, String phoneNr, String mobileNr,
                                                             String email, JXElement extra, String loginName, String password,
                                                             String key) throws UtopiaException { */

                String portalId = "" + portal.getId();
                Application application = (Application) oase.getByNameAndValue(Application.class, "name", "geoapp").get(0);
                String applicationId = "" + application.getId();
                String firstName = getParameter(request, "firstname", "anon");
                String lastName = getParameter(request, "lastName", "anon");
                String email = getParameter(request, "email", null);
                String loginName = getParameter(request, "loginname", null);
                String password = getParameter(request, "password", null);
                Person person = personLogic.insertUser(portalId, applicationId, null, null, firstName, lastName, null, null, null, null, null, null, null, null, email, null, loginName, password, null);
                model.set(ATTR_CONTENT_URL, "inc-home.html");
                model.set(ATTR_STATUS_MSG, STATUS_MSG_NULL);
                model.setResultMsg("register ok");


            } else if ("edit-medium".equals(command)) {
                String id = getParameter(request, "id", null);
                if (id == null) {
                    model.setResultMsg("geen medium id");
                    return;
                }
                String trackId = getParameter(request, "trackid", null);
                if (trackId == null) {
                    model.setResultMsg("geen track id");
                    return;
                }
                model.set(ATTR_CONTENT_URL, "inc-edit-medium.jsp?id=" + id + "&trackid=" + trackId);
                model.setResultMsg("edit-medium id=" + id + "&trackid=" + trackId);

            } else if ("edit-track".equals(command)) {
                String id = getParameter(request, "id", null);
                if (id == null) {
                    model.setResultMsg("geen medium id");
                    return;
                }
                model.set(ATTR_CONTENT_URL, "inc-edit-track.jsp?id=" + id);
            } else if ("edit-tracks".equals(command)) {
                model.set(ATTR_CONTENT_URL, "inc-edit-tracks.jsp");
            } else if ("delete-medium".equals(command)) {
                // Go back to track edit
                String trackId = getParameter(request, "trackid", null);
                if (trackId == null) {
                    model.setResultMsg("geen track id");
                    return;
                }
                String id = getParameter(request, "id", null);
                if (id == null) {
                    model.setResultMsg("geen medium id");
                    return;
                }

                model.set(ATTR_CONTENT_URL, "inc-delete-medium.jsp?id=" + id + "&trackid=" + trackId);

            } else if ("delete-medium-cnf".equals(command)) {
                // Go back to track edit
                String trackId = getParameter(request, "trackid", null);
                if (trackId == null) {
                    model.setResultMsg("geen track id");
                    return;
                }
                String id = getParameter(request, "id", null);
                if (id == null) {
                    model.setResultMsg("geen medium id");
                    return;
                }
                String cancel = getParameter(request, "cancel", null);
                if (cancel != null) {
                    model.set(ATTR_CONTENT_URL, "inc-edit-medium.jsp?id=" + id + "&trackid=" + trackId);
                    return;
                }
                JXElement req = new JXElement("medium-delete-req");
                JXElement medium = new JXElement("medium");
                req.setAttr("id", id);
                req.addChild(medium);
                JXElement rsp = HttpConnector.executeRequest(session, req);
                if (Protocol.isNegativeResponse(rsp)) {
                    model.setResultMsg("fout bij delete medium " + rsp.toEscapedString());
                    model.set(ATTR_CONTENT_URL, "inc-edit-medium.jsp?id=" + id + "&trackid=" + trackId);
                    return;
                } else {
                    model.setResultMsg("foto weggegooid met id=" + id);
                }
                model.set(ATTR_CONTENT_URL, "inc-edit-track.jsp?id=" + trackId);
            } else if ("update-medium".equals(command)) {
                // Go back to track edit
                String trackId = getParameter(request, "trackid", null);
                if (trackId == null) {
                    model.setResultMsg("geen track id");
                    return;
                }
                String cancel = getParameter(request, "cancel", null);
                if (cancel != null) {
                    model.set(ATTR_CONTENT_URL, "inc-edit-track.jsp?id=" + trackId);
                    return;
                }
                String id = getParameter(request, "id", null);
                if (id == null) {
                    model.setResultMsg("geen medium id");
                    return;
                }
                String name = getParameter(request, "name", null);
                if (name == null) {
                    model.setResultMsg("geen name");
                    return;
                }
                String description = getParameter(request, "description", null);
                if (description == null) {
                    model.setResultMsg("geen description");
                    return;
                }

                // Escape HTML stuff
                description = forHTMLTag(description);


                JXElement req = new JXElement("medium-update-req");
                JXElement medium = new JXElement("medium");
                req.setAttr("id", id);
                medium.setChildText("name", forHTMLTag(name));
                medium.setChildText("description", description);
                medium.setChildText("filename", "non-existing-file");
                req.addChild(medium);
                JXElement rsp = HttpConnector.executeRequest(session, req);
                if (Protocol.isNegativeResponse(rsp)) {
                    model.setResultMsg("fout bij update medium " + rsp.toEscapedString());
                    return;
                }

                model.set(ATTR_CONTENT_URL, "inc-edit-track.jsp?id=" + trackId);
            } else if ("nav-init".equals(command)) {
                model.set(ATTR_CONTENT_URL, "inc-login.html");
                model.setState(MODEL_STATE_READY);
            } else if ("nav-home".equals(command)) {
                model.set(ATTR_CONTENT_URL, "inc-home.html");
            } else if ("nav-login".equals(command)) {
				model.reset();
				model.set(ATTR_CONTENT_URL, "inc-login.html");
                 if (model.isLoggedIn()) {
                   //  model.set(ATTR_CONTENT_URL, "inc-edit-tracks.jsp");
                } else {
               }
            } else if ("nav-colofon".equals(command)) {
                model.set(ATTR_CONTENT_URL, "inc-colofon.html");
            } else if ("nav-contact".equals(command)) {
                model.set(ATTR_CONTENT_URL, "inc-contact.html");
            } else if ("nav-help".equals(command)) {
                model.set(ATTR_CONTENT_URL, "inc-help.html");
            } else if ("nav-live".equals(command)) {
                model.set(ATTR_PAGE_URL, "staalkaart2.jsp?cmd=live");
            } else if ("nav-staalkaart".equals(command)) {
                String log_id = getParameter(request, "login", "no_log_id");
                String log_name = getParameter(request, "loginName", "no_log_name");
                if (log_id != "no_log_id" && log_name != "no_log_name")
                    model.set(ATTR_PAGE_URL, "staalkaart2.jsp?cmd=staalkaart&login=" + log_id + "&loginName=" + log_name);
                else
                    model.set(ATTR_PAGE_URL, "staalkaart2.jsp?cmd=staalkaart");
            } else if ("nav-autoplay".equals(command)) {
                model.set(ATTR_PAGE_URL, "staalkaart2.jsp?cmd=autoplay");
            } else if ("media-import".equals(command)) {
/*			 String location = getParameter(request, "location", null);
			 if (location == null) {
				 result.setMessage("missing location parameter for media-import");
				 return result;
			 }
			 HashMap fields = null;
             String name = getParameter(request, MediaFiler.FIELD_NAME, null);
			 if (name != null) {
				 fields = new HashMap(2);
				 fields.put(MediaFiler.FIELD_NAME, name);
			 }
			 Record[] records = DB.importLocationMedia(location, fields, DBDefs.VAL_JUST, 0L);
			 result.setMessage("Import ok record count=" + records.length);   */
            } else if ("media-delete".equals(command)) {
/*			String idsParm = getParameter(request, "ids", null);
			if (idsParm == null) {
				result.setMessage("missing ids parameter for media-delete");
				return result;
			}

			String[] ids = idsParm.split(" ");
			DB.deleteLocationMedia(ids);
			result.setMessage("Delete ok count=" + ids.length);  */
            } else if ("track-delete".equals(command)) {
                String id = getParameter(request, "id", null);
                if (id == null) {
                    model.setResultMsg("missing id parameter for track-delete");
                    return;
                }

                TrackLogic trackLogic = new TrackLogic(oase);
                String retval = trackLogic.delete(id);

                model.setResultMsg("Delete track id=" + id + " retval=" + retval);
            } else {

                model.setResultMsg("unknown cmd: " + command);

            }
        } catch (Throwable t) {
            model.setResultMsg("error during processing of cmd=" + command + "; details: \n" + t);
            t.printStackTrace();
        }
    }
%>
<%
    String command = getParameter(request, "cmd", null);
    if (command == null) {
        model.setResultMsg("no command specified");
        return;
    }

    doCommand(command, request, response, session, model);
    Log.info("control.jsp[sobp]: command=" + command + " result=" + model.getResultMsg() + " user=" + model.getString(ATTR_USER_NAME));

    // Client side redirect
    response.sendRedirect(model.getString(ATTR_PAGE_URL));
%>







