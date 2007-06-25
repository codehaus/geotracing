var s1,s2,s3,s4,s5,s6,s7,s8,s9,s10;
var genRouteId;

function initMakeRouteForm() {
	KW.CMS.getstartpoints(maakStartpuntForm);
	KW.CMS.getendpoints(maakEindpuntForm);
	KW.CMS.getthemes(maakThemasForm);
	maakAfstandForm();
	DH.getObject("wandelen").checked = true;
	DIWIAPP.pr('	Stel uw eigen route samen naar uw eigen voorkeuren! Klik op "maakroute" en er wordt een zo nauwkeurig mogelijke route voor u gemaakt, geheel naar eigen wensen. Let wel op! Wanneer u een nieuwe route maakt, wordt de vorige overschreven.');
	s1 = new Slider(DH.getObject("slider1"), DH.getObject("s1input"));
	s1.setMinimum(0);
	s1.setMaximum(100);
	s1.onchange = function () {
		DH.getObject("s1value").value = s1.getValue();
	};
	s1.setValue(0);

	s2 = new Slider(DH.getObject("slider2"), DH.getObject("s2input"));
	s2.setMinimum(0);
	s2.setMaximum(100);
	s2.onchange = function () {
		DH.getObject("s2value").value = s2.getValue();
	};
	s2.setValue(0);

	s3 = new Slider(DH.getObject("slider3"), DH.getObject("s3input"));
	s3.setMinimum(0);
	s3.setMaximum(100);
	s3.onchange = function () {
		DH.getObject("s3value").value = s3.getValue();
	};
	s3.setValue(0);

	s4 = new Slider(DH.getObject("slider4"), DH.getObject("s4input"));
	s4.setMinimum(0);
	s4.setMaximum(100);
	s4.onchange = function () {
		DH.getObject("s4value").value = s4.getValue();
	};
	s4.setValue(0);

	s5 = new Slider(DH.getObject("slider5"), DH.getObject("s5input"));
	s5.setMinimum(0);
	s5.setMaximum(100);
	s5.onchange = function () {
		DH.getObject("s5value").value = s5.getValue();
	};
	s5.setValue(0);

	s6 = new Slider(DH.getObject("slider6"), DH.getObject("s6input"));
	s6.setMinimum(0);
	s6.setMaximum(100);
	s6.onchange = function () {
		DH.getObject("s6value").value = s6.getValue();
	};
	s6.setValue(0);

	s7 = new Slider(DH.getObject("slider7"), DH.getObject("s7input"));
	s7.setMinimum(0);
	s7.setMaximum(100);
	s7.onchange = function () {
		DH.getObject("s7value").value = s7.getValue();
	};
	s7.setValue(0);

	s8 = new Slider(DH.getObject("slider8"), DH.getObject("s8input"));
	s8.setMinimum(0);
	s8.setMaximum(100);
	s8.onchange = function () {
		DH.getObject("s8value").value = s8.getValue();
	};
	s8.setValue(0);

	s9 = new Slider(DH.getObject("slider9"), DH.getObject("s9input"));
	s9.setMinimum(0);
	s9.setMaximum(100);
	s9.onchange = function () {
		DH.getObject("s9value").value = s9.getValue();
	};
	s9.setValue(0);

	s10 = new Slider(DH.getObject("slider10"), DH.getObject("s10input"));
	s10.setMinimum(0);
	s10.setMaximum(100);
	s10.onchange = function () {
		DH.getObject("s10value").value = s10.getValue();
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

	var ni = DH.getObject('startpunt-lb');
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

	var ni = DH.getObject('eindpunt-lb');
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

	var ni = DH.getObject('thema-lb');
	ni.innerHTML = themesString;
}

function maakAfstandForm() {
	var afstandString = '<p>Afstand</p><form><select id="afstand" ><option value="0" selected="selected">geen voorkeur</option><option value="3" >3 km</option><option value="5" >5 km</option><option value="10" >10 km</option><option value="15" >15 km</option><option value="20" >20 km</option></select></form>';

	var ni = DH.getObject('afstand-lb');
	ni.innerHTML = afstandString;
}

function selectWandelen() {
	DH.getObject("wandelen").checked = true;
	DH.getObject("fietsen").checked = false;
}

function selectFietsen() {
	DH.getObject("wandelen").checked = false;
	DH.getObject("fietsen").checked = true;
}


function maakGenRoutesList(elm) {
	generatedString = '';
	for (i = 1; i < 5; i++) {
		generatedString += '<p><a href=\"\">' + i + '-de route </a></p>';
	}
	var ni = DH.getObject('generated-routes');
	ni.innerHTML = generatedString;
}

function verwerkGenRoute(elm) {
	genRouteId = elm.firstChild.getAttribute('id');
	var route = elm.firstChild;
	var name = route.getElementsByTagName('name')[0].firstChild.nodeValue;
	var description = route.getElementsByTagName('description')[0].firstChild.nodeValue;
	var routeString = '<h2>' + name + '</h2>' + description;
	DIWIAPP.pr(routeString);
}

function verwerkPlaatje(elm) {
	DIWINAV.loadPage('pages/routemap.html');
	MAP.create();
	MAP.addRouteLayer(genRouteId);
}
