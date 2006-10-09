// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

// $Id$

/**
 * Control playing back Tracks.
 *
 * @constructor
 */
function TrackPlayer() {

	this.track = null;
	this.active = false;
	this.playing = false;
	this.intervalId = null;
	this.segIndex = 0;
	this.pointIndex = 0;
	this.featureIndex = 0;
	this.nextFeature = null;
	this.playPoints = [];
	this.FEATURE_DISPLAY_TIME = 2000;
	this.POINT_INTERVAL_TIME = 100;
	this.featureShowing = false;
	this.controls = DH.getObject('trackcontrols')

	// Setup controls-click callbacks
	var self = this;

	this.onPlayPause = function(e) {
		DH.cancelEvent(e);

		// First time ?
		if (self.isActive() == false) {
			self.play();
			return;
		}

		// Pause/resume when already active
		if (self.isPlaying()) {
			self.pause();
		} else {
			self.resume();
		}

	}

	this.onStop = function(e) {
		DH.cancelEvent(e);
		self.stop();

		if (self.track != null) {
			self.track.draw();
		}
	}

	this.onNext = function(e) {
		DH.cancelEvent(e);
		self.next();
	}

	this.onPrev = function(e) {
		DH.cancelEvent(e);
		self.prev();
	}

	DH.addEvent('trackplaypause', 'click', this.onPlayPause, true);
	DH.addEvent('trackstop', 'click', this.onStop, true);
	DH.addEvent('tracknext', 'click', this.onNext, true);
	DH.addEvent('trackprev', 'click', this.onPrev, true);


	// Setting the visibility to visible
	this.hide = function() {
		DH.hide(this.controls);
		GTW.getFeaturePlayer().hide();
	}

	// Are we initialized ?
	this.isActive = function() {
		return this.active;
	}

	// Are we playing a Track ?
	this.isPlaying = function() {
		return this.active == true && this.playing == true;
	}

	// Play next point
	this.next = function() {
		if (this.isPlaying() == true) {
			return;
		}

		if (this.isActive() == false) {
			this._initPlay();
		}

		this._playNext();
	}

	// Play previous point
	this.prev = function() {
		this.showInfo('not yet implemented');
	}

	// Show message in window
	this.showInfo = function(msg) {
		DH.setHTML('trackview', msg);
	}

	// Show general track info
	this.showTrackInfo = function() {
		this.showInfo('start: ' + GTW.formatDateAndTime(this.track.startDate) + '<br/>end: ' + GTW.formatDateAndTime(this.track.endDate) + '<br/>distance: ' + this.track.distance.toFixed(2) + ' km');
	}

	// Suspend playing
	this.pause = function() {
		this.playing = false;
		// this.playPause.className = 'trackplayerplay';

		// Draw remaining points
		if (this.playPoints.length > 0) {
			this.track.drawPoints(this.playPoints);
			var lastPt = this.playPoints[this.playPoints.length - 1]
			this.playPoints = [];
			this.playPoints.push(lastPt);
		}

		this.showInfo('paused');
		this._stopLoop();

	}


	// Go playing
	this.play = function() {
		if (this.isActive() == false) {
			this._initPlay();
		}

		// this.playPause.className = 'trackplayerpause';

		this.playing = true;
		this.showInfo('playing');

		this._startLoop(this.POINT_INTERVAL_TIME);
	}

	// Resume playing
	this.resume = function() {
		GTW.showStatus('playing resumed');

		this.playing = true;
		// this.playPause.className = 'trackplayerpause';
		var player = this;
		this._startLoop(this.POINT_INTERVAL_TIME);
	}

	// Setting the visibility to visible
	this.show = function() {
		DH.show(this.controls);
		GTW.getFeaturePlayer().show();
	}

	// Stop playing
	this.stop = function() {
		this.pause();
		this.active = false;
		this.intervalId = null;
		this.segIndex = 0;
		this.pointIndex = 0;
		this.curPoint = null;
		this.featureIndex = 0;
		this.nextFeature = null;
		this.playPoints = [];
		this.track.showInfo();

	}

	// Set track to control
	this.setTrack = function(track) {
		if (this.track == track) {
			return;
		}
		if (this.isActive() == true) {
			this.stop();
		}
		this.track = track;
		GTW.getFeaturePlayer().setFeatureSet(track.featureSet);
		this.track.showInfo();
	}

	// Initalize play-state
	this._initPlay = function() {
		if (this.track == null) {
			return;
		}

		this.track.clear();
		this.segIndex = 0;
		this.pointIndex = 0;
		this.featureIndex = 0;
		this.distance = 0;
		this.nextFeature = this.track.featureSet.getFeature(this.featureIndex++);

		this.curPoint = this.track.getPoint(0, this.pointIndex++);
		if (this.curPoint == null) {
			return;
		}
		GMAP.map.panTo(this.curPoint);

		this.active = true;

	}

	// Start interval loop
	this._startLoop = function(interval) {
		this._stopLoop();
		var player = this;
		this.intervalId = setInterval(function() {
			player._playNext()
		}, interval);
	}

	// Stop interval loop
	this._stopLoop = function() {
		if (this.intervalId == null) {
			return;
		}
		clearInterval(this.intervalId);
		this.intervalId = null;
	}

	// Play next points
	this._playNext = function() {
		try {
			if (this.isPlaying() == true && this.featureShowing == true) {
				this.featureShowing == false;
				this._startLoop(this.POINT_INTERVAL_TIME);
			}

			var segment = this.track.getSegment(this.segIndex);

			if (this.curPoint != null) {
				// Recenter when map edge reached
				if (!GMAP.map.getBounds().contains(this.curPoint)) {
					GMAP.map.panTo(this.curPoint);

					// Wait a while
					if (this.isPlaying() == true) {
						this.featureShowing = true;
						this._startLoop(this.FEATURE_DISPLAY_TIME);
					}
				}

				var endOfSeg = this.pointIndex >= segment.length - 1;
				var endOfTrack = endOfSeg && (this.segIndex >= this.track.segments.length - 1);
				if (this.nextFeature != null && (this.curPoint.time >= this.nextFeature.time || endOfTrack == true)) {
					this._playFeature(this.nextFeature);
					this.nextFeature = this.track.featureSet.getFeature(this.featureIndex++);
					return;
				}

				this.track.getTracer().setLocation(this.curPoint);
				this.playPoints.push(this.curPoint);
				this.distance += this.curPoint.distance;

				if (this.playPoints.length % 5 == 0 || this.isPlaying() == false) {
					this.showInfo(GTW.formatDateAndTime(this.curPoint.time) + ' - ' + this.distance.toFixed(2) + ' km - ' + this.curPoint.speed.toFixed(2) + ' km/h');
				}

				// Draw every N points or if end of segment is reached
				if (this.playPoints.length % 5 == 0 || endOfSeg == true) {
					this.track.drawPoints(this.playPoints);
					this.playPoints = [];
					if (endOfSeg == false) {
						this.playPoints.push(this.curPoint);
					}
				}
			}

			// Advance point  and test for end of segment
			if (segment != null && ++this.pointIndex >= segment.length) {
				this.playPoints = [];
				this.showInfo('end of segment');
				this.pointIndex = 0;
				++this.segIndex;
			}

			// Stop if end of track and no features remaining
			if (this.segIndex >= this.track.segments.length && this.nextFeature == null) {
				this.stop();
			} else {
				this.curPoint = this.track.getPoint(this.segIndex, this.pointIndex);
			}

		} catch(e) {
			// panic
			this.stop();
			this.showInfo('trackplay canceled');
		}
	}

	// Play feature
	this._playFeature = function(feature) {
		feature.show();
		feature.blink(6);
		feature.display();

		this.featureShowing = true;
		if (this.isPlaying() == true) {
			this._startLoop(this.FEATURE_DISPLAY_TIME);
		}
	}

}
