// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.client;

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.midlet.MIDlet;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * Misc utilities.
 *
 * @author  Just van den Broecke
 * @version $Id$
 */
public class Util {
	private static boolean soundOn = true;

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
	 * Split string into multiple strings
	 * @param original      Original string
	 * @param separator     Separator string in original string
	 * @return              Splitted string array
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
	 * Splits the given String around the matches defined by the given delimiter into an array.
	 * Example:
	 * <value>TextUtil.split("one;two;three", ';')</value> results into the array
	 * <value>{"one", "two", "three"}</value>.
	 *
	 * @param value the String which should be split into an array
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


	static public void showAlert(MIDlet aMidlet, String title, String mesg) {
		Alert a = new Alert(title, mesg, null, AlertType.ALARM);
		a.setTimeout(Alert.FOREVER);
		Display.getDisplay(aMidlet).setCurrent(a);
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
