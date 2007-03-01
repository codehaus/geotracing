using System;
using System.Collections.Generic;
using System.Text;
using System.Drawing;
using System.Windows.Forms;
using System.Threading;
using System.Collections;

namespace Diwi {   // base class for Diwi Pages.
    // 
    class DiwiPageBase : Form {
        private enum sKeys { M_UP=38, M_DOWN=40, M_LEFT=37, M_RIGHT=39 };
        protected static Bitmap offScreenBitmap;
        protected static Graphics offScreenGraphics;
        protected static Graphics onScreenGraphics;
        protected Form mParent;
        protected bool mIsInitialized=false;
        protected Color mBackgroundColor;
        private DiwiUIText smallMessage;
        private ArrayList mDrawableElements;
        private Rectangle mCurrentRect;
        protected DiwiUIMenu mMenu;
        private string mTitle;

        public DiwiPageBase(Form parent) {


            mParent = parent;
            mBackgroundColor = Color.FromArgb(180, 250, 0);

            mCurrentRect = this.ClientRectangle;

            offScreenBitmap = new Bitmap(this.ClientRectangle.Width, this.ClientRectangle.Height);
            offScreenGraphics = Graphics.FromImage(offScreenBitmap);
            onScreenGraphics = this.CreateGraphics();
            offScreenGraphics.Clear(Color.Black);

            mDrawableElements = new ArrayList();
            mMenu = new DiwiUIMenu(offScreenGraphics,this);
            addDrawable(mMenu);

            smallMessage = new DiwiUIText(offScreenGraphics,Color.Black);
            smallMessage.x = 4; smallMessage.y = this.ClientRectangle.Bottom - 20;

            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.None;
            this.AutoScroll = false;
            this.ClientSize = new System.Drawing.Size(240, 320);
        
            this.Name = "";
            this.Text = "";
            this.WindowState = System.Windows.Forms.FormWindowState.Maximized;
            this.ResumeLayout(false);
        }

        public string title {
            get { return mTitle; }
            set { mTitle = value; }
        }

        protected void addDrawable(DiwiDrawable d) {
            mDrawableElements.Add(d);
        }

        public static void redrawRect(Rectangle oldR, Rectangle newR) {
            if (oldR.Width > newR.Width)
                onScreenGraphics.DrawImage(offScreenBitmap, newR.X, newR.Y, oldR, GraphicsUnit.Pixel);
            else
                onScreenGraphics.DrawImage(offScreenBitmap, newR.X, newR.Y, newR, GraphicsUnit.Pixel);
        }

        protected override void OnLoad(EventArgs e) {
            this.Visible = true;
            this.Focus();  // give the form focus so it can receive KeyEvents
            this.KeyDown += new KeyEventHandler(this.keydown);
        }

        void keydown(Object o, KeyEventArgs e) {
            string txt="";
            switch (e.KeyValue) {
                case (int)sKeys.M_DOWN:
                    txt = "down";
                    mMenu.decIndex();
                    break;
                case (int)sKeys.M_LEFT:
                    txt = "left";
                    break;
                case (int)sKeys.M_RIGHT:
                    txt = "right";
                    break;
                case (int)sKeys.M_UP:
                    txt = "up";
                    mMenu.incIndex();
                    break;

            }
            if (txt != "") {
                draw();
            }

            if (e.KeyCode == Keys.Enter) {
                Program.sGpsReader.stop();
                Program.sKwxClient.stop();
                Thread.Sleep(1000);
                Application.DoEvents();
                Application.Exit();
            }

            e.Handled = true;  //all key events handled by the form
        }

        protected void drawDebugText(string txt) {
            Rectangle oldRect = smallMessage.rect;
            smallMessage.erase(mBackgroundColor);
            smallMessage.draw(txt);
            redrawRect(oldRect, smallMessage.rect);
        }

        protected override void OnPaint(PaintEventArgs e) {
            onScreenGraphics.DrawImage(offScreenBitmap, 0, 0, this.ClientRectangle, GraphicsUnit.Pixel);
        }

        protected void draw() {
            offScreenGraphics.Clear(mBackgroundColor);
            foreach (DiwiDrawable d in mDrawableElements) {
                d.draw();
            }
            onScreenGraphics.DrawImage(offScreenBitmap, 0, 0, this.ClientRectangle, GraphicsUnit.Pixel);
        }

        protected override void OnResize(EventArgs e) {
            base.OnResize(e);

            if (mCurrentRect.Width != this.ClientRectangle.Width || mCurrentRect.Height != this.ClientRectangle.Height) {

                mCurrentRect = this.ClientRectangle;

                if (onScreenGraphics != null)
                    onScreenGraphics.Dispose();
                if (offScreenGraphics != null)
                    offScreenGraphics.Dispose();
                if (offScreenBitmap != null)
                    offScreenBitmap.Dispose();

                offScreenBitmap = new Bitmap(this.ClientRectangle.Width, this.ClientRectangle.Height);
                offScreenGraphics = Graphics.FromImage(offScreenBitmap);
                offScreenGraphics.Clear(mBackgroundColor);
                onScreenGraphics = this.CreateGraphics();

                mMenu.resize(this.ClientRectangle);
                smallMessage.y = this.ClientRectangle.Bottom - 20;
            }

            if (mIsInitialized) {
                smallMessage.setGraphics(offScreenGraphics);
                foreach (DiwiDrawable d in mDrawableElements) {
                    d.setGraphics(offScreenGraphics);
                }
            }
        }
    }
}
