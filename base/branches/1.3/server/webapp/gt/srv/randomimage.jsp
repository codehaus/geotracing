<%@ page import="org.geotracing.handler.QueryHandler"%>
<%@ page import="org.keyworx.amuse.core.Amuse"%>
<%@ page import="org.keyworx.oase.api.Record"%>
<%@ page import="org.keyworx.utopia.core.util.Oase"%>
<%@ page import="javax.servlet.ServletContext"%>
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

	Record[] records = QueryHandler.queryStore(getOase(application),
			/* tables: */ "base_medium",
			/* fields: */ null,
			/* where:  */ "kind = 'image'",
			/* relations: */ null,
			/* postCond: */ "ORDER BY RAND() LIMIT 1");


	String url = webAppURL + "/media.srv?id=" + records[0].getId();

	// Optional resize
	String resize = request.getParameter("resize");
	if (resize != null && resize.length() > 0) {
		url += "&resize=" + resize;
	}
	response.sendRedirect(url);
%>


