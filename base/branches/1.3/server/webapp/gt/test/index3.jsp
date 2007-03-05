<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ include file="map/gmap-keys.jsp" %>

<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml">
<head>
<title>Keeping it tall - redmelon.net</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
<link rel="stylesheet" type="text/css" href="css/widget.css"/>
<link rel="stylesheet" type="text/css" href="css/gtwidget.css"/>
<link rel="stylesheet" type="text/css" href="css/gtapp.css"/>

<style type="text/css">
	html, body {
		width: 98%;
		height: 98%;
		margin: 0;
		padding: 0;
		background-color: #ddd;
	}

	#container {
		width: 100%;
		height: 100%;
		margin: 0;
		padding: 0;
		background-color: #fff;
		color: #333;
	}

	#top {
		width: 100%;
		height: 80px;
		background-color: #ddd;
		border-bottom: 1px solid gray;
	}

	#middle {
		width: 100%;
		height: 100%;
		margin: 0;
		padding: 0;
	}

	#leftcol {
		float: left;
		width: 160px;
		height: 100%;
		margin: 0;
		padding: 0;
		border-right: 1px solid gray;
		background-color: #000044;
	}

	#rightcol {
		float: right;
		width: 160px;
		height: 100%;
		margin: 0;
		padding: 0;
		border-left: 1px solid gray;
		background-color: #000044;
	}

	#content {
		width: 100%;
		height: 100%;
		background-color: #444444;
	}

	#footer {
		width: 100%;
		height: 40px;
		clear: both;
		margin: 0;
		padding: 0;
		color: #333;
		background-color: #ddd;
		border-top: 1px solid gray;
	}

	#top h1 {
		padding: 0;
		margin: 0;
	}

	#leftcol p {
		margin: 0 0 1em 0;
	}

	#content h2 {
		margin: 0 0 .5em 0;
	}
</style>

<script type="text/javascript" src="lib/js-pushlet-client.jsp"></script>

<!-- get Google Maps API with right key for this server/path -->
<script src="http://maps.google.com/maps?file=api&amp;v=1&amp;key=<%= getGmapKey() %>"
		type="text/javascript"></script>

<script type="text/javascript" src="script/gmap.js"></script>
<script type="text/javascript" src="script/dhtml.js"></script>
<script type="text/javascript" src="script/server.js"></script>
<script type="text/javascript" src="script/widget.js"></script>
<script type="text/javascript" src="script/gtwidget.js"></script>
<script type="text/javascript" src="script/gtapp.js"></script>
<script type="text/javascript" src="script/traceland.js"></script>
<script type="text/javascript" src="my/myapp.js"></script>

</head>

<body>

<div id="container">
	<div id="top">
		<h1>Header</h1>
	</div>

	<div id="middle">
		<div id="leftcol">
			<p>
				Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut.
			</p>
		</div>

		<div id="content">
			<div id="map"></div>
		</div>

		<div id="rightcol">
			<p>
				Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut.
			</p>
		</div>
	</div>

	<div id="footer">
		Footer
	</div>
</div>
</body>
</html>
