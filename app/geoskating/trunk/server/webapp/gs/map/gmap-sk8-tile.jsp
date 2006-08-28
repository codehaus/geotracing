<%@ page import="java.net.InetAddress"%>
<%!
	static String TILE_BASE_URL = "/rsc/tiles/";

	static String TILE_DIR = "/var/keyworx/webapps/test.geoskating.com/rsc/tiles/";
	static int MAX_ZOOM = 15;

	static {
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			String hostName = localHost.getHostName();
			if (hostName.equals("pundit")) {
				TILE_DIR = "/home/httpd/geoskating.com/rsc/tiles/";
			}
		} catch (Throwable t) {
			System.err.println("sk8tile: cannot get local hostname");
		}
	}
%>
<%@ include file="gmap-tile.jsp" %>
