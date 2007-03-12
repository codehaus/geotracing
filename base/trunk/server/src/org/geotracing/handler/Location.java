// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.handler;

import org.geotracing.gis.PostGISUtil;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.BaseImpl;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.postgis.PGgeometryLW;
import org.postgis.Point;

/**
 * The Location data object
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class Location extends BaseImpl {
	/*
	<field name="id" type="INTEGER" required="true" key="true" />
		<field name="name" type="STRING" size="64" required="false" />
		<field name="description" type="TEXT" required="false" />


		<field name="lon" type="REAL" required="true" default="-180"/>
		<field name="lat" type="REAL" required="true" default="90" />
		<field name="ele" type="REAL" required="false" default="0" />
		<field name="time" type="TIMESTAMP" />

		<field name="type" type="INTEGER" default="1" required="false" />

		<field name="state" type="INTEGER" default="1" required="false" />
		<field name="creationdate" type="TIMESTAMP"/>
		<field name="modificationdate" type="TIMESTAMP"/>
		<field name="extra" type="XML" required="false"/>
	*/
	public static final String TABLE_NAME = "g_location";

	public static final String FIELD_NAME = "name";
	public static final String FIELD_DESCRIPTION = "description";
	public static final String FIELD_STATE = "state";
	public static final String FIELD_CREATION_DATE = "creationdate";
	public static final String FIELD_MODIFICATION_DATE = "modificationdate";
	public static final String FIELD_ELE = "ele";
	public static final String FIELD_LON = "lon";
	public static final String FIELD_LAT = "lat";
	public static final String FIELD_TIME = "time";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_EXTRA = "extra";
	public static final String FIELD_POINT = "point";

	public static final int VAL_TYPE_MEDIUM = 1;
	public static final int VAL_TYPE_TRACK_PT = 2;
	public static final int VAL_TYPE_USER_LOC = 3;

	/**
	 * Default constructor.
	 */
	public Location() {
		super(TABLE_NAME);
	}

	/**
	 * Creates a new Location (but does not insert into DB).
	 *
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	static public Location create(Oase anOase) throws UtopiaException {
		return (Location) anOase.get(Location.class);
	}

	/**
	 * Creates a relation between this Location and a Record.
	 *
	 * @param aRecordId The record id of the target object.
	 * @param aTag	  optional relation tag.
	 * @throws UtopiaException Standard exception
	 */
	public void createRelation(int aRecordId, String aTag) throws UtopiaException {
		try {
			createRelation(oase.getFinder().read(aRecordId), aTag);
		} catch (OaseException e) {
			throw new UtopiaException("Exception in createRelation", e);
		}
	}

	/**
	 * Creates a relation between this Location and a Record.
	 *
	 * @param aRecord The record of the target object.
	 * @param aTag	optional relation tag.
	 * @throws UtopiaException Standard exception
	 */
	public void createRelation(Record aRecord, String aTag) throws UtopiaException {
		try {
			oase.getRelater().relate(getRecord(), aRecord, aTag);
		} catch (OaseException e) {
			throw new UtopiaException("Exception in createRelation", e);
		}
	}

	/**
	 * Get lon,lat,elevation and time values.
	 *
	 * @return a GeoPoint
	 */
	public Point getPoint() {
		PGgeometryLW geom = (PGgeometryLW) getRecord().getObjectField(FIELD_POINT);
		return (Point) (geom != null ? geom.getGeometry() : null);
	}

	/**
	 * Set Point value.
	 *
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public void setPoint(Point aPoint) throws UtopiaException {
		// For the time being we set conventional columns as well...
		setOldAttrs(aPoint.x, aPoint.y, aPoint.z, (long) aPoint.m);

		// Update geometry column using PostGIS Geometry wrapper
		getRecord().setObjectField(FIELD_POINT, new PGgeometryLW(aPoint));
	}

	/**
	 * Overides Object.toString()
	 *
	 * @return the string
	 */
	public String toString() {
		Point point = getPoint();
		return point != null ? point.toString() : "empty";
	}

	/**
	 * Set lon,lat and time values (for forward compat, to be removed).
	 *
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	protected void setOldAttrs(double aLon, double aLat, double anEle, long aTime) throws UtopiaException {
		try {
			// For the time being we set conventional columns as well...
			setRealValue(FIELD_LON, aLon);
			setRealValue(FIELD_LAT, aLat);
			setRealValue(FIELD_ELE, anEle);
			setLongValue(FIELD_TIME, aTime);
		} catch (Throwable e) {
			throw new UtopiaException("Exception in Location.setOldAttrs", e);
		}
	}
}

/*
* $Log: Location.java,v $
* Revision 1.5  2006-04-05 13:10:41  just
* implemented daytracks
*
* Revision 1.4  2005/10/19 09:39:22  just
* *** empty log message ***
*
* Revision 1.3  2005/09/29 10:46:56  just
* *** empty log message ***
*
*
* Revision 1.1  2005/09/26 22:18:15  just
* *** empty log message ***
*
*
*
*/


