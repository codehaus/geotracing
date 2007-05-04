// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

// $Id: MyMedium.js 51 2006-09-25 16:16:02Z just $

/**
 * Location-based medium.
 * @extends Medium
 * @constructor
 */
function MyMedium(id, name, desc, type, mime, time, lon, lat, subtype, locid) {
	Medium.apply(this, new Array(id, name, desc, type, mime, time, lon, lat));

	this.subtype = subtype;
	this.icon = null;
	this.point = new GLatLng(lat, lon);
	this.locid=locid;

	if (this.subtype == 1) {
		this.icon = "images/rood.png";
	}
	else if (this.subtype == 2) {
		this.icon = "images/groen.png";
	}
	else if (this.subtype == 3) {
		this.icon = "images/oranje.png";
	}else if (this.subtype == 4) {
		this.icon = "images/blauw.png";
	}

	/*this._displayInfo = function() {

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
*/

	this.popupInfoWindow = function() {
		var html = '<h3>' + this.name + '</h3>';

		html += "<label onmouseover=this.style.cursor='pointer' onclick=LOCAPP.deleteLocationReq(" + this.locid + ")><u>Verwijder marker</u></label><br/><label onmouseover=this.style.cursor='pointer' onclick=LOCAPP.loadFormToevoegen('locationapp/update-location-form.html'," + lng + "," + lat + "," + this.locid + ",'updatelocationform'><u>Wijzig marker</u></label>";
		GMAP.map.openInfoWindowHtml(this.point, html);
	}

	// Shows icon on map
	this.getIconDiv = function() {
		return '<div onmouseover=this.style.cursor="pointer" id="' + this.iconId + '" ><img src="' + this.icon + '" border="0" /></div>';
	}

	this.getMarker = function() {
		var icon = new GIcon();
		icon.image=this.icon;
		icon.iconSize = new GSize(15, 23);
		icon.iconAnchor = new GPoint(10, 30);
		icon.infoWindowAnchor = new GPoint(9, 2);
		var point=new GPoint(this.lon,this.lat);
		GMAP.map.addOverlay(this.createMarker(point, icon,this.iconId));
	}
	
	this.createMarker= function(point, icon,id) {
		// ======== Add a "directions" link ======
		var marker = new GMarker(point, icon);
		var lng = point.x;
		var lat = point.y;
		var html = '<h3>' + this.name + '</h3>';
		var desc=this.desc;
		desc=desc.replace(' ' ,'_');
		html += "<label onmouseover=this.style.cursor='pointer' onclick=LOCAPP.deleteLocationReq(" + this.locid + ")><u>Verwijder marker</u></label><br/><label onmouseover=this.style.cursor='pointer' onclick=LOCAPP.loadFormUpdate('locationapp/update-location-form.html','" + lng + "','" + lat + "','" + this.locid + "','" + this.name + "','" + this.subtype + "','" + desc + "','updatelocationform')><u>Wijzig marker</u></label>";

		GEvent.addListener(marker, "click", function() {
			latestoverlay = marker;
			marker.openInfoWindowHtml(html);
		});
		var jj=this
		GEvent.addListener(marker, "mouseover", function() {
			latestoverlay = marker;
			jj.display();
     	});
		return marker;
	}
}


