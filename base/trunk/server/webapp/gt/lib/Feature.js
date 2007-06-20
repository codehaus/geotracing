// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

// $Id$

var FEAT = {
	current: null
}

/**
 * Location-based item on map.
 * GIS-folks call that a "feature".
 *
 * @constructor
 */
function Feature(theId, name, desc, type, time, lon, lat) {
	this.id = theId;
	this.name = name;
	this.type = type;
	this.desc = desc;
	if (!this.desc || this.desc == null || this.desc == 'null') {
		this.desc = 'no description';
	}
	this.time = new Number(time);
	// somehow needed
	this.tlabel = null;
	this.iconId = 'featicon' + this.id;
	this.previewId = 'feat' + this.id;
	this.previewTileId = 'featurepreview';
	this.bgColor = GTW.FEATURE_BG_COLOR;
	this.fgColor = GTW.FEATURE_FG_COLOR;
	this.gLatLng = new GLatLng(lat, lon);
	this.userName = null;
	this.record = null;

	this.blink = function(cnt) {
		DH.blink(this.iconId, cnt, 150);
	}

	this.blowUp = function(cnt) {
		GMAP.map.showMapBlowup(this.getGLatLng());
	}

	// Displays feature in Panel
	this.display = function() {
		FEAT.current = this;
		if (this.record != null && this.record != -1) {
			this._display();
			return;
		}

		// No record: fetch and display later
		this._queryInfo();
	}

	this.getBGColor = function() {
		return this.bgColor;
	}

	this.getDate = function() {
		var date = new Date(this.time);
		return date.format("DDD D MMM YYYY HH:mm:ss");
	}

	this.getGLatLng = function() {
		return this.gLatLng;
	}


	this.getTitle = function() {
		return this.name;
	}

	this.getPreviewTile = function() {
		return DH.getObject(this.previewTileId);
	}

	this.remove = function() {
		if (this.tlabel == null) {
			return;
		}
		// remove Tlabel from map
		GMAP.map.removeTLabel(this.tlabel);

		this.tlabel = null;
	}

	// Show icon on map
	this.show = function() {
		var tl = new TLabel(true);
		tl.id = 'tlab' + this.id;
		tl.anchorLatLng = this.getGLatLng();
		tl.anchorPoint = 'center';

		// Overridden in subclass
		tl.content = this.getIconDiv();
		tl.setScaling(this.iconId, 16, 16, .2);

		// Add Tlabel to map
		GMAP.map.addTLabel(tl);

		this.tlabel = tl;
		var self = this;

		// Define callback for mouse over medium icon
		this.onMouseOverIcon = function (e) {
			self.display();
			// self.blowUp();
			DH.cancelEvent(e);
		}

		DH.addEvent(DH.getObject(this.iconId), 'mouseover', this.onMouseOverIcon, false);
	}

	/** Internal display function. */
	this._display = function() {
		if (FEAT.current != this && this.record != null && this.record != -1) {
			return;
		}
		this._displayTitle();
		this._displayInfo();
		this._displayPreview();
		this._displayDescr();
		this._displayUser();
	}

	this._displayTitle = function() {
		DH.setHTML('featuretitle', this.getTitle());
	}

	this._displayUser = function() {
		this.userName = this.record.getField('loginname');

		if (this.userName != null) {
			DH.setHTML('tracerinfo', this.userName);
			var tracer = GTW.getTracer(this.userName);
			if (!tracer || tracer == null) {
				tracer = GTW.createTracer(this.userName);
			}
			tracer.showInfo();
		}
	}

	this._displayInfo = function() {
		DH.setHTML('featureinfo', this.getDate() + ' <span class="cmtlink"><a href="#" onclick="CMT.showCommentPanel(' + this.id + ',\'' + this.type + '\',\'' + this.name + '\')" >comments (' + this.record.getField('comments') + ')</a></span>');
		if (CMT.isCommentPanelOpen() == true) {
			// CMT.showCommentPanel(this.id, this.type, this.name);
		}
	}

	this._displayDescr = function() {
		DH.setHTML('featuredesc', this.desc);
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
			}
			self._display();
		}
		//alert('getting thumbid for ' + this.name);
		this.record = -1;
		SRV.get("q-feature-info", this.onGetRecord, "id", this.id);
	}
}




