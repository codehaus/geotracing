
var DIWIAPP = {
	userName: null,
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
		var accData = KW.getAccountData();
		if (accData != null && accData[0] != '') {
			var loginForm = DH.getObject('loginform');
			loginForm.name.value = accData[0];
			loginForm.password.value = accData[1];
			DIWIAPP.login();
		}
	},

// called from form submit
	login: function() {
		DIWIAPP.setStatus('inloggen...');
		var loginForm = DH.getObject('loginform');
		DIWIAPP.userName = loginForm.name.value;
		var password = loginForm.password.value;

		// Call KWClient
		KW.login(DIWIAPP.userName, password);
		DIWIAPP.pr('login sent');
		return false;
	},

	logout: function() {
		DIWIAPP.setStatus('uitloggen...');
		KW.clearAccount();
		// KeyWorx client
		KW.logout();
		return false;
	},

	createRoute: function() {
		DIWIAPP.pr('maakroute start');
		var params = new Array();
		params[KW.DIWI.BESLOTEN_PARAM] = s1.getValue();
		params[KW.DIWI.HALFOPEN_PARAM] = s2.getValue();
		params[KW.DIWI.OPEN_PARAM] = s3.getValue();
		params[KW.DIWI.BEDRIJVEN_PARAM] = s4.getValue();
		params[KW.DIWI.BEWONING_PARAM] = s5.getValue();
		params[KW.DIWI.BOS_PARAM] = s6.getValue();
		params[KW.DIWI.HEIDE_PARAM] = s7.getValue();
		params[KW.DIWI.GRASLAND_PARAM] = s8.getValue();
		params[KW.DIWI.ZEE_PARAM] = s9.getValue();
		params[KW.DIWI.SLOTEN_PARAM] = s10.getValue();
		params[KW.DIWI.STARTPUNT_PARAM] = document.getElementById("startpunt").value;
		params[KW.DIWI.EINDPUNT_PARAM] = document.getElementById("eindpunt").value;
		params[KW.DIWI.THEMA_PARAM] = document.getElementById("thema").value;
		params[KW.DIWI.AFSTAND_PARAM] = document.getElementById("afstand").value;
		params[KW.DIWI.WANDELAAR_PARAM] = document.getElementById("wandelen").checked;

		KW.DIWI.generateroute(null, params);
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

	prepareLogin: function() {
		DH.getStyleObject('inlogbox').display = 'block';
		DH.getStyleObject('inlogform').display = 'block';
		var loginForm = DH.getObject('loginform');
		var accData = KW.getAccountData();
		if (accData != null) {
			loginForm.name.value = accData[0];
			loginForm.password.value = accData[1];
		}

	},

	onRsp: function(elm) {
		if (!elm) {
			DIWIAPP.pr('empty response');
			return;
		}

		DIWIAPP.pr('server response ' + elm.tagName);
		if (elm.tagName == 'login-rsp') {
			DIWIAPP.setStatus('response ok');
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
		} else {
			DIWIAPP.pr('rsp tag=' + elm.tagName + ' ' + elm);
		}
	},

// KWClient negative response handler.
	onNegRsp: function(errorId, error, details) {
		DIWIAPP.setStatus('server zegt:' + error);
	},

// Util for printing/displaying debug output
	pr: function (s) {
		// DH.addHTML('result', '<br/>' + s);
	},

	setStatus: function (s) {
		DH.setHTML('msg', s);
	}
}


// Call DIWIAPP.init() when page is fully loaded
DH.addEvent(window, 'load', DIWIAPP.init, false);

// DH.addEvent(window, 'load', isLoggedIn(), false);
// DH.addEvent(window, 'load', TST.getFixedRoutes(), false);
