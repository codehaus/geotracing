<%@ page import="java.util.Date,org.keyworx.oase.api.MediaFiler,org.keyworx.oase.api.Record" %>
<%@ page import="org.geotracing.server.POI"%>
<%@ include file="../model.jsp" %>
<%
	String msg="";
	int id = Integer.parseInt(request.getParameter("id"));
	Record[] tracks = null;
	Record track = null;
	Record poi = null;
	Record[] locations = null;
	Record location = null;
	try {
		poi = model.getOase().getFinder().read(id);
		tracks = model.getOase().getRelater().getRelated(poi, "g_track", null);
		if (tracks.length > 0) {
			track = tracks[0];
		}
		locations = model.getOase().getRelater().getRelated(poi, "g_location", null);
		if (locations.length > 0) {
			location = locations[0];
		}
	} catch (Throwable t) {
		msg = "Error t=" + t;
		return;
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
	You can adapt the name, type and/or description for this POI.
		If you are not satisfied with this POI
			<a href="control.jsp?cmd=poi-delete&id=<%= id %>">click here to delete this poi</a>.
	Note: <strong>deletion is immediate and permanent</strong>
</p>

<table border="0" cellpadding="4" cellspacing="2" width="100%">
	<form id="poiform" name="poiform" method="post" action="control.jsp?cmd=poi-update">
		<tr>
			<td class="strong">id</td>
			<td><%= poi.getId() %></td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="strong">name</td>
			<td><input name="name" id="name" type="text" value="<%= poi.getField("name")%>"/>
			<input name="id" id="id" type="hidden" value="<%= id %>"/>
			</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="strong">type</td>
			<td><input name="type" id="type" type="text" value="<%= poi.getField("type")%>"/>
				</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="strong">date</td>
			<td><%= DATE_FORMAT.format(new Date(poi.getLongField(POI.FIELD_TIME))) %></td>
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
			<td><textarea cols="40" rows="8" name="description" id="description"><%= poi.getField(MediaFiler.FIELD_DESCRIPTION) %></textarea></td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>&nbsp;</td><td><input type="submit" name="cancel" value="Cancel"/>&nbsp;&nbsp;<input type="submit" name="ok" value="Ok"/></td><td>&nbsp;</td>
		</tr>
	</form>
</table>
