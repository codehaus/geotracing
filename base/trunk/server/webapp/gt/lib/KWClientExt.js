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
 * Example:
 * KW.init(MY.rspCallback, My.nrspCallback, 60, '/basic');
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

/** Comment handler functions. */
KW.CMT = {

	/**
	 * Add comment.
	 * @param callback - user callback function or null
	 */
	add: function(callback, itemId, text) {
		var req = KW.createRequest('cmt-insert-req');
		KW.UTIL.addTextElement(req, 'target', itemId);
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
	 * @param id - id of comment
	 */
	del: function(callback, id) {
		var req = KW.createRequest('cmt-delete-req');
		KW.UTIL.setAttr(req, 'id', id);
		KW.utopia(req, callback);
	}
}

/** Tagging handler functions. */
KW.TAG = {

	/** Add tags for specified items. */
	add: function(callback, itemIds, tags) {
	    // <tagging-tag-req items="${item1id},${item2id},${item3id}" tags="tag1 tag2 'tag 3' 'tag 4' tag5 tag6" mode="add"/>
		var req = KW.createRequest('tagging-tag-req ');
		KW.UTIL.setAttr(req, 'items', itemIds);
		KW.UTIL.setAttr(req, 'tags', tags);
		KW.UTIL.setAttr(req, 'mode', 'add');
		KW.utopia(req, callback);
	},

	/** Replace all tags for specified items. */
	replace: function(callback, itemIds, tags) {
	    // <tagging-tag-req items="${item1id},${item2id},${item3id}" tags="tag1 tag2 'tag 3' 'tag 4' tag5 tag6" mode="replace"/>
		var req = KW.createRequest('tagging-tag-req ');
		KW.UTIL.setAttr(req, 'items', itemIds);
		KW.UTIL.setAttr(req, 'tags', tags);
		KW.UTIL.setAttr(req, 'mode', 'replace');
		KW.utopia(req, callback);
	},

	/** Remove tags for specified items. */
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

	create: function(callback, name) {
		var req = KW.createRequest('t-trk-create-req');
		KW.UTIL.setOptAttr(req, 'name', name);
		KW.utopia(req, callback);
	},

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

	suspend: function(callback, id) {
		var req = KW.createRequest('t-trk-suspend-req');
		KW.UTIL.settAttr(req, 'id', id);
		KW.utopia(req, callback);
	},

	resume: function(callback, id) {
		var req = KW.createRequest('t-trk-resume-req');
		KW.UTIL.settAttr(req, 'id', id);
		KW.utopia(req, callback);
	},

	del: function(callback, id) {
		var req = KW.createRequest('t-trk-delete-req');
		KW.UTIL.settAttr(req, 'id', id);
		KW.utopia(req, callback);
	}
}

/** Utility functions, mainly for manipulating XML DOM. */
KW.UTIL = {

/** Create XML element with text content and add to parent */
	addTextElement: function(parent, name, text) {
		var doc = parent.ownerDocument;

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
			KW.UTIL.addTextElement(node, name, text);
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
