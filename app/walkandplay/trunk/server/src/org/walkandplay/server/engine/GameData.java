/****************************************************************
 * Copyright 2004 - Waag Society - www.waag.org - See license below *
 ****************************************************************/

package org.walkandplay.server.engine;

import org.keyworx.amuse.core.Amuse;
import org.keyworx.common.util.Sys;
import org.keyworx.oase.api.Finder;
import org.keyworx.oase.api.MediaFiler;
import org.keyworx.oase.api.Modifier;
import org.keyworx.oase.api.Oase;
import org.keyworx.oase.api.OaseSession;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.api.Relater;

import nl.justobjects.jox.dom.JXElement;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages persistent (DB) data of game.
 *
 * @author Just van den Broecke;
 * @version $Id$
 */
public class GameData implements GameDataDef {
	private OaseSession oaseSession;
	private Finder finder;
	private Modifier modifier;
	private MediaFiler mediaFiler;
	private Relater relater;
	private String gameName;

	public GameData(String aGameName) {
		gameName = aGameName;
	}

	/**
	 * Delete all media records (danger!).
	 */
	public void deleteAllMedia() throws GameException {
		try {
			Record[] records = finder.readAll(TABLE_MEDIUM);
			for (int i = 0; i < records.length; i++) {
				mediaFiler.delete(records[i]);
			}
		} catch (Throwable t) {
			throw new GameException("Error in deleteMedia()", t);
		}
	}

	/**
	 * Delete record by id.
	 */
	public void delete(int aRecordId) throws GameException {
		try {
			modifier.delete(aRecordId);
		} catch (Throwable t) {
			throw new GameException("Error in delete() id=" + aRecordId, t);
		}
	}

	/**
	 * Delete medium by id.
	 */
	public void deleteMedium(int aRecordId) throws GameException {
		try {
			mediaFiler.delete(aRecordId);
		} catch (Throwable t) {
			throw new GameException("Error in deleteMedium() id=" + aRecordId, t);
		}
	}

	/**
	 * Delete all team-related assets i.e. answers, media and boobytraps.
	 */
	public void deleteAssetsForTeam(String aTeamName) throws GameException {
		try {
			Record teamRecord = getTeamRecord(aTeamName);

			// Remove media
			Record[] mediumRecords = relater.getRelated(teamRecord, TABLE_MEDIUM, null);
			String nextName;
			for (int i = 0; i < mediumRecords.length; i++) {
				nextName = mediumRecords[i].getStringField(FIELD_NAME);
				if (nextName != null && nextName.startsWith("test")) {
					// test medium: just unrelate
					relater.unrelate(mediumRecords[i], teamRecord);
					continue;
				}
				modifier.delete(mediumRecords[i].getId(), TABLE_MEDIUM);
			}

			// Remove answers
			Record[] answerRecords = relater.getRelated(teamRecord, TABLE_ANSWER, null);
			for (int i = 0; i < answerRecords.length; i++) {
				modifier.delete(answerRecords[i].getId(), TABLE_ANSWER);
			}

			// Remove placed boobytraps
			Record[] placedBoobytrapRecords = relater.getRelated(teamRecord, TABLE_BOOBYTRAP, REL_TAG_OWNER);
			for (int i = 0; i < placedBoobytrapRecords.length; i++) {
				modifier.delete(placedBoobytrapRecords[i].getId(), TABLE_BOOBYTRAP);
			}

			// Unrelate boobytraps for which we were/are victim
			Record[] victimBoobytrapRecords = relater.getRelated(teamRecord, TABLE_BOOBYTRAP, REL_TAG_VICTIM);
			for (int i = 0; i < victimBoobytrapRecords.length; i++) {
				relater.unrelate(teamRecord, victimBoobytrapRecords[i]);
			}

			// Remove confrontations
			Record[] confrontationRecords = relater.getRelated(teamRecord, TABLE_CONFRONTATION, null);
			for (int i = 0; i < confrontationRecords.length; i++) {
				String winner = confrontationRecords[i].getStringField(FIELD_WINNER);
				if (winner.equals(aTeamName)) {
					modifier.delete(confrontationRecords[i].getId(), TABLE_CONFRONTATION);
				} else {
					relater.unrelate(teamRecord, confrontationRecords[i]);

				}
			}

			// Remove cloaks
			Record[] cloakRecords = relater.getRelated(teamRecord, TABLE_CLOAK, null);
			for (int i = 0; i < cloakRecords.length; i++) {
				modifier.delete(cloakRecords[i].getId(), TABLE_CLOAK);
			}

		} catch (Throwable t) {
			throw new GameException("Error in deleteAssetsForTeam()", t);
		}
	}

	/**
	 * Delete records related to a record.
	 */
	public void deleteRelatedRecords(Record aRecord, String aTable, String aTag) throws GameException {
		try {
			Record[] records = getRelatedRecords(aRecord, aTable, aTag);
			for (int i = 0; i < records.length; i++) {
				delete(records[i].getId());
			}
		} catch (Throwable t) {
			throw new GameException("Error in deleteRelatedRecords()", t);
		}
	}

	/**
	 * Get all zone records.
	 */
	public Record[] getAssignmentsForZone(Record aZoneRecord) throws GameException {
		try {
			return relater.getRelated(aZoneRecord, TABLE_ASSIGNMENT, null);
		} catch (Throwable t) {
			throw new GameException("Error in getAssignmentsForZone()", t);
		}
	}

	/**
	 * Get boobytraps owned by team.
	 */
	public Record[] getOwnedBoobyTraps(Record aTeamRecord) throws GameException {
		try {
			return relater.getRelated(aTeamRecord, TABLE_BOOBYTRAP, REL_TAG_OWNER);
		} catch (Throwable t) {
			throw new GameException("Error in getPlacedBoobyTrapsForTeam() for team=" + aTeamRecord.getField(FIELD_NAME), t);
		}
	}

	public Record getGameRecord() throws GameException {
		return getByName(gameName, TABLE_GAME);
	}

	public Record getTeamRecord(String aTeamName) throws GameException {
		return getByName(aTeamName, TABLE_TEAM);
	}


	public Record[] getTeamRecords() throws GameException {
		try {
			Record gameRecord = getGameRecord();
			if(gameRecord == null) return null;
			return relater.getRelated(gameRecord, TABLE_TEAM, null);
		} catch (Throwable t) {
			throw new GameException("Error in getTeamRecords()", t);
		}
	}

	public Record getPlayerForName(String aName) throws GameException {
		return getByName(aName, TABLE_PLAYER);
	}

	public Record[] getPlayerRecords() throws GameException {
		try {
			Record[] teamRecords = getTeamRecords();
			List result = new ArrayList(4);
			for (int i = 0; i < teamRecords.length; i++) {
				Record[] playerRecords = getPlayerRecords(teamRecords[i]);
				for (int j = 0; j < playerRecords.length; j++) {
					result.add(playerRecords[j]);
				}
			}
			return (Record[]) result.toArray(new Record[0]);
		} catch (Throwable t) {
			throw new GameException("Error in getPlayerRecords()", t);
		}
	}

	public Record[] getPlayerRecords(Record aTeamRecord) throws GameException {
		try {
			return relater.getRelated(aTeamRecord, TABLE_PLAYER, null);
		} catch (Throwable t) {
			throw new GameException("Error in getPlayerRecords()", t);
		}
	}

	public Record getLocationForTeam(String aTeamName) throws GameException {
		return getRelatedRecord(getTeamRecord(aTeamName), TABLE_LOCATION, null);
	}

	public Record getPersonageForTeam(Record aTeamRecord) throws GameException {
		return getRelatedRecord(aTeamRecord, TABLE_PERSONAGE, null);
	}

	public Record getTeamForPlayer(Record aPlayerRecord) throws GameException {
		return getRelatedRecord(aPlayerRecord, TABLE_TEAM, null);
	}

	/**
	 * Get all zone records.
	 */
	public Record[] getZoneRecords() throws GameException {
		try {
			return finder.readAll(TABLE_ZONE);
		} catch (Throwable t) {
			throw new GameException("Error in getZoneRecords()", t);
		}
	}

	/**
	 * Get all boobytrap records related to zone.
	 */
	public Record[] getBoobytrapRecords(String aZoneName) throws GameException {
		try {
			Record zoneRecord = getZoneByName(aZoneName);
			return relater.getRelated(zoneRecord, TABLE_BOOBYTRAP, null);
		} catch (Throwable t) {
			throw new GameException("Error in getBoobytrapRecords(zoneName) for zone=" + aZoneName, t);
		}
	}

	/**
	 * Get all cloak records.
	 */
	public Record[] getCloakRecords() throws GameException {
		try {
			return finder.readAll(TABLE_CLOAK);
		} catch (Throwable t) {
			throw new GameException("Error in getCloakRecords()", t);
		}
	}

	/**
	 * Get  cloak for team.
	 */
	public Record getCloakForTeam(String aTeamName) throws GameException {
		try {
			Record teamRecord = getTeamRecord(aTeamName);
			return getRelatedRecord(teamRecord, TABLE_CLOAK, null);
		} catch (Throwable t) {
			throw new GameException("Error in getCloakRecords()", t);
		}
	}

	/**
	 * Get all boobytrap records.
	 */
	public Record[] getBoobytrapRecords() throws GameException {
		try {
			return finder.readAll(TABLE_BOOBYTRAP);
		} catch (Throwable t) {
			throw new GameException("Error in getBoobytrapRecords()", t);
		}
	}

	public Record getZoneByName(String aZoneName) throws GameException {
		return getByName(aZoneName, TABLE_ZONE);
	}


	public Record getById(int anId) throws GameException {
		try {
			return finder.read(anId);
		} catch (Throwable t) {
			throw new GameException("Error in getRecord() forid=" + anId, t);
		}
	}


	public Record getById(int anId, String aTable) throws GameException {
		try {
			return finder.read(anId, aTable);
		} catch (Throwable t) {
			throw new GameException("Error in getRecord() forid=" + anId, t);
		}
	}

	public Record getByName(String aName, String aTable) throws GameException {
		try {
			Record[] records = finder.queryTable(aTable, "WHERE name='" + aName + "'");

			if(records == null) return null;

			if (records.length > 1) {
				throw new GameException("More than one record found for name=" + aName + " in table " + aTable);
			} else if (records.length < 1) {
				// throw new GameException("No records found for name=" + aName + " in table " + aTable);
				return null;
			}
			return records[0];

		} catch (Throwable t) {
			throw new GameException("Error in getByName()", t);
		}
	}

	public Record getRelatedRecord(Record aRecord, String aTable, String aTag) throws GameException {
		try {
			Record[] records = relater.getRelated(aRecord, aTable, aTag);
			if (records.length == 1) {
				return records[0];
			} else if (records.length == 0) {
				return null;
			} else {
				throw new GameException("More than one record related to name=" + aRecord.getId() + " of table " + aTable);
			}
		} catch (Throwable t) {
			throw new GameException("Error in getRelatedRecord()", t);
		}
	}

	public Record[] getRelatedRecords(Record aRecord, String aTable, String aTag) throws GameException {
		try {
			return relater.getRelated(aRecord, aTable, aTag);
		} catch (Throwable t) {
			throw new GameException("Error in getRelatedRecords()", t);
		}
	}


	public String getRelationTag(Record aRecord, Record anotherRecord) throws GameException {
		try {
			return relater.getTag(aRecord, anotherRecord);
		} catch (Throwable t) {
			throw new GameException("Error in getRelationTag()", t);
		}
	}


	public void init() throws GameException {
		try {
			String portalName = Amuse.server.getPortal().getId();
			oaseSession = Oase.createSession(portalName);
			finder = oaseSession.getFinder();
			modifier = oaseSession.getModifier();
			relater = oaseSession.getRelater();
			mediaFiler = oaseSession.getMediaFiler();
		} catch (Throwable t) {
			throw new GameException("Error in init()", t);
		}
	}

	public Record insertAnswer(String aTeamName, int anAssignmentId) throws GameException {
		return insertAnswer(aTeamName, getById(anAssignmentId));
	}

	public Record insertAnswer(String aTeamName, Record anAssignmentRecord) throws GameException {
		try {
			Record teamRecord = getTeamRecord(aTeamName);
			String assignmentName = anAssignmentRecord.getStringField(FIELD_NAME);

			// Create record and populate with initial values
			Record answerRecord = modifier.create(TABLE_ANSWER);
			answerRecord.setStringField(FIELD_NAME, assignmentName);
			answerRecord.setStringField(FIELD_TEAM, aTeamName);
			answerRecord.setTextField(FIELD_DESCRIPTION, anAssignmentRecord.getTextField(FIELD_DESCRIPTION));
			answerRecord.setStringField(FIELD_TYPE, anAssignmentRecord.getStringField(FIELD_TYPE));
			answerRecord.setXMLField(FIELD_CONTENT, new JXElement(FIELD_CONTENT));

			// Insert in DB
			modifier.insert(answerRecord);

			// Relate with assignment name to team
			relater.relate(teamRecord, answerRecord, assignmentName);

			// Relate with team name to assignment
			relater.relate(anAssignmentRecord, answerRecord, aTeamName);
			return answerRecord;
		} catch (Throwable t) {
			throw new GameException("Error in insertAnswer() for team=" + aTeamName + " id=" + anAssignmentRecord.getId(), t);
		}
	}

	public Record insertAssignment(Record aZoneRecord, String aRelTag, String aName, String aType, JXElement anInfo, JXElement aLocation, JXElement aContent, String anAnswer) throws GameException {
		try {

			// Create record and populate with initial values
			Record record = modifier.create(TABLE_ASSIGNMENT);
			record.setStringField(FIELD_NAME, aName);
			record.setStringField(FIELD_TYPE, aType);
			record.setTextField(FIELD_DESCRIPTION, "opdracht " + aName);
			record.setXMLField(FIELD_INFO, anInfo);
			record.setXMLField(FIELD_LOCATION, aLocation);
			record.setXMLField(FIELD_CONTENT, aContent);
			record.setStringField(FIELD_ANSWER, anAnswer);

			// Insert in DB
			modifier.insert(record);

			// Relate with assignment name to team
			relater.relate(aZoneRecord, record, aRelTag);
			return record;
		} catch (Throwable t) {
			throw new GameException("Error in insertAssignment() for aName=" + aName, t);
		}
	}

	/**
	 * Insert boobytrap at location.
	 */
	public Record insertBoobytrap(String aTeamName, JXElement aLocation, String aZoneName) throws GameException {
		try {
			Record teamRecord = getTeamRecord(aTeamName);
			// exception if team record not found

			Record zoneRecord = getZoneByName(aZoneName);
			// exception if zone record not found

			// Create record and populate with initial values
			Record boobytrapRecord = modifier.create(TABLE_BOOBYTRAP);
			boobytrapRecord.setStringField(FIELD_OWNER, aTeamName);
			boobytrapRecord.setXMLField(FIELD_LOCATION, aLocation);

			// Insert in DB
			modifier.insert(boobytrapRecord);

			// Relate with owning team
			relate(teamRecord, boobytrapRecord, REL_TAG_OWNER);

			// Relate boobytrap to zone with team name as tag
			relate(boobytrapRecord, zoneRecord, aTeamName);

			return boobytrapRecord;
		} catch (Throwable t) {
			throw new GameException("Error in insertBoobytrap() for team=" + aTeamName, t);
		}
	}

	/**
	 * Insert cloak.
	 */
	public Record insertCloak(String aTeamName, int aTTL) throws GameException {
		try {
			Record teamRecord = getTeamRecord(aTeamName);
			// exception if team record not found
			// Create record and populate with initial values
			Record cloakRecord = modifier.create(TABLE_CLOAK);
			cloakRecord.setStringField(FIELD_OWNER, aTeamName);
			cloakRecord.setIntField(FIELD_TIMETOLIVE, aTTL);
			cloakRecord.setStringField(FIELD_STATE, "active");

			// Insert in DB
			modifier.insert(cloakRecord);

			// Relate with owning team
			relate(teamRecord, cloakRecord, REL_TAG_OWNER);

			return cloakRecord;
		} catch (Throwable t) {
			throw new GameException("Error in insertBoobytrap() for team=" + aTeamName, t);
		}
	}

	/**
	 * Insert confrontation.
	 */
	public Record insertConfrontation(String anInitiator, String aTarget, String aWinner, JXElement aLocation) throws GameException {
		try {
			Record initiatorRecord = getTeamRecord(anInitiator);
			Record targetRecord = getTeamRecord(aTarget);

			// exception if team records not found

			// Create record and populate with initial values
			Record confrontationRecord = modifier.create(TABLE_CONFRONTATION);
			confrontationRecord.setTimestampField(FIELD_DATE, new Timestamp(Sys.now()));
			confrontationRecord.setStringField(FIELD_OWNER, anInitiator);
			confrontationRecord.setStringField(FIELD_INITIATOR, anInitiator);
			confrontationRecord.setStringField(FIELD_TARGET, aTarget);
			confrontationRecord.setStringField(FIELD_WINNER, aWinner);
			confrontationRecord.setXMLField(FIELD_LOCATION, aLocation);

			// Insert in DB
			modifier.insert(confrontationRecord);

			// Relate with teams tagged with winner
			relate(initiatorRecord, confrontationRecord, aWinner);
			relate(targetRecord, confrontationRecord, aWinner);

			return confrontationRecord;
		} catch (Throwable t) {
			throw new GameException("Error in insertConfrontation() for initiator=" + anInitiator, t);
		}
	}


	public Record insertMedium(String anURL, String aType, HashMap theFields) throws GameException {
		try {
			return mediaFiler.grab(anURL, theFields)[0];
		} catch (Throwable t) {
			throw new GameException("Error in insertMedium() for url=" + anURL, t);
		}
	}


	public Record insertMedium(byte[] theData, String theEncoding, String aType, HashMap theFields) throws GameException {
		try {
			return mediaFiler.insert(theData, theEncoding, theFields);
		} catch (Throwable t) {
			throw new GameException("Error in insertMedium() for raw data encoding=" + theEncoding, t);
		}
	}

	public boolean isRelated(Record aRecord, Record anotherRecord) throws GameException {
		try {
			return relater.isRelated(aRecord, anotherRecord);
		} catch (Throwable t) {
			throw new GameException("Error in isRelated", t);
		}
	}

	public void relate(Record aRecord, Record anotherRecord, String aTag) throws GameException {
		try {
			relater.relate(aRecord, anotherRecord, aTag);
		} catch (Throwable t) {
			throw new GameException("Error in relate()", t);
		}
	}


	public void relate(Record aRecord, int aRecordId, String aTag) throws GameException {
		try {
			relater.relate(aRecord, getById(aRecordId), aTag);
		} catch (Throwable t) {
			throw new GameException("Error in relate()", t);
		}
	}

	public void setRelationTag(Record aRecord, Record anotherRecord, String aTag) throws GameException {
		try {
			relater.setTag(aRecord, anotherRecord, aTag);
		} catch (Throwable t) {
			throw new GameException("Error in getRelationTag()", t);
		}
	}

	public void update(Record aRecord) throws GameException {
		try {
			modifier.update(aRecord);
		} catch (Throwable t) {
			throw new GameException("Error in update()", t);
		}
	}

	public void updateAssignment(Record anAssignmentRecord, String aName, String aType, JXElement anInfo, JXElement aLocation, JXElement aContent, String anAnswer) throws GameException {
		try {

			anAssignmentRecord.setStringField(FIELD_NAME, aName);
			anAssignmentRecord.setStringField(FIELD_TYPE, aType);
			anAssignmentRecord.setTextField(FIELD_DESCRIPTION, "opdracht " + aName);
			anAssignmentRecord.setXMLField(FIELD_INFO, anInfo);
			anAssignmentRecord.setXMLField(FIELD_LOCATION, aLocation);
			anAssignmentRecord.setXMLField(FIELD_CONTENT, aContent);
			anAssignmentRecord.setStringField(FIELD_ANSWER, anAnswer);

			// Update in DB
			update(anAssignmentRecord);
		} catch (Throwable t) {
			throw new GameException("Error in updateAssignment() for aName=" + aName, t);
		}
	}

	public void unrelate(Record aRecord, Record anotherRecord) throws GameException {
		try {
			relater.unrelate(aRecord, anotherRecord);
		} catch (Throwable t) {
			throw new GameException("Error in unrelate(Record aRecord, Record anotherRecord)", t);
		}
	}

	public void unrelate(Record aRecord, String aTableName, String aRelTag) throws GameException {
		try {
			Record[] relatedRecords = relater.getRelated(aRecord, aTableName, aRelTag);
			for (int i = 0; i < relatedRecords.length; i++) {
				relater.unrelate(aRecord, relatedRecords[i]);
			}
		} catch (Throwable t) {
			throw new GameException("Error in unrelate(Record aRecord, String aTableName, aRelTag)", t);
		}
	}

	public void unrelate(Record aRecord, String aTableName) throws GameException {
		unrelate(aRecord, aTableName, null);
	}

}
