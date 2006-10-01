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

	/** returns the Google zoom level for the keyhole string. */
	public static int getY(double lat, Rectangle2D.Double llBox) {
		// 256/lengte * ln(tan(PI/4 + (lat)/2)
		return 256 - (int) (256.0D / Math.toRadians(llBox.height) * Math.log(Math.tan(Math.PI/4 + Math.toRadians(lat)/2)));
	}

		/**
	 * returns a keyhole string for a longitude (x), latitude (y), and zoom
	 */
	public static String getKeyholeRef(double lon, double lat, int zoom) {
		// zoom = 18 - zoom;

		// first convert the lat lon to transverse mercator coordintes.
		if (lon > 180) {
			lon -= 360;
		}

		lon /= 180;

		// convert latitude to a range -1..+1
		lat = Math.log(Math.tan((Math.PI / 4) + ((0.5 * Math.PI * lat) / 180))) / Math.PI;

		double tLat = -1;
		double tLon = -1;
		double lonWidth = 2;
		double latHeight = 2;

		StringBuffer keyholeString = new StringBuffer("t");

		for (int i = 0; i < zoom; i++) {
			lonWidth /= 2;
			latHeight /= 2;

			if ((tLat + latHeight) > lat) {
				if ((tLon + lonWidth) > lon) {
					keyholeString.append('t');
				} else {
					tLon += lonWidth;
					keyholeString.append('s');
				}
			} else {
				tLat += latHeight;

				if ((tLon + lonWidth) > lon) {
					keyholeString.append('q');
				} else {
					tLon += lonWidth;
					keyholeString.append('r');
				}
			}
		}

		return keyholeString.toString();
	}
	// http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	public static int lonToX(double lon, int zoom) {
		double TILE_SIZE = 256.0D;
		double tiles = Math.pow(2, zoom);
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
      double tiles = Math.pow(2, zoom);
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

	public static final double TILE_SIZE = 256.0D;
		/**
	 * Returns x,y of tile for Keyhole string (e.g. "trtqstr" etc).
	 * See:
	 * http://dunck.us/collab/Simple_20Analysis_20of_20Google_20Map_20and_20Satellite_20Tiles
	 */
	public static GoogleTiles.XY getTileXY(String keyholeStr) {
		// must start with "t"
		if ((keyholeStr == null) || (keyholeStr.length() == 0) || (keyholeStr.charAt(0) != 't')) {
			throw new RuntimeException("Keyhole string must start with 't'");
		}

		int x = 0, y = 0;
		for (int i = 1; i < keyholeStr.length(); i++) {
			x = x << 1;
			y = y << 1;

			char c = keyholeStr.charAt(i);

			switch (c) {
				case 'q':
					break;

				case 'r':
					x = x | 1;

					break;
				case 's':
					y = y | 1;
					x = x | 1;

					break;


				case 't':
					y = y | 1;
					break;

				default:
					throw new RuntimeException("unknown char '" + c + "' when decoding keyhole string.");
			}
		}

		GoogleTiles.XY xy = new GoogleTiles.XY();
		xy.x = x;
		xy.y = y;
		return xy;

	}

	/**
	 * Returns pixel offset in tile for given lon,lat,zoom.
	 * see http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	*/
	public static GoogleTiles.XY getPixelXY(double lon, double lat, int zoom) {
		// Number of tiles in each axis at zoom level
		double tiles = Math.pow(2, zoom);

		// Circumference in pixels
		double circumference = TILE_SIZE * tiles;
		double radius = circumference / (2 * Math.PI);

		// Use radians
		double longitude = Math.toRadians(lon);
		double latitude = Math.toRadians(lat);

		// To correct for origin in top left but calculated values from center
		double falseEasting = circumference / 2.0D;
		double falseNorthing = circumference / 2.0D;

		// Do x
		int x = (int) (radius * longitude);
		// System.out.println("x1=" + x);

		// Correct for false easting
		x = (int) falseEasting + x;
		// System.out.println("x2=" + x);

		int tilesXOffset = x / (int) TILE_SIZE;
		x = x - (int) (tilesXOffset * TILE_SIZE);
		// System.out.println("x3=" + x + " tilesXOffset =" + tilesXOffset);

		// Do y
		int y = (int) (radius / 2.0 * Math.log((1.0 + Math.sin(latitude)) / (1.0 - Math.sin(latitude))));
		// System.out.println("y1=" + y);

		// Correct for false northing
		y = (y - (int) falseNorthing) * -1;
		// System.out.println("y2=" + y);

		// Number of pixels to subtract for tiles skipped (offset)
		int tilesYOffset = y / (int) TILE_SIZE;
		y = y - (int) (tilesYOffset * TILE_SIZE);
		// System.out.println("y3=" + y + " tilesYOffset =" + tilesYOffset);

		GoogleTiles.XY xy = new GoogleTiles.XY();
		xy.x = x;
		xy.y = y;
		return xy;
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

	String khRef = null;
	File file = null;

	if (mapType.equals("sat")) {
		khRef = getKeyholeRef(lon, lat, zoom);
		fileRef = tileRef = khRef;
	 	tileURL= "http://kh1.google.com/kh?v=10&t=" + khRef;
		file = new File(CACHE_DIR + fileRef + ".jpg");
		llBox = GoogleTiles.getLatLong(khRef);
	} else if (mapType.equals("map")) {
		khRef = getKeyholeRef(lon, lat, zoom);
		GoogleTiles.XY xy = getTileXY(khRef);

		// Hmm, somehow we need to request one zoomlevel lower...
		// zoom -= 1;

		tileRef = "x=" + xy.x + "&y=" + xy.y + "&zoom=" + (17-zoom);
		fileRef = xy.x + "-" + xy.y + "-" + zoom;
		file = new File(CACHE_DIR + fileRef + ".png");

		// e.g. http://mt.google.com/mt?v=.1&x=480&y=-109&zoom=5;
        tileURL= "http://mt" + GoogleTiles.getTileServerNo(xy.x, xy.y) +".google.com/mt?v=w2.21&" + tileRef;
        llBox = GoogleTiles.getLatLong(xy.x, xy.y, zoom);
	}


	if (!file.exists()) {
		Net.fetchURL(tileURL, file);
	} else {
		Net.fetchURL(tileURL, file);
		// log.info("tile=" + tileRef + " from cache");
	}

	// File with plotted location
	File locFile = new File(CACHE_DIR + tileRef + "-loc.jpg");
	//int x = lonToX(lon, zoom);
	//int y = latToY(lat, zoom);
	GoogleTiles.XY xy = getPixelXY(lon, lat, zoom);
	int x = xy.x;
	int y = xy.y;
	int x0 = x+3;
	int y0 = y+3;

	String[] command = {DRAW_LOC_SCRIPT, ""+x, ""+y, ""+x0, ""+y0, file.getAbsolutePath(), locFile.getAbsolutePath()};

    StringBuffer stdout = new StringBuffer(24);
	StringBuffer stderr = new StringBuffer(24);
	int exitCode = Sys.execute(command, stdout, stderr);

	if (exitCode == 0) {
		// log.info("drawloc returned " + exitCode);
	} else {
		log.warn("drawloc returned " + exitCode + " stderr=" + stderr.toString());
	}
	Servlets.sendFile(request, response, locFile.getAbsolutePath(), "image/jpeg", false);
	locFile.delete();
%>


