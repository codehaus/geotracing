using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using System.IO;
using System.Globalization;
using System.Resources;
using Diwi;

namespace Diwi {
    /// <summary>
    /// Implements the GPS connection.
    /// singleton class; uses file with raw nmea if a GPS is not found.
    /// communicates information through callback.
    /// </summary>
    public class GpsReader
    {
        public enum sMess  {M_POS, M_SPEED, M_FIX, M_PREC, M_BEARING, M_NUMSAT, M_DEMO, M_MESSAGE };
        public static float KPHPerKnot = 1.852F;

        public delegate void CallbackHandler(int message);
        public event CallbackHandler callback;


        private CultureInfo mUSFormat = new CultureInfo(0x0409); // constant voor en-US domain. gebruikt in nummerformat parsing 
        private System.IO.Ports.SerialPort mSerialPort;
        private Thread mReadDataThread = null;
        private bool mIsRunning = false;
        private string mPort;
        private string mNMEA;
        private bool mIsLogging = true;
        private StreamReader nmeaDemoFile = null;

        static private GpsReader sGPS = null;


        private bool  mCanDemo = true;
        private bool  mDemo = false;
        private bool  mHasFix = false;
        private float mBearing, mSpeed, mLat, mLon, mPrecision;
        private int   mNumSats;

        #region properties


        static public string nmea {
            get { return sGPS.mNMEA; }
        }

        static public bool canDemo {
            get { return sGPS.mCanDemo; }
            set { 
                sGPS.mCanDemo = value; 
            }
        }
        static public bool demo {
            get { return sGPS.mDemo; }
        }
        static public bool fix {
            get { return sGPS.mHasFix; }
        }
        static public int numSats {
            get { return sGPS.mNumSats; }
        }
        static public float bearing {
            get { return sGPS.mBearing; }
        }
        static public float speed {
            get { return sGPS.mSpeed; }
        }
        static public float precision {
            get { return sGPS.mPrecision; }
        }
        static public float lat {
            get { return sGPS.mLat; }
        }
        static public float lon {
            get { return sGPS.mLon; }
        }
        static public string latitude {
            get {
                string s;
                float deg = sGPS.mLat;
                if (deg < 0) {
                    deg = -deg;
                    s = " S";
                } else {
                    s = " N";
                }
                float min = (float)60.0 * (deg - (float)((int)deg));
                float sec = (float)60.0 * (min - (float)((int)min));
                s = ((int)deg).ToString() + "°" + ((int)min).ToString() + "'" + ((int)sec).ToString() + "''" + s;
                return s;
            }
        }
        static public string longtitude {
            get {
                string s;
                float deg = sGPS.mLon;
                if (deg < 0) {
                    deg = -deg;
                    s = " W";
                } else {
                    s = " E";
                }
                float min = (float)60.0 * (deg - (float)((int)deg));
                float sec = (float)60.0 * (min - (float)((int)min));
                s = ((int)deg).ToString() + "°" + ((int)min).ToString() + "'" + ((int)sec).ToString() + "''" + s;
                return s;
            }
        }




#endregion


        public static GpsReader instance {
            get {
                if (sGPS != null) return sGPS;
                return sGPS = new GpsReader();
            }
        }

        private GpsReader() {
            mPort = Diwi.Properties.Resources.GpsSerialPort;
            start();
        }


        /// <summary>
        /// open a serial port and start reading, asynchronously.
        /// </summary>
        public void start() {
            if (mCanDemo) {
                try {
                    nmeaDemoFile = new StreamReader(@"\DemoNMEA.txt");
                } catch (FileNotFoundException) {
                    mCanDemo = false;
                }
            }
            if (mReadDataThread == null) {
                if (mSerialPort == null) {
                    mSerialPort = new System.IO.Ports.SerialPort(mPort);
                }
                mReadDataThread = new Thread(readThread);
            }
            mIsRunning = true;
            mReadDataThread.Start();
        }

        /// <summary>
        /// stop reading, close ports.
        /// </summary>
        public void stop()
        {
            mIsRunning = false;
            Thread.Sleep(500);
            if (mSerialPort.IsOpen) {
                mSerialPort.Close();
                mSerialPort.Dispose();
                mSerialPort = null;
            }

            if (mReadDataThread != null) {
                mReadDataThread.Abort();
                mReadDataThread = null;
            }

            if (nmeaDemoFile != null) {
                nmeaDemoFile.Close();
                nmeaDemoFile = null;
            }

        }


        void setFixStatus(bool f) {
            if (mHasFix != f) {
                mHasFix = f;
                if (callback != null) {
                    callback((int)sMess.M_FIX);
                }
            }
        }

        /// <summary>
        /// thread sun loop reading GPS, or trying.
        /// </summary>
        private void readThread() {

            // keep trying to open serial port; when fail, send data from file if 'candemo'
            while (mIsRunning == true) {

                // new attempt every 2 seconds
                while (mIsRunning == true) {
                    try {
                        mSerialPort.Open();
                        if (callback != null) {
                            mDemo = false;
                            if(mCanDemo)
                                callback((int)sMess.M_DEMO);
                        }
                        break;
                    } catch (IOException) {
                        mDemo = true;
                        if (mCanDemo) {
                            if (callback != null) {
                                callback((int)sMess.M_DEMO);
                            }
                            // read from file
                            for (int i = 0; i < 6; i++) {
                                string s = nmeaDemoFile.ReadLine();
                                if (s != null) {
                                    parse(s);
                                } else {
                                    nmeaDemoFile.Close();
                                    try {
                                        nmeaDemoFile = new StreamReader(@"\DemoNMEA.txt");
                                    } catch (Exception) {
                                        mCanDemo = false;
                                    }
                                }
                            }
                        }
                        Thread.Sleep(2000);
                    }
                }

                while (mIsRunning == true) {
                    string data = "";

                    try {
                        data = mSerialPort.ReadLine();
                    } catch (IOException) {
                        // port broken, or somesuch
                        if (mSerialPort.IsOpen) {
                            mSerialPort.Close();
                        }
                        // up one loop and try to open the port again
                        break;
                    } catch (TimeoutException) {
                        // no data, sleep for a while and try again
                        Thread.Sleep(1000);
                        continue;
                    }

                    if (data.Length > 0) {
                        // got data from GPS
                        parse(data);
                    }
                }
            }
        }

        #region parsing

        /// <summary>
        /// parse NMEA string received.
        /// currently only RMC and GGA strings are processed
        /// </summary>
        public bool parse(string sentence)
        {
            string[] words;

            if (mIsLogging) {
                AppController.sLog.WriteLine(sentence);
            }

            mNMEA = sentence;

            words = sentence.Split(',');

            switch (words[0]) {
                case "$GPRMC":
                    return ParseGPRMC(words);
                case "$GPGGA":
                    return ParseGPGGA(words);
                default:
                    return false;
            }
        }


        public bool ParseGPGGA(string[] words) {

            if (words[7] != "") {
                mNumSats = int.Parse(words[7]);
                if (callback != null)
                    callback((int)sMess.M_NUMSAT);
            }

            if (words[8] != "") {
                mPrecision = float.Parse(words[8], mUSFormat);
                if (callback != null)
                    callback((int)sMess.M_PREC);
            }

            return true;
        }

        public bool ParseGPRMC(string[] words) {

            if (words[2] == "A")
                setFixStatus(true);
            else {
                setFixStatus(false);
                return false; // geen fix; geen interessante data!
            }

            if (words[3] != "" && words[4] != "" && words[5] != "" && words[6] != "") {

                float lat = float.Parse(words[3].Substring(0, 2), mUSFormat);
                float min = float.Parse(words[3].Substring(2), mUSFormat);

                lat += min / (float)60.0;
                if (words[4] != "N") lat = -lat;


                float lon = float.Parse(words[5].Substring(0, 3), mUSFormat);
                min = float.Parse(words[5].Substring(3), mUSFormat);

                lon += min / (float)60.0;
                if (words[6] != "E") lon = -lon;

                mLat = lat;
                mLon = lon;

                // Notify the calling application of the change
                if (callback != null)
                    callback((int)sMess.M_POS);
                AppController.sKwxClient.sendSample();
            }

            if (words[7] != "") {

                mSpeed = float.Parse(words[7], mUSFormat) * KPHPerKnot;
                if (callback != null)
                    callback((int)sMess.M_SPEED);
            }

            if (words[8] != "") {
                mBearing = float.Parse(words[8], mUSFormat);
                if (callback != null)
                    callback((int)sMess.M_BEARING);
            }
            return true;
        }
        #endregion

    }
}
