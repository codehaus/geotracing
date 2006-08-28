// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * API for requests to KW server.
 *
 * PURPOSE
 * This library can be used for any request to KW server. Mostly prepared
 * queries. Each request returns an XML document.  SRV.get() is a varargs function.
 * The first parameter identifies a "command", specific to the application.
 *
 * USAGE
 * SRV.init()
 * SRV.get("q-get-active-tracks", myfun, "state", "3", "name", "piet);
 *
 * Author: Just van den Broecke
 * $Id: Server.js,v 1.2 2006-05-02 15:12:29 just Exp $
 */

//
// API to invoke commands on KW server
//
var SRV = {
// Query/get data URL to server (note: set this if invoked outside webapp)
	url: 'srv/get.jsp?',
	initialized: false,

// Initialization: must be called before anything
	init: function() {
		if (SRV.initialized == true) {
			return;
		}

		// Need DHTML lib...
		if (!DH) {
			alert('dhtml.js not found; is it imported before server.js?');
			return;
		}

		// Init DHTML lib
		DH.init();
		// alert(SRV.url);
		SRV.initialized = true;
	},

// Generic varargs command function for all requests to server.
// arg[0] command
// arg[1] optional user supplied callback function or null (return result sync)
// arg[2]...arg[2+2N] N arguments specific to command
// each argument has a name and a value like "tables", "person"
// example:
// SRV.get("q-get-active-tracks", myfun, "state", "3", "name", "piet);
	get: function(aCmd, aCallback, theOtherArgs) {
		var argv = SRV.get.arguments;
		var argc = arguments.length;
		if (argc < 2) {
			alert('get command must have at least 2 argv (cmd+callback)');
			return;
		}

		/*var s='argv=';
			for (var i=0; i < argc; i++) {
			  s+= ('argv[' + i + ']=' + argv[i]);
			}

			alert('argc=' + argc + ' ' + s); */
		var cmd = aCmd;
		var callback = aCallback;

		// Use remaining args to form URL-query string
		var qs = 'cmd=' + cmd;
		for (var i = 2; i < argc; i++) {
			qs = SRV._xpand(qs, argv[i], argv[++i]);
		}

		// Query commands get intercepted to convert to Record array
		var url = SRV.url + qs;

		if (cmd.indexOf("q-") != -1) {
			// Query-type request: intercept and convert result to records
			// before doing callback
			if (callback != null) {
				qr = new SRV._queryRspHandler(callback);
				DH.getXML(url, qr.onQueryRsp);
			} else {
				return SRV._rsp2Records(DH.getXML(url));
			}
		} else {
			if (callback != null) {
				DH.getXML(url, callback);
			} else {
				return DH.getXML(url);
			}
		}
	},

// Do generic query to KW server
	query: function(callback, tables, fields, where, rels, orderby, directions) {
		var qs = 'cmd=q-store';
		qs = SRV._xpand(qs, 'tables', tables);
		qs = SRV._xpand(qs, 'fields', fields);
		qs = SRV._xpand(qs, 'where', where);
		qs = SRV._xpand(qs, 'rels', rels);
		qs = SRV._xpand(qs, 'orderby', orderby);
		qs = SRV._xpand(qs, 'directions', directions);
		if (callback != null) {
			qr = new SRV._queryRspHandler(callback);
			DH.getXML(SRV.url + qs, qr.onQueryRsp);
		} else {
			return SRV._rsp2Records(DH.getXML(SRV.url + qs));
		}
	},

// Internal util object to allow interception of callback
	_queryRspHandler: function(cb) {

		// This is needed since JS only allows local scope vars for
		// callbacks. See also http://w3future.com/html/stories/callbacks.xml
		var userCallback = cb;
		this.onQueryRsp = function(xml) {
			// Call original user
			userCallback(SRV._rsp2Records(xml));
		}
	},

// Internal util to convert XML response to Record array
	_rsp2Records: function(xml) {
		var records = [];
		if (!xml) {
			return records;
		}

		// Convert xml doc to array of Record objects
		var recordElements = xml.documentElement.getElementsByTagName('record');
		for (i = 0; i < recordElements.length; i++) {
			records.push(new Record(recordElements[i]));
		}

		// Call original user
		return records;

	},

// Internal util to expand name value pairs into URL query part.
	_xpand: function(qs, name, value) {
		if (value && value != null) {
			qs += ('&' + name + '=' + value);
		}
		return qs;
	}
} // END SRV functions




