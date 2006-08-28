<%@ page import="java.net.InetAddress,
				 java.awt.geom.Rectangle2D,
				 org.geotracing.gis.GoogleTiles,
				 org.keyworx.oase.util.Net,
				 java.io.File,
				 org.keyworx.oase.util.Servlets,
				 org.keyworx.common.util.Sys,
				 org.keyworx.server.ServerConfig,
				 org.keyworx.common.log.Log,
				 org.keyworx.common.log.Logging"%>
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
			log.info("chmod +x " + DRAW_LOC_SCRIPT + " OK");
		}
	  }

	String getParameter(ServletRequest req, String name, String defaultValue) {
	String value = req.getParameter(name);
	if (value == null || value.length() == 0) {
		return defaultValue;
	}

	return value.trim();
}

	/** returns the Google zoom level for the keyhole string. */
	public static int getY(double lat, Rectangle2D.Double llBox) {
		// 256/lengte * ln(tan(PI/4 + (lat)/2)
		return 256 - (int) (256.0D / Math.toRadians(llBox.height) * Math.log(Math.tan(Math.PI/4 + Math.toRadians(lat)/2)));
	}

%>
<%
    double lon = Double.parseDouble(getParameter(request, "lon", "0"));
	double lat = Double.parseDouble(getParameter(request, "lat", "0"));
	int zoom = Integer.parseInt(getParameter(request, "zoom", "10"));
    String mapType = getParameter(request, "type", "sat");

	String tileRef = null;
	String fileRef = null;
    Rectangle2D.Double llBox = null;
	String tileURL=null;

	String khRef = GoogleTiles.getKeyholeRef(lon, lat, zoom);

	if (mapType.equals("sat")) {
		fileRef = tileRef = khRef;
	 	tileURL= "http://kh1.google.com/kh?v=4&t=" + khRef;
		llBox = GoogleTiles.getLatLong(khRef);
	} else if (mapType.equals("map")) {
		GoogleTiles.XY xy = GoogleTiles.getTileXY(khRef);

		// Hmm, somehow we need to request one zoomlevel lower...
		zoom -= 1;

		tileRef = "x=" + xy.x + "&y=" + xy.y + "&zoom=" + zoom;
		fileRef = xy.x + "-" + xy.y + "-" + zoom;

		// e.g. http://mt.google.com/mt?v=.1&x=480&y=-109&zoom=5;
        tileURL= "http://mt" + GoogleTiles.getTileServerNo(xy.x, xy.y) +".google.com/mt?n=404&v=w2.7&" + tileRef;
        llBox = GoogleTiles.getLatLong(xy.x, xy.y, zoom);
	}


	File file = new File(CACHE_DIR + fileRef + ".jpg");
	if (!file.exists()) {
		Net.fetchURL(tileURL, file);
	} else {
		log.info("tile=" + tileRef + " from cache");
	}

	// File with plotted location
	File locFile = new File(CACHE_DIR + tileRef + "-loc.jpg");

	int x= GoogleTiles.getX(lon, llBox);
	int y= GoogleTiles.getY(lat, llBox);
	int x0 = x+3;
	int y0 = y+3;

	String[] command = {DRAW_LOC_SCRIPT, ""+x, ""+y, ""+x0, ""+y0, file.getAbsolutePath(), locFile.getAbsolutePath()};

    StringBuffer stdout = new StringBuffer(24);
	StringBuffer stderr = new StringBuffer(24);
	int exitCode = Sys.execute(command, stdout, stderr);

	if (exitCode == 0) {
		log.info("drawloc returned " + exitCode);
	} else {
		log.warn("drawloc returned " + exitCode + " stderr=" + stderr.toString());
	}
	Servlets.sendFile(response, locFile.getAbsolutePath(), "image/jpeg");
	locFile.delete();
%>


