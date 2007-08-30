/* usemedia.com . joes koppers . 04.2007 */
/* thnx for reading this code */


//wp game create and play

wp_add_location = false;

wp_game_selected = false;
wp_game_edit = false;

wp_selected_game = false;
wp_selected_round = false;
wp_selected_play = false;
wp_selected_location = false;

function wpGameInit()
{
	wp_live = false;
	wp_live_subscribed = false;
	
	//panes
	//wpCreatePane('list_games');
	
	wpCreatePane('list_view');
	wpCreatePane('list_locations');
 	wpCreatePane('play');
 	
	wpCreatePane('location_info');
	

// 	wpCreatePane('view');

	//game collections	
	wp_games = new idArray('game');
	wp_rounds = new idArray('round');
	wp_plays = new idArray('play');
	wp_players = new wpPlayers();
	
	//locations and players
 	wp_locations = new wpLocations();
 	wp_players = new wpPlayers();

	wp_view = false;
}

function wpListGames(resp)
{
	var header = '';
	var list = '';

	for (var i in resp)
	{
		//wp_games.push( new wpGame(resp[i]) );
		
		var name = resp[i].getField('name');
		var desc = resp[i].getField('description');
		var bullit = '&bull; '; //'<span class="red">&bull;</span> ';
		var shortname = (name.length>30)? name.substring(0,29)+'..':name;		
		
		if (wp_mode=='play')
		{
			var id = resp[i].getField('gameid');
			var roundid = resp[i].getField('roundid');
			var playid = resp[i].getField('gameplayid');
			
			list+= bullit+'<a href="javascript://play_game" onclick="wpSelectGame(\'play\','+id+','+roundid+','+playid+')" title="'+desc+'">'+shortname+'</a><br>';
		}
		else if (wp_mode=='create')
		{
			var id = resp[i].getField('id');
			list+= bullit+'<a href="javascript://edit_game" onclick="wpSelectGame(\'create\','+id+')" title="'+desc+'">'+shortname+'</a>';//<br>';
			if (resp[i].getField('state')==2) list+= '&nbsp;<img src="media/locked.gif">';
			list+= '<br>';
		}
		else if (wp_mode=='view')
		{
			var id = resp[i].getField('id');
			//list+= bullit+'<a href="javascript://view_gamerounds" onclick="wpListRounds('+id+')" title="'+desc+'">'+shortname+'</a><br>';
			list+= bullit+'<a href="javascript://view_gamerounds" onclick="wpGameProfile('+id+','+i+');this.blur()" title="'+desc+'">'+shortname+'</a><br>';
			//list+= bullit+'<a href="javascript://view_gamerounds" onclick="SRV.get(\'q-gamerounds\',function(resp) { wpListRounds('+id+',resp) },\'gameid\','+id+');" title="'+desc+'">'+shortname+'</a><br>';
		}
	}

// 	switch(wp_mode)
// 	{
// 		case 'create': header = '<span class="red">select a game to edit or <br/><span style="color:black">[<a href="javascript://new_game" onclick="JO.gameCreate()">create new game</a>]</span>:</span><br><br>'; break;
// 		case 'play': header = '<span class="red">select a game to play:</span><br>'; break;
// 		case 'view': header = '<span class="red">select a game to view:</span><br>'; break;
// 	}
// 	var header = (wp_mode=='create')? '<span class="red">select a game to edit:</span><br>':'<span class="red">select a game to play:</span><br>';
	if (list=='') list = '- no games available -';

// 	if (wp_mode=='view')
// 	{
// 		//panes['list_games'].setContent(wpGuiCreate('list_games_view')+header+list);
// 		//panes['list_games'].setContent(wpGuiCreate('list_games_view'));
// 		wpCreatePane('list_games_view');
// 		panes['list_games'].content.lastChild.innerHTML = header+list;
// 	}
// 	else panes['list_games'].setContent(header+list);

	var pane = panes['list_'+wp_mode];
 	pane.updateList(list);
	pane.show();
}

function wpCreateGame(action)
{
	/*	create game profile (pane)	
	*/
	
	if (!action)
	{
		panes.hide('list_create');
		//show (empty) profile pane
		wpCreatePane('edit_profile',{ mode:'create' });
	}
	else
	{
		//validate
		var form = document.forms['editgameform'];
		if (form.name.value=='')
		{
			alert('please enter a game name');
			return false;
		}
		
		KW.WP.gameCreate(function(resp) { panes.hide('edit_profile'); wpSelectGame('create',resp.getAttribute('id'))  },form.name.value,form.desc.value,form.intro.value,form.outro.value);
		
		//prevent form submit
		return false;		
	}
}

function wpDeleteGame(id)
{
	/*	confirm and delete game
	*/
	if (confirm('are you sure?\ndeletion is immediate and will also remove all related gamerounds and plays permanently!\n'))
	{
		KW.WP.gameDelete(function() { wpSelect('create') },id);
	}
}

function wpGameProfile(id,list_index)
{
	var list = panes['list_view'].content.lastChild.getElementsByTagName('A');

	var str = '';
	for (var i=0; i<list.length; i++) list[i].style.color = 'black';
// 	{
// 		str+= elm+' ('+typeof(elm)+'), ';
// 		//typeof(list);
// 		//elm.
// 	}
// 	alert(str);
	list[list_index].style.color = 'rgb(200,0,20)';

	//panes.hide('list_games');
	//wpCreatePane('game_profile');
	
	//panes.show('game_profile');
	
	wpSelectGame('view',id);

	//get game details
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
// 	else
// 	{
// 		
// 	}

	var game = wp_games.game[id];

	switch (type)
	{
		case 'play':
			//add gameround and play
			if (!wp_rounds[wp_selected_round]) wp_rounds.push( new wpRound(wp_selected_round) );
			if (!wp_plays[wp_selected_play]) wp_plays.push( new wpPlay(wp_selected_play) );

			//close list, open play display
			panes.hide('list_games');
			panes['play'].clearContents();
			panes.show('play');
			
			//get current gamestate
			game.getLocations(true); //will update playstate when loaded
			break;
			
		case 'create':
			wp_games.game[id].edit();
			break;

		case 'view':
			//get avail rounds
			SRV.get('q-gamerounds',function(resp) { wp_games.game[id].showProfile(resp) },'gameid',id);
			break;
		
		case 'view_round':
			//add gameround and play
			if (!wp_rounds[wp_selected_round]) wp_rounds.push( new wpRound(wp_selected_round) );
			
			//get plays for this round
			SRV.get('q-gameplays',wpSelectRound,'roundid',wp_selected_round);
			
			//if (!wp_plays[wp_selected_play]) wp_plays.push( new wpPlay(wp_selected_play) );
			
			panes.hide('list_view','game_profile');
			
			//wpCreatePane('view');
			
			//panes['view'].setContent(wpGuiCreate('view'));
			panes['play'].clearContents();
			panes.show('view','play');
			
			game.getLocations(true); 
			
			break;
			
		default:
			break;
	}
}

function wpSelectRound(resp)
{
	//update round properties
	wp_rounds.round[wp_selected_round].addTeams(resp)

	//show view ctls and load game locations
	panes.hide('list_games','list_rounds');
	//panes['view'].setContent(wpGuiCreate('view'));
	wpCreatePane('view');
	
	panes['play'].clearContents();
	panes.show('view','play');
	wp_games.game[wp_selected_game].getLocations(true); 
}

function wpLoadGame(resp)
{
	//new game obj
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

function wpGetPlay()
{

}

function wpUpdatePlay(resp)
{
	var game = wp_games.game[wp_selected_game];
	var tasks_hit = 0;
	var media_hit = 0;
	var tasks_done = 0;

	if (!resp)
	{
		//for view playback, start values;
		var state = 'playback';
		var team = (wp_selected_play)? wp_rounds.round[wp_selected_round].teams.team[wp_selected_play].name:'';
		var score = 0;
	}
	else
	{
		var gameplay = resp.getElementsByTagName('gameplay')[0];
		var tasks = resp.getElementsByTagName('task-result');
		var media = resp.getElementsByTagName('medium-result');
	
		//update locations, count state	
		for (var i=0; i<tasks.length; i++)
		{
			var id = tasks[i].getAttribute('taskid');
			var enabled = (tasks[i].getAttribute('state')!='open');
			var answerstate = tasks[i].getAttribute('answerstate');
			var mediastate = tasks[i].getAttribute('mediastate');
			
			if (enabled) tasks_hit++;
			if (answerstate=='ok') tasks_done++;
			wp_games.game[wp_selected_game].locations.location[id].enable(enabled);
			
			var lastanswer = '';
			var score= '';
			var mediumid = false;
			
			if (answerstate!='open')
			{
				lastanswer = tasks[i].getAttribute('answer');
				score = tasks[i].getAttribute('score');
			}
			if (mediastate!='open')
			{
				mediumid = tasks[i].getAttribute('mediumid');
			}
			wp_games.game[wp_selected_game].locations.location[id].updateAnswer(answerstate,lastanswer,mediumid,score);
		}
	
		for (var i=0; i<media.length; i++)
		{
			var id = media[i].getAttribute('mediumid');
			var enabled = (media[i].getAttribute('state')!='open')
	
			if (enabled) media_hit++;
			wp_games.game[wp_selected_game].locations.location[id].enable(enabled);
		}

		var state = gameplay.getAttribute('state');
		var team = gameplay.getAttribute('team');
		var score = gameplay.getAttribute('score');
	}

	//update play (and play display)
	if (wp_selected_play)
	{
		var play = wp_plays.play[wp_selected_play];
		play.state = state;
		play.score = Number(score);
		play.team = team;
		play.tasks_hit = tasks_hit;
		play.media_hit = media_hit;
		play.tasks_done = tasks_done;
	}

	var str = '';
		str+= '<span class="title">game</span> "<b>'+game.name+'</b>"<br>';
		str+= '<span class="grey">status:</span></div><br>&nbsp;&nbsp;<i>'+state+'</i><br>';
		str+= '<span class="grey">locations:</span><br>&nbsp;&nbsp; '+game.tasks+' tasks and '+game.media+' media. ';
	panes['play'].game.innerHTML = str;
	
	wpUpdatePlayScore();
	var msg = (wp_mode=='play')? 'welcome '+team+' HQ':'';
	wpUpdatePlayRound(msg);
		
	//(re)start live events handling
	if (wp_mode=='play')
	{
		if (!wp_live)
		{
			wp_live = true;
			wp_live_subscribed = true;
			//start live events listener
			PL.joinListen('/wp/round/'+wp_selected_round+',/wp/play/'+wp_selected_play);
			onData = wpLive;
		}
		else if (!wp_live_subscribed)
		{
			PL.subscribe('/wp/round/'+wp_selected_round+',/wp/play/'+wp_selected_play);
			wp_live_subscribed = true;
		}
	}
}

function wpUpdatePlayScore()
{
	if (!wp_selected_play) return;
	
	var game = wp_games.game[wp_selected_game];
	var play = wp_plays.play[wp_selected_play];
	
	if (!play.team) return;
	
	var m = (game.tasks==0 || game.media==0)? 0:(play.media_hit/game.media) * (game.media/(game.tasks+game.media)) * 100;
	var t = (game.tasks==0 || game.media==0)? 0:(play.tasks_done/game.tasks) * (game.tasks/(game.tasks+game.media)) * 100;
	var completed = Math.round(m+t);
	
	var str = '';
		str+= '<span class="title">team</span> "<b class="'+play.team.substring(0,1)+'">'+play.team+'</b>"<br>';
		str+= '<span class="'+play.team.substring(0,1)+'" style="float:right; margin-top:10px; margin-right:5px; line-height:18px;"><b style="font-size:18px">'+play.score+'</b> points</span>';
		str+= '<span class="grey">score:</span></div><br>&nbsp;&nbsp;'+completed+'% completed<br>';
		str+= '<span class="grey">locations hit:</span><br>&nbsp;&nbsp; <b id="">'+play.tasks_hit+'</b> tasks and <b id="">'+play.media_hit+'</b> media. ';
	panes['play'].play.innerHTML = str;
}

function wpUpdatePlayRound(msg)
{
	//game messages to all players and total score display
	var str = '';
		//str+= '<b>playing as team</b> "<b class="'+play.team.substring(0,1)+'">'+play.team+'</b>"<br>';
		str+= '<span class="title">round</span><br>';
		if (wp_mode=='play')
		{
			str+= new Date().format('time');
			str+= ' - <i>';
			str+= msg || '';
		}
		else
		{
			str+= msg;
		}
		str+= '</i><br>';
		str+= '<span class="grey">scores:</span></div><br>';

		//-> temp, need round info to generate these..
		str+= '<div style="position:relative; background-color:rgb(200,0,20); width:80px; height:9px; margin-left:10px; margin-top:4px; font-size:1px;"></div>';
		str+= '<div style="position:relative; background-color:rgb(50,100,200); width:50px; height:9px; margin-left:10px; margin-top:3px; font-size:1px;"></div>';
		str+= '<div style="position:relative; background-color:rgb(45,170,75); width:50px; height:9px; margin-left:10px; margin-top:3px; font-size:1px;"></div>';

	panes['play'].round.innerHTML = str;
}

function wpLeavePlay()
{
	if (confirm('leave this gameplay?'))
	{
		wpSelect('play');
		window.setTimeout("panes.hide('play')",250); //to force hide in IE, //->fix in future version?
	}
}






/* game object */

function wpGame(record)
{
	//id,owner,name,desc,intro,outro,state,t
	var properties = ['id','owner','name','description','intro','outro','state','creationdate','modificationdate','ownername'];

	for (var i in properties)
	{
		this[properties[i]] = record.getField(properties[i]);
	}
	
	//this.loaded = false;
	this.locations = new wpLocations('game');
	
	//location rollover
//	panes.dispose('location_info');
		
	this.locations.info = panes['location_info'];
	
	//game rounds
	this.rounds = new idArray('round');
	
	this.tasks = 0;
	this.media = 0;

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
	
	//store result
	this.tasks = tasks;
	this.media = media;
	
	//show totals in edit pane
	if (wp_mode=='create')
	{
		document.getElementById('edit_game_tasks_cnt').innerHTML = tasks;
		document.getElementById('edit_game_media_cnt').innerHTML = media;
	}
	//update locations list
	if (panes['list_locations'] && panes['list_locations'].visible) this.listLocations();
	
	//center game in mapview
	if (center && this.locations.length>0) this.locations.center();
	
	//update play/view state
	if (wp_mode=='play' && wp_selected_play) KW.WP.playGetGamePlay(wpUpdatePlay,wp_selected_play);
	if (wp_mode=='view') wpUpdatePlay();
}

wpGame.prototype.listLocations = function()
{
	wpCreatePane('list_locations',this);

	//panes['list_locations'].setContent(str);
	panes['list_locations'].show();
}

wpGame.prototype.selectLocation = function(id)
{
	var location = this.locations.location[id]
	gmap.panTo(location.geo);
	location.expand();
}

wpGame.prototype.newLocation = function(p)
{
	//add a temporary location
	wp_locations.push( new wpLocation(wp_locations,'new',p,'tmp','enabled') );
	//gmap.addOverlay(new GMarker(p));

	//clear formfields
	var form = document.forms['locationform'];
	form.type.disabled = false;
	
//	form.reset();


//	var fields = ['name','desc','answer','file','description','text'];
 	for (var i=0; i<form.elements.length; i++) 
 	{
 		if (form.elements[i].type=='text' || form.elements[i].type=='textarea' || form.elements[i].type=='file') form.elements[i].value = '';
 	}
//  	form.name.value = '';
//  	form.desc.value = '';
//  	form.answer.value = '';
//  	form.file.value = '';
//  	form.description.value = '';
//  	form.text.value = '';

	var px = gmap.fromLatLngToDivPixel(p);
	var pane = panes['edit_location'];
	pane.setType('add');
	pane.setPosition(px.x,px.y);
	pane.show();
	
	//submit handler for form
	var obj = this;
	form.onsubmit = function() { return obj.addLocation() };
}

wpGame.prototype.editLocation = function(id)
{
	/*	edit location pane
	*/
	
	//get location
	var location = this.locations.location[id];
	wp_selected_location = location;


	panes.hide('location_info');

//	alert( tmp_print_object(location) );

	
	//get location info if needed
// 	if (location.desc==undefined)
// 	{
		var resp = SRV.get('q-'+location.type,null,'id',location.id);
		var record = resp[0];

		location.mediumid = (location.type=='medium')? location.id:record.getField('mediumid');
		location.desc = record.getField('description');
		location.mediumtype = (location.type=='medium')? record.getField('type'):record.getField('mediumtype');
		if (location.type=='task')
		{
			location.answer = record.getField('answer');
			location.score = record.getField('score');
		}
		
		//update original object
//		this.locations.location[id] = location;
// 	}

	//set form type
	var form = document.forms['locationform'];
	var type = (location.type=='medium')? (location.mediumtype=='text')? 'text':'medium':'task';
	form.type.value = type;
	form.type.disabled = true;

	wpEditLocation(type);
	
	//set form contents
	form.name.value = location.name;
	form.file.value = '';
	
	if (location.type=='task')
	{
	  	form.desc.value = location.desc;
		form.answer.value = location.answer;
		form.score.value = location.score;
	}
	else if (location.mediumtype=='text') 
	{
		form.text.value = DH.getURL('/wp/media.srv?id='+location.mediumid+'&t='+new Date().getTime());
	}
	else
	{
		form.description.value = location.desc;
	}






	
//  	form.desc.value = '';
//  	form.file.value = '';
//  	form.description.value = '';
//  	form.text.value = '';



	
	
	var px = gmap.fromLatLngToDivPixel(location.geo);
	var pane = panes['edit_location'];
	pane.setType('update');
	
	
	
	
	
	pane.setPosition(px.x,px.y);
	pane.show();
	
	var obj = this;
	form.onsubmit = function() { return obj.updateLocation() };
	//form.onsubmit = function() { return location.updateContents() };
	//

}

wpGame.prototype.updateLocation = function(elm)
{
	/*	update location (contents)
		upload new medium first if needed
	*/
	
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
		KW.MEDIA.upload(function(elm) { obj.updateLocation(elm) },form);
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
	
	switch (type)
	{
		case 'task':
			var desc = form.desc.value;
			var answer = form.answer.value;
			var score = form.score.value;
			KW.WP.gameUpdateTask(function() { wp_selected_location.refresh(name) },wp_selected_location.id,name,desc,score,answer,mediumid);
			break;
		
		case 'medium':
			var desc = form.description.value;
			if (mediumid)
			{
				wp_selected_location.collapse();
				KW.WP.gameUpdateMedium(function() { wp_games.game[wp_selected_game].getLocations() },wp_selected_location.id,mediumid,name,desc);
			}
			else KW.WP.gameUpdateMedium(function() { wp_selected_location.refresh(name) },wp_selected_location.id,mediumid,name,desc);
			break;
			
		case 'text':
			var text = form.text.value;
			KW.WP.gameUpdateTextMedium(function() { wp_selected_location.refresh(name) },wp_selected_location.id,text,name,desc);
			break;
	}
	
	panes.hide('edit_location');
	
	//prevent form submit
	return false;
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

wpGame.prototype.showProfile = function(resp)
{
	//update and show profile pane
	
// 	var str = '';
// 	for (var p in this)
// 	{
// 		if (typeof(this[p])=='function') continue;
// 		str+= p+' = '+this[p]+'<br>';
// 	}


	//gamerounds list
	var list = '';
		
	for (var i in resp)
	{
		var roundid = resp[i].getField('id');
		var name = resp[i].getField('name');
		var desc = name;
		
		var shortname = (name.length>26)? name.substring(0,25)+'..':name;
		
		list+= '&bull; <a href="javascript://view_round" onclick="wpSelectGame(\'view_round\','+this.id+','+roundid+')" title="'+desc+'">'+shortname+'</a><br>';
	}
	if (list=='') list = '- no rounds available -';

	var stats = '*'


	wpCreatePane('game_profile',{ game:this, rounds:list, stats:stats });
	panes.show('game_profile');


	
// 	document.getElementById('profile_name').innerHTML = this.name;
// 	panes['game_profile'].update(str);
	
}

wpGame.prototype.edit = function()
{
	/*	edit game pane
	*/

	wp_game_edit = this.id;
	wpCreatePane('edit_game',this);

	panes.hide('list_create');
	panes.show('edit_game');
	
	this.getLocations(true);
}

wpGame.prototype.update = function(resp)
{
	/*	update game properties (name, desc, intro, outro, state)
	*/

	if (!resp)
	{
		//get game details
		var obj = this;
		SRV.get('q-game',function(resp) { obj.update(resp) }, 'id', this.id);
		return;		
	}
	
	this.name = resp[0].getField('name');
	this.description = resp[0].getField('description');
	this.intro = resp[0].getField('intro');
	this.outro = resp[0].getField('outro');
	this.state = resp[0].getField('state');
	
	//reload edit pane
	this.edit();
}

wpGame.prototype.editProfile = function()
{
	/*	edit game profile pane
	*/

	panes.hide('edit_rounds','edit_round'); //if clicked while editing round(s)
	
	wpCreatePane('edit_profile',this);	
}

wpGame.prototype.updateProfile = function(id,type)
{
	/*	update game profile
	*/

	//form validate
 	var form = document.forms['editgameform'];
	if (form.name.value=='')
	{
		alert('please enter a game name');
		return false;
	}
	
// 	if (type=='create')
// 	{
// 		KW.WP.gameCreate(function(resp) { rsp.getAttribute('id') },form.name.value,form.desc.value,form.intro.value,form.outro.value);	
// 	}
// 	else
// 	{
	var obj = this;
	KW.WP.gameUpdate(function() { panes.hide('edit_profile'); obj.update() },this.id,form.name.value,form.desc.value,form.intro.value,form.outro.value);
// 	}

	
	
	//prevent form submit
	return false;	
}

wpGame.prototype.editRounds = function(resp)
{
	/*	list gamerounds pane (list, add-button)
	*/
	
	if (!resp) 
	{
		panes.hide('edit_profile'); //if clicked while editing profile
	
		//get avail rounds
		var obj = this;
		SRV.get('q-gamerounds',function(resp) { obj.editRounds(resp) }, 'gameid', this.id);
		return;
	}
	
	var list = '';
	for (var i in resp)
	{
		list+= '&bull; <a href="javascript://edit_round" onclick="wp_games.game[wp_selected_game].editRound(0,'+resp[i].id+')">'+resp[i].getField('name')+'</a><br>';
	}
	if (list=='') list = '- no rounds available -';

	wpCreatePane('edit_rounds',this);
	var pane = panes['edit_rounds'];
	pane.content.lastChild.innerHTML = list;
	pane.show();
}
wpGame.prototype.addRound = function(id)
{
	/*	add gameround pane (==edit gameround pane)
	*/

	this.editRound(false,false,'add');
}

wpGame.prototype.editRound = function(resp,id,add)
{
	/*	edit gameround pane (name, player multiselect, delete-button)
	*/

	if (!resp && !add)
	{
		//get roundinfo
		var obj = this;
		SRV.get('q-gameround',function(resp) { obj.editRound(resp,id,mode) },'id',id);
		return;
	}
	
	var players = SRV.get('q-all-users', null);
	if (!add && resp[0].getField('players'))
	{	
		var selected = (add)? []:resp[0].getField('players').split(',');
		//store current selected players for updateRound()
		this.edit_round_players = resp[0].getField('players');
	}
	else var selected = [];

	//build multi select
	var str = '<select name="players" multiple size=10 style="width:199px; margin-top:4px;">';
	for (var i in players)
	{
		var player = players[i].getField('loginname');
		//selected?
		var select = '';
		for (var n in selected)
		{
			if (player==selected[n])
			{
				select = 'selected';
				delete selected[n];
				break;
			}
		}
		str+= '<option value="'+player+'" '+select+'>'+player+'</option>';
	}
	str+= '</select>';

	//create and show edit pane
	var mode = add || 'update';
	var name = (add)? '':resp[0].getField('name');
	wpCreatePane('edit_round',{ gameid:this.id, roundid:id, name:name, list:str, mode:mode });
	panes.hide('edit_rounds');
	panes.show('edit_round');
}

wpGame.prototype.updateRound = function(id,type)
{
	/*	add or update gameround
	*/

	//form validate
 	var form = document.forms['editroundform'];
	if (form.name.value=='')
	{
		alert('please enter a gameround name');
		return false;
	}
	
	//get selected players
	var selected = [];
	for (var i=0; i<form.players.length; i++) if (form.players[i].selected) selected.push(form.players[i].value);
	
	if (type=='add')
	{
		var players = selected.join(',');
		var obj = this;
		KW.WP.roundCreate(function() { panes.hide('edit_round'); obj.update(); obj.editRounds(); }, this.id, form.name.value, players);
	}
	else
	{
		//define obsolete and new players //->move to serverside??
		var original = (this.edit_round_players)? this.edit_round_players.split(','):[];
		for (var i in selected)
		{
			//player in original selection?
			for (n in original)
			{	
				if (selected[i]==original[n])
				{
					//remove from both lists
					delete selected[i];
					delete original[n];
					break;
				}
			}
		}
		var add = [];
		var del = [];
		for (var i in selected)	if (selected[i]!=undefined) add.push(selected[i]);
		for (var i in original)	if (original[i]!=undefined) del.push(original[i]);

		tmp_debug(3,'round upd: add=',add.join(','),' del=',del.join(','));
		
		//add/remove
		var obj = this;
		if (del.length>0) KW.WP.roundRemovePlayers(null,id,del.join(','));
		if (add.length>0) KW.WP.roundAddPlayers(null,id,add.join(','));

		delete this.edit_round_players;
		panes.hide('edit_round'); 
		this.editRounds();
	}
	
	//prevent form submit
	return false;
}


wpGame.prototype.deleteRound = function(id)
{
	/* confirm and delete gameround (and related gameplays)
	*/
	
	if (confirm('are you sure?\ndeletion in immediate and all related gameplays are removed permanently!'))
	{
		var obj = this;
		KW.WP.roundDelete(function() { panes.hide('edit_round'); obj.update(); obj.editRounds() } ,id);
	}
}
wpGame.prototype.deleteAllRounds = function()
{
	/* confirm and delete all gamerounds
	*/
	
	if (confirm('are you sure?\nyour are deleting ALL gamerounds\ndeletion in immediate and all related gameplays are removed permanently!'))
	{
		var obj = this;
		KW.WP.roundDeleteAll(function() { panes.hide('edit_rounds'); obj.update() } ,this.id);
	}
}

wpGame.prototype.unLoad = function()
{
	//remove all locations
	for (var id in this.locations.location) this.locations.del(id);
	
	if (wp_mode=='play')
	{
		//unsubscibe from pushlet events
		PL.unsubscribe();
		wp_live_subscribed = false;
		//PL.leave();
		//kill users
		for (var id in wp_players.player) wp_players.del(id);
	}
	
	//GLog.write('unload game '+ this.id);
	
	tmp_debug(2,'game unloaded, id=',this.id);
}

wpGame.prototype.dispose = function()
{
	//remove locations
	this.unLoad();
}



function wpRound(id,name)
{
	this.id = id;
	this.name = name || 'gameround';

	this.teams = new idArray('team');
}

wpRound.prototype.addTeams = function(resp)
{
	for (var i in resp)
	{
		var id = resp[i].getField('id');
		var name = resp[i].getField('loginname');
		
		this.teams.push( {id:id, name:name } );
	}
}

function wpPlay(id,team)
{
	this.id = id;
	this.team = team;
	
	this.state = '';
	this.score = 0;
	this.tasks_hit = 0;
	this.media_hit = 0;
	this.tasks_done = 0;
}
wpPlay.prototype.dispose = function()
{
	//dispose
}



/* live and playback event handling */

function wpLive(event)
{
	if (wp_mode=='play' && !wp_live_subscribed) return;
	
	var eventType = event.get('event');

/*	Event types and attributes:
	common: userid, username, roundid, gameplayid
	
	¥	play-start: (common)
	¥	user-move: (common), lon, lat
	¥	task-done: (common), taskid, taskresultid, score
	¥	play-finish: (common)
	¥	timeout (time is up, voor later)

	¥	task-hit: (common), taskid, taskresultid
	¥	medium-hit: (common), mediumid, mediumresultid
	¥	answer-submit: (common), taskid, taskresultid, answer
	¥	medium-add (common), mediumid (if medium only added to trace)
	¥	medium-add (common), mediumid, taskid, taskresultid (if medium added to trace AND part of anwering task)
	¥	message (later) 	*/

	var playid = event.get('gameplayid');


	switch (eventType)
	{
		/* round events */
		
		case 'user-move':
			var id = event.get('userid');
	
			var p = new GLatLng(event.get('lat'),event.get('lon'));
			var t = Number(event.get('t'));
			var timestamp = new Date().getTime();
			var name = event.get('username');
			
			//add or update player
			if (wp_players.player[id]) wp_players.player[id].updateLocation(p,t);
			else wp_players.push( new wpPlayer(wp_players,id,p,name,t) );

			//tmp_debug(2,'<span style="color:#dd0000">user-move:&gt; user=',name,'</span>');
			break;
			
		case 'task-done':
			var taskid = event.get('taskid');
			var score = event.get('score');
			
			if (wp_selected_play==playid)
			{
				//update score
				wp_games.game[wp_selected_game].locations.location[taskid].updateAnswer('score-update',false,false,score);
				wp_plays.play[wp_selected_play].tasks_done++;
				wp_plays.play[wp_selected_play].score += Number(score);
				wpUpdatePlayScore();
				//panes['play'].team_score.innerHTML = score;
			}
			else
			{
				//score by other team
				//var t = Number(event.get('t'));
				var team = event.get('username');
				var msg = team+' scored '+score+' points!';
				wpUpdatePlayRound(msg);
			}
		
			break;
	
		case 'play-finish':
			//->this is for gamebot demo only -> change later
			//..
			if (wp_mode=='view')
			{
				document.getElementById('view_progress_bar').style.width = '180px';
				wp_view.startstop();
				return;
			}
			
			wpUpdatePlayRound(event.get('username')+' finished the game!');

			//remove trace
			var id = event.get('userid');
			var player = wp_players.player[id];
			//GLog.write('play-finish, '+event.get('username')+' id='+id+', player='+player)
			if (player)
			{
				player.trace = new Array(0);
				if (player.traceOverlay) gmap.removeOverlay(player.traceOverlay);
			}
			
			break;
			
		case 'play-start':
			//->this is for gamebot demo only -> change later
			//..
			if (wp_mode=='view') return;
			
			if (wp_selected_play==playid)
			{
				//reset gameplay
				KW.WP.playGetGamePlay(wpUpdatePlay,wp_selected_play);
				panes['display'].hide(1);
			}
			else
			{
				wpUpdatePlayRound(event.get('username')+' started playing..');
			}
			break;
			
			
			/* play events */
			
		case 'task-hit':
		case 'medium-hit':
			
			var taskid = (eventType=='task-hit')? event.get('taskid'):event.get('mediumid');
			
			var location = wp_games.game[wp_selected_game].locations.location[taskid];
			//update score
			if (location.state=='disabled')
			{
				if (eventType=='task-hit') wp_plays.play[wp_selected_play].tasks_hit++;
				else wp_plays.play[wp_selected_play].media_hit++;
				wpUpdatePlayScore();
			}
			
			location.enable(true,true);
			break;
			
		case 'answer-submit':
			var taskid = event.get('taskid');
			var answer = event.get('answer');
			var answerstate = event.get('answerstate');
			
			wp_games.game[wp_selected_game].locations.location[taskid].updateAnswer(answerstate,answer);
			
			break;
			
		case 'medium-add':
			var taskid = event.get('taskid');
			var mediumid = event.get('mediumid');
			
			if (taskid)
			{
				var type = event.get('kind');
				wp_games.game[wp_selected_game].locations.location[taskid].updateAnswer('medium-add',false,mediumid);
			}
			
			break;
			
		default:
			//tmp_debug(2,'<span style="color:#dd0000">wpLive:&gt; type=',eventType,'</span>');
			break;
		
	}
	
	
	tmp_debug(2,'<span style="color:#dd0000">wpLive:&gt; type=',eventType,' (',event.get('username'),')</span>');
	//GLog.write(eventType+' ('+event.get('username')+')');
}