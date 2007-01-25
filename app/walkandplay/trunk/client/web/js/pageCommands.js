var qsParm = new Array();
dojo.event.connect(window,'onload', function(evt) {									 
		qs();
		if(qsParm['cmd'].length == 0) {
				PL.joinListen('/');
		} else {
			if(qsParm['cmd'] == 'get-track') {
				SRV.get('get-track', WP.onTrackSelect, 'id', qsParm['id']);
			}
		}
	});
function qs() {
	var query = window.location.search.substring(1);
	var parms = query.split('&');
	for (var i=0; i<parms.length; i++) {
		var pos = parms[i].indexOf('=');
			if (pos > 0) {
				var key = parms[i].substring(0,pos);
				var val = parms[i].substring(pos+1);
				qsParm[key] = val;
			}
		}
	}	 