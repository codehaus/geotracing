// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/**
 * LiveListener - acts on incoming (Pushlet) events.
 * @constructor
 * $Id: BliinLiveListener.js,v 1.2 2006-08-11 16:13:56 just Exp $
 */
function BliinLiveListener(aStatusElm) {
	LiveListener.apply(this, new Array(aStatusElm));

	this.clearStatus = function() {
	}

	this.onHeartbeat = function(tracer, event) {
		// User sends heartbeat
	}

	this.showStatus = function(aTracer, aMsg) {
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
		MYAPP.media.addFeature(medium);
		medium.show();
		medium.blink(20);
		// medium.display();
		// this.showStatus(tracer, 'adds ' + event.get('kind'));
	}
}
