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
dojo.event.connect(document.getElementById('mainbuttonscreate'),'onmouseover',function(evt) {
	document.getElementById('mainbuttons').src = 'img/cpv_c_over.gif';
});
dojo.event.connect(document.getElementById('mainbuttonsplay'),'onmouseover',function(evt) {
	document.getElementById('mainbuttons').src = 'img/cpv_p_over.gif';
});
dojo.event.connect(document.getElementById('mainbuttonsview'),'onmouseover',function(evt) {
	document.getElementById('mainbuttons').src = 'img/cpv_v_over.gif';
});
dojo.event.connect(document.getElementById('mainbuttonscreate'),'onmouseout',function(evt) {
	document.getElementById('mainbuttons').src = mainbuttonsoriginimage;
});
dojo.event.connect(document.getElementById('mainbuttonsplay'),'onmouseout',function(evt) {
	document.getElementById('mainbuttons').src = mainbuttonsoriginimage;
});
dojo.event.connect(document.getElementById('mainbuttonsview'),'onmouseout',function(evt) {
	document.getElementById('mainbuttons').src = mainbuttonsoriginimage;
});			
dojo.event.connect(document.getElementById('mapbox').getElementsByTagName('li')[0],'onclick', function(evt) {
	WP.mSetMap('streets');
});
dojo.event.connect(document.getElementById('mapbox').getElementsByTagName('li')[1],'onclick', function(evt) {
	WP.mSetMap('satellite');
});
dojo.event.connect(document.getElementById('mapbox').getElementsByTagName('li')[2],'onclick', function(evt) {
	WP.mSetMap('hybrid');
});
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
		WP.mLastTracks(100);
	});	
	dojo.event.connect(document.getElementById('archivenavbox').getElementsByTagName('li')[1],'onclick',function(evt) {
		WP.mAutoPlay();
	});
	dojo.event.connect(document.getElementById('archivenavbox').getElementsByTagName('li')[2],'onclick',function(evt) {
		WP.mAutoPlay();
	});
}
dojo.event.connect(search_bt,'onclick',function(evt) {
	toggle('searchbox');
	hide('mapbox');
	hide('archivenavbox');
	hide('livenavbox');
});
dojo.event.connect(maps_bt,'onclick',function(evt) {
	toggle('mapbox');
	hide('searchbox');
	hide('archivenavbox');
	hide('livenavbox');	
});
dojo.event.connect(live_bt,'onclick',function(evt) {
	toggle('livenavbox');
	hide('mapbox');
	hide('searchbox');
	hide('archivenavbox');	
});
dojo.event.connect(archive_bt,'onclick',function(evt) {
	toggle('archivenavbox');	
	hide('mapbox');
	hide('livenavbox');
	hide('searchbox');	
});
dojo.event.connect(signin_bt,'onclick',function(evt) {
	toggle('loginbox');												
	hide('archivenavbox');	
	hide('mapbox');
	hide('livenavbox');
	hide('searchbox');	
});
dojo.event.connect(help_bt,'onclick',function(evt) {
											  
});
