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
    class TestPage : DiwiPageBase {

        private DiwiImage res;

        public TestPage(DiwiPageBase parent)
            : base(parent) {

            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));

            title = "Test";

            System.Reflection.Assembly asse = System.Reflection.Assembly.GetExecutingAssembly();
            Stream stream = null;
            try{
                stream = asse.GetManifestResourceStream("start.bmp");
                Bitmap resBmp = new Bitmap(stream);
                DiwiImage res = new DiwiImage(offScreenGraphics, this);
                addDrawable(res);
                draw();
            }catch(System.IO.FileNotFoundException e){
                MessageBox.Show(e.Message);
            }
        }

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            mIsInitialized = true;
        }

        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if (base.doResize(e) == true) {
                if (mIsInitialized) {
                    //if (this.ClientRectangle.Width > this.ClientRectangle.Height) {
                    //    res.x = 10;
                    //    res.y = 100;
                    //} else {
                    //    res.x = 10;
                    //    res.y = 170;
                    //}
                    draw();
                }
            }
        }
    }
}
