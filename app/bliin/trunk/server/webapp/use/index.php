<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<?php
	switch($_SERVER["HTTP_HOST"])
	{
		case "usemedia": $key = "ABQIAAAA6wAMqFuY8aYUX67TtQkcKRR9K9grcqoQe1tHkLoYPt38-q6EehRkJh5NYPiv32J2_6Cs7EtFN96o0w"; $type="local"; break;
		case "bliin.com": case "www.bliin.com": case "test.bliin.com": $key= "ABQIAAAA6wAMqFuY8aYUX67TtQkcKRSFYVerP0SQAckZ4lP_icglXWi2BhSgH_3wnCI0rd9qUGwD7e6DSIMphw"; $type="online"; break;
		case "test.geotracing.com": $key= "ABQIAAAAD3bxjYK2kuWoA5XU4dh89xRwJAeN3G50o_YQw1rKznRUFZZPJBQfSrLrU-Dm48ebDMIZwnAIMGhljQ"; break;
	}
	//needed for google polylines in MSIE
	if (eregi("MSIE",$_SERVER["HTTP_USER_AGENT"]))
	{
		$urn = "xmlns:v=\"urn:schemas-microsoft-com:vml\"";
		$vml = "\n<style type=\"text/css\">v\:* { behavior:url(#default#VML); }</style>\n";
	}
	//for beta development
 	$dev = eregi("dev",$_SERVER["REQUEST_URI"])? "javascript:tmp_debug('toggle')":"";
 	$title = ($dev=="")? "bliin YourLIVE! &ordm; real-time social network":"bliin &ordm; development";
 	$debug = ($dev=="")? "":"//debug mode ON\n\t\ttmp_debug('toggle');\n";
 	$defaultzoom = ($dev=="")? 2:15;
 	$queries = ($_GET['q']==1)? "ii_queries_off = false;":"ii_queries_off = true;";
 	if ($_GET['user']) $search = "ii_link = '".$_GET["user"]."'; ii_link_type = 'user';\n";
 	else if ($_GET['share']) $search = "ii_link = '".$_GET["share"]."'; ii_link_type = 'share';\n";
 	else $search = "ii_link = false;\n";
?>
<html xmlns="http://www.w3.org/1999/xhtml" <?=$urn?>>

<head><title><?=$title?></title>

<!-- //USE06 -->

<meta http-equiv="content-type" content="text/html; charset=utf-8"/>
<meta name="description" content="bliin YourLIVE! is a social networking service where users can spot, trace and share experiences - pictures, videos, audio and text - with one another in real-time on a Google Map."/>

<link rel="stylesheet" type="text/css" href="style/bliin.css"/>
<?=$vml?>

<script type="text/javascript" src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<?=$key?>"></script>

<!-- bliin gui -->

<script type="text/javascript" src="script/iiApplication.js"></script>

<script type="text/javascript" src="script/iiGui.js"></script>
<script type="text/javascript" src="script/iiPane.js"></script>
<script type="text/javascript" src="script/slider.js"></script>
<script type="text/javascript" src="script/qtcheck.js"></script>
<script type="text/javascript" src="script/humandate.js"></script>

<script type="text/javascript" src="script/iiUser.js"></script>
<script type="text/javascript" src="script/iiMedia.js"></script>

<!-- beta development -->

<script type="text/javascript" src="script/dev.js"></script>

<!-- geotracing -->

<script type="text/javascript" src="/bliin01/lib/DHTML.js"></script>
<script type="text/javascript" src="/bliin01/lib/Server.js"></script>
<script type="text/javascript" src="/bliin01/lib/Record.js"></script>
<script type="text/javascript" src="/bliin01/lib/ajax-pushlet-client.js"></script>
<script type="text/javascript" src="/bliin01/lib/KWClient.js"></script>
<script type="text/javascript" src="/bliin01/lib/KWClientExt.js"></script>
<!-- 
<script type="text/javascript" src="script/KWClientExt_mod.js"></script>
 -->

<!-- init -->

<script type="text/javascript">
//<![CDATA[

<?=$search?>

function init()
{
	if (!GBrowserIsCompatible())
	{
		alert('sorry, unsupported browser')
	}
	else
	{
		<?=$debug?>
		//iiLoadingAnim(true);
		
		/* init the google map */

		gmap = new GMap2(document.getElementById("map"));
		
		//tmp custom gmap settings
		ii_default_location = new GLatLng(52.372842,4.879949);
		//ii_default_location = new GLatLng(52.376463737430335, 4.885321855545044);
		var mapcenter = ii_default_location;
		var mapzoom = <?=$defaultzoom?>;
		//init
		gmap.setCenter(mapcenter, mapzoom);
		//default map settings
		gmap.enableDoubleClickZoom();
		gmap.addControl(new GScaleControl(),new GControlPosition(G_ANCHOR_BOTTOM_LEFT,new GSize(167,5)));
		gmap.setMapType(G_SATELLITE_TYPE); 
		gmaptype = 'sat';
		//geotracinglogo
		iiAddGTlogo();

		/* mapview settings */
	
		//map
		ii_live_traces = false; //show lines for live users
		ii_max_livetrace = 100; //max number of points in live traces
		ii_sending_gps = false;
		ii_keep_me_centered = false;
		//media
		ii_media_autosize = true;
		ii_media_random = false; //true;
		ii_media_scale =  10* Math.pow(3,(gmap.getZoom()/10));
		ii_media_scale = Math.min(100,Math.max(22,ii_media_scale));
		ii_media_expanded = false;

		/* init geotracing stuff */
		
		//queries
		<?=$queries?> //use javascript boundingbox filtering instead of server query for each update
		
		SRV.init();
		SRV.url = '/bliin01/srv/get.jsp?';
		//requests
		ii_KW_inited = false; //will be inited at login window
		//live events (pushlet)
		ii_live = false; //will be inited after media and user init
		PL._showStatus = function() { }; //no pushlet statusbar updates
	
		//build gui
		iiStartup();

		//init user management, when done, media management will be inited
		ii_users = false;
		iiGetUsers();
		
		//enable gui (activate imagemap, do login if set to auto)
		iiAutoLogin();

		/* route gmap events */
		
		ii_map_click = GEvent.addListener(gmap,"click",iiMapClick);
		ii_map_moveend = GEvent.addListener(gmap,'moveend',iiMapMoveend);
		ii_map_zoomend = GEvent.addListener(gmap,'zoomend',iiMapZoomend);
		
		window.onresize = iiWindowResize;
	}
}

function iiUnload() //-> for beta development
{
	//clear memory
	iiCleanup();
	
	//block rollover errors on refresh 
	document.getElementById('bliin_me_ctrl').innerHTML = '';	
}
//]]>
</script>
</head>

<body onload="init()" onunload="iiUnload();GUnload()">

	<!-- test browser capabilities -->
	
	<script type="text/javascript">
		ii_browser_cssfilter = (typeof(document.body.style.filter)=='string')? true:false;
		ii_browser_pngsupport = (ii_browser_cssfilter && navigator.userAgent.indexOf('MSIE 7')==-1)? false:true;
		ii_browser_safari = (navigator.userAgent.indexOf("Safari")!=-1)? true:false;
		ii_platform_windows = (navigator.userAgent.indexOf('Windows')!=-1)? true:false;
	</script>

	<!-- header -->
	
	<div id="header">

		<script type="text/javascript">document.write(tmp_dev())</script>

		<div id="bliin_logo" style="position:absolute; left:45px; top:0px; height:40px">
			<img src="media/bliin_yourlive_BETA.gif" ondblclick="tmp_debug('toggle')" style="float:left">
			<div id="live">localtime</div>
		</div>

		<div id="meta">
			<span id="togglestatus"><a href="javascript://show_status" onclick="iiToggleDisplay()">show status</a> | </span><a href="javascript://about_bliin" onclick="iiAbout('pane')">info</a><!--  | <a href="javascript://promo_videos" onclick="iiPromo('pane')">watch</a> --> | <a href="http://blog.bliin.com" target="_blank">blog</a> | <a href="http://forum.bliin.com" target="_blank">forum</a><span id="ii_logout"></span>
		</div>
		
	</div>
	
	
	<!-- Gmap -->
	
	<div id="map"></div>
	<div id="powered_by" onclick="window.open('http://www.geotracing.com')"></div>
	
	
	<!-- bliin gui -->

	<div id="bliin_me">
		<!-- activity color -->
		<div id="me_activity"><div style="left:45px; top:30px; width:90px; height:120px; background-color:#ffffff;"><div style="width:90px; height:120px;"></div></div><div style="left:30px; top:50px; width:120px; height:80px; background-color:#ffffff;"><div style="width:120px; height:80px;"></div></div></div>
		<!-- loading anim -->
		<img id="me_loading" src="media/bliin_me_anim.gif" style="visibility:hidden;">
		<!-- status color -->
		<div id="me_status"><div style="background-color:rgb(20,220,20); width:10px; height:10px;"></div></div>
		<!-- graphics -->
		<script type="text/javascript">document.write(iiGuiCreate('bliin_me'))</script>
	</div>
	
	<div id="bliin_user">
		<!-- bliin/user display -->
		<script type="text/javascript">document.write(iiGuiCreate('bliin_user'))</script>
	</div>

	
	<!-- hilights -->
    
	<div id="footer">
	</div>


	<!-- search text -->
	
	<div id="about">
		<b class="bliin">bliin</b> YourLIVE! is a social networking service where users can spot, trace and share experiences &mdash; pictures, videos, audio and text &mdash; with one another in real-time on a Google Map.<br><br>
		Users create &lsquo;bliins&rsquo; to navigate and monitor their interests in a location or area. bliins can be saved and shared amongst users.<br><br>
		<b class="bliin">bliin</b> is available for Desktop &amp; Pocket internet.<br><br>
		<b class="bliin">bliin</b> is currently testing in closed beta. Users you see are live and media uploaded in real-time.<br><br>
		You can sign up <a href="javascript:iiSignup()">here</a> to become a beta tester. We will be looking for testers based on: travel behaviour, geo-and demographic data &amp; technical compliance. We don't share email addresses.<br><br>
		A public beta is planned for early 2007.<br><br>
		Please check out our <a href="http://www.kkep.com/bliin/blog" target="_blank">blog</a> for update information about the service ... or just click around and see what you find.<br>
		<br><br><br>
		<br><br>Hope to see you around<br>With kind<br><b class="bliin">bliin</b> team
	</div>


	<!-- bliin gui panes are added below -->
	

</body>	
</html>