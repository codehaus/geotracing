<%@ include file="static-layout-header.html" %>
<%@ page import="java.util.*" %>
<%@ page import="javax.mail.*" %>
<%@ page import="javax.mail.internet.*" %>
<%@ page import="javax.activation.*" %>

<link href="css/brainport.css" rel="stylesheet" type="text/css"/>
	<tr>
		<td width="170" valign="top" bgcolor="#F5F5F5" class="borderright"><img src="../jgps/images/DSC00757.jpg"
																		width="170" height="100"
																		class="imageborderonder"/><br/><br />
	<img src="../jgps/images/Schoolstraat_11.jpg" width="170" height="100" class="imageborderonder"/><br />
	<br/>
	<img src="../jgps/images/verkeersborden.jpg" width="170" height="250" class="imageborderonder"/></td>
		<td colspan="3" valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="0">
<tr>
<td valign="top" width="510">


<%
	String publiek= request.getParameter("publiek");
	if(publiek!=null)
	{
		publiek="publiek";
	}
	else
	{
		publiek="niet publiek";
	}
    
    String to = "dirk.roox@uhasselt.be";
    String from = "dirk.roox@uhasselt.be"; 
    String subject ="Registratie jeugdgepositioneerd " + request.getParameter("gemeente");
    String messageText = "Registratie gevraagd voor gemeente " + request.getParameter("gemeente") + " en de markers zijn " + publiek + ". Het contact emailadres is " + request.getParameter("to");
    boolean sessionDebug = false;

    Properties props = System.getProperties();
    //props.put("mail.uhasselt.be", host);
    //props.put("mail.uhasselt.be", "smtp");
	props.put("mail.smtp.host", "mail.uhasselt.be");

    Session mailSession = Session.getDefaultInstance(props, null);

    mailSession.setDebug(sessionDebug);

    Message msg = new MimeMessage(mailSession);

    msg.setFrom(new InternetAddress(from));
    InternetAddress[] address = {new InternetAddress(to)};
    msg.setRecipients(Message.RecipientType.TO, address);
    msg.setSubject(subject);
    msg.setSentDate(new Date());
    msg.setText(messageText);

    Transport.send(msg);
    
   /* out.println("Mail was sent to " + to);
    out.println(" from " + from);
    out.println(" using host " + host + ".");
	*/
	out.println("Uw registratie is verstuurd. U ontvangt zo snel mogelijk uw login gegevens");

%>
 </td>
	  <td valign="top" class="kolomrechts" bgcolor="#EFEFEF" align="right" width="170"><img src=../jgps/images/luchtfoto.jpg width="170" height="100"><br /><br /><a href='Handleiding.jsp'><img src=../jgps/images/telefoon.jpg width="170" height="100" border="0"></a><br /><br /><img src=../jgps/images/map.jpg width="170" height="100"><br /><br />	  </td>
	  </tr>
</table>
	  </td>
	</tr>
		
<%@ include file="static-layout-footer.html" %>

