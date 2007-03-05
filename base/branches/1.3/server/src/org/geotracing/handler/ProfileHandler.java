// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.handler;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.util.Oase;

/**
 * Handles management of user profiles.
 * <p/>
  *
 * @author Just van den Broecke
 * @version $Id$
 */
public class ProfileHandler extends DefaultHandler {

    public final static String PROFILE_GET_SERVICE = "profile-get";
    public final static String PROFILE_CREATE_SERVICE = "profile-create";
    public final static String PROFILE_UPDATE_SERVICE = "profile-update";
    public final static String PROFILE_ACTIVATE_SERVICE = "profile-activate";
    public final static String PROFILE_RESETPASSWORD_SERVICE = "profile-resetpassword";
    public final static String PROFILE_SENDJAD_SERVICE = "profile-sendjad";

    private Log log = Logging.getLog("ProfileHandler");
	private Class logicClass;

	/**
     * Processes the Client Request.
     *
     * @param anUtopiaRequest A UtopiaRequest
     * @return A UtopiaResponse.
     * @throws UtopiaException Standard Utopia exception
     */
    public UtopiaResponse processRequest(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        // Get the service name for the request
        String service = anUtopiaRequest.getServiceName();
        log.info("Handling request for service=" + service);

        JXElement response;
        try {
			if (service.equals(PROFILE_GET_SERVICE)) {
                response = getProfile(anUtopiaRequest);
			} else if (service.equals(PROFILE_UPDATE_SERVICE)) {
				response = updateProfile(anUtopiaRequest);
            } else {
                log.warn("Unknown service " + service);
                response = createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
            }

            log.info("Handled service=" + service + " response=" + response.getTag());
            return new UtopiaResponse(response);
        } catch (UtopiaException ue) {
            log.error("Negative response for service: " + service, ue);
            return new UtopiaResponse(createNegativeResponse(service, ue.getErrorCode(), "Error in request: " + ue.getMessage()));
        } catch (Throwable t) {
            log.error("Unexpected error in service: " + service, t);
            return new UtopiaResponse(createNegativeResponse(service, ErrorCode.__6005_Unexpected_error, "Unexpected error in request"));
        }
    }


    /**
     * get a user profile
     *
     * @param anUtopiaRequest The request holding all needed parameters
     * @return a utopia response
     * @throws UtopiaException
     */
    public JXElement getProfile(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {

            JXElement profile = createLogic(anUtopiaRequest).getProfile(HandlerUtil.getUserId(anUtopiaRequest));

            JXElement response = createResponse(PROFILE_GET_SERVICE);
			response.addChild(profile);
			return response;
		} catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

	/**
     * Updates a user profile
     *
     * @param anUtopiaRequest The request holding all needed parameters
     * @return a utopia response
     * @throws UtopiaException
     */
    public JXElement updateProfile(UtopiaRequest anUtopiaRequest) throws UtopiaException {
        try {
            JXElement requestElement = anUtopiaRequest.getRequestCommand();
			HandlerUtil.throwOnMissingChildElement(requestElement, ProfileLogic.TAG_PROFILE);

			JXElement profileElm = requestElement.getChildByTag(ProfileLogic.TAG_PROFILE);

			// Use id of logged-in user (person) if not provided
			if (!profileElm.hasAttr("id")) {
				profileElm.setAttr("id", HandlerUtil.getUserId(anUtopiaRequest));
			}

            createLogic(anUtopiaRequest).updateProfile(profileElm);

            return createResponse(PROFILE_UPDATE_SERVICE);
        } catch (UtopiaException ue) {
            throw ue;
        } catch (Throwable t) {
            throw new UtopiaException(t);
        }
    }

    /**
     * Overridden to have a hook to do the initialisation.
     *
     * @param aKey
     * @param aValue
     * @see org.keyworx.utopia.core.control.Handler#setProperty(java.lang.String,java.lang.String)
     */
    public void setProperty(String aKey, String aValue) {
        if (aKey.equals("logic")) {
            try {
                logicClass = Class.forName(aValue);
            }
            catch (Exception e) {
                log.error("Exception while processing profile handler configuration.", e);
                throw new RuntimeException("Exception while processing profile handler configuration.", e);
            }

        }
        super.setProperty(aKey, aValue);
    }

	protected ProfileLogic createLogic(UtopiaRequest anUtopiaRequest) throws UtopiaException  {
		try {
			Oase oase = anUtopiaRequest.getUtopiaSession().getContext().getOase();
			Object[] args={oase};
			Class[] argTypes={Oase.class};
			return (ProfileLogic) logicClass.getConstructor(argTypes).newInstance(args);
		} catch (Throwable t) {
			throw new UtopiaException("Cannot create logic class: " + logicClass.getName(), t);
		}
	}

}
