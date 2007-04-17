package org.walkandplay.server.control;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Sys;
import org.keyworx.common.util.Rand;
import org.keyworx.utopia.core.config.ContentHandlerConfig;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.walkandplay.server.util.Constants;

/**
 * GamePlayHandler.
 * <p/>
 * Redirects the requests to the right logic method
 *
 * @author Ronald Lenz
 * @version $Id: GameScheduleHandler.java 327 2007-01-25 16:54:39Z just $
 */
public class GamePlayHandler extends DefaultHandler implements Constants {

	public final static String PLAY_START_SERVICE = "play-start";
	public final static String PLAY_GETSTATE_SERVICE = "play-getstate";
	public final static String PLAY_LOCATION_SERVICE = "play-location";
	public final static String PLAY_ANSWERTASK_SERVICE = "play-answertask";
	public final static String PLAY_GETSCORES_SERVICE = "play-getscores";

	private Log log = Logging.getLog("GamePlayHandler");
	private ContentHandlerConfig config;

	/**
	 * Processes the Client Request.
	 *
	 * @param anUtopiaRequest A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public UtopiaResponse processRequest(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		Log log = Logging.getLog(anUtopiaRequest);

		// Get the service name for the request
		String service = anUtopiaRequest.getServiceName();
		log.info("Handling request for service=" + service);
		log.info(new String(anUtopiaRequest.getRequestCommand().toBytes(false)));

		JXElement response;
		try {
			if (service.equals(PLAY_START_SERVICE)) {
				response = playStartReq(anUtopiaRequest);
			} else if (service.equals(PLAY_GETSTATE_SERVICE)) {
				response = playGetStateReq(anUtopiaRequest);
			} else if (service.equals(PLAY_LOCATION_SERVICE)) {
				response = playLocationReq(anUtopiaRequest);
			} else if (service.equals(PLAY_ANSWERTASK_SERVICE)) {
				response = playAnswerTaskReq(anUtopiaRequest);
			} else if (service.equals(PLAY_GETSCORES_SERVICE)) {
				response = playGetScoresReq(anUtopiaRequest);
			} else {
				log.warn("Unknown service " + service);
				response = createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
			}

			log.info("Handled service=" + service + " response=" + response.getTag());
			log.info(new String(response.toBytes(false)));
			return new UtopiaResponse(response);
		} catch (UtopiaException ue) {
			log.error("Negative response for service=" + service + "; exception:" + ue.getMessage());
			return new UtopiaResponse(createNegativeResponse(service, ue.getErrorCode(), "Error in request: " + ue.getMessage()));
		} catch (Throwable t) {
			log.error("Unexpected error in service : " + service, t);
			return new UtopiaResponse(createNegativeResponse(service, ErrorCode.__6005_Unexpected_error, "Unexpected error in request"));
		}
	}


	public JXElement playLocationReq(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		JXElement response = createResponse(PLAY_LOCATION_SERVICE);

		if (Rand.randomInt(0, 2) == 1) {
			JXElement hit = new JXElement(TAG_TASK_HIT);
			hit.setAttr(ID_FIELD, 1234);
			response.addChild(hit);
		}
		
		if (Rand.randomInt(0, 2) == 1 && !response.hasChildren()) {
			JXElement hit = new JXElement(TAG_MEDIUM_HIT);
			hit.setAttr(ID_FIELD, 5679);
			response.addChild(hit);
		}

		return response;
	}

	public JXElement playStartReq(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		return createResponse(PLAY_START_SERVICE);
	}

    public JXElement playAnswerTaskReq(UtopiaRequest anUtopiaRequest) throws UtopiaException {
/*
        <play-answertask-req id="[taskid]" answer="blabla" />
        <play-answertask-rsp answer="[boolean]" score="[nrofpoints] />
*/

        JXElement rsp = createResponse(PLAY_ANSWERTASK_SERVICE);
        rsp.setAttr("answer", "true");
        rsp.setAttr("score", "10");
        return rsp;
    }

    public JXElement playGetScoresReq(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		/*<play-getscores-req />
        <play-getscores-rsp>
            <score team="[teamname]>[scorecount]</score>
        </play-getscores-rsp>*/
        
        JXElement rsp = createResponse(PLAY_GETSCORES_SERVICE);
        JXElement score = new JXElement("score");
        score.setAttr("team", "red2");
        score.setText("60");
        rsp.addChild(score);
        return rsp;
    }


    public JXElement playGetStateReq(UtopiaRequest anUtopiaRequest) throws UtopiaException {
		/*<play-getstate-rsp>
					<tour id="234" name"sdvsdv" state="scheduled|running|done" />
				</play=getstate-rsp>*/
		// TODO: change this later on!!!!
		JXElement rsp = createResponse(PLAY_GETSTATE_SERVICE);
		JXElement t1 = new JXElement("game");
		t1.setAttr("id", "1");
		t1.setAttr("name", "Nieuwendijk pilot 1");
		t1.setAttr("state", "scheduled");
		rsp.addChild(t1);
		JXElement t2 = new JXElement("game");
		t2.setAttr("id", "2");
		t2.setAttr("name", "Nieuwendijk pilot 2");
		t2.setAttr("state", "running");
		rsp.addChild(t2);
		return rsp;
	}

	/**
	 * Overridden to have a hook to do the initialisation.
	 *
	 * @param aKey
	 * @param aValue
	 * @see org.keyworx.utopia.core.control.Handler#setProperty(String,String)
	 */
	public void setProperty(String aKey, String aValue) {
		if (aKey.equals("config")) {
			try {
				config = ContentHandlerConfig.getConfiguration(aValue);
			}
			catch (Exception e) {
				log.error("Exception while processing content handler configuration.", e);
				throw new RuntimeException("Exception while processing content handler configuration.", e);
			}

		}
		super.setProperty(aKey, aValue);
	}

}
