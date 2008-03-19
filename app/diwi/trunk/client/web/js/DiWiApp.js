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
	//	if (document.location.href.indexOf('test.digitalewichelroede.nl') != -1) {
	//		DIWIAPP.PORTAL = '/diwitest';
	//	}

		SRV.init();
		SRV.url = DIWIAPP.PORTAL + '/srv/get.jsp?';
		
		// KeyWorx client with
		// callbacks (2x)
		// server timeout in minutes
		// server root path /diwi
		KW.init(DIWIAPP.onRsp, DIWIAPP.onNegRsp, 2, DIWIAPP.PORTAL);
		DIWINAV.init();
				
		var accData = KW.getAccountData();
		if (accData != null && accData[0] != '' && accData[0].length > 0) { //user and pass in a cookie
			
			DH.getObject('username_field').value = accData[0];
			DH.getObject('password_field').value = accData[1];
			DH.getObject('autologin').checked = true;
						
			DIWIAPP.autoLogin = true;
		}

		Date.MONTHS = [
				'Januari', 'Februari', 'Maart', 'April', 'Mei', 'Juni', 'Juli',
				'Augustus', 'September', 'October', 'November', 'December'
				];

		Date.DAYS = [
				'Zondag', 'Maandag', 'Dinsdag', 'Woensdag',
				'Donderdag', 'Vrijdag', 'Zaterdag'
				];

		// Swap images in top (just for fun)
		//window.setInterval(DIWIAPP.imageSwap, 30000);
	},

// called from form submit
	login: function() {
		DIWIAPP.pr('inloggen...');
				
		DIWIAPP.userName = DH.getObject('username_field').value;
		var password = DH.getObject('password_field').value;
					
		if(DIWIAPP.userName == "") 
		{			
			DIWIAPP.pr('u moet een gebruikersnaam invullen',"login_error");
			return false;
		}
		else if(password == "")
		{
			DIWIAPP.pr('u moet een wachtwoord invullen',"login_error");
			return false;
		}
		
		KW.login(DIWIAPP.userName, password);
		
		return false;
	},

	logout: function() {
		DIWIAPP.pr('uitloggen...');
		
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
				DIWIAPP.pr('Helaas, met de door u ingegeven waarden kon geen route worden samengesteld. <br/>Probeert u het nogmaals met andere waarden.',"route_info");
				break;

			case 'login-nrsp':
				DIWIAPP.pr(details,"login_error");
				break;

			default:
				DIWIAPP.pr('Helaas, er is een onbekende fout opgetreden. De melding is: <br/><i>"' + details + '"</i>');
				break;
		}
	},

// Util for printing/displaying debug output
	pr: function (s,type) {
		if(type == null)
		{
			DH.setHTML('footer', s);
		}
		else if(type == "login_error")
		{
			DH.setHTML('login_error', s);
		}
		else if(type == "route_info")
		{
			DH.setHTML('route_info', s);
		}
	},

	
	setStatus: function (s) {
		DH.setHTML('status', s);
	}
}


// Call DIWIAPP.init() when page is fully loaded
DH.addEvent(window, 'load', DIWIAPP.init, false);

// DH.addEvent(window, 'load', isLoggedIn(), false);
// DH.addEvent(window, 'load', TST.getFixedRoutes(), false);
