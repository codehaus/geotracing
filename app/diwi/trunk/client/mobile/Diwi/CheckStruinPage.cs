using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.IO;
using System.Diagnostics;
using System.Threading;
using System.Windows.Forms;
using Microsoft.WindowsMobile.Forms;

namespace Diwi {
    class CheckStruinPage : DiwiPageBase {
        TimerCallback timerDelegate;
        System.Threading.Timer mSplashTimer;
        private delegate void splashEnd();

        public CheckStruinPage(DiwiPageBase parent)
            : base(parent) {
            title = "Route verlaten";
            mMenu.addItem("Ga Struinen", new DiwiUIMenu.DiwiMenuCallbackHandler(doStruin));
            mMenu.addItem("Vervolg route", new DiwiUIMenu.DiwiMenuCallbackHandler(doVervolg));
            mIsInitialized = true;
        }


        void doVervolg(int i, string s) {
            mSplashTimer.Dispose();
            mIsActive = false;
            doTerug(0, "");
        }

        void doStruin(int i, string s) {
            if (AppController.sActiveRouteID != -1) {
                AppController.sKwxClient.deActivateRoute();
                AppController.sActiveRouteID = -1;
                AppController.sActiveRoute = null;
            }
            mSplashTimer.Dispose();
            mIsActive = false;
            doTerug(0, "");
        }

        void doTimeout() {
            mSplashTimer.Dispose();
            mIsActive = false;
            doTerug(0,"");
        }

        void doTimeout(Object stateInfo) {
            if (InvokeRequired) {
                this.Invoke(new splashEnd(this.doTimeout), null);
            } else {
                doTimeout();
            }
        }

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            AutoResetEvent autoEvent = new AutoResetEvent(false);

            mIsInitialized = true;
            timerDelegate = new TimerCallback(doTimeout);
            mSplashTimer = new System.Threading.Timer(timerDelegate, autoEvent, 10000, 0);
        }

        protected override void OnResize(EventArgs e) {
            if (mInitializing) return;
            base.doResize(e);
        }
    }    
}
