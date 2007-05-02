/* usemedia.com . joes koppers . 04.2007 */
/* thnx for reading this code */


//wp game players (locations)

//wp_player_expanded = false;


function wpPlayers()
{
	var array = new idArray('player');
	
	//extend obj with update function
	array.update = function()
	{
		for (id in this.player) this.player[id].update();
	}
	
	return array;
}



playertest = false;

function testPlayer()
{
	if (wp_players.player[100])
	{
		//delete test player
		wp_players.del(100);
	}
	else
	{
		//add testplayer
		var p = gmap.getCenter();
		var id = '100'
		var name = 'test';
		var t = new Date().getTime();
	
		wp_players.push( playertest = new wpPlayer(wp_players,id,p,name,t) );
	}
	
	
//	playertest.animate(true);

}


/* location object */

function wpPlayer(collection,id,p,name,t)
{
	this.collection = collection;
	this.id = id;
	this.geo = p;

 	this.name = name;
 	var color = this.name.substring(0,1);
	this.icon = 'icon_player_'+color+'.png';
 	
 	this.trace = new Array(0); //geo points history
 	this.trace_color = (color=='r')? 'rgb(200,0,20)':(color=='b')? 'rgb(50,100,200)':'rgb(45,170,75)';
 	
	this.x_smoothing = .025;
	this.y_smoothing = .025;
	
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
	location.onclick = function() { alert('debug: player, id='+obj.id+', name='+obj.name) };
}

wpPlayer.prototype.updateLocation = function(p,t)
{
	if (p.equals(this.geo)) return; //no update needed
	
	//save previous and new location
	this.trace.push(this.geo);
	
	//for speed calculations
	this.prevgeo = this.geo;
	this.prevtime = this.time;
	
	this.geo = p;
	this.time = t;

	var px = gmap.fromLatLngToDivPixel(this.geo);
	this.x = px.x - this.w/2;
	this.y = px.y - this.h/2;
	
	//this.update();
	this.showTrace();
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
	//tmp_debug(1,'smooth: this.x=',this.x,' this.smoothX=',this.smoothX); 

	with (Math)
	{
		if ( round(this.smoothX)==round(this.x) && round(this.smoothY)==round(this.y) )
		{
			//tmp_querytime = tmp_debug(1,'smooth, no update','querytime');
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
}
// 

wpPlayer.prototype.animate = function(show)
{
	var obj = this;
	if (show)
	{
		this.animating = window.setInterval(function() { obj.smooth() } ,150);
		this.blink(0);
	}
	else
	{
		if (this.animating) window.clearTimeout(this.animating);
		this.blink(1);
	}
}

wpPlayer.prototype.blink = function(stop)
{
	this.div.style.visibility = (this.blinkdelay==1500)? 'hidden':'visible';

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

/*
wpPlayer.prototype.updateDetails = function(resp)
{
	var record = resp[0];
	
	this.mediumid = (this.type=='medium')? this.id:record.getField('mediumid');
	this.desc = record.getField('description');

	if (this.type=='medium')
	{
		this.mediumtype = record.getField('type');
		var title = this.mediumtype;
	}
	else
	{
		this.mediumtype = record.getField('mediumtype');
		var title = 'task';
	}
	

	var str = '';
		str+= '<a href="javascript://zoom_to" onclick="wp_games.game['+wp_game_selected+'].locations.location['+this.id+'].zoomTo()">zoom to</a><br>';
		str+= '<br><span class="title">'+title+'</span> "<b>'+this.name+'</b>"<br>';
//		str+= 'type=<b>'+this.type+'</b><br>';
//		str+= 'medium=<br>';
		str+= '<div id="medium_display" style="position:relative; margin-top:6px; width:225px; margin-bottom:2px;">';
		
		switch (this.mediumtype)
		{
			case 'text':
				str+= DH.getURL('/wp/media.srv?id='+this.mediumid);
				break;
			
			case 'image':
				str+= '<img src="/wp/media.srv?id='+this.mediumid+'&resize=225x169">';
				break;
			
			case 'video':
			case 'audio':
//				str+= '<embed id="video" src="/wp/media.srv?id='+this.mediumid+'" style="width:225px; height=168px;"></embed>';
				
				//QuickTime embed
				str+= '<OBJECT id="qtvideo" CLASSID="clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B" WIDTH="225" HEIGHT="184" CODEBASE="http://www.apple.com/qtactivex/qtplugin.cab">';
				str+= '<PARAM name="SRC" VALUE="/wp/media.srv?id='+this.mediumid+'">';
				str+= '<PARAM name="CONTROLLER" VALUE="true">';
				str+= '<PARAM name="AUTOPLAY" VALUE="true">';
				str+= '<PARAM name="BGCOLOR" VALUE="white">';
				str+= '<PARAM name="CACHE" VALUE="true">';
				str+= '<EMBED name="qtvideo" SRC="/wp/media.srv?id='+this.mediumid+'" BGCOLOR="white" WIDTH="225" HEIGHT="184" CONTROLLER="true" AUTOPLAY="true" CACHE="true" PLUGINSPAGE="http://www.apple.com/quicktime/download/">';
				str+= '</EMBED>';
				str+= '</OBJECT>';
				
				
				break;
		}

		str+= '</div>';
		if (this.desc) str+= this.desc+'<br>';
		
		
	if (this.type=='task')
	{
		str+= '<div id="taskform" style="position:relative; margin-left:-5px; padding:5px; width:225px; margin-top:6px; background-color:#d5d5d5; margin-bottom:10px;">';
		this.answer = record.getField('answer');
		this.score = record.getField('score');
		str+= 'answer: '+this.answer+'<br>';
		str+= 'score: '+this.score+' points<br>';
		str+= '</div>';
	}
	

	panes['display'].content.firstChild.innerHTML = str;

	if (this.mediumtype!='text')
	{
 		var div = document.getElementById('medium_display');
		div.style.backgroundColor = 'white';
		div.style.textAlign = 'center';
		div.style.lineHeight = '0px';
	}
	
	//tmp_debug(3,'details height:',panes['display'].content.firstChild.offsetHeight);

}


wpPlayer.prototype.collapse = function()
{
	this.expanded = false;
	wp_location_expanded = false;
	this.changeIcon(this.icon);
	
	panes['display'].hide(1);
	
	tmp_debug(2,'collapse ',this.id);
}
*/

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
