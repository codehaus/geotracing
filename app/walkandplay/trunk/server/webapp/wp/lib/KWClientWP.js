/*
* KWClient extension functions for WalkAndPlay.
*
* EXTERNALS
* KWClient.js
* KWClientExt.js
*
* PURPOSE
* Provide direct usable functions for WalkAndPlay requests in AJAX.
*
*/

KW.WP = {

/**
 * Add medium to Game.
 * @param callback - user callback function or null
 * @param gameId - id of item to be commented
 * @param mediumId - id of medium
 * @param lon - x-coordinate (longitude)
 * @param lat - y-coordinate (latitude)
 */
	gameAddMedium: function(callback, gameId, mediumId, lon, lat) {
		var req = KW.createRequest('game-add-medium-req');

		KW.UTIL.setAttr(req, 'id', gameId);

		// Add medium element
		var medium = req.createElement('medium');
		KW.UTIL.addTextElement(medium, 'id', mediumId);
		KW.UTIL.addTextElement(medium, 'lon', lon);
		KW.UTIL.addTextElement(medium, 'lat', lat);

		req.documentElement.appendChild(medium);
		KW.utopia(req, callback);
	},

/**
 * Add text medium to Game.
 * @param callback - user callback function or null
 * @param gameId - id of item to be commented
 * @param aText - medium
 * @param lon - x-coordinate (longitude)
 * @param lat - y-coordinate (latitude)
 */
	gameAddTextMedium: function(callback, gameId, aName, aText, lon, lat) {
		var req = KW.createRequest('game-add-medium-req');

		KW.UTIL.setAttr(req, 'id', gameId);

		// Add medium element
		var medium = req.createElement('medium');
		KW.UTIL.addTextElement(medium, 'name', aName);
		KW.UTIL.addTextElement(medium, 'text', aText);
		KW.UTIL.addTextElement(medium, 'lon', lon);
		KW.UTIL.addTextElement(medium, 'lat', lat);

		req.documentElement.appendChild(medium);
		KW.utopia(req, callback);
	},

/**
 * Add task to Game.
 * @param callback - user callback function or null
 * @param gameId - id of item to be commented
 * @param name - name
 * @param desc - description
 * @param score - score
 * @param answer - answer 
 * @param mediumId - id of medium
 * @param lon - x-coordinate (longitude)
 * @param lat - y-coordinate (latitude)
 */
	gameAddTask: function(callback, gameId, name, desc, score, answer, mediumId, lon, lat) {
		var req = KW.createRequest('game-add-task-req');

		KW.UTIL.setAttr(req, 'id', gameId);

		// Add medium element
		var task = req.createElement('task');
		KW.UTIL.addTextElement(task, 'name', name);
		KW.UTIL.addTextElement(task, 'description', desc);
		KW.UTIL.addTextElement(task, 'score', score);
		KW.UTIL.addTextElement(task, 'answer', answer);
		KW.UTIL.addTextElement(task, 'mediumid', mediumId);
		KW.UTIL.addTextElement(task, 'lon', lon);
		KW.UTIL.addTextElement(task, 'lat', lat);

		req.documentElement.appendChild(task);
		KW.utopia(req, callback);
	},

/**
 * Create a new game.
 * @param callback - user callback function or null
 * @param name - name of game
 * @param desc - description of game
 * @param intro - intro text of game
 * @param outro - outro text of game
 */
	gameCreate: function(callback, name, desc, intro, outro) {
		var req = KW.createRequest('game-create-req');

		// Add game element
		var game = req.createElement('game');
		KW.UTIL.addTextElement(game, 'name', name);
		KW.UTIL.addOptTextElement(game, 'description', desc);
		KW.UTIL.addOptTextElement(game, 'intro', intro);
		KW.UTIL.addOptTextElement(game, 'outro', outro);

		req.documentElement.appendChild(game);

		KW.utopia(req, callback);
	},


/**
 * Delete a Game (only game owner is allowed to do this).
 * @param callback - user callback function or null
 * @param gameId - id of game
 */
	gameDelete: function(callback, gameId) {
		var req = KW.createRequest('game-delete-req');

		// Only game id is required
		KW.UTIL.setAttr(req, 'id', gameId);

		KW.utopia(req, callback);
	},

/**
 * Delete medium from Game.
 * @param callback - user callback function or null
 * @param mediumId - id of medium
 */
	gameDelMedium: function(callback, mediumId) {
		var req = KW.createRequest('game-delete-medium-req');

		// Only medium id is required (game is coupled to medium)
		KW.UTIL.setAttr(req, 'id', mediumId);

		KW.utopia(req, callback);
	},

/**
 * Update general Game data.
 * @param callback - user callback function or null
 * @param gameId - id of game
 * @param name - name (optional)
 * @param desc - description (optionaL
 * @param intro - intro text (optional)
 * @param outro - outro text (optional)
 */
	gameUpdate: function(callback, gameId, name, desc, intro, outro) {
		var req = KW.createRequest('game-update-req');

		KW.UTIL.setAttr(req, 'id', gameId);

		// Add medium element
		var game = req.createElement('game');
		KW.UTIL.addOptTextElement(game, 'name', name);
		KW.UTIL.addOptTextElement(game, 'description', desc);
		KW.UTIL.addOptTextElement(game, 'intro', intro);
		KW.UTIL.addOptTextElement(game, 'outro', outro);

		req.documentElement.appendChild(game);
		KW.utopia(req, callback);
	},

/**
 * Update medium in Game.
 * @param callback - user callback function or null
 * @param mediumId - id of medium to be updated
 * @param newMediumId - id of medium to replace existing medium (optional)
 * @param name - name (optional)
 * @param desc - description (optional)
 * @param lon - x-coordinate (longitude) (optional)
 * @param lat - y-coordinate (latitude)  (optional)
 */
	gameUpdateMedium: function(callback, mediumId, newMediumId, name, desc, lon, lat) {
		var req = KW.createRequest('game-update-medium-req');

		KW.UTIL.setAttr(req, 'id', mediumId);

		// Create medium element with supplied fields
		var medium = req.createElement('medium');
		KW.UTIL.addOptTextElement(medium, 'mediumid', newMediumId);
		KW.UTIL.addOptTextElement(medium, 'name', name);
		KW.UTIL.addOptTextElement(medium, 'description', desc);
		KW.UTIL.addOptTextElement(medium, 'lon', lon);
		KW.UTIL.addOptTextElement(medium, 'lat', lat);

		req.documentElement.appendChild(medium);
		KW.utopia(req, callback);
	},

		/**
 * Update medium in Game.
 * @param callback - user callback function or null
 * @param mediumId - id of medium to be updated
 * @param newText - new text to replace existing medium text (optional)
 * @param name - name (optional)
 * @param desc - description (optional)
 * @param lon - x-coordinate (longitude) (optional)
 * @param lat - y-coordinate (latitude)  (optional)
 */
	gameUpdateTextMedium: function(callback, mediumId, newText, name, desc, lon, lat) {
		var req = KW.createRequest('game-update-medium-req');

		KW.UTIL.setAttr(req, 'id', mediumId);

		// Create medium element with supplied fields
		var medium = req.createElement('medium');
		KW.UTIL.addOptTextElement(medium, 'text', newText);
		KW.UTIL.addOptTextElement(medium, 'name', name);
		KW.UTIL.addOptTextElement(medium, 'description', desc);
		KW.UTIL.addOptTextElement(medium, 'lon', lon);
		KW.UTIL.addOptTextElement(medium, 'lat', lat);

		req.documentElement.appendChild(medium);
		KW.utopia(req, callback);
	},
		
/**
 * Update task in Game.
 * @param callback - user callback function or null
 * @param taskId - id of task to be updated
 * @param name - name (optional)
 * @param desc - description (optional)
 * @param score - score (optional)
 * @param answer - answer (optional)
 * @param mediumId - id of medium  (optional)
 * @param lon - x-coordinate (longitude) (optional)
 * @param lat - y-coordinate (latitude) (optional)
 */
	gameUpdateTask: function(callback, taskId, name, desc, score, answer, mediumId, lon, lat) {
		var req = KW.createRequest('game-update-task-req');

		KW.UTIL.setAttr(req, 'id', taskId);

		// Add medium element
		var task = req.createElement('task');
		KW.UTIL.addOptTextElement(task, 'name', name);
		KW.UTIL.addOptTextElement(task, 'description', desc);
		KW.UTIL.addOptTextElement(task, 'score', score);
		KW.UTIL.addOptTextElement(task, 'answer', answer);
		KW.UTIL.addOptTextElement(task, 'mediumid', mediumId);
		KW.UTIL.addOptTextElement(task, 'lon', lon);
		KW.UTIL.addOptTextElement(task, 'lat', lat);

		req.documentElement.appendChild(task);
		KW.utopia(req, callback);
	},
/**
 * Delete task from Game.
 * @param callback - user callback function or null
 * @param taskId - id of task
 */
	gameDelTask: function(callback, taskId) {
		var req = KW.createRequest('game-delete-task-req');

		// Only medium id is required (game is coupled to medium)
		KW.UTIL.setAttr(req, 'id', taskId);

		KW.utopia(req, callback);
	},

/**
 * Get gameplay results for team (user).
 * @param callback - user callback function or null
 * @param gamePlayId - id of gameplay (game instance for team)
 */
	playGetGamePlay: function(callback, gamePlayId) {
		var req = KW.createRequest('play-get-gameplay-req');

		// Only medium id is required (game is coupled to medium)
		KW.UTIL.setAttr(req, 'id', gamePlayId);

		KW.utopia(req, callback);
	},

	/**
	 * Send heartbeat.
	 * @param callback - user callback function or null
	 */
	playHeartbeat: function(callback) {
		var req = KW.createRequest('play-hb-req');
		KW.utopia(req, callback);
	},

/**
 * Add players to Gameround (only gameround owner is allowed to do this).
 * @param callback - user callback function or null
 * @param roundId* - id of gameround
 * @param players* - comma-separated player loginnames e.g. "bob,carol,ted"
 */
	roundAddPlayers: function(callback, roundId, players) {
		var req = KW.createRequest('round-add-players-req');

		KW.UTIL.setAttr(req, 'roundid', roundId);
		KW.UTIL.setAttr(req, 'players', players);

		KW.utopia(req, callback);
	},

/**
 * Remove players from Gameround (only gameround owner is allowed to do this).
 * @param callback - user callback function or null
 * @param roundId* - id of gameround
 * @param players* - comma-separated player loginnames e.g. "bob,carol,ted"
 */
	roundRemovePlayers: function(callback, roundId, players) {
		var req = KW.createRequest('round-remove-players-req');

		KW.UTIL.setAttr(req, 'roundid', roundId);
		KW.UTIL.setAttr(req, 'players', players);

		KW.utopia(req, callback);
	},

/**
 * Create a new game round.
 * @param callback - user callback function or null
 * @param gameId* - id game to create round for
 * @param name* - name of gameround
 * @param players - comma-separated player loginnames e.g. "bob,carol,ted"
 */
	roundCreate: function(callback, gameId, name, players) {
		var req = KW.createRequest('round-create-req');
		KW.UTIL.setAttr(req, 'gameid', gameId);
		KW.UTIL.setAttr(req, 'name', name);
		KW.UTIL.setOptAttr(req, 'players', players);

		KW.utopia(req, callback);
	},


/**
 * Delete a Gameround (only gameround owner is allowed to do this).
 * @param callback - user callback function or null
 * @param roundId* - id of gameround
 */
	roundDelete: function(callback, roundId) {
		var req = KW.createRequest('round-delete-req');

		// Only game id is required
		KW.UTIL.setAttr(req, 'id', roundId);

		KW.utopia(req, callback);
	}
}
