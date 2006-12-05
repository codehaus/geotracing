// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.gis;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Decodes stream of NMEA 0183 data into GeoSamples.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class GPSDecoder {
	/**
	 * NMEA time format, e.g. 224321.657.
	 */
	public static final String NMEA_TIME_FORMAT = "HHmmss.SSS";
	/**
	 * NMEA time format, e.g. 224321.657.
	 */
	public static final SimpleDateFormat TIME_DECODER = new SimpleDateFormat("yyMMdd" + NMEA_TIME_FORMAT);

	private BufferedReader reader;
	private static String startYYMMDD;
	private static GPSSample lastSample;

	/**
	 * Needed since GPS is 2 hours too early.
	 */
	private static final long TIME_CORRECTION_MILLIS = 7200000L;
	private static final long FULL_DAY_MILLIS = 24L * 60L * 60L * 1000L;

	public GPSDecoder(BufferedReader aReader) throws IOException {
		reader = aReader;
	}

	public GPSSample getNext() throws IOException {

		GPSSample geoSample = null;
		while (geoSample == null) {
			String line = reader.readLine();

			if (line == null) {
				try {
					reader.close();
				} catch (Throwable ignore) {
					throw new EOFException("EOF reached");
				}
				break;
			}
			geoSample = parseSample(line);
		}

		return geoSample;

	}


	// Convert to dd.ddddd
	public static void setStartDate(String YYMMDD) {
		startYYMMDD = YYMMDD;
		lastSample = null;
	}

	// Convert to dd.ddddd
	public static GPSSample parseSample(String aLine) {
		GPSSample geoSample = null;
		try {
			// p(aLine);
			/*
			GGA,123519,4807.038,N,01131.324,E,1,08,0.9,545.4,M,46.9,M, , *42
					  123519       Fix taken at 12:35:19 UTC
					  4807.038,N   Latitude 48 deg 07.038' N
					  01131.324,E  Longitude 11 deg 31.324' E
					  1            Fix quality: 0 = invalid
												1 = GPS fix
												2 = DGPS fix
					  08           Number of satellites being tracked
					  0.9          Horizontal dilution of position
					  545.4,M      Altitude, Metres, above mean sea level
					  46.9,M       Height of geoid (mean sea level) above WGS84
								   ellipsoid
					  (empty field) time in seconds since last DGPS update
					  (empty field) DGPS station ID number

			*/
			if (aLine.indexOf("GGA") != -1) {
				p(aLine);
				String[] tokens = aLine.split(",");
				// 1 is fix quality ok
				if (!tokens[6].equals("1")) {
					return null;
				}

				// p("GGA time=" + tokens[1] + " lat=" + tokens[2] + tokens[3] + " long=" + tokens[4] + tokens[5]);
				geoSample = new GPSSample(parseLatitude(tokens[2], tokens[3]), parseLongitude(tokens[4], tokens[5]), parseTime(tokens[1]));
				geoSample.rawData = aLine;

				// Horizontal Dilution of Precision
				// HDOP see http://www.gpsdotnet.com/kb/article.aspx?id=10002
				double hdop = Double.parseDouble(tokens[8]);

				// Make percentage: 100 is best
				geoSample.accuracy = 100 - (int) Math.round((hdop * 100.0D) / 50.0D);

				if (geoSample.accuracy < 90) {
					return null;
				}

				// Calculate speed km/s from lastsample
				/* if (lastSample != null) {
					double distance = distance(geoSample.lat, geoSample.lon, lastSample.lat, lastSample.lon, 'K');
					double timeLapse = geoSample.timestamp - lastSample.timestamp;

					// Double sample
					if (timeLapse <= 0) {
						return null;
					}

					geoSample.speed = distance / (timeLapse * MILLIS_TO_HOURS);
					// System.out.println("GGA speed=" + geoSample.speed);
				} */

				lastSample = geoSample;

			}
			/*
			RMC - Recommended minimum specific GPS/Transit data
				 RMC,225446,A,4916.45,N,12311.12,W,000.5,054.7,191194,020.3,E*68
					225446       Time of fix 22:54:46 UTC
					A            Navigation receiver warning A = OK, V = warning
					4916.45,N    Latitude 49 deg. 16.45 min North
					12311.12,W   Longitude 123 deg. 11.12 min West
					000.5        Speed over ground, Knots
					054.7        Course Made Good, True
					191194       Date of fix  19 November 1994
					020.3,E      Magnetic variation 20.3 deg East
					*68          mandatory checksum

              */
			else if (aLine.indexOf("RMC") != -1) {
				// p(aLine);
				String[] tokens = aLine.split(",");

				// A=OK
				if (!tokens[2].equals("A")) {
					return null;
				}

				geoSample = new GPSSample(parseLatitude(tokens[3], tokens[4]), parseLongitude(tokens[5], tokens[6]), parseTime(tokens[1]));
				geoSample.speed = parseSpeed(tokens[7]);
				// p("RMC time=" + tokens[1] + " lat=" + tokens[3] + tokens[4] + " long=" + tokens[5] + tokens[6]);

				lastSample = geoSample;
			} else {
			}
		} catch (Throwable t) {
			p("ERROR: " + t);
			return null;
		}


		return geoSample;

	}

	// Convert to dd.ddddd
	private static double parseLatitude(String aLatitude, String aHemisphere) {
		double degrees = Double.parseDouble(aLatitude.substring(0, 2));
		double minutes = Double.parseDouble(aLatitude.substring(2, aLatitude.length()));

		double result = degrees + minutes / 60.0F;
		return aHemisphere.equals("N") ? result : -result;
	}

	// Convert to dd.ddddd
	private static double parseLongitude(String aLongitude, String aHemisphere) {
		double degrees = Double.parseDouble(aLongitude.substring(0, 3));
		double minutes = Double.parseDouble(aLongitude.substring(3, aLongitude.length()));
		double result = degrees + minutes / 60.0F;
		return aHemisphere.equals("E") ? result : -result;

	}


	// Convert to dd.ddddd
	private static double parseSpeed(String aKnotsStr) {
		double knots = Double.parseDouble(aKnotsStr);
		return knots * GISCalc.KM_PER_KNOT;
	}

	// Convert to dd.ddddd
	private static long parseTime(String aTimeStr) {
		try {
			Date date = TIME_DECODER.parse(startYYMMDD + aTimeStr);
			String s = date.toString(); //TIME_DECODER.format(date);
			long time = date.getTime() + TIME_CORRECTION_MILLIS;
			// Correct to previous day for time between 10 and 12 at night
			if (aTimeStr.startsWith("22") || aTimeStr.startsWith("23")) {
				time = time - FULL_DAY_MILLIS;
			}
			return time;
		} catch (Throwable t) {
			return 0;
		}
	}

	public static void p(String s) {
		// System.out.println(s);
	}


	public static void main(String[] args) {
		try {
			// In case no GPS present use file data
			String GPS_DEVICE = "/Users/just/project/waag/keyworx/research/gps/data/nmea-sample.txt";
			//String GPS_DEVICE = "/dev/tty.GPS";
			p("opening GPS device: " + GPS_DEVICE);

			File gpsDevice = new File(GPS_DEVICE);
			BufferedReader reader = new BufferedReader(new FileReader(gpsDevice));
			GPSDecoder nmeaDecoder = new GPSDecoder(reader);
			GPSSample geoSample = null;

			while (true) {

				geoSample = nmeaDecoder.getNext();
				if (geoSample == null) {
					break;
				}
				p("OK: " + geoSample);
			}
		} catch (Throwable t) {
			p("ERROR: " + t);
		}
	}

}