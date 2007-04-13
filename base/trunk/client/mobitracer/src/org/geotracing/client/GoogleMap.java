// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.client;

import nl.justobjects.mjox.JXBuilder;
import nl.justobjects.mjox.JXElement;

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.midlet.MIDlet;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * Google Maps utilities.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class GoogleMap {
	/**
	 * Constants for Google Map tile calcs
	 */
	public static MFloat F_GMAP_TILE_SIZE = new MFloat(256L);
	public static int I_GMAP_TILE_SIZE = 256;
	final static public MFloat MINUS_ONE = new MFloat(-1L);
	final static public MFloat ONE = MFloat.ONE;
	final static public MFloat TWO = new MFloat(2L);
	final static public MFloat FOUR = new MFloat(4L);
	final static public MFloat TWO_PI = TWO.Mul(MFloat.PI);
	final static public MFloat DEG_180 = new MFloat(180L);
	final static public MFloat DEG_360 = new MFloat(360L);
	final static public MFloat HALF = new MFloat(2L, -1L);

	public static class XY {
		public int x;
		public int y;
	}

	/**
	 * Returns pixel offset in tile for given lon,lat,zoom.
	 * see http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	 */
	public static GoogleMap.XY getPixelXY(String aLon, String aLat, int aZoom) {
		MFloat lon = MFloat.parse(aLon, 10);
		MFloat lat = MFloat.parse(aLat, 10);
		return getPixelXY(lon, lat, aZoom);
	}

	/**
	 * Returns pixel offset in GMap tile for given lon,lat,zoom.
	 * see http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	 */
	public static GoogleMap.XY getPixelXY(MFloat aLon, MFloat aLat, int aZoom) {

		// Number of tiles in each axis at zoom level
		MFloat tiles = MFloat.pow(TWO, new MFloat(aZoom));

		// Circumference in pixels
		MFloat circumference = F_GMAP_TILE_SIZE.Mul(tiles);
		MFloat radius = circumference.Div(TWO_PI);

		// Use radians
		MFloat longitude = MFloat.toRadians(aLon);
		MFloat latitude = MFloat.toRadians(aLat);

		// To correct for origin in top left but calculated values from center
		MFloat falseEasting = circumference.Div(TWO);
		MFloat falseNorthing = falseEasting; // circumference / 2.0D;

		// Do x
		MFloat xf = radius.Mul(longitude);
		// System.out.println("x1=" + x);

		// Correct for false easting
		xf = falseEasting.Add(xf);
		// System.out.println("x2=" + x);

		int x = (int) xf.toLong();
		int tilesXOffset = x / I_GMAP_TILE_SIZE;
		x = x - tilesXOffset * I_GMAP_TILE_SIZE;
		// System.out.println("x3=" + x + " tilesXOffset =" + tilesXOffset);

		// Do yf
		MFloat sinLat = MFloat.sin(latitude);
		//MFloat onePlusSinLat = ONE.Add(sinLat);
		//MFloat oneMinSinLat = ONE.Sub(sinLat);
		// MFloat log = MFloat.log((ONE.Add(sinLat).Div(ONE.Sub(sinLat))));
		// System.out.println(" radius=" + radius + " sinLat=" + sinLat + " onePlusSinLat=" + onePlusSinLat + " oneMinSinLat=" + oneMinSinLat + " log=" + log);

		// radius / 2.0 * Math.log( (1.0 + Math.sin(latitude)) / (1.0 - Math.sin(latitude)));
		MFloat yf = (radius.Div(TWO)).Mul(MFloat.log((ONE.Add(sinLat).Div(ONE.Sub(sinLat)))));
		// System.out.println("y1=" + yf);

		// Correct for false northing
		yf = (yf.Sub(falseNorthing)).Mul(MINUS_ONE);
		// System.out.println("y2=" + yf);

		// Number of pixels to subtract for tiles skipped (offset)
		int y = (int) yf.toLong();
		int tilesYOffset = y / I_GMAP_TILE_SIZE;
		y = y - tilesYOffset * I_GMAP_TILE_SIZE;
		// System.out.println("y3=" + yf + " tilesYOffset =" + tilesYOffset);

		// Construct result
		GoogleMap.XY xy = new GoogleMap.XY();
		xy.x = x;
		xy.y = y;
		return xy;
	}

	/**
	 * Returns pixel offset in tile for given lon,lat,zoom.
	 * see http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	 */
	public static String getKeyholeRef(String aLon, String aLat, int aZoom) {
		MFloat lon = MFloat.parse(aLon, 10);
		MFloat lat = MFloat.parse(aLat, 10);
		return getKeyholeRef(lon, lat, aZoom);
	}

	/**
	 * Get keyhole string for a longitude (x), latitude (y), and zoom
	 */
	public static String getKeyholeRef(MFloat aLon, MFloat aLat, int aZoom) {
		// zoom = 18 - zoom; obsolete for GMAP v2

		// first convert the lat lon to transverse mercator coordintes.
		//	if (lon > 180) lon -= 360;
		if (aLon.Great(DEG_180)) {
			aLon = aLon.Sub(DEG_360);
		}

		// 	lon /= 180;
		aLon = aLon.Div(DEG_180);

		// convert latitude to a range -1..+1
		// lat = Math.log(Math.tan((Math.PI / 4) + ((0.5 * Math.PI * lat) / 180))) / Math.PI;
		aLat = MFloat.log(MFloat.tan((MFloat.PI.Div(FOUR)).Add((HALF.Mul(MFloat.PI.Mul(aLat).Div(DEG_180))).Div(MFloat.PI))));

		MFloat tLat = MINUS_ONE;
		MFloat tLon = MINUS_ONE;
		MFloat lonWidth = TWO;
		MFloat latHeight = TWO;

		String keyholeString = "t";

		for (int i = 0; i < aZoom; i++) {
			// lonWidth /= 2;
			lonWidth = lonWidth.Div(TWO);
			// latHeight /= 2;
			latHeight = latHeight.Div(TWO);

			/* if ((tLat + latHeight) > lat) {
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
			}   */

			if (tLat.Add(latHeight).Great(aLat)) {
				if (tLon.Add(lonWidth).Great(aLon)) {
					keyholeString += "t";
				} else {
					tLon = tLon.Add(lonWidth);
					keyholeString += "s";
				}
			} else {
				tLat = tLat.Add(latHeight);

				if (tLon.Add(lonWidth).Great(aLon)) {
					keyholeString += "q";
				} else {
					tLon = tLon.Add(lonWidth);
					keyholeString += "r";
				}
			}

		}

		return keyholeString;
	}
}
