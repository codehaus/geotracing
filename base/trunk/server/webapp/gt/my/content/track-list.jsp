<%@ page import="java.util.Date"%>
<%@ include file="../model.jsp" %>

<%
		String tables = "utopia_person,g_track";
		String fields = "g_track.id,g_track.name,g_track.state,g_track.startdate,g_track.enddate,g_track.ptcount";
		String where = "utopia_person.id = " + model.getPersonId();
		String relations = "g_track,utopia_person";
		String postCond = "ORDER BY g_track.startdate DESC";

		Record[] recs  = model.query(tables, fields, where, relations, postCond);
%>
<p>You have <%= recs.length %>  tracks. The most recent are at top. Click on [edit] to edit
track properties and features (media, POIs).
</p>
<table border="1" cellpadding="6" >
    <tr>
		<th>id</th>
		<th>name</th>
		<th>state</th>
		<th>start</th>
		<th>end</th>
		<th>points</th>
		<th>action</th>
	</tr>
   <%
	   String actPre = "<a href=\"control.jsp?cmd=nav-track-edit&id=";
	   String actPost = "\">[edit]</a>";
	   String action;
	   Record rec;
	   for (int i=0; i < recs.length; i++) {
		 rec = recs[i];
		 action = actPre + rec.getId() + actPost;
   %>
    <tr>
      <td><%= rec.getId() %></td>
      <td><%= rec.getField("name") %></td>
      <td><%= rec.getIntField("state") == 1 ? "active" : "archived" %></td>
      <td><%= DATE_FORMAT.format(new Date(rec.getLongField("startdate"))) %></td>
      <td><%= DATE_FORMAT.format(new Date(rec.getLongField("enddate"))) %></td>
      <td><%= rec.getField("ptcount") %></td>
      <td><%= action %></td>
	 </tr>
	<%
	   }
	%>
</table>

