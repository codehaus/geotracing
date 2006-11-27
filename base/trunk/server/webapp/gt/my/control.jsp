<%@ page import="
				com.oreilly.servlet.MultipartRequest,
				 nl.justobjects.jox.dom.JXElement,
				 org.geotracing.server.TracingHandler,
				 org.keyworx.amuse.core.Protocol,
				 org.keyworx.common.net.Servlets,
				 org.keyworx.common.util.IO,
				 org.keyworx.utopia.core.data.Account" %>
<%@ page import="org.keyworx.utopia.core.data.Application" %>
<%@ page import="org.keyworx.utopia.core.data.Portal" %>
<%@ page import="org.keyworx.utopia.core.data.Role" %>
<%@ page import="org.keyworx.utopia.core.logic.PersonLogic" %>
<%@ page import="org.keyworx.utopia.core.util.Core" %>
<%@ page import="javax.servlet.ServletRequest" %>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import="javax.servlet.http.HttpServletResponse" %>
<%@ page import="javax.servlet.http.HttpSession" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.keyworx.common.util.MD5" %>
<%@ page import="org.keyworx.oase.api.MediaFiler" %>
<%@ page import="org.keyworx.oase.service.MediaFilerImpl" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.io.File" %>
<%@ page import="nl.justobjects.jox.parser.JXBuilder"%>
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

	private JXElement updateTags(String oldTags, String newTags, String id, HttpSession session) {
		JXElement req;
		if (!newTags.equals(oldTags)) {
			// Tags changed: update tags for this medium
			if (newTags.length() == 0 && oldTags.length() > 0) {
				// <tagging-untag-req items="id1,id2,..." [tags="tag1 'tag 3'"] />
				req = new JXElement("tagging-untag-req");
			} else {
				// 	<tagging-tag-req items="id1,id2,..." tags="tag1 tag2 ..." mode="add|replace"/>
				req = new JXElement("tagging-tag-req");
				req.setAttr("tags", newTags);
				req.setAttr("mode", "replace");
			}
			req.setAttr("items", id);
			return HttpConnector.executeRequest(session, req);
		}
		return null;
	}


	private void doCommand(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		String command = null;
		MultipartRequest multipartRequest = null;

		String requestEncoding = request.getContentType();

		// Use MultiparRequest whenever "multipart/form-data" FORMs are submitted.
		if (requestEncoding != null && requestEncoding.trim().startsWith("multipart/form-data")) {
			try {
				MediaFiler mediaFiler = (MediaFilerImpl) model.getOase().getMediaFiler();
				multipartRequest = new MultipartRequest(request, mediaFiler.getIncomingDir(), (int) 10000000);
			} catch (Exception e) {
				model.setResultMsg("Create MultipartRequest failed: " + e);
				return;
			}
		}

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
				Application application = (Application) model.getOase().getByNameAndValue(Application.class, "name", "geoapp").get(0);
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
				model.set(ATTR_LEFT_MENU_URL, "menu/left-media.html");
				model.set(ATTR_CONTENT_URL, "content/media.jsp");
				model.set(ATTR_PREV_CONTENT_URL, model.getObject(ATTR_CONTENT_URL));
			} else if ("nav-media-list".equals(command)) {
				model.set(ATTR_LEFT_MENU_URL, "menu/left-media.html");
				model.set(ATTR_CONTENT_URL, "content/media-list.jsp");
				model.set(ATTR_PREV_CONTENT_URL, model.getObject(ATTR_CONTENT_URL));
			} else if ("nav-media-upload".equals(command)) {
				model.set(ATTR_LEFT_MENU_URL, "menu/left-media.html");
				model.set(ATTR_CONTENT_URL, "content/media-upload.jsp");
				model.set(ATTR_PREV_CONTENT_URL, model.getObject(ATTR_CONTENT_URL));
			} else if ("nav-tracks".equals(command)) {
				model.set(ATTR_LEFT_MENU_URL, "menu/left-tracks.html");
				model.set(ATTR_CONTENT_URL, "content/tracks.jsp");
			} else if ("nav-track-list".equals(command)) {
				model.set(ATTR_LEFT_MENU_URL, "menu/left-tracks.html");
				model.set(ATTR_CONTENT_URL, "content/track-list.jsp");
			} else if ("nav-track-upload".equals(command)) {
				model.set(ATTR_LEFT_MENU_URL, "menu/left-tracks.html");
				model.set(ATTR_CONTENT_URL, "content/track-upload.jsp");
			} else if ("nav-pois".equals(command)) {
				model.set(ATTR_LEFT_MENU_URL, "menu/left-init.html");
				model.set(ATTR_CONTENT_URL, "content/poi-list.jsp");
				model.set(ATTR_PREV_CONTENT_URL, model.getObject(ATTR_CONTENT_URL));
			} else if ("nav-profile".equals(command)) {
				model.set(ATTR_LEFT_MENU_URL, "menu/left-init.html");
				model.set(ATTR_CONTENT_URL, "content/profile-form.jsp");
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
					model.set(ATTR_CONTENT_URL, "content/error.jsp");
					return;
				}

				model.setResultMsg("Delete Medium OK id=" + id);
				model.set(ATTR_CONTENT_URL, model.getObject(ATTR_PREV_CONTENT_URL));
			} else if ("media-upload".equals(command)) {
				String cancel = getParameter(multipartRequest, "cancel", null);
				if (cancel != null) {
					model.set(ATTR_CONTENT_URL, "content/media.jsp");
					return;
				}

				// Get the files and possible parameters from the MultipartRequest
				Enumeration fileNames = multipartRequest.getFileNames();

				// Go through all uploaded files.
				while (fileNames.hasMoreElements()) {
					String nextFileName = (String) fileNames.nextElement();

					// Get the next uploaded file
					File nextFile = multipartRequest.getFile(nextFileName);

					// Form field may be empty
					if (nextFile == null) {
						continue;
					}

					// Do upload and location determiniation
					JXElement req = Protocol.createRequest(TracingHandler.T_TRK_UPLOAD_MEDIUM_SERVICE);
					req.setAttr(TracingHandler.ATTR_FILE, nextFile.getAbsolutePath());
					req.setAttr(TracingHandler.ATTR_NAME, IO.forHTMLTag(getParameter(multipartRequest, "name", "")));
					req.setAttr(TracingHandler.ATTR_DESCRIPTION, IO.forHTMLTag(getParameter(multipartRequest, "description", "")));
					JXElement rspElm = HttpConnector.executeRequest(session, req);
					if (Protocol.isNegativeResponse(rspElm)) {
						model.logWarning("upload Medium failed: " + Protocol.getStatusString(rspElm.getIntAttr("errorId")));
						model.setResultMsg("upload Medium failed: " + Protocol.getStatusString(rspElm.getIntAttr("errorId")));
						continue;
					}
					model.logInfo("upload-media: uploaded " + nextFile.getAbsolutePath());
				}

				model.set(ATTR_CONTENT_URL, "content/ok.html");
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
					model.set(ATTR_CONTENT_URL, "content/error.jsp");
					return;
				}
				String name = getParameter(request, "name", null);
				if (name == null) {
					model.setResultMsg("please provide a title");
					model.set(ATTR_CONTENT_URL, "content/error.jsp");
					return;
				}
				String description = getParameter(request, "description", null);
				if (description == null) {
					model.setResultMsg("please provide a description");
					model.set(ATTR_CONTENT_URL, "content/error.jsp");
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
					model.setResultMsg("error updating medium " + rsp.toEscapedString());
					model.set(ATTR_CONTENT_URL, "content/error.jsp");
					return;
				}

				rsp = updateTags(getParameter(request, "otags", ""), getParameter(request, "tags", ""), id, session);
				if (rsp != null && Protocol.isNegativeResponse(rsp)) {
					model.setResultMsg("error updating tags " + rsp.toEscapedString());
					model.set(ATTR_CONTENT_URL, "content/error.jsp");
					return;
				}
				model.set(ATTR_CONTENT_URL, model.getObject(ATTR_PREV_CONTENT_URL));
			} else if ("profile-update".equals(command)) {
				String cancel = getParameter(multipartRequest, "cancel", null);
				if (cancel != null) {
					model.set(ATTR_CONTENT_URL, "content/cancel.html");
					return;
				}

				String msg = "";
				Record person, account, thumb = null;
				try {
					person = model.getOase().getFinder().read(Integer.parseInt(model.getPersonId()));
					account = model.getOase().getRelater().getRelated(person, "utopia_account", null)[0];
					Record[] thumbs = model.getOase().getRelater().getRelated(person, "base_medium", "thumb");
					if (thumbs.length > 0) {
						thumb = thumbs[0];
					}
				} catch (Throwable t) {
					msg = "Error t=" + t;
					model.setResultMsg(msg);
					model.set(ATTR_CONTENT_URL, "content/error.jsp");
					return;
				}

				// Account updates
				String password1 = getParameter(multipartRequest, "password1", null);
				if (password1 != null) {
					String password2 = getParameter(multipartRequest, "password2", "");
					if (!password1.equals(password2)) {
						model.setResultMsg("passwords not equal");
						model.set(ATTR_CONTENT_URL, "content/error.jsp");
						return;
					}
					account.setStringField("password", MD5.createStringDigest(password1));
					model.getOase().getModifier().update(account);
				}

				// Person updates
				String firstname = getParameter(multipartRequest, "firstname", null);
				person.setStringField("firstname", firstname);

				String lastname = getParameter(multipartRequest, "lastname", null);
				person.setStringField("lastname", lastname);

				String mobilenr = getParameter(multipartRequest, "mobilenr", null);
				person.setStringField("mobilenr", mobilenr);

				String emails = getParameter(multipartRequest, "emails", null);
				person.setStringField("email", emails);

				String desc = getParameter(multipartRequest, "desc", null);
				if (desc != null) {
					JXElement extra = person.getXMLField("extra");
					if (extra == null) {
						extra = new JXElement("profile");
					} else {
						extra.removeChildByTag("desc");
					}
					extra.setChildText("desc", desc);
					person.setXMLField("extra", extra);
				}

				model.getOase().getModifier().update(person);

				// Person Icon update
				// Upload and insert new user icon file
				Record[] newThumbs = ((MediaFilerImpl) model.getOase().getMediaFiler()).upload(multipartRequest);

				if (newThumbs.length > 0) {
					// Relate icon to person
					model.getOase().getRelater().relate(person, newThumbs[0], "thumb");

					// Delete old user icon if available
					if (thumb != null) {
						model.getOase().getModifier().delete(thumb);
					}
				} else {
					model.setResultMsg("no user icon uploaded");
				}

				// Update tags
				JXElement rsp = updateTags(getParameter(multipartRequest, "otags", ""), getParameter(multipartRequest, "tags", ""), model.getPersonId(), session);
				if (rsp != null && Protocol.isNegativeResponse(rsp)) {
					model.setResultMsg("error in tag update " + rsp.toEscapedString());
					model.set(ATTR_CONTENT_URL, "content/error.jsp");
					return;
				}

				model.set(ATTR_CONTENT_URL, "content/ok.html");
			} else if ("track-delete".equals(command)) {
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
			} else if ("track-upload".equals(command)) {
				String cancel = getParameter(multipartRequest, "cancel", null);
				if (cancel != null) {
					model.set(ATTR_CONTENT_URL, "content/tracks.jsp");
					return;
				}

				// Get the files and possible parameters from the MultipartRequest
				Enumeration fileNames = multipartRequest.getFileNames();
				File file=null;
				// Go through all uploaded files.
				while (fileNames.hasMoreElements()) {
					String nextFileName = (String) fileNames.nextElement();

					// Get the next uploaded file
					file = multipartRequest.getFile(nextFileName);

					// Form field may be empty
					if (file != null) {
						break;
					}
				}

				if (file == null) {
					model.logWarning("upload Track: no file uploaded: ");
					model.setResultMsg("upload Track: failed, no file specified");
					model.set(ATTR_CONTENT_URL, "content/error.jsp");
					return;
				}

				// Do upload
				JXElement req = Protocol.createRequest(TracingHandler.T_TRK_IMPORT_SERVICE);
				req.setAttr(TracingHandler.ATTR_NAME, IO.forHTMLTag(getParameter(multipartRequest, "name", "trk-upload")));
				JXElement trackDoc = new JXBuilder().build(file);
				JXElement data = new JXElement("data");
				data.addChild(trackDoc);
				req.addChild(data);
				JXElement rspElm = HttpConnector.executeRequest(session, req);
				if (Protocol.isNegativeResponse(rspElm)) {
					model.logWarning("upload Track failed: " + Protocol.getStatusString(rspElm.getIntAttr("errorId")));
					model.setResultMsg("upload Track failed: " + rspElm.toEscapedString());
					model.set(ATTR_CONTENT_URL, "content/error.jsp");
					return;
				}
				model.logInfo("upload-track: uploaded " + file.getAbsolutePath());
				model.set(ATTR_CONTENT_URL, "content/ok.html");
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
				rec.setStringField("name", name);
				rec.setStringField("type", type);
				rec.setStringField("description", description);
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
					model.set(ATTR_CONTENT_URL, "content/error.jsp");
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
				rec.setStringField("name", name);
				rec.setStringField("description", description);
				model.getOase().getModifier().update(rec);

				// Update tags
				JXElement rsp = updateTags(getParameter(request, "otags", ""), getParameter(request, "tags", ""), id, session);
				if (rsp != null && Protocol.isNegativeResponse(rsp)) {
					model.setResultMsg("error in tag-req " + rsp.toEscapedString());
					model.set(ATTR_CONTENT_URL, "content/error.jsp");
					return;
				}

				model.set(ATTR_CONTENT_URL, "content/track-list.jsp");
			} else {

				model.setResultMsg("unknown cmd: " + command);
				model.set(ATTR_CONTENT_URL, "content/error.jsp");

			}

			// Log only "real" actions
			if (!command.startsWith("nav")) {
				model.logInfo("command handled cmd=" + command);
			}
		} catch (Throwable t) {
			model.setResultMsg("error during processing of cmd=" + command + "; details: \n" + t);
			model.logWarning("error during processing of cmd=" + command, t);
			model.set(ATTR_CONTENT_URL, "content/error.jsp");
			return;
		}
	}
%>
<%
	doCommand(request, response, session);

	// Client side redirect
	response.sendRedirect(model.getString(ATTR_PAGE_URL));
%>







