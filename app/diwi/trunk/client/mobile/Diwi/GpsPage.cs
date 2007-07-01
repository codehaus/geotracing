using System;
using System.Collections.Generic;
using System.Text;
using System.Drawing;
using System.Windows.Forms;

namespace Diwi {
    class GpsPage : DiwiPageBase {
        private DiwiUIText latLonText;
        private DiwiUIText fixText;
        private DiwiUIText numSatText;
        private DiwiUIText hDopText;
        private DiwiUIText demoText;
        private DiwiUIText speedText;


        public GpsPage(DiwiPageBase parent)
            : base(parent) {

            Rectangle r = this.ClientRectangle;

            latLonText = new DiwiUIText(Color.Black);
            numSatText = new DiwiUIText(Color.Black);
            speedText = new DiwiUIText(Color.Black);
            hDopText = new DiwiUIText(Color.Black);
            demoText = new DiwiUIText(Color.Red);
            fixText = new DiwiUIText(Color.Black);

            addDrawable(latLonText);
            addDrawable(numSatText);
            addDrawable(speedText);
            addDrawable(hDopText);
            addDrawable(demoText);
            addDrawable(fixText);

            mMenu.addItem("terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));

            title = "Gps Status";

            AppController.sGpsReader.callback += new GpsReader.CallbackHandler(gpsMessage);
        }

       
        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            int y = this.ClientRectangle.Height - 4;
            latLonText.x = 8; latLonText.y = y -= 20;
            numSatText.x = 8; numSatText.y = y -= 20;
            hDopText.x = 8; hDopText.y = y -= 20;
            speedText.x = 8; speedText.y = y -= 20;
            demoText.x = 8; demoText.y = y -= 20;
            fixText.x = 8; fixText.y = y -= 20;
            mIsInitialized = true;
        }

       protected override void OnResize(EventArgs e) {
           if (mInitializing) return;
           if (base.doResize(e)) {
                int y = this.ClientRectangle.Height - 4;
                latLonText.x = 8; latLonText.y = y -= 20;
                numSatText.x = 8; numSatText.y = y -= 20;
                hDopText.x = 8; hDopText.y = y -= 20;
                speedText.x = 8; speedText.y = y -= 20;
                demoText.x = 8; demoText.y = y -= 20;
                fixText.x = 8; fixText.y = y -= 20;
                draw();
            }
        }


        #region callbacks



        void updatePrecision() {
            Rectangle oldRect = hDopText.rect;
            hDopText.erase(sBackgroundColor);
            hDopText.draw("HDOP: " + GpsReader.precision.ToString());
            redrawRect(oldRect, hDopText.rect);
        }

        void updatePosition() {
            Rectangle oldRect = latLonText.rect;
            latLonText.erase(sBackgroundColor);
            latLonText.draw(GpsReader.latitude + "; " + GpsReader.longtitude);
            redrawRect(oldRect, latLonText.rect);
        }


        void updateNumSats() {
            Rectangle oldRect = numSatText.rect;
            numSatText.erase(sBackgroundColor);
            numSatText.draw("sattelites in view: " + GpsReader.numSats.ToString());
            redrawRect(oldRect, numSatText.rect);
        }


        void updateFix() {
            Rectangle oldRect = fixText.rect;
            fixText.erase(sBackgroundColor);
            fixText.draw(GpsReader.fix ? "found fix" : "no fix");
            redrawRect(oldRect, fixText.rect);
        }


        void updateSpeed() {
            Rectangle oldRect = speedText.rect;
            speedText.erase(sBackgroundColor);
            speedText.draw("speed: " + GpsReader.speed.ToString() + " kph.");
            redrawRect(oldRect, speedText.rect);
        }


        void updateDemo() {
            Rectangle oldRect = demoText.rect;
            demoText.erase(sBackgroundColor);
            demoText.draw(GpsReader.demo ? "GPS Demo Mode!" : "");
            redrawRect(oldRect, demoText.rect);
        }


        private delegate void updateGpsMessage(int m);
        void newGgpsMessage(int m) {
            if (!mIsActive) return;
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
