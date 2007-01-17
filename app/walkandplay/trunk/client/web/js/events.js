dojo.event.connect('body','onunload','GUnload');


/*
ELEMENTS
*/
if(document.getElementById('mainbuttons')) {
	var mainbuttonsoriginimage = document.getElementById('mainbuttons').src;
	}
var search_bt = document.getElementById('search_bt');
if(document.getElementById('tagcloud')) {
	var tagcloud = document.getElementById('tagcloud');
	var tags = document.getElementById('tagcloud').getElementsByTagName('li');	
}
var tag_block = document.getElementById('tag_block');
var tagblock_title = document.getElementById('tagblock_title');
var maps_bt = document.getElementById('maps_bt');	
var archive_bt = document.getElementById('archive_bt');
var live_bt = document.getElementById('live_bt');
var signin_bt = document.getElementById('signin_bt');
var help_bt = document.getElementById('help_bt');
var archivetoursbox = document.getElementById('archivetoursbox');
var archivetracksbox = document.getElementById('archivetracksbox');
var submit_signin = document.getElementById('submit_signin');
var loginbox = document.getElementById('loginbox');
var search_bt = document.getElementById('search_bt');
/*
FUNCTIONS
*/

function toggle(elm) {
	if(document.getElementById(elm)) {
		elm = document.getElementById(elm);
		if(elm.style.display == 'block') {
			elm.style.display = 'none';
		} else {
			elm.style.display = 'block';
	}
}
}

function hide(elm) {
	document.getElementById(elm).style.display = 'none';	
}
function show(elm) {
	document.getElementById(elm).style.display = 'block';	
}

/*
EVENTS
*/

if(tags) {	
	for(var i = 0; i < tags.length; i++) {
			dojo.event.connect(tags[i],'onclick',function(evt) {
				tag_block.style.left = dojo.style.totalOffsetLeft(evt.target) +'px';
				tagblock_title.innerHTML = "tours tagged " +evt.target.firstChild.nodeValue +":"; 
				tag_block.style.display = 'block';
			});
		}
}
if(document.getElementById('mainbuttonscreate')) {
	dojo.event.connect(document.getElementById('mainbuttonscreate'),'onmouseover',function(evt) {
		document.getElementById('mainbuttons').src = 'img/cpv_c_over.gif';
	});
}
if(document.getElementById('mainbuttonsplay')) {
	dojo.event.connect(document.getElementById('mainbuttonsplay'),'onmouseover',function(evt) {
		document.getElementById('mainbuttons').src = 'img/cpv_p_over.gif';
	});
}
if(document.getElementById('mainbuttonsview')) {
	dojo.event.connect(document.getElementById('mainbuttonsview'),'onmouseover',function(evt) {
		document.getElementById('mainbuttons').src = 'img/cpv_v_over.gif';
	});
}
if(document.getElementById('mainbuttonscreate')) {
	dojo.event.connect(document.getElementById('mainbuttonscreate'),'onmouseout',function(evt) {
		document.getElementById('mainbuttons').src = mainbuttonsoriginimage;
	});
}
if(document.getElementById('mainbuttonsplay')) {
	dojo.event.connect(document.getElementById('mainbuttonsplay'),'onmouseout',function(evt) {
		document.getElementById('mainbuttons').src = mainbuttonsoriginimage;
	});
}
if(document.getElementById('mainbuttonsview')) {
	dojo.event.connect(document.getElementById('mainbuttonsview'),'onmouseout',function(evt) {
		document.getElementById('mainbuttons').src = mainbuttonsoriginimage;
	});
}
if(document.getElementById('mapbox')) {
	dojo.event.connect(document.getElementById('mapbox').getElementsByTagName('li')[0],'onclick', function(evt) {
		WP.mSetMap('streets');
	});
	dojo.event.connect(document.getElementById('mapbox').getElementsByTagName('li')[1],'onclick', function(evt) {
		WP.mSetMap('satellite');
	});
	dojo.event.connect(document.getElementById('mapbox').getElementsByTagName('li')[2],'onclick', function(evt) {
		WP.mSetMap('hybrid');
	});
}
if(document.getElementById('livenavbox')) {
	dojo.event.connect(document.getElementById('livenavbox').getElementsByTagName('li')[0],'onclick',function(evt) {
		WP.mLive();
	});	
	dojo.event.connect(document.getElementById('livenavbox').getElementsByTagName('li')[1],'onclick',function(evt) {
		WP.mLive();
	});
	dojo.event.connect(document.getElementById('livenavbox').getElementsByTagName('li')[2],'onclick',function(evt) {
		WP.mLive();
	});
}
if(document.getElementById('archivenavbox')) {
	dojo.event.connect(document.getElementById('archivenavbox').getElementsByTagName('li')[0],'onclick',function(evt) {
		WP.mLastTracks(10);
		show('archivetoursbox');
	});	
	dojo.event.connect(document.getElementById('archivenavbox').getElementsByTagName('li')[1],'onclick',function(evt) {
		show('archivetoursbox');																												 
		WP.mArchive();
		
	});
	dojo.event.connect(document.getElementById('archivenavbox').getElementsByTagName('li')[2],'onclick',function(evt) {
		WP.mShowPOIsInBbox();
	});
}
if(search_bt) {
	dojo.event.connect(search_bt,'onclick',function(evt) {
		toggle('searchbox');
		hide('mapbox');
		hide('archivenavbox');
		hide('livenavbox');
		hide('archivetoursbox');
		hide('archivetracksbox');	
	
	});
}
if(maps_bt) {
	dojo.event.connect(maps_bt,'onclick',function(evt) {
		toggle('mapbox');
		hide('searchbox');
		hide('archivenavbox');
		hide('livenavbox');	
		hide('archivetoursbox');
		hide('archivetracksbox');
		
	});
}
if(live_bt) {
	dojo.event.connect(live_bt,'onclick',function(evt) {
		toggle('livenavbox');
		hide('mapbox');
		hide('searchbox');
		hide('archivenavbox');	
		hide('archivetoursbox');
		hide('archivetracksbox');	
	});
}
if(archive_bt) {
	dojo.event.connect(archive_bt,'onclick',function(evt) {
		toggle('archivenavbox');	
		hide('mapbox');
		hide('livenavbox');
		hide('searchbox');	
		hide('archivetoursbox');
		hide('archivetracksbox');	
	});
}
if(signin_bt) {
	signin_bt.getElementsByTagName('a')[0].href = "javascript:toggleSignin()";
	}
	
function toggleSignin() {
		toggle('loginbox');												
		hide('archivenavbox');	
		hide('mapbox');
		hide('livenavbox');
		hide('searchbox');	
		hide('archivetoursbox');
		hide('archivetracksbox');	
}
if(help_bt) {	
dojo.event.connect(help_bt,'onclick',function(evt) {
											  
});
}

if(submit_signin) {
	dojo.event.connect(submit_signin,'onclick',function(evt) {
		User.role = 'user';													
		User.signin();													
	});
}

function WPCallback(element) {
		switch(element.nodeName)
		{
	//LOGIN 		
			case 'login-rsp':
				KW.selectApp('geoapp',User.role);
			break;	
			case 'select-app-rsp':
			if(User.role == 'guest') {
				var doc = KW.createRequest('license-getlist-req');
				var xml = doc.documentElement;
			} else if(User.role == 'user') {
				loginbox.style.display = 'none';	
			}
			//	KW.utopia(doc);
			break;
			case 'profile-create-rsp':
				window.location = "congratulations.html";
			break;
			case 'license-getlist-rsp':
				licenses = element.getElementsByTagName('record');
				var option;
				var sel = document.getElementById('licenses');
				for(var i = 0; i <licenses.length; i++) {
					option = document.createElement('option');
					option.innerHTML = licenses[i].getElementsByTagName('type')[0].firstChild.nodeValue;
					sel.appendChild(option);
					
				}			
			break;	
		}		
}
function WPNegResp(elm) {

}

KW.init(WPCallback, WPNegResp, 60,'/wp');
