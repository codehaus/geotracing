/**
 * Main application: starts all and handles session.
 *
 * Author: Just van den Broecke
 */
var DIWIAPP = {
	userName: null,
	personId: null,
	swapImageIndex: 1+ Math.round(5*Math.random()),
	hbTimer: null,
	PORTAL: '/diwi',
   	autoLogin: false,

	/** Swap images in top. */
	imageSwap: function() {
		DH.getObject('plaatje').src = 'media/images/0' + DIWIAPP.swapImageIndex + '.jpg';
		if (DIWIAPP.swapImageIndex++ == 6) {
			DIWIAPP.swapImageIndex = 1;
		}
	},

// Initialization of all KWClient and all application objects.
	init: function() {
		DIWIAPP.pr('init...');

		// Change portal base url for test
		if (document.location.href.indexOf('test.digitalewichelroede.nl') != -1) {
			DIWIAPP.PORTAL = '/diwitest';
		}

		SRV.init();
		SRV.url = DIWIAPP.PORTAL + '/srv/get.jsp?';
		// KeyWorx client with
		// callbacks (2x)
		// server timeout in minutes
		// server root path /diwi
		KW.init(DIWIAPP.onRsp, DIWIAPP.onNegRsp, 2, DIWIAPP.PORTAL);
		DIWINAV.init();
		// DIWIAPP.pr('init done');
		// MAP.init();
		var accData = KW.getAccountData();
		if (accData != null && accData[0] != '' && accData[0].length > 0) {
			var loginForm = DH.getObject('loginform');
			loginForm.name.value = accData[0];
			loginForm.password.value = accData[1];
			DIWIAPP.login();
		}

		DIWIAPP.imageSwap();

		Date.MONTHS = [
				'Januari', 'Februari', 'Maart', 'April', 'Mei', 'Juni', 'Juli',
				'Augustus', 'September', 'October', 'November', 'December'
				];

		Date.DAYS = [
				'Zondag', 'Maandag', 'Dinsdag', 'Woensdag',
				'Donderdag', 'Vrijdag', 'Zaterdag'
				];

		// Swap images in top (just for fun)
		window.setInterval(DIWIAPP.imageSwap, 30000);
	},

// called from form submit
	login: function() {
		DIWIAPP.pr('inloggen...');
		var loginForm = DH.getObject('loginform');
		DIWIAPP.userName = loginForm.name.value;
		var password = loginForm.password.value;

		// Call KWClient
		KW.login(DIWIAPP.userName, password);
		DIWIAPP.pr('login sent');
		return false;
	},

	logout: function() {
		DIWIAPP.pr('uitloggen...');
		KW.clearAccount();
		// KeyWorx client
		KW.logout();
		return false;
	},

	restoreSession: function() {
		SRV.init();
		SRV.url = DIWIAPP.PORTAL + '/srv/get.jsp?';
		KW.restoreSession();
	},

/** Send heartbeat. */
	sendHeartbeat: function() {
		var doc = KW.createRequest('echo-req');
		KW.post(DIWIAPP.onRsp, doc);
	},

	toggleAutoLogin: function() {
		DIWIAPP.autoLogin = DH.getObject('autologin').checked;
		if (DIWIAPP.autoLogin == false) {
			KW.clearAccount();
		}
		// DIWIAPP.pr('autologin=' + autologin);
	},

	onRsp: function(elm) {
		if (!elm) {
			DIWIAPP.pr('empty response');
			return;
		}

		// DIWIAPP.pr('server response ' + elm.tagName);
		if (elm.tagName == 'login-rsp') {
			DIWIAPP.pr('login response ok');
			DIWIAPP.personId = elm.getElementsByTagName('personid')[0].childNodes[0].nodeValue;
			KW.selectApp('geoapp', 'user');
		} else if (elm.tagName == 'select-app-rsp') {
			DIWIAPP.hbTimer = window.setInterval('DIWIAPP.sendHeartbeat()', 120000)
			if (DIWIAPP.autoLogin == true) {
				KW.storeAccount();
				KW.storeSession();
			}
 			DIWINAV.onLogin();
		} else if (elm.tagName == 'echo-rsp') {
		} else if (elm.tagName == 'logout-rsp') {
			window.clearInterval(DIWIAPP.hbTimer);
			DIWINAV.onLogout();
		} else {
			DIWIAPP.pr('rsp tag=' + elm.tagName + ' ' + elm);
		}
	},

// KWClient negative response handler.
	onNegRsp: function(errorId, error, details, responseTag) {
		switch (responseTag) {
			case 'route-generate-nrsp':
				DIWIAPP.pr('Helaas, met de door u ingegeven waarden kon geen route worden samengesteld. <br/>Probeert u het nogmaals met andere waarden.');
				break;

			case 'login-nrsp':
				DIWIAPP.pr('Gebruiker of wachtwoord is onbekend');
				break;

			default:
				DIWIAPP.pr('Helaas, er is een onbekende fout opgetreden. De melding is: <br/><i>"' + details + '"</i>');
				break;
		}
	},

// Util for printing/displaying debug output
	pr: function (s) {
		DH.setHTML('balloontext', s);
	},

	setStatus: function (s) {
		DH.setHTML('status', s);
	}
}


// Call DIWIAPP.init() when page is fully loaded
DH.addEvent(window, 'load', DIWIAPP.init, false);

// DH.addEvent(window, 'load', isLoggedIn(), false);
// DH.addEvent(window, 'load', TST.getFixedRoutes(), false);
