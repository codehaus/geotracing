// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.handler;

import org.geotracing.gis.GeoBox;
import org.keyworx.utopia.core.data.BaseImpl;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.postgis.Point;

/**
 * The Area data object
 *
 * Maps the geoLog data to an object
 *
 * @author   Just van den Broecke
 * @version $Id$
 */
public class Area extends BaseImpl {
	/*
	<field name="id" type="INTEGER" required="true" key="true" />

	<!-- Describes bounding box from bottom-left (SW) to top-right (NE) -->
	<field name="lon1" type="REAL" required="true" default="-180"/>
	<field name="lat1" type="REAL" required="true" default="-90" />
	<field name="lon2" type="REAL" required="true" default="180"/>
	<field name="lat2" type="REAL" required="true" default="90" />

	<field name="name" type="STRING" size="64" required="false" />
	<field name="description" type="TEXT" required="false" />
	<field name="type" type="INTEGER" required="false" />
	<field name="state" type="INTEGER" required="false" />
	<field name="creationdate" type="TIMESTAMP"/>
	<field name="modificationdate" type="TIMESTAMP"/>
	<field name="extra" type="XML" required="false"/>
	*/
	public static final String TABLE_NAME = "g_area";

	public static final String FIELD_LON1 = "lon1";
	public static final String FIELD_LAT1 = "lat1";
	public static final String FIELD_LON2 = "lon2";
	public static final String FIELD_LAT2 = "lat2";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_DESCRIPTION = "description";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_STATE = "state";
	public static final String FIELD_CREATION_DATE = "creationdate";
	public static final String FIELD_MODIFICATION_DATE = "modificationdate";
	public static final String FIELD_EXTRA = "extra";

	public static final int VAL_TYPE_GEN = 1;
	public static final int VAL_TYPE_TRACK_AREA = 2;

	/** Default constructor. */
	public Area() {
		super(TABLE_NAME);
	}


	/**
	 * Creates a new Area (but does not insert into DB).
	 *
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard exception
	 */
	static public Area create(Oase anOase) throws UtopiaException {
		try {
			return (Area) anOase.get(Area.class);
		} catch (Exception e) {
			throw new UtopiaException("Exception in Areacreate", e);
		}
	}

	/**
	 * Get lon,lat, elevation and time values.
	 *
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard exception
	 */
	public GeoBox getArea() throws UtopiaException {
		return new GeoBox(getRealValue(FIELD_LON1), getRealValue(FIELD_LAT1), getRealValue(FIELD_LON2), getRealValue(FIELD_LAT2));
	}


	/**
	 * Set area by GeoBox.
	 * 
	 * @param aGeoBox the area specification
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard exception
	 */
	public void setArea(GeoBox aGeoBox) throws UtopiaException {
		setArea(aGeoBox.lon1, aGeoBox.lat1, aGeoBox.lon2, aGeoBox.lat2);
	}

	/**
	 * Set area by two GeoPoints.
	 *
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard exception
	 */
	public void setArea(Point p1, Point p2) throws UtopiaException {
		setArea(new GeoBox(p1, p2));
	}

	/**
	 * Set area by coordinates.
	 *
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard exception
	 */
	public void setArea(double aLon1, double aLat1, double aLon2, double aLat2) throws UtopiaException {
		try {
			setRealValue(FIELD_LON1, aLon1);
			setRealValue(FIELD_LAT1, aLat1);
			setRealValue(FIELD_LON2, aLon2);
			setRealValue(FIELD_LAT2, aLat2);
		} catch (Throwable e) {
			throw new UtopiaException("Exception in Area.setArea", e);
		}
	}

	/**
	 * Overides Object.toString()
	 *
	 * @return the string
	 */
	public String toString() {
		try {
			return getArea().toString();
		} catch (Throwable t) {
			return "error";

		}
	}
}

/*
* $Log: Area.java,v $
* Revision 1.3  2006-03-08 17:12:34  just
* no message
*
* Revision 1.2  2005/09/29 11:53:33  just
* *** empty log message ***
*
* Revision 1.1  2005/09/29 10:46:56  just
* *** empty log message ***
*
*
*
*/


