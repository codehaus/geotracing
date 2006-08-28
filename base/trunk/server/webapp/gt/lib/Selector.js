// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.


DH.include('Widget.js');

/**
 * Drop-down (ala combo box) Selector.
 *
 * @extends Widget
 * @constructor
 * $Id: Selector.js,v 1.7 2006-07-20 14:21:41 just Exp $
 */
function Selector(title, id, callback) {
	Widget.apply(this, new Array(id));

	// Setting the visibility to hidden
	this.addOption = function(name, label, value) {
		var option = document.createElement('option');
		option.id = this.getId() + 'o' + Widget.curOptionId++;
		option.name = name;
		option.value = value;

		// Set both label and child text with label (defeats Safari bug..)
		option.label = label; // label;
		var text = document.createTextNode(label);
		option.appendChild(text)
		this.select.appendChild(option);
	}

	// Cleart out all options
	this.clear = function() {
		//var options = this.select.getElementsByTagName('option');
		//var count = options.length;
		while (this.select.hasChildNodes() == true) {
			this.select.removeChild(this.select.childNodes[0])
		}
		this.addOption('title', this.title, this.title);
	}

	/** Remove form. */
	this.remove = function() {
		this.hide();
		this.clear();
		document.body.removeChild(this.form);
	}

	// Set the callback function
	this.setCallback = function(fun) {
		this.callback = fun;
	}

	this.callback = callback;

	var form = document.createElement('form');
	form.className = 'sel-form';
	form.id = this.getId();
	var select = document.createElement('select');
	select.className = 'sel-select';
	select.id = select.className + this.getId();
	form.appendChild(select);
	document.body.appendChild(form);

	this.form = form;
	this.select = select;
	this.title = title;

	this.addOption('title', this.title, this.title);
	var selector = this;

	this.onSelect = function(e) {

		var option = selector.select.options[selector.select.selectedIndex];

		// First is title
		if (selector.select.selectedIndex != 0) {
			// selector.callback(option.name, option.childNodes.item(0).nodeValue, option.label);
			selector.callback(option.name, option.childNodes.item(0).nodeValue, option.value);
		}

		DH.cancelEvent(e);
	}

	DH.addEvent(this.select, 'change', selector.onSelect, false);
}

