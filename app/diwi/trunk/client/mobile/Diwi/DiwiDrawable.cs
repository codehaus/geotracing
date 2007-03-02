using System;
using System.Collections.Generic;
using System.Text;
using System.Drawing;

namespace Diwi {
    interface DiwiDrawable {
        void draw();
        void setGraphics(Graphics g);
        void doMouseClick(int x, int y);
    }
}
