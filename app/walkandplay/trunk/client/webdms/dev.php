<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<?php
	switch($_SERVER["HTTP_HOST"])
	{
		case "local.mlgk.nl":
		case "test.mlgk.nl": 
			$key = "ABQIAAAA6wAMqFuY8aYUX67TtQkcKRRGAXkkWYA3JiTOCSaqJOEk_4qGkhRYUEVifAgdoyuk3uEfBOCthrNEBg";
			break;
	}
	//needed for google polylines in MSIE
	if (eregi("MSIE",$_SERVER["HTTP_USER_AGENT"]))
	{
		$urn = "xmlns:v=\"urn:schemas-microsoft-com:vml\"";
		$vml = "\n<style type=\"text/css\">v\:* { behavior:url(#default#VML); }</style>\n";
	}
	//for beta development
 	$dev = eregi("dev",$_SERVER["REQUEST_URI"])? "javascript:tmp_debug('toggle')":"";
 	$title = ($dev=="")? "Mobile Learning Game Kit":"mlgk - development";
 	$debug = ($dev=="")? "":"tmp_debug('toggle');\n";
 	//$defaultzoom = ($dev=="")? 8:15;
 	$defaultzoom = 15;
?>
<html xmlns="http://www.w3.org/1999/xhtml" <?=$urn?>>

<head><title><?=$title?></title>

<!-- yeah! -->

<!-- //USE07 -->

<meta http-equiv="content-type" content="text/html; charset=utf-8"/>
<meta name="description" content=""/>

<link rel="stylesheet" type="text/css" href="style/wp.css"/>
<?=$vml?>

<script type="text/javascript" src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<?=$key?>"></script>

<!-- gui -->

<script type="text/javascript" src="script/common/utils.js"></script>

<script type="text/javascript" src="script/Application.js"></script>
<script type="text/javascript" src="script/GuiPublic.js"></script>

<script type="text/javascript" src="script/Game.js"></script>
<script type="text/javascript" src="script/Location.js"></script>
<script type="text/javascript" src="script/Player.js"></script>

<script type="text/javascript" src="script/Justs.js"></script>

<script type="text/javascript" src="script/common/pane.js"></script>
<script type="text/javascript" src="script/common/humandate.js"></script> 

<!-- development -->

<script type="text/javascript" src="script/common/dev.js"></script>

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
		/* init the google map */

		var mapdiv = document.getElementById('map');
		gmap = new GMap2(mapdiv);
		mapdiv.style.backgroundColor = 'rgb(153,179,204)';
		
		//custom map settings
		default_location = new GLatLng(52.37278268707501, 4.900331497192383); //waag
		var mapcenter = default_location;
		var mapzoom = <?=$defaultzoom?>;
		//init
		gmap.setCenter(mapcenter, mapzoom);
		//default map settings
		gmap.enableDoubleClickZoom();
		gmap.addControl(new GLargeMapControl(),new GControlPosition(G_ANCHOR_TOP_LEFT,new GSize(30,170)));
		gmap.addControl(new GScaleControl(),new GControlPosition(G_ANCHOR_BOTTOM_LEFT,new GSize(180,15)));
		gmap.addControl(new GMapTypeControl());
		gmap.setMapType(G_MAP_TYPE); 
		gmaptype = 'map';
		//geotracinglogo
		AddGTlogo(mapdiv);
		//fancy map edge fading
		wpMapFadeEdges(mapdiv);

		/* init gui */
				
		<?=$debug?>
		wp_max_livetrace = 100;
		
		//queries
		SRV.init();
		SRV.url = '/wp/srv/get.jsp?';
		//requests
		wp_KW_inited = false; //will be inited at login
		//live events (pushlet)
		PL._showStatus = function() { }; //no pushlet statusbar updates

		wpStartup();

		//login
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
		browser.properpngsupport = !browser.cssfilter; //IE7 still has buggy alpha png support
		browser.safari = (navigator.userAgent.indexOf("Safari")!=-1)? true:false;
		browser.windows = (navigator.userAgent.indexOf('Windows')!=-1)? true:false;
		
		//for development
		document.write(tmp_dev());
	</script>

	
	<!-- Gmap -->
	
	<div id="map"></div>
	

<!-- label test
	<div style="position:absolute; left:700px; top:50px;">

		<div class="tag" style="position: relative; float: left;">
			<img src="media/bg_tag_leftX.png"/>
			<span style="color: black; background-color: rgb(161, 203, 226);">label</span>
			<img style="margin-right: 3px;" src="media/bg_tag_rightX.png"/>
		</div>

    </div>
-->

</body>	
</html>