// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.handler;

import nl.justobjects.jox.dom.JXAttributeTable;
import nl.justobjects.jox.dom.JXElement;
import org.keyworx.amuse.core.Protocol;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Generic QueryHandler.
 * <p/>
 * Allows doing any query.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class QueryHandler extends DefaultHandler {

	public final static String QUERY_STORE_SERVICE = "query-store";

	/* public final static String ATTR_TABLES = "tables";
		public final static String ATTR_FIELDS = "fields";
		public final static String ATTR_RELATIONS = "relations";
		public final static String ATTR_POST_COND = "postcond";  */
	public final static String ATTR_CMD = "cmd";

	private Log log = Logging.getLog("QueryHandler");
	private QueryLogic queryLogic;

	/**
	 * Processes the Client Request.
	 *
	 * @param anUtopiaRequest A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws UtopiaException Standard Utopia exception
	 */
	public UtopiaResponse processRequest(UtopiaRequest anUtopiaRequest) throws UtopiaException {

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
	 * <p/>
	 * required attrs: tables (one or more tables) <br/>
	 * optional attr: fields <br/>
	 * optional attr: relations (one or more relations, e.g. relations="base_medium,g_location,[tag]") <br/>
	 * optional attr: postcond (SQL postfix e.g. ORDER BY, LIMIT etc)<br/>
	 * optional text: a WHERE clause (without WHERE)<br/>
	 * <p/>
	 * Example request/response format:
	 * </p>
	 * <code>
	 * &lt;query-store-req
	 * tables="base_medium,g_location"
	 * fields="base_medium.id,base_medium.name,base_medium.kind,..."
	 * postcond="ORDER BY base_medium.creationdate DESC"&gt;
	 * base_medium.kind = 'image' AND g.location.lon > 4.787 AND g.location.lon < 5.123 ...
	 * lt;query-store-req&gt;
	 * </code>
	 *
	 * @param oase	 Oase session
	 * @param aRequest the request
	 * @return A UtopiaResponse.
	 * @throws UtopiaException Standard Utopia exception
	 */
	public JXElement queryStoreReq(Oase oase, JXElement aRequest) throws UtopiaException {
		// String tables, String fields, String constraints, String orderBy, String directions
		/*String tables = aRequest.getAttr(ATTR_TABLES, null);
		String fields = aRequest.getAttr(ATTR_FIELDS, null);
		String relations = aRequest.getAttr(ATTR_RELATIONS, null);
		String postCond = aRequest.getAttr(ATTR_POST_COND, null);

		// WHERE contrains as optional body text
		String where = aRequest.getText();
		if (where != null && where.trim().length() == 0) {
			where = null;
		} */

		// The specific query name
		String command = aRequest.getAttr(ATTR_CMD);
		HandlerUtil.throwOnMissingAttr(ATTR_CMD, command);

		// Get query parameters from request and put in map
		JXAttributeTable attrs = aRequest.getAttrs();
		Map parms = new HashMap(attrs.size() - 1);
		Iterator iter = attrs.keys();
		String nextParm;
		while (iter.hasNext()) {
			nextParm = (String) iter.next();
			if (nextParm.equals(ATTR_CMD)) {
				continue;
			}
			parms.put(nextParm, attrs.get(nextParm));
		}

		// Execute query and return result
		JXElement result =  queryLogic.doQuery(command, parms);
		if (result.getTag().equals(QueryLogic.TAG_ERROR))  {
			// In case of error create negative response
			result = Protocol.createNegativeResponse(QUERY_STORE_SERVICE, result.getText());
		}
		return result;

	}

	/**
	 * Overridden to have a hook to do the initialisation and assign query logic class.
	 *
	 * @param aKey
	 * @param aValue
	 * @see org.keyworx.utopia.core.control.Handler#setProperty(java.lang.String,java.lang.String)
	 */
	public void setProperty(String aKey, String aValue) {
		if (aKey.equals("logic")) {
			try {
				// Create (possibly derived) QueryLogic
				log.info("Creating QueryLogic class=" + aValue);
				queryLogic = QueryLogic.create(aValue);
			}
			catch (Exception e) {
				log.error("Cannot instantiate QueryLogic class: " + aValue, e);
				throw new RuntimeException("Cannot instantiate QueryLogic class: " + aValue, e);
			}

		}
		super.setProperty(aKey, aValue);
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


