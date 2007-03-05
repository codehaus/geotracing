// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.gis;

/**
 * Rectangle of two lon,lat coordinates in resp SW and NE.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class GeoBox {

	/**
	 * Box coordinates.
	 * <p/>
	 * Values:  world GeoBox is e.g. { (-180,-90) (180,90) }.
	 * Values in fractional degrees. Negative values denote
	 * W, western hemisphere, positive is E.
	 */
	public double lon1, lat1, lon2, lat2;

	public GeoBox() {
	}

	public GeoBox(GeoPoint p1, GeoPoint p2) {
		this(p1.lon, p1.lat, p2.lon, p2.lat);
	}

	public GeoBox(double aLon1, double aLat1, double aLon2, double aLat2) {

		// May swap coordinates to form SW to NE box
		lon1 = aLon1 < aLon2 ? aLon1 : aLon2;
		lat1 = aLat1 < aLat2 ? aLat1 : aLat2;
		lon2 = aLon1 < aLon2 ? aLon2 : aLon1;
		lat2 = aLat1 < aLat2 ? aLat2 : aLat1;
	}

	/**
	 * Is point inside the box ?.
	 */
	public boolean isInside(GeoPoint p) {
		return p.lon >= lon1 && p.lon <= lon2 && p.lat >= lat1 && p.lat <= lat2;
	}

	/**
	 * Expand box if point outside.
	 */
	public boolean expand(GeoPoint p) {
		// No expand if point inside box
		if (isInside(p)) {
			return false;
		}

		// Need to grow the box

		// West expansion ?
		lon1 = p.lon < lon1 ? p.lon : lon1;

		// East expansion ?
		lon2 = p.lon > lon2 ? p.lon : lon2;

		// South expansion ?
		lat1 = p.lat < lat1 ? p.lat : lat1;

		// North expansion ?
		lat2 = p.lat > lat2 ? p.lat : lat2;

		return true;
	}

	public String toString() {
		return "{(" + lon1 + "," + lat1 + ") (" + lon2 + "," + lat2 + ")}";
	}
}
