package org.geotracing.test.postgis;

import org.keyworx.common.util.Sys;
import org.keyworx.oase.api.Modifier;
import org.keyworx.oase.api.Record;

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
				info(records[i].getField("point") + "");
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
				info(records[i].getField("point") + "");
			}
			assertEquals("result not equal ", 1, records.length);
			assertEquals("result[0] not " + MIDDLE_POINT, MIDDLE_POINT, records[0].getField("point") + "");
			//assertEquals("result[1] not " + EXTENT_POINT_4, EXTENT_POINT_4, records[1].getField("point") + "");
			//assertEquals("result[2] not " + EXTENT_POINT_3, EXTENT_POINT_3, records[2].getField("point") + "");

		} catch (Throwable t) {
			failTest("testSelectNearestPointsInBBox: ", t);
		}
	}


	public void testSelectPointsInBBoxIndexed() {
		try {
			fillGLocation();
			// get all
			long t1, t2;
			Record[]	 records;

			t1 = Sys.now();
			String queryBBox = "SELECT * from g_location where lon >= 3 AND lat >= 52 AND lon <= 5 AND lat <= 54";
			records = getFinder().freeQuery(queryBBox);
			t2 = Sys.now();
			info("Original lat/lon Query: " + (t2 - t1) + " ms   cnt=" + records.length);

			t1 = Sys.now();
			queryBBox = "SELECT * from g_location where pointnoidx && SetSRID('BOX3D(3 52, 5 54)'::box3d,4326)";
			records = getFinder().freeQuery(queryBBox);
			t2 = Sys.now();
			info("Query without index: " + (t2 - t1) + " ms   cnt=" + records.length);

			t1 = Sys.now();
			queryBBox = "SELECT * from g_location where point && SetSRID('BOX3D(3 52, 5 54)'::box3d,4326)";
			records = getFinder().freeQuery(queryBBox);
			t2 = Sys.now();
			info("Query with index: " + (t2 - t1) + " ms   cnt=" + records.length);
		} catch (Throwable t) {
			failTest("testSelectPointsInBBoxIndexed: ", t);
		}
	}

	public void testLineStringNearness() {
		try {

			int id = insertTestLinestring();

			// within 1000
			String query = "SELECT * from g_route where id=" + id ;
			query += " AND distance(GeomFromText('POINT(40 40)',4326),route_geom)  < 1000";
			Record[] records = getFinder().freeQuery(query);
			assertEquals("result not equal ", 1, records.length);

			// not within 1
			query = "SELECT * from g_route where id=" + id ;
			query += " AND distance(GeomFromText('POINT(40 40)',4326),route_geom)  < 1";
			records = getFinder().freeQuery(query);
			assertEquals("result not equal ", 0, records.length);
			// get middle
		} catch (Throwable t) {
			failTest("testSelectPointsInBBox: ", t);
		}
	}

	protected void fillGLocation() {
		try {
		/*	String queryCount = "SELECT count(id) as count from g_location";
			Record[] records = getFinder().freeQuery(queryCount);
			long count = records[0].getLongField("count");
			if (count > 0) {
				return;
			} */
			Modifier modifier = getModifier();
			Record loc;
			String lon, lat, pt;
			info("inserting: recs");
			int cnt=0;
			for (int x = 2; x < 10; x++) {
				for (int y = 50; y < 60; y++) {
					loc = modifier.create(LOCATION_TABLE_NAME);
					lon = x + "";
					lat = y + "";
	/*				if (x == 4 && y== 52) {
						lon = x + ".73";
						lat = y + ".37";

					}   */
					loc.setField("name", lon + "," + lat);
					loc.setField("lon", lon);
					loc.setField("lat", lat);
					loc.setField("time", Sys.now() + "");
					pt = "SRID=4326;POINT(" + lon + " " + lat + ")";
					loc.setField("point", pt);
					loc.setField("pointnoidx", pt);
					modifier.insert(loc);
					cnt++;
				}

			}

			info("done: " + cnt + " recs");
		} catch (Throwable t) {
			failTest("fillGLocation: ", t);
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


	protected int insertTestLinestring() throws Exception {
		String line = "LINESTRING(1 2 3,5 6 7,9 10 11)";
		String lineWGS = "SRID=4326;" + line;
		String lineRD = "SRID=28992;" + line;
		// Simple: create and update and commit.
		Record record = getModifier().create(ROUTE_TABLE_NAME);
		assertNotNull("table.create()", record);

		record.setStringField("name", "track1");
		record.setField("route_geom", lineWGS);
		record.setField("rd_route_geom", lineRD);
		getModifier().insert(record);
		return record.getId();
	}

}
