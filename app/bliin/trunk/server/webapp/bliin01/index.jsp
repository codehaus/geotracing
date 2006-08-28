<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ include file="map/gmap-keys.jsp" %>

<!-- <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> -->
<!-- <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> -->
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml">
<head>
	<title>GeoTracing, loading please wait....</title>
	<link rel="stylesheet" type="text/css" href="css/widget.css"/>
	<link rel="stylesheet" type="text/css" href="css/gtwidget.css"/>
	<link rel="stylesheet" type="text/css" href="css/gtapp.css"/>
	<script type="text/javascript" src="lib/ajax-pushlet-client.js"></script>

	<!-- get Google Maps API with right key for this server/path -->
	<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%= getGmapKey() %>"
			type="text/javascript"></script>

	<script type="text/javascript" src="lib/DHTML.js"></script>
	<script type="text/javascript" src="lib/GTApp.js"></script>

</head>

<body onunload="GUnload()">
<div id="map"></div>

<span class="hidden" id="title"></span>
<span class="hidden" id="mode">(select mode in Show)</span>
<span class="hidden" id="livestatus">&nbsp;</span>

<div style="visibility: hidden;" id="tracerinfo" class="infohead">USER INFO </div>

<div style="visibility: hidden;" id="trackinfo" class="infohead">TRACK INFO</div>

<div style="visibility: hidden;" id="trackcontrols" class="controls">
	<span class="control" id="trackprev"> </span>
	<span class="control" id="trackplaypause"> </span>
	<span class="control" id="tracknext"> </span>
	<span class="control" id="trackstop"> </span>
</div>

<div style="visibility: hidden;" id="featuretitle" class="infohead">FEATURE TITLE</div>

<div style="visibility: hidden;" id="featurepreview">FEATURE (MEDIA+POIS) PREVIEW</div>

<div style="visibility: hidden;" id="featureinfo" class="infohead">FEATURE INFO</div>

<div style="visibility: hidden;" id="featurecontrols" class="controls">
	<span class="control" id="featfirst"> </span>
	<span class="control" id="featprev"> </span>
	<span class="control" id="featnext"> </span>
	<span class="control" id="featlast"> </span>
</div>

<div style="visibility: hidden;" id="featuredesc">feature description (if available)</div>

<div style="visibility: hidden;" id="bottomanchor">&nbsp;</div>
</body>
</html>
