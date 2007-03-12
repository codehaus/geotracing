// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package calc;

/**
 * Utils for GIS calculations.
 * ALternative calculations.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class GISCalcAlt {

	// 1 knots = 1.85200 km/h
	public static final double KM_PER_KNOT = 1.85200;
	private static double MILLIS_TO_HOURS = ((1d / 1000d) / 3600d);

	// CONSTANTS USED INTERNALLY
	static final double DEGREES_TO_RADIANS = (Math.PI / 180.0);

// Mean radius in KM
	static final double EARTH_RADIUS = 6371.0;

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


	public static void main(String[] args) {

	}
}