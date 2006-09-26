<%@ page import="org.keyworx.common.util.IO,java.io.File,java.net.InetAddress,
				 org.keyworx.common.log.Log,
				 org.keyworx.common.log.Logging,
				 java.util.Properties,
				 org.keyworx.common.util.Sys"%>
<%!
	String getParameter(ServletRequest req, String name, String defaultValue) {
	String value = req.getParameter(name);
	  if (value == null || value.length() == 0) {
		return defaultValue;
	  }

	  return value.trim();
	}
	static Log log = Logging.getLog("ota");

	private static Properties properties;
	private static String BASE_URL;

	public void init(String aRootDir) throws Exception  {
		// Init only once
		if (properties != null) {
			return;
		}

		properties = Sys.loadProperties(aRootDir + "/wp.properties");
		InetAddress localHost = InetAddress.getLocalHost();
		// System.out.println("ADDR=" + localHost.getHostAddress() + " NAME=" + localHost.getHostName());
		String hostName = localHost.getHostName();
		BASE_URL = "http://" + properties.getProperty(hostName + ".url");
	}

%>
<%
	response.setContentType("text/vnd.sun.j2me.app-descriptor");
	String otaDir = application.getRealPath("/ota");

	init(otaDir);

	String jadPath = otaDir + "/wp.jad";
	String jadString = IO.FileToString(jadPath);

	String jarPath = otaDir + "/wp.jar";
	String jarSize = new File(jarPath).length() + "";
	jadString = jadString.replaceFirst("[JAR_SIZE]", jarSize);

	String jarURL = BASE_URL + "/ota/wp.jar";
	jadString = jadString.replaceFirst("[JAR_URL]", jarURL);

	// KW options
	jadString = jadString.replaceFirst("[KW_PROTO_URL]", BASE_URL);

	// Populate wp-options, default is "full"
	jadString = jadString.replaceFirst("[NT_OPTIONS]", properties.getProperty("wp.options", "full"));

	// GPS sample interval in millis default 20 secs
	jadString = jadString.replaceFirst("[GPS_SAMPLE_INTERVAL]", properties.getProperty("gps.sample.interval", "20000"));

	// Populate username/password from query parms
	String user = getParameter(request, "u", null);
	String password = getParameter(request, "p", null);
	if (password == null) {
		password = user;
	}

	jadString = jadString.replaceFirst("[KW_USER]", user);
	jadString = jadString.replaceFirst("[KW_PASSWORD]", password);
	//response.setContentLength(jadString.length());
	response.getWriter().write(jadString);
	//response.getWriter().close();
	log.info("MT download: user=" + user);
%>