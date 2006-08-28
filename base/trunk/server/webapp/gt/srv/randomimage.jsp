<%@ include file="model.jsp" %>
<%
	Record[] records = model.query(
			/* tables: */ "base_medium",
			/* fields: */ null,
			/* where:  */ "kind = 'image'",
			/* relations: */ null,
			/* postCond: */ "ORDER BY RAND() LIMIT 1");


	String url = "media.srv?id=" + records[0].getId();

	// Optional resize
	String resize = request.getParameter("resize");
	if (resize != null && resize.length() > 0) {
		url += "&resize=" + resize;
	}
	response.sendRedirect(url);
%>


