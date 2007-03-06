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
        public event MessageCallback messageCallback;

        private string mUser = Diwi.Properties.Resources.KwxServerUsername;
        private string mPass = Diwi.Properties.Resources.KwxServerPassword;
        private string mServer;
        private string mAgentKey;
        private Thread mHbThread;
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
                    messageCallback("select-app: succesful!");
                }
            }

            x = newTrack("diwiTrack");
            if (x.tag == "utopia-rsp") {
                if (messageCallback != null) {
                    messageCallback("track created");
               }
            }
        }


        public void stop() {
            if (mAgentKey != null) {
                doLogout();
                mHbThread.Abort();
            }
        }

        /// <summary>
        /// While connected, send heartbeat every 5 seconds.
        /// Asynchronous thread run loop.
        /// </summary>
        private void hbHandler()
        {
            while (mAgentKey != null) {
                Thread.Sleep(5000);
                DateTime d1 = DateTime.UtcNow;
                XMLement req = new XMLement();
                req.tag = "t-hb-req";
                req.addAttribute("t", "" + d1.ToFileTime());

                lock (this) {
                    req = utopiaRequest(req);
                    if (messageCallback != null) {
                        //messageCallback(req.toString());
                    }
                }
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

            mHbThread = new Thread(new ThreadStart(hbHandler));
            mHbThread.Start();

            return xml;
        }

        /// <summary>
        /// create new track.
        /// </summary>
        public XMLement newTrack(string aName) {
            if (mAgentKey != null) {
                DateTime d1 = DateTime.UtcNow;
                XMLement req = new XMLement("t-trk-create-req");
                req.addAttribute("name", aName);
                req.addAttribute("t", d1.ToFileTime().ToString());
                // Minimal mode: tracks are made daily (Track type 2)
                req.addAttribute("type", "2");

                req = utopiaRequest(req);

                trackId = req.getAttributeValue("id");
                return req;
            }
            return new XMLement("NotLoggedInError");
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
                XMLement xml = new XMLement("t-trk-write-req");

                XMLement pt = new XMLement("pt");

                pt.addAttribute("nmea", GpsReader.nmea);

                //pt.addAttribute("lon", GpsReader.lon.ToString());
                //pt.addAttribute("lat", GpsReader.lat.ToString());
                xml.addChild(pt);
                xml = utopiaRequest(xml);
                string s = xml.toString();
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
            return doRequest(req);
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
                else
                {
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
 
 
