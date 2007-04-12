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
	/** Constant representing earth radius: 60 * 1.1515 * 1609.344 */
	private static MFloat DIST_CONST = new MFloat(111189, 5769);

	/** Constant for Google Map tile size */
	private static MFloat GMAP_TILE_SIZE = new MFloat(256L);
	final static public MFloat MINUS_ONE = new MFloat(-1L);
	final static public MFloat ONE = new MFloat(1L);
	final static public MFloat TWO = new MFloat(2L);
	final static public MFloat TWO_TIMES_PI = TWO.Mul(MFloat.PI);

	/** Great circle distance in meters between two lon/lat points. */
	public static MFloat distanceMeters(MFloat lon1, MFloat lat1, MFloat lon2, MFloat lat2) {
		// Same points: zero distance
		if (lat1.Sub(lat2).Equal(MFloat.ZERO) && lon1.Sub(lon2).Equal(MFloat.ZERO)) {
			return MFloat.ZERO;
		}

		MFloat theta = MFloat.toRadians(lon1.Sub(lon2));
		lat1 = MFloat.toRadians(lat1);
		lat2 = MFloat.toRadians(lat2);

		MFloat dist = MFloat.sin(lat1).Mul(MFloat.sin(lat2).Add(MFloat.cos(lat1).Mul(MFloat.cos(lat2)).Mul(MFloat.cos(theta))));

		dist = MFloat.acos(dist);
		dist = MFloat.toDegrees(dist);
		dist = dist.Mul(DIST_CONST);
		return dist;
	}

	public static class XY {
		public int x;
		public int y;
	}

	/**
	 * Returns pixel offset in tile for given lon,lat,zoom.
	 * see http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	 */
	public static Util.XY getGMapPixelXY(String aLon, String aLat, String aZoom) {
		MFloat lon = MFloat.parse(aLon, 10);
		MFloat lat = MFloat.parse(aLat, 10);
		MFloat zoom = MFloat.parse(aZoom, 10);

		// Number of tiles in each axis at zoom level
		MFloat tiles = MFloat.pow(TWO, new MFloat(zoom));

		// Circumference in pixels
		MFloat circumference = GMAP_TILE_SIZE.Mul(tiles);
		MFloat radius = circumference.Div(TWO_TIMES_PI);

		// Use radians
		MFloat longitude = MFloat.toRadians(lon);
		MFloat latitude = MFloat.toRadians(lat);

		// To correct for origin in top left but calculated values from center
		MFloat falseEasting = circumference.Div(TWO);
		MFloat falseNorthing = falseEasting; // circumference / 2.0D;

		// Do x
		MFloat xf = radius.Mul(longitude);
		// System.out.println("x1=" + x);

		// Correct for false easting
		int x = (int) falseEasting.Add(xf).toLong();
		// System.out.println("x2=" + x);

		MFloat tilesXOffset = xf.Div(GMAP_TILE_SIZE);
		xf = xf.Sub((tilesXOffset.Mul(GMAP_TILE_SIZE)));
		// System.out.println("x3=" + x + " tilesXOffset =" + tilesXOffset);

		// Do yf
		MFloat yf = radius.Div(TWO.Mul(MFloat.log((ONE.Add(MFloat.sin(latitude)).Div(ONE.Sub(MFloat.sin(latitude)))))));
		// System.out.println("y1=" + yf);

		// Correct for false northing
		yf = (yf.Sub(falseNorthing).Mul(MINUS_ONE));
		// System.out.println("y2=" + yf);

		// Number of pixels to subtract for tiles skipped (offset)
		MFloat tilesYOffset = yf.Div(GMAP_TILE_SIZE);
		yf = yf.Sub(tilesYOffset.Mul(GMAP_TILE_SIZE));
		// System.out.println("y3=" + yf + " tilesYOffset =" + tilesYOffset);

		Util.XY xy = new Util.XY();
		xy.x = (int) xf.toLong();
		xy.y = (int) yf.toLong();
		return xy;
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
		return (year < 10 ? "0" : "") + year + "-" + (mon < 10 ? "0" : "") + mon+ "-" + (day < 10 ? "0" : "") + day
		+ " " + (hour < 10 ? "0" : "") + hour + ":" + (min < 10 ? "0" : "") + min + ":" + (sec < 10 ?"0" : "") + sec;
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
