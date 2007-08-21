/*
* Justs gui additions to WP.
*
*
* PURPOSE
* Provide functionality to create and manage games/gamerounds without
* interfering with Joes' code.
*
*/

JO = {

	gameCreate: function() {
		var pane = new Pane('game_create',340,188,300,300,1,true);
		var str = '';
		str+= '<form name="creategameform" method="post" action="#"">';
		str+= '<span class="title">create new game</span><br><br>';
		str+= '<div id="taskform" style="position:relative; margin-left:-5px; padding:5px; width:295px; background-color:#d5d5d5; margin-bottom:10px;">';
		str+= '<div class="column">name*:</div><input type="text" name="name" class="inputtext" style="width:180px; margin-right:10px">';

		str+= '<div class="column">desc*:</div><textarea name="desc" style="width:230px; height:50px;"></textarea><br><br>';
		str+= '<div class="column">intro:</div><textarea name="intro" style="width:230px; height:50px;"></textarea><br><br>';
		str+= '<div class="column">outro:</div><textarea name="outro" style="width:230px; height:50px;"></textarea><br><br>';
		str+= '</div>';

		str+= '<div style="right:15px; bottom:5px;"><input type="button" value="cancel" onclick="JO.onGameCreateCancel()">&nbsp;<input type="button" value=" create " class="red" onclick="JO.onGameCreate()"></div>';
		str+= '</form>';

		pane.setContent(str);
		pane.show();
	},


	onGameCreate: function() {
		var form = document.forms['creategameform'];
		KW.WP.gameCreate(JO.onGameCreateRsp, form['name'].value, form['desc'].value, form['intro'].value, form['outro'].value);
	},

	onGameCreateRsp: function(rsp) {
		panes['game_create'].hide();
		panes['game_create'].dispose();
		alert('new game created OK ! id=' + rsp.getAttribute('id') + ' (refresh Create button to view/edit)');
	},

	onGameCreateCancel: function() {
		panes['game_create'].hide();
		panes['game_create'].dispose();
	},

	gameUpdate: function(gameId) {
		var gameRec = SRV.get('q-game', null, 'id', gameId)[0];

		var pane = new Pane('game_update',340,188,300,300,1,true);
		var str = '';
		str+= '<form name="updategameform" method="post" action="#"">';
		str+= '<span class="title">edit game info</span><br><br>';
		str+= '<div id="taskform" style="position:relative; margin-left:-5px; padding:5px; width:295px; background-color:#d5d5d5; margin-bottom:10px;">';
		str+= '<div class="column">name:</div><input type="text" name="name" class="inputtext" style="width:180px; margin-right:10px" value="' + gameRec.getField('name') + '">';

		str+= '<div class="column">desc:</div><textarea name="desc" style="width:230px; height:50px;">' + gameRec.getField('description') + '</textarea><br><br>';
		str+= '<div class="column">intro:</div><textarea name="intro" style="width:230px; height:50px;">' + gameRec.getField('intro') + '</textarea><br><br>';
		str+= '<div class="column">outro:</div><textarea name="outro" style="width:230px; height:50px;">' + gameRec.getField('outro') + '</textarea><br><br>';
		str+= '<input type="hidden" name="id" value="' + gameId + '"></div>';

		str+= '<div style="right:15px; bottom:5px;"><input type="button" value="cancel" onclick="JO.onGameUpdateCancel()">&nbsp;<input type="button" value=" update " class="red" onclick="JO.onGameUpdate()"></div>';
		str+= '</form>';

		pane.setContent(str);
		pane.show();
	},


	onGameUpdate: function() {
		var form = document.forms['updategameform'];
		KW.WP.gameUpdate(JO.onGameUpdateRsp, form['id'].value, form['name'].value, form['desc'].value, form['intro'].value, form['outro'].value);
	},

	onGameUpdateRsp: function(rsp) {
		panes['game_update'].hide();
		panes['game_update'].dispose();
		alert('new game updated OK ! id=' + rsp.getAttribute('id') + ' (refresh Create button to view/edit)');
	},

	onGameUpdateCancel: function() {
		panes['game_update'].hide();
		panes['game_update'].dispose();
	},

	roundCreate: function(gameId) {
		var pane = new Pane('round_create',340,188,300,300,1,true);
		var str = '';
		str+= '<form name="createroundform" method="post" action="#"">';
		str+= '<span class="title">add game round</span><br><br>';
		str+= '<div id="taskform" style="position:relative; margin-left:-5px; padding:5px; width:295px; background-color:#d5d5d5; margin-bottom:10px;">';
		str+= '<div class="column">name*:</div><input type="text" name="name" class="inputtext" style="width:180px; margin-right:10px">';

		str+= '<div class="column">players*:</div><textarea name="players" style="width:230px; height:50px;"></textarea><br><br>';
		str+= '<div>add comma-separated list of players e.g. "ted,carol,bob" (without quotes/spaces)</div><br><br>';
		str+= '</div>';

		str+= '<div style="right:15px; bottom:5px;"><input type="button" value="cancel" onclick="JO.onRoundCreateCancel()">&nbsp;<input type="button" value=" create " class="red" onclick="JO.onRoundCreate(' + gameId + ')"></div>';
		str+= '</form>';

		pane.setContent(str);
		pane.show();
	},

	onRoundCreate: function(gameId) {
		var form = document.forms['createroundform'];
		KW.WP.roundCreate(JO.onRoundCreateRsp, gameId, form['name'].value, form['players'].value);
	},

	onRoundCreateRsp: function(rsp) {
		panes['round_create'].hide();
		panes['round_create'].dispose();
		alert('new round created OK ! id=' + rsp.getAttribute('id') + ' (refresh Create button to view/edit)');
	},

	onRoundCreateCancel: function() {
		panes['round_create'].hide();
		panes['round_create'].dispose();
	},

	roundDelete: function(gameId) {
		var rounds = SRV.get('q-gamerounds', null, 'gameid', gameId);

		var pane = new Pane('round_delete',340,188,300,300,1,true);
		var str = '';
		str+= '<form name="deleteroundform" method="post" action="#"">';
		str+= '<span class="title">delete game round</span><br><br>';
		str+= '<div id="taskform" style="position:relative; margin-left:-5px; padding:5px; width:295px; background-color:#d5d5d5; margin-bottom:10px;">';
		if (rounds.length==0) {
			str+= '- no gamerounds available for this game -<br>';
		} else {
			str+= '<select name="round"">';
			str+= '<option value="">select a gameround..</option>';
			str+= '<option value=""></option>';
			for (var i=0; i < rounds.length; i++) {
				str+= '<option value="' + rounds[i].id + '">' + rounds[i].getField('name') + '</option>';
			}
			str+= '<option value=""></option>';
			str+= '</select><br>';
			str+= '<br><div class="red">DELETION IS IMMEDIATE !!!</div>';
			str+= '<br><div class="red">ALL RELATED GAMEPLAYS WILL ALSO BE DELETED !!!</div><br><br>';
		}

		//task fields
		str+= '</div>';

		str+= '<div style="right:15px; bottom:5px;"><input type="button" value="cancel" onclick="JO.onRoundDeleteCancel()">&nbsp;<input type="button" value=" delete " class="red" onclick="JO.onRoundDelete()"></div>';
		str+= '</form>';

		pane.setContent(str);
		pane.show();
	},

	onRoundDelete: function() {
		var form = document.forms['deleteroundform'];
		var roundId = form['round'].value;
		if (!roundId) {
			alert('no rounds for this game');
			JO.onRoundDeleteCancel();
		}
		KW.WP.roundDelete(JO.onRoundDeleteRsp, roundId);
	},

	onRoundDeleteRsp: function(rsp) {
		panes['round_delete'].hide();
		panes['round_delete'].dispose();
		alert('round deleted OK !');
	},

	onRoundDeleteCancel: function() {
		panes['round_delete'].hide();
		panes['round_delete'].dispose();
	}
}