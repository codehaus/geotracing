// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.server;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.api.OaseException;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.amuse.core.Protocol;

/**
 * Generic QueryHandler.
 *
 * Allows doing any query. TODO: if stable port back into KWX
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class QueryHandler extends DefaultHandler {

	public final static String QUERY_STORE_SERVICE = "query-store";
	public final static String ATTR_TABLES = "tables";
	public final static String ATTR_FIELDS = "fields";
	public final static String ATTR_RELATIONS = "relations";
	public final static String ATTR_POST_COND = "postcond";

	/** Clause template for relation queries. */
	 public static String IS_RELATED =
			 "( REL_ALIAS.rec1 = TABLE1.id AND REL_ALIAS.rec2 = TABLE2.id ) ";


	/** Clause template for tagged relation queries. */
	public static final String IS_RELATED_WITH_TAG =
			"(" + IS_RELATED +
			" AND REL_ALIAS.tag = 'TAG' )";

	/** To separate multiple relation specs. */
	public static final String REL_SPEC_SEPARATOR = ";";

	public static final String REL_ALIAS_BASE = "rel";

	/**
	 * Processes the Client Request.
	 *
	 * @param anUtopiaRequest A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @exception UtopiaException Standard Utopia exception
	 */
	public UtopiaResponse processRequest(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		Log log = Logging.getLog(anUtopiaRequest);

		// Get the service name for the request
		String service = anUtopiaRequest.getServiceName();
		log.trace("Handling request for service=" + service);

		JXElement response = null;
		try {
			if (service.equals(QUERY_STORE_SERVICE)) {
				response = queryStoreReq(anUtopiaRequest.getUtopiaSession().getContext().getOase(), anUtopiaRequest.getRequestCommand());
			} else {
				log.warn("Unknown service " + service);
				response = createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
			}
		} catch (UtopiaException ue) {
			log.warn("Negative response for service=" + service);
			response = createNegativeResponse(service, ue.getErrorCode(), "Error in request: " + ue.getMessage());
		} catch (Throwable t) {
			log.error("Unexpected error in service : " + service, t);
			response = createNegativeResponse(service, ErrorCode.__6005_Unexpected_error, "Unexpected error in request");
		}

		// Always return a response
		log.trace("Handled service=" + service + " response=" + response.getTag());
		return new UtopiaResponse(response);

	}

	/**
	 * Executes an almost full SQL query.
	 *
	 * required attrs: tables (one or more tables) <br/>
	 * optional attr: fields <br/>
	 * optional attr: relations (one or more relations, e.g. relations="base_medium,g_location,[tag]") <br/>
	 * optional attr: postcond (SQL postfix e.g. ORDER BY, LIMIT etc)<br/>
	 * optional text: a WHERE clause (without WHERE)<br/>
	 * <p>
	 * Example request/response format:
	 * </p>
	 * <code>
	 * &lt;query-store-req
	 * tables="base_medium,g_location"
	 * fields="base_medium.id,base_medium.name,base_medium.kind,..."
	 * postcond="ORDER BY base_medium.creationdate DESC"&gt;
	 *   	 base_medium.kind = 'image' AND g.location.lon > 4.787 AND g.location.lon < 5.123 ...
	 * lt;query-store-req&gt;
	 * </code>
	 * @param oase  Oase session
	 * @param aRequest  the request
	 * @return A UtopiaResponse.
	 * @exception UtopiaException Standard Utopia exception
	 */
	static public JXElement queryStoreReq(Oase oase, JXElement aRequest) throws UtopiaException {
		// String tables, String fields, String constraints, String orderBy, String directions
		String tables = aRequest.getAttr(ATTR_TABLES, null);
		String fields = aRequest.getAttr(ATTR_FIELDS, null);
		String relations = aRequest.getAttr(ATTR_RELATIONS, null);
		String postCond = aRequest.getAttr(ATTR_POST_COND, null);

		// WHERE contrains as optional body text
		String where = aRequest.getText();
		if (where != null && where.trim().length() == 0) {
			where = null;
		}
		return queryStoreReq(oase, tables, fields, where, relations, postCond);

	}


	/**
	 * Executes an full sql query.
	 *
	 * Static method to allow usage from different contexts, like REST requests.
	 *
	 * @param oase Oase session object
	 * @param tables one or more comma-separated tables
	 * @param fields one or more comma-separated fields
	 * @param where the WHERE clause (without WHERE)
	 * @param relations one or more relations e.g. utopia_person,utopia_medium
	 * @param postCond query post fix string (ORDER BY, LIMIT etc)
	 * @return XML &lt;query-store-rsp /&gt; response
	 * @exception UtopiaException Standard Utopia exception
	 */
	static public JXElement queryStoreReq(Oase oase, String tables, String fields, String where, String relations, String postCond) throws UtopiaException {
		JXElement rsp = Protocol.createResponse(QUERY_STORE_SERVICE);
		rsp.setAttr(ATTR_TABLES, tables);
		rsp.setAttr(ATTR_FIELDS, fields);
		try {

			// Do query with Record[] result
			Record[] result = queryStore(oase, tables, fields, where, relations, postCond);

			// Convert Record[] to XML
			JXElement nextRecord = null;
			for (int i = 0; i < result.length; i++) {
				nextRecord = result[i].toXML();

				// Fix multi-table query (TODO: move to Oase)
				nextRecord.removeAttr("table");
				if (tables.indexOf(",") > 0) {
					// Multi table query: remove id attr
					if (nextRecord.hasAttr("id")) {
						nextRecord.removeAttr("id");
					}
				} else {
					// Single table query: make id attr
					nextRecord.removeChildByTag("id");
				}

				rsp.addChild(nextRecord);
			}
		} catch (OaseException oe) {
			rsp = Protocol.createNegativeResponse(QUERY_STORE_SERVICE, ErrorCode.__6006_database_irregularity_error, oe.getMessage());
			// throw new UtopiaException("DB error " + oe, oe, ErrorCode.__6005_Unexpected_error);
		} catch (Throwable t) {
			rsp = Protocol.createNegativeResponse(QUERY_STORE_SERVICE, ErrorCode.__6005_Unexpected_error, t.getMessage());
		}

		return rsp;
	}

	/**
	 * Executes an full Oase sql query.
	 *
	 * Static method to allow usage from different contexts, like REST requests.
	 *
	 * @param oase Oase session object
	 * @param tables one or more comma-separated tables
	 * @param fields one or more comma-separated fields
	 * @param where the WHERE clause (without WHERE)
	 * @param relations one or more relations e.g. utopia_person,utopia_medium
	 * @param postCond query post fix string (ORDER BY, LIMIT etc)
	 * @return Record[] size >= 0
	 * @exception OaseException Standard Oase exception
	 */
	static public Record[] queryStore(Oase oase, String tables, String fields, String where, String relations, String postCond) throws OaseException {

		try {
			Record[] result = null;

			// Init query constraints
			String constraints = null;
			StringBuffer constraintBuf = new StringBuffer();
			if (where != null || relations != null) {
				constraintBuf.append("WHERE ");
			}

			// Null means all fields.
			if (fields == null) {
				fields = "*";
			}

			if (tables.indexOf(",") > 0) {
				// Prepare query store parms
				if (relations != null) {

					// Prepare relation clause
					String allRelClause = "";

					// Get all relation specs e.g. (person,medium;account,person)
					String relSpecs[] = relations.trim().split(REL_SPEC_SEPARATOR);
					for (int i = 0; i < relSpecs.length; i++) {
						String relSpec[] = relSpecs[i].trim().split(",");
						String[] orderedTables = oase.getRelater().getRelationTableOrder(relSpec[0].trim(), relSpec[1].trim());
						String table1 = orderedTables[0];
						String table2 = orderedTables[1];

						// Create alias name (required for multi-relation specs)
						String relAlias = REL_ALIAS_BASE + i;

						// Assume no tag
						String relClause = IS_RELATED;

						// Optional tag as e.g. {base_medium,utopia_person,image}
						if (relSpec.length == 3) {
							relClause = IS_RELATED_WITH_TAG.replaceAll("TAG", relSpec[2]);
						}

						// Replace relation alias
						relClause = relClause.replaceAll("REL_ALIAS", relAlias);

						// Replace related tables
						relClause = relClause.replaceAll("TABLE1", table1).replaceAll("TABLE2", table2);

						// Determine if multiple relations need to be AND-ed
						if (allRelClause.length() == 0) {
							allRelClause = relClause;
						} else {
							allRelClause += (" AND " + relClause);
						}

						// Extend FROM with relation table alias
						tables += ",oase_relation " + relAlias;
					}

					// Make final WHERE dependent if there was a WHERE clause
					constraintBuf.append(where == null ? allRelClause : (where + " AND " + allRelClause));
				}

				if (postCond != null) {
					constraintBuf.append(" " + postCond);
				}

				constraints = (constraintBuf.length() > 0) ? constraintBuf.toString() : null;

				// Let oase do multi-table query
				result = oase.getFinder().queryStore(tables, fields, constraints);
			} else {
				// Query on single table
				if (where != null) {
					constraintBuf.append(where);
				}
				if (postCond != null) {
					constraintBuf.append(" " + postCond);
				}

				constraints = (constraintBuf.length() > 0) ? constraintBuf.toString() : null;

				// Simple one-table query
				result = oase.getFinder().queryTable(tables, constraints);
			}
			return result;
		} catch (OaseException oe) {
			throw oe;
		} catch (Throwable t) {
			throw new OaseException("Unexpected error in queryStore2()", t);
		}

	}

	/**
	 * Throw exception when attribute empty or not present.
	 */
	static public void throwOnMissingAttr(String aName, String aValue) throws UtopiaException {
		if (aValue == null || aValue.length() == 0) {
			throw new UtopiaException("Missing name=" + aName + " value=" + aValue, ErrorCode.__6002_Required_attribute_missing);
		}
	}
}

/*
* $Log: QueryHandler.java,v $
* Revision 1.8  2005/10/21 13:09:50  just
* basic bomb protocol
*
* Revision 1.7  2005/10/18 12:54:44  just
* *** empty log message ***
*
* Revision 1.6  2005/10/18 07:38:00  just
* *** empty log message ***
*
*
*/


