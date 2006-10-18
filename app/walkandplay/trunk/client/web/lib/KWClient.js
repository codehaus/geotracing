/*
 * AJAX-based KeyWorx client protocol library.
 *
 * EXTERNALS
 * This library requires DHTML.js to be included first.
 *
 * PURPOSE
 * This library can be used to quickly setup a KeyWorx protocol
 * session.
 *
 * USAGE
 * This library works asynchronously. The main thing to do is to supply your
 * JavaScript callback functions for positive and negative responses when
 * calling KW.init().
 *
 * Steps:
 * KW.init(MY.rspCallback, My.nrspCallback, 60);
 * KW.login('basic', 'auser', 'apasswd');
 * KW.selectApp('basicapp', 'user');
 * KW.utopia(your utopia request);
 *  .
 * KW.logout();
 *
 * Author: Just van den Broecke
 * $Id: KWClient.js,v 1.3 2006-07-09 14:58:20 just Exp $
 */
var KW = {
	url: 'proto.srv',
	timeoutMins : 5,
	protocolVersion : '4.0',
	agentKey: null,
	onRsp: null,
	onNegRsp: null,
	webRoot: null,

// Initialization: must be called before anything
	init: function(rspCallback, nrspCallback, theTimeoutMins) {

		// Sniff required browser features
		if (!document.getElementsByTagName) {
			alert('No browser XML support');
			return;
		}

		// Set client callback for XML responses and protocol neg responses
		KW.onRsp = rspCallback;
		KW.onNegRsp = nrspCallback;

		// Set protocol servlet URL
	//	KW.url = KW._getWebRoot() + KW.url;

		// Set (optional) timeout
		KW.timeoutMins = theTimeoutMins ? theTimeoutMins : KW.timeoutMins;
	},

	isLoggedIn: function() {
		return KW.agentKey != null;
	},

// Send XML request to server
	post: function(callback, doc) {
		// Obtain XMLHttpRequest object (cross-browser through Sarissa)
		var xmlhttp = new XMLHttpRequest();

		// Setup query string based on login state
		var qs = KW.agentKey == null ? '?timeout=' + KW.timeoutMins : '?agentkey=' + KW.agentKey;

		// Open URL
		xmlhttp.open('POST', KW.url + qs, true);

		// Setup response handling
		xmlhttp.onreadystatechange = function() {
			if (xmlhttp.readyState == 4) {
			//	alert(xmlhttp.responseText);
				var element = xmlhttp.responseXML.documentElement;
		//		alert(xmlhttp.responseText);
				// (new DOMParser()).parseFromString(xmlhttp.responseText, "text/xml").documentElement;
				if (KW.isPositive(element)) {
					callback(element);
				} else {
					KW.onNegRsp(element.getAttribute('errorId'), element.getAttribute('error'), element.getAttribute('details'));
				}
			}
		};

		// Send XML to KW server
		xmlhttp.send(doc.xml);
	},

// Create XML DOM document and return document element
	createRequest: function(tag) {
		var xmlDoc  = XmlDocument.create();
		var elm = xmlDoc.createElement(tag);
		xmlDoc.appendChild(elm);
		return xmlDoc;
	},

// Do login request
	login: function(portal, user, password) {
		var doc = KW.createRequest('login-req');
		var xml = doc.documentElement;
		xml.setAttribute('portalname', portal);
		xml.setAttribute('name', user);
		xml.setAttribute('password', password);
		xml.setAttribute('protocolVersion', KW.protocolVersion);
		KW.post(KW._loginRsp, doc);
	},

// Do logout request
	logout: function() {
		var doc = KW.createRequest('logout-req');
		var xml = doc.documentElement;
		KW.post(KW._logoutRsp, doc);
	},


// Do select-app request
	selectApp: function(app, role) {
		var doc = KW.createRequest('select-app-req');
		var xml = doc.documentElement;
		xml.setAttribute('appname', app);
		xml.setAttribute('rolename', role);
		KW.post(KW._selectAppRsp, doc);
	},


// Do utopia request
	utopia: function(reqDoc) {
		var doc = KW.createRequest('utopia-req');
		var xml = doc.documentElement;
		xml.appendChild(reqDoc.documentElement);
		KW.post(KW._utopiaRsp, doc);
	},

// Check for positive response
	isPositive: function(element) {
		return element.tagName.lastIndexOf('-rsp') != -1;
	},

//
// Private/internal functions
//
	_getWebRoot: function() {
		/** Return directory of this relative to document URL. */
		if (KW.webRoot != null) {
			return KW.webRoot;
		}
		//derive the baseDir value by looking for the script tag that loaded this file
		var head = document.getElementsByTagName('head')[0];
		var nodes = head.childNodes;
		for (var i = 0; i < nodes.length; ++i) {
			var src = nodes.item(i).src;
			if (src) {
				var index = src.indexOf("KWClient.js");
				if (index >= 0) {
					index = src.indexOf("lib");
					KW.webRoot = src.substring(0, index);
					break;
				}
			}
		}
		return KW.webRoot;
	},

// Callback for login response
	_loginRsp: function(element) {
		KW.agentKey = element.getAttribute('agentkey');
		KW.onRsp(element);
	},

// Callback for select-app response
	_selectAppRsp: function(element) {
		KW.onRsp(element);
	},


// Callback for utopia response
	_utopiaRsp: function(element) {
		KW.onRsp(element.childNodes[0]);
	},


// Callback for logout response
	_logoutRsp: function(element) {
		KW.agentKey = null;
		KW.onRsp(element);
	}
}

/*----------------------------------------------------------------------------\
|                                 XML Extras                                  |
|-----------------------------------------------------------------------------|
|                         Created by Erik Arvidsson                           |
|                  (http://webfx.eae.net/contact.html#erik)                   |
|                      For WebFX (http://webfx.eae.net/)                      |
|-----------------------------------------------------------------------------|
| XML and XML HTTP request abstraction.                                       |
|-----------------------------------------------------------------------------|
|             Copyright (c) 2001, 2002, 2003, 2006 Erik Arvidsson             |
|-----------------------------------------------------------------------------|
| Licensed under the Apache License, Version 2.0 (the "License"); you may not |
| use this file except in compliance with the License.  You may obtain a copy |
| of the License at http://www.apache.org/licenses/LICENSE-2.0                |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| Unless  required  by  applicable law or  agreed  to  in  writing,  software |
| distributed under the License is distributed on an  "AS IS" BASIS,  WITHOUT |
| WARRANTIES OR  CONDITIONS OF ANY KIND,  either express or implied.  See the |
| License  for the  specific language  governing permissions  and limitations |
| under the License.                                                          |
|-----------------------------------------------------------------------------|
| 2001-09-27 | Original Version Posted.                                       |
| 2006-05-29 | Changed license to Apache Software License 2.0.                |
|-----------------------------------------------------------------------------|
| Created 2001-09-27 | All changes are in the log above. | Updated 2006-05-29 |
\----------------------------------------------------------------------------*/

//<script>
//////////////////
// Helper Stuff //
//////////////////

// used to find the Automation server name
function getDomDocumentPrefix() {
	if (getDomDocumentPrefix.prefix)
		return getDomDocumentPrefix.prefix;

	var prefixes = ["MSXML2", "Microsoft", "MSXML", "MSXML3"];
	var o;
	for (var i = 0; i < prefixes.length; i++) {
		try {
			// try to create the objects
			o = new ActiveXObject(prefixes[i] + ".DomDocument");
			return getDomDocumentPrefix.prefix = prefixes[i];
		}
		catch (ex) {};
	}

	throw new Error("Could not find an installed XML parser");
}

function getXmlHttpPrefix() {
	if (getXmlHttpPrefix.prefix)
		return getXmlHttpPrefix.prefix;

	var prefixes = ["MSXML2", "Microsoft", "MSXML", "MSXML3"];
	var o;
	for (var i = 0; i < prefixes.length; i++) {
		try {
			// try to create the objects
			o = new ActiveXObject(prefixes[i] + ".XmlHttp");
			return getXmlHttpPrefix.prefix = prefixes[i];
		}
		catch (ex) {};
	}

	throw new Error("Could not find an installed XML parser");
}

//////////////////////////
// Start the Real stuff //
//////////////////////////


/*
// XmlHttp factory
function XmlHttp() {}

XmlHttp.create = function () {
	try {
		if (window.XMLHttpRequest) {
			var req = new XMLHttpRequest();

			// some versions of Moz do not support the readyState property
			// and the onreadystate event so we patch it!
			if (req.readyState == null) {
				req.readyState = 1;
				req.addEventListener("load", function () {
					req.readyState = 4;
					if (typeof req.onreadystatechange == "function")
						req.onreadystatechange();
				}, false);
			}

			return req;
		}
		if (window.ActiveXObject) {
			return new ActiveXObject(getXmlHttpPrefix() + ".XmlHttp");
		}
	}
	catch (ex) {}
	// fell through
	throw new Error("Your browser does not support XmlHttp objects");
}; */

// XmlDocument factory
function XmlDocument() {}

XmlDocument.create = function () {
	try {
		// DOM2
		if (document.implementation && document.implementation.createDocument) {
			var doc = document.implementation.createDocument("", "", null);

			// some versions of Moz do not support the readyState property
			// and the onreadystate event so we patch it!
			if (doc.readyState == null) {
				doc.readyState = 1;
				doc.addEventListener("load", function () {
					doc.readyState = 4;
					if (typeof doc.onreadystatechange == "function")
						doc.onreadystatechange();
				}, false);
			}

			return doc;
		}
		if (window.ActiveXObject)
			return new ActiveXObject(getDomDocumentPrefix() + ".DomDocument");
	}
	catch (ex) {}
	throw new Error("Your browser does not support XmlDocument objects");
};

// Create the loadXML method and xml getter for Mozilla
if (window.DOMParser &&
	window.XMLSerializer &&
	window.Node && Node.prototype && Node.prototype.__defineGetter__) {

	// XMLDocument did not extend the Document interface in some versions
	// of Mozilla. Extend both!
	//XMLDocument.prototype.loadXML =
	Document.prototype.loadXML = function (s) {

		// parse the string to a new doc
		var doc2 = (new DOMParser()).parseFromString(s, "text/xml");

		// remove all initial children
		while (this.hasChildNodes())
			this.removeChild(this.lastChild);

		// insert and import nodes
		for (var i = 0; i < doc2.childNodes.length; i++) {
			this.appendChild(this.importNode(doc2.childNodes[i], true));
		}
	};


	/*
	 * xml getter
	 *
	 * This serializes the DOM tree to an XML String
	 *
	 * Usage: var sXml = oNode.xml
	 *
	 */
	// XMLDocument did not extend the Document interface in some versions
	// of Mozilla. Extend both!
	/*
	XMLDocument.prototype.__defineGetter__("xml", function () {
		return (new XMLSerializer()).serializeToString(this);
	});
	*/
	try {

		Document.prototype.__defineGetter__("xml", function () {
			return (new XMLSerializer()).serializeToString(this);
		});
	} catch(e) {

	}
}

