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
        
        public delegate void CallbackHandler(string udata);
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
        }

        void navPointReceive(XMLement xml, float lat, float lon) {
            XMLement poi = xml.firstChild();
            if (poi != null) {
                // stumbled on an intersting point...
            }

        }

        void navPointMessage(XMLement xml, float lat, float lon) {
            if (InvokeRequired)
                Invoke(poiCB, new object[] { xml,lat,lon });
            else
                navPointReceive(xml, lat, lon);
        }

        void mapReceived(string path) {
            if (AppController.sActiveRouteMapPathHor == null) {
                AppController.sActiveRouteMapPathHor = path;
                if (horizontal) {
                    setBackGround();
                    draw();
                }
                string mapUrl = AppController.sKwxClient.getBoundsMap((float)1.0, false);
//                string mapUrl = AppController.sKwxClient.getRouteMap(AppController.sActiveRoute.getAttributeValue("id"), false);
                if (mapUrl != null) {
                    new MediaDownloader(mapUrl, @"\verMap.jpg", new CallbackHandler(mapReceivedCB));
                }
            } else {
                AppController.sActiveRouteMapPathVer = path;
                if (!horizontal) {
                    setBackGround();
                    draw();
                }
            }
      }

        void mapReceivedCB(string path) {
            if( InvokeRequired ) 
                Invoke(new CallbackHandler(mapReceived),new object[] { path } );
            else
                mapReceived(path);
        }


        private void viewVideoFileInWMP(string fn) {
            Process process = new Process();
            process.StartInfo.FileName = fn;
            process.StartInfo.Verb = "Open";
            process.StartInfo.UseShellExecute = true;
            process.Start();
        }


        void doVideo(int i, string s) {
            CameraCaptureDialog cameraCaptureDialog = new CameraCaptureDialog();
            cameraCaptureDialog.Owner = this;
            cameraCaptureDialog.Title = "Neem een video";
            cameraCaptureDialog.Mode = CameraCaptureMode.VideoWithAudio;
            if (cameraCaptureDialog.ShowDialog() == DialogResult.OK && cameraCaptureDialog.FileName.Length > 0) {
                viewVideoFileInWMP(cameraCaptureDialog.FileName);
            }
        }


        void doFoto(int i, string s) {
            string fileName = AppController.makeFoto();
            if (fileName != null) {
                (new MakePhotoPage(this,fileName)).ShowDialog();
            }
        }


        void doStopRoute(int i, string s) {
            AppController.sActiveRoute = null;
            doTerug(0, null);
        } 

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            mIsInitialized = true;
            string mapUrl = AppController.sKwxClient.getBoundsMap((float)1.0, true);
//            string mapUrl = AppController.sKwxClient.getRouteMap(AppController.sActiveRoute.getAttributeValue("id"), true);
            if (mapUrl != null) {
                new MediaDownloader(mapUrl, @"\horMap.jpg", new CallbackHandler(mapReceivedCB));
            }
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
