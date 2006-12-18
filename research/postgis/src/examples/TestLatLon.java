package examples;

import org.postgis.PGgeometryLW;

import java.sql.*;

/*
 * TestLatLon.java
 *
  * test using lat/lon with SRID for Google Maps.
 * $Id$
 */

public class TestLatLon {

	public static void main(String[] args) {

		String dburl = "jdbc:postgresql://localhost:5432/gistest";
		// String dburl = "jdbc:postgresql_lwgis://localhost:5432/gistest";
		String driverClass = "org.postgresql.Driver";
		// String driverClass = "org.postgis.DriverWrapperLW";
		String dbuser = "oaseuser";
		String dbpass = "oase";
		String dropSQL = "drop table latlon_test";
		String createTableSQL = "create table latlon_test (id int4)";
		String addPointGeom = "; SELECT AddGeometryColumn('gistest', 'latlon_test','point', 4326,'POINT',2)";
		String addLineGeom = "; SELECT AddGeometryColumn('gistest', 'latlon_test','line',4326,'LINESTRING',2)";
		String dropGeom = "SELECT DropGeometryColumn('gistest', 'latlon_test','point')";

		String createSQL = createTableSQL + addPointGeom + addLineGeom;
		String insertSQL = "INSERT INTO latlon_test values (10, GeomFromText('POINT (4.92 52.35)', 4326), ";
		insertSQL += " GeomFromText('LINESTRING(4.92 52.35,4.93 52.35,4.92 52.36,4.96 52.70)', 4326) )";

		try {

			pl("Creating JDBC connection...");
			Class.forName(driverClass);
			Connection conn = DriverManager.getConnection(dburl, dbuser, dbpass);
			pl("connection class==" + conn.getClass().getName());

			Statement s = conn.createStatement();
			pl("Creating table with geometric types...");
			try {
				// table might exist
				// s.execute(dropGeom);
				s.execute(dropSQL);
			} catch (Exception e) {
				pl("Error dropping table: " + e.getMessage());
			}

			pl("Create table...");
			s.execute(createSQL);

			pl("Adding geometric types...");
			//s.execute(addPointGeom);
			//s.execute(addLineGeom);

			pl("Inserting rec...");
			s.execute(insertSQL);

			// s = conn.createStatement();
			pl("Querying table...");
			// ResultSet r = s.executeQuery("select asText(geom),id from " + dbtable);
			ResultSet r = s.executeQuery("select point, line from latlon_test");
			ResultSetMetaData metaData = r.getMetaData();
			int columnCount = metaData.getColumnCount();
			while (r.next()) {
				for (int i = 1; i < columnCount + 1; i++) {
					Object obj = r.getObject(i);
					if (obj instanceof org.postgis.PGgeometryLW) {
						p(((PGgeometryLW) obj).getGeometry().getClass().toString() + " ");
					}
					p(metaData.getColumnName(i) + "=" + obj.toString() + " [" + obj.getClass().getName() + "], ");
				}
				pl("");
			}
			r.close();
			s.close();
			conn.close();
		} catch (Exception e) {
			System.err.println("Aborted due to error:");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void pl(String s) {
		System.out.println(s);
	}

	public static void p(String s) {
		System.out.print(s);
	}
}
