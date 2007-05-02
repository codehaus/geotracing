/* usemedia.com . joes koppers . 04.2007 */
/* thnx for reading this code */


//wp game create and play

wp_add_location = false;

wp_game_selected = false;
wp_game_edit = false;

wp_selected_game = false;
wp_selected_round = false;
wp_selected_play = false;


function wpGameInit()
{
	//panes
	wpCreatePane('list_games');
	wpCreatePane('list_locations');
	wpCreatePane('edit_game');
	wpCreatePane('edit_location');
	wpCreatePane('play');

	//game collections	
	wp_games = new idArray('game');
	wp_rounds = new idArray('round');
	wp_plays = new idArray('play');
	wp_players = new wpPlayers();
}

function wpListGames(resp)
{
	var list = ''; 
	if (resp)
	{
		tmp_debug(3,'q-play-state-by-user: records=',resp.length);
		
		for (var i in resp)
		{
			//wp_games.push( new wpGame(resp[i]) );
			
			var name = resp[i].getField('name');
			var desc = resp[i].getField('description');
			
			
			if (wp_mode=='play')
			{
				var id = resp[i].getField('gameid');
				var roundid = resp[i].getField('roundid');
				var playid = resp[i].getField('gameplayid');
				
				list+= '<a href="javascript://play_game" onclick="wpSelectGame(\'play\','+id+','+roundid+','+playid+')" title="'+desc+'">'+name+'</a><br>';
			}
			else if (wp_mode=='create')
			{
				var id = resp[i].getField('id');
				list+= '<a href="javascript://edit_game" onclick="wpSelectGame(\'create\','+id+')" title="'+desc+'">'+name+'</a><br>';
			}
		}
	}
// 	else
// 	{
// 		for (var id in wp_games.game)
// 		{
// 			str+= '<a href="javascript://edit_game" onclick="wp_games.game['+id+'].edit()">'+wp_games.game[id].name+'</a><br>';
// 		}
// 	}

	var header = (wp_mode=='create')? '<span class="red">select a game to edit:</span><br>':'<span class="red">select a game to play:</span><br>';
	if (list=='') list = '- no games available -';

//str+= '<br><br>';
//str+= '<input type="button" value="player test" onclick="testPlayer()"><br><br>';
//str+= '<input type="button" value="game state test" onclick="playGetTeamResult()"><br>';

	panes['list_games'].setContent(header+list);
	panes['list_games'].show();
}

function wpSelectGame(type,id,roundid,playid)
{
	wp_selected_game = id || wp_selected_game;
	wp_selected_round = roundid || wp_selected_round;
	wp_selected_play = playid || wp_selected_play;

	//create new if needed, will return to this function
	if (!wp_games.game[id])
	{
		SRV.get('q-game',wpLoadGame,'id',id);
		return;
	}

	var game = wp_games.game[id];

	switch (type)
	{
		case 'play':
			game.getLocations(true);
			
			//add gameround and play
			if (!wp_rounds[wp_selected_round]) wp_rounds.push( new wpRound(wp_selected_round) );
			if (!wp_plays[wp_selected_play]) wp_plays.push( new wpRound(wp_selected_play) );

			//start events listener
			PL.joinListen('/wp/round/'+wp_selected_round+',/wp/play/'+wp_selected_play);
			onData = wpLive;
	
			//get current gamestate
			KW.WP.playGetGamePlay(function(resp) { wpUpdatePlay(resp,wp_selected_play) },wp_selected_play);
			
			//close list, open play display
			panes.hide('list_games');
			panes.show('play');
			break;
			
		case 'create':
			wp_games.game[id].edit();
			break;
			
		case 'view':
			break;
			
		default:
			break;
	}
}

function wpLoadGame(resp)
{
	//new game
	wp_games.push ( new wpGame(resp[0]) );
	
	//return to select	
	wpSelectGame(wp_mode,wp_selected_game);
}


/* create functions */

function wpAddLocation()
{
	if (wp_locations.location['new']) wpCancelLocation(true);
	
	//show tooltip and editLocation at (single) map click
	document.getElementById('map').firstChild.firstChild.style.cursor = 'crosshair';

	wp_add_location.update(0,285,199); //position of add button

	wp_add_location.enable(1);
	document.onmousemove = function(e)
	{
		if (!e) e = event;
		wp_add_location.update(e);
	}
}

function wpCancelLocation(check)
{
	if (check)
	{
		if (!confirm('warning, there\'s an unsaved new location which will be deleted\n\ncontinue?')) return true;
	}
	
	//remove temporary location
	wp_locations.del('new');
	panes['edit_location'].hide(1);
	//wp_add_location.enable(0);
}

function wpEditLocation(type)
{
	//change form contents
	document.getElementById('taskform').style.display = (type=='task')? 'block':'none';
	document.getElementById('textform').style.display = (type=='text')? 'block':'none';
	document.getElementById('mediumform').style.display = (type=='text')? 'none':'block';
	document.getElementById('mediumdescform').style.display = (type=='medium')? 'block':'none';
	//document.getElementById('mediumtype').innerHTML = (type=='task')? 'medium:':type+':';
}


/* play functions */

function wpUpdatePlay(resp,id)
{
	var tasks = resp.getElementsByTagName('task-result');
	var media = resp.getElementsByTagName('medium-result');
	
	var tasksdone = 0;
	var mediadone = 0;

	//update locations, count state	
 	for (var i=0; i<tasks.length; i++)
 	{
 		var id = tasks[i].getAttribute('taskid');
 		var enabled = (tasks[i].getAttribute('state')!='open');
 		var answerstate = tasks[i].getAttribute('answerstate');

 		if (enabled) tasksdone++;
		wp_games.game[wp_selected_game].locations.location[id].enable(enabled);
		
		var lastanswer,score;
		
		if (answerstate!='open')
		{
			var lastanswer = tasks[i].getAttribute('answer');
			var score = tasks[i].getAttribute('score');
		}
		wp_games.game[wp_selected_game].locations.location[id].updateAnswer(answerstate,lastanswer,score);
 	}

 	for (var i=0; i<media.length; i++)
 	{
 		var id = media[i].getAttribute('mediumid');
 		var enabled = (media[i].getAttribute('state')!='open')

 		if (enabled) mediadone++;
		wp_games.game[wp_selected_game].locations.location[id].enable(enabled);
 	}

	tmp_debug(1,'play state: tasks=',tasks.length,' (',tasksdone,' done), media=',media.length,' (',mediadone,' done)');	
}



/* view functions */

function wpCloseDisplay()
{	
	if (wp_location_expanded) wp_location_expanded.collapse();
	else panes['display'].hide(1);	
}







/* games collection */

//games
// function wpGames()
// {
// 	return new idArray('game');
// }


// function wpGames()
// {
// 	this.game = new Object();
// 	this.length = 0;
// }
// wpGames.prototype.push = function(obj)
// {
// 	this.game[obj.id] = obj;
// 	this.length++;
// }
// wpGames.prototype.del = function(id)
// {
// 	this.game[id].dispose();
// 	this.lenght--;
// }



/* game object */

function wpGame(record)
{
	//id,owner,name,desc,intro,outro,state,t
	var properties = ['id','owner','name','description','intro','outro','state','creationdate','modificationdate'];

	for (var i in properties)
	{
		this[properties[i]] = record.getField(properties[i]);
	}
	
	//this.loaded = false;
	this.locations = new wpLocations('game');
	
	//location rollover
	var pane = new Pane('location_info',0,0,120,20,250,false,gmap.getPane(G_MAP_FLOAT_PANE));
		pane.content.onmousedown = function(e)
		{
			if (!e) e = event;
			cancelEvents(e);
		}
		pane.content.style.paddingTop = '4px';
	this.locations.info = pane;
	
	//game rounds
	this.rounds = new idArray('round');
	

	//this.getLocations();
}

wpGame.prototype.getLocations = function(center)
{
	//remove edit location dialogue	
	if (wp_locations.location['new']) wpCancelLocation();
	
	wp_game_selected = this.id;
	var obj = this;
	SRV.get('q-game-locations',function(resp) { obj.updateLocations(resp,center) },'id',this.id);
}

wpGame.prototype.updateLocations = function(resp,center)
{
	tmp_debug(3,'game (',this.id,') locations: ',resp.length);
	
	var locations = resp;

	//compare with current locations
	var list = new Object();
	var tasks = 0; var media = 0;
	for (var i=0; i<locations.length; i++)
	{
		list[locations[i].getField('id')] = i;
		//cnt totals
		if (locations[i].getField('type')=='task') tasks++;
		else media++;
	}

	var str = '';
	var used = new Object();

	//update/add locations
	for (var id in list)
	{
		var index = list[id];

		if (this.locations.location[id]) 
		{
			//existing
			str+= 'keep>'+id+', ';
			used[id] = true;
		}
		else
		{
			//add new location
			var id = locations[index].getField('id');
			var p = new GLatLng(locations[index].getField('lat'),locations[index].getField('lon'));
			var type = locations[index].getField('type');
			//var state = locations[index].getField('state');
			var name = locations[index].getField('name');
			
			var state = (wp_mode=='create')? 'enabled':'disabled';
		
			this.locations.push( new wpLocation(this.locations,id,p,type,state,name) );

			used[id] = true;
			str+= 'new>'+id+', ';
		}
	}
	//remove obsolete locations
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
	
	//show totals in edit pane
	if (wp_mode=='create')
	{
		document.getElementById('edit_game_tasks_cnt').innerHTML = tasks;
		document.getElementById('edit_game_media_cnt').innerHTML = media;
	}
	//update locations list
	if (panes['list_locations'].visible) this.listLocations();
	
	//center game in mapview
	if (center) this.locations.center();
}

wpGame.prototype.listLocations = function()
{
	var str = '';
		str+= '<span class="title">locations</span>';
		str+= '<div style="left:11px; top:30px; width:128px; height:123px; overflow:auto;">';
		for (var id in this.locations.location)
		{
			var location = this.locations.location[id];
			str+= '<a href="javascript://show_location" onclick="wp_games.game['+this.id+'].selectLocation('+location.id+');this.blur()">'+location.name+'</a><br>';
		}
		str+= '</div>';
		str+= '<div style="right:11px; top:8px"><a href="javascript://close" onclick="panes[\'list_locations\'].hide(0)">close</a></div>';
	
	panes['list_locations'].content.innerHTML = str;
	panes['list_locations'].show();
}

wpGame.prototype.selectLocation = function(id)
{
	var location = this.locations.location[id]
	gmap.panTo(location.geo);
	location.expand();
}

wpGame.prototype.edit = function()
{
	wp_game_edit = this.id;

	panes['list_games'].hide(1);
	
	var str = '';
		str+= '<span class="red" style="font-size:14px; font-weight:bold">edit game</span><br>';
		str+= '"<b>'+this.name+'</b>"';
		str+= '<div style="position:relative; width:175px; margin-top:5px">';
		str+= '<span class="grey">description:</span><br>'+this.description.substr(0,50)+'...<br>';
		str+= '<span class="grey">locations:</span><br><span id="edit_game_tasks_cnt">0</span> tasks and <span id="edit_game_media_cnt">0</span> media. ';
		str+= '<a href="javascript://list_locations" onclick="wp_games.game['+this.id+'].listLocations()">show list</a>';
		str+= '</div>';
		
		//add location button
		if (!browser.cssfilter) str+= '<img src="media/icon_add_location.png" title="add new location to game" style="cursor:pointer; position:absolute; right:15px; top:12px;" onclick="wpAddLocation()" onmouseover="this.src=\'media/icon_add_locationX.png\'" onmouseout="this.src=\'media/icon_add_location.png\'">';
		else
		{
			str+= '<div title="add new location to game" style="cursor:pointer; right:15px; top:12px; width:38px; height:38px; '+PNGbgImage('icon_add_location.png')+'" onclick="wpAddLocation()" onmouseover="this.style.filter=PNGbgImage(\'icon_add_locationX.png\').substr(7)" onmouseout="this.style.filter=PNGbgImage(\'icon_add_location.png\').substr(7)"></div>';
		}
		
		str+= '<br><a href="javascript://edit_info" onclick="alert(\'not yet\')">edit game info</a>';
		str+= '<input type=button value="done" onclick="wpSelect(\'create\');this.blur()" style="position:absolute; right:11px; bottom:8px; width:50px;">';

	var pane = panes['edit_game'];
		pane.setContent(str);
		pane.show();
		
	this.getLocations();
}

wpGame.prototype.editLocation = function(p)
{
	//->used for adding location, not for editing at this time
	
	//add a temporary location
	wp_locations.push( new wpLocation(wp_locations,'new',p,'tmp','enabled') );
	//gmap.addOverlay(new GMarker(p));

	//clear formfields
	var form = document.forms['locationform'];
	form.name.value = '';
	form.desc.value = '';
	form.answer.value = '';
	form.file.value = '';
	form.description.value = '';
	form.text.value = '';

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
		else if (type!='text' && form.file.value=='') msg = 'please select a mediafile for upload';
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

		if (type=='text')
		{
			//add new location with text directly to game
			var p = wp_locations.location['new'].geo;
			KW.WP.gameAddTextMedium(function() { obj.getLocations() },this.id,form.name.value,form.text.value,p.lng(),p.lat());	
		}
		else
		{
			//upload medium first
			KW.MEDIA.upload(function(elm) { obj.addLocation(elm) },form);
		}
	}
	
	//prevent form from submitting
	return false;
}

wpGame.prototype.deleteLocation = function(id)
{
	if (!confirm('delete location, are you sure?\nthere is no undo')) return;
	//forcehide info pane
	this.locations.info.hide(1);
	//close display
	if (panes['display'].visible) panes['display'].hide(1);

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
	
	if (wp_mode=='play')
	{
		//unsubscibe from pushlet events
		PL.unsubscribe();
		//kill users
		for (var id in wp_players.player) wp_players.del(id);
	}
	
	tmp_debug(2,'game unloaded, id=',this.id);
}

wpGame.prototype.dispose = function()
{
	//remove locations
	this.unLoad();
}


function wpRound(id)
{
	this.id = id;
	this.name = 'testround';
	
	//this.plays = new idArray('play');
}

function wpPlay(id)
{
	this.id = id;
	//players in this gameplay
	//this.players = new wpPlayers();
}


function wpLive(event)
{
	var eventType = event.get('event');

/*	Event types and attributes:
	common: userid, username, roundid, gameplayid
	
	¥	play-start: (common)
	¥	user-move: (common), lon, lat
	¥	task-done: (common), taskid, taskresultid, score
	¥	play-finish: (common)
	¥	timeout (time is up, voor later)

	¥	task-hit (common), taskid, taskresultid
	¥	medium-hit (common), mediumid, mediumresultid
	¥	answer-submit (common), taskid, taskresultid, answer
	¥	medium-add
	¥	message (later) 	*/


	switch (eventType)
	{
		case 'user-move':
			var id = event.get('userid');
	
			var p = new GLatLng(event.get('lat'),event.get('lon'));
			var t = event.get('t');
			var timestamp = new Date().getTime();
			var name = event.get('username');
		
			
			
			//add or update player
			if (wp_players.player[id]) wp_players.player[id].updateLocation(p,t);
			else wp_players.push( new wpPlayer(wp_players,id,p,name,t) );

			//tmp_debug(2,'<span style="color:#dd0000">user-move:&gt; user=',name,'</span>');
			break;
			
//			playertest.
			
			
		case 'task-hit':
		case 'medium-hit':
			
			var taskid = (eventType=='task-hit')? event.get('taskid'):event.get('mediumid');

			wp_games.game[wp_selected_game].locations.location[taskid].enable(true,true);
			break;
			
		case 'answer-submit':
			var taskid = event.get('taskid');
			var answer = event.get('answer');
			var answerstate = event.get('answerstate');
			
			wp_games.game[wp_selected_game].locations.location[taskid].updateAnswer(answerstate,answer);
			
			break;
			
		case 'task-done':
			var playid = event.get('gameplayid');
			var taskid = event.get('taskid');
			var score = event.get('score');
			
			if (wp_selected_play==playid)
			{
				//update score
				wp_games.game[wp_selected_game].locations.location[taskid].updateAnswer('scoreupdate',false,score);
			}
			else
			{
				//score by other team
				//-> update play display (team scores)
			}
		
			break;

		
		case 'play-finish':
			//->update all locations to disabled (for now, with gamebot)
			//..
			var game = wp_games.game[wp_selected_game];
			for (var id in game.locations.location)
			{
				game.locations.location[id].enable(false); 
				game.locations.location[id].play_answerstate = 'open';
			}
			//remove trace
			for (var id in wp_players.player) wp_players.player[id].trace = new Array();
			
			break;
			
		default:
			//tmp_debug(2,'<span style="color:#dd0000">wpLive:&gt; type=',eventType,'</span>');
			break;
		
	}
	
	
	tmp_debug(2,'<span style="color:#dd0000">wpLive:&gt; type=',eventType,'</span>');
}