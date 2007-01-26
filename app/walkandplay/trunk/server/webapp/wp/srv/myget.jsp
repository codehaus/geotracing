<%@ page import="nl.justobjects.jox.dom.JXElement"%>
<%@ page import="org.geotracing.server.QueryHandler"%>
<%@ page import="org.keyworx.oase.api.Record"%>
<%@ page import="org.keyworx.plugin.tagging.logic.TagLogic"%>
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
			if (command.equals(CMD_QUERY_USER_INFO)) {
				String personId = getParameter(request, PAR_ID, null);
				throwOnMissingParm(PAR_ID, personId);

				Record person = oase.getFinder().read(Integer.parseInt(personId), "utopia_person");

				if (person == null) {
					throw new IllegalArgumentException("No person found for id=" + personId);
				}

				Record account = oase.getRelater().getRelated(person, "utopia_account", null)[0];

				if (account == null || account.getIntField("state") != 1) {
					throw new IllegalArgumentException("No (active) account found for person id=" + personId);
				}

				// Construct final result
				result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);

				// Optional profile info
				if (!person.isNull("extra")) {
					JXElement extra = person.getXMLField("extra");

					// Show info conditionally
					boolean profilePublic = extra.getBoolAttr("profilepublic");
						if (profilePublic) {
						// We have valid person/account: create user info record custom
						JXElement userInfo = new JXElement("record");

						//userInfo.setChildText("profilepublic", profilePublic+"");
						//userInfo.setChildText("emailpublic", emailPublic+"");
						userInfo.setChildText("id", personId);

						// Add account name
						boolean emailPublic = extra.getBoolAttr("emailPublic");
						userInfo.setChildText("nickname", extra.getAttr("nickname"));

						if (emailPublic) {
							userInfo.setChildText("email", account.getStringField("loginname"));
						}

						// Add optional thumb (icon) id
						Record[] thumbRecords = oase.getRelater().getRelated(person, "base_medium", "profile");
						if (thumbRecords.length > 0 && thumbRecords[0] != null) {
							userInfo.setChildText("photoid", thumbRecords[0].getIdString());
						}

						// Add tags made on this person
						String tags = new TagLogic(oase.getOaseSession()).getTagsString(person.getId(), person.getId());
						if (tags != null && tags.length() > 0) {
							userInfo.setChildText("tags", tags);
						}

						result.addChild(userInfo);
					}
				}


			}
		} catch (Throwable t) {
			result = new JXElement(TAG_ERROR);
			result.setText("Unexpected Error during query " + t);
			log.error("Unexpected Error during query cmd=" + command, t);
		}
		return result;
	}
%>
