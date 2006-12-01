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

		GMAP.addMapType('streets', G_NORMAL_MAP);
		GMAP.addMapType('satellite', G_SATELLITE_MAP);
		GMAP.addMapType('hybrid', G_HYBRID_MAP);

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
		GMAP.map.setCenter(new GLatLng(52.37261, 4.900435), 9, GMAP.mapTypes['streets']);
		GMAP.map.addControl(new GLargeMapControl(),
           	 new GControlPosition(G_ANCHOR_TOP_LEFT, new GSize(600, 10)));

	}
}
