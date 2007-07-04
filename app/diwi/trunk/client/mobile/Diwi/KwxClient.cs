using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Net;
using System.Threading;
using System.Windows.Forms;
using System.Globalization;

using Diwi;

namespace Diwi {
    /// <summary>
    /// The KeyWorx server singleton class.
    /// Error handling needs work, media up- and download need to be implemented.
    /// curently uses geotracing protocol....
    /// </summary>

    class KwxClient {
        private CultureInfo mUSFormat = new CultureInfo(0x0409);

        public delegate void MessageCallback(string mess);
        public delegate void POICallback(XMLement mess, float lat, float lon);
        public event MessageCallback messageCallback;
        public event POICallback poiCallback;

        private string mUser = Diwi.Properties.Resources.KwxServerUsername;
        private string mPass = Diwi.Properties.Resources.KwxServerPassword;
        private string mServer;
        private string mAgentKey;
        private string xmlString;
        private XMLement selectAppRequest;
        string trackId;

        static private KwxClient sKwxC = null;

        public static KwxClient instance {
            get {
                if (sKwxC != null) return sKwxC;
                return sKwxC = new KwxClient();
            }
        }

        public string agentKey {
            get {
                return mAgentKey;
            }
        }

        private KwxClient() {
            
            mServer = Diwi.Properties.Resources.KwxServerUrl;
            mAgentKey = null;
        }

        public void start(string u, string p) {
            mUser = u;
            mPass = p;
            start();
        }

        /// <summary>
        /// login, select app and make a track
        /// currently no error recovery...
        /// </summary>
        public void start() {

            if (messageCallback != null) {
                messageCallback("login: ");
            }
            XMLement x = doLogin();

            if (x.tag == "login-rsp") {
                StreamWriter userProps = new StreamWriter(AppController.sUserProps);
                if (userProps != null) {
                    AppController.sUserName = mUser;
                    AppController.sUserPass = mPass;
                    userProps.WriteLine(mUser);
                    userProps.WriteLine(mPass);
                    userProps.Close();
                }

                if (messageCallback != null) {
                    messageCallback("login: succesful!");
                }
            } else {
                return;
            }

            x = selectApp();
            if (x != null) {
                if (messageCallback != null) {
                    string s = x.toString();
                    messageCallback("select-app: succesful!");
                }
            }


            x = getRouteList();
            if (x != null) {
                AppController.sRoutes = x;
                if (messageCallback != null) {
                    string s = x.toString();
                    messageCallback("got route list!");
                }
            }


            AppController.sActiveRoute = null;
            x = navState();
            if (x != null) {
                string s = x.getAttributeValue("routeid");
                if (s != null && s != "") {
                    AppController.sActiveRoute = AppController.sRoutes.getChildForAttribute("id", s);
                    AppController.sActiveRouteID = int.Parse(s);
                    AppController.sActiveRouteMapPathHor = null;
                    AppController.sActiveRouteMapPathVer = null;
                    if (AppController.sActiveRoute == null) {
                        AppController.sActiveRouteID = -1;
                    }
                }
                if (messageCallback != null) {
                    messageCallback("get_state: succesful!");
                    ; // do something with nav state
                }
            }

           x = doNavStart();

        }


        public void stop() {
            if (mAgentKey != null) {
                doLogout();
            }
        }



        public XMLement doLogout() {
            XMLement xml = new XMLement(Protocol.TAG_LOGOUT_REQ);
            AppController.showStatus("logout");
            lock (this) {
                xml = doRequest(xml);
            }
            mAgentKey = null;
            return xml;
        }


        public XMLement navState() {
            XMLement xml = new XMLement(Protocol.TAG_NAV_STATE_REQ);
            lock (this) {
                xml = utopiaRequest(xml);
            }
            if (xml != null && xml.tag == Protocol.TAG_NAV_STATE_RSP)
                return xml;
            else
                return null;
        }

        public XMLement navUGC(bool ugc) {
            XMLement xml = new XMLement(Protocol.TAG_NAV_UGC_REQ);
            xml.addAttribute("visible", (ugc) ? "true" : "false");
            lock (this) {
                xml = utopiaRequest(xml);
            }
            if (xml != null && xml.tag == Protocol.TAG_NAV_UGC_RSP)
                return xml;
            else
                return null;
        }

        public XMLement getPOI(string id) {
            XMLement xml = new XMLement(Protocol.TAG_POI_GET_REQ);
            AppController.showStatus("getPOI");
            xml.addAttribute(Protocol.ATTR_ID, id);
            lock (this) {
                xml = utopiaRequest(xml);
            }
            if (xml != null && xml.tag == Protocol.TAG_POI_GET_RSP) {
                xmlString = xml.toString();
                return xml;
            } else
                return null;
        }

        public XMLement doNavStart() {
            XMLement xml = new XMLement(Protocol.TAG_NAV_START_REQ);
            AppController.showStatus("navStart");
            lock (this) {
                xml = utopiaRequest(xml);
            }
            if (xml != null && xml.tag == Protocol.TAG_NAV_START_RSP)
                return xml;
            else
                return null;
        }

        public XMLement activateRoute(int routeID, bool init) {
            XMLement xml = new XMLement(Protocol.TAG_ACTIVATE_ROUTE_REQ);
            AppController.showStatus("activateRoute");
            xml.addAttribute(Protocol.ATTR_ID, routeID);
            xml.addAttribute(Protocol.ATTR_INIT, init.ToString());

            lock (this) {
                xml = utopiaRequest(xml);
            }
            if (xml != null && xml.tag == Protocol.TAG_ACTIVATE_ROUTE_RSP)
                return xml;
            else
                return null;
        }

        public XMLement deActivateRoute() {
            XMLement xml = new XMLement(Protocol.TAG_DEACTIVATE_ROUTE_REQ);
            AppController.showStatus("deActivateRoute");
            lock (this) {
                xml = utopiaRequest(xml);
            }
            if (xml != null && xml.tag == Protocol.TAG_DEACTIVATE_ROUTE_RSP)
                return xml;
            else
                return null;
        }

        
        public XMLement addMedium(string id) {
            XMLement xml = new XMLement(Protocol.TAG_ADD_MEDIUM_REQ);
            AppController.showStatus("addMedium");
            xml.addAttribute(Protocol.ATTR_ID, id);

            lock (this) {
                xml = utopiaRequest(xml);
            }
            if (xml != null && xml.tag == Protocol.TAG_ADD_MEDIUM_RSP)
                return xml;
            else
                return null;
        }


        public XMLement doLogin() {
            XMLement xml = new XMLement(Protocol.TAG_LOGIN_REQ);
            xml.addAttribute(Protocol.ATTR_NAME, mUser);
            xml.addAttribute(Protocol.ATTR_PASSWORD, mPass);
            xml.addAttribute(Protocol.ATTR_PROTOCOLVERSION, Diwi.Properties.Resources.KwxServerProtocolVersion);

            lock (this) {
                xml = doRequest(xml);
            }

            if (xml != null && xml.tag == Protocol.TAG_LOGIN_RSP) {
                mAgentKey = xml.getAttributeValue("agentkey");
                if (messageCallback != null)
                    messageCallback("Kwx login succes: " + mAgentKey);
            } else {
                if (messageCallback != null)
                    messageCallback("Kwx login failed: " + xml.getAttributeValue("reason"));
            }
            return xml;
        }

        /// <summary>
        /// get route list.
        /// </summary>
        public XMLement getRouteList() {
            if (mAgentKey != null) {
                XMLement req1 = new XMLement(Protocol.TAG_GET_ROUTELIST_REQ);
                XMLement req2 = new XMLement(Protocol.TAG_GET_ROUTELIST_REQ);
                req1.addAttribute("type", "fixed");
                req2.addAttribute("type", "generated");

                req1 = utopiaRequest(req1);
                req2 = utopiaRequest(req2);

                if ( req1!=null && req2 != null && (req1.tag == Protocol.TAG_GET_ROUTELIST_RSP) && (req1.tag == Protocol.TAG_GET_ROUTELIST_RSP)) {
                  for (int i = 0; ; i++) {
                    XMLement x = req1.getChild(i);
                    if (x != null) req2.addChild(x);
                    else break;
                  }
                  return req2;
                } else
                    return null;
            }
            return new XMLement("NotLoggedInError");
        }

        public XMLement getRoute(string id) {
            if (mAgentKey != null) {
                XMLement xml = new XMLement(Protocol.TAG_GET_ROUTE_REQ);
                xml.addAttribute("id",id);

                xml = utopiaRequest(xml);

                if (xml != null && xml.tag == Protocol.TAG_GET_ROUTE_RSP)
                    return xml;
                else
                    return null;
            }
            return new XMLement("NotLoggedInError");
        }

        public string getRouteMap(string id, bool hor) {
            XMLement xml = new XMLement(Protocol.TAG_GET_ROUTE_MAP_REQ);
            AppController.showStatus("getRouteMap");
            xml.addAttribute("id", id);
            if (hor) {
                xml.addAttribute("height", "240");
                xml.addAttribute("width", "320");
            } else {
                xml.addAttribute("height", "320");
                xml.addAttribute("width", "240");
            }
            xml = utopiaRequest(xml);

            if (xml != null && xml.tag == Protocol.TAG_GET_ROUTE_MAP_RSP)
                return xml.getAttributeValue("url");
            else
                return null;
        }

        public string getBoundsMap(int id, float radiusKm, bool hor) {

            float urtLat, urtLon, llbLat, llbLon;
            XMLement req = new XMLement(Protocol.TAG_NAV_GET_MAP_REQ);
            AppController.showStatus("getBoundedMap");

            if (hor) {
                urtLat = GpsReader.lat + GpsReader.km2degLat(radiusKm);
                llbLat = GpsReader.lat - GpsReader.km2degLat(radiusKm);
                urtLon = GpsReader.lon + GpsReader.km2degLon((float)(radiusKm * 1.5));
                llbLon = GpsReader.lon - GpsReader.km2degLon((float)(radiusKm * 1.5));
                req.addAttribute("height", "320");
                req.addAttribute("width", "240");
            } else {
                urtLat = GpsReader.lat + GpsReader.km2degLat((float)(radiusKm * 1.5));
                llbLat = GpsReader.lat - GpsReader.km2degLat((float)(radiusKm * 1.5));
                urtLon = GpsReader.lon + GpsReader.km2degLon(radiusKm);
                llbLon = GpsReader.lon - GpsReader.km2degLon(radiusKm);
                req.addAttribute("height", "240");
                req.addAttribute("width", "320");
            }

            MapHandler.setBounds(hor, urtLat, urtLon, llbLat, llbLon);
  //         req.addAttribute("id", id.ToString() );

            req.addAttribute("llbLat", llbLat.ToString(mUSFormat));
            req.addAttribute("llbLon", llbLon.ToString(mUSFormat));
            req.addAttribute("urtLat", urtLat.ToString(mUSFormat));
            req.addAttribute("urtLon", urtLon.ToString(mUSFormat));

            req = utopiaRequest(req);

            if ((req != null) && (req.tag == Protocol.TAG_NAV_GET_MAP_RSP)) {
                return req.getAttributeValue("url");
            }
            return null;
        }

	/// <summary>
    /// Select application on server.
    /// </summary>
        public XMLement selectApp() {

            // Create XML request
            XMLement request = new XMLement(Protocol.TAG_SELECT_APP_REQ);
            request.addAttribute(Protocol.ATTR_APPNAME, Diwi.Properties.Resources.KwApp);
            request.addAttribute(Protocol.ATTR_ROLENAME, Diwi.Properties.Resources.KwRole);

            // Save for later session restore
            selectAppRequest = request;

            // Execute request
            request = doRequest(request);

            // Throw exception or return positive response
            return request;
        }


        /// <summary>
        ///  Sends position sample to the server.
        /// lat/lon format broken on server ?
        /// </summary>
        public void sendSample() {

            if (mAgentKey != null) {
                XMLement xml = new XMLement(Protocol.TAG_NAV_POINT_REQ);
                XMLement pt = new XMLement("pt");

                pt.addAttribute("lon", GpsReader.lon.ToString(GpsReader.mUSFormat));
                pt.addAttribute("lat", GpsReader.lat.ToString(GpsReader.mUSFormat));

               // pt.addAttribute("nmea", GpsReader.nmea);

                xml.addChild(pt);

                xml = utopiaRequest(xml);

                if (xml != null && xml.tag == Protocol.TAG_NAV_POINT_RSP) {
                    if (poiCallback != null) {
                        poiCallback(xml, GpsReader.lat, GpsReader.lon);
                    }
                } 
            }
        }


        /// <summary>
        /// Encapsulate request in utopia request.
        /// </summary>
        public XMLement utopiaRequest(XMLement anElement)
        {
            XMLement req = new XMLement();
            req.tag = Protocol.TAG_UTOPIA_REQ;
            req.addChild(anElement);
            lock (this) {
                req = doRequest(req);
            }
            if ((req != null) && (req.tag == "utopia-rsp")) {
                return req.firstChild();
            } else {
                // add error handling!
            }

            return null;
        }

        private XMLement doRequest(XMLement anElement) {
            string url = mServer;

            if (mAgentKey != null) {
                url += "?agentkey=" + mAgentKey;
            } else {
                // login
                url += "?timeout=" + Diwi.Properties.Resources.KwxServerTimeout;
            }

            try {
                // create the web request
                HttpWebRequest req = (HttpWebRequest)WebRequest.Create(url);
                UTF8Encoding encoding = new UTF8Encoding();
                byte[] postBytes = encoding.GetBytes(anElement.toString());

                req.KeepAlive = true;
                req.Timeout = 20000;
                req.Method = "POST";
                req.ContentType = "text/xml";
                req.ContentLength = postBytes.Length;
                Stream postStream = req.GetRequestStream();
                postStream.Write(postBytes, 0, postBytes.Length);
                postStream.Close();

                // make the connect
                HttpWebResponse resp = (HttpWebResponse)req.GetResponse();

                // get the page data
                StreamReader sr = new StreamReader(resp.GetResponseStream());
                string pageData = sr.ReadToEnd();
                sr.Close();

                // get the status code (should be 200)
                // status = resp.StatusCode;

                // close the connection
                resp.Close();

                AppController.showStatus("");

                return XMLement.createFromRawXml(pageData);

            } catch (WebException e) {
                AppController.showStatus("");
                string str = string.Format("Caught WebException: {0}", e.Status.ToString());
                AppController.sLog.WriteLine(str);
                return new XMLement("web-exception");
                /*
                HttpWebResponse resp = (HttpWebResponse)e.Response;
                if (null != resp)
                {
                    // get the failure code from the response
                    status = resp.StatusCode;
                    str = string.Format("{0} ({1})", str, status);

                    // close the response
                    resp.Close();
                }
                else {
                    // generic connection error
                    status = (HttpStatusCode)(-1);
                }

                // update the ui so we can know what went wrong
                if (CurrentPageEvent != null)
                {
                    CurrentPageEvent(str);
                }
                */
            } catch (Exception) {
                AppController.showStatus("");
                return null;
            }
        }
    }
}
 
 
