// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.server;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.gis.GeoPoint;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Sys;
import org.keyworx.common.util.IO;
import org.keyworx.oase.api.*;
import org.keyworx.oase.store.record.FileFieldImpl;
import org.keyworx.server.ServerConfig;
import org.keyworx.utopia.core.data.*;
import org.keyworx.utopia.core.util.Oase;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Implements logic for Track manipulation.
 */
public class CommentLogic {
	public static final String TABLE_COMMENT = "kw_comment";
	public static final String TABLE_PERSON = "utopia_person";

	public static final String FIELD_CONTENT = "content";
	public static final String FIELD_OWNER = "owner";
	public static final String FIELD_STATE = "state";
	public static final String FIELD_TARGET = "target";
	public static final String FIELD_TARGET_TABLE = "targettable";
	public static final String FIELD_TARGET_PERSON = "targetperson";

	public static final int STATE_UNREAD = 1;
	public static final int STATE_READ = 2;

	private Oase oase;

	/*
	* <field name="id" type="INTEGER" required="true" key="true" />
 * <p/>
 * <field name="owner" type="INTEGER" required="false" foreignkey="cascade" />
 * <p/>
 * <field name="target" type="INTEGER" required="true" index="true" foreignkey="cascade" />
 * <field name="targettable" type="INTEGER" required="true" foreignkey="nocascade"  />
 * <field name="targetperson" type="INTEGER" required="true" foreignkey="nocascade"  />
 * <p/>
 * <field name="author" type="STRING" size="64" required="true" default="anon"/>
 * <field name="email" type="STRING" size="64" required="false" />
 * <field name="url" type="STRING" size="128" required="false" />
 * <field name="ip" type="STRING" size="64" required="false" />
 * <field name="content" type="TEXT" required="true" />
 * <p/>
 * <field name="state" type="INTEGER" default="1" required="true" />
 * <p/>
 * <field name="creationdate" type="TIMESTAMP" required="true"/>
 * <field name="modificationdate" type="TIMESTAMP" required="true" />
 * <p/>
 * <field name="extra" type="XML" required="false"/>
 */
	public CommentLogic(Oase o) {
		oase = o;
	}

	/**
	 * Creates an Record object (used for query by example).
	 *
	 * @return the example comment record
	 * @throws UtopiaException Standard exception
	 */
	public Record createExampleRecord() throws UtopiaException {
		try {
			return oase.getFinder().createExampleRecord(TABLE_COMMENT);
		} catch (OaseException oe) {
			throw new UtopiaException("Cannot create comment example record", oe, ErrorCode.__6006_database_irregularity_error);
		}
	}

	/**
	 * Creates a Record object (but does not insert).
	 *
	 * @return the comment record
	 * @throws UtopiaException Standard exception
	 */
	public Record createRecord() throws UtopiaException {
		try {
			return oase.getModifier().create(TABLE_COMMENT);
		} catch (OaseException oe) {
			throw new UtopiaException("Cannot create comment record", oe, ErrorCode.__6006_database_irregularity_error);
		}
	}

	/**
	 * Inserts a comment.
	 *
	 * @param aRecord a comment record
	 * @return the inserted record
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public Record insert(Record aRecord) throws UtopiaException {

		try {
			// TODO check if anonymous comments allowed

			int targetId = aRecord.getIntField(FIELD_TARGET);
			Record targetRecord = oase.getFinder().read(targetId);

			// Determine and set target table id
			int targetTableId = targetRecord.getTableDef().getId();
			aRecord.setIntField(FIELD_TARGET_TABLE, targetTableId);

			// Determine and set target person if possible
			Record persons[] = oase.getRelater().getRelated(targetRecord, TABLE_PERSON, null);
			if (persons.length == 1) {
				aRecord.setIntField(FIELD_TARGET_PERSON, persons[0].getId());
			} else {
				// TODO may determine target person from owner field (int)
			}

			// Set initial state
			aRecord.setIntField(FIELD_STATE, STATE_UNREAD);

			// Escape special chars in content
			String content = aRecord.getStringField(FIELD_CONTENT);
			aRecord.setStringField(FIELD_CONTENT, IO.forHTMLTag(content));

			// TODO check maximum size

			// Finally try to insert
			oase.getModifier().insert(aRecord);
            return aRecord;
		} catch (OaseException oe) {
			throw new UtopiaException("Cannot insert comment record", oe, ErrorCode.__6006_database_irregularity_error);
		} catch (Throwable t) {
			throw new UtopiaException("Exception in CommentLogic.insert() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
		}
	}

	/**
	 * Read comments using query by example.
	 *
	 * @param anExampleRecord a comment example record
	 * @throws UtopiaException Standard exception
	 */
	public Record[] read(Record anExampleRecord) throws UtopiaException {
		try {
			return oase.getFinder().queryTable(anExampleRecord);
		} catch (OaseException oe) {
			throw new UtopiaException("Cannot read comments by example", oe, ErrorCode.__6006_database_irregularity_error);
		}
	}

	/**
	 * Updates a comment.
	 *
	 * @param aRecord a comment record
	 * @return the inserted record
	 * @throws org.keyworx.utopia.core.data.UtopiaException
	 *          Standard exception
	 */
	public Record update(Record aRecord) throws UtopiaException {
		try {
			// TODO implement
			// TODO check maximum size

			// Escape special chars in content
			//String content = aRecord.getStringField(FIELD_CONTENT);
			//aRecord.setStringField(FIELD_CONTENT, IO.forHTMLTag(content));

			// Reset to initial state
			//aRecord.setIntField(FIELD_STATE, STATE_UNREAD);

			// Finally try to update
			//oase.getModifier().update(aRecord);
            return aRecord;
//		} catch (OaseException oe) {
//			throw new UtopiaException("Cannot insert comment record", oe, ErrorCode.__6006_database_irregularity_error);
		} catch (Throwable t) {
			throw new UtopiaException("Exception in CommentLogic.insert() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
		}
	}


	/**
	 * Delete a comment.
	 *
	 * @param aCommentId a comment id
	 * @throws UtopiaException Standard exception
	 */
	public void delete(int aCommentId) throws UtopiaException {
		try {
			oase.getModifier().delete(aCommentId);
		} catch (OaseException oe) {
			throw new UtopiaException("Cannot delete comment record with id=" + aCommentId, oe, ErrorCode.__6006_database_irregularity_error);
		}
	}

}

