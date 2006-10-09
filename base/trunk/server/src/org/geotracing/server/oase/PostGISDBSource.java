/********************************************************
 * Copyright (C)2002 - Waag Society - See license below *
 ********************************************************/

package org.geotracing.server.oase;

import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.TableDef;
import org.keyworx.oase.store.source.PostgreSQLDBSource;

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
		return null;
	}
}

/*
 * $Log: PostgreSQLDBSource.java,v $
 *
 */
