// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.handler;

import org.keyworx.common.util.IO;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.Person;
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
	public static final String PROP_ALLOW_ANONYMOUS = "allow-anonymous";
	public static final String PROP_MAX_COMMENTS_PER_TARGET = "max-commments-per-target";
	public static final String PROP_MAX_CONTENT_CHARS = "max-content-chars";
	public static final String PROP_THREAD_ALERT = "thread-alert";

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
	 * Util: get number of comments for given target record.
	 *
	 * @param oase oase session (for using this in multiple contexts)
	 * @param aTargetId id of target record
	 * @return the number of comments
	 * @throws UtopiaException Standard exception
	 */
	public static int getCommentCountForTarget(Oase oase, int aTargetId) throws UtopiaException {
		try {
			Record[] records = oase.getFinder().freeQuery("select count(target) AS comments from " + TABLE_COMMENT + " where " + FIELD_TARGET + " = " + aTargetId);
			return records.length == 0 ? 0 : Integer.parseInt(records[0].getField("comments").toString());
		} catch (Throwable t) {
			throw new UtopiaException("Cannot count comment records for target=" + aTargetId, t, ErrorCode.__6006_database_irregularity_error);
		}
	}


	/**
	 * Util: get number of comments for given owner record.
	 *
	 * @param oase oase session (for using this in multiple contexts)
	 * @param anOwnerId id of owner record
	 * @return the number of comments
	 * @throws UtopiaException Standard exception
	 */
	public static int getCommentCountForOwner(Oase oase, int anOwnerId) throws UtopiaException {
		try {
			Record[] records = oase.getFinder().freeQuery("select count(owner) AS comments from " + TABLE_COMMENT + " where " + FIELD_OWNER + " = " + anOwnerId);
			return records.length == 0 ? 0 : Integer.parseInt(records[0].getField("comments").toString());
		} catch (Throwable t) {
			throw new UtopiaException("Cannot count comment records for owner=" + anOwnerId, t, ErrorCode.__6006_database_irregularity_error);
		}
	}

	/**
	 * Util: get person ids of all users that have commented on a target.
	 *
	 * @param oase oase session (for using this in multiple contexts)
	 * @param aTargetId id of target record
	 * @return the array of commenter (person) ids
	 * @throws UtopiaException Standard exception
	 */
	public static int[] getCommenterIds(Oase oase, int aTargetId) throws UtopiaException {
		try {
			Record[] records = oase.getFinder().freeQuery("select DISTINCT(owner) from " + CommentLogic.TABLE_COMMENT + " WHERE target = " + aTargetId + " AND owner IS NOT NULL");
			int[] result = new int[records.length];
			for (int i=0; i < records.length; i++) {
				result[i] = records[i].getIntField(FIELD_OWNER);
			}
			return result;
		} catch (Throwable t) {
			throw new UtopiaException("Cannot get commenters records for target=" + aTargetId, t, ErrorCode.__6006_database_irregularity_error);
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

			// Check if max comment count exceeded
			if ((getCommentCountForTarget(oase, targetId) >=
				Integer.parseInt(properties.getProperty(PROP_MAX_COMMENTS_PER_TARGET, "128")))) {
				throw new UtopiaException("Maximum comment count exceeded");
			}

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

			// Comment insert OK, send optional thread alerts
			// These are comments to each person that has commented on this item
			if (properties.getProperty(PROP_THREAD_ALERT, "false").equals("true")) {
				int[] commenterIds = getCommenterIds(oase, targetId);
				Record alertComment;
				for (int i=0; i < commenterIds.length; i++) {
					// Skip optional user who made comment
					if (!aRecord.isNull(CommentLogic.FIELD_OWNER)) {
						if (aRecord.getIntField(FIELD_OWNER) == commenterIds[i]) {
							continue;
						}
					}


					alertComment = createRecord();
					if (!aRecord.isNull(CommentLogic.FIELD_OWNER)) {
						alertComment.setIntField(FIELD_OWNER, aRecord.getIntField(FIELD_OWNER));
					}
					alertComment.setIntField(FIELD_TARGET_PERSON, commenterIds[i]);
					alertComment.setIntField(FIELD_TARGET, aRecord.getId());
					alertComment.setIntField(FIELD_TARGET_TABLE, aRecord.getTableDef().getId());
					alertComment.setIntField(FIELD_STATE, STATE_UNREAD);

					String from = "anonymous";
					if (!aRecord.isNull(CommentLogic.FIELD_OWNER)) {
						int ownerId = aRecord.getIntField(CommentLogic.FIELD_OWNER);
						Person ownerPerson = (Person) oase.get(Person.class,  ownerId + "");
						from = ownerPerson.getAccount().getLoginName();
					}

					alertComment.setStringField(FIELD_CONTENT, "**  " + from + " added comment ** ");

					// Finally try to insert
					oase.getModifier().insert(alertComment);

					EventPublisher.commentAdd(alertComment, oase);

				}

			}
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
	 * Updates state for zero or more comments.
	 *
	 * This may be used to mark comments as read or reset to unread.
	 * The specific comments to update can be selected by specifying
	 * parameters for target person (usually), one specific comment (commentId) or
	 * all comments on specific target (targetId).
	 *
	 * @param aTargetPersonId a target person id or -1
	 * @param aState a new state value
	 * @param aCommentId a comment id or -1
	 * @param aTargetId a target id or -1
	 * @return empty string or comma separted list of updated comment ids
	 * @throws UtopiaException Standard exception
	 */
	public String updateState(int aTargetPersonId, int aState, int aCommentId, int aTargetId) throws UtopiaException {
		try {
			// Find the records to update
			String query = "select * from " + TABLE_COMMENT + " WHERE state != " + aState;

			// Optional: target person
			if (aTargetPersonId > 0) {
				query += " AND targetperson =" + aTargetPersonId;
			}

			// Optional: specific comment id (if target person specified may exist but not found)
			if (aCommentId > 0) {
				query += " AND id =" + aCommentId;
			}

			// Optional: specific target id (if target person specified may exist but not found)
			if (aTargetId > 0) {
				query += " AND target =" + aTargetId;
			}

			// Do query
			Record[] records = oase.getFinder().freeQuery(query + " ORDER BY id", CommentLogic.TABLE_COMMENT);

			// Update each record to new state
			String ids="";
			for (int i=0; i < records.length; i++) {
				// Set to new state
				records[i].setIntField(FIELD_STATE, aState);

				// Finally try to update
				oase.getModifier().update(records[i]);

				ids += (i != 0) ? "," + records[i].getIdString() :  records[i].getIdString();
			}

			// Return comma separated list of ids
			return ids;
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

	/** Properties passed on from Handler. */
	public static String getProperty(String propertyName) {
		return (String) properties.get(propertyName);
	}

	/** Properties passed on from Handler. */
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

