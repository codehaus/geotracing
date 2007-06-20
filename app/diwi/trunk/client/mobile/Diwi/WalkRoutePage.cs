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
    class WalkRoutePage : DiwiPageBase {
        
        public delegate void CallbackHandler();
        public delegate void POIHandler(XMLement data, float lat, float lon);

        private POIHandler poiCB;

        public WalkRoutePage(DiwiPageBase parent)
            : base(parent) {

            title = "Route: " + AppController.sActiveRoute.getChildValue("name");
            mMenu.addItem("Voeg Foto toe", new DiwiUIMenu.DiwiMenuCallbackHandler(doFoto));
            mMenu.addItem("Voeg Video toe", new DiwiUIMenu.DiwiMenuCallbackHandler(doVideo));
            mMenu.addItem("Stop Route", new DiwiUIMenu.DiwiMenuCallbackHandler(doStopRoute));

            poiCB = new POIHandler(navPointReceive);

            AppController.sKwxClient.poiCallback += new KwxClient.POICallback(navPointMessage);
            MapHandler.sDownloadCallback += new MapHandler.CallbackHandler(mapReceivedCB);
        }

        void drawPos(int x, int y) {
            Rectangle oldRect = mouseText.rect;
            mouseText.erase(sBackgroundColor);

            mouseText.text = "pos: " + x.ToString() + ", " + y.ToString();
            mouseText.x = 4;
            mouseText.y = mCurrentRect.Height - 18;
            mouseText.draw();
            redrawRect(oldRect, mouseText.rect);
        }


        void navPointReceive(XMLement xml, float lat, float lon) {
                XMLement poi = xml.firstChild();
                int x = MapHandler.currentXpixel(horizontal);
                int y = MapHandler.currentYpixel(horizontal);
                setPosition(x, y);
                if (poi != null) {
                    // stumbled on an intersting point...
                }
        }

        void navPointMessage(XMLement xml, float lat, float lon) {
            if (xml != null) {
                if (InvokeRequired)
                    Invoke(poiCB, new object[] { xml, lat, lon });
                else
                    navPointReceive(xml, lat, lon);
            }
        }

        void mapReceived() {
            if ( (!horizontal && AppController.sActiveRouteMapPathVer != null) || (horizontal && AppController.sActiveRouteMapPathHor != null)) {
                setBackGround();
                draw();
            } 
        }

        void mapReceivedCB() {
            if( InvokeRequired ) 
                Invoke(new CallbackHandler(mapReceived),null );
            else
                mapReceived();
        }

        void doVideo(int i, string s)
        {
            string fileName = AppController.makeVideo();
            if (fileName != null)
            {
                (new MakeVideoPage(this, fileName)).ShowDialog();
            }
        }

        void doFoto(int i, string s)
        {
            string fileName = AppController.makeFoto();
            if (fileName != null)
            {
                (new MakePhotoPage(this, fileName)).ShowDialog();
            }
        }


        void doStopRoute(int i, string s) {
            AppController.sActiveRoute = null;
            doTerug(0, null);
        } 

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            mIsInitialized = true;
            MapHandler.active = true;
            setBackGround();
        }

        void setBackGround() {
            if (horizontal) {
                if (AppController.sActiveRouteMapPathHor == null)
                    setBackGroundImg(@"Diwi.Resources.back_horz.gif", 320, 240, 0, 0);
                else
                    setBackGroundFromFile(AppController.sActiveRouteMapPathHor, 320, 240, 0, 0);
            } else {
                if (AppController.sActiveRouteMapPathVer == null)
                    setBackGroundImg(@"Diwi.Resources.back_vert.gif", 240, 320, 0, 0);
                else
                    setBackGroundFromFile(AppController.sActiveRouteMapPathVer, 320, 240, 0, 0);
            }
        }

        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if (mInitializing) return;
            if (base.doResize(e)) {
                setBackGround();
                draw();
            }
        }
    }
}
