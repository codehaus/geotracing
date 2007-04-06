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
    class StartPage : DiwiPageBase {

        public StartPage(DiwiPageBase parent)
            : base(parent) {

            //mMenu.addItem("Maak en Speel video", new DiwiUIMenu.DiwiMenuCallbackHandler(doVideo));
            //mMenu.addItem("Maak foto", new DiwiUIMenu.DiwiMenuCallbackHandler(doFoto));
            //mMenu.addItem("Over DiWi", new DiwiUIMenu.DiwiMenuCallbackHandler(doOver));
            //mMenu.addItem("FAQ", new DiwiUIMenu.DiwiMenuCallbackHandler(doFaq));
            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));

            title = "StartPagina";
            //display start image
            setBackGroundImg(@"Diwi.Resources.start_vert.gif", 240, 320, 0, 0);

        }

        void doOver() {
        }

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            mIsInitialized = true;
        }

        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if (base.doResize(e) == true) {
                if (mIsInitialized) {
                    if (this.ClientRectangle.Width > this.ClientRectangle.Height)
                    {
                        setBackGroundImg(@"Diwi.Resources.start_horz.gif", 320, 240, 0, 0);
                    } else {
                        setBackGroundImg(@"Diwi.Resources.start_vert.gif", 240, 320, 0, 0);
                    }
                    draw();
                }
            }
        }
    }
}
