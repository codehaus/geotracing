using System;
using System.Collections;
using System.Text;
using System.IO;
using System.Drawing;
using System.Windows.Forms;
using System.Threading;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using Microsoft.WindowsMobile.Forms;




namespace Diwi {
    class Progress {
        private delegate void timerTick();
        System.Threading.Timer mTimer;
        static Bitmap[] mLogo = new Bitmap[4];
        int index = 0;
        int activeCount = 0;


        public Progress() {

            mLogo[0] = new Bitmap(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.minil1.GIF"));
            mLogo[1] = new Bitmap(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.minil2.GIF"));
            mLogo[2] = new Bitmap(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.minil3.GIF"));
            mLogo[3] = new Bitmap(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.minil4.GIF"));



            mTimer = new System.Threading.Timer(new TimerCallback(doTimeoutT), new AutoResetEvent(false), Timeout.Infinite, 1000);
        }

 

        public void bumpUp() {
            activeCount++;
            if (activeCount == 1) {
                mTimer.Change(0, 100);
            }
        }

        public void bumpDown() {
            activeCount--;
            if (activeCount <= 0) {
                activeCount = 0;
                mTimer.Change(Timeout.Infinite, Timeout.Infinite);
            }
        }


        void doTimeoutT(Object stateInfo) {
            index++;
            if (index > 3) index = 0;
            DiwiPageBase.drawMini(mLogo[index]);
        }
    }
}
