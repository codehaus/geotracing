using System;
using System.Collections.Generic;
using System.Text;
using System.Windows.Forms;

namespace Diwi {
    class UitlegPage : DiwiPageBase {
        public UitlegPage(Form parent)
            : base(parent) {

            mMenu.addItem("Speel video", new DiwiUIMenu.DiwiMenuCallbackHandler(doVideo));
            mMenu.addItem("Over DiWi", new DiwiUIMenu.DiwiMenuCallbackHandler(doOver));
            mMenu.addItem("FAQ", new DiwiUIMenu.DiwiMenuCallbackHandler(doFaq));
            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));

            title = "Uitleg";
        }

        void doVideo() {
        }

        void doOver() {
        }

        void doFaq() {
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
