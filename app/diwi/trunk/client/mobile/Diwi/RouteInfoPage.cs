using System;
using System.Windows.Forms;
using System.Collections.Generic;
using System.Text;
using System.Drawing;

namespace Diwi {
    class RouteInfoPage : DiwiPageBase {
        DiwiUIStaticText desc;
        XMLement route;

        public RouteInfoPage(DiwiPageBase parent, XMLement r)
            : base(parent) {
            route = r;
            title = route.getChildValue("name");

            mMenu.addItem("Loop deze route", new DiwiUIMenu.DiwiMenuCallbackHandler(doLoopRoute));
            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));


            desc = new DiwiUIStaticText(this);
            desc.size = new Size(200, 200);
            desc.x = 4;
            if (this.ClientRectangle.Width > this.ClientRectangle.Height) {
                desc.y = 100;
            } else {
                desc.y = 100;
            }
            desc.text = route.getChildValue("description") + "\n\r\n\rAfstand is " + route.getChildValue("distance") + "km.";
            addDrawable(desc);

        }


        void doLoopRoute(int i, string s) {
            AppController.sActiveRouteID = int.Parse( route.getAttributeValue("id") );
            AppController.sActiveRoute = route;
            AppController.sActiveRouteMapPathHor = null;
            AppController.sActiveRouteMapPathVer = null;
            doTerug(0, null);
        }

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            mIsInitialized = true;
        }



        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if (mInitializing) return;
            if (base.doResize(e)) {
                if (this.ClientRectangle.Width > this.ClientRectangle.Height) {
                    desc.y = 100;
                } else {
                    desc.y = 100;
                }
                draw();
            }
        }
    }
}
