package nl.diwi.external;

import java.util.Vector;

import nl.diwi.util.Constants;

import org.keyworx.utopia.core.util.Oase;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;

/**
 * KICHDataSource provides data that is available from the KICH database like 
 * fixed routes, points of interest etc.
 * 
 * It is not our intention to copy the whole KICH database into our database but only what we need to service 
 * currently active clients. Data is provided either from local database or remote system, the calling party 
 * should not care about this.
 * 
 * Maybe all these sync methods should have a point/radius argument. 'struinen' is defenitely range limited. 
 * Maybe this goes for everything?
 * 
 * @author hspeijer
 *
 */

public class DataSource implements Constants {

	private Oase oase;

	public DataSource(Oase oase) {
		this.oase = oase;
	}
	
	// Purely to catch the scenario where a generated route includes a location which is not known yet 
	public Record syncLocation(String uri) {
		Record route = null;
		try {
			route = oase.getModifier().create(LOCATION_TABLE);
		} catch (OaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	
	    return route;
	}
	
	// This request is required to get all points of interest for 'struinen'
	public Vector syncLocations() {
		return new Vector();
	}
	
	public Vector syncFixedRoutes() {
		return new Vector();		
	}
	
	public Vector syncStartLocations() {
		return new Vector();		
	}

	public Vector syncEndLocations() {
		return new Vector();		
	}	
}
