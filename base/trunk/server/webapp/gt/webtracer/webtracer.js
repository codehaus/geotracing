// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * WebTracer - AJAX-based GeoTracing client.
 *
 * $Id: webtracer.js,v 1.6 2006-08-09 09:53:25 just Exp $
 */

DH.include('Panel.js');
DH.include('Menu.js');
DH.include('KWClient.js');
DH.include('GPS.js');

var WT = {
	gpsPanel: null,
	outputPanel: null,
	loginPanel: null,
	suspended: true,
	hbTimer: null,
	iframe: null,

	init: function() {
		// Setup Panels and Menu
		WT.loginPanel = new Panel('session', '#0000ff', '#ffffff');
		WT.loginPanel.setXY(10, 50);
		WT.loginPanel.setDimension(300, 400);
		WT.loginPanel.setContent('do login from "Session" menu');

		WT.outputPanel = new Panel('session status', '#ff0000', '#ffffff');
		WT.outputPanel.setXY(320, 50);
		WT.outputPanel.setDimension(300, 100);
		WT.outputPanel.setContent('ok. <a href="gpsdoor.html">download GPSDoor</a> if not done already');

		WT.gpsPanel = new Panel('GPS status', '#ff0000', '#ffffff');
		WT.gpsPanel.setXY(320, 160);
		WT.gpsPanel.setDimension(300, 100);
		WT.gpsPanel.setContent('waiting for GPS...');

		WT.menu = new Menu('mainmenu');

		// Start GPSDoor interface
		GPS.start(5000);
		setInterval('WT.checkGPS()', 10000);

		// KeyWorx client
		KW.init(WT.onRsp, WT.onNegRsp, 5, DH.getBaseDir() + '/..');
	},

/** Checks if GPS data was set by GPSDoor. */
	checkGPS: function() {
		// See lib/GPS.js
		var msg = 'no GPS (gpsdoor running?)';
		if (GPS.set == true) {
			msg = 'lon=' + GPS.lon + '<br/>lat=' + GPS.lat;
			if (KW.isLoggedIn() != true) {
				msg += '<br/>NOT writing to track<br/>(do Session|Login and Track|Resume first)'
			} else if (WT.suspended == true) {
				msg += '<br/>NOT writing to track <br/>(do Track|Resume first)'
			} else {
				msg += '<br/>adding GPS point to track...'
				WT.writeTrack(GPS.lon, GPS.lat);
			}
			GPS.set = false;
		}
		WT.gpsPanel.setContent(msg);
	},

/** Add POI to current active Track. */
	addMedium: function () {
		WT.pr('adding medium...');
		document.addmediumform.agentkey.value = KW.agentKey;
		document.addmediumform.submit();
		WT._checkIFrameRsp();
		//var form = DH.getElementById('addmediumform');
		//form.agentkey.value = KW.agentKey;
		// form.submit();
		// WT.pr('add Medium sent name=' + name);
		return false;
	},

/** Add POI to current active Track. */
	addPOI: function () {
		WT.pr('adding POI...');

		var form = document.getElementById('addpoiform');
		var name = form.name.value;
		var type = form.type.value;
		var description = form.description.value;
		var req = KW.createRequest('t-trk-add-poi-req');
		req.documentElement.setAttribute('name', name);
		req.documentElement.setAttribute('type', type);
		req.documentElement.setAttribute('description', description);
		KW.utopia(req);
		WT.pr('add POI sent name=' + name);
		return false;
	},

/** Create new Track. */
	createTrack: function () {
		WT.pr('creating track...');

		var createform = document.getElementById('createform');
		var name = createform.name.value;
		if (name == "") {
			alert("Please fill in track name.");
			createform.name.focus();
			return false;
		}

		var req = KW.createRequest('t-trk-create-req');
		req.documentElement.setAttribute('name', name);
		KW.utopia(req);
		WT.pr('create track sent name=' + name);
		return false;
	},

/** Form handler: login on KeyWorx portal. */
	login: function () {
		WT.pr('logging in...');

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
		WT.pr('login sent');
		return false;
	},

/** Send heartbeat. */
	sendHeartbeat: function() {
		WT.pr('sending heartbeat..');
		var req = KW.createRequest('t-hb-req');
		KW.utopia(req);
	},

/** Write GPS data to current active Track. */
	writeTrack: function (lon, lat) {
		WT.pr('sending sample: lon=' + lon + ' lat=' + lat)
		var trkWriteReq = KW.createRequest('t-trk-write-req');
		var pt = trkWriteReq.createElement('pt');
		pt.setAttribute('lon', lon);
		pt.setAttribute('lat', lat);
		trkWriteReq.documentElement.appendChild(pt);
		KW.utopia(trkWriteReq);
	},

	pr: function(msg) {
		WT.outputPanel.setContent(msg);
	},

/** Menu callback: login. */
	onMenuLogin: function() {
		WT.menu.replaceItem('loginout', 'Logout', 'WT.onMenuLogout');
		WT.loginPanel.loadContent('login-form.html');
	},

/** Menu callback: logout. */
	onMenuLogout: function() {
		WT.menu.replaceItem('loginout', 'Login', 'WT.onMenuLogin');
		KW.logout();
	},

/** Menu callback: create track. */
	onMenuCreateTrack: function () {
		WT.loginPanel.loadContent('create-track-form.html');
	},

/** Menu callback: add Point of Interest. */
	onMenuAddPOI: function () {
		WT.loginPanel.loadContent('add-poi-form.html');
	},

/** Menu callback: add media file. */
	onMenuAddMedium: function () {
		WT.loginPanel.loadContent('add-medium-form.html');
	},

/** Menu callback: suspend Track. */
	onMenuSuspendTrack: function () {
		WT.pr('suspending track...');
		WT.menu.replaceItem('suspendresume', 'Resume', 'WT.onMenuResumeTrack');
		var req = KW.createRequest('t-trk-suspend-req');
		KW.utopia(req);
	},

/** Menu callback: resume Track. */
	onMenuResumeTrack: function () {
		WT.pr('resuming track...');
		WT.menu.replaceItem('suspendresume', 'Suspend', 'WT.onMenuSuspendTrack');
		var req = KW.createRequest('t-trk-resume-req');
		KW.utopia(req);
	},

/** Menu callback: hyperlink with url arg. */
	onMenuLink: function (url) {
		window.open(url);
	},

/** KWClient positive response handler. */
	onRsp: function(elm) {
		if (!elm) {
			WT.pr('empty response');
			return;
		}
		WT.pr('server response ' + elm.tagName);
		if (elm.tagName == 'login-rsp') {
			KW.selectApp('geoapp', 'user');
		} else if (elm.tagName == 'select-app-rsp') {
			WT.pr('login OK');
			WT.hbTimer = window.setInterval('WT.sendHeartbeat()', 25000)
		} else if (elm.tagName == 'logout-rsp') {
			WT.pr('logout OK');
			window.clearInterval(WT.hbTimer);
		} else if (elm.tagName == 't-trk-suspend-rsp') {
			WT.suspended = true;
			WT.pr('track suspended OK');
		} else if (elm.tagName == 't-trk-resume-rsp') {
			WT.suspended = false;
			WT.pr('track resumed OK');
		} else if (elm.tagName == 't-hb-rsp') {
			WT.pr('heartbeat OK');
		} else {
			WT.pr('rsp tag=' + elm.tagName + ' ' + elm);
		}
		// alert('onSelect name=' + name + ' value=' + value + ' label=' + label);
	},

/** KWClient negative response handler. */
	onNegRsp: function(errorId, error, details) {
		WT.pr('negative resp:' + error + ' details=' + details);
	},

	_checkIFrameRsp: function() {
		var iframe = DH.getObject('uploadFrame');
		if (!iframe) {
			WT.pr('cannot get uploadFrame');
			return;
		}

		var iframeDoc = null;

		if (iframe.contentDocument) {
			// For NS6
			iframeDoc = iframe.contentDocument;
		} else if (iframe.contentWindow) {
			// For IE5.5 and IE6
			iframeDoc = iframe.contentWindow.document;
		} else if (iframe.document) {
			// For IE5
			iframeDoc = iframe.document;
		}
		if (iframeDoc == null) {
			WT.pr('iframeDoc == null, recheck..');
			setTimeout('WT._checkIFrameRsp()', 2000);
		} else {
			WT.pr('iframeDoc found !!!');
			// WT.onRsp(iframeDoc.documentElement);
			WT.pr('iframeDoc.innerHTML=' + iframeDoc.documentElement.innerHTML);
		}
	},

// http://developer.apple.com/internet/webcontent/iframe.html
	_createIFrame: function() {
		if (!document.createElement) {
			return true
		}

		if (WT.iframe == null && document.createElement) {
			// create the IFrame and assign a reference to the
			// object to our global variable WT.iframe.
			// this will only happen the first time
			// callToServer() is called
			try {
				var tempIFrame = document.createElement('iframe');
				tempIFrame.setAttribute('id', 'rspFrame');
				tempIFrame.style.border = '0px';
				tempIFrame.style.width = '0px';
				tempIFrame.style.height = '0px';
				WT.iframe = document.body.appendChild(tempIFrame);

				if (document.frames) {
					// this is for IE5 Mac, because it will only
					// allow access to the document object
					// of the IFrame if we access it through
					// the document.frames array
					WT.iframe = document.frames['rspFrame'];
				}
			} catch(exception) {
				// This is for IE5 PC, which does not allow dynamic creation
				// and manipulation of an iframe object. Instead, we'll fake
				// it up by creating our own objects.
				iframeHTML = '\<iframe id="rspFrame" style="';
				iframeHTML += 'border:0px;';
				iframeHTML += 'width:0px;';
				iframeHTML += 'height:0px;';
				iframeHTML += '"><\/iframe>';
				document.body.innerHTML += iframeHTML;
			}
		}
	}

}
DH.onReady = WT.init;



