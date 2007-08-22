package org.walkandplay.server.logic;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.handler.Location;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.IO;
import org.keyworx.oase.api.*;
import org.keyworx.utopia.core.util.Oase;
import org.walkandplay.server.util.Constants;

import java.util.HashMap;

/**
 * Manage game objects.
 * <p/>
 * A Game specifies a series of locations with tasks/media.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class GameCreateLogic implements Constants {
	static private Log log = Logging.getLog("GameCreateLogic");
	private Oase oase;

	public GameCreateLogic(Oase anOase) {
		oase = anOase;
	}

	/**
	 * Add medium and its location to a game.
	 *
	 * @param aPersonId person id creating the gameround
	 * @throws OaseException Standard Utopia exception
	 */
	public Record addGameMedium(int aPersonId, int aGameId, JXElement aMediumElm) throws OaseException {
		// Throws exception if game cannot be updated
		verifyGameUpdate(aPersonId, aGameId);

		Transaction transaction = oase.getOaseSession().createTransaction();
		Record mediumRecord = null;
		try {
			transaction.begin();

			String mediumIdStr = aMediumElm.getChildText(ID_FIELD);
			if (mediumIdStr != null) {
				// Medium must exist
				mediumRecord = oase.getFinder().read(Integer.parseInt(mediumIdStr), MediaFiler.MEDIUM_TABLE);
			} else if (aMediumElm.getChildText(TEXT_FIELD) != null) {
				// Medium text is provided in element: basically a text upload
				mediumRecord = insertTextMedium(aPersonId, aMediumElm);
			} else {
				throw new OaseException("No medium id or medium text found in medium elm");
			}


			if (mediumRecord == null) {
				throw new OaseException("No medium found");
			}

			// Ok we have valid medium
			Location location = Location.create(oase);
			String lon = aMediumElm.getChildText(Location.FIELD_LON);
			String lat = aMediumElm.getChildText(Location.FIELD_LAT);
			long time = System.currentTimeMillis();
			location.setIntValue(Location.FIELD_TYPE, LOC_TYPE_GAME_MEDIUM);

			location.setPoint(Double.parseDouble(lon), Double.parseDouble(lat), 0.0D, time);
			location.saveInsert();

			location.createRelation(mediumRecord.getId(), RELTAG_MEDIUM);
			location.createRelation(aGameId, RELTAG_MEDIUM);


			transaction.commit();
		} catch (Throwable t) {
			transaction.cancel();
			throw new OaseException("addMedium transaction failed", t);
		}
		return mediumRecord;
	}

	/**
	 * Add task and its location and media to a game.
	 *
	 * @param aPersonId person id doing the add
	 * @throws OaseException Standard Utopia exception
	 */
	public Record addGameTask(int aPersonId, int aGameId, JXElement aTaskElm) throws OaseException {
		// Throws exception if game cannot be updated
		verifyGameUpdate(aPersonId, aGameId);

		Transaction transaction = oase.getOaseSession().createTransaction();
		Record taskRecord = null;

		try {
			transaction.begin();
			taskRecord = oase.getModifier().create(TASK_TABLE);
			taskRecord.setField(NAME_FIELD, aTaskElm.getChildText(NAME_FIELD));
			taskRecord.setField(DESCRIPTION_FIELD, aTaskElm.getChildText(DESCRIPTION_FIELD));
			taskRecord.setField(SCORE_FIELD, aTaskElm.getChildText(SCORE_FIELD));
			taskRecord.setField(ANSWER_FIELD, aTaskElm.getChildText(ANSWER_FIELD));

			oase.getModifier().insert(taskRecord);

			// Couple to location
			Location location = Location.create(oase);
			String lon = aTaskElm.getChildText(Location.FIELD_LON);
			String lat = aTaskElm.getChildText(Location.FIELD_LAT);
			long time = System.currentTimeMillis();
			location.setIntValue(Location.FIELD_TYPE, LOC_TYPE_GAME_TASK);

			location.setPoint(Double.parseDouble(lon), Double.parseDouble(lat), 0.0D, time);
			location.saveInsert();

			int mediumId = Integer.parseInt(aTaskElm.getChildText(MEDIUM_ID_FIELD));
			Record mediumRecord = oase.getFinder().read(mediumId, MEDIUM_TABLE);

			// Create relations
			oase.getRelater().relate(taskRecord, mediumRecord, RELTAG_MEDIUM);
			location.createRelation(taskRecord.getId(), RELTAG_TASK);
			location.createRelation(aGameId, RELTAG_TASK);
			transaction.commit();
		} catch (Throwable t) {
			transaction.cancel();
			throw new OaseException("addGameTask transaction failed", t);
		}
		return taskRecord;
	}

	/**
	 * Create a game.
	 *
	 * @param aPersonId person id creating the gameround
	 * @return a response.
	 * @throws OaseException Standard Utopia exception
	 */
	public Record createGame(int aPersonId, JXElement aGameElm) throws OaseException {
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();
		Transaction transaction = oase.getOaseSession().createTransaction();
		Record game;
		try {
			transaction.begin();
			game = modifier.create(GAME_TABLE);
			game.setIntField(OWNER_FIELD, aPersonId);
			game.setStringField(NAME_FIELD, aGameElm.getChildText(NAME_FIELD));
			String desc = aGameElm.getChildText(DESCRIPTION_FIELD);
			if (desc == null) {
				desc = "description for game " + aGameElm.getChildText(NAME_FIELD);
			}

			game.setStringField(DESCRIPTION_FIELD, desc);

			String intro = aGameElm.getChildText(INTRO_FIELD);
			if (intro != null) {
				game.setStringField(INTRO_FIELD, intro);
			}
			String outro = aGameElm.getChildText(OUTRO_FIELD);

			if (outro != null) {
				game.setStringField(OUTRO_FIELD, outro);
			}
			modifier.insert(game);

			// Set owner to person creating the game
			relater.relate(game, finder.read(aPersonId, PERSON_TABLE), RELTAG_CREATOR);

			transaction.commit();
		} catch (Throwable t) {
			transaction.cancel();
			throw new OaseException("createGame transaction failed", t);
		}
		return game;
	}

	/**
	 * Delete a game.
	 *
	 * @param aGameId game id to delete
	 * @throws OaseException Standard Utopia exception
	 */
	public void deleteGame(int aPersonId, int aGameId) throws OaseException {
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();
		Transaction transaction = oase.getOaseSession().createTransaction();
		try {
			transaction.begin();
			Record gameRecord = finder.read(aGameId, GAME_TABLE);
			throwIfNotOwner(aPersonId, gameRecord);

			// Delete locations related to game with their tasks/media
			Record[] locations = relater.getRelated(gameRecord, LOCATION_TABLE, null);
			for (int i = 0; i < locations.length; i++) {
				deleteGameLocation(locations[i]);
			}

			// Delete all gamerounds (and gameplays!)
			Record[] gameRounds = relater.getRelated(gameRecord, GAMEROUND_TABLE, null);
			GameRoundLogic gameRoundLogic = new GameRoundLogic(oase);
			for (int i = 0; i < gameRounds.length; i++) {
				gameRoundLogic.deleteRound(aPersonId, gameRounds[i]);
			}

			// Finally delete game itself
			modifier.delete(gameRecord);

			transaction.commit();
		} catch (Throwable t) {
			transaction.cancel();
			throw new OaseException("deleteGame transaction failed for id=" + aGameId, t);
		}
	}


	/**
	 * Delete a game medium and its location.
	 *
	 * @param aMediumId medium id related to game
	 * @throws OaseException Standard Utopia exception
	 */
	public void deleteGameMedium(int aPersonId, int aMediumId) throws OaseException {
		// Throws exception if game cannot be updated
		verifyGameUpdate(aPersonId, WPQueryLogic.getGameForGameMedium(aMediumId));

		Record medium = oase.getFinder().read(aMediumId, MEDIUM_TABLE);
		if (medium == null) {
			throw new OaseException("No medium found for id=" + aMediumId);
		}
		deleteGameMedium(oase.getFinder().read(aMediumId, MEDIUM_TABLE));
	}



	/**
	 * Delete a game task and its location.
	 *
	 * @param aTaskId task id related to game
	 * @throws OaseException Standard Utopia exception
	 */
	public void deleteGameTask(int aPersonId, int aTaskId) throws OaseException {
		// Throws exception if game cannot be updated
		verifyGameUpdate(aPersonId, WPQueryLogic.getGameForGameTask(aTaskId));

		Record task = oase.getFinder().read(aTaskId, TASK_TABLE);
		if (task == null) {
			throw new OaseException("No task found for id=" + aTaskId);
		}
		deleteGameTask(task);
	}

	/**
	 * Update a game.
	 *
	 * @param aGameId game id for update
	 * @throws OaseException Standard Utopia exception
	 */
	public void updateGame(int aPersonId, int aGameId, JXElement aGameElm) throws OaseException {
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Record game = finder.read(aGameId, GAME_TABLE);

		// Throws exception if game cannot be updated
		verifyGameUpdate(aPersonId, game);

		String name = aGameElm.getChildText(NAME_FIELD);
		if (name != null) {
			game.setStringField(NAME_FIELD, name);
		}
		String desc = aGameElm.getChildText(DESCRIPTION_FIELD);

		if (desc != null) {
			game.setStringField(DESCRIPTION_FIELD, desc);
		}
		String intro = aGameElm.getChildText(INTRO_FIELD);
		if (intro != null) {
			game.setStringField(INTRO_FIELD, intro);
		}
		String outro = aGameElm.getChildText(OUTRO_FIELD);

		if (outro != null) {
			game.setStringField(OUTRO_FIELD, outro);
		}
		modifier.update(game);
	}



	/**
	 * Throw exception if game cannot be updated.
	 *
	 * @param aPersonId person id to check
	 * @param aGameId record id of game to be checked
	 * @throws OaseException thrown if verify failed
	 */
	public void verifyGameUpdate(int aPersonId, int aGameId) throws OaseException {
		verifyGameUpdate(aPersonId, oase.getFinder().read(aGameId, GAME_TABLE));
	}

	/**
	 * Throw exception if game cannot be updated.
	 *
	 * @param aPersonId person id to check
	 * @param aGameRecord record of game to be checked
	 * @throws OaseException thrown if verify failed
	 */
	public void verifyGameUpdate(int aPersonId, Record aGameRecord) throws OaseException {
		throwIfNotOwner(aPersonId, aGameRecord);
		throwIfNotDraft(aGameRecord);
	}

	/***************************** INTERNAL METHODS *******************************************/

	/**
	 * Delete a game location.
	 *
	 * @param aLocationRec location  related to game
	 * @throws OaseException Standard Utopia exception
	 */
	protected void deleteGameLocation(Record aLocationRec) throws OaseException {
		Relater relater = oase.getRelater();
		switch (aLocationRec.getIntField(TYPE_FIELD)) {

			case LOC_TYPE_GAME_TASK:
				Record[] tasks = relater.getRelated(aLocationRec, TASK_TABLE, null);
				if (tasks.length != 1) {
					log.warn("No task found for location id=" + aLocationRec.getId() + " (ignoring)");
					break;
				}
				deleteGameTask(tasks[0]);
				break;

			case LOC_TYPE_GAME_MEDIUM:
				Record[] media = relater.getRelated(aLocationRec, MEDIUM_TABLE, null);
				if (media.length != 1) {
					log.warn("No medium found for location id=" + aLocationRec.getId() + " (ignoring)");
					break;
				}
				deleteGameMedium(media[0]);
				break;
			default:
				log.warn("unknown location type for id=" + aLocationRec.getId() + " (ignoring)");
				break;
		}

	}

	/**
	 * Delete a game medium and its location.
	 *
	 * @param aMediumRec medium related to game
	 * @throws OaseException Standard Utopia exception
	 */
	protected void deleteGameMedium(Record aMediumRec) throws OaseException {
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();
		Transaction transaction = oase.getOaseSession().createTransaction();
		try {
			transaction.begin();
			Record locationRecord = relater.getRelated(aMediumRec, Location.TABLE_NAME, null)[0];
			modifier.delete(locationRecord);
			modifier.delete(aMediumRec);

			transaction.commit();
		} catch (Throwable t) {
			transaction.cancel();
			throw new OaseException("deleteGameMedium transaction failed for id=" + aMediumRec.getId(), t);
		}
	}


	/**
	 * Delete a game task and related location/medium.
	 *
	 * @param aTaskRec task related to game
	 * @throws OaseException Standard Utopia exception
	 */
	protected void deleteGameTask(Record aTaskRec) throws OaseException {
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();
		Transaction transaction = oase.getOaseSession().createTransaction();
		try {
			transaction.begin();
			Record locationRecord = relater.getRelated(aTaskRec, Location.TABLE_NAME, null)[0];
			Record mediumRecord = relater.getRelated(aTaskRec, MEDIUM_TABLE, null)[0];

			modifier.delete(aTaskRec);
			modifier.delete(locationRecord);
			modifier.delete(mediumRecord);

			transaction.commit();
		} catch (Throwable t) {
			transaction.cancel();
			throw new OaseException("deleteGameTask transaction failed for id=" + aTaskRec.getId(), t);
		}
	}


	/**
	 * Internal util to insert a text medium.
	 *
	 * @param aPersonId person id creating the gameround
	 * @throws OaseException Standard Utopia exception
	 */
	protected Record insertTextMedium(int aPersonId, JXElement aMediumElm) throws OaseException {
		Finder finder = oase.getFinder();
		Relater relater = oase.getRelater();
		Record medium;
		try {
			// This is basically a text upload
			// Create a new medium from name and text in element
			String name = aMediumElm.getChildText(NAME_FIELD);
			String text = aMediumElm.getChildText(TEXT_FIELD);
			if (name == null || text == null) {
				throw new OaseException("No name and or text found for text medium");
			}

			// Fill in fields
			HashMap fields = new HashMap(4);

			// Setup standard medium fields
			fields.put(MediaFiler.FIELD_NAME, name);
			// fields.put(MediaFiler.FIELD_DESCRIPTION, "null");
			fields.put(MediaFiler.FIELD_MIME, "text/plain");
			fields.put(MediaFiler.FIELD_KIND, MediaFiler.KIND_TEXT);

			// Create medium record and relate to person
			try {
				MediaFiler mediaFiler = oase.getMediaFiler();
				text = IO.forHTMLTag(text);
				medium = mediaFiler.insert(text.getBytes(), fields);
				Record personRecord = finder.read(aPersonId, PERSON_TABLE);
				relater.relate(medium, personRecord);
			} catch (Throwable t) {
				throw new OaseException("Error in insertTextMedium for text " + text, t);
			}

			return medium;

		} catch (Throwable t) {
			throw new OaseException("insertTextMedium failed", t);
		}
	}

	/**
	 * Throw exception if person id is not the owner
	 *
	 * @param aPersonId person id to check
	 * @param aRecordId record id to be checked
	 * @throws OaseException Standard Utopia exception
	 */
	protected void throwIfNotOwner(int aPersonId, int aRecordId) throws OaseException {
		throwIfNotOwner(aPersonId, oase.getFinder().read(aRecordId));
	}

	/**
	 * Throw exception if person id is not the owner
	 *
	 * @param aPersonId person id to check
	 * @param aRecord record to be checked
	 * @throws OaseException Standard Utopia exception
	 */
	protected void throwIfNotOwner(int aPersonId, Record aRecord) throws OaseException {
		if (aRecord.getIntField(OWNER_FIELD) != aPersonId) {
			throw new OaseException("you are not owner of this game");
		}
	}

	/**
	 * Throw exception if game not in draft state.
	 *
	 * @param aGameId game id to check
	 * @throws OaseException Standard Utopia exception
	 */
	protected void throwIfNotDraft(int aGameId) throws OaseException {
		throwIfNotDraft(oase.getFinder().read(aGameId, GAME_TABLE));
	}

	/**
	 * Throw exception if game not in draft state.
	 *
	 * @param aRecord record to be checked
	 * @throws OaseException Standard Utopia exception
	 */
	protected void throwIfNotDraft(Record aRecord) throws OaseException {
		if (aRecord.getIntField(STATE_FIELD) != GAME_CREATE_STATE_DRAFT) {
			throw new OaseException("game is not in draft state");
		}
	}
}
