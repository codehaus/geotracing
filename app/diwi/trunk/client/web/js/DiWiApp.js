
var DIWIAPP = {
	userName: null,
	fixedRoutes: {},
	swapImageIndex: 1+ Math.round(5*Math.random()),


	/** Swap images in top. */
	imageSwap: function() {
		DH.getObject('plaatje').src = 'media/images/0' + DIWIAPP.swapImageIndex + '.jpg';
		if (DIWIAPP.swapImageIndex++ == 6) {
			DIWIAPP.swapImageIndex = 1;
		}
	},

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

		DH.setHTML('fixed_routes_form', optionStr);
		MAP.create();
	},

	showFixedRoute: function(option) {
		if (!option.value) {
			return;
		}

		var record = DIWIAPP.fixedRoutes[option.value];
		var content = '<h2>' + record.getField('name') + '</h2>';
		content += record.getField('description');

		DIWIAPP.pr(content);
		MAP.addRouteLayer(record.id);
	},


	getBigMap: function() {
		KW.DIWI.getmap(DIWIAPP.onMapRsp, DIWIAPP.currentGeneratedRouteId, 640, 480);
	},

	onMapRsp: function(elm) {
		if (!elm) {
			return;
		}
		verwerkPlaatje(elm);
		//document.getElementById("maakroutebox").style.display = 'block';
		// document.getElementById("leguitbox").style.display = 'none';
		//document.getElementById("leguittext").style.display = 'none';
		//document.getElementById("leguitbullet").style.display = 'none';
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
			// Show new content, here logout form
			DH.getStyleObject('butmaakroute').display = 'block';
			DH.getStyleObject('butmijnpagina').display = 'block';
			DH.getStyleObject('butinloggen').display = 'none';
			DH.getStyleObject('butuitloggen').display = 'block';
			DH.hide('loginform');
			DH.getStyleObject('inlogbox').display = 'none';
			DH.getStyleObject('inlogform').display = 'none';
			DIWIAPP.setStatus('ingelogd als ' + DIWIAPP.userName);
			DIWIAPP.pr('ingelogd als ' + DIWIAPP.userName);
			DIWINAV.buttons['b8'].onSelect();
		} else if (elm.tagName == 'logout-rsp') {
			DIWIAPP.pr('logout OK');
			DH.getStyleObject('butmaakroute').display = 'none';
			DH.getStyleObject('butmijnpagina').display = 'none';
			DH.getStyleObject('butinloggen').display = 'block';
			DH.getStyleObject('butuitloggen').display = 'none';
			DH.show('loginform');
			DIWIAPP.setStatus('niet ingelogd');
			DIWINAV.buttons['b1'].onSelect();
		} else if (elm.tagName == 'route-getlist-rsp') {
			maakVasteRoutesForm(elm);
		} else if (elm.tagName == 'route-generate-rsp') {
			DIWIAPP.currentGeneratedRouteId = elm.firstChild.getAttribute('id');
			verwerkGenRoute(elm);
			KW.DIWI.getmap(verwerkPlaatje, DIWIAPP.currentGeneratedRouteId, 580, 400);
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
