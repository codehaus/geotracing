// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Test KWClientExt.js.
 *
 * $Id$
 */
var LOCAPP = {

	loadForm: function(aFormFile) {
		LOCAPP.pr('load form: ' + aFormFile);
		DH.getURL(aFormFile, LOCAPP.onLoadForm);
	},


	onLoadForm: function(aFormText) {
		LOCAPP.pr('onLoadForm');
		DH.setHTML('session', aFormText)
	},

	startSession: function() {
		LOCAPP.pr('start');
		KW.init(LOCAPP.onRsp, LOCAPP.onNegRsp, 60);
		LOCAPP.loadForm('locationapp/login-form.html');
		GEvent.addListener(GMAP.map, "click", function(marker, point) {
			if (marker) {
				GMAP.map.removeOverlay(marker);
			} else {
				var addLocationForm = DH.getObject('addlocationform');
				if (addLocationForm) {
					GMAP.map.addOverlay(new GMarker(point));
					addLocationForm.lon.value = point.lng();
					addLocationForm.lat.value = point.lat();
				}

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
		return false;
	},

	pr: function (s) {
		var elm = document.getElementById('result');
		elm.innerHTML = elm.innerHTML + '<br/>' + s;
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

	addLocationReq: function(lon, lat, relateids) {
		// <add-location-req relateids="123,456,789" lon="4.99' lat="54.45/>
		var req = KW.createRequest('loc-create-req');
		KW.UTIL.setAttr(req, 'lon', lon);
		KW.UTIL.setAttr(req, 'lat', lat);
		KW.UTIL.setAttr(req, 'relateids', relateids);
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
		LOCAPP.addLocationReq(lon, lat, id);
	},


	onRsp: function(elm) {
		if (!elm) {
			LOCAPP.pr('empty response');
			return;
		}
		LOCAPP.pr('server response ' + elm.tagName);
		if (elm.tagName == 'login-rsp') {
			KW.selectApp('geoapp', 'user'); // of guest
		} else if (elm.tagName == 'select-app-rsp') {
			LOCAPP.pr('login OK');
			DH.setHTML('session', 'ingelogd');
			LOCAPP.loadForm('locationapp/add-location-form.html')
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



