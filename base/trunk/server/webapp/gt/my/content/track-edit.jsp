<%@ page import="java.util.Date,org.keyworx.oase.api.MediaFiler,org.keyworx.oase.api.Record" %>
<%@ page import="org.geotracing.server.Track"%>
<%@ include file="../model.jsp" %>
<%
	int id = Integer.parseInt(request.getParameter("id"));
	Record track = null;
	Record[] media;
	Record[] pois;

	try {
		track = model.getOase().getFinder().read(id);
		media = model.getOase().getRelater().getRelated(track, "base_medium", null);
		pois = model.getOase().getRelater().getRelated(track, "g_poi", null);
	} catch (Throwable t) {
		model.setResultMsg("Error t=" + t);
		return;
	}

%>
<p>
	You can adapt the title and/or description for this Track and or edit any of the media and/or POIs
	related to this track.
		If you are not satisfied with this Track
			<a href="control.jsp?cmd=track-delete&id=<%= id %>">click here to delete this Track</a>.
	Note: <strong>deletion is immediate and permanent</strong>
</p>

<h2>Track Info</h2>
<table border="0" cellpadding="4" cellspacing="2" width="100%">
	<form id="mediumform" name="mediumform" method="post" action="control.jsp?cmd=track-update">
		<tr>
			<td class="strong">id</td>
			<td><%= id %></td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="strong">state</td>
			<td>
				<%= track.getIntField(Track.FIELD_STATE) == 1 ? "active" : "archived" %>
			</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="strong">title</td>
			<td><input name="name" id="name" type="text" value="<%= track.getField(Track.FIELD_NAME)%>"/>
			<input name="id" id="id" type="hidden" value="<%= id %>"/>
			</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="strong">description</td>
			<td><textarea cols="40" rows="8" name="description" id="description"><%= track.getField(Track.FIELD_DESCRIPTION) %></textarea></td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="strong">start</td>
			<td><%= DATE_FORMAT.format(new Date(track.getLongField(Track.FIELD_START_DATE))) %></td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="strong">end</td>
			<td><%= DATE_FORMAT.format(new Date(track.getLongField(Track.FIELD_END_DATE))) %></td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="strong">points</td>
			<td>
				<%= track.getField(Track.FIELD_PTCOUNT) %>
			</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>&nbsp;</td><td><input type="submit" name="cancel" value="Cancel"/>&nbsp;&nbsp;<input type="submit" name="ok" value="Ok"/></td><td>&nbsp;</td>
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

<h2>Track POIs</h2>
<p>This Track has <%= pois.length %>  POIs. </p>
<table border="1" cellpadding="6" >
    <tr>
		<th>id</th>
		<th>name</th>
		<th>type</th>
		<th>date</th>
		<th>edit</th>
	</tr>
   <%
	    editActPre = "<a href=\"control.jsp?cmd=nav-poi-edit&id=";
	    editActPost = "\">[edit]</a>";
	   for (int i=0; i < pois.length; i++) {
		 rec = pois[i];
		 editAction = editActPre + rec.getId() + editActPost;
   %>
    <tr>
      <td><%= rec.getId() %></td>
      <td><%= rec.getField("name") %></td>
      <td><%= rec.getField("type") %></td>
      <td><%= DATE_FORMAT.format(new Date(rec.getLongField("time"))) %></td>
       <td><%= editAction %></td>
	 </tr>
	<%
	   }
	%>
</table>

