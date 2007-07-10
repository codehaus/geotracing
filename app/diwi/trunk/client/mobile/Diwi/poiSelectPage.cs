using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.IO;
using System.Diagnostics;
using System.Windows.Forms;
using Microsoft.WindowsMobile.Forms;

namespace Diwi {

    class PoiSelectPage : DiwiPageBase {
        static PoiViewerPage sPoiPage = null;
        Label mTextBox = new Label();
        List<string> poiIDs;
        XMLement mCurrentPOI;
        bool mHasContent = false;

        public PoiSelectPage(DiwiPageBase parent)
            : base(parent) {


            mMenu.addItem("Toon Inhoud", new DiwiUIMenu.DiwiMenuCallbackHandler(doView),null);
            mMenu.addItem("Volgende", new DiwiUIMenu.DiwiMenuCallbackHandler(doNext),AppController.sVolgIcon);

            this.Controls.Add(mTextBox);

            mTextBox.Font = new Font("Tahoma", 12, FontStyle.Regular);
            //mTextBox.Multiline = true;
            //mTextBox.ReadOnly = true;
            //mTextBox.ScrollBars = ScrollBars.Vertical;
            mTextBox.ForeColor = Color.Black;
            mTextBox.BackColor = mTextBox.BackColor = Color.FromArgb(198, 255, 0);

            reOrient();

            sPoiPage = new PoiViewerPage(this);

            title = "";

        }

        void doView(int i, string s) {

            if (mHasContent) {
                mIsActive = false;
                sPoiPage.ShowDialog();
            } else {
                resetMenu();
            }
        }

        void doNext(int i, string s) {
            if (poiIDs.Count > 0) {
                string id = poiIDs[0];
                if (id != null) {
                    poiIDs.Remove(id);
                    if (poiIDs.Count == 0) {
                        setMenuText(1, "Terug");
                        setMenuIcon(1, AppController.sTerugIcon);
                    }
                    bool n = offerPOI(id);
                    if (!n) {
                        sPoiPage.abortDownload();
                        doTerug(0, "");
                    }
                }
            } else {
                sPoiPage.abortDownload();
                doTerug(0, "");
            }
            draw();
        }

        bool offerPOI(string poiID) {
            XMLement x = AppController.sKwxClient.getPOI(poiID);

            if (x != null) {

                AppController.sEventLog.WriteLine("\tpoi: {0}", poiID);
                x = x.getChildByName("poi");

                if (x != null) {
                    mCurrentPOI = x;
                    XMLement name_xml = x.getChildByName("name");
                    if (name_xml != null) {
                        AppController.sEventLog.WriteLine("\tname: {0}", name_xml.nodeText);
                        title = name_xml.nodeText;
                    }
                    XMLement desc_xml = x.getChildByName("description");
                    if (desc_xml != null) {
                        AppController.sEventLog.WriteLine("\tdesc: {0}", desc_xml.nodeText);
                        mTextBox.Text = desc_xml.nodeText;
                    }
                    mTextBox.Visible = true;
                    mHasContent = sPoiPage.setContent(x);
                    if (mHasContent) {
                        setMenuText(0, "Toon Media");
                    } else {
                        setMenuText(0, "Geen Media");
                    }
                    return true;
                }
            }
            return false;
        }
 
        public bool setContent(List<string> pids) {
            poiIDs = pids;
            string id = poiIDs[0];
            if (id != null) {
                poiIDs.Remove(id);
                return offerPOI(id);
            }
            return false;
        }

        private void reOrient() {
            if (horizontal) {
                mTextBox.Left = 4;
                mTextBox.Top = 100;
                mTextBox.Size = new Size(280, 140);

            } else {
                mTextBox.Left = 4;
                mTextBox.Top = 100;
                mTextBox.Size = new Size(200, 220);
            }
        }


        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            if (mParent != null) {
                mParent.Visible = false;
            }
            if (poiIDs.Count == 0) {
                setMenuText(1, "Terug");
                setMenuIcon(1, AppController.sTerugIcon);
            } else {
                setMenuText(1, "Volgende");
                setMenuIcon(1, AppController.sVolgIcon);
            }
            reOrient();
            mIsInitialized = true;
            this.Focus();
        }

        protected override void OnResize(EventArgs e) {
            // change location of stuff
            if (base.doResize(e) == true) {
                if (mIsInitialized) {
                    reOrient();
                    draw();
                }
            }
        }
    }
}
