package nl.diwi.logic;

import java.util.Map;

import nl.justobjects.jox.dom.JXElement;

import org.geotracing.handler.QueryLogic;
import org.keyworx.utopia.core.data.UtopiaException;

public class DIWIQueryLogic extends QueryLogic {
	
	public JXElement doQuery(String aQueryName, Map theParms) {
		JXElement result;

		try {
			if("q-diwi-routes".equals(aQueryName)) {
				return queryRoutes(theParms);
			}

			return super.doQuery(aQueryName, theParms);
		} catch (UtopiaException ue) {
			result = new JXElement(TAG_ERROR);
			result.setText("Unexpected Error during query " + ue);
			log.error("Unexpected Error during query", ue);			
		}
		
		return result;
	}

	private JXElement queryRoutes(Map theParms) throws UtopiaException {
		String tables = "diwi_route";
		String fields = "diwi_route.id,diwi_route.name,diwi_route.description";
		String where = "diwi_route.type=" + getParameter(theParms,"type", "0");
		where = null;
		String relations = null;
		String postCond = null;
		
		return QueryLogic.queryStoreReq(getOase(), tables, fields, where, relations, postCond);
	}
}
