// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Cross-browser DHTML lib.
 *
 * PURPOSE
 * This library can be used for common DHTML functions
 *
 * USAGE
 * DH.init()
 * @constructor
 * Author: Just van den Broecke
 * $Id$
 */
var DH = {
	initialized: false,
	isIE6CSS: (document.compatMode && document.compatMode.indexOf("CSS1") >= 0) ? true : false,
	isIE: false,
	isSafari : (navigator.userAgent && navigator.vendor && (navigator.userAgent.toLowerCase().indexOf("applewebkit") != -1 || navigator.vendor.indexOf("Apple") != -1)),
	baseDir: null,
/*************** Debug utility *******************************/
	timestamp: 0,
	debugWindow: null,
	messages: new Array(),
	messagesIndex: 0,

/** Includes loading stuff. */
	includes: new Array(),
	onReady: null,

/** Send debug messages to a (D)HTML window. */
	debug: function(flag, label, value) {
		if (flag == false) {
			return;
		}
		var funcName = "none";

		// Fetch JS function name if any
		if (DH.debug.caller) {
			funcName = DH.debug.caller.toString()
			funcName = funcName.substring(9, funcName.indexOf(")") + 1)
		}

		// Create message
		var msg = "-" + funcName + ": " + label + "=" + value

		// Add optional timestamp
		var now = new Date()
		var elapsed = now - DH.timestamp
		if (elapsed < 10000) {
			msg += " (" + elapsed + " msec)"
		}

		DH.timestamp = now;

		// Show.

		if ((DH.debugWindow == null) || DH.debugWindow.closed) {
			DH.debugWindow = window.open("", "DebugWin", "toolbar=no,scrollbars=yes,resizable=yes,width=600,height=400");
		}

		// Add message to current list
		DH.messages[DH.messagesIndex++] = msg;

		// Write doc header
		DH.debugWindow.document.writeln('<html><head><title>Debug Window</title></head><body bgcolor=#DDDDDD>');

		// Write the messages
		for (var i = 0; i < DH.messagesIndex; i++) {
			DH.debugWindow.document.writeln('<pre>' + i + ': ' + DH.messages[i] + '</pre>');
		}

		// Write doc footer and close
		DH.debugWindow.document.writeln('</body></html>');
		DH.debugWindow.document.close();
		DH.debugWindow.focus();

	},


/** Initialization: is called when document is loaded (see below). */
	init: function() {
		if (DH.initialized == true) {
			return;
		}

		// Some checks need this
		DH.isIE = !window.opera && navigator.userAgent.indexOf('MSIE') != -1;

		// Sniff required browser features
		if (!document.getElementById) {
			alert('Your browser does not support W3C DHTML, use a modern browser like FireFox');
			return;
		}

		// Sniff required browser features
		if (!document.getElementsByTagName) {
			alert('No browser XML support, use a modern browser like FireFox');
			return;
		}

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
		// Dynamic script loading
		if (DH.onReady != null) {
			//DH.debug(true, 'ready=' + document.readyState, 'start loading...')
			DH.includesTimer = setInterval('DH.checkIncludes()', 100);
		}

		DH.initialized = true;
	},

/** Check for loading of included scripts. */
	checkIncludes: function() {
		if (document.readyState != null) {
			// IE client

			// Scripts are removed from array when they have loaded
			while (DH.includes.length > 0
					&& (DH.includes[0].readyState == "loaded" || DH.includes[0].readyState == "complete" || DH.includes[0].readyState == null)) {
				DH.includes.shift();
			}

			// All loaded ?
			if (DH.includes.length == 0) {
				clearInterval(DH.includesTimer);
				DH.onReady();
				return true;
			} else {
				return false;
			}
		}
		else {
			// Mozilla client (single threaded)
			clearInterval(DH.includesTimer);
			DH.onReady();
			return true;
		}
	},

// Cross-browser add event listener to element
	addEvent: function(elm, evType, callback, useCapture) {
		var obj = DH.getObject(elm);
		if (obj.addEventListener) {
			obj.addEventListener(evType, callback, useCapture);
			return true;
		} else if (obj.attachEvent) {
			var r = obj.attachEvent('on' + evType, callback);
			return r;
		} else {
			obj['on' + evType] = callback;
		}
	},

// Cross-browser remove event listener to element
// See http://www.quirksmode.org/js/events_advanced.html
	removeEvent: function(elm, evType, callback) {
		var obj = DH.getObject(elm);
		if (obj.removeEventListener) {
			obj.removeEventListener(evType, callback, false);
			return true;
		} else if (obj.detachEvent) {
			var r = obj.detachEvent('on' + evType, callback);
			return r;
		} else {
			obj['on' + evType] = null;
		}
	},


// Get event
	getEvent: function(e) {
		// Obtain event  (cross-browser)
		return window.event ? window.event : e;
	},

// Get target object for event
	getEventTarget: function(e) {
		// Obtain event target (cross-browser)
		var target = window.event ? window.event.srcElement : e ? e.target : null;

		// defeat Safari bug
		// http://www.quirksmode.org/js/events_properties.html
		if (target != null && target.nodeType == 3) {
			target = target.parentNode;
		}
		return target;
	},

// Get target object for event
	cancelEvent: function(e) {
		// Cancel event propagation (IE)
		if (window.event) {
			window.event.cancelBubble = true;
			window.event.returnValue = false;
			return;
		}

		// Cancel event propagation (others)
		if (e) {
			e.stopPropagation();
			e.preventDefault();
		}
	},

// x-coord of event
	getEventX: function(e) {
		var x;
		if (e.pageX) {
			x = e.pageX;
		} else if (e.clientX) {
			x = e.clientX;
			if (DH.isIE) {
				x += document.body.scrollLeft;
			}
		}
		return x;
	},

// y-coord of event
	getEventY: function(e) {
		var y;
		if (e.pageY) {
			y = e.pageY;
		} else if (e.clientY) {
			y = e.clientY;
			if (DH.isIE) {
				y += document.body.scrollTop;
			}
		}
		return y;
	},

	blink: function(elm, cnt, interval) {
		var obj = DH.getObject(elm)
		if (cnt <= 0) {
			DH.show(obj);
			return;
		}

		DH.toggleVisibility(obj);
		setTimeout(function() {
			DH.blink(obj, cnt - 1, interval)
		}, interval);
	},

/* PNG transparancy fix IE.
 * See http://homepage.ntlworld.com/bobosola/index.htm
 * To implement this only on specific PNGs, add the following to each PNG image you wish to transform:
 * <img src="xyz.png" alt="foo" width="10" height="20" onload="DH.fixPNG(this)">
 */
	fixPNG: function(myImage) {
		var arVersion = navigator.appVersion.split("MSIE")
		var version = parseFloat(arVersion[1])
		if ((version >= 5.5) && (version < 7) && (document.body.filters))
		{
			var imgID = (myImage.id) ? "id='" + myImage.id + "' " : ""
			var imgClass = (myImage.className) ? "class='" + myImage.className + "' " : ""
			var imgTitle = (myImage.title) ?
						   "title='" + myImage.title + "' " : "title='" + myImage.alt + "' "
			var imgStyle = "display:inline-block;" + myImage.style.cssText
			var strNewHTML = "<span " + imgID + imgClass + imgTitle
					+ " style=\"" + "width:" + myImage.width
					+ "px; height:" + myImage.height
					+ "px;" + imgStyle + ";"
					+ "filter:progid:DXImageTransform.Microsoft.AlphaImageLoader"
					+ "(src=\'" + myImage.src + "\', sizingMethod='scale');\"></span>"
			myImage.outerHTML = strNewHTML
		}
	},


/** Return directory of this relative to document URL. */
	getBaseDir: function() {
		if (DH.baseDir != null) {
			return DH.baseDir;
		}
		//derive the baseDir value by looking for the script tag that loaded this file
		var head = document.getElementsByTagName('head')[0];
		var nodes = head.childNodes;
		for (var i = 0; i < nodes.length; ++i) {
			var src = nodes.item(i).src;
			DH.debug(false, 'src', src)
			if (src) {
				var index = src.indexOf("/DHTML.js");
				if (index >= 0) {
					DH.baseDir = src.substring(0, index);
					break;
				}
			}
		}
		return DH.baseDir;
	},

// Return the available content width space in browser window
	getInsideWindowWidth: function () {
		if (window.innerWidth) {
			return window.innerWidth;
		} else if (DH.isIE6CSS) {
			// measure the html element's clientWidth
			return document.body.parentElement.clientWidth
		} else if (document.body && document.body.clientWidth) {
			return document.body.clientWidth;
		}
		return 0;
	},

// Return the available content height space in browser window
	getInsideWindowHeight: function () {

		if (window.innerHeight) {
			return window.innerHeight;
		} else if (DH.isIE6CSS) {
			// measure the html element's clientHeight
			return document.body.parentElement.clientHeight
		} else if (document.body && document.body.clientHeight) {
			return document.body.clientHeight;
		}
		return 0;
	},

// Get object reference
	getObject: function(obj) {
		if (typeof obj == "string") {
			return document.getElementById(obj);
		} else {
			// pass through object reference
			return obj;
		}
	},

// Get object in another window.
	getObjectInWindow: function(winId, objId) {
		try {
			// Get window object.
			var winObj = eval(winId);
			return winObj.document.getElementById(objId);
		} catch(e) {
			return null;
		}
	},

// Get object.style reference by id
	getStyleObject: function(obj) {
		try {
			return DH.getObject(obj).style;
		} catch(e) {
			return null;
		}
	},

// Object X coordinate
// Based on findPos*, by ppk (http://www.quirksmode.org/js/findpos.html)
	getObjectX: function(obj) {
		obj = DH.getObject(obj);
		var curLeft = 0;
		if (obj.offsetParent) {
			do {
				curLeft += obj.offsetLeft;
			} while (obj = obj.offsetParent);
		} else if (obj.x) {
			curLeft += obj.x;
		}
		return curLeft;
	},

// Object Y coordinate
// Based on findPos*, by ppk (http://www.quirksmode.org/js/findpos.html)
	getObjectY: function(obj) {
		obj = DH.getObject(obj);
		var curTop = 0;
		if (obj.offsetParent) {
			do {
				curTop += obj.offsetTop;
			} while (obj = obj.offsetParent);
		} else if (obj.y) {
			curTop += obj.y;
		}
		return curTop;
	},

// Retrieve the x coordinate of a positionable object
	getObjectLeft: function (obj) {
		var styleObj = DH.getStyleObject(obj);
		if (styleObj == null) {
			return 0;
		}
		return parseInt(styleObj.left);
	},

// Retrieve the y coordinate of a positionable object
	getObjectTop: function (obj) {
		var styleObj = DH.getStyleObject(obj);
		if (styleObj == null) {
			return 0;
		}
		return parseInt(styleObj.top);
	},

// Retrieve the rendered width of an element
	getObjectWidth: function (obj) {
		var elem = DH.getObject(obj);
		var result = 0;
		if (elem.offsetWidth) {
			result = elem.offsetWidth;
		} else if (elem.clip && elem.clip.width) {
			result = elem.clip.width;
		} else if (elem.style && elem.style.pixelWidth) {
			result = elem.style.pixelWidth;
		}
		return parseInt(result);
	},

// Retrieve the rendered height of an element
	getObjectHeight: function (obj) {
		var elem = DH.getObject(obj);
		var result = 0;
		if (elem.offsetHeight) {
			result = elem.offsetHeight;
		} else if (elem.clip && elem.clip.height) {
			result = elem.clip.height;
		} else if (elem.style && elem.style.pixelHeight) {
			result = elem.style.pixelHeight;
		}
		return parseInt(result);
	},

// Parse parameter out of a string.
	_getParameter: function(string, parm, delim) {
		// returns value of parm from string
		if (string.length == 0) {
			return '';
		}
		var sPos = string.indexOf(parm + "=");
		if (sPos == -1) {
			return '';
		}
		sPos = sPos + parm.length + 1;
		var ePos = string.indexOf(delim, sPos);
		if (ePos == -1) {
			ePos = string.length;
		}
		return unescape(string.substring(sPos, ePos));
	}
	,

// Get parameter from query string passed to my page
	getPageParameter: function(parameterName, defaultValue) {
		var s = self.location.search;
		if ((s == null) || (s.length < 1)) {
			return defaultValue;
		}
		s = DH._getParameter(s, parameterName, '&');
		if ((s == null) || (s.length < 1)) {
			s = defaultValue;
		}
		return s;
	}
	,

/******* START XML DOM Functions. *************/

	/** Find MSXML prefix for DomDocument object. */
	getMSDomDocumentPrefix:  function() {
		if (DH.msDomDocumentPrefix) {
			return DH.msDomDocumentPrefix;
		}

		var prefixes = ["MSXML2", "Microsoft", "MSXML", "MSXML3"];
		var o;
		for (var i = 0; i < prefixes.length; i++) {
			try {
				// try to create the objects
				o = new ActiveXObject(prefixes[i] + ".DomDocument");

				// Found it: save and return
				return DH.msDomDocumentPrefix = prefixes[i];
			}
			catch (ex) {
				// Not found, try next
			}
		}

		alert("Could not find an installed MSXML parser");
	},

	/** Cross-browser create XML DOM document. */
	createXmlDocument: function () {
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

			// MS IE case
			if (window.ActiveXObject) {
				return new ActiveXObject(DH.getMSDomDocumentPrefix() + ".DomDocument");
			}
		}
		catch (ex) {
		}
		alert("Sorry, your browser does not support XmlDocument objects");
	},
/******* END XML DOM Functions. *************/

	/**
	 * Escape the given string chacters that correspond to the five predefined XML entities
	 * @param s the string to escape
	 */
	escape : function(s){
		return s.replace(/&/g, "&amp;")
			.replace(/</g, "&lt;")
			.replace(/>/g, "&gt;")
			.replace(/"/g, "&quot;")
			.replace(/'/g, "&apos;");
	},


/******* XMLHTTPrequest (XHR) functions.  **********/

// Get XML doc from server
// On response  optional callback fun is called with optional user data.
	getXML: function(url, callback) {
		DH._xhrBusy();

		// Obtain XMLHttpRequest object
		var xmlhttp = new XMLHttpRequest();
		if (!xmlhttp || xmlhttp == null) {
			alert('No browser XMLHttpRequest (AJAX) support');
			return;
		}

		// Setup optional async response handling via callback
		var cb = callback;
		var async = false;

		if (cb) {
			// Async mode
			async = true;
			xmlhttp.onreadystatechange = function() {
				if (xmlhttp.readyState == 4) {
					if (xmlhttp.status == 200) {
						// Processing statements go here...
						cb(xmlhttp.responseXML);
						DH._xhrReady();
					} else {
						alert('Problem retrieving XML data:\n' + xmlhttp.statusText);
					}
				}
			};
		}


		// Open URL
		xmlhttp.open('GET', url, async);

		// Send XML to KW server
		xmlhttp.send(null);

		if (!cb) {
			DH._xhrReady();
			if (xmlhttp.status != 200) {
				alert('Problem retrieving XML data:\n' + xmlhttp.statusText);
				return null;
			}
			// Sync mode (no callback)
			return xmlhttp.responseXML;
		}
	}
	,

// Post XML doc to server
// On response  optional callback fun is called with optional XML response doc.
	postXML: function(url, doc, callback) {
		DH._xhrBusy();

		// Obtain XMLHttpRequest object
		var xmlhttp = new XMLHttpRequest();
		if (!xmlhttp || xmlhttp == null) {
			alert('No browser XMLHttpRequest (AJAX) support');
			return;
		}

		// Setup optional async response handling via callback
		var cb = callback;
		var async = false;

		if (cb) {
			// Async mode
			async = true;
			xmlhttp.onreadystatechange = function() {
				if (xmlhttp.readyState == 4) {
					if (xmlhttp.status == 200) {
						// Processing statements go here...
						cb(xmlhttp.responseXML);
						DH._xhrReady();
					} else {
						alert('Problem retrieving XML data using async POST:\n' + xmlhttp.statusText);
					}
				}
			};
		}


		// Open URL
		xmlhttp.open('POST', url, async);
		// xmlhttp.setRequestHeader("Content-Type", "text/xml;charset=utf-8");

		// POST XML to KW server
		// Safari needs just doc, others support doc.xml
		if (this.isSafari) {
			xmlhttp.send(doc);
		} else {
			xmlhttp.send(doc.xml);
		}

		if (!cb) {
			DH._xhrReady();
			if (xmlhttp.status != 200) {
				alert('Problem retrieving XML data using async POST:\n' + xmlhttp.statusText);
				return null;
			}
			// Sync mode (no callback)
			return xmlhttp.responseXML;
		}
	}
	,

// get content of URL from server
	getURL: function(url, callback) {

		// Obtain XMLHttpRequest object
		var xmlhttp = new XMLHttpRequest();
		if (!xmlhttp || xmlhttp == null) {
			alert('No browser XMLHttpRequest (AJAX) support');
			return;
		}

		// Setup response handling
		var cb = callback;
		var async = false;

		if (cb) {
			// Async mode
			async = true;
			xmlhttp.onreadystatechange = function() {
				DH._xhrReady();
				if (xmlhttp.readyState == 4) {
					if (xmlhttp.status == 200) {
						// Processing statements go here...
						cb(xmlhttp.responseText);
					} else {
						alert('Problem retrieving URL data (' + xmlhttp.status + ')\n' + xmlhttp.statusText);
					}
				}
			};
		}

		DH._xhrBusy();

		// Open URL
		xmlhttp.open('GET', url, async);

		// Send XML to KW server
		xmlhttp.send(null);


		if (!cb) {
			DH._xhrReady();
			if (xmlhttp.status != 200) {
				alert('Problem retrieving URL data:\n' + xmlhttp.statusText);
				return null;
			}

			// Sync mode (no callback)
			return xmlhttp.responseText;
		}
	}
	,

/** Interceptor functions , replace to use these. */
	_xhrBusy: function() {
	},

/** Interceptor functions , replace to use these. */
	_xhrReady: function(httpStatus) {
	},

/**
 * Dynamically load a script file if it has not already been loaded.
 * @param path path of the script relative to this file.
 */
	include: function(url) {
		var fullPath = DH.getBaseDir() + '/' + url;
		if (!document.getElementById(url)) {
			var script = document.createElement('script');
			script.defer = false;
			//not sure of effect of this?
			script.type = "text/javascript";
			script.src = fullPath;
			DH.debug(false, 'jspath', fullPath);
			script.id = url;
			document.getElementsByTagName('head')[0].appendChild(script);
			DH.includes.push(script);
		}
	},

// Position an object at a specific pixel coordinate
	setObjectXY: function(obj, x, y) {
		var theObj = DH.getStyleObject(obj);
		if (theObj != null) {
			// equalize incorrect numeric value type
			var units = (typeof theObj.left == "string") ? 'px' : 0;
			if (x) theObj.left = x + units;
			if (y) theObj.top = y + units;
		}
	}
	,

// Set object width and height
	setObjectWH: function(obj, w, h) {
		var theObj = DH.getStyleObject(obj);
		if (theObj != null) {
			// equalize incorrect numeric value type
			var units = (typeof theObj.left == "string") ? 'px' : 0;
			if (w) theObj.width = w + units;

			if (h) theObj.height = h + units;
		}
	}
	,

// Set object bounding box
	setObjectXYWH: function(obj, x, y, w, h) {
		DH.setObjectXY(obj, x, y);
		DH.setObjectWH(obj, w, h);
	}
	,

// Position an object at a specific pixel coordinate
	shiftTo: function(obj, x, y) {
		DH.setObjectXY(obj, x, y);
	}
	,

// Position an object at a specific pixel coordinate
	shiftTo: function(obj, x, y) {
		var theObj = DH.getStyleObject(obj);
		if (theObj != null) {
			// equalize incorrect numeric value type
			var units = (typeof theObj.left == "string") ? 'px' : 0;
			theObj.left = x + units;
			theObj.top = y + units;
		}
	}
	,

// Move an object by x and/or y pixels
	shiftBy: function(obj, deltaX, deltaY) {
		var theObj = DH.getStyleObject(obj);
		if (theObj != null) {
			// equalize incorrect numeric value type
			var units = (typeof theObj.left == "string") ? 'px' : 0;
			// TODO: does not seem to work...
			theObj.left = (parseInt(theObj.left) + deltaX) + units;
			theObj.top = (parseInt(theObj.top) + deltaY) + units;
		}
	}
	,

// Set the background color of an object
	setBGColor: function(obj, color) {
		var theObj = DH.getStyleObject(obj);
		if (theObj != null) {
			theObj.backgroundColor = color;
		}
	}
	,


// Write text into a layer object.
	setHTML: function(obj, html) {
		var theObj = DH.getObject(obj);
		if (theObj != null) {
			theObj.innerHTML = html;
		}
	}
	,

/** Add HTML to element */
	addHTML: function(obj, html) {
		var theObj = DH.getObject(obj);
		if (theObj != null) {
			theObj.innerHTML += html;
		}
	}
	,

/** Set opacity for element, val 0..1.  */
	setOpacity: function(obj, val) {
		var o = DH.getObject(obj);
		if (o != null) {
			// sniff browser-specific setting
			if (typeof(o.style.filter) == 'string') {
				o.style.filter = 'alpha(opacity:' + val * 100 + ')';
			}
			if (typeof(o.style.KHTMLOpacity) == 'string') {
				o.style.KHTMLOpacity = val;
			}
			if (typeof(o.style.MozOpacity) == 'string') {
				o.style.MozOpacity = val;
			}
			if (typeof(o.style.opacity) == 'string') {
				o.style.opacity = val;
			}
		}
	}
	,

// Set the z-order of an object
	setZIndex: function(obj, zOrder) {
		var theObj = DH.getStyleObject(obj);
		if (theObj != null) {
			theObj.zIndex = zOrder;
		}
	}
	,

// Set the visibility of an object to visible
	show: function(obj) {
		var theObj = DH.getStyleObject(obj);
		if (theObj != null) {
			theObj.visibility = "visible";
		}
	}
	,

// Set the visibility of an object to visible
	isVisible: function(obj) {
		var theObj = DH.getStyleObject(obj);
		if (theObj == null) {
			return false;
		}
		return theObj.visibility == "visible" ? true : false;
	}
	,

// Set the visibility of an object to visible
	toggleVisibility: function(obj) {
		var theObj = DH.getStyleObject(obj);
		if (theObj != null) {
			theObj.visibility = (theObj.visibility == "visible") ? "hidden" : "visible";
		}
	}
	,

// Set the display style property of an object to visible
	toggleDisplay: function(obj) {
		var theObj = DH.getStyleObject(obj);
		if (theObj != null) {
			theObj.display = (theObj.display == "none") ? "block" : "none";
		}
	}
	,
// Set the display style property of an object to visible
	displayOn: function(obj) {
		var theObj = DH.getStyleObject(obj);
		if (theObj != null) {
			theObj.display = 'block';
		}
	}
	,
// Set the display style property of an object to invisible
	displayOff: function(obj) {
		var theObj = DH.getStyleObject(obj);
		if (theObj != null) {
			theObj.display = 'none';
		}
	}
	,

// Set the visibility of an object to hidden
	hide: function(obj) {
		var theObj = DH.getStyleObject(obj);
		if (theObj != null) {
			theObj.visibility = "hidden";
		}
	}
	,

/** CROSS-BROWSER DRAG AND DROP SUPPORT */
	dragging: false,
	dragTarget
			:
			null,
	dragStartX
			:
			0,
	dragStartY
			:
			0,

// Register object for dragging
// @parm callback, called during dragging
	dragEnable
			:
			function (target, onStart, onDrag, onEnd) {
				// Remember client callback function
				target.onStart = onStart;
				target.onDrag = onDrag;
				target.onEnd = onEnd;
				DH.addEvent(target, 'mousedown', DH._dragStart, false);
			}
	,

// Unregister object for dragging
	dragDisable: function (target) {
		target.onStart = null;
		target.onDrag = null;
		target.onEnd = null;

		DH.removeEvent(target, 'mousedown', DH._dragStart);
	}
	,

// (internal) called when dragging starts
	_dragStart: function (e) {
		// Set dragging state
		DH.dragging = true;
		DH.dragTarget = DH.getEventTarget(e);
		e = DH.getEvent(e);
		DH.dragStartX = DH.getEventX(e);
		DH.dragStartY = DH.getEventY(e);

		// Global events during dragging
		DH.addEvent(document, 'mousemove', DH._dragMove, false);
		DH.addEvent(document, 'mouseup', DH._dragEnd, false);

		// Optional call client callback
		if (DH.dragTarget.onStart != null) {
			DH.dragTarget.onStart(DH.dragTarget, DH.dragStartX, DH.dragStartY);
		}

	}
	,

// (internal) called when dragged
	_dragMove: function (e) {
		if (DH.dragging == true) {
			var e = DH.getEvent(e);

			// Current x,y of mouse
			var ex = DH.getEventX(e);
			var ey = DH.getEventY(e);

			// Optional call client callback
			if (DH.dragTarget.onDrag != null) {
				DH.dragTarget.onDrag(DH.dragTarget, ex, ey, ex - DH.dragStartX, ey - DH.dragStartY);
			}

			// Remember last x,y
			DH.dragStartX = ex;
			DH.dragStartY = ey;
			return true;
		} else {
			return false;
		}
	}
	,

// (internal) called when dragging ends
	_dragEnd: function (e) {
		// Optional call client callback
		var e = DH.getEvent(e);
		if (DH.dragTarget.onEnd != null) {
			DH.dragTarget.onEnd(DH.dragTarget, DH.getEventX(e), DH.getEventY(e));
		}

		DH.dragging = false;
		DH.dragTarget = null;
		DH.cancelEvent(e);
		DH.removeEvent(document, 'mousemove', DH._dragMove);
		DH.removeEvent(document, 'mouseup', DH._dragEnd);

		return false;
	}

}

DH.addEvent(window, 'load', DH.init, false);
