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
		}else if("q-tourschedules-by-user".equals(aQueryName)) {
			String tables = "tourschedule";
			String fields = "tourschedule.id,tourschedule.name,tourschedule.description";
			String where = "tourschedule.type=" + getParameter(theParms,"type", "0");
			where = null;
			String relations = null;
			String postCond = null;
            JXElement rsp = new JXElement("tourschedule-getmylist-rsp");

            JXElement tourschedule1 = new JXElement("tourschedule");
            rsp.addChild(tourschedule1);
            tourschedule1.setAttr("id", "1");
            JXElement name = new JXElement("name");
            name.setText("Nieuwendijk pilot 1");
            JXElement description = new JXElement("description");
            description.setText("Media archeology pilot");
            tourschedule1.addChild(name);
            tourschedule1.addChild(description);

            JXElement tourschedule2 = new JXElement("tourschedule");
            rsp.addChild(tourschedule2);
            tourschedule2.setAttr("id", "2");
            name = new JXElement("name");
            name.setText("Nieuwendijk pilot 2");
            description = new JXElement("description");
            description.setText("Oral history pilot");
            tourschedule2.addChild(name);
            tourschedule2.addChild(description);
            
            return rsp;
            /*try {
				return QueryLogic.queryStoreReq(getOase(), tables, fields, where, relations, postCond);
            } catch (UtopiaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		return super.doQuery(aQueryName, theParms);
	}

}
