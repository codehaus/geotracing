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

        public static string SERVICE_ECHO = "echo";
        public static string SERVICE_LOGIN = "login";
        public static string SERVICE_LOGOUT = "logout";
        public static string SERVICE_UTOPIA = "utopia";
        public static string SERVICE_SELECT_APP = "select-app";
        public static string SERVICE_NAV_START = "nav-start";
        public static string SERVICE_NAV_STATE = "nav-get-state";
        public static string SERVICE_ACTIVATE_ROUTE = "nav-activate-route";
        public static string SERVICE_DEACTIVATE_ROUTE = "nav-deactivate-route";
        public static string SERVICE_ADD_MEDIUM = "nav-add-medium";
        public static string SERVICE_GET_ROUTELIST = "nav-route-getlist";
        public static string SERVICE_GET_ROUTE = "nav-route-get";
        public static string SERVICE_GET_ROUTE_MAP = "route-get-map";
        public static string SERVICE_NAV_GET_MAP = "nav-get-map";
        public static string SERVICE_NAV_POINT = "nav-point";
        public static string SERVICE_NAV_UGC = "nav-toggle-ugc";
        public static string SERVICE_POI_GET = "nav-poi-get";
        public static string SERVICE_HOME_GET = "nav-route-home";
        public static string SERVICE_UGCON_GET = "nav-ugc-on";
        public static string SERVICE_UGCOFF_GET = "nav-ugc-off";

        public static string TAG_ECHO_REQ = SERVICE_ECHO + POSTFIX_REQ;
        public static string TAG_UGC_ON_REQ = SERVICE_UGCON_GET + POSTFIX_REQ;
        public static string TAG_UGC_OFF_REQ = SERVICE_UGCOFF_GET + POSTFIX_REQ;
        public static string TAG_NAV_HOME_REQ = SERVICE_HOME_GET + POSTFIX_REQ;
        public static string TAG_NAV_UGC_REQ = SERVICE_NAV_UGC + POSTFIX_REQ;
        public static string TAG_POI_GET_REQ = SERVICE_POI_GET + POSTFIX_REQ;
        public static string TAG_LOGIN_REQ = SERVICE_LOGIN + POSTFIX_REQ;
        public static string TAG_LOGOUT_REQ = SERVICE_LOGOUT + POSTFIX_REQ;
        public static string TAG_UTOPIA_REQ = SERVICE_UTOPIA + POSTFIX_REQ;
        public static string TAG_SELECT_APP_REQ = SERVICE_SELECT_APP + POSTFIX_REQ;
        public static string TAG_NAV_START_REQ = SERVICE_NAV_START + POSTFIX_REQ;
        public static string TAG_NAV_STATE_REQ = SERVICE_NAV_STATE + POSTFIX_REQ;
        public static string TAG_ACTIVATE_ROUTE_REQ = SERVICE_ACTIVATE_ROUTE + POSTFIX_REQ;
        public static string TAG_DEACTIVATE_ROUTE_REQ = SERVICE_DEACTIVATE_ROUTE + POSTFIX_REQ;
        public static string TAG_ADD_MEDIUM_REQ = SERVICE_ADD_MEDIUM + POSTFIX_REQ;
        public static string TAG_GET_ROUTELIST_REQ = SERVICE_GET_ROUTELIST + POSTFIX_REQ;
        public static string TAG_GET_ROUTE_REQ = SERVICE_GET_ROUTE + POSTFIX_REQ;
        public static string TAG_GET_ROUTE_MAP_REQ = SERVICE_GET_ROUTE_MAP + POSTFIX_REQ;
        public static string TAG_NAV_GET_MAP_REQ = SERVICE_NAV_GET_MAP + POSTFIX_REQ;
        public static string TAG_NAV_POINT_REQ = SERVICE_NAV_POINT + POSTFIX_REQ;


        public static string TAG_UGC_ON_RSP = SERVICE_UGCON_GET + POSTFIX_RSP;
        public static string TAG_UGC_OFF_RSP = SERVICE_UGCOFF_GET + POSTFIX_RSP;
        public static string TAG_NAV_HOME_RSP = SERVICE_HOME_GET + POSTFIX_RSP;
        public static string TAG_NAV_UGC_RSP = SERVICE_NAV_UGC + POSTFIX_RSP;
        public static string TAG_POI_GET_RSP = SERVICE_POI_GET + POSTFIX_RSP;
        public static string TAG_LOGIN_RSP = SERVICE_LOGIN + POSTFIX_RSP;
        public static string TAG_LOGOUT_RSP = SERVICE_LOGOUT + POSTFIX_RSP;
        public static string TAG_UTOPIA_RSP = SERVICE_UTOPIA + POSTFIX_RSP;
        public static string TAG_SELECT_APP_RSP = SERVICE_SELECT_APP + POSTFIX_RSP;
        public static string TAG_NAV_START_RSP = SERVICE_NAV_START + POSTFIX_RSP;
        public static string TAG_NAV_STATE_RSP = SERVICE_NAV_STATE + POSTFIX_RSP;
        public static string TAG_ACTIVATE_ROUTE_RSP = SERVICE_ACTIVATE_ROUTE + POSTFIX_RSP;
        public static string TAG_DEACTIVATE_ROUTE_RSP = SERVICE_DEACTIVATE_ROUTE + POSTFIX_RSP;
        public static string TAG_ADD_MEDIUM_RSP = SERVICE_ADD_MEDIUM + POSTFIX_RSP;
        public static string TAG_GET_ROUTELIST_RSP = SERVICE_GET_ROUTELIST + POSTFIX_RSP;
        public static string TAG_GET_ROUTE_RSP = SERVICE_GET_ROUTE + POSTFIX_RSP;
        public static string TAG_GET_ROUTE_MAP_RSP = SERVICE_GET_ROUTE_MAP + POSTFIX_RSP;
        public static string TAG_NAV_GET_MAP_RSP = SERVICE_NAV_GET_MAP + POSTFIX_RSP;
        public static string TAG_NAV_POINT_RSP = SERVICE_NAV_POINT + POSTFIX_RSP;
      





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
