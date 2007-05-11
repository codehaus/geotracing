/* usemedia.com . joes koppers . 04.2007 */
/* thnx for reading this code */


//gui


function wpCreatePane(type)
{
	switch (type)
	{
		/* public panes */
		
		case 'main':
			var pane = new Pane('main',40,40,225,80,1,true);
			pane.setContent(wpGuiCreate('main'));
			pane.show();
			break;

		case 'login':
			var pane = new Pane('login',310,40,140,115,1,true);
			pane.setContent(wpGuiCreate('login'));
			//pane.show();
			break;
			
		case 'display':
			var pane = new Pane('display',0,40,230,100,1,true,undefined,true); //auto-size pane
			pane.setContent(wpGuiCreate('display'));
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
			
		case 'playdisplay':
			var pane = new Pane('playdisplay',110,0,600,80,1,true);
			pane.setContent(wpGuiCreate('playdisplay'));
			//align bottom if window
			pane.div.style.top = '';
			pane.div.style.bottom = '30px';
			//refs for updating
			pane.game = pane.content.childNodes[0];
			pane.play = pane.content.childNodes[1];
			pane.round = pane.content.childNodes[2];
			break;
		
		default:
			if (wp_login.id) wpCreatePaneUser(type);
	}
}

//pngfixed html
function wpGuiCreate(type,s,id,n)
{
	var str='';
	
	switch(type)
	{
		case 'main':
 			if (!browser.cssfilter) str+= '<img src="media/mlgk.png" ondblclick="tmp_debug(\'toggle\')">';
 			else str+= '<div style="width:116px; height:48px; '+PNGbgImage('mlgk.png')+'"></div>';
			str+= '<div id="menu" style="line-height:20px;">';
			str+= '<a href="javascript://create" onclick="wpSelect(\'create\')">create</a>, ';
			str+= '<a href="javascript://play" onclick="wpSelect(\'play\')">play</a> and ';
//			str+= '<a href="javascript://view">view</a>';
			str+= '<span class="red">view</span>'; //view is default modus 
			str+= '</div>';
			str+= '<div id="login"><a href="javascript://login" onclick="wpLogin()">login</a></div>';
			break;

		case 'login':
			str+= '<form name="loginform" method="" action="" onsubmit="return wpDoLogin();">';
			
			str+= '<div id="loginerror" style="left:15px; top:4px; width:130px; font-size:10px; color:#dd0000; text-align:center;"></div>';
			
			str+= '<div style="left:11px; top:19px;">';
			str+= '<div class="column" style="width:45px;">name:</div><input type=text name=login value="" class="inputtext" style="width:85px;"><br>';
			str+= '<div style="margin-top:5px; position:relative"><div class="column" style="width:45px;">pass:</div><input type=password name=password value="" class="inputtext"  style="width:85px;"></div><br>';
			str+= '&nbsp;&nbsp;<input type="checkbox" name="auto" onclick="wpToggleAutoLogin()" style="vertical-align:middle; border:0px; height:14px; background-color:transparent">&nbsp;';
 			str+= '<a href="javascript://toggle_autologin" onclick="wpToggleAutoLogin();this.blur()" title="requires cookies enabled in your browser">remember login</a>';
			str+= '</div>';

			str+= '<input type="button" value="cancel" onclick="panes[\'login\'].hide(1)" style="position:absolute; right:65px; bottom:8px; width:50px;">';
			str+= '<input type=submit value="login" style="position:absolute; right:11px; bottom:8px; width:50px;">';
			str+= '</form>';
			break;
			
		case 'display':
			str+= '<div id="media_display" style="width:228px; margin-bottom:5px"></div>';
			break;
			
		case 'playdisplay':
			str+= '<div id="playdisplay_game" style="left:11px; top:9px; width:160px"></div>';
			str+= '<div id="playdisplay_game" style="left:175px; top:5px; width:190px; padding:4px 10px 5px 10px; background-color:#dbdbdb; height:80px"></div>';
			str+= '<div id="playdisplay_game" style="left:400px; top:9px; width:215px"></div>';

			str+= '<a style="position:absolute; right:13px; top:5px" href="javascript://exit" onmouseup="wpLeavePlay()">exit</a>';
//			str+= '<input type="button" style="position:absolute; right:13px; top:5px" value="exit" onclick="if(confirm(\'leave gameplay?\'))wpSelect(\'play\')">';
			break;
		
		default:
			if (wp_login.id) str+= wpGuiCreateUser(type,s,id,n);
	}
	
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
function HiImg(id,img,show)
{
	var elm = (typeof(id)=='object')? id:document.getElementById(id);
	elm.src = (show)? 'media/'+img.replace(/\./,'X.'):'media/'+img;
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
