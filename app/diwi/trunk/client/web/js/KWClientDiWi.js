/*
 * KWClient extension functions for DiWi Portal.
 *
 * EXTERNALS
 * KWClient.js
 *
 * PURPOSE
 * Provide direct usable functions for DiWi Portal
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
 * Author: Hans Speijer
 * $Id:  $
 */

/**
 * Diwi portal functions.
 */
KW.DIWI = {
	BOS_PARAM:          'bos',
	HEIDE_PARAM: 		'heide',
	START_POINT_PARAM:	'startpoint',
	DISTANCE_PARAM:		'distance',


	/*
	sample calling:
	
	var params = new Array();
	params[KW.DIWI.BOS_PARAM] = 15;
	params[KW.DIWI.HEIDE_PARAM] = 80;	
	KW.DIWI.generateroute(myCallback, params);
	*/
	
	generateroute: function(callback, params) {
		var req = KW.createRequest('route-generate-req');
		var key;
		for (key in params) {
			var pref = req.createElement('pref');
			pref.setAttribute('name', key);
			pref.setAttribute('value', params[key]);			
			req.documentElement.appendChild(pref);
		} 

		KW.utopia(req, callback);
	}
}
