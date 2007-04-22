package nl.diwi.logic;

import java.util.Vector;

import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;

import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.postgis.Point;

public class NavigationLogic implements Constants{
	
	private Oase oase;

	public NavigationLogic(Oase oase) {
		this.oase = oase;
	}

	
	public Vector checkPoint(Point point, int personId) throws UtopiaException {
		Vector result = new Vector();
		
		return checkPoiHits(point); 
		/*
		if(getActiveRoute(personId) != null) {
			//Record awayFromRouteEvent = checkProximity(activeRoute, point);			
			
		} else {
			//struinen
			//Record [] hitPois = checkPois(point);		
		}
		
		if(isUserContentEnabled(personId)) {
			//Record [] hitUgc = checkUGContent(ugc);
		}
		
	*/
		//return result;
	}

	public void enableUserContent(int personId) throws UtopiaException {
	
	}

	public void disableUserContent(int personId) throws UtopiaException {
		
	}

	public boolean isUserContentEnabled(int personId) throws UtopiaException {
		return false;
	}
	
	public void deactivateRoute(int personId) throws UtopiaException {
		try {
			//Find the person
			Record person = oase.getFinder().read(personId);
			//Find the 'active' route.
			Record activeRoute = getActiveRoute(personId);
			
			if(activeRoute == null) {
				throw new UtopiaException("No active route set!");
			}
			//Unrelate
			oase.getRelater().unrelate(person, activeRoute);
		} catch (OaseException oe) {
			throw new UtopiaException("Cannot set active route", oe);			
		}
	}

	public Record getActiveRoute(int personId) throws UtopiaException {
		try {
			//Find the person
			Record person = oase.getFinder().read(personId);
			Record [] activeRoute = oase.getRelater().getRelated(person, null, ACTIVE_TAG);
			
			if(activeRoute.length == 0) {
				return null;
			} else {
				return activeRoute[0];
			}
		} catch (OaseException oe) {
			throw new UtopiaException("Cannot set active route", oe);			
		}
		
	}
		
	public void activateRoute(int routeId, int personId) throws UtopiaException {
		try {
			//Find the person
			Record person = oase.getFinder().read(personId);
			//Find the Route
			Record route = oase.getFinder().read(routeId);
			
			//If an active route is allready set, deactivate first
			if(getActiveRoute(personId) != null) {
				deactivateRoute(personId);
			}
			
			//Relate route to person as active route
			oase.getRelater().relate(person, route, ACTIVE_TAG);
		} catch (OaseException oe) {
			throw new UtopiaException("Cannot set active route", oe);			
		}
	}

	private Vector checkPoiHits(Point point) throws UtopiaException {
		Vector result = new Vector();
		try {
			String queryString = "select id, distance_sphere(GeomFromEWKT('"+ point +"') , point) as distance from " + POI_TABLE + " where distance_sphere(GeomFromEWKT('"+ point +"') , point) < " + HIT_DISTANCE;
			Record [] poiHits = oase.getFinder().freeQuery(queryString);  
			
			for(int i = 0; i < poiHits.length; i++) {
				JXElement hit = new JXElement(POI_HIT_ELM);
				hit.setAttr(ID_FIELD, poiHits[i].getIntField(ID_FIELD));
				hit.setAttr(DISTANCE_ATTR, poiHits[i].getField(DISTANCE_FIELD).toString());
				result.add(hit);
			}
		} catch (OaseException oe) {
			throw new UtopiaException("could not determine distance", oe);			
		}
		
		return result;
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
