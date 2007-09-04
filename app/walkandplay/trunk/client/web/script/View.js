/* usemedia.com . joes koppers . 04.2007 [rev 08.2007] */
/* thnx for reading this code */


//wp game view (live and playback archived)

function wpSelectView(type)
{
	wp_selected_view = type; //live or archived
	
	//get games
 	SRV.get('q-games',wpListGames);
}

// function wpListRounds(id,resp)
// {
// //	alert('list round id='+id);
// 
// 	if (!panes['list_rounds']) wpCreatePane('list_rounds');
// 	var list = '';
// 		
// 	for (var i in resp)
// 	{
// 		var roundid = resp[i].getField('id');
// 		var name = resp[i].getField('name');
// 		var desc = name;
// 		
// 		var shortname = (name.length>26)? name.substring(0,25)+'..':name;
// 		
// 		list+= '&bull; <a href="javascript://view_round" onclick="wpSelectGame(\'view_round\','+id+','+roundid+')" title="'+desc+'">'+shortname+'</a><br>';
// 	}
// 
// 	var header = '<span class="red">select a gameround:</span><br>';
// 	if (list=='') list = '- no rounds available -';
// 
// 	panes['list_rounds'].setContent(header+list);
// 	panes['list_rounds'].show();
// }

function wpCreateView(resp)
{
// 	if (!resp)
// 	{
// 		//get game-events
// 		SRV.get('get-gameplay-events',function(resp) { wpGetView(id,resp) },'id',id);
// 		return;
// 	}

	//make game-events collection
	var events = resp.getElementsByTagName('event')
	
	if (events.length==0)
	{
		document.getElementById('view_ctls').style.display = 'none';
		alert('team has not played yet')
	}
	else
	{
	
		wp_view = new wpView();
		for (var i=0; i<events.length; i++)
		{
			wp_view.events.push( new wpEvent(events[i]) );
		}
		wp_view.begin = Number(events[0].getAttribute('time'));
		wp_view.end = Number(events[events.length-1].getAttribute('time'));
		
		var d =  (wp_view.end - wp_view.begin)/1000; //seconds total
		var hrs = Math.floor(d/3600);
		var mins = Math.floor(d/60) - (hrs*60);
		var secs = Math.floor(d - (hrs*60*60) - (mins*60));
		if (hrs<10) hrs = '0'+hrs;
		if (mins<10) mins = '0'+mins;
		if (secs<10) secs = '0'+secs;
		wp_view.duration = hrs+':'+mins+':'+secs;
		
		document.getElementById('view_duration').innerHTML = '<span class="grey">playtime:</span> '+wp_view.duration;
	
		//enable playback controls
		document.getElementById('view_ctls').style.display = 'block';
	}
}

function wpView()
{
	this.events = new Array();
	
	this.state = 'paused';
	this.step = 0;
	this.progress = 0;
	this.rate = 1; //==realtime, smaller = faster
	this.rate_change = false;
	this.viewing = false;
}
wpView.prototype.startstop = function()
{
	this.state = (this.state=='paused')? 'playing':'paused';

	if (this.state=='playing')
	{
		if (this.step<this.events.length-1)
		{
			//start
			this.playback();
			//progress
			this.update(true);
			//restart blinking
			if (this.step>0) for (var id in wp_players.player) wp_players.player[id].blink();
		}
		else this.state = 'paused';
	}
	else
	{
		//stop
		if (this.viewing) window.clearTimeout(this.viewing);
		if (this.updating) window.clearTimeout(this.updating);
		this.viewing = false;
		this.updating = false;
		//stop smoothing, blinking players
		wp_players.update();
		for (var id in wp_players.player) wp_players.player[id].blink(1);
	}

	//update play/pause button
	var img = (this.state=='paused')? 'play':'pause';
	document.getElementById('view_start').src = (browser.properpngsupport)? 'media/button_'+img+'.png':'media/button_'+img+'.gif';
}

wpView.prototype.update = function(sync)
{
	if (sync || this.rate_change)
	{
		this.rate_change = false;
		this.progress = (this.events[this.step].timestamp-this.begin) * this.rate;
	}
	
 	var progress = this.progress / ((this.end-this.begin)*this.rate);
 	document.getElementById('view_progress_bar').style.width = Math.min(180,Math.round(progress * 180)) +'px';
	
	this.progress+=200;
	
	var obj = this;
	this.updating = window.setTimeout(function() { obj.update() },200);
}

wpView.prototype.playback = function()
{

// 	var progress = (time - this.begin) / (this.end-this.begin);
// 	document.getElementById('view_progress_bar').style.width = Math.round(progress * 180) +'px';
	
	//document.getElementById('view_ctls').lastChild.innerHTML = new Date(time).format('time in game: ','longtime'); //->move to round pane
	
	//var time = this.events[this.step].timestamp;
	//wpUpdatePlayRound(new Date(time).format('time in game: ','longtime'));
	
	wpUpdatePlayRound('time in game: '+this.events[this.step].time);
	
	//send event to live handler
	wpLive( this.events[this.step] );

	if (this.step<this.events.length-1)
	{
		this.step++;
		var interval = this.events[this.step].timestamp - this.events[this.step-1].timestamp;
		
		tmp_debug(3,'playback: step=',this.step,' (',this.events.length,'),interval=',(interval/1000),'s)');
		
		//continue playback
		var obj = this;
		this.viewing = window.setTimeout(function() { obj.playback() },interval * this.rate);
	}
	else
	{
		//stop view playback;
		if (this.state=='playing') this.startstop();
	}
}

wpView.prototype.setRate = function(r)
{
	this.rate = 1/r;
	this.rate_change = true;
}

wpView.prototype.rset = function()
{
	if (this.state=='playing') this.startstop();
// 	{
// 		var restart = true;
// 		this.startstop();
// 	}
// 	else var restart = false;

	//progress
	this.step = 0;
	this.progress = 0;
	document.getElementById('view_progress_bar').style.width = '0px';
	//kill players
	for (var id in wp_players.player) wp_players.del(id);
	//disable locations
	for (var id in wp_games.game[wp_selected_game].locations.location) wp_games.game[wp_selected_game].locations.location[id].enable(false);
	//update play display
	wpUpdatePlay();
	panes.hide('display');
	
	//if (restart) this.startstop();
}

function wpEvent(xml)
{
	this.timestamp = Number(xml.getAttribute('time'));
	this.time = new Date(Number(this.timestamp)).format('date',', ','longtime');
	this.xml = xml;
}
wpEvent.prototype.get = function(name)
{
	//for wpLive()
	return this.xml.getAttribute(name);
}

function wpSelectPlay(id)
{
	if (wp_view)
	{
		//if (wp_view.state=='playing') wp_view.startstop();
		wp_view.rset();
	}

	//select team, get team events, reset playback to start
	if (id!='')
	{
		wp_selected_play = Number(id);
		wp_view_state = 'paused';
		if (!wp_plays[wp_selected_play]) wp_plays.push( new wpPlay(wp_selected_play) );
		
		//get game events for this gameplay
		SRV.get('get-gameplay-events',wpCreateView,'id',id);
	}
	else 
	{
		wp_selected_play = false;
		document.getElementById('view_ctls').style.display = 'none';
		document.getElementById('view_duration').innerHTML = '';
	}
	
	wpUpdatePlay();
}

function wpLeaveView()
{
	if (wp_view) 
	{
		//if (wp_view.state=='playing') wp_view.startstop();
		wp_view.rset();
	}
	wpSelect('view');
}
