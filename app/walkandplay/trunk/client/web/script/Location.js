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
// 		var ave_lat = 0;
// 		var ave_lon = 0;
// 		for (id in this.location)
// 		{
// 			var p = this.location[id].geo
// 			ave_lat += Number(p.lat());
// 			ave_lon += Number(p.lng());
// 		}
// 		ave_lat = ave_lat/this.length;
// 		ave_lon = ave_lon/this.length;
// 		
// 		gmap.panTo( new GLatLng(ave_lat,ave_lon) );

		//gmap center and zoom method
		var locations = [];
		for (id in this.location) locations.push(this.location[id].geo);
		
		var bounds = new GPolyline(locations).getBounds();
		var zoom = gmap.getBoundsZoomLevel(bounds);
		zoom--;
		gmap.setCenter(bounds.getCenter(),zoom);

	}
	
	return array;
}


/* location object */

function wpLocation(collection,id,p,type,state,name)
{
	this.collection = collection;
	this.id = id;
	this.prevgeo = this.geo = p;
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
		//location.style.border = '1px dotted red';
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
		if (obj.editing) return;
		
		if (infopane)
		{
			var str = '';
				str+= '"<b>'+obj.name+'</b>"<br>'; // ('+obj.type.substring(0,1)+'='+obj.id+')<br>';
				if (obj.state=='enabled') str+= '<a href="javascript://view" onclick="wp_games.game['+wp_game_selected+'].locations.location['+obj.id+'].expand()">view</a>&nbsp;';
				if (gmap.getZoom()<17) str+= '<a href="javascript://zoom_to" onclick="wp_games.game['+wp_game_selected+'].locations.location['+obj.id+'].zoomTo()">zoom to</a>&nbsp;';
				//delete button
				//if (obj.collection.name=='game' && wp_mode=='create')
				if (wp_mode=='create' && wp_games.game[wp_selected_game].state!=2)
				{
					//str+= ' <a href="javascript://edit_location" onclick="wp_games.game['+wp_game_edit+'].locations.location['+obj.id+'].edit()" class="red">edit</a> ';
					str+= ' <a href="javascript://edit_location" onclick="wp_games.game['+wp_game_edit+'].editLocation('+obj.id+')" style="color:rgb(200,0,20)">edit</a> ';
					str+= ' <a href="javascript://delete_location" onclick="wp_games.game['+wp_game_edit+'].deleteLocation('+obj.id+')" style="color:rgb(200,0,20)">delete</a>';
				}
			infopane.content.innerHTML = str+'<br><br>';
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
		if (obj.editing) return;
		
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
	


	//make location dragable (??)
	this.drag = makeDragableItem(location);
	this.drag.dragging = function(e)
	{
		//update location object
		obj.x = this.x;
		obj.y = this.y;
		//move shadow along
		obj.shadow_div.style.left = obj.x +'px';
		obj.shadow_div.style.top = obj.y +obj.h -(obj.scale*40) +'px';
		//move edit pane along
		panes['edit_location'].setPosition(obj.x + obj.w/1.4, obj.y + obj.h);
		
// 		var x = obj.x - obj.w/1.4;
// 		var y = obj.y - obj.h;
// 		
// //		panes[''].


	}
	this.drag.drop = function()
	{
		//update location geo
		//obj.geo = new GlatLng()
		
		var p = gmap.fromDivPixelToLatLng(new GPoint(obj.x + obj.w/1.4,obj.y + obj.h)) //-> get correct offset here!
		obj.geo = p;
		//var px = gmap.fromLatLngToDivPixel(this.geo);
		
		
		
		tmp_debug(2,'drop location, geo=',obj.geo);
	}
	this.drag.setEnabled(false);
		
	this.update();

	//->debugging info
	location.title = 'debug: id='+this.id+', type='+this.type;

	
	//default state in play
	this.play_answerstate = 'open';
	this.play_answer = '';
	this.play_answer_medium = false;
	this.play_answer_mediumtype = '';
	this.play_score = '';
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
	
	//update drag-drop elm
	this.drag.x = this.x;
	this.drag.y = this.y;
	
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
	
	if (this.state=='disabled')
	{
		//reset playstate
		this.play_answerstate = 'open';
		this.play_answer = '';
		this.play_answer_medium = false;
		this.play_answer_mediumtype = '';
		this.play_score = '';
	}
	
	if (expand)
	{
		if (wp_location_expanded && wp_location_expanded.id==this.id) return;
		else this.expand();
	}
}

wpLocation.prototype.zoomTo = function()
{
	if (this.collection.info) this.collection.info.hide(1);
	gmap.setCenter(this.geo,17);
}

wpLocation.prototype.refresh = function(name)
{
	/*	update name of location
		and refresh display (if open)
	*/
	
	this.name = name;
	if (this.expanded) this.expand();
	
	if (panes['list_locations'] && panes['list_locations'].visible) wp_games.game[wp_selected_game].listLocations();
	
	//update prev geo to current
	this.prevgeo = this.geo;
	this.editing = false;
	
	wp_selected_location = false;
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



wpLocation.prototype.updateContents = function(elm)
{
	var form = document.forms['locationform'];
	var type = form.type.value;
	var obj = this;	

	//validate form
	var msg = '';
	if (form.name.value=='') msg = 'please add a name for this location';
	//else if (type!='text' && form.file.value=='') msg = 'please select a mediafile for upload';
	else if (type=='task')
	{
		if (form.desc.value=='') msg = 'please add a task description';
		else if (form.answer.value=='') msg = 'please add an answer';
	}
	else if (type=='text')
	{
		if (form.text.value=='') msg = 'please add a text';
	}
	if (msg!='')
	{
		alert(msg);
		return false;
	}

	//is there a medium to upload?
	if (form.file.value!='' && !elm)
	{
		KW.MEDIA.upload(function(elm) { obj.updateContents(elm) },form);
		return false;
	}
	
	//new medium is uploaded (or failed)
	if (elm)
	{
		var mediumid = elm.getAttribute('id');
		tmp_debug(1,'uploaded new medium, id=',mediumid);

		if (!mediumid)
		{
			alert('sorry, error while uploading.\nplease try again');
			return false;
		}
	}
	else var mediumid = null;
	
	
	//apply update
	var name = form.name.value;
	var desc = form.desc.value;
	
	switch (type)
	{
		case 'task':
			var answer = form.answer.value;
			var score = form.score.value;
			KW.WP.gameUpdateTask(function() { wp_games.game[wp_selected_game].getLocations() },this.id,name,desc,score,answer,mediumid);
			break;
		
		case 'medium':
			KW.WP.gameUpdateMedium(function() { wp_games.game[wp_selected_game].getLocations() },this.id,mediumid,name,desc);
			break;
			
		case 'text':
			var text = form.text.value;
			KW.WP.gameUpdateTextMedium(function() { wp_games.game[wp_selected_game].getLocations() },mediumid,text,name,desc);
			break;
	}
	
	return false;
}

wpLocation.prototype.updateDetails = function(resp)
{
	if (resp)
	{
		//clear first;
		panes['display'].setContent('<div id="media_display" style="width:228px; margin-bottom:5px"></div>');	
	
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
		
		//display pane contents
		var str = '';
			str+= '<a style="float:right; margin-right:2px;" href="javascript://close" onclick="wp_location_expanded.collapse()">close</a>';
			str+= '<a href="javascript://zoom_to" onclick="wp_games.game['+wp_game_selected+'].locations.location['+this.id+'].zoomTo();this.blur()">zoom to</a><br>';
			str+= '<br><span class="title">'+title+'</span> "<b>'+this.name+'</b>"<br>';
			str+= '<div id="display_medium" style="position:relative; margin-top:6px; width:225px; margin-bottom:2px;">';
			str+= wpEmbedMedium(this.mediumtype,this.mediumid);
			str+= '</div>';
			if (this.desc) str+= this.desc+'<br>';
			
//			str+= 'answrstate:'+this.play_answerstate;
			
			//answer button for tasks in play-mode
			if (this.type=='task' && wp_mode=='play')
			{
				var display = (this.play_answerstate=='notok' || this.play_answerstate=='open')? 'block':'none';
				str+= '<a id="add_answer" style="display:'+display+'; color:rgb(200,0,20); float:right" href="javascript://add_answer" onclick="panes.show(\'answer\');this.blur()">add answer</a><br>';
			}
			
		//create
		if (this.type=='task' && wp_mode=='create')
		{
			//show answer and score
			str+= '<div style="position:relative; margin-left:-5px; padding:5px; width:225px; margin-top:6px; margin-bottom:10px;" class="setbg">';
			this.answer = record.getField('answer');
			this.score = record.getField('score');
			str+= 'answer: '+this.answer+'<br>';
			str+= 'score: '+this.score+' points<br>';
			str+= '</div>';
		}
	}
	
	//play and viewmode
	if (this.type=='task' && wp_mode!='create')
	{
		//add answer pane (updated by gamestate and live events)
		if (resp) str+= '<div id="display_answer" style="position:relative; margin-left:-5px; padding:5px; width:225px; margin-top:6px; margin-bottom:10px;" class="setbg">';
		else var str='';
		
		//update answer
		if (this.play_answerstate=='open') str+= '- no answer given yet -<br>';
		else
		{
			str+= '<span class="title">score:</span> ';
			if (this.play_answerstate=='notok') str+= '<span class="red">wrong answer!</span><br>';
			else  str+= '<b>'+this.play_score+'</b><br>';
			str+= 'last answer: "'+this.play_answer+'"<br>';
		}
		//medium
		if (this.play_answer_medium)
		{
			str+= 'last answer medium:<br>';
			str+= '<div id="display_answer_medium" style="position:relative; margin-top:6px; width:225px; margin-bottom:2px;">';
			str+= wpEmbedMedium(this.play_answer_mediumtype,this.play_answer_medium);
			str+= '</div>';
		}
		str+= '</div>';
	}

	if (!resp) //only update answer, not entire pane
	{
		if (document.getElementById('display_answer'))
		{
			document.getElementById('display_answer').innerHTML = str;
			
			//show/hide 'add answer' button
			document.getElementById('add_answer').style.display = (this.play_answerstate=='notok' || this.play_answerstate=='open')? 'block':'none';
		}
	}
	else
	{	
		//update pane
		panes['display'].content.firstChild.innerHTML = str;
		//center medium
		if (this.mediumtype!='text')
		{
			var div = document.getElementById('display_medium');
			div.style.backgroundColor = 'white';
			div.style.textAlign = 'center';
			div.style.lineHeight = '0px';
		}
	}
}

wpLocation.prototype.answer = function(form)
{
	var answer = form.answer.value;
	
	tmp_debug(1,'add answer, task=',this.id,', answer=',answer);
	
	if (answer=='')
	{
		alert('you didn\'t insert an answer!');
		return;
	}
	
	//reset form
	form.answer.value = '';
	panes.hide('answer');

	//submit answer (<play-answertask-req id="[taskid]" answer="blabla" />)
	var req = KW.createRequest('play-answertask-req');
		req.documentElement.setAttribute('id',this.id);
		req.documentElement.setAttribute('answer',answer);

	KW.utopia(req,null);
}

wpLocation.prototype.updateAnswer = function(answerstate,answer,medium,score)
{
	if (answerstate!='score-update' && answerstate!='medium-add')
	{
		this.play_answerstate = answerstate;
		this.play_answer = answer || '';
	}
	this.play_score = score || '<em style="font-weight:normal">waiting for medium upload</em>';

	if (answerstate!='score-update' && medium)
	{
		this.play_answer_medium = medium;
		var obj = this;
		var f = function(resp) //->SRV doesn't seem to work in synchronous mode!
		{
			obj.play_answer_mediumtype = resp[0].getField('kind');
			if (obj.expanded) obj.updateDetails();
		}
		SRV.get('q-medium-info',f,'id',this.play_answer_medium);
	}
	else if (this.expanded) this.updateDetails();
}

wpLocation.prototype.collapse = function()
{
	panes.hide('display');
	
	this.expanded = false;
	wp_location_expanded = false;
	this.changeIcon(this.icon);
	
	tmp_debug(1,'collapse ',this.id);

//	panes['display'].setContent('');

//	panes['display'].hide(1);
	//panes['display'].content.firstChild.innerHTML = '';

}

wpLocation.prototype.changeIcon = function(src)
{
	if (browser.properpngsupport) this.div.src = 'media/'+src;
	else
	{
		this.div.style.filter = PNGbgImage(src).substr(7);
	}
}

// wpLocation.prototype.edit = function()
// {
// 	//edit location task or medium
// 	alert('edit location id='+this.id);
// 	
// }

wpLocation.prototype.dispose = function()
{
	gmap.getPane(G_MAP_MARKER_PANE).removeChild(this.div);
	gmap.getPane(G_MAP_MARKER_SHADOW_PANE).removeChild(this.shadow_div);
}