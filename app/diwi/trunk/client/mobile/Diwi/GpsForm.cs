using System;
using System.Collections.Generic;
using System.Text;
using System.Drawing;
using System.Windows.Forms;

namespace Diwi {
    class GpsForm : DiwiPageBase {
        private DiwiUIText latLonText;
        private DiwiUIText fixText;
        private DiwiUIText kwxMessageText;
        private DiwiUIText numSatText;
        private DiwiUIText hDopText;
        private DiwiUIText demoText;
        private DiwiUIText speedText;

        private DiwiUIText mouseText;

        public GpsForm(Form parent)
            : base(parent) {

            Rectangle r = this.ClientRectangle;

            kwxMessageText = new DiwiUIText(offScreenGraphics,Color.Black);
            latLonText = new DiwiUIText(offScreenGraphics,Color.Black);
            numSatText = new DiwiUIText(offScreenGraphics,Color.Black);
            speedText = new DiwiUIText(offScreenGraphics,Color.Black);
            hDopText = new DiwiUIText(offScreenGraphics,Color.Black);
            demoText = new DiwiUIText(offScreenGraphics,Color.Red);
            fixText = new DiwiUIText(offScreenGraphics,Color.Black);
            mouseText = new DiwiUIText(offScreenGraphics);

            addDrawable(kwxMessageText);
            addDrawable(latLonText);
            addDrawable(numSatText);
            addDrawable(speedText);
            addDrawable(hDopText);
            addDrawable(demoText);
            addDrawable(fixText);

            mMenu.addItem("terug", null);
            mMenu.addItem("over diwi", null);
            mMenu.addItem("vragen", null);
            mMenu.addItem("speel video", null);

            Program.sGpsReader.callback += new GpsReader.CallbackHandler(gpsMessage);
        }

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            Rectangle r = this.ClientRectangle;
            kwxMessageText.x = 8; kwxMessageText.y = 12;
            latLonText.x = 8; latLonText.y = 32;
            numSatText.x = 8; numSatText.y = 52;
            hDopText.x = 8; hDopText.y = 72;
            speedText.x = 8; speedText.y = 92;
            demoText.x = 8; demoText.y = 142;
            fixText.x = 8; fixText.y = 112;
            if (Program.sKwxClient.agentKey != null) {
                newKwxMessage("Kwx login succes: " + Program.sKwxClient.agentKey);
            }
            mIsInitialized = true;
        }

        protected override void OnMouseMove(MouseEventArgs e) {
            Rectangle oldRect = mouseText.rect;
            mouseText.erase(mBackgroundColor);

            mouseText.text = "mouse: " + e.X.ToString() + ", " + e.Y.ToString();
            mouseText.x = e.X;
            mouseText.y = e.Y;
            mouseText.Draw();
            redrawRect(oldRect, mouseText.rect);
        }

        protected override void OnResize(EventArgs e) {
            // change location of stuff
            base.OnResize(e);
        }


        #region callbacks
 
        private delegate void updateKwxMessageDelegate(string m);
        void updateKwxMessage(string m) {
            Rectangle oldRect = kwxMessageText.rect;
            kwxMessageText.erase(mBackgroundColor);
            kwxMessageText.Draw(m);
            redrawRect(oldRect, kwxMessageText.rect);
        }
        void newKwxMessage(string m) {
            if (InvokeRequired)
                this.Invoke(new updateKwxMessageDelegate(this.updateKwxMessage), new object[] { m });
            else
                updateKwxMessage(m);
        }


        void updatePrecision() {
            Rectangle oldRect = hDopText.rect;
            hDopText.erase(mBackgroundColor);
            hDopText.Draw("HDOP: " + GpsReader.precision.ToString());
            redrawRect(oldRect, hDopText.rect);
        }

        void updatePosition() {
            Rectangle oldRect = latLonText.rect;
            latLonText.erase(mBackgroundColor);
            latLonText.Draw("lat: " + GpsReader.latitude + "   lon: " + GpsReader.longtitude);
            redrawRect(oldRect, latLonText.rect);
        }


        void updateNumSats() {
            Rectangle oldRect = numSatText.rect;
            numSatText.erase(mBackgroundColor);
            numSatText.Draw("sattelites in view: " + GpsReader.numSats.ToString());
            redrawRect(oldRect, numSatText.rect);
        }


        void updateFix() {
            Rectangle oldRect = fixText.rect;
            fixText.erase(mBackgroundColor);
            fixText.Draw(GpsReader.fix ? "found fix" : "no fix");
            redrawRect(oldRect, fixText.rect);
        }


        void updateSpeed() {
            Rectangle oldRect = speedText.rect;
            speedText.erase(mBackgroundColor);
            speedText.Draw("speed: " + GpsReader.speed.ToString() + " kph.");
            redrawRect(oldRect, speedText.rect);
        }


        void updateDemo() {
            Rectangle oldRect = demoText.rect;
            demoText.erase(mBackgroundColor);
            demoText.Draw(GpsReader.demo ? "GPS Demo Mode!" : "");
            redrawRect(oldRect, demoText.rect);
        }


        private delegate void updateGpsMessage(int m);
        void newGgpsMessage(int m) {
            switch (m) {
                case (int)GpsReader.sMess.M_DEMO:
                    updateDemo();
                    break;
                case (int)GpsReader.sMess.M_SPEED:
                    updateSpeed();
                    break;
                case (int)GpsReader.sMess.M_FIX:
                    updateFix();
                    break;
                case (int)GpsReader.sMess.M_NUMSAT:
                    updateNumSats();
                    break;
                case (int)GpsReader.sMess.M_POS:
                    updatePosition();
                    break;
                case (int)GpsReader.sMess.M_PREC:
                    updatePrecision();
                    break;
            }
        }


        void gpsMessage(int m) {
            if (InvokeRequired) {
                this.Invoke(new updateGpsMessage(this.newGgpsMessage), new object[] { m });
            } else {
                newGgpsMessage(m);
            }
        }


        #endregion


    }
}
