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

var PL;

// This file contains specific functions for Bliin webapp
var MYAPP = {
	DOC_TITLE: 'JGPS',
	WINDOW_TITLE: 'GeoTracing - JGPS',
	media: null,
	currentUser: null,

/** Load file that contains app-specific menu. */
	createMenu: function(aMenuContent) {
		if (!aMenuContent) {
			DH.getURL('locmenu.html', GTAPP.createMenu);
			return;
		}

		// Content loaded: setup menu
		DH.setHTML('menucontainer', aMenuContent);
		GTAPP.menu = new Menu('mainmenu');
	},

	empty: function() {

	},


/** Bootstrap: called by GTAPP.init() */
	init: function() {
		// Overrule GTApp.js functions here
		GTAPP.createMap = MYAPP.createMap;
		GTAPP.createMenu = MYAPP.createMenu;

	//	GTAPP.doPageCommand=MYAPP.doPageCommand;
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
		// GTW.getFactory().setClassDef('Tracer', 'MyTracer');
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
		//var cmd = DH.getPageParameter('cmd', 'archive');
		var cmd = DH.getPageParameter('cmd', null);
		if (cmd == 'archive') {
			var tracerName = DH.getPageParameter('user', null);
			if (tracerName == null) {
				GTAPP.mShowMediaInBbox(1);
			}
		}else if (cmd == 'kaart') {
			// Optional user to follow immediately
			GTAPP.mode = 'media';
			GTAPP.showMode();
			var loginName = DH.getPageParameter('user', null);
			MYAPP.currentUser = loginName;

			// Get all active tracks
			GTAPP.blinkStatus('Getting random media...');
			SRV.get('q-locations-by-user', MYAPP.showlocations, 'user', loginName);
		}
	},


	createMap: function() {
		GTAPP.showStatus('Kaart laden ...');

		// var WMS_URL_EINDHOVEN_SAT = 'http://www.senseofthecity.nl/sotce/map/eindhoven-sat.jsp?';
		//var G_MAP_EINDHOVEN_SAT = createWMSSpec(WMS_URL_EINDHOVEN_SAT, "Luchtfoto", "Fot", "bl", "bla", "image/jpeg", "1.1.1");

		// Add map specs to app (see also menu in map.jsp)
		GMAP.addMapType('map', G_NORMAL_MAP);
		GMAP.addMapType('satellite', G_SATELLITE_MAP);
		GMAP.addMapType('hybrid', G_HYBRID_MAP);
		// GMAP.addMapType('luchtfoto', G_MAP_EINDHOVEN_SAT);

		// Create the Google Map
		GMAP.createGMap('map');

		GMAP.map.addControl(new GMapTypeControl());
		GMAP.map.addControl(new GOverviewMapControl());
		GMAP.map.addControl(new GLargeMapControl());
		GMAP.map.addControl(new GScaleControl());

		GMAP.map.enableContinuousZoom();
		GMAP.map.enableDoubleClickZoom();

		// Set map parm defaults (may be overridden by page parms in GMAP.showMap())
		GMAP.setDefaultMapParms(new GLatLng(50.9303, 5.3382), 13, 'hybrid');

		// Show the map
		GMAP.showMap();

		GTAPP.showStatus('Kaart geladen');
	},

/** Create listener to incoming live events. */
	createLiveListener: function() {
		// May overload with MyApp LiveListener
		GTAPP.liveListener = new LiveListener('status');
	},

	showlocations: function(records){
		// alert('u=' + userId + ' l=' + loginName);
		GTW.featureSet.dispose();
		GTAPP.showStatus('Found ' + records.length + ' locations, displaying...');

		var location, record;
		for (var i = 0; i < records.length; i++) {
			record = records[i];
			location = new MyMedium(record.getField('mediumid'),
				record.getField('name'),
				record.getField('description'),
				record.getField('kind'),
				record.getField('mime'),
				record.getField('creationdate'),
				record.getField('lon'),
				record.getField('lat'),
				record.getField('subtype'));
			 location.userName = MYAPP.currentUser;
			// Create and draw location
			GTW.featureSet.addFeature(location);
		}
		GTW.featureSet.show();
		GTW.getFeaturePlayer().setFeatureSet(GTW.featureSet);
		GTW.getFeaturePlayer().show()
		GTW.featureSet.displayFirst();
		GTAPP.showStatus('Displaying ' + records.length + ' locations');
	}
}
