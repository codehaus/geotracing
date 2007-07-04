/**
 * Route generation and such.
 *
 * author: Just van den Broecke
 */
var ROUTE = {
	fixedRoutes: null,
	sliders: new Array(),
	startPOIs: null,
	endPOIs: null,

	createDistanceForm: function () {
		var formHTML = '<p>Afstand</p><form><select id="afstand" ><option value="0" selected="selected">geen voorkeur</option><option value="3000" >3 km</option><option value="5000" >5 km</option><option value="10000" >10 km</option><option value="15000" >15 km</option><option value="20000" >20 km</option></select></form>';

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
		var optionStr = ' ';
		if (records != null) {
			ROUTE.fixedRoutes = new Array();
			for (i = 0; i < records.length; i++) {
				ROUTE.fixedRoutes[records[i].id] = records[i];
				optionStr += '<option name="fr' + records[i].id +'" value="' + records[i].id + '" onClick="ROUTE.showFixedRoute(this)">';
				optionStr += records[i].getField('name');
				optionStr += '</option>';
			}
		}

		DH.setHTML('fixed_routes_form', optionStr);
		MAP.show();
	},

	createGenerateRouteForm: function () {
		KW.CMS.getstartpoints(ROUTE.createStartPOIsForm);
		KW.CMS.getendpoints(ROUTE.createEndPOIsForm);
		KW.CMS.getthemes(ROUTE.createThemesForm);
		ROUTE.createDistanceForm();

	//	DH.getObject("wandelen").checked = true;
		DIWIAPP.pr('Maak uw eigen route naar uw eigen voorkeuren! Klik op "maakroute" en er wordt een zo nauwkeurig mogelijke route voor u gemaakt, geheel naar eigen wensen. Let wel op! Wanneer u een nieuwe route maakt, wordt de vorige overschreven.');

		// Create and configure the sliders
		for (var i=1; i < 11; i++) {
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
		var i = DH.getObject('startpunt').value;
		if (i > -1) {
			params[KW.DIWI.STARTX_PARAM] = Math.round(ROUTE.startPOIs[i].getField('x'));
			params[KW.DIWI.STARTY_PARAM] = Math.round(ROUTE.startPOIs[i].getField('y'));
		}

		// End-point RD coordinates
		i = DH.getObject('eindpunt').value;
		if (i > -1) {
			params[KW.DIWI.ENDX_PARAM] = Math.round(ROUTE.endPOIs[i].getField('x'));
			params[KW.DIWI.ENDY_PARAM] = Math.round(ROUTE.endPOIs[i].getField('y'));
		}

		// POI theme
		var themeElm = DH.getObject('thema');
		if (themeElm.selectedIndex > 0) {
			// Theme value is value of displayed option in drop down
			params[KW.DIWI.THEMA_PARAM] = themeElm.options[themeElm.selectedIndex].childNodes[0].nodeValue;
		}

		// Distance in meters
		var distance = DH.getObject('afstand').value;
		if (distance && distance > 0) {
			params[KW.DIWI.AFSTAND_PARAM] = distance;
		}

		// All "omgeving" slider values
		var slider, sliderVal;
		for (i in ROUTE.sliders) {
			slider = ROUTE.sliders[i];
			sliderVal = slider.getValue();
			if (sliderVal > 0) {
				params[slider.omgeving] = sliderVal;
			}

		}

//		params[KW.DIWI.WANDELAAR_PARAM] = document.getElementById("wandelen").checked;

		KW.DIWI.generateroute(ROUTE.onCreateRouteRsp, params);
		DIWIAPP.pr('even geduld, uw persoonlijke route wordt gegenereerd...');

	},

	onCreateRouteRsp: function(xmlRsp) {
		ROUTE.generatedRouteId = xmlRsp.firstChild.getAttribute('id');
		var route = xmlRsp.firstChild;
		var name = route.getElementsByTagName('name')[0].firstChild.nodeValue;
		var description = route.getElementsByTagName('description')[0].firstChild.nodeValue;
		var routeString = '<h2>' + name + '</h2>' + description;
		DIWIAPP.pr(routeString);
		KW.DIWI.getmap(ROUTE.onGetRouteMapRsp, ROUTE.generatedRouteId, 580, 400);
	},

	onGetRouteMapRsp: function(xmlRsp) {
		DIWINAV.loadPage('pages/routemap.html');
		MAP.show();
		MAP.addRouteLayer(ROUTE.generatedRouteId);
	},

	showFixedRoutes: function() {
		SRV.get('q-diwi-routes', ROUTE.creatFixedRoutesForm, 'type', 'fixed');
	},

	showFixedRoute: function(option) {
		if (!option.value) {
			return;
		}

		var record = ROUTE.fixedRoutes[option.value];
		var content = '<h2>' + record.getField('name') + '</h2>';
		content += record.getField('description');

		DIWIAPP.pr(content);
		MAP.addRouteLayer(record.id);
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

/*
function selectWandelen() {
	DH.getObject("wandelen").checked = true;
	DH.getObject("fietsen").checked = false;
}

function selectFietsen() {
	DH.getObject("wandelen").checked = false;
	DH.getObject("fietsen").checked = true;
} */
