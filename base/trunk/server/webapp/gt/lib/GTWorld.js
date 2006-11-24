// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * The world of Tracers, Tracks and Features.
 *
 * $Id$
 */


var GTW = {
// Array of N available colors, each in triples: N+0 (avail) N+1 (name) N+2 (colorcode)
	/* colors: [0, 'blue', '#FF9933', '#000000', 0, 'yellow', '#FFFF00', '#222222', 0, 'green','#00EE00', '#222222', 0,'red','#CC0000', '#ffffff', 0, 'blue','#0066FF', '#ffffff', 0,'purple','#FF66FF', '#ffffff'],
	randomColors: ['#FFCCFF',  '#88AACC',
			'#DDDDDD',  '#FFFFCC',  '#FFCCCC',  '#FF99CC', '#FF66CC',
			'#FF33CC',  '#FF00CC',  '#FFFF99',  '#FFCC99', '#FF9999',
			'#FF6699',  '#FF3399',  '#99FFFF',  '#99CCFF',
			'#99FF00',  '#99CC00',  '#6FFFF',  '#66CCFF', '#00FFFF',
			'#00CCFF', '#00FF66',  '#00CC66', '#00EE00', '#CC3333', '#00CCFF', '#666666' ],  */
	TRACK_COLOR: '#ff00cc',
	TRACER_ICON_URL: 'img/blueball.gif',
	FEATURE_BG_COLOR: '#ff0000',
	FEATURE_FG_COLOR: '#ffffff',

	tracers: [],
	followTracer: null,
	featureSet:null,
	trackPlayer: null,
	featurePlayer: null,
	trackAutoPlayer: null,
	imageFullPanel: null,
	polyLineWidth: 3,
	polyLineOpacity: 0.80,
// Minimal distance between trackpoints (when reading tracks from server)
	minTrackPtDist: 15,
	factory: null,

	boot: function() {
		// Setup object creation rules
		// specific apps may replace these with their own classes.
		GTW.factory = new Factory();
		GTW.factory.setClassDef('Tracer', 'Tracer');
		GTW.factory.setClassDef('Medium', 'Medium');
	},

	getFactory: function() {
		return GTW.factory;
	},

	init: function() {
		GTW.featureSet = new FeatureSet();
		GTW.clearPanels();
	},

	clearFeatures: function() {
		GTW.featureSet.dispose();
	},

	clearMap: function() {
		GTW.clearTracers();
		GTW.clearFeatures();
		GTW.stopAutoPlay();
		GTW.clearPanels();
	},

	clearFeaturePlayer: function() {
		if (GTW.featurePlayer != null) {
			GTW.featurePlayer.hide();
		}
	},

	clearPanels: function() {
		GTW.clearFeaturePlayer();
		GTW.clearTrackPlayer();
		DH.setHTML('tracerinfo', ' ');
		DH.setHTML('trackinfo', ' ');
		DH.setHTML('trackview', 'track info');
		DH.setHTML('featuretitle', ' ');
		DH.setHTML('featureinfo', ' ');
		DH.setHTML('featurepreview', 'feature preview');
		DH.setHTML('featuredesc', 'feature description (if avail)');
	},

	clearTrackPlayer: function() {
		if (GTW.trackPlayer != null) {
			GTW.trackPlayer.hide();
		}
	},

// Delete all tracers from view
	clearTracers: function() {
		for (tracer in GTW.tracers) {
			var t = GTW.getTracer(tracer);
			t.clear();
			t.hide();
		}
	},

/** Create new Medium object. */
	createMedium: function(id, name, desc, type, mime, time, lon, lat) {
		var medium = GTW.factory.create('Medium', id, name, desc, type, mime, time, lon, lat);
		// medium.init();

		return medium; // new Medium(id, name, desc, type, mime, time, lon, lat);
	},

/** Create new POI object. */
	createPOI: function(id, name, desc, type, time, lon, lat) {
		// Create POI
		return new POI(id, name, desc, type, time, lon, lat);
	},

// Create new Tracer object and put in tracers array
	createTracer: function(name, lon, lat, time) {
		// See if tracer already exists
		var tracer = GTW.getTracer(name);
		if (tracer) {
			if (lon && lat) {
				tracer.setLocation(new GLatLng(lat, lon), time);
			}
			// tracer.show();
			return tracer;
		}


		var point;
		if (lon && lat) {
			point = new GLatLng(lat, lon);
		}

		tracer = GTW.factory.create('Tracer', name, GTW.TRACK_COLOR, GTW.TRACER_ICON_URL, point, time);
		tracer.init();
		GTW.tracers[name] = tracer;
		return tracer;
	},

// Create new Tracer object and put in tracers array
	createTracerByRecord: function(record) {
		var tracer = GTW.createTracer(record.getField('loginname'),
				record.getField('lon'), record.getField('lat'), record.getField('time'));

		// tracer.record = record;
		return tracer;
	},

/** Display media. */
	displayMedia: function(records) {
		GTW.featureSet.dispose();
		GTW.featureSet.addMedia(records);
		GTW.featureSet.show();
		GTW.getFeaturePlayer().setFeatureSet(GTW.featureSet);
		GTW.getFeaturePlayer().show()
		GTW.featureSet.displayFirst();
	},

// Display POIs
	displayPOIs: function(records) {
		GTW.featureSet.dispose();
		GTW.featureSet.addPOIs(records);
		GTW.getFeaturePlayer().setFeatureSet(GTW.featureSet);
		GTW.featureSet.show();
		GTW.getFeaturePlayer().show()
		GTW.featureSet.displayFirst();
	},

// Display media
	displayTrackPlayer: function() {
		GTW.getTrackPlayer().show();
	},

	formatDate: function(time) {
		var date = new Date(time);
		return date.format("DD-MM-YY");
	},

	formatTime: function(time) {
		var date = new Date(time);
		return date.format("HH:mm:ss");
	},

	formatDateAndTime: function(time) {
		try {
			var date = new Date(time);
			return date.format("DD-MM-YY HH:mm:ss");
		} catch(e) {
			return 'date format error: t=' + time + ' e=' + e;
		}
	},

	getImageFullPanel: function() {
		if (GTW.imageFullPanel == null) {
			GTW.imageFullPanel = new Panel('imagefullpanel', 'red', 'white');
			GTW.imageFullPanel.setDimension(640, 510);
			GTW.imageFullPanel.setXY(50, 50);
		}
		GTW.imageFullPanel.show();
		return GTW.imageFullPanel;
	},



// Get tracer by name
	getTracer: function(name) {
		return GTW.tracers[name];
	},

/** Lazy creation of TrackPlayer */
	getTrackPlayer: function() {
		if (GTW.trackPlayer == null) {
			GTW.trackPlayer = new TrackPlayer();
		}

		return GTW.trackPlayer;
	},

/** Lazy creation of FeaturePlayer */
	getFeaturePlayer: function() {
		if (GTW.featurePlayer == null) {
			GTW.featurePlayer = new FeaturePlayer();
		}

		return GTW.featurePlayer;
	},

// Get tracers array
	getTracers: function() {
		return GTW.tracers;
	},

	startAutoPlay: function() {
		GTW.stopAutoPlay();
		GTW.trackAutoPlayer = new TrackAutoPlayer();
		GTW.trackAutoPlayer.start();
	},

	stopAutoPlay: function() {
		if (GTW.trackAutoPlayer != null) {
			GTW.trackAutoPlayer.stop();
			GTW.trackAutoPlayer = null;
		}
	},

	showStatus: function(msg) {
		DH.setHTML('status', msg);
	}
}



