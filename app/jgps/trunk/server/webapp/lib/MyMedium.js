// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

// $Id: MyMedium.js 51 2006-09-25 16:16:02Z just $

/**
 * Location-based medium.
 * @extends Medium
 * @constructor
 */
function MyMedium(id, name, desc, type, mime, time, lon, lat, subtype) {
	Medium.apply(this, new Array(id, name, desc, type, mime, time, lon, lat, subtype));

	// Shows icon on map
	this.getBGColor = function() {
		if (this.userName && this.userName != null) {
			var tracer = GTW.getTracer();
			if (!tracer) {
				tracer = GTW.createTracer(this.userName);
			}
			this.bgColor = tracer.getColor();
		}
		return this.bgColor;
	}

	this._displayDescr = function() {
		DH.setHTML('featuredesc', this.desc);
		GMAP.resize();
	}
}


