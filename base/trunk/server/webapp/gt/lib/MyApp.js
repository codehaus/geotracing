/*
 * Traceland app.
 *
 * PURPOSE
 * Library representing the traceland app. All starts here.
 *
 * THIS IS THE USER APP. YOU NEED TO REPLACE THIS FILE
 * WITH YOUR SPECIFIC APP FUNCTIONS.
 *
 * Author: Just van den Broecke
 * $Id$
 */

// The TraceLand application functions
// Mainly overrules GTApp.js functions

var MYAPP = {
/* Title to appear on top browser. */
	WINDOW_TITLE: 'GeoTracing - TraceLand',
	DOC_TITLE: 'TraceLand',

	init: function() {
		// Overrule GTApp.js functions here
		GTAPP.createMap = MYAPP.createMap;
	},

	start: function() {
		GTAPP.mShowHelp("content/message.html");
    },

// Called in GTAPP.init() (see overload above)
	createMap: function() {
		GTAPP.blinkStatus('Creating map...');

		// Map WMS URLs
		var WMS_URL_BLACK = 'img/blacksquare.jpg?';
		var WMS_URL_GREY = 'img/greysquare.jpg?';
		var WMS_URL_STREETS = 'http://www.n8spel.nl/gt/geodan-streets.jsp?';
		var WMS_URL_TOP = 'http://www.n8spel.nl/gt/geodan-top.jsp?';
		var WMS_URL_TOPDAG = 'http://www.n8spel.nl/gt/geodan-topdag.jsp?';
		var WMS_URL_TOPNACHT = 'http://www.n8spel.nl//gt/geodan-topnacht.jsp?';
		var WMS_URL_CIVIC = 'http://maps.civicactions.net//cgi/mapserv.cgi?';
		var WMS_URL_NASA = 'http://wms.jpl.nasa.gov/wms.cgi?';

		// Create Google MapSpecs (see gmap-wms.js)
		var G_MAP_BLACK = createWMSSpec(WMS_URL_BLACK, "Night", "Night", "fakelayer", "fakedef", "image/jpeg", "1.1.1");
		var G_MAP_GREY = createWMSSpec(WMS_URL_GREY, "Blank", "Blank", "bl", "bla", "image/jpeg", "1.1.1");
		var G_MAP_STREETS = createWMSSpec(WMS_URL_STREETS, "Streets", "Streets", "s1", "s2", "image/jpeg", "1.1.1");
		var G_MAP_TOP = createWMSSpec(WMS_URL_TOP, "Topo", "Topo", "t1", "t2", "image/jpeg", "1.1.1");
		var G_MAP_TOPDAG = createWMSSpec(WMS_URL_TOPDAG, "Day", "Day", "t3", "t4", "image/jpeg", "1.1.1");
		var G_MAP_TOPNACHT = createWMSSpec(WMS_URL_TOPNACHT, "Night", "Night", "t5", "t6", "image/jpeg", "1.1.1");
		var G_MAP_NASA = createWMSSpec(WMS_URL_NASA, 'NASA', 'global_mosaic_base', 'global_mosaic_base', 'pseudo', "image/jpeg", "1.1.1");

		// Add map specs to app (see also menu in index.jsp)
		//GMAP.addMapType('topdag', G_MAP_TOPDAG);
		//GMAP.addMapType('topnacht', G_MAP_TOPNACHT);
		//GMAP.addMapType('nasa', G_MAP_NASA);

		GMAP.addMapType('streets', G_NORMAL_MAP);
		GMAP.addMapType('satellite', G_SATELLITE_MAP);
		GMAP.addMapType('hybrid', G_HYBRID_MAP);
		GMAP.addMapType('blanc', G_MAP_GREY);

		// Create the Google Map
		GMAP.createGMap('map');

		// map.addControl(new GMapTypeControl());
		GMAP.map.addControl(new GOverviewMapControl());
		GMAP.map.addControl(new GLargeMapControl());
		GMAP.map.addControl(new GScaleControl());

		GMAP.map.enableContinuousZoom();
		GMAP.map.enableDoubleClickZoom();

		// Set map parm defaults (may be overridden by page parms in GMAP.showMap())
		GMAP.setDefaultMapParms(new GLatLng(52.37261, 4.900435), 10, 'satellite');

		// Show the map
		GMAP.showMap();
		GTAPP.showStatus('Map created');
	}

}

