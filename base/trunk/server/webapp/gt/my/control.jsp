<%@ page import="
				com.oreilly.servlet.MultipartRequest,
				nl.justobjects.jox.dom.JXElement,
				org.geotracing.server.TracingHandler,
				org.keyworx.amuse.core.Protocol,
				org.keyworx.common.net.Servlets,
				org.keyworx.common.util.IO,
				org.keyworx.oase.util.Log,
				org.keyworx.utopia.core.data.Account" %>
<%@ page import="org.keyworx.utopia.core.data.Application"%>
<%@ page import="org.keyworx.utopia.core.data.Portal"%>
<%@ page import="org.keyworx.utopia.core.data.Role"%>
<%@ page import="org.keyworx.utopia.core.logic.PersonLogic"%>
<%@ page import="org.keyworx.utopia.core.util.Core"%>
<%@ page import="javax.servlet.ServletRequest"%>
<%@ page import="javax.servlet.http.HttpServletRequest"%>
<%@ page import="javax.servlet.http.HttpServletResponse"%>
<%@ page import="javax.servlet.http.HttpSession"%>
<%@ page import="java.util.ArrayList"%>
<%@ include file="model.jsp" %>
<%!

   	public String getParameter(ServletRequest req, String name, String defaultValue) {
		// Delegate to KWX util.
		return Servlets.getParameter(req, name, defaultValue);
	}

 	public String getParameter(MultipartRequest req, String name, String defaultValue) {
		String value = req.getParameter(name);
		if (value == null || value.length() == 0) {
			return defaultValue;
		}
	return value.trim();
}

private void doCommand(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
    String command = null;
 	try {
		// model.setResultMsg("OK");
		command = getParameter(request, "cmd", null);
		if (command == null) {
			model.setResultMsg("no command specified");
			return;
		}

		if (command.indexOf("update") != -1 || command.indexOf("delete") != -1) {
			String id = getParameter(request, "id", null);
			if (id != null) {
				Record personRecord = model.getOase().getFinder().read(Integer.parseInt(model.getPersonId()));
				Record modRecord = model.getOase().getFinder().read(Integer.parseInt(id));
				if (!model.getOase().getRelater().isRelated(personRecord, modRecord)) {
					model.setResultMsg("hmmm, you wimp, trying update other people's data ?");
					model.set(ATTR_CONTENT_URL, "content/init.html");
					return;
				}
			}
		}
		if ("login".equals(command)) {
		String userName = request.getParameter("username");
          String password = request.getParameter("password");
		  HttpConnector.logout(session);
			JXElement rspElm = HttpConnector.login(session, Amuse.server.getPortal().getId(), "geoapp", Role.USER_ROLE_VALUE, userName, password, null);
 		  if (Protocol.isNegativeResponse(rspElm)) {
			  model.set(ATTR_CONTENT_URL, "content/login-form.jsp");
		      model.setResultMsg("login failed: " + Protocol.getStatusString(rspElm.getIntAttr("errorId")));
     		  return;
		  }
		  model.set(ATTR_USER_NAME, userName);
		  model.set(ATTR_PASSWORD, password);
		  model.set(ATTR_STATUS_MSG, STATUS_MSG_LOGGED_IN + userName);
	      model.set(ATTR_CONTENT_URL, "content/welcome.html");
		  model.set(ATTR_TOP_MENU_URL, "menu/top-main.html");
		  model.setResultMsg("login ok");
		} else if ("logout".equals(command)) {
			HttpConnector.logout(session);
			model.set(ATTR_STATUS_MSG, STATUS_MSG_NULL);
			model.set(ATTR_TOP_MENU_URL, "menu/top-init.html");
			model.set(ATTR_LEFT_MENU_URL, "menu/left-init.html");
			model.set(ATTR_CONTENT_URL, "content/init.html");
			model.setResultMsg("logout ok");
		} else if ("register".equals(command)) {
			Portal portal = null;
			ArrayList portalList = model.getOase().getByNameAndValue(Core.PORTAL, Portal.NAME_FIELD, model.getPortalName());
			if (portalList != null && portalList.size() > 0) {
				portal = (Portal) portalList.get(0);
			}


			/*public Person insertUser(String portalId, String applicationId, String organisationId, String organisationTag, String firstName, String lastName,
										 String birthDate, String street, String streetNr, String zipCode,
										 String city, String country, String phoneNr, String mobileNr,
										 String email, JXElement extra, String loginName, String password,
										 String key) throws UtopiaException { */

			String portalId = "" + portal.getId();
			Application  application = (Application) model.getOase().getByNameAndValue(Application.class, "name", "geoapp").get(0);
			String applicationId = "" + application.getId();
			String firstName = getParameter(request, "firstname", "anon");
			String lastName = getParameter(request, "lastname", "anon");
			String email = getParameter(request, "email", null);
			String mobile = getParameter(request, "mobilenr", null);
			String loginName = getParameter(request, "loginname", null);
			String password = getParameter(request, "password", null);

			if (loginName == null || email == null || password == null) {
				model.setResultMsg("register failed: required field missing");
				return;
			}

			Account result = (Account) model.getOase().getUniqueByNameValue(Core.ACCOUNT, Account.LOGINNAME_FIELD, loginName);
			if (result != null) {
				model.setResultMsg("register failed: account " + loginName + " already in use");
				return;
			}

			PersonLogic personLogic = new PersonLogic(model.getOase());
			personLogic.insertUser(portalId, applicationId, null, null, firstName, lastName, null, null, null, null, null, null, null, mobile, email, null, loginName, password, null);
			model.set(ATTR_STATUS_MSG, STATUS_MSG_NULL);
			model.set(ATTR_TOP_MENU_URL, "menu/top-null.html");
			model.set(ATTR_LEFT_MENU_URL, "menu/left-null.html");
			model.set(ATTR_CONTENT_URL, "content/login-form.jsp");
			model.setResultMsg("register ok user=" + loginName);
		} else if ("nav-media".equals(command)) {
			model.set(ATTR_LEFT_MENU_URL, "menu/left-init.html");
			model.set(ATTR_CONTENT_URL, "content/media-list.jsp");
			model.set(ATTR_PREV_CONTENT_URL, model.getObject(ATTR_CONTENT_URL));
			} else if ("nav-tracks".equals(command)) {
			model.set(ATTR_LEFT_MENU_URL, "menu/left-init.html");
			model.set(ATTR_CONTENT_URL, "content/track-list.jsp");
		} else if ("nav-pois".equals(command)) {
			model.set(ATTR_LEFT_MENU_URL, "menu/left-init.html");
			model.set(ATTR_CONTENT_URL, "content/poi-list.jsp");
			model.set(ATTR_PREV_CONTENT_URL, model.getObject(ATTR_CONTENT_URL));
	    } else if ("nav-init".equals(command)) {
		   model.set(ATTR_STATUS_MSG, STATUS_MSG_NULL);
		   model.set(ATTR_TOP_MENU_URL, "menu/top-init.html");
		   model.set(ATTR_LEFT_MENU_URL, "menu/left-init.html");
		   model.set(ATTR_CONTENT_URL, "content/init.html");
		   model.setState(MODEL_STATE_READY);
		   model.setResultMsg("nav-init ok");
		} else if ("nav-login".equals(command)) {
		  model.set(ATTR_CONTENT_URL, "content/login-form.jsp");
		  model.setResultMsg("nav-login ok");
		} else if ("nav-mobitracer".equals(command)) {
		  model.set(ATTR_LEFT_MENU_URL, "menu/left-init.html");
		  model.set(ATTR_CONTENT_URL, "content/mobitracer.jsp");
		  model.setResultMsg("nav-mobitracer ok");
		} else if ("nav-register".equals(command)) {
		  model.set(ATTR_CONTENT_URL, "content/register-form.jsp");
		  model.setResultMsg("nav-register ok");

		} else if ("nav-medium-edit".equals(command)) {
			String id = getParameter(request, "id", null);
			if (id == null) {
				model.setResultMsg("missing id parameter for medium-edit");
				return;
			}
		  model.set(ATTR_CONTENT_URL, "content/medium-edit.jsp?id=" + id);
		  model.setResultMsg("nav-medium-edit ok");
		} else if ("nav-poi-edit".equals(command)) {
			String id = getParameter(request, "id", null);
			if (id == null) {
				model.setResultMsg("missing id parameter for nav-poi-edit");
				return;
			}
		  model.set(ATTR_CONTENT_URL, "content/poi-edit.jsp?id=" + id);
	      model.setResultMsg("nav-poi-edit ok");
		} else if ("nav-track-edit".equals(command)) {
			String id = getParameter(request, "id", null);
			if (id == null) {
				model.setResultMsg("missing id parameter for nav-track-edit");
				return;
			}
		  model.set(ATTR_CONTENT_URL, "content/track-edit.jsp?id=" + id);
		  model.set(ATTR_PREV_CONTENT_URL, model.getObject(ATTR_CONTENT_URL));
		  model.setResultMsg("nav-track-edit ok");
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
		} else if ("medium-delete".equals(command)) {
			String id = getParameter(request, "id", null);
			if (id == null) {
				model.setResultMsg("missing id parameter for poi-delete");
				return;
			}
			JXElement req = Protocol.createRequest(TracingHandler.T_TRK_DELETE_MEDIUM_SERVICE);
			req.setAttr("id", id);
			JXElement rspElm = HttpConnector.executeRequest(session, req);
			if (Protocol.isNegativeResponse(rspElm)) {
				model.setResultMsg("delete Medium failed: " + Protocol.getStatusString(rspElm.getIntAttr("errorId")));
				return;
			}

			model.setResultMsg("Delete Medium OK id=" + id);
			model.set(ATTR_CONTENT_URL, model.getObject(ATTR_PREV_CONTENT_URL));
		} else if ("media-upload".equals(command)) {
/*			Record[] records = DB.uploadLocationMedia(request, DBDefs.VAL_JUST, 0L);
			result.setMessage("Upload ok record count=" + records.length); */
		} else if ("medium-update".equals(command)) {
			// Go back to track edit
			/* String trackId = getParameter(request, "trackid", null);
			if (trackId == null) {
				model.setResultMsg("geen track id");
				return;
			}  */
			String cancel = getParameter(request, "cancel", null);
			if (cancel != null) {
				model.set(ATTR_CONTENT_URL, model.getObject(ATTR_PREV_CONTENT_URL));
				return;
			}
			String id = getParameter(request, "id", null);
			if (id == null) {
				model.setResultMsg("no medium id");
				return;
			}
			String name = getParameter(request, "name", null);
			if (name == null) {
				model.setResultMsg("no name");
				return;
			}
			String description = getParameter(request, "description", null);
			if (description == null) {
				model.setResultMsg("no description");
				return;
			}

			// Escape HTML stuff
			name = IO.forHTMLTag(name);
			description = IO.forHTMLTag(description);


			JXElement req = new JXElement("medium-update-req");
			JXElement medium = new JXElement("medium");
			req.setAttr("id", id);
			medium.setChildText("name", name);
			medium.setChildText("description", description);
			medium.setChildText("filename", "non-existing-file");
			req.addChild(medium);
			JXElement rsp = HttpConnector.executeRequest(session, req);
			if (Protocol.isNegativeResponse(rsp)) {
				model.setResultMsg("fout bij update medium " + rsp.toEscapedString());
				return;
			}

			model.set(ATTR_CONTENT_URL, model.getObject(ATTR_PREV_CONTENT_URL));
		}  else if ("track-delete".equals(command)) {
			String id = getParameter(request, "id", null);
			if (id == null) {
				model.setResultMsg("missing id parameter for track-delete");
				return;
			}

			JXElement req = Protocol.createRequest(TracingHandler.T_TRK_DELETE_SERVICE);
			req.setAttr("id", id);
			JXElement rspElm = HttpConnector.executeRequest(session, req);
			if (Protocol.isNegativeResponse(rspElm)) {
				model.setResultMsg("delete track failed: " + Protocol.getStatusString(rspElm.getIntAttr("errorId")));
				return;
			}

			model.setResultMsg("Delete track OK id=" + id);
			model.set(ATTR_CONTENT_URL, "content/track-list.jsp");
		} else if ("poi-update".equals(command)) {
			// Go back to track edit
			/* String trackId = getParameter(request, "trackid", null);
			if (trackId == null) {
				model.setResultMsg("geen track id");
				return;
			}  */
			String cancel = getParameter(request, "cancel", null);
			if (cancel != null) {
				model.set(ATTR_CONTENT_URL, model.getObject(ATTR_PREV_CONTENT_URL));
				return;
			}
			String id = getParameter(request, "id", null);
			if (id == null) {
				model.setResultMsg("no poi id");
				return;
			}
			String name = getParameter(request, "name", null);
			if (name == null) {
				model.setResultMsg("no name");
				return;
			}
			String type = getParameter(request, "type", null);
			if (type == null) {
				model.setResultMsg("no type");
				return;
			}
			String description = getParameter(request, "description", null);
			if (description == null) {
				description = "no description";
			}

			// Escape HTML stuff
			name = IO.forHTMLTag(name);
			type = IO.forHTMLTag(type);
			description = IO.forHTMLTag(description);

			// TODO: make real Utopia request !!!
			Record rec = model.getOase().getFinder().read(Integer.parseInt(id));
			rec.setStringField("name",  name);
			rec.setStringField("type",  type);
			rec.setStringField("description",  description);
			model.getOase().getModifier().update(rec);

/*			JXElement req = new JXElement("poi-update-req");
			JXElement poi = new JXElement("poi");
			req.setAttr("id", id);
			poi.setChildText("name", name);
			poi.setChildText("type", type);
			poi.setChildText("description", description);
			req.addChild(poi);
			JXElement rsp = HttpConnector.executeRequest(session, req);
			if (Protocol.isNegativeResponse(rsp)) {
				model.setResultMsg("fout bij update poi " + rsp.toEscapedString());
				return;
			}     */

			model.set(ATTR_CONTENT_URL, model.getObject(ATTR_PREV_CONTENT_URL));
		} else if ("poi-delete".equals(command)) {
			String id = getParameter(request, "id", null);
			if (id == null) {
				model.setResultMsg("missing id parameter for poi-delete");
				return;
			}
			JXElement req = Protocol.createRequest(TracingHandler.T_TRK_DELETE_POI_SERVICE);
			req.setAttr("id", id);
			JXElement rspElm = HttpConnector.executeRequest(session, req);
			if (Protocol.isNegativeResponse(rspElm)) {
				model.setResultMsg("delete POI failed: " + Protocol.getStatusString(rspElm.getIntAttr("errorId")));
				return;
			}

			model.setResultMsg("Delete POI OK id=" + id);
			model.set(ATTR_CONTENT_URL, model.getObject(ATTR_PREV_CONTENT_URL));
		} else if ("track-update".equals(command)) {
			// Go back to track edit
			/* String trackId = getParameter(request, "trackid", null);
			if (trackId == null) {
				model.setResultMsg("geen track id");
				return;
			}  */
			String cancel = getParameter(request, "cancel", null);
			if (cancel != null) {
				model.set(ATTR_CONTENT_URL, "content/track-list.jsp");
				return;
			}
			String id = getParameter(request, "id", null);
			if (id == null) {
				model.setResultMsg("no track id");
				return;
			}
			String name = getParameter(request, "name", null);
			if (name == null) {
				model.setResultMsg("no name");
				return;
			}
			String description = getParameter(request, "description", null);
			if (description == null) {
				description = "no description";
			}

			// Escape HTML stuff
			name = IO.forHTMLTag(name);
			description = IO.forHTMLTag(description);

			// TODO: make real Utopia request !!!
			Record rec = model.getOase().getFinder().read(Integer.parseInt(id));
			rec.setStringField("name",  name);
			rec.setStringField("description",  description);
			model.getOase().getModifier().update(rec);

/*			JXElement req = new JXElement("poi-update-req");
			JXElement poi = new JXElement("poi");
			req.setAttr("id", id);
			poi.setChildText("name", name);
			poi.setChildText("type", type);
			poi.setChildText("description", description);
			req.addChild(poi);
			JXElement rsp = HttpConnector.executeRequest(session, req);
			if (Protocol.isNegativeResponse(rsp)) {
				model.setResultMsg("fout bij update poi " + rsp.toEscapedString());
				return;
			}     */

			model.set(ATTR_CONTENT_URL, "content/track-list.jsp");
		} else {

			model.setResultMsg("unknown cmd: " + command);

		}
	} catch (Throwable t) {
		model.setResultMsg("error during processing of cmd=" + command + "; details: \n" + t);
		 Log.warn("error during processing of cmd=" + command + "; details: \n" + t);
		t.printStackTrace();
	}
}
%>
<%
	doCommand(request, response, session);

	// Client side redirect
	response.sendRedirect(model.getString(ATTR_PAGE_URL));
%>







