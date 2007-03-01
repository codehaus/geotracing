using System;
using System.Collections.Generic;
using System.Text;

namespace Diwi
{
    class Protocol {

        public static string POSTFIX_REQ = "-req";
        public static string POSTFIX_RSP = "-rsp";
        public static string POSTFIX_IND = "-ind";
        public static string POSTFIX_NRSP = "-nrsp";


        public static string SERVICE_LOGIN = "login";
        public static string SERVICE_LOGOUT = "logout";
        public static string SERVICE_UTOPIA = "utopia";
        public static string SERVICE_SELECT_APP = "select-app";


        public static string TAG_LOGIN_REQ = SERVICE_LOGIN + POSTFIX_REQ;
        public static string TAG_LOGOUT_REQ = SERVICE_LOGOUT + POSTFIX_REQ;
        public static string TAG_UTOPIA_REQ = SERVICE_UTOPIA + POSTFIX_REQ;
        public static string TAG_SELECT_APP_REQ = SERVICE_SELECT_APP + POSTFIX_REQ;


        public static string ATTR_AGENTKEY = "agentkey";
        public static string ATTR_APPNAME = "appname";
        public static string ATTR_DETAILS = "details";
        public static string ATTR_ERROR = "error";
        public static string ATTR_ERRORID = "errorId"; // yes id must be Id !!
        public static string ATTR_NAME = "name";
        public static string ATTR_PASSWORD = "password";
        public static string ATTR_PORTALNAME = "portalname";
        public static string ATTR_PROTOCOLVERSION = "protocolversion";
        public static string ATTR_ROLENAME = "rolename";
    }
}
