package buoy;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Parse boeienlijst.
 *
 * @author Just van den Broecke
 */
public class BuoyParser {


	private void parse(String aFileName) {
		String lines[] = fileToStringArray(aFileName);
		pr("read data ok, cnt=" + lines.length);
		for (int i=0; i < lines.length; i++) {
			parseEntry(lines[i]);
		}
	}

	private void parseEntry(String aLine) {
		String elms[] = aLine.split("\t");
		String name = elms[0].trim();
		String type = elms[1].trim();
		String color = elms[2].trim();
		String pt = elms[3].trim();
		//pr("name=[" + name + "]");
		//pr("type=[" + type + "]");
		//pr("color=[" + color + "]");
	    // 		new Buoy("Boei", "VL5", 5.086067, 52.99828);

		pr("new Buoy(\"" + type + "\", \"" + name + "\", " + parseCoord(pt) + ");");
	}

	private String parseCoord(String aLine) {
		String lonlat[] = aLine.split("N");
		String latDM = lonlat[0];
		String lonDM = lonlat[1].split("E")[0];
		return DM2DD(lonDM) + ", " + DM2DD(latDM);
	}

	private String DM2DD(String aDM) {
		double deg = Double.parseDouble(aDM.substring(0, 2));
		double fraction = Double.parseDouble(aDM.substring(3, aDM.length())) / 60.0D;
		return (deg + fraction) + "";
	}

	private static void pr(String s) {
		if (s == null) {
			pr("null message");
		}
		System.out.println(s);
	}

	/**
	 * Return contents of file as array of String.
	 *
	 * @param fileName the filename
	 */
	private String[] fileToStringArray(String fileName) {
		File f = new File(fileName);
		FileInputStream fis = null;
		Vector vector = new Vector(20);
		DataInputStream dis = null;

		try {
			fis = new FileInputStream(f);
			dis = new DataInputStream(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataInputStreamToStringArray(dis);
	}

	/**
	 * Return contents of DataInputStream as String array.
	 *
	 * @param dis the DataInputStream
	 */
	private String[] dataInputStreamToStringArray(DataInputStream dis) {
		List vector = new ArrayList(100);
		try {
			String line = null;
			line = dis.readLine();
			while (line != null) {
				vector.add(line);
				line = dis.readLine();
			}
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (String[]) vector.toArray(new String[vector.size()]);
	}

	/**
	 */
	public static void main(String[] theArgs) {
		BuoyParser cc = new BuoyParser();
		cc.parse("svr-boeien.txt");

	}
}
