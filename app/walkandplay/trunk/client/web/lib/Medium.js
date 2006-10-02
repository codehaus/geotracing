// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

// $Id: Medium.js,v 1.8 2006-07-06 23:06:16 just Exp $

DH.include('Feature.js');

/**
 * Location-based medium.
 * @extends Feature
 * @constructor
 */
function Medium(id, name, desc, type, mime, time, lon, lat) {
	Feature.apply(this, new Array(id, name, desc, type, time, lon, lat))

	this.mime = mime;
	this.url = 'media.srv?id=' + this.id;

	// Shows icon on map
	this.getIconDiv = function() {
		return '<div class="medicon" id="' + this.iconId + '" style="background-color:' + this.bgColor + ';" >&nbsp;&nbsp;&nbsp;&nbsp;</div>';
	}

	this.getTitle = function() {
		return this.name + ' [' + this.id + ']';
	}

	// Displays medium
	this._displayPreview = function() {
		if (this.type == 'video') {
			this._displayVideo();
		} else if (this.type == 'image') {
			this._displayImage();
		} else if (this.type == 'audio') {
			this._displayAudio();
		}
	}

	this.displayImageFull = function() {
		var url = this.url + '&resize=640x480';
		var panel = GTW.getImageFullPanel();
		panel.setTitle(this.getTitle());
		panel.setContent('<img title="' + this.getTitle() + '" src="' + url + '" border="0"  />');
	}

	this._displayAudio = function() {
		var previewTile = this.getPreviewTile()

		previewTile.innerHTML = '<img class="mediumpreview" id="' + this.previewId + '" title="click to play" src="img/audioicon.jpg" border="0"  />';

		var medium = this;
		this.onClick = function(e) {
			DH.cancelEvent(e);

			// medium.tracer.full = medium.tracer.full == true ? false : true;
			// if (medium.tracer.full == false) {
			//   content = '<img title="' + medium.getTitle() + '" src="img/videoicon.gif" border="0"  />';
			// } else {
			var url = medium.url;
			content = '<embed src="' + medium.url + '"/>';
			// (navigator.userAgent.toLowerCase().indexOf('mac') != -1) ?
			//'<object classid="clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B"  codebase="http://www.apple.com/qtactivex/qtplugin.cab" align="left"><param name="src" value="' + medium.url + '"><param name="AUTOPLAY" value="true"><param name="type" value="video/quicktime"><param name="CONTROLLER" value="true"><embed src="' + medium.url + '" width="176" height="154" autoplay="true" controller="true" loop="false" type="video/quicktime" pluginspage="http://www.apple.com/quicktime/download/"></embed></object>' :
			//'<embed src="' + medium.url + '"/>';
			//   '<object classid="clsid:22D6F312-B0F6-11D0-94AB-0080C74C7E95"> <param name="FileName" value="'+medium.url+'"><param name="ShowControls" value="true"><param name="bgcolor" value="#ffffff"><param name="loop" value="true"></object>';
			// }

			//var d = DH.getObject(medium.divId);
			// d.innerHTML = content;
			previewTile.innerHTML = content;
			// DH.removeEvent(medium.previewId, 'click', medium.onClick);
		}

		DH.addEvent(this.previewId, 'click', this.onClick, false);
	}

	this._displayImage = function() {
		var src = this.url + '&resize=320x240';
		var previewTile = this.getPreviewTile()

		previewTile.innerHTML = '<img class="mediumpreview" id="' + this.previewId + '" title="click to see larger image" src="' + src + '" border="0"  />';

		var medium = this;
		this.onClick = function(e) {
			DH.cancelEvent(e);
			medium.displayImageFull();
		}

		DH.addEvent(this.previewId, 'click', this.onClick, false);
	}

	this._displayVideo = function() {
		var previewTile = this.getPreviewTile()

		previewTile.innerHTML = '<img class="mediumpreview" id="' + this.previewId + '" title="click to play" src="img/videoicon.gif" border="0"  />';

		var medium = this;
		this.onClick = function(e) {
			DH.cancelEvent(e);

			// medium.tracer.full = medium.tracer.full == true ? false : true;
			// if (medium.tracer.full == false) {
			//   content = '<img title="' + medium.getTitle() + '" src="img/videoicon.gif" border="0"  />';
			// } else {
			var url = medium.url;
			content = (navigator.userAgent.toLowerCase().indexOf('mac') != -1) ?
					  '<object classid="clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B" width="176" height="154" codebase="http://www.apple.com/qtactivex/qtplugin.cab" align="center"><param name="src" value="' + medium.url + '"><param name="AUTOPLAY" value="true"><param name="type" value="video/quicktime"><param name="CONTROLLER" value="true"><embed src="' + medium.url + '" width="176" height="154" autoplay="true" controller="true" loop="true" type="video/quicktime" pluginspage="http://www.apple.com/quicktime/download/"></embed></object>' :
					  '<object classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0" align="center"><param name="movie" value="' + medium.url + '&format=swf"><param name="quality" value="high"><param name="bgcolor" value="#ffffff"><param name="loop" value="true"><embed src="' + medium.url + '&format=swf" quality="high" bgcolor="#ffffff" swliveconnect="true" loop="true" type="application/x-shockwave-flash"pluginspage="http://www.macromedia.com/shockwave/download/index.cgi?p1_prod_version=shockwaveflash"></embed></object>';
			// }

			//var d = DH.getObject(medium.divId);

			previewTile.innerHTML = content;
			// DH.removeEvent(medium.previewId, 'click', medium.onClick);
		}

		DH.addEvent(this.previewId, 'click', this.onClick, false);
	}

}


