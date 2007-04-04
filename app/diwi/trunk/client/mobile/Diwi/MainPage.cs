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

        public MainPage(DiwiPageBase parent)
            : base(parent) {

            AppController.activate();

            mMenu.addItem("Uitleg", new DiwiUIMenu.DiwiMenuCallbackHandler(doUitleg));
            mMenu.addItem("Kies route", new DiwiUIMenu.DiwiMenuCallbackHandler(doKiesRoute));
            mMenu.addItem("Struinen", new DiwiUIMenu.DiwiMenuCallbackHandler(doStruin));
            mMenu.addItem("Route maken", new DiwiUIMenu.DiwiMenuCallbackHandler(doMaakRoute));
            mMenu.addItem("Inloggen", new DiwiUIMenu.DiwiMenuCallbackHandler(doLogin));
            mMenu.addItem("GPS Status", new DiwiUIMenu.DiwiMenuCallbackHandler(doGPS));
            mMenu.addItem("Test", new DiwiUIMenu.DiwiMenuCallbackHandler(doTest));
            mMenu.addItem("Startpagina", new DiwiUIMenu.DiwiMenuCallbackHandler(doStart));
            mMenu.addItem("Quit", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));

            title = "Hoofdmenu";
        }

        void doUitleg()
        {
            if (uitLegPage == null)
                uitLegPage = new UitlegPage(this);
            uitLegPage.ShowDialog();
        }

        void doGPS() {
            if (gpsPage == null)
                gpsPage = new GpsPage(this);
            gpsPage.ShowDialog();
        }

        void doTest()
        {
            if (testPage == null)
                testPage = new TestPage(this);
            testPage.ShowDialog();
        }
        void doStart()
        {
            if (startPage == null)
                startPage = new StartPage(this);
            startPage.ShowDialog();
        }
       protected override void doTerug() {
            AppController.deactivate();
            Close();
        }

        void doKiesRoute() {
        }

        void doStruin() {
        }

        void doMaakRoute() {
        }

        void doLogin() {
          //  LoginPage lp = new LoginPage(this);
           // lp.ShowDialog();
            //lp.Dispose();

            if (liPage == null)
                liPage = new LoginPage(this);
            liPage.ShowDialog();
        }

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            mIsInitialized = true;
        }
        
        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if( base.doResize(e) ) {
                draw();
            }
        }
    }
}
