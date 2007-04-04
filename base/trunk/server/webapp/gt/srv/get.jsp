<%@ page import="nl.justobjects.jox.parser.JXBuilder,
				 org.geotracing.handler.QueryHandler,
				 org.geotracing.handler.QueryLogic,
				 org.geotracing.handler.TrackLogic,
				 org.keyworx.amuse.core.Protocol,
				 org.keyworx.common.log.Log" %>
<%@ page import="org.keyworx.common.log.Logging" %>
<%@ page import="org.keyworx.common.util.Sys" %>
<%@ page import="org.keyworx.oase.api.Record" %>
<%@ page import="javax.servlet.ServletRequest" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.Writer" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Vector" %>
<%!
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd'.'MM'.'yy-HH:mm:ss");

	// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
	// Distributable under LGPL license. See terms of license at gnu.org.

	// This JSP implements a REST-like service to obtain
	// data (in XML) from the server DB.
	// $Id: get.jsp,v 1.25 2006-08-28 09:43:23 just Exp $

	/** All queries should start with q- */
	public static final String CMD_QUERY = "q-";
	public static final String CMD_GET_TRACK = "get-track";
	public static final String CMD_DESCRIBE = "describe";

	public static final String PAR_ID = "id";
	public static final String PAR_CMD = "cmd";
	public static final String PAR_OUTPUT = "output";
	public static final String PAR_TYPE = "type";
	public static final String TAG_ERROR = "error";

	public final static String OUTPUT_JSON = "json";
	public final static String OUTPUT_XML = "xml";

	public static QueryLogic queryLogic = QueryLogic.getInstance();
	public static Log log = Logging.getLog("get.jsp");

	String getParameter(ServletRequest req, String name, String defaultValue) {
		String value = req.getParameter(name);
		if (value == null || value.length() == 0) {
			return defaultValue;
		}

		return value.trim();
	}

	Map getParameters(ServletRequest req) {
		return req.getParameterMap();
	}

	/**
	 * Throw exception when parm empty or not present.
	 */
	public void throwOnMissingParm(String aName, String aValue) throws IllegalArgumentException {
		if (aValue == null || aValue.length() == 0) {
			throw new IllegalArgumentException("Missing parameter=" + aName);
		}
	}

	/** Create query-response XML from Record array. */
	public JXElement createResponse(Record[] theRecords) throws Exception {
		JXElement result = Protocol.createResponse(QueryHandler.QUERY_STORE_SERVICE);
		for (int i = 0; i < theRecords.length; i++) {
			result.addChild(theRecords[i].toXML());
		}
		return result;
	}

	class JSONQueryEncoder {
		private Writer writer;
		private boolean debug = false;

		public JSONQueryEncoder() {
		}

		public void encode(JXElement aQueryResult, Writer aWriter) throws Exception {
			writer = aWriter;
			outputD("<pre>");
			output("{");
			output("query_rsp");


			output(": { cnt: ");
			outputString(aQueryResult.getAttr("cnt"));
			output(", records: [");
			Vector records = aQueryResult.getChildren();
			int cnt = records.size();
			for (int i = 0; i < cnt; i++) {
				outputRecord((JXElement) records.get(i));
				if (i < cnt - 1) {
					output(",\n");
				}
			}
			output("]");
			output("}");
			output("}");
			outputD("</pre>");
		}

		private void outputRecord(JXElement aRec) throws Exception {
			Vector fields = aRec.getChildren();
			if (fields.size() == 0) {
				return;
			}

			StringBuffer sb = new StringBuffer();
			sb.append("{");

			JXElement field;
			String encodedField = null;
			for (int i = 0; i < fields.size(); i++) {
				field = (JXElement) fields.get(i);
				if (field.getTag().equals("extra")) {
					if (field.hasChildren()) {
						fields.addAll(field.getChildAt(0).getChildren());
					} else if (field.hasText() && field.getText().trim().length() != 0) {
						fields.addAll(new JXBuilder().build(field.getText()).getChildren());
					}
					continue;
				}

				encodedField = encodeField(field);
				if (encodedField != null) {
					sb.append(encodedField);
					sb.append(",\n");
				}

				// outputD("\n");
			}

			// Crude but simplest way to deal with last "," (all kinds of exceptions,
			// empty fields etc.
			sb.replace(sb.length() - 2, sb.length() - 1, " ");
			sb.append("}");
			output(sb.toString());
			outputD("\n");
		}

		private String encodeField(JXElement field) throws Exception {
			if (!field.hasText()) {
				return null;
			}

			String text = field.getText().trim();
			if (text.length() == 0) {
				return null;
			}
			String name = field.getTag().replaceAll("-", "");
			return name + ": '" + text + "'";
		}

		private void outputD(String s) {
			if (!debug) {
				return;
			}
			output(s);
		}

		private void output(String s) {
			try {
				writer.write(s);
			} catch (IOException ioe) {
				System.out.println("error: " + ioe);
			}
		}

		private void outputString(String s) {
			try {
				writer.write("'" + s + "'");
			} catch (IOException ioe) {
				System.out.println("error: " + ioe);
			}
		}
	}

	/** Performs command and returns XML result. */
	public JXElement doCommand(HttpServletRequest request, HttpServletResponse response) {
		JXElement result;
		String command = getParameter(request, PAR_CMD, CMD_DESCRIBE);
		long t1, t2;
		try {
			if (command.startsWith(CMD_QUERY)) {
				// Let (derived QueryHandler do all queries)
				Map parms = request.getParameterMap();
				return queryLogic.doQuery(command, parms);
			} else if (command.equals(CMD_GET_TRACK)) {
				TrackLogic trackLogic = new TrackLogic(queryLogic.getOase());
				String id = getParameter(request, "id", null);
				throwOnMissingParm("id", id);
				String format = getParameter(request, "format", "gtx");
				String attrs = getParameter(request, "attrs", null);
				boolean media = getParameter(request, "media", "true").equals("true");
				long minPtDist = Long.parseLong(getParameter(request, "mindist", "0"));
				int maxPoint = Integer.parseInt(getParameter(request, "maxpoints", "-1"));
				result = trackLogic.export(id, format, attrs, media, minPtDist, maxPoint);
			} else if (command.equals(CMD_DESCRIBE)) {
				// Return documentation file
				result = null;
				response.sendRedirect("get-usage.txt");
			} else {
				result = new JXElement(TAG_ERROR);
				result.setText("unknown command " + command);
				log.warn("unknown command " + command);
			}
		} catch (IllegalArgumentException iae) {
			result = new JXElement(TAG_ERROR);
			result.setText("Error in parameter: " + iae.getMessage());
			log.error("Unexpected Error during query", iae);
		} catch (Throwable t) {
			result = new JXElement(TAG_ERROR);
			result.setText("Unexpected Error during query " + t);
			log.error("Unexpected Error during query", t);
		}
		return result;
	}

	// Defines optional app-specific command processing
%>
<%@ include file="myget.jsp" %>
<%
	// Main handling below

	// Start performance timing
	long t1 = Sys.now();
	JXElement result = null;

	// Try optional app-specific command handling (see myget.jsp)
	if (result == null) {
		result = doMyCommand(request, response);
	}

	// perform base command and return XML result
	if (result == null) {
		result = doCommand(request, response);
	}

	// Send XML response to client (is null when redirected)
	if (result != null) {
		// Get command parameter
		String command = getParameter(request, PAR_CMD, CMD_DESCRIBE);
		// Get command parameter
		String output = getParameter(request, PAR_OUTPUT, OUTPUT_XML);
		result.setAttr("cnt", result.getChildCount());

		if (output.equals(OUTPUT_XML)) {
			response.setContentType("text/xml;charset=utf-8");
			try {
				Writer writer = response.getWriter();
				writer.write(result.toString(false));
				writer.flush();
				writer.close();
			} catch (Throwable th) {
				log.info("error " + command + " writing response");
			}
/*		} else if (output.equals(OUTPUT_JSON)) {
			response.setContentType("text/plain;charset=utf-8");
			try {
				Writer writer = response.getWriter();
				JSONWriter jsonWriter = new JSONWriter(writer);
				jsonWriter.object();
				jsonWriter.key(result.getTag());

				jsonWriter.object();
				jsonWriter.key("count");
				jsonWriter.value(result.getChildCount());
				jsonWriter.key("records");
				jsonWriter.array();
				Vector records = result.getChildren();
				JXElement rec;
				for (int i=0; i < records.size(); i ++) {
					jsonWriter.object();
					rec = (JXElement) records.get(i);
					Vector fields = rec.getChildren();
					JXElement field;
					for (int j=0; j < fields.size(); j++) {
						field = (JXElement) fields.get(j);
						jsonWriter.key(field.getTag());
						jsonWriter.value(field.getText());
					}
					jsonWriter.endObject();
				}
				jsonWriter.endArray();
				jsonWriter.endObject();

				jsonWriter.endObject();
				//writer.write(result.toFormattedString());
				writer.flush();
				writer.close();
			} catch (Throwable th) {
				log.info("error " + command + " writing response th=" + th);
			}  */
		} else if (output.equals(OUTPUT_JSON)) {
			// response.setContentType("text/plain;charset=utf-8");
			try {
				Writer writer = response.getWriter();
				new JSONQueryEncoder().encode(result, writer);
				Vector records = result.getChildren();

				writer.flush();
				writer.close();
			} catch (Throwable th) {
				log.info("error " + command + " writing response th=" + th);
			}
		}
		if (command.indexOf("info") == -1) {
			log.info("[" + queryLogic.getOase().getOaseSession().getContextId() + "] cmd=" + command + " rsp=" + result.getTag() + " childcount=" + result.getChildCount() + " dt=" + (Sys.now() - t1) + " ms");
		}

	}
%>