/* usemedia.com . joes koppers . 04.2007 */
/* thnx for reading this code */


//wp gui for loggedin user

function wpGuiUserInit()
{
	//enable registered user functions
		
	//add logout button
	tmp_debug('2','GuiUser.js loaded','querytime');
	
	//send heartbeats
	wp_hb = window.setInterval('wpHeartBeat()',60000*5) //send hb every minute (=idle user)
	
	wpCreatePane('edit_game');
	wpCreatePane('edit_location');

	//wpGameInit();
}

function wpHeartBeat()
{
	var req = KW.createRequest('play-hb-req');
	KW.utopia(req);
}

function wpCreatePaneUser(type)
{
	switch (type)
	{
		/* registered user panes */
					
		case 'edit_game':
			var pane = new Pane('edit_game',100,160,180,160,1,true);
			pane.setContent(wpGuiCreate('edit_game'));
			break;

		case 'edit_location':
			var pane = new Pane('edit_location',310,60,300,210,1,true,gmap.getPane(G_MAP_FLOAT_PANE));
			pane.setContent(wpGuiCreate('edit_location'));
			pane.content.style.lineHeight = '9px';
			//block gmap events (since pane is attached to map pane)
			pane.content.onmousedown = function(e)
			{
				if (!e) e = event;
				cancelEvents(e);
			}
			break;
	}
}

//pngfixed html
function wpGuiCreateUser(type,s,id,n)
{
	var str='';
	
	switch(type)
	{
		/* user panes */
		case 'edit_location':
			str+= '<form name="locationform" method="post" action="/wp/media.srv" enctype="multipart/form-data">';
			str+= '<span class="title">location</span><br><br>';
			str+= '<div class="column">name:</div><input type="text" name="name" class="inputtext" style="width:130px; margin-right:10px">';
			str+= '<select name="type" onchange="wpEditLocation(this.value)" style="height:16px;"><option value="task">task</option><option value="medium">medium</option><option value="text">text</option></select><br><br>';
			
			//task fields
			str+= '<div id="taskform" style="position:relative; margin-left:-5px; padding:5px; width:295px; background-color:#d5d5d5; margin-bottom:10px;">';
			str+= '<div class="column">task:</div><textarea name="desc" style="width:230px; height:50px;"></textarea><br><br>';
			str+= '<div class="column">answer:</div><input type="text" class="inputtext" name="answer" style="width:130px; margin-right:10px;">';
			str+= 'score: <select name="score">'
			var scores = [25,50,75,100,200];
			for (var i in scores) str+= '<option value="'+scores[i]+'">'+scores[i]+'</option>';
			str+= '</select>';
			str+= '</div>';
			
			str+= '<div id="textform" style="display:none; position:relative; margin-left:-5px; padding:5px; width:295px; background-color:#d5d5d5; margin-bottom:10px;">';
			str+= '<div class="column">text:</div><textarea name="text" style="width:230px; height:50px;"></textarea><br><br>';
			str+= '</div>';
			
			//medium
			str+= '<div id="mediumform" style="position:relative">';
			str+= '<div id="mediumtype" class="column">medium:</div><input type="file" name="file" class="inputtext" style="height:16px;">';
			str+= '</div>';
			str+= '<div id="mediumdescform" style="display:none; position:relative; margin-top:8px;">';
			str+= '<div class="column">desc:</div><textarea name="description" style="width:230px; height:50px;"></textarea><br><br>';
			str+= '</div>';
			
			str+= '<div style="right:15px; bottom:15px;"><input type="button" value="cancel" onclick="wpCancelLocation()">&nbsp;<input type="submit" value=" add " class="red"></div>';
			str+= '</form>';
			break;
						
		default:
			break;
	}

	return str;
}

///init when loaded
wpGuiUserInit();