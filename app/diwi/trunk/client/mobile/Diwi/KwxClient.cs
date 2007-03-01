using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Net;
using System.Threading;
using System.Windows.Forms;
using Diwi;

namespace Diwi {
    class KwxClient {
        public delegate void MessageCallback(string mess);
        public event MessageCallback messageCallback;

        private string mServer;
        private string mAgentKey;
        private Thread mHbThread;

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
            doLogin();
        }

        public void stop() {
            if (mAgentKey != null) {
                doLogout();
                mHbThread.Abort();
            }
        }

        private void hbHandler() {
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
            XMLement xml = new XMLement();
            xml.tag = Protocol.TAG_LOGOUT_REQ;
            lock (this) {
                xml = doRequest(xml);
            }

            mAgentKey = null;

            return xml;
        }


        public XMLement doLogin() {
            XMLement xml = new XMLement();
            xml.tag = Protocol.TAG_LOGIN_REQ;
            xml.addAttribute(Protocol.ATTR_NAME, Diwi.Properties.Resources.KwxServerUsername);
            xml.addAttribute(Protocol.ATTR_PASSWORD, Diwi.Properties.Resources.KwxServerPassword);
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


        public XMLement utopiaRequest(XMLement anElement) {
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
                Console.WriteLine(str);
                throw e;

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
 
 
