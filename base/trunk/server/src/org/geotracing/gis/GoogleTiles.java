// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.gis;

/*
 * Originally written by Andrew Rowbottom.
 * Released freely into the public domain, use it how you want, don't blame me.
 * No warranty for this code is taken in any way.
 * original : http://www.ponies.me.uk/maps/GoogleTileUtils.java
 *
 * See also: http://www.cse.buffalo.edu/~ajay/googlemaps.html (for map ref)
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.text.DecimalFormat;

/**
 * A utility class to assist in encoding and decoding google tile references
 * <p/>
 * For reasons of my own longitude is treated as being between -180 and +180
 * and internally latitude is treated as being from -1 to +1 and then converted to a mercator projection
 * before return.
 * <p/>
 * All rectangles are sorted so the width and height are +ve
 */
public class GoogleTiles {
	public static final double TILE_SIZE = 256.0D;

	/**
	 * hidden constructor, this is a Utils class
	 */
	private GoogleTiles() {
		super();
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

		Rectangle2D r = getLatLong(keyholeString);
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

		Rectangle2D r = getLatLong(x, y, zoom);
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
	 * returns a Rectangle2D with x = lon, y = lat, width=lonSpan, height=latSpan for a keyhole string.
	 */
	public static Rectangle2D.Double getLatLong(String keyholeStr) {
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
	public static Rectangle2D.Double getLatLong(int x, int y, int zoom) {
		double lon = -180; // x
		double lonWidth = 360; // width 360

		//double lat = -90;  // y
		//double latHeight = 180; // height 180
		double lat = -1;
		double latHeight = 2;

		int tilesAtThisZoom = 1 << (17 - zoom);
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
	 * Returns x,y of tile emclosing lon, lat.
	 */
	public static XY getTileXY(double lon, double lat, int zoom) {
		return getTileXY(getKeyholeRef(lon, lat, zoom));
	}

	/**
	 * Returns x,y of tile for Keyhole string (e.g. "trtqstr" etc).
	 * See:
	 * http://dunck.us/collab/Simple_20Analysis_20of_20Google_20Map_20and_20Satellite_20Tiles
	 */
	public static XY getTileXY(String keyholeStr) {
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

		XY xy = new XY();
		xy.x = x;
		xy.y = y;
		return xy;

	}

	public static class XY {
		public int x;
		public int y;
	}

	/**
	 * returns a keyhole string for a longitude (x), latitude (y), and zoom
	 */
	public static String getKeyholeRef(double lon, double lat, int zoom) {
		zoom = 18 - zoom;

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

	/**
	 * returns the Google zoom level for the keyhole string.
	 */
	public static int getTileZoom(String keyHoleString) {
		return 18 - keyHoleString.length();
	}

	/**
	 * returns the Google zoom level for the keyhole string GMap v2.
	 */
	public static int getTileZoomV2(String keyHoleString) {
		return keyHoleString.length() - 1;
	}

	/**
	 * Returns xoffset for lon in tile.
	 */
	public static int getX(double lon, Rectangle2D.Double llBox) {
		return (int) ((Math.abs(llBox.x - lon)) * (256.0D / llBox.width));
	}

	/**
	 * Returns yoffset for lat in tile.
	 */
	public static int getY(double lat, Rectangle2D.Double llBox) {
		return 256 - (int) ((Math.abs(llBox.y - lat)) * (256.0D / llBox.height));
	}

	/**
	 * Returns pixel offset in tile for given lon,lat,zoom.
	 * see http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	*/
	public static XY getPixelXY(double lon, double lat, int zoom) {
		// Number of tiles in each axis at zoom level
		double tiles = Math.pow(2, 17 - zoom);

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
		System.out.println("x1=" + x);

		// Correct for false easting
		x = (int) falseEasting + x;
		System.out.println("x2=" + x);

		int tilesXOffset = x / (int) TILE_SIZE;
		x = x - (int) (tilesXOffset * TILE_SIZE);
		System.out.println("x3=" + x + " tilesXOffset =" + tilesXOffset);

		// Do y
		int y = (int) (radius / 2.0 * Math.log((1.0 + Math.sin(latitude)) / (1.0 - Math.sin(latitude))));
		System.out.println("y1=" + y);

		// Correct for false northing
		y = (y - (int) falseNorthing) * -1;
		System.out.println("y2=" + y);

		// Number of pixels to subtract for tiles skipped (offset)
		int tilesYOffset = y / (int) TILE_SIZE;
		y = y - (int) (tilesYOffset * TILE_SIZE);
		System.out.println("y3=" + y + " tilesYOffset =" + tilesYOffset);

		XY xy = new XY();
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

		Rectangle2D.Double llBox = getLatLong(tileRef);
		System.out.println(llBox);

		System.out.println("x=" + getX(lon, llBox) + " y=" + getY(lat, llBox));

	}
}
