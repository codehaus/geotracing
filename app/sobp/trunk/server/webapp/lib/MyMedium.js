// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

// $Id: MyMedium.js,v 1.5 2006-08-11 18:47:43 just Exp $

/**
 * Location-based medium.
 * @extends Medium
 * @constructor
 */
function MyMedium(id, name, desc, type, mime, time, lon, lat) {
	Medium.apply(this, new Array(id, name, desc, type, mime, time, lon, lat));

	// Displays medium in pop-up info window
	this.display = function() {
		if (this.kind == 'image') {
			MYAPP.displayImageInfo(this.id);
		}
	}
}


