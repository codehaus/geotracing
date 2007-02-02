<%@ page import="org.geotracing.handler.Track"%>
<%@ include file="../model.jsp" %>

<p>Upload a GPX file using the form below. Note that your GPX-file must contain timestamps otherwise the file is discarded<br/>
	You may later upload pictures. If these have matching timestamps these will get geotagged
	and added to the track. <br/> After the upload succeeds you may later edit other attributes of your track.
</p>

<table cellspacing="0" cellpadding="4" border="1">

	<form action="control.jsp?cmd=track-upload" method=post enctype="multipart/form-data">
		<tr><td>Name</td><td><input type=text name="<%= Track.FIELD_NAME %>"></td></tr>
		<tr><td>File </td><td><input type=file name=file1></td></tr>
		<tr>
			<td>&nbsp;</td>
			<td>
				<input type="submit" name="cancel" id="cancel" value="Cancel"/>&nbsp;&nbsp;
				<input type="submit" name="ok" id="ok" value="Upload"/>
			</td>
		</tr>
	</form>

</table>

