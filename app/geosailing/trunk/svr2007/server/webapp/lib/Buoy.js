// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Mange drawing of buoys (boeien).
 *
 *
 * Author: Just van den Broecke
 * $Id$
 */

// This file contains specific app functions
var BUOY = {
	buoys: new Array(),
	icon: null,

	init: function() {
		var icon = new GIcon();
		icon.image = "http://labs.google.com/ridefinder/images/mm_20_red.png";
		icon.shadow = "http://labs.google.com/ridefinder/images/mm_20_shadow.png";
		icon.iconSize = new GSize(12, 20);
		icon.shadowSize = new GSize(22, 20);
		icon.iconAnchor = new GPoint(6, 20);
		icon.infoWindowAnchor = new GPoint(5, 1);
		BUOY.icon = icon;

		new Buoy("Boei", "VL5", 5.086067, 52.99828);
		new Buoy("Boei", "ZS13", 5.84132, 53.4155867);
		new Buoy("Boei", "BS20", 5.306925, 53.19933);
		new Buoy("Haveningang", "Vlieland", 5.0985, 53.294667);
		new Buoy("Haveningang", "Harlingen", 5.4038333, 53.1765833);
	},

	show: function() {
		for (var i = 0; i < BUOY.buoys.length; i++) {
			BUOY.buoys[i].show();
		}
	},

	hide: function() {
		for (var i = 0; i < BUOY.buoys.length; i++) {
			BUOY.buoys[i].hide();
		}
	}
}

/**
 * Location-based item on map.
 * GIS-folks call that a "feature".
 *
 * @constructor
 */
function Buoy(type, name, lon, lat) {
	this.name = name;
	this.type = type;
	this.point = new GLatLng(lat, lon);
	this.marker = new GMarker(this.point, BUOY.icon);

	var self = this;
	GEvent.addListener(this.marker, "click", function() {
		self.marker.openInfoWindowHtml(self.type + " <b>" + self.name + "</b> op " + lon + ', ' + lat);
	});

	BUOY.buoys.push(this);

	// Displays feature in Panel
	this.show = function() {
		GMAP.map.addOverlay(this.marker);
	}

	// Displays feature in Panel
	this.hide = function() {
		GMAP.map.removeOverlay(this.marker);
	}
}