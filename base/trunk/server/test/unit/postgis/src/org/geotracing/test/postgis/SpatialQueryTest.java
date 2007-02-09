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
	public static final String EXTENT_POINT_2 = "SRID=4326;POINT(1 7)";
	public static final String EXTENT_POINT_3 = "SRID=4326;POINT(9 1)";
	public static final String EXTENT_POINT_4 = "SRID=4326;POINT(9 7)";
	public static final String MIDDLE_POINT = "SRID=4326;POINT(5 4)";
	public static final String[] RECT = {EXTENT_POINT_1, EXTENT_POINT_2, EXTENT_POINT_3, EXTENT_POINT_4, MIDDLE_POINT};

	public SpatialQueryTest() {
		super("SpatialQueryTest");
	}

	public void testSelectPointsInBBox() {
		try {

			insertTestPoints();

			// get all
			String queryBBox = "SELECT * from spatialone where point && SetSRID('BOX3D(0 0, 10 10)'::box3d,4326)";
			Record[] records = getFinder().freeQuery(queryBBox);
			assertEquals("result not equal ", 5, records.length);

			// get middle
			queryBBox = "SELECT * from spatialone where point && SetSRID('BOX3D(2 2, 6 5)'::box3d,4326)";
			info(queryBBox);
			records = getFinder().freeQuery(queryBBox);
			assertEquals("result not equal ", 1, records.length);
		} catch (Throwable t) {
			failTest("testSelectPointsInBBox: ", t);
		}
	}

	public void testSelectPointsInRadius() {
		try {

			insertTestPoints();

			// Get single near middle point in radius 100km
			String queryAround = "SELECT * FROM spatialone WHERE distance_sphere(GeomFromText('POINT(5.1 4.1)',4326),point) < 100000";
			Record[] records = getFinder().freeQuery(queryAround);
			assertEquals("result not equal ", 1, records.length);
			assertEquals("result not middle point ", MIDDLE_POINT, records[0].getField("point") + "");

			// Get all in large radius 10000 km
			queryAround = "SELECT * FROM spatialone WHERE distance_sphere(GeomFromText('POINT(5.1 4.1)',4326),point) < 10000000";
			info(queryAround);
			records = getFinder().freeQuery(queryAround);
			assertEquals("result not equal ", 5, records.length);
			// assertEquals("result not middle point ", "POINT(5 4)", records[0].getField("point")+"");
		} catch (Throwable t) {
			failTest("testSelectPointsInRadius: ", t);
		}
	}


	public void testSelectNearestPoints() {
		try {
			// select * from villages order by distance((select geom from towns where town='mytown'),geom) asc limit 1;

			insertTestPoints();

			// Get middle
			String queryNearest =
					"SELECT * FROM spatialone order by distance_sphere(GeomFromText('POINT(6 5)',4326),point) asc limit 3";

			// "SELECT name, AsText(point) AS point FROM " + SPATIAL_ONE_TABLE_NAME +
			//		" WHERE point && Expand(GeomFromText('POINT(2.1 2.1)',4326), 0.2)"
			//		+ " AND Distance(GeomFromText('POINT(2.1 2.1)',4326),point) < 0.2";
			// queryMiddle = "SELECT name,AsText(point) AS point FROM spatialone WHERE point && Expand(GeomFromText('POINT(2 3)',4326), 100) AND Distance(GeomFromText('POINT(2 3)',4326),point) < 100";

			// works linux:
			// SELECT name,point AS point FROM spatialone WHERE point && Expand(GeomFromText('POINT(2 3)',4326), 100) AND Distance(GeomFromText('POINT(2 3)',4326),point) < 100;
			Record[] records = getFinder().freeQuery(queryNearest);

			info(queryNearest);
			for (int i = 0; i < records.length; i++) {
				info(records[i].getField("point")+"");
			}
			assertEquals("result not equal ", 3, records.length);
			assertEquals("result[0] not " + MIDDLE_POINT, MIDDLE_POINT, records[0].getField("point") + "");
			assertEquals("result[1] not " + EXTENT_POINT_4, EXTENT_POINT_4, records[1].getField("point") + "");
			assertEquals("result[2] not " + EXTENT_POINT_3, EXTENT_POINT_3, records[2].getField("point") + "");

		} catch (Throwable t) {
			failTest("testSelectNearestPoints: ", t);
		}
	}

	public void testSelectNearestPointsInBBox() {
		try {
			// select * from villages order by distance((select geom from towns where town='mytown'),geom) asc limit 1;

			insertTestPoints();

			// Get middle
			String queryNearest =
					"SELECT * FROM spatialone where point && SetSRID('BOX3D(2 2, 8 6)'::box3d,4326) order by distance_sphere(GeomFromText('POINT(6 5)',4326),point) asc limit 3";

			// "SELECT name, AsText(point) AS point FROM " + SPATIAL_ONE_TABLE_NAME +
			//		" WHERE point && Expand(GeomFromText('POINT(2.1 2.1)',4326), 0.2)"
			//		+ " AND Distance(GeomFromText('POINT(2.1 2.1)',4326),point) < 0.2";
			// queryMiddle = "SELECT name,AsText(point) AS point FROM spatialone WHERE point && Expand(GeomFromText('POINT(2 3)',4326), 100) AND Distance(GeomFromText('POINT(2 3)',4326),point) < 100";

			// works linux:
			// SELECT name,point AS point FROM spatialone WHERE point && Expand(GeomFromText('POINT(2 3)',4326), 100) AND Distance(GeomFromText('POINT(2 3)',4326),point) < 100;
			Record[] records = getFinder().freeQuery(queryNearest);

			info(queryNearest);
			for (int i = 0; i < records.length; i++) {
				info(records[i].getField("point")+"");
			}
			assertEquals("result not equal ", 1, records.length);
			assertEquals("result[0] not " + MIDDLE_POINT, MIDDLE_POINT, records[0].getField("point") + "");
			//assertEquals("result[1] not " + EXTENT_POINT_4, EXTENT_POINT_4, records[1].getField("point") + "");
			//assertEquals("result[2] not " + EXTENT_POINT_3, EXTENT_POINT_3, records[2].getField("point") + "");

		} catch (Throwable t) {
			failTest("testSelectNearestPointsInBBox: ", t);
		}
	}

	protected Record insertRecord(String aName, String aPoint) throws Exception {
		Record record = getModifier().create(SPATIAL_ONE_TABLE_NAME);
		record.setStringField("name", aName);
		record.setField("point", aPoint);
		getModifier().insert(record);
		return record;
	}

	protected void insertTestPoints() throws Exception {
		// Simple: create and update and commit.
		for (int i = 0; i < RECT.length; i++) {
			insertRecord("p" + (i + 1), RECT[i]);
		}
	}


}
