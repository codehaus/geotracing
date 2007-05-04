<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    	<title>Jeugd GePoSitioneerd</title>

<!--    <script src="http://maps.google.com/maps?file=api&amp;v=2"
            type="text/javascript"></script>-->
				<script src="http://maps.google.com/maps?file=api&v=2&key=ABQIAAAAD3bxjYK2kuWoA5XU4dh89xTwM0brOpm-All5BF6PoaKBxRWWERRoOsVQVA34Dw_ROVWVdbWHUb4KkA"
			type="text/javascript"></script>
    <script type="text/javascript">
    //<![CDATA[

    function load() {
        var map = new GMap2(document.getElementById("map"));
        map.addControl(new GLargeMapControl());
        map.addControl(new GMapTypeControl());
        map.setCenter(new GLatLng(50.795272,4.253817), 8);
		map.addMapType('map', G_NORMAL_MAP);
		map.addMapType('satellite', G_SATELLITE_MAP);
		map.addMapType('hybrid', G_HYBRID_MAP);
    }

    //]]>
</script>
<style>
		a:link {
			color: #ffffff;
			text-decoration: none;
			font-weight: bold;
		}
		
		a:visited {
			text-decoration: none;
			color: #ffffff;
			font-weight: bold;
		}
		a:hover {
			text-decoration: underline;
			color: #ffffff;
			font-weight: bold;
		}
		
		a:active {
			text-decoration: none;
			color: #ffffff;
			font-weight: bold;
		}
</style>	
</head>
  <body onload="load()" onunload="GUnload()">
<table width=1000>
	<tr>
		<td height="23" width="1000" bgcolor="#004b7d" align="right">
		<a href="index.jsp">home</a> | <a href="kaart.jsp">kaart</a> | <a href="deelnemers.jsp">deelnemers</a> | <a href="help.jsp">help</a> | <a href="colofon.jsp">partners</a>
		</td>
	</tr>
	<tr>
		<td>
			<div id="map" style="width: 1000px; height: 600px"></div>
		</td>
	</tr>
</table>
  </body>
</html>
