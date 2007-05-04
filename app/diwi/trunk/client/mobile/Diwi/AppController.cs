using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Threading;
using System.Drawing;
using System.Reflection;
using Microsoft.WindowsMobile.Forms;
using System.Windows.Forms;
using System.Runtime.InteropServices;


namespace Diwi {
    /// <summary>
    /// central controller for main components in the app.
    /// KwxServer, GpsReader, MapReader, access to resources
    /// </summary>
    class AppController
    {
        public static int sActiveRouteID = 0;
        public static XMLement sActiveRoute = null;
        public static string sActiveRouteMapPathHor = null;
        public static string sActiveRouteMapPathVer = null;
        public static XMLement sFixedRoutes;
        public static StreamWriter sLog;
        public static KwxClient sKwxClient;
        public static GpsReader sGpsReader;
        public static Assembly sAssembly = Assembly.GetExecutingAssembly();

        public static Bitmap backgroundHorBitmap;
        public static Bitmap backgroundVerBitmap;

        private static Sound sPloink;


        public static void activate() {
            sLog = File.CreateText("DiwiLog.txt");

            Stream stream = sAssembly.GetManifestResourceStream(@"Diwi.Resources.back_horz.gif");
            backgroundHorBitmap = new Bitmap(stream);
            stream.Close();

            stream = sAssembly.GetManifestResourceStream(@"Diwi.Resources.back_vert.gif");
            backgroundVerBitmap = new Bitmap(stream);
            stream.Close();

            stream = sAssembly.GetManifestResourceStream(@"Diwi.Resources.ploink.wav");
            sPloink = new Sound(stream);
            stream.Close();

            sKwxClient = KwxClient.instance;
            sGpsReader = GpsReader.instance;
        }

        public static void deactivate() {
            sGpsReader.stop();
            sKwxClient.stop();
            Thread.Sleep(1000);
        }


        public static string makeFoto() {
            CameraCaptureDialog cameraCaptureDialog = new CameraCaptureDialog();
            cameraCaptureDialog.Owner = null;
            cameraCaptureDialog.Title = "Neem een foto";
            cameraCaptureDialog.Mode = CameraCaptureMode.Still;
            if (cameraCaptureDialog.ShowDialog() == DialogResult.OK && cameraCaptureDialog.FileName.Length > 0) {
                return cameraCaptureDialog.FileName;
            }
            return null;
        }


        public static void SysBeep() {
            sPloink.Play();
        }
 

    }
}
