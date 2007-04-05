// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.gis;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Logging;
import org.keyworx.common.log.Log;

import org.postgis.LineString;
import org.postgis.PGgeometryLW;
import org.postgis.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Utility functions for PostGIS spatial database.
 * <p/>
 * <h3>Purpose</h3>
 * <p/>
 * Provide utility functions to work with spatial columns and objects.
 * </p>
 * <h3>Examples</h3>
 * see {@link org.geotracing.handler.Location}
 * <p/>
 * <h3>Implementation</h3>
 * see {@link org.keyworx.oase.api.Record}
 * <p/>
 * <h3>Concurrency</h3>
 * not applicable
 *
 * @author Just van den Broecke
 * @version $Id$
 */

public class PostGISUtil {
	public static final int SRID_RD = 28992;
	public static final int SRID_WGS84 = 4326;
	public static final int SRID_DEFAULT = SRID_WGS84;
	public static Log log = Logging.getLog("PostGISUtil");
	/**
	 * Create PostGIS geometry object wrapped with Point.
	 */
	public static PGgeometryLW createPointGeom(int anSRID, double aLon, double aLat, double anEle, long aTime) {
		return new PGgeometryLW(createPoint(anSRID, aLon, aLat, anEle, aTime));
	}

	/**
	 * Create PostGIS Point object wrapped with Point.
	 */
	public static Point createPoint(int anSRID, double aLon, double aLat, double anEle, long aTime) {
		Point point = new Point(aLon, aLat, anEle);
		point.setSrid(anSRID);
		point.setM(aTime);
		return point;
	}

	/**
	 * Create PostGIS Point as WGS84 (standard lon/lat) object wrapped with Point.
	 */
	public static Point createPoint(double aLon, double aLat, double anEle, long aTime) {
		return createPoint(SRID_WGS84, aLon, aLat, anEle, aTime);
	}

	/**
	 * Create PostGIS Point as WGS84 (standard lon/lat) object wrapped with Point.
	 */
	public static Point createPoint(double aLon, double aLat, double anEle) {
		return createPoint(SRID_WGS84, aLon, aLat, anEle, 0L);
	}

	/**
	 * Create PostGIS Point as WGS84 (standard lon/lat) object wrapped with Point.
	 */
	public static Point createPoint(double aLon, double aLat) {
		return createPoint(SRID_WGS84, aLon, aLat, 0.0d, 0L);
	}

	public static Point createPoint(String aLonStr, String aLatStr) {
		return createPoint(Double.parseDouble(aLonStr), Double.parseDouble(aLatStr));
	}

	public static Point createPoint(String aLonStr, String aLatStr, String anEleStr) {
		return createPoint(Double.parseDouble(aLonStr), Double.parseDouble(aLatStr), Double.parseDouble(anEleStr));
	}

	/**
	 * Create PostGIS Point as WGS84 (standard lon/lat) object wrapped with Point from GPS sample.
	 */
	public static Point createPoint(GPSSample aGPSsample) {
		return createPoint(SRID_WGS84, aGPSsample.lon, aGPSsample.lat, aGPSsample.elevation, aGPSsample.timestamp);
	}

	/**
	 * Return distance between two points.
	 */
	public static double distance(Point point1, Point point2) {
		if (point1.getSrid() == SRID_WGS84) {
			// Calculate great-circle distance in kms
			return GISCalc.distanceKm(point1.y, point1.x, point2.y, point2.x);
		} else {
			// Calculate distance in SRID system (Pythagoras), e.g. meters in NL RD
			return point1.distance(point2);
		}
	}

	/**
	 * Calculate speed in km/h between two points.
	 */
	public static double speed(Point point1, Point point2) {
		if (point1.getSrid() == SRID_WGS84) {
			// Calculate great-circle distance in kms
			return GISCalc.speedKmh(point1.y, point1.x, (long) point1.m, point2.y, point2.x, (long) point2.m);
		} else {
			// Calculate distance in SRID system (Pythagoras), e.g. meters in NL RD
			throw new IllegalArgumentException("speed for this SRID not (yet) supported");
		}
	}

	/**
	 * Convert GPX file to linestring.
	 *
	 * @param gpx GPX doc
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
					log.info("No track segments found");
					continue;
				}

				// Parse and handle all track segments in current track
				for (int j = 0; j < nextTrkSegs.size(); j++) {
					JXElement nextSeg = (JXElement) nextTrkSegs.elementAt(j);
					Vector nextTrkPts = nextSeg.getChildrenByTag("trkpt");
					if (nextTrkPts == null || nextTrkPts.size() == 0) {
						log.info("No track points found");
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
						point.setSrid(SRID_DEFAULT);
						points.add(point);
					}
				}
			}

			lineString = new LineString((Point[]) points.toArray(new Point[points.size()]));
			lineString.setSrid(SRID_DEFAULT);
			return lineString;


		} catch (Throwable t) {
			log.info("Error converting GPX to linestring: " + t);
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
					log.info("No route points found");
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
					point.setSrid(SRID_DEFAULT);
					points.add(point);
				}
			}

			lineString = new LineString((Point[]) points.toArray(new Point[points.size()]));
			lineString.setSrid(SRID_DEFAULT);
			return lineString;


		} catch (Throwable t) {
			log.info("Error converting GPX to linestring: " + t);
			t.printStackTrace();
		}

		return null;
	}

}

