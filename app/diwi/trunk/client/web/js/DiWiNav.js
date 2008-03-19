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
		
		//alert("diwinav init")
		
		// Setup main menu buttons
		/*new Button('b1', 'welkom', DIWINAV.showVideoLink);
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
		DH.addEvent(DH.getObject('butloginsubmit'), 'click', DIWIAPP.login, false);*/
		// DH.addEvent(DH.getObject('fieldpassword'), 'keypress', DIWINAV.onPasswordChar, false);
	},


	loadPage: function(aPageURL, aCallback) {
		MAP.hide();
//		DIWIAPP.pr('&nbsp;');
		DH.setHTML(DIWINAV.PAGE_ID, DH.getURL(aPageURL, aCallback));
	},

	/** Callback (from DIWIAPP) when login ok.  */
	onLogin: function() {
		
		/**
		extend the menu
		*/
		$("#browse_b").css("display","inline");
		$("#maak_route *").css("display","inline");
		$("#mijn_routes *").css("display","inline");
		$("#aanmelden #mijn_diwi").css("display","none");
		$("#aanmelden #log_uit").css("display","inline");
		
		//hide all submenus
		select_menu_item(null);
		
		if (DIWIAPP.autoLogin == true) {
			KW.storeAccount();
		}
		
		
		DIWIAPP.setStatus('ingelogd als ' + DIWIAPP.userName);
	
	},

	/** Callback (from DIWIAPP) when logout ok.  */
	onLogout: function() {
		// Show new content, here logout form
		DIWIAPP.pr('logout OK');
		
		$("#browse_b").css("display","none");
		$("#maak_route *").css("display","none");
		$("#mijn_routes *").css("display","none");
		$("#aanmelden #mijn_diwi").css("display","inline");
		$("#aanmelden #log_uit").css("display","none");
		
		if (DIWIAPP.autoLogin != true) {
			KW.clearAccount();
		}
	
		
		DH.getObject('username_field').value = "";
		DH.getObject('password_field').value = "";
		select_menu_item(null);
		
		
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
		
		var accData = KW.getAccountData();
		if (accData != null) {
			DH.getObject('username_field').value = accData[0];
			DH.getObject('password_field').value = accData[1];
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

