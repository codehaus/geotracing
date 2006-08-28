
var APP = {
	outputPanel: null,
	loginPanel: null,

	init: function() {
		DH.init();
		DH.debug(false, 'baseDir', DH.getBaseDir());

		APP.outputPanel = new Panel('output', '#ff0000', '#ffffff');
		APP.outputPanel.setXY(200, 100);
		APP.outputPanel.setDimension(300, 100);
		APP.outputPanel.setContent('waiting for GPS...');

		APP.loginPanel = new Panel('session', '#0000ff', '#ffffff');
		APP.loginPanel.setXY(200, 240);
		APP.loginPanel.setDimension(300, 100);
		APP.loginPanel.setContent('waiting for GPS...');

		GPS.start(5000);
		setInterval('APP.checkGPS()', 7000);

		KW.init(APP.onRsp, APP.onNegRsp);
		KW.login('geotracing', 'just', 'skee1er');
	},

	checkGPS: function() {
		var msg = 'no GPS';
		if (GPS.set == true) {
			msg = 'lon,lat=' + GPS.lon + ',' + GPS.lat;
			var trkWriteReq = KW.createRequest('t-trk-write-req');
			var pt = trkWriteReq.createElement('pt');
			pt.setAttribute('lon', GPS.lon);
			pt.setAttribute('lat', GPS.lat);
			trkWriteReq.documentElement.appendChild(pt);
			KW.utopia(trkWriteReq);
			GPS.set = false;
		}
		APP.outputPanel.setContent(msg);
		// alert('lon=' + APP.lon + ' lat=' + APP.lat);
	},

	onRsp: function(elm) {
		// APP.outputPanel.setContent('status: ' + mh_status + '<br>lon, lat=' + mh_lon + ',' + mh_lat);
		// APP.outputPanel.loadContent('http://localhost:7305/js');
		APP.outputPanel.setContent('rsp:' + elm.tagName);
		if (elm.tagName == 'login-rsp') {
			KW.selectApp('geoapp', 'user');
		}
		// alert('onSelect name=' + name + ' value=' + value + ' label=' + label);
	},

	onNegRsp: function(errorId, error, details) {
		// APP.outputPanel.setContent('status: ' + mh_status + '<br>lon, lat=' + mh_lon + ',' + mh_lat);
		// APP.outputPanel.loadContent('http://localhost:7305/js');
		APP.outputPanel.setContent('negrsp:' + error + ' details=' + details);
		// alert('onSelect name=' + name + ' value=' + value + ' label=' + label);
	}
}

DH.onReady = APP.init;
DH.include('Panel.js');
DH.include('KWClient.js');
DH.include('Selector.js');
DH.include('GPS.js');

