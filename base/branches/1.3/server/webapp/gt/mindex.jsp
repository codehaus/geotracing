<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ include file="map/gmap-keys.jsp" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8"/>
<title>Map</title>

<style type="text/css">
	html, body, table {
		width: 100%;
		height: 100%;
	}

	html {
		overflow: hidden;
	}

	body {
		margin: 0px 0px 0px 0px;
		padding: 0px 0px 0px 0px;
	}
</style>

<script type="text/javascript" src="lib/ajax-pushlet-client.js"></script>

<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%= getGmapKey() %>"
		type="text/javascript"></script>
<script type="text/javascript" src="lib/TLabel.js"></script>

<script type="text/javascript">

	//<![CDATA[

	var map = null;
	var geocoder = null;
	var center = null;
	var tl = null;

	function addTLabel() {
		tl = new TLabel();
		tl.id = 'tlab1';
		tl.anchorLatLng = center;
		tl.anchorPoint = 'center';
		// To shift icon on exact lat/lon location (half size of icon)
		tl.markerOffset = new GSize(5, 5);

		// Overridden in subclass
		tl.content = '<img src="img/blueball.gif" border="0" />';
		map.addTLabel(tl);
	}

	function moveTLabel() {
		var lon = Math.random() / 10.0 + 4.85;
		var lat = Math.random() / 10.0 + 52.35;
		tl.moveToLatLng(new GLatLng(lat, lon));
	}

	function load() {

		mapHeight = document.getElementById("map").offsetHeight;

		//locatorHeight = document.getElementById("locator").offsetHeight;

		//var x= parseInt(100*mapHeight/(mapHeight+locatorHeight));

		//document.getElementById("map").style.height=x+"%";


		if (GBrowserIsCompatible()) {
			//  var map = new GMap2(document.getElementById("map"));
			map = new GMap2(document.getElementById("map"));
			new GKeyboardHandler(map);

			map.addControl(new GScaleControl(),
					new GControlPosition(G_ANCHOR_TOP_LEFT, new GSize(16, 10)));

			map.addControl(new GSmallZoomControl(),
					new GControlPosition(G_ANCHOR_TOP_LEFT, new GSize(3, 2)));

			map.addControl(new GMapTypeControl(),
					new GControlPosition(G_ANCHOR_TOP_LEFT, new GSize(49, 0)));

			// map.addControl(new GOverviewMapControl());

			var OMap = new GOverviewMapControl();
			map.addControl(OMap);
			OMap.hide(true);
			center = new GLatLng(52.35, 4.85);
			map.setCenter(center, 11);
			addTLabel();
			setInterval('moveTLabel()', 3000);

			// geocoder = new GClientGeocoder();
		}


	}

	function showAddress(address) {
		if (geocoder) {
			geocoder.getLatLng(
					address,
					function(point) {
						if (!point) {
							alert(address + " not found. ");
						} else {
							map.setCenter(point, 12);
							var marker = new GMarker(point);

						}
					}
					);
		}
	}

	//]]>
</script>
</head>

<body onload="load()" onunload="GUnload()">

<!--  <div id="locator" style="z-index:1;background-color:lightgray;width:100%;border-bottom:1px solid white;padding:2px">
   <form action="#" onsubmit="showAddress(this.address.value); return false">
   <input type="text" size=30  name="address" value="Mountain View, CA" />
   <input type="submit" value="Go" style="margin:2px"/>
   </form>
  </div>     -->

<div id="map" style="height: 100%">
</div>


</body>
</html>
