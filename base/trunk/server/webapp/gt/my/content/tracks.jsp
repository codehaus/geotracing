<%@ include file="../model.jsp" %>

<%
		String tables = "utopia_person,g_track";
		String fields = "g_track.id";
		String where = "utopia_person.id = " + model.getPersonId();
		String relations = "g_track,utopia_person";
		String postCond = null;

		Record[] recs  = model.query(tables, fields, where, relations, postCond);
%>
<p>You have <strong><%= recs.length %></strong>  tracks. Use the menu on the left to view and manage your tracks.
</p>
