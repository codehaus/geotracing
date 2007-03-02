package nl.diwi.external;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;
import java.io.*;

import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;

import org.keyworx.utopia.core.util.Oase;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.server.ServerConfig;
import org.keyworx.common.net.NetUtil;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;

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
    private Log log = Logging.getLog("DataSource");

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

    /**
     * <media>
     *      <medium>
     *          <naam></naam>
     *          <description></description>
     *          <uri></uri>
     *      </medium>
     * </media>
     * @return
     */
    public JXElement getKICHMedia(){
        String kichrestUrl = ServerConfig.getProperty("kich.rest.url");
        kichrestUrl += "?action=getmedia";
        return getXMLFromREST(kichrestUrl);
    }

    // creates, updates and deletes a poi in KICH
    public void cudPOI(String anAction, JXElement aPOIElement){
        String kichPostUrl = ServerConfig.getProperty("kich.post.url");
        Properties postParams = new Properties();
        postParams.setProperty("action", anAction);
        postParams.setProperty("xml", aPOIElement.toEscapedString());
        postData(kichPostUrl, postParams);
    }

    private void postData(String aPostUrl, Hashtable thePostParams){
        HttpClient client = new HttpClient();
        //client.getParams().setParameter("http.useragent", "Test Client");
        BufferedReader br = null;
        PostMethod method = new PostMethod(aPostUrl);
        method.setParameter("http.useragent", "KICH Client");

        if(thePostParams!=null){
            Enumeration elements = thePostParams.keys();
            while (elements.hasMoreElements()){
                String name = (String)elements.nextElement();
                String value = (String)thePostParams.get(name);
                method.addParameter(name, value);
            }
        }

        try{
          int returnCode = client.executeMethod(method);
          if(returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
            log.error("The Post method is not implemented by this URI");
            // still consume the response body
            method.getResponseBodyAsString();
          } else {
            br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
            String readLine;
            while(((readLine = br.readLine()) != null)) {
              log.error(readLine);
          }
          }
        } catch (Exception e) {
          log.error(e.toString());
        } finally {
          method.releaseConnection();
          if(br != null) try { br.close(); } catch (Exception fe) {}
        }

    }

    private JXElement getXMLFromREST(String aRESTUrl){
        try{
            return new JXBuilder().build(NetUtil.fetchURL(aRESTUrl));
        }catch(Throwable t){
            log.error("Exception retrieving xml data from " + aRESTUrl, t);
            return null;
        }
    }

    
}
