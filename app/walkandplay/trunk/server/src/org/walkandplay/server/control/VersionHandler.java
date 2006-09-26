/*
*	cd:	Thu May 06 16:33:32 CEST 2004
*	author:	Ronald Lenz
*
*	$Id: RssHandler.java,v 1.1 2005/08/18 08:43:52 rlenz Exp $
*************************************************************/

package org.walkandplay.server.control;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;
import org.walkandplay.server.logic.VersionLogic;

/**
 * RssHandler.
 * <p/>
 * Redirects the requests to the right logic method
 *
 * @author Ronald Lenz
 * @version $Id: RssHandler.java,v 1.1 2005/08/18 08:43:52 rlenz Exp $
 */
public class VersionHandler extends DefaultHandler {

	public final static String VERSION_GET_SERVICE = "version-get";
	public final static String VERSION_INSERT_SERVICE = "version-insert";

	/**
	 * Processes the Client Request.
	 *
	 * @param anUtopiaRequest A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws UtopiaException Standard Utopia exception
	 */
	public UtopiaResponse processRequest(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		Log log = Logging.getLog(anUtopiaRequest);

		// Get the service name for the request
		String service = anUtopiaRequest.getServiceName();
		log.info("Handling request for service=" + service);

		JXElement response = null;
		try {
			if (service.equals(VERSION_GET_SERVICE)) {
				response = getVersion(anUtopiaRequest);
			} else if (service.equals(VERSION_INSERT_SERVICE)) {
				response = createVersion(anUtopiaRequest);
			} else {
				log.warn("Unknown service " + service);
				response = createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
			}
		} catch (UtopiaException ue) {
			log.warn("Negative response for service=" + service);
			response = createNegativeResponse(service, ue.getErrorCode(), "Error in request: " + ue.getMessage());
		} catch (Throwable t) {
			log.error("Unexpected error in service : " + service, t);
			response = createNegativeResponse(service, ErrorCode.__6005_Unexpected_error, "Unexpected error in request");
		} finally {
			// Always return a response
			log.info("Handled service=" + service + " response=" + response.getTag());
			return new UtopiaResponse(response);
		}
	}

	public JXElement getVersion(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		try {
			Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
			VersionLogic logic = new VersionLogic(oase);

			JXElement responseElement = createResponse(VERSION_GET_SERVICE);
			responseElement.addChild(logic.getVersion());

			return responseElement;
		} catch (UtopiaException ue) {
			throw ue;
		} catch (Throwable t) {
			throw new UtopiaException(t.toString());
		}
	}

	public JXElement createVersion(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		try {
			JXElement requestElement = anUtopiaRequest.getRequestCommand();
			String versionNr = requestElement.getAttr("version");
			String description = requestElement.getAttr("description");

			Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
			VersionLogic logic = new VersionLogic(oase);

			logic.createVersion(versionNr, description, VersionLogic.ACTIVE_STATE);

			JXElement responseElement = createResponse(VERSION_INSERT_SERVICE);

			return responseElement;
		} catch (UtopiaException ue) {
			throw ue;
		} catch (Throwable t) {
			throw new UtopiaException(t.toString());
		}
	}


}

/*
* $Log:
*
*/