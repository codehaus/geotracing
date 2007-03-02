<%@ page import="java.io.Writer" %>
<%@ page import="nl.justobjects.jox.dom.JXElement" %>
<%@ page import="nl.justobjects.jox.parser.JXBuilder" %>
<%@ page import="org.keyworx.amuse.core.Amuse" %>
<%@ page import="org.keyworx.common.util.Sys" %>
<%@ page import="org.keyworx.common.log.Logging" %>
<%@ page import="org.keyworx.common.log.Log" %>
<%@ page import="org.keyworx.utopia.core.util.Oase" %>


<%!
    public static Oase oase;
    public static Log log = Logging.getLog("get.jsp");
    // Start performance timing
    long t1 = Sys.now();
    JXElement result = null;
%>
<%
    // Get global Oase (DB) session.
    /*try{
        // Use one Oase session
        if (oase == null) {
            oase = (Oase) application.getAttribute("oase");
            if (oase == null) {
                // First time: create and save in app context
                String oaseContextId = Amuse.server.getPortal().getId();
                oase = Oase.createOaseSession(oaseContextId);
                application.setAttribute("oase", oase);
            }
        }
    }catch (Throwable th){
        result = new JXElement("error");
        result.setText("error creating oase session" + th);
        log.error("error creating oase session", th);
    }
*/
    String action = request.getParameter("action");
    String xml = "";
    if(action!=null){
        if(action.equals("getmedia")){
            xml = "<media>";
            xml += "<medium><name>plaatje 1</name><description>echt een heel mooi plaatje</description><uri>http://www.kich.nl?id=100</uri></medium>";
            xml += "<medium><name>plaatje 2</name><description>echt een heel mooi plaatje</description><uri>http://www.kich.nl?id=200</uri></medium>";
            xml += "<medium><name>plaatje 3</name><description>echt een heel mooi plaatje</description><uri>http://www.kich.nl?id=300</uri></medium>";
            xml += "</media>";
        }
    }

    response.setContentType("text/xml;charset=utf-8");
    try{
        JXElement xmlMsg = new JXBuilder().build(xml);
        Writer writer = response.getWriter();
        writer.write(xmlMsg.toFormattedString());
        writer.flush();
        writer.close();
    }catch (Throwable th){
        log.info("error writing response");
    }


%>