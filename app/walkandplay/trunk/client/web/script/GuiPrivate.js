/* usemedia.com . joes koppers . 04.2007 [rev 08.2007] */
/* thnx for reading this code */


//wp gui for logged user

function wpGuiUserInit()
{
	tmp_debug('2','GuiUser.js loaded','querytime');

	wpCreatePane('list_create');
 	wpCreatePane('list_play');
 	wpCreatePane('edit_location');
 	
	//send heartbeats
	wp_hb = window.setInterval('wpHeartBeat()',60000*5) //send hb every minute (=idle user)
	
	//switch mode
	if (wp_login_action)
	{
		wpSelect(wp_login_action);
		wp_login_action = false;
	}
}

function wpHeartBeat()
{
	var req = KW.createRequest('play-hb-req');
	KW.utopia(req);
}

function wpCreatePaneUser(type,obj)
{
	var str = '';
	
	switch (type)
	{
		/* registered user panes */
		
		/* create */
		
		case 'list_create':
			var pane = new Pane('list_create',100,160,200,200,1,true);
			//header
			str+= '<b><span class="red">edit a game</span>, or <a href="javascript://create_new_game" onclick="wpCreateGame()">create new</a></b><br><br>';
			str+= '<div class="list"></div>';
			pane.setContent(str);
			pane.updateList = function(list)
			{
				this.content.lastChild.innerHTML = list;
			}
			break;
			
		case 'edit_game':
			var pane = new Pane('edit_game',100,160,180,150,1,true);

			str+= '<span class="red" style="font-size:14px; font-weight:bold">edit game</span><br>';
			str+= '<div style="position:relative; width:130px;">"<b>'+obj.name+'</b>"</div>';
			str+= '<div style="position:relative; width:175px; margin-top:5px">';
			//str+= '<span class="grey">description:</span><br><em>'+obj.description.substr(0,50)+'...</em><br>';
			str+= '<span class="grey">locations: </span><br>';
			str+= '<em><span id="edit_game_tasks_cnt">0</span> tasks and <span id="edit_game_media_cnt">0</span> media. </em>';
			str+= '[<a href="javascript://list_locations" onclick="wp_games.game['+obj.id+'].listLocations();this.blur()" style="margin:0px 2px">list</a>]';
			str+= '</div>';
			//add-location button
			if (obj.state!=2)
			{
				if (!browser.cssfilter) str+= '<img src="media/icon_add_location_w.png" title="add new location to game" style="cursor:pointer; position:absolute; right:15px; top:12px;" onclick="wpAddLocation()" onmouseover="this.src=\'media/icon_add_location_wX.png\'" onmouseout="this.src=\'media/icon_add_location_w.png\'">';
				else str+= '<div title="add new location to game" style="cursor:pointer; right:15px; top:12px; width:38px; height:38px; '+PNGbgImage('icon_add_location_w.png')+'" onclick="wpAddLocation()" onmouseover="this.style.filter=PNGbgImage(\'icon_add_location_wX.png\').substr(7)" onmouseout="this.style.filter=PNGbgImage(\'icon_add_location_w.png\').substr(7)"></div>';
			}
			str+= '<br>';
			if (obj.state==2) str+= '<img src="media/locked.gif"> <span class="red">remove all gamerounds to unlock and edit game locations</span>';

			if (obj.state!=2) str+= '<br>[<a href="javascript://edit_profile" onclick="wp_games.game['+obj.id+'].editProfile()">edit profile</a>]';
			str+= '<br>[<a href="javascript://edit_rounds" onclick="wp_games.game['+obj.id+'].editRounds();this.blur()">edit gamerounds</a>]';
			str+= '<br>[<a href="javascript://delete_game" onclick="wpDeleteGame('+obj.id+');this.blur()">delete game</a>]';
			 
			str+= '<input type=button value="done" onclick="wpSelect(\'create\');this.blur()" style="position:absolute; right:11px; bottom:8px; width:50px;">';

			pane.setContent(str);
			break;

		case 'edit_profile':
			var pane = new Pane('edit_profile',340,110,500,400,1,true);
			var header = (obj.mode)? 'create':'edit';
			var action = (obj.mode)? 'wpCreateGame(true)':'wp_games.game['+obj.id+'].updateProfile()';
			var cancel = (obj.mode)? 'wpSelect(\'create\')':'wp_games.game['+obj.id+'].edit()';
			var submit = obj.mode || 'update';
			
			var name = obj.name || '';
			var desc = obj.description || '';
			var intro = obj.intro || '';
			var outro = obj.outro || '';
			
			str+= '<span class="red" style="font-size:14px; font-weight:bold">'+header+' game profile</span><br><br>';
			//str+= 'created by <span class="red">'+obj.ownername+'</span><br><br>';
			//str+= '<a style="position:absolute; right:12px; top:6px" href="javascript://close" onclick="wpSelect(\'view\')">close</a>';
			str+= '<form name="editgameform" method="" action="" onsubmit="return '+action+'">';

 			str+= '<div class="column">name:</div><input type="text" name="name" value="'+name+'" class="inputtext" style="width:180px; margin-right:10px; margin-top:4px;"><br><br>';
 			str+= '<div style="position:relative; margin-left:-5px; padding:5px; width:498px; background-color:#eeeeee; margin-bottom:10px;">';
 			str+= '<div class="column">desc:</div><textarea name="desc" style="width:430px; height:50px;">'+desc+'</textarea><br>';
			str+= '</div>';
 			str+= '<div class="column"><em>intro:</em></div><textarea name="intro" style="width:430px; height:80px;">'+intro+'</textarea><br><br>';
 			str+= '<div class="column"><em>outro:</em></div><textarea name="outro" style="width:430px; height:80px;">'+outro+'</textarea><br>';

			str+= '<div style="right:15px; bottom:5px;"><input type="button" value="cancel" onclick="panes.hide(\'edit_profile\');'+cancel+'">&nbsp;<input type="submit" value=" '+submit+' " class="red" onclick=""></div>';
			str+= '</form>';

// 			str+= '<div id="profile_submit" style="right:15px; bottom:15px;"></div>';
			pane.setContent(str);
			pane.show();
			//pane.update = function(str) { this.content.lastChild.innerHTML = str; };
			break;
// 			var pane = new Pane('create_game',100,160,400,300,1,true);
// 			str+= '<span class="red" style="font-size:14px; font-weight:bold">create new game</span><br>';
// 			str+= '<div id="profile_submit" style="right:15px; bottom:15px;"></div>';
// 			pane.setContent(str);
// 			pane.show();
			break;

			
		case 'edit_rounds':
			var pane = new Pane('edit_rounds',320,160,200,200,1,true);
			str+= '<b><span class="red">edit round</span>, or <a href="javascript://add_round" onclick="wp_games.game[wp_selected_game].addRound()">add new</a></b><br><br>';
//			str+= 'or <a href="javascript://remove_all_rounds" onclick="wp_games.game[wp_selected_game].deleteAllRounds()">remove all</a></b><br>';
			str+= '<div style="right:11px; top:8px"><a href="javascript://close" onclick="panes.hide(\'edit_rounds\')">close</a></div>';
			str+= '<div style="left:15px; bottom:5px;">[<a href="javascript://remove_all_rounds" onclick="wp_games.game[wp_selected_game].deleteAllRounds()">remove all rounds</a>]</div>';

			str+= '<div class="list" style="height:160px;"></div>';
			pane.setContent(str);
			break;
			
		case 'edit_round':
			var pane = new Pane('edit_round',320,160,260,200,1,true);
			var header = (obj.mode=='add')? 'add':'edit';
			str+= '<span class="red" style="font-size:14px; font-weight:bold">'+header+' round</span><br>';// "<b>'+obj.name+'</b>"';
			
			str+= '<form name="editroundform" method="" action="" onsubmit="return wp_games.game[wp_selected_game].updateRound('+obj.roundid+',\''+obj.mode+'\');">';
 			str+= '<div class="column">name:</div><input type="text" name="name" value="'+obj.name+'" class="inputtext" style="width:180px; margin-right:10px; margin-top:4px;"><br>';
 			str+= '<div class="column">players: <span class="grey">(select multiple)</span></div>';//<textarea name="players" style="width:230px; height:50px;"></textarea><br><br>';
			str+= obj.list;
			str+= '<div style="right:15px; bottom:5px;"><input type="button" value="cancel" onclick="panes.hide(\'edit_round\');panes.show(\'edit_rounds\')">&nbsp;<input type="submit" value=" '+obj.mode+' " class="red" onclick=""></div>';
			str+= '</form>';

			if (obj.mode!='add') str+= '<div style="left:15px; bottom:5px;">[<a href="javascript://delete_round" onclick="wp_games.game['+obj.gameid+'].deleteRound('+obj.roundid+')">delete round</a>]</div>';

			pane.setContent(str);
			break;

		case 'edit_location':
			var pane = new Pane('edit_location',310,60,300,210,1,true,gmap.getPane(G_MAP_FLOAT_PANE));

			str+= '<form name="locationform" method="post" action="/wp/media.srv" enctype="multipart/form-data">';
//			str+= '<form name="locationform" method="post" action="" enctype="multipart/form-data">';
			str+= '<span class="title">location</span><br><br>';
			str+= '<div class="column">name:</div><input type="text" name="name" class="inputtext" style="width:130px; margin-right:10px">';
			str+= '<select name="type" onchange="wpEditLocation(this.value)" style="height:16px;"><option value="task">task</option><option value="medium">medium</option><option value="text">text</option></select><br><br>';
			//task fields
			str+= '<div id="taskform" style="position:relative; margin-left:-5px; padding:5px; width:295px; margin-bottom:10px;" class="setbg">';
			str+= '<div class="column">task:</div><textarea name="desc" style="width:230px; height:50px;"></textarea><br><br>';
			str+= '<div class="column">answer:</div><input type="text" class="inputtext" name="answer" style="width:130px; margin-right:10px;">';
			str+= 'score: <select name="score">'
			var scores = [25,50,75,100,200];
			for (var i in scores) str+= '<option value="'+scores[i]+'">'+scores[i]+'</option>';
			str+= '</select>';
			str+= '</div>';
			str+= '<div id="textform" style="display:none; position:relative; margin-left:-5px; padding:5px; width:295px; background-color:#eeeeee; margin-bottom:10px;">';
			str+= '<div class="column">text:</div><textarea name="text" style="width:230px; height:50px;"></textarea><br><br>';
			str+= '</div>';
			//medium
			str+= '<div id="mediumform" style="position:relative">';
			str+= '<div id="mediumtype" class="column">medium:</div><input type="file" name="file" class="inputtext" style="height:16px;">';
			str+= '</div>';
			str+= '<div id="mediumdescform" style="display:none; position:relative; margin-top:8px;">';
			str+= '<div class="column">desc:</div><textarea name="description" style="width:230px; height:50px;"></textarea><br><br>';
			str+= '</div>';
			//submit
			str+= '<div id="location_submit" style="right:15px; bottom:15px;"></div>';
			str+= '</form>';

			pane.setContent(str);
			pane.setType = function(type)
			{
				if (type=='add') var str= '<input type="button" value="cancel" onclick="wpCancelLocation()">&nbsp;<input type="submit" value=" add " class="red">';
				else var str='<input type="button" value="cancel" onclick="panes.hide(\'edit_location\');wp_selected_location=false;">&nbsp;<input type="submit" value=" update " class="red">';
				document.getElementById('location_submit').innerHTML = str;
			}
			pane.content.style.lineHeight = '9px';
			//block gmap events (since pane is attached to map pane)
			pane.content.onmousedown = function(e)
			{
				if (!e) e = event;
				cancelEvents(e);
			}
			break;

		case 'list_locations':
			var pane = new Pane('list_locations',100,350,120,140,1,true);

			str+= '<span class="title">locations</span>';
			str+= '<div style="left:11px; top:30px; width:128px; height:123px; overflow:auto;">';
			for (var id in obj.locations.location)
			{
				var location = obj.locations.location[id];
				str+= '<a href="javascript://show_location" onclick="wp_games.game['+obj.id+'].selectLocation('+location.id+');this.blur()">'+location.name+'</a><br>';
			}
			str+= '</div>';
			str+= '<div style="right:11px; top:8px"><a href="javascript://close" onclick="panes.hide(\'list_locations\')">close</a></div>';
			pane.setContent(str);
			break;
			
		/* play */
			
		case 'list_play':
			var pane = new Pane('list_play',100,160,200,200,1,true);
			//header
			str+= '<b><span class="red">play a gameround</span></b><br><br>';
			str+= '<div class="list"></div>';
			pane.setContent(str);
			pane.updateList = function(list)
			{
				this.content.lastChild.innerHTML = list;
			}
			break;
					
		default:
			break;
	}
}

///init when loaded
wpGuiUserInit();