/* usemedia.com . joes koppers . 11.2006 */
/* thnx for reading this code */


//generic dragdrop for (absolute positioned) DOM elements

/* usage:
	var thing = document.getElementById('thing');
	dragthing = makeDragableItem(thing);
	//optional:
	dragthing.setEnabled(true/false,x_only,y_only) 	//default = true, add x_only or y_only for single-axis dragging
	dragthing.setRange()							//default = none, add a dragging range relative to original postion
	dragthing.appearance = function(d)				//called at drag-start (d=true) and drag-end (d=false), default 50% opacity
	dragthing.dragging = function(e)				//executed at mousemove while dragging the elm (e=eventobj)
	dragthing.drop = function(e) 					//action when item is dropped (e=eventobj)
	dragthing.click = function()					//action when item is dropped but has not moved (single click on elm)
	dragthing.rset()								//restores elm to original state; */

DD_dragging = false; //global var, can be used to block events during a drag

function makeDragableItem(elm) //for code readability
{
	return new DD(elm);
}	

function DD(elm)
{
	this.elm = elm;
	this.originalX = this.prevX = this.x = (elm.style.left)? parseInt(elm.style.left):0;
	this.originalY = this.prevY = this.y = (elm.style.top)? parseInt(elm.style.top):0;
	this.offsetX = 0;
	this.offsetY = 0;
	this.rangeX = { enabled:false }
	this.rangeY = { enabled:false }
	this.z = elm.style.zIndex;
	this.is_dragging = false;

	var obj = this;
	this.elm.onmousedown = function(e) { obj.down(e) };
	
	this.setEnabled(true);
}

DD.prototype.setEnabled = function(enabled,x_only,y_only)
{
	this.enabled = enabled;
	this.x_only = (x_only)? true:false;
	this.y_only = (y_only)? true:false;
}

DD.prototype.setPosition = function(x,y)
{
	this.elm.style.left = x +'px';
	this.elm.style.top = y +'px';
	this.originalX = this.x = x;
	this.originalY = this.y = y;
}

DD.prototype.setRange = function(type,enable,min,max)
{
	if (type=='x')
	{
		this.rangeX.enabled = enable;
		if (!enable) return;
		this.rangeX.min = (min>0)? -min:min;
		this.rangeX.max = (max<0)? -max:max;
	}
	if (type=='y')
	{
		this.rangeY.enabled = enable;
		if (!enable) return;
		this.rangeY.min = (min>0)? -min:min;
		this.rangeY.max = (max<0)? -max:max;
	}
}

DD.prototype.rset = function()
{
	this.appearance(false);
	this.is_dragging = false;
	this.x = this.originalX;
	this.y = this.originalY;
	this.elm.style.left =  this.x +'px';
	this.elm.style.top = this.y +'px';
}

DD.prototype.down = function(e)
{
	DD_dragging = true;
	
	if (!e) e = event;
	this.cancelEvents(e,true);

	if (!this.enabled) return;

	this.is_dragging = true;

	this.prevX = this.x;
	this.prevY = this.y;
	
	//relative click position
	this.offsetX = e.clientX - this.x;
	this.offsetY = e.clientY - this.y;
	
	//appearance
	this.appearance(true);
	this.elm.style.zIndex = 100000;
	
	//additional code to be executed
	this.ondragstart();

	//capture doc events (so dragging with mouse oustide the actual element is possible)
	var obj = this;
	document.onmouseup = function(e) { obj.release(e) };
	document.onmousemove = function(e) { obj.drag(e) };
}
	
DD.prototype.drag = function(e)
{
	if (!e) e = event;
	this.cancelEvents(e,true);
	
	if (!this.y_only)
	{
		if (!this.rangeX.enabled) this.x = e.clientX - this.offsetX;
		else this.x = Math.max(Math.min(e.clientX-this.offsetX,this.originalX+this.rangeX.max),this.originalX+this.rangeX.min);
		//apply 
		this.elm.style.left = this.x +'px';
	}
	if (!this.x_only)
	{
		if (!this.rangeY.enabled) this.y = e.clientY - this.offsetY;
		else this.y = Math.max(Math.min(e.clientY-this.offsetY,this.originalY+this.rangeY.max),this.originalY+this.rangeY.min);
		//apply
		this.elm.style.top = this.y +'px';
	}
	
	//for execution of more code while dragging
	this.dragging(e);
}

DD.prototype.dragging = function(e)
{
	//default empty, extend in object definition
}

DD.prototype.release = function(e)
{
	DD_dragging = false;
	
	if (!e) e = event;
	//reset appearance
	this.is_dragging = false;
	this.appearance(false);
	this.elm.style.zIndex = this.z;
	//release doc events
	document.onmouseup = null;
	document.onmousemove = null;
	//other actions (object based)
	if (this.prevX==this.x && this.prevY==this.y) this.click();
	this.drop(e);
}

DD.prototype.click = function(e)
{
	//default empty, extend in object definition
}

DD.prototype.ondragstart = function()
{
	//default empty, extend in object definition
}

DD.prototype.drop = function(e)
{
	//default empty, extend in object definition
}

DD.prototype.appearance = function(dragging)
{
	//default: 50% opacity while dragging. overrule in Object definition
	var f = (dragging)? 50:100;
	if (typeof(document.body.style.filter)=='string') this.elm.style.filter = 'alpha(opacity='+f+')';
	else this.elm.style.opacity = f/100;
}

DD.prototype.cancelEvents = function(e,complete)
{
	if (typeof(e.stopPropagation)=='function') e.stopPropagation();
	e.cancelBubble = true;
	if (complete)
	{
		if (typeof(e.preventDefault)=='function') e.preventDefault();
		if (complete) e.returnValue = false;
	}
}