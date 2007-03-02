using System;
using System.Windows.Forms;
using System.Collections.Generic;
using System.Text;
using System.Drawing;

namespace Diwi {
    class LoginPage : DiwiPageBase {

        DiwiUIText mServerText = new DiwiUIText(offScreenGraphics);
        DiwiUIText mServerMess = new DiwiUIText(offScreenGraphics);
        TextBox mUserBox = new TextBox();
        TextBox mPassBox = new TextBox();
        private Font mFont = new Font("Tahoma", 11, FontStyle.Bold);

        public LoginPage(DiwiPageBase parent)
            : base(parent) {

            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));

            title = "Login";
        }

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);

            mUserBox.Width = mPassBox.Width = 160;
            mUserBox.Height = mPassBox.Height = 20;
            mUserBox.Left = mPassBox.Left = 40;
            mUserBox.Top = 160;
            mPassBox.Top = 186;

            mUserBox.Text = Properties.Resources.KwxServerUsername;
            mPassBox.Text = Properties.Resources.KwxServerPassword;

            mUserBox.Font = mFont;
            mPassBox.Font = mFont;

            this.Controls.Add(mUserBox);
            this.Controls.Add(mPassBox);

            mServerText.x = 4; mServerText.y = 160; mServerText.draw("user:");
            mServerText.x = 4; mServerText.y = 186; mServerText.draw("pass:");

            mUserBox.Focus();

            mIsInitialized = true;
        }


        
        
        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if (base.doResize(e)) {
                int y = this.ClientRectangle.Height - 4;
                mServerMess.x = 8; mServerMess.y = y -= 20;
                mServerText.x = 8; mServerText.y = y -= 20;
            }
        }
    }
}
