// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Sense of Brainport main app.
 *
 * PURPOSE
 * Library representing the app. All starts here. See GTApp.init();
 * GTApp.init() will call MYAPP.init() and MYAPP.start() when initialized.
 *
 * Author: Just van den Broecke
 * $Id: MyApp.js,v 1.9 2006-08-13 01:09:03 just Exp $
 */

// Add project-specific JS files to be loaded.
DH.include('MyTracer.js');
DH.include('MyMedium.js');

// This file contains specific functions for Bliin webapp
var MYAPP = {
	DOC_TITLE: 'Sense of Brainport',
	WINDOW_TITLE: 'GeoTracing - Sense of Brainport',
	media: null,

	empty: function() {

	},

	/** Bootstrap: called by GTAPP.init() */
	init: function() {
		// Overrule GTApp.js functions here
		GTAPP.createMap = MYAPP.createMap;

		// Disable menu
		//GTAPP.createMenu = MYAPP.empty;

		// Custom resize (page filling map)
		//GMAP.resize = MYAPP.resize;

		// Overrule LiveListener implementation
		GTAPP.createLiveListener = MYAPP.createLiveListener;
		//GTAPP.onQueryActiveUsers = MYAPP.onQueryActiveUsers;

		// This is the base URL for directional icons (dir_icon_green_01.png through dir_icon_green_08.png)
		// MyTracer will determine appropriate icon based on geo-course (heading)
		// GTW.TRACER_ICON_URL = 'img/dir_icon_green_0';

		// Overrule creation of GT base classes with our
		// own specific classes.
		GTW.getFactory().setClassDef('Tracer', 'MyTracer');
		GTW.getFactory().setClassDef('Medium', 'MyMedium');

		// Manage media (features)
		//MYAPP.media = new FeatureSet();


		// Use single color when not live
		var cmd = DH.getPageParameter('cmd', 'archive');
		if (cmd != 'live') {
			GTW.colors = [0, 'blue', '#5555FF', '#000000'];
			GTW.randomColors = ['#FF00CC'];

		}


		GTAPP.showStatus('Laden OK');
	},

	/** Bootstrap: called by GTAPP.init() */
	start: function() {
		// Position mainmenu (solves IE menu positioning problem)
		if (DH.isIE == true) {
			DH.setObjectXY('mainmenu', DH.getObjectX('mainmenuanchor'), DH.getObjectY('mainmenuanchor'));
		}
		var cmd = DH.getPageParameter('cmd', 'archive');
		if (cmd == 'archive') {
			var tracerName = DH.getPageParameter('user', null);
			if (tracerName == null) {
				GTAPP.mShowMediaInBbox(1);
			}
		}
	},

	createMap: function() {
		GTAPP.showStatus('Kaart laden ...');

		var WMS_URL_EINDHOVEN_SAT = 'http://www.senseofthecity.nl/sotce/map/eindhoven-sat.jsp?';
		var G_MAP_EINDHOVEN_SAT = createWMSSpec(WMS_URL_EINDHOVEN_SAT, "Luchtfoto", "Fot", "bl", "bla", "image/jpeg", "1.1.1");

		// Add map specs to app (see also menu in map.jsp)
		GMAP.addMapType('map', G_NORMAL_MAP);
		GMAP.addMapType('satellite', G_SATELLITE_MAP);
		GMAP.addMapType('luchtfoto', G_MAP_EINDHOVEN_SAT);

		// Create the Google Map
		GMAP.createGMap('map');

		GMAP.map.addControl(new GOverviewMapControl());
		GMAP.map.addControl(new GSmallMapControl());
		GMAP.map.addControl(new GMapTypeControl());
		GMAP.map.addControl(new GScaleControl());
		// Set map parm defaults (may be overridden by page parms in GMAP.showMap())
		GMAP.setDefaultMapParms(new GLatLng(51.4365, 5.4781), 13, 'map');

		// Show the map
		GMAP.showMap();

		GTAPP.showStatus('Kaart geladen');
	},

/** Create listener to incoming live events. */
	createLiveListener: function() {
		// May overload with MyApp LiveListener
		GTAPP.liveListener = new LiveListener('status');
	}
}
