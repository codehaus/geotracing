/* usemedia.com . joes koppers . 01.2007 */
/* thnx for reading this code */


//popup panes

var panes = new Panes();

function Panes()
{
	//panes collection
}
Panes.prototype.dispose = function()
{
	for (var i=0; i<arguments.length; i++)
	{
		var pane = this[arguments[i]];
		if (pane)
		{
			pane.dispose();
			delete this[arguments[i]];
		}
		
	}
}
Panes.prototype.hide = function()
{
	for (var i=0; i<arguments.length; i++)
	{
		var pane = this[arguments[i]];
		if (pane) pane.hide(1);
	}
}
Panes.prototype.show = function()
{
	for (var i=0; i<arguments.length; i++)
	{
		var pane = this[arguments[i]];
		if (pane) pane.show();
	}
}

function Pane(id,x,y,w,h,hide_delay,keep_visible,parent,autosize)
{
	this.id = id;
	this.x = x;
	this.y = y;
	this.w = w;
	this.h = h;
	this.hideTimeout = false;
	this.hide_delay = (hide_delay)? hide_delay:150;
	this.keep_visible = (keep_visible)? true:false;
	this.closing = false;
	this.parent = parent;
	this.autosize = autosize;
	
	/* build */

	var pane = document.createElement('div');
		pane.style.visibility = 'hidden'; //hide during creation
		pane.id = this.id;
		pane.style.display = 'none';
		pane.className = (!this.autosize)? 'pane':'pane_autosize';
		pane.style.left = this.x +"px";
		pane.style.top = this.y +"px";
		pane.style.width = (this.w+16+16) +"px";
		if (!this.autosize) pane.style.height = (this.h+16+16) +"px";
	if (parent) parent.appendChild(pane);
	else document.body.appendChild(pane);
	
	var c = 'lg'; //default white pane
	if (!this.autosize)
	{
		//bg	
		var str = '<div style="left:0px; top:0px; width:16px; height:16px; '+PNGbgImage('pane_'+c+'_nw.png')+'"></div>';
			str+= '<div style="left:16px; top:0px; width:'+(w)+'px; height:16px; '+PNGbgImage('pane_'+c+'_n.png')+'"></div>';
			str+= '<div style="left:'+(w+16)+'px; top:0px; width:16px; height:16px; '+PNGbgImage('pane_'+c+'_ne.png')+'"></div>';
			str+= '<div style="left:0px; top:16px; width:16px; height:'+h+'px; '+PNGbgImage('pane_'+c+'_w.png')+'"></div>';
			str+= '<div style="left:16px; top:16px; width:'+(w)+'px; height:'+(h)+'px; '+PNGbgImage('pane_'+c+'_c.png')+'"></div>';
			str+= '<div style="left:'+(w+16)+'px; top:16px; width:16px; height:'+(h)+'px; '+PNGbgImage('pane_'+c+'_e.png')+'"></div>';
			str+= '<div style="left:0px; top:'+(h+16)+'px; width:16px; height:16px; '+PNGbgImage('pane_'+c+'_sw.png')+'"></div>';
			str+= '<div style="left:16px; top:'+(h+16)+'px; width:'+(w)+'px; height:16px; '+PNGbgImage('pane_'+c+'_s.png')+'"></div>';
			str+= '<div style="left:'+(w+16)+'px; top:'+(h+16)+'px; width:16px; height:16px; '+PNGbgImage('pane_'+c+'_se.png')+'"></div>';
		pane.innerHTML = str;
		//content
		var content = document.createElement('div');
			content.className = 'content';
			content.style.width = this.w +9 +"px";
			content.style.height = this.h +12 +"px";
			if (browser.cssfilter) content.style.filter = 'Alpha(opacity=100)'; //IE display bug
		pane.appendChild(content);
		pane.style.visibility = 'visible';
	}
	else
	{
		//with vertical auto-sizing
		var str = '<div style="float:left; width:16px; height:16px; '+PNGbgImage('pane_'+c+'_nw.png')+'"></div>';
			str+= '<div style="float:left; width:'+(w)+'px; height:16px; '+PNGbgImage('pane_'+c+'_n.png')+'"></div>';
			str+= '<div style="float:left; width:16px; height:16px; '+PNGbgImage('pane_'+c+'_ne.png')+'"></div>';
			//embedded content div (table is necessary to get this working in IE)
			str+= '<div style="float:left; width:'+(w+32)+'px;">';
			str+= '<table border=0 cellpadding=0 cellspacing=0>';
			str+= '<tr>';
			str+= '<td style="width:'+16+'px; '+PNGbgImage('pane_'+c+'_w.png')+'"></td>';
			str+= '<td style="width:'+w+'px; height:'+(h)+'px; vertical-align:top; '+PNGbgImage('pane_'+c+'_c.png')+'"><div class="content"></div></td>';
			str+= '<td style="width:'+16+'px; '+PNGbgImage('pane_'+c+'_e.png')+'"></td>';
			str+= '</tr>';
			str+= '</table>';
 			str+= '</div>';
			str+= '<div style="position:relative; float:left; width:16px; height:16px; '+PNGbgImage('pane_'+c+'_sw.png')+'"></div>';
			str+= '<div style="position:relative; float:left; width:'+(w)+'px; height:16px; '+PNGbgImage('pane_'+c+'_s.png')+'"></div>';
			str+= '<div style="position:relative; float:left; width:16px; height:16px; '+PNGbgImage('pane_'+c+'_se.png')+'"></div>';
		pane.innerHTML = str;
		pane.style.visibility = 'visible';
	}
	
	//keep visible onmousover
	var obj = this;
	if (!keep_visible) pane.onmouseover = function() { if (!obj.closing) obj.show() };
	pane.onmouseout = function() { if (!obj.keep_visible) obj.hide() };
	
	//refs
	this.div = pane;
	this.content = (!this.autosize)? content:pane.childNodes[3].getElementsByTagName('div')[0];
	
	//add to panes
	panes[id] = this;
}

Pane.prototype.setContent = function(str)
{
	this.content.innerHTML = str;
}

Pane.prototype.setPosition = function(x,y)
{
	this.x = x;
	this.y = y;
	this.div.style.left = this.x +'px';
	this.div.style.top = this.y +'px';
}

Pane.prototype.setSize = function(w,h)
{
	if (w>0) this.w = w;
	if (h>0) this.h = h;
	
	var elms = this.div.childNodes;
	
	elms[1].style.width = this.w +'px';
	elms[2].style.left = this.w+16 +'px';
	elms[3].style.height = this.h +'px';
	elms[4].style.width = this.w +'px'; elms[4].style.height = this.h +'px'; 
	elms[5].style.height = this.h +'px'; elms[5].style.left = this.w+16 +'px';
	elms[6].style.top = this.h+16 +'px';
	elms[7].style.width = this.w +'px'; elms[7].style.top = this.h+16 +'px';
	elms[8].style.left = this.w+16 +'px'; elms[8].style.top = this.h+16 +'px';
	
	this.div.style.width = (this.w+16+16) +'px';
	this.div.style.height = (this.h+16+16) +'px';
	this.content.style.width = this.w +9 +'px';
	this.content.style.height = this.h +12 +'px';
}

Pane.prototype.show = function()
{
	this.doShow(true);
	this.visible = true;
}
Pane.prototype.hide = function(force)
{
	if (force) this.closing = true;
	this.doShow(false,force);
	this.visible = false;
}

Pane.prototype.doShow = function(show,forcehide)
{
	/*	show or hide pane, hide occurs after small delay, 
		and can be cancelled by calling pane.show() again */
	
	if (this.hideTimeout) window.clearTimeout(this.hideTimeout);
	
	if (forcehide)
	{
		this.div.style.display = 'none';
		this.hideMore();
		this.closing = false;
		return;
	}
		
	if (show)
	{
		this.div.style.display = 'block';
	}
	else
	{
		//hide afer delay
		var obj = this;
		this.hideTimeout = window.setTimeout('panes[\''+this.id+'\'].doShow(0,true)',obj.hide_delay);
	}
}

Pane.prototype.dispose = function()
{
	if (this.parent) this.parent.removeChild(this.div)
	else document.body.removeChild(this.div);
}

Pane.prototype.hideMore = function()
{
	/*	called when hiding
		default empty, extend in object */
}
