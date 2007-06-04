// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

var COURSES_NL = new Array('N', 'NO', 'O', 'ZO', 'Z', 'ZW', 'W', 'NW', 'N');

/**
 * Represents GeoTracing user for GeoSailing app.
 *
 * Extends the standard Tracer class, overruling
 * standards functions.
 *
 * $Id: SailTracer.js,v 1.8 2006-08-13 19:16:36 just Exp $
 */
function SailTracer(name, color, iconURL, pt, time) {
	// Inherits from Tracer
	Tracer.apply(this, new Array(name, color, iconURL, pt, time));
	this.course = 0;
	this.courseStr = '-';
	this.speed = 0;

	/** Create Map icon. */
	this.createTLabel = function () {
		this.color = MYAPP.trimString(this.color);

		this.iconId = 'icon' + this.id;
		var iconPath = this.getIconPath('N');
		// var html = '<img id="' + this.iconId + '" src="' + this.iconURL + '1.png" border="0" width="55" height="55" onload="DH.fixPNG(this)" />';
		var html = '<a href="#"><img border="0" id="' + this.iconId + '" src="' + iconPath + '" onload="DH.fixPNG(this)" /></a>';

		// Setup TLabel object
		tl = new TLabel();
		tl.glide = true;
		tl.id = 'trclab' + this.name;
		tl.anchorLatLng = this.point;
		tl.anchorPoint = 'topLeft';
		tl.content = html;

		// DH.fixPNG(DH.getObject(this.iconId));

		// To shift icon on exact lat/lon location (half size of icon)
		tl.markerOffset = new GSize(8, 8);

		return tl;
	}

	this.createStatusLine = function() {
		var id = this.id;
		var div = '<a id="user' + id + '" class="boatinfo" href="#" onclick="MYAPP.showUserDetails(\'' + this.name + '\'); return false;" name="meer info over boot" title="bekijk meer info over deze boot">';
		div += '<div class="boatcolor" id="color' + id + '" style="background-color: ' + this.color + '">&nbsp;&nbsp;&nbsp;&nbsp;</div>';
		div += '<div class="boatname">' + this.name + '</div>';
		div += '<div class="boatspeed" id="speed' + id + '">0 km/h</div>';
		div += '<div class="boatheading" id="course' + id + '">-</div>';
		div += '</a>';
		div += '<a class="findboat" href="#" onclick="MYAPP.drawActiveTrack(\'' + this.name + '\')" name="teken route van boot op de kaart" title="teken route van boot op de kaart">route</a></div>';
		div += '<a class="findboat" href="#" onclick="MYAPP.zoomToBoat(\'' + this.name + '\')" name="zoom in en volg boot op de kaart" title="zoom in en volg boot op de kaart">volg</a></div>';
		/*
		<a href="#" name="meer info over deze boot" title="bekijk meer info over deze boot" class="boatinfo">
			<img src="images/sidebar-bootkleur-1.gif" class="boatcolor" />
			<div class="boatname">
			Hurde Wyn
			</div>
			<div class="boatspeed">
			30 km/h
			</div>
			<div class="boatheading">
			NW
			</div>
		</a>
		<a href="#" name="teken route van boot op de kaart" title="teken route van boot op de kaart" class="findboat">route</a>
		<a href="#" name="zoom in en volg boot op de kaart" title="zoom in en volg boot op de kaart" class="findboat">volg</a>

		*/
		// return '<div class="boatinfo" id="user' + id + '" >&nbsp;&nbsp;' + '<span id="color' + id + '" style="background-color: ' + this.color + '">&nbsp;&nbsp;&nbsp;&nbsp;</span>' + this.name + '&nbsp;&nbsp;&nbsp;&nbsp;<span id="speed' + id + '">-</span>&nbsp;&nbsp;&nbsp;&nbsp;' + '<span id="course' + id + '">-</span></div>';
		return div;
	}

	this.getIconPath = function(aCourseStr) {
		var colorStr = this.color.substring(1, 7);
		return GTW.TRACER_ICON_URL + colorStr + '/boot-' + colorStr + '-' + aCourseStr + '.png';
	}

	// Move Tracer to lon/lat location
	this.move = function(lon, lat, time) {

		// replace point
		pt = new GLatLng(lat, lon);
		if (time) {
			pt.time = new Number(time);
		}


		// Move TLabel
		this.setLocation(pt);

		if (this.activeTrack != null) {
			this.activeTrack.addLivePoint(pt);
		}
		this.showLiveInfo();
	}

	this.openInfoWindow = function() {
	//	var html = '<b>name:</b> ' + this.name + '<p><img width="128" height="96" src="img/users/' + this.name + '.jpg" border="0"/>&nbsp;&nbsp;<a href="#" onclick="javascript:MYAPP.mShowMediaByUser(\'' + this.name + '\');return false\" >view media</a></p>';

	//	GMAP.map.openInfoWindowHtml(this.point, html);
	//	MYAPP.showUserDetails(this.name);
	}


	this.popupInfoWindow = function() {
		BOAT.show(this.name);
	}

	// Show static info
	this.showInfo = function() {
		//DH.setHTML('tracerid', this.name);
		//if (!DH.getObject("tracerimg").src) {
		//	DH.getObject("tracerimg").src = this.thumbURL;
		//}
		//DH.setHTML('tracerdesc', '&nbsp;');
		TRACER.current = this;
		if (this.record != null && this.record != -1) {
			this._showInfo();
			return;
		}

		// No record: fetch and display later
		this._queryInfo();
	}

	// Show live track info
	this.showLiveInfo = function() {
		this.showInfo();
		this.speed = 0;

		DH.blink('color' + this.id, 6, 250);
		if (this.point != null && this.lastPoint != null) {
			// If we have a previous point calculate course and set directional icon
			// Set icon based on where we are heading:
			// 1 S, 2 SW, 3 W, 4 NW, 5 N, 6 NE, 7 E, 8 SE
			var oldCourseIndex = Math.round(this.course / 45);
			this.course = GMAP.heading(this.lastPoint.lat(), this.lastPoint.lng(), this.point.lat(), this.point.lng());

			// Course is degrees value between 0 (N) through 180 (S) to 360 (N)
			var courseIndex = Math.round(this.course / 45);
			this.courseStr = COURSES_NL[courseIndex];

			// Change icon if course changed
			if (oldCourseIndex != courseIndex) {

				// replace image with directed icon image
				var iconSrc = this.getIconPath(this.courseStr);

				DH.getObject(this.iconId).src = iconSrc;
				DH.fixPNG(DH.getObject(this.iconId));
			}
			
			this.speed = GMAP.speed(this.lastPoint, this.point);

			// for bots
			if (this.speed > 1000) {
				this.speed /= 100;
			}

			if (this.speed > 200) {
				this.speed /= 10;
			}

			this.speed = this.speed.toFixed(2);
			DH.getObject('speed' + this.id).innerHTML = this.speed + ' km/h';
			DH.getObject('course' + this.id).innerHTML = this.courseStr;

		}
		// DH.setHTML('trackview', GTW.formatDateAndTime(this.point.time) + ' <br/>' + speed);
	}

	/** Center map around tracer location. */
	this.zoomTo = function () {
		if (this.point != null) {
			var zoom = GMAP.map.getZoom();
			// Keep zoom-level if already zoomed in.
			zoom = (zoom > 13) ? zoom : 13;
			GMAP.map.setCenter(this.point, zoom);
		} else {
			alert('De boot ' + this.name + ' heeft nog geen locatie.');
		}
	}

	/** Internal display function. */
	this._showInfo = function() {
		if (TRACER.current != this && this.record != null && this.record != -1) {
			return;
		}

		//DH.getObject("tracerimg").src = this.thumbURL;
		var desc = this.record.getField('desc');
		if (desc == null) {
			desc = ' ';
		}

		//DH.setHTML('tracerdesc', '<i>' + desc + '</i> <br/><span class="cmtlink"><a href="#" onclick="GTAPP.mUserTracks(\'' + this.name + '\')" >tracks (' + this.record.getField('tracks') + ')</a>&nbsp;&nbsp;<a href="#" onclick="GTAPP.mShowMediaByUser(\'' + this.name + '\')" >media (' + this.record.getField('media') + ')</a>&nbsp;&nbsp;<a href="#" onclick="CMT.showCommentPanel(' + this.id + ',\'user\',\'' + this.name + '\')" >msgs (' + this.record.getField('comments') + ')</a>&nbsp;&nbsp;</span>');
		if (CMT.isCommentPanelOpen() == true) {
			// CMT.showCommentPanel(this.id, 'user', this.name);
		}
	}
}

