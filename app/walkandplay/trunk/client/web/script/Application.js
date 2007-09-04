/* usemedia.com . joes koppers . 04.2007 */
/* thnx for reading this code */


//the wp (general) application

wp_autologin = false;
wp_set_autologin = false;
wp_login = new Object();
wp_login_action = false;

//defaults
wp_mode = 'view'; 
wp_viewmode = 'archived';
wp_viewstate = 'paused';

function wpStartup()
{
	/* create and enable gui  */
	
 	wpCreatePane('main');
 	wpCreatePane('login');
 	wpCreatePane('display');
 	
 	wpToggleAutoLogin(); //auto-login enabled by default
 	
 	wpGameInit();

	wp_login_action = 'create'; //switch to create mode if autologin
	wpAutoLogin();
	
	
	
	//wpSelect(wp_mode);
}

function wpSelect(mode)
{
	if (!wp_login.id && (mode=='create' || mode=='play')) 
	{
		wp_login_action = mode;
		//wpSelect();
		wpLogin();
		document.getElementById('loginerror').innerHTML = 'please login first';
		mode = '';
	}

	//is there a game selected/active?
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
		if (wp_mode=='view')
		{
			if (wp_view) wp_view.rset();
		}
		//unload game
		wp_games.game[wp_selected_game].unLoad();
		wp_selected_game = false;
		wp_selected_round = false;
		wp_selected_play = false;
	}
	
	//hide panes
	panes.hide('list_create','list_play','list_view');
	panes.hide('edit_game','edit_rounds','edit_round','edit_profile');
	panes.hide('game_profile','display');
	panes.hide('edit_game','list_games','list_rounds','list_locations','display','play','view');
	if (mode=='view') panes.hide('login');
	
	wp_mode = mode;
	
	var create = '<a href="javascript://create" onclick="wpSelect(\'create\')">create</a>';
	var play = '<a href="javascript://play" onclick="wpSelect(\'play\')">play</a>';
	var view = '<a href="javascript://view" onclick="wpSelect(\'view\')">view</a>';

	switch (mode)
	{
		case 'create':
			create = '<span class="red" style="cursor:pointer" onclick="wpSelect()">create</span>';

			wp_add_location = new Tooltip('icon_location_b_enabled.png',-17,-38,20,34);

			//get editable games for user
			SRV.get('q-games-by-user',wpListGames,'user',wp_login.loginname);
			break;
			
		case 'play':
			play = '<span class="red" style="cursor:pointer" onclick="wpSelect()">play</span>';
			panes['play'].clearContents();
			panes['play'].content.lastChild.innerHTML = '<a href="javascript://exit" onmouseup="wpLeavePlay()">exit</a>';
			
			//get scheduled games for user
			SRV.get('q-play-status-by-user',wpListGames,'user',wp_login.loginname);
			break;
			
		case 'view':
			view = '<span class="red" style="cursor:pointer" onclick="wpSelect()">view</span>';
			panes['play'].clearContents();
			panes['play'].content.lastChild.innerHTML = '';
			
			//get available games (live or archived)
			var select = wp_viewmode;
			wpSelectView(select);
			break;
			
		default:
			//no selected mode
			break;
	}
	
	//rewrite menu
	document.getElementById('menu').innerHTML = create+', '+play+' and '+view;
}

function wpCloseDisplay()
{	
	if (wp_location_expanded) wp_location_expanded.collapse();
	else panes['display'].hide(1);	
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
	else wpSelect('view');
}

function wpLogin()
{
	wpSelect();
	//panes.hide('list_view');
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

function wpCancelLogin()
{
	document.getElementById('loginerror').innerHTML = '';
	panes.hide('login');
	wp_login_action = false;
}

function wpLoggedIn()
{
	if (!wp_login.id) return;
	
	/* now we are logged in */
	wp_loggedIn = true;

	tmp_querytime = tmp_debug(1,'login succes: user=',wp_login.loginname,',id=',wp_login.id);
	
	if (panes['login'].visible) panes['login'].hide(true);

	if (!wp_autologin && wp_set_autologin) wpSetAutoLogin(true);
	
	document.getElementById('login').innerHTML = '<span class="red">'+wp_login.loginname+'</span><br><a href="javascript://logout" onclick="wpDologout()" title="logged in as \''+wp_login.loginname+'\'">sign out</a>';


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
 	//no mode (unloads game)
 	wpSelect();
	
	//clear data
	document.forms['loginform'].login.value = wp_login.loginname = '';
	document.forms['loginform'].password.value = wp_login.password = '';
	document.getElementById('loginerror').innerHTML = '';

	//clear login obj
	wp_login = new Object();

	//clear cookie
	wpSetAutoLogin(false);

	if (wp_live) PL.leave();
	wp_live = false;
	wp_live_subscribed = false;
	
	//unload stuff
	panes.dispose('edit_game','edit_location');
	
	if (wp_hb) window.clearTimeout(wp_hb);
 	
 	document.getElementById('login').innerHTML = '<a href="javascript://login" onclick="wpLogin()">login</a>';
 	
 	//-> dispose all games ?
 	//..
	//for (var id in wp_games.game) wp_games.del(id);
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
		wp_games.game[wp_game_selected].newLocation(p);
	}
}

function wpMapMoveend()
{
	tmp_debug(3,'moveend');
	
	//locations, players
	wp_locations.update();
	wp_players.update();

	//game locations	
	if (wp_game_selected)
	{
		var game = wp_games.game[wp_game_selected];

		game.locations.update();
	}
}

function wpMapZoomend(b,e)
{
	tmp_debug(3,'zoomend');
	
	//position editLocation pane
	if (panes['edit_location'] && panes['edit_location'].visible)
	{
		var geo = (wp_locations.location['new'])? wp_locations.location['new'].geo:wp_selected_location.geo;
		var px = gmap.fromLatLngToDivPixel(geo);
		panes['edit_location'].setPosition(px.x,px.y);
	}
}

function wpWindowResize()
{
	//not used
}