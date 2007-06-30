/**
 * Main application: starts all and handles session.
 *
 * Author: Just van den Broecke
 */
var DIWIAPP = {
	userName: null,
	swapImageIndex: 1+ Math.round(5*Math.random()),


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
		SRV.init();
		SRV.url = '/diwi/srv/get.jsp?';
		// KeyWorx client with
		// callbacks (2x)
		// server timeout in minutes
		// server root path /diwi
		KW.init(DIWIAPP.onRsp, DIWIAPP.onNegRsp, 60, '/diwi');
		DIWINAV.init();
		DIWIAPP.pr('init done');
		// MAP.init();
		var accData = KW.getAccountData();
		if (accData != null && accData[0] != '') {
			var loginForm = DH.getObject('loginform');
			loginForm.name.value = accData[0];
			loginForm.password.value = accData[1];
			DIWIAPP.login();
		}

		DIWIAPP.imageSwap();
		// Swap images in top (just for fun)
		window.setInterval(DIWIAPP.imageSwap, 20000);
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
		// KW.clearAccount();
		// KeyWorx client
		KW.logout();
		return false;
	},

	restoreSession: function() {
		SRV.init();
		SRV.url = '/diwi/srv/get.jsp?';
		KW.restoreSession();
	},

	onRsp: function(elm) {
		if (!elm) {
			DIWIAPP.pr('empty response');
			return;
		}

		DIWIAPP.pr('server response ' + elm.tagName);
		if (elm.tagName == 'login-rsp') {
			DIWIAPP.pr('login response ok');
			KW.selectApp('geoapp', 'user');
		} else if (elm.tagName == 'select-app-rsp') {

			KW.storeAccount();
			KW.storeSession();
			DIWINAV.onLogin();
		} else if (elm.tagName == 'logout-rsp') {
			DIWINAV.onLogout();
		} else {
			DIWIAPP.pr('rsp tag=' + elm.tagName + ' ' + elm);
		}
	},

// KWClient negative response handler.
	onNegRsp: function(errorId, error, details) {
		DIWIAPP.pr('hmm, de server zegt: "' + error + '" en in bijzonder "' + details + '"');
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
