<%@ page import="nl.justobjects.jox.dom.JXElement,
                 org.keyworx.amuse.client.web.HttpConnector,
                 org.keyworx.common.log.Log,
                 org.keyworx.utopia.core.data.Role,
                 org.keyworx.common.log.Logging"
%>
<%@ page import="org.keyworx.utopia.core.util.LoginManager" %>
<%@ page import="org.keyworx.utopia.core.data.Person" %>
<%
    Log log = Logging.getLog("confirmation.jsp");
    String code = request.getParameter("code");
    String id = request.getParameter("id");
    String email = request.getParameter("email");
    if (login(session, "geoapp-guest", "guest", Role.GUEST_ROLE_VALUE)) {
        log.info("Login ok in register.jsp");
        if (code != null && code.length() > 0) {
            JXElement req = new JXElement("profile-activate-req");
            req.setAttr("code", code);
            JXElement rsp = processRequest(session, req);
            log.info(new String(rsp.toBytes(false)));
            if (rsp.getTag().indexOf("-rsp") != -1) {
                response.sendRedirect("../index.html?msg=signupconfirm-ok");
            } else {
                response.sendRedirect("../index.html?msg=signupconfirm-nok");
            }
        } else if (id != null && id.length() > 0 && email != null && email.length() > 0) {
            JXElement personGetReq = new JXElement("person-get-req");
            personGetReq.setAttr(Person.EMAIL_FIELD, email);
            JXElement personGetRsp = processRequest(session, personGetReq);
            log.info("Searching person with email[" + email + "]- result:" + new String(personGetRsp.toBytes(false)));
            if(personGetRsp.getChildByTag(Person.XML_TAG)!=null){        
                JXElement req = new JXElement("tourschedule-confirm-invitation-req");
                req.setAttr("id", id);
                req.setAttr("email", email);
                JXElement rsp = processRequest(session, req);
                log.info(new String(rsp.toBytes(false)));
                if (rsp.getTag().indexOf("-rsp") != -1) {
                    response.sendRedirect("../index.html?msg=invitationconfirm-ok");
                } else {
                    response.sendRedirect("../index.html?msg=invitationconfirm-nok");
                }
            }else{
                response.sendRedirect("../index.html?msg=signup");
            }
        }

    } else {
        log.info("Login failed in confirmation.jsp");
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

<html>
<head>
    <title>Sign Up Confirmation</title>
</head>
<body>Confirming Sign Up...</body>
</html>