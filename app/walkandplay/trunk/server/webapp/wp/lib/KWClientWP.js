/*
* KWClient extension functions for WalkAndPlay.
*
* EXTERNALS
* KWClient.js
* KWClientExt.js
*
* PURPOSE
* Provide direct usable functions for WalkAndPlay requests in Ajax.
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
 * Add task to Game.
 * @param callback - user callback function or null
 * @param gameId - id of item to be commented
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
 * Delete task from Game.
 * @param callback - user callback function or null
 * @param taskId - id of task
 */
	gameDelTask: function(callback, taskId) {
		var req = KW.createRequest('game-delete-task-req');

		// Only medium id is required (game is coupled to medium)
		KW.UTIL.setAttr(req, 'id', taskId);

		KW.utopia(req, callback);
	}


}
