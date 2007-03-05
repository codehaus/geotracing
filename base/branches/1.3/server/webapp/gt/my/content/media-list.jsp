<%@ page import="java.util.Date"%>
<%@ include file="../model.jsp" %>

<%
		String tables = "utopia_person,base_medium";
		String fields = "base_medium.id,base_medium.name,base_medium.kind,base_medium.creationdate";
		String where = "utopia_person.id = " + model.getPersonId();
		String relations = "base_medium,utopia_person";
		String postCond = "ORDER BY base_medium.creationdate DESC";

		Record[] recs  = model.query(tables, fields, where, relations, postCond);
%>
<p>You have <%= recs.length %>  media. Most recent at top. You can view each medium in its raw format. This opens
a separate window. A bit primitive but you can make that window smaller and keep it aside to view subsequent media.
</p>
<table border="1" cellpadding="6" >
    <tr>
		<th>id</th>
		<th>name</th>
		<th>kind</th>
		<th>date</th>
		<th>view</th>
		<th>edit</th>
	</tr>
   <%
	   String editActPre = "<a href=\"control.jsp?cmd=nav-medium-edit&id=";
	   String editActPost = "\">[edit]</a>";
	   String editAction;

	   String viewActPre = "<a target=\"mediumview\" href=\"../media.srv?id=";
	   String viewActPost = "\">[view]</a>";
	   String viewAction;

	   Record rec;
	   for (int i=0; i < recs.length; i++) {
		 rec = recs[i];
		 editAction = editActPre + rec.getId() + editActPost;
		 viewAction = viewActPre + rec.getId() + viewActPost;
   %>
    <tr>
      <td><%= rec.getId() %></td>
      <td><%= rec.getField("name") %></td>
      <td><%= rec.getField("kind") %></td>
      <td><%= DATE_FORMAT.format(new Date(rec.getLongField("creationdate"))) %></td>
	   <td><%= viewAction %></td>
       <td><%= editAction %></td>
	 </tr>
	<%
	   }
	%>
</table>

