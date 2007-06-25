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
		new Button('b1', 'welkom');
		new Button('b2', 'routes', DIWIAPP.showFixedRoutes);
		new Button('b3', 'aanmelden');
		new Button('b4', 'faq');
		new Button('b5', 'inloggen', DIWINAV.prepareLogin);
		new Button('b6', 'uitloggen', DIWIAPP.logout);
		new Button('b7', 'maakroute', initMakeRouteForm);
		new Button('b8', 'mijnpagina');

		// Start with loading welcome page
		DIWINAV.buttons['b1'].onSelect();

		// Listen to login button and password field
		DH.addEvent(DH.getObject('butloginsubmit'), 'click', DIWIAPP.login, false);
		DH.addEvent(DH.getObject('fieldpassword'), 'keypress', DIWINAV.onPasswordChar, false);
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

	select: function(aButton) {
		DIWINAV.selectedButtonId = aButton.id;
	},

	reset: function() {
		for (b in DIWINAV.buttons) {
			if (DIWINAV.buttons[b].id != DIWINAV.selectedButtonId) {
				DIWINAV.buttons[b].lowLight();
			}
		}
	},

	loadPage: function(aPageURL, aCallback) {
		DH.setHTML(DIWINAV.PAGE_ID, DH.getURL(aPageURL, aCallback));
	},


	onPasswordChar: function(e) {
		var event = DH.getEvent(e);
		var keyCode = event.which ? event.which : event.keyCode;

		if (keyCode == 13) {
			DIWIAPP.login();
		}
		return false;
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

