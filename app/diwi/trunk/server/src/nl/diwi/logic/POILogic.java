package nl.diwi.logic;

import nl.diwi.external.DataSource;
import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import org.geotracing.gis.Transform;
import org.geotracing.handler.QueryLogic;
import org.keyworx.amuse.core.Amuse;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Java;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.Medium;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.postgis.PGgeometryLW;
import org.postgis.Point;

import java.util.Properties;
import java.util.Vector;

public class POILogic implements Constants {
    private static final Properties properties = new Properties();
    private Oase oase;
    private DataSource dataSource;
    private Log log = Logging.getLog("POILogic");

    public POILogic(Oase o) {
        oase = o;
        dataSource = new DataSource(oase);
    }


    /**
     * Inserts a poi.
     * <p/>
     * <poi>
     * <name></name>
     * <description></description>
     * <category></category>
     * <x></x>
     * <y></y>
     * <media>
     * <kich-uri><kich-uri>
     * <kich-uri><kich-uri>
     * </media>
     * </poi>
     *
     * @param aPOIElement a poi xml element
     * @return the inserted poi id
     * @throws org.keyworx.utopia.core.data.UtopiaException
     *          Standard exception
     */
    public int insert(JXElement aPOIElement) throws UtopiaException {
        try {
            // first insert the data in the Kich DB
            String kichId = dataSource.insertPOI(aPOIElement);

            if (kichId == null || kichId.length() == 0) {
                throw new UtopiaException("No valid kich id from KICH POI insert post message");
            }

            Record poi = oase.getModifier().create(POI_TABLE);
            setFields(poi, kichId, aPOIElement);
            oase.getModifier().insert(poi);

            return poi.getId();
        } catch (OaseException oe) {
            throw new UtopiaException("Cannot insert poi record", oe, ErrorCode.__6006_database_irregularity_error);
        } catch (Throwable t) {
            throw new UtopiaException("Exception in POILogic.insert() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
        }
    }

    public int insertForSync(JXElement aPOIElement) throws UtopiaException {
        try {
            Record poi = oase.getModifier().create(POI_TABLE);
            poi.setStringField(KICHID_FIELD, aPOIElement.getChildText(ID_FIELD));
            setFields(poi, null, aPOIElement);
            oase.getModifier().insert(poi);

            //processTestMedia(aPOIElement, poi);

            return poi.getId();
        } catch (OaseException oe) {
            throw new UtopiaException("Cannot insert poi record", oe, ErrorCode.__6006_database_irregularity_error);
        } catch (Throwable t) {
            throw new UtopiaException("Exception in POILogic.insert() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
        }
    }

    /**
     * Updates a poi.
     * <p/>
     * <poi>
     * <name></name>
     * <description></description>
     * <category></category>
     * <x></x>
     * <y></y>
     * <media>
     * <kich-uri><kich-uri>
     * <kich-uri><kich-uri>
     * </media>
     * </poi>
     *
     * @param aPOIElement a poi xml element
     * @throws org.keyworx.utopia.core.data.UtopiaException
     *          Standard exception
     */
    public void update(int aPOIId, JXElement aPOIElement) throws UtopiaException {
        try {
            // first update the KICH db
            dataSource.updatePOI(aPOIElement);

            Record poi = oase.getFinder().read(aPOIId);
            updateInKWXOnly(poi, aPOIElement);

        } catch (OaseException oe) {
            throw new UtopiaException("Cannot update poi record", oe, ErrorCode.__6006_database_irregularity_error);
        } catch (Throwable t) {
            throw new UtopiaException("Exception in POILogic.update() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
        }
    }

    public void updateInKWXOnly(Record aPOI, JXElement aPOIElement) throws UtopiaException {
        try {
            setFields(aPOI, null, aPOIElement);
            oase.getModifier().update(aPOI);

        } catch (OaseException oe) {
            throw new UtopiaException("Cannot update poi record", oe, ErrorCode.__6006_database_irregularity_error);
        } catch (Throwable t) {
            throw new UtopiaException("Exception in POILogic.update() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
        }
    }

    private void setFields(Record aPOI, String aKICHID, JXElement aPOIElement) throws UtopiaException {
        String name = aPOIElement.getChildText(NAME_FIELD);
        String desc = aPOIElement.getChildText(DESCRIPTION_FIELD);
        String type = aPOIElement.getChildText(TYPE_FIELD);
        String category = aPOIElement.getChildText(CATEGORY_FIELD);
        String lat = aPOIElement.getChildText(LAT_FIELD);
        String lon = aPOIElement.getChildText(LON_FIELD);

        if (aKICHID != null && aKICHID.length() > 0) {
            aPOI.setStringField(KICHID_FIELD, aKICHID);
        }
        if (name != null && name.length() > 0) {
            aPOI.setStringField(NAME_FIELD, name);
        }
        if (desc != null && desc.length() > 0) {
            aPOI.setStringField(DESCRIPTION_FIELD, desc);
        }
        if (type != null && type.length() > 0 && Java.isInt(type)) {
            aPOI.setIntField(TYPE_FIELD, Integer.parseInt(type));
        }
        if (category != null && category.length() > 0) {
            aPOI.setStringField(CATEGORY_FIELD, category);
        }

        // Create and set locations
        Point wgsPoint;
        Point rdPoint;
        if (lat == null || lat.length() == 0 || lon == null || lon.length() == 0) {
            //let's try if we got RD coords
            String x = aPOIElement.getChildText(X_FIELD);
            String y = aPOIElement.getChildText(Y_FIELD);

            if (x == null || x.length() == 0 || y == null || y.length() == 0) {
                throw new UtopiaException("No valid lat and lon coordinates found");
            }

            rdPoint = new Point(Double.parseDouble(x), Double.parseDouble(y));

            double latlon[];
            try {
                latlon = Transform.RDtoWGS84(Double.parseDouble(x), Double.parseDouble(y));
            } catch (Exception e) {
                throw new UtopiaException("No valid lat and lon coordinates found");
            }
            wgsPoint = new Point(latlon[0], latlon[1]);
        } else {
            wgsPoint = new Point(Double.parseDouble(lon), Double.parseDouble(lat));

            double xy[];
            try {
                xy = Transform.WGS84toRD(Double.parseDouble(lon), Double.parseDouble(lat));
            } catch (Exception e) {
                throw new UtopiaException("No valid lat and lon coordinates found");
            }
            rdPoint = new Point(xy[0], xy[1]);
        }

        // wgs 84 point
        wgsPoint.setSrid(EPSG_WGS84);
        PGgeometryLW wgsGeom = new PGgeometryLW(wgsPoint);
        aPOI.setObjectField(WGSPOINT_FIELD, wgsGeom);

        // rd point
        rdPoint.setSrid(EPSG_DUTCH_RD);
        PGgeometryLW rdGeom = new PGgeometryLW(rdPoint);
        aPOI.setObjectField(RDPOINT_FIELD, rdGeom);

        JXElement media = aPOIElement.getChildByTag(MEDIA_FIELD);
        if(media!=null && media.hasChildren()){
            aPOI.setXMLField(MEDIA_FIELD, aPOIElement.getChildByTag(MEDIA_FIELD));
        }
    }

    /**
     * Gets a poi by id.
     *
     * @throws UtopiaException Standard exception
     */
    public JXElement get(int aPersonId, int aPOIId) throws UtopiaException {
        try {
            Record poi = oase.getFinder().read(aPOIId);
            JXElement poiElm = poiToXML(poi);

            log.info(new String(poiElm.toBytes(false)));

            // put the type explicitey in the kich-uri
            Vector kichUrisElms = poiElm.getChildByTag(MEDIA_FIELD).getChildren();
            for (int i = 0; i < kichUrisElms.size(); i++) {
                JXElement kichUriElm = (JXElement) kichUrisElms.elementAt(i);
                String kichUri = kichUriElm.getText();
                if (kichUri.indexOf(".jpg") != -1 || kichUri.indexOf(".gif") != -1 || kichUri.indexOf(".bmp") != -1) {
                    kichUriElm.setAttr(TYPE_FIELD, Medium.IMAGE_KIND);
                } else
                if (kichUri.indexOf(".mov") != -1 || kichUri.indexOf(".avi") != -1 || kichUri.indexOf(".3gp") != -1 || kichUri.indexOf(".mp4") != -1 || kichUri.indexOf(".wmv") != -1) {
                    kichUriElm.setAttr(TYPE_FIELD, Medium.VIDEO_KIND);
                } else
                if (kichUri.indexOf(".aiff") != -1 || kichUri.indexOf(".mp3") != -1 || kichUri.indexOf(".wav") != -1) {
                    kichUriElm.setAttr(TYPE_FIELD, Medium.AUDIO_KIND);
                } else if (kichUri.indexOf(".txt") != -1) {
                    kichUriElm.setAttr(TYPE_FIELD, Medium.TEXT_KIND);
                }
            }

            // now also provide extra info on routes that are
            poiElm.addChildren(addRoutesForPoint(aPersonId, aPOIId));

            return poiElm;
        } catch (OaseException oe) {
            throw new UtopiaException("Cannot read poi with id " + aPOIId, oe, ErrorCode.__6006_database_irregularity_error);
        }
    }

    /**
     * Gets a poi by KICH ID.
     *
     * @throws UtopiaException Standard exception
     */
    public Record getRecord(String aKICHId) throws UtopiaException {
        try {
            Record[] pois = oase.getFinder().queryTable(POI_TABLE, KICHID_FIELD + "='" + aKICHId + "'", null, null);
            if (pois.length == 0) return null;
            if (pois.length > 1)
                throw new UtopiaException(pois.length + " poi's found with KICHID:" + aKICHId + ". This should not have happened");
            return pois[0];
        } catch (OaseException oe) {
            throw new UtopiaException("Cannot read poi with KICHID " + aKICHId, oe, ErrorCode.__6006_database_irregularity_error);
        }
    }

    /**
     * Gets a poi by KICH ID.
     *
     * @throws UtopiaException Standard exception
     */
    public JXElement get(int aPersonId, String aKICHId) throws UtopiaException {
        Record poi = getRecord(aKICHId);
        return get(aPersonId, poi.getId());
    }

    private Vector addRoutesForPoint(int aPersonId, int aPOIId) throws UtopiaException {
        try {
            NavigationLogic navLogic = new NavigationLogic(oase);
            Record activeRoute = navLogic.getActiveRoute(aPersonId);
            if(activeRoute == null) return new Vector(0);

            String tables = POI_TABLE + "," + ROUTE_TABLE;
            String fields = ROUTE_TABLE + "." + ID_FIELD;
            String where = POI_TABLE + "." + ID_FIELD + "=" + aPOIId + " AND " + ROUTE_TABLE + "." + ID_FIELD + "<>" + activeRoute.getId();
            String relations = ROUTE_TABLE + "," + POI_TABLE;
            String postCond = null;
            Record[] recs = QueryLogic.queryStore(oase, tables, fields, where, relations, postCond);

            Vector results = new Vector(recs.length);
            for (int i = 0; i < recs.length; i++) {
                Record route = recs[i];
                JXElement routeElm = route.toXML();
                routeElm.setTag(ROUTE_ELM);
                results.add(routeElm);
            }

            return results;
        } catch (Throwable t) {
            log.error("Exception in addRoutesForPoint:" + t.getMessage());
            throw new UtopiaException(t);
        }
    }

    /**
     * Gets all pois.
     *
     * @throws UtopiaException Standard exception
     */
    public Vector getList() throws UtopiaException {
        try {
            Record[] pois = oase.getFinder().queryTable(POI_TABLE, null);
            return getPOIList(pois);
        } catch (OaseException oe) {
            throw new UtopiaException("Cannot read pois ", oe, ErrorCode.__6006_database_irregularity_error);
        }
    }


    private Vector getPOIList(Record[] pois) throws UtopiaException {
        if (pois == null || pois.length == 0) return new Vector(0);

        Vector results = new Vector(pois.length);
        for (int i = 0; i < pois.length; i++) {
            results.add(poiToXML(pois[i]));
        }

        return results;
    }

    private JXElement poiToXML(Record aPoi) throws UtopiaException {
        JXElement poiElm = aPoi.toXML();
        JXElement mediaElm = aPoi.getXMLField(MEDIA_FIELD);
        if(mediaElm!=null){
            poiElm.removeChildByTag(MEDIA_FIELD);
            poiElm.addChild(mediaElm);
        }
        poiElm.setTag(POI_ELM);

        //Insert lat/lon fields
        Point wgsPoint = (Point) ((PGgeometryLW) aPoi.getObjectField(WGSPOINT_FIELD)).getGeometry();
        poiElm.addTextChild(LON_FIELD, "" + wgsPoint.x);
        poiElm.addTextChild(LAT_FIELD, "" + wgsPoint.y);

        //Insert x/y fields
        Point rdPoint = (Point) ((PGgeometryLW) aPoi.getObjectField(RDPOINT_FIELD)).getGeometry();
        poiElm.addTextChild(X_FIELD, "" + rdPoint.x);
        poiElm.addTextChild(Y_FIELD, "" + rdPoint.y);
        return poiElm;
    }

    /**
     * Gets all typed point pois.
     *
     * @throws UtopiaException Standard exception
     */
    public Vector getPoisByType(int aPoiType) throws UtopiaException {
        try {
            Record[] pois = oase.getFinder().queryTable(POI_TABLE, TYPE_FIELD + "='" + aPoiType + "'", null, null);
            return getPOIList(pois);
        } catch (OaseException oe) {
            throw new UtopiaException("Cannot read pois ", oe, ErrorCode.__6006_database_irregularity_error);
        }
    }


    /**
     * Delete a poi.
     *
     * @param aPOIId a poi id
     * @throws UtopiaException Standard exception
     */
    public void delete(int aPOIId) throws UtopiaException {
        try {
            String kichId = oase.getFinder().read(aPOIId).getStringField(KICHID_FIELD);
            JXElement poi = new JXElement(POI_ELM);
            JXElement id = new JXElement(ID_FIELD);
            id.setText(kichId);
            poi.addChild(id);
            dataSource.deletePOI(poi);

            oase.getModifier().delete(aPOIId);

        } catch (OaseException oe) {
            throw new UtopiaException("Cannot delete poi record with id=" + aPOIId, oe, ErrorCode.__6006_database_irregularity_error);
        }
    }


    /*
    <poi>
       <id>NL.DIWI.81b8b03e-28a0-40d5-b184-f1c99d21d886</id>
       <name>Kazemat en loopgraaf bij de stoplijn</name>
       <description>Kazemat S-17, een zogenaamd Stekelvarken.
       </description>
       <category>italiaans</category>
       <type>2</type>
       <x>166463</x>
       <y>470532.17</y>
       <media>
         <kich-uri>
         http://simlandscape.alterra.nl/diwimedia/photo1.bmp
         </kich-uri>
         <kich-uri>
         http://simlandscape.alterra.nl/diwimedia/audio1.mp3
         </kich-uri>
       </media>
   </poi>
     */
    /**
     * relate a poi.
     *
     * @param aPOIId a poi id
     * @throws UtopiaException Standard exception
     */
    public void relateMedia(int aPOIId, Vector theMediaIds) throws UtopiaException {
        try {
            String mediaUrl = Amuse.server.getPortal().getProperty(MEDIA_URL);

            dataSource.relateMediaToPoi(aPOIId, theMediaIds);

            Record record = oase.getFinder().read(aPOIId);

            // replace the media
            JXElement media = record.getXMLField(MEDIA_FIELD);
            if(media == null) media = new JXElement(MEDIA_FIELD);

            for (int i = 0; i < theMediaIds.size(); i++) {
                JXElement kichuri = new JXElement("kich-uri");
                String url = mediaUrl + ((JXElement) theMediaIds.elementAt(i)).getText();
                kichuri.setText(url);
                media.addChild(kichuri);
            }

            record.setXMLField(MEDIA_FIELD, media);

            oase.getModifier().update(record);

        } catch (OaseException oe) {
            throw new UtopiaException("Cannot relate media to poi record with id=" + aPOIId, oe, ErrorCode.__6006_database_irregularity_error);
        }
    }

    /**
     * relate a poi.
     *
     * @param aPOIId a poi id
     * @throws UtopiaException Standard exception
     */
    public void unrelateMedia(int aPOIId, Vector theMediaIds) throws UtopiaException {
        try {
            String mediaUrl = Amuse.server.getPortal().getProperty(MEDIA_URL);

            dataSource.unrelateMediaFromPoi(aPOIId, theMediaIds);

            Record record = oase.getFinder().read(aPOIId);
            JXElement media = record.getXMLField(MEDIA_FIELD);
            if (media == null || !media.hasChildren()) return;

            Vector kichURIs = (Vector) media.getChildren().clone();
            for (int i = 0; i < kichURIs.size(); i++) {
                JXElement kichUriElm = (JXElement) kichURIs.elementAt(i);
                String kichUri = kichUriElm.getText();
                for (int j = 0; j < theMediaIds.size(); j++) {
                    String s = mediaUrl + ((JXElement) theMediaIds.elementAt(j)).getText();
                    if (kichUri.equals(s)) {
                        media.removeChild(kichUriElm);
                    }
                }
            }
            record.setXMLField(MEDIA_FIELD, media);
            oase.getModifier().update(record);

        } catch (OaseException oe) {
            throw new UtopiaException("Cannot unrelate media to poi record with id=" + aPOIId, oe, ErrorCode.__6006_database_irregularity_error);
        }
    }

    /**
     * Properties passed on from Handler.
     */
    public static String getProperty(String propertyName) {
        return (String) properties.get(propertyName);
    }

    /**
     * Properties passed on from Handler.
     */
    public static void setProperty(String propertyName, String propertyValue) {
        properties.put(propertyName, propertyValue);
    }

}
