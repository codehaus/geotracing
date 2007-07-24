/**
 * Route generation and such.
 *
 * createroute command:
 - startx (integer, RD)
 - starty (integer, RD)
 - endx (integer, RD)
 - endy (integer, RD)
 - type ('cycling'of 'walking')
 - afstand (integer; mogelijke waardes:
 * 3000,5000 (=wandelen)
 * 10000,15000,20000(=fietsen)
 - besloten (integer, >= 0)
 - halfopen (integer, >= 0)
 - open (integer, >= 0)
 - industrie (integer, >= 0)
 - bebouwd (integer, >= 0)
 - bos (integer, >= 0)
 - heizand (integer, >= 0)
 - natuurgras (integer, >= 0)
 - grootwater (integer, >= 0)
 - kleinwater  (integer, >= 0)
 - poi (mag achterwege gelaten worden, wordt niets mee gedaan.
 Gehandhaafd voor toekomstig gebruik)

 De parameters startx, starty, endx, endy en type zijn verplicht;
 Als er een afstand wordt opgegeven dan moeten alle omgevingstypen een
 waarde hebben (>=0).
 *
 * author: Just van den Broecke
 */
var ROUTE = {
	fixedRoutes: null,
	sliders: new Array(),
	startEndPOIs: null,
	selectedStartEndPOI: null,
	startPOIs: null,
	endPOIs: null,

	createDistanceForm: function () {
		var formHTML = '<p>Wandelen/fietsen en afstand</p><form><select id="afstand" ><option value="-1" selected="selected">kies...</option><option value="3000" >wandelen 3 km</option><option value="5000" >wandelen 5 km</option><option value="10000" >fietsen 10 km</option><option value="15000" >fietsen 15 km</option><option value="20000" >fietsen 20 km</option></select></form>';

		DH.setHTML('afstand-lb', formHTML);
	},

	createEndPOIsForm: function(rspXML) {
		var formHTML = '<p>Eindpunt</p><form><select style="width:120px;" id="eindpunt" ><option value="-1" selected="selected">geen voorkeur</option>';

		ROUTE.endPOIs = ROUTE.rsp2Records(rspXML);

		for (i = 0; i < ROUTE.endPOIs.length; i++) {
			formHTML += '<option value="' + i + '">';
			formHTML += ROUTE.endPOIs[i].getField('name');
			formHTML += '</option>';
		}
		formHTML += '</select></form>';

		DH.setHTML('eindpunt-lb', formHTML);
	},

	creatFixedRoutesForm: function(records) {
		var formHTML = '<form><select onChange="ROUTE.showFixedRoute(this.value)"><option name="frnone" value="-1" selected="selected" >Kies een route...</option>';
		if (records != null) {
			ROUTE.fixedRoutes = new Array();
			for (i = 0; i < records.length; i++) {
				ROUTE.fixedRoutes[records[i].id] = records[i];
				formHTML += '<option name="fr' + records[i].id + '" value="' + records[i].id + '" >';
				formHTML += records[i].getField('name');
				formHTML += '</option>';
			}
		}
		formHTML += '</select></form>';
		DH.setHTML('vasteroutes', formHTML);
		MAP.show();
		MAP.addPOILayer();
		MAP.addUGCLayer();
		DIWIAPP.pr('Selecteer hiernaast een van de vaste routes. Punten:<br/>rood: Points of Interest<br/>oranje: start/eindpunten<br/>blauw: media van gebruikers.');
	},

	createGenerateRouteForm: function () {
		// KW.CMS.getstartendpoints(ROUTE.createStartPOIsForm);
		KW.CMS.getstartendpoints(ROUTE.createStartEndPOIsForm);
		//KW.CMS.getthemes(ROUTE.createThemesForm);
		ROUTE.createDistanceForm();

		//DH.getObject("wandelen").checked = true;
		//DH.getObject("fietsen").checked = false;

		//	DH.getObject("wandelen").checked = true;
		DIWIAPP.pr('Maak uw eigen route naar uw eigen voorkeuren! Klik op "maakroute" en er wordt een zo nauwkeurig mogelijke route voor u gemaakt, geheel naar eigen wensen. Let wel op! Wanneer u een nieuwe route maakt, wordt de vorige overschreven.');

		// Create and configure the sliders
		for (var i = 1; i < 11; i++) {
			ROUTE.createSlider('s' + i);
		}
	},

	createSlider: function(id) {
		var slider = new Slider(DH.getObject(id), DH.getObject(id + 'input'));
		slider.setMinimum(0);
		slider.setMaximum(100);
		slider.setValue(0);

		var sliderValue = DH.getObject(id + 'value');
		slider.onchange = function () {
			sliderValue.value = slider.getValue();
		}

		// Store "omgevingstype" in slider
		slider.omgeving = sliderValue.parentNode.id;

		sliderValue.onchange = function () {
			slider.setValue(parseInt(sliderValue.value));
		}
		ROUTE.sliders[id] = slider;
	},

	createStartPOIsForm: function(rspXML) {
		var formHTML = '<p>Startpunt</p><form ><select style="width:120px;" id="startpunt"><option value="-1" selected="selected">geen voorkeur</option>';

		ROUTE.startPOIs = ROUTE.rsp2Records(rspXML);

		for (i = 0; i < ROUTE.startPOIs.length; i++) {
			formHTML += '<option value="' + i + '">';
			formHTML += ROUTE.startPOIs[i].getField('name');
			formHTML += '</option>';
		}

		formHTML += '</select></form>';
		DH.setHTML('startpunt-lb', formHTML);
	},

	createStartEndPOIsForm: function(rspXML) {
		var formHTML = '<p>Start/Eindpunt</p><form ><select style="width:120px;" id="starteindpunt"><option value="-1" selected="selected">kies...</option>';

		ROUTE.startEndPOIs = ROUTE.rsp2Records(rspXML);

		for (i = 0; i < ROUTE.startEndPOIs.length; i++) {
			formHTML += '<option value="' + i + '">';
			formHTML += ROUTE.startEndPOIs[i].getField('name');
			formHTML += '</option>';
		}

		formHTML += '</select></form>';
		DH.setHTML('starteindpunt-lb', formHTML);
	},

	createThemesForm: function(rspXML) {
		var formHTML = '<p>Thema\'s</p><form><select style="width:120px;" id="thema" ><option value="-1" selected="selected">geen voorkeur</option>';
		var themes = rspXML.getElementsByTagName('theme');
		for (i = 0; i < themes.length; i++) {
			formHTML += '<option value="' + i + '">';
			formHTML += themes[i].firstChild.nodeValue;
			formHTML += '</option>';
		}
		formHTML += '</select></form>';
		DH.setHTML('thema-lb', formHTML);
	},

	generateRoute: function() {
		// http://137.224.112.237/diwirouting/RoutingServlet?request=createroute&startx=166463&starty=470532&endx=169816&endy=447368&poi=Erfscheiding&afstand=20000&besloten=20
		var params = new Array();
		// params['request'] = 'createroute';

		// Start-point RD coordinates
		var i = DH.getObject('starteindpunt').value;
		if (i < 0) {
			DIWIAPP.pr('U dient een start/eindpunt te kiezen.');
			return;
		}

		ROUTE.selectedStartEndPOI = ROUTE.startEndPOIs[i];
		params[KW.DIWI.STARTX_PARAM] = Math.round(ROUTE.startEndPOIs[i].getField('x'));
		params[KW.DIWI.STARTY_PARAM] = Math.round(ROUTE.startEndPOIs[i].getField('y'));
		params[KW.DIWI.ENDX_PARAM] = Math.round(ROUTE.startEndPOIs[i].getField('x'));
		params[KW.DIWI.ENDY_PARAM] = Math.round(ROUTE.startEndPOIs[i].getField('y'));

		// POI theme
		/* var themeElm = DH.getObject('thema');
		params[KW.DIWI.THEMA_PARAM] = 'niets';
		if (themeElm.selectedIndex > 0) {
			// Theme value is value of displayed option in drop down
			params[KW.DIWI.THEMA_PARAM] = themeElm.options[themeElm.selectedIndex].childNodes[0].nodeValue;
		} */

		// Distance in meters
		var distance = DH.getObject('afstand').value;
		if (!distance || distance < 0) {
			DIWIAPP.pr('U dient een afstand te kiezen.');
			return;
		}

		if (distance && distance > 0) {
			params[KW.DIWI.AFSTAND_PARAM] = distance;
		}

		// All "omgeving" slider values
		var slider, sliderVal;
		for (i in ROUTE.sliders) {
			slider = ROUTE.sliders[i];
			sliderVal = slider.getValue();
			params[slider.omgeving] = 0;
			if (sliderVal > 0) {
				params[slider.omgeving] = sliderVal;
			}
		}


		params['type'] = distance < 10000 ? 'walking' : 'cycling';

		KW.DIWI.generateroute(ROUTE.onCreateRouteRsp, params);
		DIWIAPP.pr('even geduld, uw persoonlijke route wordt gegenereerd...');

	},

	onCreateRouteRsp: function(xmlRsp) {
		ROUTE.generatedRouteId = xmlRsp.firstChild.getAttribute('id');
		var route = xmlRsp.firstChild;
		if (!route || route.childNodes.length == 0) {
			DIWIAPP.pr('Helaas, er kon geen route gemaakt worden met de door u ingebrachte gegevens. Probeert u het nog een keer met andere gegevens.');
			return;
		}
		DIWINAV.loadPage('pages/routemap.html');
		MAP.show();

		SRV.get('q-diwi-route-info', ROUTE.onQueryRouteInfo, 'id', ROUTE.generatedRouteId);
		// KW.DIWI.getmap(ROUTE.onGetRouteMapRsp, ROUTE.generatedRouteId, 580, 400);
	},

	onGetRouteMapRsp: function(xmlRsp) {
		DIWINAV.loadPage('pages/routemap.html');
		MAP.show();
		MAP.addRouteLayer(ROUTE.generatedRouteId);
	},

	onQueryRouteInfo: function(records) {
		var routeRec = records[0];
		var name = routeRec.getField('name');
		var description = routeRec.getField('description');
		var routeString = '<h2>' + name + '</h2>' + description + '<br/>afstand: ' + routeRec.getField('distance') / 1000 + ' km';
		DIWIAPP.pr(routeString);
		MAP.addRouteLayer(routeRec);
	},

	selectWandelen: function() {
		DH.getObject("wandelen").checked = true;
		DH.getObject("fietsen").checked = false;
	},

	selectFietsen: function() {
		DH.getObject("wandelen").checked = false;
		DH.getObject("fietsen").checked = true;
	},

	showFixedRoutes: function() {
		SRV.get('q-diwi-routes', ROUTE.creatFixedRoutesForm, 'type', 'fixed');
	},

	showFixedRoute: function(optionValue) {
		if (!optionValue || optionValue == -1) {
			return;
		}

		var record = ROUTE.fixedRoutes[optionValue];
		var content = '<h2>' + record.getField('name') + '</h2>';
		content += record.getField('description');

		DIWIAPP.pr(content);
		SRV.get('q-diwi-route-info', ROUTE.onQueryRouteInfo, 'id', record.id);
	},

	rsp2Records: function(anRsp) {
		var records = [];

		// Convert xml doc to array of Record objects
		var recordElements = anRsp.childNodes;
		for (i = 0; i < recordElements.length; i++) {
			records.push(new XMLRecord(recordElements[i]));
		}
		return records;
	}
}

