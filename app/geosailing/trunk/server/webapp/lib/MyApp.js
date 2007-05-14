// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * My application.
 *
 * PURPOSE
 * Library representing the specific app. All starts here.
 *
 * Author: Just van den Broecke
 * $Id: MyApp.js,v 1.4 2006-06-06 13:56:38 just Exp $
 */

// This file contains specific app functions
var MYAPP = {
	WINDOW_TITLE: 'Schuttevaer Live',
	DOC_TITLE: 'Schuttevaer Live',

	init: function() {
		// Overrule GTApp.js functions here
		GTAPP.createMap = MYAPP.createMap;
		TRACER.BLINK_INTERVAL_SHOW = 1500;
		TRACER.BLINK_INTERVAL_HIDE = 400;
		TRACER.MARKER_OFFSET_X=10;
		TRACER.MARKER_OFFSET_Y=16;
		TRACER.SMOOTH_FACTOR=.025;
		GTW.TRACER_ICON_URL = 'img/sea-boat-red.gif';
	},

	start: function() {
		// GTAPP.mShowHelp("content/appabout.html");
		GTAPP.mLive();
	},

// Called in GTAPP.init() (see overload above)

	createMap: function() {
		GTAPP.blinkStatus('Creating map...');
		GMAP.addMapType('satellite', G_SATELLITE_TYPE);
		GMAP.addMapType('map', G_MAP_TYPE);
		GMAP.addMapType('hybrid', G_HYBRID_TYPE);

		// Create the Google Map
		GMAP.createGMap('map');

		// map.addControl(new GMapTypeControl());
		GMAP.map.addControl(new GOverviewMapControl());
		GMAP.map.addControl(new GLargeMapControl());
		GMAP.map.addControl(new GMapTypeControl());
		GMAP.map.addControl(new GScaleControl());

		GMAP.map.enableContinuousZoom();
		GMAP.map.enableDoubleClickZoom();

		// Set map parm defaults (may be overridden by page parms in GMAP.showMap())
		// Weerribben
		GMAP.setDefaultMapParms(new GLatLng(52.86581372, 5.2679443359375), 9, 'satellite');

		// Show the map
		GMAP.showMap();

		// GMAP.map.setCenter(new GLatLng(52.782605, 5.96349), 10, GMAP.mapTypes['satellite']);
		GTAPP.showStatus('Map created');

	}
}