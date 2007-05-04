// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/**
 * LiveListener - acts on incoming (Pushlet) events.
 * @constructor
 * $Id: LiveListener.js 214 2006-12-07 15:40:51Z just $
 */
function LiveListener(aStatusElm) {
	this.statusElm = DH.getObject(aStatusElm);

	this.clearStatus = function() {
		DH.setHTML(this.statusElm, '&nbsp;');
	}

	this.showStatus = function(aTracer, aMsg) {
		DH.setHTML(this.statusElm, aTracer.name + ' ' + aMsg);
		DH.blink(this.statusElm, 4, 150);
	}

	this.onMove = function(tracer, event) {
		// User sends location
		// Set indicator
		tracer.setLive();
		if (tracer.activeTrack == null) {
			tracer.show();
			// tracer.readTrack(event.get('trackid'), event.get('trackname'), true);
		}

		tracer.move(event.get('lon'), event.get('lat'), event.get('t'));
		this.showStatus(tracer, 'moves');
	}

	this.onHeartbeat = function(tracer, event) {
		// User sends heartbeat
		this.showStatus(tracer, 'sends heartbeat');
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
				event.get('lat'),
				event.get('subtype'),
				event.get('id'));
		
		medium.userName = tracer.name;

		tracer.addMedium(medium);
		medium.show();
		medium.blink(20);
		medium.display();
		this.showStatus(tracer, 'adds ' + event.get('kind'));
	}

	this.onTrackCreate = function(tracer, event) {
		// User creates new track
		tracer.newTrack(event.get('id'), event.get('name'));
		this.showStatus(tracer, 'creates track ' + event.get('name'));
	}

	this.onTrackDelete = function(tracer, event) {
		tracer.deleteTrack(event.get('id'), event.get('name'));
		this.showStatus(tracer, 'deletes track ' + event.get('name'));
	}

	this.onTrackSuspend = function(tracer, event) {
		tracer.suspendTrack();
		this.showStatus(tracer, 'suspends track');
	}

	this.onTrackResume = function(tracer, event) {
		tracer.resumeTrack();
		this.showStatus(tracer, 'resumes track');
	}

	// Pushlet Data Event Callback from Server
	// These are events sent by tracing users (e.g. from mobile phone)
	this.onEvent = function (event) {
		// See EventPublisher.java for Event types/fields
		// Event types and attributes:
		// "user-hb" : id, username, time, [trackname]
		// "user-move": id, username, t, trackid, trackname, lon, lat
		// "medium-add": id, name, kind, mime, time, userid, username, trackid, trackname, lon, lat, ele
		// "medium-hit": id, name, type, state, time, userid, username, ownerid, ownername, trackid, trackname, lon, lat, ele
		//" track-create": id, name, userid, username
		// "track-delete": id, name, userid, username
		// "track-suspend": id, name, userid, username
		// "track-resume": id, name, userid, username
		// "comment-add" : id, target, ownerid, ownername (only sent to relevant /person/personid subjects)

		var eventType = event.get('event');

		var tracerName = event.get('username');
		var tracer = GTW.getTracer(tracerName);
		if (!tracer) {
			// create new Tracer object, most events have lon/lat
			tracer = GTW.createTracer(tracerName, event.get('lon'), event.get('lat'), event.get('time'));
		}

		if (GTAPP.mode != 'live') {
			this.showStatus(tracer, '<a href="#" onclick="GTAPP.mLive();return false">is live &lt;show it!&gt;</a>');
			return;
		}

		
		if (eventType == 'user-move') {
			this.onMove(tracer, event);
		} else if (eventType == 'user-hb') {
			this.onHeartbeat(tracer, event);
		} else if (eventType == 'medium-add') {
			this.onMediumAdd(tracer, event);
	//	} else if (eventType == 'poi-hit') {
	//		this.onPOIHit(tracer, event);
		} else if (eventType == 'track-create') {
			this.onTrackCreate(tracer, event);
		} else if (eventType == 'track-delete') {
			this.onTrackDelete(tracer, event);
		} else if (eventType == 'track-suspend') {
			this.onTrackSuspend(tracer, event);
		} else if (eventType == 'track-resume') {
			this.onTrackResume(tracer, event);
		} else {
			this.showStatus(tracer, 'unhandled event ' + eventType);
		}
	}


}
