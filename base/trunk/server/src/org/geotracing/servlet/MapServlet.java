// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.
package org.geotracing.servlet;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.gis.GoogleTiles;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Rand;
import org.keyworx.common.util.Sys;
import org.keyworx.oase.util.Net;
import org.keyworx.oase.util.Servlets;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Render map images to client.
 */
public class MapServlet extends HttpServlet {

	private Log log;
	public static final String CACHE_DIR = System.getProperty("java.io.tmpdir") + "/";
	public static final Map IMG_FORMATS = new HashMap(3);

	static {// Make exif script executable
		IMG_FORMATS.put("image/jpeg", ".jpg");
		IMG_FORMATS.put("image/png", ".png");
		IMG_FORMATS.put("image/gif", ".gif");
		// IO.mkdir(CACHE_DIR);
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

	String getKeyholeRef(GoogleTiles.LonLat ll, int zoom) {
		return GoogleTiles.getKeyholeRef(ll.lon, ll.lat, zoom);
	}

	String getEnclosingKeyholeRef(BBox aBBox) {
		String result = null;
		String khRef = null;
		for (int zoom = 0; zoom < 18; zoom++) {
			khRef = getEnclosingKeyholeRef(aBBox, zoom);
			if (khRef != null) {
				result = khRef;
			} else {
				break;
			}
		}
		return result;
	}

	String getEnclosingKeyholeRef(BBox aBBox, int aZoom) {
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
		String khRef = null;
		int zoom = 0;
		for (zoom = 0; zoom < 18; zoom++) {
			khRef = getEnclosingKeyholeRef(aBBox, zoom);
			if (khRef == null) {
				// First zoom level where bbox not fits in tile
				break;
			}
		}

		// First zoomlevel where bbox is completely enclosed
		// try to find optimal zoomlevel ozoom from here.
		String nw, ne, se, sw;

		// Bbox already in adjacent tiles in zoom, increase optimal zoom.
		for (int ozoom = zoom + 1; ozoom < 18; ozoom++) {
			nw = getKeyholeRef(aBBox.nw, ozoom);
			ne = getKeyholeRef(aBBox.ne, ozoom);
			sw = getKeyholeRef(aBBox.sw, ozoom);
			se = getKeyholeRef(aBBox.se, ozoom);

			// Check if bbox is enclosed in adjacent tiles
			// if so we can increase zoomlevel
			if (GoogleTiles.getAdjacentKeyholeRef(nw, "se").equals(se) && GoogleTiles.getAdjacentKeyholeRef(sw, "ne").equals(ne))
			{
				continue;
			}
			if (GoogleTiles.getAdjacentKeyholeRef(nw, "e").equals(ne) && nw.equals(sw)) {
				continue;
			}
			if (GoogleTiles.getAdjacentKeyholeRef(sw, "e").equals(se) && sw.equals(nw)) {
				continue;
			}
			if (GoogleTiles.getAdjacentKeyholeRef(nw, "s").equals(sw) && nw.equals(ne)) {
				continue;
			}
			if (GoogleTiles.getAdjacentKeyholeRef(ne, "s").equals(se) && ne.equals(nw)) {
				continue;
			}

			// Break: at this zoomlevel we have no adjacency for bbox
			// decrease to previous and return below.
			zoom = ozoom - 1;
			break;
		}
		return zoom;
	}


	class BBox {
		GoogleTiles.LonLat nw, sw, ne, se;

		public BBox(String aBBox) {
			String[] bboxArr = aBBox.split(",");
			nw = new GoogleTiles.LonLat(bboxArr[0], bboxArr[3]);
			sw = new GoogleTiles.LonLat(bboxArr[0], bboxArr[1]);
			ne = new GoogleTiles.LonLat(bboxArr[2], bboxArr[3]);
			se = new GoogleTiles.LonLat(bboxArr[2], bboxArr[1]);
		}
	}

	class Tile {
		public String khRef;
		public GoogleTiles.XY tileRef;
		public GoogleTiles.LonLat ll;
		public GoogleTiles.XY pxy;
		public String type;
		public File file;
		public int zoom;
		private String url;


		public Tile(String aType, String aKHRef, GoogleTiles.LonLat aLL) {
			type = aType;
			if (type.equals("streets")) {
				type = "map";
			}
			khRef = aKHRef;
			ll = aLL;
			zoom = GoogleTiles.getTileZoom(khRef);
			setPoint(aLL);
		}

		public Tile(String aType, String aKHRef) {
			this(aType, aKHRef, null);
		}

		public Tile(String aType, GoogleTiles.LonLat aLL, int aZoom) {
			this(aType, GoogleTiles.getKeyholeRef(aLL.lon, aLL.lat, aZoom), aLL);
		}

		public void clear() {
			if (file != null) {
				file.delete();
				file = null;
			}
		}

		public void fetch() throws Exception {
			Net.fetchURL(getURL(), getFile());
		}

		public File getFile() {
			if (file == null) {
				if (type.equals("sat")) {
					file = new File(CACHE_DIR + khRef + Rand.randomString(3) + ".jpg");
				} else if (type.equals("map")) {
					String fileRef = getTileRef().x + "-" + getTileRef().y + "-" + zoom;
					file = new File(CACHE_DIR + fileRef + Rand.randomString(3) + ".png");
				}
			}
			return file;
		}

		public String getFilePath() {
			return getFile().getAbsolutePath();
		}

		public GoogleTiles.XY getTileRef() {
			if (tileRef == null) {
				tileRef = GoogleTiles.getTileXY(khRef);
			}
			return tileRef;
		}

		public String getTileRefStr() {
			return "x=" + getTileRef().x + "&y=" + getTileRef().y + "&zoom=" + (17 - zoom);
		}

		public String getURL() {
			if (url == null) {
				if (type.equals("sat")) {
					int num = Rand.randomInt(0, 3);
					url = "http://kh" + num + ".google.com/kh?v=10&t=" + khRef;
				} else if (type.equals("map")) {
					// e.g. http://mt.google.com/mt?v=.1&x=480&y=-109&zoom=5;
					// v=ap.31
					url = "http://mt" + GoogleTiles.getTileServerNo(getTileRef().x, getTileRef().y) + ".google.com/mt?" + getTileRefStr();
				}

			}
			return url;
		}

		public void setPoint(GoogleTiles.LonLat aLL) {
			if (aLL != null) {
				pxy = GoogleTiles.getPixelXY(ll.lon, ll.lat, zoom);
			}
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
		public File file, fileCrop;
		public String layer;
		public int width;
		public int height;
		String cropStr = null;
		int cropWidth, cropHeight;
		String format;

		public TileComp(String aLayer, BBox aBBox, int aWidth, int aHeight, String aFormat) {
			bbox = aBBox;
			width = aWidth;
			height = aHeight;
			format = aFormat;
			layer = aLayer;
			zoom = getZoom(bbox);
			init();
		}


		public void init() {
			nw = new Tile(layer, bbox.nw, zoom);
			ne = new Tile(layer, bbox.ne, zoom);
			sw = new Tile(layer, bbox.sw, zoom);
			se = new Tile(layer, bbox.se, zoom);

			// default: bbox contained in 4 tiles
			type = 3;
			if (nw.khRef.equals(ne.khRef) || sw.khRef.equals(se.khRef)) {
				// 2 tiles n-s
				type = 1;
			} else if (nw.khRef.equals(sw.khRef) || ne.khRef.equals(se.khRef)) {
				// 2 tiles e-w
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
			String ext = (String) IMG_FORMATS.get(format);

			file = new File(CACHE_DIR + Rand.randomString(8) + "-wms" + ext);
			fileCrop = new File(CACHE_DIR + Rand.randomString(8) + "-wmsc" + ext);

			// 153600_610800.png -geometry +0+0 -composite
			String[] command = null;
			switch (type) {
				// n-s composite
				case 1:
					nw.fetch();
					sw.fetch();
					// , "-crop", "100x100+10+20"
					cropWidth = ne.pxy.x - nw.pxy.x;
					cropHeight = 256 + sw.pxy.y - nw.pxy.y;
					cropStr = cropWidth + "x" + cropHeight + "+" + nw.pxy.x + "+" + nw.pxy.y;
					String[] c1 = {"convert", "-size", "256x512", "xc:skyblue", nw.getFilePath(), "-geometry", "+0+0", "-composite", sw.getFilePath(), "-geometry", "+0+256", "-composite", "-crop", cropStr, fileCrop.getAbsolutePath()};
					command = c1;
					break;
					// e-w composite
				case 2:
					nw.fetch();
					ne.fetch();
					cropWidth = 256 + ne.pxy.x - nw.pxy.x;
					cropHeight = sw.pxy.y - nw.pxy.y;
					cropStr = cropWidth + "x" + cropHeight + "+" + nw.pxy.x + "+" + nw.pxy.y;
					String[] c2 = {"convert", "-size", "512x256", "xc:skyblue", nw.getFilePath(), "-geometry", "+0+0", "-composite", ne.getFilePath(), "-geometry", "+256+0", "-composite", "-crop", cropStr, fileCrop.getAbsolutePath()};
					command = c2;
					break;
				case 3:
					nw.fetch();
					ne.fetch();
					sw.fetch();
					se.fetch();
					// , "-crop", "100x100+10+20"
					cropWidth = 256 + ne.pxy.x - nw.pxy.x;
					cropHeight = 256 + sw.pxy.y - nw.pxy.y;
					cropStr = cropWidth + "x" + cropHeight + "+" + nw.pxy.x + "+" + nw.pxy.y;
					String[] c3 = {"convert", "-size", "512x512", "xc:skyblue", nw.getFilePath(), "-geometry", "+0+0", "-composite", ne.getFilePath(), "-geometry", "+256+0", "-composite", sw.getFilePath(), "-geometry", "+0+256", "-composite", se.getFilePath(), "-geometry", "+256+256", "-composite", "-crop", cropStr, fileCrop.getAbsolutePath()};
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


	public void init() {
		try {
			log = Logging.getLog("MapServlet");
			// Assume cfg dir in WEB-INF/cfg first
			// String cfgDirPath = getServletContext().getRealPath("/") + "/WEB-INF/cfg";
			log.info("init - MapServlet started");
		} catch (Throwable t) {
			log.error("ERROR starting MapServlet : ", t);
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String bboxParm = getParameter(request, "bbox", null);
		String format = getParameter(request, "format", "image/jpeg");
		String layers = getParameter(request, "layers", "sat");

		int width = Integer.parseInt(getParameter(request, "width", "256"));
		int height = Integer.parseInt(getParameter(request, "height", "256"));

		BBox bbox = new BBox(bboxParm);
// http://test.geotracing.com/gt/map/gmap-wms.jsp?LAYERS=map&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&FORMAT=image%2Fjpeg&SRS=EPSG%3A4326&BBOX=4.532386%2C46.489192%2C7.344886%2C49.301692&WIDTH=512&HEIGHT=512
		TileComp tileComp = new TileComp(layers, bbox, width, height, format);

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
			rsp.setAttr("sw", tileComp.sw.khRef);
			rsp.setAttr("ne", tileComp.ne.khRef);
			rsp.setAttr("se", tileComp.se.khRef);
			rsp.setAttr("type", tileComp.type);
			try {
				Writer writer = response.getWriter();
				writer.write(rsp.toFormattedString());
				writer.flush();
				writer.close();
			} catch (Throwable th) {
				log.info("error gmap writing response");
			}
		} else {
			// Create and send image
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
				log.warn("error sending composite file" + t);
			} finally {
				tileComp.clear();
			}
		}
	}

	public void destroy() {
		log.info("destroy(): Stopping MapServlet");
	}
}

