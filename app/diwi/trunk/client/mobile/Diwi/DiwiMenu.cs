using System;
using System.Collections;
using System.Text;
using System.Drawing;
using System.Windows.Forms;

namespace Diwi {
    class DiwiUIMenu : DiwiDrawable {
        public static Color sBackColor = Color.FromArgb(180, 250, 0);
        public static Color sBarColor = Color.FromArgb(220, 250, 0);
        public static Color sSelColor = Color.FromArgb(130, 100, 30);
        public static Color sTextColor = Color.Black;
        public static Color sSelTextColor = Color.FromArgb(180, 180, 180);
        private SolidBrush mBrush = new SolidBrush(Color.Red);
        private DiwiUIText mText = new DiwiUIText(null,Color.Black,"",new Font("Tahoma", 11, FontStyle.Bold));
        private DiwiPageBase mParentForm;
        public delegate void DiwiMenuCallbackHandler();
        private Graphics mMenuGraphics;
        private int mCurrentMenuIndex = 1;

        ArrayList mItems;
        ArrayList mCallbacks;
        ArrayList mItemRects;
        Rectangle mParentRect;

        public DiwiUIMenu(Graphics g, DiwiPageBase form) {
            mParentForm = form;
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


        public void draw() {
            int index = 0;
            Rectangle r;
            int x = mParentRect.X + mParentRect.Width - 30;
            int y = mParentRect.Y;

            // draw yellow bar on the side
            mBrush.Color = sBarColor;
            mMenuGraphics.FillRectangle(mBrush, x, y, 30, mParentRect.Height);

            mBrush.Color = sSelColor;
            mMenuGraphics.FillRectangle(mBrush, 32, 4, mParentRect.Width - 32 - 4 - 30, 24);
            mMenuGraphics.FillRectangle(mBrush, mParentRect.Width - 27, 4, 24, 24);
            mText.text = mParentForm.title;
            mText.x = 36;
            mText.y = 5;
            mText.color = sSelTextColor;
            mText.draw();

            mBrush.Color = sSelColor;
            x = mParentRect.Width - 27; y = 34;

            foreach (string item in mItems) {
                mBrush.Color = sSelColor;
                mMenuGraphics.FillRectangle(mBrush, x, y, 24, 24);
                mText.text = item;
                mText.x = x - 10 - mText.width();
                mText.y = y+2;
                mItemRects[index] = r = new Rectangle(mText.x - 6, y - 2, mText.width() + 10 + 28, 26);
                if (index == mCurrentMenuIndex) {
                    mMenuGraphics.FillRectangle(mBrush, r.X, r.Y, r.Width - 26, 26);
                    mText.color = sSelTextColor;
                    mBrush.Color = sSelTextColor;
                    mMenuGraphics.FillRectangle(mBrush, x+3, y+3, 17, 17);
                } else {
                    mText.color = sTextColor;
                }
                mText.draw();
                //mMenuGraphics.DrawRectangle(new Pen(Color.Black, 1.0F), (Rectangle)mItemRects[index]);
                index++;
                y += 30;

            }
        }
    }
}