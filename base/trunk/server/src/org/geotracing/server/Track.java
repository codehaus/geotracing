// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.server;

import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import nl.justobjects.jox.parser.JXBuilderListener;
import org.geotracing.gis.GPSDecoder;
import org.geotracing.gis.GPSSample;
import org.geotracing.gis.GeoPoint;
import org.geotracing.gis.GeoBox;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.IO;
import org.keyworx.common.util.Sys;
import org.keyworx.oase.api.FileField;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.api.Transaction;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.store.record.FileFieldImpl;
import org.keyworx.utopia.core.data.BaseImpl;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Core;
import org.keyworx.utopia.core.util.Oase;

import java.io.FileInputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * The Track data object
 *
 * Maps the geoLog data to an object
 *
 * @author   Just van den Broecke
 * @version $Id: Track.java,v 1.21 2006-04-05 13:10:41 just Exp $
 */

public class Track extends BaseImpl {
	/*
		<field name="id" type="INTEGER" required="true" key="true" />
		<field name="name" type="STRING" size="64" required="false" />
		<field name="description" type="TEXT" required="false" />

		<!-- reserved, e.g. routes or waypoints -->
		<field name="type" type="INTEGER" default="1" required="true" />

		<!-- reserved, data type, e.g. standard or GPX file -->
		<field name="format" type="STRING" size="12" required="true" default="gtbasic" />

		<!-- 1=active 2=inactive 3=invalid -->
        <field name="state" type="INTEGER" default="2" required="true" />

		<!-- When first event e.g. a point was added to data-->
		<field name="startdate" type="TIMESTAMP" required="true"/>

		<!-- When last event e.g. a point was added to data-->
 		<field name="enddate" type="TIMESTAMP" required="true"/>

		<!-- The track event elements: points, open, close etc. -->
 		<field name="data" type="FILE" required="true" />

		<!-- Total number of points in data. -->
		<field name="ptcount" type="INTEGER"  required="true" default="0" />

		<!-- Distance in kilometers traveled. -->
		<field name="distance" type="REAL"  required="true" default="0" />

		<!-- Last event added to data. -->
		<field name="lastevt" type="XML"  />

		<!-- Timestamp of creation -->
        <field name="creationdate" type="TIMESTAMP"/>

		<!-- Timestamp of last modification -->
         <field name="modificationdate" type="TIMESTAMP"/>

		<!-- Reserved -->
		<field name="extra" type="XML" required="false"/>

	*/
	private static final SimpleDateFormat DATE_STRING = new SimpleDateFormat("yyMMdd");
	public static final DecimalFormat SPEED_FORMAT = new DecimalFormat("0.00");
	public static final DecimalFormat DISTANCE_FORMAT = new DecimalFormat("0.000");
	public static final DecimalFormat LAT_LON_FORMAT = new DecimalFormat("0.0000000");
	public static final String TABLE_NAME = "g_track";

	public static final String FIELD_NAME = "name";
	public static final String FIELD_DESCRIPTION = "description";
	public static final String FIELD_STATE = "state";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_START_DATE = "startdate";
	public static final String FIELD_END_DATE = "enddate";
	public static final String FIELD_CREATION_DATE = "creationdate";
	public static final String FIELD_MODIFICATION_DATE = "modificationdate";
	public static final String FIELD_DATA = "data";
	public static final String FIELD_DISTANCE = "distance";
	public static final String FIELD_PTCOUNT = "ptcount";
	public static final String FIELD_FORMAT = "format";
	public static final String FIELD_LAST_EVT = "lastevt";
	public static final String FIELD_EXTRA = "extra";

	public static final String REL_TAG_FIRST_PT = "firstpt";
	public static final String REL_TAG_LAST_PT = "lastpt";
	public static final String REL_TAG_LAST_LOC = "lastloc";

	public static final String REL_TAG_AREA = "trkarea";
	public static final String REL_TAG_OWNER = "owner";

	public static final String TAG_TRACK = "track";
	public static final String TAG_PT = "pt";
	public static final String TAG_CREATE = "create";
	public static final String TAG_RESUME = "resume";
	public static final String TAG_SUSPEND = "suspend";

	public static final String ATTR_ACCURACY = "acc";
	public static final String ATTR_TIME = "t";
	public static final String ATTR_MODE = "mode";
	public static final String ATTR_NMEA = "nmea";
	public static final String ATTR_LON = "lon";
	public static final String ATTR_LAT = "lat";
	public static final String ATTR_ELE = "ele";
	public static final String ATTR_SPEED = "speed";

	public static final int VAL_ACTIVE = 1;
	public static final int VAL_INACTIVE = 2;
	public static final int VAL_INVALID = 3;

	/** Track is managed by user. */
	public static final int VAL_NORMAL_TRACK = 1;

	/** Track is daily log */
	public static final int VAL_DAY_TRACK = 2;

	/** About train speed. */
	public static final double MAX_SPEED = 250.0D;

	private Log log = Logging.getLog("Track");

	/** Default constructor. */
	public Track() {
		super(TABLE_NAME);
	}

	public void addData(JXElement anElement) throws UtopiaException {

		Vector elements = new Vector(1);
		elements.add(anElement);
		addData(elements);
	}

	/**
	 * Add data elements.
	 *
	 * @param theElements track data JXElements (points events etc)
	 * @return
	 * @throws UtopiaException
	 */
	public Vector addData(Vector theElements) throws UtopiaException {
		Transaction transaction = oase.startTransaction();

		try {
			JXElement nextElement = null;
			Location firstLocation = null;
			Location lastLocation = null;
			Area area = null;
			int pointCount = 0;
			StringBuffer appendData = new StringBuffer(128);

			// TODO fix, this is error prone
			GPSDecoder.setStartDate(DATE_STRING.format(new Date()));

			Vector invalidSamples = new Vector(1);
			for (int i = 0; i < theElements.size(); i++) {
				nextElement = (JXElement) theElements.get(i);

				// Set timestamp if not set
				if (!nextElement.hasAttr(ATTR_TIME)) {
					nextElement.setAttr(ATTR_TIME, System.currentTimeMillis());
				}

				// Handle point element
				if (nextElement.getTag().equals(TAG_PT)) {

					// Increment point count
					pointCount = getIntValue(FIELD_PTCOUNT);

					// Get last location only once
					if (lastLocation == null) {
						lastLocation = (Location) getRelatedObject(Location.class, REL_TAG_LAST_PT);
					}

					// Get area only once
					if (area == null) {
						area = (Area) getRelatedObject(Area.class, REL_TAG_AREA);
					}

					// Get current value of last point
					GeoPoint lastPoint = lastLocation.getLocation();

					// Handle GPS sample data
					if (nextElement.hasAttr(ATTR_NMEA)) {
						// Parse the NMEA line
						GPSSample geoSample = GPSDecoder.parseSample(nextElement.getAttr(ATTR_NMEA));

						// Skip on parse error
						if (geoSample == null) {
							log.warn("Cannot parse NMEA line : " + nextElement.getAttr(ATTR_NMEA));
							invalidSamples.add(nextElement);
							continue;
						}

						// NOTE: always use time explicitly send by client (never GPS time)
						geoSample.timestamp = nextElement.getLongAttr(ATTR_TIME);

						// Transer parsed location values to point element
						nextElement.setAttr(ATTR_LON, LAT_LON_FORMAT.format(geoSample.lon));
						nextElement.setAttr(ATTR_LAT, LAT_LON_FORMAT.format(geoSample.lat));
						nextElement.setAttr(ATTR_ELE, geoSample.elevation);
						nextElement.setAttr(ATTR_ACCURACY, geoSample.accuracy);

						// Always calc speed even if provided by GPS (GPRMC)
                        // We don't use GPS-provided speed since this may be from
                        // a "spike" and thus an erroneous sample.
                        if (pointCount > 1) {
							geoSample.speed = lastPoint.speed(geoSample);

							// Check for unreasonable speed (GPS error)
							// TODO: fix; we may have a sick case where the very first sample is erroneous
							if (geoSample.speed > MAX_SPEED) {
								log.warn("track-" + getId() + " Discard sample speed=" +geoSample.speed + " s=" + nextElement.getAttr(ATTR_NMEA));
								invalidSamples.add(nextElement);
								continue;
							}
						}

						// Always set speed
						nextElement.setAttr(ATTR_SPEED, SPEED_FORMAT.format(geoSample.speed));
					} else if (nextElement.hasAttr(ATTR_LON) && nextElement.hasAttr(ATTR_LAT)) {
						// Data already in lon,lat
						nextElement.setAttr(ATTR_LON, LAT_LON_FORMAT.format(nextElement.getDoubleAttr(ATTR_LON)));
						nextElement.setAttr(ATTR_LAT, LAT_LON_FORMAT.format(nextElement.getDoubleAttr(ATTR_LAT)));

						// Fill in missing attrs
						if (!nextElement.hasAttr(ATTR_TIME)) {
							nextElement.setAttr(ATTR_TIME, Sys.now());
						}
						if (!nextElement.hasAttr(ATTR_ELE)) {
							nextElement.setAttr(ATTR_ELE, 0.0D);
						}
						if (!nextElement.hasAttr(ATTR_ACCURACY)) {
							nextElement.setAttr(ATTR_ACCURACY, 100);
						}
					}

					setIntValue(FIELD_PTCOUNT, ++pointCount);

					// Create object for new point
					GeoPoint newPoint = new GeoPoint(nextElement.getDoubleAttr(ATTR_LON), nextElement.getDoubleAttr(ATTR_LAT), nextElement.getDoubleAttr(ATTR_ELE), nextElement.getLongAttr(ATTR_TIME));
					if (!nextElement.hasAttr(ATTR_SPEED) && pointCount > 0) {
						nextElement.setAttr(ATTR_SPEED, SPEED_FORMAT.format(lastPoint.speed(newPoint)));
					}

					// Update first Location only on first point
					if (pointCount == 1) {
						firstLocation = (Location) getRelatedObject(Location.class, REL_TAG_FIRST_PT);
						firstLocation.setLocation(newPoint);
					}

					// Update total distance if at least two points in Track
					if (pointCount > 1) {
						double distance = getRealValue(FIELD_DISTANCE);

						// Use last Location
						distance += lastPoint.distance(newPoint);

						// Set formatted distance
						setRealValue(FIELD_DISTANCE, Double.parseDouble(DISTANCE_FORMAT.format(distance)));
					}


					// Init area on first two points in Track
					if (pointCount == 2) {
						GeoBox geoBox = new GeoBox(lastPoint, newPoint);
						area.setArea(geoBox);
					}

					if (pointCount > 2) {
						// If new point expands current area expand it
						GeoBox geoBox = area.getArea();
						if (geoBox.expand(newPoint)) {
							area.setArea(geoBox);
						}
					}

					// Always update the last Location
					lastLocation.setLocation(newPoint);
				}

				// Remember last event
				setXMLValue(FIELD_LAST_EVT, nextElement);

				// Add to data buffer
				appendData.append(nextElement.toFormattedString());

				// Enddate update
				setEndDate(nextElement.getLongAttr(ATTR_TIME));
			}

			// Append all new data in one action
			FileField fileField = getDataFileField();
			fileField.append(appendData.toString().getBytes());

			// If related locations updated, save them
			if (firstLocation != null) {
				firstLocation.saveUpdate();
			}

			if (lastLocation != null) {
				lastLocation.saveUpdate();
			}

			if (area != null && pointCount > 1) {
				area.saveUpdate();
			}

			// Save changes to this Track
			saveUpdate();

			transaction.commit();

			// Remove invalid samples
			if (theElements.removeAll(invalidSamples)) {
				// log.warn("removed " + invalidSamples.size() + " invalid samples");
			}
			return theElements;
		} catch (UtopiaException ue) {
			log.warn("Error in " + className + ".addData ", ue);
			oase.cancelTransaction(transaction);
			throw ue;
		} catch (Throwable t) {
			oase.cancelTransaction(transaction);
			log.error("Exception in " + className + ".addData", t);
			throw new UtopiaException("Unexpected Exception in " + className + ".addData", t);
		}
	}


	/**
	 * Clear all data elements (track content).
	 *
	 * @throws UtopiaException
	 */
	public void clearData() throws UtopiaException {
		try {
			// Create temp file.
			File emptyFile = File.createTempFile("tcl", ".txt");
			FileField fileField = getDataFileField();
			fileField.setIncomingFile(emptyFile);
			setIntValue(FIELD_PTCOUNT, 0);
			saveUpdate();

		} catch (Throwable t) {
			log.error("Exception in " + className + ".clearData", t);
			throw new UtopiaException("Unexpected Exception in " + className + ".clearData", t);
		}

	}

	/**
	 * Creates a new Track (but does not insert into DB).
	 *
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard exception
	 */
	static public Track create(Oase anOase) throws UtopiaException {
		try {
			Track track = (Track) anOase.get(Track.class);
			Record record = track.getRecord();
			FileField fileField = record.createFileField(null);
			record.setFileField(FIELD_DATA, fileField);
			return track;
		} catch (Exception e) {
			throw new UtopiaException("Exception in Track.create", e);
		}
	}

	/**
	 * Creates a relation between this Track and a Record.
	 *
	 * @param aRecordId The record id of the target object.
	 * @param aTag optional relation tag.
	 * @exception UtopiaException Standard exception
	 */
	public void createRelation(int aRecordId, String aTag) throws UtopiaException {
		try {
			createRelation(oase.getFinder().read(aRecordId), aTag);
		} catch (OaseException e) {
			throw new UtopiaException("Exception in createRelation", e);
		}
	}

	/**
	 * Creates a relation between this Track and a Record.
	 *
	 * @param aRecord The record of the target object.
	 * @param aTag optional relation tag.
	 * @exception UtopiaException Standard exception
	 */
	public void createRelation(Record aRecord, String aTag) throws UtopiaException {
		try {
			oase.getRelater().relate(getRecord(), aRecord, aTag);
		} catch (OaseException e) {
			throw new UtopiaException("Exception in createRelation", e);
		}
	}


	/**
	 * Inserts a Track and related objects in DB.
	 *
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard exception
	 */
	public void insert(int aPersonId, String aName, int aTrackType, long aTime) throws UtopiaException {
		Transaction transaction = oase.startTransaction();
		try {
			if (aName == null || aName.length() == 0) {
				aName = DATE_STRING.format(new Date(aTime));
			}
			setStringValue(FIELD_NAME, aName);
			setType(aTrackType);
			setState(VAL_ACTIVE);
			setStartDate(aTime);
			setEndDate(aTime + 1);
			FileField fileField = getDataFileField();
			JXElement createElm = new JXElement(TAG_CREATE);
			createElm.setAttr(ATTR_TIME, aTime);
			fileField.append(createElm.toString().getBytes());

			saveInsert();

			Location firstLocation = Location.create(oase);
			firstLocation.setStringValue(Location.FIELD_NAME, "First Track Location");
			firstLocation.setLongValue(Location.FIELD_TIME, aTime);
			firstLocation.setIntValue(Location.FIELD_TYPE, Location.VAL_TYPE_TRACK_PT);
			firstLocation.saveInsert();

			Location lastLocation = Location.create(oase);
			lastLocation.setStringValue(Location.FIELD_NAME, "Last Track Location");
			lastLocation.setLongValue(Location.FIELD_TIME, aTime + 1);
			lastLocation.setIntValue(Location.FIELD_TYPE, Location.VAL_TYPE_TRACK_PT);
			lastLocation.saveInsert();

			Area area = Area.create(oase);
			area.setStringValue(Area.FIELD_NAME, "Track Area");
			area.setIntValue(Area.FIELD_TYPE, Area.VAL_TYPE_TRACK_AREA);
			area.saveInsert();

			// Relate Track to first and last Location, Area and Person
			createRelation(firstLocation.getRecord(), REL_TAG_FIRST_PT);
			createRelation(lastLocation.getRecord(), REL_TAG_LAST_PT);
			createRelation(area.getRecord(), REL_TAG_AREA);
			createRelation(aPersonId, REL_TAG_OWNER);

			// Commit to DB
			transaction.commit();
		} catch (UtopiaException ue) {
			oase.cancelTransaction(transaction);
			throw ue;
		} catch (Throwable t) {
			oase.cancelTransaction(transaction);
			throw new UtopiaException("Exception in Track.insert", t);
		}
	}


	/**
	 * Gets data file field.
	 *
	 * @return FileField attr
	 */
	public FileFieldImpl getDataFileField() {
		return (FileFieldImpl) getRecord().getFileField(FIELD_DATA);
	}

	/**
	 * Gets data file field as (huge!) String.
	 *
	 * @return FileField attr
	 */
	public String getDataString() throws UtopiaException {
		try {
			FileInputStream fis = getDataFileField().getFileInputStream();
			return IO.inputStream2String(fis);
		} catch (Exception e) {
			log.error("Exception in " + className + ".getSamples" + e);
			throw new UtopiaException("Exception in " + className + ".getSamples", e);
		}
	}

	/**
	 * Gets data file field as Vector of JXElements.
	 *
	 * @return Vector of JXElement
	 */
	public Vector getDataElements() throws UtopiaException {
		try {
			final Vector result = new Vector();
			JXBuilder builder = new JXBuilder(
					new JXBuilderListener() {
						/** Called by XmlElementParser when it parsed and created an JXElement. */
						public void element(JXElement e) {
							result.add(e);
						}

						/** Called when parser encounters an error. */
						public void error(String msg) {
							log.warn("getDataElements() error: " + msg);
						}

						/**
						 * End of input stream is reached.
						 *
						 * This may occur when listening for multiple documents on a stream.
						 *
						 * @param message text message
						 * @param anException optional exception that caused the stream end
						 */
						public void endInputStream(String message, Throwable anException) {
							log.trace("getDataElements() EOF reached: " + message + " e=" + anException);
						}

					}
			);

			// Track data file
			File file = getDataFileField().getStoredFile();

			// Only makes sense to parse a file with content
			if (file != null && file.exists() && file.length() > 0) {
				builder.setMultiDoc(true);
				builder.build(getDataFileField().getFileInputStream());
			}
			return result;
		} catch (Throwable t) {
			log.warn("Exception in " + className + ".getDataElements" + t);
			throw new UtopiaException("Exception in " + className + ".getDataElements", t);
		}
	}

	/**
	 * Gets the geoLog's name.
	 *
	 * @return geoLog's name.
	 */
	public String getName() {
		return getStringValue(FIELD_NAME);
	}

	/**
	 * Gets the geoLog's name.
	 *
	 * @return geoLog's name.
	 */
	public String getDescription() {
		return getStringValue(FIELD_DESCRIPTION);
	}

	/**
	 * Gets the startdate field.
	 *
	 * @return the time.
	 */
	public long getStartDate() {
		return getLongValue(FIELD_START_DATE);
	}

	/**
	 * Gets the geoLog's state.
	 *
	 * @return geoLog's state.
	 */
	public int getState() {
		return getIntValue(FIELD_STATE);
	}

	/**
	 * Gets the track type.
	 *
	 * @return track type.
	 */
	public int getType() {
		return getIntValue(FIELD_TYPE);
	}

	/**
	 * Gets the enddate field.
	 *
	 * @return the time.
	 */
	public long getEndDate() {
		return getLongValue(FIELD_END_DATE);
	}

	/**
	 * Gets the extra field.
	 *
	 * @return the extra field.
	 */
	public JXElement getExtra() {
		return getExtraElementValue(FIELD_EXTRA);
	}

	/**
	 * Sets the end date to current time.
	 */
	public void setEndDate(long aTime) {
		setLongValue(FIELD_END_DATE, aTime);
	}

	/**
	 * Sets the start date to current time.
	 */
	public void setStartDate(long aTime) {
		setLongValue(FIELD_START_DATE, aTime);
	}

	/**
	 * Sets state.
	 */
	public void setState(int aValue) {
		setIntValue(FIELD_STATE, aValue);
	}

	/**
	 * Sets type.
	 */
	public void setType(int aValue) {
		setIntValue(FIELD_TYPE, aValue);
	}

	/**
	 * Overides Object.toString()
	 *
	 * @return the string
	 */
	public String toString() {
		return "Track(" + getId() + "," + getName() +")";
	}
}

/*
* $Log: Track.java,v $
* Revision 1.21  2006-04-05 13:10:41  just
* implemented daytracks
*
* Revision 1.20  2006-04-04 15:15:33  just
* fix invalid gps sample writing
*
* Revision 1.19  2006-03-08 17:12:34  just
* no message
*
* Revision 1.18  2006/02/12 19:55:25  just
* *** empty log message ***
*
* Revision 1.17  2006/01/06 22:26:22  just
* *** empty log message ***
*
* Revision 1.16  2005/12/29 13:48:33  just
* *** empty log message ***
*
* Revision 1.15  2005/12/07 12:49:01  just
* *** empty log message ***
*
* Revision 1.14  2005/11/18 16:33:59  just
* *** empty log message ***
*
* Revision 1.13  2005/11/05 12:03:04  just
* *** empty log message ***
*
* Revision 1.12  2005/10/23 18:21:42  just
* *** empty log message ***
*
* Revision 1.11  2005/10/21 23:52:49  just
* *** empty log message ***
*
* Revision 1.10  2005/10/20 09:12:04  just
* *** empty log message ***
*
* Revision 1.9  2005/10/19 15:10:07  just
* *** empty log message ***
*
* Revision 1.8  2005/10/19 09:39:22  just
* *** empty log message ***
*
* Revision 1.7  2005/10/18 12:54:44  just
* *** empty log message ***
*
* Revision 1.6  2005/10/18 07:38:00  just
* *** empty log message ***
*
* Revision 1.5  2005/10/06 13:51:08  just
* *** empty log message ***
*
* Revision 1.4  2005/09/29 11:53:33  just
* *** empty log message ***
*
* Revision 1.3  2005/09/29 10:46:56  just
* *** empty log message ***
*
* Revision 1.2  2005/09/28 18:39:58  just
* *** empty log message ***
*
* Revision 1.1  2005/09/28 18:33:00  just
* *** empty log message ***
*
* Revision 1.14  2005/09/28 15:43:25  just
* *** empty log message ***
*
* Revision 1.13  2005/09/28 14:59:37  just
* *** empty log message ***
*
* Revision 1.12  2005/09/28 14:09:18  just
* *** empty log message ***
*
* Revision 1.11  2005/09/27 13:20:47  just
* *** empty log message ***
*
* Revision 1.10  2005/09/27 09:49:51  just
* *** empty log message ***
*
* Revision 1.9  2005/09/26 21:23:11  just
* *** empty log message ***
*
* Revision 1.8  2005/09/26 18:46:53  just
* *** empty log message ***
*
* Revision 1.7  2005/09/25 14:14:07  just
* *** empty log message ***
*
* Revision 1.6  2005/09/23 09:33:18  just
* *** empty log message ***
*
* Revision 1.5  2005/09/22 16:11:05  just
* *** empty log message ***
*
* Revision 1.4  2005/09/21 14:39:04  just
* *** empty log message ***
*
* Revision 1.3  2005/09/21 14:10:40  just
* *** empty log message ***
*
* Revision 1.3  2005/09/21 14:09:39  just
* *** empty log message ***
*
* Revision 1.3  2005/09/05 21:43:02  just
* *** empty log message ***
*
* Revision 1.2  2005/07/28 21:32:20  just
* geolog service impl
*
* Revision 1.1  2005/07/28 15:24:56  just
* first version geolog
*
*
*/


