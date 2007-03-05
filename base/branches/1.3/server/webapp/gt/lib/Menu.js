// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

// $Id$

DH.include('Widget.js');

/**
 * Drop-down menu .
 *
 * @extends Widget
 * @constructor
 */
function Menu(anId) {
	Widget.apply(this, new Array(anId));

	/** Replace menu item. */
	this.replaceItem = function(id, name, fn, arg) {
		var li = DH.getObject(id);
		if (!li) {
			return;
		}

		var a = li.getElementsByTagName('a')[0];
		a.innerHTML = name;
		if (fn) {
			a.setAttribute('fn', fn);
			if (arg) {
				a.setAttribute('arg', arg);
			}
		}
	}

	/** Replace menu item. */
	this.replaceItemText = function(id, text) {
		var li = DH.getObject(id);
		if (!li) {
			return;
		}

		var a = li.getElementsByTagName('a')[0];
		a.innerHTML = text;
	}

	/** Remove menu item. */
	this.removeItem = function(id) {
		var li = DH.getObject(id);
		if (!li) {
			return;
		}
		this._unbindLink(li.getElementsByTagName('a')[0]);
		li.parentNode.removeChild(li);
		delete li;
	}


	// Item in menu clicked
	this._onSelect = function (e) {
		// Get callback function name
		var anchor = DH.getEventTarget(e);
		DH.cancelEvent(e);

		var fn = anchor.getAttribute('fn');
		if (fn) {
			var arg = anchor.getAttribute('arg');

			// Call handler function with/without arg
			if (!arg) {
				eval(fn + '()');
			} else {
				eval(fn + '("' + arg + '")');
			}
		}
	}

	/** Bind anchor to our select callback fun. */
	this._bindLink = function(link) {
		var onSelect = this._onSelect;
		DH.addEvent(link, 'click', onSelect, false);
	}

	/** Bind anchor to our select callback fun. */
	this._unbindLink = function(link) {
		var onSelect = this._onSelect;
		DH.removeEvent(link, 'click', onSelect);
	}

	/** Fix IE hover */
	this._fixIE = function(li) {
		li.onmouseover = function() {
			this.className += " sfhover";
		}

		li.onmouseout = function() {
			this.className = this.className.replace(new RegExp(" sfhover\\b"), "");
		}
	}

	var links = this.getContainer().getElementsByTagName('a');
	for (var i = 0; i < links.length; i++) {
		this._bindLink(links[i]);
	}

	// MS IE &$@%
	if (DH.isIE == true) {
		var sfEls = this.getContainer().getElementsByTagName("LI");
		for (var i = 0; i < sfEls.length; i++) {
			this._fixIE(sfEls[i]);
		}
	}
}
