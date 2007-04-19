/* usemedia.com . joes koppers . 04.2007 */
/* thnx for reading this code */


//wp game create and play

wp_add_location = false;

wp_game_selected = false;
wp_game_edit = false;


function wpCreateInit()
{
	//panes
	wpCreatePane('list_games');
	wpCreatePane('edit_game');
	wpCreatePane('edit_location');
	
	wp_add_location = new Tooltip('icon_location+.png',-17,-38,20,34);
	
	//get games for user
	wp_games = new wpGames();
	SRV.get('q-games-by-user',wpListGames,'user',wp_login.loginname);
}


function wpListGames(resp)
{
	if (wp_locations.location['new'])
	{
		if (!confirm('warning, there\'s an unsaved new location which will be deleted\n\ncontinue?')) return;
		wpCancelLocation();
	}
	if (wp_add_location && wp_add_location.enabled) wp_add_location.enable(0);

	if (panes['edit_game'].visible) panes['edit_game'].hide(1);
	
	var str = '';
		str+= '<span class="red">select a game to edit:</span><br>';

	//load games
	if (resp)
	{
		tmp_debug(3,'q-games-by-user: records=',resp.length);
		
		for (var i in resp)
		{
			wp_games.push( new wpGame(resp[i]) );
			
			var id = resp[i].getField('id');
			var name = resp[i].getField('name');
			str+= '<a href="javascript://edit_game" onclick="wp_games.game['+id+'].edit()">'+name+'</a><br>';
		}
	}
	else
	{
		for (var id in wp_games.game)
		{
			str+= '<a href="javascript://edit_game" onclick="wp_games.game['+id+'].edit()">'+wp_games.game[id].name+'</a><br>';
		}
	}
	
	panes['list_games'].setContent(str);
	panes['list_games'].show();
	
	
	//->tmp dev
	//wp_games.game[243].edit();
	
	//->view game:
	//..
	//<a href="javascript://show_game" onclick="wp_games.game['+id+'].getLocations(true)">'+name+'</a>
	
}



/* games collection */

function wpGames()
{
	this.game = new Object();
	this.length = 0;
}

wpGames.prototype.push = function(obj)
{
	this.game[obj.id] = obj;
	this.length++;
}
wpGames.prototype.del = function(id)
{
	this.game[id].dispose();
	this.lenght--;
}



/* game object */

function wpGame(record)
{
	//id,owner,name,desc,intro,outro,state,t
	var properties = ['id','owner','name','description','intro','outro','state','creationdate','modificationdate'];

	for (var i in properties)
	{
		this[properties[i]] = record.getField(properties[i]);
	}
	
	this.loaded = false;
	this.locations = new wpLocations('game');
	
	//location rollover
	var pane = new Pane('location_info',0,0,110,30,150,false,gmap.getPane(G_MAP_FLOAT_PANE));
		pane.content.onmousedown = function(e)
		{
			if (!e) e = event;
			cancelEvents(e);
		}
	this.locations.info = pane;
}

wpGame.prototype.getLocations = function(viewonly)
{
	if (viewonly) wp_game_edit = false;

	//remove edit location dialogue	
	if (wp_locations.location['new']) wpCancelLocation();
	
	wp_game_selected = this.id;
	var obj = this;
	SRV.get('q-game-locations',function(resp) { obj.updateLocations(resp) },'id',this.id);
}

wpGame.prototype.updateLocations = function(resp)
{
	tmp_debug(3,'game (',this.id,') locations: ',resp.length);
	
	var locations = resp;

	//compare with current locations
	var list = new Object();
	for (var i=0; i<locations.length; i++) list[locations[i].getField('id')] = i;

	var str = '';
	var used = new Object();

	//update/add locations
	for (var id in list)
	{
		var index = list[id];

		if (this.locations.location[id]) 
		{
			str+= 'keep>'+id+', ';
			used[id] = true;
		}
		else
		{
			//add new location

			var id = locations[index].getField('id');
			var p = new GLatLng(locations[index].getField('lat'),locations[index].getField('lon'));
			var type = locations[index].getField('type');
			var state = locations[index].getField('state');
			var name = locations[index].getField('name');
		
			this.locations.push( new wpLocation(this.locations,id,p,type,state,name) );

			used[id] = true;
			str+= 'new>'+id+', ';
		}
	}
	//remove obsolete media
	for (var id in this.locations.location)
	{
		if (used[id]) continue;
		else
		{
//			if (this.media[id].expanded) continue; //leave expanded media
			//delete media
			this.locations.del(id);
			str+= 'del>'+id+', ';
		}
	}
	
//	alert(str);

}

wpGame.prototype.edit = function()
{
	wp_game_edit = this.id;

	this.getLocations();

	panes['list_games'].hide(1);
	
	var pane = panes['edit_game'];
	var str = '';
		str+= '<span class="red" style="font-size:14px; font-weight:bold">edit</span> ';
		str+= this.name;
		
//		str+= '<br><a href="javascript://add_location" ">add location</a><br>';
		
		//add location button
		if (browser.pngsupport) str+= '<img src="media/icon_add_location.png" style="cursor:pointer; position:absolute; right:15px; top:20px;" onclick="wpAddLocation()" onmouseover="this.src=\'media/icon_add_locationX.png\'" onmouseout="this.src=\'media/icon_add_location.png\'">';
		else
		{
			str+= '<div style="cursor:pointer; right:15px; top:20px; width:38px; height:38px; '+PNGbgImage('icon_add_location.png')+'" onclick="wpAddLocation()" onmouseover="this.style.filter=PNGbgImage(\'icon_add_locationX.png\').substr(7)" onmouseout="this.style.filter=PNGbgImage(\'icon_add_location.png\').substr(7)"></div>';
		}
		
		str+= '<br>';
		str+= '<br><br><input type=button value="done" onclick="wpListGames()">';
	pane.setContent(str);
	pane.show();
}

wpGame.prototype.editLocation = function(p)
{
	//->used for adding a 'new' location, not for 'edit' at this time
	
	//add a temporary location
	wp_locations.push( new wpLocation(wp_locations,'new',p) );
	//gmap.addOverlay(new GMarker(p));

	//clear formfields
	var form = document.forms['locationform'];
	form.name.value = '';
	form.desc.value = '';
	form.answer.value = '';
	form.file.value = '';

	var px = gmap.fromLatLngToDivPixel(p);
	panes['edit_location'].setPosition(px.x,px.y);
	panes['edit_location'].show();
	
	//submit handler for form
	var obj = this;
	form.onsubmit = function() { return obj.addLocation() };

}

wpGame.prototype.addLocation = function(elm)
{
	var form = document.forms['locationform'];
	var type = form.type.value;
	var obj = this;	

	//medium uploaded?
	if (elm)
	{
		var mediumid = elm.getAttribute('id');
		
		tmp_debug(1,'uploaded medium, id=',mediumid);
		
		if (!mediumid) alert('sorry, error while uploading.\nplease try again');
		else
		{
			var p = wp_locations.location['new'].geo;
			
			//add location to game
			if (type=='task')
			{
				var name = form.name.value;
				var desc = form.desc.value;
				var answer = form.answer.value;
				var score = form.score.value;
				
				KW.WP.gameAddTask(function() { obj.getLocations() },this.id,name,desc,score,answer,mediumid,p.lng(),p.lat());
			}
			else
			{
				KW.WP.gameAddMedium(function() { obj.getLocations() },this.id,mediumid,p.lng(),p.lat());
			}
		}
	}
	else
	{
		tmp_debug(1,'adding location');

		//form checks
		var msg = ''
		if (form.name.value=='') msg = 'please add a name for this location';
		else if (form.file.value=='') msg = 'please select a mediafile for upload';
		else if (type=='task')
		{
			if (form.desc.value=='') msg = 'please add a task description';
			else if (form.answer.value=='') msg = 'please add an answer';
		}
		if (msg!='')
		{
			alert(msg);
			return false;
		}

		KW.MEDIA.upload(function(elm) { obj.addLocation(elm) },form);
	}
	
	//prevent form from submitting
	return false;
}

wpGame.prototype.deleteLocation = function(id)
{
	if (!confirm('delete location, are you sure?\nthere is no undo')) return;
	//forcehide info pane
	this.locations.info.hide(1);

	var type = this.locations.location[id].type;
	var obj = this;

	tmp_debug(2,'delete location: id=',id,', type=',type);
	
	if (type=='task') KW.WP.gameDelTask(function() { obj.getLocations() },id);
	else KW.WP.gameDelMedium(function() { obj.getLocations() },id);
}

wpGame.prototype.unLoad = function()
{
	//remove all locations
	for (var id in this.locations.location) this.locations.del(id);
}

wpGame.prototype.dispose = function()
{
	//remove locations
	this.unLoad();
}



/* create: add/edit locations */

function wpAddLocation()
{
	if (wp_locations.location['new'])
	{
		if (!confirm('warning, there\'s an unsaved new location which will be overwritten.\n\ncontinue?')) return; 
		wpCancelLocation();
	}
	
	//show tooltip and editLocation at (single) map click
	document.getElementById('map').firstChild.firstChild.style.cursor = 'crosshair';

	wp_add_location.update(0,285,205); //position of add button

	wp_add_location.enable(1);
	document.onmousemove = function(e)
	{
		if (!e) e = event;
		wp_add_location.update(e);
	}
}

function wpCancelLocation()
{
	//remove temporary location
	wp_locations.del('new');
	panes['edit_location'].hide(1);
}

function wpEditLocation(type)
{
	//change form contents
	document.getElementById('taskform').style.display = (type=='task')? 'block':'none';
	document.getElementById('mediumtype').innerHTML = (type=='task')? 'medium':type;
}
