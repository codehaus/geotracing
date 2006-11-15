<%@ page import="org.keyworx.oase.api.MediaFiler" %>
<%@ include file="../model.jsp" %>

<p>Upload one or more media files. Each uploaded file will be geotagged with the GPS-location
	corresponding to the date/time in the image file. <br/>
	Images for which no location can be determined will be discarded.
</p>

<table cellspacing="0" cellpadding="4" border="1">

	<form action="control.jsp?cmd=media-upload" method=post enctype="multipart/form-data">
		<tr><td>Name</td><td><input type=text name=<%= MediaFiler.FIELD_NAME %>></td></tr>
		<!--  <tr><td>Timestamp</td><td> <input type=text name=<%= MediaFiler.FIELD_CREATIONDATE %>>  yyyy-mm-dd hh:mm:ss (optional)</td> </tr> -->
		<tr>
			<td>Description</td>
			<td><textarea cols="40" rows="8" name="description" id="description"></textarea></td>

			</tr>
		<tr><td>File </td><td><input type=file name=file1></td></tr>
		<tr><td>File </td><td><input type=file name=file2></td></tr>
		<tr><td>File </td><td><input type=file name=file3></td></tr>
		<tr><td>File </td><td><input type=file name=file4></td></tr>
		<tr><td>File </td><td><input type=file name=file5></td></tr>
		<tr><td>File </td><td><input type=file name=file6></td></tr>
		<tr><td>File </td><td><input type=file name=file7></td></tr>
		<tr><td>File </td><td><input type=file name=file8></td></tr>
		<tr><td>File </td><td><input type=file name=file9></td></tr>
		<tr><td>File </td><td><input type=file name=file10></td></tr>
		<tr><td>File </td><td><input type=file name=file11></td></tr>
		<!--   <tr><td>File </td><td><input type=file name=file12> </td> </tr>
		   <tr><td>File </td><td><input type=file name=file13> </td> </tr>
		   <tr><td>File </td><td><input type=file name=file14> </td> </tr>
		   <tr><td>File </td><td><input type=file name=file15> </td> </tr>
		   <tr><td>File </td><td><input type=file name=file16> </td> </tr>
		   <tr><td>File </td><td><input type=file name=file17> </td> </tr>
		   <tr><td>File </td><td><input type=file name=file18> </td> </tr>
		   <tr><td>File </td><td><input type=file name=file19> </td> </tr>
		   <tr><td>File </td><td><input type=file name=file20> </td> </tr>   -->
		<tr>
			<td>&nbsp;</td>
			<td>
				<input type="submit" name="cancel" id="cancel" value="Cancel"/>&nbsp;&nbsp;
				<input type="submit" name="ok" id="ok" value="Upload"/>
			</td>
		</tr>
	</form>

</table>

