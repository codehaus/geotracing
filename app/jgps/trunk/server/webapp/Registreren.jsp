<%@ include file="static-layout-header.html" %>
<link href="css/brainport.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript">

	function verzend()
	{
		if(document.form(0).gemeente.value=="" || document.form(0).gemeente.value==null)
		{
			alert("Gemeente is een verplicht veld");
			document.form(0).gemeente.focus();
		}
		else
		{
			if(document.form(0).to.value=="" || document.form(0).to.value==null)
			{
				alert("Emailadres is een verplicht veld");
				document.form(0).to.focus();
			}
			else
			{
				document.form(0).submit();
			}
		}
		
	}
</script>


	<tr>
		<td width="170" valign="top" bgcolor="#F5F5F5" class="borderright"><img src="images/DSC00757.jpg"
																		width="170" height="100"
																		class="imageborderonder"/><br/><br />
	<img src="images/Schoolstraat_11.jpg" width="170" height="100" class="imageborderonder"/><br />
	<br/>
	<img src="images/verkeersborden.jpg" width="170" height="250" class="imageborderonder"/></td>
		<td colspan="3" valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="0">
<tr>
<td valign="top" width="510">
	<div class="nieuwsbericht">
		<div class="nieuwsberichttitel">Registreren</div><br/>
		<form action="sendMail.jsp" method="post">
			<table>
				<tr>
					<td>gemeente: </td>
					<td><input type="text" name="gemeente" size="30" maxlength="30"></td>
				</tr>
				<tr>
					<td>resultaten publiek:</td>
					<td><input type="checkbox" name="publiek" value="publiek"></td>
				</tr>
				<tr>
					<td>emailadres: </td>
					<td><input type="text" name="to" size="30" maxlength="30"></td>
				</tr>
			</table>
			<input type="submit" onclick="verzend" value="Versturen">
		</form>
	</div>
	  </td>
	 	  <td valign="top" class="kolomrechts" bgcolor="#EFEFEF" align="right" width="107"><img src=./images/luchtfoto.jpg width="170" height="100"><br /><br /><a href='Handleiding.jsp'><img src=./images/telefoon.jpg width="170" height="100" border="0"></a><br /><br /><img src=./images/map.jpg width="170" height="100"><br /><br />	  </td>
</tr>
</table></td>
	</tr>
<%@ include file="static-layout-footer.html" %>

