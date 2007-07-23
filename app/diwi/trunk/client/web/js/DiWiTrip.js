/**
 * Trip display and such.
 *
 * author: Just van den Broecke
 */
var TRIP = {
	trips: null,
	mediumPopup: null,

	onShowTrip: function(rsp) {
		DH.displayOff('triplist');
		DIWIAPP.pr('Mijn trip ingetekend.');
		DH.displayOn('triplistbacklink');
		MAP.show();
		MAP.addMarkerLayer('Mijn trip');
		TRIP.showTrace(rsp.getElementsByTagName('pt'));
		TRIP.showTraceMedia(rsp.getElementsByTagName('medium'));
	},

	onShowTrips: function(rsp) {
		TRIP.trips = TRIP.rsp2Records(rsp);

		var tripsCont = ' ';
		var nextTrip;
		for (var i = 0; i < TRIP.trips.length; i++) {
			nextTrip = TRIP.trips[i];
			tripsCont += '<a onclick="TRIP.showTrip(\'' + nextTrip.getField('id') + '\');" href="#"> ' + nextTrip.getField('name') + '</a><br/>';
		}
		DH.setHTML('triplist', tripsCont);
		DH.displayOn('triplist');
		DIWIAPP.pr('Hiernaast een lijst van uw gemaakte tochten.');
	},

  	showTraceMedia: function(theMedia) {
		var x,y,img,w,h,medium;
		img = 'media/images/icon-trace.png';
		w = 10;
		h = 10;

		for (var i = 0; i < theMedia.length; i++) {
			medium = theMedia[i];
			// alert('medium');
			x = medium.getAttribute('x');
			y = medium.getAttribute('y');
			var marker = new OpenLayers.Marker(new OpenLayers.LonLat(x,y));
			marker.events.register('mousedown', marker, function(evt) { TRIP.showMedium(medium); Event.stop(evt); });
			MAP.overlays['markers'].addMarker(marker);
			//MAP.map.addPopup(popup);
			//MAP.addMarker(x, y, img, w, h);
		}
	},

	showMedium: function(medium) {
		var x = medium.getAttribute('x');
		var	y = medium.getAttribute('y');
		var id = medium.getAttribute('id');
		if (TRIP.mediumPopup == null) {
			var popup = new OpenLayers.Popup("mediumpop"+i,
				   new OpenLayers.LonLat(x,y),
				   new OpenLayers.Size(320,240),
				   "here is medium id=" + id,
				   false);
		}
		popup.show();
		
	},

	showTrace: function(thePts) {
		var x,y,img,w,h,pt,xsw=0,ysw=0,xne=0,yne=0;
		img = 'media/images/icon-trace.png';
		w = 10;
		h = 10;

		for (var i = 0; i < thePts.length; i++) {
			pt = thePts[i];
			x = pt.getAttribute('x');
			y = pt.getAttribute('y');
			if (i == 0) {
				xsw = xne = x;
				ysw = yne = y;
			} else {
				if (x < xsw) {
					xsw = x;
				} else if (x > xne) {
					xne = x;
				}
				if (y < ysw) {
					ysw = y;
				} else if (y > yne) {
					yne = y;
				}
			}

			MAP.addMarker(x,y,img,w,h);
		}

		if (xsw != 0 & xne != 0 && xne != xsw) {
			var bounds = new OpenLayers.Bounds(xsw, ysw, xne, yne);
			MAP.map.zoomToExtent(bounds);
		}
	},

	showTrips: function() {
 		DH.displayOff('triplistbacklink');
		KW.DIWI.gettrips(TRIP.onShowTrips, DIWIAPP.personId);
		MAP.hide();
	},

	showTrip: function(anId) {
		KW.DIWI.gettrip(TRIP.onShowTrip, anId);
		DIWIAPP.pr('Trip ophalen...');
		return false;
	},

	rsp2Records: function(anRsp) {
		var records = [];

		// Convert xml doc to array of Record objects
		var recordElements = anRsp.childNodes;
		for (i = 0; i < recordElements.length; i++) {
			records.push(new XMLRecord(recordElements[i]));
		}
		return records;
	}
}
