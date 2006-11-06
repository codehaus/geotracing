// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.server;

import org.keyworx.common.util.IO;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;

import java.util.Properties;

/**
  * Handles all logic related to commenting.
  * <p/>
  * Uses Oase directly for DB updates.
  *
  * @author Just van den Broecke
  * @version $Id$
 */
public class CommentLogic {
	public static final String TABLE_COMMENT = "kw_comment";
	public static final String TABLE_PERSON = "utopia_person";

	public static final String FIELD_CONTENT = "content";
	public static final String FIELD_OWNER = "owner";
	public static final String FIELD_STATE = "state";
	public static final String FIELD_URL = "url";
	public static final String FIELD_TARGET = "target";
	public static final String FIELD_TARGET_TABLE = "targettable";
	public static final String FIELD_TARGET_PERSON = "targetperson";

	public static final int STATE_UNREAD = 1;
	public static final int STATE_READ = 2;
	private static final Properties properties = new Properties();
	public static final String PROP_MAX_CONTENT_CHARS = "max-content-chars";
	public static final String PROP_ALLOW_ANONYMOUS = "allow-anonymous";

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
			// Check if anonymous comments allowed
			if ((aRecord.getIntField(FIELD_OWNER) < 0)  &&
				!properties.getProperty(PROP_ALLOW_ANONYMOUS, "false").equals("true")) {
				throw new UtopiaException("Anonymous commenting not allowed");
			}

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

			// Check and optionally trim maximum size allowed
			content = trimContent(content);
			aRecord.setStringField(FIELD_CONTENT, IO.forHTMLTag(content));

			// Escape special chars in content
			String url = aRecord.getStringField(FIELD_URL);

			// Optionally prefix URL with "http://"
			if (url != null && url.length() > 0 && !url.startsWith("http://")) {
				aRecord.setStringField(FIELD_URL, "http://" + url);
			}

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
	 * Updates a comment state.
	 *
	 * This may be used to mark a comment as read or reset to unread.
	 *
	 * @param aCommentId a comment id
	 * @throws UtopiaException Standard exception
	 */
	public void updateState(int aCommentId, int aNewState) throws UtopiaException {
		try {
			Record record = oase.getFinder().read(aCommentId, TABLE_COMMENT);
			if (record == null) {
				throw new UtopiaException("Cannot read record for update id=" + aCommentId, ErrorCode.__6006_database_irregularity_error);
			}

			// Set to new state
			record.setIntField(FIELD_STATE, aNewState);

			// Finally try to update
			oase.getModifier().update(record);
		} catch (OaseException oe) {
			throw new UtopiaException("Cannot read or update comment record", oe, ErrorCode.__6006_database_irregularity_error);
		} catch (Throwable t) {
			throw new UtopiaException("Exception in CommentLogic.updateState() : " + t.toString(), ErrorCode.__6005_Unexpected_error);
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

	public static String getProperty(String propertyName) {
		return (String) properties.get(propertyName);
	}

	protected static void setProperty(String propertyName, String propertyValue) {
		properties.put(propertyName, propertyValue);
	}

	/** Trim content for empty begin/end and maximum chars allowed. */
	protected String trimContent(String aContentString) {
		String result = aContentString.trim();

		// Check for maximum allowed content size, trim if necessary
		int maxContentSize = Integer.parseInt(properties.getProperty(PROP_MAX_CONTENT_CHARS, "512"));
		if (aContentString.length() > maxContentSize) {
			result = result.substring(0, maxContentSize);
		}
		return result;
	}

}

