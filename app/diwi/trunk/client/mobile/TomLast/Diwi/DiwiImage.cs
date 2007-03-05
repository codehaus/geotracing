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
    /// Implements an image.
    /// Needs picturebox to be able to scale...
    /// </summary>
    class DiwiImage : DiwiDrawable
    {
        private Graphics mGraphics;
        private int _x = 10;
        private int _y = 170;
        private Size mSize;
        private Bitmap mBitmap = null;
        private PictureBox mPicBox;

        public DiwiImage(Graphics g, Form form) {
            mGraphics = g;
            mPicBox = new PictureBox();
            mPicBox.SizeMode = PictureBoxSizeMode.StretchImage;
            form.Controls.Add(mPicBox);
        }

        public PictureBox picbox {
            get { return mPicBox; }
        }

        public void setGraphics(Graphics g) {
            mGraphics = g;
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





}
