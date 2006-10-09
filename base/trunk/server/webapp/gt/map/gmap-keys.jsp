<%@ page import="java.net.InetAddress,
				 org.keyworx.common.util.Sys,
				 java.util.Properties"%>
<%!
	private static String GMAP_KEY = null;

	public void setGmapKey(ServletContext theApp) throws Exception  {
		// Set only once
		if (GMAP_KEY != null) {
			return;
		}
		
		Properties properties = Sys.loadProperties(theApp.getRealPath("/map") + "/gmap-keys.properties");
		InetAddress localHost = InetAddress.getLocalHost();
		// System.out.println("ADDR=" + localHost.getHostAddress() + " NAME=" + localHost.getHostName());
		String hostName = localHost.getHostName();
		GMAP_KEY = properties.getProperty(hostName + ".key");
	}

	public String getGmapKey() throws Exception  {
			return GMAP_KEY;
	}
%>
<%
	setGmapKey(application);
%>
