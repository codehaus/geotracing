<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ include file="map/gmap-keys.jsp" %>

<!-- <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> -->
<!-- <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> -->
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml">
<head>
	<title>GeoTracing - TraceLand</title>
	<link rel="stylesheet" type="text/css" href="css/widget.css"/>
	<link rel="stylesheet" type="text/css" href="css/gtwidget.css"/>
	<link rel="stylesheet" type="text/css" href="css/gtapp.css"/>
	<script type="text/javascript" src="lib/js-pushlet-client.jsp"></script>

	<!-- get Google Maps API with right key for this server/path -->
	<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%= getGmapKey() %>"
			type="text/javascript"></script>

	<script type="text/javascript" src="lib/DHTML.js"></script>
	<script type="text/javascript" src="lib/MyApp.js"></script>

</head>

<body>
<div id="map" style="position:absolute;float:left; width:40%; height:100%;border:0;"></div>
<table width="100%" height="100%" border="0" cellspacing="0" cellpadding="0">
<tr height="42">
	<td bgcolor="#000044" colspan="3">
		<div id="mainmenu" class="mn-container">
			<ul class="mn-content">
				<li><a href="#">Show</a>
					<ul>
						<li><a href="#" fn="GTAPP.mLive">Live/Active Users</a></li>
						<li><a href="#" fn="GTAPP.mArchive">Archived Tracks</a></li>
						<li><a href="#" fn="GTAPP.mAutoPlay">Autoplay Tracks</a></li>
						<li><a href="#" fn="GTAPP.mShowMediaInBbox">Random Media</a></li>
						<li><a href="#" fn="GTAPP.mShowPOIsInBbox">Points of Interest</a></li>
					</ul>
				</li>

				<li><a href="#">Map</a>

					<ul>
						<li><a href="#" fn="GTAPP.mSetMap" arg="map">Map Google</a></li>
						<li><a href="#" fn="GTAPP.mSetMap" arg="satellite">Satellite Google</a></li>
						<li><a href="#" fn="GTAPP.mSetMap" arg="hybrid">Hybrid Google</a></li>
						<li><a href="#" fn="GTAPP.mSetMap" arg="blanc">Blanc</a></li>
<!--						<li><a href="#" fn="GTAPP.mSetMap" arg="topdag">AmsterdamC - Day</a></li>
						<li><a href="#" fn="GTAPP.mSetMap" arg="topnacht">AmsterdamC - Nite</a></li>
						<li><a href="#" fn="GTAPP.mSetMap" arg="nasa">Satellite NASA</a></li>  -->

					</ul>
				</li>
				<li><a href="#">Help</a>
					<ul>
						<li><a href="#" fn="GTAPP.mShowHelp" arg="content/navhelp.html">Map Navigation</a></li>
						<li><a href="#" fn="GTAPP.mShowHelp" arg="content/trkhelp.html">Viewing Tracks and Media</a>
						</li>
						<li><a href="#" fn="GTAPP.mShowHelp" arg="content/appabout.html">About TraceLand</a></li>
						<li><a href="#" fn="GTAPP.mShowHelp" arg="content/gtabout.html">About GeoTracing</a></li>
					</ul>

				</li>
			</ul>

			<div style="clear: both;"></div>
		</div>
	</td>
</tr>
<tr bgcolor="#000066">
	<td height="20" align="left">
		<span id="mode">(select a mode from Show menu)</span>
	</td>
	<td height="20" align="right">
		<span id="status">Status</span>
	</td>
	<td width="320" bgcolor="#000066">
		&nbsp;
	</td>

</tr>
<tr>
	<td align="left" valign="top" colspan="2">
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
				<td valign="middle" height="18" >
					<div id="trackcontrols" class="controls">
						<span class="control" id="trackprev">[prev]</span>
						<span class="control" id="trackplaypause">[play]</span>
						<span class="control" id="trackstop">[stop]</span>
						<span class="control" id="tracknext">[next]</span>
					</div>
				</td>
			</tr>
			<tr>
				<td height="20" bgcolor="#000066">
					<div id="featuretitle" class="infohead">FEATURE TITLE</div>
				</td>
			</tr>
			<tr>
				<td height="240" valign="middle" align="center" bgcolor="#dddddd">
					<div id="featurepreview">FEATURE PREVIEW</div>
				</td>
			</tr>
			<tr>
				<td height="20" align="center" bgcolor="#0066FF">
					<div id="featureinfo" class="infohead">FEATURE INFO</div>
				</td>
			</tr>
			<tr>
				<td height="18" >

					<div id="featurecontrols" class="controls">
						<span class="control" id="featfirst">|&lt;</span>
						<span class="control" id="featprev">&lt;&lt;</span>
						<span class="control" id="featnext">&gt;&gt;</span>
						<span class="control" id="featlast">&gt;|</span>
					</div>


				</td>
			</tr>
			<tr>
				<td>
					<div id="featuredescr">
						Phrygium enim adiectivum est absolute positum, ut in re Vestiaria fieri amat; subintelligitur
						autem Phtygium
						pileum, aut phrygium lorum, aut tale aliquid. Similia sunt vitta et fascia; quae et
						de vinculo capitis et de vestium ornatura, de qua retro diximus in Lorata, usurpata occurrunt.
						Quemadm odum et Lorum utrumque significat et vestis praetextam et diadema; et pezi/dion,
						quod proprie oram vestis, vel quod orae adsutum est denotat, ad caput translatum est et pro
						diademate regio usurpatum, Salmas. ad Vopisc. in Aurel. In specie Lorum appellavit posterior
						aetas fasciam illam Consularem, quam prior clavum vel subarmalem vocavit; quod Lori s. cinguli
						formam referret.
					</div>

					</td>
			</tr>
			<tr>
				<td>
					<div id="bottomanchor">&nbsp</div>
					<script type="text/javascript">p_embed('/gt')</script>
				</td>
			</tr>
		</table>

	</td>
</tr>
</table>
</body>
</html>
