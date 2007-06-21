package nl.diwi.logic;

import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;

import java.util.Properties;
import java.util.Vector;

public class TrafficLogic implements Constants {
    private Oase oase;
    private Log log = Logging.getLog("TrafficLogic");
    private static final Properties properties = new Properties();

    public TrafficLogic(Oase o) {
        oase = o;
    }

    public void storeTraffic(String aPersonId, JXElement aRequest, JXElement aResponse) throws UtopiaException {
        try {
            Record r = oase.getModifier().create(TRAFFIC_TABLE);
            r.setXMLField(REQUEST_FIELD, aRequest);
            r.setXMLField(RESPONSE_FIELD, aResponse);
            oase.getModifier().insert(r);
            // relate the traffic to the person            
            oase.getRelater().relate(oase.getFinder().read(Integer.parseInt(aPersonId)), r);
        } catch (Throwable t) {
            log.error("Exception storing the traffic: " + t.toString());
            throw new UtopiaException(t);
        }
    }

    public Vector getAllTraffic() throws UtopiaException {
        try {
            Record[] recs = oase.getFinder().readAll(TRAFFIC_TABLE);
            Vector result = new Vector(recs.length);
            for (int i = 0; i < recs.length; i++) {
                JXElement t = recs[i].toXML();
                t.setTag(TRAFFIC_ELM);
                result.add(t);
            }
            return result;
        } catch (Throwable t) {
            log.error("Exception reading the traffic: " + t.toString());
            throw new UtopiaException(t);
        }
    }

    public Vector getTrafficForPerson(String aPersonId) throws UtopiaException {
        try {
            Record[] recs = oase.getRelater().getRelated(oase.getFinder().read(Integer.parseInt(aPersonId)), TRAFFIC_TABLE, null);
            Vector result = new Vector(recs.length);
            for (int i = 0; i < recs.length; i++) {
                JXElement t = recs[i].toXML();
                t.setTag(TRAFFIC_ELM);
                result.add(t);
            }
            return result;
        } catch (Throwable t) {
            log.error("Exception reading the traffic: " + t.toString());
            throw new UtopiaException(t);
        }
    }

    /**
     * Properties passed on from Handler.
     */
    public static void setProperty(String propertyName, String propertyValue) {
        properties.put(propertyName, propertyValue);
    }
}
