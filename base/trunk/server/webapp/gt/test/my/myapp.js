/*
 * Traceland app.
 *
 * PURPOSE
 * Library representing the traceland app. All starts here.
 *
 * Author: Just van den Broecke
 * $Id: traceland.js,v 1.4 2006-03-02 23:34:42 just Exp $
 */

// The TraceLand application functions
// Mainly overrules gtapp.js functions
var MY = {
	topMenu: null,

	init: function() {
		topMenu = new Menu('topmenu');
	},

	hideStatus: function() {
		DH.hide('status');
		if (MY.statusId != null) {
			clearInterval(MY.statusId);
			MY.statusId = null;
		}

	},

	blinkStatus: function(txt) {
		MY.hideStatus();
		DH.setHTML('status', txt);
		MY.statusId = setInterval(function() {
			DH.toggleVisibility('status')
		}, 400);
	},

			showStatus: function(txt) {
		MY.hideStatus();
		DH.setHTML('status', txt);
		DH.show('status');
	},
		
	login: function() {
		MY.loadContent('cont/login.html');
	},

	register: function() {
		MY.loadContent('cont/register.html');
	},

	loadContent: function(url) {
		MY.blinkStatus('loading ' + url)
		DH.getURL(url, MY.onContentLoaded);
	},

	onContentLoaded: function(text) {
		MY.showStatus('loaded');
		DH.setHTML('content', text);
	}
}

// Starts it all
DH.addEvent(window, 'load', MY.init, false);
