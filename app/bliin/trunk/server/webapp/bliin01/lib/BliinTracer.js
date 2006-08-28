// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/**
 * Represents GeoTracing user for Bliin app.
 *
 * Extends the standard Tracer class, overruling
 * standards functions.
 *
 * $Id: BliinTracer.js,v 1.8 2006-08-13 19:16:36 just Exp $
 */
function BliinTracer(name, color, iconURL, pt, time) {
	// Inherits from Tracer
	Tracer.apply(this, new Array(name, color, iconURL, pt, time));
	this.blinkIntervalShow = 3000;
	this.blinkIntervalHide = 400;


	// The continuous blinking
	this.blink = function() {
		DH.toggleVisibility(this.iconId);

		// JS Trick to have setTimeout() call our object method
		var blinkInterval = DH.isVisible(this.iconId) ? this.blinkIntervalShow : this.blinkIntervalHide
		var self = this;
		setTimeout(function() {
			self.blink();
		}, blinkInterval);
		//  DH.isVisible(this.iconId) ? 3000 : 400
	}

	/** Create Map icon. */
	this.createTLabel = function () {
		this.iconId = 'icon' + this.name;
		var html = '<img id="' + this.iconId + '" src="' + this.iconURL + '1.png" border="0" width="55" height="55" onload="DH.fixPNG(this)" />';

		// Setup TLabel object
		tl = new TLabel();
		tl.id = 'trclab' + this.name;
		tl.anchorLatLng = this.point;
		tl.anchorPoint = 'topLeft';
		tl.content = html;

		// To shift icon on exact lat/lon location (half size of icon)
		tl.markerOffset = new GSize(28, 28);

		return tl;
	}

	// Overrule: do nothing
	this.deleteTrack = function () {
	}

	// Overrule: do nothing
	this.newTrack = function (id, name) {
	}

	// Move Tracer to lon/lat location
	this.move = function(lon, lat, time) {

		// replace point
		pt = new GLatLng(lat, lon);
		if (time) {
			pt.time = new Number(time);
		}
		// Move TLabel
		this.setLocation(pt);

		// If we have a previous point calculate course and set directional icon
		if (this.lastPoint) {
			// Set icon based on where we are heading:
			// 1 S, 2 SW, 3 W, 4 NW, 5 N, 6 NE, 7 E, 8 SE
			var course = GMAP.heading(this.lastPoint.lat(), this.lastPoint.lng(), this.point.lat(), this.point.lng());

			// Course is degrees value between 0 (N) through 180 (S) to 360 (N)
			var iconId = (Math.round(course/45) +1) + 4;

			// Correct for icon id's: 5 (N) (see above)
			if (iconId > 8) {
				iconId = iconId - 8;
			}

			// replace image with directed icon image
			DH.getObject(this.iconId).src = this.iconURL + iconId + '.png';
			// Fix for IE
			DH.fixPNG(DH.getObject(this.iconId));
		}
	}

	this.openInfoWindow = function() {
		var html = '<b>name:</b> ' + this.name + '<p><img width="128" height="96" src="img/users/' + this.name + '.jpg" border="0"/>&nbsp;&nbsp;<a href="#" onclick="javascript:MYAPP.mShowMediaByUser(\'' + this.name + '\');return false\" >view media</a></p>';

		GMAP.map.openInfoWindowHtml(this.point, html);
		GMAP.map.panTo(this.point);
	}
}

