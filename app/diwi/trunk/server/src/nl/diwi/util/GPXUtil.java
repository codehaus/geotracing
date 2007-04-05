package nl.diwi.util;

import nl.justobjects.jox.dom.JXElement;
import org.postgis.LineString;
import org.postgis.PGgeometryLW;
import org.postgis.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class GPXUtil implements Constants {

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
	public static LineString GPXTrack2LineString(JXElement gpx) {
		try {
			// Create new Track object
			Vector trkElms = gpx.getChildrenByTag("trk");

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

	public static LineString GPXRoute2LineString(JXElement gpx) {
		try {
			// Create new Track object
			Vector routeElements = gpx.getChildrenByTag("rte");

			List points = new ArrayList();
			Point point;
			LineString lineString;

			for (int i = 0; i < routeElements.size(); i++) {
				JXElement nextRoute = (JXElement) routeElements.elementAt(i);
				Vector pointElements = nextRoute.getChildrenByTag("rtept");
				if (pointElements == null || pointElements.size() == 0) {
					p("No route points found");
					continue;
				}

				// Parse and handle all routepoints 
				for (int j = 0; j < pointElements.size(); j++) {
					JXElement nextPoint = (JXElement) pointElements.elementAt(j);

					// Lat/lon
					double lon = nextPoint.getDoubleAttr("lon");
					double lat = nextPoint.getDoubleAttr("lat");
					double ele = 0.0d;
					// Height (elevation)
					String eleStr = nextPoint.getChildText("ele");
					if (eleStr != null) {
						ele = Double.parseDouble(eleStr);
					}

					point = new Point(lon, lat, ele);
					point.setSrid(DEFAULT_SRID);


					points.add(point);
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
