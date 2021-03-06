// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.client;

import nl.justobjects.mjox.JXBuilder;
import nl.justobjects.mjox.JXElement;

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.midlet.MIDlet;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * Misc utilities.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class Util {
	private static boolean soundOn = true;
	private static long timeOffset = 0;
	/**
	 * Constant representing earth radius: 60 * 1.1515 * 1609.344
	 */
	private static MFloat DIST_CONST = new MFloat(111190L);
	private static MFloat DIST_FRACTION = new MFloat(10, -2);

	/**
	 * Great circle distance in meters between two lon/lat points.
	 */
	public static MFloat distanceMeters(MFloat lon1, MFloat lat1, MFloat lon2, MFloat lat2) {
		// Same points: zero distance
		if (lat1.Sub(lat2).Equal(MFloat.ZERO) && lon1.Sub(lon2).Equal(MFloat.ZERO)) {
			return MFloat.ZERO;
		}

		/*
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
		*/
		MFloat theta = MFloat.toRadians(lon1.Sub(lon2));
		lat1 = MFloat.toRadians(lat1);
		lat2 = MFloat.toRadians(lat2);

		MFloat d1 = MFloat.sin(lat1).Mul(MFloat.sin(lat2));
		MFloat d2 = MFloat.cos(lat1).Mul(MFloat.cos(lat2)).Mul(MFloat.cos(theta));
		MFloat dist = d1.Add(d2);

		dist = MFloat.acos(dist);
		dist = MFloat.toDegrees(dist);
		dist = dist.Mul(DIST_CONST);
		// System.out.println("distance: " + dist + " meters");
		return dist;
	}

	/** Calculates meters per degree at GeoPoint. */
	public static MFloat metersPerDegreeLon(MFloat lon, MFloat lat) {
		// Calc meters per degree longitude
		return distanceMeters(lon.Add(DIST_FRACTION), lat, lon, lat).Div(DIST_FRACTION);
	}

	/** Calculates meters per degree at GeoPoint. */
	public static MFloat metersPerDegreeLat(MFloat lon, MFloat lat) {
	// Calc meters per degree longitude
		return distanceMeters(lon, lat.Add(DIST_FRACTION), lon, lat).Div(DIST_FRACTION);
	}

	/**
	 * Create a string from the TimeOfDay portion of a time/date as
	 * yyyy-mm-dd hh::mm::ss TZ
	 *
	 * @param date The date/time as milliseconds since the epoch.
	 */
	public static String timeToString(long date) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(date));
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
		int sec = c.get(Calendar.SECOND);
		int year = c.get(Calendar.YEAR);
		int mon = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DATE);
		return (year < 10 ? "0" : "") + year + "-" + (mon < 10 ? "0" : "") + mon + "-" + (day < 10 ? "0" : "") + day
				+ " " + (hour < 10 ? "0" : "") + hour + ":" + (min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec;
	}

	static public String format(MFloat aFloat, int maxLen) {
		String s = aFloat.toString();
		if (s.length() <= maxLen) {
			return s;
		}
		return s.substring(0, maxLen - 1);
	}

	public static Image getImage(String url) throws IOException {
		ContentConnection connection = (ContentConnection) Connector.open(url);
		InputStream is = connection.openInputStream();

		try {
			/*		byte imageData[];
					ByteArrayOutputStream bStrm = new ByteArrayOutputStream();
					int ch;
					while ((ch = dis.read()) != -1)
						bStrm.write(ch);
					imageData = bStrm.toByteArray();
					bStrm.close();        */
			return Image.createImage(is);
		} finally {
			if (is != null) {
				is.close();
			}
			connection.close();
		}
	}

	public static void playStream(String url) throws Exception {
		Player player = Manager.createPlayer(url);
		player.prefetch();
		player.start();
	}

	/**
	 * Get page content from URL.
	 */
	public static String getPage(String url) throws IOException {
		DataInputStream dis = null;
		HttpConnection c = null;
		String result = null;
		try {
			c = (HttpConnection) Connector.open(url);
			dis = new DataInputStream(c.openInputStream());

			int len = (int) c.getLength();
			if (len != -1) {
				// Have length: read in one go
				byte[] bytes = new byte[len];
				dis.readFully(bytes);

				// Produce result.
				result = new String(bytes);
			} else {
				// Read until the connection is closed.
				StringBuffer b = new StringBuffer();
				int ch;
				while ((ch = dis.read()) != -1) {
					b.append((char) ch);
				}
				result = b.toString();
			}
		} finally {
			if (dis != null) {
				dis.close();
			}
			if (c != null) {
				c.close();
			}
		}
		return result;

	}

	/**
	 * Get page content from URL as XML element.
	 */
	public static JXElement getXML(String url) throws IOException {
		HttpConnection c = (HttpConnection) Connector.open(url);
		InputStream is = c.openInputStream();

		try {
			return new JXBuilder().build(is);
		} catch (Throwable t) {
			throw new IOException("Error parsing from URL " + url);
		} finally {
			if (is != null) {
				is.close();
			}
			c.close();
		}
	}

	/**
	 * Encodes data in hexasc.
	 */
	public static String encode(byte[] data, int off, int len) {
		char[] ch;
		int i;

		// Convert bytes to hex digits
		ch = new char[data.length * 2];
		i = 0;

		while (len-- > 0) {
			int b;
			int d;

			// Convert next byte into a hex digit pair
			b = data[off++] & 0xFF;

			d = b >> 4;
			d = d < 0xA ? d + '0' : d - 0xA + 'A';
			ch[i++] = (char) d;

			d = b & 0xF;
			d = d < 0xA ? d + '0' : d - 0xA + 'A';
			ch[i++] = (char) d;
		}

		return new String(ch);
	}

	/**
	 * Get time corrected with offset (see setTime()).
	 */
	static public long getTime() {
		return System.currentTimeMillis() + timeOffset;
	}

	/**
	 * Get time offset with server.
	 */
	static public long getTimeOffset() {
		return timeOffset;
	}

	/**
	 * Split string into multiple strings
	 *
	 * @param original  Original string
	 * @param separator Separator string in original string
	 * @return Splitted string array
	 */
	/*public static String[] split(String original, String separator) {
		// Safety check
		if (original == null || original.length() == 0) {
			return new String[0];
		}

		Vector nodes = new Vector();

		// Parse nodes into vector
		int index = original.indexOf(separator);
		while (index >= 0) {
			nodes.addElement(original.substring(0, index));
			original = original.substring(index + separator.length());
			index = original.indexOf(separator);
		}

		// Get the last node
		nodes.addElement(original);

		// Create splitted string array
		String[] result = new String[nodes.size()];
		if (nodes.size() > 0) {
			for (int loop = 0; loop < nodes.size(); loop++) {
				result[loop] = (String) nodes.elementAt(loop);
			}
		}
		return result;
	} */

	/**
	 * Scale source image to specified width/height.
	 */
	public static Image scaleImage(Image src, int width, int height) {
		//long start = System.currentTimeMillis();
		int scanline = src.getWidth();
		int srcw = src.getWidth();
		int srch = src.getHeight();
		int buf[] = new int[srcw * srch];
		src.getRGB(buf, 0, scanline, 0, 0, srcw, srch);
		int buf2[] = new int[width * height];
		for (int y = 0; y < height; y++) {
			int c1 = y * width;
			int c2 = (y * srch / height) * scanline;
			for (int x = 0; x < width; x++) {
				buf2[c1 + x] = buf[c2 + x * srcw / width];
			}
		}
		//long end = System.currentTimeMillis();
		//System.out.println("Scaled " + src.getWidth() + "x" + src.getHeight() + " in " + ((end - start) / 1000) + " seconds");
		return Image.createRGBImage(buf2, width, height, true);
	}

	/**
	 * Splits the given String around the matches defined by the given delimiter into an array.
	 * Example:
	 * <value>TextUtil.split("one;two;three", ';')</value> results into the array
	 * <value>{"one", "two", "three"}</value>.
	 *
	 * @param value	 the String which should be split into an array
	 * @param delimiter the delimiter which marks the boundaries of the array
	 * @return an array, when the delimiter was not found, the array will only have a single element.
	 */
	public static String[] split(String value, char delimiter) {
		char[] valueChars = value.toCharArray();
		int lastIndex = 0;
		Vector strings = null;
		for (int i = 0; i < valueChars.length; i++) {
			char c = valueChars[i];
			if (c == delimiter) {
				if (strings == null) {
					strings = new Vector();
				}
				strings.addElement(new String(valueChars, lastIndex, i - lastIndex));
				lastIndex = i + 1;
			}
		}
		if (strings == null) {
			return new String[]{value};
		}
		// add tail:
		strings.addElement(new String(valueChars, lastIndex, valueChars.length - lastIndex));

		// Create splitted string array
		String[] result = new String[strings.size()];
		if (strings.size() > 0) {
			for (int loop = 0; loop < strings.size(); loop++) {
				result[loop] = (String) strings.elementAt(loop);
			}
		}

		return result;
	}

	/**
	 * Sets time offset to correct for UTC time mismatch on some phones.
	 */
	static public void setTime(long aTimeMillis) {
		// Calculate offset from (server) time
		timeOffset = aTimeMillis - System.currentTimeMillis();
	}

	static public void showAlert(MIDlet aMidlet, String title, String mesg) {
		Alert a = new Alert(title, mesg, null, AlertType.ALARM);
		a.setTimeout(Alert.FOREVER);
		//Display.getDisplay(aMidlet).setCurrent(a, Display.getDisplay(aMidlet).getCurrent());
	}

	static public boolean hasSound() {
		return soundOn;
	}

	static public void toggleSound() {
		soundOn = !soundOn;
	}

	static public void playTone(int aNote, int aDur, int aVolume) {
		if (!soundOn) {
			return;
		}

		try {
			Manager.playTone(aNote, aDur, aVolume);
		} catch (Throwable t) {
			// ignore
		}
	}
}
