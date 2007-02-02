<%
	String title = request.getParameter("title");
	String mimeType = request.getParameter("mime");
	String id = request.getParameter("id");

	String url = "/gt/media.srv?id=" + id;
	String kind = "video";

	if (mimeType.startsWith("image")) {
	    String resize = request.getParameter("resize");
		if (resize != null && resize.length() > 0) {
			url += "&resize=" + resize;
		}
		kind = "image";
	} else if (mimeType.equals("video/3gpp")) {
		if (request.getHeader("User-Agent").toLowerCase().indexOf("mac os x") == -1) {
		 url += "&format=swf";
		 mimeType = "application/x-shockwave-flash";
		}
	}


	String caption = title;

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>

	<title>MediaPlayer - <%= title %></title>
	<link rel="stylesheet" type="text/css" href="css/mediaplayer.css">
 	<script type="text/javascript">
		// <!--
		function closeFrame() {
			if (window.parent.hideMediumFrame) {
				window.parent.hideMediumFrame('<%= kind %>');
			} else {
				window.close();
			}
		}

		function openFullImage() {
		 window.open('mediaplayer.jsp?title=<%=title %>&mime=<%= mimeType %>&id=<%= id %>', 'medium<%= id %>', 'status=no,resizable=yes,scrollbars=yes,toolbar=no,width=800,height=600');
		}
	// -->
  </script>
</head>

<body>
<center>
<table border="0" cellspacing="0" cellpadding="4">
<tr>
<td align="center"><a href="#" onclick="closeFrame(); return false;">[Close]</a></td>
</tr>
<tr>
<td align="center">
	<table border="1" cellspacing="0" cellpadding="0">
		<tr>
				<td>
				<%
					// Note: convert 3gp to swf (may use QT for Mac OS browsers)
					if (mimeType.equals("application/x-shockwave-flash")) {
				%>
				<object classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000"
					 codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0">
					 <param name="movie" value="<%= url %>">
					 <param name="quality" value="high">
					 <param name="bgcolor" value="#ffffff">
					 <param name="loop" value="true">
					 <embed src="<%= url %>" quality="high" bgcolor="#ffffff" loop="true" type="application/x-shockwave-flash"
					 pluginspage="http://www.macromedia.com/shockwave/download/index.cgi?p1_prod_version=shockwaveflash">
					 </embed>
				</object>

				<%
					} else if (mimeType.startsWith("image")) {
				%>

					<a href="#" onclick="openFullImage(); return false;" ><img src="<%= url %>" border="0" alt="click to see full-size picture" ></a>

				<%
					} else if (mimeType.equals("video/3gpp")) {
				%>
				  <p>
 <OBJECT CLASSID="clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B" WIDTH="176" HEIGHT="154" CODEBASE="http://www.apple.com/qtactivex/qtplugin.cab">

 <PARAM name="SRC" VALUE="<%= url %>">
 <PARAM name="AUTOPLAY" VALUE="true">
 <PARAM NAME="type" VALUE="video/quicktime">
  <PARAM name="CONTROLLER" VALUE="true">
  <EMBED SRC="<%= url %>" WIDTH="176" HEIGHT="154" AUTOPLAY="true" CONTROLLER="true" type="video/quicktime" PLUGINSPAGE="http://www.apple.com/quicktime/download/">
  </EMBED>
  </OBJECT>
  </p>
				<%
}
				%>
				</td>
		</tr>
	</table>
</td>
</tr>
<tr>
<td align="center">
<p class="caption"><%= caption %></p>
</td>
</tr>
</table>
</center>
</body>
</html>

