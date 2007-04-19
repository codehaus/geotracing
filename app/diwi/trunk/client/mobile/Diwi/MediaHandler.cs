using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Net;
using System.Threading;

namespace Diwi {

    class MediaDownloader {
        private Thread mThread;
        string uri;
        bool busy=false;
        WalkRoutePage.CallbackHandler callb;



        public MediaDownloader(string url, WalkRoutePage.CallbackHandler cb) {
            uri = Uri.UnescapeDataString(url);
            callb = cb;
            mThread = new Thread(new ThreadStart(threadHandler));
            mThread.Start();
        }

        bool doDownload(string url) {
            if( !busy ) {
                uri = Uri.UnescapeDataString(url);
                mThread.Start();
                return true;
            }
            return false;
        }



        private void threadHandler() {
            int n;
            byte[] inBuffer = new byte[1024];
            Stream stream = null;
            string path = @"\tmp.jpg";

            busy = true;

            HttpWebRequest req = (HttpWebRequest)WebRequest.Create(uri);
            HttpWebResponse response = (HttpWebResponse)req.GetResponse();
            stream = response.GetResponseStream();

            FileStream fstream = new FileStream(path, FileMode.OpenOrCreate, FileAccess.Write);

            do {
                n = stream.Read(inBuffer, 0, 1024);
                fstream.Write(inBuffer, 0, n);
            } while (n == 1024);

            fstream.Close();
            stream.Close();

            if (callb != null)
                callb(path);

            busy = false;

        }
    }

    class MediaHandler {
        static public bool doDownload(string url, string path) {
            int n;
            int num=0;
            byte[] inBuffer = new byte[1024];
            Stream stream = null;


            HttpWebRequest req = (HttpWebRequest)WebRequest.Create(Uri.UnescapeDataString(url));
            HttpWebResponse response = (HttpWebResponse)req.GetResponse();
            stream = response.GetResponseStream();

            FileStream fstream = new FileStream(path, FileMode.OpenOrCreate, FileAccess.Write);

            do {
                n = stream.Read(inBuffer, 0, 1024);
                fstream.Write(inBuffer, 0, n);
                num += n;
            } while (n == 1024);

            fstream.Close();
            stream.Close();

            return true;
        }
    }
}

