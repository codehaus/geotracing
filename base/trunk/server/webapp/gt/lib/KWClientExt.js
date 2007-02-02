/*
 * KWClient extension functions, mainly Utopia request wrappers.
 *
 * EXTERNALS
 * KWClient.js
 *
 * PURPOSE
 * Provide direct usable functions for Utopia Handler requests in Ajax.
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
 * $Id$
 */

/**
 * Comment handler functions.
 * The commenting protocol is described in
 * http://svn.codehaus.org/geotracing/base/trunk/doc/schema/comment-protocol.xml
 */
KW.CMT = {

	/**
	 * Add comment.
	 * @param callback - user callback function or null
	 * @param targetId - id of item to be commented
	 * @param text - comment text
	 */
	add: function(callback, targetId, text) {
		var req = KW.createRequest('cmt-insert-req');
		KW.UTIL.addTextElement(req, 'target', targetId);
		KW.UTIL.addTextElement(req, 'content', text);
		KW.utopia(req, callback);
	},

	/**
	 * Mark one or more comments as having been read.
	 *
	 * @param callback - user callback function or null
	 * @param commentId - id of comment (opt)
	 * @param targetId - target id of comment (opt)
	 */
	markRead: function(callback, commentId, targetId) {
		var req = KW.createRequest('cmt-update-state-req');
		KW.UTIL.setAttr(req, 'state', 2);
		KW.UTIL.setOptAttr(req, 'id', commentId);
		KW.UTIL.setOptAttr(req, 'target', targetId);
		KW.utopia(req, callback);
	},

	/**
	 * Mark one or more comments on target as having been read.
	 * @param callback - user callback function or null
	 * @param targetId - target id of comment
	 */
	markReadForTarget: function(callback, targetId) {
		KW.CMT.markRead(callback, null, targetId);
	},

	/**
	 * Mark a comment as having been read.
	 * @param callback - user callback function or null
	 * @param commentId - id of comment
	 */
	markReadForComment: function(callback, commentId) {
		KW.CMT.markRead(callback, commentId, null);
	},

	/**
	 * Delete comment.
	 * @param callback - user callback function or null
	 * @param commentId - id of comment
	 */
	del: function(callback, commentId) {
		var req = KW.createRequest('cmt-delete-req');
		KW.UTIL.setAttr(req, 'id', commentId);
		KW.utopia(req, callback);
	}
}

/** Medium handler functions. */
KW.MEDIA = {
	iframeCnt: 0,

	/**
     * Upload medium using a form.
     *
	 * Form requires: inputs "file" (type file) and "name" and "description", callback is
	 * usually medium-insert-rsp with id or if fails medium-insert-nrsp.
     *
	 * Example form:
	 * <form id="addmediumform" name="addmediumform" enctype="multipart/form-data"  method="post" action="/gt/media.srv" >
	 *	<input name="file" id="file" type="file"  />
	 *	<input name="name" id="description" value="null" />
	 *	<input name="description" id="name" value="null" />
	 * 	<a href="#" onclick="return uploadMedium();">[Upload]</a>
	 * </form>
	 * and in uploadMedium():
	 *   KW.MEDIA.upload(KWCT.onUploadMediumRsp, DH.getObject('addmediumform'));
	 */
	upload: function(callback, form) {
		// Add extra input elements required by server
		var xmlrsp = document.createElement('input');
		xmlrsp.name = 'xmlrsp';
		xmlrsp.type = 'hidden';
		xmlrsp.value = 'true';
		form.appendChild(xmlrsp);

		var agentkey = document.createElement('input');
		agentkey.name = 'agentkey';
		agentkey.type = 'hidden';
		agentkey.value = KW.agentKey;
		form.appendChild(agentkey);

		// Set target to hidden IFrame
		// Create separate IFrame per upload.
		var iframeId = 'kwrspframe' + KW.MEDIA.iframeCnt;
		form.target = iframeId;

		// Optional name (required by server).
		if (!form.name.value) {
			form.name.value = 'unnamed';
		}

		// Create or clear responseframe

		KW.MEDIA._createRspIFrame(iframeId);

		// Send form to server by POST
		form.submit();

		// Start checking responseframe
		KW.MEDIA._checkRspIFrame(callback, iframeId);
		KW.MEDIA.iframeCnt++;
		return false;
	},

	/**
	 * Delete medium by id.
	 * @param callback - user callback function or null
	 * @param id the medium id
	 */
	del: function(callback, id) {
		var req = KW.createRequest('medium-delete-req');
		KW.UTIL.setAttr(req, 'id', id);
		KW.utopia(req, callback);
	},

	/**
	 * Update medium.
	 *
	 * @param callback - user callback function or null
	 * @param id the medium id
	 * @param name the medium name or null (opt)
	 * @param desc the medium description or null (opt)
	 */
	update: function(callback, id, name, desc) {
		var req = KW.createRequest('medium-update-req');
		KW.UTIL.setAttr(req, 'id', id);
		var elm = req.createElement('medium');
		KW.UTIL.addOptTextElement(elm, 'name', name);
		KW.UTIL.addOptTextElement(elm, 'description', desc);
		req.documentElement.appendChild(elm);

		KW.utopia(req, callback);
	},


	_checkRspIFrame: function(callback, iframeId) {
		var iframe = window.frames[iframeId];
		if (!iframe) {
			iframe = DH.getObject(iframeId);
			if (!iframe) {
				alert('cannot get rspFrame: ' + iframeId);
				return;
			}
		}

		var iframeDoc;

		if (iframe.contentDocument) {
			// For NS6
			iframeDoc = iframe.contentDocument;
		} else if (iframe.contentWindow) {
			// For IE5.5 and IE6
			iframeDoc = iframe.contentWindow.document;
		} else if (iframe.document) {
			// For IE5
			iframeDoc = iframe.document;
		}


		if (iframeDoc && iframeDoc.documentElement && iframeDoc.documentElement.tagName.indexOf('rsp') != -1) {
			// Got document with response: send element in callback.
			callback(iframeDoc.documentElement);
		} else {
			// No iframe response document (yet) keep checking iframe content
			var f = function() {
				KW.MEDIA._checkRspIFrame(callback, iframeId);
			}

			// Problem with IE: response is loaded in iframe but as an HTML document
			if (iframeDoc && iframeDoc.documentElement && iframeDoc.documentElement.innerHTML.indexOf('rsp') != -1) {
				// KWCT.pr('_checkRspIFrame ih=' + DH.escape(iframeDoc.documentElement.innerHTML));
				/* var doc = (new DOMParser()).parseFromString(iframeDoc.documentElement.lastChild.innerHTML, "text/xml");
				var rspElm = doc.getElementsByTagName('medium-insert-rsp')[0];
				callback(rspElm);  */
                var id = iframeDoc.body.getElementsByTagName('B')[0].innerHTML;
				var rsp = KW.createRequest('medium-insert-rsp');
				rsp.documentElement.setAttribute('id', id);
				callback(rsp.documentElement);
				return;
			}
			setTimeout(f, 50);
		}
	},

	/** See also:
	 *  http://www.howtocreate.co.uk/tutorials/jsexamples/importingXML.html
	 *  http://sean.treadway.info/articles/2006/05/29/iframe-remoting-made-easy
	 */
	_createRspIFrame: function(iframeId) {
		// Create iframe (not sure why the DIV is required...)
		var iframeDiv = document.createElement('DIV');
		iframeDiv.style.visibility = 'hidden';
		iframeDiv.style.position = 'absolute';
		iframeDiv.style.top = '0px';
		iframeDiv.style.left = '0px';
		iframeDiv.style.width = '0px';
		iframeDiv.style.height = '0px';

		iframeDiv.innerHTML = '<iframe id="' + iframeId + '"  name="' + iframeId + '" style="width: 0px; height: 0px; border: 0px;"><\/iframe>';
		document.body.appendChild(iframeDiv);
		return true;
	}
}

/** Tagging handler functions. */
KW.TAG = {

	/**
	 * Add tags for specified items.
	 * @param callback - user callback function or null
	 * @param itemIds - id's (comma separated) of items to be tagged
	 * @param tags - tags to be added to items
	 */
	add: function(callback, itemIds, tags) {
	    // <tagging-tag-req items="123,345,567" tags="tag1 tag2 'tag 3' 'tag 4' tag5 tag6" mode="add"/>
		var req = KW.createRequest('tagging-tag-req ');
		KW.UTIL.setAttr(req, 'items', itemIds);
		KW.UTIL.setAttr(req, 'tags', tags);
		KW.UTIL.setAttr(req, 'mode', 'add');
		KW.utopia(req, callback);
	},

	/**
	 * Replace all tags for specified items.
	 * @param callback - user callback function or null
	 * @param itemIds - id's of items to be replace tags
	 * @param tags - tags to be replaced in items
	 */
	replace: function(callback, itemIds, tags) {
	    // <tagging-tag-req items="${item1id},${item2id},${item3id}" tags="tag1 tag2 'tag 3' 'tag 4' tag5 tag6" mode="replace"/>
		var req = KW.createRequest('tagging-tag-req ');
		KW.UTIL.setAttr(req, 'items', itemIds);
		KW.UTIL.setAttr(req, 'tags', tags);
		KW.UTIL.setAttr(req, 'mode', 'replace');
		KW.utopia(req, callback);
	},

	/**
	 * Remove tags for specified items.
	 * @param callback - user callback function or null
	 * @param itemIds - id's of items to be replace tags
	 * @param tags - tags to be replaced in items
	 */
	del: function(callback, itemIds, tags) {
		// <tagging-untag-req items="${item1id},${item2id}" tags="tag1 'tag 3'"/>
		var req = KW.createRequest('tagging-untag-req ');
		KW.UTIL.setAttr(req, 'items', itemIds);
		KW.UTIL.setAttr(req, 'tags', tags);
		KW.utopia(req, callback);
	}
}

/** Track recording functions. */
KW.TRACK = {

	/**
	 * Create new track and make it active.
	 * @param callback - user callback function or null
	 * @param name - name of track (opt)
	 */
	create: function(callback, name) {
		var req = KW.createRequest('t-trk-create-req');
		KW.UTIL.setOptAttr(req, 'name', name);
		KW.utopia(req, callback);
	},

	/**
	 * Write point to track.
	 * @param callback - user callback function or null
	 * @param lon - longitude
	 * @param lat - latitude
	 * @param time - time in millis since 1.1.70 (opt)
	 * @param ele - elevation in meters (opt)
	 */
	write: function(callback, lon, lat, time, ele) {
		var req = KW.createRequest('t-trk-write-req');

		var pt = req.createElement('pt');
		KW.UTIL.setAttr(pt, 'lon', lon);
		KW.UTIL.setAttr(pt, 'lat', lat);
		KW.UTIL.setOptAttr(pt, 't', time);
		KW.UTIL.setOptAttr(pt, 'ele', ele);

		req.documentElement.appendChild(pt);
		KW.utopia(req, callback);
	},

	/**
	 * Suspend writing to track.
	 * @param callback - user callback function or null
	 * @param id - the track id (opt) if not specified active track is suspended
	 */
	suspend: function(callback, id) {
		var req = KW.createRequest('t-trk-suspend-req');
		KW.UTIL.settOptAttr(req, 'id', id);
		KW.utopia(req, callback);
	},

	/**
	 * Resume writing to track.
	 * @param callback - user callback function or null
	 * @param id - the track id (opt) if not specified active track is resumed
	 */
	resume: function(callback, id) {
		var req = KW.createRequest('t-trk-resume-req');
		KW.UTIL.settOptAttr(req, 'id', id);
		KW.utopia(req, callback);
	},

	/**
	 * Delete a track.
	 * @param callback - user callback function or null
	 * @param id - the track id (user must be owner)
	 */
	del: function(callback, id) {
		var req = KW.createRequest('t-trk-delete-req');
		KW.UTIL.settAttr(req, 'id', id);
		KW.utopia(req, callback);
	}
}

/** User profile management functions. */
KW.USER = {

	/**
	 * Update user profile.
	 * @param callback - user callback function or null
	 * @param profileObj - profile object, containing fields to be updated
	 */
	update: function(callback, profileObj) {
		var req = KW.createRequest('profile-update-req');
		addOptTextElement('firstname', profileObj.firstName);
		addOptTextElement('lastname', profileObj.lastName);
		addOptTextElement('email', profileObj.email);
		addOptTextElement('password', profileObj.password);
		addOptTextElement('mobilenr', profileObj.mobilenr);
		addOptTextElement('description', profileObj.description);
		addOptTextElement('visibility', profileObj.visibility);
		addOptTextElement('iconid', profileObj.iconid);
		KW.utopia(req, callback);
	}
}

/** Utility functions, mainly for manipulating XML DOM. */
KW.UTIL = {

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

	/** Create XML element with text content and add to parent */
	addTextElement: function(parent, name, text) {
		var doc = parent.ownerDocument;

		// Must have text
		if (!text || text == null) {
			alert('error: no text passed for element ' + name);
			return;
		}

		// We may pass a document: in that case we have the ownerDocument
		// already. Then parent is the main (document) Element.
		if (parent.nodeName == '#document') {
			doc = parent;
			parent = doc.documentElement;
		}

		var elm = doc.createElement(name);
		var textNode = doc.createTextNode(text);
		elm.appendChild(textNode);
		parent.appendChild(elm);
	},

	/** Add text element if text value present. */
	addOptTextElement: function(parent, name, text) {
		if (text && text != null) {
			KW.UTIL.addTextElement(parent, name, text);
		}
	},

	/** Set attribute in XML element. */
	setAttr: function(node, name, value) {
		// We may pass a document: in that case the node is the documentElement.
		if (node.nodeName == '#document') {
			node = node.documentElement;
		}

		if (!value || value == null) {
			alert('error: no value passed for attr ' + name);
			return;
		}
		node.setAttribute(name, value);
	},

	/** Set attribute in XML element if value present. */
	setOptAttr: function(node, name, value) {
		if (value && value != null) {
			KW.UTIL.setAttr(node, name, value);
		}
	}
}
