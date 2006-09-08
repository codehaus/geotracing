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
		<td class="teksttitel">Foto weggooien</td>
	</tr>
	<tr>
		<td class="textplain">
			Bevestig hieronder of je deze foto echt wilt weggooien.
		</td>
	</tr>
	<tr>
		<td>
<table border="0" cellpadding="4" cellspacing="2" width="100%">
	<form id="mediumform" name="mediumform" method="post" action="control.jsp?cmd=delete-medium-cnf">
		<tr>
			<td>&nbsp;</td><td><img src="<%= thumbURL %>" border="0" alt=""/></td><td>&nbsp;</td>
		</tr>
		<tr>
			<td class="mediakop">titel</td>
			<td><%= medium.getField(MediaFiler.FIELD_NAME)%>
			<input name="id" id="id" type="hidden" value="<%= id %>"/>
			<input name="trackid" id="trackid" type="hidden" value="<%= trackId %>"/>
			</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>&nbsp;</td><td><input type="submit" name="cancel" value="Terug"/>&nbsp;&nbsp;<input type="submit" name="ok" value="Gooi weg!"/></td><td>&nbsp;</td>
		</tr>
	</form>
</table>
		</td>
	</tr>
</table>

