package nl.diwi.external;

import nl.diwi.logic.POILogic;
import nl.diwi.logic.RouteLogic;
import nl.diwi.util.Constants;
import nl.diwi.util.NetConnection;
import nl.justobjects.jox.dom.JXElement;
import org.keyworx.amuse.core.Amuse;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;

import java.util.Properties;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: ronald
 * Date: Mar 29, 2007
 * Time: 12:07:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataSource implements Constants {
    private Oase oase;
        private Log log = Logging.getLog("DataSource");

        public DataSource(Oase oase) {
            this.oase = oase;
        }

        // This request is required to get all points of interest for 'struinen'
        public void syncPOIs() throws UtopiaException {
            try{
                POILogic logic = new POILogic(oase);
                String kichRESTUrl = Amuse.server.getPortal().getProperty(KICH_REST_URL);
                kichRESTUrl += "?request=GetPois";
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
                JXElement poisElm = NetConnection.getXMLFromREST(kichRESTUrl);
                if(poisElm!=null){
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
                throw new UtopiaException(t);
            }
        }

        public void syncFixedRoutes() throws UtopiaException{
            try{
                RouteLogic logic = new RouteLogic(oase);
                String routingServletUrl = Amuse.server.getPortal().getProperty(ROUTING_SERVLET_URL);
                String url = routingServletUrl + "?request=GetPredefinedRouteList";
                JXElement routesResultElm = NetConnection.getXMLFromREST(url);
                log.info("$$$$$$$$ " + routesResultElm);
                /*
                <routes>
                  <route>
                    <id>
                      12345
                    </id>
                    <description>
                      Rondje om de kerk
                    </description>
                  </route>
                </routes> */
                if(routesResultElm!=null & routesResultElm.getChildrenByTag(ROUTE_ELM).size()>0){
                    Vector routes = routesResultElm.getChildrenByTag(ROUTE_ELM);
                    for(int i=0;i<routes.size();i++){
                        JXElement routeElm = (JXElement)routes.elementAt(i);
                        log.info("$$$$$$$$ " + routeElm);
                        String id = routeElm.getChildText(ID_FIELD);
                        url = routingServletUrl + "?request=GetPredefinedRoute&RouteID=" + id;
                        JXElement routeResultElm = NetConnection.getXMLFromREST(url);
                        /*<gpx>
                            <bounds minlon="152779" minlat="453239" maxlon="153925" maxlat="455055" />
                            <name><![CDATA[route 1]]></name>
                            <desc><![CDATA[test route 1 description]]></desc>
                            <wpt lon="152781" lat="453254">
                                <name>28_508983</name>
                            </wpt>
                            <rte>
                                <rtept lon="153925" lat="453397"></rtept>
                                <rtept lon="153915" lat="453406"></rtept>
                                <rtept lon="153901" lat="453448"></rtept>
                                <rtept lon="153879" lat="453513"></rtept>
                                <rtept lon="153877" lat="453520"></rtept>
                                <rtept lon="153861" lat="453562"></rtept>
                                <rtept lon="153836" lat="453624"></rtept>
                                <rtept lon="153810" lat="453678"></rtept>
                                <rtept lon="153802" lat="453688"></rtept>
                                <rtept lon="153780" lat="453699"></rtept>
                            </rte>
                        </gpx>*/
                        if(routeResultElm == null) throw new UtopiaException("No route found with id " + id);
                        logic.insertRoute(routeResultElm, ROUTE_TYPE_FIXED);
                    }
                }
            }catch(Throwable t){
                log.error("Exception in syncFixedRoutes:" + t.toString());
                throw new UtopiaException("Exception in syncFixedRoutes", t);
            }
        }

        /**
         * <media>
         * <medium>
         * <naam></naam>
         * <description></description>
         * <uri></uri>
         * </medium>
         * </media>
         *
         * @return media element
         */
        public JXElement getKICHMedia() {
            String kichRestUrl = Amuse.server.getPortal().getProperty(KICH_REST_URL);
            kichRestUrl += "?action=getmedia";
            return NetConnection.getXMLFromREST(kichRestUrl);
        }

        // creates, updates and deletes a poi in KICH
        public String cudPOI(String anAction, JXElement aPOIElement) throws UtopiaException {
            String kichPostUrl = Amuse.server.getPortal().getProperty(KICH_POST_URL);
            Properties postParams = new Properties();
            postParams.setProperty("action", anAction);
            postParams.setProperty("xml", aPOIElement.toEscapedString());
            //return postData(kichPostUrl, postParams);
            NetConnection.postData(kichPostUrl, postParams);
            // TODO: change this later - only for testing purposes
            return new String("1261265");
        }

}
