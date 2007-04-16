// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Test KWClientExt.js.
 *
 * $Id$
 */

var LOCAPP = {

	currentLon: null,
	currentLat: null,
	currentForm: null,

	latestoverlay:null ,
	loadForm: function(aFormFile) {
		LOCAPP.pr('load form: ' + aFormFile);
		DH.getURL(aFormFile, LOCAPP.onLoadForm);
	},

	loadFormToevoegen: function(aFormFile, lng, lat, form) {
		LOCAPP.pr('load form: ' + aFormFile);
		LOCAPP.currentLon = lng;
		LOCAPP.currentLat = lat;
		LOCAPP.currentForm = form;

		DH.getURL(aFormFile, LOCAPP.onLoadForm);

	},

	onLoadForm: function(aFormText) {
		LOCAPP.pr('onLoadForm');
		DH.setHTML('session', aFormText)
		var toevoegenForm = DH.getObject(LOCAPP.currentForm);
		if (toevoegenForm) {
			//alert(lng);
			toevoegenForm.lon.value = LOCAPP.currentLon;
			toevoegenForm.lat.value = LOCAPP.currentLat;
		}
		GMAP.resize();


	},

	deleteMarker: function() {
		GMAP.map.removeOverlay(latestoverlay);
		GMAP.map.closeInfoWindow();
	},

	createMarker: function(point, icon) {
		// ======== Add a "directions" link ======
		var marker = new GMarker(point, icon);
		//  var htmltekst="<div style='width:20;height:20'><a href='./locationapp/Toevoegen.jsp?punt=" + point + "' target='_blank' style='color:blue'>editeren</a></div>";
		var lng = point.lng();
		var lat = point.lat();
		var htmltekst = "<label onmouseover=this.style.cursor='pointer' onclick=LOCAPP.loadFormToevoegen('locationapp/add-location-form.html'," + lng + "," + lat + ",'addlocationform')><u>upload figuur</u></label><br><label onmouseover=this.style.cursor='pointer' onclick=LOCAPP.loadFormToevoegen('locationapp/Toevoegen-form.html'," + lng + "," + lat + ",'toevoegenform')><u>Kies type punt</u></label><br><label onmouseover=this.style.cursor='pointer' onclick=LOCAPP.deleteMarker()><u>verwijder punt</u></label>";
		GEvent.addListener(marker, "click", function() {
			latestoverlay = marker;
			marker.openInfoWindowHtml(htmltekst);
		});
		return marker;
	} ,

	startSession: function() {
		LOCAPP.pr('start');
		KW.init(LOCAPP.onRsp, LOCAPP.onNegRsp, 60);
		LOCAPP.loadForm('locationapp/login-form.html');

		var icon = new GIcon();
		icon.image = "./images/nieuw.png";
		icon.iconSize = new GSize(20, 34);
		icon.iconAnchor = new GPoint(10, 30);
		icon.infoWindowAnchor = new GPoint(9, 2);

		GEvent.addListener(GMAP.map, "click", function(marker, point) {
			if (!marker) {

				//if (addLocationForm) {
				GMAP.map.addOverlay(LOCAPP.createMarker(point, icon));
				//addLocationForm.lon.value = point.lng();
				//addLocationForm.lat.value = point.lat();
				//}

			}
		});

	},

	login: function() {
		LOCAPP.pr('logging in...');

		var loginForm = document.getElementById('loginform');

		if (loginForm.username.value == "") {
			alert("Please fill in you name.");
			loginForm.username.focus();
			return false;
		}

		if (loginForm.password.value == "") {
			alert("Please fill in your password.");
			loginForm.password.focus();
			return false;
		}

		// Do the login
		KW.login(loginForm.username.value, loginForm.password.value);
		LOCAPP.pr('login sent');
		LOCAPP.loadForm('locationapp/ingelogd.html');
		var doc = KW.createRequest('q-media-by-user');
		return false;

	},

	pr: function (s) {
		/*	var elm = document.getElementById('result');
				elm.innerHTML = elm.innerHTML + '<br/>' + s;*/
	},

/** Send heartbeat. */
	sendHeartbeat: function() {
		LOCAPP.pr('sending heartbeat..');
		var req = KW.createRequest('t-hb-req');
		KW.utopia(req);
	},


	deleteMedium: function (id) {
		LOCAPP.pr('deleting medium...id=' + id);
		KW.MEDIA.del(LOCAPP.onDeleteMediumRsp, id);
	},

	onDeleteMediumRsp: function(elm) {
		LOCAPP.pr('deleted medium OK');
		DH.getObject('uploadedMedium').innerHTML = 'medium deleted';
		DH.getObject('uploadedMediumActions').innerHTML = ' ';
	},

	updateMedium: function (id) {
		LOCAPP.pr('updateMedium...id=' + id);
		KW.MEDIA.update(LOCAPP.onUpdateMediumRsp, id, 'new name', 'new description');
	},

	onUpdateMediumRsp: function(elm) {
		LOCAPP.pr('onUpdateMediumRsp  OK');
		// DH.getObject('uploadedMedium').innerHTML = 'medium updated';
	},

	addLocation: function () {
		LOCAPP.pr('uploading medium...');
		KW.MEDIA.upload(LOCAPP.onUploadMediumRsp, DH.getObject('addlocationform'));
		return false;
	},

	addLocationReq: function(lon, lat, relateids, category) {
		// <add-location-req relateids="123,456,789" lon="4.99' lat="54.45/>
		var req = KW.createRequest('loc-create-req');
		KW.UTIL.setAttr(req, 'lon', lon);
		KW.UTIL.setAttr(req, 'lat', lat);
		KW.UTIL.setAttr(req, 'relateids', relateids);
		KW.UTIL.setAttr(req, 'subtype', category);
		KW.utopia(req);
	},


	onUploadMediumRsp: function(elm) {
		// var rsp = elm.getElementsByTagname('medium-insert-rsp');
		LOCAPP.pr('medium upload: ' + elm.tagName + ' id=' + elm.getAttribute('id'));
		var mediumDiv = DH.getObject('featurepreview');
		var id = elm.getAttribute('id');
		mediumDiv.innerHTML = '<img src="media.srv?id=' + id + '&resize=100" border="0" />';
		var addLocationForm = DH.getObject('addlocationform');
		var lon = addLocationForm.lon.value;
		var lat = addLocationForm.lat.value;
		var label = addLocationForm.categories.value;
		LOCAPP.addLocationReq(lon, lat, id, label);
	},


	onRsp: function(elm) {
		if (!elm) {
			LOCAPP.pr('empty response');
			return;
		}
		LOCAPP.pr('server response ' + elm.tagName);
		if (elm.tagName == 'login-rsp') {
			KW.selectApp('geoapp', 'user');
			// of guest
		} else if (elm.tagName == 'select-app-rsp') {
			LOCAPP.pr('login OK');
			DH.setHTML('session', 'ingelogd');
			// LOCAPP.loadForm('locationapp/add-location-form.html')
		} else if (elm.tagName == 'logout-rsp') {
			LOCAPP.pr('logout OK');
			window.clearInterval(LOCAPP.hbTimer);
		} else if (elm.tagName == 't-hb-rsp') {
			LOCAPP.pr('heartbeat OK');
		} else {
			LOCAPP.pr('rsp tag=' + elm.tagName + ' ' + elm);
		}
		// alert('onSelect name=' + name + ' value=' + value + ' label=' + label);
	},

/** KWClient negative response handler. */
	onNegRsp: function(errorId, error, details) {
		LOCAPP.pr('negative resp:' + error + ' details=' + details);
	}
}



