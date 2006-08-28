package org.geotracing.server;

import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import org.geotracing.gis.GPSSample;
import org.geotracing.gis.GeoPoint;
import org.geotracing.gis.GoogleTiles;
import org.geotracing.gis.proj.WGS84toRD;
import org.geotracing.gis.proj.XY;
import org.keyworx.common.util.IO;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.*;

/**
 * Draws maps from track data.
 *
 * NOTE: a very rough version geared at GeoSkating.
 */
public class MapDrawer extends Component {
	private static final Color[] COLOR_INDEX = new Color[6];
	private JXElement properties;
	private String propertiesFilePath;
	private boolean forceDraw;
	private static long startTime;
	private MapTileManager mapTileManager;
	private int zoomMax, zoomMin;
	private Color lowZoomColor;
	private Color borderColor;
	private long lastTrackTime;
	private long newTrackTime;
	private int processedCnt = 0;

	public static void main(String[] args) {
			if (args.length != 1) {
			p("Usage: MapDrawer <mapdrawer.xml>");
			System.exit(0);
		}
		MapDrawer mapDrawer = new MapDrawer(args[0]);
		mapDrawer.init();
		mapDrawer.start();
		mapDrawer.stop();
	}

	public MapDrawer(String aPropertiesFilePath) {
		propertiesFilePath = aPropertiesFilePath;
	}

	public void init() {
		startTime = System.currentTimeMillis();

		p("START MapDrawer time=" + new Date(startTime));
		File propertiesFile = new File(propertiesFilePath);
		if (!propertiesFile.exists()) {
			e("properties file: " + propertiesFilePath + " does not exist");
			System.exit(-1);

		}
		try {
			properties = new JXBuilder().build(propertiesFile);
		} catch (Throwable t) {
			e("Cannot parse properties file: " + propertiesFile, t);
			System.exit(-1);
		}

		System.setProperty("java.awt.headless", getProperty(properties, "java.awt.headless"));

		forceDraw = getProperty(properties, "forceDraw").equals("true");
		zoomMin = getIntProperty(properties, "zoomMin");
		zoomMax = getIntProperty(properties, "zoomMax");

		// Get colors for road ratings (rr)
		JXElement rrColor = getProperties(properties, "rrColor");
		for (int i = 0; i < COLOR_INDEX.length; i++) {
			COLOR_INDEX[i] = getColorProperty(rrColor, "" + i);
		}

		lowZoomColor = getColorProperty(properties, "lowZoomColor");
		borderColor = getColorProperty(properties, "borderColor");
		lastTrackTime = getLongProperty(properties, "lastTrackTime");
		newTrackTime = lastTrackTime;
		mapTileManager = new MapTileManager(getProperties(properties, "tiles"));
		if (forceDraw) {
			if (!mapTileManager.deleteTiles()) {
				e("cannot delete tiles");
				return;
			}
		}

		mapTileManager.init();
	}

	public void start() {
		// Get track records
		JXElement[] trackRecords;
		try {
			p("start getting trackRecords");
			trackRecords = getTrackRecords();
			p("start got " + trackRecords.length + " trackRecords");
			ArrayList dirtyTracks = new ArrayList();
			for (int i = 0; i < trackRecords.length; i++) {
				// If track has been updated after our last draw add to dirty tracks
				long endDate = Long.parseLong(trackRecords[i].getChildText("enddate"));
				if (endDate > lastTrackTime || forceDraw) {
					dirtyTracks.add(trackRecords[i]);
					if (endDate > newTrackTime) {
						newTrackTime = endDate;
					}
				}
			}
			trackRecords = (JXElement[]) dirtyTracks.toArray(new JXElement[dirtyTracks.size()]);
			p("start got " + trackRecords.length + " dirty trackRecords");
		} catch (Exception e) {
			e("error retrieving track records ", e);
			return;
		}
		p("processing tracks");
		// for (int i = 0; i < 12; i++) {
		for (int i = 0; i < trackRecords.length; i++) {
			if (i > 21) {
				// continue;
			}
			String id = trackRecords[i].getAttr("id");
			try {
				p("creating TrackEntry for id= " + id);

				// The samples for this track
				TrackSample[] trackSamples = getTrackSamples(id);
				TrackEntry trackEntry = new TrackEntry(trackRecords[i], trackSamples);
				p("created " + trackEntry);

				// Do the drawing work for this track
				processTrack(trackEntry);
			} catch (Exception e) {
				e("exception processing track id=" + id, e);
			}
		}
	}

	public void stop() {
		// Save out tiles being worked on.
		mapTileManager.save();

		// Save very last track point date/time so we will
		// draw only new track data next time.
		long oldVal = Long.parseLong(setProperty(properties, "lastTrackTime", newTrackTime+""));
		p("set lastTrackTime old=" + new Date(oldVal) + " new=" + new Date(newTrackTime));
		saveProperties(properties, propertiesFilePath);
		p("FINISHED MapDrawer - drawing took " + (System.currentTimeMillis() - startTime) / 1000 + " seconds drawn cnt=" + processedCnt);
	}

	/** Get track list from server. */
	protected JXElement[] getTrackRecords() throws Exception {
		String queryURL = getProperty(properties, "baseURL") + "/" + getProperty(properties, "queryTracks");
		return (JXElement[]) new JXBuilder().build(new URL(queryURL)).getChildren().toArray(new JXElement[0]);
	}

	/** Get track samples for Track. */
	protected TrackSample[] getTrackSamples(String aTrackId) {
		ArrayList trackSamples = new ArrayList(128);
		try {
			// Get the track GTX file
			String gtxURL = getProperty(properties, "baseURL") + "/" + getProperty(properties, "getTrack") + aTrackId;
			JXElement gtxElement = new JXBuilder().build(new URL(gtxURL));

			// Get all points in track
			Vector segments = gtxElement.getChildByTag("trk").getChildren();
			Vector points = new Vector(64);
			for (int i = 0; i < segments.size(); i++) {
				points.addAll(((JXElement) segments.get(i)).getChildren());
			}

			// Create TrackSample from points
			TrackSample nextSample;
			JXElement nextPt;
			for (int i = 0; i < points.size(); i++) {
				nextPt = (JXElement) points.get(i);
				GPSSample sample = new GPSSample(nextPt.getDoubleAttr("lat"), nextPt.getDoubleAttr("lon"), nextPt.getLongAttr("t"));

				// p(event.getField("data"));

				nextSample = new TrackSample(sample, nextPt.getIntAttr("rr"));

				trackSamples.add(nextSample);
			}


		} catch (Throwable t) {
			e("error processing route file", t);
		}

		return (TrackSample[]) trackSamples.toArray(new TrackSample[trackSamples.size()]);

	}

	/** Do the drawing work for single Track. */
	protected void processTrack(TrackEntry aTrack) {
		try {
			p("processing " + aTrack);

			// Determine parms
			MapTile mapTile;
			TrackSample[] trackSamples = aTrack.getSamples();

			// Go through all zoom levels.
			for (int zoom = zoomMin; zoom <= zoomMax; zoom++) {
				p("doing zoom " + zoom);
				int step = 1;

				// Tricky stuff: reduce points by varying stepsize
				// by zoom (TODO: make configurable)
				if (zoom < 15) {
					step = 16 - zoom;
				}
				if (zoom < 10) {
					step = 32 - zoom;
				}

				// Go through all samples, finding tile, drawing it.
				for (int i = 0; i < trackSamples.length; i++) {
					long sampleTime = trackSamples[i].geoSample.timestamp;
					if (sampleTime < lastTrackTime && !forceDraw) {
						p("skipping" + aTrack + " " + new Date(trackSamples[i].geoSample.timestamp));
						continue;
					}

					// Get tile in which point is contained.
					mapTile = mapTileManager.getMapTile(trackSamples[i].geoSample, zoom);
					processedCnt++;

					// Draw sample on tile.
					drawSample(mapTile, trackSamples[i]);
					i = i + step;
				}
			}
			p("processed " + aTrack);
		} catch (Throwable t) {
			e("ERROR processing track: " + aTrack, t);
		}

	}

	/** Draw single track point on tile. */
	protected void drawSample(MapTile aMapTile, TrackSample aSample) throws Exception {

		try {

			int roadRating = aSample.roadRating;

			// Get x,y coordinates from lon/lat of sample.
			int plotX = aMapTile.getPlotX(aSample);
			int plotY = aMapTile.getPlotY(aSample);
			// p("x=" + plotX + " y=" + plotY);

			Graphics2D g2 = aMapTile.getGraphics();
			int size;
			int zoom = aMapTile.getZoom();

			// Determine what to draw based on zoom
			// TODO: make configurable
			if (zoom >= 9) {
				g2.setColor(borderColor);
				size = 6;
				g2.fillOval(plotX - size / 2, plotY - size / 2, size, size);

				g2.setColor(COLOR_INDEX[roadRating]);
				size = 4;
				g2.fillOval(plotX - size / 2, plotY - size / 2, size, size);
			} else {
				g2.setColor(lowZoomColor);
				size = 1;
				g2.fillOval(plotX, plotY, size, size);
			}

		} catch (Throwable t) {
			e("drawSample continueing...", t);
		}
	}

	public static void p(String s) {
		System.out.println(s);
	}

	public static void e(String s) {
		p("ERROR " + s);
	}

	public static void e(String s, Throwable t) {
		p("ERROR " + s + " t=" + t);
		t.printStackTrace();
	}

	/** Property stuff. */
	static public Color getColorProperty(JXElement properties, String aName) {
		try {
			return new Color(Integer.parseInt(getProperty(properties, aName), 16));
		} catch (Throwable t) {
			e("error parsing property: " + aName);
			return null;
		}
	}

	static public int getIntProperty(JXElement properties, String aName) {
		try {
			return Integer.parseInt(getProperty(properties, aName));
		} catch (Throwable t) {
			e("error parsing int property: " + aName);
			return -1;
		}
	}

	static public long getLongProperty(JXElement properties, String aName) {
		try {
			return Long.parseLong(getProperty(properties, aName));
		} catch (Throwable t) {
			e("error parsing long property: " + aName);
			return -1;
		}
	}

	static public String getProperty(JXElement properties, String aName) {
		if (properties == null) {
			return null;
		}

		JXElement propertyElement = properties.getChildByAttr("name", aName);
		if (propertyElement == null) {
			e("cannot find property: " + aName);
			return null;
		}
		return propertyElement.getText();
	}


	static public JXElement getProperties(JXElement properties, String aName) {
		if (properties == null) {
			return null;
		}
		JXElement example = new JXElement("properties");
		example.setAttr("name", aName);
		JXElement propertyElement = properties.getChildByExample(example);
		if (propertyElement == null) {
			e("cannot find property: " + aName);
			return null;
		}
		return propertyElement;
	}

	static public void saveProperties(JXElement properties, String aFilePath) {
	   		try {
			File file = new File(aFilePath);
			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream dos = new DataOutputStream(fos);
			dos.writeBytes(properties.toFormattedString() + "\n");
			dos.flush();
			dos.close();
		} catch (Throwable e) {
			e("Cannot save properties to " + aFilePath, e);
		}
	}

	static public String setProperty(JXElement properties, String aName, String aValue) {
		String oldValue = null;
		JXElement propertyElement = properties.getChildByAttr("name", aName);
		if (propertyElement == null) {
			propertyElement = new JXElement("propeprty");
			propertyElement.setAttr("name", aName);
			properties.addChild(propertyElement);
		} else {
			oldValue = propertyElement.getText();
		}
		propertyElement.setText(aValue);
		return oldValue;
	}

	/** Represents single track point. */
	private static class TrackSample {
		/** Dutch RD coordinates. */
		public XY xy;
		public GPSSample geoSample;
		public int roadRating;
		public int sceneryRating;

		public TrackSample(GPSSample aGeoSample, int aRoadRating, int aSceneryRating) {
			geoSample = aGeoSample;
			roadRating = aRoadRating == -1 ? 0 : aRoadRating;
			sceneryRating = aSceneryRating;

			// Hack: use RD projection for plot calc
			// TODO: use transverse Mercator calc.
			xy = WGS84toRD.calculate(geoSample.lat, geoSample.lon);
		}

		public TrackSample(GPSSample aGeoSample, int aRoadRating) {
			this(aGeoSample, aRoadRating, 0);
		}

		public JXElement toXML() {
			JXElement result = new JXElement("p");
			result.setAttr("x", xy.x);
			result.setAttr("y", xy.y);
			result.setAttr("time", geoSample.timestamp);
			result.setAttr("ar", roadRating);
			result.setAttr("sr", sceneryRating);
			result.setAttr("speed", Math.round(geoSample.speed));
			return result;
		}
	}

	/** Manages lifecycle of map tiles. */
	private class MapTileManager {
		private ArrayList mapTiles;
		private Map mapTilesTable;
		private JXElement properties;
		private String tileDir;
		private int maxInMemory;

		public MapTileManager(JXElement theProperties) {
			properties = theProperties;
			tileDir = getProperty(properties, "dir");
			maxInMemory = getIntProperty(properties, "maxInMemory");
			mapTiles = new ArrayList(maxInMemory);
			mapTilesTable = new HashMap(maxInMemory);
		}

		/** Creates standard tiles: empty, no-tile and legend. */
		public void createDefaultTiles() {
			Tile emptyTile = new Tile(tileDir + "/" + getProperty(properties, "empty"), 1, 1);
			if (!emptyTile.exists()) {
				emptyTile.create();
				emptyTile.save();
				p("mgr - created empty tile " + emptyTile.path);
			}

			Tile noTile = new Tile(tileDir + "/" + getProperty(properties, "none"), 256, 256);
			if (!noTile.exists()) {
				noTile.create();
				Graphics2D g2 = noTile.getGraphics();
				g2.setFont(new Font("Helvetica", Font.BOLD, 12));
				g2.setColor(new Color(255, 0, 255));

				g2.drawString("no routes at this zoom level", 15, 15);
				g2.drawString("please zoom in or out", 15, 30);
				noTile.save();
			}

			Tile legend = new Tile(tileDir + "/" + getProperty(properties, "legend"), 256, 116);
			legend.create();
			Graphics2D g2 = legend.getGraphics();

			g2.setFont(new Font("Helvetica", Font.BOLD, 12));

			int size, x, y, idx;

			size = 6;
			x = 10;
			y = 10;
			idx = 1;

			g2.setColor(borderColor);
			g2.fillOval(x - size / 2, y - size / 2, size, size);

			g2.setColor(COLOR_INDEX[idx]);
			size = 4;
			g2.fillOval(x - size / 2, y - size / 2, size, size);

			g2.setColor(Color.BLACK);
			g2.drawString("bad road quality", x + 10, y + 5);

			size = 6;
			x = 10;
			y = 25;
			idx = 3;

			g2.setColor(borderColor);
			g2.fillOval(x - size / 2, y - size / 2, size, size);

			g2.setColor(COLOR_INDEX[idx]);
			size = 4;
			g2.fillOval(x - size / 2, y - size / 2, size, size);

			g2.setColor(Color.BLACK);
			g2.drawString("so so road quality", x + 10, y + 5);

			size = 6;
			x = 10;
			y = 40;
			idx = 5;

			g2.setColor(borderColor);
			g2.fillOval(x - size / 2, y - size / 2, size, size);

			g2.setColor(COLOR_INDEX[idx]);
			size = 4;
			g2.fillOval(x - size / 2, y - size / 2, size, size);

			g2.setColor(Color.BLACK);
			g2.drawString("good road quality", x + 10, y + 5);
 
			size = 6;
			x = 10;
			y = 55;
			idx = 0;

			g2.setColor(borderColor);
			g2.fillOval(x - size / 2, y - size / 2, size, size);

			g2.setColor(COLOR_INDEX[idx]);
			size = 4;
			g2.fillOval(x - size / 2, y - size / 2, size, size);

			g2.setColor(Color.BLACK);
			g2.drawString("unknown road quality", x + 10, y + 5);

			size = 2;
			x = 10;
			y = 75;
			idx = 5;
			g2.setColor(lowZoomColor);
			g2.fillOval(x - size / 2, y - size / 2, size, size);
			x += 2;
			g2.fillOval(x - size / 2, y - size / 2, size, size);
			x += 2;
			g2.fillOval(x - size / 2, y - size / 2, size, size);
			x += 2;
			g2.fillOval(x - size / 2, y - size / 2, size, size);
			g2.setColor(Color.BLACK);
			g2.drawString("route (when zoomed out) or GPS-track", x + 10, y + 5);

			g2.setColor(Color.BLACK);
			size = 10;
			x = 10;
			y = 100;
			idx = 5;
			g2.fillRect(x - size / 2, y - size / 2, size, size);
			g2.setColor(Color.RED);
			size = 8;
			g2.fillRect(x - size / 2, y - size / 2, size, size);
			g2.setColor(Color.BLACK);
			g2.drawString("media locations (view with mouse over)", x + 10, y + 5);
			legend.save();
		}

		public boolean deleteTiles() {
			p("mgr - deleting ALL tiles in " + tileDir);
			try {
				IO.rmdir(tileDir);
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		public void init() {
			p("mgr - init dir=" + tileDir);
			createDefaultTiles();
		}

		public void save() {
			p("mgr - saving tiles");
			for (int i = 0; i < mapTiles.size(); i++) {
				MapTile tile = (MapTile) mapTiles.get(i);
				mapTilesTable.remove(tile.khRef);
				tile.save();
			}
		}

		public MapTile getMapTile(GeoPoint aPoint, int aZoom) {
			String khRef = GoogleTiles.getKeyholeRef(aPoint.lon, aPoint.lat, 18 - aZoom);
			MapTile mapTile = (MapTile) mapTilesTable.get(khRef);
			if (mapTile == null) {
				mapTile = addMapTile(khRef);
			}
			return mapTile;
		}

		public MapTile addMapTile(String aKHRef) {
			String tilePath = getMapTilePath(aKHRef);
			MapTile mapTile = new MapTile(aKHRef, tilePath, 256, 256);
			mapTile.init();

			// remove "oldest" tile from memory
			if (mapTiles.size() >= maxInMemory) {
				MapTile oldestTile = (MapTile) mapTiles.remove(0);
				mapTilesTable.remove(oldestTile.khRef);
				oldestTile.save();
				p("removed tile: " + oldestTile.khRef);

			}
			p("mgr - added tile: " + aKHRef);

			mapTilesTable.put(aKHRef, mapTile);
			mapTiles.add(mapTile);
			return mapTile;
		}

		public String getMapTilePath(String aKHRef) {
			return tileDir + "/" + GoogleTiles.getKeyHolePath(aKHRef) + getProperty(properties, "name");
		}
	}

	private class Tile {
		protected String path;
		protected int w, h;
		protected BufferedImage bi;
		protected Graphics2D g2;
		protected String format;

		public Tile(String aPath, int aWidth, int aHeight) {
			this(aPath, aWidth, aHeight, "png");
		}

		public Tile(String aPath, int aWidth, int aHeight, String aFormat) {
			path = aPath;
			w = aWidth;
			h = aHeight;
			format = aFormat;
		}


		public void create() {
			try {
				// Determine parms
				bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				g2 = bi.createGraphics();
			} catch (Throwable t) {
				e("ERROR creating tile", t);
			}
			p("created " + this);
		}

		public boolean exists() {
			return new File(path).exists();
		}

		public void load() {
			p("loading " + this);
			try {
				File file = new File(path);
				bi = ImageIO.read(file);
				g2 = bi.createGraphics();
			} catch (Throwable t) {
				e("ERROR loading=" + path, t);
			}
		}

		public void save() {
			try {
				File outFile = new File(path);
				IO.mkdir(outFile.getParent());
				ImageIO.write(bi, format, outFile);
				bi.flush();
				bi = null;
			} catch (Throwable t) {
				e("ERROR saving=" + path, t);
			}
			p("saved tile " + this);
		}

		public Graphics2D getGraphics() {
			return g2;
		}

		public String toString() {
			return "tile [" + path + "]";
		}
	}

	private class MapTile extends Tile {
		// 0_630000_320000_294000
		public XY xyNW, xySE;
		public Rectangle2D.Double llBox;
		private String khRef;
		private double mPerPixX, mPerPixY;

		public MapTile(String aKHRef, String aPath, int aWidth, int aHeight) {
			super(aPath, aWidth, aHeight);
			khRef = aKHRef;
			llBox = GoogleTiles.getLatLong(khRef);
			xyNW = WGS84toRD.calculate(llBox.getY() + llBox.height, llBox.getX());
			xySE = WGS84toRD.calculate(llBox.getY(), llBox.getX() + llBox.getWidth());
			mPerPixX = (double) (xySE.x - xyNW.x) / (double) w;
			mPerPixY = (double) (xyNW.y - xySE.y) / (double) h;
		}

		public void init() {
			if (!exists()) {
				create();
			} else {
				load();
			}
		}

		public int getPlotX(TrackSample aTrackSample) {
			//double m = aTrackSample.xy.x - xyNW.x;
			//return (int) (m / mPerPixX);
			return GoogleTiles.getX(aTrackSample.geoSample.lon, llBox);
		}

		public int getPlotY(TrackSample aTrackSample) {
			// Use RD projection
			// TODO use GoogleTiles util (TBS).
			double m = xyNW.y - aTrackSample.xy.y;
			return (int) (m / mPerPixY);
			// return GoogleTiles.getY(aTrackSample.geoSample.lat, llBox);
		}

		public int getZoom() {
			return GoogleTiles.getTileZoomV2(khRef);
		}

		public String toString() {
			return "name=" + khRef + " path=" + path + " xyNW=" + xyNW + " xySE=" + xySE;
		}
	}

	private class TrackEntry {
		/*
		<record id="1317">
<name>050325</name>
<description/>
<type>1</type>
<format>gtbasic</format>
<state>2</state>
<startdate>1111765204937</startdate>
<enddate>1111769458689</enddate>
<data>1317.data</data>
<ptcount>295</ptcount>
<distance>14.8</distance>
-
	<lastevt>
<pt nmea="$GPGGA,155058.689,5216.3489,N,00452.5545,E,1,05,1.3,33.2,M,47.0,M,18.4,0000*76" t="1111769458689" rr="4" sr="0" lon="4.8759083" lat="52.2724817" ele="0.0" acc="97" speed="20.83"/>
</lastevt>
<creationdate>1148643197217</creationdate>
<modificationdate>1148643263724</modificationdate>
<extra/>
</record>
		*/
		private JXElement record;
		private TrackSample[] samples;

		public TrackEntry(JXElement aRecord, TrackSample[] theSamples) {
			record = aRecord;
			samples = theSamples;
		}

		public String getId() {
			return record.getAttr("id");
		}

		public String getName() {
			return record.getChildText("name");
		}

		public long getEndDate() {
			return Long.parseLong(record.getChildText("enddate"));
		}

		public TrackSample[] getSamples() {
			return samples;
		}

		public String toString() {
			return "Track[" + getId() + "] name=" + getName() + " cnt=" + samples.length;
		}
	}
}
