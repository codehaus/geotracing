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
 * $Id$
 */

//
// API to invoke commands on KW server
//
var SRV = {
// Query/get data URL to server (note: set this if invoked outside webapp)
	url: 'srv/get.jsp?',
	putURL: null,
	initialized: false,

	addChildTextNode: function(doc, parent, tag, text) {
		if (!text || text == null) {
			return false;
		}

		if (typeof text != 'string') {
			text = text + '';
		}
		var elm = doc.createElement(tag);
		var textNode = doc.createTextNode(text);
		elm.appendChild(textNode);
		parent.appendChild(elm);
	},

// arg[2]...arg[2+2N] N arguments specific to command
// each argument has a name and a value like "tables", "person"
// example:
// SRV.createXMLReq("cmt-insert-req", "target", "12345", "content", "the comment text");
	createXMLReq: function(aTag, theFields) {
		// Create put request
		var doc = DH.createXmlDocument();
		var reqElm = doc.createElement(aTag);
		var argv = SRV.createXMLReq.arguments;
		var argc = arguments.length;
		for (var i = 1; i < argc; i++) {
			SRV.addChildTextNode(doc, reqElm, argv[i], argv[++i]);
		}

		doc.appendChild(reqElm);
		return doc;
	},

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

		SRV.putURL = DH.getBaseDir() + '/../srv/put.jsp';

		// alert(SRV.url);
		SRV.initialized = true;
	},

// Generic varargs command function for all query requests to server.
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
		// &t=UTC-time makes URL random to prevent possible (IE) caching
		var url = SRV.url + qs + '&t=' + (new Date().getTime());

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

// Generic function for put request to server.
// arg[0] XML document with request
// arg[1] optional user supplied callback function or null (return result sync)
// SRV.put(commentReqDoc, myfun);
	put: function(aReq, aCallback) {
		// &t=UTC-time makes URL random to prevent possible (IE) caching
		var url = SRV.putURL + '?t=' + (new Date().getTime());
		if (aCallback != null) {
			DH.postXML(url, aReq, aCallback);
		} else {
			DH.postXML(url, aReq);
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

		if (!xml.documentElement) {
			alert('sorry problem with XML records, working on this, please try again');
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




