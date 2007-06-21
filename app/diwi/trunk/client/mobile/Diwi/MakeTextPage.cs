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
    class MakeTextPage : DiwiPageBase {
        TextBox mNameBox = new TextBox();

        public MakeTextPage(DiwiPageBase parent, string fileName)
            : base(parent) {

            mMenu.addItem("Voeg toe", new DiwiUIMenu.DiwiMenuCallbackHandler(voegToe));
            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));

            title = "Commentaar bij locatie";

            mNameBox.Multiline = true;
            mNameBox.ForeColor = Color.Black;
            mNameBox.BackColor = Color.Transparent;
            this.Controls.Add(mNameBox);
        }


        void voegToe(int i, string s) {
            string tFile = @"\tempText";
            // create file, save & upload.
            new MediaUploader(tFile, mNameBox.Text, @"text/plain", null);
            doTerug(0, null);
        }


        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            
            mNameBox.Left = 10;
            if (horizontal) {
                mNameBox.Height = 140;
                mNameBox.Width = 260;
                mNameBox.Top = 94;
            } else {
                mNameBox.Height = 220;
                mNameBox.Width = 196;
                mNameBox.Top = 94;
            }
            
            
            mNameBox.Focus();

            mIsInitialized = true;
        }

        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if (base.doResize(e) == true) {
                if (mIsInitialized) {
                    if (horizontal) {
                        mNameBox.Height = 140;
                        mNameBox.Width = 260;
                        mNameBox.Top = 94;
                    } else {
                        mNameBox.Height = 220;
                        mNameBox.Width = 196;
                        mNameBox.Top = 94;
                    }
                    draw();
                    mNameBox.Focus();

                }
            }
        }
    }
}
