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
using Microsoft.WindowsMobile.Status;
using System.ComponentModel;

using System.Data;
using System.Diagnostics;


namespace Diwi {
    /// <summary>
    /// central controller for main components in the app.
    /// KwxServer, GpsReader, MapReader, access to resources
    /// </summary>
    class AppController
    {
        public static int sActiveRouteID = -1;
        public static XMLement sActiveRoute = null;
        public static string sActiveRouteMapPathHor = null;
        public static string sActiveRouteMapPathVer = null;
        public static XMLement sRoutes;
        public static StreamWriter sTrackLog;
        public static StreamWriter sEventLog;
        public static KwxClient sKwxClient;
        public static GpsReader sGpsReader;
        public static string sUserName = null;
        public static string sUserPass = null;
        public static string sUserProps = null;
        public static string sVideoFileName = null;
        public static Progress sProgBar;
        public static Assembly sAssembly = Assembly.GetExecutingAssembly();

        public delegate void DownloadCallbackHandler(string path);

        public static Bitmap backgroundHorBitmap;
        public static Bitmap backgroundVerBitmap;

        //public static Backlight sBacklight = new Backlight();

        private static Sound sPloink;
        private static Sound sClick;
        private static Sound sPOI;

        public static Icon sTerugIcon, sEmptyIcon, sUGCIcon, sFotoIcon, sInfoIcon;
        public static Icon sTextIcon, sVolgIcon, sVideoIcon, sKiesIcon, sStruinIcon, sCheckIcon;

        public static void showStatus(string s) {
            DiwiPageBase.sCurrentPage.printStatus(s);
        }

        public static void setRequestIn() {
            DiwiPageBase.sCurrentPage.printStatus("");
        }


        public static void activate() {
            sTrackLog = File.CreateText("DiwiTrackLog.txt");
            sEventLog = File.CreateText("DiwiEventLog.txt");

            Stream stream = sAssembly.GetManifestResourceStream(@"Diwi.Resources.back_horz.gif");
            backgroundHorBitmap = new Bitmap(stream);
            stream.Close();

            stream = sAssembly.GetManifestResourceStream(@"Diwi.Resources.back_vert.gif");
            backgroundVerBitmap = new Bitmap(stream);
            stream.Close();

            stream = sAssembly.GetManifestResourceStream(@"Diwi.Resources.ploink.wav");
            sPloink = new Sound(stream);
            stream.Close();

            stream = sAssembly.GetManifestResourceStream(@"Diwi.Resources.click.wav");
            sClick = new Sound(stream);
            stream.Close();

            stream = sAssembly.GetManifestResourceStream(@"Diwi.Resources.horns.wav");
            sPOI = new Sound(stream);
            stream.Close();

            sTerugIcon = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.terug.ico"));
            sEmptyIcon = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.empty.ico"));
            sUGCIcon = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.ugc.ico"));
            sFotoIcon = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.foto.ico"));
            sTextIcon = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.text.ico"));
            sVolgIcon = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.volgende.ico"));
            sVideoIcon = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.video.ico"));
            sKiesIcon = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.kiesroute.ico"));
            sStruinIcon = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.struin.ico"));
            sInfoIcon = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.routeinfo.ico"));
            sCheckIcon = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.check.ico"));

            sProgBar = new Progress();

            try {
                string myDocumentsPath = Environment.GetFolderPath(Environment.SpecialFolder.Personal);
                sUserProps = myDocumentsPath + "\\DiwiProps.txt";
                sVideoFileName = myDocumentsPath + "\\diwi-concept.3gp";

                StreamReader userProps = new StreamReader(sUserProps);
                if (userProps != null) {
                    sUserName = userProps.ReadLine();
                    sUserPass = userProps.ReadLine();
                    userProps.Close();
                }
            } catch (IOException) {
            }

            sKwxClient = KwxClient.instance;
            sGpsReader = GpsReader.instance;


           // sBacklight.Activate();
        }

        public static void deactivate() {
            sGpsReader.stop();
            sKwxClient.stop();
            MediaDownloader.sQuitting = true;
            MediaUploader.sQuitting = true;
            sTrackLog.Close();
            sEventLog.Close();
            Thread.Sleep(1000);
        }

        public static void playVideo(string fn)
        {
            Process process = new Process();
            process.StartInfo.FileName = fn;
            process.StartInfo.Verb = "Open";
            process.StartInfo.UseShellExecute = true;
            process.Start();
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

        public static string makeVideo()
        {
            CameraCaptureDialog cameraCaptureDialog = new CameraCaptureDialog();
            cameraCaptureDialog.Owner = null;
            cameraCaptureDialog.Title = "Neem een video";
            cameraCaptureDialog.Mode = CameraCaptureMode.VideoWithAudio;
            if (cameraCaptureDialog.ShowDialog() == DialogResult.OK && cameraCaptureDialog.FileName.Length > 0)
            {
                return cameraCaptureDialog.FileName;
                // viewVideoFileInWMP(cameraCaptureDialog.FileName);
            }
            return null;
        }

        public static void poiHit() {
            sPOI.Play();
        }
        public static void SysBeep() {
            sPloink.Play();
        }
        public static void SysClick()
        {
            sClick.Play();
        }
    }
}
