var username = '';
var DIWI = {
// Initialization of KWClient library
	init: function() {
		DIWI.pr('init...');
		SRV.init();
		SRV.url = '/diwi/srv/get.jsp?';
		// KeyWorx client with
		// callbacks (2x)
		// server timeout in minutes
		// server root path /diwi
		KW.init(DIWI.onRsp, DIWI.onNegRsp, 1, '/diwi');
		DIWI.pr('init done');
	},

// called from form submit
	login: function() {
		DIWI.pr('login start');
		var loginForm = DH.getObject('loginform');
		var name = loginForm.name.value;
		username = loginForm.name.value;
		var password = loginForm.password.value;
		DIWI.pr('login name=' + name + ' password=' + password);

		// Call KWClient
		KW.login(name, password);
		DIWI.pr('login sent');
		return false;
	},
	login_neutral: function() {
		DIWI.pr('login start');
		var name = 'diwi-web';
		var password = 'diwi';
		// Call KWClient
		KW.login(name, password);
		return false;
	},
	logout: function() {
		DIWI.pr('logout start');
		// KeyWorx client
		KW.logout();
		DIWI.pr('logout sent');
		return false;
	},
	restoreSession: function() {
		SRV.init();
		SRV.url = '/diwi/srv/get.jsp?';
		KW.restoreSession();
	},

	getFixedRoutes: function() {
		SRV.get('q-diwi-routes', DIWI.onFixedRoute, 'type', '0');

	},

	onFixedRoute: function(elm) {
		maakVasteRoutesForm(elm);
	},


	onRsp: function(elm) {
		if (!elm) {
			DIWI.pr('empty response');
			return;
		}

		DIWI.pr('server response ' + elm.tagName);
		if (elm.tagName == 'login-rsp') {
			KW.selectApp('geoapp', 'user');
		} else if (elm.tagName == 'select-app-rsp') {

			KW.storeSession();
			// Show new content, here logout form
			DH.toggleDisplay(document.getElementById('butmaakroute'));
			DH.toggleDisplay(document.getElementById('butmijnpagina'));
			DH.hide('loginform');
			toggleLogButton();
			toggleInlogBox();

		} else if (elm.tagName == 'logout-rsp') {
			DIWI.pr('logout OK');
			DH.toggleDisplay(document.getElementById('butmaakroute'));
			DH.toggleDisplay(document.getElementById('butmijnpagina'));
			DH.show('loginform');
		} else if (elm.tagName == 'route-getlist-rsp') {
			maakVasteRoutesForm(elm);
		} else {
			DIWI.pr('rsp tag=' + elm.tagName + ' ' + elm);
		}
	},

// KWClient negative response handler.
	onNegRsp: function(errorId, error, details) {
		DIWI.pr('negative resp:' + error + ' details=' + details);
	},

// Util for printing/displaying debug output
	pr: function (s) {
		// DH.addHTML('result', '<br/>' + s);
	}

}

// Call DIWI.init() when page is fully loaded
DH.addEvent(window, 'load', DIWI.init, false);

// DH.addEvent(window, 'load', isLoggedIn(), false);
// DH.addEvent(window, 'load', TST.getFixedRoutes(), false);
