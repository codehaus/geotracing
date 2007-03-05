package org.geotracing.test.postgis;

import org.keyworx.oase.api.Record;
import org.postgis.PGgeometryLW;
import org.postgis.Point;

/**
 * Test class Oase-PostGIS basics..
 *
 * @author Just van den Broecke
 */
public class BasicsTest extends PGTestCase {
	public static final String SRID_POINT_1 = "SRID=4326;POINT(4.92 52.35)";
	public static final String SRID_POINT_2 = "SRID=4326;POINT(-4.92 -52.35)";
	public static final String SRID_POINT_3 = "SRID=4326;POINT(-4.92 52.35)";
	public static final String SRID_POINT_4 = "SRID=4326;POINT(-4.98 52.37)";

	public BasicsTest() {
		super("BasicsTest");
	}

	public void testInsertPoint() {
		try {

			// Simple: create and update and commit.
			Record record = getModifier().create(SPATIAL_ONE_TABLE_NAME);
			assertNotNull("table.create()", record);

			String name = "TheName";

			record.setStringField("name", name);

			// METHOD 1: create Point and PGgeometryLW wrapper object
			Point point = new Point(4.92, 52.35);
			point.setSrid(4326);

			PGgeometryLW geom = new PGgeometryLW(point);
			record.setObjectField("point", geom);
			getModifier().insert(record);

			int id = record.getId();
			record = getFinder().read(id);
			geom = (PGgeometryLW) record.getObjectField("point");
			String result = geom.toString();
			assertEquals("result not equal to " + SRID_POINT_1 + " but " + result, result, SRID_POINT_1);

			// METHOD 2: create PGgeometryLW wrapper object from String
			geom = new PGgeometryLW(SRID_POINT_2);
			record.setObjectField("point", geom);
			getModifier().update(record);
			record = getFinder().read(id);
			geom = (PGgeometryLW) record.getObjectField("point");
			Point geometry = (Point) geom.getGeometry();
			result = geom.toString();
			assertEquals("result not equal to " + SRID_POINT_2 + " but " + result, result, SRID_POINT_2);

			// METHOD 3: let Oase create PGgeometryLW by setting field from String
			record.setField("point", SRID_POINT_3);
			getModifier().update(record);
			record = getFinder().read(id);
			geom = (PGgeometryLW) record.getObjectField("point");
			result = geom.toString();
			assertEquals("result not equal to " + SRID_POINT_3 + " but " + result, result, SRID_POINT_3);

			// METHOD 4: use OGC String format
			record.setStringField("point", "GeomFromText('POINT (-4.98 52.37)', 4326)");
			// record.setField("point", SRID_POINT_4);
			getModifier().update(record);
			record = getFinder().read(id);
			geom = (PGgeometryLW) record.getObjectField("point");
			result = geom.toString();
			assertEquals("result not equal to " + SRID_POINT_4 + " but " + result, result, SRID_POINT_4);

		} catch (Throwable t) {
			failTest("testInsertPoint: ", t);
		}
	}

}
