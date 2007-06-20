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

namespace Diwi
{
    class MakeVideoPage : DiwiPageBase
    {
        string currentFilename = null;
        TextBox mNameBox = new TextBox();

        public MakeVideoPage(DiwiPageBase parent, string fileName)
            : base(parent)
        {

            mMenu.addItem("Voeg toe", new DiwiUIMenu.DiwiMenuCallbackHandler(voegToe));
            mMenu.addItem("Speel af", new DiwiUIMenu.DiwiMenuCallbackHandler(doPlay));
            mMenu.addItem("Opnieuw", new DiwiUIMenu.DiwiMenuCallbackHandler(doVideo));
            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));

            title = "Maak Video";
            currentFilename = fileName;


            this.Controls.Add(mNameBox);


        }

        void doPlay(int i, string s)
        {
            AppController.playVideo(currentFilename);
            draw();
        }


        void voegToe(int i, string s)
        {
            new MediaUploader(currentFilename, mNameBox.Text, @"video/mp4", null);
            doTerug(0, null);
        }



        void doVideo(int i, string s)
        {
            FileInfo fi = new FileInfo(currentFilename);
            fi.Delete();
            currentFilename = AppController.makeVideo();
        }


        protected override void OnLoad(EventArgs e)
        {
            base.OnLoad(e);
            mNameBox.Width = 192;
            mNameBox.Height = 24;
            mNameBox.Left = 10;
            if (horizontal)
                mNameBox.Top = 190;
            else
                mNameBox.Top = 268;
            mNameBox.Focus();

            mIsInitialized = true;
        }

        protected override void OnResize(EventArgs e)
        {
            // change location of stuff
            if (base.doResize(e) == true)
            {
                if (mIsInitialized)
                {
                    if (horizontal)
                    {
                        mNameBox.Top = 190;
                    }
                    else
                    {
                        mNameBox.Top = 268;
                    }
                    draw();
                }
            }
        }
    }
}
