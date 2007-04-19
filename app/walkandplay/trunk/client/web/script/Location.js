/* usemedia.com . joes koppers . 04.2007 */
/* thnx for reading this code */


//game locations

/* locations collection */

function wpLocations(name)
{
	this.location = new Object();
	this.name = name;
	this.length = 0;
}
wpLocations.prototype.push = function(obj)
{
	this.location[obj.id] = obj;
	this.length++;
}
wpLocations.prototype.del = function(id)
{
	this.location[id].dispose();
	delete this.location[id];
	this.lenght--;
}
wpLocations.prototype.update = function()
{
	for (id in this.location) this.location[id].update();
}

/* location object */

function wpLocation(collection,id,p,type,state,name)
{
	this.collection = collection;
	this.id = id;
	this.geo = p;
	this.type = type; //->use these!
	this.state = state;
	this.name = name;
	
	this.scale = .5;
	this.w = this.scale * 40;
	this.h = this.scale * 68;
	
	//create marker
	var location = document.createElement('IMG');
		location.className = 'location';
		location.style.width = this.w +'px';
		location.style.height = this.h +'px';
		location.style.zIndex = this.z = parseInt(this.geo.lat()  * -100000);
		//location.style.border = '1px dotted lime';
		if (browser.pngsupport) location.src = 'media/icon_location+.png';
		else
		{
			location.src = 'media/blank.gif';
			location.style.filter = PNGbgImage('icon_location+.png').substr(7);
		}
		
	//add type icon ..?
	//..
	
	//event handling
	var obj = this;
	//info rollover
	var infopane = this.collection.info;
	location.onmouseover = function()
	{
		if (infopane)
		{
			infopane.setPosition(obj.x+14,obj.y-52);
			infopane.content.innerHTML = obj.name+' ('+obj.type.substring(0,1)+'='+obj.id+')';
			if (obj.collection.name=='game' && wp_game_edit)
			{
				infopane.content.innerHTML+= '<br><a href="javascript://delete_location" onclick="wp_games.game['+wp_game_edit+'].deleteLocation('+obj.id+')">del</a>';
			}
			infopane.show();
		}
	}
	location.onmouseout = function()
	{
		if (infopane) infopane.hide();
	}
	//detail view
	location.onclick = function ()
	{
		if (obj.expanded) obj.collapse()
		else obj.expand();
	}
	
		
	//create shadow
	var shadow = document.createElement('IMG');
		shadow.style.position = 'absolute';
		shadow.style.width = (this.scale*82) +'px';
		shadow.style.height = (this.scale*40) +'px';
		shadow.style.zIndex = this.z;
		//shadow.style.border = '1px dotted red';
		if (browser.pngsupport) shadow.src = 'media/icon_location_shadow.png';
		else
		{
			shadow.src = 'media/blank.gif';
			shadow.style.filter = PNGbgImage('icon_location_shadow.png').substr(7);
		}

	gmap.getPane(G_MAP_MARKER_PANE).appendChild(location);
	gmap.getPane(G_MAP_MARKER_SHADOW_PANE).appendChild(shadow);
	
	this.div = location;
	this.shadow_div = shadow;
	
	this.update();


	//->debug
	location.title = 'debug: id='+this.id+', type='+this.type;

	
	//reveal
// 	this.div.style.visibility = 'visible';
// 	this.shadow_div.style.visibility = 'visible';
	
	
	//id,owner,name,desc,intro,outro,state,t
// 	var properties = ['id','owner','name','description','intro','outro','state','creationdate','modificationdate'];
// 
// 	for (var i in properties)
// 	{
// 		this[properties[i]] = record.getField(properties[i]);
// 	}
// 	
// 	this.loaded = false;
}

wpLocation.prototype.update = function()
{
	//mapzoom based scale
	var z = gmap.getZoom();
	var s = Math.pow(3,(z/8)) / 12;
	
	this.scale = Math.max(.3,s);
	
//	tmp_debug(2,'scaling, zoom=',z,', s=',s); //Math.round(10* Math.pow(3,(z/10))));
	
	
	this.w = this.scale * 40;
	this.h = this.scale * 68;
	
	//position
	var px = gmap.fromLatLngToDivPixel(this.geo);
	this.x = px.x - this.w/2 - 5;
	this.y = px.y - this.h;
	
	//apply to elm
	this.div.style.left = this.x +'px'; 
	this.div.style.top = this.y +'px';
	this.div.style.width = this.w +'px';
	this.div.style.height = this.h +'px';

	this.shadow_div.style.left = this.x +'px';
	this.shadow_div.style.top = this.y +this.h -(this.scale*40) +'px';
	this.shadow_div.style.width = (this.scale*82) +'px';
	this.shadow_div.style.height = (this.scale*40) +'px';

}


wpLocation.prototype.expand = function()
{
	

}

wpLocation.prototype.collapse = function()
{
	

}


wpLocation.prototype.changeIcon = function(src)
{
	if (browser.pngsupport) location.src = 'media/icon_location+.png';
	else
	{
		location.src = 'media/blank.gif';
		location.style.filter = PNGbgImage('icon_location+.png').substr(7);
	}
}

wpLocation.prototype.dispose = function()
{
	gmap.getPane(G_MAP_MARKER_PANE).removeChild(this.div);
	gmap.getPane(G_MAP_MARKER_SHADOW_PANE).removeChild(this.shadow_div);
}
