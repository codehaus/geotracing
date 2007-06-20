var username = '';
var DIWIAPP = {
	fixedRoutes: {},

// Initialization of KWClient library
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
	},

// called from form submit
	login: function() {
		DIWIAPP.pr('login start');
		var loginForm = DH.getObject('loginform');
		var name = loginForm.name.value;
		username = loginForm.name.value;
		var password = loginForm.password.value;
		DIWIAPP.pr('login name=' + name + ' password=' + password);

		// Call KWClient
		KW.login(name, password);
		DIWIAPP.pr('login sent');
		return false;
	},

	logout: function() {
		DIWIAPP.pr('logout start');
		// KeyWorx client
		KW.logout();
		DIWIAPP.pr('logout sent');
		return false;
	},

	restoreSession: function() {
		SRV.init();
		SRV.url = '/diwi/srv/get.jsp?';
		KW.restoreSession();
	},

	showFixedRoutes: function() {
		SRV.get('q-diwi-routes', DIWIAPP.creatFixedRoutesForm, 'type', '0');
	},

	creatFixedRoutesForm: function(records) {
		var optionStr = ' ';
		if (records != null) {
			for (i = 0; i < records.length; i++) {
				DIWIAPP.fixedRoutes[records[i].id] = records[i];
				optionStr += '<option name="fr' + records[i].id +'" value="' + records[i].id + '" onClick="DIWIAPP.showFixedRoute(this)">';
				optionStr += records[i].getField('name');
				optionStr += '</option>';
			}
		}
		DH.setHTML('fixed_routes_form', optionStr)
	},

	showFixedRoute: function showFixedRoute(option) {
		if (!option.value) {
			return;
		}

		var record = DIWIAPP.fixedRoutes[option.value];
		var content = '<h2>' + record.getField('name') + '</h2>';
		content += '<p>' + record.getField('description') + '</p>';

		DH.setHTML('fixed_routes_content', content);
	},

	onRsp: function(elm) {
		if (!elm) {
			DIWIAPP.pr('empty response');
			return;
		}

		DIWIAPP.pr('server response ' + elm.tagName);
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
			// window.location.href = "mypage/mypage.html";

		} else if (elm.tagName == 'logout-rsp') {
			DIWIAPP.pr('logout OK');
			DH.toggleDisplay(document.getElementById('butmaakroute'));
			DH.toggleDisplay(document.getElementById('butmijnpagina'));
			DH.show('loginform');
		} else if (elm.tagName == 'route-getlist-rsp') {
			maakVasteRoutesForm(elm);
		} else {
			DIWIAPP.pr('rsp tag=' + elm.tagName + ' ' + elm);
		}
	},

// KWClient negative response handler.
	onNegRsp: function(errorId, error, details) {
		DIWIAPP.pr('negative resp:' + error + ' details=' + details);
	},

// Util for printing/displaying debug output
	pr: function (s) {
		// DH.addHTML('result', '<br/>' + s);
	}

}


// Call DIWIAPP.init() when page is fully loaded
DH.addEvent(window, 'load', DIWIAPP.init, false);

// DH.addEvent(window, 'load', isLoggedIn(), false);
// DH.addEvent(window, 'load', TST.getFixedRoutes(), false);
