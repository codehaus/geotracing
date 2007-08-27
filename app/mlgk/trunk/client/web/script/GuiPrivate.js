/* usemedia.com . joes koppers . 04.2007 */
/* thnx for reading this code */


//wp gui for logged user

function wpGuiUserInit()
{
	tmp_debug('2','GuiUser.js loaded','querytime');

	//send heartbeats
	wp_hb = window.setInterval('wpHeartBeat()',60000*5) //send hb every minute (=idle user)

	//edit location pane	
 	wpCreatePane('edit_location');
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
					
		case 'edit_game':
			var pane = new Pane('edit_game',100,160,180,160,1,true);

			str+= '<span class="red" style="font-size:14px; font-weight:bold">edit game</span><br>';
			str+= '<div style="position:relative; width:130px;">"<b>'+obj.name+'</b>"</div>';
			str+= '<div style="position:relative; width:175px; margin-top:5px">';
			str+= '<span class="grey">description:</span><br><em>'+obj.description.substr(0,50)+'...</em><br>';
			str+= '<span class="grey">locations: [<a href="javascript://list_locations" class="grey" onclick="wp_games.game['+obj.id+'].listLocations()">show list</a>]</span><br>';
			str+= '<em><span id="edit_game_tasks_cnt">0</span> tasks and <span id="edit_game_media_cnt">0</span> media. </em>';
			str+= '</div>';
			//add-location button
			if (obj.state!=2)
			{
				if (!browser.cssfilter) str+= '<img src="media/icon_add_location_w.png" title="add new location to game" style="cursor:pointer; position:absolute; right:15px; top:12px;" onclick="wpAddLocation()" onmouseover="this.src=\'media/icon_add_location_wX.png\'" onmouseout="this.src=\'media/icon_add_location_w.png\'">';
				else str+= '<div title="add new location to game" style="cursor:pointer; right:15px; top:12px; width:38px; height:38px; '+PNGbgImage('icon_add_location_w.png')+'" onclick="wpAddLocation()" onmouseover="this.style.filter=PNGbgImage(\'icon_add_location_wX.png\').substr(7)" onmouseout="this.style.filter=PNGbgImage(\'icon_add_location_w.png\').substr(7)"></div>';
			}
			str+= '<br>';
			if (obj.state!=2) str+= '[<a href="javascript://edit_info" onclick="JO.gameUpdate('+obj.id+')">edit info</a>]';
			
			str+= '<br>[<a href="javascript://add_round" onclick="JO.roundCreate(' + obj.id + ')">add round]</a>&nbsp;&nbsp;[<a href="javascript://del_round" onclick="JO.roundDelete(' + this.id +')">del round</a>]';
			str+= '<br>[<a href="javascript://edit_rounds" onclick="wp_games.game['+obj.id+'].rounds()">edit gamerounds</a>]';
			
			str+= '<input type=button value="done" onclick="wpSelect(\'create\');this.blur()" style="position:absolute; right:11px; bottom:8px; width:50px;">';

			pane.setContent(str);
			break;
			
		case 'edit_rounds':
			var pane = new Pane('edit_rounds',100,160,180,160,1,true);
			pane.setContent(wpGuiCreate('edit_rounds',obj));
			break;

		case 'edit_location':
			var pane = new Pane('edit_location',310,60,300,210,1,true,gmap.getPane(G_MAP_FLOAT_PANE));

			str+= '<form name="locationform" method="post" action="/wp/media.srv" enctype="multipart/form-data">';
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
			str+= '<div style="right:15px; bottom:15px;"><input type="button" value="cancel" onclick="wpCancelLocation()">&nbsp;<input type="submit" value=" add " class="red"></div>';
			str+= '</form>';

			pane.setContent(str);
			pane.content.style.lineHeight = '9px';
			//block gmap events (since pane is attached to map pane)
			pane.content.onmousedown = function(e)
			{
				if (!e) e = event;
				cancelEvents(e);
			}
			break;

		case 'list_locations':
			var pane = new Pane('list_locations',100,360,120,140,1,true);

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
			
		default:
			break;
	}
}

///init when loaded
wpGuiUserInit();