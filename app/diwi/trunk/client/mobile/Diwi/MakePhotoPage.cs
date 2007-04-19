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
    class MakePhotoPage : DiwiPageBase {
        private DiwiScalingImage mFoto;
        string currentFilename = null;

        public MakePhotoPage(DiwiPageBase parent)
            : base(parent) {

            mMenu.addItem("Voeg toe", new DiwiUIMenu.DiwiMenuCallbackHandler(voegToe));
            mMenu.addItem("Opnieuw", new DiwiUIMenu.DiwiMenuCallbackHandler(doFoto));
            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));

            title = "Maak Foto";

            mFoto = new DiwiScalingImage(this);
            mFoto.size = new Size(192, 144);
            mFoto.x = 10;
            mFoto.y = 120;
            addDrawable(mFoto);
        }


        void voegToe(int i, string s) {
            new MediaUploader(currentFilename, null);
            doTerug(0, null);
        }



        void doFoto(int i, string s) {
            CameraCaptureDialog cameraCaptureDialog = new CameraCaptureDialog();
            cameraCaptureDialog.Owner = this;
            cameraCaptureDialog.Title = "Neem een foto";
            cameraCaptureDialog.Mode = CameraCaptureMode.Still;
            currentFilename = null;
            if (cameraCaptureDialog.ShowDialog() == DialogResult.OK && cameraCaptureDialog.FileName.Length > 0) {
                currentFilename = cameraCaptureDialog.FileName;
                mFoto.bitmap = new Bitmap(currentFilename);
                draw();
            }
        }


        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            mIsInitialized = true;
            doFoto(0, null);
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
