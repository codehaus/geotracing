package nl.diwi.control;

import nl.diwi.external.DataSource;
import nl.diwi.util.Constants;
import nl.diwi.util.NetConnection;
import nl.diwi.logic.POILogic;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.control.ThreadSafe;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.oase.api.Record;
import org.keyworx.amuse.core.Amuse;

import java.util.Vector;

public class KICHHandler extends DefaultHandler implements ThreadSafe, Constants {
    public final static String KICH_GET_MEDIA_SERVICE = "kich-get-media";
    public final static String KICH_GET_THEMES_SERVICE = "kich-get-themes";
    public final static String KICH_SYNC_SERVICE = "kich-sync";
    public final static String KICH_SYNC_MEDIA_SERVICE = "kich-sync-media";

    /**
     * Processes the Client Request.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws org.keyworx.utopia.core.data.UtopiaException
     *          standard Utopia exception
     */
    public UtopiaResponse processRequest(UtopiaRequest anUtopiaReq) throws UtopiaException {
        Log log = Logging.getLog(anUtopiaReq);

        // Get the service name for the request
        String service = anUtopiaReq.getServiceName();
        log.trace("Handling request for service=" + service);

        JXElement response;
        try {
            if (service.equals(KICH_GET_MEDIA_SERVICE)) {
                // get all kich media
                response = getMedia(anUtopiaReq);
            } else if (service.equals(KICH_GET_THEMES_SERVICE)) {
                // get all kich media
                response = getThemes(anUtopiaReq);
            } else if (service.equals(KICH_SYNC_SERVICE)) {
                // get all kich media
                response = syncKICH(anUtopiaReq);
            } else if (service.equals(KICH_SYNC_MEDIA_SERVICE)) {
                // get all kich media
                response = syncMediaForPois(anUtopiaReq);
            } else {
                // May be overridden in subclass
                response = unknownReq(anUtopiaReq);
            }

        } catch (UtopiaException ue) {
            log.warn("Negative response service=" + service, ue);
            response = createNegativeResponse(service, ue.getErrorCode(), ue.getMessage());
        } catch (Throwable t) {
            log.error("Unexpected error service=" + service, t);
            response = createNegativeResponse(service, ErrorCode.__6005_Unexpected_error, "Unexpected error in request " + t);
        }

        // Always return a response
        log.trace("Handled service=" + service + " response=" + response.getTag());
        return new UtopiaResponse(response);
    }

    private JXElement getThemes(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement response = createResponse(KICH_GET_THEMES_SERVICE);
        DataSource ds = new DataSource(anUtopiaReq.getUtopiaSession().getContext().getOase());
        response.addChildren(ds.getKICHThemes());

        return response;
    }

    private JXElement syncKICH(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement response = createResponse(KICH_SYNC_SERVICE);
        DataSource ds = new DataSource(anUtopiaReq.getUtopiaSession().getContext().getOase());
        ds.syncPOIs();
        ds.syncFixedRoutes();

        return response;
    }

    private JXElement syncMediaForPois(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement response = createResponse(KICH_SYNC_MEDIA_SERVICE);
        try {
            Oase oase = anUtopiaReq.getUtopiaSession().getContext().getOase();
            POILogic poiLogic = new POILogic(oase);
            Record[] pois = oase.getFinder().readAll(POI_TABLE);
            for(int i=0;i<pois.length;i++){
                Record poi = pois[i];
                JXElement media = poi.getXMLField(MEDIA_FIELD);
                if(media!=null){
                    Vector kichUris = media.getChildrenByTag("kich-uri");
                    Vector mediumList = new Vector(kichUris.size());
                    for(int j=0;j<kichUris.size();j++){
                        JXElement kichUri = (JXElement)kichUris.elementAt(j);
                        String s = kichUri.getText();
                        String fileName = s.substring(s.lastIndexOf("/") + 1, s.length());
                        JXElement medium = new JXElement("medium");
                        medium.setText(fileName);
                        mediumList.add(medium);
                    }
                    int id = poi.getId();

                    // now first unrelate the media from the pois to start clean
                    poiLogic.unrelateMedia(id, mediumList);

                    // now relate them all again
                    poiLogic.relateMedia(id, mediumList);
                }
            }

        } catch (Throwable t) {
            throw new UtopiaException(t);
        }

        return response;
    }

    /**
     * Gets all media.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws org.keyworx.utopia.core.data.UtopiaException
     *          standard Utopia exception
     */
    protected JXElement getMedia(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement response = createResponse(KICH_GET_MEDIA_SERVICE);
        DataSource dataSource = new DataSource(anUtopiaReq.getUtopiaSession().getContext().getOase());
        JXElement media = dataSource.getKICHMedia();
        if (media != null) response.addChild(media);
        return response;
    }

    /**
     * Default implementation for unknown service request.
     * <p/>
     * Override this method in extended class for handling additional
     * requests.
     *
     * @param anUtopiaReq A UtopiaRequest
     * @return A negative UtopiaResponse.
     * @throws org.keyworx.utopia.core.data.UtopiaException
     *          Standard Utopia exception
     */
    protected JXElement unknownReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
        String service = anUtopiaReq.getServiceName();
        Logging.getLog(anUtopiaReq).warn("Unknown service " + service);
        return createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
    }

}
