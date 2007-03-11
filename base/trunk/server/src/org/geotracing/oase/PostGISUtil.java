// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.oase;

import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.TableDef;
import org.keyworx.oase.api.FieldDef;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.store.source.PostgreSQLDBSource;
import org.keyworx.oase.store.record.RecordImpl;
import org.keyworx.oase.config.TypeDef;
import org.keyworx.oase.config.StoreContextConfig;
import org.keyworx.oase.util.Log;
import org.postgis.PGgeometryLW;
import org.postgis.Point;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Utility functions for PostGIS spatial database.
 * <p/>
 * <h3>Purpose</h3>
 * <p/>
 * Provide utility functions to work with spatial columns and objects.
 * </p>
 * <h3>Examples</h3>
 * see {@link org.geotracing.handler.Location}
 * <p/>
 * <h3>Implementation</h3>
 * see {@link org.keyworx.oase.api.Record}
 * <p/>
 * <h3>Concurrency</h3>
 * not applicable
 *
 * @author Just van den Broecke
 * @version $Id$
 */

public class PostGISUtil {
	public static final int SRID_RD = 28992;
	public static final int SRID_WGS84 = 4326;
	public static final int SRID_DEFAULT = SRID_WGS84;

	/**
	 * Create PostGIS geometry object wrapped with Point.
	 */
	public static PGgeometryLW createPointGeom(int anSRID, double aLon, double aLat, double anEle, long aTime) {
		Point point = new Point(aLon, aLat, anEle);
		point.setSrid(anSRID);
		point.setM(aTime);
		return new PGgeometryLW(point);
	}
}

