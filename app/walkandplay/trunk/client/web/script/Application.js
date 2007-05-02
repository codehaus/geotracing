/* usemedia.com . joes koppers . 04.2007 */
/* thnx for reading this code */


//the wp (general) application

wp_autologin = false;
wp_set_autologin = false;
wp_login = new Object();
wp_mode = 'play'; //default modus is play

function wpStartup()
{
	/* create and enable gui  */
	
 	wpCreatePane('main');
 	wpCreatePane('login');
 	wpCreatePane('display');
 	
 	wpToggleAutoLogin(); //auto-login enabled by default
 	
 	wp_locations = new wpLocations();
 	wp_players = new wpPlayers();
}

function wpSelect(mode)
{
	if (!wp_login.id && (mode=='create' || mode=='play')) 
	{
		alert('please login first');
		wpLogin();
		return false;
	}

	//unload game if any
	if (wp_selected_game)
	{
		if (wp_mode=='create')
		{
			//check if there's an unsaved location
			if (wp_locations.location['new']) if (wpCancelLocation(true)) return;
			
			//disable location tooltip
			if (wp_add_location && wp_add_location.enabled)
			{
				wp_add_location.enable(0);
				document.onmousemove = null;
			}
		}
		
		//unload game
		wp_games.game[wp_selected_game].unLoad();
		wp_selected_game = false;
		//hide panes
		panes.hide('edit_game','list_games','list_locations','display','play');
		
	}

	wp_mode = mode;
	
	var create = '<a href="javascript://create" onclick="wpSelect(\'create\')">create</a>';
	var play = '<a href="javascript://play" onclick="wpSelect(\'play\')">play</a>';
	var view = '<a href="javascript://view" onclick="wpSelect(\'view\')">view</a>';

	switch (mode)
	{
		case 'create':
			create = '<span class="red">create</span>';

			wp_add_location = new Tooltip('icon_location_b_enabled.png',-17,-38,20,34);

			//get editable games for user
			//SRV.get('q-games-by-user',wpEditGames,'user',wp_login.loginname);
			SRV.get('q-games-by-user',wpListGames,'user',wp_login.loginname);
			break;
			
		case 'play':
			play = '<span class="red">play</span>';
			
			//get scheduled games for user
			SRV.get('q-play-status-by-user',wpListGames,'user',wp_login.loginname);
			break;
			
		case 'view':
			view = '<span class="red">view</span>';
			panes.hide('list_games');
			break;
	}
	
	//rewrite menu
	document.getElementById('menu').innerHTML = create+', '+play+' and '+view;
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
	if (!(browser.cssfilter&&browser.pngsupport)) document.forms['loginform'].login.focus();
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
		KW.init(wpProcessLogin,function(errorId,error,details) { wpProcessLogin(false,errorId,error,details) },30,DH.getBaseDir()+'/..');
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
	KW.logout();
	wpLoggedOut();
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
			
			//failed auto-login
			var autologin = readCookie('autologin');
			if (autologin && autologin!='false') wpSetAutoLogin(false);
		}
		
		//do something useful with other errors here
		tmp_debug(3,'n-rsp:',errorId,':',error,' details=',details);
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
	
	document.getElementById('login').innerHTML = '<a href="javascript://logout" onclick="wpDologout()" title="logged in as \''+wp_login.loginname+'\'">sign out</a>';


	/* load registered user gui */

	wpLoadScript('GuiPrivate.js');
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
	if (wp_selected_game)
	{
		if (wp_mode=='create')
		{
			//check if there's an unsaved location
			if (wp_locations.location['new']) if (wpCancelLocation(true)) return;
			
			//disable location tooltip
			if (wp_add_location && wp_add_location.enabled)
			{
				wp_add_location.enable(0);
				document.onmousemove = null;
			}
		}
		
		//unload game
		wp_games.game[wp_selected_game].unLoad();
		wp_selected_game = false;
	}	
	
	//clear data
	document.forms['loginform'].login.value = wp_login.loginname = '';
	document.forms['loginform'].password.value = wp_login.password = '';
	document.getElementById('loginerror').innerHTML = '';

	//clear login obj
	wp_login = new Object();

	//clear cookie
	wpSetAutoLogin(false);
	
	//unload stuff
	panes.dispose('list_games','list_locations','edit_game','edit_location');

	//dispose games
	for (var id in wp_games.game) wp_games.del(id);

 	
 	document.getElementById('login').innerHTML = '<a href="javascript://login" onclick="wpLogin()">login</a>';
 	
 	//back to view mode
 	wpSelect('view');
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
	
	
	//if (playertest) playertest.updateLocation(p,new Date().getTime());
	
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
	wp_players.update();
	
	
	if (wp_game_selected)
	{
		var game = wp_games.game[wp_game_selected];

		game.locations.update();
	//	game.players.update();
	}
	
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


/* KW server communication and live events */

function _wpLiveInit(subject)
{
	//wp_live = true;
	//return; //tmp disable

	//register pushlet
	PL.joinListen('/wp/'+subject);
	onData = wpLive;
}

function _wpLive(event)
{
	var eventType = event.get('event');

/*	Event types and attributes:
	"user-move": userid, username, gameplayid, lon, lat */


	switch (eventType)
	{
		case 'user-move':
			var id = event.get('userid');
	
			var p = new GLatLng(event.get('lat'),event.get('lon'));
			var t = event.get('t');
			var timestamp = new Date().getTime();
			var name = event.get('username');
		
			
			
			//add or update player
			if (wp_players.player[id]) wp_players.player[id].updateLocation(p,t);
			else wp_players.push( new wpPlayer(wp_players,id,p,name,t) );

			tmp_debug(2,'<span style="color:#dd0000">user-move:&gt; user=',name,'</span>');
			break;
			
//			playertest.
			
			
		case 'task-hit':
			
			
			break;
			
		default:
			tmp_debug(2,'<span style="color:#dd0000">wpLive:&gt; type=',eventType,'</span>');
			break;
		
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
