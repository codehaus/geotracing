// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

// $Id: MyMedium.js 51 2006-09-25 16:16:02Z just $

/**
 * Location-based medium.
 * @extends Medium
 * @constructor
 */
function MyMedium(id, name, desc, type, mime, time, lon, lat, subtype) {
	Medium.apply(this, new Array(id, name, desc, type, mime, time, lon, lat));

	this.subtype = subtype;
	this.icon = null;
	this.point = new GLatLng(lat, lon);

	if (this.subtype == 1) {
		this.icon = "images/rood.png";
	}
	else if (this.subtype == 2) {
		this.icon = "images/groen.png";
	}
	else if (this.subtype == 3) {
		this.icon = "images/oranje.png";
	}

	this._displayInfo = function() {

		self = this;
		this.onClick = function(e) {
			DH.cancelEvent(e);
			self.popupInfoWindow();
		}

		DH.addEvent(this.iconId, 'click', self.onClick, false);
	}

	this._displayDescr = function() {
		DH.setHTML('featuredesc', this.desc);
		GMAP.resize();
	}


	this.popupInfoWindow = function() {
		var html = '<h3>' + this.name + '</h3>';

		html += "<label onmouseover=this.style.cursor='pointer' onclick=LOCAPP.deleteLocationReq(" + this.id + ")><u>Verwijder marker</u></label><br/><label onmouseover=this.style.cursor='pointer' onclick=LOCAPP.updateLocationReq(" + this.id + ")><u>Wijzig marker</u></label>";
		GMAP.map.openInfoWindowHtml(this.point, html);
	}

	// Shows icon on map
	this.getIconDiv = function() {
		return '<div onmouseover=this.style.cursor="pointer" id="' + this.iconId + '" ><img src="' + this.icon + '" border="0" /></div>';
	}


}


