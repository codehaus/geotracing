using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Net;
using System.Threading;
using System.Windows.Forms;

namespace Diwi {


    class MediaDownloader {
        private Thread mThread;
        string uri;
        string path;
        bool busy=false;
        AppController.DownloadCallbackHandler callb;


        public MediaDownloader(string url, string p, AppController.DownloadCallbackHandler cb) {
            if (url != null) {
                uri = Uri.UnescapeDataString(url);
                callb = cb;
                mThread = new Thread(new ThreadStart(threadHandler));
                path = p;
                mThread.Start();
            }
        }

        public void abort() {
            mThread.Abort();
            AppController.sProgBar.bumpDown();
        }

        public bool doDownload(string url, string p) {
            if( !busy ) {
                uri = Uri.UnescapeDataString(url);
                path = p;
                mThread.Start();
                return true;
            }
            return false;
        }

        private void threadHandler() {
            int n;
            byte[] inBuffer = new byte[4096];
            Stream stream = null;


            try {

                busy = true;

                HttpWebRequest req = (HttpWebRequest)WebRequest.Create(uri);
                HttpWebResponse response = (HttpWebResponse)req.GetResponse();
                stream = response.GetResponseStream();

                AppController.sProgBar.bumpUp();

                try {
                    FileStream fstream = new FileStream(path, FileMode.Create, FileAccess.Write);
                    do {
                        n = stream.Read(inBuffer, 0, 4096);
                        fstream.Write(inBuffer, 0, n);
                    } while (n > 0);

                    fstream.Close();
                } catch (IOException) {
                    ; // MessageBox.Show(e.Message, "Error downloading file.");
                }

                AppController.sProgBar.bumpDown();

                stream.Close();

                if ((AppController.sQuitting == false) && callb != null)
                    callb(path, false);
            } catch (WebException) { 

            }

            busy = false;

        }
    }

    class MediaUploader {
        public static bool sQuitting = false;
        private static string bounds = "---------------" + DateTime.UtcNow.Ticks.ToString();
        string localFile;
        string name;
        string mimeType;
        UTF8Encoding encoding;
        byte[] boundary;
        byte[] NEWLINE;
        byte[] PREFIX;

        private Thread mThread;
        string uri;
        WalkRoutePage.CallbackHandler callb;



        public MediaUploader(string fileName, string n, string mime, WalkRoutePage.CallbackHandler cb) {
            encoding = new UTF8Encoding();
            localFile = fileName;
            name = n;
            mimeType = mime;
            boundary = encoding.GetBytes(bounds);
            NEWLINE = encoding.GetBytes("\r\n");
            PREFIX = encoding.GetBytes("--");
            uri = Diwi.Properties.Resources.KwxMediaServerUrl;
            callb = cb;
            mThread = new Thread(new ThreadStart(threadHandler));
            mThread.Start();
        }

 
        private void writeField(string name, string value, Stream s) {
            if (value == null || value == "") {
                value = "noName";
            }
            /*
            --boundary\r\n
            Content-Disposition: form-data; name="<fieldName>"\r\n
            \r\n
            <value>\r\n
            */
            // write boundary

            s.Write(PREFIX, 0, PREFIX.Length);
            s.Write(boundary, 0, boundary.Length);
            s.Write(NEWLINE, 0, NEWLINE.Length);
            
            
            // write content header
            string t = "Content-Disposition: form-data; name=\"" + name + "\"";
            byte[] b = encoding.GetBytes(t);
            s.Write(b,0,b.Length);

            s.Write(NEWLINE, 0, NEWLINE.Length);
            s.Write(NEWLINE, 0, NEWLINE.Length);
            // write content
            b = encoding.GetBytes(value);
            s.Write(b,0,b.Length);
            s.Write(NEWLINE, 0, NEWLINE.Length);
        }


        private void threadHandler() {
            byte[] inData = new byte[4096];
            FileInfo fi = new FileInfo(localFile);

            HttpWebRequest req = (HttpWebRequest)WebRequest.Create(uri);

            req.ContentType = "multipart/form-data; boundary=" + bounds;

            req.AllowWriteStreamBuffering = true;
            req.Method = "POST";


            // we need the request stream 
            Stream reqStream = req.GetRequestStream();



            writeField("xmlrsp", "true", reqStream);
            writeField("agentkey", AppController.sKwxClient.agentKey, reqStream);
            writeField("name", name, reqStream);


            // add the file...



            reqStream.Write(PREFIX, 0, PREFIX.Length);
            reqStream.Write(boundary, 0, boundary.Length);
            reqStream.Write(NEWLINE, 0, NEWLINE.Length);
            // write content header
            string t = "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fi.Name + "\"";
            inData = encoding.GetBytes(t);
            reqStream.Write(inData, 0, inData.Length);
            reqStream.Write(NEWLINE, 0, NEWLINE.Length);

            t = "Content-Type: " + mimeType;
            inData = encoding.GetBytes(t);
            reqStream.Write(inData, 0, inData.Length);
            reqStream.Write(NEWLINE, 0, NEWLINE.Length);

            reqStream.Write(NEWLINE, 0, NEWLINE.Length);

            // write content

            inData = new byte[1024];
            FileStream rdr = File.OpenRead(localFile);

            //FileStream rdr = new FileStream(localFile, FileMode.Open);
            int bytesRead = (int)rdr.Length;
            AppController.sProgBar.bumpUp();

            int total = 0;
            bytesRead = rdr.Read(inData, 0, 1024);
            while (bytesRead > 0) {
                reqStream.Write(inData, 0, bytesRead);
                bytesRead = rdr.Read(inData, 0, 1024);
                total += bytesRead;
            }


            rdr.Close();

            // add last boundary

            reqStream.Write(NEWLINE, 0, NEWLINE.Length);
            reqStream.Write(PREFIX, 0, PREFIX.Length);
            reqStream.Write(boundary, 0, boundary.Length);
            //           reqStream.Write(PREFIX, 0, PREFIX.Length);
            reqStream.Write(NEWLINE, 0, NEWLINE.Length);

            reqStream.Close();

            try {

                HttpWebResponse response = (HttpWebResponse)req.GetResponse();
                StreamReader sr = new StreamReader(response.GetResponseStream());
                string pageData = sr.ReadToEnd();
                sr.Close();

                XMLement xml = XMLement.createFromRawXml(pageData);
                if (xml != null) {
                    if (xml.tag == "medium-insert-rsp") {
                        xml = AppController.sKwxClient.addMedium(xml.getAttributeValue("id"));
                    }
                }


                if ((AppController.sQuitting == false) && callb != null) callb();

            } catch (IOException) {
            }

            if (AppController.sQuitting == false) AppController.sProgBar.bumpDown();

        }
    }
}

/*
public void UploadFileBinary(string localFile, string uploadUrl)
{
  HttpWebRequest req = (HttpWebRequest)WebRequest.Create(uploadUrl);

  req.Method = "POST";
  req.AllowWriteStreamBuffering = true;

  // Retrieve request stream 
  Stream reqStream = req.GetRequestStream();

  // Open the local file
  FileStream rdr = new FileStream(localFile, FileMode.Open);

  // Allocate byte buffer to hold file contents
  byte[] inData = new byte[4096];

  // loop through the local file reading each data block
  //  and writing to the request stream buffer
  int bytesRead = rdr.Read(inData, 0, inData.Length);
  while (bytesRead > 0)
  {
    reqStream.Write(inData, 0, bytesRead);
    bytesRead = rdr.Read(inData, 0, inData.Length);
  }

  rdr.Close();
  reqStream.Close();

  req.GetResponse();
}

 * 
 * 
 * 
 * 	public JXElement uploadMedium(String aName, String aType, String aMime, long aTime, byte[] theData, boolean encode, String theTags) {
		HTTPUploader uploader = new HTTPUploader();
		JXElement rsp = null;
		try {
			uploader.connect(url + "/media.srv");
			if (aName == null || aName.length() == 0) {
				aName = "unnamed " + aType;
			}

			uploader.writeField("agentkey", agentKey);
			uploader.writeField("name", aName);
			uploader.writeFile(aName, aMime, "mt-upload", theData);

			rsp = uploader.getResponse();
			if (Protocol.isNegativeResponse(rsp)) {
				return rsp;
			}

			// Upload OK, now add medium to track
			JXElement req = new JXElement("t-trk-add-medium-req");
			req.setAttr("id", rsp.getAttr("id"));
			req.setAttr("t", aTime);

			// Optional tags
			if (theTags != null && theTags.length() > 0) {
				req.setAttr("tags", theTags);
			}

			utopiaReq(req);

		} catch (Throwable t) {
			Log.log("Upload err: " + t);
		}
		return rsp;
	}


 */