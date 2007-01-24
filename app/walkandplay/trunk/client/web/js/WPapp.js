// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * GeoTracing app.
 *
 * PURPOSE
 * Library representing the app. All starts here.
 *
 * Author: Just van den Broecke
 * $Id: WP.js,v 1.25 2006-07-22 22:51:35 just Exp $
 */


DH.include('Date.js');
DH.include('FeatureSet.js');
DH.include('FeaturePlayer.js');
DH.include('GTWorld.js');
DH.include('../WPLiveListener.js');
DH.include('Factory.js');
DH.include('Comment.js');
DH.include('Panel.js');
DH.include('Record.js');
DH.include('Selector.js');
DH.include('Server.js');
DH.include('../WPTracer.js');
DH.include('Track.js');
DH.include('TrackAutoPlayer.js');
DH.include('TrackPlayer.js');
DH.include('Widget.js');
DH.include('../MyApp.js');
DH.include('TLabel.js');
DH.include('../myMedium.js');


// Pushlet Data Event Callback from Server
// These are PushletEvents sent by tracing users (e.g. from mobile phone)
// js/gt/ajax-pushlet-client.js
function onData(anEvent) {
	WP.liveListener.onEvent(anEvent);
}

// The GeoTracing application functions
var WP = {
	statusId: null,
	mode: 'none',
	defaultMode: 'live',
	userSelector: null,
	trackSelector: null,
	initialized: false,
	liveListener: null,

/** Bootstrap for entire app. */
	init: function() {
		if (WP.initialized == true) {
			return;
		}
		GTW.boot();
		// Initialize user app
		MYAPP.init();
		// Init server.js
		SRV.init();
		// Creates Google Map object
		WP.createMap();
		// Init gtwidget.js
		GTW.init();
		// Live Pushlet event setup
		WP.createLiveListener();
		//PL.joinListen('/wp')
		WP.doPageCommand();
		WP.initialized = true;
		// Optional start of user app
		if (MYAPP.start) {
			MYAPP.start();
		}
	},



/** Create listener to incoming live events. */
	createLiveListener: function() {
		// May overload with MyApp LiveListener
		WP.liveListener = new LiveListener('livestatus');
	},

	createMap: function() {
		alert('createMap needs to be defined in your app');
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

			WP.showStatus('Drawing track for user ' + tracerName);

			WP.onTrackSelect(id, null, tracerName);
			GTW.displayTrackPlayer();

			WP.showStatus('Track drawn for user ' + tracerName);

			return true;
		} else if (cmd == 'autoplay') {
			// No menu visible
			WP.mAutoPlay();
		} else if (cmd == 'live') {
			// Optional user to follow immediately
			var tracerName = DH.getPageParameter('user', null);
			if (tracerName != null) {
				GTW.followTracer = tracerName;
			}

			// Set in live mode
			WP.mLive();
		} else {
			return false;
		}
	},

// Query active tracks callback
	onQueryActiveUsers: function(records) {
		WP.showStatus(records.length + ' active users');
		WP.userSelector = new Selector('Follow User', 'usersel', WP.onActiveUserSelect);
		WP.userSelector.hide();

		// Create a Tracer for each active track
		for (i = 0; i < records.length; i++) {
			trackId = records[i].getField('id');
			trackName = records[i].getField('name');
			tracerName = records[i].getField('loginname');
				WP.userSelector.addOption(trackId, tracerName + ' - ' + trackName, tracerName);

			tracer = GTW.createTracer(records[i].getField('loginname'),
			records[i].getField('lon'), records[i].getField('lat'));
			tracer.show();

			// If we need to immediately see a tracer
			if (GTW.followTracer != null && GTW.followTracer == tracerName) {
				WP.showStatus('following ' + tracerName + '...');
				WP.onActiveUserSelect(trackId, trackName, tracerName);
				GTW.followTracer = null;
			}
		}

		// Listen to Pushlet events from server (see onData)
		WP.mode = 'live';
		WP.showMode();

		// Hide status div


		WP.showStatus('Live Mode - ' + records.length + ' users');
	},

	onQueryTracks: function (records) {
		var cont = document.getElementById('archivetoursbox').getElementsByTagName('div')[0];
		var trackId, trackName, tracerName;
		cont.innerHTML = '';
		for (var i = 0; i < records.length; i++) {
			trackId = records[i].getField('id');
			trackName = records[i].getField('name');
			tracerName = records[i].getField('loginname');
			//WP.trackSelector.addOption(trackId, tracerName + ' - ' + trackName, tracerName);
			cont.innerHTML = cont.innerHTML + '<a href="#"><div id="'+trackId+'">'+tracerName+'</div></a>';
		}
		var tracks = cont.getElementsByTagName('div');
		for(var i = 0; i < tracks.length; i++) {
			dojo.event.connect(tracks[i],'onclick',function(evt) {
				SRV.get('get-track', WP.onTrackSelect, 'id', evt.target.getAttribute('id'));
			});


		}

	},

	onQueryAllUsers: function (records) {
		var cont = document.getElementById('archivetoursbox').getElementsByTagName('div')[0];
		cont.innerHTML = '<b>select user</b><br/><br/>';
		var userId, userName;
		var string = '';
		for (var i = 0; i < records.length; i++) {
			userId = records[i].getField('id');
			userName = records[i].getField('loginname');
			string = string + '<a href="#"><div id="'+userName+'">'+userName+'</div></a>';
		}
		cont.innerHTML = cont.innerHTML + string;
		var users = cont.getElementsByTagName('div');
		for(var i = 0; i < users.length; i++) {
			dojo.event.connect(users[i],'onclick',function(evt) {
				loginName = evt.target.getAttribute('id');
				SRV.get('q-tracks-by-user', WP.onQueryUserTracks, 'user', loginName);
			});


		}
	},

	onQueryUserTracks: function (records) {
		var trackId, trackName, userName;
		show('archivetracksbox');
		var cont = archivetracksbox.getElementsByTagName('div')[0];
		cont.innerHTML = '<b>select track</b><br/><br/>';
		var string ='';
		for (var i = 0; i < records.length; i++) {
			trackId = records[i].getField('id');
			trackName = records[i].getField('name');
			userName = records[i].getField('loginname');
			string = string + '<a href="#"><div id="'+trackId+'">'+trackName+'</div></a>';
		}
		cont.innerHTML = cont.innerHTML + string;
		var tracks = cont.getElementsByTagName('div');
		for(var i = 0; i < tracks.length; i++) {
			dojo.event.connect(tracks[i],'onclick',function(evt) {
				id = evt.target.getAttribute('id');
				SRV.get('get-track', WP.onTrackSelect, 'id', evt.target.getAttribute('id'));
			});


		}
	},

	onQueryMedia: function (records) {
		WP.showStatus('Found ' + records.length + ' media, displaying...');
		GTW.displayMedia(records);
		WP.showStatus('Displaying ' + records.length + ' media');
	},

	onQueryPOIs: function (records) {
		WP.showStatus('Found ' + records.length + ' POIs, displaying...');
		GTW.displayPOIs(records);
		WP.showStatus('Displaying ' + records.length + ' POIs');
	},


	clearMap: function () {
		GTW.clearMap();
	},


	hideStatus: function() {
		DH.hide('status');
		if (WP.statusId != null) {
			clearInterval(WP.statusId);
			WP.statusId = null;
		}

	},

	blinkStatus: function(txt) {
		WP.hideStatus();
		DH.setHTML('status', txt);
		WP.statusId = setInterval(function() {
			DH.toggleVisibility('status')
		}, 400);
	},

	showMode: function() {
		DH.setHTML('mode', WP.mode);
	},

	showStatus: function(txt) {
		WP.hideStatus();
		DH.setHTML('status', txt);
		DH.show('status');
	},


	_deleteSelectors: function() {
		if (WP.trackSelector != null) {
			WP.trackSelector.remove();
			WP.trackSelector = null;
		}

		if (WP.userSelector != null) {
			WP.userSelector.remove();
			WP.userSelector = null;
		}

	},

//
// SELECTOR CALLBACK FUNCTIONS
//

// Track selected e.g. in combo box
	onTrackSelect: function(trackXML) {
		var trackId = (trackXML.getElementsByTagName('info')[0].getAttribute('id'));
		var tracerName = (trackXML.getElementsByTagName('info')[0].getAttribute('name'));
		var trackName = (trackXML.getElementsByTagName('info')[0].getAttribute('name'));
		WP.clearMap();
		GTW.displayTrackPlayer();
		var tracer = GTW.createTracer(tracerName);
		tracer.readTrack(trackId, trackName, true);
		tracer.show();
	},

// User selected
	onActiveUserSelect: function(trackId, trackName, tracerName) {
		var tracer = GTW.getTracer(tracerName);
		if (!tracer) {
			WP.showStatus('no tracer for ' + tracerName);
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
		if (WP.trackSelector != null) {
			WP.trackSelector.remove();
			WP.trackSelector = null;
		}
		WP.blinkStatus('Getting tracks for ' + loginName + "...");
		SRV.get('q-tracks-by-user', WP.onQueryUserTracks, 'user', loginName);
	},

//
// MENU CALLBACK FUNCTIONS
//

	mAutoPlay: function() {
		if (WP.mode == 'autoplay') {
			return;
		}

		WP.showStatus('autoplay');
		WP.mode = 'autoplay';
		WP.showMode();
		WP.clearMap();
		WP._deleteSelectors();
		GTW.startAutoPlay();
	},

	mArchive: function() {
		if (WP.mode == 'archive') {
			return;
		}

		WP.mode = 'archive';
		WP.showMode();
		WP.clearMap();
		WP._deleteSelectors();
		WP.blinkStatus('Getting all users...');
		SRV.get('q-all-users', WP.onQueryAllUsers);
	},

	mLive: function(e) {
		WP.showMode();

		if (WP.mode == 'live') {
			return;
		}

		WP.liveListener.clearStatus();

		// Get all active tracks
		WP.clearMap();
		WP._deleteSelectors();

		SRV.get('q-active-tracks', WP.onQueryActiveUsers);
	},

	mLastTracks: function(max) {
	//	WP.mode = 'tracks';
	//	WP.showMode();

		// Get all active tracks
	//	WP.clearMap();
	//	WP._deleteSelectors();

		SRV.get('q-recent-tracks', WP.onQueryTracks, 'max', max);
	},

	mShowMedia: function(max) {
		WP.mode = 'media';
		WP.showMode();

		// Get all active tracks
		WP.clearMap();
		WP._deleteSelectors();

		WP.blinkStatus('Getting random media...');
		SRV.get('q-locative-media', WP.onQueryMedia, 'random', 'true', 'max', max);
	},

	mShowRecentMedia: function(max) {
		WP.mode = 'media';
		WP.showMode();

		// Get all active tracks
		WP.clearMap();
		WP._deleteSelectors();

		WP.blinkStatus('Getting last ' + max + ' media...');
		SRV.get('q-recent-media', WP.onQueryMedia, 'max', max);
	},

	mShowRecentMediaInBbox: function(max) {
		WP.mode = 'media';
		WP.showMode();

		// Get all active tracks
		WP.clearMap();
		WP._deleteSelectors();

		WP.blinkStatus('Getting last ' + max + ' media in area...');
		var bounds = GMAP.map.getBounds();
		var bbox = bounds.getSouthWest().x + ',' + bounds.getSouthWest().y + ',' + bounds.getNorthEast().x + ',' + bounds.getNorthEast().y;
		SRV.get('q-recent-media', WP.onQueryMedia, 'max', max, 'bbox', bbox);
	},

	mShowMediaInBbox: function(max) {
		WP.mode = 'media';
		WP.showMode();

		// Get all active tracks
		WP.clearMap();
		WP._deleteSelectors();

		WP.blinkStatus('Getting random media in area...');
		var bounds = GMAP.map.getBounds();
		var bbox = bounds.getSouthWest().x + ',' + bounds.getSouthWest().y + ',' + bounds.getNorthEast().x + ',' + bounds.getNorthEast().y;
		// var bbox = bounds.minX + ',' + bounds.minY + ',' + bounds.maxX + ',' + bounds.maxY;
		SRV.get('q-locative-media', WP.onQueryMedia, 'random', 'true', 'max', max, 'bbox', bbox);
	},

	mShowPOIs: function(max) {
		WP.mode = 'poi';
		WP.showMode();

		// Get all active tracks
		WP.clearMap();
		WP._deleteSelectors();

		WP.blinkStatus('Getting random POIs...');
		SRV.get('q-pois', WP.onQueryPOIs, 'random', 'true', 'max', max);
	},

	mShowLastPOIs: function(max) {
		WP.mode = 'poi';
		WP.showMode();

		// Get all active tracks
		WP.clearMap();
		WP._deleteSelectors();

		WP.blinkStatus('Getting last ' + max + ' POIs...');
		SRV.get('q-pois', WP.onQueryPOIs, 'max', max);
	},


	mShowPOIsInBbox: function(max) {

		WP.mode = 'poi';
		WP.showMode();

		// Get all active tracks
		WP.clearMap();
		WP._deleteSelectors();

		WP.blinkStatus('Getting POIs in area...');
		var bounds = GMAP.map.getBounds();
		var bbox = bounds.getSouthWest().x + ',' + bounds.getSouthWest().y + ',' + bounds.getNorthEast().x + ',' + bounds.getNorthEast().y;
		SRV.get('q-pois', WP.onQueryPOIs, 'bbox', bbox, 'max', max);
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
DH.onReady = WP.init;

/* get, set, and delete cookies */
function getCookie( name ) {
	var start = document.cookie.indexOf( name + "=" );
	var len = start + name.length + 1;
	if ( ( !start ) && ( name != document.cookie.substring( 0, name.length ) ) ) {
		return null;
	}
	if ( start == -1 ) return null;
	var end = document.cookie.indexOf( ";", len );
	if ( end == -1 ) end = document.cookie.length;
	return unescape( document.cookie.substring( len, end ) );
}
	
function setCookie( name, value, expires, path, domain, secure ) {
	var today = new Date();
	today.setTime( today.getTime() );
	if ( expires ) {
		expires = expires * 1000 * 60 * 60 * 24;
	}
	var expires_date = new Date( today.getTime() + (expires) );
	document.cookie = name+"="+escape( value ) +
		( ( expires ) ? ";expires="+expires_date.toGMTString() : "" ) + //expires.toGMTString()
		( ( path ) ? ";path=" + path : "" ) +
		( ( domain ) ? ";domain=" + domain : "" ) +
		( ( secure ) ? ";secure" : "" );
}
	
function deleteCookie( name, path, domain ) {
	if ( getCookie( name ) ) document.cookie = name + "=" +
			( ( path ) ? ";path=" + path : "") +
			( ( domain ) ? ";domain=" + domain : "" ) +
			";expires=Thu, 01-Jan-1970 00:00:01 GMT";
}
