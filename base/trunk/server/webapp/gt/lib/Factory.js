// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

// $Id: Factory.js,v 1.2 2006-08-10 23:06:34 just Exp $

/**
 * Implements Factory design pattern.
 *
 * @constructor
 */
function Factory(theClassDefs) {
	/** Map of (name,className) pairs. */
	this.classDefs = [];

	// Optional init with map of classdefs.
	if (theClassDefs) {
		this.classDefs = theClassDefs;
	}

	/** Create object by name. */
	this.create = function(aName) {
		var className = this.classDefs[aName];
		if (!className) {
			alert('cannot find real class name for ' + aName);
			return null;
		}

		// Constructs object from real class name
		var obj = null;
		if (arguments.length > 1) {
			var argv = [];
			var argStr = ' ';
			for (var i = 1; i < arguments.length; i++) {
				argv[i-1] = arguments[i];
				argStr += 'argv[' + (i-1) + ']';
				if (i < arguments.length - 1) {
					argStr += ', ';
				}
			}
			// alert('arguments.length=' + arguments.length + ' argStr=' + argStr);
			obj = eval('new ' + className + '(' + argStr + ')');
		} else {
			obj = eval('new ' + className + '()');
		}

		return obj;
	}

	/** Returns class name for given name. */
	this.getClassDef = function(aName) {
		return this.classDefs[aName];
	}

	/** Binds a name to a class name. */
	this.setClassDef = function(aName, aClassName) {
		this.classDefs[aName] = aClassName;
	}
}




