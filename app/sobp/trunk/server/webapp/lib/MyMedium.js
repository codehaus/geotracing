// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

// $Id$

/**
 * Location-based medium.
 * @extends Medium
 * @constructor
 */
function MyMedium(id, name, desc, type, mime, time, lon, lat) {
	Medium.apply(this, new Array(id, name, desc, type, mime, time, lon, lat));

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
}


