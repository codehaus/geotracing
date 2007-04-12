package nl.diwi.logic;

import java.util.Vector;

import nl.justobjects.jox.dom.JXElement;

import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.postgis.Point;

public class NavigationLogic {

	private final static String ACTIVE_TAG = "active";
	
	private Oase oase;

	public NavigationLogic(Oase oase) {
		this.oase = oase;
	}

	public Vector checkPoint(Point point) {
		Vector result = new Vector();
/*		
		if(activeRoute != null) {
			Record awayFromRouteEvent = checkProximity(activeRoute, point);			
		} else {
			//struinen
			Record [] hitPois = checkPois(point);		
		}
		
		if(userContent) {
			Record [] hitUgc = checkUserGeneratedContent(ugc);
		}
		
*/		
		return result;
	}

	public void deactivateRoute(int personId) {
		//Find the person
		//Find the 'active' route.
		//Unrelate

	}

	public void activateRoute(int routeId, int personId) throws UtopiaException {
		try {
			//Find the person
			Record person = oase.getFinder().read(personId);
			//Find the Route
			Record route = oase.getFinder().read(routeId);
			//Relate route to person as active route
			oase.getRelater().relate(person, route, ACTIVE_TAG);
		} catch (OaseException oe) {
			throw new UtopiaException("Cannot set active route", oe);			
		}
	}

	public String getActiveMap(int personId) throws UtopiaException {
		try {
			//Find the person
			Record person = oase.getFinder().read(personId);
		} catch (OaseException oe) {
			throw new UtopiaException("Cannot set active route", oe);			
		}
		
		return null;
	}

}
