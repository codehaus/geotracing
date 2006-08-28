/**
 * Google Maps hacks and utils for version 2 of the GMap API.
 *
 * - displaying WMS map-data
 * - transparent WMS overlaying
 * - adding custom labels (TLabel)
 * - utility methods (speed, distance etc)
 *
 * Merge of several JS files (needs cleanup!).
 *
 * version: $Id$
 *
 */

// Generic Google Map functions (may move this to generic file);
var GMAP = {
	map: null,
	mapDiv: null,
	mapTypes: [],
	defaultCenter: new GLatLng(52.37261, 4.900435),
	defaultZoom: 10,
	defaultMapName: 'satellite',
	DEGREES_PER_RADIAN: 360 / (2 * Math.PI),
	RAD_PER_DEGREE: 0.01745566,

	addMapType: function(aName, aSpec) {
		GMAP.mapTypes[aName] = aSpec;
	},

	setMapType: function(aName) {
		GMAP.map.setMapType(GMAP.mapTypes[aName]);
	},

	getMapSize: function(aName) {
		return new GSize(DH.getObjectWidth(GMAP.mapDiv), DH.getObjectHeight(GMAP.mapDiv));
	},

	setDefaultMapParms: function(aCenter, aZoom, aMapName) {
		GMAP.defaultCenter = aCenter;
		GMAP.defaultZoom = aZoom;
		GMAP.defaultMapName = aMapName;
	},

	showMap: function() {
		// Optional page parameters: map type and/or zoom level and/or center
		// e.g. Manchester UK:
		// http://test.geotracing.com/gt/?center=-2.25,53.4833&map=satellite&zoom=14&cmd=live
		var center = DH.getPageParameter('center', null);
		if (center == null) {
			center = GMAP.defaultCenter;
			// new GLatLng(52.37261, 4.900435);
		} else {
			var llArr = center.split(',');
			center = new GLatLng(llArr[1], llArr[0]);
		}

		var mapName = DH.getPageParameter('map', GMAP.defaultMapName);

		// NOTE: zoom parm somehow does not work!!!
		var zoomLevel = DH.getPageParameter('zoom', null);
		if (zoomLevel == null) {
			zoomLevel = GMAP.defaultZoom;
		} else {
			zoomLevel = parseInt(zoomLevel);
		}
		// alert('zoom=' + zoomLevel)

		// Display map with center, zoom level and map type
		GMAP.map.setCenter(center, zoomLevel, GMAP.mapTypes[mapName]);
	},

/** Create Google Map object. */
	createGMap: function(divId) {
		GMAP.mapDiv = DH.getObject(divId);
		// GLog.write('before' + GMAP.getMapSize().toString());

		GMAP.resize();
		/*		var mapSpecs = new Array();
				if (GMAP.mapTypes.length > 0) {
					// GMap expects normal (non-associative array)
					for (var i in GMAP.mapTypes) {
						mapSpecs.push(GMAP.mapTypes[i]);
					}
				}  */

		var mapOpts = {
			size: GMAP.getMapSize()
			// mapTypes: GMAP.mapTypes
		}
		// GLog.write('after' + GMAP.getMapSize().toString());
		GMAP.map = new GMap2(GMAP.mapDiv, mapOpts);
		for (var i in GMAP.mapTypes) {
			GMAP.map.addMapType(GMAP.mapTypes[i]);
		}
	},

// Is GPoint within GBounds ?
	isInBox: function(pt, bounds) {
		return (pt.x > bounds.minX && pt.y > bounds.minY && pt.x < bounds.maxX && pt.y < bounds.maxY);
	},

// Calculate great-circle distance between GLatLng points
	distance: function(a, b) {
		// Could be...
		if (a.x == b.x && a.y == b.y) {
			return 0.0;
		}
		var rad = 0.01745566;
		Lo = Math.abs(a.x - b.x);
		Drad = Math.acos((Math.sin(a.y * rad) * Math.sin(b.y * rad)) + (Math.abs(Math.cos(a.y * rad)) * Math.abs(Math.cos(b.y * rad)) * Math.cos(Lo * rad)));

		Crad = Math.acos((Math.sin(b.y * rad) - (Math.sin(a.y * rad) * Math.cos(Drad))) / (Math.cos(a.y * rad) * Math.sin(Drad)));
		var Cdeg = Crad * 57.295779;
		return Drad * 57.295779 * 69.06 * 1.6094;
		// KIlometers Just
	},

/*
	  /* This value is the heading you'd leave point 1 at to arrive at  point 2.
 * Inputs and outputs are in radians.

 double heading( double lat1, double lon1, double lat2, double lon2 ) {
   double v1, v2;
   v1 = sin(lon1 - lon2) * cos(lat2);
   v2 = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(lon1 -  lon2);
   // rounding error protection
   if (fabs(v1) < 1e-15) v1 = 0.0;
   if (fabs(v2) < 1e-15) v2 = 0.0;
   return atan2(v1, v2);
 }
 */
/**
 * Calculate heading (course) in degrees between 2 lat/lon points.
 *
 * Credits: Andy Armstrong @ hexten.net for providing the algorithm in C.
 *
 * @param lat1,lon1 (ll of point from)
 * @param lat2,lon2 (ll of point to)
 * @return degrees value between 0 (N) thorugh 180 (S) to 360 (N)
 */
	heading: function(lat1, lon1, lat2, lon2) {
		var v1, v2;
		lat1 = lat1 * GMAP.RAD_PER_DEGREE;
		lon1 = lon1 * GMAP.RAD_PER_DEGREE;
		lat2 = lat2 * GMAP.RAD_PER_DEGREE;
		lon2 = lon2 * GMAP.RAD_PER_DEGREE;

		// The wiz-stuff
		v1 = Math.sin(lon1 - lon2) * Math.cos(lat2);
		v2 = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2);

		// rounding error protection
		if (Math.abs(v1) < 1e-15) {
			v1 = 0.0;
		}

		if (Math.abs(v2) < 1e-15) {
			v2 = 0.0;
		}

		var course = Math.atan2(v1, v2);

		// Course is in radians from -PI to PI
		// Correct for degrees 0..360
		course = course * GMAP.DEGREES_PER_RADIAN

		// hmmm, correct negative values
		if (course < 0) {
			course = -course;
		}

		// hmmmm correct when westbound...
		if (lon2 < lon1) {
			course = 360 - course;
		}

		return course;
	},

// Calculate speed between GLatLng points
	speed: function(a, b) {
		//		return GCdistanceBetween(a, b) / ((b.time - a.time) / 3600000);
		return (a.distanceFrom(b) / 1000) / ((b.time - a.time) / 3600000);

	},

/** Position GOverviewMapControl */
	positionOverview: function () {
		var mapOverview = DH.getObject('map_overview');
		DH.setObjectXY(mapOverview, DH.getObjectWidth(GMAP.mapDiv) - DH.getObjectWidth(mapOverview) - 4, DH.getObjectTop(GMAP.mapDiv) + 4);
		/*    var omap=document.getElementById("map_overview");
				omap.style.left = x+"px";
				omap.style.top = y+"px";

				// == restyling ==
				omap.firstChild.style.border = "1px solid gray";

				omap.firstChild.firstChild.style.left="4px";
				omap.firstChild.firstChild.style.top="4px";
				omap.firstChild.firstChild.style.width="190px";
				omap.firstChild.firstChild.style.height="190px";   */
	},

	resize: function() {
		var topAnchor = DH.getObject("topanchor");
		var bottomAnchor = DH.getObject("bottomanchor");
		DH.setObjectXYWH(GMAP.mapDiv, 0, DH.getObjectY(topAnchor), DH.getObjectX(bottomAnchor), DH.getInsideWindowHeight() - DH.getObjectY(topAnchor));

		if (GMAP.map) {
			GMAP.map.checkResize();
			if (DH.getObject('map_overview')) {
				/** See http://www.econym.demon.co.uk/googlemaps/examples/map13.htm */
				setTimeout("GMAP.positionOverview()", 1);
			}
			// GMAP.map.checkResize();
		}
	}
}


/*
 * Create GoogleMap WMS (transparent) layers on any GoogleMap.
 *
 * Just van den Broecke - just AT justobjects.nl - www.justobjects.nl - www.geoskating.com
 *
 * This (experimental) code can be downloaded from
 * http://www.geotracing.com/gt/script/gmap.js
 *
 * CREDITS
 * This code is based on and inspired by:
 * Brian Flood - http://www.spatialdatalogic.com/cs/blogs/brian_flood/archive/2005/07/11/39.aspx and
 * Kyle Mulka - http://blog.kylemulka.com/?p=287
 * I have merely merged the two approaches taken by each of these great minds !
 *
 * EXAMPLE
 *   // Fake WMS server to be used for overlaying map with transparent GIF
 *   // Use a real WMS server here.
 *   var WMS_URL_ROUTE='http://www.geoskating.com/gmap/route-wms.jsp?';
 *
 *   // Create WMSSpec
 *   // need: wmsURL, gName, gShortName, wmsLayers, wmsStyles, wmsFormat, [wmsVersion], [wmsBgColor], [wmsSrs]
 *   var G_MAP_WMS_SPEC = createWMSSpec(WMS_URL_ROUTE, "MyWMS", "MyWMS", "routes", "default", "image/gif", "1.0.0");
 *
 *   // Use WMSSpec to create transparent overlay on a standard Google MapSpec
 *   var G_MAP_WMS_OVERLAY_SPEC = createWMSOverlaySpec(G_SATELLITE_TYPE, G_MAP_WMS_SPEC, "MyOvWMS", "MyOvWMS");
 *
 *   // Create mapspecs array
 *   var mapSpecs = [];
 *   mapSpecs.push(G_SATELLITE_TYPE);
 *   mapSpecs.push(G_MAP_WMS_SPEC);
 *   mapSpecs.push(G_MAP_WMS_OVERLAY_SPEC);
 *
 *   // Setup the map
 *   var map = new GMap(document.getElementById("map"), mapSpecs);
 *   map.addControl(new GMapTypeControl());
 *   map.addControl(new GSmallMapControl());
 *   map.setMapType(G_SATELLITE_TYPE);
 *   map.setCenter(new GPoint(4.9, 52.35), 10);
 */



/*
Call generic wms service for GoogleMaps v2
John Deck, UC Berkeley
Inspiration & Code from:
	Mike Williams http://www.econym.demon.co.uk/googlemaps2/ V2 Reference & custommap code
	Brian Flood http://www.spatialdatalogic.com/cs/blogs/brian_flood/archive/2005/07/11/39.aspx V1 WMS code
	Kyle Mulka http://blog.kylemulka.com/?p=287  V1 WMS code modifications
	http://search.cpan.org/src/RRWO/GPS-Lowrance-0.31/lib/Geo/Coordinates/MercatorMeters.pm
*/

var MAGIC_NUMBER = 6356752.3142;
var DEG2RAD = 0.0174532922519943;
var PI = 3.14159267;
function dd2MercMetersLng(p_lng) {
	return MAGIC_NUMBER * (p_lng * DEG2RAD);
}

function dd2MercMetersLat(p_lat) {
	if (p_lat >= 85) p_lat = 85;
	if (p_lat <= -85) p_lat = -85;
	return MAGIC_NUMBER * Math.log(Math.tan(((p_lat * DEG2RAD) + (PI / 2)) / 2));
}

CustomGetTileUrl = function(a, b, c) {
	if (typeof(window['this.myMercZoomLevel']) == "undefined") this.myMercZoomLevel = 0;
	if (typeof(window['this.myStyles']) == "undefined") this.myStyles = "";
	var lULP = new GPoint(a.x * 256, (a.y + 1) * 256);
	var lLRP = new GPoint((a.x + 1) * 256, a.y * 256);
	var lUL = G_NORMAL_MAP.getProjection().fromPixelToLatLng(lULP, b, c);
	var lLR = G_NORMAL_MAP.getProjection().fromPixelToLatLng(lLRP, b, c);
	// switch between Mercator and DD if merczoomlevel is set
	if (this.myMercZoomLevel != 0 && map.getZoom() < this.myMercZoomLevel) {
		var lBbox = dd2MercMetersLng(lUL.lngDegrees) + "," + dd2MercMetersLat(lUL.latDegrees) + "," + dd2MercMetersLng(lLR.lngDegrees) + "," + dd2MercMetersLat(lLR.latDegrees);
		var lSRS = "EPSG:54004";
	} else {
		var lBbox = lUL.x + "," + lUL.y + "," + lLR.x + "," + lLR.y;
		var lSRS = "EPSG:4326";
	}
	var lURL = this.myBaseURL;
	lURL += "&REQUEST=GetMap";
	lURL += "&SERVICE=WMS";
	lURL += "&VERSION=" + this.myVersion;
	lURL += "&LAYERS=" + this.myLayers;
	lURL += "&STYLES=" + this.myStyles;
	lURL += "&FORMAT=" + this.myFormat;
	lURL += "&BGCOLOR=" + this.myBgColor;
	lURL += "&TRANSPARENT=TRUE";
	lURL += "&SRS=" + lSRS;
	lURL += "&BBOX=" + lBbox;
	lURL += "&WIDTH=256";
	lURL += "&HEIGHT=256";
	lURL += "&reaspect=false";
	return lURL;
}


/*
See http://chignik.berkeley.edu/google/wmstest236.html
var map = new GMap(document.getElementById("map"));

// Create tile layers
var tileCountry = new GTileLayer(new GCopyrightCollection(""), 1, 17);
tileCountry.myLayers = 'country';
tileCountry.myFormat = 'image/gif';
tileCountry.myBaseURL = 'http://chignik.berkeley.edu/cgi-bin/mapserv440?map=/usr/local/web/html/google/wms.map&';
tileCountry.getTileUrl = CustomGetTileUrl;
//tileCountry.getOpacity = function() {return 0.5;}

var tileCounty = new GTileLayer(new GCopyrightCollection(""), 1, 17);
tileCounty.myLayers = 'us_county';
tileCounty.myFormat = 'image/gif';
tileCounty.myBaseURL = 'http://chignik.berkeley.edu/cgi-bin/mapserv440?map=/usr/local/web/html/google/wms.map&';
tileCounty.getTileUrl = CustomGetTileUrl;
//tileCounty.getOpacity = function() {return 0.5;}

var tileDoq = new GTileLayer(new GCopyrightCollection(""), 1, 17);
tileDoq.myLayers = 'doq';
tileDoq.myFormat = 'image/jpeg';
tileDoq.myBaseURL = 'http://terraservice.net/ogcmap.ashx?';
tileDoq.getTileUrl = CustomGetTileUrl;

var layer1 = [tileDoq,tileCounty];
var layer2 = [tileCountry,tileCounty];
var layer3 = [G_SATELLITE_MAP.getTileLayers()[0],G_HYBRID_MAP.getTileLayers()[1],tileCounty];

var custommap1 = new GMapType(layer1, G_SATELLITE_MAP.getProjection(), "WMS 3", G_SATELLITE_MAP);
var custommap2 = new GMapType(layer2, G_SATELLITE_MAP.getProjection(), "WMS 2", G_SATELLITE_MAP);
var custommap3 = new GMapType(layer3, G_SATELLITE_MAP.getProjection(), "WMS 1", G_SATELLITE_MAP);

map.getMapTypes().length = 0;
map.addMapType(custommap3);
map.addMapType(custommap2);
map.addMapType(custommap1);

map.setCenter(new GLatLng(37.8, -122.4819), 11);
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());

// Test adding a polyline (this caused problems in previous version of code)
var pointArray = new Array(0);
pointArray.push(new GLatLng(37.81, -122.48));
pointArray.push(new GLatLng(37.81, -122.4));
pointArray.push(new GLatLng(37.89, -122.4));
pointArray.push(new GLatLng(37.89, -122.48));
pointArray.push(new GLatLng(37.81, -122.48));
var pl = new GPolyline(pointArray, '#ff00ff', 5, .5);
map.addOverlay(pl);
*/

/** Create WMS type spec as a GMap Spec. */
function createWMSSpec(wmsURL, gName, gShortName, wmsLayers, wmsStyles, wmsFormat, wmsVersion, wmsBgColor, wmsSrs) {
	var tile = new GTileLayer(new GCopyrightCollection(""), 1, 17);
	tile.myLayers = wmsLayers;
	tile.myStyles = (wmsStyles ? wmsStyles : "");
	;
	tile.myFormat = (wmsFormat ? wmsFormat : "image/gif");
	;
	tile.myVersion = (wmsVersion ? wmsVersion : "1.1.1");
	tile.myBgColor = (wmsBgColor ? wmsBgColor : "0xFFFFFF");
	tile.myBaseURL = wmsURL;
	tile.getTileUrl = CustomGetTileUrl;
	//tileCounty.getOpacity = function() {return 0.5;}


	var layer = [tile];

	var mapType = new GMapType(layer, G_SATELLITE_MAP.getProjection(), gName, G_SATELLITE_MAP);

	return mapType;
}

/** Create transparent WMS overlay layer on standard GMap Spec. */
function createWMSOverlaySpec(gSpec, wmsSpec, gName, gShortName) {
	// New object
	var overlaySpec = new Object();
	// Override with members of wmsSpec
	for (var m in wmsSpec) {
		overlaySpec[m] = wmsSpec[m];
	}

	// Copy all GMap-spec (e.g. G_SATELLITE_TYPE) object members (attrs+functions)
	for (var m in gSpec) {
		overlaySpec[m] = gSpec[m];
	}

	// Override GmapSpec-specific attrs (not future-proof!)
	overlaySpec.Name = gName;
	overlaySpec.ShortName = gShortName;

	// Override GmapSpec function: Having overlay is the whole purpose!
	overlaySpec.hasOverlay = function () {
		return true;
	};

	// Override GmapSpec function: Gets URL for overlayed tile
	overlaySpec.getOverlayURL = wmsSpec.getTileURL;

	overlaySpec.getLinkText = wmsSpec.getLinkText;

	overlaySpec.getShortLinkText = wmsSpec.getShortLinkText;

	overlaySpec.getCopyright = function() {
		return SIG;
	};

	return overlaySpec;
}

var SIG = '<a style="background-color:#555555; font:10px verdana;text-decoration:none;padding:2px;color:yellow;" href="#" onClick="window.open(\'http://www.geotracing.com\'); return false;">GeoTracing Powered</a>';


