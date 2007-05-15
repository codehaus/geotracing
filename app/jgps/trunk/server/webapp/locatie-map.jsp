<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!-- <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> -->
<!-- <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> -->
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml">
<head>
	<title>Jeugd GePoSitioneerd, laden...</title>
	<link rel="stylesheet" type="text/css" href="css/widget.css"/>
	<link rel="stylesheet" type="text/css" href="css/gtwidget.css"/>
	<link rel="stylesheet" type="text/css" href="css/gtapp.css"/>
	<link href="css/brainportkaart.css" rel="stylesheet" type="text/css"/>
	<style type="text/css">
		<!--
		/* Overrule Menu css for SOBP design */
		.mn-container {
			top: 25px;
			left: 15px;
			font-family: Verdana, Arial, sans-serif;
			font-size: 12px;
			font-style: normal;
			font-weight: bold;
			background-color: #004b7d;
			border: none;
		}

		.mn-content {
			font-family: Verdana, Arial, sans-serif;
			font-size: 12px;
			font-style: normal;
			font-weight: bold;
			color: white;
			background-color: #004b7d;
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
			background: #004b7d;
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

		.cmtlink a, .cmtlink a:visited {
		  font-size: 10px;
		  color: #0000ff;
		  text-decoration: none;
		}

		-->
	</style>
	<!--<script type="text/javascript" src="lib/ajax-pushlet-client.js"></script>-->
	<script type="text/javascript" src="lib/DHTML.js"></script>
	<script type="text/javascript" src="lib/GMap.js"></script>
	<script type="text/javascript" src="lib/GMapKeys.js"></script>
	<script type="text/javascript" src="lib/KWClient.js"></script>
	<script type="text/javascript" src="lib/KWClientExt.js"></script>
	<script type="text/javascript" src="lib/LocationApp.js"></script>
	<script type="text/javascript" src="lib/GTApp.js"></script>

</head>

<body onunload="GUnload()">
<div id="map"></div>

<!-- Main menu starts here, loaded dynamically -->
<div id="menucontainer"></div>
<!-- Main menu ends here -->

<table width="100%" height="100%" border="0" cellspacing="0" cellpadding="0">
<tr >
	<td height="25" bgcolor="#ffffff" align="right">&nbsp;
		
	</td>
	<td height="25" width="320" bgcolor="#ffffff">
		<span class="logotekst">Jeugd GePoSitioneerd</span>
	</td>
</tr>
<tr>
	<td height="23" bgcolor="#004b7d">
		<table width="100%"  border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td width="25%" align="left">
					<div id="mainmenuanchor">&nbsp;</div>
				</td>
				<td width="25%" align="left">
					<span id="mode" style="visibility: hidden">&nbsp;</span>
				</td>
				<td width="50%" align="right">
					<span id="status">&nbsp;</span>
				</td>
			</tr>
		</table>

	</td>
	<td height="23" width="320" bgcolor="#004b7d" class="sitewide">
		<a href="index.jsp">home</a>
		| <a href="deelnemers.jsp">gemeentes</a>
		| <a href="help.jsp">help</a> | <a href="colofon.jsp">partners</a>
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
						<td align="left" width="220">
							<div id="session"/>

							</td>
					</tr>
				</table>

			</td>
		</tr>
		<tr>
			<td height="20" valign="middle" align="center" bgcolor="#004b7d">
				<div id="debug" class="fragmenttitel"></div>
			</td>
		</tr>
		<tr>
			<td>
				<!--<img id="tracerimg" height="100" width="100" src="img/default-user-thumb.jpg" align="right" alt="foto deelnemer" hspace="0" vspace="0"/>-->
			</td>
		</tr>
		<tr>
			<td height="60">
				<div id="result"></div>
			</td>
		</tr>
		<tr>
			<td valign="middle" height="18" bgcolor="#004b7d">
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
			<td height="20" bgcolor="#004b7d">
				<div id="featuretitle" class="fragmenttitel">foto titel</div>
			</td>
		</tr>
		<tr>
			<td height="240" width="320" valign="middle" align="center">
				<div id="featurepreview"></div>
			</td>
		</tr>
		<tr>
			<td height="20" align="center">
				<div id="featureinfo" class="fragmenttekst">foto datum</div>
			</td>
		</tr>
		<tr>
			<td height="18" bgcolor="#004b7d">
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
				<div class="copyright">© 2007 Jeugd GePoSitioneerd</div>
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
