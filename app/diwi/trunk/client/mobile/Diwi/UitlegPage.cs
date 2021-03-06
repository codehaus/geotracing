using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.IO;
using System.Diagnostics;
using System.Windows.Forms;
using Microsoft.WindowsMobile.Forms;


namespace Diwi {
    class UitlegPage : DiwiPageBase {

        public UitlegPage(DiwiPageBase parent)
            : base(parent) {

            mMenu.addItem("Over DiWi", new DiwiUIMenu.DiwiMenuCallbackHandler(doOver),null);
            mMenu.addItem("FAQ", new DiwiUIMenu.DiwiMenuCallbackHandler(doFaq),null);
            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug),AppController.sTerugIcon);

            title = "Uitleg";

        }

        void doOver(int i, string s) {
        }

        void doFaq(int i, string s) {
        }

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            mIsInitialized = true;
        }

        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if (base.doResize(e) == true) {
                if (mIsInitialized) {
                    if (this.ClientRectangle.Width > this.ClientRectangle.Height) {
                    } else {
                    }
                    draw();
                }
            }
        }
    }
}
