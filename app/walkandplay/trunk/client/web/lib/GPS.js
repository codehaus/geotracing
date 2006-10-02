// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Acces to locally connected GPS.
 *
 * PURPOSE
 * This library provides realtime access to a local GPS server (GPSDoor).
 *
 * USAGE
 * You must run gpsdoor daemon first. When running GPS.start(millis)
 * will fetch GPS coordinates each interval milliseconds by loading
 * a script from gpsdoor daemon (this in order to escape cross-domain issues).
 * GPSDoor will set GPS.lat/lon and GPS.set to true.
 * Another script should periodically check GPS.set for "true" and fetch the coordinates
 * GPS.lat/lon.
 *
 * GPS.start(5000)
 * Author: Just van den Broecke
 * $Id: GPS.js,v 1.1 2006-07-09 12:31:05 just Exp $
 */
var GPS = {
	lon: null,
	lat: null,
	set: false,
	timer: null,

	start: function(interval) {
		setInterval('GPS._fetch()', interval);
	},

	stop: function() {
		if (GPS.timer == null) {
			return;
		}
		clearInterval(GPS.timer);
		GPS.timer = null;
	},

	/** X-domain JS trick: http://www.ariadne-cms.org/news/00009/ */
	_fetch: function() {
		var script = document.getElementById('script_gps');
		if (script) {
			script.parentNode.removeChild(script);
		}
		script = document.createElement('SCRIPT');
		script.id = 'script_gps';
		script.src = 'http://127.0.0.1:7305/js';
		var head = document.getElementsByTagName('HEAD')[0];
		head.appendChild(script);
	}
}
