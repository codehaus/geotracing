/*
 * DiWi page navigation
 */
var DIWINAV = {
	PAGE_ID: 'pagina',
	buttons: {},
	selectedButtonId: null,

	init: function() {
		DIWINAV.buttons['b1'] = new Button('b1', 'welkom');
		DIWINAV.buttons['b2'] = new Button('b2', 'routes', DIWIAPP.showFixedRoutes);
		DIWINAV.buttons['b3'] = new Button('b3', 'aanmelden');
		DIWINAV.buttons['b4'] = new Button('b4', 'faq');
		DIWINAV.buttons['b1'].onSelect();
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

	loadPage: function(aPageURL) {
		DH.setHTML(DIWINAV.PAGE_ID, DH.getURL(aPageURL));
	}
}

function Button(anId, aContentId, anActionFun) {
	this.id = anId;
	this.imageL = 'media/buttons/' + aContentId + '.gif';
	this.imageH = 'media/buttons/' + aContentId + 'H.gif';
	this.pageURL = 'pages/' + aContentId + '.html';
	this.actionFun = anActionFun;

	var button = this;

	// Define callback for mouse over
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

	// Swap image
	this.highLight = function() {
		DIWINAV.reset();
		DH.getObject(this.id).src = this.imageH;
	}

	// Swap image
	this.lowLight = function() {
		if (DIWINAV.selectedButtonId == this.id) {
			return;
		}
		DH.getObject(this.id).src = this.imageL;
	}

	DH.addEvent(DH.getObject(this.id), 'mouseover', this.onMouseOver, false);
	DH.addEvent(DH.getObject(this.id), 'mouseout', this.onMouseOut, false);
	DH.addEvent(DH.getObject(this.id), 'click', this.onSelect, false);
}


function toggleInlogBox() {
	DH.toggleDisplay(document.getElementById('inlogbox'));
	DH.toggleDisplay(document.getElementById('inlogform'));
}
function toggleLogButton() {
	DH.toggleDisplay(document.getElementById('butuitloggen'));
	DH.toggleDisplay(document.getElementById('butinloggen'));
}

function toonLogoutButton() {
	toggleLogButton();
}

function toonLoginButton() {
	DIWI.logout();
	toggleLogButton();
}

function hideText(d) {
	if (d.length < 1) {
		return;
	}
	document.getElementById(texten[d]).style.display = "none";
}

function showText(d) {
	if (d.length < 1) {
		return;
	}
	document.getElementById(texten[d]).style.display = "block";
}

function show(d) {
	for (i = 0; i < aantaltexten; i++) {
		if (i == d) {
			showText(i);
		} else {
			hideText(i);
		}
	}
	if (d == 2)
		toonSelectedVasteRoute();
}

function restoreLoggedIn() {
	if (queryString('logged') == 'true') {
		KW.restoreSession();
		document.getElementById('butinloggen').style.display = "none";
		document.getElementById('butuitloggen').style.display = "block";
	}
}

function submitenter(myfield, e) {
	var keycode;
	if (window.event)
		keycode = window.event.keyCode;
	else if (e)
		keycode = e.which;
	else
		return true;

	if (keycode == 13) {
		DIWI.login();
		return false;
	} else
		return true;
}
