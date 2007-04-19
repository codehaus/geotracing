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
        
        MediaDownloader dnl;

        public delegate void CallbackHandler(string udata);


        public WalkRoutePage(DiwiPageBase parent)
            : base(parent) {
            string path = @"\tmpfile.jpg";
            title = "Route: " + AppController.sActiveRoute.getChildValue("name");
            mMenu.addItem("Voeg Foto toe", new DiwiUIMenu.DiwiMenuCallbackHandler(doFoto));
            mMenu.addItem("Voeg Video toe", new DiwiUIMenu.DiwiMenuCallbackHandler(doVideo));
            mMenu.addItem("Stop Route", new DiwiUIMenu.DiwiMenuCallbackHandler(doStopRoute));
           
            

            string mapUrl = AppController.sKwxClient.getRouteMap(AppController.sActiveRoute.getAttributeValue("id"));
            if (mapUrl != null) {
                dnl = new MediaDownloader(mapUrl, new CallbackHandler(mapReceivedCB));
                /*

                if (MediaHandler.doDownload(mapUrl, path) ) {
                    AppController.sActiveRouteMapPath = path;
                    setBackGround();
                }
                 * 
                 * */
            }

        }

        void mapReceived(string path) {
            AppController.sActiveRouteMapPath = path;
            setBackGround();
            draw();
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
            CameraCaptureDialog cameraCaptureDialog = new CameraCaptureDialog();
            cameraCaptureDialog.Owner = this;
            cameraCaptureDialog.Title = "Neem een foto";
            cameraCaptureDialog.Mode = CameraCaptureMode.Still;
            if (cameraCaptureDialog.ShowDialog() == DialogResult.OK && cameraCaptureDialog.FileName.Length > 0) {
               // mFoto.bitmap = new Bitmap(cameraCaptureDialog.FileName);
                draw();
            }
        }


        void doStopRoute(int i, string s) {
            AppController.sActiveRoute = null;
            doTerug(0, null);
        } 

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            mIsInitialized = true;
            setBackGround();
        }

        void setBackGround() {
            if (this.ClientRectangle.Width > this.ClientRectangle.Height) {
                if (AppController.sActiveRouteMapPath == null)
                    setBackGroundImg(@"Diwi.Resources.back_horz.gif", 320, 240, 0, 0);
                else
                    setBackGroundFromFile(AppController.sActiveRouteMapPath, 320, 240, 0, 0);
            } else {
                if (AppController.sActiveRouteMapPath == null)
                    setBackGroundImg(@"Diwi.Resources.back_vert.gif", 240, 320, 0, 0);
                else
                    setBackGroundFromFile(AppController.sActiveRouteMapPath, 320, 240, 0, 0);
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
