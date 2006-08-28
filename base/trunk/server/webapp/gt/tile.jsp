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
	// Get single Google maps tile
    // $Id: tile.jsp,v 1.10 2006-07-06 23:06:16 just Exp $
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

	// http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	public static int lonToX(double lon, int zoom) {
		double TILE_SIZE = 256.0D;
		double tiles = Math.pow(2, 17-zoom);
		double circumference = TILE_SIZE * tiles;
		double falseEasting = circumference/2.0D;
		double radius = circumference / (2 * Math.PI);
		double longitude = Math.toRadians(lon);
		int x = (int) (radius * longitude);
		System.out.println("x1=" + x) ;
		x = (int) falseEasting + x ;
		System.out.println("x2=" + x) ;

		int tilesOffset = x / (int) TILE_SIZE;
		x =  x - (int) (tilesOffset * TILE_SIZE) ;
		System.out.println("x3=" + x +" tilesOffset =" + tilesOffset);

		return x;
	}

	// http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	public static int latToY(double lat, int zoom) {
      double TILE_SIZE = 256.0D;
      double tiles = Math.pow(2, 17-zoom);
      double circumference = TILE_SIZE * tiles;
      double falseNorthing = circumference/2.0D;
      double radius = circumference / (2 * Math.PI);
      double latitude = Math.toRadians(lat);
	  System.out.println("tiles=" + tiles + " circumference=" + circumference + " falseNorthing="  + falseNorthing);

	  int y = (int) (radius/2.0 * Math.log( (1.0 + Math.sin(latitude)) / (1.0 - Math.sin(latitude)) ));
      System.out.println("y1=" + y) ;

	  y = (y - (int) falseNorthing) * -1;
	  System.out.println("y2=" + y) ;

	  // Number of pixels to subtract for tiles skipped (offset)
	  int tilesOffset = y / (int) TILE_SIZE;
      y =  y - (int) (tilesOffset * TILE_SIZE) ;
      System.out.println("y3=" + y +" tilesOffset =" + tilesOffset);
      return y;
  }

%>
<%
    double lon = Double.parseDouble(getParameter(request, "lon", "0"));
	double lat = Double.parseDouble(getParameter(request, "lat", "0"));
	int zoom = Integer.parseInt(getParameter(request, "zoom", "10"));
    String mapType = getParameter(request, "type", "map");

	String tileRef = null;
	String fileRef = null;
    Rectangle2D.Double llBox = null;
	String tileURL=null;

	String khRef = GoogleTiles.getKeyholeRef(lon, lat, zoom);

	if (mapType.equals("sat")) {
		fileRef = tileRef = khRef;
		// v2: /kh?n=404&v=6&cookie=fzwq1IBDFYbm257TeTY1EVO6wDPTVAM8EOEuJw&t=trtqtqtrrrrq
		// api v1  tileURL= "http://kh1.google.com/kh?v=4&t=" + khRef;
		tileURL= "http://kh1.google.com/kh?v=6&t=" + khRef;
		llBox = GoogleTiles.getLatLong(khRef);
		zoom -=1;
	} else if (mapType.equals("map")) {
		GoogleTiles.XY xy = GoogleTiles.getTileXY(khRef);

		// Hmm, somehow we need to request one zoomlevel lower...
		zoom -= 1;

		tileRef = "x=" + xy.x + "&y=" + xy.y + "&zoom=" + zoom;
		fileRef = xy.x + "-" + xy.y + "-" + zoom;

		// e.g. http://mt.google.com/mt?v=.1&x=480&y=-109&zoom=5;
		// v2: /mt?n=404&v=ap.6&x=1049&y=672&zoom=6
		// v1 tileURL= "http://mt" + GoogleTiles.getTileServerNo(xy.x, xy.y) +".google.com/mt?n=404&v=w2.7&" + tileRef;
		tileURL= "http://mt" + GoogleTiles.getTileServerNo(xy.x, xy.y) +".google.com/mt?n=404&v=ap.6&" + tileRef;
        llBox = GoogleTiles.getLatLong(xy.x, xy.y, zoom);
	}


	File file = new File(CACHE_DIR + fileRef + ".jpg");
	if (!file.exists()) {
		Net.fetchURL(tileURL, file);
	} else {
		log.trace("tile=" + tileRef + " from cache");
	}

	// File with plotted location
	File locFile = new File(CACHE_DIR + tileRef + "-loc.jpg");

	//int x= GoogleTiles.getX(lon, llBox);
	//int y= GoogleTiles.getY(lat, llBox);
	int x = lonToX(lon, zoom);
	int y = latToY(lat, zoom);
	int x0 = x+5;
	int y0 = y+5;

	String[] command = {DRAW_LOC_SCRIPT, ""+x, ""+y, ""+x0, ""+y0, file.getAbsolutePath(), locFile.getAbsolutePath()};

    StringBuffer stdout = new StringBuffer(24);
	StringBuffer stderr = new StringBuffer(24);
	int exitCode = Sys.execute(command, stdout, stderr);

	if (exitCode == 0) {
		log.trace("drawloc returned " + exitCode);
	} else {
		log.warn("drawloc returned " + exitCode + " stderr=" + stderr.toString());
	}
	Servlets.sendFile(request, response, locFile.getAbsolutePath(), "image/jpeg", false);
	locFile.delete();
%>


