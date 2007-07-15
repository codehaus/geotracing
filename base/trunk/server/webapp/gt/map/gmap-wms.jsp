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
<%@ page import="java.io.File" %>
<%@ page import="java.io.Writer" %>
<%!
	private static Log log;
	public static final String CACHE_DIR = "/tmp/wms/";

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
		String result = null;
		String khRef = null;
		for (int zoom = 0; zoom < 18; zoom++) {
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
		String[] khRef = new String[4];
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

	int getZoom(BBox aBBox) {
		String result = null;
		String khRef = null;
		int zoom = 0;
		for (zoom = 0; zoom < 18; zoom++) {
			khRef = getKeyholeRef(aBBox, zoom);
			if (khRef != null) {
				result = khRef;
			} else {
				break;
			}
		}
		return zoom;
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
		LonLat nw, sw, ne, se;

		public BBox(String aBBox) {
			String[] bboxArr = aBBox.split(",");
			nw = new LonLat(bboxArr[0], bboxArr[3]);
			sw = new LonLat(bboxArr[0], bboxArr[1]);
			ne = new LonLat(bboxArr[2], bboxArr[3]);
			se = new LonLat(bboxArr[2], bboxArr[1]);
		}
	}

	class XY {
		public int x;
		public int y;
	}

	class Tile {
		public String khRef;
		public LonLat ll;
		public GoogleTiles.XY xy;
		public String type;
		public File file;
		public int zoom;

		public Tile(String aType, String aKHRef, LonLat aLL) {
			type = aType;
			khRef = aKHRef;
			ll = aLL;
			zoom = GoogleTiles.getTileZoom(khRef);
			xy = GoogleTiles.getPixelXY(ll.lon, ll.lat, zoom);
		}

		public void clear() {
			if (file != null) {
				file.delete();
				file = null;
			}
		}

		public void fetchFile() throws Exception {
			String tileURL=null;
			String tileRef = null;
			String fileRef = null;
			if (type.equals("sat")) {
				fileRef = tileRef = khRef;
				tileURL = "http://kh1.google.com/kh?v=10&t=" + khRef;
				file = new File(CACHE_DIR + fileRef + Rand.randomString(3) + ".jpg");
				// llBox = GoogleTiles.getBoundingBox(khRef);
			} else if (type.equals("map")) {
				GoogleTiles.XY xy = GoogleTiles.getTileXY(khRef);

				// Hmm, somehow we need to request one zoomlevel lower...
				// zoom -= 1;

				tileRef = "x=" + xy.x + "&y=" + xy.y + "&zoom=" + (17 - zoom);
				fileRef = xy.x + "-" + xy.y + "-" + zoom;
				file = new File(CACHE_DIR + fileRef + Rand.randomString(3) + ".png");

				// e.g. http://mt.google.com/mt?v=.1&x=480&y=-109&zoom=5;
				// v=ap.31
				tileURL = "http://mt" + GoogleTiles.getTileServerNo(xy.x, xy.y) + ".google.com/mt?" + tileRef;
				// llBox = GoogleTiles.getBoundingBox(xy.x, xy.y, zoom);
			}
			Net.fetchURL(tileURL, file);
		}

		public String getFilePath() {
			return file.getAbsolutePath();
		}
	}

	class TileComp {
		public Tile nw;
		public Tile ne;
		public Tile sw;
		public Tile se;
		public int type;
		public int zoom;
		public BBox bbox;
		public File file,fileCrop;
		public String layer;
		public int width;
		public int height;
		String cropStr = null;
		int cropWidth, cropHeight;

		public TileComp(String aLayer, BBox aBBox, int aWidth, int aHeight) {
			bbox = aBBox;
			width = aWidth;
			height = aHeight;
			zoom = getZoom(aBBox);
			layer = aLayer;
			nw = new Tile(layer, getKeyholeRef(bbox.nw, zoom), bbox.nw);
			ne = new Tile(layer, getKeyholeRef(bbox.ne, zoom), bbox.ne);
			sw = new Tile(layer, getKeyholeRef(bbox.sw, zoom), bbox.sw);
			se = new Tile(layer, getKeyholeRef(bbox.se, zoom), bbox.se);

			type = 3;
			if (nw.khRef.equals(ne.khRef)) {
				type = 1;
			} else if (nw.khRef.equals(sw.khRef)) {
				type = 2;
			}
		}

		public void clear() {
			nw.clear();
			ne.clear();
			sw.clear();
			se.clear();
			if (fileCrop != null) {
				fileCrop.delete();
				fileCrop = null;
			}
			if (file != null) {
				file.delete();
				file = null;
			}
		}

		public void createFile() throws Exception {
			String ext = layer.equals("sat") ? ".jpg" : ".png";

			file = new File(CACHE_DIR + Rand.randomString(8) + "-wms.jpg");
			fileCrop = new File(CACHE_DIR + Rand.randomString(8) + "-wmsc.jpg");

			// 153600_610800.png -geometry +0+0 -composite
			String[] command = null;
			switch(type) {
				// n-s composite
				case 1:
					nw.fetchFile();
					sw.fetchFile();
					// , "-crop", "100x100+10+20"
					 cropWidth = ne.xy.x - nw.xy.x;
					 cropHeight = 256 + sw.xy.y - nw.xy.y;
					cropStr = cropWidth + "x" + cropHeight + "+" + nw.xy.x + "+" + nw.xy.y;
					String[] c1 = {"convert", "-size", "256x512", "xc:skyblue", nw.getFilePath(), "-geometry", "+0+0", "-composite", sw.getFilePath(), "-geometry", "+0+256", "-composite", "-crop", cropStr, fileCrop.getAbsolutePath()};
					command = c1;
					break;
				// e-w composite
				case 2:
					nw.fetchFile();
					ne.fetchFile();
					cropWidth = 256 + ne.xy.x - nw.xy.x;
					cropHeight = sw.xy.y - nw.xy.y;
				    cropStr = cropWidth + "x" + cropHeight + "+" + nw.xy.x + "+" + nw.xy.y;
					String[] c2 = {"convert", "-size", "512x256",  "xc:skyblue", nw.getFilePath(), "-geometry", "+0+0", "-composite", ne.getFilePath(), "-geometry", "+256+0", "-composite", "-crop", cropStr, fileCrop.getAbsolutePath()};
					command = c2;
					break;
				case 3:
					nw.fetchFile();
					ne.fetchFile();
					sw.fetchFile();
					se.fetchFile();
					// , "-crop", "100x100+10+20"
					 cropWidth = 256 + ne.xy.x - nw.xy.x;
					 cropHeight = 256 + sw.xy.y - nw.xy.y;
					 cropStr = cropWidth + "x" + cropHeight + "+" + nw.xy.x + "+" + nw.xy.y;
					String[] c3 = {"convert", "-size", "512x512",  "xc:skyblue", nw.getFilePath(), "-geometry", "+0+0", "-composite", ne.getFilePath(), "-geometry", "+256+0", "-composite", sw.getFilePath(), "-geometry", "+0+256", "-composite", se.getFilePath(), "-geometry", "+256+256", "-composite", "-crop", cropStr, fileCrop.getAbsolutePath()};
					command = c3;
					break;
			}


			StringBuffer stdout = new StringBuffer(24);
			StringBuffer stderr = new StringBuffer(24);
			int exitCode = Sys.execute(command, stdout, stderr);

			if (exitCode == 0) {
				 // log.info("createFile returned " + exitCode);
			} else {
				log.warn("convert returned " + exitCode + " stderr=" + stderr.toString());
			}

			String size = width + "x" + height + "!";

			String[] command2 = {"convert", "-resize", size, fileCrop.getAbsolutePath(), file.getAbsolutePath()};
			stdout = new StringBuffer(24);
			stderr = new StringBuffer(24);
			exitCode = Sys.execute(command2, stdout, stderr);

			if (exitCode == 0) {
				// log.info("drawloc returned " + exitCode);
			} else {
				log.warn("convert returned " + exitCode + " stderr=" + stderr.toString());
			}

		}

		public String getFilePath() {
			return file.getAbsolutePath();
		}

		public String toString() {
			return "l=" + layer + " type=" + type + " zoom=" + zoom + " w=" + width + " h=" + height + " cw=" + cropWidth + " ch=" + cropHeight;
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
// http://test.geotracing.com/gt/map/gmap-wms.jsp?LAYERS=map&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&FORMAT=image%2Fjpeg&SRS=EPSG%3A4326&BBOX=4.532386%2C46.489192%2C7.344886%2C49.301692&WIDTH=512&HEIGHT=512
	TileComp tileComp = new TileComp(layers, bbox, width, height);

	if (format.equals("xml")) {
		response.setContentType("text/xml;charset=utf-8");
		JXElement rsp = new JXElement("gmap");
	//	rsp.setAttr("url", tileURL);
	//	rsp.setAttr("cropWidth", cropWidth);
	//	rsp.setAttr("cropHeight", cropHeight);
//		rsp.setAttr("x", xySE.x);
//		rsp.setAttr("y", xySE.y);
//		rsp.setAttr("khref", khRef);
//		rsp.setAttr("zoom", zoom);
		rsp.setAttr("zoomComp", tileComp.zoom);
		rsp.setAttr("nw", tileComp.nw.khRef);
		rsp.setAttr("sw",  tileComp.sw.khRef);
		rsp.setAttr("ne",  tileComp.ne.khRef);
		rsp.setAttr("se",  tileComp.se.khRef);
		rsp.setAttr("type",  tileComp.type);
		try {
			Writer writer = response.getWriter();
			writer.write(rsp.toFormattedString());
			writer.flush();
			writer.close();
		} catch (Throwable th) {
			log.info("error gmap writing response");
		}
	} else {

		try {
			tileComp.createFile();
		} catch (Throwable t) {
			log.error("error creating composite file", t);
			tileComp.clear();
			return;
		}

		log.info(tileComp.toString());

		try {
			Servlets.sendFile(request, response, tileComp.getFilePath(), "image/jpeg", false);
		} catch (Throwable t) {
			log.error("error sending composite file", t);
		} finally {
			tileComp.clear();
		}
	}
%>


