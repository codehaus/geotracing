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
        TextBox mNameBox = new TextBox();

        public MakePhotoPage(DiwiPageBase parent, string fileName)
            : base(parent) {

            mMenu.addItem("Voeg toe", new DiwiUIMenu.DiwiMenuCallbackHandler(voegToe));
            mMenu.addItem("Opnieuw", new DiwiUIMenu.DiwiMenuCallbackHandler(doFoto));
            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));

            title = "Maak Foto";
            currentFilename = fileName;

            mFoto = new DiwiScalingImage(this);
            mFoto.size = new Size(192, 144);
            mFoto.x = 10;
            if (horizontal) mFoto.y = 36; else mFoto.y = 120;
            addDrawable(mFoto);

            this.Controls.Add(mNameBox);

            mFoto.bitmap = new Bitmap(currentFilename);

        }


        void voegToe(int i, string s) {
            new MediaUploader(currentFilename, mNameBox.Text, "image/jpeg", null);
            doTerug(0, null);
        }



        void doFoto(int i, string s) {
            FileInfo fi = new FileInfo(currentFilename);
            fi.Delete();

            currentFilename = AppController.makeFoto();
            if (currentFilename != null) {
                mFoto.bitmap = new Bitmap(currentFilename);
                draw();
            }
        }


        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);

            if (mParent != null) {
                mParent.Visible = false;
            }

            mNameBox.Width  = 192;
            mNameBox.Height = 24;
            mNameBox.Left = 10;
            if( horizontal )
                mNameBox.Top = 190;
            else
                mNameBox.Top = 268;
            mNameBox.Focus();

            mIsInitialized = true;
        }

        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if (base.doResize(e) == true) {
                if (mIsInitialized) {
                    if (horizontal) {
                        mFoto.y = 36;
                        mNameBox.Top = 190;
                    } else {
                        mFoto.y = 120;
                        mNameBox.Top = 268;
                    }
                    draw();
                }
            }
        }
    }
}
