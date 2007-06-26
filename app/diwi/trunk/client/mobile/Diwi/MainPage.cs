using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using System.Windows.Forms;

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


            mMenu.addItem("Uitleg", new DiwiUIMenu.DiwiMenuCallbackHandler(doUitleg));
            mMenu.addItem("Kies route", new DiwiUIMenu.DiwiMenuCallbackHandler(doKiesRoute));
            mMenu.addItem("Terug naar route", new DiwiUIMenu.DiwiMenuCallbackHandler(walkRoute));
            mMenu.addItem("Struinen", new DiwiUIMenu.DiwiMenuCallbackHandler(doStruin));
            mMenu.addItem("GPS Status", new DiwiUIMenu.DiwiMenuCallbackHandler(doGPS));
            mMenu.addItem("Quit", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));

            title = "Hoofdmenu";
        }

        void doUitleg(int i,string s)
        {
            if (uitLegPage == null)
                uitLegPage = new UitlegPage(this);
            uitLegPage.ShowDialog();
        }

        void walkRoute(int i, string s) {
            if (walkRoutePage == null)
                walkRoutePage = new WalkRoutePage(this);
            walkRoutePage.ShowDialog();
        }

        void doGPS(int i, string s) {
            if (gpsPage == null)
                gpsPage = new GpsPage(this);
            gpsPage.ShowDialog();
        }

        void doTest(int i, string s)
        {
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
            if (AppController.sActiveRouteID != -1) {
                AppController.sKwxClient.deActivateRoute();
                AppController.sActiveRouteID = -1;
                AppController.sActiveRoute = null;
            }
            walkRoute(0, null);
        }

        void doLogin(int i, string s) {
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
