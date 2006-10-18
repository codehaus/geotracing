// JavaScript Document
var Interface = {
	search_bt: document.getElementById('search_bt'),
	tagcloud: document.getElementById('tagcloud'),
	tags:null,
	tag_block: document.getElementById('tag_block'),
	tagblock_title:document.getElementById('tagblock_title'),
	toggleVisibility:function(element) {
		var element = document.getElementById(element);
		if(element.style.display == 'block') {
			element.style.display = 'none';						   
		} else {
			element.style.display = 'block';	
		}
	}
	
}

if(Interface.search_bt) {
	dojo.event.connect(Interface.search_bt,'onclick', function(evt) {
		Interface.toggleVisibility('searchbox');									  
	});
}

if(Interface.tagcloud) {		
Interface.tags = document.getElementById('tagcloud').getElementsByTagName('li');
	for(var i = 0; i < Interface.tags.length; i++) {
	
		dojo.event.connect(Interface.tags[i],'onclick',function(evt) {
			Interface.tag_block.style.left = dojo.style.totalOffsetLeft(evt.target) +'px';
			Interface.tagblock_title.innerHTML = "tours tagged " +evt.target.firstChild.nodeValue +":"; 
			Interface.tag_block.style.display = 'block';
		});
	}

}
dojo.event.connect('body','onunload','GUnload');

	var mainbuttonsoriginimage = document.getElementById('mainbuttons').src;
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
	GTAPP.mSetMap('streets');
});
dojo.event.connect(document.getElementById('mapbox').getElementsByTagName('li')[1],'onclick', function(evt) {
	GTAPP.mSetMap('satellite');
});
dojo.event.connect(document.getElementById('mapbox').getElementsByTagName('li')[2],'onclick', function(evt) {
	GTAPP.mSetMap('hybrid');
});

	dojo.event.connect(document.getElementById('livenavbox').getElementsByTagName('li')[0],'onclick',function(evt) {
		GTAPP.mLive();
	});	
	dojo.event.connect(document.getElementById('livenavbox').getElementsByTagName('li')[1],'onclick',function(evt) {
		GTAPP.mLive();
	});
	dojo.event.connect(document.getElementById('livenavbox').getElementsByTagName('li')[2],'onclick',function(evt) {
		GTAPP.mLive();
	});