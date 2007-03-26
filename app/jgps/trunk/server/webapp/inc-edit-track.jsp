<%@ page import="java.util.Date,org.keyworx.oase.api.MediaFiler,org.keyworx.oase.api.Record"%>
<%@ include file="model.jsp" %>
<%
	String msg="";
	Record[] records = new Record[0];
	Record trackRecord = null;
	String trackId = request.getParameter("id");
	try {
	 /* records =  model.query(
			"utopia_person,base_medium",
			"base_medium.id,base_medium.mime,base_medium.creationdate,base_medium.name,base_medium.description",
			"utopia_person.id = '" + model.getPersonId() + "'",
			"utopia_person,base_medium",
			/"ORDER BY base_medium.creationdate");  */
		trackRecord = model.getRecord(trackId);

		records =  model.query(
			/* tables: */ "g_track,base_medium",
			/* fields: */ "base_medium.id,base_medium.mime,base_medium.creationdate,base_medium.name,base_medium.description",
			/* where:  */ "g_track.id = '" + trackId + "'",
			/* relations: */ "g_track,base_medium",
			/* postCond: */ "ORDER BY base_medium.creationdate");
	} catch (Throwable t) {
		msg = "Error t=" + t;
	}
%>
<%= msg %>
<table border="0" cellpadding="4" cellspacing="4" width="100%">
	<tr>
		<td class="teksttitel">Mijn foto's op route <%= trackRecord.getField("name") %></td>
	</tr>
	<tr>
		<td class="textplain">Je hebt <%= records.length%> foto's op deze route.
			Klik op een foto om titel en/of beschrijving aan te passen.</td>
	</tr>
  <% if (records.length > 8) { %>
    <tr>
      <td class="textplain">Ga terug naar het
		  <a href="control.jsp?cmd=edit-tracks">overzicht van mijn routes</a>.</td>
    </tr>
  <% } %>
	<tr>
		<td>
			<table border="1" cellpadding="4" cellspacing="0" width="100%">
				<tr>
					<td class="subkop">Titel</td>
					<td class="subkop">Datum</td>
					<td class="subkop">Foto</td>
				</tr>
	<%
					String thumbURL, editURL;
					Record rec;
					int id;
					for (int i=0; i < records.length; i++) {
						rec = records[i];
						id = rec.getId();
						thumbURL = "media.srv?id=" + id + "&scale=60";
						editURL = "control.jsp?cmd=edit-medium&id=" + id + "&trackid=" + trackId;

	%>
				<tr>
					<td class="mediatekst"><%= rec.getField(MediaFiler.FIELD_NAME)%></td>
					<td class="mediadatum"><%= DATE_FORMAT.format(new Date(rec.getLongField(MediaFiler.FIELD_CREATIONDATE))) %></td>
					<td><a href="<%= editURL %>"><img src="<%= thumbURL %>" border="1" alt="" /></a></td>
				</tr>
	<%
					}
	%>
			</table>

		</td>
	</tr>
  <tr>
    <td class="textplain">Ga terug naar het <a href="control.jsp?cmd=edit-tracks">overzicht van mijn routes</a>.</td>
  </tr>
</table>
