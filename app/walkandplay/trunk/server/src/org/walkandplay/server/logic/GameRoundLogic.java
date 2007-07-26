package org.walkandplay.server.logic;

import org.geotracing.handler.QueryLogic;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.*;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.walkandplay.server.util.Constants;

/**
 * Manage gamerounds.
 * <p/>
 * A GameRound ties players and gameplay t a specific game.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class GameRoundLogic implements Constants {
	static private Log log = Logging.getLog("GameRoundLogic");
	private Oase oase;

	public GameRoundLogic(Oase anOase) {
		oase = anOase;
	}

	/**
	 * Create a gameround.
	 *
	 * @param aPersonId person id creating the gameround
	 * @return a response.
	 * @throws OaseException Standard Utopia exception
	 */
	public Record createRound(int aPersonId, int aGameId, String aName) throws OaseException {
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();

		// Get game record
		Record game = finder.read(aGameId, GAME_TABLE);
		if (game == null) {
			throw new OaseException("No game found for id=" + aGameId);
		}

		// Get person record
		Record person = finder.read(aPersonId, PERSON_TABLE);
		if (person == null) {
			throw new OaseException("No person found for id=" + aPersonId);
		}

		Transaction transaction = oase.getOaseSession().createTransaction();
		Record gameRound;

		try {
			transaction.begin();
			gameRound = modifier.create(GAMEROUND_TABLE);

			// Set ourselves as owner
			gameRound.setIntField(OWNER_FIELD, aPersonId);

			gameRound.setStringField(NAME_FIELD, aName);
			modifier.insert(gameRound);

			relater.relate(game, gameRound);
			relater.relate(person, gameRound, RELTAG_OWNER);

			transaction.commit();
		} catch (Throwable t) {
			transaction.cancel();
			throw new OaseException("createRound transaction failed", t);
		}

		return gameRound;
	}

	/**
	 * Delete a gameround.
	 *
	 * @param aRoundId gameround record id
	 * @throws UtopiaException Standard Utopia exception
	 */
	public void deleteRound(int aPersonId, int aRoundId) throws OaseException, UtopiaException {
		deleteRound(aPersonId, oase.getFinder().read(aRoundId, GAMEROUND_TABLE));
	}

	/**
	 * Delete a gameround.
	 *
	 * @param aGameRound gameround record
	 * @throws UtopiaException Standard Utopia exception
	 */
	public void deleteRound(int aPersonId, Record aGameRound) throws OaseException, UtopiaException {
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();
		throwIfNotOwner(aPersonId, aGameRound);

		Record[] gamePlays = relater.getRelated(aGameRound, GAMEPLAY_TABLE, null);
		GamePlayLogic gamePlayLogic = new GamePlayLogic(oase);
		for (int i = 0; i < gamePlays.length; i++) {
			gamePlayLogic.delete(gamePlays[i]);
		}
		modifier.delete(aGameRound);
	}

	/**
	 * Add players (persons) to gameround.
	 *
	 * @param aRoundId	  gameround record id
	 * @param somePlayers comma-separated list of player account names
	 * @throws OaseException Standard exception
	 */
	public void addPlayers(int aRoundId, String somePlayers) throws OaseException {
		addPlayers(oase.getFinder().read(aRoundId, GAMEROUND_TABLE), somePlayers);

	}

	/**
	 * Add players (persons) to gameround.
	 *
	 * @param aGameRound	gameround record
	 * @param somePlayers comma-separated list of playe ids
	 * @throws OaseException Standard exception
	 */
	public void addPlayers(Record aGameRound, String somePlayers) throws OaseException {
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();
		Record[] persons = WPQueryLogic.getPersonsForLoginNames(somePlayers);

		Transaction transaction = oase.getOaseSession().createTransaction();
		Record gamePlay;
		try {
			transaction.begin();

			for (int i = 0; i < persons.length; i++) {
				gamePlay = modifier.create(GAMEPLAY_TABLE);
				modifier.insert(gamePlay);

				// Relate player to gameround and gameplay
				relater.relate(aGameRound, gamePlay);
				relater.relate(persons[i], aGameRound, RELTAG_PLAYER);
				relater.relate(persons[i], gamePlay, RELTAG_PLAYER);
			}
			transaction.commit();
		} catch (Throwable t) {
			transaction.cancel();
			throw new OaseException("addplayers transaction failed", t);
		}

	}

	/**
	 * Remove players (persons) from gameround.
	 *
	 * @param aRoundId	  gameround record id
	 * @param somePlayers comma-separated list of playe ids
	 * @throws OaseException Standard exception
	 */
	public void removePlayers(int aRoundId, String somePlayers) throws OaseException, UtopiaException {
		removePlayers(oase.getFinder().read(aRoundId, GAMEROUND_TABLE), somePlayers);
	}

	/**
	 * Remove players (person) from gameround.
	 *
	 * @param aGameRound	gameround record
	 * @param somePlayers comma-separated list of playe ids
	 * @throws OaseException Standard exception
	 */
	public void removePlayers(Record aGameRound, String somePlayers) throws OaseException, UtopiaException {
		Relater relater = oase.getRelater();
		Record[] persons = WPQueryLogic.getPersonsForLoginNames(somePlayers);
		String personIds = "";
		String nextId;
		for (int i=0; i < persons.length; i++) {
			nextId = (persons.length > 0 && i < persons.length-1) ? persons[i].getIdString() + "," : persons[i].getIdString();
			personIds += nextId;
		}
		String tables = "wp_gameround,wp_gameplay,utopia_person";
		String fields = "wp_gameplay.id";
		String where = "utopia_person.id in (" + personIds + ") AND wp_gameround.id = " + aGameRound.getId();
		String relations = "wp_gameround,wp_gameplay;wp_gameround,utopia_person;wp_gameplay,utopia_person";
		String postCond = null;
		Record[] gamePlays = QueryLogic.queryStore(oase, tables, fields, where, relations, postCond);

		GamePlayLogic gamePlayLogic = new GamePlayLogic(oase);
		for (int i = 0; i < gamePlays.length; i++) {
			gamePlayLogic.delete(gamePlays[i].getIntField(ID_FIELD));
		}

		String[] loginNames = somePlayers.split(",");
		for (int i = 0; i < persons.length; i++) {
			// UnRelate player from gameround and gameplay
			relater.unrelate(persons[i], aGameRound);
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
			throw new OaseException("you are not owner of this record");
		}
	}
}
