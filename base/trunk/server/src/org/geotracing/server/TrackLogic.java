// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.server;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.gis.GeoPoint;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Sys;
import org.keyworx.oase.api.*;
import org.keyworx.oase.store.record.FileFieldImpl;
import org.keyworx.server.ServerConfig;
import org.keyworx.utopia.core.data.*;
import org.keyworx.utopia.core.util.Oase;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Implements logic for Track manipulation.
 */
public class TrackLogic {
	/**
	 * EXIF time format, e.g. 20040520-120454.
	 */
	public static final SimpleDateFormat EXIF_TIME_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");
	public static final SimpleDateFormat YMD_FORMAT = new SimpleDateFormat("yyyyMMdd");
	public static final String REL_TAG_MEDIUM = "medium";

	/**
	 * Script to find timestamp (EXIF) in JPEG file.
	 */
	public static final String EXIF_DATE_SCRIPT = ServerConfig.getConfigDir() + "/../bin/exifdate.sh";
	public static final SimpleDateFormat GPX_TIME_FORMAT = TrackExport.GPX_TIME_FORMAT;

	private Oase oase;
	static private Log log;

	static {
		// Init once
		log = Logging.getLog("TrackLogic");

		// Make exif script executable
		String[] command = {"chmod", "+x", EXIF_DATE_SCRIPT};
		StringBuffer stdout = new StringBuffer(24);
		StringBuffer stderr = new StringBuffer(24);
		int exitCode = Sys.execute(command, stdout, stderr);

		if (exitCode != 0) {
			log.error("chmod +x " + EXIF_DATE_SCRIPT + "failed stderr=" + stderr.toString());
		} else {
			log.info("chmod +x " + EXIF_DATE_SCRIPT + " OK");
		}
	}

	public TrackLogic(Oase o) {
		oase = o;
	}

	/**
	 * Creates an track
	 *
	 * @param aPersonId The person id
	 * @return the track object
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public Track create(int aPersonId, String aName, int aTrackType, long aTime) throws UtopiaException {
		log.trace("Start TrackLogic.createTrack");
		Transaction aTransaction = oase.startTransaction();

		try {
			Track track = null;

			// Make any active Track inactive
			Track activeTrack = getActiveTrack(aPersonId);
			if (activeTrack != null) {
				int trackType = activeTrack.getType();
				if (trackType == Track.VAL_NORMAL_TRACK) {
					activeTrack.setState(Track.VAL_INACTIVE);
					activeTrack.saveUpdate();
					log.info("make inactive " + activeTrack);
				} else if (trackType == Track.VAL_DAY_TRACK) {
					if (!hasExpired(activeTrack, aTime)) {
						log.info("reuse active daytrack " + activeTrack);
						track = activeTrack;
					} else {
						activeTrack.setState(Track.VAL_INACTIVE);
						activeTrack.saveUpdate();
						log.info("make daytrack inactive " + activeTrack);
					}
				}
			}

			if (track == null) {
				// Create new Track and make active
				track = Track.create(oase);
				track.insert(aPersonId, aName, aTrackType, aTime);
				log.info("insert new " + track);
			}

			// Commit
			aTransaction.commit();
			return track;
		} catch (UtopiaException ue) {
			oase.cancelTransaction(aTransaction);
			throw ue;
		} catch (Throwable t) {
			oase.cancelTransaction(aTransaction);
			throw new UtopiaException("Exception in TrackLogic.insertTrack() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
		} finally {
			log.trace("Exit TrackLogic.createTrack");
		}
	}

	/**
	 * Create Location object for given Person, record and timestamp.
	 *
	 * @param aRecordId a medium id
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public Location createLocation(int aPersonId, int aRecordId, long aDate, String aTag) throws UtopiaException {
		Transaction transaction = oase.startTransaction();
		try {
			// Find the Track(s) for the timestamp
			Track track = getTrackByDateAndPerson(aDate, aPersonId);
			if (track == null) {
				// Not in any track, try active Track
				track = getActiveTrack(aPersonId);

				// Check if active track found
				if (track == null) {
					throw new UtopiaException("Cannot find any Track for date and person");
				}
			}

			// OK a track found

			// Find nearest point int time within track
			GeoPoint geoPoint = getPointByDate(aPersonId, aDate);
			if (geoPoint == null) {
				log.warn("Cannot find GeoPoint for person=" + aPersonId + " record=" + aRecordId + " tag=" + aTag);
				throw new UtopiaException("Cannot find GeoPoint for record=" + aRecordId + " tag=" + aTag);
			}

			// OK point found

			// Relate Medium to Track
			track.createRelation(aRecordId, aTag);

			// Create and relate location record
			Location location = Location.create(oase);
			location.setLocation(geoPoint);
			location.setStringValue(Location.FIELD_NAME, "Location for " + aTag);

			location.saveInsert();
			location.createRelation(aRecordId, aTag);
			transaction.commit();
			log.info("Related Location " + location.getId() + " and Track " + track.getId() + " to Record " + aRecordId + " tag=" + aTag);

			return location;
		} catch (UtopiaException ue) {
			oase.cancelTransaction(transaction);
			throw ue;
		} catch (Throwable t) {
			oase.cancelTransaction(transaction);
			throw new UtopiaException("Error in createLocation() time=" + new Date(aDate) + " record id=" + aRecordId + " tag=" + aTag, t);
		}

	}

	/**
	 * Create Location object for given Medium.
	 *
	 * @param aMediumId a medium id
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public Location createMediumLocation(int aMediumId) throws UtopiaException {
		log.trace("Start TrackLogic.createLocation");
		long timestamp = -1L;
		try {
			log.info("Creating Location for Medium id=" + aMediumId);

			// Find medium
			Medium medium = (Medium) oase.get(Medium.class, aMediumId + "");
			if (medium == null) {
				throw new UtopiaException("Cannot find medium id=" + aMediumId);
			}

			// Find Person related to Medium
			Person person = medium.getPerson(null);
			if (person == null) {
				throw new UtopiaException("Cannot find Person for medium id=" + aMediumId);
			}

			Record mediumRecord = medium.getRecord();

			// 1. Find the exact time for medium
			if (mediumRecord.getField(MediaFiler.FIELD_MIME).equals("image/jpeg")) {
				FileFieldImpl fileField = (FileFieldImpl) mediumRecord.getFileField(MediaFiler.FIELD_FILE);
				String filePath = fileField.getStoredFile().getAbsolutePath();
				String[] command = {EXIF_DATE_SCRIPT, filePath};
				StringBuffer stdout = new StringBuffer(24);
				StringBuffer stderr = new StringBuffer(24);
				int exitCode = Sys.execute(command, stdout, stderr);

				if (exitCode == 0) {
					// e.g. 20040520-120454
					String dateString = stdout.toString();
					if (dateString != null && dateString.trim().length() > 0) {
						Date date = EXIF_TIME_FORMAT.parse(dateString);
						log.trace("Got date from EXIF date=" + dateString + " java Date=" + date);

						// Update Medium creationdate
						timestamp = date.getTime();
						mediumRecord.setTimestampField(MediaFiler.FIELD_CREATIONDATE, new Timestamp(timestamp));
						medium.saveUpdate();
					} else {
						log.trace("exifdate returned " + exitCode + " but no date");
					}
				} else {
					log.warn("exifdate returned " + exitCode + " stderr=" + stderr.toString());
				}
			}

			// if no time yet found use creation date
			if (timestamp <= 0L) {
				timestamp = mediumRecord.getTimestampField(MediaFiler.FIELD_CREATIONDATE).getTime();
			}

			return createLocation(person.getId(), aMediumId, timestamp, REL_TAG_MEDIUM);

		} catch (UtopiaException ue) {
			throw ue;
		} catch (Throwable t) {
			throw new UtopiaException("Error in createLocation() time=" + new Date(timestamp) + " medium id=" + aMediumId, t);
		}
	}

	/**
	 * Delete Track and its related objects.
	 *
	 * @param aTrackId a track id
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public String delete(String aTrackId) throws UtopiaException {
		Track track = (Track) oase.get(Track.class, aTrackId);
		Person person = (Person) track.getRelatedObject(Person.class);
		return delete(person.getId(), aTrackId);
	}

	/**
	 * Delete Track and its related objects (except Person).
	 *
	 * @param aPersonId a person id
	 * @param aTrackId  a track id
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public String delete(int aPersonId, String aTrackId) throws UtopiaException {
		log.trace("Start TrackLogic.delete trackId=" + aTrackId);
		Transaction transaction = oase.startTransaction();

		try {
			// Use active track if no trackId supplied otherwise get any track by id.
			Track track = (aTrackId == null) ? getActiveTrack(aPersonId) : getTrackById(aTrackId);

			// Must have existing track
			if (track == null) {
				throw new UtopiaException("Cannot find any Track to delete for personId=" + aPersonId + " trackId=" + aTrackId);
			}

			// Remember track id
			String trackId = track.getId() + "";

			// Build up list of objects to be deleted
			List deleteList = track.getRelatedObjects(Medium.class);
			deleteList.add(track.getRelatedObject(Area.class));
			deleteList.add(track);

			// Delete Track and related objects (except Person!)
			Base nextObj = null;
			Record[] locationRecords = null;
			Relater relater = oase.getRelater();
			Modifier modifier = oase.getModifier();
			int deleteCount = 0;
			for (int i = 0; i < deleteList.size(); i++) {
				nextObj = (Base) deleteList.get(i);

				// Delete any possible Location records (Track, Medium and POI)
				locationRecords = relater.getRelated(nextObj.getRecord(), Location.TABLE_NAME, null);
				for (int j = 0; j < locationRecords.length; j++) {
					modifier.delete(locationRecords[j]);
					deleteCount++;
				}
				nextObj.delete();
				deleteCount++;
			}

			// Do it.
			transaction.commit();
			log.info("deleteTrack OK id=" + trackId + " total records deleted=" + deleteCount);

			// All is well...
			return trackId;
		} catch (UtopiaException ue) {
			oase.cancelTransaction(transaction);
			throw ue;
		} catch (Throwable t) {
			oase.cancelTransaction(transaction);
			throw new UtopiaException("Exception in TrackLogic.delete() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
		} finally {
			log.trace("Exit TrackLogic.delete");
		}
	}

	/**
	 * Export XML elements from given Track to given format.
	 *
	 * @param aTrackId a track id
	 * @param aFormat  a format e.g. "gpx"
	 * @return theElements one or more JXElements
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public JXElement export(String aTrackId, String aFormat, String theAttrs, boolean addMedia, long aMinDist, int aMaxPoint) throws UtopiaException {
		log.trace("Start export(trackId=" + aTrackId + " format=" + aFormat + ")");
		Track track = getTrackById(aTrackId);
		TrackExport trackExport = new TrackExport(oase);
		return trackExport.export(track, aFormat, theAttrs, addMedia, aMinDist, aMaxPoint);
	}

	/**
	 * Get last track for person.
	 *
	 * @return track the result or null
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public Track getActiveTrack(int aPersonId) throws UtopiaException {
		log.trace("Start TrackLogic.getActiveTrack()");
		Track track = null;
		try {
			Person person = (Person) oase.get(Person.class, aPersonId + "");
			if (person == null) {
				throw new UtopiaException("Invalid person id " + aPersonId);
			}

			// Get Track with relation tag "active"
			// TODO make efficient query
			Track[] tracks = getTracksByPersonId(aPersonId);
			for (int i = 0; i < tracks.length; i++) {
				if (tracks[i].getState() == Track.VAL_ACTIVE) {
					track = tracks[i];
					break;
				}
			}
		} catch (Throwable t) {
			throw new UtopiaException("Exception in TrackLogic.getActiveTrack() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
		}
		log.trace("Exit TrackLogic.getActiveTrack() " + track);
		return track;
	}

	/**
	 * Get active Tracks.
	 *
	 * @return array of zero or more Track objects
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public Track[] getActiveTracks() throws UtopiaException {
		log.trace("Start TrackLogic.getCurrentTracks()");
		List result = null;
		try {
			result = oase.getByNameAndValue(Track.class, Track.FIELD_STATE, "" + Track.VAL_ACTIVE);
		} catch (UtopiaException ue) {
			throw ue;
		} catch (Throwable t) {
			throw new UtopiaException("Exception in TrackLogic.getCurrentTracks() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
		}
		log.trace("Exit TrackLogic.getCurrentTracks()");
		return list2TrackArray(result);
	}

	/**
	 * Gets a specific track
	 *
	 * @param trackId The track id
	 * @return a Track object or null
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public Track getTrackById(String trackId) throws UtopiaException {
		return (Track) oase.get(Track.class, trackId);
	}

	/**
	 * Get all Tracks.
	 *
	 * @return array of zero or more Track objects
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public Track[] getTracks() throws UtopiaException {
		log.trace("Start TrackLogic.getTracks()");
		List result = null;
		try {
			result = oase.getObjectList(Track.class, null, null, null);
		} catch (UtopiaException ue) {
			throw ue;
		} catch (Throwable t) {
			throw new UtopiaException("Exception in TrackLogic.getTracks() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
		}
		log.trace("Exit TrackLogic.getTracks()");
		return result == null ? new Track[0] : (Track[]) result.toArray(new Track[result.size()]);
	}

	/**
	 * Gets all tracks for given person id.
	 *
	 * @param aPersonId The person id
	 * @return a Track array (size >= 0)
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public Track[] getTracksByPersonId(int aPersonId) throws UtopiaException {
		List result = null;
		try {
			Person person = (Person) oase.get(Person.class, aPersonId + "");
			if (person == null) {
				throw new UtopiaException("Invalid person id " + aPersonId);
			}

			result = person.getRelatedObjects(Track.class);
		} catch (Throwable t) {
			throw new UtopiaException("Exception in TrackLogic.getTracksByPersonId() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
		}
		return list2TrackArray(result);
	}

	/**
	 * Gets nearest GeoPoint for given date ans person.
	 *
	 * @param aPersonId look into Tracks related to person
	 * @param aDate	 The date
	 * @return a Track array (size >= 0)
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public GeoPoint getPointByDate(int aPersonId, long aDate) throws UtopiaException {
		// Find the Track(s) for the timestamp
		Track track = getTrackByDateAndPerson(aDate, aPersonId);
		if (track == null) {
			// Not in any track, try active Track
			track = getActiveTrack(aPersonId);

			// Check if active track found
			if (track == null) {
				throw new UtopiaException("Cannot find any Track for date and person");
			}
		}

		// OK a track found

		// Find nearest point int time within track
		return getPointByDate(track, aDate);
	}

	/**
	 * Gets nearest GeoPoint for given date in given Track.
	 *
	 * @param aTrack The Track to search
	 * @param aDate  The date
	 * @return a Track array (size >= 0)
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public GeoPoint getPointByDate(Track aTrack, long aDate) throws UtopiaException {
		GeoPoint point = null;
		try {

			Vector elements = aTrack.getDataElements();

			JXElement lastElement = null;
			JXElement nextElement = null;
			for (int i = 0; i < elements.size(); i++) {
				nextElement = (JXElement) elements.get(i);

				// Skip non-point elements
				if (!nextElement.getTag().equals(Track.TAG_PT)) {
					continue;
				}

				// compare time of point to given time
				if (nextElement.getLongAttr(Track.ATTR_TIME) < aDate) {
					// Not found but remember last point
					lastElement = nextElement;
					continue;
				} else {
					// Found it!
					point = new GeoPoint(nextElement.getDoubleAttr(Track.ATTR_LON),
							nextElement.getDoubleAttr(Track.ATTR_LAT), nextElement.getDoubleAttr(Track.ATTR_ELE), aDate);
					break;
				}
			}

			// Found ?
			if (point == null) {
				log.trace("cannot find GeoPoint trying lastGeoSample...");
				if (lastElement == null) {
					log.warn("also lastElement == null, giving up...");
					return null;
				} else {
					log.trace("using lastElement...");
					point = new GeoPoint(lastElement.getDoubleAttr(Track.ATTR_LON),
							lastElement.getDoubleAttr(Track.ATTR_LAT), lastElement.getDoubleAttr(Track.ATTR_ELE), aDate);

				}
			}

		} catch (UtopiaException ue) {
			throw ue;
		} catch (Throwable t) {
			throw new UtopiaException("Exception in TrackLogic.getPointByDate() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
		}
		return point;
	}

	/**
	 * Gets nearest GeoPoint for given person id on given date in given Track.
	 *
	 * @param aDate	 The date
	 * @param aPersonId The person id
	 * @return a Track array (size >= 0)
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public GeoPoint getPointByDateAndPerson(long aDate, int aPersonId) throws UtopiaException {
		// get all Tracks for person on date
		Track track = getTrackByDateAndPerson(aDate, aPersonId);
		if (track == null) {
			throw new UtopiaException("Cannot find Track for person=" + aPersonId + "and date=" + new Date(aDate));
		}
		return getPointByDate(track, aDate);
	}

	/**
	 * Gets a track for given person id on given date.
	 *
	 * @param aDate	 The date
	 * @param aPersonId The person id
	 * @return a Track array (size >= 0)
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public Track getTrackByDateAndPerson(long aDate, int aPersonId) throws UtopiaException {
		Track result = null;
		try {

			// TODO: make efficient query !!!
			Track[] tracks = getTracksByPersonId(aPersonId);

			for (int i = 0; i < tracks.length; i++) {
				if (aDate >= tracks[i].getStartDate() && aDate < tracks[i].getEndDate()) {
					result = tracks[i];
					break;
				}
			}
		} catch (Throwable t) {
			throw new UtopiaException("Exception in TrackLogic.getTrackByPersonId() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
		}
		return result;
	}

	/**
	 * Is this a day track that has expired into next day ?
	 *
	 * @param aTrack The track
	 * @param aTime  time to check expiry against
	 * @return true if expired
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public boolean hasExpired(Track aTrack, long aTime) throws UtopiaException {
		// Only for day tracks
		if (aTrack.getType() != Track.VAL_DAY_TRACK) {
			return false;
		}

		// Check expiry
		String trackEndDate = YMD_FORMAT.format(new Date(aTrack.getEndDate()));
		String nowDate = YMD_FORMAT.format(new Date(aTime));

		// Expired if we are on a different day (aTime is always later)
		return !trackEndDate.equals(nowDate);
	}

	/**
	 * Import a track.
	 *
	 * @param aPersonId a person id
	 * @param aTrackDoc a track document (usually GPX doc)
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public Track importTrack(int aPersonId, String aName, JXElement aTrackDoc) throws UtopiaException {
		log.info("Start import (personId=" + aPersonId + ")");

		// Create new Track object
		Vector trkElms = aTrackDoc.getChildrenByTag("trk");
		if (trkElms == null || trkElms.size() == 0) {
			throw new UtopiaException("No trk elements found in GPX", ErrorCode.__6004_Invalid_attribute_value);
		}

		Vector trackEvents = new Vector();
		long startTime = -1;
		for (int i = 0; i < trkElms.size(); i++) {
			JXElement nextTrk = (JXElement) trkElms.elementAt(i);
			Vector nextTrkSegs = nextTrk.getChildrenByTag("trkseg");
			if (nextTrkSegs == null || nextTrkSegs.size() == 0) {
				log.warn("No track segments found");
				continue;
			}

			// Parse and handle all track segments in current track
			for (int j = 0; j < nextTrkSegs.size(); j++) {
				JXElement nextSeg = (JXElement) nextTrkSegs.elementAt(j);
				Vector nextTrkPts = nextSeg.getChildrenByTag("trkpt");
				if (nextTrkPts == null || nextTrkPts.size() == 0) {
					log.warn("No track points found");
					continue;
				}

				// Parse and handle all track points in current track segment
				for (int k = 0; k < nextTrkPts.size(); k++) {
					JXElement nextTrkPt = (JXElement) nextTrkPts.elementAt(k);

					// Event is adding a point to track
					JXElement nextEvent = new JXElement("pt");

					// Lat/lon
					nextEvent.setAttr("lon", nextTrkPt.getAttr("lon"));
					nextEvent.setAttr("lat", nextTrkPt.getAttr("lat"));

					// Height (elevation)
					String ele = nextTrkPt.getChildText("ele");
					if (ele == null) {
						ele = "0.0";
					}
					nextEvent.setAttr("ele", ele);

					// Handle time (mandatory)
					String time = nextTrkPt.getChildText("time");
					if (time == null) {
						throw new UtopiaException("No time elements found in GPX", ErrorCode.__6004_Invalid_attribute_value);
					}
					try {
						long timeMillis;
						if (time.length() == 20) {
							timeMillis = TrackExport.GPX_TIME_FORMAT.parse(time).getTime();
						} else if (time.length() == 24) {
							timeMillis = TrackExport.GPX_TIME_FORMAT_MILLIS.parse(time).getTime();
						} else {
							timeMillis = Long.parseLong(time); // try for unix time
						}
						if (startTime == -1) {
							startTime = timeMillis;
						}

						// On start of segment: add resume, using our current time
						if (k == 0) {
							JXElement resumeEvent = new JXElement("resume");
							resumeEvent.setAttr("t", timeMillis);
							trackEvents.add(resumeEvent);
						}
						nextEvent.setAttr("t", timeMillis);
					} catch (ParseException pe) {
						throw new UtopiaException("Cannot parse time element in GPX: " + time, ErrorCode.__6004_Invalid_attribute_value);
					}
					trackEvents.add(nextEvent);
				}
			}
		}

		// Sanity check
		if (trackEvents.size() == 0) {
			throw new UtopiaException("no track events could be distilled from GPX: ", ErrorCode.__6004_Invalid_attribute_value);
		}

		// Create and save the track
		Track track = Track.create(oase);
		track.insert(aPersonId, aName, Track.VAL_NORMAL_TRACK, startTime);
		log.info("Track import: importing " + trackEvents.size() + " events");
		track.addData(trackEvents);

		// Reset to inactive
		track.setState(Track.VAL_INACTIVE);
		track.saveUpdate();

		// track.addData(resumeElement);
		log.info("Track imported trackId=" + track.getId() + " personId=" + aPersonId);
		return track;
	}

	/**
	 * Read XML elements from given Track.
	 *
	 * @param aTrackId a track id
	 * @return theElements one or more JXElements
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public Vector read(String aTrackId) throws UtopiaException {
		log.trace("Start read(trackId=" + aTrackId + ")");
		Track track = getTrackById(aTrackId);
		if (track == null) {
			throw new UtopiaException("Cannot find track, trackId=" + aTrackId, ErrorCode.__6004_Invalid_attribute_value);
		}

		Vector result = track.getDataElements();
		log.trace("End read(trackId=" + aTrackId + " elmcount=" + result.size() + ")");
		return result;
	}

	/**
	 * Resume track.
	 *
	 * @param aPersonId a person id
	 * @param aTime	 time of resume
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public Track resume(int aPersonId, int aType, long aTime) throws UtopiaException {
		log.trace("Start resume (personId=" + aPersonId + ")");

		// First get a new or existing Track object
		Track track = getActiveTrack(aPersonId);
		if (track == null || hasExpired(track, aTime)) {
			// Ok, create a new Track
			track = create(aPersonId, null, aType, aTime);
		}

		// Append resume element
		JXElement resumeElement = new JXElement(Track.TAG_RESUME);
		resumeElement.setAttr(Track.ATTR_TIME, aTime);
		track.addData(resumeElement);
		log.info("Track resumed trackId=" + track.getId() + " personId=" + aPersonId);
		return track;
	}

	/**
	 * Suspend active track.
	 *
	 * @param aPersonId a person id
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public Track suspend(int aPersonId, long aTime) throws UtopiaException {
		try {
			// Make any active Track inactive
			Track track = getActiveTrack(aPersonId);
			if (track == null) {
				throw new UtopiaException("Cannot find active track, personId=" + aPersonId, ErrorCode.__6004_Invalid_attribute_value);
			}

			JXElement suspendElement = new JXElement(Track.TAG_SUSPEND);
			suspendElement.setAttr(Track.ATTR_TIME, aTime);
			track.addData(suspendElement);
			log.info("Track suspended trackId=" + track.getId() + " personId=" + aPersonId);
			return track;
		} catch (Throwable t) {
			throw new UtopiaException("Exception in TrackLogic.suspend() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
		}
	}

	/**
	 * Add XML elements to given Track.
	 *
	 * @param theElements one or more JXElements
	 * @param aPersonId   a person id
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public Vector write(Vector theElements, int aPersonId) throws UtopiaException {
		log.trace("Start write(count=" + theElements.size() + " elms aPersonId=" + aPersonId + ")");

		// Must have track active
		Track track = getActiveTrack(aPersonId);
		if (track == null) {
			throw new UtopiaException("No active track, do resume or create first, personId=" + aPersonId, ErrorCode.__6004_Invalid_attribute_value);
		}

		// Track available but is it expired ?
		JXElement lastSample = (JXElement) theElements.lastElement();
		long time = System.currentTimeMillis();
		if (lastSample.hasAttr(Track.ATTR_TIME)) {
			time = lastSample.getLongAttr(Track.ATTR_TIME);
		}

		if (hasExpired(track, time)) {
			// Creates new Track and resumes it
			track = resume(aPersonId, Track.VAL_DAY_TRACK, time);
		}

		// Add elements to Track (will handle transaction/exceptions etc)
		Vector result = track.addData(theElements);

		log.trace("End write OK track=" + track + " count=" + result.size());
		return result;
	}


	/**
	 * Convert Track List to Track array.
	 *
	 * @param aTrackList The Track List
	 * @return a Track array (size >= 0)
	 */
	static public Track[] list2TrackArray(List aTrackList) {
		return aTrackList == null ? new Track[0] : (Track[]) aTrackList.toArray(new Track[aTrackList.size()]);
	}

}

/*
* $Log: TrackLogic.java,v $
* Revision 1.19  2006-04-27 09:51:06  just
* trackfiltering improved
*
* Revision 1.18  2006-04-05 17:23:08  just
* daytrack stuff
*
* Revision 1.17  2006-04-05 13:10:41  just
* implemented daytracks
*
* Revision 1.16  2005/11/05 12:03:04  just
* *** empty log message ***
*
* Revision 1.15  2005/10/23 18:21:42  just
* *** empty log message ***
*
* Revision 1.14  2005/10/20 15:37:20  just
* *** empty log message ***
*
* Revision 1.13  2005/10/20 12:32:56  just
* *** empty log message ***
*
* Revision 1.12  2005/10/20 09:12:04  just
* *** empty log message ***
*
* Revision 1.11  2005/10/19 09:39:22  just
* *** empty log message ***
*
* Revision 1.10  2005/10/18 15:23:51  just
* *** empty log message ***
*
* Revision 1.9  2005/10/18 12:54:44  just
* *** empty log message ***
*
* Revision 1.8  2005/10/13 12:55:23  just
* *** empty log message ***
*
* Revision 1.7  2005/10/09 14:34:16  just
* *** empty log message ***
*
* Revision 1.6  2005/10/07 15:23:09  just
* *** empty log message ***
*
* Revision 1.5  2005/10/06 13:51:08  just
* *** empty log message ***
*
* Revision 1.4  2005/09/29 22:21:18  just
* *** empty log message ***
*
* Revision 1.3  2005/09/29 11:53:33  just
* *** empty log message ***
*
* Revision 1.2  2005/09/28 18:39:58  just
* *** empty log message ***
*
* Revision 1.1  2005/09/28 18:33:01  just
* *** empty log message ***
*
* Revision 1.15  2005/09/28 15:43:25  just
* *** empty log message ***
*
* Revision 1.14  2005/09/28 14:09:18  just
* *** empty log message ***
*
* Revision 1.13  2005/09/27 15:15:53  just
* *** empty log message ***
*
* Revision 1.12  2005/09/27 14:24:00  just
* *** empty log message ***
*
* Revision 1.11  2005/09/27 13:20:47  just
* *** empty log message ***
*
* Revision 1.10  2005/09/27 09:49:51  just
* *** empty log message ***
*
* Revision 1.9  2005/09/26 22:18:15  just
* *** empty log message ***
*
* Revision 1.8  2005/09/26 21:23:11  just
* *** empty log message ***
*
* Revision 1.7  2005/09/26 18:46:53  just
* *** empty log message ***
*
* Revision 1.6  2005/09/25 14:14:07  just
* *** empty log message ***
*
* Revision 1.5  2005/09/22 16:11:05  just
* *** empty log message ***
*
* Revision 1.4  2005/09/21 14:39:04  just
* *** empty log message ***
*
*
*/

