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
        Label mTextBox = new Label();
        TimerCallback timerDelegate;
        System.Threading.Timer mSplashTimer;
        private delegate void splashEnd();

        public CheckStruinPage(DiwiPageBase parent)
            : base(parent) {
            title = "Route verlaten";
            mMenu.addItem("Ga Struinen", new DiwiUIMenu.DiwiMenuCallbackHandler(doStruin),null);
            mMenu.addItem("Vervolg route", new DiwiUIMenu.DiwiMenuCallbackHandler(doVervolg),null);
            mIsInitialized = true;
            this.Controls.Add(mTextBox);

            mTextBox.Font = new Font("Tahoma", 12, FontStyle.Regular);
            mTextBox.ForeColor = Color.Black;
            mTextBox.BackColor = Color.FromArgb(198,255,0);
            mTextBox.Text = "U bent een flink eind van uw route afgeweken... \n\nWilt u gaan struinen?\nOf wilt u de route vervolgen?";
            reOrient();
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
            if (this.mIsActive) {
                if (InvokeRequired) {
                    this.Invoke(new splashEnd(this.doTimeout), null);
                } else {
                    doTimeout();
                }
            }
        }

        private void reOrient() {
            if (horizontal) {
                mTextBox.Left = 4;
                mTextBox.Top = 100;
                mTextBox.Size = new Size(280, 140);

            } else {
                mTextBox.Left = 4;
                mTextBox.Top = 100;
                mTextBox.Size = new Size(200, 220);
            }
        }


        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            AutoResetEvent autoEvent = new AutoResetEvent(false);
            AppController.poiHit();
            mIsInitialized = true;
            timerDelegate = new TimerCallback(doTimeout);
            mSplashTimer = new System.Threading.Timer(timerDelegate, autoEvent, 20000, 0);
        }

        protected override void OnResize(EventArgs e) {
            if (mInitializing) return;
            base.doResize(e);
        }
    }    
}
