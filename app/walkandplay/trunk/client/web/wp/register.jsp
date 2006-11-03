<%@ page import="nl.justobjects.jox.dom.JXElement" %>
<%@ page import="org.keyworx.amuse.core.Amuse" %>
<%@ page import="org.keyworx.amuse.client.web.HttpConnector" %>
<%@ page import="org.keyworx.utopia.core.data.Role" %>
<%
    boolean success;
    String msg = "";
    String code = request.getParameter("code");
    if(code!=null && code.length() == 0){
        JXElement rsp = HttpConnector.login(session, Amuse.server.getPortal().getId(), "geoapp", Role.USER_ROLE_VALUE, "wp-user", "user", null);
        rsp = HttpConnector.selectApp(session, "walkandplay", Role.USER_ROLE_VALUE);
        if(rsp!=null && rsp.getTag().indexOf("-rsp")!=-1){
            JXElement req = new JXElement("profile-activate-req");
            req.setAttr("code", code);
            rsp = HttpConnector.executeRequest(session, req);
            if(rsp!=null && rsp.getTag().indexOf("nrsp")!=-1){
                success = false;
                msg = "Oeps activation failed!";
            }
        }else{
            success = false;
            msg = "Oeps autologin failed!";
        }
    }else{
        success = false;
        msg = "Oeps no code found!";
    }


%>