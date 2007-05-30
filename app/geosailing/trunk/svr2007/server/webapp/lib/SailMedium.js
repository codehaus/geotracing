// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

// $Id: BliinMedium.js,v 1.5 2006-08-11 18:47:43 just Exp $

/**
 * Location-based medium.
 * @extends Medium
 * @constructor
 */
function SailMedium(id, name, desc, type, mime, time, lon, lat) {
	Medium.apply(this, new Array(id, name, desc, type, mime, time, lon, lat));

	// Shows icon on map
	this.getIconDiv = function() {
		return '<img id="' + this.iconId + '" src="img/media_icon.png" border="0" width="34" height="35" onload="DH.fixPNG(this)" />';
	}

	// Displays medium in pop-up info window
	this.display = function() {
		this.openInfoWindow();
	}

	// Show icon on map
	this.show = function() {
		var tl = new TLabel(true);
		tl.id = 'tlab' + this.id;
		tl.anchorLatLng = this.getGLatLng();
		tl.anchorPoint = 'topLeft';

		// Overridden in subclass
		tl.content = this.getIconDiv();
		tl.markerOffset = new GSize(17, 17);

		// Add Tlabel to map
		GMAP.map.addTLabel(tl);

		this.tlabel = tl;
		var self = this;
		// alert('show() url=' + this.url + ' name=' + this.name);
		// Define callback for mouse over medium icon
		this.onClickIcon = function (e) {

			self.openInfoWindow();

			// self.display();
			// self.blowUp();
			// DH.cancelEvent(e);
		}

		DH.addEvent(this.tlabel.elm, 'click', this.onClickIcon, false);
	}

	this.openInfoWindow = function() {
		var src = this.url + '&resize=280x210';
		// alert('url=' + this.url + ' name=' + this.name);
		var html = '<b>name:</b> ' + this.name + '<p><img width="280" height="210" id="' + this.previewId + '" title="" src="' + src + '" border="0" align="left" hspace="4" vspace="4" /><b>user:</b> ' + this.userName + '<br/>' + this.getDate() + '</p><p><i>'+ this.desc +'</i></p><p><a href="javascript:MYAPP.mDisplayPrevMedium()">prev</a> | <a href="javascript:MYAPP.mDisplayNextMedium()">next</a></p>';
		GMAP.map.openInfoWindowHtml(this.getGLatLng(), html);
		GMAP.map.panTo(this.getGLatLng());
	}
}


