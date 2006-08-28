// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.gis;

import nl.justobjects.jox.parser.JXBuilder;
import nl.justobjects.jox.dom.JXElement;

import java.net.URL;
import java.util.Vector;

/**
 * Utils for GIS calculations.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class GISCalc {

	// 1 knots = 1.85200 km/h
	public static final double KM_PER_KNOT = 1.85200;
	private static double MILLIS_TO_HOURS = ((1d / 1000d) / 3600d);

	/*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
/*::                                                                         :*/
/*::  This routine calculates the distance between two points (given the     :*/
/*::  lat/lon of those points). It is being used to calculate     :*/
/*::  the distance between two ZIP Codes or Postal Codes using our           :*/
/*::  ZIPCodeWorld(TM) and PostalCodeWorld(TM) products.                     :*/
/*::                                                                         :*/
/*::  Definitions:                                                           :*/
/*::    South latitudes are negative, east longitudes are positive           :*/
/*::                                                                         :*/
/*::  Passed to function:                                                    :*/
/*::    lat1, lon1 = Latitude and Longitude of point 1 (in decimal degrees)  :*/
/*::    lat2, lon2 = Latitude and Longitude of point 2 (in decimal degrees)  :*/
/*::    unit = the unit you desire for results                               :*/
/*::           where: 'M' is statute miles                                   :*/
/*::                  'K' is kilometers (default)                            :*/
/*::                  'N' is nautical miles                                  :*/
/*::  United States ZIP Code/ Canadian Postal Code databases with lat & :*/
/*::  lon are available at http://www.zipcodeworld.com                 :*/
/*::                                                                         :*/
/*::  For enquiries, please contact sales@zipcodeworld.com                   :*/
/*::                                                                         :*/
/*::  Official Web site: http://www.zipcodeworld.com                         :*/
/*::                                                                         :*/
/*::  Hexa Software Development Center © All Rights Reserved 2004            :*/
/*::                                                                         :*/
/*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*
	The formula is:

	Distance = acos(sin(lat1)*sin(lat2)+cos(lat1)*cos(lat2)*cos(lon1-lon2))

	 where  lat1,lon1 = latitude and longitude of waypoint1 (in radians)

	 and     lat2,lon2 = latitude and longitude of waypoint2 (in radians)


	Distance=ACOS(SIN(lat1/180*PI)*SIN(lat2/180*PI)+ COS(lat1/180*PI)*COS(lat2/180*PI)*COS(lon1/180*PI-lon2/180*PI))*180*60/PI)

	 where PI = 3.141592654.......*/
	public static double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
		if (lat1 - lat2 == 0.0d && lon1 - lon2 == 0.0d) {
			return 0.0d;
		}
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == 'K') {
			dist = dist * 1.609344;
		} else if (unit == 'N') {
			dist = dist * 0.8684;
		}
		return (dist);
	}

	// CONSTANTS USED INTERNALLY
	static final double DEGREES_TO_RADIANS = (Math.PI / 180.0);

// Mean radius in KM
	static final double EARTH_RADIUS = 6371.0;

	/**
	 * Method to compute Great Circle distance between
	 * two points. Please note that this algorithm
	 * assumes the Earth to be a perfect sphere, whereas
	 * in fact the equatorial radius is about 30Km
	 * greater than the Polar.
	 *
	 * @param alt other point to compute distance to
	 * @return The distance in Kilometres
	 */

	public static double distance2(double lat1, double lon1, double lat2, double lon2) {

// There is no real reason to break this lot into
// 4 statements but I just feel it's a little more
// readable.
		double p1 = Math.cos(lat1) * Math.cos(lon1)
				* Math.cos(lat2) * Math.cos(lon2);
		double p2 = Math.cos(lat1) * Math.sin(lon1)
				* Math.cos(lat2) * Math.sin(lon2);
		double p3 = Math.sin(lat1) * Math.sin(lat2);

		return (Math.acos(p1 + p2 + p3) * EARTH_RADIUS);
	}

	/**
	 * **********************************************************************
	 * http://www.cs.princeton.edu/introcs/12types/GreatCircle.java
	 * Compilation:  javac GreatCircle.java
	 * Execution:    java GreatCircle L1 G1 L2 G2
	 * <p/>
	 * Given the latitude and longitude (in degrees) of two points compute
	 * the great circle distance between them. The following formula assumes
	 * that sin, cos, and arcos are comptued in degrees, so need to convert
	 * back and forth between radians.
	 * <p/>
	 * d  = 69.1105 * acos ( sin(L1) * sin(L2) +
	 * cos(L1) * cos(L2) * cos(G1 - G2))
	 * <p/>
	 * <p/>
	 * % java GreatCircle 59.9 -30.3 37.8 122.4        // Leningrad to SF
	 * 5510.836338644345 miles
	 * <p/>
	 * % java GreatCircle 48.87 -2.33 30.27 97.74      // Paris to Austin
	 * 5094.757824562662 miles
	 * <p/>
	 * <p/>
	 * ***********************************************************************
	 */

	static public double distance3(double lat1, double lon1, double lat2, double lon2) {

		// convert to radians
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		lon1 = Math.toRadians(lon1);
		lon2 = Math.toRadians(lon2);

		// do the spherical trig calculation
		double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2) +
				Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

		// convert back to degrees
		angle = Math.toDegrees(angle);

		// each degree on a great circle of Earth is 69.1105 miles
		return 69.1105 * angle;

	}

	/**
	 * Great Circle distance of the Earth in Meters.  Used in the distance formula.
	 */
	public static final int METERS = 6367000;
	/**
	 * Great Circle distance of the Earth in Kilometers.  Used in the distance formula.
	 */
	public static final int KILOMETERS = 6367;
	/**
	 * Great Circle distance of the Earth in Miles.  Used in the distance formula.
	 */
	public static final int MILES = 3956;

	/**
	 * Calculates the distance between two points using the haversine formula.
	 * The haversine formula requires the great circle distance in some measure.
	 * This returns km.
	 *
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @return
	 */
	public static double getDistance(double x1, double x2, double y1, double y2) {
		return getDistance(x1, x2, y1, y2, KILOMETERS);
	}

	/**
	 * Calculates the distance between two points using the haversine formula.
	 * The haversine formula requires the great circle distance in some measure.
	 * This is the distance of the great circle in meters, km, or miles
	 *
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param measure This is the distance of the great circle in meters, km, or miles
	 * @return
	 */
	public static double getDistance(double x1, double x2, double y1, double y2, int measure) {
		x1 = x1 * (Math.PI / 180);
		x2 = x2 * (Math.PI / 180);
		y1 = y1 * (Math.PI / 180);
		y2 = y2 * (Math.PI / 180);
		double dlong = x1 - x2;
		double dlat = y1 - y2;
		double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(y1) *
				Math.cos(y2) * Math.pow(Math.sin(dlong / 2), 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return measure * c;
	}

	/**
	 * Converts decimal degrees to radians.
	 */
	public static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/**
	 * Converts radians to decimal degrees.
	 */
	public static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	public static double distance(GeoPoint aPoint, GeoPoint aNextPoint) {
		return distance(aPoint.lat, aPoint.lon, aNextPoint.lat, aNextPoint.lon, 'K');
	}

	public static double speed(GeoPoint aPoint, GeoPoint aNextPoint) {
		double distance = distance(aPoint, aNextPoint);
		double timeLapse = aNextPoint.timestamp - aPoint.timestamp;

		// Double sample
		if (timeLapse <= 0) {
			return 0;
		}

		return distance / (timeLapse * MILLIS_TO_HOURS);
	}

	public static void p(String s) {
		System.out.println(s);

	}

	public static void main(String[] args) {
		double lon1 = 4.9123;
		double lon2 = 4.9123;
		double lat1 = 52.9123451;
		double lat2 = 52.912345;

		p("all eq=" + distance(lat1, lon1, lat2, lon2, 'K'));
		double dist = 0.0;
		JXElement lastPt = null;
		try {
			JXBuilder builder = new JXBuilder();
			JXElement track = builder.build(new URL("http://www.geotracing.com/gt/srv/get.jsp?cmd=get-track&id=635724"));
			Vector segs = track.getChildByTag("trk").getChildren();
			for (int i = 0; i < segs.size(); i++) {
				Vector pts = ((JXElement) segs.get(i)).getChildren();
				p("seg=" + i);
				lastPt = null;
				for (int j = 0; j < pts.size(); j++) {
					JXElement pt = ((JXElement) pts.get(j));
					p(pt.toString());
					double delta = 0.0;
					if (lastPt != null) {
						delta = distance(pt.getDoubleAttr("lat"), pt.getDoubleAttr("lon"), lastPt.getDoubleAttr("lat"), lastPt.getDoubleAttr("lon"), 'K');
						dist += delta;
					}
					p("delta= " + delta + " dist=" + dist);
					lastPt = pt;
				}

			}

		} catch (Throwable t) {
			p("ERROR: " + t);
		}
	}
}