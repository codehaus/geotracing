package nl.diwi.logic;

import nl.diwi.external.DataSource;
import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import org.geotracing.gis.Transform;
import org.keyworx.amuse.core.Amuse;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Java;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.ErrorCode;
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

    /*private void processTestMedia(JXElement aPOIElement, Record aPOI) throws UtopiaException {
            try {
                String mediaUrl = Amuse.server.getPortal().getProperty(MEDIA_URL);
                // now check if there's is media present so we can add these
                JXElement media = aPOIElement.getChildByTag("media");
                if (media != null) {
                    Vector uris = media.getChildrenByTag(KICH_URI_ELM);
                    for (int i = 0; i < uris.size(); i++) {
                        JXElement uri = (JXElement) uris.elementAt(i);
                        String mediumFileName = uri.getAttr("medium");
                        if (mediumFileName != null && mediumFileName.length() > 0) {
                            //File f = new File(TEST_DATA_URL + "/" + mediumFileName);
                            File f = new File(Amuse.server.getPortal().getProperty(TEST_DATA_LOCATION) + "/" + mediumFileName);
                            if (f.exists()) {
                                log.info("create the medium!!:" + mediumFileName);
                                HashMap attrs = new HashMap(3);
                                attrs.put(MediaFiler.FIELD_FILENAME, mediumFileName);
                                if (mediumFileName.indexOf("3gp") != -1) {
                                    attrs.put(MediaFiler.FIELD_MIME, "video/3gpp");
                                }
                                Record medium = oase.getMediaFiler().insert(f, attrs);
                                oase.getRelater().relate(aPOI, medium);

                                uri.setText(mediaUrl + medium.getId());
                                uri.removeAttr("medium");
                            } else {
                                log.info("could not find the file!!!:" + mediumFileName);
                            }
                        }
                    }
                    // update the poi
                    aPOI.setXMLField(MEDIA_FIELD, media);
                    oase.getModifier().update(aPOI);
                }
            } catch (Throwable t) {
                log.error("Exception in processTestMedia:" + t.toString());
                throw new UtopiaException("Exception in processTestMedia:" + t.toString(), t);

            }
        }
    */
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

        // Create and set location: assume default SRID (4326)
        Point point = null;
        if (lat == null || lat.length() == 0 || lon == null || lon.length() == 0) {
            //let's try if we got RD coords
            String x = aPOIElement.getChildText(X_FIELD);
            String y = aPOIElement.getChildText(Y_FIELD);

            if (x == null || x.length() == 0 || y == null || y.length() == 0) {
                throw new UtopiaException("No valid lat and lon coordinates found");
            }
            double xy[];
            try {
                xy = Transform.RDtoWGS84(Double.parseDouble(x), Double.parseDouble(y));
            } catch (Exception e) {
                throw new UtopiaException("No valid lat and lon coordinates found");
            }
            point = new Point(xy[0], xy[1]);
        } else {
            point = new Point(Double.parseDouble(lat), Double.parseDouble(lon));
        }
        point.setSrid(DEFAULT_SRID);
        PGgeometryLW geom = new PGgeometryLW(point);
        aPOI.setObjectField(POINT_FIELD, geom);

        aPOI.setXMLField(MEDIA_FIELD, aPOIElement.getChildByTag(MEDIA_FIELD));
    }

    /**
     * Gets a poi by id.
     *
     * @throws UtopiaException Standard exception
     */
    public JXElement get(int aPOIId) throws UtopiaException {
        try {
            Record poi = oase.getFinder().read(aPOIId);
            JXElement poiElm = poi.toXML();
            poiElm.setTag(POI_ELM);

            // now also provide extra info on routes that are
            poiElm.addChildren(addRoutesForPoint((Point)poi.getObjectField(POINT_FIELD)));

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
    public JXElement get(String aKICHId) throws UtopiaException {
        Record poi = getRecord(aKICHId);
        JXElement poiElm = poi.toXML();
        poiElm.setTag(POI_ELM);

        // now also provide extra info on routes that are
        poiElm.addChildren(addRoutesForPoint((Point)poi.getObjectField(POINT_FIELD)));

        return poiElm;
    }

    private Vector addRoutesForPoint(Point aPoint) throws UtopiaException{
        try {
            // contains, touches, intersects
            String queryString = "select id, name from " + ROUTE_TABLE + " where contains(GeomFromEWKT('" + aPoint + "'), path)";            
            Record[] routes = oase.getFinder().freeQuery(queryString);

            Vector results = new Vector(routes.length);
            for(int i=0;i<routes.length;i++){
                Record route = routes[i];
                JXElement routeElm = route.toXML();
                routeElm.setTag(ROUTE_ELM);
                results.add(routeElm);
            }

            return results;
        } catch (Throwable t) {
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
            log.info("poi getlist");
            Record[] pois = oase.getFinder().queryTable(POI_TABLE, null);
            return getPOIList(pois);
        } catch (OaseException oe) {
            throw new UtopiaException("Cannot read pois ", oe, ErrorCode.__6006_database_irregularity_error);
        }
    }


    private Vector getPOIList(Record[] pois) throws UtopiaException {
        Vector results = new Vector(pois.length);        
        for (int i = 0; i < pois.length; i++) {
            JXElement poiElm = new JXElement();
            poiElm.setTag(POI_ELM);

            //name + description
            poiElm.addTextChild(ID_FIELD, pois[i].getIdString());
            poiElm.addTextChild(NAME_FIELD, pois[i].getStringField(NAME_FIELD));
            poiElm.addTextChild(DESCRIPTION_FIELD, pois[i].getStringField(DESCRIPTION_FIELD));
            poiElm.addTextChild(TYPE_FIELD, "" + pois[i].getIntField(TYPE_FIELD));
            poiElm.addTextChild(CATEGORY_FIELD, pois[i].getStringField(CATEGORY_FIELD));

            poiElm.addText(pois[i].getStringField(MEDIA_FIELD));

            //Insert lat/lon fields
            Point point = (Point) ((PGgeometryLW) pois[i].getObjectField(POINT_FIELD)).getGeometry();

            poiElm.addTextChild(LAT_FIELD, "" + point.x);
            poiElm.addTextChild(LON_FIELD, "" + point.y);

            double xy[];
            try {
                xy = Transform.WGS84toRD(point.x, point.y);
            } catch (Exception e) {
                throw new UtopiaException("No valid lat and lon coordinates found");
            }
            poiElm.addTextChild(X_FIELD, "" + xy[0]);
            poiElm.addTextChild(Y_FIELD, "" + xy[1]);

            results.add(poiElm);
        }

        return results;
    }

    /**
     * Gets all start point pois.
     *
     * @throws UtopiaException Standard exception
     */
    public Vector getStartPoints() throws UtopiaException {
        try {
            Record[] pois = oase.getFinder().queryTable(POI_TABLE, TYPE_FIELD + "='" + POI_STARTPOINT + "'", null, null);
            return getPOIList(pois);
        } catch (OaseException oe) {
            throw new UtopiaException("Cannot read pois ", oe, ErrorCode.__6006_database_irregularity_error);
        }
    }

    /**
     * Gets all start point pois.
     *
     * @throws UtopiaException Standard exception
     */
    public Vector getEndPoints() throws UtopiaException {
        try {
            Record[] pois = oase.getFinder().queryTable(POI_TABLE, TYPE_FIELD + "='" + POI_ENDPOINT + "'", null, null);
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

            // first remove the current media
            record.getXMLField(MEDIA_FIELD).removeChildren();

            for (int i = 0; i < theMediaIds.size(); i++) {
                JXElement medium = new JXElement("kich-uri");
                medium.setText(mediaUrl + theMediaIds.elementAt(i).toString());
                record.getXMLField(MEDIA_FIELD).addChild(medium);
            }

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
            Vector kichURIs = record.getXMLField(MEDIA_FIELD).getChildren();
            for (int i = 0; i < kichURIs.size(); i++) {
                for (int j = 0; j < theMediaIds.size(); j++) {
                    if (kichURIs.elementAt(i).equals(MEDIA_URL + theMediaIds.elementAt(j))) {
                        record.getXMLField(MEDIA_FIELD).removeChildAt(i);
                    }
                }
            }
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
