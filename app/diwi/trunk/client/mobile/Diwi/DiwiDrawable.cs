using System;
using System.Collections.Generic;
using System.Text;
using System.Drawing;

namespace Diwi {
    /// <summary>
    /// Interface for all graphical elements in the app.
    /// can draw and be clicked on, and receive key events (to do)
    /// </summary>
    interface DiwiDrawable
    {
        void draw();
        void doMouseClick(int x, int y);
        bool IsMenu();
    }
}
