<%@ page import="org.geotracing.handler.Track,java.util.Date" %>
<%@ include file="../model.jsp" %>
<%
	int id = Integer.parseInt(request.getParameter("id"));
	Record track = null;
	Record[] media;
	String tags, tagCloud="", myTagCloud="";

	try {
		track = model.getOase().getFinder().read(id);
		media = model.getOase().getRelater().getRelated(track, "base_medium", null);
		// Get tag info for this id and general tags
		int personId = Integer.parseInt(model.getPersonId());
		TagLogic tagLogic = new TagLogic(model.getOase().getOaseSession());
		tags = tagLogic.getTagsString(personId, id);
		tagCloud = model.getTagCloud();
		myTagCloud = model.getMyTagCloud();
	} catch (Throwable t) {
		model.setResultMsg("Error t=" + t);
		return;
	}

%>
<p>
	You can adapt the title, description and tags for this Track and or edit any of the media
	related to this track. <br/>
		If you are not satisfied with this Track
			<a href="control.jsp?cmd=track-delete&id=<%= id %>">click here to delete this Track</a>.
	Note: <strong>deletion is immediate and permanent</strong>
</p>

<h2>Track Info</h2>
<table border="1" cellpadding="8" cellspacing="0" >
	<form id="mediumform" name="mediumform" method="post" action="control.jsp?cmd=track-update">
		<tr>
			<td class="strong">id</td>
			<td><%= id %></td>
		</tr>
		<tr>
			<td class="strong">state</td>
			<td>
				<%= track.getIntField(Track.FIELD_STATE) == 1 ? "active" : "archived" %>
			</td>
		</tr>
		<tr>
			<td class="strong">title</td>
			<td><input name="name" id="name" type="text" size="40" value="<%= track.getField(Track.FIELD_NAME)%>"/>
			<input name="id" id="id" type="hidden" value="<%= id %>"/>
			</td>
		</tr>
		<tr>
			<td class="strong">description</td>
			<td><textarea cols="40" rows="8" name="description" id="description"><%= track.getField(Track.FIELD_DESCRIPTION) %></textarea></td>
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
			<td class="strong">start</td>
			<td><%= DATE_FORMAT.format(new Date(track.getLongField(Track.FIELD_START_DATE))) %></td>
		</tr>
		<tr>
			<td class="strong">end</td>
			<td><%= DATE_FORMAT.format(new Date(track.getLongField(Track.FIELD_END_DATE))) %></td>
		</tr>
		<tr>
			<td class="strong">points</td>
			<td>
				<%= track.getField(Track.FIELD_PTCOUNT) %>
			</td>
		</tr>

		<tr>
			<td>&nbsp;</td><td align="right"><input type="submit" name="cancel" value="Cancel"/>&nbsp;&nbsp;<input type="submit" name="ok" value="Ok"/></td>
		</tr>
	</form>
</table>

<h2>Track Media</h2>
 <p>This Track has <%= media.length %>  media.  You can view each medium in its raw format. This opens
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
	   for (int i=0; i < media.length; i++) {
		 rec = media[i];
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
