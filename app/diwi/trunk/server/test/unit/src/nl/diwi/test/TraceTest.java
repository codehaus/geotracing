package nl.diwi.test;

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
public class TraceTest extends DIWITestCase {
	public static final String SRID_LINE_1 = "SRID=28992;LINESTRING(1 2 3 4,5 6 7 8)";
	public static final String SRID_LINE_2 = "SRID=28992;LINESTRING(1 2 3 4,5 6 7 8,9 10 11 12)";
	public static final String SRID_POINT_2 = "SRID=28992;POINT(9 10 11 12)";
	public static final String SRID_POINT_3 = "SRID=28992;POINT(-4.92 52.35)";
	public static final String SRID_POINT_4 = "SRID=28992;POINT(-4.98 52.37)";

	public TraceTest() {
		super("LineStringTest");
	}

	public void testCreateLine() {
		try {

			// Simple: create and update and commit.
			Record record = getModifier().create(TRACE_TABLE_NAME);
			assertNotNull("table.create()", record);

			record.setStringField("name", "track1");
			record.setStringField("description", "track1 descr");
			record.setField("path", SRID_LINE_1);
			getModifier().insert(record);

			int id = record.getId();
			record = getFinder().read(id);

			String result = record.getObjectField("path").toString();
			assertEquals("result not equal to " + SRID_LINE_1 + " but " + result, result, SRID_LINE_1);

		} catch (Throwable t) {
			failTest("testCreateLine: ", t);
		}
	}

	public void testAppendLine() {
		try {

			// Simple: create and update and commit.
			Record record = getModifier().create(TRACE_TABLE_NAME);
			assertNotNull("table.create()", record);

			record.setStringField("name", "track2");
			record.setStringField("description", "track2 descr");
			record.setField("path", SRID_LINE_1);
			getModifier().insert(record);

			int id = record.getId();
			record = getFinder().read(id);

			// UPDATE stock SET stock = stock + 1 WHERE isbn = '0385121679';
			// String update = "UPDATE diwi_trace SET line  = GeomFromText(SELECT AddPoint(line, GeomFromText('POINT(9 10 11 12)',28992)) as line FROM ( SELECT line FROM diwi_trace WHERE id=" + id + ") foo));";

			// AddPoint(linestring, point, [<position>])
			String select = "SELECT AddPoint(path, GeomFromText('POINT(9 10 11 12)',28992)) as path FROM ( SELECT path FROM diwi_trace WHERE id=" + id + ") foo;";
			Record[] records = getFinder().freeQuery(select);
			assertEquals("result not equal ", 1, records.length);
			LineString path = (LineString) ((PGgeometryLW) record.getObjectField("path")).getGeometry();

			// record.setObjectField("line", records[0].getField("line"));

			// Update diwi_trace set line=AddPoint(line, GeomFromText('POINT(4.92 52.35)',28992)) where id=101;
			record.setStringField("path", "AddPoint(path, GeomFromText('POINT(9 10 11 12)',28992))");
			startTimer();
			getModifier().update(record);
			showTimeDelta("AddPoint");
			record = getFinder().read(id);
			String result = record.getObjectField("path").toString();
			assertEquals("result not equal to " + SRID_LINE_2 + " but " + result, result, SRID_LINE_2);

		} catch (Throwable t) {
			failTest("testAppendLine: ", t);
		}
	}
}
