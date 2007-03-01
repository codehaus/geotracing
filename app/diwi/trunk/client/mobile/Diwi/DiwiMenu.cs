using System;
using System.Collections;
using System.Text;
using System.Drawing;
using System.Windows.Forms;

namespace Diwi {
    class DiwiUIMenu : DiwiDrawable {
        public static Color sBackColor = Color.Green;
        public static Color sBarColor = Color.Yellow;
        public static Color sSelColor = Color.Brown;
        public static Color sTextColor = Color.Black;
        public static Color sSelTextColor = Color.White;
        private SolidBrush mBrush = new SolidBrush(Color.Red);
        private DiwiUIText mText = new DiwiUIText(null,Color.Black,"",new Font("Tahoma", 12, FontStyle.Bold));
        private Form mParentForm;
        public delegate void DiwiMenuCallbackHandler();
        private Graphics mMenuGraphics;
        private int mCurrentMenuIndex = 1;

        ArrayList mItems;
        ArrayList mCallbacks;
        ArrayList mItemRects;
        Rectangle mParentRect;

        public DiwiUIMenu(Graphics g, DiwiPageBase form) {
            mParentRect = form.ClientRectangle;
            mMenuGraphics = g;
            mText.setGraphics(g);
            mItems = new ArrayList();
            mCallbacks = new ArrayList();
            mItemRects = new ArrayList();
        }

        public void setGraphics(Graphics g) {
            mMenuGraphics = g;
            mText.setGraphics(g);
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
            mCurrentMenuIndex += 1;
            if (mCurrentMenuIndex >= mItems.Count)
                mCurrentMenuIndex = -1;
            Draw();
        }

        public void decIndex() {
            mCurrentMenuIndex -= 1;
            if (mCurrentMenuIndex < -1 )
                mCurrentMenuIndex = mItems.Count-1;
            Draw();
        }


        public void Draw() {
            int index = 0;
            Rectangle r;
            int x = mParentRect.X + mParentRect.Width - 30;
            int y = mParentRect.Y;

            // draw yellow bar on the side
            mBrush.Color = sBarColor;
            mMenuGraphics.FillRectangle(mBrush, x, y, 30, mParentRect.Height);

            mBrush.Color = sSelColor;
            x += 4; y = y + mParentRect.Height - 4 - 22; // - 22 because screensize does not seen to be 320??
            foreach (string item in mItems) {
                mBrush.Color = sSelColor;
                mMenuGraphics.FillRectangle(mBrush, x, y, 22, 22);
                mText.text = item;
                mText.x = x - 10 - mText.width();
                mText.y = y+2;
                mItemRects[index] = r = new Rectangle(mText.x - 6, y - 2, mText.width() + 10 + 28, 26);
                if (index == mCurrentMenuIndex) {
                    mMenuGraphics.FillRectangle(mBrush, r.X, r.Y, r.Width - 26, 26);
                    mText.color = sSelTextColor;
                    mBrush.Color = sSelTextColor;
                    mMenuGraphics.FillRectangle(mBrush, x+3, y+3, 16, 16);
                } else {
                    mText.color = sTextColor;
                }
                mText.Draw();
                // g.DrawRectangle(new Pen(Color.Black, 1.0F), (Rectangle)mItemRects[index]);
                index++;
                y -= 27;

            }
        }
    }
}
