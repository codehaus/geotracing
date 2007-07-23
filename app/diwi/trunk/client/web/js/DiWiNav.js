/*
 * DiWi page navigation.
 *
 * Handles main navigation like buttons and forms for index.html.
 *
 * author: Just van den Broecke
 */
var DIWINAV = {
	/** div where pages are loaded into. */
	PAGE_ID: 'pagina',
	buttons: {},
	selectedButtonId: null,

	addButton: function(aButton) {
		DIWINAV.buttons[aButton.id] = aButton;
	},

	init: function() {
		// Setup main menu buttons
		new Button('b1', 'welkom', DIWINAV.showVideoLink);
		new Button('b2', 'routes', ROUTE.showFixedRoutes);
		new Button('b3', 'aanmelden', DIWINAV.showVideoLink);
		new Button('b4', 'faq', DIWINAV.showVideoLink);
		new Button('b5', 'inloggen', DIWINAV.prepareLogin);
		new Button('b6', 'uitloggen', DIWIAPP.logout);
		new Button('b7', 'maakroute', ROUTE.createGenerateRouteForm);
		new Button('b8', 'mijnpagina', TRIP.showTrips);

		// Start with loading welcome page
		DIWINAV.buttons['b1'].onSelect();

		// Listen to login button and password field
		DH.addEvent(DH.getObject('butloginsubmit'), 'click', DIWIAPP.login, false);
		// DH.addEvent(DH.getObject('fieldpassword'), 'keypress', DIWINAV.onPasswordChar, false);
	},


	loadPage: function(aPageURL, aCallback) {
		MAP.hide();
//		DIWIAPP.pr('&nbsp;');
		DH.setHTML(DIWINAV.PAGE_ID, DH.getURL(aPageURL, aCallback));
	},

	/** Callback (from DIWIAPP) when login ok.  */
	onLogin: function() {
		// Show new content, here logout form
		DH.displayOn('butmaakroute');
		DH.displayOn('butmijnpagina');
		DH.displayOff('butinloggen');
		DH.displayOn('butuitloggen');
		DH.hide('loginform');
		DH.displayOff('inlogbox');
		DH.displayOff('inlogform');
		DIWIAPP.setStatus('ingelogd als ' + DIWIAPP.userName);
		DIWIAPP.pr('ingelogd als ' + DIWIAPP.userName);
		DIWINAV.buttons['b8'].onSelect();
	},

	/** Callback (from DIWIAPP) when logout ok.  */
	onLogout: function() {
		// Show new content, here logout form
		DIWIAPP.pr('logout OK');
		DH.displayOff('butmaakroute');
		DH.displayOff('butmijnpagina');
		DH.displayOn('butinloggen');
		DH.displayOff('butuitloggen');
		DH.show('loginform');
		DIWIAPP.setStatus('niet ingelogd');
		DIWINAV.buttons['b1'].onSelect();
	},

	onPasswordChar: function(e) {
		var event = DH.getEvent(e);
		var keyCode = event.which ? event.which : event.keyCode;

		if (keyCode == 13) {
			DIWIAPP.login();
		}
		return false;
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

	showVideoLink: function() {
		DIWIAPP.pr('Wat is de "Digitale Wichelroede" en wat kun je er allemaal mee ? <p>&nbsp;</p><a href="media/video/diwi-promo.swf"><strong>Bekijk de introductie-video.</strong></a>');
	},

	reset: function() {
		for (b in DIWINAV.buttons) {
			if (DIWINAV.buttons[b].id != DIWINAV.selectedButtonId) {
				DIWINAV.buttons[b].lowLight();
			}
		}
	},

	select: function(aButton) {
		DIWINAV.selectedButtonId = aButton.id;
	}
}

// Class representing single main menu button and its state.
function Button(anId, aContentId, anActionFun) {
	this.id = anId;
	this.imageL = 'media/buttons/' + aContentId + '.gif';
	this.imageH = 'media/buttons/' + aContentId + 'H.gif';
	this.pageURL = 'pages/' + aContentId + '.html';
	this.actionFun = anActionFun;

	var button = this;

	// Define callback for click and select
	this.onSelect = function (e) {
		DIWINAV.select(button);
		button.highLight();
		if (e) {
			DH.cancelEvent(e);
		}
		DIWINAV.loadPage(button.pageURL);
		if (button.actionFun) {
			button.actionFun();
		}
	}

	// Define callback for mouse over
	this.onMouseOver = function (e) {
		button.highLight();
		DH.cancelEvent(e);
	}

	// Define callback for mouse out
	this.onMouseOut = function (e) {
		button.lowLight();
		DH.cancelEvent(e);
	}

	// Swap image to highlighted
	this.highLight = function() {
		DIWINAV.reset();
		DH.getObject(this.id).src = this.imageH;
	}

	// Swap image to lowlighted
	this.lowLight = function() {
		if (DIWINAV.selectedButtonId == this.id) {
			return;
		}
		DH.getObject(this.id).src = this.imageL;
	}

	// Setup button events
	DH.addEvent(DH.getObject(this.id), 'mouseover', this.onMouseOver, false);
	DH.addEvent(DH.getObject(this.id), 'mouseout', this.onMouseOut, false);
	DH.addEvent(DH.getObject(this.id), 'click', this.onSelect, false);

	DIWINAV.addButton(this);
}

