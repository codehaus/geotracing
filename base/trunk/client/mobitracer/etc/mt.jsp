<%@ page import="org.keyworx.amuse.core.Amuse,
				 org.keyworx.common.log.Log,
				 org.keyworx.common.log.Logging,
				 org.keyworx.common.util.IO,
				 org.keyworx.common.util.Sys,
				 javax.servlet.ServletRequest,
				 java.io.File,
				 java.util.Properties" %>
<%!
	/** Util to get parameter. */
	String getParameter(ServletRequest req, String name, String defaultValue) {
		String value = req.getParameter(name);
		if (value == null || value.length() == 0) {
			return defaultValue;
		}

		return value.trim();
	}

	static Log log = Logging.getLog("ota");

	private static Properties properties;

	/** Get web URL from context (host) specific portal.xml */
	private static String BASE_URL = Amuse.server.getPortal().getProperty("webappurl");

%>
<%
	// Generates user-specific .jad file and sends it to user */

	// Required jad-file content-type
	response.setContentType("text/vnd.sun.j2me.app-descriptor");

	// Get abs path to download dir
	String otaDir = application.getRealPath("/ota");

	// Load global properties once
	if (properties == null) {
		properties = Sys.loadProperties(otaDir + "/mt.properties");
	}

	// Get template jad file as string
	String jadString = IO.FileToString(otaDir + "/mt.jad");

	// Set exact .jar size
	String jarPath = otaDir + "/mobitracer.jar";
	String jarSize = new File(jarPath).length() + "";
	jadString = jadString.replaceFirst("JAR_SIZE", jarSize);

	// Set jar download URL
	String jarURL = BASE_URL + "/ota/mobitracer.jar";
	jadString = jadString.replaceFirst("JAR_URL", jarURL);

	// KW options
	jadString = jadString.replaceFirst("KW_PROTO_URL", BASE_URL);

	// Populate mt-options, default is "full" (other is "minimal")
	jadString = jadString.replaceFirst("MT_OPTIONS", properties.getProperty("mt.options", "full"));

	// GPS sample interval in millis default 20 secs
	jadString = jadString.replaceFirst("GPS_SAMPLE_INTERVAL", properties.getProperty("gps.sample.interval", "5000"));


	// GPS sample interval in millis default 20 secs
	jadString = jadString.replaceFirst("GPS_SEND_INTERVAL", properties.getProperty("gps.send.interval", "25000"));

	// Set username/password from query parms
	String user = getParameter(request, "u", null);
	String password = getParameter(request, "p", null);
	if (password == null) {
		password = user;
	}

	jadString = jadString.replaceFirst("KW_USER", user);
	jadString = jadString.replaceFirst("KW_PASSWORD", password);

	// Send populated jad string as if .jad file
	response.setContentLength(jadString.length());
	response.getWriter().write(jadString);
	response.getWriter().close();
	log.info("MT download: user=" + user);
%>