
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
        private Graphics mTextGraphics;
        private int _x = 10;
        private int _y = 260;

        #endregion

        #region Properties

        public void setGraphics(Graphics g) {
            mTextGraphics = g;
        }

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
            mTextGraphics = g;
        }

        public DiwiUIText(Graphics g, Color color) {
            mTextRect = new Rectangle();
            mBrush = new SolidBrush(color);
            mTextGraphics = g;
        }

        public DiwiUIText(Graphics g, Color color, string text) {
            mTextRect = new Rectangle();
            mBrush = new SolidBrush(color);
            mText = text;
            mTextGraphics = g;
        }

        public DiwiUIText(Graphics g, Color color, string text, Font font) {
            mTextRect = new Rectangle();
            mBrush = new SolidBrush(color);
            mFont = font;
            mText = text;
            mTextGraphics = g;
        }

        public DiwiUIText(Graphics g, Color color, string text, Font font, int x, int y) {
            mTextRect = new Rectangle();
            mBrush = new SolidBrush(color);
            mFont = font;
            mText = text;
            _x = x;
            _y = y;
             mTextGraphics = g;
       }

        #endregion

        public int width() {
            return (int) mTextGraphics.MeasureString(mText, mFont).Width;
        }

        private void setTextRect() {
            SizeF ts = mTextGraphics.MeasureString(mText, mFont);
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
            mTextGraphics.FillRectangle(meBrush, r);
        }

        public void Draw() {
            setTextRect();
            mTextGraphics.DrawString(mText, mFont, mBrush, _x, _y);

        }

        public void Draw(string s) {
            mText = s;
            setTextRect();
            mTextGraphics.DrawString(mText, mFont, mBrush, _x, _y);
        }
    }

}
