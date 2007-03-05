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
            Record poi = oase.getModifier().create(POI_TABLE);
            poi.setStringField(NAME_FIELD, aPOIElement.getChildText(NAME_FIELD));
            poi.setStringField(DESCRIPTION_FIELD, aPOIElement.getChildText(DESCRIPTION_FIELD));

            Point point = new Point(Double.parseDouble(aPOIElement.getChildText(X_FIELD)), Double.parseDouble(aPOIElement.getChildText(Y_FIELD)));
			point.setSrid(28992);
			PGgeometryLW geom = new PGgeometryLW(point);
			poi.setObjectField(POINT_FIELD, geom);

            poi.setXMLField(MEDIA_FIELD, aPOIElement.getChildByTag(MEDIA_FIELD));

            oase.getModifier().insert(poi);

            dataSource.cudPOI(POI_INSERT_ACTION, aPOIElement);
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
            Record poi = oase.getFinder().read(aPOIId);
            poi.setStringField(NAME_FIELD, aPOIElement.getChildText(NAME_FIELD));
            poi.setStringField(DESCRIPTION_FIELD, aPOIElement.getChildText(DESCRIPTION_FIELD));

            Point point = new Point(Double.parseDouble(aPOIElement.getChildText(X_FIELD)), Double.parseDouble(aPOIElement.getChildText(Y_FIELD)));
            point.setSrid(28992);
            PGgeometryLW geom = new PGgeometryLW(point);
            poi.setObjectField(POINT_FIELD, geom);

            poi.setXMLField(MEDIA_FIELD, aPOIElement.getChildByTag(MEDIA_FIELD));

            oase.getModifier().update(poi);

            JXElement idElm = new JXElement(ID_FIELD);
            idElm.setText("" + aPOIId);
            aPOIElement.addChild(idElm);
            dataSource.cudPOI(POI_UPDATE_ACTION, aPOIElement);

        } catch (OaseException oe) {
            throw new UtopiaException("Cannot update poi record", oe, ErrorCode.__6006_database_irregularity_error);
        } catch (Throwable t) {
            throw new UtopiaException("Exception in POILogic.update() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
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
            oase.getModifier().delete(aPOIId);
            JXElement poi = new JXElement(POI_ELM);
            JXElement id = new JXElement(ID_FIELD);
            id.setText("" + aPOIId);
            poi.addChild(id);

            dataSource.cudPOI(POI_DELETE_ACTION, poi);
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