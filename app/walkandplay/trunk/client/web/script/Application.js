/* usemedia.com . joes koppers . 04.2007 */
/* thnx for reading this code */


//the wp (general) application

wp_autologin = false;
wp_set_autologin = false;
wp_login = new Object();

function wpStartup()
{
	/* create and enable gui  */
	
 	wpCreatePane('main');
 	wpCreatePane('login');
 	
 	
 	wp_locations = new wpLocations();
}


function wpGetCurrentPixelperDegree()
{
	//current boundingbox
 	ii_current_bounds = gmap.getBounds();
// 	var sw = b.getSouthWest();
// 	var ne = b.getNorthEast();
	//var c = b.getCenter();

	var d = ii_current_bounds.toSpan();
	
	//pixel per degree latitude (global variable)
	var px = gmap.getSize().height;
	ii_current_ppd_lat = px/d.lat();

	//pixel per degree longitude (global variable)
	var px = gmap.getSize().width;
	ii_current_ppd_lng = px/d.lng();
	
	return d; //for further calculations	
}


/* centralized map events handling */

function wpMapClick(m,p)
{
	tmp_debug(3,'single click on map: ',p);

	//add location (taks/medium) to game
	if (wp_add_location && wp_add_location.enabled)
	{
		//disable tooltip
 		wp_add_location.enable(0);
		document.onmousemove = null;

		//add tmp location and edit
		wp_games.game[wp_game_selected].editLocation(p);
	}
}

function wpMapMoveend()
{
	tmp_debug(3,'moveend');
	
	//new bounds and ppd
//	iiGetCurrentPixelperDegree();
	
	//media
//	iiGetMedia();

//	if (ii_media_expanded) ii_media_hideTimeout = 300; //shorten detailoverlay hide timeout after map panning

	//locations
	wp_locations.update();

	if (wp_game_selected) wp_games.game[wp_game_selected].locations.update();
}

function wpMapZoomend(b,e)
{
	tmp_debug(3,'zoomend');
	
	//position editLocation pane
	if (wp_locations.location['new'])
	{
		var px = gmap.fromLatLngToDivPixel(wp_locations.location['new'].geo);
		panes['edit_location'].setPosition(px.x,px.y);
	}

}

function wpWindowResize()
{
	//->update map div height
	//..

	//keep status & live users display centered
// 	if (ii_panes['status']) ii_panes['status'].div.style.left = (gmap.getSize().width/2)-205 +'px';
// 	if (ii_panes['users']) ii_panes['users'].div.style.left = (gmap.getSize().width/2)+140 +'px';
}





/* login functions */

function wpAutoLogin()
{
	var autologin = readCookie('autologin');
	if (autologin && autologin!='false')
	{
		var data = encrypt(autologin,true).split(',');
		wp_login.loginname = data[0];
		wp_login.password = data[1];
		wp_autologin = true;
		wpDoLogin(true);
		return true;
	}
}

function wpLogin()
{
	panes['login'].show();
	document.forms['loginform'].login.focus();
}

function wpToggleAutoLogin()
{
	wp_set_autologin = !wp_set_autologin;
	//toggle checkbox
	document.forms['loginform'].auto.checked = wp_set_autologin;
}

function wpSetAutoLogin(set)
{
	if (!set) wp_autologin = false;
	//set cookie
	var days = (!set)? -1:14; //cookie expires 2 weeks from now
	var value = (!set)? false:encrypt(wp_login.loginname+','+wp_login.password);
	
	var d = new Date();
	d.setTime(d.getTime()+(days*24*60*60*1000));
	var expires = '; expires='+d.toGMTString();
	document.cookie = 'autologin='+value+expires+'; path=/';
}

function wpDoLogin(auto)
{
	//enable KW client interface if not done already
	if (!wp_KW_inited)
	{
		KW.init(wpProcessLogin,function(errorId,error,details) { wpProcessLogin(false,errorId,error,details) },5,DH.getBaseDir()+'/..');
		wp_KW_inited = true;
	}
		
	if (!auto)
	{
		wp_login.loginname = document.forms['loginform'].login.value;
		wp_login.password = document.forms['loginform'].password.value;
	}
	
	if (wp_login.loginname=='' || wp_login.password=='') 
	{
		//warning
		alert('please enter both username and password');
		return false;
	}

	document.forms['loginform'].login.blur();
	document.forms['loginform'].password.blur();

	//send login request
	KW.login(wp_login.loginname,wp_login.password);
	
	return false; //prevent form from submitting
}

function wpDologout()
{
	wpLoggedOut();
	KW.logout();
}

function wpProcessLogin(resp,errorId,error,details)
{
	if (!error)
	{
		switch(resp.tagName)
		{
			case 'login-rsp':
				wp_login.id = resp.ownerDocument.getElementsByTagName('personid')[0].firstChild.nodeValue;
				//select-app to complete login
				KW.selectApp('geoapp','user');
				break;
				
			case 'select-app-rsp': //now we are logged in
				wpLoggedIn();
				break;
		}
	}
	else
	{
		if (errorId==4100) //bad login
		{
			document.getElementById('loginerror').innerHTML = 'invalid login !';
		}
		
		//do something useful with other errors here
		tmp_debug(3,'login negative resp:',errorId,':',error,' details=',details);
	}
}

function wpLoggedIn()
{
	if (!wp_login.id) return;
	
	/* now we are logged in */
	wp_loggedIn = true;

	tmp_querytime = tmp_debug(1,'login succes: user=',wp_login.loginname,',id=',wp_login.id);
	
	if (panes['login'].visible) panes['login'].hide(true);

	if (!wp_autologin && wp_set_autologin) wpSetAutoLogin(true);
	
	document.getElementById('login').innerHTML = '<a href="javascript://logout" onclick="wpDologout()" title="logged in as \''+wp_login.loginname+'\'">logout</a>';


	/* load registered user gui */

	wpLoadScript('GuiUser.js');
}

function wpLoadScript(src)
{
	tmp_querytime = tmp_debug(2,'load script init');

	var script = document.createElement('script');
	script.type = 'text/javascript';
	script.src = 'script/'+src+'?'+new Date().getTime(); //anti-cache timestamp
	document.getElementsByTagName('head')[0].appendChild(script);
}

function wpLoggedOut()
{
	//dispose games
	for (var id in wp_games.game) wp_games.del(id);
	
	
	//clear data
	document.forms['loginform'].login.value = wp_login.loginname = '';
	document.forms['loginform'].password.value = wp_login.password = '';
	document.getElementById('loginerror').innerHTML = '';

	//clear login obj
	wp_login = new Object();

	//clear cookie
	wpSetAutoLogin(false);
	
	//unload stuff
	panes.dispose('list_games','edit_game','edit_location');

 	document.getElementById('login').innerHTML = '<a href="javascript://login" onclick="wpLogin()">login</a>';
	
}





















/* public functions */

function iiMe(action)
{
	switch (action)
	{
		//public functions
		case 'login': iiLogin(); break;
		case 'about': iiAbout('pane'); break;
	
		case 'options': iiMapOptions(); break;
		case 'zoomin': gmap.zoomIn(); break;
		case 'zoomout': if (gmap.getZoom()>2) gmap.zoomOut(); break;
		case 'satellite': gmap.setMapType(G_SATELLITE_TYPE); gmaptype='sat'; break;
		case 'map': gmap.setMapType(G_MAP_TYPE); gmaptype='map'; break;
		case 'hybrid': 
			if (gmaptype=='hybrid')
			{
				gmap.setMapType(G_SATELLITE_TYPE)
				gmaptype = 'sat';
			}
			else
			{
				gmap.setMapType(G_HYBRID_TYPE);
				gmaptype = 'hybrid';
			}
			break;
			
		//userpage functions
		case 'user_close': iiUserPage(); break;
		case 'user_profile': iiUserPagePane('profile'); break;
		case 'user_bliins': alert('coming soon..'); break;
		case 'user_shares': iiUserPagePane('shares'); break;

		//user functions
		case 'bliin':
			alert('sorry,\nbliins are not yet available');
			//iiCreateBliin(); 
			break;
		case 'edit': iiMy(); break;
		case 'me':
			if (ii_users.user[ii_login.loginname]) p = ii_users.user[ii_login.loginname].geo;
			else
			{
				//get home location later, or last known point
				p = ii_default_location; //-> CHANGE THIS!
			}
			gmap.panTo(p);
			break;
		case 'status': iiToggleVisibility(); break;
		case 'new': 
			if (ii_panes['my_select'].visible) iiMySelect('my_inbox');
			else iiMy('my_inbox');
			break;
	}
	
	
}


function iiMapOptions()
{
	iiCloseMetaPanes();
	if (ii_panes['login'].visible) ii_panes['login'].close();
	if (ii_panes['my_select'] && ii_panes['my_select'].visible) ii_panes['my_select'].close();

	
	//keep button pressed
	iiHi('me_options',1);
	
	if (ii_panes['options'].visible)
	{
		iiHi('me_options',0);
		ii_panes['options'].close();
	}
	else
	{
		//update zoomslider to current map zoom level & keep updating on zoomchange
		Slider.setValue(ii_panes['options'].zoomlevel,gmap.getZoom());
//		ii_panes['options'].zoom = GEvent.addListener(gmap,'zoomend',function(begin,end) { Slider.setValue(ii_panes['options'].zoomlevel,end) } );
		
		ii_panes['options'].fade('in');
		
		//extend class	
		ii_panes['options'].closeMore = function()
		{
			//reset button
			iiHi('me_options',0);
			//detach the zoomslider
//			GEvent.removeListener(ii_panes['options'].zoom);
		}
	}
}

function iiSetZoom(s)
{
	if (Math.round(s)==gmap.getZoom()) return; //no update required
	else gmap.setZoom(Math.round(s));
}

function iiToggleMediaAutosize()
{
	ii_media_autosize = !ii_media_autosize;
	
	document.getElementById('checkbox_media_autosize').src = (ii_media_autosize)? 'media/checkbox_wX.gif':'media/checkbox_w.gif';
	Slider.setEnabled(ii_panes['options'].mediasize,!ii_media_autosize);
	
	if (ii_media_autosize) iiSetMediasize(gmap.getZoom(),true);
}

function iiToggleMediaRandom()
{
	ii_media_random = !ii_media_random;
	document.getElementById('checkbox_media_random').src = (ii_media_random)? 'media/checkbox_wX.gif':'media/checkbox_w.gif';
	Slider.setEnabled(ii_panes['options'].age,!ii_media_random);

	//iiUpdateMedia();	
	iiFilterMedia(); //single query version
}

function iiToggleLiveTraces()
{
	ii_live_traces = !ii_live_traces;
	
	document.getElementById('ii_live_traces').src = (ii_live_traces)? 'media/checkboxX.gif':'media/checkbox.gif';

	//show or remove current traces
	for (var name in ii_users.user) ii_users.user[name].showTrace();
}

function iiToggleEffects()
{
	ii_disable_effects = !ii_disable_effects;
	document.getElementById('ii_disable_effects').src = (!ii_disable_effects)? 'media/checkboxX.gif':'media/checkbox.gif';

	//(re)start user glowing
	if (ii_users) ii_users.glow(1);
}

function iiToggleDisplay(close)
{
	if (!close) ii_panes['status'].open();
	document.getElementById('togglestatus').style.visibility = (close)? 'visible':'hidden';
}

function iiCenterUser(name)
{
	if (!ii_users.user[name]) return;
	
	ii_media_blockupdate = false;
	
	if (ii_media_expanded) ii_media_expanded.collapse();
	ii_users.user[name].center();
}

function iiGo(lat,lng,livemedia)
{
	if (livemedia)
	{
		//make sure most recent media is shown
		Slider.setValue(ii_panes['options'].age,100);
		//and disble random
		if (ii_media_random)
		{
			ii_media_random = false;
			document.getElementById('checkbox_media_random').src = 'media/checkbox_w.gif';
			Slider.setEnabled(ii_panes['options'].age,true);
		}
	}
	//center map
	var p = new GLatLng(lat,lng);
	gmap.setCenter(p);
}


function iiClock(update)
{
	if (!update)
	{
		ii_local_date = new Date().format('longhumandate');
		document.getElementById('live').title = 'local date: '+ii_local_date;
		if (!ii_pulsate) ii_pulsate = window.setInterval('iiClockPulse()',1000);
	}
	var now = new Date();
	var h = now.getHours();
	var m = now.getMinutes(); if (m<10) m = '0'+m;
	document.getElementById('live').innerHTML = ii_local_date.substring(0,3)+' &nbsp;'+h+'<span id="pulse" style="color:white;">:</span>'+m; 

	if (h=='23' && m=='59') window.setTimeout('iiClock()',60000); //change date on next update
	else window.setTimeout('iiClock(1)',60000);
}

function iiClockPulse()
{
	ii_pulse = !ii_pulse;
	document.getElementById('pulse').style.color = (ii_pulse)? '#ff9900':'white';
}

function iiGetCurrentLocalTime()
{
	var now = new Date();
	var h = now.getHours();
	var m = now.getMinutes(); if (m<10) m = '0'+m;
	var s = now.getSeconds(); if (s<10) s = '0'+s;

	return (h+':'+m+':'+s);
}
function iiGetCurrentLocalDate()
{
	var now = new Date();
	var d = now.getDate();
	var m = now.getMonth()+1;
	var y = now.getFullYear();

	return (d+'/'+m+'/'+y);
}




/* (public) meta panes */

function iiAbout(tab)
{
	iiCloseMetaPanes('about');
	
	if (!ii_panes['about'])
	{
		iiCreatePane('about');
		ii_panes['about'].open();
	}
	else
	{
		if (tab=='pane' && ii_panes['about'].visible)
		{
			//close the pane
			ii_panes['about'].close();
		}
		else
		{
			//show pane or select tab
			if (tab=='pane') ii_panes['about'].open();
			else 
			{
				ii_about_selectedtab = tab;
				ii_panes['about'].content.innerHTML = iiGuiCreate('about_'+tab);
				ii_panes['about'].addCloseButton();
			}
		}
	}
}

function iiInfo(p)
{
	//browse info pages
	var page = (p!=1)? '_'+p:'';
	ii_panes['about'].content.innerHTML = iiGuiCreate('about_info'+page);
	ii_panes['about'].addCloseButton();
}


function iiPromo(promo)
{
// 	iiCloseMetaPanes('promo');
// 
// 	if (!ii_panes['promo'])
// 	{
// 		//stop glowing users
// 		//ii_user.glow(false);
// 		
// 		iiCreatePane('promo');
// 		ii_panes['promo'].fade('in');
// 	}
// 	else
// 	{
// 		if (promo=='pane' && ii_panes['promo'].visible) ii_panes['promo'].close();
// 		else
// 		{
// 			if (promo=='pane') ii_panes['promo'].fade('in');
// 			else
// 			{
				var str = '';
				str+= '<OBJECT id="promoqt" CLASSID="clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B" WIDTH="536" HEIGHT="230" CODEBASE="http://www.apple.com/qtactivex/qtplugin.cab">';
				str+= '<PARAM name="SRC" VALUE="/download/promo/'+promo+'.mp4">';
				str+= '<PARAM name="CONTROLLER" VALUE="false">';
				str+= '<PARAM name="AUTOPLAY" VALUE="true">';
				str+= '<PARAM name="BGCOLOR" VALUE="white">';
				str+= '<PARAM name="CACHE" VALUE="true">';
				str+= '<EMBED name="promoqt" SRC="/download/promo/'+promo+'.mp4" BGCOLOR="white" WIDTH="536" HEIGHT="230" CONTROLLER="false" AUTOPLAY="true" CACHE="true" PLUGINSPAGE="http://www.apple.com/quicktime/download/">';
				str+= '</EMBED>';
				str+= '</OBJECT>';

				ii_panes['about'].content.childNodes[3].innerHTML = str;
// 			}
// 		}
// 	}
}
function iiPromoCredits(promo)
{
	//safari qt stop playing bug
	if (navigator.userAgent.indexOf("Safari")!=-1 && document.getElementById('promoqt')) document.getElementById('promoqt').Stop();

	var str = '';
	switch(promo)
	{
		case 'stanbul':
			str+= '<br>"stanbul"<br>KKEP in collaboration with Martine Stig<br><br>photography: Martine Stig<br>music: Istanbul\'da Sonbahar by Teoman<br>actors: Caglar and Asli<br>thanks: Juul and Dori<br><br>rights reserved 2006';
			break;
		case 'mrbrown':
			str+= '<br>"mr.brown"<br>KKEP in collaboration with Martine Stig<br><br>photography: Martine Stig<br>music: Ruldo by Murcof<br>kid: Bruin<br>thanks: Thirza and Braz<br><br>rights reserved 2006';
			break;
		case 'cruising':
			str+= '<br>"cruising"<br>KKEP in collaboration with Martine Stig<br><br>photography: Martine Stig<br>music: Sommersonneschein by Novisad<br>actor: Nami<br><br><br>rights reserved 2006';
			break;
	}
	ii_panes['about'].content.childNodes[3].innerHTML = str;
}

function iiSignup()
{
	//for now
	signupwin = window.open('http://www.kkep.com/caress/phpmailer/bliin_signup03.html','signup','width=380,height=320,scrollbars=0,status=0,resizable=0');
}

function iiCloseMetaPanes(pane)
{
	if (pane!='about' && ii_panes['about'] && ii_panes['about'].visible) ii_panes['about'].close();
	if (pane!='download' && ii_panes['download'] && ii_panes['download'].visible) ii_panes['download'].close();
	if (pane!='promo' && ii_panes['promo'] && ii_panes['promo'].visible)
	{
		//restart glowing users
		//ii_user.glow(true);
		ii_panes['promo'].close();
	}
}









/* KW server communication and live events */

function iiLiveInit()
{
	ii_live = true;
	//return; //tmp disable
	//register pushlet to all events
	PL.joinListen('/gt');
	onData = iiLive;
}

function iiLive(event)
{
/*	See EventPublisher.java for Event types/fields
	Event types and attributes:

	"user-hb" : id, username, time, [trackname]
	"user-move": id, username, t, trackid, trackname, lon, lat
	
	"medium-add": id, name, kind, mime, time, userid, username, trackid, trackname, lon, lat, ele
	
	"track-create": id, name, userid, username
	"track-delete": id, name, userid, username
	"track-suspend": id, name, userid, username
	"track-resume": id, name, userid, username
	
	"comment-add": id, target, ownerid (if not anonymous), ownername (if not anonymous) */

	
	var eventType = event.get('event');
	var id = event.get('id');
	var name = event.get('username');

	switch (eventType)
	{
		case 'user-hb': 	
			//add or update idle user
			var t = event.get('time');
			var timestamp = new Date().getTime();
			
			if (ii_users.user[name]) break; //a live user is not idle

			if (ii_users.idle[name])
			{
				ii_users.idle[name].time = t;
				ii_users.idle[name].timestamp = timestamp;
			}
			else
			{
				//new idle user
				ii_users.push('idle', { id:id, name:name, time:t, timestamp:timestamp } );
			}
			break;
		
		case 'user-move':
			//add or update live user

			var p = new GLatLng(event.get('lat'),event.get('lon'));
			var t = event.get('t');
			var timestamp = new Date().getTime();
			
			if (ii_users.idle[name]) ii_users.del('idle',name); //a live user is not idle

			if (ii_users.user[name])
			{
				ii_users.user[name].updateLocation(p,t);
				ii_users.user[name].timestamp = timestamp;
			}
			else 
			{
				//new live user (icon)
				ii_users.push('user', new iiUser(id,name,p,t,timestamp) );
			}
			break;
			
		case 'comment-add':
			//update comment notification
			
			tmp_debug(3,'new comment for loginid');
			if (event.get('userid')!=ii_login.id) iiMyUpdateMessages();
			break;
			
		case 'medium-add':
			//add media to collection
			var p = new GLatLng(event.get('lat'),event.get('lon'));
			
			//fetch media info
			SRV.get('q-medium-info',function(result) { iiLiveMedium(p,name,result) },'id',id);
			
			/* 
			ii_media_added = true; //will force new query on next screen update
			var p = new GLatLng(event.get('lat'),event.get('lon'));
			
			
			//add medium to world collection
			ii_world.add(
			

			//visible?
			if (gmap.getBounds().contains(p))
			{
				var w = event.get('width'); //-> do media info query (for w,h) first?
				var h = event.get('height');
				var id = event.get('id');
				var type = event.get('kind');
			
				//new iiMedia(id,type,p,w,h,true);
				ii_map.push( new iiMedia(id,type,p,w,h,true) ); 
			}
			//status
			if (ii_users.user[name]) ii_users.user[name].statusDisplay('media',p.lat(),p.lng());
			*/

			break;
			
		case 'track-delete':
			ii_media_added = true; //force query on next screen update //-> media can still be on the map in the meantime! NEEDS FIX
			
			//forced update now
			if (!ii_media_expanded) iiGetMedia();
			
			ii_panes['status'].display.childNodes[4].innerHTML = '';
			break;
	}
		
	tmp_debug(2,'<span style="color:#dd0000">LIVE&gt; user:',name,', type=',eventType,', p=',p,'</span>');
}

function iiLiveMedium(p,user,records)
{
	if (records.length==0) return;
	
	/* world query version, temporary */
	
	var medium = records[0];
	var id = medium.getField('id');
	medium.geo = p;

	tmp_debug(1,'adding new medium id=',medium.getField('id'),', geo=',p);

	//add to world media
	ii_world.add(medium);
	
	//add to map collection (if in area)
	if (gmap.getBounds().contains(medium.geo))
	{
		var w = medium.getField('width');
		var h = medium.getField('height');
		var type = medium.getField('kind');
	
		ii_map.push( new iiMedia(id,type,p,w,h,true) );
	}
	
	if (ii_users.user[user]) ii_users.user[user].statusDisplay('media',id);
	
}

//KWClient response handlers

function iiKWClientRsp(elm)
{
	if (!elm) return; //empty response

	tmp_debug(3,'server response:' + elm.tagName);
	
	
	switch(elm.tagName)
	{
		case 'login-rsp':
// 			var elements = elm.ownerDocument.getElementsByTagName('personid');
// 			ii_loginid = elements[0].firstChild.nodeValue;
//			ii_loginid = elm.ownerDocument.getElementsByTagName('personid')[0].firstChild.nodeValue;
			ii_login.id = elm.ownerDocument.getElementsByTagName('personid')[0].firstChild.nodeValue;
			tmp_debug(1,'logged in: id=',ii_login.id);
			
			KW.selectApp('geoapp', 'user');
			break;
			
		case 'select-app-rsp': //now we are logged in
			//enable logged-in functions
			iiLoggedIn();
			break;
			
		case 'logout-rsp': //now we are logged out
			//disable logged-in functions
			//iiLoggedOut();
			break;
			
		case 'cmt-insert-rsp':
		case 'cmt-delete-rsp':
			
			//-> change this, when KW.utopia is updated to have per request callback
		
// 			tmp_debug(1,'ii_media_commentModified=',ii_media_commentModified);
// 			if (ii_media_commentModified>-1) ii_map_media[ii_media_commentModified].getComments();
			break;
			
		case 'medium-update-rsp':

			//-> change this, when KW.utopia is updated to have per request callback

// 			tmp_debug(1,'ii_media_detailsModified=',ii_media_detailsModified);
// 			if (ii_media_detailsModified>-1) ii_map_media[ii_media_detailsModified].editDetails();
			break;
			
		case 'cmt-insert-nrsp':
		case 'cmt-delete-nrsp':
			alert('comment failed'); //-> change this
			break;
			
		case 't-trk-suspend-rsp': break;
		case 't-trk-resume-rsp': break;
		case 't-hb-rsp': break;
	}
}

function iiKWClientNegRsp(errorId, error, details)
{
	if (errorId==4100) //bad login
	{
		//warning
		iiActivityGlow(true,'#ff0000',100,0,false,14,50);
		document.getElementById('loginerror').innerHTML = 'invalid login !';
		
		//failed auto-login
		var autologin = readCookie('autologin');
		if (autologin && autologin!='false')
		{
		 	 iiSetAutoLogin(false)
		 	 document.getElementById('me_login').style.display = 'block';
		 	 document.getElementById('me_clickevents').style.display = 'block';
		}
	}
	
	//do something useful with other errors here
	tmp_debug(3,'negative resp:',errorId,':',error,' details=',details);
}



