<%@ page import="org.geotracing.gis.GoogleTiles"%>
<%@ page import="org.keyworx.common.log.Log"%>
<%@ page import="org.keyworx.common.log.Logging"%>
<%@ page import="java.io.File"%>
<%!
	static Log log = Logging.getLog("gmap-tile.jsp");
%>
<%
	// System.out.println("uri =" + request.getRequestURI());
	// response.setContentType("image/png");

	// Custom Google tile serving: see gmap-my-tile.jsp and app/geoskating/server/webapp/gs/map
	// A bit hacky until we get a cleaner solution using context-dependent
	// config.
	String khRef = request.getParameter("t");
	if (khRef == null || khRef.length() == 0) {
		String x = request.getParameter("x");
		String y = request.getParameter("y");
		String z = request.getParameter("z");
		khRef = GoogleTiles.getKeyholeRef(Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z));
	}
	String layer = request.getParameter("layer");
	String khDir = GoogleTiles.getKeyHolePath(khRef);
	String filePath = TILE_DIR + khDir + "/" + layer + ".png";
	String url;
	int zoom = GoogleTiles.getTileZoom(khRef);
	if (zoom > MAX_ZOOM) {
		//log.info("no tile");
		url = TILE_BASE_URL + "none.png";

	} else if (new File(filePath).exists()) {
		//log.info("ok tile");
		url = TILE_BASE_URL + khDir + "/" + layer + ".png";
	} else {
		// Send empty
		//log.info("empty tile");
		url = TILE_BASE_URL + "empty.png";
	}

	// Always send cached map image.
	// Servlets.sendFile(response, filePath, "image/png");

	// System.out.println("q =" + request.getQueryString() + "url=" + url);
	response.sendRedirect(url);
%>
