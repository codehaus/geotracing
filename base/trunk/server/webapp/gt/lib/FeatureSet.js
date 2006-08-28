// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * View Media.
 *
 * $Id$
 */

function FeatureSet() {
	this.features = [];
	this.index = 0;

	// Get Feature by index
	this.getFeature = function(index) {
		if (this.features.length == 0 || index < 0 || index >= this.features.length) {
			return null;
		}

		return this.features[index];
	}

	// Add a media from (server) Records
	this.addMedia = function(records) {
		for (var i = 0; i < records.length; i++) {
			// Create and draw location-based medium
			this.addMedium(records[i]);
		}
	}

	// Add a media from (server) Records
	this.addMedium = function(record) {
		var medium = GTW.createMedium(record.getField('id'),
				record.getField('name'),
				record.getField('description'),
				record.getField('kind'),
				record.getField('mime'),
				record.getField('creationdate'),
				record.getField('lon'),
				record.getField('lat'));

		// Optional
		medium.userName = record.getField('loginname');
		
		this.addFeature(medium);
	}

	// Add a media from (server) Records
	this.addPOIs = function(records) {
		for (var i = 0; i < records.length; i++) {
			this.addPOI(records[i]);
		}
	}

	// Add a media from (server) Records
	this.addPOI = function(record) {
		var poi = GTW.createPOI(record.getField('id'),
				record.getField('name'),
				record.getField('description'),
				record.getField('type'),
				record.getField('time'),
				record.getField('lon'),
				record.getField('lat'));

		this.addFeature(poi);

	}
	// Add a media from (server) Records
	this.addFeature = function(feature) {
		this.features[this.features.length] = feature;
	}

	this.clear = function () {
		for (var i = 0; i < this.features.length; i++) {
			if (this.features[i]) {
				this.features[i].remove();
			}
		}
	}

	this.dispose = function () {
		for (var i = 0; i < this.features.length; i++) {
			if (this.features[i]) {
				this.features[i].remove();
				delete this.features[i];
			}
		}
		this.features = [];
	}

	this.getCurrent = function () {
		return this.features[this.index];
	}

	/** Show features on map. */
	this.show = function () {
		for (var i = 0; i < this.features.length; i++) {
			if (this.features[i]) {
				this.features[i].show();
			}
		}
	}

	this.displayFirst = function(centerMap) {
		this.index = 0;
		return this._displayFeature(this.features[this.index], centerMap);
	}

	this.displayLast = function (centerMap) {
		this.index = this.features.length - 1;
		return this._displayFeature(this.features[this.index], centerMap);
	}

	this.displayNext = function (centerMap) {
		if (++this.index >= this.features.length) {
			this.index = 0;
		}
		return this._displayFeature(this.features[this.index], centerMap);
	}

	this.displayPrev = function (centerMap) {
		if (--this.index <= 0) {
			this.index = this.features.length - 1;
		}
		return this._displayFeature(this.features[this.index], centerMap);
	}

	this._displayFeature = function(feature, centerMap) {
		if (feature) {
			var blinks = 12;
			if (centerMap && centerMap == true) {
				GMAP.map.panTo(feature.getGLatLng());
				blinks = 20;
			}
			feature.display();
			feature.blink(blinks);
			return feature;
		}
	}

}

