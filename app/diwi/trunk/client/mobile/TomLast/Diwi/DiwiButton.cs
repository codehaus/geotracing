using System;
using System.Collections;
using System.Text;
using System.Drawing;
using System.Windows.Forms;

namespace Diwi {
    /// <summary>
    /// Implements a simple clickable button in the Diwi style.
    /// </summary>
    class DiwiUIButton : DiwiDrawable
    {
        public delegate void DiwiButtonCallbackHandler();

        public static Color sBackColor = Color.FromArgb(180, 250, 0);
        public static Color sButColor = Color.FromArgb(130, 100, 30);
        public static Color sTextColor = Color.FromArgb(180, 180, 180);
        private SolidBrush mBrush = new SolidBrush(Color.Red);
        private DiwiUIText mText = new DiwiUIText(null, sTextColor, "", new Font("Tahoma", 11, FontStyle.Bold));
        private DiwiPageBase mParentForm;
        private Graphics mGraphics;
        private Rectangle mRect;
        private DiwiButtonCallbackHandler mCallback;

        public DiwiUIButton(Graphics g, int x, int y, string t, DiwiButtonCallbackHandler cb,DiwiPageBase form) {
            mParentForm = form;
            mGraphics = g;
            mText.setGraphics(g);
            mText.text = t;
            mRect = new Rectangle(x, y, 12 + mText.width , 24);
            mCallback = cb;
        }

        public void doMouseClick(int x, int y) {
            Point p = new Point(x, y);
            if (mRect.Contains(p)) {
                if (mCallback != null) {
                    mCallback();
                    return;
                }
            }
        }

        public void select() {
            if (mCallback != null) {
                mCallback();
            }
        }


        public void setGraphics(Graphics g) {
            mGraphics = g;
            mText.setGraphics(g);
        }

        public void draw() {
            mBrush.Color = sButColor;
            mGraphics.FillRectangle(mBrush, mRect.X, mRect.Y, mRect.Width, mRect.Height);

            mText.x = mRect.X+5;
            mText.y = mRect.Y+2;
            mText.color = sTextColor;
            mText.draw();
        }
    }
}
