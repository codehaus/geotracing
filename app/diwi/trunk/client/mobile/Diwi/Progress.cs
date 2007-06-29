using System;
using System.Collections;
using System.Text;
using System.IO;
using System.Drawing;
using System.Windows.Forms;

namespace Diwi {
    class Progress {
        static Bitmap[] mLogo = new Bitmap[4];
        int index = 0;

        public Progress() {

            mLogo[0] = new Bitmap(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.minil1.GIF"));
            mLogo[1] = new Bitmap(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.minil2.GIF"));
            mLogo[2] = new Bitmap(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.minil3.GIF"));
            mLogo[3] = new Bitmap(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.minil4.GIF"));
        }

        public void bump() {
            index++;
            if(index > 3 ) index = 0;
            DiwiPageBase.drawMini(mLogo[index]);
        }

        public void reset() {
            index = 3;
            bump();
        }

    }
}
