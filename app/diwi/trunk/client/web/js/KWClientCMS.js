 /*
 * KWClient extension functions for DiWi CMS.
 *
 * EXTERNALS
 * KWClient.js
 *
 * PURPOSE
 * Provide direct usable functions for DiWi CMS Handler requests in Ajax.
 *
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
		KW.UTIL.addOptTextElement(poi, 'description', poiObj.description);
		KW.UTIL.addOptTextElement(poi, 'type', poiObj.type);
		KW.UTIL.addOptTextElement(poi, 'category', poiObj.category);
		KW.UTIL.addOptTextElement(poi, 'x', poiObj.x);
		KW.UTIL.addOptTextElement(poi, 'y', poiObj.y);

        var media = req.createElement('media');
        poi.appendChild(media);
        // TODO: see how to solve this
        KW.UTIL.addOptTextElement(media, 'kich-uri', poiObj.kichuri1);
        KW.UTIL.addOptTextElement(media, 'kich-uri', poiObj.kichuri2);
        KW.UTIL.addOptTextElement(media, 'kich-uri', poiObj.kichuri3);
		
		req.documentElement.appendChild(poi);
		
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
		KW.UTIL.addOptTextElement(poi, 'description', poiObj.description);
		KW.UTIL.addOptTextElement(poi, 'type', poiObj.type);
		KW.UTIL.addOptTextElement(poi, 'category', poiObj.category);
		KW.UTIL.addOptTextElement(poi, 'x', poiObj.x);
		KW.UTIL.addOptTextElement(poi, 'y', poiObj.y);

        var media = req.createElement('media');
        poi.appendChild(media);
        // TODO: see how to solve this
        KW.UTIL.addOptTextElement(media, 'kich-uri', poiObj.kichuri1);
        KW.UTIL.addOptTextElement(media, 'kich-uri', poiObj.kichuri2);
        KW.UTIL.addOptTextElement(media, 'kich-uri', poiObj.kichuri3);

		req.documentElement.appendChild(poi);

        KW.utopia(req, callback);
	},

	getthemes: function(callback) {
		var req = KW.createRequest('kich-get-themes-req');

		KW.utopia(req, callback);
	},
	
	getendpoints: function(callback) {
		var req = KW.createRequest('poi-get-endpoints-req');

		KW.utopia(req, callback);
	},	

	getstartpoints: function(callback) {
		var req = KW.createRequest('poi-get-startpoints-req');

		KW.utopia(req, callback);
	},		
	
    /**
	 * Delete poi.
	 * @param callback - user callback function or null
	 * @param commentId - id of comment
	 */
	deletepoi: function(callback, targetId) {
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

	getpoi: function(callback, id) {
		var req = KW.createRequest('poi-get-req');
		KW.UTIL.setAttr(req, 'id', id);
		KW.utopia(req, callback);
	},

    /**
	 * Gets all kich media.
	 * @param callback - user callback function or null
	 * @param commentId - id of comment
	 */
	getallmedia: function(callback) {
		var req = KW.createRequest('kich-get-media');
		KW.utopia(req, callback);
	},

    /**
	 * Syncs KICH pois and routes
	 * @param callback - user callback function or null
	 */
	syncKICH: function(callback) {
		var req = KW.createRequest('kich-sync-req');
		KW.utopia(req, callback);
	}
	
	
}
