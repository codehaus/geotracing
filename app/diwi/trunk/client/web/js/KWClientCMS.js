 /*
 * KWClient extension functions for DiWi CMS.
 *
 * EXTERNALS
 * KWClient.js
 *
 * PURPOSE
 * Provide direct usable functions for DiWi CMS Handler requests in Ajax.
 *
 * USAGE
 * Use KWClient to login and then call functions of this library.
 *
 * Each function has an optional "callback" parameter:
 * - used only for positive (utopia) responses
 * - if callback is null then the positive callback function specified in KW.init() is used
 * - negative responses will still use KW.onNegRsp specified in KW.init()
 *
 * Example:
 * KW.init(MY.rspCallback, My.nrspCallback);
 * KW.login('auser', 'apasswd');
 * KW.selectApp('basicapp', 'user');
 *
 * KW.TAG.add(myCallback, '123,345,678', 'monkey,lion')
 *  .
 * KW.logout();
 *
 * Author: Just van den Broecke
 * $Id: KWClientExt.js 377 2007-02-16 11:24:05Z just $
 */

/**
 * CMS handler functions.
 */
KW.CMS = {

	/**
	 * Add poi.
	 * @param callback - user callback function or null
	 * @param targetId - id of item to be commented
	 * @param text - comment text
	 */
	addpoi: function(callback, poiObj) {
		var req = KW.createRequest('poi-insert-req');
        var poi = req.createElement('poi');
		KW.UTIL.addOptTextElement(poi, 'name', poiObj.name);
		KW.UTIL.addOptTextElement(poi, 'decription', poiObj.description);
		KW.UTIL.addOptTextElement(poi, 'category', poiObj.category);
		KW.UTIL.addOptTextElement(poi, 'x', poiObj.x);
		KW.UTIL.addOptTextElement(poi, 'y', poiObj.y);

        var media = req.createElement('media');
        poi.documentElement.appendChild(media);
        // TODO: see how to solve this
        KW.UTIL.addOptTextElement(media, 'kich-uri', poiObj.kichuri1);
        KW.UTIL.addOptTextElement(media, 'kich-uri', poiObj.kichuri2);
        KW.UTIL.addOptTextElement(media, 'kich-uri', poiObj.kichuri3);

        req.documentElement.appendChild(profile);
        KW.utopia(req, callback);
	},

    /**
	 * Update poi.
	 * @param callback - user callback function or null
	 * @param targetId - id of item to be commented
	 * @param text - comment text
	 */
	updatepoi: function(callback, targetId, poiObj) {
		var req = KW.createRequest('poi-update-req');
        KW.UTIL.setAttr(req, 'id', targetId);
        var poi = req.createElement('poi');
		KW.UTIL.addOptTextElement(poi, 'name', poiObj.name);
		KW.UTIL.addOptTextElement(poi, 'decription', poiObj.description);
		KW.UTIL.addOptTextElement(poi, 'category', poiObj.category);
		KW.UTIL.addOptTextElement(poi, 'x', poiObj.x);
		KW.UTIL.addOptTextElement(poi, 'y', poiObj.y);

        var media = req.createElement('media');
        poi.documentElement.appendChild(media);
        // TODO: see how to solve this
        KW.UTIL.addOptTextElement(media, 'kich-uri', poiObj.kichuri1);
        KW.UTIL.addOptTextElement(media, 'kich-uri', poiObj.kichuri2);
        KW.UTIL.addOptTextElement(media, 'kich-uri', poiObj.kichuri3);

        req.documentElement.appendChild(profile);
        KW.utopia(req, callback);
	},

    /**
	 * Delete poi.
	 * @param callback - user callback function or null
	 * @param commentId - id of comment
	 */
	delpoi: function(callback, targetId) {
		var req = KW.createRequest('poi-delete-req');
		KW.UTIL.setAttr(req, 'id', targetId);
		KW.utopia(req, callback);
	},

    /**
	 * Gets all poi's.
	 * @param callback - user callback function or null
	 * @param commentId - id of comment
	 */
	getallpoi: function(callback) {
		var req = KW.createRequest('poi-getlist-req');
		KW.utopia(req, callback);
	},


    /**
	 * Gets all kich media.
	 * @param callback - user callback function or null
	 * @param commentId - id of comment
	 */
	getallpoi: function(callback) {
		var req = KW.createRequest('kich-getlist-req');
		KW.utopia(req, callback);
	}
}
