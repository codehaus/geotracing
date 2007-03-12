// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.gis;

import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.TableDef;
import org.keyworx.oase.api.FieldDef;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.store.source.PostgreSQLDBSource;
import org.keyworx.oase.store.record.RecordImpl;
import org.keyworx.oase.config.TypeDef;
import org.keyworx.oase.config.StoreContextConfig;
import org.keyworx.oase.util.Log;
import org.postgis.PGgeometryLW;
import org.postgis.Point;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

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
			return GISCalc.speedKmh(point1.y, point1.x, (long) point1.m, point2.y, point2.x, (long) point1.m);
		} else {
			// Calculate distance in SRID system (Pythagoras), e.g. meters in NL RD
			throw new IllegalArgumentException("speed for this SRID not (yet) supported");
		}
	}
}

