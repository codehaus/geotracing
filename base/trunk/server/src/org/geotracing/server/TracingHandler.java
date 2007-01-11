// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.server;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.IO;
import org.keyworx.oase.api.MediaFiler;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.control.DefaultHandler;
import org.keyworx.utopia.core.data.*;
import org.keyworx.utopia.core.session.*;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.amuse.core.Protocol;

import java.util.HashMap;
import java.util.Vector;
import java.io.File;
import java.sql.Timestamp;

/**
 * Handles all operations related to Tracks.
 * <p/>
 * Redirects the requests to TrackLogic methods.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class TracingHandler extends DefaultHandler {
	public final static String T_HEARTBEAT_SERVICE = "t-hb";
	public final static String T_TRK_CREATE_SERVICE = "t-trk-create";
	public final static String T_TRK_SUSPEND_SERVICE = "t-trk-suspend";
	public final static String T_TRK_RESUME_SERVICE = "t-trk-resume";
	public final static String T_TRK_WRITE_SERVICE = "t-trk-write";
	public final static String T_TRK_READ_SERVICE = "t-trk-read";
	public final static String T_TRK_IMPORT_SERVICE = "t-trk-import";
	public final static String T_TRK_EXPORT_SERVICE = "t-trk-export";
	public final static String T_TRK_DELETE_SERVICE = "t-trk-delete";
	public final static String T_TRK_ADD_MEDIUM_SERVICE = "t-trk-add-medium";
	public final static String T_TRK_DELETE_MEDIUM_SERVICE = "t-trk-delete-medium";
	public final static String T_TRK_UPLOAD_MEDIUM_SERVICE = "t-trk-upload-medium";

	public final static String TAG_DATA = "data";
	public final static String ATTR_NAME = "name";
	public final static String ATTR_DESCRIPTION = "description";
	public final static String ATTR_ENCODING = "encoding";
	public final static String ATTR_EXTRA = "extra";
	public final static String ATTR_FILE = "file";
	public final static String ATTR_TYPE = "type";
	public final static String ATTR_MIME = "mime";
	public final static String ATTR_FORMAT = "format";
	public final static String ATTR_MINDIST = "mindist";
	public final static String ATTR_MAXPOINTS = "maxpoints";
	public final static String ATTR_MODE = "mode";
	public final static String ATTR_ID = "id";
	public final static String ATTR_STATE = "state";
	public final static String ATTR_T = "t";
	public final static String ATTR_TAGS = "tags";
	public final static String ATTR_VALUE = "value";
	public final static String ATTR_ATTRS = "attrs";
	public final static String ATTR_MEDIA = "media";
	public final static String ATTR_POIS = "pois";
	public final static String VAL_SPOT = "spot";

	protected TrackLogic trackLogic;

	/**
	 * Processes the Client Request.
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public UtopiaResponse processRequest(UtopiaRequest anUtopiaReq) throws UtopiaException {
		Log log = Logging.getLog(anUtopiaReq);

		trackLogic = new TrackLogic(anUtopiaReq.getUtopiaSession().getContext().getOase());

		// Get the service name for the request
		String service = anUtopiaReq.getServiceName();
		log.trace("Handling request for service=" + service);

		JXElement response = null;
		try {
			if (service.equals(T_TRK_WRITE_SERVICE)) {
				response = writeReq(anUtopiaReq);
			} else if (service.equals(T_HEARTBEAT_SERVICE)) {
				response = heartbeatReq(anUtopiaReq);
			} else if (service.equals(T_TRK_CREATE_SERVICE)) {
				response = createReq(anUtopiaReq);
			} else if (service.equals(T_TRK_SUSPEND_SERVICE)) {
				response = suspendReq(anUtopiaReq);
			} else if (service.equals(T_TRK_RESUME_SERVICE)) {
				response = resumeReq(anUtopiaReq);
			} else if (service.equals(T_TRK_READ_SERVICE)) {
				response = readReq(anUtopiaReq);
			} else if (service.equals(T_TRK_DELETE_SERVICE)) {
				response = deleteReq(anUtopiaReq);
			} else if (service.equals(T_TRK_EXPORT_SERVICE)) {
				response = exportReq(anUtopiaReq);
			} else if (service.equals(T_TRK_IMPORT_SERVICE)) {
				response = importReq(anUtopiaReq);
			} else if (service.equals(T_TRK_ADD_MEDIUM_SERVICE)) {
				response = addMediumReq(anUtopiaReq);
			} else if (service.equals(T_TRK_UPLOAD_MEDIUM_SERVICE)) {
				response = uploadMediumReq(anUtopiaReq);
			} else if (service.equals(T_TRK_DELETE_MEDIUM_SERVICE)) {
				response = deleteMediumReq(anUtopiaReq);
			} else {
				// To be overridden in subclass
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
		trackLogic = null;
		return new UtopiaResponse(response);
	}


	/**
	 * Add medium.
	 * <p/>
	 * <p/>
	 * Example
	 * <code>
	 * &lt;t-trk-add-medium-req id="medium-id" /&gt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement addMediumReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();

		// Mandatory medium id attr
		int mediumId = reqElm.getIntAttr(ATTR_ID);
		throwNegNumAttr(ATTR_ID, mediumId);

		Location location = trackLogic.createMediumLocation(mediumId);
		JXElement rsp = createResponse(T_TRK_ADD_MEDIUM_SERVICE);
		rsp.setId(location.getId());

		EventPublisher.mediumAdd(mediumId, location, anUtopiaReq);
		return rsp;
	}

	/**
	 * Create new Track.
	 * <p/>
	 * Examples
	 * <code>
	 * &lt;t-trk-create-req &gt; &lt;
	 * <p/>
	 * &lt;t-trk-create-rsp id="t-trk-id" &gt; &lt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement createReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();
		// Get optional parms
		String trackName = reqElm.getAttr(ATTR_NAME, null);

		// Track type: normal (user managed) or daytrack (we keep track per day)
		String trackTypeStr = reqElm.getAttr(ATTR_TYPE, Track.VAL_NORMAL_TRACK + "");
		int trackType = Integer.parseInt(trackTypeStr);

		// Create Track object
		Track track = trackLogic.create(getUserId(anUtopiaReq), trackName, trackType, setTimeIfMissing(reqElm));

		// Create and return response with open track id.
		JXElement response = createResponse(T_TRK_CREATE_SERVICE);
		response.setAttr(ATTR_ID, track.getId());
		EventPublisher.trackCreate(track.getId(), anUtopiaReq);

		return response;
	}

	/**
	 * Delete track.
	 * <p/>
	 * <p/>
	 * Example
	 * <code>
	 * &lt;t-trk-delete-req [id="t-trk-id"] /&gt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement deleteReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();

		String trackId = trackLogic.delete(getUserId(anUtopiaReq), reqElm.getAttr(ATTR_ID, null));
		JXElement response = createResponse(T_TRK_DELETE_SERVICE);
		response.setAttr(ATTR_ID, trackId);

		if (trackId != null) {
			EventPublisher.trackDelete(Integer.parseInt(trackId), anUtopiaReq);
		}

		return response;
	}

	/**
	 * Delete Medium from Track.
	 * <p/>
	 * <p/>
	 * Example
	 * <code>
	 * &lt;t-trk-delete-medium-req id="12345"  /&gt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement deleteMediumReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();

		// Required attr id
		String id = reqElm.getAttr(ATTR_ID, null);
		throwOnMissingAttr(ATTR_ID, id);

		// Get neccessary objects
		Oase oase = anUtopiaReq.getUtopiaSession().getContext().getOase();
		Medium medium = (Medium) oase.get(Medium.class, id);
		int intId = medium.getId();


		Location location = (Location) medium.getRelatedObject(Location.class);
		if (location != null) {
			location.delete();
		}

		int trackId = -1;
		Track track = (Track) medium.getRelatedObject(Track.class);
		if (track != null) {
			trackId = track.getId();
		}

		// Delete medium
		medium.delete();

		// Create response
		JXElement rsp = createResponse(T_TRK_DELETE_MEDIUM_SERVICE);
		rsp.setId(intId);

		EventPublisher.mediumDelete(intId, trackId, anUtopiaReq);
		return rsp;
	}

	/**
	 * Export track.
	 * <p/>
	 * required fields: data<br /><br />
	 * <p/>
	 * request format:<br /><br />
	 * <p/>
	 * &lt;trk-export-req personid="24" day="50723" startday="50723" endday="50824" />
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement exportReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();

		// Mandatory track id
		String trackId = reqElm.getAttr(ATTR_ID, null);
		if (trackId != null) {
			throwOnNonNumAttr(ATTR_ID, trackId);
		}

		String format = reqElm.getAttr(ATTR_FORMAT, null);
		String attrs = reqElm.getAttr(ATTR_ATTRS, null);
		boolean media = reqElm.getBoolAttr(ATTR_MEDIA);
		long minDist = reqElm.getLongAttr(ATTR_MINDIST);
		int maxPoint = reqElm.getIntAttr(ATTR_MAXPOINTS);

		JXElement result = trackLogic.export(trackId, format, attrs, media, minDist, maxPoint);

		// Fill response
		JXElement responseElement = createResponse(T_TRK_EXPORT_SERVICE);
		responseElement.addChild(result);
		return responseElement;
	}

	/**
	 * Create location for medium.
	 * <p/>
	 * <p/>
	 * Example
	 * <code>
	 * &lt;loc-add-medium-req id="medium-id" /&gt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement heartbeatReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();

		// Optional attr
		setTimeIfMissing(reqElm);

		// Forward heartbeat
		Track track = trackLogic.getActiveTrack(getUserId(anUtopiaReq));
		EventPublisher.heartbeat(track, reqElm.getLongAttr(ATTR_T), anUtopiaReq);

		return createResponse(T_HEARTBEAT_SERVICE);
	}


	/**
	 * Import track.
	 * <p/>
	 * required fields: data<br /><br />
	 * <p/>
	 * request format:<br /><br />
	 * <p/>
	 * &lt;t-trk-import-req personid="24" day="50723" startday="50723" endday="50824" />
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement importReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();

		// String format = reqElm.getAttr(ATTR_FORMAT, null);
		String name = reqElm.getAttr(ATTR_NAME, "unnamed");
		JXElement data = reqElm.getChildByTag("data");
		if (data == null || data.getChildCount() != 1) {
			throw new UtopiaException("Missing track data", ErrorCode.__6002_Required_attribute_missing);
		}

		Track track = trackLogic.importTrack(getUserId(anUtopiaReq), name, data.getChildAt(0));

		// Fill response
		JXElement responseElement = createResponse(T_TRK_IMPORT_SERVICE);
		responseElement.setAttr(ATTR_ID, track.getId());
		return responseElement;
	}

	/**
	 * Read track.
	 * <p/>
	 * required fields: id<br /><br />
	 * <p/>
	 * Example
	 * <code>
	 * &lt;t-trk-read-req id="t-trk-id" /&gt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement readReq(UtopiaRequest anUtopiaReq) throws UtopiaException {

		JXElement reqElm = anUtopiaReq.getRequestCommand();

		// Mandatory track id attr
		String trackId = reqElm.getAttr(ATTR_ID, null);
		if (trackId != null) {
			throwOnNonNumAttr(ATTR_ID, trackId);
		}

		Vector data = trackLogic.read(trackId);

		// Fill response
		JXElement responseElement = createResponse(T_TRK_READ_SERVICE);
		responseElement.addChildren(data);
		return responseElement;
	}

	/**
	 * Create new or open existing Track.
	 * <p/>
	 * Examples
	 * <code>
	 * &lt;t-trk-resume-req /&gt;
	 * <p/>
	 * &lt;t-trk-resume-rsp /&gt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement resumeReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();

		// Track type: normal (user managed) or daytrack (we keep track per day)
		String trackTypeStr = reqElm.getAttr(ATTR_TYPE, Track.VAL_NORMAL_TRACK + "");
		int trackType = Integer.parseInt(trackTypeStr);

		// Resume current Track for this user
		Track track = trackLogic.resume(getUserId(anUtopiaReq), trackType, setTimeIfMissing(reqElm));

		// Create and return response with open track id.
		JXElement response = createResponse(T_TRK_RESUME_SERVICE);
		response.setAttr(ATTR_ID, track.getId());
		EventPublisher.trackResume(track.getId(), anUtopiaReq);

		return response;
	}

	/**
	 * Suspend current Track.
	 * <p/>
	 * Examples
	 * <code>
	 * &lt;t-trk-suspend-req /&gt;
	 * <p/>
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement suspendReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();

		// Find today's Track record for this user
		Track track = trackLogic.suspend(getUserId(anUtopiaReq), setTimeIfMissing(reqElm));
		// Create and return response with open track id.
		JXElement response = createResponse(T_TRK_SUSPEND_SERVICE);
		response.setAttr(ATTR_ID, track.getId());
		EventPublisher.trackSuspend(track.getId(), anUtopiaReq);
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
	public JXElement unknownReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		String service = anUtopiaReq.getServiceName();
		Logging.getLog(anUtopiaReq).warn("Unknown service " + service);
		return createNegativeResponse(service, ErrorCode.__6000_Unknown_command, "unknown service: " + service);
	}

	/**
	 * Medium upload.
	 * <p/>
	 * Examples
	 * <code>
	 * &lt;t-trk-upload-medium-req &gt; &lt;
	 * <p/>
	 * &lt;t-trk-upload-medium-rsp id="t-trk-id" &gt; &lt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement uploadMediumReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();
		if (reqElm.hasAttr(ATTR_FILE)) {
			return uploadFileMedium(anUtopiaReq);
		} else if (reqElm.getChildByTag(TAG_DATA) != null || reqElm.getAttr(ATTR_TYPE).equals(VAL_SPOT)) {
			return uploadRawMedium(anUtopiaReq);
		} else {
			throw new UtopiaException("Invalid upload request use file or raw data", ErrorCode.__6004_Invalid_attribute_value);
		}
	}

	/**
	 * Medium upload.
	 * <p/>
	 * Examples
	 * <code>
	 * &lt;loc-medium-upload-req &gt; &lt;
	 * <p/>
	 * &lt;t-trk-medium-rsp id="t-trk-id" &gt; &lt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement uploadFileMedium(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();
		String type = reqElm.getAttr(ATTR_TYPE, null);
		String mime = reqElm.getAttr(ATTR_MIME, null);
		// file attr is filename
		String filePath = reqElm.getAttr(ATTR_FILE, null);
		throwOnMissingAttr(ATTR_FILE, filePath);

		// Fill in fields
		HashMap fields = new HashMap(2);
		String name = reqElm.getAttr(ATTR_NAME, "noname");
		String description = reqElm.getAttr(ATTR_DESCRIPTION, "upload by " + getUserName(anUtopiaReq));

		fields.put(ATTR_NAME, name);
		fields.put(ATTR_DESCRIPTION, description);
		if (mime != null) {
			fields.put(MediaFiler.FIELD_MIME, mime);
		}
		if (type != null) {
			fields.put(MediaFiler.FIELD_KIND, type);
		}

		// Create medium record
		Record mediumRecord, personRecord;
		try {
			Oase oase = anUtopiaReq.getUtopiaSession().getContext().getOase();
			MediaFiler mediaFiler = oase.getMediaFiler();
			File file = new File(filePath);
			mediumRecord = mediaFiler.insert(file, fields);
			if (reqElm.hasAttr(ATTR_T)) {
				mediumRecord.setTimestampField(MediaFiler.FIELD_CREATIONDATE, new Timestamp(reqElm.getLongAttr(ATTR_T)));
				oase.getModifier().update(mediumRecord);
			}
			personRecord = oase.getFinder().read(getUserId(anUtopiaReq));
			oase.getRelater().relate(mediumRecord, personRecord);
		} catch (Throwable t) {
			throw new UtopiaException("Error in uploadFileMedium() for file=" + filePath, t);
		}

		// Create Location for medium and relate to other objects
		Location location;
		if (reqElm.hasAttr(ATTR_T)) {
			// if a timestamp was provided we assume we already have the correct creation time
			location = trackLogic.createLocation(personRecord.getId(), mediumRecord.getId(), reqElm.getLongAttr(ATTR_T), TrackLogic.REL_TAG_MEDIUM);
		} else {
			// Determines timestamp from medium (e.g. EXIF) to create location
			location = trackLogic.createMediumLocation(mediumRecord.getId());
		}

		// Add optional tags
		if (reqElm.hasAttr(ATTR_TAGS)) {
			addTags(anUtopiaReq.getUtopiaSession(), reqElm.getAttr(ATTR_TAGS),  mediumRecord.getIdString());
		}

		// Create and return response with open track id.
		JXElement response = createResponse(T_TRK_UPLOAD_MEDIUM_SERVICE);
		response.setAttr(ATTR_ID, mediumRecord.getId());
		EventPublisher.mediumAdd(mediumRecord.getId(), location, anUtopiaReq);

		return response;
	}

	/**
	 * Medium upload.
	 * <p/>
	 * Examples
	 * <code>
	 * &lt;loc-medium-upload-req &gt; &lt;
	 * <p/>
	 * &lt;t-trk-medium-rsp id="t-trk-id" &gt; &lt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement uploadRawMedium(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();
		String type = reqElm.getAttr(ATTR_TYPE, null);

		throwOnMissingAttr(ATTR_TYPE, type);

		// Hack: for spots
		if (type.equals(VAL_SPOT)) {
			reqElm.setAttr(ATTR_MIME, "spot/location");
			JXElement data = new JXElement(TAG_DATA);
			data.setAttr(ATTR_ENCODING, MediaFiler.ENC_RAW);
			data.setText("spot");
			reqElm.addChild(data);
		}

		String mime = reqElm.getAttr(ATTR_MIME, null);
		throwOnMissingAttr(ATTR_MIME, mime);

		// Fill in fields
		HashMap fields = new HashMap(2);
		String name = reqElm.getAttr(ATTR_NAME, "noname");
		String description = reqElm.getAttr(ATTR_DESCRIPTION, type + " from " + getUserName(anUtopiaReq));

		// Fill in standard medium fields
		fields.put(ATTR_NAME, name);
		fields.put(ATTR_DESCRIPTION, description);
		fields.put(MediaFiler.FIELD_MIME, mime);
		fields.put(MediaFiler.FIELD_KIND, type);

		// <data> element contains CDATA file bytes and
		// how these are encoded in "encoding" attr.
		// Data must be present except for "spot" medium type
		JXElement data = reqElm.getChildByTag(TAG_DATA);
		if (data == null) {
			throw new UtopiaException("No data in raw medium upload");
		}

		String encoding = data.getAttr(ATTR_ENCODING, null);
		throwOnMissingAttr(ATTR_ENCODING, encoding);

		// Create medium record
		Record mediumRecord = null;
		Record personRecord = null;
		try {
			Oase oase = anUtopiaReq.getUtopiaSession().getContext().getOase();
			MediaFiler mediaFiler = oase.getMediaFiler();
			byte[] rawData = new byte[0];
			if (type.equals(MediaFiler.KIND_TEXT) || type.equals(VAL_SPOT)) {
				if (data.hasText()) {
					rawData = IO.forHTMLTag(data.getText()).getBytes();
				} else if (data.hasCDATA()) {
					rawData = IO.forHTMLTag(new String(data.getCDATA())).getBytes();
				}
			} else {
				rawData = data.getCDATA();
			}

			mediumRecord = mediaFiler.insert(rawData, encoding, fields);
			if (reqElm.hasAttr(ATTR_T)) {
				mediumRecord.setTimestampField(MediaFiler.FIELD_CREATIONDATE, new Timestamp(reqElm.getLongAttr(ATTR_T)));
				oase.getModifier().update(mediumRecord);
			}

			personRecord = oase.getFinder().read(getUserId(anUtopiaReq));
			oase.getRelater().relate(mediumRecord, personRecord);
		} catch (Throwable t) {
			throw new UtopiaException("Error in uploadMedium() for raw data encoding=" + encoding, t);
		}

		// Create Location for medium and relate to other objects
		Location location;
		if (reqElm.hasAttr(ATTR_T)) {
			// if a timestamp was provided we assume we already have the correct creation time
			location = trackLogic.createLocation(personRecord.getId(), mediumRecord.getId(), reqElm.getLongAttr(ATTR_T), TrackLogic.REL_TAG_MEDIUM);
		} else {
			// Determines timestamp from medium (e.g. EXIF) to create location
			location = trackLogic.createMediumLocation(mediumRecord.getId());
		}

		// Add optional tags
		if (reqElm.hasAttr(ATTR_TAGS)) {
			addTags(anUtopiaReq.getUtopiaSession(), reqElm.getAttr(ATTR_TAGS),  mediumRecord.getIdString());
		}

		// Create and return response with open track id.
		JXElement response = createResponse(T_TRK_UPLOAD_MEDIUM_SERVICE);
		response.setAttr(ATTR_ID, mediumRecord.getId());
		EventPublisher.mediumAdd(mediumRecord.getId(), location, anUtopiaReq);

		return response;
	}

	/**
	 * Write entry into track.
	 * <p/>
	 * <p/>
	 * Example
	 * <code>
	 * &lt;t-trk-write-req  &gt;
	 * &lt;pt nmea="GPRMC,131246.908,A,5211.3596,..." t="1267612282"/&gt;
	 * &lt;/t-trk-write-req
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard Utopia exception
	 */
	public JXElement writeReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();

		Vector result = trackLogic.write(reqElm.getChildren(), getUserId(anUtopiaReq));
		Track track = trackLogic.getActiveTrack(getUserId(anUtopiaReq));

		// Publish any points
		// TODO this should be done through KW subscription
		EventPublisher.tracerMove(track, result, anUtopiaReq);

		return createResponse(T_TRK_WRITE_SERVICE);
	}

	/**
	 * Add tags to item(s).
	 */
	protected void addTags(UtopiaSession anUtopiaSession, String theTags, String theIds) {
		UtopiaRequest tagRequest = null;

		try {
			JXElement tagRequestElm = new JXElement("tagging-tag-req");
			tagRequestElm.setAttr("tags", theTags.trim().toLowerCase());
			tagRequestElm.setAttr("mode", "add");
			tagRequestElm.setAttr("items",theIds);


			// Create Utopia request
			tagRequest = new UtopiaRequest(anUtopiaSession, tagRequestElm);

			// Perform Utopia request
			UtopiaApplication utopiaApplication = anUtopiaSession.getContext().getUtopiaApplication();
			UtopiaResponse tagReqResponse = utopiaApplication.performRequest(tagRequest);
			Logging.getLog(tagRequest).info("Added tags to ids=" + theIds + " rsp=" + tagReqResponse.getResponseCommand().getTag());
		} catch (Throwable t) {
			Logging.getLog(tagRequest).warn("Cannot add tags to ids=" + theIds, t);
		}
	}

	/**
	 * Get user Account from request.
	 */
	protected Account getAccount(UtopiaRequest anUtopiaReq) throws UtopiaException {

		// Get account name for event subject
		// Expensive but we have to
		UtopiaSessionContext sc = anUtopiaReq.getUtopiaSession().getContext();
		Oase oase = sc.getOase();
		Person person = (Person) oase.get(Person.class, sc.getUserId());
		return person.getAccount();
	}

	/**
	 * Get user (Person) id from request.
	 */
	protected int getUserId(UtopiaRequest anUtopiaReq) throws UtopiaException {
		return Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());
	}

	/**
	 * Get user (Person) name from request.
	 */
	protected String getUserName(UtopiaRequest anUtopiaReq) throws UtopiaException {
		return anUtopiaReq.getUtopiaSession().getContext().getUserName();
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

	protected long setTimeIfMissing(JXElement anElement) {
		if (!anElement.hasAttr(ATTR_T)) {
			anElement.setAttr(ATTR_T, System.currentTimeMillis());
		}
		return anElement.getLongAttr(ATTR_T);
	}

}

/*
* $Log: TracingHandler.java,v $
* Revision 1.20  2006-08-10 23:40:01  just
* no message
*
* Revision 1.19  2006-07-06 23:06:48  just
* no message
*
* Revision 1.18  2006-04-27 14:42:19  just
* trk migration
*
* Revision 1.17  2006-04-27 09:51:06  just
* trackfiltering improved
*
* Revision 1.16  2006-04-18 14:38:45  just
* added delete medium service
*
* Revision 1.15  2006-04-05 13:10:41  just
* implemented daytracks
*
* Revision 1.14  2006-04-04 15:15:33  just
* fix invalid gps sample writing
*
* Revision 1.13  2005/12/07 12:49:01  just
* *** empty log message ***
*
* Revision 1.12  2005/10/31 23:15:50  just
* *** empty log message ***
*
* Revision 1.11  2005/10/24 22:09:13  just
* *** empty log message ***
*
* Revision 1.10  2005/10/21 20:49:20  just
* *** empty log message ***
*
* Revision 1.9  2005/10/21 13:09:50  just
* basic bomb protocol
*
* Revision 1.8  2005/10/20 15:37:20  just
* *** empty log message ***
*
* Revision 1.7  2005/10/20 09:12:04  just
* *** empty log message ***
*
* Revision 1.6  2005/10/19 09:39:22  just
* *** empty log message ***
*
* Revision 1.5  2005/10/18 15:23:51  just
* *** empty log message ***
*
* Revision 1.4  2005/10/18 12:54:44  just
* *** empty log message ***
*
* Revision 1.3  2005/10/18 07:38:00  just
* *** empty log message ***
*
* Revision 1.2  2005/10/13 14:19:21  just
* *** empty log message ***
*
* Revision 1.1  2005/10/13 13:22:20  just
* *** empty log message ***
*
* Revision 1.12  2005/10/13 13:15:58  just
* *** empty log message ***
*
* Revision 1.11  2005/10/13 12:55:23  just
* *** empty log message ***
*
* Revision 1.10  2005/10/09 14:34:16  just
* *** empty log message ***
*
* Revision 1.9  2005/10/07 20:51:21  just
* *** empty log message ***
*
* Revision 1.8  2005/10/07 15:23:09  just
* *** empty log message ***
*

*
*
*/



