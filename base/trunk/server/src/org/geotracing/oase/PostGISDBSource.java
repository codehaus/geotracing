/********************************************************
 * Copyright (C)2002 - Waag Society - See license below *
 ********************************************************/

package org.geotracing.oase;

import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.TableDef;
import org.keyworx.oase.api.FieldDef;
import org.keyworx.oase.store.source.PostgreSQLDBSource;

import java.util.List;
import java.util.ArrayList;

/**
 * JDBC extensions for PostGIS spatial columns.
 * <p/>
 * <h3>Purpose</h3>
 * <p/>
 * This extension allows you to configure spatial columns in an Oase tabledef.
 * For example you can add a 2 dimensional POINT column with SRID 4326 like this:
 * &lt;field name="point" type="OBJECT" class="org.postgis.PGgeometryLW" spec="POINT,2,4326,INDEX_GIST"/&gt;
 * <p/>
 * <p>
 * This maps a type OBJECT to the PostGIS class org.postgis.PGgeometryLW and set a spatial index on it.
 * </p>
 * <h3>Examples</h3>
 * see {@link org.keyworx.oase.api.Record}
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

public class PostGISDBSource extends PostgreSQLDBSource {
	public static final String INDEX_GIST = "INDEX_GIST";

	/**
	 * Extended DBSources may provide their specific DB post-creation constraints.
	 */
	protected String[] getPostCreateTableSQL(TableDef aTableDef) throws OaseException {
		// For PostGIS: select spatial columns (defined as type OBJECT)

		// Syntax examples:
		// SELECT AddGeometryColumn('gistest', 'latlon_test','point', 4326,'POINT',2)";
		// SELECT AddGeometryColumn('gistest', 'latlon_test','line',4326,'LINESTRING',2)";
		List result = new ArrayList(1);
		FieldDef[] fieldDefs = aTableDef.getFieldDefs();
		FieldDef fieldDef;
		String spec, specParms[], fieldName;
		String dbName = dbParms.db;
		String tableName = aTableDef.getName();

		// Check all fielddefs for (spatial) columns.
		for (int i = 0; i < fieldDefs.length; i++) {
			fieldDef = fieldDefs[i];

			// Handle spatial object fields
			if (fieldDef.getType() == FieldDef.TYPE_OBJECT) {
				// spec has <TYPE>,<DIMENSION>,<SRID>,[INDEX_GIST]"
				spec = fieldDef.getConfig().getAttr("spec");

				fieldName = fieldDef.getName();
				if (spec != null && spec.length() > 0) {
					// Add spatial column using OGC OpenGIS syntax
					specParms = spec.split(",");
					result.add("SELECT AddGeometryColumn('" + dbName + "', '" + tableName + "', '" + fieldName + "', " + specParms[2] + ", '" + specParms[0] + "', " + specParms[1] + "); ");

					// Add spatial index if specified
					if (specParms.length > 3) {
						// See http://postgis.refractions.net/docs/ch04.html#id2838748
						// CREATE INDEX [indexname] ON [tablename] USING GIST ( [geometryfield] GIST_GEOMETRY_OPS );
						String indexType = specParms[3];
						if (indexType.equals(INDEX_GIST)) {
							result.add("CREATE INDEX idx_" + tableName + fieldName + " ON " + tableName + " USING GIST ( " + fieldName + " GIST_GEOMETRY_OPS ); ");
						}
					}
				}
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
	}
}

/*
 * $Log: PostgreSQLDBSource.java,v $
 *
 */
