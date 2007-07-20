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
			relater.relate(person, gameRound);

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
	public void deleteRound(int aRoundId) throws OaseException, UtopiaException {
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();
		Record gameRound = finder.read(aRoundId, GAMEROUND_TABLE);
		Record[] gamePlays = relater.getRelated(gameRound, GAMEPLAY_TABLE, null);

	}

	/**
	 * Add players (persons) to gameround.
	 *
	 * @param aRoundId	  gameround record id
	 * @param somePlayerIds comma-separated list of playe ids
	 * @throws OaseException Standard exception
	 */
	public void addPlayers(int aRoundId, String somePlayerIds) throws OaseException {
		addPlayers(oase.getFinder().read(aRoundId, GAMEROUND_TABLE), somePlayerIds);

	}

	/**
	 * Add players (persons) to gameround.
	 *
	 * @param aGameRound	gameround record
	 * @param somePlayerIds comma-separated list of playe ids
	 * @throws OaseException Standard exception
	 */
	public void addPlayers(Record aGameRound, String somePlayerIds) throws OaseException {
		Finder finder = oase.getFinder();
		Modifier modifier = oase.getModifier();
		Relater relater = oase.getRelater();
		String[] playerIds = somePlayerIds.split(",");

		Transaction transaction = oase.getOaseSession().createTransaction();
		Record gamePlay, player;
		try {
			transaction.begin();
			
			for (int i = 0; i < playerIds.length; i++) {
				player = finder.read(Integer.parseInt(playerIds[i]), PERSON_TABLE);
				gamePlay = modifier.create(GAMEPLAY_TABLE);
				modifier.insert(gamePlay);

				// Relate player to gameround and gameplay
				relater.relate(player, aGameRound);
				relater.relate(player, gamePlay);
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
	 * @param somePlayerIds comma-separated list of playe ids
	 * @throws OaseException Standard exception
	 */
	public void removePlayers(int aRoundId, String somePlayerIds) throws OaseException, UtopiaException {
		removePlayers(oase.getFinder().read(aRoundId, GAMEROUND_TABLE), somePlayerIds);
	}

	/**
	 * Remove players (person) from gameround.
	 *
	 * @param aGameRound	gameround record
	 * @param somePlayerIds comma-separated list of playe ids
	 * @throws OaseException Standard exception
	 */
	public void removePlayers(Record aGameRound, String somePlayerIds) throws OaseException, UtopiaException {
		Finder finder = oase.getFinder();
		Relater relater = oase.getRelater();
		String[] playerIds = somePlayerIds.split(",");
		Record player;
		String tables = "wp_gameround,wp_gameplay,utopia_person";
		String fields = "wp_gameplay.id as gameplayid";
		String where = "utopia_person.id in (" + somePlayerIds + ") AND wp_gameround.id = " + aGameRound;
		String relations = "wp_gameround,wp_gameplay;wp_gameround,utopia_person;wp_gameplay,utopia_person";
		String postCond = null;
		Record[] gamePlays = QueryLogic.queryStore(oase, tables, fields, where, relations, postCond);

		GamePlayLogic gamePlayLogic = new GamePlayLogic(oase);
		for (int i = 0; i < gamePlays.length; i++) {
			gamePlayLogic.delete(gamePlays[i].getIntField("gameplayid"));
		}

		for (int i = 0; i < playerIds.length; i++) {
			player = finder.read(Integer.parseInt(playerIds[i]), PERSON_TABLE);
			// UnRelate player from gameround and gameplay
			relater.unrelate(player, aGameRound);
		}
	}

}
