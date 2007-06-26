using System;
using System.Windows.Forms;
using System.Collections.Generic;
using System.Text;
using System.Drawing;

namespace Diwi {
    class SelectRoutePage : DiwiPageBase {

        public SelectRoutePage(DiwiPageBase parent)
            : base(parent) {
            DiwiUIMenu.DiwiMenuCallbackHandler sr = new DiwiUIMenu.DiwiMenuCallbackHandler(doBekijkRoute);
            if (AppController.sFixedRoutes != null) {
                foreach (XMLement xml in AppController.sFixedRoutes.children) {
                    mMenu.addItem(xml.getChildValue("name"),sr );
                }
                mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));

            } else {
                mMenu.addItem("Geen Routes", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));
            }
            AppController.sActiveRoute = null;
            title = "Kies Route";
        }


        void doBekijkRoute(int i, string s) {
            XMLement route = AppController.sFixedRoutes.getChild(i);
            (new RouteInfoPage(this, route)).ShowDialog();
            if (AppController.sActiveRoute != null) {
                // activate route
                AppController.sKwxClient.activateRoute(AppController.sActiveRouteID, true);
                doTerug(0, null);
            }
        } 

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            mIsInitialized = true;
        }

        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if (mInitializing) return;
            if (base.doResize(e)) {
                draw();
            }
        }
    }
}
