package org.geotracing.test.postgis;

import org.keyworx.oase.api.Record;
import org.postgis.PGgeometry;
import org.postgis.Point;

/**
 * Test class Oase-PostGIS spatial queries..
 *
 * @author Just van den Broecke
 */
public class SpatialQueryTest extends PGTestCase {
	public static final String EXTENT_POINT_1 = "SRID=4326;POINT(1 1)";
	public static final String EXTENT_POINT_2 = "SRID=4326;POINT(1 5)";
	public static final String EXTENT_POINT_3 = "SRID=4326;POINT(3 5)";
	public static final String EXTENT_POINT_4 = "SRID=4326;POINT(3 1)";
	public static final String MIDDLE_POINT = "SRID=4326;POINT(2 2)";
	public static final String[] RECT = {EXTENT_POINT_1, EXTENT_POINT_2, EXTENT_POINT_3, EXTENT_POINT_4};

	public SpatialQueryTest() {
		super("SpatialQueryTest");
	}

	public void testPointInExtent() {
		try {

			// Simple: create and update and commit.
			for (int i = 0; i < RECT.length; i++) {
				insertRecord("p" + (i + 1), RECT[i]);
			}

			insertRecord("middle", MIDDLE_POINT);
			String queryAll = "SELECT * FROM " + SPATIAL_ONE_TABLE_NAME;
			Record[] records = getFinder().freeQuery(queryAll);
			// assertEquals("result not equal ", 5, records.length);

			// Get middle
			String queryMiddle = "SELECT name, AsText(point) AS point FROM " + SPATIAL_ONE_TABLE_NAME +
					" WHERE point && Expand(GeomFromText('POINT(2.1 2.1)',4326), 0.2)"
				 + " AND Distance(GeomFromText('POINT(2.1 2.1)',4326),point) < 0.2";
			// queryMiddle = "SELECT name,AsText(point) AS point FROM spatialone WHERE point && Expand(GeomFromText('POINT(2 3)',4326), 100) AND Distance(GeomFromText('POINT(2 3)',4326),point) < 100";

			// works linux:
			// SELECT name,point AS point FROM spatialone WHERE point && Expand(GeomFromText('POINT(2 3)',4326), 100) AND Distance(GeomFromText('POINT(2 3)',4326),point) < 100;
			records = getFinder().freeQuery(queryMiddle);

			System.out.println(queryMiddle);
			for (int i = 0; i < records.length; i++) {
				System.out.println(records[i].getField("point"));
			}

			 queryMiddle = "SELECT name,AsText(point) AS point FROM " + SPATIAL_ONE_TABLE_NAME +
					" WHERE point && GeomFromText('BOX3D(1 2, 5 6)'::box3d,4326);";
			//queryMiddle ="select name,AsText(point) AS point from spatialone where point && GeomFromText('POLYGON((0 0,0 10,10 10,10 0,0 0))', 4326);";
			queryMiddle ="SELECT * from spatialone where point && SetSRID('BOX3D(1.5 1.5,2.5 2.5)'::box3d,4326);";
				System.out.println(queryMiddle);
			records = getFinder().freeQuery(queryMiddle);
			assertEquals("result not equal ", 1, records.length);
			PGgeometry geom = (PGgeometry)records[0].getObjectField("point");
			Point pt = (Point) geom.getGeometry();

			for (int i = 0; i < records.length; i++) {
				 geom = (PGgeometry)records[i].getObjectField("point");
				 pt = (Point) geom.getGeometry();
				System.out.println("pt lon=" + pt.getX() + " lat=" + pt.getY());
			}

		} catch (Throwable t) {
			failTest("testExtent: ", t);
		}
	}

	protected Record insertRecord(String aName, String aPoint) throws Exception {
		Record record = getModifier().create(SPATIAL_ONE_TABLE_NAME);
		record.setStringField("name", aName);
		record.setField("point", aPoint);
		getModifier().insert(record);
		return record;
	}

}
