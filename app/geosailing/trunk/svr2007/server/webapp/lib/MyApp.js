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
DH.include('BoatPopup.js');

TLabel.prototype.getXY = function(a, b) {
	var xy = this.map.fromLatLngToDivPixel(this.anchorLatLng)
	var x = parseInt(xy.x);
	var y = parseInt(xy.y);
	with (Math) {
		switch (this.anchorPoint) {
			case 'topLeft':break;
			case 'topCenter':x -= floor(this.w / 2);break;
			case 'topRight':x -= this.w;break;
			case 'midRight':x -= this.w;y -= floor(this.h / 2);break;
			case 'bottomRight':x -= this.w;y -= this.h;break;
			case 'bottomCenter':x -= floor(this.w / 2);y -= this.h;break;
			case 'bottomLeft':y -= this.h;break;
			case 'midLeft':y -= floor(this.h / 2);break;
			case 'center':x -= floor(this.w / 2);y -= floor(this.h / 2);break;
			default:break;
		}
	}

	if (this.scaleElmId) {
		// mapzoom based scale
		var zoom = this.map.getZoom();
		if (this.lastZoom && (zoom == this.lastZoom)) {
			// zoom not changed: no need to apply scaling
			// return;
		}

		// Zoom changed: remember
		this.lastZoom = zoom;

		// Calc scale
		var scale = Math.pow(3,(zoom/8)) / 12;

		// Keep to minimum
		scale = Math.max(this.minScaleFactor, scale);

	//	tmp_debug(2,'scaling, zoom=',z,', s=',s); //Math.round(10* Math.pow(3,(z/10))));

		// calc scaled w/h
		var sw = Math.round(scale * this.maxW);
		var sh = Math.round(scale * this.maxH);

		// alert('this.w=' + this.w + ' this.h=' + this.h)
		//apply to elm
		var icon = document.getElementById(this.scaleElmId);
		if (!icon) {
			return;
		}

		icon.style.width = sw +'px';
		icon.style.height = sh +'px';

		// Correct marker offset (assume middle and topleft anchor!!!)
		this.markerOffset.width = sw/2;
		this.markerOffset.height = sh/2;
	}

	if (this.diffuse == true) {
		x = x - this.markerOffset.width + diffusions[Math.floor(Math.random() * 10)].x;
		y = y - this.markerOffset.height + diffusions[Math.floor(Math.random() * 10)].y;
	} else {
		x = x - this.markerOffset.width;
		y = y - this.markerOffset.height;
	}

	if (this.glide == true) {
		this.targetX = x;
		this.targetY = y;
		if (!this.x) {
			this.x = x;
			this.y = y;
		}

	} else {
		this.x = x;
		this.y = y;
	}

	xy.x = x;
	xy.y = y;

	return xy;
}

TLabel.prototype.setScaling = function(aScaleElmId, aMaxW, aMaxH, aMinScaleFactor) {
	this.scaleElmId = aScaleElmId;
	this.maxW = aMaxW;
	this.maxH = aMaxH;
	this.minScaleFactor = .3;
	if (aMinScaleFactor) {
		this.minScaleFactor = aMinScaleFactor;
	}
}

CMT.showCommentPanel = function(aTargetId, aTargetType, aTargetName) {
	if (CMT.commentPanel == null) {
		CMT.commentPanel = new Panel('commentpanel', '#5e0000', 'white', null, CMT.onPanelClose);
		CMT.commentPanel.setDimension(640, 500);
		CMT.commentPanel.setXY(50, 50);
	}


	// Save target id
	CMT.currentTarget = aTargetId + '';
	CMT.currentType = aTargetType;
	CMT.currentName = aTargetName;
	CMT.commentPanel.show();
	CMT.commentPanel.loadContent('popup/comment.html');
	CMT.commentPanel.setTitle('Commentaar');
	CMT.displayComments();
	CMT.panelOpen = true;
	return false;
}

// This file contains specific app functions
var MYAPP = {
	WINDOW_TITLE: 'Geosailing - Schuttevaer 2007 Live',
	DOC_TITLE: 'Geosailing',
	LOGOS: new Array('sidebar/images/devriessailsW.gif', 'sidebar/images/maxleadW.gif', 'sidebar/images/4ptelecomW.gif', 'sidebar/images/kuiperverzW.gif'),
// LOGOS_LINKS: new Array('http://www.sneekweek.nl', 'http://www.schuttevaer.nl', 'http://www.nhl.nl'),
	LOGOS_LINKS: new Array('http://www.devriessails.nl', 'http://www.maxlead.nl', 'http://www.4ptelecom.nl', 'http://www.kuiperverzekeringen.nl'),
	logoIndex: 0,
	MAP_INIT_CENTER: new GLatLng(53.12715, 5.175445),
	MAP_INIT_ZOOM: 10,
	liveMedia: new FeatureSet(),

/** Add uploaded medium. */
	addLiveMedium: function(aMedium) {
		MYAPP.liveMedia.addFeature(aMedium);
	},

/** Create listener to incoming live events. */
	createLiveListener: function() {
		// May overload with MyApp LiveListener
		GTAPP.liveListener = new SailLiveListener('livestatus');
	},

	clearMap: function() {
		GTW.clearTracers();
		GTW.clearFeatures();
		MYAPP.liveMedia.dispose();
		BOAT.mediaSet.dispose();
		GMAP.map.clearOverlays();
		// TRACER.follow = null;
	},


// Called in GTAPP.init() (see overload above)

	createMap: function() {
		GTAPP.blinkStatus('Creating map...');
		GMAP.addMapType('satellite', G_SATELLITE_TYPE);
		GMAP.addMapType('map', G_MAP_TYPE);
		GMAP.addMapType('hybrid', G_HYBRID_TYPE);

		// Create the Google Map
		GMAP.createGMap('map');

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
		// 5.179128333333333, 53.09179666666667
		GMAP.setDefaultMapParms(MYAPP.MAP_INIT_CENTER, MYAPP.MAP_INIT_ZOOM, 'satellite');

		// Show the map
		GMAP.showMap();

		// GMAP.map.setCenter(new GLatLng(52.782605, 5.96349), 10, GMAP.mapTypes['satellite']);
		GTAPP.showStatus('Map created');

	},

	drawActiveTrack: function(aLoginName) {
		SRV.get('q-tracks-by-user', MYAPP.onQueryUserTracks, 'user', aLoginName);
	},


	followBoat: function(aBoatName) {
		var tracer = GTW.getTracer(aBoatName);
		if (!tracer) {
			alert('kan boot genaamd ' + aBoatName + ' niet vinden !!');
			return;
		}

		// Kijk of ie al locatie heeft
		if (!tracer.getLocation() || tracer.getLocation() == null) {
			alert('De boot ' + aBoatName + ' heeft nog geen locatie.');
			return;
		}

		tracer.followToggle();
	},

	zoomOut: function() {
		GMAP.map.setCenter(MYAPP.MAP_INIT_CENTER, MYAPP.MAP_INIT_ZOOM);
	},

	onQueryUserTracks: function(records) {
		var activeTrackRec;
		for (var i = 0; i < records.length; i++) {
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
		var tracer = GTW.getTracer(aLoginName);
		BOAT.show(aLoginName);
/*		var panel = new Panel(aLoginName + ' Info', '#072855', 'white');
		panel.setXY(200, 100);
		panel.setDimension(400, 300);
		panel.loadContent('popup/boot.html');  */
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
		GTW.minTrackPtDist = 30;
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
		var helpPanel = new Panel('Help', '#072855', 'white');
		helpPanel.setXY(100, 100);
		helpPanel.setDimension(640, 500);
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
			if (i == records.length -1) {
				userList.innerHTML += '<span class="clearfloats"></span>';
			}
			userList.innerHTML += userDiv;
		}
		userList.innerHTML += userListHTML;

		GTAPP.showStatus('Archive Mode - ' + records.length + ' users');
	},

	start: function() {
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
	}

}
