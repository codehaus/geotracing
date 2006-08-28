// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Bliin main app.
 *
 * PURPOSE
 * Library representing the app. All starts here. See GTApp.init();
 *
 * Author: Just van den Broecke
 * $Id: MyApp.js,v 1.9 2006-08-13 01:09:03 just Exp $
 */

// Add project-specific JS files to be loaded.
DH.include('BliinLiveListener.js');
DH.include('BliinTracer.js');
DH.include('BliinMedium.js');

// This file contains specific functions for Bliin webapp
var MYAPP = {
	DOC_TITLE: 'bliin01',
	WINDOW_TITLE: 'GeoTracing - Bliin01',
	media: null,

	empty: function() {

	},

/** Bootstrap: called by GTAPP.init() */
	init: function() {
		// Overrule GTApp.js functions here
		GTAPP.createMap = MYAPP.createMap;

		// Disable menu
		GTAPP.createMenu = MYAPP.empty;

		// Custom resize (page filling map)
		GMAP.resize = MYAPP.resize;

		// Overrule LiveListener implementation
		GTAPP.createLiveListener = MYAPP.createLiveListener;

		// This is the base URL for directional icons (dir_icon_green_01.png through dir_icon_green_08.png)
		// BliinTracer will determine appropriate icon based on geo-course (heading)
		GTW.TRACER_ICON_URL = 'img/dir_icon_green_0';

		// Overrule creation of GT base classes with our
		// own specific classes.
		GTW.getFactory().setClassDef('Tracer', 'BliinTracer');
		GTW.getFactory().setClassDef('Medium', 'BliinMedium');

		// Manage media (features)
		MYAPP.media = new FeatureSet();
	},

	createMap: function() {
		// Add map specs to app (see also menu in index.jsp)
		GMAP.addMapType('map', G_NORMAL_MAP);
		GMAP.addMapType('satellite', G_SATELLITE_MAP);
		GMAP.addMapType('hybrid', G_HYBRID_MAP);

		// Create the Google Map
		GMAP.createGMap('map');

		// GMAP.map.addControl(new GOverviewMapControl());
		GMAP.map.addControl(new GLargeMapControl());
		GMAP.map.addControl(new GMapTypeControl());
		GMAP.map.addControl(new GScaleControl());
		GMAP.map.setCenter(new GLatLng(52, 4), 2, GMAP.mapTypes['map']);
	},

/** Create listener to incoming live events. */
	createLiveListener: function() {
		// May overload with MyApp LiveListener
		GTAPP.liveListener = new BliinLiveListener('livestatus');
	},

// Query media callback
	onQueryMediaByUser: function(records) {
		MYAPP.onQueryMedia(records);
	},

// Query media callback
	onQueryMedia: function (records) {
		// Listen to Pushlet events from server (see onData)
		GTAPP.mode = 'live';
		MYAPP.media.dispose();
		MYAPP.media.addMedia(records);
		MYAPP.media.show();
		MYAPP.media.displayFirst();
	},

/** Display media for given user. */
	mShowMediaByUser: function(aLoginName) {
		// alert('query media for ' + aLoginName);
		GTW.clearMap();
		MYAPP.media.dispose();
		SRV.get('q-media-by-user', MYAPP.onQueryMediaByUser, 'user', aLoginName);
	},

/** Display next medium from current set. */
	mDisplayNextMedium: function() {
		// alert('query media for ' + aLoginName);
		MYAPP.media.displayNext();
	},

/** Display previous medium from current set. */
	mDisplayPrevMedium: function(aLoginName) {
		// alert('query media for ' + aLoginName);
		MYAPP.media.displayPrev();
	},

/** Called on window.resize: maintains page-filling map. */
	resize: function() {
		// Set map w/h to window size
		DH.setObjectXYWH(GMAP.mapDiv, 0, 0, DH.getInsideWindowWidth(), DH.getInsideWindowHeight());

		if (GMAP.map) {
			GMAP.map.checkResize();
		}
	},

/** Bootstrap: called by GTAPP.init() */
	start: function() {
		// Start app with showing last media added
		SRV.get('q-recent-media', MYAPP.onQueryMedia, 'max', 30);
	}
}
