package org.geotracing.test.postgis;

import org.postgis.Point;
import org.postgis.PGgeometryLW;
import org.postgis.PGgeometry;

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

}
