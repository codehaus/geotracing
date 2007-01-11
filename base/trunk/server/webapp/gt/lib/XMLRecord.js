// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Class to represent KeyWorx Oase Record
 *
 * Author: Just van den Broecke
 * $Id$
 */

//
function Record(xmlElement) {
	this.xml = xmlElement;
	this.id = -1;
	if (this.xml.attributes && this.xml.attributes[0]) {
		this.id = this.xml.attributes[0].value;
	}

	// Get any text/numeric field by name
	this.getField = function(name) {
		var field = this.getXMLField(name);
		if (field != null && field.childNodes.length > 0) {
			return field.childNodes[0].nodeValue;
		} else {
			return null;
		}
	}

	// Get XML field by name; returns XML DOM Element
	this.getXMLField = function(name) {
		var list = this.xml.getElementsByTagName(name);
		if (list && list.length > 0) {
			return list[0];
		} else {
			// alert('cannot get XMLField ' + name);
			return null;
		}
	}

	// Debug: render HTML to show record content.
	this.toHTML = function() {
		var html = '<pre>';
		var xml = this.xml;
		html += (xml.tagName + ' id=' + this.id + '\n');

		var fields = xml.childNodes;
		var fieldCount = fields.length;
		var nextField;
		for (j = 0; j < fieldCount; j++) {
			nextField = fields[j];
			html += (nextField.tagName + '=');

			if (nextField.childNodes[0]) {
				html += nextField.childNodes[0].nodeValue;
			}

			html += '\n';
		}

		html += '\n</pre>';
		return html;
	}

}

