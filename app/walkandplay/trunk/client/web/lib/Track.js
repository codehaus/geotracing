// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
* Track.
*
* $Id: Track.js,v 1.8 2006-07-21 22:28:35 just Exp $
*/


// Track
function Track(id, name, tracer) {
	this.id = id;
	this.name = name;
	this.startDate = 0;
	this.endDate = 0;
	this.tracer = tracer;
	this.segments = [];
	this.featureSet = new FeatureSet();
	this.polyLines = [];
	this.color = tracer.color;

	// Add point
	this.addPoint = function (aPoint) {
		if (this.segments.length == 0) {
			this.segments[0] = [];
		}

		this.segments[this.segments.length - 1].push(aPoint);
	}

	// Add live point
	this.addLivePoint = function (aPoint) {

		var lastPoint = this.getLastPoint();
		this.addPoint(aPoint);

		// Only draw line if we have a previous point
		if (lastPoint != null) {
			var ptArr = [];
			ptArr[0] = lastPoint;
			ptArr[1] = aPoint;
			this.drawPoints(ptArr);
		}
	}

	// Add medium to this tracer
	this.addMedium = function (medium) {
		this.featureSet.addFeature(medium);
		medium.show();
	}

	// Add medium to this tracer
	this.addPOI = function (poi) {
		this.featureSet.addFeature(poi);
		poi.show();
	}

	this.clear = function () {
		for (var i = 0; i < this.polyLines.length; i++) {
			GMAP.map.removeOverlay(this.polyLines[i]);
		}

		this.polyLines = [];

		this.featureSet.clear();

	}

	// Drawin view
	this.draw = function () {

		for (var i = 0; i < this.segments.length; i++) {
			// Draw the entire Track on map
			this.drawPoints(this.segments[i]);
		}

		this.featureSet.show();
		this.featureSet.displayFirst();
	}

	// Draw array of points
	this.drawPoints = function (ptArr) {
		// Draw slices to prevent browser problems with large polyliness
		if (ptArr.length > 200) {
			this.drawPoints(ptArr.slice(0, 200));
			this.drawPoints(ptArr.slice(200));
			return;
		}

		// Draw using GMap Polyline
		var pl = new GPolyline(ptArr, this.color, GTW.polyLineWidth, GTW.polyLineOpacity);
		GMAP.map.addOverlay(pl);
		this.polyLines.push(pl);
	}

	// remove from view
	this.getLastPoint = function () {
		var segment = this.getSegment(this.segments.length - 1);
		if (segment == null) {
			return null;
		}

		return this.getPoint(this.segments.length - 1, segment.length - 1);
	}

	// remove from view
	this.getPoint = function (segIndex, ptIndex) {
		var segment = this.getSegment(segIndex);
		if (segment == null) {
			return null;
		}

		if (segment.length == 0 || ptIndex < 0 || ptIndex >= segment.length) {
			return null;
		}

		return segment[ptIndex];
	}

	// get track segment
	this.getSegment = function (segIndex) {
		if (this.segments.length <= 0 || segIndex < 0 || segIndex >= this.segments.length) {
			return null;
		}
		return this.segments[segIndex];
	}

	this.getTracer = function() {
		return this.tracer;
	}

	// remove from view
	this.remove = function () {
		this.clear();
		this.polyLines = [];
		this.segments = [];
		this.featureSet.clear();
		DH.setHTML('tracktitle', '&nbsp;');
		DH.setHTML('trackview', 'track info');
	}

	// Populate from GTX (GPX-like) document
	this.setGTX = function (gtx) {
		// Sanity check for invalid XML (empty doc)
		if (!gtx.documentElement) {
			alert('empty track document (maybe invalid chars in XML)');
			return;
		}

		// Get general info
		var infoElm = gtx.documentElement.getElementsByTagName('info')[0];
		if (infoElm) {
			this.name = infoElm.getAttribute('name');
			this.startDate = new Number(infoElm.getAttribute('startdate'));
			this.endDate = new Number(infoElm.getAttribute('enddate'));
		}

		// Get segments
		var segElms = gtx.documentElement.getElementsByTagName('seg');
		this.segments = [];

		// Sick case
		if (segElms) {
			this.distance = 0;
			for (i = 0; i < segElms.length; i++) {

				// Get points in segment
				var ptElements = segElms[i].getElementsByTagName('pt');

				// Sick case
				if (!ptElements || ptElements.length == 0) {
					continue;
				}

				// Go through all Track points for this segment
				var ptArr = [];
				var nextPt;
				for (j = 0; j < ptElements.length; j++) {
					nextPt = new GLatLng(ptElements[j].getAttribute('lat'), ptElements[j].getAttribute('lon'));
					nextPt.time = new Number(ptElements[j].getAttribute('t'));
					nextPt.distance = 0;
					nextPt.speed = 0;
					if (j > 0) {
						nextPt.distance = GMAP.distance(ptArr[j - 1], nextPt);
						nextPt.speed = nextPt.distance / ((nextPt.time - ptArr[j - 1].time) / 3600000);
						this.distance += nextPt.distance;
					}
					ptArr.push(nextPt);
				}

				this.segments.push(ptArr);
			}

		}

		// Get medium locations
		var mediumElements = gtx.documentElement.getElementsByTagName('medium');
		this.media = [];
		if (mediumElements) {
			var nextMedium;
			var nextDesc = 'no description';
			for (i = 0; i < mediumElements.length; i++) {
				nextMedium = mediumElements[i];
				if (nextMedium.childNodes[0]) {
					nextDesc = nextMedium.childNodes[0].nodeValue;
				}
				// Create and draw location-based medium
				medium = GTW.createMedium(nextMedium.getAttribute('id'),
						nextMedium.getAttribute('name'),
						nextDesc,
						nextMedium.getAttribute('kind'),
						nextMedium.getAttribute('mime'),
						nextMedium.getAttribute('time'),
						nextMedium.getAttribute('lon'),
						nextMedium.getAttribute('lat'));

				this.featureSet.addFeature(medium);
			}
		}


		// Get POI locations
		var poiElements = gtx.documentElement.getElementsByTagName('poi');
		this.pois = [];
		if (poiElements) {
			var nextPOI;
			for (i = 0; i < poiElements.length; i++) {
				nextPOI = poiElements[i];

				// Create and draw location-based medium
				poi = GTW.createPOI(nextPOI.getAttribute('id'),
						nextPOI.getAttribute('name'),
						nextPOI.childNodes[0].nodeValue,
						nextPOI.getAttribute('type'),
						nextPOI.getAttribute('time'),
						nextPOI.getAttribute('lon'),
						nextPOI.getAttribute('lat'));
				this.featureSet.addFeature(poi);
			}
		}
	}

	// Show general track info
	this.showInfo = function() {
		DH.setHTML('trackview', 'start: ' + GTW.formatDateAndTime(this.startDate) + '<br/>end: ' + GTW.formatDateAndTime(this.endDate) + '<br/>distance: ' + this.distance.toFixed(2) + ' km');
	}
	
	// Show general track info
	this.showTitle = function() {
		DH.setHTML('trackinfo', this.name + ' [' + this.id + ']');
	}

	this.showTitle();
}

