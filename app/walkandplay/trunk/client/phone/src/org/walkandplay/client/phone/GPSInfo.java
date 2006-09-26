package org.walkandplay.client.phone;

/** GPS status. */
public class GPSInfo {
	static MFloat NULL = new MFloat(0);

	String msg = "";
	MFloat lat = NULL;
	MFloat lon = NULL;
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
		return "msg=" + msg + "\nlatlon=" + format(lat.toString(), 7) + " " + format(lon.toString(), 7) + "\nspeed=" + speed + " km/h" + " max=" + maxSpeed.toShortString() + " km/h\nalt=" + altitude + " m course="
				+ course + "\nsats=" + satsInView + "  used=" + satsUsed + "\nhdopFix=" + hdopFix
				+ "\nhdop=" + hdop + " pdop=" + pdop + " vdop=" + vdop;
	}

	static public String format(String s, int maxLen) {
		if (s.length() <= maxLen) {
			return s;
		}
		return s.substring(0, maxLen - 1);
	}
}