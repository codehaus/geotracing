// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * GeoTracing app.
 *
 * PURPOSE
 * Library representing the app. All starts here.
 *
 * Author: Just van den Broecke
 * $Id$
 */

DH.include('Date.js');
DH.include('FeatureSet.js');
DH.include('FeaturePlayer.js');
DH.include('Factory.js');
DH.include('GMap.js');
DH.include('GTWorld.js');
DH.include('LiveListener.js');

DH.include('Medium.js');
DH.include('Menu.js');
DH.include('Panel.js');
DH.include('POI.js');
DH.include('Record.js');
DH.include('Selector.js');
DH.include('Server.js');
DH.include('TLabel.js');
DH.include('Tracer.js');
DH.include('Track.js');
DH.include('TrackAutoPlayer.js');
DH.include('TrackPlayer.js');
DH.include('Widget.js');

DH.include('MyApp.js');


// Pushlet Data Event Callback from Server
// These are PushletEvents sent by tracing users (e.g. from mobile phone)
// lib/ajax-pushlet-client.js
function onData(anEvent) {
	if (GTAPP.liveListener != null)  {
		GTAPP.liveListener.onEvent(anEvent);
	}
}

// The GeoTracing application functions
var GTAPP = {
	statusId: null,
	mode: 'none',
	defaultMode: 'live',
	menu: null,
	userSelector: null,
	trackSelector: null,
	// SIG: '<a style="background-color:red; border: 1px solid #eeeeee; font:10px verdana;text-decoration:none;padding:2px;color:#eeeeee;" href="http://www.geotracing.com" target="_new">Powered by GeoTracing</a>',
	SIG: '<a href="http://www.geotracing.com" target="_new"><img src="img/powered-gt.png" border="0"/></a>',
	initialized: false,
	liveListener: null,

/** Bootstrap for entire app. */
	init: function() {
		if (GTAPP.initialized == true) {
			return;
		}

		// Bootstrap GT world
		GTW.boot();

		// Initialize user app
		MYAPP.init();

		// Set the document title
		document.title = 'Please set MYAPP.WINDOW_TITLE to appear here';
		if (MYAPP.WINDOW_TITLE) {
			document.title = MYAPP.WINDOW_TITLE;
		}
		DH.setHTML('title', MYAPP.DOC_TITLE);

		// Init server.js
		SRV.init();

		DH.hide(DH.getObject('help'));

		GTAPP.showStatus('Initializing...');

		// Creates Google Map object
		GTAPP.createMap();

		// Add GT signature
		GTAPP.addSig();

		// Init gtwidget.js
		GTW.init();

		GTAPP.createMenu();

		GTAPP.showStatus('init OK');

		GMAP.resize();
		DH.addEvent(window, 'resize', GMAP.resize, false);

		// Live Pushlet event setup
		GTAPP.createLiveListener();
		PL.joinListen('/gt')

		GTAPP.doPageCommand();

		GTAPP.initialized = true;

		// Optional start of user app
		if (MYAPP.start) {
			MYAPP.start();
		}
	},

/** Add GT signature to map. */
	addSig: function() {
		var id = 'gtsig';
		if (!DH.getObject(id)) {
			var b = document.createElement('div');
			b.id = id;
			b.style.position = 'absolute';
			b.style.left = '30%';
			b.style.bottom = '5px';
			b.style.zIndex = 25600;
			b.innerHTML = GTAPP.SIG;
			document.body.appendChild(b);
			DH.setOpacity(b, 0.7);
		}
	},

/** Create listener to incoming live events. */
	createLiveListener: function() {
		// May overload with MyApp LiveListener
		GTAPP.liveListener = new LiveListener('livestatus');
	},

	createMap: function() {
		alert('createMap needs to be defined in your app');
	},

// Setup callbacks in CSS drop-down menu
	createMenu: function() {
		GTAPP.menu = new Menu('mainmenu');
	},

// Optional command passed as query string to the page
	doPageCommand: function() {
		// NOTE: see also GMAP.showMap() for specific page
		// map parms (center,zoom,mapname)

		// Command
		var cmd = DH.getPageParameter('cmd', null);
		if (cmd == null) {
			// nothing to do
			return;
		}

		// Handle command
		if (cmd == 'showtrack') {
			var id = DH.getPageParameter('id', null);
			var tracerName = DH.getPageParameter('user', null);
			if (id == null || tracerName == null) {
				alert('need track id (id) and user name (user)');
				return;
			}

			GTAPP.showStatus('Drawing track for user ' + tracerName);

			GTAPP.onTrackSelect(id, null, tracerName);
			GTW.displayTrackPlayer();

			GTAPP.showStatus('Track drawn for user ' + tracerName);

			return true;
		} else if (cmd == 'autoplay') {
			// No menu visible
			if (GTAPP.menu != null) {
				GTAPP.menu.hide();
			}
			GTAPP.mAutoPlay();
		} else if (cmd == 'live') {
			// Optional user to follow immediately
			var tracerName = DH.getPageParameter('user', null);
			if (tracerName != null) {
				GTW.followTracer = tracerName;
			}

			// Set in live mode
			GTAPP.mLive();
		} else if (cmd == 'archive') {
			// Optional user to follow immediately
			var tracerName = DH.getPageParameter('user', null);

			if (tracerName != null) {
				// Show trackselector for user
				GTAPP.onUserSelect(null, tracerName);
			}

		} else {
			return false;
		}
	},

// Query active tracks callback
	onQueryActiveUsers: function(records) {
		GTAPP.showStatus(records.length + ' active users');

		GTAPP.userSelector = new Selector('Follow User', 'usersel', GTAPP.onActiveUserSelect);
		GTAPP.userSelector.hide();

		// Create a Tracer for each active track
		for (i = 0; i < records.length; i++) {
			trackId = records[i].getField('id');
			trackName = records[i].getField('name');
			tracerName = records[i].getField('loginname');
				GTAPP.userSelector.addOption(trackId, tracerName + ' - ' + trackName, tracerName);

			tracer = GTW.createTracerByRecord(records[i]);
			tracer.show();

			// If we need to immediately see a tracer
			if (GTW.followTracer != null && GTW.followTracer == tracerName) {
				GTAPP.showStatus('following ' + tracerName + '...');
				GTAPP.onActiveUserSelect(trackId, trackName, tracerName);
				GTW.followTracer = null;
			}
		}

		// Listen to Pushlet events from server (see onData)
		GTAPP.mode = 'live';
		GTAPP.showMode();

		// Hide status div
		var bbox = GTAPP.menu.getBBox();
		GTAPP.userSelector.setXY((bbox.x + bbox.w + 40), bbox.y);
		GTAPP.userSelector.show();

		GTAPP.showStatus('Live Mode - ' + records.length + ' users');
	},

	onQueryTracks: function (records) {
		GTAPP.trackSelector = new Selector('Select a Track', 'tracksel', GTAPP.onTrackSelect);
		GTAPP.trackSelector.hide();
		var trackId, trackName, tracerName;
		for (var i = 0; i < records.length; i++) {
			trackId = records[i].getField('id');
			trackName = records[i].getField('name');
			tracerName = records[i].getField('loginname');
			GTAPP.trackSelector.addOption(trackId, tracerName + ' - ' + trackName, tracerName);
		}
		var bbox = GTAPP.menu.getBBox();
		GTAPP.trackSelector.setXY((bbox.x + bbox.w + 40), bbox.y);
		GTAPP.trackSelector.show();
		GTAPP.showStatus('Archive Mode - ' + records.length + ' tracks');
	},

	onQueryAllUsers: function (records) {
		GTAPP.showStatus('Got ' + records.length + ' users');

		GTAPP.userSelector = new Selector('Select a User', 'usersel', GTAPP.onUserSelect);
		GTAPP.userSelector.hide();

		var userId, userName;
		for (var i = 0; i < records.length; i++) {
			userId = records[i].getField('id');
			userName = records[i].getField('loginname');
			GTAPP.userSelector.addOption(userId, userName, userName);
		}

		GTAPP.userSelector.setWidth(10);
		var bbox = GTAPP.menu.getBBox();
		GTAPP.userSelector.setXY((bbox.x + bbox.w + 40), bbox.y);
		GTAPP.userSelector.show();
		// PL.debugOn = true;

		GTAPP.showStatus('Archive Mode - ' + records.length + ' users');
	},

	onQueryUserTracks: function (records) {
		GTAPP.trackSelector = new Selector('Select a Track', 'tracksel', GTAPP.onTrackSelect);
		GTAPP.trackSelector.hide();

		var trackId, trackName, userName;
		for (var i = 0; i < records.length; i++) {
			trackId = records[i].getField('id');
			trackName = records[i].getField('name');
			userName = records[i].getField('loginname');
			GTAPP.trackSelector.addOption(trackId, trackName, userName);
		}

		var bbox = null;
		if (GTAPP.userSelector && GTAPP.userSelector != null) {
			bbox = GTAPP.userSelector.getBBox();
			GTAPP.trackSelector.setXY((bbox.x + bbox.w + 40), bbox.y);
		} else {
			bbox = GTAPP.menu.getBBox();
			GTAPP.trackSelector.setXY((bbox.x + bbox.w + 40), bbox.y);
		}
		
		GTAPP.trackSelector.show();
		GTAPP.showStatus('Archive - user has ' + records.length + ' tracks');
	},

	onQueryMedia: function (records) {
		GTAPP.showStatus('Found ' + records.length + ' media, displaying...');
		GTW.displayMedia(records);
		GTAPP.showStatus('Displaying ' + records.length + ' media');
	},

	onQueryPOIs: function (records) {
		GTAPP.showStatus('Found ' + records.length + ' POIs, displaying...');
		GTW.displayPOIs(records);
		GTAPP.showStatus('Displaying ' + records.length + ' POIs');
	},


	clearMap: function () {
		GTW.clearMap();
	},


	hideStatus: function() {
		DH.hide('status');
		if (GTAPP.statusId != null) {
			clearInterval(GTAPP.statusId);
			GTAPP.statusId = null;
		}

	},

	blinkStatus: function(txt) {
		GTAPP.hideStatus();
		DH.setHTML('status', txt);
		GTAPP.statusId = setInterval(function() {
			DH.toggleVisibility('status')
		}, 400);
	},

	showMode: function() {
		DH.setHTML('mode', GTAPP.mode);
	},

	showStatus: function(txt) {
		GTAPP.hideStatus();
		DH.setHTML('status', txt);
		DH.show('status');
	},


	_deleteSelectors: function() {
		if (GTAPP.trackSelector != null) {
			GTAPP.trackSelector.remove();
			GTAPP.trackSelector = null;
		}

		if (GTAPP.userSelector != null) {
			GTAPP.userSelector.remove();
			GTAPP.userSelector = null;
		}

	},

//
// SELECTOR CALLBACK FUNCTIONS
//

// Track selected e.g. in combo box
	onTrackSelect: function(trackId, trackName, tracerName) {
		// alert('select track: id=' + trackId + ' tracer=' + tracerName + ' trkName=' + trackName);
		// GTAPP.clearMap();
		GTW.displayTrackPlayer();


		var tracer = GTW.createTracer(tracerName);
		tracer.readTrack(trackId, trackName, true);
		tracer.show();
	},

// User selected
	onActiveUserSelect: function(trackId, trackName, tracerName) {
		var tracer = GTW.getTracer(tracerName);
		if (!tracer) {
			GTAPP.showStatus('no tracer for ' + tracerName);
			return;
		}
		GTW.displayTrackPlayer();

		tracer.zoomTo();
		tracer.readTrack(trackId, trackName, true);
		tracer.show();
	},

// User selected
	onUserSelect: function(userId, loginName) {
		// alert('u=' + userId + ' l=' + loginName);
		if (GTAPP.trackSelector && GTAPP.trackSelector != null) {
			GTAPP.trackSelector.remove();
			GTAPP.trackSelector = null;
		}

		var tracer = GTW.createTracer(loginName);
		tracer.showInfo();
		GTAPP.blinkStatus('Getting tracks for ' + loginName + "...");
		SRV.get('q-tracks-by-user', GTAPP.onQueryUserTracks, 'user', loginName);
	},

//
// MENU CALLBACK FUNCTIONS
//
	mAutoPlay: function() {
		if (GTAPP.mode == 'autoplay') {
			return;
		}

		GTAPP.showStatus('autoplay');
		GTAPP.mode = 'autoplay';
		GTAPP.showMode();
		GTAPP.clearMap();
		GTAPP._deleteSelectors();
		GTW.startAutoPlay();
	},

	mArchive: function() {
		if (GTAPP.mode == 'archive') {
			return;
		}

		GTAPP.mode = 'archive';
		GTAPP.showMode();
		GTAPP.clearMap();
		GTAPP._deleteSelectors();
		GTAPP.blinkStatus('Getting all users...');
		SRV.get('q-all-users', GTAPP.onQueryAllUsers);
	},

	mLive: function(e) {
		GTAPP.showMode();

		if (GTAPP.mode == 'live') {
			return;
		}

		GTAPP.liveListener.clearStatus();

		// Get all active tracks
		GTAPP.clearMap();
		GTAPP._deleteSelectors();

		GTAPP.blinkStatus('Getting active users...');
		SRV.get('q-active-tracks', GTAPP.onQueryActiveUsers);

	},

	mLastTracks: function(max) {
		GTAPP.mode = 'tracks';
		GTAPP.showMode();

		// Get all active tracks
		GTAPP.clearMap();
		GTAPP._deleteSelectors();

		GTAPP.blinkStatus('Getting last ' + max + ' tracks...');
		SRV.get('q-recent-tracks', GTAPP.onQueryTracks, 'max', max);
	},

	mMy: function() {
	   window.open('my', 'MyGeoTracing');
	},

	mWebTracer: function() {
	   window.open('webtracer', 'WebTracer');
	},

	mShowMedia: function(max) {
		GTAPP.mode = 'media';
		GTAPP.showMode();

		// Get all active tracks
		GTAPP.clearMap();
		GTAPP._deleteSelectors();

		GTAPP.blinkStatus('Getting random media...');
		SRV.get('q-locative-media', GTAPP.onQueryMedia, 'random', 'true', 'max', max);
	},

	mShowRecentMedia: function(max) {
		GTAPP.mode = 'media';
		GTAPP.showMode();

		// Get all active tracks
		GTAPP.clearMap();
		GTAPP._deleteSelectors();

		GTAPP.blinkStatus('Getting last ' + max + ' media...');
		SRV.get('q-recent-media', GTAPP.onQueryMedia, 'max', max);
	},

	mShowRecentMediaInBbox: function(max) {
		GTAPP.mode = 'media';
		GTAPP.showMode();

		// Get all active tracks
		GTAPP.clearMap();
		GTAPP._deleteSelectors();

		GTAPP.blinkStatus('Getting last ' + max + ' media in area...');
		var bounds = GMAP.map.getBounds();
		var bbox = bounds.getSouthWest().x + ',' + bounds.getSouthWest().y + ',' + bounds.getNorthEast().x + ',' + bounds.getNorthEast().y;
		SRV.get('q-recent-media', GTAPP.onQueryMedia, 'max', max, 'bbox', bbox);
	},

	mShowMediaInBbox: function(max) {
		GTAPP.mode = 'media';
		GTAPP.showMode();

		// Get all active tracks
		GTAPP.clearMap();
		GTAPP._deleteSelectors();

		GTAPP.blinkStatus('Getting random media in area...');
		var bounds = GMAP.map.getBounds();
		var bbox = bounds.getSouthWest().x + ',' + bounds.getSouthWest().y + ',' + bounds.getNorthEast().x + ',' + bounds.getNorthEast().y;
		// var bbox = bounds.minX + ',' + bounds.minY + ',' + bounds.maxX + ',' + bounds.maxY;
		SRV.get('q-locative-media', GTAPP.onQueryMedia, 'random', 'true', 'max', max, 'bbox', bbox);
	},

	mShowPOIs: function(max) {
		GTAPP.mode = 'poi';
		GTAPP.showMode();

		// Get all active tracks
		GTAPP.clearMap();
		GTAPP._deleteSelectors();

		GTAPP.blinkStatus('Getting random POIs...');
		SRV.get('q-pois', GTAPP.onQueryPOIs, 'random', 'true', 'max', max);
	},

	mShowLastPOIs: function(max) {
		GTAPP.mode = 'poi';
		GTAPP.showMode();

		// Get all active tracks
		GTAPP.clearMap();
		GTAPP._deleteSelectors();

		GTAPP.blinkStatus('Getting last ' + max + ' POIs...');
		SRV.get('q-pois', GTAPP.onQueryPOIs, 'max', max);
	},


	mShowPOIsInBbox: function(max) {

		GTAPP.mode = 'poi';
		GTAPP.showMode();

		// Get all active tracks
		GTAPP.clearMap();
		GTAPP._deleteSelectors();

		GTAPP.blinkStatus('Getting POIs in area...');
		var bounds = GMAP.map.getBounds();
		var bbox = bounds.getSouthWest().x + ',' + bounds.getSouthWest().y + ',' + bounds.getNorthEast().x + ',' + bounds.getNorthEast().y;
		SRV.get('q-pois', GTAPP.onQueryPOIs, 'bbox', bbox, 'max', max);
	},

	mShowHelp: function(url) {
		var helpPanel = new Panel('INFO', 'red', 'white');
		helpPanel.setXY(100, 100);
		helpPanel.setDimension(600, 500);
		helpPanel.loadContent(url);
	},

// Switch background map type
	mSetMap: function(type) {
		GMAP.setMapType(type);
	}
}

// Starts it all
DH.onReady = GTAPP.init;
