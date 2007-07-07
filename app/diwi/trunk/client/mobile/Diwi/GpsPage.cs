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

        Font gpsFont = new Font("Arial", 12, FontStyle.Bold);


        public GpsPage(DiwiPageBase parent)
            : base(parent) {

            Rectangle r = this.ClientRectangle;

            latLonText = new DiwiUIText(Color.Black, "",gpsFont);
            numSatText = new DiwiUIText(Color.Black, "", gpsFont);
            speedText = new DiwiUIText(Color.Black, "", gpsFont);
            hDopText = new DiwiUIText(Color.Black, "", gpsFont);
            demoText = new DiwiUIText(Color.Black, "", gpsFont);
            fixText = new DiwiUIText(Color.Black, "", gpsFont);

            addDrawable(latLonText);
            addDrawable(numSatText);
            addDrawable(speedText);
            addDrawable(hDopText);
            addDrawable(demoText);
            addDrawable(fixText);

            mMenu.addItem("terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug),AppController.sTerugIcon);

            title = "Gps Status";

            AppController.sGpsReader.callback += new GpsReader.CallbackHandler(gpsMessage);
        }

       
        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            int y = this.ClientRectangle.Height - 20;
            numSatText.x = 8; numSatText.y = y -= 24;
            fixText.x = 8; fixText.y = y -= 24;
            hDopText.x = 8; hDopText.y = y -= 24;
            speedText.x = 8; speedText.y = y -= 24;
            latLonText.x = 8; latLonText.y = y -= 34;
            demoText.x = 8; demoText.y = y -= 30;
            updateDemo();
            mIsInitialized = true;
        }

       protected override void OnResize(EventArgs e) {
           if (mInitializing) return;
           if (base.doResize(e)) {
                int y = this.ClientRectangle.Height - 20;
                numSatText.x = 8; numSatText.y = y -= 24;
                fixText.x = 8; fixText.y = y -= 24;
                hDopText.x = 8; hDopText.y = y -= 24;
                speedText.x = 8; speedText.y = y -= 24;
                latLonText.x = 8; latLonText.y = y -= 34;
                demoText.x = 8; demoText.y = y -= 30;
                draw();
            }
        }


        #region callbacks



        void updatePrecision() {
            Rectangle oldRect = hDopText.rect;
            hDopText.erase(sBackgroundColor);
            if (GpsReader.up)
                hDopText.draw("Precision: " + GpsReader.precision.ToString());
            else
                hDopText.draw("Precision: -" );
            redrawRect(oldRect, hDopText.rect);
        }

        void updatePosition() {
            Rectangle oldRect = latLonText.rect;
            latLonText.erase(sBackgroundColor);
            if (GpsReader.up)
                latLonText.draw(GpsReader.latitude + "; " + GpsReader.longtitude);
            else
                latLonText.draw("");
            redrawRect(oldRect, latLonText.rect);
        }


        void updateNumSats() {
            Rectangle oldRect = numSatText.rect;
            numSatText.erase(sBackgroundColor);
            if (!GpsReader.demo || GpsReader.demoFile)
                numSatText.draw("Sattelites in view: " + GpsReader.numSats.ToString());
            else
                numSatText.draw("Sattelites in view: -");
            redrawRect(oldRect, numSatText.rect);
        }


        void updateFix() {
            Rectangle oldRect = fixText.rect;
            fixText.erase(sBackgroundColor);
            if (!GpsReader.demo || GpsReader.demoFile)
                fixText.draw(GpsReader.fix ? "Sattelite fix: found" : "Sattelite fix: no");
            else
                fixText.draw("Sattelite fix: -");
            updateSpeed();
            updatePosition();
            updatePrecision();
            redrawRect(oldRect, fixText.rect);
        }


        void updateSpeed() {
            Rectangle oldRect = speedText.rect;
            speedText.erase(sBackgroundColor);
            if (GpsReader.up)
                speedText.draw("Speed: " + GpsReader.speed.ToString() + " kph.");
            else
                speedText.draw("Speed: -");
            redrawRect(oldRect, speedText.rect);
        }


        void updateDemo() {
            Rectangle oldRect = demoText.rect;
            demoText.erase(sBackgroundColor);
            if (GpsReader.demo) {
                if (GpsReader.demoFile)
                    demoText.draw("GPS Found (simulation)");
                else
                    demoText.draw("GPS not found");
            } else {
                demoText.draw("GPS Found.");
            }
            redrawRect(oldRect, demoText.rect);
            updateSpeed();
            updateFix();
            updateNumSats();
            updatePosition();
            updatePrecision();
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
