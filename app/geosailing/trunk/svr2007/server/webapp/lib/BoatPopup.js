// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Boat popup
 *
 * $Id$
 */
BOAT = {
	panel: null,
	boatName: null,
	panelOpen: false,
	initContent: null,
	mediaPlayer: null,
	mediaSet: new FeatureSet(),

	isCommentPanelOpen: function() {
		return BOAT.panelOpen;
	},

	onMove: function(tracer) {
		// User sends location, only update when
		if (tracer.name != BOAT.boatName) {
			return;
		}
		BOAT.showLocationInfo(tracer);
	},

	onPanelClose: function() {
		BOAT.panelOpen = false;
		BOAT.panel.hide();
		BOAT.boatName = null;
	},

	onShowMedia: function() {
		DH.getStyleObject('boatpopupinfo').display = 'none';
		DH.getStyleObject('boatpopupmedia').display = 'block';
		var records = SRV.get("q-media-by-user", null, "user", BOAT.boatName);
		BOAT.mediaSet.dispose();
		BOAT.mediaSet.addMedia(records);
		for (var i = 0; i < BOAT.mediaSet.features.length; i++) {
			if (BOAT.mediaSet.features[i]) {
				BOAT.mediaSet.features[i].userName = BOAT.boatName;
			}
		}
		BOAT.mediaSet.show();
		BOAT.mediaPlayer.setFeatureSet(BOAT.mediaSet);
		if (records.length > 0) {
			var tracer = GTW.getTracer(BOAT.boatName);
			if (tracer.activeTrack != null) {
				tracer.activeTrack.featureSet.dispose();
			}
			BOAT.mediaSet.displayFirst();
		}
		DH.setHTML("mediacount", '(' + records.length + ')');		
	},

	onShowInfo: function() {
		DH.getStyleObject('boatpopupmedia').display = 'none';
		DH.getStyleObject('boatpopupinfo').display = 'block';
		BOAT.mediaSet.dispose();
		DH.setHTML("tracerid", BOAT.boatName);

		var tracer = GTW.getTracer(BOAT.boatName);
		var record = tracer.record;
		tracer.thumbId = record.getField("thumbid");
		if (tracer.thumbId != null) {
			tracer.thumbURL = 'media.srv?id=' + tracer.thumbId + "&resize=80x60!";
		}
		DH.getObject('tracerimg').src = tracer.thumbURL;

		var slogan = record.getField("slogan");
		if (slogan != null) {
			DH.setHTML("slogan", slogan);
		}

		var regnr = record.getField("regnr");
		if (regnr != null) {
			DH.setHTML("regnr", regnr);
		}

		var thuishaven = record.getField("thuishaven");
		if (thuishaven != null) {
			DH.setHTML("thuishaven", thuishaven);
		}

		var firstName = record.getField("firstname");
		if (firstName == null) {
			firstName = '(voornaam)';
		}
		var lastName = record.getField("lastname");
		if (lastName == null) {
			lastName = '(achternaam)';
		}

		DH.setHTML("schipper", firstName + ' ' + lastName);

		var crewlid1 = record.getField("crewlid1");
		if (crewlid1 != null) {
			DH.setHTML("crewlid1", crewlid1);
		}

		var crewlid2 = record.getField("crewlid2");
		if (crewlid2 != null) {
			DH.setHTML("crewlid2", crewlid2);
		}

		var fietser = record.getField("fietser");
		if (fietser != null) {
			DH.setHTML("fietser", fietser);
		}

		var hardloper = record.getField("hardloper");
		if (hardloper != null) {
			DH.setHTML("hardloper", hardloper);
		}

		var url = record.getField("url");
		if (url != null) {
			DH.setHTML("url", '<a target="_blanc" href="http://' + url + '" >' + url + '</a>');
		}

		BOAT.showLocationInfo(tracer);
	},


	show: function(aBoatName) {
		if (BOAT.panel == null) {
			BOAT.panel = new Panel(aBoatName, '#072855', 'white', null, BOAT.onPanelClose);
			BOAT.panel.setXY(200, 100);
			BOAT.panel.setDimension(400, 400);
			BOAT.initContent = DH.getURL('popup/boot.html');
		}

		// Save target id
		BOAT.boatName = aBoatName;
		BOAT.panel.setTitle(aBoatName);
		BOAT.panel.setContent(BOAT.initContent);
		BOAT.mediaPlayer = new FeaturePlayer();
		BOAT.panel.show();
		//		BOAT.panel.close = BOAT.onPanelClose;
		BOAT.panelOpen = true;
		BOAT.mediaSet.dispose();

		var tracer = GTW.getTracer(BOAT.boatName);
		if (tracer.record == null) {
			var records = SRV.get("q-user-info", null, "user", BOAT.boatName);
			tracer.record = records[0];
		}

		BOAT.onShowInfo();
		return false;
	},

	showLocationInfo: function(tracer) {
		var location = tracer.getLocation();
		if (!location || location == null) {
			if (tracer.record != null && tracer.record.getField("lon") != null) {
				location = new GLatLng(tracer.record.getField("lat"), tracer.record.getField("lon"));
				location.time = new Number(tracer.record.getField("lonlattime"));
			}
		}

		if (!location || location == null) {
			return;
		}

		DH.setHTML("tracerloc", location.lng() + ', ' + location.lat());
		DH.setHTML("tracerlocdate", '(niet bekend)');
		if (location.time) {
			var lonLatTime = new Number(location.time);
			var date = new Date(lonLatTime);
			DH.setHTML("tracerlocdate", date.format("DDD D MMM YYYY HH:mm:ss"));
		}
	}
}

