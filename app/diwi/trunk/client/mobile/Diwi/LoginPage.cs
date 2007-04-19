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
        DiwiUIButton mOkButton;
        private Font mFont = new Font("Tahoma", 11, FontStyle.Bold);

        public LoginPage(DiwiPageBase parent)
            : base(parent) {

            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));
            mOkButton = new DiwiUIButton(offScreenGraphics, 146, 220, "Login", buttonOK, this);
            addDrawable(mOkButton);

            AppController.sKwxClient.messageCallback += new KwxClient.MessageCallback(serverMessage);

            title = "Login";
        }

        
        private delegate void updateKwxMessageDelegate(string m);
        void updateKwxMessage(string m) {
            Rectangle oldRect = mServerMess.rect;
            mServerMess.erase(sBackgroundColor);
            mServerMess.draw(m);
            redrawRect(oldRect, mServerMess.rect);
        }
        void serverMessage(string m) {
            if (InvokeRequired)
                this.Invoke(new updateKwxMessageDelegate(this.updateKwxMessage), new object[] { m });
            else
                updateKwxMessage(m);
        }


        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);

            mUserBox.Width = mPassBox.Width = 160;
            mUserBox.Height = mPassBox.Height = 20;
            mUserBox.Left = mPassBox.Left = 40;
            mUserBox.Top = 100;
            mPassBox.Top = 130;

            mUserBox.Text = Properties.Resources.KwxServerUsername;
            mPassBox.Text = Properties.Resources.KwxServerPassword;

            mUserBox.Font = mFont;
            mPassBox.Font = mFont;

            this.Controls.Add(mUserBox);
            this.Controls.Add(mPassBox);

            mServerMess.x = 4; mServerMess.y = 110; mServerMess.draw("user:");
            mServerMess.x = 4; mServerMess.y = 134; mServerMess.draw("pass:");

            mUserBox.Focus();

            mServerMess.y = this.ClientRectangle.Height - 34;
            mServerMess.draw(Properties.Resources.KwxServerUrl);
            mIsInitialized = true;
        }

        public void buttonOK() {
            AppController.sKwxClient.stop();
            AppController.sKwxClient.start(mUserBox.Text, mPassBox.Text);
            if (AppController.sKwxClient.agentKey != null)
                doTerug(0,null);
        }
        
        
        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if (mInitializing) return;
            if (base.doResize(e)) {
                int y = this.ClientRectangle.Height - 4;
                mServerMess.x = 4; mServerMess.y = y -= 20;
            }
        }
    }
}
