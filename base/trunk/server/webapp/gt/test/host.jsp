<%@ page import="org.keyworx.amuse.core.Amuse"%>
<%@ page import="java.net.InetAddress"%>
<%
	InetAddress localHost = InetAddress.getLocalHost();
	// System.out.println("ADDR=" + localHost.getHostAddress() + " NAME=" + localHost.getHostName());
	String hostName = localHost.getHostName();
	String hostAddr = localHost.getHostAddress();
	String siteURL = Amuse.server.getPortal().getProperty("siteurl");
	String webAppURL = Amuse.server.getPortal().getProperty("webappurl");
%>

<pre>
OK URL retrieved from www.geotracing.com
host=<%= hostName %>
addr=<%= hostAddr %>
siteurl=<%= siteURL %>
</pre>
