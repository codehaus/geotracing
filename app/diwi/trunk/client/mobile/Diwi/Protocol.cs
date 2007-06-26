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
        public static string SERVICE_NAV_START = "nav-start";
        public static string SERVICE_NAV_STATE = "nav-state";
        public static string SERVICE_ACTIVATE_ROUTE = "nav-activate-route";
        public static string SERVICE_ADD_MEDIUM = "nav-add-medium";
        public static string SERVICE_GET_ROUTELIST = "route-getlist";


        public static string TAG_LOGIN_REQ = SERVICE_LOGIN + POSTFIX_REQ;
        public static string TAG_LOGOUT_REQ = SERVICE_LOGOUT + POSTFIX_REQ;
        public static string TAG_UTOPIA_REQ = SERVICE_UTOPIA + POSTFIX_REQ;
        public static string TAG_SELECT_APP_REQ = SERVICE_SELECT_APP + POSTFIX_REQ;
        public static string TAG_NAV_START_REQ = SERVICE_NAV_START + POSTFIX_REQ;
        public static string TAG_NAV_STATE_REQ = SERVICE_NAV_STATE + POSTFIX_REQ;
        public static string TAG_ACTIVATE_ROUTE_REQ = SERVICE_ACTIVATE_ROUTE + POSTFIX_REQ;
        public static string TAG_ADD_MEDIUM_REQ = SERVICE_ADD_MEDIUM + POSTFIX_REQ;
        public static string TAG_GET_ROUTELIST_REQ = SERVICE_GET_ROUTELIST + POSTFIX_REQ;


        public static string TAG_LOGIN_RSP = SERVICE_LOGIN + POSTFIX_RSP;
        public static string TAG_LOGOUT_RSP = SERVICE_LOGOUT + POSTFIX_RSP;
        public static string TAG_UTOPIA_RSP = SERVICE_UTOPIA + POSTFIX_RSP;
        public static string TAG_SELECT_APP_RSP = SERVICE_SELECT_APP + POSTFIX_RSP;
        public static string TAG_NAV_START_RSP = SERVICE_NAV_START + POSTFIX_RSP;
        public static string TAG_NAV_STATE_RSP = SERVICE_NAV_STATE + POSTFIX_RSP;
        public static string TAG_ACTIVATE_ROUTE_RSP = SERVICE_ACTIVATE_ROUTE + POSTFIX_RSP;
        public static string TAG_ADD_MEDIUM_RSP = SERVICE_ADD_MEDIUM + POSTFIX_RSP;
        public static string TAG_GET_ROUTELIST_RSP = SERVICE_GET_ROUTELIST + POSTFIX_RSP;
       





        public static string ATTR_AGENTKEY = "agentkey";
        public static string ATTR_APPNAME = "appname";
        public static string ATTR_DETAILS = "details";
        public static string ATTR_ERROR = "error";
        public static string ATTR_ERRORID = "errorId"; // yes id must be Id !!
        public static string ATTR_NAME = "name";
        public static string ATTR_ID = "id";
        public static string ATTR_INIT = "init";
        public static string ATTR_PASSWORD = "password";
        public static string ATTR_PORTALNAME = "portalname";
        public static string ATTR_PROTOCOLVERSION = "protocolversion";
        public static string ATTR_ROLENAME = "rolename";
    }
}
