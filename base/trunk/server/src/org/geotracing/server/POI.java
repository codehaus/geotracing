// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.server;

import org.keyworx.utopia.core.data.BaseImpl;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.geotracing.gis.GeoPoint;

/**
 * The POI data object
 *
 * @author   Just van den Broecke
 * @version $Id: POI.java,v 1.2 2006-02-26 14:14:24 just Exp $
 */
public class POI extends BaseImpl {
	/*
		<field name="id" type="INTEGER" required="true" key="true" />
		<field name="name" type="STRING" size="64" required="true" />
		<field name="description" type="TEXT" required="false" />
        <field name="type" type="STRING" size="32" required="true" default="poi"/>
        <field name="state" type="INTEGER" required="true" default="0"/>
        <field name="value" type="INTEGER" required="false" />
		<field name="time" type="TIMESTAMP" required="true" />
		<field name="creationdate" type="TIMESTAMP"/>
        <field name="modificationdate" type="TIMESTAMP"/>
		<field name="extra" type="XML" />
		*/
	public static final String TABLE_NAME = "g_poi";

	public static final String FIELD_NAME = "name";
	public static final String FIELD_DESCRIPTION = "description";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_STATE = "state";
	public static final String FIELD_VALUE = "value";
	public static final String FIELD_TIME = "time";
	public static final String FIELD_CREATION_DATE = "creationdate";
	public static final String FIELD_MODIFICATION_DATE = "modificationdate";
	public static final String FIELD_EXTRA = "extra";

	/** Default constructor. */
	public POI() {
		super(TABLE_NAME);
	}

	/**
	 * Creates a new POI (but does not insert into DB).
	 *
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard exception
	 */
	static public POI create(Oase anOase, String aName, String aType, long aTime) throws UtopiaException {
		POI poi = (POI) anOase.get(POI.class);

		poi.setStringValue(FIELD_NAME, aName);
		poi.setStringValue(FIELD_TYPE, aType);
		poi.setLongValue(FIELD_TIME, aTime);
		return poi;
	}

	/**
	 * Deletes a POI.
	 *
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard exception
	 */
	static public void delete(Oase anOase, String anId) throws UtopiaException {
		POI poi = (POI) anOase.get(POI.class, anId);
		Location location = poi.getLocation();

		// Delete both poi and related location
		location.delete();
		poi.delete();
	}

	/**
	 * Get Location object related to this POI..
	 *
	 * @return a Location
	 */
	public Location getLocation() throws UtopiaException {
		return (Location) getRelatedObject(Location.class);
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
}

/*
* $Log: POI.java,v $
* Revision 1.2  2006-02-26 14:14:24  just
* no message
*
* Revision 1.1  2005/10/20 09:12:04  just
* *** empty log message ***
*
*
*/


