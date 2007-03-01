using System;
using System.IO;
using System.Collections.Generic;
using System.Windows.Forms;

namespace Diwi {
    static class Program {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>

        public static StreamWriter sLog;
        public static KwxClient sKwxClient;
        public static GpsReader sGpsReader;

        [MTAThread]
        static void Main() {
            sLog = File.CreateText("DiwiLog.txt");

            Form f1 = new Form1();
            Application.Run(f1);
        }
    }
}