<%@ page import="nl.justobjects.jox.dom.JXElement,
				 nl.justobjects.jox.parser.JXBuilder,
				 org.geotracing.handler.CommentHandler,
				 org.geotracing.handler.CommentLogic,
				 org.geotracing.handler.EventPublisher,
				 org.keyworx.amuse.core.Amuse,
				 org.keyworx.amuse.core.Protocol,
				 org.keyworx.common.log.Log,
				 org.keyworx.common.log.Logging,
				 org.keyworx.common.util.Sys,
				 org.keyworx.oase.api.Record,
				 org.keyworx.utopia.core.util.Oase,
				 javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse"%>
<%@ page import="java.io.PrintWriter"%>
<%!

	// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
	// Distributable under LGPL license. See terms of license at gnu.org.

	// This JSP implements a public services to supply content
	// data (in XML) to the server DB.
	// $Id$

	public static final String REQ_COMMENT_INSERT = CommentHandler.CMT_INSERT_SERVICE + "-req";

	public static final String ATTR_ID = "id";


	public static Oase oase;
	public static Log log = Logging.getLog("put.jsp");

	/**
	 * Throw exception when parm empty or not present.
	 */
	public void throwOnMissingParm(String aName, String aValue) throws IllegalArgumentException {
		if (aValue == null || aValue.length() == 0) {
			throw new IllegalArgumentException("Missing parameter=" + aName);
		}
	}

	/** Send Amuse response in HTTP response body. */
	public void sendResponse(JXElement anAppResponse, HttpServletResponse aServletResponse) {
		try {
			// Convert response to String
			String responseString = anAppResponse.toString();

			// Set HTTP response headers
			aServletResponse.setContentType("text/xml;charset=utf-8");
			aServletResponse.setContentLength(responseString.length());

			// Get stream to client
			PrintWriter pw = aServletResponse.getWriter();

			//  Send response
			pw.write(responseString);
			pw.flush();
			pw.close();
		} catch (Throwable t) {

		}
	}

	/** Performs request and returns XML response. */
	public JXElement doRequest(JXElement anAppRequest, HttpServletRequest request) {
		JXElement appResponse=null;
		String reqTag = anAppRequest.getTag();
		String service = reqTag.substring(0, reqTag.lastIndexOf('-'));
		try {
			if (reqTag.equals(REQ_COMMENT_INSERT)) {
				CommentLogic logic = new CommentLogic(oase);
				Record record = logic.createRecord();

				String ip = request.getRemoteHost();
				if (ip == null) {
					ip = request.getRemoteAddr();
				}

				if (ip != null) {
					anAppRequest.setChildText("ip", ip);
				}

				// Set fields directly from request
				// May throw IllegalArgumentException if non-existing fields added
				record.setFields(anAppRequest);

				// Add person id as comment owner
				logic.insert(record);

				// Create and return response with open comment id.
				appResponse = Protocol.createResponse(CommentHandler.CMT_INSERT_SERVICE);
				appResponse.setAttr(ATTR_ID, record.getId());

				EventPublisher.commentAdd(record, oase);
			} else {
				appResponse = Protocol.createNegativeResponse(service, Protocol.__4000_Unknown_command, "unknown request tag");
				log.warn("unknown request tag " + reqTag);
			}
		} catch (IllegalArgumentException iae) {
			appResponse = Protocol.createNegativeResponse(service, Protocol.__4004_Invalid_attribute_value, iae.getMessage());
			log.error("Parameter error during put", iae);
		} catch (Throwable t) {
			appResponse = Protocol.createNegativeResponse(service, Protocol.__4005_Unexpected_error, t.getMessage());
			log.error("Unexpected Error during put", t);
		}
		return appResponse;
	}
%>
<%
	// Start performance timing
	long t1 = Sys.now();


	// Get global Oase (DB) session.
	try {
		// Use one Oase session
		if (oase == null) {
			oase = (Oase) application.getAttribute("oase");
			if (oase == null) {
				// First time: create and save in app context
				oase = Oase.createOaseSession(Amuse.server.getPortal().getId());
				application.setAttribute("oase", oase);
			}
		}
	} catch (Throwable th) {
		sendResponse(Protocol.createNegativeResponse("oase", Protocol.__4005_Unexpected_error, th.getMessage()), response);
		log.error("error creating oase session", th);
		return;
	}


	// Build XML element from input
	JXElement appRequest=null, appResponse=null;

	// Main handling below
	try {
		// Build amuse request from POST body
		appRequest = new JXBuilder().build(request.getInputStream());
		// log.info("got request: " + appRequest);
	} catch (Throwable t) {
		// Severe error
		log.warn("error in put.jsp", t);
		appResponse = Protocol.createNegativeResponse("oase", Protocol.__4005_Unexpected_error, "request parse error");
	}

	// log.info("appRequest OK tag=" + appRequest.getTag());

	// Send request to Agent and return response.
	if (appRequest != null) {
		// To prevent sending id.
		appRequest.setChildText("id", null);
		appResponse = doRequest(appRequest, request);
	}
	// log.info("request done rsp=" + appResponse);

	// Send response to client.
	if (appResponse != null) {
		try {
			sendResponse(appResponse, response);
		} catch (Throwable t) {
			// Severe error
			log.warn("error in put.jsp", t);
			return;
		}
		log.info("[" + oase.getOaseSession().getContextId() + "] req=" + appRequest.getTag() + " rsp=" + appResponse.getTag() + " dt=" + (Sys.now() - t1) + " ms");
	}
%>