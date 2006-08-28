<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ include file="map/gmap-keys.jsp" %>

<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
	<link rel="stylesheet" type="text/css" href="css/widget.css" />
	 <link rel="stylesheet" type="text/css" href="css/gtwidget.css" />
	 <link rel="stylesheet" type="text/css" href="css/gtapp.css" />
	<style type="text/css"><!--
	@import "my/skidoo_too.css";
	--></style>
	<link rel="stylesheet" href="my/skidoo_too_print.css" type="text/css" media="print"/>
	<script type="text/javascript" src="my/ruthsarian_utilities.js"></script>
	<script type="text/javascript" src="lib/js-pushlet-client.jsp"></script>

	<!-- get Google Maps API with right key for this server/path -->
	<script src="http://maps.google.com/maps?file=api&amp;v=1&amp;key=<%= getGmapKey() %>" type="text/javascript"></script>

	<script type="text/javascript" src="script/gmap.js" ></script>
	<script type="text/javascript" src="script/dhtml.js" ></script>
	<script type="text/javascript" src="script/server.js" ></script>
	<script type="text/javascript" src="script/widget.js" ></script>
	<script type="text/javascript" src="script/gtwidget.js" ></script>
	<script type="text/javascript" src="script/gtapp.js" ></script>
	<script type="text/javascript" src="script/traceland.js" ></script>
	<script type="text/javascript" src="my/myapp.js"></script>

	<script type="text/javascript">
		<!--
		var font_sizes = new Array(100, 110, 120);
		var current_font_size = 0;
		if (( typeof( NN_reloadPage ) ).toLowerCase() != 'undefined') {
			NN_reloadPage(true);
		}
		if (( typeof( opacity_init  ) ).toLowerCase() != 'undefined') {
			opacity_init();
		}
		if (( typeof( set_min_width ) ).toLowerCase() != 'undefined') {
			set_min_width('pageWrapper', 600);
		}
		if (( typeof( loadFontSize ) ).toLowerCase() != 'undefined') {
			event_attach('onload', loadFontSize);
		}
		//-->
	</script>

	<title>Skidoo Too : Ruthsarian Layouts</title>
</head>

<body>
<div id="pageWrapper">
<div id="masthead" class="inside">

	<!-- masthead content begin -->

	<h1>My Traces</h1>

	<!-- masthead content end -->

	<hr class="hide"/>
</div>

<div id="topmenu" class="hnav">

	<!--
	 you must preserve the (lack of) whitespace between elements when creating
	 your own horizontal navigation elements, otherwise the extra whitespace
	 will break the visual layout. although how it breaks (tiny spaces between
	 each element) is an effect that some may desire.
 -->

	<ul
			><li class="hide"
			><a class="hide" href="../../">Ruthsarian</a
			><span class="divider"> : </span
			></li
			><li
			><a href="#" fn="MY.login">Login</a
			><span class="divider"> : </span
			></li
			><li
			><a href="#" fn="MY.register">Register</a
			><span class="divider"> : </span
			></li
			><li
			><a href="#" fn="MY.help">Help</a
			><span class="divider"> : </span
			></li
			></ul>

	<!-- horizontal nav end -->

	<hr class="hide">
</div>

<div id="outerColumnContainer">
<div id="innerColumnContainer">
<div id="SOWrap">
<div id="middleColumn">
<div id="content" class="inside">
	<div id="map"  ></div>


	<div id="status"></div>

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
				<li><a href="#" fn="GTAPP.mSetMap" arg="blanc" >Blanc</a></li>
				<li><a href="#" fn="GTAPP.mSetMap" arg="map" >Map Google</a></li>
				<li><a href="#" fn="GTAPP.mSetMap" arg="satellite" >Satellite Google</a></li>
		        <li><a href="#" fn="GTAPP.mSetMap" arg="topdag" >Amsterdam Day</a></li>
				<li><a href="#" fn="GTAPP.mSetMap" arg="topnacht" >Amsterdam Nite</a></li>
  				<li><a href="#" fn="GTAPP.mSetMap" arg="hybrid" >Hybrid Google</a></li>
				<li><a href="#" fn="GTAPP.mSetMap" arg="nasa" >Satellite NASA</a></li>

			</ul>
		</li>
		<li><a href="#" >Help</a>
		<ul>
				<li><a href="#" fn="GTAPP.mShowHelp" arg="content/navhelp.html">Map Navigation</a></li>
				<li><a href="#" fn="GTAPP.mShowHelp" arg="content/trkhelp.html" >Viewing Tracks and Media</a></li>
				<li><a href="#" fn="GTAPP.mShowHelp" arg="content/appabout.html" >About TraceLand</a></li>
				<li><a href="#" fn="GTAPP.mShowHelp" arg="content/gtabout.html" >About GeoTracing</a></li>
			</ul>

		</li>
	  </ul>

	  <div style="clear: both;"> </div>
   </div>

    <!-- Include pushlet frame. -->
    <script type="text/javascript">p_embed('/gt')</script>
</div>
</div>

<div id="leftColumn">
	<div class="inside">

		<!--- left column begin -->

		<div class="vnav">
			<h3>Stylesheets</h3>

			<ul
					><li
					><a href="skidoo_too.css">skidoo_too.css</a
					></li
					><li
					><a href="skidoo_too_print.css">skidoo_too_print.css</a
					></li
					></ul>

			<h3>JavaScript</h3>
			<ul
					><li
					><a href="/etribou/layouts/javascript/ruthsarian_utilities.js">ruthsarian_utilities.js</a
					></li
					></ul>

			<h3>Demos</h3>
			<ul
					><li
					><a href="border_into_masthead/index.html">Border Into Masthead</a
					></li
					><li
					><a href="two_columns_left.html">Two Columns - Left</a
					></li
					><li
					><a href="two_columns_right.html">Two Columns - Right</a
					></li
					><li
					><a href="split_masthead_01.html">Split Masthead 1</a
					></li
					><li
					><a href="split_masthead_02.html">Split Masthead 2</a
					></li
					></ul>

			<h3>References</h3>
			<ul
					><li
					><a href="http://www.css-discuss.org/">css-discuss.org</a
					></li
					><li
					><a href="http://www.dithered.com/css_filters/css_only/index.php">CSS Filters</a
					></li
					><li
					><a href="http://www.positioniseverything.net/piefecta-rigid.html">Piefecta Demo Layout</a
					></li
					><li
					><a href="http://www.redmelon.net/tstme/3cols2/">Douglas Livingstone's 3-Column Layout</a
					></li><li
					><a href="http://www.456bereastreet.com/">456 Berea Street</a
					></li
					><li
					><a href="http://www.behr.com/behrx/workbook/index.jsp">Explore Color</a
					></li
					></ul>
		</div>
		<script type="text/javascript">
			<!--
			var browser = new browser_detect();
			if (browser.versionMajor > 4 || !( browser.isIE || browser.isNS ))
			{
				/* only offer style/font changing to version 5 and later browsers
						 * which have javascript enabled. curiously, if you print this out
						 * in NS4, NS4 breaks for some reason.
						 */
				document.write('									\
			<p class="fontsize-set">							\
				<a href="#" onclick="setFontSize(0); return false;"			\
					><img src="images/font_small.gif" width="17" height="21"	\
						alt="Small Font" title="Small Font"			\
				/><\/a>									\
				<a href="#" onclick="setFontSize(1); return false;"			\
					><img src="images/font_medium.gif" width="17" height="21" 	\
						alt="Medium Font" title="Medium Font"			\
				/><\/a>									\
				<a href="#" onclick="setFontSize(2); return false;"			\
					><img src="images/font_large.gif" width="17" height="21"	\
						alt="Large Font" title="Large Font"			\
				/><\/a>									\
			<\/p>										\
		');
			}
			//-->
		</script>

		<!--- left column end -->

		<hr class="hide"/>
	</div>
</div>

<div class="clear"></div>
</div>

<div id="rightColumn">
	<div class="inside">
	<!--	<div id="status">OK</div> -->
		<!--- right column begin -->

		<p>
			A shameless self plug:<br>
			<a href="http://webhost.bridgew.edu/etribou/layouts/skidoo_too/gargoyles/index.html">Skidoo Too :
				Gargoyles</a>
			is a modified version of this layout. It has some nifty features that you might be interested
			in investigating.
		</p>

		<p>
			If you've ever thought about looking to make a donation to Ruthsarian Layouts,
			you'll want to <a href="http://webhost.bridgew.edu/etribou/layouts/skidoo_too/gargoyles/index.html">check
			out
			Skidoo Too : Gargoyles</a>.
		</p>

		<p>
			And if you've ever thought that anyone looking to donate money for a CSS-based layout
			is just really crazy,
			you'll want to <a href="http://webhost.bridgew.edu/etribou/layouts/skidoo_too/gargoyles/index.html">check
			out
			Skidoo Too : Gargoyles</a>.
		</p>

		<!--- right column end -->

		<hr class="hide"/>
	</div>
</div>

<div class="clear"></div>

</div>
</div>

<div id="footer" class="inside">

	<!-- footer begin -->

	<p style="margin:0;">
		&copy; Nobody. All CSS/HTML is released into the public domain.<br>
		Last Updated: June 28, 2005
	</p>

	<!-- footer end -->

	<hr class="hide"/>
</div>
</div>

</body>
</html>
