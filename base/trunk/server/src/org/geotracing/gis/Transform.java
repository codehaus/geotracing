// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.gis;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.spatialschema.geometry.DirectPosition;

/*
 * Utility class for coordinate transformation/reprojection.
 *
 * Uses GeoTools2 library.
* $Id:$
*/
public class Transform {
	private static CoordinateReferenceSystem CRS_EPSG_28992;
	private static String CRS_DUTCH_RD="epsg:28992";

	/** Transform between two coordinate systems. */
	public static double[] transform(String aSourceCRS, String aTargetCRS, double aX, double aY) throws Exception {
		CoordinateReferenceSystem sourceCRS = getCRS(aSourceCRS);

		CoordinateReferenceSystem targetCRS = getCRS(aTargetCRS);

		MathTransform math = CRS.findMathTransform(sourceCRS, targetCRS);

		DirectPosition directPosition = new DirectPosition2D(aX, aY);

		DirectPosition result = math.transform(directPosition, null);

		return result.getCoordinates();
	}

	/** Transform from lon/lat to Dutch coordinate system. */
	public static double[] WGS84toRD(double aX, double aY) throws Exception {
		return transform("EPSG:4326", "EPSG:28992", aX, aY);
	}

	/** Transform from Dutch coordinate system to lon/lat. */
	public static double[] RDtoWGS84(double aX, double aY) throws Exception {
		return transform("EPSG:28992", "EPSG:4326", aX, aY);
	}

	/** Get a CRS by epsg code. */
	public static CoordinateReferenceSystem getCRS(String anEPSGCode) throws Exception {
		initCRS();

		if (anEPSGCode.toLowerCase().equals(CRS_DUTCH_RD)) {
			return CRS_EPSG_28992;
		} else {
			return CRS.decode(anEPSGCode);
		}
	}


	private static void initCRS() throws Exception {
		if (CRS_EPSG_28992 != null) {
			return;
		}

		// WKT for Dutch RD coordinate system (EPSG:28992).
		// See http://www.mail-archive.com/geotools-gt2-users@lists.sourceforge.net/msg02530.html
		// Thanks to Wim Blanken (Geon).
		// Far from elegant, but at least it works with the WKT EPSG version of GT2 2.3.0
		String wkt = "PROJCS[\"Amersfoort / RD New\", GEOGCS[\"Amersfoort\", DATUM[\"Amersfoort\","
				+ "SPHEROID[\"Bessel 1841\", 6377397.155, 299.1528128,"
				+ "AUTHORITY[\"EPSG\",\"7004\"]],"
				+ "      TOWGS84[565.040,49.910,465.840,-0.40939,0.35971,-1.86849,4.0772],"
				+ "      AUTHORITY[\"EPSG\",\"6289\"]],  "
				+ "    PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], "
				+ "    UNIT[\"degree\", 0.017453292519943295],"
				+ "   AXIS[\"Geodetic latitude\", NORTH], "
				+ "    AXIS[\"Geodetic longitude\", EAST], "
				+ "   AUTHORITY[\"EPSG\",\"4289\"]],"
				+ "  PROJECTION[\"Oblique Stereographic\", AUTHORITY[\"EPSG\",\"9809\"]], "
				+ "  PARAMETER[\"central_meridian\", 5.387638888888891], "
				+ "  PARAMETER[\"latitude_of_origin\", 52.15616055555556],"
				+ "  PARAMETER[\"scale_factor\", 0.9999079],  "
				+ "  PARAMETER[\"false_easting\", 155000.0], "
				+ " PARAMETER[\"false_northing\", 463000.0], "
				+ " UNIT[\"m\", 1.0],"
				+ "  AXIS[\"Easting\", EAST],"
				+ "  AXIS[\"Northing\", NORTH], "
				+ " AUTHORITY[\"EPSG\",\"28992\"]]";

		CRSFactory crsFactory = FactoryFinder.getCRSFactory(null);
		CRS_EPSG_28992 = crsFactory.createFromWKT(wkt);
	}

	private static void p(String s) {
		System.out.println(s);
	}

	/** Testing 123 and command line util. */
	public static void main(String[] args) {
		try {
			if (args.length != 4) {
				p("Usage: org.geotracing.gis.Transform <srcCRS> <destCRS> x y \n e.g. EPSG:4326, \"EPSG:28992, 4.726329, 52.378022");
				System.exit(-1);
			}
			double[] xy = transform(args[0], args[1], Double.parseDouble(args[2]), Double.parseDouble(args[3]));
			p(xy[0] + " " + xy[1]);
			// double[] lonlat = transform("EPSG:28992", "EPSG:4326", xy[0], xy[1]);
			//p("lon=" + lonlat[0] + " lat=" + lonlat[1]);
		} catch (Throwable t) {
			p("Error " + t);
			t.printStackTrace();
		}
	}

}

