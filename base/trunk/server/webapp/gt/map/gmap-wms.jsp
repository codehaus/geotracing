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
	private static Log log;
	public static final String CACHE_DIR = "/tmp/";

	static {// Make exif script executable
		log = Logging.getLog("GoogleTile");
	}

	String getParameter(ServletRequest req, String name, String defaultValue) {
		String value = req.getParameter(name.toUpperCase());
		if (value == null || value.length() == 0) {
			 value = req.getParameter(name.toLowerCase());
		}

		if (value == null || value.length() == 0) {
			return defaultValue;
		}

		return value.trim();
	}

	String getKeyholeRef(LonLat ll, int zoom) {
		return GoogleTiles.getKeyholeRef(ll.lon, ll.lat, zoom);
	}

	String getKeyholeRef(BBox aBBox) {
		String result=null;
		String khRef=null;
		for (int zoom=0; zoom < 18; zoom++) {
			khRef = getKeyholeRef(aBBox, zoom);
			if (khRef != null) {
				result = khRef;
			} else {
				break;
			}
		}
		return result;
	}

	String getKeyholeRef(BBox aBBox, int aZoom) {
		String result = null;
		String[] khRef=new String[4];
		khRef[0] = getKeyholeRef(aBBox.nw, aZoom);
		khRef[1] = getKeyholeRef(aBBox.ne, aZoom);
		khRef[2] = getKeyholeRef(aBBox.sw, aZoom);
		khRef[3] = getKeyholeRef(aBBox.se, aZoom);

		if (khRef[0].equals(khRef[1]) &&
				khRef[2].equals(khRef[3]) &&
				khRef[0].equals(khRef[2]) &&
				khRef[1].equals(khRef[3])
				) {
			  result = khRef[0];
		}
		return result;
	}

	class LonLat {
		public double lon;
		public double lat;

		public LonLat(String aLon, String aLat) {
			this(Double.parseDouble(aLon), Double.parseDouble(aLat));
		}

		public LonLat(double aLon, double aLat) {
			lon = aLon;
			lat = aLat;
		}
	}

	class BBox {
		LonLat nw,sw,ne,se;

		public BBox(String aBBox) {
			String[] bboxArr = aBBox.split(",");
			nw = new LonLat(bboxArr[0], bboxArr[3]);
			sw = new LonLat(bboxArr[0], bboxArr[1]);
			ne = new LonLat(bboxArr[2], bboxArr[3]);
			se = new LonLat(bboxArr[2], bboxArr[1]);
		}
	}

%>
<%
	String bboxParm = getParameter(request, "bbox", null);
	String format = getParameter(request, "format", "image/jpeg");
	String layers = getParameter(request, "layers", "sat");

	int width = Integer.parseInt(getParameter(request, "width", "256"));
	int height = Integer.parseInt(getParameter(request, "height", "256"));

	BBox bbox = new BBox(bboxParm);
	String khRef = getKeyholeRef(bbox);
	int zoom = GoogleTiles.getTileZoom(khRef);

	String tileRef = null;
	String fileRef = null;
	String tileURL = null;

	File file = null;

	if (layers.equals("sat")) {
		fileRef = tileRef = khRef;
		tileURL = "http://kh1.google.com/kh?v=10&t=" + khRef;
		file = new File(CACHE_DIR + fileRef + ".jpg");
		// llBox = GoogleTiles.getBoundingBox(khRef);
	} else if (layers.equals("map")) {
		GoogleTiles.XY xy = GoogleTiles.getTileXY(khRef);

		// Hmm, somehow we need to request one zoomlevel lower...
		// zoom -= 1;

		tileRef = "x=" + xy.x + "&y=" + xy.y + "&zoom=" + (17 - zoom);
		fileRef = xy.x + "-" + xy.y + "-" + zoom;
		file = new File(CACHE_DIR + fileRef + ".png");

		// e.g. http://mt.google.com/mt?v=.1&x=480&y=-109&zoom=5;
		// v=ap.31
		tileURL = "http://mt" + GoogleTiles.getTileServerNo(xy.x, xy.y) + ".google.com/mt?" + tileRef;
		// llBox = GoogleTiles.getBoundingBox(xy.x, xy.y, zoom);
	}

	// Get pixel offsets in tile image for cropping
	// 165x185+70+35
	GoogleTiles.XY xyNW = GoogleTiles.getPixelXY(bbox.nw.lon, bbox.nw.lat, zoom);
	GoogleTiles.XY xySE = GoogleTiles.getPixelXY(bbox.se.lon, bbox.se.lat, zoom);
	int cropWidth = xySE.x - xyNW.x;
	int cropHeight = xySE.y - xyNW.y;
	String cropString = "" + cropWidth + "x" + cropHeight + "+" + xyNW.x + "+" + xyNW.y;

	if (format.equals("xml")) {

		response.setContentType("text/xml;charset=utf-8");
		JXElement rsp = new JXElement("gmap");
		rsp.setAttr("url", tileURL);
		rsp.setAttr("cropWidth", cropWidth);
		rsp.setAttr("cropHeight", cropHeight);
		rsp.setAttr("x", xySE.x);
		rsp.setAttr("y", xySE.y);
		rsp.setAttr("khref", khRef);
		 try {
			 Writer writer = response.getWriter();
			 writer.write(rsp.toFormattedString());
			 writer.flush();
			 writer.close();
		 } catch (Throwable th) {
	  		 log.info("error gmap writing response");
		 }
	} else {
		Net.fetchURL(tileURL, file);

		String size = width + "x" + height + "!";

		File resultFile1 = new File(CACHE_DIR + Rand.randomString(8) + "-wms.jpg");
		File resultFile2 = new File(CACHE_DIR + Rand.randomString(8) + "-wms.jpg");

		String[] command = {"convert", "-crop", cropString, file.getAbsolutePath(), resultFile1.getAbsolutePath()};

		StringBuffer stdout = new StringBuffer(24);
		StringBuffer stderr = new StringBuffer(24);
		int exitCode = Sys.execute(command, stdout, stderr);

		if (exitCode == 0) {
			// log.info("drawloc returned " + exitCode);
		} else {
			log.warn("convert returned " + exitCode + " stderr=" + stderr.toString());
		}
		String[] command2 = {"convert", "-resize", size, resultFile1.getAbsolutePath(), resultFile2.getAbsolutePath()};
		 stdout = new StringBuffer(24);
		 stderr = new StringBuffer(24);
		 exitCode = Sys.execute(command2, stdout, stderr);

		if (exitCode == 0) {
			// log.info("drawloc returned " + exitCode);
		} else {
			log.warn("convert returned " + exitCode + " stderr=" + stderr.toString());
		}


		Servlets.sendFile(request, response, resultFile2.getAbsolutePath(), "image/jpeg", false);
		resultFile1.delete();
		resultFile2.delete();
		file.delete();
	}
%>


