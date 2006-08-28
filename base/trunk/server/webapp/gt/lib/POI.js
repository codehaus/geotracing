// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

// $Id$

DH.include('Feature.js');


/**
 * Location-based Point Of Interest (e.g. bomb).
 * @extends Feature
 * @constructor 
 */
function POI(id, name, desc, type, time, lon, lat) {
	Feature.apply(this, new Array(id, name, desc, type, time, lon, lat));

	// Get icon content.
	this.getIconDiv = function() {

		var src = 'img/poi.gif';
		var img = '<img title="' + this.getTitle() + '" src="' + src + '" border="0"  alt="" />';

		return '<div class="poiicon" id="' + this.iconId + '" style="border: 1px solid ' + this.bgColor + ';" >' + img + '</div>';

	}

	// Display in Panel
	this._displayPreview = function() {
		var previewTile = this.getPreviewTile()

		DH.setHTML(previewTile, '<b>POI</b><br/>name: ' + this.name + '<br/>type: ' + this.type + '<br/>lat/lon: ' + this.getGLatLng().toString());
	}
}

