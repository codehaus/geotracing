using System;
using System.Collections.Generic;
using System.Text;
using System.Drawing;
using System.Windows.Forms;
using System.Threading;
using System.Collections;
using System.IO;

namespace Diwi {   // base class for Diwi Pages.
    // 
    class DiwiPageBase : Form {
        private enum sKeys { M_UP=38, M_DOWN=40, M_LEFT=37, M_RIGHT=39 };
        public static Bitmap offScreenBitmap;
        Icon sMeIcon = null;
        public static Graphics offScreenGraphics;
        private static Rectangle mBaseRect = new Rectangle(0, 0, 0, 0);
        protected Graphics onScreenGraphics;
        protected DiwiPageBase mParent;
        protected bool mIsInitialized = false;
        protected ArrayList mDrawableElements;
        protected Rectangle mCurrentRect;
        protected DiwiUIMenu mMenu;
        public static Color sBackgroundColor;
        protected bool mInitializing = true;
        protected DiwiUIText mouseText;
        
        int mYposition = -1;
        int mXposition = -1;

        DiwiImage mBackImage = null;
        DiwiScalingImage mForeImage = null;

        private string mTitle;

        public DiwiPageBase(DiwiPageBase parent) {


            mParent = parent;
            sBackgroundColor = Color.FromArgb(180, 250, 0);

            mCurrentRect = this.ClientRectangle;

            if (sMeIcon == null) {
                sMeIcon = new Icon(AppController.sAssembly.GetManifestResourceStream(@"Diwi.Resources.mypos.ico"));
            }

            this.SuspendLayout();

            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.None;
            this.AutoScroll = false;
            this.BackColor = sBackgroundColor;
            this.ClientSize = new System.Drawing.Size(240, 320);
            this.Name = "";
            this.Text = "";
            this.WindowState = System.Windows.Forms.FormWindowState.Maximized;
            this.BackColor = sBackgroundColor;
            this.ResumeLayout(false);


            this.KeyDown += new KeyEventHandler(this.keydown);

            baseResize(this.ClientRectangle);

            mDrawableElements = new ArrayList();
            mMenu = new DiwiUIMenu(this);
            addDrawable(mMenu);
            mouseText = new DiwiUIText();

            setBackground();

            mInitializing = false;
        
        }

        protected override void OnMouseMove(MouseEventArgs e) {
            Rectangle oldRect = mouseText.rect;
            mouseText.erase(sBackgroundColor);

            mouseText.text = "m: " + e.X.ToString() + ", " + e.Y.ToString();
            mouseText.x = 4;
            mouseText.y = mCurrentRect.Height-18;
            mouseText.draw();
            redrawRect(oldRect, mouseText.rect);
        }

        public bool horizontal {
            get { 
                if (this.ClientRectangle.Width > this.ClientRectangle.Height) 
                    return true; 
                return false; 
            }
        }
        
        public string title {
            get { return mTitle; }
            set { Name = mTitle = value; }
        }

        protected void addDrawable(DiwiDrawable d) {
            mDrawableElements.Add(d);
        }

        void setBackground() {
            mBackImage = new DiwiImage(this);
            mBackImage.x = 0;
            mBackImage.y = 0;
            if (horizontal) {
                Size size = new Size(320, 240);
                mBackImage.size = size;
                mBackImage.bitmap = AppController.backgroundHorBitmap;
            } else {
                Size size = new Size(240, 320);
                mBackImage.size = size;
                mBackImage.bitmap = AppController.backgroundVerBitmap;
            }
        }

        protected void setPosition(int x, int y) {
            bool redraw = ( x != mXposition || y != mYposition);
            mXposition = x;
            mYposition = y;
            if (redraw) draw();
        }

        protected void setBackGroundImg(String anImageName, int aWidth, int aHeight, int aX, int aY) {
            Stream stream = null;
            try {
                stream = AppController.sAssembly.GetManifestResourceStream(anImageName);
                mBackImage = new DiwiImage(this);
                Size size = new Size(aWidth, aHeight);
                mBackImage.size = size;
                mBackImage.x = aX;
                mBackImage.y = aY;
                mBackImage.bitmap = new Bitmap(stream);
                draw();
            } catch (System.IO.FileNotFoundException e) {
                MessageBox.Show(e.Message);
            }
        }

        protected void setBackGroundFromFile(string path, int aWidth, int aHeight, int aX, int aY) {
            Stream stream = null;
            try {
                stream = new FileStream(path, FileMode.Open, FileAccess.Read);
                mBackImage = new DiwiImage(this);
                Size size = new Size(aWidth, aHeight);
                mBackImage.size = size;
                mBackImage.x = aX;
                mBackImage.y = aY;
                mBackImage.bitmap = new Bitmap(stream);
                draw();
                stream.Close();
            } catch (System.IO.FileNotFoundException e) {
                MessageBox.Show(e.Message);
            }
        }


        protected void setForeGroundImg(String anImageName, int aWidth, int aHeight, int aX, int aY) {
            Stream stream = null;
            if (mForeImage != null) {
                mForeImage.kill();
                mForeImage = null;
            }
            if (anImageName == null) {
                draw();
                return;
            }
            try {
                stream = AppController.sAssembly.GetManifestResourceStream(anImageName);
                mForeImage = new DiwiScalingImage(this);
                Size size = new Size(aWidth, aHeight);
                mForeImage.size = size;
                mForeImage.x = aX;
                mForeImage.y = aY;
                mForeImage.bitmap = new Bitmap(stream);
                draw();
                stream.Close();
            } catch (System.IO.FileNotFoundException e) {
                MessageBox.Show(e.Message);
            }
        }

        public void redrawRect(Rectangle oldR, Rectangle newR)
        {
            if (oldR.Width > newR.Width)
                onScreenGraphics.DrawImage(offScreenBitmap, newR.X, newR.Y, oldR, GraphicsUnit.Pixel);
            else
                onScreenGraphics.DrawImage(offScreenBitmap, newR.X, newR.Y, newR, GraphicsUnit.Pixel);
        }

        protected override void OnLoad(EventArgs e) {
            this.Visible = true;
            this.Focus();  // give the form focus so it can receive KeyEvents
        }

        protected override void OnMouseDown(MouseEventArgs e) {
            foreach (DiwiDrawable d in mDrawableElements) {
                d.doMouseClick(e.X, e.Y);
            }
        }

        void keydown(Object o, KeyEventArgs e) {
            switch (e.KeyValue) {
                case (int)sKeys.M_DOWN:
                    mMenu.decIndex();
                    draw();
                    break;
                case (int)sKeys.M_LEFT:
                    MapHandler.mapRadius *= 1.5F;
                    break;
                case (int)sKeys.M_RIGHT:
                    MapHandler.mapRadius *= 0.75F;
                    break;
                case (int)sKeys.M_UP:
                    mMenu.incIndex();
                    draw();
                    break;

            }

            e.Handled = true;  //all key events handled by the form
            if (e.KeyCode == Keys.Enter) {
                mMenu.menuSelect();
            }  
        }

        protected override void OnActivated(EventArgs e) {
            base.OnActivated(e);
            //this.Visible = true;
            draw();
        }

        protected virtual void doTerug(int i, string s) {
            //this.Visible = false;
            if (mParent != null)
                mParent.Show();
            Close();
        }


        protected override void OnPaint(PaintEventArgs e) {
            onScreenGraphics.DrawImage(offScreenBitmap, 0, 0, this.ClientRectangle, GraphicsUnit.Pixel);
        }

        public void draw() {
            offScreenGraphics.Clear(sBackgroundColor);
            if (mBackImage != null) {
                mBackImage.draw();
            }

            if (mXposition != -1 && mYposition != -1) {
                offScreenGraphics.DrawIcon(sMeIcon, mXposition-8, mYposition-8);
            }

            foreach (DiwiDrawable d in mDrawableElements) {
                d.draw();
            }
            if (onScreenGraphics == null) onScreenGraphics = this.CreateGraphics();
            onScreenGraphics.DrawImage(offScreenBitmap, 0, 0, this.ClientRectangle, GraphicsUnit.Pixel);
        }



        static bool baseResize(Rectangle r) {
            if (mBaseRect.Width != r.Width || mBaseRect.Height != r.Height) {
                mBaseRect = r;
                if (offScreenGraphics != null)
                    offScreenGraphics.Dispose();
                if (offScreenBitmap != null)
                    offScreenBitmap.Dispose();

                offScreenBitmap = new Bitmap(mBaseRect.Width, mBaseRect.Height);
                offScreenGraphics = Graphics.FromImage(offScreenBitmap);
                return true;
            }
            return false;
        }


        protected bool doResize(EventArgs e) {

            if (mCurrentRect.Width != this.ClientRectangle.Width || mCurrentRect.Height != this.ClientRectangle.Height) {

                mCurrentRect = this.ClientRectangle;
                bool didResize = baseResize(mCurrentRect);

                onScreenGraphics = this.CreateGraphics();


                if (mIsInitialized) {
                    mMenu.resize(this.ClientRectangle);
                    return true;
                }
            }
            return false;
        }
    }
}
