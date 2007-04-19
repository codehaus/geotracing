<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<?php
	switch($_SERVER["HTTP_HOST"])
	{
		case "usemedia": $key = "ABQIAAAA6wAMqFuY8aYUX67TtQkcKRR9K9grcqoQe1tHkLoYPt38-q6EehRkJh5NYPiv32J2_6Cs7EtFN96o0w"; $type="local"; break;
		case "test.walkandplay.com": $key= "ABQIAAAA6wAMqFuY8aYUX67TtQkcKRRjLdUAkr7fvd8TLmN5lAekOOCGcRQJIt7caapB5n-IidVPPNGWWOhDTw"; $type="online"; break;
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
 	$title = ($dev=="")? "Mobile Game Learning Kit":"mlgk - development";
 	$debug = ($dev=="")? "":"//debug mode ON\n\t\ttmp_debug('toggle');\n";
 	$defaultzoom = ($dev=="")? 13:15;
?>
<html xmlns="http://www.w3.org/1999/xhtml" <?=$urn?>>

<head><title><?=$title?></title>

<!-- //USE07 -->

<meta http-equiv="content-type" content="text/html; charset=utf-8"/>
<meta name="description" content=""/>

<link rel="stylesheet" type="text/css" href="style/wp.css"/>
<?=$vml?>

<script type="text/javascript" src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<?=$key?>"></script>

<!-- gui -->

<script type="text/javascript" src="script/Application.js"></script>
<script type="text/javascript" src="script/Gui.js"></script>

<script type="text/javascript" src="script/Game.js"></script>
<script type="text/javascript" src="script/Location.js"></script>

<script type="text/javascript" src="script/pane.js"></script>
<script type="text/javascript" src="script/utils.js"></script>

<!-- 
<script type="text/javascript" src="script/slider.js"></script>
<script type="text/javascript" src="script/qtcheck.js"></script>
<script type="text/javascript" src="script/humandate.js"></script> 
-->


<!-- development -->

<script type="text/javascript" src="script/dev.js"></script>

<!-- geotracing -->

<script type="text/javascript" src="/wp/lib/DHTML.js"></script>
<script type="text/javascript" src="/wp/lib/Server.js"></script>
<script type="text/javascript" src="/wp/lib/Record.js"></script>
<script type="text/javascript" src="/wp/lib/ajax-pushlet-client.js"></script>

<script type="text/javascript" src="/wp/lib/KWClient.js"></script>
<script type="text/javascript" src="/wp/lib/KWClientExt.js"></script>
<script type="text/javascript" src="/wp/lib/KWClientWP.js"></script>

<!-- init -->

<script type="text/javascript">
//<![CDATA[

function init()
{
	if (!GBrowserIsCompatible())
	{
		alert('sorry, unsupported browser')
	}
	else
	{
		<?=$debug?>
		
		/* init the google map */

		var mapdiv = document.getElementById('map');
		gmap = new GMap2(mapdiv);
		
		//tmp custom gmap settings
		default_location = new GLatLng(52.37278268707501, 4.900331497192383); //waag
		var mapcenter = default_location;
		var mapzoom = <?=$defaultzoom?>;
		//init
		gmap.setCenter(mapcenter, mapzoom);
		//default map settings
		gmap.enableDoubleClickZoom();
		//gmap.addControl(new GScaleControl(),new GControlPosition(G_ANCHOR_BOTTOM_LEFT,new GSize(167,5)));
		gmap.addControl(new GLargeMapControl(),new GControlPosition(G_ANCHOR_TOP_LEFT,new GSize(35,170)));
		gmap.addControl(new GMapTypeControl());
		
		gmap.setMapType(G_MAP_TYPE); 
		gmaptype = 'map';
		//geotracinglogo
		AddGTlogo(mapdiv);
		//fancy map edge fading
		wpMapFadeEdges(mapdiv);

//		maploaded = GEvent.addListener(gmap,'load',wpMapFadeEdges);
		
// 		mapdiv.childNodes[2].style.left = '12px'; mapdiv.childNodes[2].style.bottom = '10px';
// 		mapdiv.childNodes[1].style.right = '13px'; mapdiv.childNodes[1].style.bottom = '12px';

		
		wpStartup();
		
		/* mapview settings */
	
		//map
// 		ii_live_traces = false; //show lines for live users
// 		ii_max_livetrace = 100; //max number of points in live traces
// 		ii_sending_gps = false;
// 		ii_keep_me_centered = false;
// 		//media
// 		ii_media_autosize = true;
// 		ii_media_random = false; //true;
// 		ii_media_scale =  10* Math.pow(3,(gmap.getZoom()/10));
// 		ii_media_scale = Math.min(100,Math.max(22,ii_media_scale));
// 		ii_media_expanded = false;

		/* init geotracing stuff */
		
		//queries
		SRV.init();
		SRV.url = '/wp/srv/get.jsp?';
		//requests
		wp_KW_inited = false; //will be inited at login window

/*		//live events (pushlet)
		wp_live = false; //will be inited after media and user init
		PL._showStatus = function() { }; //no pushlet statusbar updates
*/
		//build gui
//		iiStartup();

		//init user management, when done, media management will be inited
// 		ii_users = false;
// 		iiGetUsers();
		
		//enable gui (activate imagemap, do login if set to auto)
 		wpAutoLogin();

		/* route gmap events */
		
		wp_map_click = GEvent.addListener(gmap,"click",wpMapClick);
		wp_map_moveend = GEvent.addListener(gmap,'moveend',wpMapMoveend);
		wp_map_zoomend = GEvent.addListener(gmap,'zoomend',wpMapZoomend);
		
		window.onresize = wpWindowResize;
		
	
	}
}

//]]>
</script>
</head>

<body onload="init()" onunload="GUnload()" scroll="no">

	<!-- test browser capabilities -->
	
	<script type="text/javascript">
		browser = new Object();
		browser.cssfilter = (typeof(document.body.style.filter)=='string')? true:false;
		browser.pngsupport = (browser.cssfilter && navigator.userAgent.indexOf('MSIE 7')==-1)? false:true;
		browser.safari = (navigator.userAgent.indexOf("Safari")!=-1)? true:false;
		browser.windows = (navigator.userAgent.indexOf('Windows')!=-1)? true:false;
		
		//for development
		document.write(tmp_dev());
	</script>

	
	<!-- Gmap -->
	
	<div id="map"></div>
	

<!--
	<div style="position:absolute; left:700px; top:50px;">

		<div class="tag" style="position: relative; float: left;">
			<img src="media/bg_tag_leftX.png"/>
			<span style="color: black; background-color: rgb(161, 203, 226);">label</span>
			<img style="margin-right: 3px;" src="media/bg_tag_rightX.png"/>
		</div>

    </div>
-->

<!-- 
	<div id="footer">
	</div>
 -->


</body>	
</html>