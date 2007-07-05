/**
 * Trip display and such.
 *
 * author: Just van den Broecke
 */
var TRIP = {
	trips: null,

	onShowTrip: function(rsp) {
		DH.displayOff('triplist');
		MAP.show();
		MAP.addMarkerLayer();
		TRIP.showTrace(rsp.getElementsByTagName('pt'));
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
		DIWIAPP.pr('Hiernaast een lijst van uw gemaakte tochten.');
	},


	showTrace: function(thePts) {
		var x,y,img,w,h,pt;
		img = 'media/images/icon-trace.png';
		w = 10;
		h = 10;
		for (var i = 0; i < thePts.length; i++) {
			pt = thePts[i];
			x = pt.getAttribute('x');
			y = pt.getAttribute('y');

			MAP.addMarker(x,y,img,w,h);
		}
	},

	showTrips: function() {
		MAP.hide();
		KW.DIWI.gettrips(TRIP.onShowTrips, DIWIAPP.personId);
	},

	showTrip: function(anId) {
		KW.DIWI.gettrip(TRIP.onShowTrip, anId);
		DIWIAPP.pr('Hiernaast trip #' + anId);
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
