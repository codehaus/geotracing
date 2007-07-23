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
        static PoiSelectPage sPoiSelectPage = null;
        static CheckStruinPage chckStruinPage = null;
        static bool sShowUGC = false;
        private DiwiUIText gpsText;
        private POIHandler poiCB;
        public static bool checkStruinWithUser;
        DiwiUIButton mZoomIn;
        DiwiUIButton mZoomOut;
        List<string> mHitPOI = new List<string>(10);
        List<string> mHitUGC = new List<string>(10);

        public WalkRoutePage(DiwiPageBase parent)
            : base(parent) {

            mMenu.addItem("Voeg Text toe", new DiwiUIMenu.DiwiMenuCallbackHandler(doText),AppController.sTextIcon);
            mMenu.addItem("Voeg Foto toe", new DiwiUIMenu.DiwiMenuCallbackHandler(doFoto),AppController.sFotoIcon);
            mMenu.addItem("Voeg Video toe", new DiwiUIMenu.DiwiMenuCallbackHandler(doVideo),AppController.sVideoIcon);
            mMenu.addItem("Stop Route", new DiwiUIMenu.DiwiMenuCallbackHandler(doStopRoute), AppController.sStopRIcon);
            mMenu.addItem("Toon Volksmond", new DiwiUIMenu.DiwiMenuCallbackHandler(doUGC),AppController.sUGCIcon);
            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug),AppController.sTerugIcon);

            poiCB = new POIHandler(navPointReceive);

            AppController.sKwxClient.poiCallback += new KwxClient.POICallback(navPointMessage);
            MapHandler.sDownloadCallback += new MapHandler.CallbackHandler(mapReceivedCB);

            mZoomIn    = new DiwiUIButton(offScreenGraphics, 146, 170, "Zoom+", buttonZoomIn, this);
            mZoomOut   = new DiwiUIButton(offScreenGraphics, 146, 170, "Zoom-", buttonZoomOut, this);

            addDrawable(mZoomIn);
            addDrawable(mZoomOut);

            gpsText = new DiwiUIText(Color.Black, "Geen GPS", new Font("Arial", 12, FontStyle.Bold));
            AppController.sGpsReader.callback += new GpsReader.CallbackHandler(gpsMessage);

            addDrawable(gpsText);

            reOrient();

            mIsMapPage = true;
        }

        private delegate void updateGpsMessage(int m);
        void gpsMessage(int m) {
            if (InvokeRequired) {
                this.Invoke(new updateGpsMessage(updateGPS), new object[] { m });
            } else {
                updateGPS(m);
            }
        }


        void updateGPS(int m) {
            if (m == (int)GpsReader.sMess.M_DEMO || m == (int)GpsReader.sMess.M_FIX) {
               
                if (GpsReader.up) {
                    gpsText.color = Color.Black;
                    gpsText.draw("GPS Actief");
                } else if (GpsReader.present) {
                    gpsText.color = Color.Red;
                    gpsText.draw("GPS Inactief");
                } else {
                    gpsText.color = Color.Red;
                    gpsText.draw("Geen GPS");
                }
                gpsText.x = (this.ClientRectangle.Width - gpsText.width) / 2;
                draw();
            } else if (m == (int)GpsReader.sMess.M_POS) {
                setPosition(true);
            }
        }



        public void buttonZoomOut() {
            MapHandler.mapRadius *= 1.5F;
        }

        public void buttonZoomIn() {
            MapHandler.mapRadius *= 0.75F;
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

            if (mIsActive) {
                
                XMLement poi = xml.getChildByName("poi-hit");
                XMLement ugc = xml.getChildByName("ugc-hit");

                if (poi != null) {
                    List<string> pois = new List<string>();
                    for (int i = 0; ; i++) {
                        XMLement t = xml.getChild(i);
                        if (t == null) break;
                        if (t.tag == "poi-hit") {
                            string poiId = t.getAttributeValue("id");
                            if (!mHitPOI.Contains(poiId)) {
                                mHitPOI.Add(poiId);
                                pois.Add(poiId);
                            }
                        }
                    }
                    if (pois.Count > 0) {
                        doPoi(pois);
                    }
                }

                if (ugc != null) {
                    List<XMLement> ugcs = new List<XMLement>();
                    for (int i = 0; ; i++) {
                        XMLement t = xml.getChild(i);
                        if (t == null) break;
                        if (t.tag == "ugc-hit") {
                            string ugcId = t.getAttributeValue("id");
                            if (!mHitUGC.Contains(ugcId)) {
                                mHitUGC.Add(ugcId);
                                ugcs.Add(t);
                            }
                        }
                    }
                    if (ugcs.Count > 0) {
                        doUgcs(ugcs);
                    }
                }


                XMLement msg = xml.getChildByName("msg");
                if (msg != null) {
                    string text = msg.nodeText;
                    if (text == "roam" && checkStruinWithUser == true) {
                        checkStruinWithUser = false;
                        doCheckStruin(0, "");
                    }
                } 
            }
        }


        void doCheckStruin(int i, string s) {
            mIsActive = false;
            if (chckStruinPage == null) {
                chckStruinPage = new CheckStruinPage(this);
            }
            chckStruinPage.ShowDialog();
            mIsActive = true;
            mBlendTimer.Change(0, 3000);
            if (AppController.sActiveRoute == null)
                title = "Struinen...";
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
            if ((!horizontal && AppController.sActiveRouteMapPathVer != null) || (horizontal && AppController.sActiveRouteMapPathHor != null)) {
                setBackGround();
                MapHandler.copyBounds();
                setPosition(false);
                draw();
            }
        }

        void mapReceivedCB() {
            if (InvokeRequired)
                Invoke(new CallbackHandler(mapReceived), null);
            else
                mapReceived();
        }

        void doVideo(int i, string s) {
            mIsActive = false;
            AppController.sGpsReader.storeLocation();
            string fileName = AppController.makeVideo();
            if (fileName != null) {
                (new MakeVideoPage(this, fileName)).ShowDialog();
            }
            mIsActive = true;
            mBlendTimer.Change(0, 3000);
        }

        void doUGC(int i, string s) {
            AppController.sKwxClient.toggleUGC();
            MapHandler.downLoadMaps();
            if (AppController.sKwxClient.ugcState == true) {
                setMenuText(4, "Verberg Volksmond");
            } else {
                setMenuText(4, "Toon Volksmond");
            }
        }

        void doFoto(int i, string s) {
            mIsActive = false;
            AppController.sGpsReader.storeLocation();
            string fileName = AppController.makeFoto();
            if (fileName != null) {
                (new MakePhotoPage(this, fileName)).ShowDialog();
            }
            mIsActive = true;
            mBlendTimer.Change(0, 3000);
        }

        void doText(int i, string s) {
            mIsActive = false;
            AppController.sGpsReader.storeLocation();
            (new MakeTextPage(this, null)).ShowDialog();
            mIsActive = true;
            mBlendTimer.Change(0, 3000);
        }


        void doPoi(List<string> pois) {
            AppController.doVibrate();
            AppController.poiHit();
            AppController.sEventLog.WriteLine("hit {0} pois:", pois.Count);
            if (sPoiSelectPage == null)
                sPoiSelectPage = new PoiSelectPage(this);
            if (sPoiSelectPage.setContent(pois)) {
                mIsActive = false;
                sPoiSelectPage.ShowDialog();
                mBlendTimer.Change(0, 3000);
            }
        }

        void doUgcs(List<XMLement> ugcs) {
            AppController.poiHit();
            AppController.sEventLog.WriteLine("hit {0} ugc:", ugcs.Count);
            if (sPoiSelectPage == null)
                sPoiSelectPage = new PoiSelectPage(this);

            XMLement raw = XMLement.createFromRawXml("<poi><name>VolksMond</name><description>Door andere gebruikers gemaakte media</description></poi>");
            XMLement mmd = new XMLement("media");

            foreach (XMLement x in ugcs) {
                mmd.addChild(x);
            }

            raw.addChild(mmd);

            if (PoiSelectPage.sPoiPage.setContent(raw)) {
                mIsActive = false;
                PoiSelectPage.sPoiPage.ShowDialog();
                mBlendTimer.Change(0, 3000);
            }
        }


        void doStopRoute(int i, string s) {
            if (AppController.sActiveRouteID != -1)
                AppController.sKwxClient.deActivateRoute();
            AppController.sActiveRoute = null;
            AppController.sActiveRouteID = -1;
            doTerug(0, null);
        }

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            if (AppController.sActiveRoute == null)
                title = "Struinen...";
            else
                title = AppController.sActiveRoute.getChildValue("name");
            mIsInitialized = true;
            MapHandler.active = true;
            updateGPS((int)GpsReader.sMess.M_DEMO);
            mBlendTimer.Change(0, 3000);
            checkStruinWithUser = (AppController.sActiveRoute != null);
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

        private void reOrient() {
            int h = this.ClientRectangle.Height;
            int w = this.ClientRectangle.Width;

            mZoomIn.x = w - mZoomOut.width;
            mZoomIn.y = h - 24;

            mZoomOut.x = 0;
            mZoomOut.y = h - 24;

            gpsText.x = (w - gpsText.width) / 2;
            gpsText.y = this.ClientRectangle.Height - 22;
        }



        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if (mInitializing) return;
            if (base.doResize(e)) {
                reOrient();
                setBackGround();
                draw();
            }
        }
    }
}
