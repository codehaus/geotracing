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

DH.include('SailLiveListener.js');
DH.include('SailTracer.js');
DH.include('SailMedium.js');
DH.include('Buoy.js');

// This file contains specific app functions
var MYAPP = {
	WINDOW_TITLE: 'Geosailing - Schuttevaer 2007 Live',
	DOC_TITLE: 'Geosailing',
	LOGOS: new Array('img/logo/devriessailsW.gif', 'img/logo/maxleadW.gif', 'img/logo/4ptelecomW.gif', 'img/logo/kuiperverzW.gif'),
// LOGOS_LINKS: new Array('http://www.sneekweek.nl', 'http://www.schuttevaer.nl', 'http://www.nhl.nl'),
	LOGOS_LINKS: new Array('http://www.devriessails.nl', 'http://www.maxlead.nl', 'http://www.4ptelecom.nl', 'http://www.kuiperverzekeringen.nl'),
	logoIndex: 0,


/** Create listener to incoming live events. */
	createLiveListener: function() {
		// May overload with MyApp LiveListener
		GTAPP.liveListener = new SailLiveListener('livestatus');
	},

	clearMap: function() {
		GTW.clearTracers();
		GTW.clearFeatures();
		//		GTW.stopAutoPlay();
		//		GTW.clearPanels();
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
		// Noord	:	53.25.000 = 53.416667
		// Zuid	:	52.50.000  = 52.83333
		// Oost	:	5.30.000   = 5.5
		// West	:	4.45.000   = 4.75
		// GMAP.setDefaultMapParms(new GLatLng(52.86581372, 5.2679443359375), 9, 'satellite');
		GMAP.setDefaultMapParms(new GLatLng(53.1249985, 5.125), 9, 'satellite');

		// Show the map
		GMAP.showMap();

		// GMAP.map.setCenter(new GLatLng(52.782605, 5.96349), 10, GMAP.mapTypes['satellite']);
		GTAPP.showStatus('Map created');

	},

	onQueryUserTracks: function(records) {
		var activeTrackRec;
		for (var i=0; i < records.length; i++) {
			if (records[i].getField('state') == 1) {
				activeTrackRec = records[i];
				break;
			}
		}

		if (!activeTrackRec) {
			alert('sorry, geen active route gevonden !');
			return;
		}

		var loginName = activeTrackRec.getField('loginname');
		var tracer = GTW.getTracer(loginName);
		tracer.readTrack(activeTrackRec.getField('id'), loginName, true);
	},

	showUserDetails: function(aLoginName) {
		SRV.get('q-tracks-by-user', MYAPP.onQueryUserTracks, 'user', aLoginName);
	},

	empty: function() {

	},

	init: function() {
		// Overrule GTApp.js functions here
		GTAPP.createMap = MYAPP.createMap;
		TRACER.BLINK_INTERVAL_SHOW = 1500;
		TRACER.BLINK_INTERVAL_HIDE = 400;
		TRACER.MARKER_OFFSET_X = 10;
		TRACER.MARKER_OFFSET_Y = 10;
		TRACER.SMOOTH_FACTOR = .025;
		// This is the base URL for directional icons (dir_icon_green_01.png through dir_icon_green_08.png)
		// SailTracer will determine appropriate icon based on geo-course (heading)
		GTW.TRACER_ICON_URL = 'img/iconen/boten/';
		// Disable menu
		GTAPP.createMenu = MYAPP.empty;

		// Overrule LiveListener implementation
		// Overrule creation of GT base classes with our
		// own specific classes.
		GTAPP.createLiveListener = MYAPP.createLiveListener;
		GTW.getFactory().setClassDef('Tracer', 'SailTracer');
		GTW.getFactory().setClassDef('Medium', 'SailMedium');
	},

	logoAnim: function() {
		// GTAPP.mShowHelp("content/appabout.html");
		var imgElm = DH.getObject('sponsorimg');
		imgElm.src = MYAPP.LOGOS[MYAPP.logoIndex];
		var linkElm = DH.getObject('sponsorlink');
		linkElm.href = MYAPP.LOGOS_LINKS[MYAPP.logoIndex];
		if (++MYAPP.logoIndex == MYAPP.LOGOS.length) {
			MYAPP.logoIndex = 0;
		}
	},

	mShowHelp: function (url) {
		var helpPanel = new Panel('HELP', 'blue', 'white');
		helpPanel.setXY(100, 100);
		helpPanel.setDimension(600, 500);
		helpPanel.loadContent(url);
	},

	onQueryAllUsers: function (records) {
		GTAPP.showStatus('Got ' + records.length + ' users');

		var userId, userName;
		var userList = DH.getObject('boatlist');
		var userDiv, color, tracer;

		// Save existing div stuff
		var userListHTML = userList.innerHTML;
		userList.innerHTML = ' ';
		for (var i = 0; i < records.length; i++) {
			tracer = GTW.createTracerByRecord(records[i]);
			userId = records[i].getField('id');
			tracer.id = userId;
			userName = records[i].getField('loginname');
			color = records[i].getField('color');
			userDiv = tracer.createStatusLine();
			userList.innerHTML += userDiv;
		}
		userList.innerHTML += userListHTML;

		GTAPP.showStatus('Archive Mode - ' + records.length + ' users');
	},

	start: function() {
		// GTAPP.mShowHelp("content/appabout.html");
		// GTAPP.mLive();
		GTAPP.mode = 'live';
		window.setInterval(MYAPP.logoAnim, 10000);
		SRV.get('q-all-users', MYAPP.onQueryAllUsers);
		BUOY.init();
		BUOY.show();
	},

	trimString: function(value) {
		value = value.replace(/^\s+/, '');
		value = value.replace(/\s+$/, '');
		return value;
	},

	zoomToBoat: function(aBoatName) {
		var tracer = GTW.getTracer(aBoatName);
		if (!tracer) {
			alert('kan boot genaamd ' + aBoatName + ' niet vinden !!');
			return;
		}

		tracer.zoomTo();

	}

}
