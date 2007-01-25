/*
 * AJAX-based KeyWorx client protocol library.
 *
 * EXTERNALS
 * none.
 *
 * PURPOSE
 * This library can be used to conduct a KeyWorx protocol
 * session. Look for KWClientExt.js for application-specific extension
 * (utopia) protocol support.
 *
 * USAGE
 * This library works asynchronously. The main thing to do is to supply your
 * JavaScript callback functions for positive and negative responses when
 * calling KW.init(). KW.utopia() allows an optional per-invokation callback.
 *
 * Steps:
 * KW.init(MY.rspCallback, My.nrspCallback, 60, '/basic');
 * KW.login('auser', 'apasswd');
 * KW.selectApp('basicapp', 'user');
 * KW.utopia(your utopia request);
 *  .
 * KW.logout();
 *
 * Notes
 * - whenever a session timeout occurs re-login and re-issueing of the request is attempted
 * - all negative responses will always be directed to the supplied neg rsp handler
 * even if a specific callback is provided with KW.utopia().
 * - all responses return the documentElement of the received rsp XML doc.
 *
 * Author: Just van den Broecke
 * $Id: KWClient.js 241 2006-12-21 10:26:45Z just $
 */
var KW = {
	url: '/proto.srv',
	timeoutMins : 5,
	protocolVersion : '4.0',
	agentKey: null,
	onRsp: null,
	onNegRsp: null,
	webRoot: null,
	IS_SAFARI : (navigator.userAgent && navigator.vendor && (navigator.userAgent.toLowerCase().indexOf("applewebkit") != -1 || navigator.vendor.indexOf("Apple") != -1)),
	loginReq: null,
	selectAppReq: null,
	recovering: false,
	userId: 0,
// Initialization: must be called before anything
	init: function(rspCallback, nrspCallback, theTimeoutMins, theWebRoot) {

		// Sniff required browser features
		if (!document.getElementsByTagName) {
			alert('No browser XML support');
			return;
		}

		// Set client callback for XML responses and protocol neg responses
		KW.onRsp = rspCallback;
		KW.onNegRsp = nrspCallback;

		// Set (optional) timeout
		KW.timeoutMins = theTimeoutMins ? theTimeoutMins : KW.timeoutMins;

		// If webroot specified use that
		if (theWebRoot) {
			KW.webRoot = theWebRoot;
		} else {
			KW.webRoot = KW._getWebRoot();
		}
		KW.url = KW.webRoot + KW.url;

	},

	isLoggedIn: function() {
		return KW.agentKey != null;
	},

// Send XML request to server
	post: function(callback, doc) {
		// Obtain XMLHttpRequest object
		var xmlhttp = new XMLHttpRequest();

		// Setup query string based on login state
		var qs = KW.agentKey == null ? '?timeout=' + KW.timeoutMins : '?agentkey=' + KW.agentKey;

		// Open URL
		xmlhttp.open('POST', KW.url + qs, true);

		// Setup response handling
		xmlhttp.onreadystatechange = function() {
			if (xmlhttp.readyState == 4) {
				var element = xmlhttp.responseXML.documentElement;
				// (new DOMParser()).parseFromString(xmlhttp.responseText, "text/xml").documentElement;
				if (KW.isPositive(element)) {
					callback(element);
				} else if (element.getAttribute('errorId') == '4007' && KW.isLoggedIn()) {
					// Session timeout: try to re-establish session and re-issue request
					KW._recoverSession(callback, doc);
				} else {
					KW._negativeRsp(element);
				}
			}
		};

		// Send XML to KW server
		// Safari needs just doc, others support doc.xml
		if (this.IS_SAFARI) {
			xmlhttp.send(doc);
		} else {
			xmlhttp.send(doc.xml);
		}
	},

// Create XML DOM document and return document element
	createRequest: function(tag) {
		var xmlDoc = XmlDocument.create();
		var elm = xmlDoc.createElement(tag);
		xmlDoc.appendChild(elm);
		return xmlDoc;
	},

// Do login request
	login: function(user, password) {
		var doc = KW.createRequest('login-req');
		var xml = doc.documentElement;
		xml.setAttribute('name', user);
		xml.setAttribute('password', password);
		xml.setAttribute('protocolVersion', KW.protocolVersion);
		KW.loginReq = doc;
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
		KW.selectAppReq = doc;
		KW.post(KW._selectAppRsp, doc);
	},


// Do utopia request
	utopia: function(reqDoc, aCallback) {
		// Wrapper request
		var doc = KW.createRequest('utopia-req');

		// Copy specific request node as child of <utopia-req>
		// copyChildNodes(reqDoc, doc.documentElement);
		var nodes = reqDoc.childNodes;
		var nodeTo = doc.documentElement;
		if (doc.importNode) {
			for (var i = 0; i < nodes.length; i++) {
				nodeTo.appendChild(doc.importNode(nodes[i], true));
			}
		} else {
			for (var i = 0; i < nodes.length; i++) {
				nodeTo.appendChild(nodes[i].cloneNode(true));
			}
		}

		var cb = KW._utopiaRsp;

		// If client supplied callback overrule the default callback
		if (aCallback) {
			cb = function(utopiaRsp) {
				// Strip off <utopia-rsp> parent element
				var appRsp = utopiaRsp.childNodes[0];
				if (KW.isPositive(appRsp)) {
					aCallback(appRsp);
				} else {
					// Handle all negative responses centrally
					KW._negativeRsp(appRsp);
				}
			}
		}

		KW.post(cb, doc);
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
		var webRoot;

		//derive the baseDir value by looking for the script tag that loaded this file
		var head = document.getElementsByTagName('head')[0];
		var nodes = head.childNodes;
		for (var i = 0; i < nodes.length; ++i) {
			var src = nodes.item(i).src;
			if (src) {
				var index = src.indexOf("KWClient.js");
				if (index >= 0) {
					if (src.indexOf("lib") >= 0) {
						index = src.indexOf("lib");
					}
					webRoot = src.substring(0, index);
					break;
				}
			}
		}
		return webRoot;
	},

// Callback for login response
	_loginRsp: function(element) {
		KW.agentKey = element.getAttribute('agentkey');
		var c;
		try {
			c = getCookie('name');
		} catch(e) { c = null;}
			if(c == null || c.length < 3) {
				setCookie( 'name', document.getElementById('username').value);
				setCookie( 'password', document.getElementById('password').value);
			}
		KW.userId = element.getElementsByTagName('personid')[0].childNodes[0].nodeValue;
		//alert(KW.userId);
		KW.onRsp(element);
	},

// Callback for select-app response
	_selectAppRsp: function(element) {
		KW.onRsp(element);
	},


// Callback for utopia response
	_utopiaRsp: function(element) {
		// Strip off <utopia-rsp> parent element
		var appRsp = element.childNodes[0];
		if (KW.isPositive(appRsp)) {
			KW.onRsp(appRsp);
		} else {
			// Handle all negative responses centrally
			KW._negativeRsp(appRsp);
		}
	},


// Callback for logout response
	_logoutRsp: function(element) {
		KW.agentKey = null;
		KW.onRsp(element);
	},

	_negativeRsp: function(element) {
		KW.onNegRsp(element.getAttribute('errorId'), element.getAttribute('error'), element.getAttribute('details'));
	},

	/** Session recovery: after re-establish re-issue request. */
	_recoverSession: function(callback, reqDoc) {
		KW.agentKey = null;
		var onLoginRsp = function(rsp) {
			KW.agentKey = rsp.getAttribute('agentkey');
			var onSelectAppRsp = function(rsp) {
				// Re-issue original request
				KW.post(callback, reqDoc);
			}
			KW.post(onSelectAppRsp, KW.selectAppReq);
		}
		KW.post(onLoginRsp, KW.loginReq);
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
		catch (ex) {
		}
		;
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
		catch (ex) {
		}
		;
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
function XmlDocument() {
}

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
	catch (ex) {
	}
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

// From DHTML.js (voor Jan)
/*
	 Setup Cross-Browser XMLHttpRequest v1.2
	Emulate Gecko 'XMLHttpRequest()' functionality in IE and Opera. Opera requires
	the Sun Java Runtime Environment <http://www.java.com/>.

	by Andrew Gregory
	http://www.scss.com.au/family/andrew/webdesign/xmlhttprequest/

	This work is licensed under the Creative Commons Attribution License. To view a
	copy of this license, visit http://creativecommons.org/licenses/by-sa/2.5/ or
	send a letter to Creative Commons, 559 Nathan Abbott Way, Stanford, California
	94305, USA.

	*/
// IE support
if (window.ActiveXObject && !window.XMLHttpRequest) {
	window.XMLHttpRequest = function() {
		var msxmls = new Array(
				'Msxml2.XMLHTTP.5.0',
				'Msxml2.XMLHTTP.4.0',
				'Msxml2.XMLHTTP.3.0',
				'Msxml2.XMLHTTP',
				'Microsoft.XMLHTTP');
		for (var i = 0; i < msxmls.length; i++) {
			try {
				return new ActiveXObject(msxmls[i]);
			} catch (e) {
			}
		}
		return null;
	};
}

// ActiveXObject emulation
if (!window.ActiveXObject && window.XMLHttpRequest) {
	window.ActiveXObject = function(type) {
		switch (type.toLowerCase()) {
			case 'microsoft.xmlhttp':
			case 'msxml2.xmlhttp':
			case 'msxml2.xmlhttp.3.0':
			case 'msxml2.xmlhttp.4.0':
			case 'msxml2.xmlhttp.5.0':
				return new XMLHttpRequest();
		}
		return null;
	};
}


/**
 * <p> Copies the childNodes of nodeFrom to nodeTo</p>
 * TODO: fix - does not work in IE!!!
 * <p> <b>Note:</b> The second object's original content is deleted before
 * the copy operation, unless you supply a true third parameter</p>
 * @argument nodeFrom the Node to copy the childNodes from
 * @argument nodeTo the Node to copy the childNodes to
 * @argument bPreserveExisting whether to preserve the original content of nodeTo, default is false
 */
function copyChildNodes(nodeFrom, nodeTo) {
	if ((!nodeFrom) || (!nodeTo)) {
		throw "Both source and destination nodes must be provided";
	}
	;

	var ownerDoc = nodeTo.nodeType == Node.DOCUMENT_NODE ? nodeTo : nodeTo.ownerDocument;
	var nodes = nodeFrom.childNodes;
	if (ownerDoc.importNode) {
		for (var i = 0; i < nodes.length; i++) {
			nodeTo.appendChild(ownerDoc.importNode(nodes[i], true));
		}
		;
	} else {
		for (var i = 0; i < nodes.length; i++) {
			nodeTo.appendChild(nodes[i].cloneNode(true));
		}
		;
	}
	;
}
;
