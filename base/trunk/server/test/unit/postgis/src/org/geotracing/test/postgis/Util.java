package org.geotracing.test.postgis;

import org.postgis.PGgeometryLW;
import org.postgis.Point;
import org.postgis.LineString;

import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: just
 * Date: Feb 1, 2007
 * Time: 5:16:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class Util {
	public static final int DEFAULT_SRID = 4326;

	public static PGgeometryLW createPoint(double lon, double lat) {
		Point point = new Point(lon, lat);
		point.setSrid(DEFAULT_SRID);

		return new PGgeometryLW(point);
	}


	public static void p(String s) {
		System.out.println(s);
	}

	/**
	 * Convert GPX file to linestring.
	 *
	 * @param aFilePath path to GPX file
	 * @return a PG LineString
	 */
	public static LineString GPX2LineString(String aFilePath) {

		try {
			JXElement gpxDoc = new JXBuilder().build(new File(aFilePath));

			// Create new Track object
			Vector trkElms = gpxDoc.getChildrenByTag("trk");

			List points = new ArrayList();
			Point point;
			LineString lineString;
			for (int i = 0; i < trkElms.size(); i++) {
				JXElement nextTrk = (JXElement) trkElms.elementAt(i);
				Vector nextTrkSegs = nextTrk.getChildrenByTag("trkseg");
				if (nextTrkSegs == null || nextTrkSegs.size() == 0) {
					p("No track segments found");
					continue;
				}

				// Parse and handle all track segments in current track
				for (int j = 0; j < nextTrkSegs.size(); j++) {
					JXElement nextSeg = (JXElement) nextTrkSegs.elementAt(j);
					Vector nextTrkPts = nextSeg.getChildrenByTag("trkpt");
					if (nextTrkPts == null || nextTrkPts.size() == 0) {
						p("No track points found");
						continue;
					}

					// Parse and handle all track points in current track segment
					for (int k = 0; k < nextTrkPts.size(); k++) {
						JXElement nextTrkPt = (JXElement) nextTrkPts.elementAt(k);

						// Lat/lon
						double lon = nextTrkPt.getDoubleAttr("lon");
						double lat = nextTrkPt.getDoubleAttr("lat");
						double ele = 0.0d;
						// Height (elevation)
						String eleStr = nextTrkPt.getChildText("ele");
						if (eleStr != null) {
							ele = Double.parseDouble(eleStr);
						}

						point = new Point(lon, lat, ele);
						point.setSrid(DEFAULT_SRID);
						points.add(point);
					}
				}
			}

			lineString = new LineString((Point[]) points.toArray(new Point[points.size()]));
			lineString.setSrid(DEFAULT_SRID);
			return lineString;


		} catch (Throwable t) {
			p("Error converting GPX to linestring: " + t);
			t.printStackTrace();
		}

		return null;
	}

}
