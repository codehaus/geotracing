// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

var TRACER = {
	current: null,
	BLINK_INTERVAL_SHOW: 250,
	BLINK_INTERVAL_HIDE: 250,
	MARKER_OFFSET_X: 5,
	MARKER_OFFSET_Y: 5
}

/**
 * Represents GeoTracing user.
* $Id$
*/
function Tracer(name, color, iconURL, pt, time) {
	this.record = null;
	this.id = -1;
	this.name = name;
	this.color = color;
	this.point = pt;
	if (this.point && time) {
		this.point.time = new Number(time);
	}
	this.iconURL = iconURL;

	this.activeTrack = null;
	this.tlabel = null;
	this.hidden = false;
	this.live = false;
	this.full = false;
	this.lastPoint = null;
	this.thumbId = null;
	this.thumbURL = 'img/default-user-thumb-4x3.jpg';

	this.init = function() {
		if (this.point) {
			this.setLocation(this.point);
		}
	}

	// Tracer window activated
	this.activate = function () {
		if (this.activeTrack != null && GTW.trackPlayer != null) {
			GTW.trackPlayer.setTrack(this.activeTrack);
		}

		this.panTo();
	}

	// Add a medium
	this.addMedium = function (medium) {
		if (this.activeTrack != null) {
			this.activeTrack.featureSet.addFeature(medium);
		}
	}

	// The continuous blinking
	this.blink = function() {
		DH.toggleVisibility(this.iconId);

		this.blinkInterval = DH.isVisible(this.iconId) ? TRACER.BLINK_INTERVAL_SHOW : TRACER.BLINK_INTERVAL_HIDE;

		// JS Trick to have setTimeout() call our object method
		var self = this;
		setTimeout(function() {
			self.blink();
		}, this.blinkInterval);
		//  DH.isVisible(this.iconId) ? 3000 : 400
	}

	// Delete a Track for id and name
	this.clear = function () {
		this.hide();
		this.deleteTrack();
	}

	/** Create Map icon. */
	this.createTLabel = function () {
		this.iconId = 'icon' + this.name;
		var html = '<a href="#"><span class="tracer"><img id="' + this.iconId + '" src="' + this.iconURL + '" border="0" />&nbsp;<span class="tracername" >' + this.name + '</span></span></a>';

		// Setup TLabel object
		tl = new TLabel();
		tl.glide = true;
		tl.id = 'tlab' + this.name;
		tl.anchorLatLng = this.point;
		tl.anchorPoint = 'topLeft';
		tl.content = html;

		// To shift icon on exact lat/lon location (half size of icon)
		tl.markerOffset = new GSize(TRACER.MARKER_OFFSET_X, TRACER.MARKER_OFFSET_Y);

		return tl;
	}

	// get the tracer color (track)
	this.getColor = function () {
		if (this.record != null && this.record != -1) {
			// In some cases the extra (profile) field may contain a color
			var userColor =  this.record.getField('color');
			if (userColor != null) {
				this.color  = userColor;
			}
		}

		return this.color;
	}

	// Delete a Track for id and name
	this.deleteTrack = function () {
		if (this.activeTrack != null) {
			this.activeTrack.remove();
		}
		this.activeTrack = null;
	}

	// Do we have a track ?
	this.hasTrack = function () {
		return this.activeTrack != null;
	}

	// get the active track
	this.getActiveTrack = function () {
		return this.activeTrack;
	}

	// Delete a Track for id and name
	this.newTrack = function (id, name) {
		this.deleteTrack(id, name);
		this.activeTrack = new Track(id, name, this);
	}


	// Read a Track by getting GPX file by (record) id
	this.readTrack = function (id, name, doDraw) {
		// Need this for JS local scope in callback
		var tracer = this;

		// Define callback for async response
		this.onGetTrackRsp = function (gtx) {

			tracer.deleteTrack();

			var track = new Track(id, name, tracer);

			track.setGTX(gtx);

			if (doDraw == true) {
				track.draw();
			}

			tracer.showInfo();

			// Show Tracer at last point of Track
			var lastPoint = track.getLastPoint();
			if (lastPoint != null) {
				tracer.setLocation(lastPoint, lastPoint.time, true);
			}

			tracer.activeTrack = track;
			if (GTW.trackPlayer != null) {
				GTW.trackPlayer.setTrack(this.activeTrack);
			}
			
			//tracer.activate();
			track.showTitle();
			track.showInfo();
			GTW.showStatus('track drawn');

		}

		// Get GTX document by id from server
		GTW.showStatus('drawing track ' + name + '...');

		SRV.get('get-track', this.onGetTrackRsp, 'id', id, 'format', 'gtx', 'attrs', 'lon,lat,t', 'mindist', GTW.minTrackPtDist, 'maxpoints', GTW.maxTrackPt);
	}

	// Set Tracer at lon/lat location
	this.getLocation = function() {
		return this.point;
	}

	// Set Tracer at lon/lat location
	this.setLocation = function(point, time, force) {
		if (point == null) {
			return;
		}

		// Set last location
		this.lastPoint = this.point;

		// Set new location
		this.point = point;
		if (time) {
			this.point.time = new Number(time);
		}

		if (this.tlabel == null) {
	        // Create icon
			this.tlabel = this.createTLabel();

			// Add icon to map
			GMAP.map.addTLabel(this.tlabel);

			// Setup onlick handling
			var self = this;
			this.onClick = function(e) {
				DH.cancelEvent(e);
				self.showInfo();
				self.popupInfoWindow();
			}

			DH.addEvent(this.tlabel.elm, 'click', this.onClick, false);

		} else {
			// Only Move TLabel
			if (force) {
				this.tlabel.forceToLatLng(this.point);				
			} else {
				this.tlabel.moveToLatLng(this.point);
			}
		}
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


	// Setting the visibility to visible
	this.show = function() {
		if (this.tlabel != null) {
			DH.show(this.tlabel.id);
		}
		this.showInfo();
		this.hidden = false;
	}

	// Setting the visibility to hidden
	this.hide = function() {
		if (this.tlabel != null) {
			DH.hide(this.tlabel.id);
		}
		this.hidden = true;
	}

	// Is Tracer live
	this.isLive = function() {
		return this.live;
	}

	// Is Tracer visible ?
	this.resumeTrack = function() {

	}

	this.popupInfoWindow = function() {
	  var html = '<h3>' + this.name + '</h3>';

	  html += 'Was here on ' + GTW.formatDateAndTime(this.point.time) + '<br/>at ' + this.point.lng() + ', ' + this.point.lat();


	  GMAP.map.openInfoWindowHtml(this.point, html);
	}

	// Set Tracer live
	this.setLive = function() {
		if (this.live == true) {
			return;
		}
		this.live = true;
		this.blink();
	}

	// Show live track info
	this.showLiveInfo = function() {
		this.showInfo();
		var speed = 'unknown';

		if (this.point != null && this.lastPoint != null) {
			speed = GMAP.speed(this.lastPoint, this.point);
			speed = speed.toFixed(2) + ' km/h';
		}
		DH.setHTML('trackview', GTW.formatDateAndTime(this.point.time) + ' <br/>' + speed);
	}

	// Show static info
	this.showInfo = function() {
		DH.setHTML('tracerid', this.name);
		if (!DH.getObject("tracerimg").src) {
			DH.getObject("tracerimg").src = this.thumbURL;
		}
		DH.setHTML('tracerdesc', '&nbsp;');
		TRACER.current = this;
		if (this.record != null && this.record != -1) {
			this._showInfo();
			return;
		}

		// No record: fetch and display later
		this._queryInfo();
	}

	// Is Tracer visible ?
	this.suspendTrack = function() {

	}

	// Is Tracer visible ?
	this.isVisible = function() {
		return this.hidden == false;
	}

	/** Center map around tracer location. */
	this.panTo = function () {
		if (this.point != null) {
			GMAP.map.setCenter(this.point, GMAP.map.getZoom());
		}
	}

	/** Internal display function. */
	this._showInfo = function() {
		if (TRACER.current != this && this.record != null && this.record != -1) {
			return;
		}

		DH.getObject("tracerimg").src = this.thumbURL;
		var desc = this.record.getField('desc');
		if (desc == null) {
			desc = ' ';
		}

		DH.setHTML('tracerdesc', '<i>' + desc + '</i> <br/><span class="cmtlink"><a title="Show list of tracks from ' + this.name + '" href="#" onclick="GTAPP.mUserTracks(\'' + this.name + '\')" >tracks (' + this.record.getField('tracks') + ')</a>&nbsp;&nbsp;<a title="Show all media (photos etc) from this user" href="#" onclick="GTAPP.mShowMediaByUser(\'' + this.name + '\')" >media (' + this.record.getField('media') + ')</a>&nbsp;&nbsp;<a href="#" title="Show user\'s messages or send message to this user" onclick="CMT.showCommentPanel(' + this.id + ',\'user\',\'' + this.name + '\')" >msgs (' + this.record.getField('comments') + ')</a>&nbsp;&nbsp;</span>');
		if (CMT.isCommentPanelOpen() == true) {
			// CMT.showCommentPanel(this.id, 'user', this.name);
		}
	}

	this._queryInfo = function() {
		if (this.record == -1) {
			return;
		}

		// Define callback for async response
		var self = this;
		this.onGetRecord = function(result) {
			self.record = -1;
			if (result != null && result.length > 0) {
				self.record = result[0];
				self.id = self.record.getField("id");
				self.thumbId = self.record.getField("thumbid");
				if (self.thumbId != null) {
					self.thumbURL = 'media.srv?id=' + self.thumbId + "&resize=80x60!";
				}
			}
			self._showInfo();
		}
		//alert('getting thumbid for ' + this.name);
		this.record = -1;
		SRV.get("q-user-info", this.onGetRecord, "user", this.name);
	}

}

