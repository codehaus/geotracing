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
using System.Globalization;
using System.Data;
using System.Diagnostics;



namespace Diwi {
    /// <summary>
    /// central controller for main components in the app.
    /// KwxServer, GpsReader, MapReader, access to resources
    /// </summary>
    class AppController {
        public static int sActiveRouteID = -1;
        public static XMLement sActiveRoute = null;
        public static string sActiveRouteMapPathHor = null;
        public static string sActiveRouteMapPathVer = null;
        public static string sMediaStore = null;
        public static XMLement sRoutes;
        public static StreamWriter sTrackLog;
        public static StreamWriter sEventLog;
        public static KwxClient sKwxClient;
        public static GpsReader sGpsReader;
        public static string sUserName = null;
        public static string sUserPass = null;
        public static string sUserProps = null;
        public static string sComPort = "COM9";// internal GPS on HTC
        public static string sPfPath;
        public static string sAppDir;

        public static PoiSelectPage sPoiSelectPage = null; // hack, sorry.

        public static string sVideoFileName = null;
        public static Progress sProgBar;
        public static Assembly sAssembly = Assembly.GetExecutingAssembly();
        public static CultureInfo sUSFormat = new CultureInfo(0x0409); // constant voor en-US domain. gebruikt in nummerformat parsing 

        public delegate void DownloadCallbackHandler(string path, bool local);

        public static Bitmap backgroundHorBitmap;
        public static Bitmap backgroundVerBitmap;

        public static bool sTapMode = false;
//        public static float sStartLat = 52.07466f; // renswoude
//        public static float sStartLon = 5.541181f;
        public static float sStartLat = 51.95605f; // rhenen
        public static float sStartLon = 5.584859f;


        private static Sound sPloink;
        private static Sound sClick;
        private static Sound sPOI;

        public static bool sSipVisible = false;
        public static bool sQuitting = true;

        public static float sDistanceMoved = 0;

        public static Icon sTerugIcon, sEmptyIcon, sUGCIcon, sFotoIcon, sInfoIcon, sStopRIcon, sVoegToeIcon;
        public static Icon sTextIcon, sVolgIcon, sVideoIcon, sKiesIcon, sStruinIcon, sCheckIcon, sTerugRIcon;

        public static void showStatus(string s) {
            DiwiPageBase.sCurrentPage.printStatus(s);
        }

        public static void setRequestIn() {
            DiwiPageBase.sCurrentPage.printStatus("");
        }

        static void processOption(string opt) {
            string[] kv = opt.Split('=');
            if (kv[0] == "comport") sComPort = kv[1];
            else if (kv[0] == "tapmode" && kv[1] == "true") sTapMode = true;
            else if (kv[0] == "startlatlon") {
                kv = kv[1].Split(',');
                sStartLat = float.Parse(kv[0], AppController.sUSFormat);
                sStartLon = float.Parse(kv[1], AppController.sUSFormat);
            }
        }

        public static string GetLoadingAssembly() {
            System.Reflection.Assembly assembly = System.Reflection.Assembly.GetCallingAssembly();
            System.Reflection.AssemblyName assemblyName = assembly.GetName();
            return assemblyName.CodeBase;
        }


        public static void activate() {
            sPfPath = GetLoadingAssembly();
            sAppDir = sPfPath.Replace("Diwi.exe", "");

            sTrackLog = File.CreateText(sAppDir + "TrackLog.txt");
            sEventLog = File.CreateText(sAppDir + "EventLog.txt");

            try {
                StreamReader dwConfSR = new StreamReader(sAppDir + "dwConfig.txt");
                if (dwConfSR != null) {
                    string option = dwConfSR.ReadLine();
                    while (option != null) {
                        processOption(option);
                        option = dwConfSR.ReadLine();
                    }
                    dwConfSR.Close();
                }
            } catch (IOException e) {
                AppController.sEventLog.WriteLine("Exception: " + e.Message);
            }

            sMediaStore = GetStorageCard() + @"\DiwiMedia\";

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

            sTerugIcon   = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.terug.ico"));
            sEmptyIcon   = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.empty.ico"));
            sUGCIcon     = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.ugc.ico"));
            sFotoIcon    = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.foto.ico"));
            sTextIcon    = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.text.ico"));
            sVolgIcon    = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.volgende.ico"));
            sVideoIcon   = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.video.ico"));
            sKiesIcon    = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.kiesroute.ico"));
            sStruinIcon  = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.struin.ico"));
            sInfoIcon    = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.routeinfo.ico"));
            sCheckIcon   = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.check.ico"));
            sStopRIcon   = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.stop-r.ico"));
            sVoegToeIcon = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.voegtoe.ico"));
            sTerugRIcon  = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.terug-r.ico"));
            sProgBar     = new Progress();

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

            sQuitting = false;
            // sBacklight.Activate();
        }

        public static void deactivate() {
            sQuitting = true;

            sGpsReader.stop();
            sKwxClient.stop();
            sTrackLog.Close();
            sEventLog.Close();
            Thread.Sleep(1000);
        }

        public static void playVideo(string fn) {
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

        public static string makeVideo() {
            CameraCaptureDialog cameraCaptureDialog = new CameraCaptureDialog();
            cameraCaptureDialog.Owner = null;
            cameraCaptureDialog.Title = "Neem een video";
            cameraCaptureDialog.Mode = CameraCaptureMode.VideoWithAudio;
            if (cameraCaptureDialog.ShowDialog() == DialogResult.OK && cameraCaptureDialog.FileName.Length > 0) {
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
        public static void SysClick() {
            sClick.Play();
        }


        public static string GetStorageCard() {
            //initialize the path as an empty string
            string firstCard = "";

            System.IO.DirectoryInfo di = new System.IO.DirectoryInfo("\\");
            System.IO.FileSystemInfo[] fsi = di.GetFileSystemInfos();

            //iterate through them
            for (int x = 0; x < fsi.Length; x++) {
                //check to see if this is a temporary storage card (e.g. SD card)
                if ((fsi[x].Attributes & System.IO.FileAttributes.Temporary) == System.IO.FileAttributes.Temporary) {
                    //if so, return the path
                    firstCard = fsi[x].FullName;
                }
            }

            return firstCard;
        }



        [DllImportAttribute("aygshell")]

        extern public static int Vibrate(uint cvn/*0*/, IntPtr rgvn/*null*/, uint fRepeat, uint dwTimeout);
        public static void doVibrate() {
            Vibrate(0, IntPtr.Zero, 1, 500);
        }


        [DllImport("coredll.dll", SetLastError = true)]
        extern static int SipShowIM(int dwFlag);

        const int SIPF_ON = 1;

        const int SIPF_OFF = 0;

        public static int ShowSIP(Boolean ShowIt) {
            sSipVisible = ShowIt;
            return SipShowIM(ShowIt ? SIPF_ON : SIPF_OFF);

        }
    }
}
