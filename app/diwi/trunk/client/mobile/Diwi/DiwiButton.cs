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
        private DiwiUIText mText = new DiwiUIText(sTextColor, "", new Font("Arial", 11, FontStyle.Bold));
        private DiwiPageBase mParentForm;
        private Rectangle mRect;
        private DiwiButtonCallbackHandler mCallback;

        public DiwiUIButton(Graphics g, int x, int y, string t, DiwiButtonCallbackHandler cb,DiwiPageBase form) {
            mParentForm = form;
            mText.text = t;
            mRect = new Rectangle(x, y, 12 + mText.width , 24);
            mCallback = cb;
        }

        public int x {
            get {
                return mRect.X;
            }
            set {
                mRect.X = value;
            }
        }

        public int y {
            get {
                return mRect.Y;
            }
            set {
                mRect.Y = value;
            }
        }

        public void setLabel(string t) {
            mText.text = t;
        }

        public void doMouseClick(int x, int y) {
            Point p = new Point(x, y);
            if (mRect.Contains(p)) {
                AppController.SysClick();
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

        public void draw() {
            mBrush.Color = sButColor;
            DiwiPageBase.offScreenGraphics.FillRectangle(mBrush, mRect.X, mRect.Y, mRect.Width, mRect.Height);

            mText.x = mRect.X + (mRect.Width - mText.width) / 2;
            mText.y = mRect.Y+2;
            mText.color = sTextColor;
            mText.draw();
        }
    }
}
