using System;
using System.Windows.Forms;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Drawing;

namespace Diwi {
    class LoginPage : DiwiPageBase {

        DiwiUIText mUserText = new DiwiUIText();
        DiwiUIText mPassText = new DiwiUIText();
        DiwiUIText mServerMess = new DiwiUIText();
        TextBox mUserBox = new TextBox();
        TextBox mPassBox = new TextBox();
        DiwiUIButton mOkButton;
        private Font mFont = new Font("Tahoma", 11, FontStyle.Bold);

        public LoginPage(DiwiPageBase parent)
            : base(parent) {

            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));
            mOkButton = new DiwiUIButton(offScreenGraphics, 146, 170, "Login", buttonOK, this);

            AppController.sKwxClient.messageCallback += new KwxClient.MessageCallback(serverMessage);

            mUserText.text = "user:";
            mPassText.text = "pass:";
            mServerMess.text = Properties.Resources.KwxServerUrl;

            addDrawable(mUserText);
            addDrawable(mPassText);
            addDrawable(mOkButton);
            addDrawable(mServerMess);

            this.Controls.Add(mUserBox);
            this.Controls.Add(mPassBox);

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

        private void reOrient() {
            if (!horizontal) { //vertical
                mOkButton.x = 146;
                mOkButton.y = 170;
                mServerMess.y = 286;
                mUserBox.Top = 100;
                mPassBox.Top = 130;
                mUserText.y = 110;
                mPassText.y = 134; 
            } else { // horizontal
                mOkButton.x = 146;
                mOkButton.y = 130;
                mServerMess.y = 206;
                mUserBox.Top = 60;
                mPassBox.Top = 90;
                mUserText.y = 70;
                mPassText.y = 94;
            }
        }

        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);

            mUserBox.Width = mPassBox.Width = 160;
            mUserBox.Height = mPassBox.Height = 20;
            mUserBox.Left = mPassBox.Left = 40;
            mServerMess.x = 4; 

            mUserBox.Text = Properties.Resources.KwxServerUsername;
            mPassBox.Text = Properties.Resources.KwxServerPassword;

            mUserBox.Font = mFont;
            mPassBox.Font = mFont;

            mUserText.x = 4; 
            mPassText.x = 4;

            reOrient();
            
            mUserBox.Focus();
            mIsInitialized = true;
        }

        public void buttonOK() {
            AppController.sKwxClient.stop();
            AppController.sKwxClient.start(mUserBox.Text, mPassBox.Text);
            if (AppController.sKwxClient.agentKey != null) {
                FileInfo fi = new FileInfo(@"\test.jpg");
                if( fi.Exists ) 
                new MediaUploader(fi.FullName, "testImage", null);
                doTerug(0, null);
            }
        }
        
        
        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if (mInitializing) return;
            if (base.doResize(e)) {
                reOrient();
                draw();
            }
        }
    }
}
