// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/**
 * Represents GeoTracing user.
* $Id$
*/
function Tracer(name, color, iconURL, pt, time) {
	this.record = null;
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
	this.blinkInterval = 250;
	this.thumbId = null;
	
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

		this.zoomTo();
	}

	// Add a medium
	this.addMedium = function (medium) {
		if (this.activeTrack != null) {
			this.activeTrack.featureSet.addFeature(medium);
		}
	}

	// Add a POI
	this.addPOI = function (poi) {
		if (this.activeTrack != null) {
			this.activeTrack.featureSet.addFeature(poi);
		}
	}

	// The continuous blinking
	this.blink = function() {
		DH.toggleVisibility(this.iconId);

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
		var html = '<span class="tracer"><img id="' + this.iconId + '" src="' + this.iconURL + '" border="0" />&nbsp;<span class="tracername" >' + this.name + '</span></span>';

		// Setup TLabel object
		tl = new TLabel();
		tl.id = 'tlab' + this.name;
		tl.anchorLatLng = this.point;
		tl.anchorPoint = 'topLeft';
		tl.content = html;

		// To shift icon on exact lat/lon location (half size of icon)
		tl.markerOffset = new GSize(5, 5);

		return tl;
	}

	// get the tracer color (track)
	this.getColor = function () {
		var record = this.getRecord();
		if (record != null) {
			// In some cases the extra field may contain a color
			var color = record.getField('color');
			if (color != null) {
				this.color = color;
			}
		}

		return this.color;
	}

	// get the user DB record
	this.getRecord = function () {
		if (this.record == null) {
			// lazy: may not have been set on creation
			var result = SRV.get("q-user-by-name", null, "user", this.name);
			if (result != null && result.length > 0) {
				this.record = result[0];
			}
		}
		return this.record;
	}

	// Get id of user thumbnail image
	this.getThumbId = function() {
		if (this.thumbId == null) {
			// lazy: may not have been set on creation
			var result = SRV.get("q-user-image", null, "user", this.name);
			if (result != null && result.length > 0) {
				this.thumbId = result[0].getField("id");
			}
		}
		return this.thumbId;
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
			tracer.setLocation(track.getLastPoint());

			tracer.activeTrack = track;

			tracer.activate();
			track.showTitle();
			track.showInfo();
			GTW.showStatus('track drawn');

		}

		// Get GTX document by id from server
		GTW.showStatus('drawing track ' + name + '...');

		SRV.get('get-track', this.onGetTrackRsp, 'id', id, 'format', 'gtx', 'attrs', 'lon,lat,t', 'mindist', GTW.minTrackPtDist);
	}

	// Set Tracer at lon/lat location
	this.getLocation = function() {
		return this.point;
	}

	// Set Tracer at lon/lat location
	this.setLocation = function(point, time) {
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
			this.tlabel.moveToLatLng(this.point);
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
      html += 'Last seen on ' + GTW.formatDateAndTime(this.point.time) + '<br/>at ' + this.point.lng() + ', ' + this.point.lat();

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
		DH.setHTML('tracerinfo', this.name);
	}

	// Is Tracer visible ?
	this.suspendTrack = function() {

	}

	// Is Tracer visible ?
	this.isVisible = function() {
		return this.hidden == false;
	}

	/** Center map around tracer location. */
	this.zoomTo = function () {
		if (this.point != null) {
			GMAP.map.setCenter(this.point, GMAP.map.getZoom());
		}
	}


}

