<%@ include file="../model.jsp" %>

<%
		String tables = "utopia_person,base_medium";
		String fields = "base_medium.id";
		String where = "utopia_person.id = " + model.getPersonId();
		String relations = "base_medium,utopia_person";
		String postCond = null;

		Record[] recs  = model.query(tables, fields, where, relations, postCond);
%>
<p>You have <strong><%= recs.length %></strong>  media. Use the menu on the left to view and manage your media.
</p>
