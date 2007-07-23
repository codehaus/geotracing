package nl.diwi.external;

import nl.diwi.logic.POILogic;
import nl.diwi.logic.RouteLogic;
import nl.diwi.util.Constants;
import nl.diwi.util.NetConnection;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import org.keyworx.amuse.core.Amuse;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;

import java.util.Vector;

public class DataSource implements Constants {
    private Oase oase;
    private Log log = Logging.getLog("DataSource");

    public DataSource(Oase oase) {
        this.oase = oase;
    }

    // This request is required to get all points of interest for 'struinen'
    public void syncPOIs() throws UtopiaException {
        try {
            POILogic logic = new POILogic(oase);
            /*String kichRESTUrl = Amuse.server.getPortal().getProperty(KICH_REST_URL);
            kichRESTUrl += "?command=selectpois";*/
            /*
           <pois>
               <poi>
                   <id></id>
                   <name><name>
                   <description></description>
                   <category></category>
                   <lon></lon>
                   <lat></lat>
                   <media>
                       <kich-uri><kich-uri>
                       <kich-uri><kich-uri>
                   </media>
               </poi>
           </pois>
            */
            /*JXElement poisElm = NetConnection.getXMLFromREST(kichRESTUrl);*/
            String result = postToKICHService("selectpois", "<pois />");
            log.info(result);
            if (result == null || result.length() == 0) {
                return;
            }

            JXElement poisElm = new JXBuilder().build(result);
            if (poisElm != null) {
                Vector poiElms = poisElm.getChildrenByTag(POI_ELM);
                for (int i = 0; i < poiElms.size(); i++) {
                    JXElement poiElm = (JXElement) poiElms.elementAt(i);
                    Record poi = logic.getRecord(poiElm.getChildText(ID_FIELD));
                    if (poi != null) {
                        logic.updateInKWXOnly(poi, poiElm);
                    } else {
                        logic.insertForSync(poiElm);
                    }
                }
            }
        } catch (Throwable t) {
            log.error("Exception in syncPOI's" + t.getMessage());
            throw new UtopiaException(t);
        }
    }

    public void syncFixedRoutes() throws UtopiaException {
        try {
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
            if (routesResultElm != null && routesResultElm.getChildrenByTag(ROUTE_ELM).size() > 0) {
                Vector routes = routesResultElm.getChildrenByTag(ROUTE_ELM);
                for (int i = 0; i < routes.size(); i++) {
                    JXElement routeElm = (JXElement) routes.elementAt(i);
                    log.info("$$$$$$$$ " + routeElm);
                    String id = routeElm.getChildText(ID_FIELD);
                    url = routingServletUrl + "?request=GetPredefinedRoute&routeid=" + id;
                    routeElm.addChild(NetConnection.getXMLFromREST(url));
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
                    log.info(new String(routeElm.toBytes(false)));
                    logic.insertRoute(routeElm, ROUTE_TYPE_FIXED);
                }
            }
        } catch (Throwable t) {
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
    public JXElement getKICHMedia() throws UtopiaException {
        try {
            /*String kichRestUrl = Amuse.server.getPortal().getProperty(KICH_REST_URL);*/
            String result = postToKICHService("selectmedia", "<media />");
            if (result == null || result.length() == 0) {
                return null;
            }
            return new JXBuilder().build(result);
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
        /*kichRestUrl += "?command=selectmedia<media />";
        return NetConnection.getXMLFromREST(kichRestUrl);*/
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
    public Vector getKICHThemes() throws UtopiaException {
        try {
            JXElement result = new JXElement("themes");
            result.addTextChild(THEME_ELM, "Kerk en kerkonderdeel");
            result.addTextChild(THEME_ELM, "Klooster, kloosteronderdl");
            result.addTextChild(THEME_ELM, "Gedenkteken");
            result.addTextChild(THEME_ELM, "Brug");
            result.addTextChild(THEME_ELM, "Gracht");
            result.addTextChild(THEME_ELM, "Fort, vesting en -onderdl");
            result.addTextChild(THEME_ELM, "Erfscheiding");
            result.addTextChild(THEME_ELM, "Industrie- en poldermolen");
            result.addTextChild(THEME_ELM, "Begraafplaats en -onderdl");
            result.addTextChild(THEME_ELM, "Kerkelijke dienstwoning");
            result.addTextChild(THEME_ELM, "Boerderij");
            result.addTextChild(THEME_ELM, "Militair verblijfsgebouw");
            result.addTextChild(THEME_ELM, "Waterkering en -doorlaat");
            result.addTextChild(THEME_ELM, "Gemaal");
            result.addTextChild(THEME_ELM, "Grensafbakening");
            result.addTextChild(THEME_ELM, "Kasteel, buitenplaats");
            result.addTextChild(THEME_ELM, "Kapel");
            result.addTextChild(THEME_ELM, "Bijgebouwen kastelen enz.");
            result.addTextChild(THEME_ELM, "Omwalling");
            return result.getChildren();
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    // insert a poi in KICH
    public String insertPOI(JXElement aPOIElement) throws UtopiaException {
        JXElement pois = new JXElement("pois");
        pois.addChild(aPOIElement);
        return postToKICHService(POI_INSERT_COMMAND, new String(pois.toBytes(false)));
    }

    // update a poi in KICH
    public String updatePOI(JXElement aPOIElement) throws UtopiaException {
        JXElement pois = new JXElement("pois");
        pois.addChild(aPOIElement);
        return postToKICHService(POI_UPDATE_COMMAND, new String(pois.toBytes(false)));
    }

    // delete a poi in KICH
    public String deletePOI(JXElement aPOIElement) throws UtopiaException {
        JXElement pois = new JXElement("pois");
        pois.addChild(aPOIElement);
        return postToKICHService(POI_DELETE_COMMAND, new String(pois.toBytes(false)));
    }

    // relate media to poi in KICH
    public String relateMediaToPoi(int aPOIId, Vector theMediaIds) throws UtopiaException {
        try{
            JXElement poi = new JXElement(POI_ELM);
            JXElement id = new JXElement(ID_FIELD);
            id.setText(oase.getFinder().read(aPOIId).getStringField(KICHID_FIELD));
            poi.addChild(id);
            for (int i = 0; i < theMediaIds.size(); i++) {
                JXElement mediumId = new JXElement("media_id");
                mediumId.setText(((JXElement)theMediaIds.elementAt(i)).getText());
                poi.addChild(mediumId);
            }
            return postToKICHService(RELATE_MEDIA_COMMAND, new String(poi.toBytes(false)));
        }catch(Throwable t){
            log.error("Exception in relateMediaToPoi:" + t.getMessage());
            throw new UtopiaException(t);
        }
    }

    // relate media to poi in KICH
    public String unrelateMediaFromPoi(int aPOIId, Vector theMediaIds) throws UtopiaException {
        try{
            JXElement poi = new JXElement(POI_ELM);
            JXElement id = new JXElement(ID_FIELD);
            id.setText(oase.getFinder().read(aPOIId).getStringField(KICHID_FIELD));
            poi.addChild(id);
            for (int i = 0; i < theMediaIds.size(); i++) {
                JXElement mediumId = new JXElement("media_id");
                mediumId.setText(((JXElement)theMediaIds.elementAt(i)).getText());
                poi.addChild(mediumId);
            }

            return postToKICHService(UNRELATE_MEDIA_COMMAND, new String(poi.toBytes(false)));
        }catch(Throwable t){
            log.error("Exception in relateMediaToPoi:" + t.getMessage());
            throw new UtopiaException(t);
        }
    }

    // creates, updates and deletes a poi in KICH
    public String postToKICHService(String anAction, String aPostString) throws UtopiaException {
        try {
            String kichPostUrl = Amuse.server.getPortal().getProperty(KICH_SERVLET_URL);
            kichPostUrl += "?command=" + anAction;
            //Properties postParams = new Properties();
            //postParams.setProperty("command", anAction);
            //postParams.setProperty("xml", aPOIElement.toEscapedString());
//            /return NetConnection.postData(kichPostUrl, postParams);
            return NetConnection.postData(kichPostUrl, aPostString);
        } catch (Throwable t) {
            log.error(t.getMessage());
            throw new UtopiaException("Exception in communcation with KICH service: command=" + anAction + ", data=" + aPostString, t);
        }
    }

}
