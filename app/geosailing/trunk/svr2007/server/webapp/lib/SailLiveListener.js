// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

var LIVE_STATS = new Array('-', '-','-','-');

/**
 * LiveListener - acts on incoming (Pushlet) events.
 * @constructor
 * $Id: SailLiveListener.js,v 1.2 2006-08-11 16:13:56 just Exp $
 */
function SailLiveListener(aStatusElm) {
	LiveListener.apply(this, new Array(aStatusElm));

	this.clearStatus = function() {
	}

	this.onHeartbeat = function(tracer, event) {
		// User sends heartbeat
	}

	this.showStatus = function(aTracer, aMsg) {
		var date = new Date();
		var dateStr = date.format("HH:mm:ss");
		var msg = '[' + dateStr + ']&nbsp&nbsp;' + aTracer.name + ' - ' + aMsg;
		var statusElm;
		LIVE_STATS[3] = LIVE_STATS[2];
		LIVE_STATS[2] = LIVE_STATS[1];
		LIVE_STATS[1] = LIVE_STATS[0];
		LIVE_STATS[0] = msg;
		for (var i=0; i < LIVE_STATS.length; i++) {
			DH.setHTML('livestatus' + i, LIVE_STATS[i]);

			if (i == 0) {
				DH.blink('livestatus' + i, 4, 150);
			}
		}
	}

	this.onTrackCreate = function(tracer, event) {
	}

	this.onTrackDelete = function(tracer, event) {
	}

	this.onTrackSuspend = function(tracer, event) {
	}

	this.onTrackResume = function(tracer, event) {
	}

	this.onPOIAdd = function(tracer, event) {
	}

	this.onMediumAdd = function(tracer, event) {
		// User submits medium
		var medium = GTW.createMedium(event.get('id'),
				event.get('name'),
				'live upload by ' + tracer.name,
				event.get('kind'),
				event.get('mime'),
				event.get('time'),
				event.get('lon'),
				event.get('lat'));

		medium.userName = tracer.name;
		MYAPP.addLiveMedium(medium);
		medium.show();
		medium.blink(20);
		// medium.display();
		this.showStatus(tracer, 'stuurt ' + event.get('kind'));
	}

	this.onMove = function(tracer, event) {
		// User sends location
		// Set indicator
		tracer.setLive();
		if (tracer.activeTrack == null) {
			tracer.newTrack(event.get('trackid'), event.get('trackname'));
			tracer.show();
			// tracer.readTrack(event.get('trackid'), event.get('trackname'), true);
		}

		tracer.move(event.get('lon'), event.get('lat'), event.get('t'));
		this.showStatus(tracer, tracer.speed + ' km/h - ' + tracer.courseStr);
		BOAT.onMove(tracer);
	}

}
