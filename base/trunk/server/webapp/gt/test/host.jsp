<%@ page import="java.net.InetAddress"%>
<%
	InetAddress localHost = InetAddress.getLocalHost();
	// System.out.println("ADDR=" + localHost.getHostAddress() + " NAME=" + localHost.getHostName());
	String hostName = localHost.getHostName();
	String hostAddr = localHost.getHostAddress();
%>

<pre>
OK URL retrieved from www.geotracing.com
host=<%= hostName %>
add=<%= hostAddr %>

</pre>
