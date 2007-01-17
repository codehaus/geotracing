<%@ page import="nl.justobjects.jox.dom.JXElement,
                 org.keyworx.amuse.client.web.HttpConnector,
                 org.keyworx.common.log.Log,
                 org.keyworx.utopia.core.data.Role,
                 org.keyworx.common.log.Logging"
%>
<%@ page import="org.keyworx.utopia.core.util.LoginManager" %>
<%
    Log log = Logging.getLog("register.jsp");
    String code = request.getParameter("code");
    if(login(session, "geoapp-user", "user", Role.USER_ROLE_VALUE)){
        log.info("Login ok in register.jsp");
        JXElement req = new JXElement("profile-activate");
        req.setAttr("code", code);
        JXElement rsp = processRequest(session, req);
        log.info(new String(rsp.toBytes(false)));
    }else{
        log.info("Login failed in register.jsp");
    }
    
%>
<%!
    private static JXElement processRequest(HttpSession aSession, JXElement aReqElement) {
        Log log = Logging.getLog(HttpConnector.getLogId(aSession));
        log.info("processing request : " + aReqElement);
        JXElement rspElement = HttpConnector.executeRequest(aSession, aReqElement);
        log.info("received response : " + rspElement);
        return rspElement;
    }

    public static boolean login(HttpSession aSession, String aUserName, String aPassword, String aRoleName) {
        JXElement stateElement = HttpConnector.getContextParam(aSession, HttpConnector.STATE);
        if (stateElement == null ||
                (!stateElement.getText().equals(LoginManager.LOGGED_ON)
                        && !stateElement.getText().equals(LoginManager.LOGGED_ON_ANONYMOUSLY)))
            HttpConnector.logout(aSession);

        String portalName = "walkandplay";
        String applicationName = "geoapp";
        JXElement loginRsp = HttpConnector.login(aSession, portalName, applicationName, aRoleName, aUserName, aPassword, null);

        // check the response to see if the login procedure was succesfull
        if ((loginRsp == null) || (loginRsp.getTag().indexOf("nrsp") != -1)) return false;
        return true;
    }
%>