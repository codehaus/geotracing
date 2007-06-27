var ROUTE = {

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

	createRoute: function() {
		var params = new Array();
		params[KW.DIWI.BESLOTEN_PARAM] = ROUTE.sliders['s1'].getValue();
		params[KW.DIWI.HALFOPEN_PARAM] = ROUTE.sliders['s2'].getValue();
		params[KW.DIWI.OPEN_PARAM] = ROUTE.sliders['s3'].getValue();
		params[KW.DIWI.BEDRIJVEN_PARAM] = ROUTE.sliders['s4'].getValue();
		params[KW.DIWI.BEWONING_PARAM] = ROUTE.sliders['s5'].getValue();
		params[KW.DIWI.BOS_PARAM] = ROUTE.sliders['s6'].getValue();
		params[KW.DIWI.HEIDE_PARAM] = ROUTE.sliders['s7'].getValue();
		params[KW.DIWI.GRASLAND_PARAM] = ROUTE.sliders['s8'].getValue();
		params[KW.DIWI.ZEE_PARAM] = ROUTE.sliders['s9'].getValue();
		params[KW.DIWI.SLOTEN_PARAM] = ROUTE.sliders['s10'].getValue();

		params[KW.DIWI.STARTPUNT_PARAM] = document.getElementById("startpunt").value;
		params[KW.DIWI.EINDPUNT_PARAM] = document.getElementById("eindpunt").value;
		params[KW.DIWI.THEMA_PARAM] = document.getElementById("thema").value;
		params[KW.DIWI.AFSTAND_PARAM] = document.getElementById("afstand").value;
//		params[KW.DIWI.WANDELAAR_PARAM] = document.getElementById("wandelen").checked;

		KW.DIWI.generateroute(ROUTE.onCreateRouteRsp, params);
		DIWIAPP.pr('uw persoonlijke route wordt gegenereerd...');

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

	createThemesForm: function(elm) {
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

		var ni = DH.getObject('thema-lb');
		ni.innerHTML = themesString;
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
		MAP.create();
		MAP.addRouteLayer(ROUTE.generatedRouteId);
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
