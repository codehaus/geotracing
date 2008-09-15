// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * GeoSkating app.
 *
 * PURPOSE
 * Library representing the app. Called by GTAPP.js..
 *
 * Author: Just van den Broecke
 * $Id$
 */

// This file contains specific functions for GeoSkating
var MYAPP = {
	DOC_TITLE: '<img src="img/gs-logo.jpg" border="0"  alt="gs-logo" align="middle" />',
	WINDOW_TITLE: 'GeoTracing - GeoSkating',

	init: function() {
		// Overrule GTApp.js functions here
		GTAPP.createMap = MYAPP.createMap;
		GTW.TRACK_COLOR = '#3366FF';

	},

	start: function() {
		if (DH.getPageParameter('cmd', null) == null) {
			// http://test.geoskating.com/gs/?cmd=showtrack&user=just&id=3804&map=map
			// 3852 (d2) 3866 (d3)
			// GTAPP.mShowHelp("content/appabout.html");
			GTAPP.mShowMediaInBbox(50);
 		}
   },

	createMap: function() {
		GTAPP.blinkStatus('Creating map...');

		var WMS_URL_GREY = 'img/greysquare.jpg?';
		var G_MAP_GREY = createWMSSpec(WMS_URL_GREY, "Blank", "Blank", "bl", "bla", "image/jpeg", "1.1.1");

		var skateMapGetTileUrl = function(tile, zoom) {
			// var khURL = G_SATELLITE_MAP.getTileLayers()[0].getTileUrl(a,b);
			var lURL = "map/gmap-sk8-tile.jsp";
			lURL += "?layer=sk8";
			lURL += "&x=" + tile.x;
			lURL += "&y=" + tile.y;
			lURL += "&z=" + zoom;
//			lURL += khURL.substring(khURL.indexOf('&t'), khURL.length);
//			lURL += "&zoom=" + b;
			return lURL;
		}

		var skateTiles = new GTileLayer(new GCopyrightCollection("GT"), 5, 16);
		skateTiles.getTileUrl = skateMapGetTileUrl;
		skateTiles.isPng = function() {
			return true;
		}
		skateTiles.getOpacity = function() {
			return 1.0;
		}

		var osmGetTileUrl = function(a, b) {
			// http://tile.openstreetmap.org/z/x/y.png;
			return 'http://tile.openstreetmap.org/' + b + '/' + a.x + "/" + a.y + '.png';
		}

		var osmTiles = new GTileLayer(new GCopyrightCollection("GT"), 5, 16);
		osmTiles.getTileUrl = osmGetTileUrl;
		osmTiles.isPng = function() {
			return true;
		}
		osmTiles.getOpacity = function() {
			return 1.0;
		}

		var mapnikGetTileUrl = function(a, b) {
			// http://tile.openstreetmap.org/z/x/y.png;
			return 'http://www.geoskating.com/rsc/mapniktiles/' + b + '/' + a.x + "/" + a.y + '.png';
		}

		var mapnikTiles = new GTileLayer(new GCopyrightCollection("GT"), 5, 16);
		mapnikTiles.getTileUrl = mapnikGetTileUrl;
		mapnikTiles.isPng = function() {
			return true;
		}
		mapnikTiles.getOpacity = function() {
			return 0.8;
		}

		var satLayers = [G_SATELLITE_MAP.getTileLayers()[0], skateTiles];
		var mapLayers = [G_NORMAL_MAP.getTileLayers()[0], skateTiles];
		var osmLayers = [osmTiles, skateTiles];
		var blancLayers = [G_MAP_GREY.getTileLayers()[0], skateTiles];
		var mapnikLayers = [osmTiles, mapnikTiles];

		var satRoutesType = new GMapType(satLayers, G_SATELLITE_MAP.getProjection(), "satroute");
		var mapRoutesType = new GMapType(mapLayers, G_NORMAL_MAP.getProjection(), "maproute");
		var osmRoutesType = new GMapType(osmLayers, G_SATELLITE_MAP.getProjection(), "osmroute");
		var blancRoutesType = new GMapType(blancLayers, G_SATELLITE_MAP.getProjection(), "blancroute");
		var mapnikRoutesType = new GMapType(mapnikLayers, G_SATELLITE_MAP.getProjection(), "osmroute");


		// Add map specs to app (see also menu in index.jsp)
		GMAP.addMapType('maproutes', mapRoutesType);
		GMAP.addMapType('satroutes', satRoutesType);
		GMAP.addMapType('osmroutes', osmRoutesType);
		GMAP.addMapType('blancroutes', blancRoutesType);
		GMAP.addMapType('map', G_NORMAL_MAP);
		GMAP.addMapType('satellite', G_SATELLITE_MAP);
		GMAP.addMapType('hybrid', G_HYBRID_MAP);
		GMAP.addMapType('blanc', G_MAP_GREY);
		GMAP.addMapType('mapnik', mapnikRoutesType);

		// Create the Google Map
		GMAP.createGMap('map');

		GMAP.map.addControl(new GOverviewMapControl());
		GMAP.map.addControl(new GLargeMapControl());
		GMAP.map.addControl(new GScaleControl());
		GMAP.map.setCenter(new GLatLng(52.37261, 4.900435), 8, GMAP.mapTypes['satroutes']);
		GMAP.map.enableContinuousZoom();
		GMAP.map.enableDoubleClickZoom();
 
		GTAPP.showStatus('Map created');
	}
}
