/* usemedia.com . joes koppers . 04.2007 */
/* thnx for reading this code */


//map locations

wp_location_expanded = false;

/* locations collection */

function wpLocations(name)
{
	var array = new idArray('location');
	array.name = name
	
	//extend obj
	array.update = function()
	{
		for (id in this.location) this.location[id].update();
	}
	array.center = function()
	{
		var ave_lat = 0;
		var ave_lon = 0;
		for (id in this.location)
		{
			var p = this.location[id].geo
			ave_lat += Number(p.lat());
			ave_lon += Number(p.lng());
		}
		ave_lat = ave_lat/this.length;
		ave_lon = ave_lon/this.length;
		
		gmap.panTo( new GLatLng(ave_lat,ave_lon) );
	}
	
	return array;
}

// function wpLocations(name)
// {
// 	this.location = new Object();
// 	this.name = name;
// 	this.length = 0;
// }
// wpLocations.prototype.push = function(obj)
// {
// 	this.location[obj.id] = obj;
// 	this.length++;
// }
// wpLocations.prototype.del = function(id)
// {
// 	this.location[id].dispose();
// 	delete this.location[id];
// 	this.lenght--;
// }
// wpLocations.prototype.update = function()
// {
// 	for (id in this.location) this.location[id].update();
// }

/* location object */

function wpLocation(collection,id,p,type,state,name)
{
	this.collection = collection;
	this.id = id;
	this.geo = p;
	this.type = type;
	this.state = state || 'disabled';
	this.icon = (this.type=='task')? 'icon_location_b_task_'+this.state+'.png':'icon_location_b_'+this.state+'.png';
	this.name = name;
	this.scale = .5;
	this.maxw = 40;
	this.maxh = 68;
	
	this.w = this.scale * this.maxw;
	this.h = this.scale * this.maxh;
	
	//create marker
	var location = document.createElement('IMG');
		location.className = 'location';
		location.style.width = this.w +'px';
		location.style.height = this.h +'px';
		location.style.zIndex = this.z = parseInt(this.geo.lat()  * -100000);
		//location.style.border = '1px dotted lime';
		if (browser.properpngsupport) location.src = 'media/'+this.icon;
		else
		{
			location.src = 'media/blank.gif';
			location.style.filter = PNGbgImage(this.icon).substr(7);
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
			var str = '';
				str+= '"<b>'+obj.name+'</b>"<br>'; // ('+obj.type.substring(0,1)+'='+obj.id+')<br>';
				if (obj.state=='enabled') str+= '<a href="javascript://view" onclick="wp_games.game['+wp_game_selected+'].locations.location['+obj.id+'].expand()">view</a>&nbsp;';
				if (gmap.getZoom()<17) str+= '<a href="javascript://zoom_to" onclick="wp_games.game['+wp_game_selected+'].locations.location['+obj.id+'].zoomTo()">zoom to</a>&nbsp;';
				//delete button
				//if (obj.collection.name=='game' && wp_mode=='create')
				if (wp_mode=='create')
				{
					str+= ' <a href="javascript://delete_location" onclick="wp_games.game['+wp_game_edit+'].deleteLocation('+obj.id+')" class="red">delete</a>';
				}
			infopane.content.innerHTML = str;
			infopane.setPosition(obj.x+12,obj.y-46);
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
		if (browser.properpngsupport) shadow.src = 'media/icon_location_shadow.png';
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
	
	
	this.w = this.scale * this.maxw;
	this.h = this.scale * this.maxh;
	
	//position
	var px = gmap.fromLatLngToDivPixel(this.geo);
	this.x = px.x - this.w/1.4;
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

wpLocation.prototype.enable = function(enable,expand)
{
	this.state = (enable)? 'enabled':'disabled';
	//update icon src
	this.icon = (this.type=='task')? 'icon_location_b_task_'+this.state+'.png':'icon_location_b_'+this.state+'.png';
	this.changeIcon(this.icon);
	
	if (expand) this.expand();
}

wpLocation.prototype.zoomTo = function()
{
	if (this.collection.info) this.collection.info.hide(1);
	gmap.setCenter(this.geo,17);
}

wpLocation.prototype.expand = function()
{
	if (this.state!='enabled')
	{
		alert(this.type+' not hit yet!');
		return;
	}
	
	if (this.collection.info) this.collection.info.hide(1);
	if (wp_location_expanded && wp_location_expanded.id!=this.id) wp_location_expanded.collapse();
	
	wp_location_expanded = this;
	this.expanded = true;
	this.changeIcon('icon_location_r.png');
	
	panes['display'].show();

	//get location info
	var obj = this;
	SRV.get('q-'+this.type,function(resp) { obj.updateDetails(resp) },'id',this.id);

	tmp_debug(2,'expand ',this.id);
}

wpLocation.prototype.updateDetails = function(resp)
{
	if (!resp)
	{
		//only update answer contents
		var str = '';
		if (this.play_answerstate=='open') str+= '- no answer given yet -';
		else
		{
			str+= 'last answer: '+this.play_answer+'<br>';
			//if (this.play_score==0) str+= 'task not complete yet';
			if (this.play_answerstate=='notok') str+= '<span class="red">wrong answer!</span>';
			else str+= 'score: '+this.play_score;
		}
		document.getElementById('display_answer').innerHTML = str;
		
		return;
	}
	
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
		str+= '<a style="float:right; margin-right:2px;" href="javascript://close" onclick="wp_location_expanded.collapse()">close</a>';
		str+= '<a href="javascript://zoom_to" onclick="wp_games.game['+wp_game_selected+'].locations.location['+this.id+'].zoomTo()">zoom to</a><br>';
		str+= '<br><span class="title">'+title+'</span> "<b>'+this.name+'</b>"<br>';
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
				str+= '<param name="movie" value="/wp/media.srv?id='+this.mediumid+'&format=swf&resize=225x169" />';
				str+= '<param name="quality" value="high" />';
				str+= '<param name="bgcolor" value="#ffffff" />';
				str+= '<embed src="/wp/media.srv?id='+this.mediumid+'&format=swf&resize=225x169" quality="high" bgcolor="#ffffff" width="225" height="169" name="world" type="application/x-shockwave-flash" pluginspage="http://www.macromedia.com/go/getflashplayer" />';
				str+= '</object>';

				break;
		}

		str+= '</div>';
		if (this.desc) str+= this.desc+'<br>';
		
	//create
	if (this.type=='task' && wp_mode=='create')
	{
		//show answer and score
		str+= '<div id="taskform" style="position:relative; margin-left:-5px; padding:5px; width:225px; margin-top:6px; background-color:#d5d5d5; margin-bottom:10px;">';
		this.answer = record.getField('answer');
		this.score = record.getField('score');
		str+= 'answer: '+this.answer+'<br>';
		str+= 'score: '+this.score+' points<br>';
		str+= '</div>';
	}
	
	//play
	if (this.type=='task' && wp_mode=='play')
	{
		//add answer pane (updated by gamestate and live events)
		str+= '<div id="display_answer" style="position:relative; margin-left:-5px; padding:5px; width:225px; margin-top:6px; background-color:#d5d5d5; margin-bottom:10px;">';
		if (this.play_answerstate=='open') str+= '- no answer given yet -';
		else
		{
			str+= 'last answer: '+this.play_answer+'<br>';
			//if (this.play_score==0) str+= 'task not complete yet';
			if (this.play_answerstate=='notok') str+= '<span class="red">wrong answer!</span>';
			else str+= 'score: '+this.play_score;
		}
		str+= '</div>';
	}

// 	if (this.type=='task' && wp_mode=='create')
// 	{
// 		str+= '<div id="taskform" style="position:relative; margin-left:-5px; padding:5px; width:225px; margin-top:6px; background-color:#d5d5d5; margin-bottom:10px;">';
// 		this.answer = record.getField('answer');
// 		this.score = record.getField('score');
// 		str+= 'answer: '+this.answer+'<br>';
// 		str+= 'score: '+this.score+' points<br>';
// 		str+= '</div>';
// 	}


	panes['display'].content.firstChild.innerHTML = str;

	//center medium
	if (this.mediumtype!='text')
	{
 		var div = document.getElementById('medium_display');
		div.style.backgroundColor = 'white';
		div.style.textAlign = 'center';
		div.style.lineHeight = '0px';
	}
}

wpLocation.prototype.updateAnswer = function(answerstate,answer,score)
{
	if (answerstate!='scoreupdate')
	{
		this.play_answerstate = answerstate;
		this.play_answer = answer || '';
	}
	this.play_score = score || '';

	if (this.expanded) this.updateDetails();
}

wpLocation.prototype.collapse = function()
{
	panes['display'].content.firstChild.innerHTML = '';
	panes['display'].hide(1);
	
	this.expanded = false;
	wp_location_expanded = false;
	this.changeIcon(this.icon);
	
	tmp_debug(1,'collapse ',this.id);
}

wpLocation.prototype.changeIcon = function(src)
{
	if (browser.properpngsupport) this.div.src = 'media/'+src;
	else
	{
		this.div.style.filter = PNGbgImage(src).substr(7);
	}
}

wpLocation.prototype.dispose = function()
{
	gmap.getPane(G_MAP_MARKER_PANE).removeChild(this.div);
	gmap.getPane(G_MAP_MARKER_SHADOW_PANE).removeChild(this.shadow_div);
}