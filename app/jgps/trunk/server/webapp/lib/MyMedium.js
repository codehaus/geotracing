// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

// $Id: MyMedium.js 51 2006-09-25 16:16:02Z just $

/**
 * Location-based medium.
 * @extends Medium
 * @constructor
 */
function MyMedium(id, name, desc, type, mime, time, lon, lat, subtype) {
	Medium.apply(this, new Array(id, name, desc, type, mime, time, lon, lat, subtype));
	
	Medium.getIconDiv=MyMedium.getIconDiv;
	
this.mime = mime;
	this.subtype = subtype;
	this.lon=lon;
	this.lat=lat;
	this.id=id;
	this.url = 'media.srv?id=' + this.id;
	// Shows icon on map
	this.getBGColor = function() {
		if (this.userName && this.userName != null) {
			var tracer = GTW.getTracer();
			if (!tracer) {
				tracer = GTW.createTracer(this.userName);
			}
			this.bgColor = tracer.getColor();
		}
		return this.bgColor;
	}

	this._displayDescr = function() {
		DH.setHTML('featuredesc', this.desc);
		GMAP.resize();
	}
	

	// Shows icon on map
	this.init = function() {
	}
	
	this.addMarker = function(point,icon)
	{
		var marker = new GMarker(point, icon );
		var lng = point.x;
		var lat = point.y;
		var htmltekst = "<label onmouseover=this.style.cursor='pointer' onclick=LOCAPP.deleteLocationReq(" + this.id + ")><u>Verwijder marker</u></label><label onmouseover=this.style.cursor='pointer' onclick=LOCAPP.ypdateLocationReq(" + this.id + ")><u>Wijzig marker</u></label>";
		
		GEvent.addListener(marker, "click", function() {
			latestoverlay = marker;
			marker.openInfoWindowHtml(htmltekst);
		});
		return marker;
		
	}

	this.markers = function() {
		var icon = new GIcon();
		if(this.subtype==1)
			{
				icon.image="images/rood.png";
			}
			else if(this.subtype==2)
			{
				icon.image="images/groen.png";
			}
			else if(this.subtype==3)
			{
				icon.image="images/oranje.png";	
			}
		icon.iconSize = new GSize(15, 25);
		icon.iconAnchor = new GPoint(10, 30);
		icon.infoWindowAnchor = new GPoint(9, 2);
		var punt=new GPoint(this.lon,this.lat);
		GMAP.map.addOverlay(this.addMarker(punt, icon,id));	
	}		
	
	
	// Shows icon on map
	this.getIconDiv = function() {
		
		if (this.type == 'text') {
			var src = 'img/poi.gif';
			var img = '<img title="' + this.getTitle() + '" src="' + src + '" border="0"  alt="" />';

			return '<div class="texticon" id="' + this.iconId + '" style="border: 1px solid ' + this.bgColor + ';" >' + img + '</div>';
		} else {
			var hulp=this.markers()
			return '<div id="' + this.iconId + '" ;" >' + this.markers() + '</div>';
		}
	}
}


