// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.client;

/**
 * Holds GPS status data.
 *
 * @author  Just van den Broecke
 * @version $Id$
 */
public class GPSInfo {
	static MFloat NULL = new MFloat(0);

	String msg = "";
	public MFloat lat = NULL;
	public MFloat lon = NULL;
	String speed = "unknown";
	static MFloat maxSpeed = NULL;
	String altitude = "unknown";
	String course = "unknown";
	String satsInView = "unknown";
	String satsUsed = "unknown";
	String hdopFix = "unknown";
	String hdop = "unknown";
	String pdop = "unknown";
	String vdop = "unknown";

	public String toString() {
		return "msg=" + msg + "\nlatlon=" + Util.format(lat, 7) + " " + Util.format(lon, 7) + "\nspeed=" + speed + " km/h" + " max=" + maxSpeed.toShortString() + " km/h\nalt=" + altitude + " m course="
				+ course + "\nsats=" + satsInView + "  used=" + satsUsed + "\nhdopFix=" + hdopFix
				+ "\nhdop=" + hdop + " pdop=" + pdop + " vdop=" + vdop;
	}
}