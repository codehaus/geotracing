
using System;
using System.Drawing;

namespace Diwi {

    public class DiwiUIText : DiwiDrawable {
        #region Fields

        private SolidBrush mBrush = new SolidBrush(Color.Red);
        private SolidBrush meBrush = new SolidBrush(Color.Red);
        private Font mFont = new Font("Tahoma", 10, FontStyle.Bold);
        private string mText = string.Empty;
        private Rectangle mTextRect;
        private int _x = 10;
        private int _y = 260;

        #endregion

        #region Properties


        public Color color {
            get { return mBrush.Color; }
            set { mBrush.Color = value; }
        }

        public Rectangle rect {
            get { return mTextRect; }
        }

        public Font font {
            get { return mFont; }
            set { mFont = value; }
        }

        public string text {
            get { return mText; }
            set { mText = value; }
        }

        public SolidBrush brush {
            get { return mBrush; }
            set { mBrush = value; }
        }

        public int x {
            get { return _x; }
            set { _x = value; }
        }


        public int y {
            get { return _y; }
            set { _y = value; }
        }

        #endregion

        #region Constructor

        public DiwiUIText(Graphics g) {
            mTextRect = new Rectangle();
        }

        public DiwiUIText(Graphics g, Color color) {
            mTextRect = new Rectangle();
            mBrush = new SolidBrush(color);
        }

        public DiwiUIText(Graphics g, Color color, string text) {
            mTextRect = new Rectangle();
            mBrush = new SolidBrush(color);
            mText = text;
        }

        public DiwiUIText(Graphics g, Color color, string text, Font font) {
            mTextRect = new Rectangle();
            mBrush = new SolidBrush(color);
            mFont = font;
            mText = text;
        }

        public DiwiUIText(Graphics g, Color color, string text, Font font, int x, int y) {
            mTextRect = new Rectangle();
            mBrush = new SolidBrush(color);
            mFont = font;
            mText = text;
            _x = x;
            _y = y;
       }

        #endregion

        public void doMouseClick(int x, int y) {
        }

        public int width {
            get {
                return (int)DiwiPageBase.offScreenGraphics.MeasureString(mText, mFont).Width;
            }
        }

        private void setTextRect() {
            SizeF ts = DiwiPageBase.offScreenGraphics.MeasureString(mText, mFont);
            mTextRect.X = _x;
            mTextRect.Y = _y;
            mTextRect.Width = (int)(ts.Width + 0.5);
            mTextRect.Height = (int)(ts.Height + 0.5);
        }

        public void erase(Color bgColor) {
            ((SolidBrush)meBrush).Color = bgColor;
            Rectangle r = mTextRect;
            r.X -= 1; r.Y += 1;
            r.Width += 2;
            r.Height += 2;
            DiwiPageBase.offScreenGraphics.FillRectangle(meBrush, r);
        }

        public void draw() {
            setTextRect();
            DiwiPageBase.offScreenGraphics.DrawString(mText, mFont, mBrush, _x, _y);

        }

        public void draw(string s) {
            mText = s;
            setTextRect();
            DiwiPageBase.offScreenGraphics.DrawString(mText, mFont, mBrush, _x, _y);
        }
    }

}
