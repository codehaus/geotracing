/* usemedia.com . joes koppers . 09.2007 */
/* thnx for reading this code */


//wp game players (traces)

function wpTraces()
{
	var array = new idArray('trace');
	
	//extend obj with update function
	array.update = function()
	{
		for (id in this.trace) this.trace[id].update();
	}
	
	return array;
}


function wpTrace(id,trackid,color)
{
	/*	obj constructor
	*/
	
	this.id = this.user = id; //==user name
	this.trackid = trackid;
	this.color = color;
	
	this.segments = [];

	this.media = new wpLocations('trace');

	var c;
	switch (color.substring(0,1))
	{
		case 'r': c = '#c80014'; break;
		case 'g': c = '#2daa4b'; break;
		case 'b': c = '#3264c8'; break;
		case 'y': c = '#ffd02b'; break;
		case 'o': c = '#df8021'; break;
		case 'p': c = '#8d2bcb'; break;
	}
 	this.tracecolor = c;
 	/// this.tracecolor = '#00ff00'; //debug
 	
 	var obj = this;
 	SRV.get('get-track',function(resp) { obj.parse(resp) },'id',this.trackid,'mindist',25);
}

wpTrace.prototype.parse = function(resp)
{
	/*	parse the gpx resp, get points and media
	*/
	
	this.gpx = resp;

	//get (segments and) points
	var segments = this.gpx.getElementsByTagName('seg');
	for (var i=segments.length-1; i>=Math.max(0,segments.length-10); i--) this.addSegment(segments[i]); //only use last 10 segments (?)
//	for (var i=0; i<segments.length; i++) this.addSegment(segments[i]);

	//get media (only for current user)
	if (wp_login.loginname==this.user)
	{
		var media = this.gpx.getElementsByTagName('medium');
		for (var i=0; i<media.length; i++) this.addMedium(media[i]);
	}
	
	this.update();
}

wpTrace.prototype.addSegment = function(segment)
{
	/*	add new segment to trace, and fill it with points
	*/
	
	this.segments.push( { trace:false, points:new Array() } );

	var points = segment.childNodes;
	for (var i=0; i<points.length; i++) this.addPoint(points[i]);
}

wpTrace.prototype.needSegment = function(point)
{
	/*	determine if new segment is created:
		if distance from last point > 500m */
	
	//get last point
	var lastsegment = this.segments[this.segments.length-1];
	var lastpoint = (lastsegment.points.length>0)? lastsegment.points[lastsegment.points.length-1]:false;

	if (lastpoint) tmp_debug(3,'[',this.user,'] ','needSegment: d=',point.distanceFrom(lastpoint),'->',point.distanceFrom(lastpoint)>500);
	
	if (lastpoint && point.distanceFrom(lastpoint)>500) return true;
	else return false;
}

wpTrace.prototype.addPoint = function(pt)
{
	/* add point to trace(segment)
	*/

	var last = this.segments.length-1;
	
//	var t = pt.getAttribute('t');
	var lat = pt.getAttribute('lat');
	var lon = pt.getAttribute('lon');
	var geo = new GLatLng(lat,lon);
		
//	this.segments[last].points.push( { geo:geo, timestamp:t } );
	this.segments[last].points.push(geo);
}

wpTrace.prototype.addLivePoint = function(geo)
{
	/* 	add live point to trace(segment)
		check first if new segment is needed */

	if (this.segments.length==0 || this.needSegment(geo))
	{
		this.segments.push( { trace:false, points:new Array() } );
	}
	
	this.segments[this.segments.length-1].points.push(geo);
	this.update();
}

wpTrace.prototype.addMedium = function(medium)
{
	/* add location (medium)
	*/

	var id = medium.getAttribute('id');
	var name = medium.getAttribute('name');
	var kind = medium.getAttribute('kind');
	var lat = medium.getAttribute('lat');
	var lon = medium.getAttribute('lon');
	var geo = new GLatLng(lat,lon);
	
	this.media.push( new wpLocation({trace:true},id,geo,'medium','enabled',name) );
}

wpTrace.prototype.addLiveMedium = function(medium)
{
	/* add live uploaded medium
	*/
	
	var id = medium.id;
	var name = medium.name;
	var lat = medium.lat;
	var lon = medium.lon;
	var geo = new GLatLng(lat,lon);
	
	this.media.push( new wpLocation({trace:true},id,geo,'medium','enabled',name) );
	this.update();
}

wpTrace.prototype.update = function()
{
	/*	redraw trace (segments) (for current zoomlevel)
		and update (media)locations
	*/
	
	for (var s in this.segments)
	{
		var segment = this.segments[s];

		if (segment.points.length<2) continue; //we need two points minimum	

		tmp_debug(1,'update trace id=',this.id,', segment=',s);
	
		//line-width adjusted to mapview
		var w = gmap.getZoom()/4;
		w = (w>4)? Math.round(w):Math.floor(w);

		//remove current trace
		if (segment.trace) gmap.removeOverlay(segment.trace);
		//generate and draw new trace
		segment.trace = new GPolyline(segment.points,this.tracecolor,Math.max(1,w),0.8);
		gmap.addOverlay(segment.trace);
	}
	
	if (this.media.length>0) this.media.update();
}

wpTrace.prototype.dispose = function()
{
	/*	remove trace overlay and media
	*/

	for (var s in this.segments)
	{
		var segment = this.segments[s];
		if (segment.trace) gmap.removeOverlay(segment.trace);
	}
	
	if (this.media) 
	{
		tmp_debug(3,'removing media');
		for (id in this.media.location) this.media.del(id);
	}
}