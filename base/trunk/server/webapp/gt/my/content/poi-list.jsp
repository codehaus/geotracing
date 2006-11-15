<%@ page import="java.util.Date"%>
<%@ include file="../model.jsp" %>

<%
		String tables = "utopia_person,g_poi";
		String fields = "g_poi.id,g_poi.name,g_poi.type,g_poi.time";
		String where = "utopia_person.id = " + model.getPersonId();
		String relations = "g_poi,utopia_person";
		String postCond = "ORDER BY g_poi.time DESC";

		Record[] recs  = model.query(tables, fields, where, relations, postCond);
%>
<p>You have <%= recs.length %>  POIs. Most recent are on top.</p>
<table border="1" cellpadding="6" >
    <tr>
		<th>id</th>
		<th>name</th>
		<th>type</th>
		<th>date</th>
		<th>edit</th>
	</tr>
   <%
	   String editActPre = "<a href=\"control.jsp?cmd=nav-poi-edit&id=";
	   String editActPost = "\">[edit]</a>";
	   String editAction;

	   Record rec;
	   for (int i=0; i < recs.length; i++) {
		 rec = recs[i];
		 editAction = editActPre + rec.getId() + editActPost;
   %>
    <tr>
      <td><%= rec.getId() %></td>
      <td><%= rec.getField("name") %></td>
      <td><%= rec.getField("type") %></td>
      <td><%= DATE_FORMAT.format(new Date(rec.getLongField("time"))) %></td>
       <td><%= editAction %></td>
	 </tr>
	<%
	   }
	%>
</table>

