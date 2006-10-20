// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.client;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.InputStream;

/**
 * Generic BT GPS data reader.
 *
 * @version $Id$
 * @author Just van den Broecke
 */
public class GPSFetcher implements Runnable {
	private StreamConnection con;
	private InputStream is;
	private static final int BUF_LEN = 200;
	private char lineBuffer[] = new char[BUF_LEN];

	/** States. */
	public static final int DISCONNECTED = 1;
	public static final int CONNECTED = 2;
	public static final int FAILED = 3;

	/** Default start state. */
	public int state = DISCONNECTED;

	public static final long DEFAULT_SAMPLE_INTERVAL_MILLIS = 4000;
	private static long BT_RECONNECT_TIMEOUT_MILLIS = 5000L; // 5 seconds
	private String connectionURL;
	private GPSFetcherListener gpsListener;
	private long sampleIntervalMillis;
	private long statusIntervalMillis = 4000;
	private Thread workerThread;
	private GPSInfo info = new GPSInfo();
	public static final MFloat KM_PER_KNOT = MFloat.parse("1.85200", 10);
	public static final MFloat MINS = new MFloat(60);
	private GPSSmoother gpsSmoother = new GPSSmoother();

	/**
	 * Create a new GPSFetcher.
	 *
	 * @param aListener listener
	 */
	public GPSFetcher(String aURL, GPSFetcherListener aListener) {
		connectionURL = aURL;
		gpsListener = aListener;
	}

	public void start() {
		start(DEFAULT_SAMPLE_INTERVAL_MILLIS);
	}

	public void start(long aSampleIntervalMillis) {
		stop();

		sampleIntervalMillis = aSampleIntervalMillis;
		workerThread = new Thread(this);
		workerThread.start();
	}

	public void stop() {
		// Already stopped or never started
		if (workerThread == null) {
			return;
		}

		// Close connection to GPS if any
		close();

		workerThread = null;
	}

	public void run() {

		long lastTimeLocSent = Util.getTime();
		long lastTimeStatusSent = Util.getTime();

		// when we got the service try sending the location data
		while (workerThread != null && Thread.currentThread() == workerThread) {
			try {

				switch (state) {
					case DISCONNECTED:
						// GPS service found: now make connection to GPS device
						connect();
						break;

					case CONNECTED:
						// Read raw GPS sample
						String nmea = readNMEASentence();
						if (nmea == null) {
							break;
						}

						// Create (parse) location sample
						GPSLocation location = createGPSLocation(nmea);
						if (location != null) {
							// Remember locations for calculating the best
							gpsSmoother.addLocation(location);
						}


						// Send info to observer if interval passed
						long now = Util.getTime();
						if (now - lastTimeStatusSent > statusIntervalMillis) {
							lastTimeStatusSent = now;
							gpsListener.onGPSInfo(info);
						}

						// Notify location if intereval passed and location available
						if (now - lastTimeLocSent > sampleIntervalMillis) {
							GPSLocation bestLocation = gpsSmoother.getBestLocation();
							if (bestLocation != null) {
								gpsSmoother.reset();
								gpsListener.onGPSLocation(bestLocation);
								lastTimeLocSent = now;
							}
						}

						// Continue in this state
						break;

					case FAILED:
						Thread.sleep(BT_RECONNECT_TIMEOUT_MILLIS);
						setState(DISCONNECTED);
						break;
				}

			} catch (Throwable t) {
				gpsListener.onGPSError("Exception in run : ", t);
				close();
				gpsListener.onGPSDisconnect();
				setState(FAILED);
			}
		}

	}

	private void setState(int aNextState) {
		state = aNextState;
	}

	/**
	 * Closes all open connection objects.
	 */
	private void close() {
		try {
			if (is != null) {
				is.close();
				is = null;
			}
			if (con != null) {
				con.close();
				con = null;
			}
		} catch (Throwable t) {
			gpsListener.onGPSStatus("Exception closing connection : " + t);
		}
	}


	/**
	 * Connect to GPS using URL.
	 */
	private void connect() {
		try {
			gpsListener.onGPSStatus("connecting");
			con = (StreamConnection) Connector.open(connectionURL, Connector.READ);

			is = con.openInputStream();
			setState(CONNECTED);
			gpsListener.onGPSConnect();
			info.msg = "connected to " + connectionURL;
			gpsListener.onGPSInfo(info);
		} catch (Throwable t) {
			gpsListener.onGPSStatus("conn error");
			close();
			setState(FAILED);
		}
	}


	/**
	 * Read NMEA GPS line.
	 * @return The line read or null
	 */
	private String readNMEASentence() {
		String result = null;

		try {
			int index = 0, c = 0;

			// Find start of NMEA sentence
			do {
				c = is.read();
				if (c == '$') {
					lineBuffer[index++] = (char) c;
					break;
				}
			} while (c != -1);

			// Read
			while (c != -1 && result == null && index < BUF_LEN - 1) {
				switch (c = is.read()) {
					case -1:
						break;

					case '\n':
					case '\r':
					case '$':
						// End of sentence (or start of new, alas we skip)
						// Create result
						result = new String(lineBuffer, 0, index);
						break;

					default:
						// Any char in NMEA line
						lineBuffer[index++] = (char) c;
						break;
				}
			}

		} catch (Throwable t) {
			gpsListener.onGPSError("readGPSLine: ", t);
			info.msg = "readGPSLine " + t;
			gpsListener.onGPSInfo(info);
			close();

			// Try reconnecting
			setState(FAILED);
		}
		// System.out.println("res=" + result);
		return result;
	}

	/**
	 * Determines whether a location data string is RMC or GGA and of good quality.
	 * @param nmea raw NMEA data string
	 * @return GPSLocation if location ok and null if not
	 */
	private GPSLocation createGPSLocation(String nmea) {
		GPSLocation location = null;
		String type = "n";
		try {
			String[] data = Util.split(nmea, ',');

			if (data.length == 0) {
				return null;
			}

			type = data[0];
			if (type.equals("$GPRMC")) {
				// A=OK
				if (data.length < 9) {
					gpsListener.onGPSStatus("no signal");
					return null;
				}
				if (!data[2].equals("A")) {
					gpsListener.onGPSStatus("no signal");
					info.msg = "bad $GPRMC t=" + data[1];
					return null;
				} else {
					location = new GPSLocation();
					location.data = nmea;
					info.msg = "$GPRMC t=" + data[1];
					location.lat = info.lat = parseLatitude(data[3], data[4]);
					location.lon = info.lon = parseLongitude(data[5], data[6]);
					info.speed = "unknown";
					if (data[7].length() > 0) {
						MFloat speed = MFloat.parse(data[7], 10).Mul(KM_PER_KNOT);
						if (speed.Great(GPSInfo.maxSpeed)) {
							GPSInfo.maxSpeed = speed;
						}
						info.speed = speed.toShortString();
					}

					info.course = "unknown";
					if (data[8].length() > 0) {
						info.course = MFloat.parse(data[8], 10).toShortString();
					}
					return location;
				}
			} else if (type.equals("$GPGGA")) {
				if (data.length < 10) {
					gpsListener.onGPSStatus("no signal");
					return null;
				}
				if (!data[6].equals("1")) {
					info.msg = "bad $GPGGA t=" + data[1];
					gpsListener.onGPSStatus("signal");
					return null;
				} else {
					location = new GPSLocation();
					location.data = nmea;

					info.msg = "$GPGGA t=" + data[1];
					location.lat = info.lat = parseLatitude(data[2], data[3]);
					location.lon = info.lon = parseLongitude(data[4], data[5]);
					info.satsUsed = data[7];
					info.hdopFix = data[8];
					info.altitude = data[9];
					return location;
				}
			} else if (type.equals("$GPGSA")) {
				if (data.length < 18) {
					gpsListener.onGPSStatus("no signal");
					return null;
				}
				info.pdop = data[15];
				info.hdop = data[16];
				info.vdop = Util.split(data[17], '*')[0];
				return null;
			} else if (type.equals("$GPGSV")) {
				if (data.length < 4) {
					gpsListener.onGPSStatus("no signal");
					return null;
				}
				info.msg = "$GPGSV";
				info.satsInView = data[3];
				return null;
			} else {
				info.msg = type;
				return null;
			}
		} catch (Throwable t) {
			gpsListener.onGPSStatus("error v");
			info.msg = "GPS verify exc";

			Log.log("GPS verify error: " + type + " e=" + t);
			return null;
		}
	}

	// Convert to dd.ddddd
	private static MFloat parseLatitude(String aLatitude, String aHemisphere) {
		MFloat degrees = MFloat.parse(aLatitude.substring(0, 2), 10);
		MFloat minutes = MFloat.parse(aLatitude.substring(2, aLatitude.length()), 10);

		MFloat result = degrees.Add(minutes.Div(MINS));
		return aHemisphere.equals("N") ? result : result.Neg();
	}

	// Convert to dd.ddddd
	private static MFloat parseLongitude(String aLongitude, String aHemisphere) {
		MFloat degrees = MFloat.parse(aLongitude.substring(0, 3), 10);
		MFloat minutes = MFloat.parse(aLongitude.substring(3, aLongitude.length()), 10);
		MFloat result = degrees.Add(minutes.Div(MINS));
		return aHemisphere.equals("E") ? result : result.Neg();

	}
}
