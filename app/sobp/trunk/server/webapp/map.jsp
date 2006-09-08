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
	<link href="css/brainportkaart.css" rel="stylesheet" type="text/css"/>
	<style type="text/css">
		<!--
		/* Menu css */
		.mn-container {
			top: 25px;
			left: 15px;
			font-family: Verdana, Arial, sans-serif;
			font-size: 12px;
			font-style: normal;
			font-weight: bold;
			background-color: #EE3224;
			border: none;
		}

		.mn-content {
			font-family: Verdana, Arial, sans-serif;
			font-size: 12px;
			font-style: normal;
			font-weight: bold;
			color: white;
			background-color: #EE3224;
		}

		.mn-content ul {
			font-family: Verdana, Arial, sans-serif;
			font-size: 12px;
			font-style: normal;
			font-weight: bold;
			border: none;
		}

		.mn-content li a {
			color: white;
			background: #EE3224;
			text-decoration: none;
			border: none;
		}

		.mn-content a:hover,
			.mn-content a:active {
			color: white;
			background: #333333;
		}

		#mode {
			padding-right: 8px;
		}

		#livestatus, #livestatus a:visited, #livestatus a, #status, #status a, #mode {
			color: #eeeeee;
		}

		#featureinfo, #tracerinfo {
			color: #000000;
			text-align: center;
		}

		#featuretitle {
			text-align: center;
		}

		-->
	</style>
	<script type="text/javascript" src="lib/ajax-pushlet-client.js"></script>

	<!-- get Google Maps API with right key for this server/path -->
	<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%= getGmapKey() %>"
			type="text/javascript"></script>

	<script type="text/javascript" src="lib/DHTML.js"></script>
	<script type="text/javascript" src="lib/GTApp.js"></script>

</head>

<body onunload="GUnload()">
<div id="map"></div>
<!-- Main menu starts here -->
<jsp:include page="mainmenu.jsp" flush="true"/>
<!-- Main menu ends here -->

<table width="100%" height="100%" border="0" cellspacing="0" cellpadding="0">
<tr >
	<td height="25" bgcolor="#ffffff" align="right">
		&nbsp;
	</td>
	<td height="25" width="320" bgcolor="#ffffff">
		<span id="title" class="logotekst">Sense of Brainport</span>
	</td>
</tr>
<tr>
	<td height="23" bgcolor="#EE3224">
		<table width="100%"  border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td width="25%" align="left">
					<div id="mainmenuanchor">&nbsp;</div>
				</td>
				<td width="25%" align="left">
					<span id="livestatus">&nbsp;</span>
				</td>
				<td width="25%" align="center">
					<span id="status">Status</span>
				</td>
				<td width="25%" align="right">
					<span id="mode">Archief</span>
				</td>
			</tr>
		</table>

	</td>
	<td height="23" width="320" bgcolor="#333333" class="sitewide">
		<a href="index.jsp">home</a>
		| <a href="map.jsp">kaarten</a> | <a href="deelnemers.jsp">deelnemers</a>
		| <a href="help.jsp">help</a> | <a href="colofon.jsp">colofon</a>
	</td>
</tr>
<tr>
<td align="left" valign="top">
	<div id="topanchor">&nbsp;</div>
</td>
<td width="320" valign="top" bgcolor="#eeeeee" >

	<table border="0" align="right" cellpadding="0" cellspacing="0">
		<tr>
			<td height="100">
				<table border="0" cellpadding="0" cellspacing="0">
					<tr>
						<td align="center" width="220">
							<span id="tracerinfo" class="deelnemertitel">deelnemer naam</span>
						</td>
						<td rowspan="2" height="100" width="100">
							<img id="tracerimg" height="100" width="100" src="img/default-user-thumb.jpg" align="right" alt="foto deelnemer" hspace="0" vspace="0"/>
						</td>
					</tr>
					<tr>
						<td align="left" width="220">
							<div id="tracerdesc" class="deelnemertekst">
								hier deelnemer tekst
							</div>

							</td>
					</tr>
				</table>

			</td>
		</tr>
		<tr>
			<td height="20" valign="middle" align="center" bgcolor="#EE3224">
				<div id="trackinfo" class="fragmenttitel">route naam</div>
			</td>
		</tr>
		<tr>
			<td height="60" valign="middle" align="center">
				<div id="trackview">route info</div>
			</td>
		</tr>
		<tr>
			<td valign="middle" height="18" bgcolor="#333333">
				<div id="trackcontrols" class="controls">
						<span class="control" id="trackprev"><img title="vorige GPS punt (niet beshikbaar)" src="img/media-seek-backward.png"
																  border="0 " alt="ctrl"onload="DH.fixPNG(this)" /></span>
						<span class="control" id="trackplaypause"><img title="speel of pauzeer"
																	   src="img/media-playback-start.png" border="0 "
																	   alt="ctrl" onload="DH.fixPNG(this)"/></span>
						<span class="control" id="trackstop"><img title="stop" src="img/media-playback-stop.png"
																  border="0 " alt="ctrl" onload="DH.fixPNG(this)"/></span>
						<span class="control" id="tracknext"><img title="volgende GPS punt"
																  src="img/media-seek-forward.png" border="0 "
																  alt="ctrl" onload="DH.fixPNG(this)"/></span>
				</div>
			</td>
		</tr>
		<tr>
			<td height="20" bgcolor="#EE3224">
				<div id="featuretitle" class="fragmenttitel">foto titel</div>
			</td>
		</tr>
		<tr>
			<td height="240" width="320" valign="middle" align="center">
				<div id="featurepreview">foto</div>
			</td>
		</tr>
		<tr>
			<td height="20" align="center">
				<div id="featureinfo" class="fragmenttekst">foto datum</div>
			</td>
		</tr>
		<tr>
			<td height="18" bgcolor="#333333">
				<div id="featurecontrols" class="controls">
						<span class="control" id="featfirst"><img title="eerste foto"
																  src="img/media-skip-backward.png" border="0 "
																  alt="ctrl" onload="DH.fixPNG(this)"/></span>
						<span class="control" id="featprev"><img title="vorige foto"
																 src="img/media-seek-backward.png" border="0 "
																 alt="ctrl" onload="DH.fixPNG(this)"/></span>
						<span class="control" id="featnext"><img title="volgende foto" src="img/media-seek-forward.png"
																 border="0 " alt="ctrl" onload="DH.fixPNG(this)"/></span>
						<span class="control" id="featlast"><img title="laatste foto" src="img/media-skip-forward.png"
																 border="0 " alt="ctrl" onload="DH.fixPNG(this)"/></span>
				</div>
			</td>
		</tr>
		<tr>
			<td align="left">
				<div id="featuredesc" class="fragmenttekst">foto beschrijving</div>
			</td>
		</tr>
		<tr>
			<td>
				<div class="copyright">© 2006 Sense of Brainport</div>
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
