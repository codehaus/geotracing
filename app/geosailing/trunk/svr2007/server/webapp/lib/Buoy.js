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
		// icon.image = "http://labs.google.com/ridefinder/images/mm_20_red.png";
		icon.image = "http://www.geosailing.com/svr/img/iconen/boei.png";
		icon.shadow = "http://labs.google.com/ridefinder/images/mm_20_shadow.png";
		// icon.iconSize = new GSize(12, 20);
		icon.iconSize = new GSize(16, 17);
		icon.shadowSize = new GSize(22, 20);
		// icon.iconAnchor = new GPoint(6, 20);
		icon.iconAnchor = new GPoint(8, 17);
		icon.infoWindowAnchor = new GPoint(5, 1);
		BUOY.icon = icon;

		/*new Buoy("Boei", "VL5", 5.086067, 52.99828);
		new Buoy("Boei", "ZS13", 5.84132, 53.4155867);
		new Buoy("Boei", "BS20", 5.306925, 53.19933);
		new Buoy("Haveningang", "Vlieland", 5.0985, 53.294667);
		new Buoy("Haveningang", "Harlingen", 5.4038333, 53.1765833); */
		new Buoy("Boei", "KZ1", 5.333863333333333, 53.08188833333333);
		new Buoy("Boei", "BO7", 5.326841666666667, 53.081495);
		new Buoy("Boei", "BO1", 5.303786666666666, 53.08134666666667);
		new Buoy("Boei", "D20", 5.2595616666666665, 53.06307);
		new Buoy("Boei", "D8", 5.139278333333333, 53.03785333333333);
		new Buoy("Boei", "D2", 5.058933333333333, 53.04246666666667);
		new Buoy("Boei", "T27", 4.990338333333334, 53.06007833333334);
		new Buoy("Boei", "T23", 4.9309, 53.06007833333334);
		new Buoy("Boei", "OS1", 4.8611716666666664, 53.0388);
		new Buoy("Boei", "SO2", 5.025366666666667, 53.06272);
		new Buoy("Boei", "SO4", 5.047475, 53.06265333333333);
		new Buoy("Boei", "SO14", 5.120205, 53.085211666666666);
		new Buoy("Boei", "SO22", 5.179128333333333, 53.09179666666667);
		new Buoy("Boei", "SO34", 5.175445, 53.12715);
		new Buoy("Boei", "SO48", 5.164075, 53.15372833333333);
		new Buoy("Boei", "IN17/SO56", 5.1857283333333335, 53.16486333333334);
		new Buoy("Boei", "IN10", 5.147903333333334, 53.19921333333333);
		new Buoy("Boei", "VL5", 5.1650833333333335, 53.3064);
		new Buoy("Boei", "VL1", 5.146983333333333, 53.31647);
		new Buoy("Boei", "ZS13", 5.121861666666667, 53.31462);
		new Buoy("Boei", "ZS11/VS2", 5.099305, 53.31088);
		new Buoy("Boei", "VS6", 5.106995, 53.30506166666667);
		new Buoy("Boei", "VS14", 5.093616666666667, 53.293038333333335);
		new Buoy("Boei", "BS2", 5.178886666666667, 53.26657);
		new Buoy("Boei", "BS4", 5.1776333333333335, 53.24643833333333);
		new Buoy("Boei", "BS6", 5.18958, 53.237696666666665);
		new Buoy("Boei", "BS8", 5.20645, 53.233455);
		new Buoy("Boei", "BS11", 5.259925, 53.229753333333335);
		new Buoy("Boei", "BS13", 5.285846666666667, 53.222408333333334);
		new Buoy("Boei", "BS20", 5.306925, 53.19933);
		new Buoy("Boei", "BO44", 5.39805, 53.17675833333333);
		new Buoy("Cardinaal West", "VL8/WM1", 5.1843216666666665, 53.28971333333333);
		new Buoy("Boei", "WM6", 5.235713333333333, 53.301786666666665);
		new Buoy("Boei", "NM4/S21", 5.2575, 53.31699666666667);
		new Buoy("Boei", "SG15/S2", 5.195188333333333, 53.34153666666667);
		new Buoy("Boei", "BO18", 5.37108, 53.10505333333333);
		new Buoy("Cardinaal Noord", "BO9/KZ2", 5.338236666666667, 53.083666666666666);
		new Buoy("Haveningang", "Terschelling", 5.219, 53.35435);
		new Buoy("Haveningang", "Hindelopen", 5.402333333333333, 52.93666666666667);
		new Buoy("Haveningang", "Stavoren", 5.353083333333333, 52.88625);
		new Buoy("Haveningang", "Vlieland", 5.0985, 53.294666666666664);
		new Buoy("Haveningang", "Harlingen", 5.403833333333333, 53.17658333333333);
		new Buoy("Haveningang", "Texel-Waddenhaven/Oudeschild", 4.852983333333333, 53.03908333333333);
		new Buoy("Sluis", "Kornwerderzand", 5.3341666666666665, 53.07866666666666);
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
		self.marker.openInfoWindowHtml(self.type + " <b>" + self.name + "</b> <br/>lon,lat =  " + lon.toFixed(6) + ', ' + lat.toFixed(5) + ' (DD)');
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