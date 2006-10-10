/********************************************************
 * Copyright (C)2002 - Waag Society - See license below *
 ********************************************************/

package org.geotracing.server.oase;

import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.TableDef;
import org.keyworx.oase.api.FieldDef;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.store.source.PostgreSQLDBSource;
import org.keyworx.oase.store.record.RecordImpl;
import org.keyworx.oase.config.TableDefImpl;
import org.keyworx.oase.config.TypeDef;
import org.keyworx.oase.config.StoreContextConfig;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Specific JDBC extensions for PostGIS.
 * <p/>
 * <h3>Purpose</h3>
 * <p/>
 * General explanation
 * <p/>
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

		for (int i=0; i < fieldDefs.length; i++) {
		   fieldDef = fieldDefs[i];
			if (fieldDef.getType() == FieldDef.TYPE_OBJECT) {
				spec = fieldDef.getConfig().getAttr("spec");
				fieldName = fieldDef.getName();
				if (spec != null && spec.length() > 0) {
					// Add spatial column using OGC OpenGIS syntax
					specParms = spec.split(",");
					result.add("SELECT AddGeometryColumn('" + dbName + "', '" + tableName + "', '" + fieldName +"', 4326, '" + specParms[0] + "', " + specParms[1] + ")");
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
