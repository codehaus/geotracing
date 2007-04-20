// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

package org.walkandplay.server.daemon;

import com.messners.mail.POP3MailMessage;
import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.TracingHandler;
import org.keyworx.amuse.core.Protocol;
import org.keyworx.amuse.daemon.EmailUploadDaemon;
import org.keyworx.client.KWClient;
import org.walkandplay.server.control.GamePlayHandler;


/**
 * Handles binding uploaded Media to Locations.
 *
 * @version $Id$
 * @author Just van den Broecke
 */
public class WPEmailUploadDaemon extends EmailUploadDaemon {

	public WPEmailUploadDaemon() {
	}

	/** Callback for overload to intercept by client. */
	protected void afterUpload(POP3MailMessage aMsg, KWClient aKWClient, JXElement aMediumInsertRsp) {

		String id = aMediumInsertRsp.getAttr("id", null);
		try {
			log.info("afterUpload id=" + id);

			// medium-insert-rsp carries one or more medium id's seperated by comma's
			String[] mediumIds = aMediumInsertRsp.getAttr("id").split(",");
			for (int i = 0; i < mediumIds.length; i++) {
				JXElement req = Protocol.createRequest(GamePlayHandler.PLAY_ADD_MEDIUM_SERVICE);
				req.setAttr("id", mediumIds[i]);
				JXElement rsp = aKWClient.performUtopiaRequest(req);
				if (Protocol.isNegativeResponse(rsp)) {
					log.warn("Negative play-add-medium-rsp for medium id=" + mediumIds[i] + " error=" + rsp.getAttr("error") + " details=" + rsp.getAttr("details"));
					continue;
				}
				log.info("play-add-medium OK  medium id=" + mediumIds[i]);
			}
		} catch (Throwable t) {
			log.warn("Unexpected error file: from=" + extractSenderEmail(aMsg) + " id=" + id, t);
		}
	}

}
