<%@ page import="org.keyworx.oase.api.Record"%>
<%@ page import="org.keyworx.utopia.core.util.Oase"%>
<%@ page import="org.keyworx.amuse.core.Amuse"%>
<%@ page import="org.geotracing.handler.QueryHandler"%>
<%!
	public static Oase oase;

	public static Oase getOase(ServletContext application) {
		// Get global Oase (DB) session.
	   try {
		  // Use one Oase session
		   if (oase == null) {
			  oase = (Oase) application.getAttribute("oase");
			  if (oase == null) {
				  // First time: create and save in app context
				  oase = Oase.createOaseSession(Amuse.server.getPortal().getId());
				  application.setAttribute("oase", oase);
			  }
		   }
	   } catch (Throwable th) {
	   }
		return oase;
	}
%>
<%
	String webAppURL  =  request.getRequestURL().toString().split("/srv/")[0];

	int count = 12;
	Record[] records = QueryHandler.queryStore(getOase(application),
			/* tables: */ "base_medium",
			/* fields: */ null,
			/* where:  */ "kind = 'image'",
			/* relations: */ null,
			/* postCond: */ "ORDER BY RAND() LIMIT " + count);


	String resize = request.getParameter("resize");
	if (resize == null || resize.length() == 0) {
		resize = "320x240!";
	}
	String[] url = new String[count];
	for (int i=0; i < url.length; i++) {
		url[i] = webAppURL + "/media.srv?id=" + records[i].getId() +  "&resize=" + resize;
	}

	// Optional resize
	//response.sendRedirect(url);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
	<title>Random Images from <%= Amuse.server.getPortal().getId() %></title>
	 <style type="text/css" xml:space="preserve">
		 html,body {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: verdana, sans-serif;
  font-size:11px;
  background-color:#222;
  color:green;
}
	 </style>
  </head>

  <body>
  <table border="0" cellspacing="0" cellpadding="0">
	  <tr>
		  <td><img src="<%= url[0] %>" alt="i" border="0"></td>
		  <td><img src="<%= url[1] %>" alt="randimg" border="0"></td>
		  <td><img src="<%= url[2] %>" alt="randimg" border="0"></td>
		  <td><img src="<%= url[3] %>" alt="randimg" border="0"></td>
	  </tr>
	  <tr>
		  <td><img src="<%= url[4] %>" alt="randimg" border="0"></td>
		  <td><img src="<%= url[5] %>" alt="randimg" border="0"></td>
		  <td><img src="<%= url[6] %>" alt="randimg" border="0"></td>
		  <td><img src="<%= url[7] %>" alt="randimg" border="0"></td>
	  </tr>
	  <tr>
		  <td><img src="<%= url[8] %>" alt="randimg" border="0"></td>
		  <td><img src="<%= url[9] %>" alt="randimg" border="0"></td>
		  <td><img src="<%= url[10] %>" alt="randimg" border="0"></td>
		  <td><img src="<%= url[11] %>" alt="randimg" border="0"></td>
	  </tr>
  </table>
  </body>
</html>
