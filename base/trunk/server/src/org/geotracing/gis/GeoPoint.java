// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.gis;

import java.util.Date;

/**
 * Point on the world in lon,lat.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class GeoPoint {

	/**
	 * Longitude ("lengtegraad") coordinate.
	 * <p/>
	 * Values:  -180..180. Values in fractional degrees. Negative values denote
	 * W, western hemisphere, positive is E.
	 */
	public double lon;

	/**
	 * Latitude ("breedtegraad") coordinate.
	 * <p/>
	 * Values: -90..90. Values in fractional degrees. Negative values denote
	 * S, southern hemisphere, positive values N(orthern).
	 */
	public double lat;


	/**
	 * Elevation coordinate in meters. 0 means no value
	 */
	public double elevation;

	/**
	 * Time in ms after 1970 (UNIX-time).
	 */
	public long timestamp;

	public GeoPoint() {

	}

	public GeoPoint(String aLonStr, String aLatStr) {
		this(Double.parseDouble(aLonStr), Double.parseDouble(aLatStr));
	}

	public GeoPoint(double aLon, double aLat) {
		this(aLon, aLat, 0D);
	}

	public GeoPoint(double aLon, double aLat, double anElevation) {
		this(aLon, aLat, anElevation, System.currentTimeMillis());
	}

	public GeoPoint(double aLon, double aLat, double anElevation, long aTime) {
		lon = aLon;
		lat = aLat;
		elevation = anElevation;
		timestamp = aTime;
	}


	public double distance(GeoPoint aPoint) {
		return GISCalc.distance(this, aPoint);
	}

	public double speed(GeoPoint aPoint) {
		return GISCalc.speed(this, aPoint);
	}

	public XYDouble metersPerDegree() {
		return GISCalc.metersPerDegree(this);
	}

	public String toString() {
		return "lon=" + lon + " lat=" + lat + " ele=" + elevation + "t=" + new Date(timestamp);
	}
}