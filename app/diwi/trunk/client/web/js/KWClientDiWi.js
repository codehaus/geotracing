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
	BESLOTEN_PARAM:		'besloten',
	HALFOPEN_PARAM:		'halfopen',
	OPEN_PARAM:			'open',
	BEDRIJVEN_PARAM:	'bedrijven',
	BEWONING_PARAM:		'bewoning',
	BOS_PARAM:          'bos',
	HEIDE_PARAM: 		'heide',
	GRASLAND_PARAM:		'grasland',
	ZEE_PARAM:			'zee',
	SLOTEN_PARAM:		'sloten',
	STARTPUNT_PARAM:	'startpunt',
	EINDPUNT_PARAM:		'eindpunt',
	THEMA_PARAM:		'thema',
	WANDELAAR_PARAM:	'wandelaar',
	AFSTAND_PARAM:		'afstand',
	
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
			if(params[key] != null && params[key] != "") {
				var pref = req.createElement('pref');
				pref.setAttribute('name', key);
				pref.setAttribute('value', params[key]);			
				req.documentElement.appendChild(pref);
			}
		} 

		KW.utopia(req, callback);
	},
	
	getroute: function(callback, id) {
		var req = KW.createRequest('route-get-req');
		req.documentElement.setAttribute('id', id);

		KW.utopia(req, callback);
	},
	
	getpreferences: function(callback) {
		var req = KW.createRequest('diwi-get-preferences-req');

		KW.utopia(req, callback);		
	},
	
	getmap: function(callback, id, width, height) {
		var req = KW.createRequest('route-get-map-req');

		req.documentElement.setAttribute('id', id);
		req.documentElement.setAttribute('height', height);
		req.documentElement.setAttribute('width', width);

		KW.utopia(req, callback);		
	},
	
	getfixedroutes: function(callback) {
		var req = KW.createRequest('route-getlist-req');
        req.documentElement.setAttribute('type', 'fixed');
        KW.utopia(req, callback);
	},
	
	getgeneratedroute: function(callback, personid) {
		var req = KW.createRequest('route-getlist-req');
        req.documentElement.setAttribute('type', 'generated');        
        KW.utopia(req, callback);
	},
	
	tracepoint: function(callback, lat, lon, time) {
		var req = KW.createRequest('nav-point-req');
		var pt = req.createElement('pt');
		pt.setAttribute('lat', lat);
		pt.setAttribute('lon', lon);			
		pt.setAttribute('t', time);			
		
		req.documentElement.appendChild(pt);
				
		KW.utopia(req, callback);		
	},
	
	starttrace: function(callback) {
		var req = KW.createRequest('nav-start-req');

		KW.utopia(req, callback);		
	},
	
	stoptrace: function(callback) {
		var req = KW.createRequest('nav-stop-req');

		KW.utopia(req, callback);		
	}
	
	gettracks: function(callback, username) {
		('q-store-req');
        req.documentElement.setAttribute('cmd', 'q-tracks-by-user');
        req.documentElement.setAttribute('user', username);
                
	}
	
	
			
}
