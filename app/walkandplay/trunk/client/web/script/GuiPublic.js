/* usemedia.com . joes koppers . 04.2007 [rev 08.2007] */
/* thnx for reading this code */


//wp public gui

function wpCreatePane(type,obj)
{
	//delete if re-creating
	if (panes[type]) panes.dispose(type);

	var str = '';

	switch (type)
	{
		/* public panes */
		
		case 'main':
			var pane = new Pane('main',40,40,225,80,1,true);
// 			if (browser.properpngsupport) str+= '<img src="media/mlgk3.png" ondblclick="tmp_debug(\'toggle\')">';
// 			else str+= '<div style="width:140px; height:45px; '+PNGbgImage('mlgk3.png')+'" ondblclick="tmp_debug(\'toggle\')"></div>';
			str+= '<div style="font-style:italic">DMS workshop tool<br>or whatever</div>';
			str+= '<div id="menu" style="line-height:20px;">';
			str+= '<a href="javascript://create" onclick="wpSelect(\'create\')">create</a>, ';
			str+= '<a href="javascript://play" onclick="wpSelect(\'play\')">play</a> and ';
			str+= '<a href="javascript://view" onclick="wpSelect(\'view\')">view</a>';
			str+= '</div>';
			str+= '<div id="login"><a href="javascript://login" onclick="wpLogin()">login</a></div>';

			pane.setContent(str);
			pane.show();
			break;

		case 'login':
			var pane = new Pane('login',310,40,140,115,1,true);

			str+= '<form name="loginform" method="" action="" onsubmit="return wpDoLogin();">';
			str+= '<div id="loginerror" style="left:15px; top:2px; width:130px; font-size:10px; color:#dd0000; text-align:center;"></div>';
			str+= '<div style="left:11px; top:19px;">';
			str+= '<div class="column" style="width:45px;">name:</div><input type=text name=login value="" class="inputtext" style="width:85px;"><br>';
			str+= '<div style="margin-top:5px; position:relative"><div class="column" style="width:45px;">pass:</div><input type=password name=password value="" class="inputtext"  style="width:85px;"></div><br>';
			str+= '&nbsp;&nbsp;<input type="checkbox" name="auto" onclick="wpToggleAutoLogin()" style="vertical-align:middle; border:0px; height:14px; background-color:transparent">&nbsp;';
 			str+= '<a href="javascript://toggle_autologin" onclick="wpToggleAutoLogin();this.blur()" title="requires cookies enabled in your browser">remember login</a>';
			str+= '</div>';
			str+= '<input type="button" value="cancel" onclick="wpCancelLogin()" style="position:absolute; right:65px; bottom:8px; width:50px;">';
			str+= '<input type=submit value="login" style="position:absolute; right:11px; bottom:8px; width:50px;">';
			str+= '</form>';

			pane.setContent(str);
			//pane.show();
			break;

		case 'list_games':
			var pane = new Pane('list_games',100,160,200,200,1,true);
			break;

		case 'game_profile':
			var pane = new Pane('game_profile',340,110,500,400,1,true);
			str+= '<span class="red" style="font-size:14px; font-weight:bold">game profile</span>&nbsp; <b>"'+obj.game.name+'"</b><br>';
			str+= 'created by <span class="red">'+obj.game.ownername+'</span><br><br>';
			str+= '<a style="position:absolute; right:12px; top:6px" href="javascript://close" onclick="wpSelect(\'view\')">close</a>';
			
//			str+= '<br><div id="profile_data"></div>';

			str+= '<div style="position:relative; margin-left:-5px; padding:5px; width:498px; background-color:#eeeeee; margin-bottom:10px;">';
			str+= obj.game.description+'<br>';
			str+= '</div>';

			str+= '<div style="font-style:italic"><br>'+obj.game.intro+'<br><br></div>';
			
			str+= '<br><fieldset style="position:absolute; left:10px; bottom:10px;" class="framed"><legend> <b>stats</b> </legend>';
			str+= '<div id="profile_stats" style="width:226px; height:145px; overflow:auto;">';
			str+= obj.stats;
			str+= '</div></fieldset>';
			str+= '<br><fieldset style="position:absolute; right:10px; bottom:10px;"  class="framed"><legend> <b>gamerounds</b> </legend>';
			str+= '<div id="profile_rounds" style="width:226px; height:145px; overflow:auto;">';
			str+= obj.rounds;
			str+= '</div></fieldset>';
			
			
			str+= '<div id="profile_submit" style="right:15px; bottom:15px;"></div>';
			pane.setContent(str);
			pane.show();
			//pane.update = function(str) { this.content.lastChild.innerHTML = str; };
			break;

		case 'list_rounds':
			var pane = new Pane('list_rounds',340,188,165,140,1,true);
			pane.setContent(wpGuiCreate('list_rounds',obj));
			break;

		case 'display':
			var pane = new Pane('display',0,40,230,100,1,true,undefined,true); //auto-size pane

			str+= '<div id="media_display" style="width:228px; margin-bottom:5px"></div>';
			pane.setContent(str);

			//align right side of window
			pane.div.style.left = '';
			pane.div.style.right = '20px';
			/*			
			pane.hideMore = function()
			{
				//clear pane contents
				var obj = this;
				window.setTimeout(function() { obj.content.firstChild.innerHTML = '' },150);
			
// 				if (browser.safari && document.getElementById('qtvideo'))
// 				{
// 					//qt safari bug (force sound stop)
// 					tmp_debug(1,'QT STOP');
// 					document.getElementById('qtvideo').Stop();
// 				}
			}
			*/
			break
			
			
		case 'location_info':
			var pane = new Pane('location_info',0,0,140,20,250,false,gmap.getPane(G_MAP_FLOAT_PANE));
			pane.content.onmousedown = function(e)
			{
				if (!e) e = event;
				cancelEvents(e);
			}
			pane.content.style.paddingTop = '4px';
			break;
			
		case 'view':
			var pane = new Pane('view',100,160,180,135,1,true);

			var round = 'date';
			str+= '<div style="position:relative; width:150px; margin-bottom:10px"><span class="title">replay </span> "<b>'+wp_games.game[wp_selected_game].name+'</b>" / <b>'+wp_rounds.round[wp_selected_round].name+'</b></div>';
			str+= '<a style="position:absolute; right:12px; top:6px" href="javascript://exit" onclick="wpLeaveView()">exit</a>';
			//str+= '<span class="title">team</span>&nbsp;';
			
			var round = wp_rounds.round[wp_selected_round];
			if (round.teams.length==0) str+= '- no gameplays available -<br>';
			else
			{
				str+= '<select name="play" onchange="wpSelectPlay(this.value)">';
				str+= '<option value="">select a team..</option>';
				str+= '<option value=""></option>';			
				for (var id in round.teams.team) str+= '<option value="'+id+'">'+round.teams.team[id].name+'</option>';
				str+= '<option value=""></option>';
				str+= '</select><!--<span id="view_duration" style="margin-left:10px;"></span>--><br>';
			}
			
			str+= '<div id="view_ctls" style="display:none; margin-top:8px; margin-left:-5px;">';
			str+= '<div style="background-color:#dbdbdb; width:188px; height:42px;">';

			var type = (browser.properpngsupport)? 'png':'gif';
			str+= '<img src="media/button_begin.'+type+'" onclick="wp_view.rset()" onmouseover="HiImg(this,1,\''+type+'\')" onmouseout="HiImg(this,0,\''+type+'\')" style="cursor:pointer; position:absolute; left:6px; top:10px; width:22px; height:22px;">';
			str+= '<img id="view_start" src="media/button_play.'+type+'" onclick="wp_view.startstop()"onmouseover="HiImg(this,1,\''+type+'\')" onmouseout="HiImg(this,0,\''+type+'\')" style="cursor:pointer; position:absolute; left:31px; top:3px; width:36px; height:36px;">';
			
			str+= '<span id="view_duration" style="position:absolute; left:73px; top:3px;">duration</span>';
			
			str+= '<select name="rate" onchange="wp_view.setRate(Number(this.value))" style="position:absolute; left:91px; top:21px;">';
			str+= '<option value=".5" style="padding-left:5px">.5 x</option>';
			str+= '<option value="1" selected>realtime</option>';
			str+= '<option value="2" style="padding-left:9px">2 x</option>';
			str+= '<option value="4" style="padding-left:9px">4 x</option>';
			str+= '<option value="8" style="padding-left:9px">8 x</option>';
			str+= '<option value="16">16 x</option>';
			str+= '<option value="32">32 x</option>';
			str+= '<option value=""></option>';
			str+= '</select>';
			str+= '</div>';

			str+= '<div id="view_progress" style="position:absolute; left:4px; top:47px; width:180px; height:20px; background-color:white;">';
			str+= '<div id="view_progress_bar" style="position:absolute; left:0px; top:0px; width:0px; height:20px; background-color:rgb(213,213,213)"></div>';
			var type = (browser.pngsupport)? 'png':'gif';
			str+= '<img src="media/progress.'+type+'" style="position:absolute; left:0px; top:0px; z-index:10; width:180px; height:20px;">';
			str+= '</div>';
			
			str+= '</div>';
			
			pane.setContent(str);

// 			pane.setContent(wpGuiCreate('view'));
// 			pane.show();
			break;

		case 'play':
			var pane = new Pane('play',110,0,590,80,1,true);
			
			str+= '<div style="left:11px; top:9px; width:160px">x</div>';
			str+= '<div style="left:175px; top:5px; width:190px; padding:4px 10px 5px 10px; background-color:#dbdbdb; height:80px">x</div>';
			str+= '<div style="left:400px; top:9px; width:215px">x</div>';
			str+= '<div style="position:absolute; right:13px; top:5px"></div>';
			pane.setContent(str);
			
			//align bottom of window
			pane.div.style.top = '';
			pane.div.style.bottom = '30px';
			//refs for updating
			pane.game = pane.content.childNodes[0];
			pane.play = pane.content.childNodes[1];
			pane.round = pane.content.childNodes[2];
			pane.clearContents = function()
			{
				this.game.innerHTML = '';
				this.play.innerHTML = '';
				this.round.innerHTML = '';
			}
			break;
			
// 		case 'list_games_view':
// 			var pane = new Pane('list_games',100,160,200,200,1,true);
// 			if (wp_viewmode=='archived') str+= '<b><span class="red">archived</span> or <a href="javascript://live_games" onclick="alert(\'not yet\')">live games</a></b><br><br>';
// 			else str+= '<b><a href="javascript://archived_games">archived</a> or <span class="red">live games</span></b><br><br>';
// 			str+= '<div id="list_rounds"></div>';
// 			pane.setContent(str);
// 			break;

		case 'list_view':
			var pane = new Pane('list_view',100,160,200,200,1,true);
			//header
			if (wp_viewmode=='archived')
			{
				var header = '<b><span class="red">archived</span> or <a href="javascript://live_games" onclick="alert(\'not yet\')">live games</a></b><br><br>';
			}
			else
			{
				var header = '<a href="javascript://archived_games">archived</a> or <span class="red">live games</span><br><br>';
			}
			str+= header;
			str+= '<div class="list"></div>';
			pane.setContent(str);
			pane.updateList = function(list)
			{
				this.content.lastChild.innerHTML = list;
			}
			break;

		default:
			if (wp_login.id) wpCreatePaneUser(type,obj);
	}
}

function wpEmbedMedium(type,id)
{
	var str = '';
	
	switch (type)
	{
		case 'text':
			str+= DH.getURL('/wp/media.srv?id='+id+'&t='+new Date().getTime());
			break;
		
		case 'image':
			str+= '<img src="/wp/media.srv?id='+id+'&resize=225x169">';
			break;
		
		case 'video':
			/*
			//QuickTime embed
			str+= '<OBJECT id="qtvideo" CLASSID="clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B" WIDTH="225" HEIGHT="168" CODEBASE="http://www.apple.com/qtactivex/qtplugin.cab">';
			str+= '<PARAM name="SRC" VALUE="/wp/media.srv?id='+this.mediumid+'">';
			str+= '<PARAM name="CONTROLLER" VALUE="true">';
			str+= '<PARAM name="AUTOPLAY" VALUE="true">';
			str+= '<PARAM name="BGCOLOR" VALUE="white">';
			str+= '<PARAM name="CACHE" VALUE="true">';
			str+= '<EMBED name="qtvideo" SRC="/wp/media.srv?id='+this.mediumid+'" BGCOLOR="white" WIDTH="225" HEIGHT="168" CONTROLLER="true" AUTOPLAY="true" CACHE="true" PLUGINSPAGE="http://www.apple.com/quicktime/download/">';
			str+= '</EMBED>';
			str+= '</OBJECT>';
			*/
			//Flash embed
			str+= '<object classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" codebase="http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=8,0,0,0" width="225" height="169" id="world">';
			str+= '<param name="movie" value="/wp/media.srv?id='+id+'&format=swf&resize=225x169" />';
			str+= '<param name="quality" value="high" />';
			str+= '<param name="bgcolor" value="#ffffff" />';
			str+= '<embed src="/wp/media.srv?id='+id+'&format=swf&resize=225x169" quality="high" bgcolor="#ffffff" width="225" height="169" name="world" type="application/x-shockwave-flash" pluginspage="http://www.macromedia.com/go/getflashplayer" />';
			str+= '</object>';
			break;
			
		case 'audio':
			str+= '<EMBED name="qtvideo" SRC="/wp/media.srv?id='+id+'" BGCOLOR="white" WIDTH="225" HEIGHT="16" CONTROLLER="true" AUTOPLAY="true" CACHE="true" PLUGINSPAGE="http://www.apple.com/quicktime/download/">';
			str+= '</EMBED>';
	}
	
	//alert(str);
	
	return str;
}

function AddGTlogo(mapdiv)
{
	//add the geotracing logo
	var logo = document.createElement('DIV');
		logo.id = 'powered_by';
		if (!browser.pngsupport) logo.style.filter = PNGbgImage('powered_by_geotracing.png').substr(7);
		else logo.style.backgroundImage = 'url(media/powered_by_geotracing.png)';
		logo.onclick = function() { window.open('http://geotracing.com') }
	//attach to map
	mapdiv.appendChild(logo);
}

function wpMapFadeEdges(mapdiv)
{
	var fades = ['n','e','s','w'];
	
	//adjust position of the google logo & copyright notice
	mapdiv.childNodes[2].style.left = '12px'; mapdiv.childNodes[2].style.bottom = '10px';
	mapdiv.childNodes[1].style.right = '13px'; mapdiv.childNodes[1].style.bottom = '12px';

	for (var i=0; i<fades.length; i++)
	{
		var fade = document.createElement('DIV');
			fade.id = 'fade_'+fades[i];
			fade.className = 'fade';
			if (!browser.pngsupport) fade.style.filter = PNGbgImage('map_fade_'+fades[i]+'.png').substr(7);
			else fade.style.backgroundImage = 'url(media/map_fade_'+fades[i]+'.png)';
		//attach to map, below google logo and copyright notice	
		mapdiv.insertBefore(fade,mapdiv.childNodes[1]);
	}
}

function PNGbgImage(media)
{
	//png transparency workaround for MSIE
	if (browser.cssfilter) return "FILTER:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale,src=\'media/"+media+"\');";
	else return "background-image:url(\'media/"+media+"\');";
}

//generic rollover functions
function Hi(id,show)
{
	document.getElementById(id).style.visibility = (show)? 'visible':'hidden';
}
function HiImg(id,show,type)
{
	var type = type || 'png';
	var elm = (typeof(id)=='object')? id:document.getElementById(id);
	
	elm.src = (show)? elm.src.replace(eval('/\.'+type+'/'),'X.'+type):elm.src.replace(eval('/\X.'+type+'/'),'.'+type);
	
	//elm.src = (show)? elm.src.replace(/\.png/,'X.png'):elm.src.replace(/\X.png/,'.png');
}

/* tooltip obj */

function Tooltip(src,x,y,w,h)
{
	var tooltip = document.createElement('img');
	tooltip.style.visibility = 'hidden';
	tooltip.style.display = 'none';
	tooltip.style.width = w +'px';
	tooltip.style.height = h +'px';
	if (src.indexOf('.png')==-1 || browser.pngsupport) tooltip.src = 'media/'+src;
	else
	{
		tooltip.src = 'media/blank.gif';
		tooltip.style.filter = PNGbgImage(src).substr(7);
	}
	
	tooltip.style.position = 'absolute';
	tooltip.style.zIndex = '100001'; //higher than dragdrop elm
	//tooltip.style.border = '1px solid red';
	document.body.appendChild(tooltip);
	
	this.x = this.offsetX = (x)? x:0;
	this.y = this.offsetY = (y)? y:0;
	this.elm = tooltip;
	this.enabled = false;
}

Tooltip.prototype.enable = function(enable)
{
	this.enabled = enable;
	this.elm.style.visibility = (enable)? 'visible':'hidden';
	this.elm.style.display = (enable)? 'block':'none';
}

Tooltip.prototype.update = function(e,x,y)
{
	var x = (x!=undefined)? x:e.clientX;
	var y = (y!=undefined)? y:e.clientY;
		
	this.x = x + this.offsetX;
	this.y = y + this.offsetY;
	
	this.elm.style.left = this.x +'px';
	this.elm.style.top = this.y +'px';
}
