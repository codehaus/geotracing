<%@ page import="java.util.Date,org.geotracing.server.Track,org.keyworx.oase.api.Record"%>
<%@ include file="model.jsp" %>
<%
	String msg="";
	Record[] records = new Record[0];
	try {
	 records =  model.query(
			/* tables: */ "utopia_person,g_track",
			/* fields: */ "g_track.id,g_track.name,g_track.startdate,g_track.enddate,g_track.distance",
			/* where:  */ "utopia_person.id = '" + model.getPersonId() + "'",
			/* relations: */ "utopia_person,g_track",
			/* postCond: */ "ORDER BY g_track.startdate");
	} catch (Throwable t) {
		msg = "Error t=" + t;
	}
%>
<%= msg %>
<table border="0" cellpadding="4" cellspacing="4" width="100%">
	<tr>
		<td class="teksttitel">Mijn Routes</td>
	</tr>
	<tr>
		<td class="textplain">Je hebt <%= records.length%> routes gemaakt.
			Klik op een route (1e kolom) om de beschrijving van de foto's aan te passen of
			eventueel een foto weg te gooien.</td>
	</tr>
	<tr>
		<td>
			<table border="1" cellpadding="4" cellspacing="0" width="80%">
				<tr>
					<td class="subkop">Route</td>
					<td class="subkop">Datum</td>
				</tr>
	<%
					String trackURL, editURL;
					Record rec;
					int id;
					for (int i=0; i < records.length; i++) {
						rec = records[i];
						id = rec.getId();
						trackURL = "sotce.jsp?cmd=showtrack&id=" + id;
						editURL = "control.jsp?cmd=edit-track&id=" + id;

	%>
				<tr>
					<td class="mediatekst"><a href="<%= editURL %>">[<%= rec.getField(Track.FIELD_NAME)%>]</a></td>
					<td class="mediadatum"><%= DATE_ONLY_FORMAT.format(new Date(rec.getLongField(Track.FIELD_START_DATE))) %></td>
				</tr>
	<%
					}
	%>
			</table>

		</td>
	</tr>
</table>
