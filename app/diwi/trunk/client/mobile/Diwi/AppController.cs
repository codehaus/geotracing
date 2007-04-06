using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Threading;
using System.Reflection;


namespace Diwi {
    /// <summary>
    /// central controller for main components in the app.
    /// KwxServer, GpsReader, MapReader, access to resources
    /// </summary>
    class AppController
    {
        public static StreamWriter sLog;
        public static KwxClient sKwxClient;
        public static GpsReader sGpsReader;
        public static Assembly sAssembly = Assembly.GetExecutingAssembly();


        public static void activate() {
            sLog = File.CreateText("DiwiLog.txt");
            sKwxClient = KwxClient.instance;
            sGpsReader = GpsReader.instance;
        }

        public static void deactivate() {
            sGpsReader.stop();
            sKwxClient.stop();
            Thread.Sleep(1000);
        }
    }
}
