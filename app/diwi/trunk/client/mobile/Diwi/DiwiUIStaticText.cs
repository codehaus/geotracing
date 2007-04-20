using System;
using System.Windows.Forms;
using System.Collections.Generic;
using System.Text;
using System.Drawing;

namespace Diwi {
    class DiwiUIStaticText : DiwiDrawable {
        private int _x = 10;
        private int _y = 170;
        private Size mSize;
        private string mText = null;
        private Label mTextBox;
        private Form mParentForm;

        public DiwiUIStaticText(Form form) {
            mParentForm = form;
            mTextBox = new Label();
            //mTextBox.ReadOnly = true;
            mTextBox.ForeColor = Color.Black;
           // mTextBox.BorderStyle = BorderStyle.None;
//            mTextBox.BackColor = Color.Transparent;
            mTextBox.BackColor = DiwiPageBase.sBackgroundColor;
            form.Controls.Add(mTextBox);
        }

        public void kill() {
            mParentForm.Controls.Remove(mTextBox);
        }

        public Label textbox {
            get { return mTextBox; }
        }

       public int x {
            get { return _x; }
           set { _x = value; mTextBox.Left = _x; }
        }

        public int y {
            get { return _y; }
            set { _y = value; mTextBox.Top = _y; }
        }

        public Size size {
            get { return mSize; }
            set {
                mSize = value;
                mTextBox.Width = mSize.Width;
                mTextBox.Height = mSize.Height;
            }
        }
    
        public string text {
            get { return mText; }
            set { 
                mText = value;
            }
        }

        public void doMouseClick(int x, int y) {
        }

        public void draw() {
            if (mText != null) {
                mTextBox.Text = mText;
            }
        }
    }
}
