// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * GeoSkating app.
 *
 * PURPOSE
 * Library representing the app. All starts here.
 *
 * Author: Just van den Broecke
 * $Id: MyApp.js,v 1.9 2006-07-25 22:38:12 just Exp $
 */

// This file contains specific functions for SotC Eindhoven
var MYAPP = {
	DOC_TITLE: '<img src="img/gs-logo.jpg" border="0"  alt="gs-logo" align="middle" />',
	WINDOW_TITLE: 'GeoTracing - WalkAndPlay',

	init: function() {
		// Overrule WP.js functions here
		WP.createMap = MYAPP.createMap;
    },

	start: function() {
		if (DH.getPageParameter('cmd', null) == null) {
			// http://test.geoskating.com/gs/?cmd=showtrack&user=just&id=3804&map=map
			// 3852 (d2) 3866 (d3)
			// WP.mShowHelp("content/appabout.html");
			//WP.mShowMediaInBbox(10);
 		}
   },

	createMap: function() {
		var WMS_URL_GREY = 'img/greysquare.jpg?';
		var G_MAP_GREY = createWMSSpec(WMS_URL_GREY, "Blank", "Blank", "bl", "bla", "image/jpeg", "1.1.1");
		CustomGetTileUrl = function(a, b) {
			var khURL = G_SATELLITE_MAP.getTileLayers()[0].getTileUrl(a,b);
			var lURL = "map/gmap-sk8-tile.jsp";
//			lURL += "?x=" + a.x;
//			lURL += "&y=" + a.y;
			lURL += "?layer=sk8";
			lURL += khURL.substring(khURL.indexOf('&t'), khURL.length);
//			lURL += "&zoom=" + b;
			return lURL;
		}
		var tile = new GTileLayer(new GCopyrightCollection("GT"), 5, 16);
		tile.getTileUrl = CustomGetTileUrl;
		tile.isPng = function() {
			return true;
		}
		tile.getOpacity = function() {
			return 1.0;
		}

		var satLayers = [G_SATELLITE_MAP.getTileLayers()[0], tile];
		var mapLayers = [G_NORMAL_MAP.getTileLayers()[0], tile];
		var blancLayers = [G_MAP_GREY.getTileLayers()[0], tile];

		var satRoutesType = new GMapType(satLayers, G_SATELLITE_MAP.getProjection(), "satroute");
		var mapRoutesType = new GMapType(mapLayers, G_NORMAL_MAP.getProjection(), "maproute");
		var blancRoutesType = new GMapType(blancLayers, G_SATELLITE_MAP.getProjection(), "blancroute");


		// Add map specs to app (see also menu in index.jsp)
		GMAP.addMapType('maproutes', mapRoutesType);
		GMAP.addMapType('satroutes', satRoutesType);
		GMAP.addMapType('blancroutes', blancRoutesType);
		GMAP.addMapType('map', G_NORMAL_MAP);
		GMAP.addMapType('satellite', G_SATELLITE_MAP);
		GMAP.addMapType('hybrid', G_HYBRID_MAP);
		GMAP.addMapType('blanc', G_MAP_GREY);
		// Create the Google Map
		GMAP.createGMap = function(divId) {		
			GMAP.mapDiv = DH.getObject(divId);
			var mapOpts = {
				size: GMAP.getMapSize()
				// mapTypes: GMAP.mapTypes
			}
		// GLog.write('after' + GMAP.getMapSize().toString());
			GMAP.map = new GMap2(GMAP.mapDiv, mapOpts);
			for (var i in GMAP.mapTypes) {
				GMAP.map.addMapType(GMAP.mapTypes[i]);
			}		
		}
		
		
		GMAP.createGMap('map');
	//	alert('test');
		//GMAP.map.addControl(new GOverviewMapControl());
		GMAP.map.setCenter(new GLatLng(52.37261, 4.900435), 9, GMAP.mapTypes['maproutes']);
		GMAP.map.addControl(new GLargeMapControl(),
           	 new GControlPosition(G_ANCHOR_TOP_LEFT, new GSize(600, 10)));

	}
}
