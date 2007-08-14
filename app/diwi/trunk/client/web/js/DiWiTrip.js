/**
 * Trip display and such.
 *
 * author: Just van den Broecke
 */
var TRIP = {
	trips: null,
	mediumPopup: null,

	curTrip: {
		points: null,
		media: null,
		poiHits: null,
		ugcHits: null
	},

	onShowTrip: function(rsp) {
		DH.displayOff('triplist');
		DH.displayOff('remarks');
		DIWIAPP.pr('Hiernaast vindt u uw tocht ingetekend.');
		DH.displayOn('triplistbacklink');
		MAP.show();
		MAP.addMarkerLayer('Mijn tocht');

		TRIP.curTrip.points = rsp.getElementsByTagName('pt');
		TRIP.curTrip.media = rsp.getElementsByTagName('medium');
		TRIP.curTrip.poiHits = null;
		TRIP.curTrip.ugcHits = null;

		TRIP.showTripPoints();
		TRIP.showTripMedia();
	},

	onShowTrips: function(rsp) {
		TRIP.trips = TRIP.rsp2Records(rsp);

		var tripsCont = ' ';
		var nextTrip;
		var date = ' ';
		for (var i = 0; i < TRIP.trips.length; i++) {
			nextTrip = TRIP.trips[i];
			date = new Date(new Number(nextTrip.getField('startdate')));
			tripsCont += (i+1) + '. <a onclick="TRIP.showTrip(\'' + nextTrip.getField('id') + '\');" href="#">tocht gemaakt op ' + date.format("DDDD D MMM YYYY") + '</a><br/>';
		}
		DH.setHTML('triplist', tripsCont);
		DH.displayOn('triplist');
		DH.displayOn('remarks');
		DIWIAPP.pr('Hiernaast een lijst van uw gemaakte tochten.');
	},

	showTripMedia: function() {
		var x,y,img,w,h,medium;
		img = 'media/images/icon-trace.png';
		w = 10;
		h = 10;

		for (var i = 0; i < TRIP.curTrip.media.length; i++) {
			TRIP.showTripMediumMarker(i);

			//MAP.addMarker(x, y, img, w, h);
		}
	},

	showTripMediumMarker: function(index) {
		// alert('medium');
		var medium = TRIP.curTrip.media[index];
		var x = medium.getAttribute('x');
		var y = medium.getAttribute('y');
		var ll = new OpenLayers.LonLat(x, y);
		var marker = new OpenLayers.Marker(ll);
		this.fun = function(evt) {
			MAP.map.setCenter(ll);
			MAP.map.pan(100, 100);
			TRIP.showMedium(index);
			Event.stop(evt);
		};

		marker.events.register('mousedown', marker, this.fun);
		MAP.overlays['markers'].addMarker(marker);
	},

	showMedium: function(index) {
		// alert('medium');
		var medium = TRIP.curTrip.media[index];
		var x = medium.getAttribute('x');
		var y = medium.getAttribute('y');
		var id = medium.getAttribute('id');
		var kind = medium.getAttribute('kind');
		var name = medium.getAttribute('name');
		var mediumURL = DIWIAPP.PORTAL + '/media.srv?id=' + id;

		var html = 'onbekend media formaat';
		if (kind == 'image') {
			html = '<a href="' + mediumURL + '" target="_new" ><img src="' + mediumURL + '&resize=240" /></a><br/>' + name;
		} else if (kind == 'video') {
			//html = '<a href="' + mediumURL + '?id=' + id + '" target="_new" >view video: ' + name + '</a>';
			mediumURL += '&format=swf';
			html = '<object classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0" align="center"><param name="movie" value="' + mediumURL + '"><param name="quality" value="high"><param name="bgcolor" value="#ffffff"><param name="loop" value="true"><embed src="' + mediumURL + '" quality="high" bgcolor="#ffffff" swliveconnect="true" loop="true" type="application/x-shockwave-flash"pluginspage="http://www.macromedia.com/shockwave/download/index.cgi?p1_prod_version=shockwaveflash"></embed></object><br/>' + name;

		} else if (kind == 'text') {
			html = name + '<br/><i>'+ DH.getURL(mediumURL) + '</i>';
		}
		var mediumPopup = new OpenLayers.Popup('mpopup' + id,
				new OpenLayers.LonLat(x, y),
				new OpenLayers.Size(280, 240),
				html,
				true);
		MAP.map.addPopup(mediumPopup, true);

	},

	showTripPoints: function() {
		var x,y,img,w,h,pt,xsw = 0,ysw = 0,xne = 0,yne = 0;
		img = 'media/images/icon-trace.png';
		w = 10;
		h = 10;

		for (var i = 0; i < TRIP.curTrip.points.length; i++) {
			pt = TRIP.curTrip.points[i];
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

			MAP.addMarker(x, y, img, w, h);
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
		DIWIAPP.pr('Tocht ophalen...');
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
