package org.walkandplay.server.logic;

import java.util.Map;

import nl.justobjects.jox.dom.JXElement;

import org.geotracing.handler.QueryLogic;
import org.keyworx.utopia.core.data.UtopiaException;

public class WPQueryLogic extends QueryLogic {

	public JXElement doQuery(String aQueryName, Map theParms) {
		if("q-bla".equals(aQueryName)) {
			String tables = "diwi_route,diwi_poi";
			String fields = "diwi_route.id,diwi_route.name,diwi_route.description";
			String where = "diwi_route.type=" + getParameter(theParms,"type", "0");
			where = null;
			String relations = null;
			String postCond = null;
			try {
				return QueryLogic.queryStoreReq(getOase(), tables, fields, where, relations, postCond);
			} catch (UtopiaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return super.doQuery(aQueryName, theParms);
	}

}
