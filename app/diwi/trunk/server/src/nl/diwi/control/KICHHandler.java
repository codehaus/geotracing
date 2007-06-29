package nl.diwi.control;

import nl.diwi.external.DataSource;
import nl.diwi.util.Constants;
import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;

public class KICHHandler extends DefaultHandler implements Constants {
    public final static String KICH_GET_MEDIA_SERVICE = "kich-get-media";
    public final static String KICH_GET_THEMES_SERVICE = "kich-get-themes";
    public final static String KICH_SYNC_SERVICE = "kich-sync";

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

    private JXElement getThemes(UtopiaRequest anUtopiaReq) {
        JXElement response = createResponse(KICH_GET_THEMES_SERVICE);

        response.addTextChild(THEME_ELM, "Kerk en kerkonderdeel");
        response.addTextChild(THEME_ELM, "Klooster");
        response.addTextChild(THEME_ELM, "Gedenkteken");
        response.addTextChild(THEME_ELM, "Brug");
        response.addTextChild(THEME_ELM, "Gracht");
        response.addTextChild(THEME_ELM, "Fort en vesting");
        response.addTextChild(THEME_ELM, "Erfscheiding");
        response.addTextChild(THEME_ELM, "Industrie- en poldermolen");
        response.addTextChild(THEME_ELM, "Begraafplaats");
        response.addTextChild(THEME_ELM, "Kerkelijke dienstwoning");
        response.addTextChild(THEME_ELM, "Boerderij");
        response.addTextChild(THEME_ELM, "Militair verblijfsgebouw");
        response.addTextChild(THEME_ELM, "Waterkering en -doorlaat");
        response.addTextChild(THEME_ELM, "Gemaal");
        response.addTextChild(THEME_ELM, "Grensafbakening");
        response.addTextChild(THEME_ELM, "Kasteel, buitenplaats");
        response.addTextChild(THEME_ELM, "Kapel");
        response.addTextChild(THEME_ELM, "Bijgebouwen kastelen");
        response.addTextChild(THEME_ELM, "Omwalling");

        return response;
    }

    private JXElement syncKICH(UtopiaRequest anUtopiaReq) throws UtopiaException {
        JXElement response = createResponse(KICH_SYNC_SERVICE);
        DataSource ds = new DataSource(anUtopiaReq.getUtopiaSession().getContext().getOase());
        ds.syncFixedRoutes();
        ds.syncPOIs();

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
        if(media!=null) response.addChild(media);
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
