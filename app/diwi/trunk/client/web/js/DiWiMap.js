/**
 * Manages OpenLayers Map object.
 *
 * author: Just van den Broecke
 */
var MAP = {
//	BOUNDS: new OpenLayers.Bounds(4.724717, 51.813062, 5.752441, 52.486596),
//	CENTER: new OpenLayers.LonLat(5.3790321, 52.171682),
	BOUNDS: new OpenLayers.Bounds(110000,440000,184000,500000),
//	BOUNDS: new OpenLayers.Bounds(152000,440000,181000,461000),   ORIGINAL
//	CENTER: new OpenLayers.LonLat(167000,450000), ORIGINAL
	CENTER: new OpenLayers.LonLat(152000,460000),
//	BOUNDS: new OpenLayers.Bounds(5.087384, 51.813062, 5.389776, 52.486596),
//	CENTER: new OpenLayers.LonLat(5.23858, 52.171682),
	CONTROLS: [new OpenLayers.Control.MouseDefaults(), new OpenLayers.Control.PanZoomBar(),new OpenLayers.Control.LayerSwitcher(),new OpenLayers.Control.MousePosition()],
	DIV_ID: 'map',
	IMAGE_FORMAT: 'image/png',
	MAX_RESOLUTION: 'auto',
	UNITS: 'meters',
	NUM_ZOOMLEVELS: 10,
	PROJECTION: 'EPSG:28992',
	WMS_URL: '/map',
	WMS_URL_WUR: 'http://geodatakich.wur.nl/wmsconnector/com.esri.wms.Esrimap/DIWI_WMS',
	ZOOM: 0,
/** OL map object. */
	map: null,

/*	currentRouteLayer: null,
	markerLayer: null,
	poiLayer: null,
	*/
	keyArray : new Array(),
	overlays: new Object(),

/** Add Google Map key and reg exp for regexp URL, e.g. "^https?://www.geotracing.com/.*" */
	addKey: function(aName, aKey, aURLRegExp) {
		MAP.keyArray[aName] = { key: aKey, reg: aURLRegExp };
	},

	addMarker: function(x,y,iconImg, w,h) {
		var marker = new OpenLayers.Marker(new OpenLayers.LonLat(x,y), new OpenLayers.Icon(iconImg, new OpenLayers.Size(w,h) ));
 		MAP.overlays['markers'].addMarker(marker);
	},

	addMarkerLayer: function(name) {
		if (MAP.hasOverlay('markers')) {
			MAP.removeOverlay('markers');
		}

		MAP.overlays['markers'] = new OpenLayers.Layer.Markers(name);

		MAP.map.addLayer(MAP.overlays['markers']);
	},

	addPOILayer: function() {
		if (MAP.hasOverlay('pois')) {
			return;
		}

		MAP.overlays['pois'] = new OpenLayers.Layer.WMS.Untiled('Points of Interest (POIs)',
				// MAP.WMS_URL + '?ID=' + aRouteId + '&LAYERS=topnl_raster,single_diwi_route');
				MAP.WMS_URL, {layers: 'diwi_pois', format: MAP.IMAGE_FORMAT, transparent: true});
		MAP.map.addLayer(MAP.overlays['pois']);

		MAP.overlays['startendpoints']  = new OpenLayers.Layer.WMS.Untiled('Start -en eindpunten',
				// MAP.WMS_URL + '?ID=' + aRouteId + '&LAYERS=topnl_raster,single_diwi_route');
				MAP.WMS_URL, {layers: 'diwi_startendpoints', format: MAP.IMAGE_FORMAT, transparent: true});
		MAP.map.addLayer(MAP.overlays['startendpoints']);
	},

	addUGCLayer: function() {
		if (MAP.hasOverlay('ugc')) {
			return;
		}
		MAP.overlays['ugc'] = new OpenLayers.Layer.WMS.Untiled('Media van gebruikers',
				// MAP.WMS_URL + '?ID=' + aRouteId + '&LAYERS=topnl_raster,single_diwi_route');
				MAP.WMS_URL, {layers: 'diwi_ugc', format: MAP.IMAGE_FORMAT, transparent: true});
		MAP.map.addLayer(MAP.overlays['ugc']);
	},

	addRouteLayer: function(routeRec) {
		var id = routeRec.getField('id');
		var bboxArr = routeRec.getField('bbox').split(',');
		var bounds = new OpenLayers.Bounds(bboxArr[0], bboxArr[1], bboxArr[2], bboxArr[3]);
		MAP.map.zoomToExtent(bounds);
		MAP.removeOverlays();

		MAP.overlays['route'] = new OpenLayers.Layer.WMS.Untiled('Route (#' + id + ')',
				// MAP.WMS_URL + '?ID=' + aRouteId + '&LAYERS=topnl_raster,single_diwi_route');
				MAP.WMS_URL, {id: id, layers: 'diwi_routes_sel', format: MAP.IMAGE_FORMAT, transparent: true});

		MAP.map.addLayer(MAP.overlays['route'] );

		var pois = routeRec.getField('pois');
		if (pois != null) {
			MAP.overlays['routepois'] = new OpenLayers.Layer.WMS.Untiled('Route POIs',
					// MAP.WMS_URL + '?ID=' + aRouteId + '&LAYERS=topnl_raster,single_diwi_route');
					MAP.WMS_URL, {id: pois, layers: 'diwi_pois_sel', format: MAP.IMAGE_FORMAT, transparent: true});

			MAP.map.addLayer(MAP.overlays['routepois'] );
		}

		MAP.addUGCLayer();
	},

	addTOPNLRasterLayer: function() {
		var topNLLayer = new OpenLayers.Layer.WMS.Untiled("Topografische Kaart",
				MAP.WMS_URL_WUR, {layers: 'id1,id2,id3,id4,id5,id6,id7,id8,id9,id10,id11,id12,id13', format: MAP.IMAGE_FORMAT});
				//MAP.WMS_URL, {layers: 'topnl_raster', format: MAP.IMAGE_FORMAT});

		MAP.map.addLayer(topNLLayer);
	},

	addGoogleSatLayer: function() {
		var googles = new OpenLayers.Layer.Google("Google Maps Satelliet", { 'type': G_SATELLITE_MAP, 'maxZoomLevel':18 });

		MAP.map.addLayer(googles);
	},

	create: function() {
		// MAP.destroy();
		MAP.map = new OpenLayers.Map(DH.getObject(MAP.DIV_ID), {
			controls: MAP.CONTROLS,
			maxExtent: MAP.BOUNDS,
			projection: MAP.PROJECTION,
			units: MAP.UNITS,
			maxResolution: MAP.MAX_RESOLUTION,
			'numZoomLevels': MAP.NUM_ZOOMLEVELS
		}
		);

//		MAP.addGoogleSatLayer();
		MAP.addTOPNLRasterLayer();
		// MAP.map.events.register("click", MAP.map, MAP.onClick);
		MAP.overlays['markers'] = null;
		MAP.overlays['ugc'] = null;
		MAP.overlays['pois'] = null;
		MAP.overlays['startendpoints'] = null;
		MAP.overlays['route'] = null;
	},

	destroy: function() {
		if (MAP.map != null) {
			// MAP.map.destroy();
			// DH.setHTML(MAP.DIV_ID, ' ');
			MAP.map = null;
		}
	},

	hasOverlay: function(id) {
		return MAP.overlays[id] && MAP.overlays[id] != null;
	},

	hide: function() {
		DH.displayOff('map');
	},


	init: function() {
		MAP.map.setCenter(MAP.CENTER, MAP.ZOOM);
		MAP.removeOverlays();
	},

	onClick: function(e) {
		var pt = MAP.map.getLonLatFromViewPortPx(e.xy);
		alert('click ' + pt);
	},

	loadGoogleMapScript: function(aVersion) {
		var version = '2';
		if (aVersion) {
			version = aVersion;
		}

		// Find the GMap key based on our URL regular expression
		for (k in MAP.keyArray) {
			var key = MAP.keyArray[k];
			var regexp = new RegExp(key.reg);

			if (regexp.test(window.location.href)) {
				// alert('load key=' + key.key);
				document.write('<' + 'script src="http://maps.google.com/maps?file=api&amp;v=' + version + '&amp;key=' + key.key + '" type="text/javascript"><' + '/script>');
				break;
			}
		}
	},

	removeOverlay: function(id) {
		if (!MAP.hasOverlay(id)) {
			return;
		}
		MAP.map.removeLayer(MAP.overlays[id]);
		MAP.overlays[id] = null;
	},

	removeOverlays: function() {
		for (o in MAP.overlays) {
			MAP.removeOverlay(o);
		}
	},

	show: function() {
		DH.displayOn('map');
		if (MAP.map == null) {
			MAP.create();
		}

		MAP.init();

		// Position map absolute based on anchor position
		DH.setObjectXY('map', DH.getObjectX('mapanchor'), DH.getObjectY('mapanchor'))
		// DH.setObjectWH('pagina', DH.getObjectWidth('pagina'), DH.getObjectHeight('map') + 50)
	}
}

/** Load google maps JS for key matching our server URL.
NOT RELEVANT FOR RD
MAP.addKey('live',
		'ABQIAAAAD3bxjYK2kuWoA5XU4dh89xSegWNS_BtfE0_SbjkW1pkdsveSEhS9935cVFSC9wEMB5FdZntmVMpl2w',
		'^https?://www.digitalewichelroede.nl/.*');

MAP.addKey('test',
		'ABQIAAAAD3bxjYK2kuWoA5XU4dh89xSY8w_XxQ1lplxjwMXbYkbJSPSvpRTetUEIpZ2OTf-U93olm8oLCoC29A',
		'^https?://test.digitalewichelroede.nl/.*');

MAP.addKey('local',
		'ABQIAAAAD3bxjYK2kuWoA5XU4dh89xSzCH91z57ocwwUF0G9rnam-69XfBSYstFMYwQaq5OD5kCUatNyH_JFqw',
		'^https?://local.diwi.nl/.*');

MAP.loadGoogleMapScript();   */