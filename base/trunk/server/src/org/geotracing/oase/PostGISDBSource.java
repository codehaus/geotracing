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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * JDBC extensions for PostGIS spatial columns.
 * <p/>
 * <h3>Purpose</h3>
 * <p/>
 * This extension allows you to configure spatial columns in an Oase tabledef.
 * For example you can add a 2 dimensional POINT column with SRID 4326 like this:
 * &lt;field name="point" type="OBJECT" class="org.postgis.PGgeometryLW" spec="POINT,2,4326,INDEX_GIST"/&gt;
 * <p/>
 * <p/>
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
	 * Add column to existing table.
	 */
	protected void addColumn(String aTableName, String aFieldName) throws OaseException {
		TableDef tableDef = getStoreContext().getStoreContextConfig().getTableDef(aTableName);
		FieldDef fieldDef = tableDef.getFieldDef(aFieldName);

		// Do the standard implementation when not dealing with Object (in our case spatial) columns
		if (fieldDef.getType() != FieldDef.TYPE_OBJECT) {
			super.addColumn(aTableName, aFieldName);
			return;
		}

		// Add PostGIS spatial column based on field def.
		String sql = getAddSpatialColumnSQL(tableDef, fieldDef);
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();

			// Do the add.
			statement.execute(sql);

			Log.info("Added spatial column " + aFieldName + " to " + aTableName + " sql=" + sql);
		} catch (Throwable e) {
			throw new OaseException("Failed addColumn() for spatial: " + sql, e);
		} finally {
			close(statement, connection);
		}
	}

	/**
	 * Get SQL string for creating table.
	 * <p/>
	 * PostGISDBsource uses alter table syntax.
	 */
	protected String getCreateTableSQL(TableDef aTableDef) throws OaseException {
		StringBuffer sb = new StringBuffer(64);

		sb.append("CREATE TABLE ");
		sb.append(aTableDef.getName());
		FieldDef[] fieldDefs = aTableDef.getFieldDefs();
		FieldDef nextFieldDef = null;
		// CREATE TABLE authors (id integer PRIMARY KEY)
		if (!fieldDefs[0].getName().equals("id")) {
			throw new OaseException("First field must be id (primary key)");
		}

		// Create table with just id
		sb.append(" ( id integer PRIMARY KEY); ");

		// Add all other fields using ADD COLUMN syntax
		for (int i = 1; i < fieldDefs.length; i++) {
			nextFieldDef = fieldDefs[i];
			// Only define fields that are mapped to DB_SOURCE
			if (!TypeDef.getSourceId(nextFieldDef.getType()).equals(StoreContextConfig.SOURCE_DB)) {
				continue;
			}

			// Append ADD COLUMN statements: special case for spatial colums
			if (nextFieldDef.getType() == FieldDef.TYPE_OBJECT) {
				// Spatial column
				sb.append(getAddSpatialColumnSQL(aTableDef, fieldDefs[i]));
			} else {
				// Other "normal" column
				sb.append("ALTER TABLE " + aTableDef.getName() + " ADD COLUMN ");

				// Add <name> <type> pair
				sb.append(nextFieldDef.getName());
				sb.append(' ');

				String sqlType = getSQLType(TypeDef.getStorageType(nextFieldDef.getType()));
				sb.append(sqlType);

				// Optional (size) constraint.
				if (nextFieldDef.getSize() != -1) {
					sb.append("(" + nextFieldDef.getSize() + ")");
				}

				// Optional required
				if (nextFieldDef.isRequired()) {
					sb.append(" NOT NULL");
				}
				sb.append("; ");
			}
		}

		// return final result.
		return sb.toString();
	}

	/**
	 * Update Record.
	 *
	 * @throws org.keyworx.oase.api.OaseException
	 *          Standard exception.
	 */
	public void update(Record aRecord) throws OaseException {

		// Create the SQL SET part of prepared statement
		Iterator changedFields = ((RecordImpl) aRecord).getChangedFields();
		String fieldSQL = null;
		TableDef tableDef = aRecord.getTableDef();
		String nextFieldName = null;
		int nextFieldType;
		Object nextFieldValue;
		while (changedFields.hasNext()) {
			nextFieldName = (String) changedFields.next();
			nextFieldType = tableDef.getFieldDef(nextFieldName).getType();

			// Skip non-db fields.
			if (!TypeDef.getSourceId(nextFieldType).equals(StoreContextConfig.SOURCE_DB)) {
				continue;
			}

			// Geometry types may use OpenGIS string values
			// these cannot be sent as String in a prepared statement but must
			// be sent unquoted.
			if (nextFieldType == FieldDef.TYPE_OBJECT) {
				nextFieldValue = aRecord.getField(nextFieldName);
				if (nextFieldValue != null && nextFieldValue instanceof String) {
					if (fieldSQL == null) {
						fieldSQL = nextFieldName + "=" + nextFieldValue;
					} else {
						fieldSQL += ", " + nextFieldName + "=" + nextFieldValue;
					}
					continue;
				}
			}

			// Add the field name using smart ',' mapping.
			if (fieldSQL == null) {
				fieldSQL = nextFieldName + "=?";
			} else {
				fieldSQL += ", " + nextFieldName + "=?";
			}
		}

		// Could be only non-db fields so do nothing in that case.
		if (fieldSQL == null) {
			return;
		}

		// Create the prepared statement and execute it.
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			String sql = "UPDATE " + aRecord.getTableDef().getName() + " SET " + fieldSQL + " WHERE id = " + aRecord.getId();

			connection = getConnection();
			statement = connection.prepareStatement(sql);

			// Fill in the values of the SQL SET part of prepared statement
			changedFields = ((RecordImpl) aRecord).getChangedFields();
			int nextFieldNuimber = 1;
			while (changedFields.hasNext()) {
				nextFieldName = (String) changedFields.next();
				nextFieldType = tableDef.getFieldDef(nextFieldName).getType();

				// Skip non-db fields.
				if (!TypeDef.getSourceId(nextFieldType).equals(StoreContextConfig.SOURCE_DB)) {
					continue;
				}

				// Skip for Geometry object types set as string
				if (nextFieldType == FieldDef.TYPE_OBJECT) {
					nextFieldValue = aRecord.getField(nextFieldName);
					if (nextFieldValue != null && nextFieldValue instanceof String) {
						continue;
					}
				}

				// Set the field value.
				statement.setObject(nextFieldNuimber++, aRecord.getField(nextFieldName));
			}

			// Perform the update.
			statement.executeUpdate();

		} catch (Throwable e) {
			throw new OaseException("PostGISDBSource Record update failed for record id=" + aRecord.getId(), e);
		} finally {
			close(statement, connection);
		}
	}

	/**
	 * Extended DBSources may provide their specific DB post-creation constraints.
	 protected String[] getPostCreateTableSQL(TableDef aTableDef) throws OaseException {
	 // For PostGIS: select spatial columns (defined as type OBJECT)

	 List result = new ArrayList(1);
	 FieldDef[] fieldDefs = aTableDef.getFieldDefs();

	 // Check all fielddefs for (spatial) columns.
	 for (int i = 0; i < fieldDefs.length; i++) {
	 // Handle spatial object fields
	 if (fieldDefs[i].getType() == FieldDef.TYPE_OBJECT) {
	 result.add(getAddSpatialColumnSQL(aTableDef, fieldDefs[i]));
	 }
	 }
	 return (String[]) result.toArray(new String[result.size()]);
	 }
	 */

	/**
	 * Return SQL string to create spatial column in PostGIS.
	 */
	protected String getAddSpatialColumnSQL(TableDef aTableDef, FieldDef aFieldDef) {
		// PostGIS syntax examples:
		// SELECT AddGeometryColumn('gistest', 'latlon_test','point', 4326,'POINT',2)";
		// SELECT AddGeometryColumn('gistest', 'latlon_test','line',4326,'LINESTRING',2)";

		// Oase fielddef spec attr has <TYPE>,<DIMENSION>,<SRID>,[INDEX_GIST]"
		String spec = aFieldDef.getConfig().getAttr("spec");
		String result = null;
		String fieldName = aFieldDef.getName();
		String tableName = aTableDef.getName();
		if (spec != null && spec.length() > 0) {
			// Add spatial column using OGC OpenGIS syntax
			String[] specParms = spec.split(",");
			result = "SELECT AddGeometryColumn('" + dbParms.db + "', '" + tableName + "', '" + fieldName + "', " + specParms[2] + ", '" + specParms[0] + "', " + specParms[1] + "); ";

			// Add spatial index if specified
			if (specParms.length > 3) {
				// See http://postgis.refractions.net/docs/ch04.html#id2838748
				// CREATE INDEX [indexname] ON [tablename] USING GIST ( [geometryfield] GIST_GEOMETRY_OPS );
				String indexType = specParms[3];
				if (indexType.equals(INDEX_GIST)) {
					result += "CREATE INDEX idx_" + tableName + fieldName + " ON " + tableName + " USING GIST ( " + fieldName + " GIST_GEOMETRY_OPS ); ";
				}
			}
		}
		return result;
	}
}

/*
 * $Log: PostgreSQLDBSource.java,v $
 *
 */
