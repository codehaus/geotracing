using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using System.Windows.Forms;
using System.IO;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Diagnostics;
using Microsoft.WindowsMobile.Forms;



namespace Diwi {
    class MainPage : DiwiPageBase {

        private DiwiPageBase uitLegPage = null;
        private DiwiPageBase gpsPage = null;
        private DiwiPageBase liPage = null;
        private DiwiPageBase testPage = null;
        private DiwiPageBase startPage = null;
        private DiwiPageBase selectRoutePage = null;
        private DiwiPageBase walkRoutePage = null;

        public MainPage(DiwiPageBase parent)
            : base(parent) {


            mMenu.addItem("Intro Video", new DiwiUIMenu.DiwiMenuCallbackHandler(doVideo),AppController.sVideoIcon);
            mMenu.addItem("Kies route", new DiwiUIMenu.DiwiMenuCallbackHandler(doKiesRoute), AppController.sKiesIcon);
            mMenu.addItem("Terug naar route", new DiwiUIMenu.DiwiMenuCallbackHandler(walkRoute),
                new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.terug-r.ico"))
            );
            mMenu.addItem("Struinen", new DiwiUIMenu.DiwiMenuCallbackHandler(doStruin), AppController.sStruinIcon);
            mMenu.addItem("GPS Status", new DiwiUIMenu.DiwiMenuCallbackHandler(doGPS),
                new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.gps.ico"))
            );
            mMenu.addItem("Stop", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug),
                new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.stop.ico"))
            );

            title = "Hoofdmenu";
        }

        void doVideo(int i, string s) {
            FileInfo fi = new FileInfo(AppController.sVideoFileName);
            if (fi.Exists) {
                Process process = new Process();
                process.StartInfo.FileName = AppController.sVideoFileName;
                process.StartInfo.Verb = "Open";
                process.StartInfo.UseShellExecute = true;
                process.Start();
            } else {
                MessageBox.Show("Zorg dat \"diwi-concept.3gp\" in de 'My Documents' folder aanwezig is...", "Video not found");
            }

        }

        void walkRoute(int i, string s) {
            mIsActive = false;
            if (walkRoutePage == null)
                walkRoutePage = new WalkRoutePage(this);
            walkRoutePage.ShowDialog();
        }

        void doGPS(int i, string s) {
            mIsActive = false;
            if (gpsPage == null)
                gpsPage = new GpsPage(this);
            gpsPage.ShowDialog();
        }

        void doTest(int i, string s)
        {
            mIsActive = false;
            if (testPage == null)
                testPage = new TestPage(this);
            testPage.ShowDialog();
        }

        protected override void doTerug(int i, string s) {
            AppController.deactivate();
            Close();
        }

        void doKiesRoute(int i, string s) {
            if (AppController.sKwxClient.agentKey == null) {
                doLogin(0,null);
            }

            if (AppController.sKwxClient.agentKey != null) {
                mIsActive = false;

                if (AppController.sRoutes == null) {
                    AppController.sRoutes = AppController.sKwxClient.getRouteList();
                }

                if (selectRoutePage == null)
                    selectRoutePage = new SelectRoutePage(this);
                selectRoutePage.ShowDialog();

                if (AppController.sActiveRouteID != -1) {
                    walkRoute(0,null);
                }
            }
        }

        void doStruin(int i, string s) {
            mIsActive = false;
            if (AppController.sActiveRouteID != -1) {
                AppController.sKwxClient.deActivateRoute();
                AppController.sActiveRouteID = -1;
                AppController.sActiveRoute = null;
            }
            walkRoute(0, null);
        }

        void doLogin(int i, string s) {
            mIsActive = false;
            if (liPage == null)
                liPage = new LoginPage(this);
            liPage.ShowDialog();
        }

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            mIsInitialized = true;
            doLogin(0, null);
            if (AppController.sActiveRouteID != -1) {
                walkRoute(0,null);
            }
        }
        
        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if (mInitializing) return;
            if( base.doResize(e) ) {
                draw();
            }
        }
    }
}
