<%@ page import="nl.justobjects.jox.dom.JXElement,org.keyworx.oase.api.MediaFiler" %>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.Vector"%>
<%@ include file="../model.jsp" %>
<%
	int id = Integer.parseInt(request.getParameter("id"));
	int personId = Integer.parseInt(model.getPersonId());
	Record track = null;
	Record medium;
	Record location = null;
	String tags, tagCloud="", myTagCloud="";
	JXElement meta = null;
	try {
		medium = model.getOase().getFinder().read(id);
		Record[] tracks = model.getOase().getRelater().getRelated(medium, "g_track", null);
		if (tracks.length > 0) {
			track = tracks[0];
		}
		Record[] locations = model.getOase().getRelater().getRelated(medium, "g_location", null);
		if (locations.length > 0) {
			location = locations[0];
		}

		// Get tag info for this id and general tags
		TagLogic tagLogic = new TagLogic(model.getOase().getOaseSession());
		tags = tagLogic.getTagsString(personId, id);
		tagCloud = model.getTagCloud();
		myTagCloud = model.getMyTagCloud();
		meta = medium.getXMLField("extra");

	} catch (Throwable t) {
		model.setResultMsg("Error t=" + t);
		return;
	}

	String metaInfo = "no meta info (yet). For images click on Ok button to update meta info";
	if (meta != null) {
		metaInfo = "";
		Vector metaData = meta.getChildren();
		JXElement metaDatum = null;
		for (int i =0; i < metaData.size(); i++) {
			metaDatum = (JXElement) metaData.get(i);
			if (metaDatum.hasText()) {
				metaInfo += metaDatum.getTag() + "=" + metaDatum.getText().trim() + " ";
			}
		}
	}

	String kind = medium.getStringField(MediaFiler.FIELD_KIND);
	String preview = "<a target=\"mediumview\" href=\"../media.srv?id=" + id + "\">see/hear " + kind + " medium</a>";
	if (kind.equals("image")) {
		preview = "<a target=\"mediumview\" href=\"../media.srv?id=" + id + "\"><img src=\"../media.srv?id=" + id + "&resize=180\" border=\"0\" alt=\"" + medium.getField(MediaFiler.FIELD_NAME) + "\"/></a>";
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
	You can adapt the title, description and tags for this <%= medium.getField(MediaFiler.FIELD_KIND) %> medium.<br/>
		If you are not satisfied with this medium
			<a href="control.jsp?cmd=medium-delete&id=<%= id %>">click here to delete this medium</a>.
	Note: <strong>deletion is immediate and permanent</strong>
</p>

<table border="1" cellpadding="8" cellspacing="0" >
	<form id="mediumform" name="mediumform" method="post" action="control.jsp?cmd=medium-update">
		<tr>
			<td class="strong">preview</td>
			<td><%= preview %></td>
		</tr>
		<tr>
			<td class="strong">id</td>
			<td><%= medium.getId() %></td>
		</tr>
		<tr>
			<td class="strong">title</td>
			<td><input name="name" id="name" type="text" size="40" value="<%= medium.getField(MediaFiler.FIELD_NAME)%>"/>
			<input name="id" id="id" type="hidden" value="<%= id %>"/>
			</td>
		</tr>
		<tr>
			<td class="strong">tags</td>
			<td>
				<input name="tags" id="tags" type="text" size="40" value="<%= tags %>"/>
				<input name="otags" id="otags" type="hidden" value="<%= tags %>"/>
				<p>
					<strong>My Tags: </strong> <%= myTagCloud %>
				</p>
				<p>
					<strong>All Tags: </strong> <%= tagCloud %>
				</p>
			</td>
		</tr>
		<tr>
			<td class="strong">date</td>
			<td><%= DATE_FORMAT.format(new Date(medium.getLongField(MediaFiler.FIELD_CREATIONDATE))) %></td>
		</tr>
		<tr>
			<td class="strong">track</td>
			<td>
				<%= trackInfo %>
			</td>
		</tr>
		<tr>
			<td class="strong">lon,lat</td>
			<td>
				<%= locationInfo %>
			</td>
		</tr>
		<tr>
			<td class="strong">description</td>
			<td><textarea cols="40" rows="8" name="description" id="description"><%= medium.getField(MediaFiler.FIELD_DESCRIPTION) %></textarea></td>
		</tr>
		<tr>
			<td class="strong">meta info</td>
			<td><%= metaInfo %></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td align="right"><input type="submit" name="cancel" value="Cancel"/>&nbsp;&nbsp;<input type="submit" name="ok" value="Ok"/></td>
		</tr>
	</form>
</table>
