package org.geotracing.client;

/** GPS location . */
public class GPSLocation {
	public String data;
	public MFloat lat;
	public MFloat lon;
	public long time = System.currentTimeMillis();
}