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
    class StartPage : DiwiPageBase {
        TimerCallback timerDelegate;
        System.Threading.Timer mSplashTimer;
        private delegate void splashEnd();

        public StartPage(DiwiPageBase parent)
            : base(parent) {
            AppController.activate();
            title = "";
            mDrawableElements.Clear();
            mIsInitialized = true;
        }

        void doTimeout() {
            mSplashTimer.Dispose();
            (new MainPage(null)).ShowDialog();
            this.Close();
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
            mSplashTimer = new System.Threading.Timer(timerDelegate, autoEvent, 5000, 0);
        }

        protected override void OnResize(EventArgs e) {
            if (mInitializing) return;
            base.doResize(e);
            if (this.ClientRectangle.Width > this.ClientRectangle.Height) {
                setBackGroundImg(@"Diwi.Resources.start_horz.gif", 320, 240, 0, 0);
            } else {
                setBackGroundImg(@"Diwi.Resources.start_vert.gif", 240, 320, 0, 0);
            }
        }
    }    
}
