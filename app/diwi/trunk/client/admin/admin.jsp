<%@ page import="org.keyworx.amuse.client.web.HttpConnector" %>
<%@ page import="nl.justobjects.jox.dom.JXElement" %>
<%@ page import="org.keyworx.common.util.Sys" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Date" %>

<%
    String s = DateFormat.getDateInstance().format(new Date(System.currentTimeMillis()));
    String portalName = "diwi";
    String fileName = s + "-diwi-statistics.xml";
    String filePath = "/var/keyworx/webapps/" + portalName + "/diwi/";
    try{
        HttpConnector.login(session, portalName, "geoapp", "user", "geoapp-user", "user", null);

        JXElement getAllStatReq = new JXElement("user-get-allstats-req");
        JXElement getAllStatRsp = HttpConnector.executeRequest(session, getAllStatReq);
       
        Sys.string2File(filePath + fileName, new String(getAllStatRsp.toBytes(false)));
    }catch(Throwable t){

    }
%>
<html>
    <head>
        <title>Digitale Wichelroede Admin</title>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
    </head>
    <body>
        <h1>Digitale Wichelroede statistieken</h1>
        <p><a href="<%=fileName%>">Download <%=fileName%></a> (rechtermuisknop - 'save as')</p>
    </body>
</html>
