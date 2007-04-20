using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Net;
using System.Threading;
using System.Windows.Forms;
using Diwi;

namespace Diwi {
    /// <summary>
    /// The KeyWorx server singleton class.
    /// Error handling needs work, media up- and download need to be implemented.
    /// curently uses geotracing protocol....
    /// </summary>

    class KwxClient {
        public delegate void MessageCallback(string mess);
        public delegate void POICallback(XMLement mess, float lat, float lon);
        public event MessageCallback messageCallback;
        public event POICallback poiCallback;

        private string mUser = Diwi.Properties.Resources.KwxServerUsername;
        private string mPass = Diwi.Properties.Resources.KwxServerPassword;
        private string mServer;
        private string mAgentKey;
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
                if (messageCallback != null) {
                    messageCallback("login: succesful!");
                }
            }

            x = selectApp();
            if (x.tag == "select-app-rsp") {
                if (messageCallback != null) {
                    string s = x.toString();
                    messageCallback("select-app: succesful!");
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
            lock (this) {
                xml = doRequest(xml);
            }
            mAgentKey = null;
            return xml;
        }


        public XMLement doNavStart() {
            XMLement xml = new XMLement(Protocol.TAG_NAV_START_REQ);
            lock (this) {
                xml = utopiaRequest(xml);
            }
            return xml;
        }


        public XMLement doLogin() {
            XMLement xml = new XMLement(Protocol.TAG_LOGIN_REQ);
            xml.addAttribute(Protocol.ATTR_NAME, mUser);
            xml.addAttribute(Protocol.ATTR_PASSWORD, mPass);
            xml.addAttribute(Protocol.ATTR_PROTOCOLVERSION, Diwi.Properties.Resources.KwxServerProtocolVersion);

            lock (this) {
                xml = doRequest(xml);
            }

            if (xml.tag == "login-rsp") {
                mAgentKey = xml.getAttributeValue("agentkey");
                if (messageCallback != null)
                    messageCallback("Kwx login succes: " + mAgentKey);
            } else {
                if (messageCallback != null)
                    messageCallback("Kwx login failed: " + xml.getAttributeValue("reason"));
                throw new Exception("login failed: " + xml.getAttributeValue("reason"));
            }
            return xml;
        }

        /// <summary>
        /// get route list.
        /// </summary>
        public XMLement getRouteList() {
            if (mAgentKey != null) {
                XMLement req = new XMLement("route-getlist-req");
                req.addAttribute("type", "fixed");

                req = utopiaRequest(req);
                return req;
            }
            return new XMLement("NotLoggedInError");
        }

        public XMLement getRoute(string id) {
            if (mAgentKey != null) {
                XMLement req = new XMLement("route-get-req");
                req.addAttribute("id",id);

                req = utopiaRequest(req);

                return req;
            }
            return new XMLement("NotLoggedInError");
        }

        public string getRouteMap(string id, bool hor) {
            XMLement req = new XMLement("route-get-map-req");
            req.addAttribute("id", id);
            req.addAttribute("height", hor? "240" : "320");
            req.addAttribute("width",  hor? "320" : "240");
            req = utopiaRequest(req);
            if (req.tag == "route-get-map-rsp") {
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
                XMLement xml = new XMLement("nav-point-req");
                XMLement pt = new XMLement("pt");
                
               // pt.addAttribute("lon", GpsReader.lon.ToString());
               // pt.addAttribute("lat", GpsReader.lat.ToString());

                pt.addAttribute("nmea", GpsReader.nmea);

                xml.addChild(pt);

                xml = utopiaRequest(xml);
                if (poiCallback != null) {
                    poiCallback(xml, GpsReader.lat, GpsReader.lon);
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

            req = doRequest(req);

            if (req.tag == "utopia-rsp") {
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

                return XMLement.createFromRawXml(pageData);

            } catch (WebException e) {
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
            }
        }
    }
}
 
 
