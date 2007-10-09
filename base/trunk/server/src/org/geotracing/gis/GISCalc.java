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
 * Note that the WGS84 lat/lon coordinate system is assumed.
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

	/** Return great-circle distance in Kilometers. */
	public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
		return distance(lat1, lon1, lat2, lon2, 'K');
	}

	/** Return great-circle distance in Meters. */
	public static double distanceM(double lat1, double lon1, double lat2, double lon2) {
		return distance(lat1, lon1, lat2, lon2, 'K') * 1000.0d;
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

	/** Calculates meters per degree at GeoPoint. */
	public static double metersPerDegreeLon(double lat, double lon) {
		// Probably not very accurate but ok...
		final double fraction = 0.1d;
		// Calc meters per degree longitude
		return distanceM(lat, lon+fraction, lat, lon) / fraction;
	}

	/** Calculates meters per degree at GeoPoint. */
	public static double metersPerDegreeLat(double lat, double lon) {
		// Probably not very accurate but ok...
		final double fraction = 0.1d;
		// Calc meters per degree longitude
		return distanceM(lat+fraction, lon, lat, lon) / fraction;
	}

	public static double speedKmh(double lat1, double lon1, long t1, double lat2, double lon2, long t2) {
		double distance = distanceKm(lat1, lon1, lat2, lon2);
		double timeLapse = t2-t1;

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
		double lon1 = 4.912;
		double lon2 = 4.924;
		double lat1 = 52.912345;
		double lat2 = 52.912345;

		p("all eq=" + distance(lat1, lon1, lat2, lon2, 'K'));
	/*	double dist = 0.0;
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
		} */
	}
}