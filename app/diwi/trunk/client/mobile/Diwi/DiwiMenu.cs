using System;
using System.Collections;
using System.Text;
using System.Drawing;
using System.Windows.Forms;

namespace Diwi {
    /// <summary>
    /// The menu and title bar on every Diwi page.
    /// </summary>
    class DiwiUIMenu : DiwiDrawable
    {
        public delegate void DiwiMenuCallbackHandler(int item,string name);

        public static Color sBackColor = Color.FromArgb(180, 250, 0);
        public static Color sBarColor = Color.FromArgb(220, 250, 0);
        public static Color sSelColor = Color.FromArgb(130, 100, 30);
        public static Color sTextColor = Color.Black;
        public static Color sSelTextColor = Color.FromArgb(180, 180, 180);
        static Bitmap mMiniLogo = null;
        private SolidBrush mBrush = new SolidBrush(Color.Red);
        private DiwiUIText mText = new DiwiUIText(Color.Black,"",new Font("Arial", 11, FontStyle.Bold));
        private DiwiPageBase mParentForm;
        private int mCurrentMenuIndex = -1;

        ArrayList mItems;
        ArrayList mCallbacks;
        ArrayList mItemRects;
        Rectangle mParentRect;

        public DiwiUIMenu(DiwiPageBase form) {
            mParentForm = form;
            mParentRect = form.ClientRectangle;
            mItems = new ArrayList();
            mCallbacks = new ArrayList();
            mItemRects = new ArrayList();
            if (mMiniLogo == null) {
                mMiniLogo = new Bitmap(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.minis.gif"));
            }
        }

        public void setMenuText(int index, string s) {
           mItems[index] = s;
        }



        public void doMouseClick(int x, int y) {
            Point p = new Point(x, y);
            int i = 0;
            foreach (Rectangle r in mItemRects) {
                if (r != null && r.Contains(p)) {
                    mCurrentMenuIndex = i;
                    AppController.SysClick();
                    mParentForm.draw();
                    DiwiMenuCallbackHandler cb = (DiwiMenuCallbackHandler)mCallbacks[i];
                    if (cb != null) {
                        mCurrentMenuIndex = -1;
                        cb(i,(string)mItems[i]);
                        return;
                    }
                }
                i++;
            }
        }

        public void menuSelect() {
            if (mCurrentMenuIndex != -1) {
                DiwiMenuCallbackHandler cb = (DiwiMenuCallbackHandler)mCallbacks[mCurrentMenuIndex];
                if (cb != null) {
                    int i = mCurrentMenuIndex;
                    mCurrentMenuIndex = -1;
                    AppController.SysClick();
                    cb(i, (string)mItems[i]);
                }
            }
        }

        public void addItem(string text, DiwiMenuCallbackHandler cb) {
            mItems.Add(text);
            mCallbacks.Add(cb);
            mItemRects.Add(null);
        }

        public void resize(Rectangle n) {
            mParentRect = n;
        }

        public void incIndex() {
            mCurrentMenuIndex -= 1;
            if (mCurrentMenuIndex < -1 )
                mCurrentMenuIndex = mItems.Count - 1;
            draw();
        }

        public void decIndex() {
            mCurrentMenuIndex += 1;
            if (mCurrentMenuIndex >= mItems.Count)
                mCurrentMenuIndex = -1;
            draw();
        }

        static Byte min(int n1, int n2) {
            if (n1 > n2) return (Byte)n2;
            return (Byte)n1;
        }

        static void lightenRect(Rectangle r) {
            Color c;
            for (int y = r.Y; y < r.Height; y++) {
                for (int x = r.X; x < r.Width; x++) {
                    c = DiwiPageBase.offScreenBitmap.GetPixel(x, y);
                    //c = onScreenBitmap.GetPixel(x, y);
                    DiwiPageBase.offScreenBitmap.SetPixel(x, y, Color.FromArgb(
                        min((c.R * 150) / 100, 255), 
                        min((c.G * 150) / 100, 255), 
                        min((c.B * 150) / 100, 255)
                        
                        ));
                }
            }
        }

        public void draw() {
            int index = 0;
            Rectangle r;
            int x = mParentRect.X + mParentRect.Width - 30;
            int y = mParentRect.Y;

            // draw yellow bar on the side
            mBrush.Color = sBarColor;
            DiwiPageBase.offScreenGraphics.FillRectangle(mBrush, x, y, 30, mParentRect.Height);

            DiwiPageBase.offScreenGraphics.DrawImage(mMiniLogo, 5, 4);

            mBrush.Color = sSelColor;
            DiwiPageBase.offScreenGraphics.FillRectangle(mBrush, 32, 4, mParentRect.Width - 32 - 4 - 30, 24);
            DiwiPageBase.offScreenGraphics.FillRectangle(mBrush, mParentRect.Width - 27, 4, 24, 24);
            mText.text = mParentForm.title;
            mText.x = 36;
            mText.y = 5;
            mText.color = sSelTextColor;
            mText.draw();

            mBrush.Color = sSelColor;
            x = mParentRect.Width - 27; y = 34;

            foreach (string item in mItems) {
                mBrush.Color = sSelColor;
                DiwiPageBase.offScreenGraphics.FillRectangle(mBrush, x, y, 24, 24);
                mText.text = item;
                mText.x = x - 10 - mText.width;
                mText.y = y+2;
                mItemRects[index] = r = new Rectangle(mText.x - 6, y , mText.width + 10 + 28, 26);
                if (index == mCurrentMenuIndex) {
                    DiwiPageBase.offScreenGraphics.FillRectangle(mBrush, r.X, r.Y, r.Width - 26, 24);
                    mText.color = sSelTextColor;
                    mBrush.Color = sSelTextColor;
                    DiwiPageBase.offScreenGraphics.FillRectangle(mBrush, x + 3, y + 3, 18, 18);
                } else {
                    mText.color = sTextColor;
                }
                //DiwiPageBase.offScreenGraphics.FillRectangle(mBrush, (Rectangle)mItemRects[index]);
                //lightenRect((Rectangle)mItemRects[index]);
                mText.draw();
                //mMenuGraphics.DrawRectangle(new Pen(Color.Black, 1.0F), (Rectangle)mItemRects[index]);
                index++;
                y += 30;

            }
        }
    }
}
