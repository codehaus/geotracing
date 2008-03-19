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
		/*var formHTML = '<select id="afstand" class="listbox" ><option value="-1" selected="selected">kies...</option><option value="3000" >wandelen 3 km</option><option value="5000" >wandelen 5 km</option><option value="10000" >fietsen 10 km</option><option value="15000" >fietsen 15 km</option><option value="20000" >fietsen 20 km</option></select>';

		DH.setHTML('afstand-lb', formHTML);*/
	},

	createEndPOIsForm: function(rspXML) {
		var formHTML = '<p>Eindpunt</p><form><select id="eindpunt" class="listbox" ><option value="-1" selected="selected">geen voorkeur</option>';

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
			
		/** generate a list of routes */
		if (records != null) {
			var routeListWandel = document.createElement("ul");
			var routeListFiets = document.createElement("ul");
			var routeListVerhaal = document.createElement("ul");
			
			
			var strong1 = document.createElement("strong");
			strong1.appendChild(document.createTextNode("Wandelroutes"));
			routeListWandel.appendChild(strong1);
			
			var strong2 = document.createElement("strong");
			strong2.appendChild(document.createTextNode("Fietsroutes"));
			routeListFiets.appendChild(strong2);
			
			ROUTE.fixedRoutes = new Array();
			for (i = 0; i < records.length; i++) {
		
				var routeItem = document.createElement("li");
				routeItem.className = "route"
				
				var name = records[i].getField('name');
				
				routeItem.appendChild(document.createTextNode(name));
				routeItem.record = records[i];
				
				if(name.search('fiets') != -1)
				{
					routeListFiets.appendChild(routeItem)
					
				}
				else if(name.search('Verhaal') != -1)
				{
					routeListVerhaal.appendChild(routeItem)	
				}
				else
				{
					routeListWandel.appendChild(routeItem)	
				}
				
				$(routeItem).click(function() {
					ROUTE.showFixedRoute(this.record);
				});

			}
			DH.setHTML('right', ""); //clear
			
			/*
			var hint1 = document.createElement("p");
			var strong1 = document.createElement("strong");
			strong1.appendChild(document.createTextNode("Wandelroutes"));
			hint1.appendChild(strong1);
			
			var hint2 = document.createElement("p");
			var strong2 = document.createElement("strong");
			strong2.appendChild(document.createTextNode("Fietsroutes"));
			hint2.appendChild(strong2);
			*/
			
			//document.getElementById("right").appendChild(hint1);
			document.getElementById("right").appendChild(routeListWandel);
			document.getElementById("right").appendChild(routeListVerhaal);
			//document.getElementById("right").appendChild(hint2);
			document.getElementById("right").appendChild(routeListFiets);
			
		}
		
		/** append "hover" functionality to list items */ 
		$(".route").mouseover(function(){
			$(this).addClass("mouseover");
	    }).mouseout(function(){
	      	$(this).removeClass("mouseover");
	    });
		
		//DH.setHTML('right', formHTML);
		//MAP.show();
		MAP.addPOILayer();
		MAP.addUGCLayer();
		/*DIWIAPP.pr('Selecteer hiernaast een van de vaste routes. Punten:<br/>rood: Points of Interest<br/>oranje: start/eindpunten<br/>blauw: media van gebruikers.');*/
	},

	createGenerateRouteForm: function () {
		// KW.CMS.getstartendpoints(ROUTE.createStartPOIsForm);
		KW.CMS.getstartendpoints(ROUTE.createStartEndPOIsForm);
		//KW.CMS.getthemes(ROUTE.createThemesForm);
		ROUTE.createDistanceForm();

		//DH.getObject("wandelen").checked = true;
		//DH.getObject("fietsen").checked = false;

		//	DH.getObject("wandelen").checked = true;
		//DIWIAPP.pr('Maak uw eigen route naar uw eigen voorkeuren!');

		// Create and configure the sliders
		/*for (var i = 1; i < 9; i++) {
			ROUTE.createSlider('s' + i);
		}*/
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
		var formHTML = '<p>Startpunt</p><form ><select class="listbox" id="startpunt"><option value="-1" selected="selected">geen voorkeur</option>';

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
		var formHTML = '<select class="listbox" id="starteindpunt"><option value="-1" selected="selected">kies...</option>';

		ROUTE.startEndPOIs = ROUTE.rsp2Records(rspXML);

		for (i = 0; i < ROUTE.startEndPOIs.length; i++) {
			formHTML += '<option value="' + i + '">';
			formHTML += ROUTE.startEndPOIs[i].getField('name');
			formHTML += '</option>';
		}

		formHTML += '</select>';
		DH.setHTML('starteindpunt-lb', formHTML);
	},

	createThemesForm: function(rspXML) {
		var formHTML = '<p>Thema\'s</p><form><select class="listbox" id="thema" ><option value="-1" selected="selected">geen voorkeur</option>';
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
			DIWIAPP.pr('U dient een start/eindpunt te kiezen.',"route_info");
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
     
        //pref type
        //pref afstand

		// Distance in meters
		var distance = DH.getObject('afstand').value;
		if (!distance || distance < 0) {
			DIWIAPP.pr('U dient een afstand te kiezen.',"route_info");
			return;
		}

		if (distance && distance > 0) {
			params[KW.DIWI.AFSTAND_PARAM] = distance;
		}

		// All "omgeving" checkbox values from params2 table in maakroute
		var checkboxes = $("#right #params2 input"); 
		for(var i=0;i<checkboxes.length;i++)
		{
			var param = checkboxes[i].id;
			if(checkboxes[i].checked == true)
			{
				params[param] = 100;
			}
			else
			{
				params[param] = 0;
			}
		}
		
		// Defaults for non-exposed sliders
		params['grootwater'] = 0;
		params['industrie'] = 0;
		params['open'] = 0;
		params['bebouwd'] = 0;

		params['type'] = document.getElementById('wandelen').checked ? 'walking' : 'cycling';

		KW.DIWI.generateroute(ROUTE.onCreateRouteRsp, params);
		DIWIAPP.pr('even geduld, uw persoonlijke route wordt gegenereerd...',"route_info");

	},

	onCreateRouteRsp: function(xmlRsp) {
		ROUTE.generatedRouteId = xmlRsp.firstChild.getAttribute('id');
		var route = xmlRsp.firstChild;
		if (!route || route.childNodes.length == 0) {
			DIWIAPP.pr('Helaas, met de door u ingegeven waarden kon geen route worden samengesteld. <br/>Probeert u het nogmaals met andere waarden..',"route_info");
			return;
		}
		/*DH.setHTML("main", DH.getURL("pages/routes.html", null));*/
		

		SRV.get('q-diwi-route-info', ROUTE.onQueryGenRouteInfo, 'id', ROUTE.generatedRouteId);
		MAP.show();
		// KW.DIWI.getmap(ROUTE.onGetRouteMapRsp, ROUTE.generatedRouteId, 580, 400);
	},

	onGetRouteMapRsp: function(xmlRsp) {
		//DIWINAV.loadPage('pages/routemap.html');
		//MAP.show();
		MAP.addRouteLayer(ROUTE.generatedRouteId);
	},

	onQueryGenRouteInfo: function(records) {
		var routeRec = records[0];
		var name = routeRec.getField('name');
		var description = routeRec.getField('description');
		var routeString = 'Uw persoonlijke route is gereed! De afstand is ' + Math.round(routeRec.getField('distance') / 1000) + ' km.<br/>Deze route kunt u straks op uw Digitale Wichelroede selecteren. Indien u een andere route wilt genereren klik dan nogmaals op "Maak Route".';
		DIWIAPP.pr(routeString,"route_info");
		MAP.addRouteLayer(routeRec);
	},

	onQueryRouteInfo: function(records) {
		var routeRec = records[0];
		var name = routeRec.getField('name');
		var description = routeRec.getField('description');
		var routeString = '<strong>' + name + '</strong><br><br>' + description + '<br/>afstand: ' + routeRec.getField('distance') / 1000 + ' km';
		DH.setHTML('route_info', routeString);
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

	showFixedRoute: function(record) {
		if (!record ) {
			return;
		}
		
		//alert(optionValue)
		
		//var record = ROUTE.fixedRoutes[optionValue];
		var content = '<h2>' + record.getField('name') + '</h2>';
		content += record.getField('description');
		//alert(content)
		//DIWIAPP.pr(content);
		DH.setHTML('route_info', content);
		
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

