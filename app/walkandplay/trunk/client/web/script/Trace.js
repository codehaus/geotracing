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


function wpTrace(id,user,color)
{
	/*	obj constructor
	*/
	
	this.id = id;
	this.user = user;
//	this.track = track;
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
 	
 	var obj = this;
 	SRV.get('get-track',function(resp) { obj.parse(resp) },'id',this.id,'mindist',25);
}

wpTrace.prototype.parse = function(resp)
{	
	this.gpx = resp;
	this.getLocations();
	this.getMedia();
	
	this.update();
}

wpTrace.prototype.getLocations = function()
{
	/*	parse gpx file for segments and locations
	*/

	var segments = this.gpx.getElementsByTagName('seg');
	var points = this.gpx.getElementsByTagName('pt');
	
//	alert('trace: '+points.length+' points');
// 	for (var i in segments)
// 	{
// 		this.addSegment()
// 	}

}

wpTrace.prototype.addLocation = function()
{
	/* add location to current segment
	*/



}

wpTrace.prototype.addSegment = function()
{
	/*	determine if new segment is needed
		and create it
	*/


}

wpTrace.prototype.getMedia = function()
{
	/* parse gpx file for media
	*/

}

wpTrace.prototype.addMedium = function()
{



}

wpTrace.prototype.update = function()
{
	/*	redraw trace (for current zoomlevel)
	*/
	
	
}


wpTrace.prototype.dispose = function()
{
	/*	remove trace overlay
	*/

}
