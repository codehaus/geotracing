<%@ page import="java.util.Date,org.keyworx.oase.api.MediaFiler,org.keyworx.oase.api.Record" %>
<%@ include file="model.jsp" %>
<%
	String msg="";
	Record[] mediumRecords = null;
	String id = request.getParameter("id");
	String trackId = request.getParameter("trackid");
	Record trackRecord = null;
	try {
		trackRecord = model.getRecord(trackId);
		mediumRecords =  model.query(
							/* tables: */ "base_medium",
				/* fields: */ "base_medium.id,base_medium.mime,base_medium.creationdate,base_medium.name,base_medium.description",
				/* where:  */ "base_medium.id = '" + id + "'",
				/* relations: */ null,
				/* postCond: */ null);
	} catch (Throwable t) {
		msg = "Error t=" + t;
	}

	// Result of query in control.jsp
	Record medium = mediumRecords[0];
	String thumbURL = "media.srv?id=" + id + "&scale=300";
%>

<table border="0" cellpadding="4" cellspacing="4" width="100%">
	<tr>
		<td class="teksttitel">Bewerk foto</td>
	</tr>
	<tr>
		<td class="textplain">
			Je kunt hier de titel en/of beschrijving van de foto aanpassen.
		Als je niet tevreden bent over deze foto
			<a href="control.jsp?cmd=delete-medium&id=<%= id %>&trackid=<%= trackId%>">klik hier om de foto weg te gooien.</a>
		</td>
	</tr>
	<tr>
		<td>
<table border="0" cellpadding="4" cellspacing="2" width="100%">
	<form id="mediumform" name="mediumform" method="post" action="control.jsp?cmd=update-medium">
		<tr>
			<td>&nbsp;</td><td><img src="<%= thumbURL %>" border="0" alt=""/></td><td>&nbsp;</td>
		</tr>
		<tr>
			<td class="mediakop">titel</td>
			<td><input name="name" id="name" type="text" value="<%= medium.getField(MediaFiler.FIELD_NAME)%>"/>
			<input name="id" id="id" type="hidden" value="<%= id %>"/>
			<input name="trackid" id="trackid" type="hidden" value="<%= trackId %>"/>
			</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="mediakop">datum</td>
			<td><%= DATE_FORMAT.format(new Date(medium.getLongField(MediaFiler.FIELD_CREATIONDATE))) %></td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="mediakop">beschrijving</td>
			<td><textarea cols="40" rows="8" name="description" id="description"><%= medium.getField(MediaFiler.FIELD_DESCRIPTION) %></textarea></td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>&nbsp;</td><td><input type="submit" name="cancel" value="Terug"/>&nbsp;&nbsp;<input type="submit" name="ok" value="Pas aan"/></td><td>&nbsp;</td>
		</tr>
	</form>
</table>
		</td>
	</tr>
</table>

