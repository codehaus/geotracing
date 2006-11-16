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
<table width="100%" height="100%" border="0" cellspacing="0" cellpadding="0">
<tr height="42">
	<td bgcolor="#000044">
		<!-- Main menu starts here -->
		<jsp:include page="mainmenu.jsp" flush="true" />
		<!-- Main menu ends here -->
	</td>
	<td width="320" bgcolor="#000044" align="right">
		<span id="title">Untitled</span>
	</td>
</tr>
<tr bgcolor="#000066">
	<td height="20">
		<table width="100%" height="20" border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td width="33%" align="left">
					<span id="mode">(select mode in Show)</span>
				</td>
				<td width="33%" align="center">
					<span id="livestatus">&nbsp;</span>
				</td>
				<td width="34%" align="right">
					<span id="status">Status</span>
				</td>
			</tr>
		</table>

	</td>
	<td width="320" bgcolor="#000066" align="right">
		<span id="cmtlink"><a href="#" onclick="CMT.showCommentPanel(23, '', 'This Site')" >[comments]</a>&nbsp;&nbsp;</span>
	</td>
</tr>
<tr>
	<td align="left" valign="top" >
		<div id="topanchor"></div>
	</td>
	<td width="320" valign="top" bgcolor="#CCE9F9">

		<table border="0" align="right" cellpadding="0" cellspacing="0">
			<tr>
				<td height="20" bgcolor="#660000" align="center">
					<div id="tracerinfo" class="infohead">
						USER INFO
					</div>
				</td>
			</tr>
			<tr>
				<td height="20" valign="middle" align="center" bgcolor="#000066">
					<div id="trackinfo" class="infohead">TRACK INFO</div>
				</td>
			</tr>
			<tr>
				<td height="60" valign="middle" align="center" bgcolor="#dddddd">
					<div id="trackview">TRACK PREVIEW</div>
				</td>
			</tr>
			<tr>
				<td valign="middle" height="18" bgcolor="#111111">
					<div id="trackcontrols" class="controls">
						<span class="control" id="trackprev"><img title="prev" src="img/media-seek-backward.png" border="0 "alt="ctrl" onload="DH.fixPNG(this)" /></span>
						<span class="control" id="trackplaypause"><img title="play or pause" src="img/media-playback-start.png" border="0 "alt="ctrl" onload="DH.fixPNG(this)"  /></span>
						<span class="control" id="trackstop"><img title="stop" src="img/media-playback-stop.png" border="0 "alt="ctrl" onload="DH.fixPNG(this)" /></span>
						<span class="control" id="tracknext"><img title="next GPS point" src="img/media-seek-forward.png" border="0 "alt="ctrl" onload="DH.fixPNG(this)" /></span>
					</div>
				</td>
			</tr>
			<tr>
				<td height="20" bgcolor="#000066">
					<div id="featuretitle" class="infohead">FEATURE TITLE</div>
				</td>
			</tr>
			<tr>
				<td height="240" width="320" valign="middle" align="center" bgcolor="#dddddd">
					<div id="featurepreview">FEATURE (MEDIA+POIS) PREVIEW</div>
				</td>
			</tr>
			<tr>
				<td height="18" bgcolor="#111111">
					<div id="featurecontrols" class="controls">
						<span class="control" id="featfirst"><img title="first feature" src="img/media-skip-backward.png" border="0 "alt="ctrl"onload="DH.fixPNG(this)"  /></span>
						<span class="control" id="featprev"><img title="previous feature" src="img/media-seek-backward.png" border="0 "alt="ctrl" onload="DH.fixPNG(this)" /></span>
						<span class="control" id="featnext"><img title="next feature" src="img/media-seek-forward.png" border="0 "alt="ctrl" onload="DH.fixPNG(this)" /></span>
						<span class="control" id="featlast"><img title="last feature" src="img/media-skip-forward.png" border="0 "alt="ctrl" onload="DH.fixPNG(this)" /></span>
					</div>
				</td>
			</tr>
			<tr>
				<td align="left">
					<div id="featureinfo">FEATURE INFO</div>
					<div id="featuredesc">feature description (if available)</div>
				</td>
			</tr>
			<tr>
				<td>
					<div id="bottomanchor">&nbsp;</div>
				</td>
			</tr>
		</table>

	</td>
</tr>
</table>
</body>
</html>
