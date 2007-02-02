<%@ page import="
				org.keyworx.common.net.Servlets,
				org.keyworx.oase.util.Log,
				org.keyworx.oase.api.Record,
				com.oreilly.servlet.MultipartRequest,
				java.io.File,
				java.util.HashMap,
				org.keyworx.oase.service.MediaFilerImpl,
				org.keyworx.amuse.core.Amuse,
				org.keyworx.utopia.core.util.Oase,
				org.geotracing.server.TrackLogic" %>
<%@ page import="org.geotracing.handler.TrackLogic"%>
<%!
 Oase oase = null;
String getParameter(ServletRequest req, String name, String defaultValue) {
	String value = req.getParameter(name);
	if (value == null || value.length() == 0) {
		return defaultValue;
	}

	return value.trim();
}

 String getParameter(MultipartRequest req, String name, String defaultValue) {
	String value = req.getParameter(name);
	if (value == null || value.length() == 0) {
		return defaultValue;
	}

	return value.trim();
}

private static class Result {
	private String message;

	public String nextPage;

	public Result(String aNextPage, String aMessage) {
		nextPage = aNextPage;
		setMessage(aMessage);
	}

	public Result(String aNextPage) {
		this(aNextPage, "no message");
	}

	public void setMessage(String aMessage) {
		message = aMessage;

	}
	public String toString() {
		return message;
	}
}


Result handleCommand(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
    String command = null;
	Result result = null;
 	try {

		 String nextPage = getParameter(request, "nextpage", null);
		 if (nextPage == null) {
			  nextPage = request.getHeader("Referer");
		 }
		 result = new Result(nextPage);

		command = getParameter(request, "cmd", null);
		if (command == null) {
			result.setMessage("no command specified");
			return result;
		}


		if ("media-upload".equals(command)) {
/*			Record[] records = DB.uploadLocationMedia(request, DBDefs.VAL_JUST, 0L);
			result.setMessage("Upload ok record count=" + records.length); */
     	} else if ("media-import".equals(command)) {
/*			 String location = getParameter(request, "location", null);
			 if (location == null) {
				 result.setMessage("missing location parameter for media-import");
				 return result;
			 }
			 HashMap fields = null;
             String name = getParameter(request, MediaFiler.FIELD_NAME, null);
			 if (name != null) {
				 fields = new HashMap(2);
				 fields.put(MediaFiler.FIELD_NAME, name);
			 }
			 Record[] records = DB.importLocationMedia(location, fields, DBDefs.VAL_JUST, 0L);
			 result.setMessage("Import ok record count=" + records.length);   */
		} else if ("media-delete".equals(command)) {
/*			String idsParm = getParameter(request, "ids", null);
			if (idsParm == null) {
				result.setMessage("missing ids parameter for media-delete");
				return result;
			}

			String[] ids = idsParm.split(" ");
			DB.deleteLocationMedia(ids);
			result.setMessage("Delete ok count=" + ids.length);  */
		} else if ("track-delete".equals(command)) {
			String id = getParameter(request, "id", null);
			if (id == null) {
				result.setMessage("missing id parameter for track-delete");
				return result;
			}

			TrackLogic trackLogic = new TrackLogic(oase);
			String retval = trackLogic.delete(id);

			result.setMessage("Delete track id=" + id + " retval=" + retval);
 		} else {

			result.setMessage("command unknown or not yet implemented");

		}
	} catch (Throwable t) {
		result.setMessage("error during processing of cmd=" + command + "; details: \n" + t);
	}

	return result;
}
%>
<%
	// Use one Oase session
	oase = (Oase) application.getAttribute("oase");
	if (oase == null) {
		oase = Oase.createOaseSession(Amuse.server.getPortal().getId());
		application.setAttribute("oase", oase);
	}

	Result result =  handleCommand(request, response, session);
	session.setAttribute("result", result.toString());
	String nextPage = result.nextPage;
	if (nextPage.startsWith("http://")) {
    	response.sendRedirect(nextPage);
	} else {
		session.getServletContext().getRequestDispatcher(nextPage).forward(request, response);
	}
	Log.info("action.jsp:  result=" + result);
%>





