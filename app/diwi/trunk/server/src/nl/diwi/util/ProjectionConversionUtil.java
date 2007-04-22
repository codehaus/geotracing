package nl.diwi.util;

import org.geotracing.gis.Transform;
import org.keyworx.utopia.core.data.UtopiaException;
import org.postgis.LineString;
import org.postgis.Point;

public class ProjectionConversionUtil implements Constants {
	/**
	 * Convert LineString from RD to WGS84.
	 *
	 * @param inLS an RD linestring
	 * @throws UtopiaException Standard exception
	 */
	public static LineString RD2WGS84(LineString inLS) throws UtopiaException {
		try {
			Point[] inPoints = inLS.getPoints();
			Point[] outPoints = new Point[inPoints.length];
			for (int i=0; i < inPoints.length; i++) {
				outPoints[i] = RD2WGS84(inPoints[i]);
			}

			LineString result = new LineString(outPoints);
			result.setSrid(EPSG_WGS84);
			
			return result; 
		} catch (Throwable t) {
			throw new UtopiaException("Cannot convert LineString", t);
		}
	}

	/**
	 * Convert Point from RD to WGS84.
	 *
	 * @param inPT an RD point
	 * @throws UtopiaException Standard exception
	 */
	public static Point RD2WGS84(Point inPT) throws UtopiaException {
		try {
			double xy[] = Transform.RDtoWGS84(inPT.x, inPT.y);
			Point out = new Point(xy[0], xy[1], inPT.z);
			out.setSrid(EPSG_WGS84);
			return out;
		} catch (Throwable t) {
			throw new UtopiaException("Cannot convert Point", t);
		}
	}

	/**
	 * Convert Point from RD to WGS84.
	 *
	 * @param inPT an RD point
	 * @throws UtopiaException Standard exception
	 */
	public static Point WGS842RD(Point inPT) throws UtopiaException {
		try {
			double xy[] = Transform.WGS84toRD(inPT.x, inPT.y);
			Point out = new Point(xy[0], xy[1], inPT.z);
			out.setSrid(EPSG_DUTCH_RD);
			return out;
		} catch (Throwable t) {
			throw new UtopiaException("Cannot convert Point", t);
		}
	}
}
