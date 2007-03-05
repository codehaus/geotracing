<form id="loginform" name="loginform" method="post" action="control.jsp?cmd=register">
	<p>Register using the form below.
		You may use multiple email adresses (e.g. your regular email and the email on your phone) separated by spaces.
	Using the email adres allows you to submit media through email, e.g. from your phone. Please remember your password
		since password recovery is not yet implemented. You'll also need your password to download/install a personalized MobiTracer
		on your phone.
	</p>
<table>
	<tr>
		<td>loginname</td><td><input name="loginname" id="loginname" type="text" />*</td>
	</tr>
	<tr>
	<td>password</td><td><input name="password" id="password" type="password" />*</td>
	</tr>
	<tr>
   <td>retype password</td><td><input name="password2" id="password2" type="password" />*</td>
   </tr>
	<tr>
   <td>first name</td><td><input name="firstname" id="firstname" type="text" /></td>
   </tr>
	<tr>
   <td>last name</td><td><input name="lastname" id="lastname" type="text" /></td>
   </tr>
	<tr>
   <td>email</td><td><input name="email" id="email" type="text" />*</td>
   </tr>
	<tr>
   <td>mobile nr</td><td><input name="mobilenr" id="mobilenr" type="text" /></td>
   </tr>
	<tr>
	<td colspan="2"><input type="submit" name="submit" value="Register"/></td>
	</tr>
	</table>
</form>



