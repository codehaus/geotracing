package org.geotracing.test.postgis;

import org.keyworx.oase.api.*;
import org.postgis.Point;
import org.postgis.PGgeometryLW;
import junit.framework.Assert;

/**
 * Test class Oase-PostGIS basics..
 *
 * @author Just van den Broecke
 */
public class SpatialQueryTest extends PGTestCase {
	public static final String EXTENT_POINT_1 = "SRID=4326;POINT(1, 2)";
	public static final String EXTENT_POINT_2 = "SRID=4326;POINT(2, 4)";

	public SpatialQueryTest() {
		super("SpatialQueryTest");
	}

	public void testExtent() {
		try {

			// Simple: create and update and commit.

			Record record = getModifier().create(SPATIAL_ONE_TABLE_NAME);
			record.setStringField("name", "p1");
			record.setField("point", EXTENT_POINT_1);
			getModifier().insert(record);

			record = getModifier().create(SPATIAL_ONE_TABLE_NAME);
			record.setStringField("name", "p2");
			record.setField("point", EXTENT_POINT_2);
			getModifier().insert(record);

		} catch (Throwable t) {
			failTest("testCreatePoint: ", t);
		}
	}

}
