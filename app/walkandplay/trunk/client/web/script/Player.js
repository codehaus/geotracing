/* usemedia.com . joes koppers . 04.2007 */
/* thnx for reading this code */


//wp game players (locations)

function wpPlayers()
{
	var array = new idArray('player');
	
	//extend obj with update function
	array.update = function()
	{
		for (id in this.player) this.player[id].update();
	}
	
	//start reality checking (there's no server side logout for users -> client-side timeout after 2 mins no user-move)
	//var obj = this;
	//this.check = window.setTimeout(function() { obj.realityCheck() },60000*2);
	
	return array;
}


wpPlayers.prototype.realityCheck = function()
{
	/*	timeout (live) players
	*/

	var t,expired;
	t = new Date().getTime();
 	expired = new Array(0);

	 var str = '';

	//check live users
	for (var name in this.user) 
	{
		if ((t-this.user[name].timestamp)/1000 > ii_user_livetimeout) expired.push(name);
 		str+= ', '+name+':time='+this.user[name].time+', ts='+this.user[name].timestamp+', diff='+((this.user[name].time-this.user[name].timestamp)/1000)+', check='+((t-this.user[name].timestamp)/1000);
	}
	//kill live users
//	for (var id in wp_players.player) wp_players.del(id);
	for (var i in expired) wp_players.del(expired[i]);
	
//debug
// var expired_live = expired.length; 
}



/* location object */

function wpPlayer(collection,id,p,name,t,playid)
{
	this.collection = collection;
	this.id = id;
	this.geo = p;

 	this.name = name;
 	
 	//get color (from gameplay id)
 	var gameplay = SRV.get('q-gameplay', null,'id',playid);
 	this.color = gameplay[0].getField('color');
 	
//  	var color = this.name.substring(0,1);
//  	if (color=='j') color = 'p'; //->just and joes are purple for now..
	this.icon = 'icon_player_'+this.color.substring(0,1)+'.png';
 	
 	this.trace = new Array(0); //geo points history
 	if (wp_mode=='view') this.trace.push(p);
	var c;
	switch (this.color.substring(0,1))
	{
		case 'r': c = '#c80014'; break;
		case 'g': c = '#2daa4b'; break;
		case 'b': c = '#3264c8'; break;
		case 'y': c = '#ffd02b'; break;
		case 'o': c = '#df8021'; break;
		case 'p': c = '#8d2bcb'; break;
	}
 	this.trace_color = c; //(color=='r')? '#c80014':(color=='b')? '#3264c8':'#ffd02b'; //'#2daa4b';

	//animation settings 	
// 	this.x_smoothing = .03;
// 	this.y_smoothing = .03;
	
	this.x_smoothing = .1;
	this.y_smoothing = .1;

	this.smooth_timeout = 150;
	this.blinkdelay = 1500;
	
	this.scale = .5; //tmp value, obj is updated right after creation
 	this.maxw = 25;
 	this.maxh = 25;
	this.w = this.scale * this.maxw;
	this.h = this.scale * this.maxh;
	
	//create marker
	var location = document.createElement('IMG');
		location.className = 'location';
		location.style.width = this.w +'px';
		location.style.height = this.h +'px';
		//location.style.zIndex = this.z = parseInt(this.geo.lat()  * -100000);
		//location.style.border = '1px dotted lime';
		if (browser.properpngsupport) location.src = 'media/'+this.icon;
		else
		{
			location.src = 'media/blank.gif';
			location.style.filter = PNGbgImage(this.icon).substr(7);
		}
		
	//add type icon ..?
	//..

/*	
	//event handling
	var obj = this;
	//info rollover
	var infopane = this.collection.info;
	location.onmouseover = function()
	{
		if (infopane)
		{
			var str = '';
				str+= '"<b>'+obj.name+'</b>"<br>'; // ('+obj.type.substring(0,1)+'='+obj.id+')<br>';
				str+= '<a href="javascript://view" onclick="wp_games.game['+wp_game_selected+'].locations.location['+obj.id+'].expand()">view</a>&nbsp;';
				if (gmap.getZoom()<17) str+= '<a href="javascript://zoom_to" onclick="wp_games.game['+wp_game_selected+'].locations.location['+obj.id+'].zoomTo()">zoom to</a>&nbsp;';
				//delete button
				if (obj.collection.name=='game' && wp_game_edit)
				{
					str+= ' <a href="javascript://delete_location" onclick="wp_games.game['+wp_game_edit+'].deletePlayer('+obj.id+')" class="red">delete</a>';
				}
			infopane.content.innerHTML = str;
			infopane.setPosition(obj.x+14,obj.y-52);
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
	
*/		

	this.animating = false;
	

	gmap.getPane(G_MAP_MARKER_PANE).appendChild(location);
	
	this.div = location;
	
	this.update();

	this.animate(true);

	//->debug
	var obj = this;
	location.onclick = function() { alert('debug: player, id='+obj.id+', name='+obj.name+', color='+obj.color) };
}

wpPlayer.prototype.updateLocation = function(p,t)
{
	if (p.equals(this.geo)) return; //no update needed
	
	//save previous and new location
	if (wp_mode=='view') this.trace.push(p); //smoothing disabled in view mode, display current geo in trace
	else this.trace.push(this.geo);
	
	//for speed calculations
	this.prevgeo = this.geo;
	this.prevtime = this.time;
	
	this.geo = p;
	this.time = t;

	var px = gmap.fromLatLngToDivPixel(this.geo);
	this.x = px.x - this.w/2;
	this.y = px.y - this.h/2;
	
	//disable smoothing in view mode, for now.
	if (wp_mode=='view') this.update();
	else
	{
		this.showTrace();

		//restart smooth
		var obj = this;
		if (!this.animating) this.animating = window.setTimeout(function() { obj.smooth() },this.smooth_timeout);
	}
}

wpPlayer.prototype.update = function(p,t)
{
	//mapzoom based scale
	var z = gmap.getZoom();
	var s = Math.pow(3,(z/8)) / 12;
	
	this.scale = Math.max(.3,s);
	
//	tmp_debug(2,'scaling, zoom=',z,', s=',s); //Math.round(10* Math.pow(3,(z/10))));
	
	this.w = this.scale * this.maxw;
	this.h = this.scale * this.maxh;
	
	//position
	var px = gmap.fromLatLngToDivPixel(this.geo);

	this.smoothX = this.x = px.x - this.w/2;
	this.smoothY = this.y = px.y - this.h/2;

	//apply to elm
	this.div.style.left = this.x +'px'; 
	this.div.style.top = this.y +'px';
	this.div.style.width = this.w +'px';
	this.div.style.height = this.h +'px';
	
	this.showTrace();
}

wpPlayer.prototype.smooth = function(obj)
{
	with (Math)
	{
		if ( round(this.smoothX)==round(this.x) && round(this.smoothY)==round(this.y) )
		{
			//no update needed, pause smooth interval
			this.animating = false;
			return;
		}
	}

	//smooth by moving slowly to current value
    this.smoothX += this.x_smoothing * (this.x - this.smoothX);
    this.smoothY += this.y_smoothing * (this.y - this.smoothY);
	//apply to icon div
	this.div.style.left = Math.round(this.smoothX) +"px"; 
	this.div.style.top = Math.round(this.smoothY) +"px";

	//icon info + shadow
// 	this.infodiv.style.left = Math.round(this.smoothX) + (this.w/2) -65 +"px"; 
// 	this.infodiv.style.top = Math.round(this.smoothY) + (this.h/2) -126 +"px";
// 	this.infoshadowdiv.style.left = Math.round(this.smoothX) + (this.w/2) -37 +"px"; 
// 	this.infoshadowdiv.style.top = Math.round(this.smoothY) + (this.h/2) -61 +"px";

	//interval
	var obj = this;
	this.animating = window.setTimeout(function() { obj.smooth() },this.smooth_timeout);
}

wpPlayer.prototype.animate = function(show)
{
	var obj = this;
	if (show)
	{
		//this.animating = window.setInterval(function() { obj.smooth() } ,150);
		if (wp_mode!='view') this.animating = window.setTimeout(function() { obj.smooth() },this.smooth_timeout);
		this.blink(0);
	}
	else
	{
		//if (this.animating) window.clearInterval(this.animating);
		if (this.animating) window.clearTimeout(this.animating);
		this.blink(1);
	}
}

wpPlayer.prototype.blink = function(stop)
{
	//if (wp_mode=='view' && wp_viewmode=='archived') return; //player icons don't blink in playback (archived view)

	this.div.style.visibility = (this.blinkdelay==500 || stop)? 'visible':'hidden';

	//show is longer than hide
	this.blinkdelay = (this.blinkdelay==1500)? 500:1500;

	var obj = this;
	if (!stop) this.blinking = window.setTimeout(function() { obj.blink() },this.blinkdelay);
	else if (this.blinking) window.clearTimeout(this.blinking);
}

wpPlayer.prototype.speed = function()
{
	return (this.geo.distanceFrom(this.prevgeo) / 1000) / ((this.time - this.prevtime) / 3600000);
}

wpPlayer.prototype.showTrace = function()
{
	if (this.trace.length<2) return; //we need two points minimum
	//remove current trace
 	if (this.traceOverlay) gmap.removeOverlay(this.traceOverlay);
// 	if (!wp_live_traces) return;
 	//width adjusted to mapview
 	var w = gmap.getZoom()/4;
 	w = (w>4)? Math.round(w):Math.floor(w);
 	//only show last 100 points (for performance)
 	var trace = (this.trace.length>wp_max_livetrace+100)? this.trace.slice(this.trace.length-wp_max_livetrace):this.trace;
 	gmap.removeOverlay(this.traceOverlay);
 	this.traceOverlay = new GPolyline(trace,this.trace_color,Math.max(1,w),0.8);
	//add new trace
 	gmap.addOverlay(this.traceOverlay);
}

wpPlayer.prototype.zoomTo = function()
{
	if (this.collection.info) this.collection.info.hide(1);
	gmap.setCenter(this.geo,17);
}

wpPlayer.prototype.expand = function()
{
	if (this.collection.info) this.collection.info.hide(1);
	if (wp_location_expanded) wp_location_expanded.collapse();
	
	wp_location_expanded = this;
	this.expanded = true;
	this.changeIcon('icon_location_r.png');
	
	panes['display'].show();

	//get location info
	var obj = this;
	SRV.get('q-'+this.type,function(resp) { obj.updateDetails(resp) },'id',this.id);
	SRV.get('q-'+this.type,obj.updateDetails,'id',this.id);
	

	tmp_debug(2,'expand ',this.id);
}

wpPlayer.prototype.changeIcon = function(src)
{
	if (browser.properpngsupport) this.div.src = 'media/'+src;
	else
	{
		this.div.style.filter = PNGbgImage(src).substr(7);
	}
}

wpPlayer.prototype.dispose = function()
{
	if (this.animating) window.clearInterval(this.animating);
	if (this.blinking) window.clearTimeout(this.blinking);
	
	if (this.traceOverlay) gmap.removeOverlay(this.traceOverlay);

	gmap.getPane(G_MAP_MARKER_PANE).removeChild(this.div);
}
