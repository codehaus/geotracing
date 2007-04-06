using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.IO;
using System.Windows.Forms;
using Microsoft.WindowsMobile.Forms;

namespace Diwi {
    /// <summary>
    /// Implements a scaling image.
    /// Needs picturebox to be able to scale...
    /// Picturebox is a control, so is always on top.
    /// </summary>
    class DiwiScalingImage : DiwiDrawable
    {
        private int _x = 10;
        private int _y = 170;
        private Size mSize;
        private Bitmap mBitmap = null;
        private PictureBox mPicBox;
        private Form mParentForm;

        public DiwiScalingImage(Form form) {
            mParentForm = form;
            mPicBox = new PictureBox();
            mPicBox.SizeMode = PictureBoxSizeMode.StretchImage;
            form.Controls.Add(mPicBox);
        }

        public void kill() {
            mParentForm.Controls.Remove(mPicBox);
        }

        public PictureBox picbox {
            get { return mPicBox; }
        }

       public int x {
            get { return _x; }
            set { _x = value; mPicBox.Left = _x; }
        }

        public int y {
            get { return _y; }
            set { _y = value; mPicBox.Top = _y; }
        }

        public Size size {
            get { return mSize; }
            set {
                mSize = value;
                mPicBox.Width = mSize.Width;
                mPicBox.Height = mSize.Height;
            }
        }
    
        public Bitmap bitmap {
            get { return mBitmap; }
            set { 
                mBitmap = value;

            }
        }

        public void doMouseClick(int x, int y) {
        }

        public void draw() {
            if (mBitmap != null) {
                mPicBox.Image = mBitmap;
            }
        }
    }


    /// <summary>
    /// Implements a non-scaling image.
    /// can be background, can be drawn over...
    /// </summary>
    class DiwiImage : DiwiDrawable {
        private int _x = 10;
        private int _y = 170;
        private Size mSize;
        private Bitmap mBitmap = null;
        private Form mParentForm;

        public DiwiImage(Form form) {
            mParentForm = form;
        }

        public void kill() {
        }

        public int x {
            get { return _x; }
            set { _x = value; }
        }

        public int y {
            get { return _y; }
            set { _y = value; }
        }

        public Size size {
            get { return mSize; }
            set {
                mSize = value;
            }
        }

        public Bitmap bitmap {
            get { return mBitmap; }
            set {
                mBitmap = value;
            }
        }

        public void doMouseClick(int x, int y) {
        }

        public void draw() {
            if (mBitmap != null) {
                DiwiPageBase.offScreenGraphics.DrawImage(mBitmap, _x, _y);
            }
        }
    }



}
