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
 * version: $Id: GMap.js,v 1.15 2006-07-22 22:51:35 just Exp $
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
			center = GMAP.defaultCenter; // new GLatLng(52.37261, 4.900435);
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

// Calculate speed between GLatLng points
	speed: function(a, b) {
//		return GCdistanceBetween(a, b) / ((b.time - a.time) / 3600000);
		return (a.distanceFrom(b) /1000) / ((b.time - a.time) / 3600000);

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
//		DH.setObjectXYWH(GMAP.mapDiv, 0, DH.getObjectY(topAnchor), '100%', DH.getInsideWindowHeight() - DH.getObjectY(topAnchor)-100);
	//	DH.setObjectXYWH(GMAP.mapDiv, 0, DH.getObjectY(topAnchor), DH.getObjectX(bottomAnchor), 600);
		
/////19 SEPTEMBER 2006 JAN: BOVENSTAAND -30 TOEGEVOEGD VOOR BETER PASSEN.		
		

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


///19 SEPTEMBER 2006::: SORRY JUST EVEN JE SIG WEGGEHAALD VOOR HET OVERNEMEN MLGK DESIGN.
//var SIG = '<a style="background-color:#555555; font:10px verdana;text-decoration:none;padding:2px;color:yellow;" href="#" onClick="window.open(\'http://www.geotracing.com\'); return false;">GeoTracing Powered</a>';
var SIG = '';

// TLabel() GMaps API extension copyright 2005-2006 Tom Mangan (tmangan@gmail.com)
// http://gmaps.tommangan.us/tlabel.html
// free for non-commercial use
//
// NOTE (Just): this version based on TLabel v2.0.1 with changes for
// 1. optional "diffusions": to randomly place colliding icons around 9 positions (diffpos)
// 2. split setPosition() into moveToLatLng() and moveToXY() to allow moving icons based on GLatLng
// 3. removed setting this.anchorLatLng in moveToXY (zoomend callback gives new zoomlevel as arg!)
// 4. replaced calculation for x,y pixels with GMap2.fromLatLngToDivPixel() (ok also in 2.0.3)
// 5. set z-index high (why?)
// 6. use this.map.getPane(G_MAP_MAP_PANE) to fetch mapTray  (ok also in 2.0.3)
// 7. keep element as object var (this.elm)
// 8. no need (afaict) to append elm to document.body (solved performance problems by leaving out)

diffpos = function(x, y) {
	this.x = x;
	this.y = y;
}

// random positions
var diffusions = new Array(new diffpos(-5, -5), new diffpos(0, -5), new diffpos(5, -5), new diffpos(-5, 0), new diffpos(0, 0), new diffpos(5, 0), new diffpos(-5, 5), new diffpos(0, 5), new diffpos(5, 5), new diffpos(10, 5));


function TLabel(diffuse) {
	this.diffuse = false;
	this.elm = null;
	if (diffuse) {
		// to avoid collisions
		this.diffuse = diffuse;
	}
}

TLabel.prototype.initialize = function(a) {
	if (typeof(a.TLabelBugged == 'undefined')) {
		this.addTBug(a);
	}
	this.map = a;
	var b = document.createElement('span');
	b.setAttribute('id', this.id);
	b.innerHTML = this.content;
	// NOT CLEAR WHY ELM SHOULD BE APPENDED TO BODY
	// Only needed for Safari, why ??
	if (navigator.userAgent.toLowerCase().indexOf('safari') != -1) {
		document.body.appendChild(b);
	}
	b.style.position = 'absolute';

	// JUST: changed from 1 to 25000
	b.style.zIndex = 25000;
	if (this.percentOpacity) {
		this.setOpacity(this.percentOpacity);
	}

	this.elm = b; // document.getElementById(this.id);
	this.w = this.elm.offsetWidth;
	this.h = this.elm.offsetHeight;
	this.mapTray = this.map.getPane(G_MAP_MAP_PANE);
	// document.getElementById(a.getContainer().id).firstChild;
	this.mapTray.appendChild(b);
	if (!this.markerOffset) {
		this.markerOffset = new GSize(0, 0);
	}
	this.moveToXY();
	//	GEvent.bind(a, "zoomend", this, this.moveToXY);
	GEvent.bind(a, "moveend", this, this.moveToXY);
}

TLabel.prototype.moveToLatLng = function(aGLatLng) {
	this.anchorLatLng = aGLatLng;
	this.moveToXY();
}

TLabel.prototype.moveToXY = function() {
	//if (a) {
	// GLog.write(a.toString());
	// this.anchorLatLng = a;
	//}
	var b = this.getXY();
	var x = parseInt(b.x);
	var y = parseInt(b.y);
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
	// commented out to allow diffusion
	//	var d = document.getElementById(this.id);
	//	d.style.left = x - this.markerOffset.width + 'px';
	//	d.style.top = y - this.markerOffset.height + 'px';

	var offsetX = 0;
	var offsetY = 0;
	var d = this.elm; // document.getElementById(this.id);
	if (this.diffuse == true) {
		d.style.left = x - this.markerOffset.width + diffusions[Math.floor(Math.random() * 10)].x + 'px';
		d.style.top = y - this.markerOffset.height + diffusions[Math.floor(Math.random() * 10)].y + 'px';
	} else {
		d.style.left = x - this.markerOffset.width + 'px';
		d.style.top = y - this.markerOffset.height + 'px';

	}
}

TLabel.prototype.getXY = function(a, b) {
	return this.map.fromLatLngToDivPixel(this.anchorLatLng);

}
/*
function normSin(a) {
	if (a > 0.9999) {
		a = 0.9999;
	}
	if (a < -0.9999) {
		a = -0.9999;
	}
	return a;
}

TLabel.prototype.getXY = function(a, b) {
	var c = a.getZoom();
	var d = a.getSize();
	var e = a.getCenter();
	with (Math) {
		var pxLng = 128 * pow(2, c) / 180;
		var pxLat = 128 * pow(2, c) / PI;
		var xDif = -(e.x - b.x) * pxLng;
		var g = normSin(sin(b.y * PI / 180));
		var h = normSin(sin(e.y * PI / 180));
		var yDif = (0.5 * log((1 + h) / (1 - h)) - 0.5 * log((1 + g) / (1 - g))) * pxLat;
		var x = round((d.width / 2) + xDif) - parseInt(this.mapTray.style.left);
		var y = round((d.height / 2) + yDif) - parseInt(this.mapTray.style.top);
	}
	return(new GPoint(x, y));
} */
TLabel.prototype.setOpacity = function(b) {
	if (b < 0) {
		b = 0;
	}
	if (b > 100) {
		b = 100;
	}
	var c = b / 100;
	var d = this.elm; // document.getElementById(this.id);
	if (typeof(d.style.filter) == 'string') {
		d.style.filter = 'alpha(opacity:' + b + ')';
	}
	if (typeof(d.style.KHTMLOpacity) == 'string') {
		d.style.KHTMLOpacity = c;
	}
	if (typeof(d.style.MozOpacity) == 'string') {
		d.style.MozOpacity = c;
	}
	if (typeof(d.style.opacity) == 'string') {
		d.style.opacity = c;
	}
}

TLabel.prototype.addTBug = function(a) {
	if (typeof(a.TLabelBugged) == 'undefined') {
		var b = document.createElement('div');
		b.id = 'TLabelBug';
		b.style.position = 'absolute';
		b.style.right = '0px';
		if (a.TBugged > 0) {
			b.style.bottom = '32px';
		} else {
			b.style.bottom = '20px';
		}
		b.style.backgroundColor = '#f2efe9';
		b.style.zIndex = 25500;
		b.innerHTML = '<a href="http://gmaps.tommangan.us/tlabel.html" style="font:10px verdana;text-decoration:none;margin:0px;padding:2px;color:#000;">Made with TLabel</a>';
		document.getElementById(a.getContainer().id).appendChild(b);
		var c = 0.7;
		var d = document.getElementById(b.id);
		if (typeof(d.style.filter) == 'string') {
			d.style.filter = 'alpha(opacity:' + c * 100 + ')';
		}
		if (typeof(d.style.KHTMLOpacity) == 'string') {
			d.style.KHTMLOpacity = c;
		}
		if (typeof(d.style.MozOpacity) == 'string') {
			d.style.MozOpacity = c;
		}
		if (typeof(d.style.opacity) == 'string') {
			d.style.opacity = c;
		}
		a.TLabelBugged = 1;
	}
}

GMap2.prototype.addTLabel = function(a) {
	a.initialize(this);
}

GMap2.prototype.removeTLabel = function(a) {
	//var b = document.getElementById(a.id);
	this.getPane(G_MAP_MAP_PANE).removeChild(a.elm);
	// 	document.getElementById(this.getContainer().id).firstChild.removeChild(b);
	delete(b);
}
