<%@ include file="../model.jsp" %>
<%
	String msg = "";
	Record person, account, thumb[];
	int thumbId=-1;
	String iconDisp = "No user icon yet";
	String firstName="", lastName="", loginName="", emails="", mobilenr="";
	try {
		person = model.getOase().getFinder().read(Integer.parseInt(model.getPersonId()));
		if (!person.isNull("firstname")) {
			firstName = person.getStringField("firstname");
		}
		if (!person.isNull("lastname")) {
			lastName = person.getStringField("lastname");
		}
		if (!person.isNull("email")) {
			emails = person.getStringField("email");
		}
		if (!person.isNull("mobilenr")) {
			mobilenr = person.getStringField("mobilenr");
		}
		account = model.getOase().getRelater().getRelated(person, "utopia_account", null)[0];
		loginName =account.getStringField("loginname");
		thumb = model.getOase().getRelater().getRelated(person, "base_medium", "thumb");
		if (thumb.length > 0) {
			thumbId = thumb[0].getId();
			iconDisp = "<img src=\"../media.srv?id=" + thumbId + "&resize=" + 100 + "\" border=0 />";
		}
	} catch (Throwable t) {
		msg = "Error t=" + t;
		model.setResultMsg(msg);
		return;
	}

%>
<p>This page allows you to edit your profile settings.</p>

<form id="profileform" name="profileform" method="post" action="control.jsp?cmd=profile-update" enctype="multipart/form-data">
	<table cellspacing="4" cellpadding="4" border="0">
		<tr>
			<td>loginname</td>
			<td><strong><%= loginName %></strong></td>
			<td>(your user name)</td>
		</tr>
		<tr>
			<td>first name</td>
			<td><input name="firstname" id="firstname" type="text" value="<%= firstName %>"/></td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>last name</td>
			<td><input name="lastname" id="lastname" type="text" value="<%= lastName %>"/></td>
			<td>(your surname)</td>
		</tr>
		<tr>
			<td>new password</td>
			<td><input name="password1" id="password1" type="password"/></td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>retype password</td>
			<td><input name="password2" id="password2" type="password"/></td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>email(s)</td>
			<td><input name="emails" id="emails" type="text" value="<%= emails %>"/></td>
			<td>(one or more emails separated by spaces)</td>
		</tr>
		<tr>
			<td>mobile nr</td>
			<td><input name="mobilenr" id="mobilenr" type="text" value="<%= mobilenr %>"/></td>
			<td>(full mobile nr, e.g +31654268628)</td>
		</tr>
		<tr>
			<td>your icon</td>
			<td>
				<p><%= iconDisp %></p>
				<input name="iconfile" id="iconfile" type="file" />
				<input name="name" id="name" type="hidden" value="thumb-<%= loginName %>" />
				<input name="description" id="description" type="hidden" value="user icon for <%= loginName %>" />
			</td>
			<td>(use a 4x3 picture dimension; picture will be scaled)</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><input type="submit" name="cancel" id="cancel" value="Cancel"/>&nbsp;&nbsp;<input type="submit" id="ok" name="ok" value="Update"/></td>
			<td>&nbsp;</td>
		</tr>
	</table>
</form>


