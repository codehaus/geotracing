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

        public TestPage(DiwiPageBase parent)
            : base(parent) {

            mMenu.addItem("GetRouteList", new DiwiUIMenu.DiwiMenuCallbackHandler(doGRL));
            mMenu.addItem("GetRoute", new DiwiUIMenu.DiwiMenuCallbackHandler(doGR));
            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));

            title = "Test";

            //getRoutes();
            //Stream mov = System.Reflection.Assembly.GetExecutingAssembly().GetManifestResourceStream("");
            //File f = File.Create("sdc");
            //viewVideoFileInWMP(f);
            
        }

        private void doGRL(int i, string s) {
            DateTime d = DateTime.UtcNow;
            int n = d.Millisecond;
            
        }

        private void doGR(int i, string s) {
            XMLement getRoutesReq = AppController.sKwxClient.getRoute("109");
            string s1 = getRoutesReq.toString();
        }

        private void viewVideoFileInWMP(string fn)
        {
            Process process = new Process();
            process.StartInfo.FileName = fn;
            process.StartInfo.Verb = "Open";
            process.StartInfo.UseShellExecute = true;
            process.Start();
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
