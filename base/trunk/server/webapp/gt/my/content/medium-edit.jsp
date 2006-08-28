<%@ page import="java.util.Date,org.keyworx.oase.api.MediaFiler,org.keyworx.oase.api.Record" %>
<%@ include file="../model.jsp" %>
<%
	int id = Integer.parseInt(request.getParameter("id"));
	Record[] tracks;
	Record track = null;
	Record medium;
	Record[] locations;
	Record location = null;
	try {
		medium = model.getOase().getFinder().read(id);
		tracks = model.getOase().getRelater().getRelated(medium, "g_track", null);
		if (tracks.length > 0) {
			track = tracks[0];
		}
		locations = model.getOase().getRelater().getRelated(medium, "g_location", null);
		if (locations.length > 0) {
			location = locations[0];
		}
	} catch (Throwable t) {
		model.setResultMsg("Error t=" + t);
		return;
	}

	String preview = "<a target=\"mediumview\" href=\"../media.srv?id=" + id + "\">see/hear medium</a>";
	if (medium.getStringField(MediaFiler.FIELD_KIND).equals("image")) {
		preview = "<img src=\"../media.srv?id=" + id + "&scale=160\" border=\"0\" alt=\"" + medium.getField(MediaFiler.FIELD_NAME) + "\"/>";
	}

	String trackInfo = "<i>no related track<i/>";
	if (track != null) {
		trackInfo = "<i>name:</i> " + track.getStringField("name") +
				" <br/><i>start:</i> " + DATE_FORMAT.format(new Date(track.getLongField("startdate"))) +
				" <br/><i>end:&nbsp;</i> " + DATE_FORMAT.format(new Date(track.getLongField("enddate")));
	}

	String locationInfo = "no related location";
	if (location != null) {
		locationInfo = location.getField("lon") + ", " + location.getField("lat");
	}

%>
<p>
	You can adapt the title and/or description for this <%= medium.getField(MediaFiler.FIELD_KIND) %> medium.
		If you are not satisfied with this medium
			<a href="control.jsp?cmd=medium-delete&id=<%= id %>">click here to delete this medium</a>.
	Note: <strong>deletion is immediate and permanent</strong>
</p>

<table border="0" cellpadding="4" cellspacing="2" width="100%">
	<form id="mediumform" name="mediumform" method="post" action="control.jsp?cmd=medium-update">
		<tr>
			<td><%= preview %></td>&nbsp;<td></td><td>&nbsp;</td>
		</tr>
		<tr>
			<td class="strong">id</td>
			<td><%= medium.getId() %></td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="strong">title</td>
			<td><input name="name" id="name" type="text" value="<%= medium.getField(MediaFiler.FIELD_NAME)%>"/>
			<input name="id" id="id" type="hidden" value="<%= id %>"/>
			</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="strong">date</td>
			<td><%= DATE_FORMAT.format(new Date(medium.getLongField(MediaFiler.FIELD_CREATIONDATE))) %></td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="strong">track</td>
			<td>
				<%= trackInfo %>
			</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="strong">lon,lat</td>
			<td>
				<%= locationInfo %>
			</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="strong">description</td>
			<td><textarea cols="40" rows="8" name="description" id="description"><%= medium.getField(MediaFiler.FIELD_DESCRIPTION) %></textarea></td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>&nbsp;</td><td><input type="submit" name="cancel" value="Cancel"/>&nbsp;&nbsp;<input type="submit" name="ok" value="Ok"/></td><td>&nbsp;</td>
		</tr>
	</form>
</table>
