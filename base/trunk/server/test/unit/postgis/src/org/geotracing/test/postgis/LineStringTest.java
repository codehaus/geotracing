package org.geotracing.test.postgis;

import org.keyworx.common.util.Sys;
import org.keyworx.oase.api.Modifier;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.store.record.RecordImpl;
import org.postgis.Point;
import org.postgis.PGgeometryLW;
import org.postgis.LineString;
import org.postgis.PGgeometry;

/**
 * Test class Oase-PostGIS LINESTRING type.
 *
 * @author Just van den Broecke
 */
public class LineStringTest extends PGTestCase {
	public static final String SRID_LINE_1 = "SRID=4326;LINESTRING(1 2 3 4,5 6 7 8)";
	public static final String SRID_LINE_2 = "SRID=4326;LINESTRING(1 2 3 4,5 6 7 8,9 10 11 12)";
	public static final String SRID_POINT_2 = "SRID=4326;POINT(9 10 11 12)";
	public static final String SRID_POINT_3 = "SRID=4326;POINT(-4.92 52.35)";
	public static final String SRID_POINT_4 = "SRID=4326;POINT(-4.98 52.37)";

	public LineStringTest() {
		super("LineStringTest");
	}

	public void testCreateLine() {
		try {

			// Simple: create and update and commit.
			Record record = getModifier().create(TRACK_TABLE_NAME);
			assertNotNull("table.create()", record);

			record.setStringField("name", "track1");
			record.setField("line", SRID_LINE_1);
			getModifier().insert(record);

			int id = record.getId();
			record = getFinder().read(id);

			String result = record.getObjectField("line").toString();
			assertEquals("result not equal to " + SRID_LINE_1 + " but " + result, result, SRID_LINE_1);

		} catch (Throwable t) {
			failTest("testCreateLine: ", t);
		}
	}

	public void testImportRoute() {
		try {
		   String gpxPath = "../data/ringvaart.gpx";
			LineString lineString = Util.GPX2LineString(gpxPath);
			PGgeometryLW geom = new PGgeometryLW(lineString);

			// Simple: create and update and commit.
			Record record = getModifier().create(ROUTE_TABLE_NAME);
			assertNotNull("table.create()", record);

			record.setStringField("name", "ringvaart");
			record.setObjectField("route_geom", geom);
			getModifier().insert(record);


		} catch (Throwable t) {
			failTest("testImportRoute: ", t);
		}
	}

		public void testAppendLine() {
		try {

			// Simple: create and update and commit.
			Record record = getModifier().create(TRACK_TABLE_NAME);
			assertNotNull("table.create()", record);

			record.setStringField("name", "track1");
			record.setField("line", SRID_LINE_1);
			getModifier().insert(record);

			int id = record.getId();
			record = getFinder().read(id);

			// UPDATE stock SET stock = stock + 1 WHERE isbn = '0385121679';
			// String update = "UPDATE g_track SET line  = GeomFromText(SELECT AddPoint(line, GeomFromText('POINT(9 10 11 12)',4326)) as line FROM ( SELECT line FROM g_track WHERE id=" + id + ") foo));";


			// AddPoint(linestring, point, [<position>])
			String select = "SELECT AddPoint(line, GeomFromText('POINT(9 10 11 12)',4326)) as line FROM ( SELECT line FROM g_track WHERE id=" + id + ") foo;";
			Record[] records = getFinder().freeQuery(select);
			assertEquals("result not equal ", 1, records.length);
			LineString line = (LineString) ((PGgeometryLW) record.getObjectField("line")).getGeometry();

			// record.setObjectField("line", records[0].getField("line"));

			// Update g_track set line=AddPoint(line, GeomFromText('POINT(4.92 52.35)',4326)) where id=101;
			record.setStringField("line", "AddPoint(line, GeomFromText('POINT(9 10 11 12)',4326))");
			startTimer();
			getModifier().update(record);
			showTimeDelta("AddPoint");
			record = getFinder().read(id);
			String result = record.getObjectField("line").toString();
			assertEquals("result not equal to " + SRID_LINE_2 + " but " + result, result, SRID_LINE_2);

		} catch (Throwable t) {
			failTest("testCreateLine: ", t);
		}
	}
}
