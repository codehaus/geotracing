using System;
using System.Windows.Forms;
using System.Collections.Generic;
using System.Text;
using System.Drawing;

namespace Diwi {
    class RouteInfoPage : DiwiPageBase {
        mediaCallback mapDownloaded;
        Label mTextBox = new Label();
        XMLement route;
        string mVerMap, mHorMap;

        public RouteInfoPage(DiwiPageBase parent)
            : base(parent) {

            mapDownloaded = new mediaCallback(this.openMap);

            mMenu.addItem("Verberg beschrijving", new DiwiUIMenu.DiwiMenuCallbackHandler(hideText), AppController.sTextIcon);
            mMenu.addItem("Loop deze route", new DiwiUIMenu.DiwiMenuCallbackHandler(doLoopRoute), AppController.sStruinIcon);
            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug),AppController.sTerugIcon);

            this.Controls.Add(mTextBox);
            mTextBox.Font = mTextBox.Font = new Font("Tahoma", 11, FontStyle.Bold);
            mTextBox.ForeColor = Color.Black;
            mTextBox.BackColor = Color.FromArgb(198, 255, 0);

            reOrient();

        }

        public void hideText(int i, string s) {
            if (mTextBox.Visible == true) {
                mTextBox.Visible = false;
                setMenuText(0, "Toon beschrijving");
                reOrient();
                draw();
            } else {
                mTextBox.Visible = true;
                setMenuText(0, "Verberg beschrijving");
                reOrient();
                draw();
            }
        }

        void openMap(string fn) {
            int n = fn.IndexOf("horMap");
            if (n >= 0) {
                if (mVerMap == null) {
                    string mapUrl = AppController.sKwxClient.getRouteMap(route.getAttributeValue("id"), false);
                    new MediaDownloader(mapUrl, "verMap.jpg", new AppController.DownloadCallbackHandler(openMapT));
                }
                mHorMap = fn;
                if (horizontal)
                    setBackGroundFromFile(fn, 320, 240, 0, 0);
            } else {
                if (mHorMap == null) {
                    string mapUrl = AppController.sKwxClient.getRouteMap(route.getAttributeValue("id"), true);
                    new MediaDownloader(mapUrl, "horMap.jpg", new AppController.DownloadCallbackHandler(openMapT));
                }
                mVerMap = fn;
                if (!horizontal)
                    setBackGroundFromFile(fn, 240, 320, 0, 0);
            }
        }

        void openMapT(string path) {
            if (this.mIsActive) {
                if (InvokeRequired) {
                    this.Invoke(mapDownloaded, new object[] { path });
                } else {
                    openMap(path);
                }
            }
        }


        public void setContent(XMLement r) {
            string mapUrl = AppController.sKwxClient.getRouteMap(r.getAttributeValue("id"),horizontal);
            if (mapUrl != null) {
                new MediaDownloader(mapUrl, horizontal ? "horMap.jpg" : "verMap.jpg", new AppController.DownloadCallbackHandler(openMapT));
            }
            mVerMap = null;
            mHorMap = null;
            route = r;
            mTextBox.Text = route.getChildValue("description") + "\n\n" +"Afstand is " + int.Parse(route.getChildValue("distance"))/1000 + "km.";
            title = route.getChildValue("name");
            reOrient();
            draw();
        }

        void doLoopRoute(int i, string s) {
            AppController.sActiveRouteID = int.Parse( route.getAttributeValue("id") );
            AppController.sActiveRoute = route;
            AppController.sActiveRouteMapPathHor = null;
            AppController.sActiveRouteMapPathVer = null;
            WalkRoutePage.checkStruinWithUser = true;
            AppController.sDistanceMoved = 0;
            doTerug(0, null);
        }

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            mIsInitialized = true;
        }

        private void reOrient() {
            mTextBox.Left = 6;
            mTextBox.Top = 124;

            if (horizontal) {
                mTextBox.Size = new Size(280, 110);
            } else {
                mTextBox.Size = new Size(200, 184);
            }
        }


        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if (mInitializing) return;
            if (base.doResize(e)) {
                reOrient();
                if (horizontal)
                    setBackGroundFromFile(mHorMap, 240, 320, 0, 0);
                else
                    setBackGroundFromFile(mVerMap, 240, 320, 0, 0);

            }
        }
    }
}
