<%@ include file="model.jsp" %>
<%

	if (model.getState() == MODEL_STATE_NULL) {
		model.set(ATTR_PAGE_URL, request.getRequestURI());

%>

<jsp:forward page="control.jsp">
	<jsp:param name="cmd" value="nav-init"/>
</jsp:forward>
<%
	}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml">
<head>
	<title>My GeoTracing</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
	<link rel="stylesheet" type="text/css" href="assets/style.css"/>

</head>

<body>

<div id="container">
	<div id="top">
		<div id="session"><i><%= model.getResultMsg() %></i> - <%= model.getString(ATTR_STATUS_MSG) %></div>

		<div id="banner">MyGeoTracing</div>

		<div id="topmenu">
			<jsp:include page="<%= model.getString(ATTR_TOP_MENU_URL) %>" flush="true"/>
		</div>
	</div>

	<div id="middle">
		<div id="leftcol">
			<jsp:include page="<%= model.getString(ATTR_LEFT_MENU_URL) %>" flush="true"/>
		</div>

		<div id="content">
			<jsp:include page="<%= model.getString(ATTR_CONTENT_URL) %>" flush="true"/>
		</div>
	</div>

</div>
</body>
</html>
