using System;
using System.Windows.Forms;
using System.Collections.Generic;
using System.Text;
using System.Drawing;

namespace Diwi {
    class SelectRoutePage : DiwiPageBase {

        static RouteInfoPage rInfoPage = null;
        int mRouteOffset = 0;
        int mLastRouteDisplay = 0;
        DiwiUIMenu.DiwiMenuCallbackHandler sr;

        public SelectRoutePage(DiwiPageBase parent)
            : base(parent) {
            sr = new DiwiUIMenu.DiwiMenuCallbackHandler(doBekijkRoute);
            doFillMenu();
            AppController.sActiveRoute = null;
            title = "Kies Route";
        }

        void doFillMenu() {
            if (AppController.sRoutes != null && AppController.sRoutes.children.Count > 0) {
                int maxRoutes = horizontal ? 5 : 7;

                mMenu.clear();

                for (int i = mRouteOffset; i < AppController.sRoutes.children.Count; i++) {
                    if (i-mRouteOffset >= maxRoutes) {
                        mMenu.addItem("Meer..", new DiwiUIMenu.DiwiMenuCallbackHandler(doMoreRoutes), AppController.sVolgIcon);
                        mLastRouteDisplay = i - 1;
                        break;
                    }
                    mMenu.addItem(AppController.sRoutes.getChild(i).getChildValue("name"), sr, AppController.sInfoIcon);
                }

//                foreach (XMLement xml in AppController.sRoutes.children) {
 //                   mMenu.addItem(xml.getChildValue("name"), sr, AppController.sInfoIcon);
 //               }
                mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug), AppController.sTerugIcon);

            } else {
                mMenu.addItem("Geen Routes", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug), AppController.sTerugIcon);
            }
        }

        void doMoreRoutes(int i, string s) {
            mRouteOffset = mLastRouteDisplay;
            doFillMenu();
            draw();
        }

        void doBekijkRoute(int i, string s) {
            XMLement route = AppController.sRoutes.getChild(i + mRouteOffset);
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
                doFillMenu();
                draw();
            }
        }
    }
}
