/* usemedia.com . joes koppers . 04.2007 */
/* thnx for reading this code */


//gui


function wpCreatePane(type)
{
	switch (type)
	{
		/* public panes */
		
		
		case 'main':
			var pane = new Pane('main',40,40,230,80,1,true);
			pane.setContent(wpGuiCreate('main'));
			pane.show();
			break;

		case 'login':
			var pane = new Pane('login',310,40,140,115,1,true);
			pane.setContent(wpGuiCreate('login'));
			//pane.show();
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
 			if (browser.pngsupport) str+= '<img src="media/mlgk.png">';
 			else str+= '<div style="width:116px; height:48px; '+PNGbgImage('mlgk.png')+'"></div>';
			str+= '<div id="menu">';
			str+= '<span class="red">create</span>, '; //-> change this to default = 'view';
			str+= '<a href="javascript://play">play</a> and ';
			str+= '<a href="javascript://view">view</a>';
			str+= '</div>';
			str+= '<div id="login"><a href="javascript://login" onclick="wpLogin()">login</a></div>';
			break;

		case 'login':
			str+= '<form name="loginform" method="" action="" onsubmit="return wpDoLogin();">';
			
			str+= '<div id="loginerror" style="left:15px; top:6px; width:130px; font-size:10px; color:#dd0000; text-align:center;"></div>';
			
			str+= '<div style="left:14px; top:23px; line-height:6px;">';
			str+= '<div class="column" style="width:45px;">name:</div><input type=text name=login value="" class="inputtext" style="width:85px;"><br><br>';
			str+= '<div class="column" style="width:45px;">pass:</div><input type=password name=password value="" class="inputtext"  style="width:85px;"><br><br>';
			str+= '&nbsp;&nbsp;<input type="checkbox" name="auto" onclick="wpToggleAutoLogin()" style="vertical-align:middle; border:0px; height:14px; background-color:transparent">&nbsp;';
 			str+= '<a href="javascript://toggle_autologin" onclick="wpToggleAutoLogin();this.blur()" title="requires cookies enabled in your browser">remember login</a>';
			str+= '</div>';

			str+= '<input type=submit value="login" style="position:absolute; left:104px; top:105px;">';


			str+= '</form>';
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
// function HiBgImg(id,img,show)
// {
// 	var elm = (typeof(id)=='object')? id:document.getElementById(id);
// 	if (browser.pngsupport) elm.src = 
// }



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






function iiAboutTabsHeader(tab)
{
	var tabs = ['info','promos','team','partners','terms of use','press','contact'];//,'signup'];

	var str = '<div class=menu>';
	for (var i in tabs)
	{
		if (tabs[i]!=tab) str+= '<a href="javascript://'+tabs[i].split(' ')[0]+'" onclick="iiAbout(\''+tabs[i].split(' ')[0]+'\')">'+tabs[i]+'</a>';
		else str+= '<b class="green">'+tabs[i]+'</b>';
		if (i<tabs.length-1) str+= ' | ';
	}
	str+= '</div>';
	str+= '<div class=subheader style="left:15px; top:17px; width:710px">&nbsp;</div>';
	
	return str;
}

//for better clickhandling in Safari
function clickHandler(target)
{
	if (navigator.userAgent.indexOf("Safari")!=-1) return 'href="javascript://'+target+'" onmousedown="iiMe(\''+target+'\')"';
	else return 'href="javascript:iiMe(\''+target+'\');" ';
}

//ii_activity_glow = false;

function iiActivityGlow(start,color,begin,end,continues,step,interval,forceEnd)
{
	if (ii_activity_glow) ii_activity_glow.stop();
	if (start)
	{
		var div = [document.getElementById('me_activity').childNodes[0].firstChild,document.getElementById('me_activity').childNodes[1].firstChild];
		div[0].style.backgroundColor = color;
		div[1].style.backgroundColor = color;
		
		if (!step) step = 8;
		if (!interval) interval = 60;
				
		ii_activity_glow = new iiFade('ii_activity_glow',div,begin,end,continues,step,interval,forceEnd);
		ii_activity_glow.start();
	}
}

function iiLoadingAnim(show)
{
	//reload needed in MSIE
	if (show && ii_browser_cssfilter) document.getElementById('me_loading').src = 'media/bliin_me_anim.gif';
	
	document.getElementById('me_loading').style.visibility = (show)? 'visible':'hidden';
}


/* bliin animation classes */

// function iiFadeRepeat(obj)
// {
// 	tmp_debug(4,'iiFadeRepeat');
// 	//to prevent bugs in MSIE when object is killed while fading
// 	if (obj)
// 	{
// 		obj.animate();
// 		tmp_debug(4,'fadeRepeat:',obj);
// 	}
// 	//else 
// }

function iiFade(obj,elm,b,e,continues,s,t,f)
{
	this.obj = obj; //name of object

	this.elm = elm; //target dom elements array
	this.fade = this.begin = b; //use begin>end for fadeout;
	this.end = e;
	this.continues = continues; //true/false for continues/once, number for specific amount of fade repeats
	this.count = 1;
	this.forceEnd = (typeof(f))? f:-1;

	this.step = (b>e)? -s:s; 	
	this.interval = t;
	this.loop = false; //ref to window.interval
}

iiFade.prototype.start = function()
{

	//reset first if used repeatedly
	this.fade = this.begin;
	this.count = 1;
	//set start level
	this.apply('show');
	
	//skip fading effects (user option)
	if (ii_disable_effects && this.obj!='ii_activity_glow' && this.obj!='loginfade')
	{
		this.stop();
		return;
	}
	
	//start interval
	this.fading = true;
	//this.loop = window.setInterval('iiFadeRepeat('+this.obj+')',this.interval);
	this.loop = window.setInterval(this.obj+'.animate()',this.interval);
}

iiFade.prototype.cancel = function()
{
	//abort
	this.fading = false;
	if (this.loop) window.clearTimeout(this.loop);
	this.loop = false;
	this.fade = this.begin;
	if (this.fade==0) this.apply('hide');
	else this.apply();
}

iiFade.prototype.stop = function()
{
	this.fading = false;
	if (this.loop) window.clearTimeout(this.loop);
	this.loop = false;
	//set fade to exact end value, unless an override end value is given
	this.fade = (this.forceEnd>-1)? this.forceEnd:this.end;
	if (this.fade==0) this.apply('hide');
	else this.apply();
}

iiFade.prototype.animate = function()
{
	this.fade += this.step;

	if ((this.step<0 && this.fade<=Math.min(this.begin,this.end)) || (this.step>0 && this.fade>=Math.max(this.begin,this.end)))
	{
		if (this.continues>1)
		{
			if (this.count<this.continues) //repeat specific amount of times
			{
				this.count++;
				this.step = -this.step;
			}
			else
			{
				//done
				this.stop();
				return;
			}
		}
		else if (this.continues)
		{
			this.step = -this.step; //reverse the fade
		}
		else
		{
			//done
			this.stop();
			return;
		}
	}
	//apply to div(s)
	this.apply();
	
	//if (this.obj=='ii_new.glow') document.getElementById('footer').innerHTML = 'fade='+this.fade;
}

iiFade.prototype.apply = function(display)
{
	for (var i=0; i<this.elm.length; i++)
	{
		//min=0, max=100;
		var f = Math.min(this.fade,100);
		var f = Math.max(f,0);
		if (ii_browser_cssfilter) this.elm[i].style.filter = 'alpha(opacity='+f+')';
		else this.elm[i].style.opacity = f/100;
		
		if (display)
		{
			this.elm[i].style.visibility = (display=='hide')? 'hidden':'visible';
			this.elm[i].style.display = (display=='hide')? 'none':'block';
		}


// 		if (f==0) this.elm[i].style.visibility = 'hidden';
// 		else this.elm[i].style.visibility = 'visible';
		
		if (display && display=='show') this.elm[i].style.display = 'block';

		//show or hide the elm entirely
// 		if (display)
// 		{
//			this.elm[i].style.display = (display=='show')? 'block':'none';
// 			if (display=='show') this.elm[i].style.visibility = 'visible';
			
//			this.elm[i].style.visibility = (display=='hide')? 'hidden':'visible';
			
// 		}
	}
}