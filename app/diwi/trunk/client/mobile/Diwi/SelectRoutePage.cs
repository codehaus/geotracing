using System;
using System.Windows.Forms;
using System.Collections.Generic;
using System.Text;
using System.Drawing;

namespace Diwi {
    class SelectRoutePage : DiwiPageBase {
        static RouteInfoPage rInfoPage = null; 

        public SelectRoutePage(DiwiPageBase parent)
            : base(parent) {
            DiwiUIMenu.DiwiMenuCallbackHandler sr = new DiwiUIMenu.DiwiMenuCallbackHandler(doBekijkRoute);
            if (AppController.sRoutes != null) {
                foreach (XMLement xml in AppController.sRoutes.children) {
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
            XMLement route = AppController.sRoutes.getChild(i);
            if (rInfoPage == null) rInfoPage = new RouteInfoPage(this);
            rInfoPage.setContent(route);
            rInfoPage.ShowDialog();
            if (AppController.sActiveRouteID != -1) {
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
