// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.handler;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.api.OaseException;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.Account;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;
import org.keyworx.utopia.core.session.UtopiaSessionContext;
import org.keyworx.utopia.core.util.Oase;

/**
 * Handles all operations related to commenting.
 * <p/>
 * Redirects the requests to CommentLogic methods.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class CommentHandler extends DefaultHandler {
	public final static String CMT_INSERT_SERVICE = "cmt-insert";
	public final static String CMT_READ_SERVICE = "cmt-read";
	public final static String CMT_UPDATE_STATE_SERVICE = "cmt-update-state";
	public final static String CMT_DELETE_SERVICE = "cmt-delete";
	public final static String ATTR_ID = "id";
	public final static String ATTR_IDS = "ids";
	public final static String ATTR_STATE = CommentLogic.FIELD_STATE;
	public final static String ATTR_TARGET = CommentLogic.FIELD_TARGET;
	public final static String ATTR_TARGET_PERSON = CommentLogic.FIELD_TARGET_PERSON;

	/**
	 * Processes the Client Request.
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws UtopiaException standard Utopia exception
	 */
	public UtopiaResponse processRequest(UtopiaRequest anUtopiaReq) throws UtopiaException {
		Log log = Logging.getLog(anUtopiaReq);

		// Get the service name for the request
		String service = anUtopiaReq.getServiceName();
		log.trace("Handling request for service=" + service);

		JXElement response;
		try {
			if (service.equals(CMT_INSERT_SERVICE)) {
				// Add new comment
				response = insertReq(anUtopiaReq);
			} else if (service.equals(CMT_READ_SERVICE)) {
				// Read comments by example
				response = readReq(anUtopiaReq);
			} else if (service.equals(CMT_UPDATE_STATE_SERVICE)) {
				// Update comment state
				response = updateStateReq(anUtopiaReq);
			} else if (service.equals(CMT_DELETE_SERVICE)) {
				// Delete a comment by id
				response = deleteReq(anUtopiaReq);
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


	/**
	 * Insert new Comment.
	 * <p/>
	 * Examples
	 * <code>
	 * &lt;cmt-insert-req &gt; &lt;
	 * <p/>
	 * &lt;cmt-insert-rsp id="cmt-id" &gt; &lt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	protected JXElement insertReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();

		// Insert Comment object
		CommentLogic logic = createLogic(anUtopiaReq);
		Record record = logic.createRecord();

		// Set fields directly from request
		// May throw IllegalArgumentException if non-existing fields added
		record.setFields(reqElm);

		// Add user id as owner
		record.setIntField(CommentLogic.FIELD_OWNER, getPersonId(anUtopiaReq));

		// Add person id as comment owner
		logic.insert(record);

		// Create and return response with open comment id.
		JXElement response = createResponse(CMT_INSERT_SERVICE);
		response.setAttr(ATTR_ID, record.getId());

		EventPublisher.commentAdd(record, anUtopiaReq.getUtopiaSession().getContext().getOase());

		return response;
	}

	/**
	 * Read Comments by example.
	 * <p/>
	 * Examples
	 * <code>
	 * &lt;cmt-read-req &gt;
	 * <p/>
	 * &lt;cmt-read-rsp &lt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws UtopiaException standard Utopia exception
	 */
	protected JXElement readReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();

		// Create Comment example object
		CommentLogic logic = createLogic(anUtopiaReq);
		Record exampleRecord = logic.createExampleRecord();

		// Set the fields (these will be matched exactly)
		if (reqElm.getChildByTag(ATTR_ID) != null) {
			// If only id specified: set explicitly (this matches only one record)
			exampleRecord.setField(ATTR_ID, reqElm.getChildText(ATTR_ID));
		} else {
			// Set zero/or more fields (no fields matches all comments)
			exampleRecord.setFields(reqElm);
		}

		// Do the query by example
		Record[] result = logic.read(exampleRecord);

		// Create and return response.
		JXElement response = createResponse(CMT_READ_SERVICE);
		JXElement nextChild;
		for (int i = 0; i < result.length; i++) {
			nextChild = result[i].toXML();
			// Remove redundant attrs
			nextChild.removeAttr("table");
			nextChild.removeAttr(ATTR_ID);
			response.addChild(nextChild);
		}

		return response;
	}

	/**
	 * Update Comment state.
	 * <p/>
	 * Examples
	 * <code>
	 * &lt;cmt-update-state-req id="cmt-id" state="new-state" &gt; &lt;
	 * <p/>
	 * &lt;cmt-update-state-rsp id="cmt-id" state="new-state" &gt; &lt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws UtopiaException Standard Utopia exception
	 */
	protected JXElement updateStateReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();
		throwOnNonNumAttr(CommentLogic.FIELD_STATE, reqElm.getAttr(CommentLogic.FIELD_STATE));

		int state = reqElm.getIntAttr(ATTR_STATE);
		int id = reqElm.getIntAttr(ATTR_ID);
		int targetId = reqElm.getIntAttr(ATTR_TARGET);
		int targetPersonId = getPersonId(anUtopiaReq);

		String ids = createLogic(anUtopiaReq).updateState(targetPersonId, state, id, targetId);

		// Create and return response
		JXElement response = createResponse(CMT_UPDATE_STATE_SERVICE);
		response.setAttr(ATTR_IDS, ids);

		return response;
	}

	/**
	 * Delete comment.
	 * <p/>
	 * <p/>
	 * Example
	 * <code>
	 * &lt;cmt-delete-req [id="cmt-id"] /&gt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws UtopiaException standard Utopia exception
	 */
	protected JXElement deleteReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();
		throwOnNonNumAttr(ATTR_ID, reqElm.getAttr(ATTR_ID));

		// Only comment owner or target person can delete a  comment.
		if (!isOwnerOrTargetPerson(anUtopiaReq.getUtopiaSession().getContext().getOase(), getPersonId(anUtopiaReq), reqElm.getIntAttr(ATTR_ID)))
		{
			throw new UtopiaException("You must be owner or target person of comment to delete", ErrorCode.__6007_insufficient_rights_error);
		}

		createLogic(anUtopiaReq).delete(reqElm.getIntAttr(ATTR_ID));

		// Create and return response
		JXElement response = createResponse(CMT_DELETE_SERVICE);
		response.setAttr(ATTR_ID, reqElm.getAttr(ATTR_ID));

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

	/** Utility methods. */

	/**
	 * Intercept and pass properties to CommentLogic.
	 * <p/>
	 * Since the Handler has no init() we do it this way
	 * for the time being....
	 */
	public void setProperty(String propertyName, String propertyValue) {
		super.setProperty(propertyName, propertyValue);
		CommentLogic.setProperty(propertyName, propertyValue);
	}

	/**
	 * Get user (Person) id from request.
	 */
	protected CommentLogic createLogic(UtopiaRequest anUtopiaReq) throws UtopiaException {
		return new CommentLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());
	}

	/**
	 * Get user (Person) id from request.
	 */
	protected int getPersonId(UtopiaRequest anUtopiaReq) throws UtopiaException {
		return Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());
	}

	/**
	 * Is person the owner or target person of comment ?
	 */
	protected boolean isOwnerOrTargetPerson(Oase anOase, int aPersonId, int aCommentId) throws UtopiaException {
		Record record;
		try {
			record = anOase.getFinder().read(aCommentId, CommentLogic.TABLE_COMMENT);
		} catch (OaseException oe) {
			throw new UtopiaException("Cannot read comment with id=" + aCommentId, ErrorCode.__6004_Invalid_attribute_value);
		}

		return record.getIntField(CommentLogic.FIELD_OWNER) == aPersonId || record.getIntField(CommentLogic.FIELD_TARGET_PERSON) == aPersonId;
	}

	/**
	 * Is person target person of comment ?
	 */
	protected boolean isTargetPerson(Oase anOase, int aPersonId, int aCommentId) throws UtopiaException {
		Record record;
		try {
			record = anOase.getFinder().read(aCommentId, CommentLogic.TABLE_COMMENT);
		} catch (OaseException oe) {
			throw new UtopiaException("Cannot read comment with id=" + aCommentId, ErrorCode.__6004_Invalid_attribute_value);
		}

		return record.getIntField(CommentLogic.FIELD_TARGET_PERSON) == aPersonId;
	}

	/**
	 * Throw exception when attribute empty or not present.
	 */
	protected void throwOnMissingAttr(String aName, String aValue) throws UtopiaException {
		if (aValue == null || aValue.length() == 0) {
			throw new UtopiaException("Missing name=" + aName + " value=" + aValue, ErrorCode.__6002_Required_attribute_missing);
		}
	}

	/**
	 * Throw exception when numeric attribute empty or not present.
	 */
	protected void throwOnNonNumAttr(String aName, String aValue) throws UtopiaException {
		throwOnMissingAttr(aName, aValue);
		try {
			Long.parseLong(aValue);
		} catch (Throwable t) {
			throw new UtopiaException("Invalid numvalue name=" + aName + " value=" + aValue, ErrorCode.__6004_Invalid_attribute_value);
		}
	}

	/**
	 * Throw exception when numeric attribute empty or not present.
	 */
	protected void throwNegNumAttr(String aName, long aValue) throws UtopiaException {
		if (aValue == -1) {
			throw new UtopiaException("Invalid numvalue name=" + aName + " value=" + aValue, ErrorCode.__6004_Invalid_attribute_value);
		}
	}
}
