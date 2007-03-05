package org.geotracing.test.postgis;

import org.keyworx.oase.api.Admin;
import org.keyworx.oase.api.Record;
import org.postgis.PGgeometryLW;
import org.postgis.Point;

/**
 * Test class Oase-PostGIS admin.
 *
 * @author Just van den Broecke
 */
public class AdminTest extends PGTestCase {
	public static final String SRID_POINT_1 = "SRID=4326;POINT(4.92 52.35)";
	public static final String SRID_POINT_2 = "SRID=4326;POINT(-4.92 -52.35)";
	public static final String SRID_POINT_3 = "SRID=4326;POINT(-4.92 52.35)";
	public static final String SRID_POINT_4 = "SRID=4326;POINT(-4.98 52.37)";

	public AdminTest() {
		super("AdminTest");
	}

	public void testBackupRestore() {
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
			Admin admin = getAdmin();
			String file = admin.backupTables("tables with postgis");
			info("backup: " + file);
			admin.restore(file);
			int id = record.getId();
			record = getFinder().read(id);
			geom = (PGgeometryLW) record.getObjectField("point");
			String result = geom.toString();
			assertEquals("result not equal to " + SRID_POINT_1 + " but " + result, result, SRID_POINT_1);

		} catch (Throwable t) {
			failTest("testBackupRestore: ", t);
		}
	}

}
