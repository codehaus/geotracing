<%@ page import="java.util.Date"%>
<%@ include file="../model.jsp" %>

<%
		String tables = "utopia_person,g_track";
		String fields = "g_track.id,g_track.name,g_track.state,g_track.startdate,g_track.enddate,g_track.ptcount";
		String where = "utopia_person.id = " + model.getPersonId();
		String relations = "g_track,utopia_person";
		String postCond = "ORDER BY g_track.startdate DESC";

		Record[] recs  = model.query(tables, fields, where, relations, postCond);

	String userName = model.getString(ATTR_USER_NAME);
%>
<p>You have <%= recs.length %>  tracks. The most recent are at top. Click on [edit] to edit
track properties and track-related media. <br>Click [view] to view your track on the map (and possibly share the URL by right click and copy).
<br>If you want to save a track as a GPX file, right click [GPX] and save the file.
</p>
<table border="1" cellpadding="6" >
    <tr>
		<th>id</th>
		<th>name</th>
		<th>state</th>
		<th>start</th>
		<th>end</th>
		<th>points</th>
		<th>view</th>
		<th>edit</th>
		<th>export</th>
	</tr>
   <%
	   String actPre = "<a href=\"control.jsp?cmd=nav-track-edit&id=";
	   String actPost = "\">[edit]</a>";
	   String viewPre = "<a target=\"_new\" href=\"../?cmd=showtrack&user=" + userName + "&map=satellite&zoom=12&id=";
	   String viewPost = "\">[view]</a>";
	   String toGPXPre = "<a href=\"../srv/get.jsp?cmd=get-track&format=gpx&mindist=20&id=";
	   String toGPXPost = "\">[GPX]</a>";
	   String action, toGPX, viewURL;
	   Record rec;
	   for (int i=0; i < recs.length; i++) {
		 rec = recs[i];
		 action = actPre + rec.getId() + actPost;
		 viewURL = viewPre + rec.getId() + viewPost;
		 toGPX = toGPXPre + rec.getId() + toGPXPost;

   %>
    <tr>
      <td><%= rec.getId() %></td>
      <td><%= rec.getField("name") %></td>
      <td><%= rec.getIntField("state") == 1 ? "active" : "archived" %></td>
      <td><%= DATE_FORMAT.format(new Date(rec.getLongField("startdate"))) %></td>
      <td><%= DATE_FORMAT.format(new Date(rec.getLongField("enddate"))) %></td>
      <td><%= rec.getField("ptcount") %></td>
	  <td><%= viewURL %></td>
      <td><%= action %></td>
	  <td><%= toGPX %></td>
	 </tr>
	<%
	   }
	%>
</table>

