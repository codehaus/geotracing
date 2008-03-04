<%@ page import="org.keyworx.amuse.client.web.HttpConnector" %>
<%@ page import="nl.justobjects.jox.dom.JXElement" %>
<%@ page import="org.keyworx.common.util.Sys" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="nl.justobjects.jox.parser.JXBuilder" %>

<%
    String s = DateFormat.getDateInstance().format(new Date(System.currentTimeMillis()));
    String portalName = "diwi";
    String fileName = s + "-diwi-statistics.xml";
    String filePath = "/var/keyworx/webapps/" + portalName + "/diwi/";

    String msg = "";
    String xml = request.getParameter("xml");

    try{
        HttpConnector.login(session, portalName, "geoapp", "user", "geoapp-user", "user", null);

        if (xml != null && xml.length() > 0) {
            JXElement rsp = HttpConnector.executeRequest(session, new JXBuilder().build(xml));
            if(rsp.getTag().indexOf("-nrsp")!=-1){
                msg += "Error - " + rsp.getAttr("details") + "\n";
            }
        }else{
            JXElement getAllStatReq = new JXElement("user-get-allstats-req");
            JXElement getAllStatRsp = HttpConnector.executeRequest(session, getAllStatReq);

            Sys.string2File(filePath + fileName, new String(getAllStatRsp.toBytes(false)));
        }
    }catch(Throwable t){
        msg += "Exception: " + t.toString();
    }

%>
<html>
    <head>
        <title>Digitale Wichelroede Admin</title>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
    </head>
    <body>
        <h1>Digitale Wichelroede statistieken</h1>
        <%if(xml == null || xml.length() == 0){%><p>Download <a href="<%=fileName%>"><%=fileName%></a> (rechtermuisknop - 'save as')</p><%}%>
        <%if(msg.length()>0){%><p><strong><%=msg%></strong></p><%}%>
    </body>
</html>
