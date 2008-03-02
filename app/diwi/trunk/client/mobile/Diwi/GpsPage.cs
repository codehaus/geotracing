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
        private DiwiUIText distText;
        private DiwiUIText speedText;

        static Color sBackColor = Color.FromArgb(198, 255, 0);

        Font gpsFont = new Font("Arial", 12, FontStyle.Bold);


        public GpsPage(DiwiPageBase parent)
            : base(parent) {

            Rectangle r = this.ClientRectangle;

            latLonText = new DiwiUIText(Color.Black, "",gpsFont);
            numSatText = new DiwiUIText(Color.Black, "", gpsFont);
            speedText = new DiwiUIText(Color.Black, "", gpsFont);
            hDopText = new DiwiUIText(Color.Black, "", gpsFont);
            demoText = new DiwiUIText(Color.Black, "", gpsFont);
            distText = new DiwiUIText(Color.Black, "", gpsFont);
            fixText = new DiwiUIText(Color.Black, "", gpsFont);

            addDrawable(latLonText);
            addDrawable(numSatText);
            addDrawable(speedText);
            addDrawable(hDopText);
            addDrawable(demoText);
            addDrawable(fixText);
            addDrawable(distText);

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
            distText.x = 8; distText.y = y -= 24;
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
                distText.x = 8; distText.y = y -= 24;
                latLonText.x = 8; latLonText.y = y -= 34;
                demoText.x = 8; demoText.y = y -= 30;
                draw();
            }
        }


        #region callbacks



        void updatePrecision() {
            Rectangle oldRect = hDopText.rect;
            hDopText.erase(sBackColor);
            if (GpsReader.up)
                hDopText.draw("precisie: " + GpsReader.precision.ToString());
            else
                hDopText.draw("precisie: -");
            redrawRect(oldRect, hDopText.rect);
        }

        void updatePosition() {
            Rectangle oldRect = latLonText.rect;
            latLonText.erase(sBackColor);
            if (GpsReader.up)
                latLonText.draw(GpsReader.latitude + "; " + GpsReader.longtitude);
            else
                latLonText.draw("");
            redrawRect(oldRect, latLonText.rect);
        }


        void updateNumSats() {
            Rectangle oldRect = numSatText.rect;
            numSatText.erase(sBackColor);
            if (!GpsReader.demo)
                numSatText.draw("Satellieten in zicht: " + GpsReader.numSats.ToString());
            else
                numSatText.draw("Satellieten in zicht: -");
            redrawRect(oldRect, numSatText.rect);
        }


        void updateFix() {
            Rectangle oldRect = fixText.rect;
            fixText.erase(sBackColor);
            if (!GpsReader.demo)
                fixText.draw(GpsReader.fix ? "Sat. fix: Ok" : "Sat. fix: geen");
            else
                fixText.draw("Sat. fix: -");
            updateSpeed();
            updatePosition();
            updatePrecision();
            redrawRect(oldRect, fixText.rect);
        }


        void updateSpeed() {
            Rectangle oldRect = speedText.rect;
            speedText.erase(sBackColor);
            if (GpsReader.up) {
                float speed = (float)(Math.Floor(100.0 * GpsReader.speed) /  100.0);
                speedText.draw("Snelheid: " + speed.ToString() + " km/u.");
            } else
                speedText.draw("Snelheid: -");
            redrawRect(oldRect, speedText.rect);
            updateDist();
        }


        void updateDist() {
            Rectangle oldRect = distText.rect;
            distText.erase(sBackColor);
            distText.draw("Afstand tot nu: " + Math.Floor(AppController.sDistanceMoved).ToString() + "m.");
            redrawRect(oldRect, distText.rect);
        }


        void updateDemo() {
            Rectangle oldRect = demoText.rect;
            demoText.erase(sBackColor);
            if (GpsReader.demo) {
                demoText.draw("GPS gevonden (simulatie)");
            } else if (GpsReader.present) {
                demoText.draw("GPS gevonden.");
            } else {
                demoText.draw("GPS: wachten...");
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
            if (this.mIsActive) {
                if (InvokeRequired) {
                    this.Invoke(new updateGpsMessage(this.newGgpsMessage), new object[] { m });
                } else {
                    newGgpsMessage(m);
                }
            }
        }

        #endregion


    }
}
