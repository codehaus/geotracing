<%@ page import="nl.justobjects.jox.dom.JXElement,
				 org.geotracing.gis.GoogleTiles,
				 org.keyworx.common.log.Log,
				 org.keyworx.common.log.Logging,
				 org.keyworx.common.util.Rand,
				 org.keyworx.common.util.Sys,
				 org.keyworx.oase.util.Net,
				 org.keyworx.oase.util.Servlets,
				 org.keyworx.server.ServerConfig,
				 javax.servlet.ServletRequest" %>
<%@ page import="java.io.File"%>
<%@ page import="java.io.Writer"%>
<%!
	public static final String DRAW_LOC_SCRIPT = ServerConfig.getConfigDir() + "/../bin/drawloc.sh";
	private static Log log;
	public static final String CACHE_DIR = "/tmp/";

	static {// Make exif script executable
		log = Logging.getLog("GoogleTile");

		String[] command = {"chmod", "+x", DRAW_LOC_SCRIPT};
		StringBuffer stdout = new StringBuffer(24);
		StringBuffer stderr = new StringBuffer(24);
		int exitCode = Sys.execute(command, stdout, stderr);

		if (exitCode != 0) {
			log.error("chmod +x " + DRAW_LOC_SCRIPT + "failed stderr=" + stderr.toString());
		} else {
			// log.info("chmod +x " + DRAW_LOC_SCRIPT + " OK");
		}
	}

	String getParameter(ServletRequest req, String name, String defaultValue) {
		String value = req.getParameter(name);
		if (value == null || value.length() == 0) {
			return defaultValue;
		}

		return value.trim();
	}

%>
<%
	String format = getParameter(request, "format", "imageplot");

	double lon = Double.parseDouble(getParameter(request, "lon", "0"));
	double lat = Double.parseDouble(getParameter(request, "lat", "0"));
	int zoom = Integer.parseInt(getParameter(request, "zoom", "10"));
	String mapType = getParameter(request, "type", "sat");

	String tileRef = null;
	String fileRef = null;
	// Rectangle2D.Double llBox = null;
	String tileURL = null;

	String khRef = GoogleTiles.getKeyholeRef(lon, lat, zoom);

	File file = null;

	if (mapType.equals("sat")) {
		fileRef = tileRef = khRef;
		tileURL = "http://kh1.google.com/kh?v=10&t=" + khRef;
		file = new File(CACHE_DIR + fileRef + ".jpg");
		// llBox = GoogleTiles.getBoundingBox(khRef);
	} else if (mapType.equals("map")) {
		GoogleTiles.XY xy = GoogleTiles.getTileXY(khRef);

		// Hmm, somehow we need to request one zoomlevel lower...
		// zoom -= 1;

		tileRef = "x=" + xy.x + "&y=" + xy.y + "&zoom=" + (17 - zoom);
		fileRef = xy.x + "-" + xy.y + "-" + zoom;
		file = new File(CACHE_DIR + fileRef + ".png");

		// e.g. http://mt.google.com/mt?v=.1&x=480&y=-109&zoom=5;
		// v=ap.31
		tileURL = "http://mt" + GoogleTiles.getTileServerNo(xy.x, xy.y) + ".google.com/mt?v=ap.31&" + tileRef;
		// llBox = GoogleTiles.getBoundingBox(xy.x, xy.y, zoom);
	}

	// Get pixel offsets in tile image
	GoogleTiles.XY xy = GoogleTiles.getPixelXY(lon, lat, zoom);

	if (format.equals("imageplot")) {
		Net.fetchURL(tileURL, file);

		// File with plotted location

		int x = xy.x;
		int y = xy.y;
		int x0 = x + 3;
		int y0 = y + 3;

		String size = getParameter(request, "size", "176x208");
		String plot = getParameter(request, "true", "true");

		File locFile = null;
		if (plot.equals("true")) {
			locFile = new File(CACHE_DIR + Rand.randomString(8) + "-loc.jpg");
		}

		String[] command = {DRAW_LOC_SCRIPT, "" + x, "" + y, "" + x0, "" + y0, size, file.getAbsolutePath(), locFile.getAbsolutePath()};

		StringBuffer stdout = new StringBuffer(24);
		StringBuffer stderr = new StringBuffer(24);
		int exitCode = Sys.execute(command, stdout, stderr);

		if (exitCode == 0) {
			// log.info("drawloc returned " + exitCode);
		} else {
			log.warn("drawloc returned " + exitCode + " stderr=" + stderr.toString());
		}
		Servlets.sendFile(request, response, locFile.getAbsolutePath(), "image/jpeg", false);
		file.delete();
		locFile.delete();
	} else 	if (format.equals("image")) {
		Net.fetchURL(tileURL, file);

		String size = getParameter(request, "size", "256x256");

		File resultFile = new File(CACHE_DIR + Rand.randomString(8) + "-loc.jpg");

		String[] command = {"convert", "-resize", size +"!", file.getAbsolutePath(), resultFile.getAbsolutePath()};

		StringBuffer stdout = new StringBuffer(24);
		StringBuffer stderr = new StringBuffer(24);
		int exitCode = Sys.execute(command, stdout, stderr);

		if (exitCode == 0) {
			// log.info("drawloc returned " + exitCode);
		} else {
			log.warn("convert returned " + exitCode + " stderr=" + stderr.toString());
		}
		Servlets.sendFile(request, response, resultFile.getAbsolutePath(), "image/jpeg", false);
		file.delete();
		resultFile.delete();
	} else if (format.equals("xml")) {

		response.setContentType("text/xml;charset=utf-8");
		JXElement rsp = new JXElement("gmap");
		rsp.setAttr("url", tileURL);
		rsp.setAttr("x", xy.x);
		rsp.setAttr("y", xy.y);
		rsp.setAttr("khref", khRef);
		 try {
			 Writer writer = response.getWriter();
			 writer.write(rsp.toFormattedString());
			 writer.flush();
			 writer.close();
		 } catch (Throwable th) {
	  		 log.info("error gmap writing response");
		 }
	}
%>


