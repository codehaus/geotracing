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
    class UitlegPage : DiwiPageBase {

        private DiwiScalingImage mFoto;

        public UitlegPage(DiwiPageBase parent)
            : base(parent) {

            mMenu.addItem("Maak en Speel video", new DiwiUIMenu.DiwiMenuCallbackHandler(doVideo));
            mMenu.addItem("Maak foto", new DiwiUIMenu.DiwiMenuCallbackHandler(doFoto));
            mMenu.addItem("Over DiWi", new DiwiUIMenu.DiwiMenuCallbackHandler(doOver));
            mMenu.addItem("FAQ", new DiwiUIMenu.DiwiMenuCallbackHandler(doFaq));
            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));

            title = "Uitleg";

            mFoto = new DiwiScalingImage(this);
            mFoto.size = new Size(180, 135);
            mFoto.x = 10;
            mFoto.y = 170;
            addDrawable(mFoto);


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
                mFoto.bitmap = new Bitmap(fileName);
                draw();
            }
        }



        void doOver(int i, string s) {
        }

        void doFaq(int i, string s) {
        }

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            mIsInitialized = true;
        }

        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if (base.doResize(e) == true) {
                if (mIsInitialized) {
                    if (this.ClientRectangle.Width > this.ClientRectangle.Height) {
                        mFoto.x = 10;
                        mFoto.y = 100;
                    } else {
                        mFoto.x = 10;
                        mFoto.y = 170;
                    }
                    draw();
                }
            }
        }
    }
}
