using System;
using System.Collections.Generic;
using System.Text;
using System.Windows.Forms;
using System.Threading;

namespace Diwi {
    class MainPage : DiwiPageBase {

        private DiwiPageBase uitLegPage = null;
        private DiwiPageBase gpsPage = null;

        public MainPage(Form parent)
            : base(parent) {

            mMenu.addItem("Uitleg", new DiwiUIMenu.DiwiMenuCallbackHandler(doUitleg));
            mMenu.addItem("Kies route", new DiwiUIMenu.DiwiMenuCallbackHandler(doKiesRoute));
            mMenu.addItem("Struinen", new DiwiUIMenu.DiwiMenuCallbackHandler(doStruin));
            mMenu.addItem("Route maken", new DiwiUIMenu.DiwiMenuCallbackHandler(doMaakRoute));
            mMenu.addItem("Inloggen", new DiwiUIMenu.DiwiMenuCallbackHandler(doLogin));
            mMenu.addItem("GPS Status", new DiwiUIMenu.DiwiMenuCallbackHandler(doGPS));
            mMenu.addItem("Quit", new DiwiUIMenu.DiwiMenuCallbackHandler(doQuit));

            title = "Hoofdmenu";
        }

        void doUitleg() {
            if (uitLegPage == null)
                uitLegPage = new UitlegPage(this);
            uitLegPage.ShowDialog();
            draw();
        }

        void doGPS() {
            if (gpsPage == null)
                gpsPage = new GpsPage(this);
            gpsPage.ShowDialog();
            draw();
        }

        void doQuit() {
            Program.sGpsReader.stop();
            Program.sKwxClient.stop();
            Thread.Sleep(1000);
            Close();
        }


        void doKiesRoute() {
        }

        void doStruin() {
        }

        void doMaakRoute() {
        }

        void doLogin() {
        }

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            mIsInitialized = true;
        }
        
        protected override void OnResize(EventArgs e) {
            // change location of stuff
            base.OnResize(e);
            if (mIsInitialized) {
                draw();
            }
        }

  
    }
}
