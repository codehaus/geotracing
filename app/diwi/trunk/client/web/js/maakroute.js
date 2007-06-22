var s1,s2,s3,s4,s5,s6,s7,s8,s9,s10;

function initMakeRouteForm() {
	KW.CMS.getstartpoints(maakStartpuntForm);
	KW.CMS.getendpoints(maakEindpuntForm);
	KW.CMS.getthemes(maakThemasForm);
	maakAfstandForm();
	DH.getObject("wandelen").checked = true;
	DIWIAPP.pr('	Stel uw eigen route samen naar uw eigen voorkeuren! Klik op "maakroute" en er wordt een zo nauwkeurig mogelijke route voor u gemaakt, geheel naar eigen wensen. Let wel op! Wanneer u een nieuwe route maakt, wordt de vorige overschreven.');
	s1 = new Slider(document.getElementById("slider1"), document.getElementById("s1input"));
	s1.setMinimum(0);
	s1.setMaximum(100);
	s1.onchange = function () {
		document.getElementById("s1value").value = s1.getValue();
	};
	s1.setValue(0);

	s2 = new Slider(document.getElementById("slider2"), document.getElementById("s2input"));
	s2.setMinimum(0);
	s2.setMaximum(100);
	s2.onchange = function () {
		document.getElementById("s2value").value = s2.getValue();
	};
	s2.setValue(0);

	s3 = new Slider(document.getElementById("slider3"), document.getElementById("s3input"));
	s3.setMinimum(0);
	s3.setMaximum(100);
	s3.onchange = function () {
		document.getElementById("s3value").value = s3.getValue();
	};
	s3.setValue(0);

	s4 = new Slider(document.getElementById("slider4"), document.getElementById("s4input"));
	s4.setMinimum(0);
	s4.setMaximum(100);
	s4.onchange = function () {
		document.getElementById("s4value").value = s4.getValue();
	};
	s4.setValue(0);

	s5 = new Slider(document.getElementById("slider5"), document.getElementById("s5input"));
	s5.setMinimum(0);
	s5.setMaximum(100);
	s5.onchange = function () {
		document.getElementById("s5value").value = s5.getValue();
	};
	s5.setValue(0);

	s6 = new Slider(document.getElementById("slider6"), document.getElementById("s6input"));
	s6.setMinimum(0);
	s6.setMaximum(100);
	s6.onchange = function () {
		document.getElementById("s6value").value = s6.getValue();
	};
	s6.setValue(0);

	s7 = new Slider(document.getElementById("slider7"), document.getElementById("s7input"));
	s7.setMinimum(0);
	s7.setMaximum(100);
	s7.onchange = function () {
		document.getElementById("s7value").value = s7.getValue();
	};
	s7.setValue(0);

	s8 = new Slider(document.getElementById("slider8"), document.getElementById("s8input"));
	s8.setMinimum(0);
	s8.setMaximum(100);
	s8.onchange = function () {
		document.getElementById("s8value").value = s8.getValue();
	};
	s8.setValue(0);

	s9 = new Slider(document.getElementById("slider9"), document.getElementById("s9input"));
	s9.setMinimum(0);
	s9.setMaximum(100);
	s9.onchange = function () {
		document.getElementById("s9value").value = s9.getValue();
	};
	s9.setValue(0);

	s10 = new Slider(document.getElementById("slider10"), document.getElementById("s10input"));
	s10.setMinimum(0);
	s10.setMaximum(100);
	s10.onchange = function () {
		document.getElementById("s10value").value = s10.getValue();
	};
	s10.setValue(0);
}


function maakStartpuntForm(elm) {
	var start_punt_string = '<p>Startpunt</p><form ><select style="width:120px;" id="startpunt"><option value="0" selected="selected">geen voorkeur</option>';

	var start_punt_list = elm.getElementsByTagName('name');
	var n_start_punt = start_punt_list.length;
	var j;
	for (i = 0; i < n_start_punt; i++) {
		j = i + 1;
		start_punt_string += '<option value="' + j + '" style="width:120px;">';
		start_punt_string += start_punt_list.item(i).firstChild.nodeValue;
		start_punt_string += '</option>';
	}
	start_punt_string += '</select></form>';

	var ni = document.getElementById('startpunt-lb');
	ni.innerHTML = start_punt_string;

}

function maakEindpuntForm(elm) {
	var eind_punt_string = '<p>Eindpunt</p><form><select style="width:120px;" id="eindpunt" ><option value="0" selected="selected">geen voorkeur</option>';

	var eind_punt_list = elm.getElementsByTagName('name');
	var n_eind_punt = eind_punt_list.length;
	var j;
	for (i = 0; i < n_eind_punt; i++) {
		j = i + 1;
		eind_punt_string += '<option value=' + j + '>';
		eind_punt_string += eind_punt_list.item(i).firstChild.nodeValue;
		eind_punt_string += '</option>';
	}
	eind_punt_string += '</select></form>';

	var ni = document.getElementById('eindpunt-lb');
	ni.innerHTML = eind_punt_string;

}


function maakThemasForm(elm) {
	var themesString = '<p>Thema\'s</p><form><select style="width:120px;" id="thema" ><option value="0" selected="selected">geen voorkeur</option>';
	var themes_list = elm.getElementsByTagName('theme');
	var n_themes = themes_list.length;
	var j;
	for (i = 0; i < n_themes; i++) {
		j = i + 1;
		themesString += '<option value=' + j + '>';
		themesString += themes_list.item(i).firstChild.nodeValue;
		themesString += '</option>';
	}
	themesString += '</select></form>';

	var ni = document.getElementById('thema-lb');
	ni.innerHTML = themesString;
}

function maakAfstandForm() {
	var afstandString = '<p>Afstand</p><form><select id="afstand" ><option value="0" selected="selected">geen voorkeur</option><option value="3" >3 km</option><option value="5" >5 km</option><option value="10" >10 km</option><option value="15" >15 km</option><option value="20" >20 km</option></select></form>';

	var ni = document.getElementById('afstand-lb');
	ni.innerHTML = afstandString;
}

function selectWandelen() {
	document.getElementById("wandelen").checked = true;
	document.getElementById("fietsen").checked = false;
}

function selectFietsen() {
	document.getElementById("wandelen").checked = false;
	document.getElementById("fietsen").checked = true;
}


function maakGenRoutesList(elm) {
	generatedString = '';
	for (i = 1; i < 5; i++) {
		generatedString += '<p><a href=\"\">' + i + '-de route </a></p>';
	}
	var ni = document.getElementById('generated-routes');
	ni.innerHTML = generatedString;
}

function verwerkGenRoute(elm) {
	var route = elm.firstChild;
	var name = route.getElementsByTagName('name')[0].firstChild.nodeValue;
	var description = route.getElementsByTagName('description')[0].firstChild.nodeValue;
	var routeString = 'Naam van de route: ' + name + '<br/>Eigenschappen:' + description;
	DIWIAPP.pr(routeString);
	DIWINAV.loadPage('pages/routemap.html');
	showRouteMap(elm.firstChild.getAttribute('id'));
}

function verwerkPlaatje(elm) {
	imageString = '<a href="javascript:DIWIAPP.getBigMap()" ><img src="' + unescape(elm.getAttribute('url')) + '"></a>';

	DH.setHTML('generated-routes-image', imageString);
}

function verwerkBigPlaatje(elm) {
	imageString = '<img src="' + unescape(elm.getAttribute('url')) + '">';

	DH.setHTML('generated-routes-image', imageString);

}

var lon = 5.3790321;
var lat = 52.171682;
var x = 155500;
var y = 462500;
var zoom = 3;
var map, layer1,layer2,layer3,layer4,layer5,layer6;

function showRouteMap(aRouteId) {
	/* map = new OpenLayers.Map( $('map'),
				{controls: [new OpenLayers.Control.MouseDefaults(), new OpenLayers.Control.PanZoomBar()],
					maxResolution: 1.40625/2,
					numZoomLevels: 17}); */

	//<BoundingBox SRS="EPSG:28992" minx="3944.0000652443" miny="309541.000249053" maxx="276149.999934756"
	//		 maxy="599336.999750947"/>
	var mapDiv = DH.getObject('map');
	map = new OpenLayers.Map(mapDiv, {
		controls: [new OpenLayers.Control.MouseDefaults(), new OpenLayers.Control.PanZoomBar()],
		maxExtent: new OpenLayers.Bounds( 4.724717,51.813062,5.752441,52.486596),
		projection: "EPSG:4326", 'maxResolution':'auto', numZoomLevels: 17});
	//layer1 = new OpenLayers.Layer.WMS( "OpenLayers WMS",
	//		  "http://labs.metacarta.com/wms/vmap0", {layers: 'basic'} );
	//map.addLayer(layer1);

	// PostGIS routes projected on topo-map
	var wmsURL = 'http://test.digitalewichelroede.nl/map84';
//	var sld = 'http://88.198.19.182/mapserver/topnl/select-route.sld';

/*	layer1 = new OpenLayers.Layer.WMS.Untiled("Geese - All Routes",
			wmsURL, {layers: 'topnl_geese,all_diwi_routes'});
	map.addLayer(layer1);

	layer2 = new OpenLayers.Layer.WMS.Untiled("Geese - Single Route",
			wmsURL, {layers: 'topnl_geese,single_diwi_route', id: aRouteId, format: 'image/png'});
	map.addLayer(layer2);
*/
	layer3 = new OpenLayers.Layer.WMS.Untiled("Alleen Kaart",
			wmsURL, {layers: 'topnl_raster', format: 'image/png'});
	map.addLayer(layer3);

	layer4 = new OpenLayers.Layer.WMS.Untiled("Kaart plus Route",
			wmsURL + '?ID=' + aRouteId + '&LAYERS=topnl_raster,single_diwi_route');
	map.addLayer(layer4);
	map.setBaseLayer(layer4)

	map.setCenter(new OpenLayers.LonLat(lon, lat), zoom);
	map.addControl(new OpenLayers.Control.LayerSwitcher());

	/*	layer5 = new OpenLayers.Layer.WMS.Untiled( "Single Route Only",
						  wmsURL, {layers: 'routes', sld: sld} );
				map.addLayer(layer5);

				layer6 = new OpenLayers.Layer.WMS.Untiled( "All Routes Only",
						  wmsURL, {layers: 'all_skeeler_routes'} );
				map.addLayer(layer6);       */


	/* wmsURL = 'http://www.geese.nl/milcon?';
				layer3 = new OpenLayers.Layer.WMS.Untiled( "TopNL Geese",
						  wmsURL, {layers: 't250,t50,t25,t10'} );
				map.addLayer(layer3);  */

	/*		ww = new OpenLayers.Layer.WorldWind( "Bathy",
			 "http://worldwind25.arc.nasa.gov/tile/tile.aspx?", 36, 4,
			 {T:"bmng.topo.bathy.200406"});
			 ww2 = new OpenLayers.Layer.WorldWind( "LANDSAT",
				 "http://worldwind25.arc.nasa.gov/tile/tile.aspx", 2.25, 4,
				 {T:"105"});
			map.addLayer(ww);
			map.addLayer(ww2);      */

	//layer2 = new OpenLayers.Layer.WMS( "Routes",
	//		  "http://suwa:8080/geoserver/wms",
	//	{layers: 'topp:g_route', format: 'image/png',
	//	 transparent: 'true'});
	//map.addLayer(layer2);

}

