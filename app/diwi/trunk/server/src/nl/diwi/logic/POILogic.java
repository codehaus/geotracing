package nl.diwi.logic;

import nl.diwi.external.DataSource;
import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.common.log.Logging;
import org.keyworx.common.log.Log;
import org.keyworx.common.util.Java;
import org.postgis.Point;
import org.postgis.PGgeometryLW;

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
     *
     * <poi>
     *      <name></name>
     *      <description></description>
     *      <category></category>
     *      <x></x>
     *      <y></y>
     *      <media>
     *          <kich-uri><kich-uri>
     *          <kich-uri><kich-uri>
     *      </media>
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
            String kichId = dataSource.cudPOI(POI_INSERT_ACTION, aPOIElement);
            if(kichId == null || kichId.length() == 0){
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

            return poi.getId();
        } catch (OaseException oe) {
            throw new UtopiaException("Cannot insert poi record", oe, ErrorCode.__6006_database_irregularity_error);
        } catch (Throwable t) {
            throw new UtopiaException("Exception in POILogic.insert() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
        }
    }

    /**
     * Updates a poi.
     *
     * <poi>
     *      <name></name>
     *      <description></description>
     *      <category></category>
     *      <x></x>
     *      <y></y> 
     *      <media>
     *          <kich-uri><kich-uri>
     *          <kich-uri><kich-uri>
     *      </media>
     * </poi>
     *
     * @param aPOIElement a poi xml element
     * @throws org.keyworx.utopia.core.data.UtopiaException
     *          Standard exception
     */
    public void update(int aPOIId, JXElement aPOIElement) throws UtopiaException {
        try {
            // first update the KICH db
            dataSource.cudPOI(POI_UPDATE_ACTION, aPOIElement);

            Record poi = oase.getFinder().read(aPOIId);
            updateForSync(poi, aPOIElement);
            
        } catch (OaseException oe) {
            throw new UtopiaException("Cannot update poi record", oe, ErrorCode.__6006_database_irregularity_error);
        } catch (Throwable t) {
            throw new UtopiaException("Exception in POILogic.update() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
        }
    }

    public void updateForSync(Record aPOI, JXElement aPOIElement) throws UtopiaException {
        try {
            setFields(aPOI, null, aPOIElement);
            oase.getModifier().update(aPOI);

        } catch (OaseException oe) {
            throw new UtopiaException("Cannot update poi record", oe, ErrorCode.__6006_database_irregularity_error);
        } catch (Throwable t) {
            throw new UtopiaException("Exception in POILogic.update() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
        }
    }

    private void setFields(Record aPOI, String aKICHID, JXElement aPOIElement) throws UtopiaException{
        String name = aPOIElement.getChildText(NAME_FIELD);
        String desc = aPOIElement.getChildText(DESCRIPTION_FIELD);
        String type = aPOIElement.getChildText(TYPE_FIELD);
        String category = aPOIElement.getChildText(CATEGORY_FIELD);
        String x = aPOIElement.getChildText(X_FIELD);
        String y = aPOIElement.getChildText(Y_FIELD);
        if(x == null || x.length() == 0 || y == null || y.length() == 0) throw new UtopiaException("No valid x and y coordinates found");

        if(aKICHID!=null && aKICHID.length()>0) aPOI.setStringField(KICHID_FIELD, aKICHID);
        if(name!=null && name.length()>0) aPOI.setStringField(NAME_FIELD, name);
        if(desc!=null && desc.length()>0) aPOI.setStringField(DESCRIPTION_FIELD, desc);
        if(type!=null && type.length()>0 && Java.isInt(type)) aPOI.setIntField(TYPE_FIELD, Integer.parseInt(type));
        if(category!=null && category.length()>0) aPOI.setStringField(CATEGORY_FIELD, category);

        Point point = new Point(Double.parseDouble(x), Double.parseDouble(y));
        point.setSrid(28992);
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
            Record[] pois = oase.getFinder().queryTable(POI_TABLE, KICHID_FIELD + "='" + aKICHId + "'", null,  null);
            if(pois.length == 0) return null;
            if(pois.length > 1) throw new UtopiaException(pois.length + " poi's found with KICHID:" + aKICHId + ". This should not have happened");
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
        return poiElm;
    }

    /**
     * Gets all pois.
     *
     * @throws UtopiaException Standard exception
     */
    public Vector getList() throws UtopiaException {
        try {
            Record[] pois = oase.getFinder().queryTable(POI_TABLE, null);
            Vector results = new Vector(pois.length);
            for(int i=0;i<pois.length;i++){
                JXElement poiElm = pois[i].toXML();
                poiElm.setTag(POI_ELM);
                results.add(poiElm);
            }
            return results;
        } catch (OaseException oe) {
            throw new UtopiaException("Cannot read pois ", oe, ErrorCode.__6006_database_irregularity_error);
        }
    }

    /**
     * Gets all start point pois.
     *
     * @throws UtopiaException Standard exception
     */
    public Vector getStartPoints() throws UtopiaException {
        try {
            Record[] pois = oase.getFinder().queryTable(POI_TABLE, TYPE_FIELD + "='" + POI_STARTPOINT + "'", null, null);
            Vector results = new Vector(pois.length);
            for(int i=0;i<pois.length;i++){
                JXElement poiElm = pois[i].toXML();
                poiElm.setTag(POI_ELM);
                results.add(poiElm);
            }
            return results;
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
            Vector results = new Vector(pois.length);
            for(int i=0;i<pois.length;i++){
                JXElement poiElm = pois[i].toXML();
                poiElm.setTag(POI_ELM);
                results.add(poiElm);
            }
            return results;
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
            dataSource.cudPOI(POI_DELETE_ACTION, poi);

            oase.getModifier().delete(aPOIId);

        } catch (OaseException oe) {
            throw new UtopiaException("Cannot delete poi record with id=" + aPOIId, oe, ErrorCode.__6006_database_irregularity_error);
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
