// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.gis;

/*
 * Original code from multiple sources:
 * by Andrew Rowbottom.
 * Released freely into the public domain, use it how you want, don't blame me.
 * No warranty for this code is taken in any way.
 * original : http://www.ponies.me.uk/maps/GoogleTileUtils.java
 *
 * Sother sources:
 * http://www.cse.buffalo.edu/~ajay/googlemaps.html (for map ref)
 * http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.text.DecimalFormat;

/**
 * A utility class to assist in working with google tiles.
 * <p/>
 * For reasons of my own longitude is treated as being between -180 and +180
 * and internally latitude is treated as being from -1 to +1 and then converted to a mercator projection
 * before return.
 * <p/>
 * All rectangles are sorted so the width and height are +ve
 */
public class GoogleTiles {
	public static final double TILE_SIZE = 256.0D;

	public static class LonLat {
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

	/* Convenience struct x,y pixel coords. */
	public static class XY {
		public int x;
		public int y;
	}

	/**
	 * hidden constructor, this is a Utils class
	 */
	private GoogleTiles() {
		super();
	}

	/**
	 * Returns keyhole string for adjacent tile in gives direction.
	 *
	 * @param aKHRef the keyhole string to return adjacent keyhole string for.
	 * @param aDirection adjacency direction, one of : "n", "s", "e", "w", "nw", "ne", "sw", "se".
	 * @return a keyhole ref of adjacent tile
	 */
	public static String getAdjacentKeyholeRef(String aKHRef, String aDirection) {
		int length = aKHRef.length();

		// Get keyhole chars in array for manipulation
		char[] khChars = new char[length];
		aKHRef.getChars(0, length, khChars, 0);

		// First get north or south (first char)
		char dir = aDirection.charAt(0);
		if (dir == 'n' || dir == 's') {
			khChars = getAdjacentKeyholeRefNS(khChars, dir, length-1);
		} else {
			khChars = getAdjacentKeyholeRefEW(khChars, dir , length-1);
		}

		// If two chars the second is an additional "e" or "w"
		khChars = aDirection.length() == 2 ? getAdjacentKeyholeRefEW(khChars, aDirection.charAt(1),length-1) : khChars;
		return new String(khChars);
	}

	/**
	 * Returns keyhole string for adjacent tile in E or W.
	 *
	 * @param khChars the keyhole string to return adjacent keyhole string for.
	 * @param aEW adjacency direction, one of : "e", "w".
	 * @param index index into khchars array.
	 * @return a keyhole ref of adjacent tile
	 */
	public static char[] getAdjacentKeyholeRefEW(char[] khChars, char aEW, int index) {
		char c = khChars[index];

		// Get neighbour tile
		switch (c) {
			case 'q':
				khChars[index] = 'r';
				break;
			case 'r':
				khChars[index] = 'q';
				break;
			case 's':
				khChars[index] = 't';
				break;
			case 't':
				khChars[index] = 's';
				break;
		}

		// Possible recursion into higher zoom if going into tile outside current 4 qrst
		if (aEW == 'e' && (c == 'r' || c == 's')) {
			return getAdjacentKeyholeRefEW(khChars, aEW, index-1);
		}

		if (aEW == 'w' && (c == 't' || c == 'q')) {
			return getAdjacentKeyholeRefEW(khChars, aEW, index-1);
		}

		return khChars;

	}

	/**
	 * Returns keyhole string for adjacent tile in N or S.
	 *
	 * @param khChars the keyhole string to return adjacent keyhole string for.
	 * @param aNS adjacency direction, one of : "n", "s".
	 * @param index index into khchars array.
	 * @return a keyhole ref of adjacent tile
	 */
	public static char[] getAdjacentKeyholeRefNS(char[] khChars, char aNS, int index) {
		char c = khChars[index];
		switch (c) {
			case 'q':
				khChars[index] = 't';
				break;
			case 'r':
				khChars[index] = 's';
				break;
			case 's':
				khChars[index] = 'r';
				break;
			case 't':
				khChars[index] = 'q';
				break;
		}

		// Possible recursion into higher zoom if going into tile outside current 4 qrst
		if (aNS == 'n' && (c == 'q' || c == 'r')) {
			return getAdjacentKeyholeRefNS(khChars, aNS, index-1);
		}

		if (aNS == 's' && (c == 't' || c == 's')) {
			return getAdjacentKeyholeRefNS(khChars, aNS, index-1);
		}

		return khChars;
	}

	/**
	 * Returns a buffered image with the corner lat/lon,keyhole id and zoom level written on it.
	 *
	 * @param keyholeString the keyhole string to return the image for.
	 * @return BufferedImage
	 */
	public static BufferedImage getDebugTileFor(String keyholeString) {
		BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		g.setColor(Color.gray);
		g.fillRect(0, 0, 256, 256);
		g.setColor(Color.black);

		int scale = 400 / keyholeString.length();
		g.setFont(new Font("Serif", Font.BOLD, scale));
		g.drawString(keyholeString + " (z=" + getTileZoom(keyholeString) + ")", 10, 200);

		Rectangle2D r = getBoundingBox(keyholeString);
		DecimalFormat df = new DecimalFormat("#.####");
		g.setFont(new Font("SanSerif", 0, 15));

		g.drawString(df.format(r.getMinY()) + "," + df.format(r.getMinX()) + " (w:" + df.format(r.getWidth()) + " h:" + df.format(r.getHeight()) + ")", 10, 250);
		g.drawString(df.format(r.getMaxY()) + "," + df.format(r.getMaxX()), 150, 20);
		g.drawRect(1, 1, 255, 255);
		g.dispose();

		return img;
	}

	/**
	 * Returns a buffered image with the corner lat/lon,x,y and zoom level written on it.
	 */
	public static BufferedImage getDebugTileFor(int x, int y, int zoom) {
		BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		g.setColor(Color.gray);
		g.fillRect(0, 0, 256, 256);
		g.setColor(Color.black);

		int scale = 20;
		g.setFont(new Font("Serif", Font.BOLD, scale));
		g.drawString("x:" + x + " y:" + y + " z:" + zoom, 10, 200);

		Rectangle2D r = getBoundingBox(x, y, zoom);
		DecimalFormat df = new DecimalFormat("#.####");
		g.setFont(new Font("SanSerif", 0, 15));

		g.drawString(df.format(r.getMinY()) + "," + df.format(r.getMinX()) + " (w:" + df.format(r.getWidth()) + " h:" + df.format(r.getHeight()) + ")", 10, 250);
		g.drawString(df.format(r.getMaxY()) + "," + df.format(r.getMaxX()), 150, 20);
		g.drawRect(1, 1, 255, 255);
		g.dispose();

		return img;
	}

	/**
	 * Converts a Keyhole string to (relative) directory path.
	 */
	public static String getKeyHolePath(String aKeyholeString) {
		char[] khChars = new char[aKeyholeString.length()];

		aKeyholeString.getChars(0, aKeyholeString.length(), khChars, 0);
		StringBuffer khPath = new StringBuffer(khChars.length * 2);
		for (int i = 0; i < khChars.length; i++) {
			khPath.append(khChars[i]);
			khPath.append('/');
		}
		return khPath.toString();
	}

	/**
	 * Get Rectangle2D with x = lon, y = lat, width=lonSpan, height=latSpan for a keyhole string.
	 */
	public static Rectangle2D.Double getBoundingBox(String keyholeStr) {
		// must start with "t"
		if ((keyholeStr == null) || (keyholeStr.length() == 0) || (keyholeStr.charAt(0) != 't')) {
			throw new RuntimeException("Keyhole string must start with 't'");
		}

		double lon = -180; // x
		double lonWidth = 360; // width 360

		//double lat = -90;  // y
		//double latHeight = 180; // height 180
		double lat = -1;
		double latHeight = 2;

		for (int i = 1; i < keyholeStr.length(); i++) {
			lonWidth /= 2;
			latHeight /= 2;

			char c = keyholeStr.charAt(i);

			switch (c) {
				case 's':

					// lat += latHeight;
					lon += lonWidth;

					break;

				case 'r':
					lat += latHeight;
					lon += lonWidth;

					break;

				case 'q':
					lat += latHeight;

					// lon += lonWidth;
					break;

				case 't':

					//lat += latHeight;
					//lon += lonWidth;
					break;

				default:
					throw new RuntimeException("unknown char '" + c + "' when decoding keyhole string.");
			}
		}

		// convert lat and latHeight to degrees in a transverse mercator projection
		// note that in fact the coordinates go from about -85 to +85 not -90 to 90!
		latHeight += lat;
		latHeight = (2 * Math.atan(Math.exp(Math.PI * latHeight))) - (Math.PI / 2);
		latHeight *= (180 / Math.PI);

		lat = (2 * Math.atan(Math.exp(Math.PI * lat))) - (Math.PI / 2);
		lat *= (180 / Math.PI);

		latHeight -= lat;

		if (lonWidth < 0) {
			lon = lon + lonWidth;
			lonWidth = -lonWidth;
		}

		if (latHeight < 0) {
			lat = lat + latHeight;
			latHeight = -latHeight;
		}

		//		lat = Math.asin(lat) * 180 / Math.PI;
		return new Rectangle2D.Double(lon, lat, lonWidth, latHeight);
	}

	/**
	 * returns a Rectangle2D with x = lon, y = lat, width=lonSpan, height=latSpan
	 * for an x,y,zoom as used by google.
	 */
	public static Rectangle2D.Double getBoundingBox(int x, int y, int zoom) {
		double lon = -180; // x
		double lonWidth = 360; // width 360

		//double lat = -90;  // y
		//double latHeight = 180; // height 180
		double lat = -1;
		double latHeight = 2;

		// int tilesAtThisZoom = 1 << (17 - zoom);
		int tilesAtThisZoom = 1 << zoom;
		lonWidth = 360.0 / tilesAtThisZoom;
		lon = -180 + (x * lonWidth);
		latHeight = -2.0 / tilesAtThisZoom;
		lat = 1 + (y * latHeight);

		// convert lat and latHeight to degrees in a transverse mercator projection
		// note that in fact the coordinates go from about -85 to +85 not -90 to 90!
		latHeight += lat;
		latHeight = (2 * Math.atan(Math.exp(Math.PI * latHeight))) - (Math.PI / 2);
		latHeight *= (180 / Math.PI);

		lat = (2 * Math.atan(Math.exp(Math.PI * lat))) - (Math.PI / 2);
		lat *= (180 / Math.PI);

		latHeight -= lat;

		if (lonWidth < 0) {
			lon = lon + lonWidth;
			lonWidth = -lonWidth;
		}

		if (latHeight < 0) {
			lat = lat + latHeight;
			latHeight = -latHeight;
		}

		return new Rectangle2D.Double(lon, lat, lonWidth, latHeight);
	}

	public static int getTileServerNo(int x, int y) {
		return (x + y) % 4;
	}

	/**
	 * Returns x,y of tile enclosing lon, lat.
	 */
	public static XY getTileXY(double lon, double lat, int zoom) {
		return getTileXY(getKeyholeRef(lon, lat, zoom));
	}

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
	 * Returns keyhole string for tile x,y,zoom.
	 */
	public static String getKeyholeRef(int x, int y, int zoom) {
		// TODO: optimize, now just getting bbox and center to obtain khref
		Rectangle2D.Double bbox = getBoundingBox(x, y, zoom);
		return getKeyholeRef(bbox.getCenterX(), bbox.getCenterY(), zoom);
	}

	/**
	 * Get keyhole string for a longitude (x), latitude (y), and zoom
	 */
	public static String getKeyholeRef(double lon, double lat, int zoom) {
		// zoom = 18 - zoom; obsolete for GMAP v2

		// first convert the lat lon to transverse mercator coordintes.
		if (lon > 180) {
			lon -= 360;
		}

		lon /= 180;

		// convert latitude to a range -1..+1
		lat = Math.log(Math.tan((Math.PI / 4) + ((0.5 * Math.PI * lat) / 180))) / Math.PI;
		// System.out.println("lat=" + lat);
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

	/**
	 * returns the Google zoom level for the keyhole string GMap v2.
	 */
	public static int getTileZoom(String keyHoleString) {
		return keyHoleString.length() - 1;
	}

	/**
	 * Returns yoffset for lat in tile.
	 * <p/>
	 * public static int getY(double lat, Rectangle2D.Double llBox) {
	 * return 256 - (int) ((Math.abs(llBox.y - lat)) * (256.0D / llBox.height));
	 * }
	 */
	// http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	public static int getPixelX(double lon, int zoom) {
		double tiles = Math.pow(2, zoom);
		double circumference = TILE_SIZE * tiles;
		double falseEasting = circumference / 2.0D;
		double radius = circumference / (2 * Math.PI);
		double longitude = Math.toRadians(lon);
		int x = (int) (radius * longitude);
		//System.out.println("x1=" + x) ;
		x = (int) falseEasting + x;
		System.out.println("x2=" + x);

		int tilesOffset = x / (int) TILE_SIZE;
		x = x - (int) (tilesOffset * TILE_SIZE);
		//System.out.println("x3=" + x +" tilesOffset =" + tilesOffset);

		return x;
	}

	// http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	public static int getPixelY(double lat, int zoom) {
		double tiles = Math.pow(2, zoom);
		double circumference = TILE_SIZE * tiles;
		double falseNorthing = circumference / 2.0D;
		double radius = circumference / (2 * Math.PI);
		double latitude = Math.toRadians(lat);
		//System.out.println("tiles=" + tiles + " circumference=" + circumference + " falseNorthing="  + falseNorthing);

		int y = (int) (radius / 2.0 * Math.log((1.0 + Math.sin(latitude)) / (1.0 - Math.sin(latitude))));
		//System.out.println("y1=" + y) ;

		y = (y - (int) falseNorthing) * -1;
		//System.out.println("y2=" + y) ;

		// Number of pixels to subtract for tiles skipped (offset)
		int tilesOffset = y / (int) TILE_SIZE;
		y = y - (int) (tilesOffset * TILE_SIZE);
		//System.out.println("y3=" + y +" tilesOffset =" + tilesOffset);
		return y;
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
		double sinLat = Math.sin(latitude);
		double onePlusSinLat = 1.0D + sinLat;
		double oneMinSinLat = 1.0D - sinLat;
		double log = Math.log( onePlusSinLat / oneMinSinLat);
		//System.out.println(" radius=" + radius + " sinLat=" + sinLat + " onePlusSinLat=" + onePlusSinLat + " oneMinSinLat=" + oneMinSinLat + " log=" + log);
		int y = (int) (radius / 2.0 * log );
		//System.out.println("y1=" + y);

		// Correct for false northing
		y = (y - (int) falseNorthing) * -1;
		//System.out.println("y2=" + y);

		// Number of pixels to subtract for tiles skipped (offset)
		int tilesYOffset = y / (int) TILE_SIZE;
		y = y - (int) (tilesYOffset * TILE_SIZE);
		//System.out.println("y3=" + y + " tilesYOffset =" + tilesYOffset);

		GoogleTiles.XY xy = new GoogleTiles.XY();
		xy.x = x;
		xy.y = y;
		return xy;
	}


	/**
	 * Tests
	 */
	public static void main(String[] args) {
//      System.out.println(getLatLong(0, 0, 15));
//      System.out.println(getLatLong(1, 1, 15));
//      System.out.println(getLatLong(2, 2, 15));
//      System.out.println(getLatLong(3, 3, 15));

		double lon = Double.parseDouble(args[0]);
		double lat = Double.parseDouble(args[1]);
		int zoom = Integer.parseInt(args[2]);
		String tileRef = getKeyholeRef(lon, lat, zoom);
		System.out.println(tileRef);

		Rectangle2D.Double llBox = getBoundingBox(tileRef);
		System.out.println(llBox);

		// System.out.println("x=" + getX(lon, llBox) + " y=" + getY(lat, llBox));

	}
}
