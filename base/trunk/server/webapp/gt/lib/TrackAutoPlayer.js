// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
* TrackAutoPlayer.
*
* $Id$
*/


// Control automatic playing back Tracks
function TrackAutoPlayer() {
	this.INTERVAL_MILLIS = 2000;
	this.intervalId = null;
	this.tracer = null;
	this.lastTracer = null;
	this.state = 'IDLE';
	this.n = 0;

	// Go playing
	this.start = function() {
		this.stop();
		var self = this;
		this.intervalId = setInterval(function() {
			self._doWork()
		}, this.INTERVAL_MILLIS);
	}

	// Go playing
	this.stop = function() {
		if (this.intervalId == null) {
			return;
		}
		clearInterval(this.intervalId);
		this.intervalId = null;
	}

	// Timer callback
	this._doWork = function() {
		if (this.intervalId == null) {
			return;
		}

		// Do action based on state
		if (this.state == 'IDLE') {
			if (this.tracer != null) {
				this.lastTracer = this.tracer;
				this.tracer = null;
			}

			// read random track info
			this.state = 'READING';

			this._getRandomTrack();
		} else if (this.state == 'READING') {
			GTW.showStatus('reading next track... ' + (this.n++));

			if (this.tracer != null && this.tracer.getActiveTrack() != null) {
				this.state = 'READY';
			}
		} else if (this.state == 'READY') {
			GTW.showStatus('ready');

			var track = this.tracer.getActiveTrack();
			var trackPlayer = GTW.getTrackPlayer();
			this.tracer.show();
			trackPlayer.setTrack(track);
			trackPlayer.hide();
			trackPlayer.play();
			this.state = 'PLAYING';
		} else if (this.state == 'PLAYING') {

			var trackPlayer = GTW.getTrackPlayer();
			if (trackPlayer.isPlaying() == false) {
				this.state = 'IDLE';
			} else if (this.tracer.getActiveTrack() != null) {
				GTW.showStatus('playing ' + this.tracer.name + '/' + this.tracer.getActiveTrack().name);
			}
		}
	}

	// Timer callback
	this._getRandomTrack = function() {

		// Need this for JS local scope in callback
		var player = this;
		// Define callback for async response
		this.onQueryTrackRsp = function (records) {
			//  id, name,state,loginname,lon,lat
			GTW.showStatus('read ' + records[0].getField('name'));
			player.tracer = GTW.createTracer(records[0].getField('loginname'), records[0].getField('lon'), records[0].getField('lat'), records[0].getField('time'));

			player.tracer.readTrack(records[0].getField('id'), records[0].getField('name'), false);

			if (player.lastTracer != null) {
				player.lastTracer.clear();
				player.lastTracer = null;
			}
		}
		SRV.get('q-random-track', this.onQueryTrackRsp);
	}

}

