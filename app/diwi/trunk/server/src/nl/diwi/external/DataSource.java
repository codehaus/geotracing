package nl.diwi.external;

import nl.diwi.logic.POILogic;
import nl.diwi.logic.RouteLogic;
import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.keyworx.amuse.core.Amuse;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.net.NetUtil;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/**
 * KICHDataSource provides data that is available from the KICH database like
 * fixed routes, points of interest etc.
 * <p/>
 * It is not our intention to copy the whole KICH database into our database but only what we need to service
 * currently active clients. Data is provided either from local database or remote system, the calling party
 * should not care about this.
 * <p/>
 * Maybe all these sync methods should have a point/radius argument. 'struinen' is defenitely range limited.
 * Maybe this goes for everything?
 *
 * @author hspeijer
 */

public class DataSource implements Constants {

	private Oase oase;
	private Log log = Logging.getLog("DataSource");

	public DataSource(Oase oase) {
		this.oase = oase;
	}

	/*// Purely to catch the scenario where a generated route includes a location which is not known yet 
	public Record syncLocation(String uri) {
		Record route = null;
		try {
			route = oase.getModifier().create(LOCATION_TABLE);
		} catch (OaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	
		return route;
	}*/

	// This request is required to get all points of interest for 'struinen'
	public void syncPOIs() throws UtopiaException {
		log.info("Sync Pois requested");
		try{
            POILogic logic = new POILogic(oase);
            String kichRESTUrl = Amuse.server.getPortal().getProperty(KICH_REST_URL);

            /*
            <pois>
                <poi>
                    <id></id>
                    <name><name>
                    <description></description>
                    <category></category>
                    <x></x>
                    <y></y>
                    <media>
                        <kich-uri><kich-uri>
                        <kich-uri><kich-uri>
                    </media>
                </poi>
            </pois>
             */

            /*JXElement poisElm = getXMLFromREST(kichRESTUrl);
            if (poisElm == null) throw new UtopiaException("No results from the KICH DB");*/
            // TODO: only done for test purposes

            
            URL url = new URL(Amuse.server.getPortal().getProperty(GENERATOR_URL) +  "?sync");
            log.info("url " + url);
            JXElement poisElm = new JXBuilder().build(url);
            if(poisElm!=null){
                log.info("poisElm " + new String(poisElm.toBytes(false), "UTF-8"));
                Vector poiElms = poisElm.getChildrenByTag(POI_ELM);
                for (int i = 0; i < poiElms.size(); i++) {
                    JXElement poiElm = (JXElement) poiElms.elementAt(i);
                    Record poi = logic.getRecord(poiElm.getChildText(ID_FIELD));
                    if (poi != null) {
                        logic.updateForSync(poi, poiElm);
                    } else {
                        logic.insertForSync(poiElm);
                    }
                }
            }
        }catch(Throwable t){
        	log.error("Could not sync", t);
            throw new UtopiaException(t);
        }
    }

	public void syncFixedRoutes() throws UtopiaException{
        try{
            RouteLogic logic = new RouteLogic(oase);
            /*
            String url = Amuse.server.getPortal().getProperty(KICH_REST_URL);
            url += "?action=getroutes";
            JXElement result = getXMLFromREST(url);
            if(result == null) throw new UtopiaException("No routes from KICH");*/

            // TODO: only done for test purposes
            JXElement result = new JXElement("routes");
            result.addChild(new JXBuilder().build(Amuse.server.getPortal().getProperty(GENERATOR_URL) + "/../testreponse/generateroute1.xml"));
            result.addChild(new JXBuilder().build(Amuse.server.getPortal().getProperty(GENERATOR_URL) + "/../testreponse/generateroute2.xml"));

            Vector routes = result.getChildrenByTag(ROUTE_ELM);
            for(int i=0;i<routes.size();i++){
                logic.insertRoute((JXElement)routes.elementAt(i));
            }
        }catch(Throwable t){

        }

	}

	// not needed all poi's are synced in one go

	/*public Vector syncStartLocations() {
		return new Vector();		
	}

	public Vector syncEndLocations() {
		return new Vector();		
	}*/

	/**
	 * <media>
	 * <medium>
	 * <naam></naam>
	 * <description></description>
	 * <uri></uri>
	 * </medium>
	 * </media>
	 *
	 * @return
	 */
	public JXElement getKICHMedia() {
		String kichRestUrl = Amuse.server.getPortal().getProperty(KICH_REST_URL);
		kichRestUrl += "?action=getmedia";
		return getXMLFromREST(kichRestUrl);
	}

	// creates, updates and deletes a poi in KICH
	public String cudPOI(String anAction, JXElement aPOIElement) throws UtopiaException {
		String kichPostUrl = Amuse.server.getPortal().getProperty(KICH_POST_URL);
		Properties postParams = new Properties();
		postParams.setProperty("action", anAction);
		postParams.setProperty("xml", aPOIElement.toEscapedString());
		//return postData(kichPostUrl, postParams);
		postData(kichPostUrl, postParams);
		// TODO: change this later - only for testing purposes
		return new String("1261265");
	}

	private String postData(String aPostUrl, Hashtable thePostParams) throws UtopiaException {
		HttpClient client = new HttpClient();
		//client.getParams().setParameter("http.useragent", "Test Client");
		BufferedReader br = null;
		PostMethod method = new PostMethod(aPostUrl);
		method.setParameter("http.useragent", "KICH Client");

		if (thePostParams != null) {
			Enumeration elements = thePostParams.keys();
			while (elements.hasMoreElements()) {
				String name = (String) elements.nextElement();
				String value = (String) thePostParams.get(name);
				method.addParameter(name, value);
			}
		}

		StringBuffer sb = new StringBuffer();
		try {
			int returnCode = client.executeMethod(method);
			if (returnCode != HttpStatus.SC_OK) {
				throw new UtopiaException("Posting to " + aPostUrl + " was unsuccesfull! - http status:" + returnCode);
			}

			br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
			String readLine;
			while (((readLine = br.readLine()) != null)) {
				sb.append(readLine);
			}

		} catch (Throwable t) {
			log.error(t.toString());
			throw new UtopiaException("Exception in posting data to " + aPostUrl + ": " + t.toString());
		} finally {
			method.releaseConnection();
			if (br != null) try {
				br.close();
			} catch (Exception fe) {
			}
		}

		return sb.toString();

	}

	private JXElement getXMLFromREST(String aRESTUrl) {
		try {
			return new JXBuilder().build(NetUtil.fetchURL(aRESTUrl));
		} catch (Throwable t) {
			log.error("Exception retrieving xml data from " + aRESTUrl, t);
			return null;
		}
	}


}
