// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

package org.geotracing.server;

import com.messners.mail.POP3MailMessage;
import nl.justobjects.jox.dom.JXElement;
import org.keyworx.amuse.core.AmuseException;
import org.keyworx.amuse.core.Protocol;
import org.keyworx.amuse.daemon.EmailUploadDaemon;
import org.keyworx.client.KWClient;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.util.Oase;


/**
 * Handles binding uploaded Media to Locations.
 *
 * @version $Id$
 * @author Just van den Broecke
 */
public class LocEmailUploadDaemon extends EmailUploadDaemon {

	public LocEmailUploadDaemon() {
	}

	/** Callback for overload to intercept by client. */
	protected JXElement beforeUpload(String aFromAddress, String aSubject, KWClient aKWClient, JXElement aMediumInsertReq) {
		// client may implement
		// log.info("beforeRecordInsert: file=" + theFile.getName() + " mime=" + theFields.get("mime"));
		return aMediumInsertReq;
	}

	/** Callback for overload to intercept by client. */
	protected void afterUpload(POP3MailMessage aMsg, KWClient aKWClient, JXElement aMediumInsertRsp) {

		String id = aMediumInsertRsp.getAttr("id", null);
		try {
			log.info("afterUpload id=" + id);

			// medium-insert-rsp carries one or more medium id's seperated by comma's
			String[] mediumIds = aMediumInsertRsp.getAttr("id").split(",");
			for (int i = 0; i < mediumIds.length; i++) {
				JXElement req = Protocol.createRequest(TracingHandler.T_TRK_ADD_MEDIUM_SERVICE);
				req.setAttr("id", mediumIds[i]);
				JXElement rsp = aKWClient.performUtopiaRequest(req);
				if (Protocol.isNegativeResponse(rsp)) {
					log.warn("Negative loc-add-medium-rsp for medium id=" + mediumIds[i] + " error=" + rsp.getAttr("error") + " details=" + rsp.getAttr("details"));
					continue;
				}
				log.info("OK related medium id=" + mediumIds[i] + " to location id=" + rsp.getId());
			}
		} catch (Throwable t) {
			log.warn("Unexpected error file: from=" + extractSenderEmail(aMsg) + " id=" + id, t);
		}
	}

	/**
	 * Find a Person id related to the email and message
	 *
	 * Override this for special find algoritms/
	 */
	protected String findPerson(POP3MailMessage aMsg, KWClient anAdminClient) throws AmuseException {
		String personId = null;
		try {
			personId = super.findPerson(aMsg, anAdminClient);
		} catch (Throwable t) {
			// not found
		}

		if (personId != null) {
			return personId;
		}

		try {
			String email = extractSenderEmail(aMsg);

			Oase oase = getContext().createOaseSession();
			Record[] personRecords = oase.getFinder().queryTable("utopia_person", "WHERE email LIKE '%" + email + "%'");
			if (personRecords.length != 1) {
				throw new AmuseException(personRecords.length + " persons found for email=" + email);
			}
			return personRecords[0].getId() + "";
		} catch (AmuseException ae) {
			throw ae;
		} catch (Throwable t) {
			throw new AmuseException("unexpected error during findPerson email=" + extractSenderEmail(aMsg), t);
		}

	}

}
