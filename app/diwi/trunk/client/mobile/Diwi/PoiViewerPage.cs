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

    class PoiViewerPage : DiwiPageBase {
        MediaDownloader mMediaDnl = null;
        TextBox mTextBox = new TextBox();
        DiwiScalingImage mImage;
        Bitmap mImageBitmap;
        XMLement mAllMedia;
        int mMediaIndex = 0;
        string xmlString, mDnlUrl;
        int mDownloadIndex=-1;
        string mDnlName = null;
        string[] dnlFileNames = new string[10];
        DiwiUIText mNameMess = new DiwiUIText(Color.Black, "", new Font("Tahoma", 14, FontStyle.Regular));
        DiwiUIText mDnlMess = new DiwiUIText(Color.Red, "", new Font("Tahoma", 12, FontStyle.Regular));

        public PoiViewerPage(DiwiPageBase parent)
            : base(parent) {

            mMenu.addItem("Volgende", new DiwiUIMenu.DiwiMenuCallbackHandler(doNext),AppController.sVolgIcon);

            this.Controls.Add(mTextBox);
            mImage = new DiwiScalingImage(this);

            mTextBox.Font = new Font("Arial", 12, FontStyle.Bold);
            mTextBox.Multiline = true;
            mTextBox.ReadOnly = true;
            mTextBox.ScrollBars = ScrollBars.Vertical;
            mTextBox.ForeColor = Color.Black;
            mTextBox.BackColor = Color.Transparent;

            addDrawable(mNameMess);

            reOrient();

            addDrawable(mImage);

            title = "";

        }

        public void abortDownload() {
            if (mMediaDnl != null) {
                mMediaDnl.abort();
                mMediaDnl = null;
            }
        }

        void openVideo(string fn) {
            if (mIsActive == false) return;
            Process process = new Process();
            process.StartInfo.FileName = fn;
            process.StartInfo.Verb = "Open";
            process.StartInfo.UseShellExecute = true;
            process.Start();
        }

        void drawNameText() {
            Rectangle oldRect = mNameMess.rect;
            mNameMess.erase(sBackgroundColor);
            mNameMess.draw(mDnlName);
            redrawRect(oldRect, mNameMess.rect);
        }


        void openImage(string fn) {
            if (mIsActive == false) return;
            mImageBitmap = new Bitmap(fn);
            reOrient();
            mImage.bitmap = mImageBitmap;
            mImage.x = 10;
            mTextBox.Visible = false;
            if (mDnlName != null) {
                mNameMess.x = mImage.x;
                mNameMess.y = mImage.y + 6 + mImage.size.Height;
                drawNameText();
            }
            draw();
        }


        void openAudio(string fn) {
            if (mIsActive == false) return;

            Process process = new Process();
            process.StartInfo.FileName = fn;
            process.StartInfo.Verb = "Open";
            process.StartInfo.UseShellExecute = true;
            process.Start();

            DiwiPageBase.sCurrentPage.Show();
        }


        void openText(string fn) {
            if (mIsActive == false) return;
            StreamReader r = new StreamReader(fn);
            string s = r.ReadToEnd();
            r.Close();
            mTextBox.Text = s;
            mTextBox.Visible = true;
            mImage.x = 800;
        }

        void openFile(string path) {
            int n = path.IndexOf("Image");
            if (n >= 0) {
                openImage(path);
                return;
            }

            n = path.IndexOf(".jpg");
            if (n >= 0) {
                openImage(path);
                return;
            }

            n = path.IndexOf("Sound");
            if (n >= 0) {
                openAudio(path);
                return;
            }
            n = path.IndexOf(".mp3");
            if (n >= 0) {
                openAudio(path);
                return;
            }

            n = path.IndexOf("Video");
            if (n >= 0) {
                openVideo(path);
                return;
            }

            n = path.IndexOf(".wmv");
            if (n >= 0) {
                openVideo(path);
                return;
            }

            n = path.IndexOf("Text");
            if (n >= 0) {
                openText(path);
                return;
            }

            n = path.IndexOf(".txt");
            if (n >= 0) {
                openText(path);
                return;
            }

        }


        void dnlDoneT(string path, bool local) {

            mMediaDnl = null;

            if (local)
                AppController.sEventLog.WriteLine("\tHit local store: {0}", path);
            else
                AppController.sEventLog.WriteLine("\tDownloaded url: {0}", mDnlUrl);

            dnlFileNames[mDownloadIndex] = path;

            if (this.mIsActive && (mDownloadIndex == mMediaIndex)) {
                if (InvokeRequired) {
                    this.Invoke(new mediaCallback(this.openFile), new object[] { path });
                } else {
                    openFile(path);
                }
            }

            if (mAllMedia.getChild(1 + mDownloadIndex) != null) {
                mDownloadIndex = mDownloadIndex + 1;
                doDownloadMedium(mDownloadIndex);
            } else {
                if (mIsActive) 
                    drawDnlText("");
                else
                    AppController.sPoiSelectPage.setDownloadMessage("downloads: done");
            }


        }

        void doDownloadMedium(int index) {
            char[] trimChars = { '\t', ' ', '\n', '\r' };
            XMLement kichUri = mAllMedia.getChild(index);
            if (kichUri.tag == "ugc-hit") {
                doDownloadUGC(index);
                return;
            }
            mDownloadIndex = index;
            if (kichUri != null) {
                string fn="";
                string url = kichUri.nodeText;
                string ext = (url.Substring(url.LastIndexOf('.'))).TrimEnd(trimChars);
                string type = kichUri.getAttributeValue("type");
                mDnlName = (url.Substring(1 + url.LastIndexOf('/'))).TrimEnd(trimChars);

                string localF = AppController.sMediaStore + mDnlName;
                FileInfo fi = new System.IO.FileInfo(localF);
                if (fi.Exists) {
                    dnlDoneT(localF,true);
                    return;
                }

                AppController.sPoiSelectPage.setDownloadMessage("ophalen: " + mDnlName);
                mDnlUrl = url;
                switch (type) {
                    case "image":
                        fn = AppController.sAppDir + "poiImage" + mMediaIndex.ToString() + ext;
                        break;
                    case "audio":
                        fn = AppController.sAppDir + "poiSound" + mMediaIndex.ToString() + ext;
                        break;
                    case "video":
                        fn = AppController.sAppDir + "poiVideo" + mMediaIndex.ToString() + ext;
                        break;
                    case "text":
                        fn = AppController.sAppDir + "poiText" + mMediaIndex.ToString() + ext;
                        break;
                }
                if( fn != "" )
                    mMediaDnl = new MediaDownloader(url, fn, new AppController.DownloadCallbackHandler(dnlDoneT));

            }
        }
 
        void doDownloadUGC(int index) {
            char[] trimChars = { '\t', ' ', '\n', '\r' };
            XMLement ugc = mAllMedia.getChild(index);
            mDownloadIndex = index;
            if (ugc != null) {
                string fn = "";
                string filename = ugc.getAttributeValue("filename");
                string url = Diwi.Properties.Resources.KwxMediaServerUrl + "?id=" + ugc.getAttributeValue("id");
                string ext = (filename.Substring(filename.LastIndexOf('.'))).TrimEnd(trimChars);
                string type = ugc.getAttributeValue("kind");
                mDnlName = ugc.getAttributeValue("name");
                drawDnlText("ophalen: " + mDnlName);
                switch (type) {
                    case "image":
                        fn = AppController.sAppDir + "poiImage" + mMediaIndex.ToString() + ext;
                        break;
                    case "audio":
                        fn = AppController.sAppDir + "poiSound" + mMediaIndex.ToString() + ext;
                        break;
                    case "video":
                        fn = AppController.sAppDir + "poiVideo" + mMediaIndex.ToString() + ext;
                        break;
                    case "text":
                        fn = AppController.sAppDir + "poiText" + mMediaIndex.ToString() + ext;
                        break;
                }
                if (fn != "")
                    mMediaDnl = new MediaDownloader(url, fn, new AppController.DownloadCallbackHandler(dnlDoneT));
            }
        }

        void drawDnlText(string t) {
            Rectangle oldRect = mDnlMess.rect;
            mDnlMess.erase(sBackgroundColor);
            mDnlMess.draw(t);
            redrawRect(oldRect, mDnlMess.rect);
        }


        void doNext(int i, string s) {
            mMediaIndex++;
            XMLement kichUri = mAllMedia.getChild(mMediaIndex);
            if (kichUri != null) {
                mTextBox.Visible = false;
                mImage.x = 500;
                if (dnlFileNames[mMediaIndex] != null) {
                    openFile(dnlFileNames[mMediaIndex]);
                } else {
                    mTextBox.Visible = false;
                    mImage.x = 800;
                    draw();
                }
            } else {
                if (mMediaDnl != null)
                    mMediaDnl.abort();
                reset();
                if (mIsInitialized)
                    doTerug(0, "");
            }
            if (mAllMedia.getChild(mMediaIndex + 1) == null) {
                setMenuText(0, "Terug");
                setMenuIcon(0, AppController.sTerugIcon);
            }
            draw();
        }

        void reset() {
            mMediaIndex = -1;
            if (mAllMedia.getChild(1) != null) {
                setMenuText(0, "Volgende");
                setMenuIcon(0, AppController.sVolgIcon);
            }
        }
 
        public bool setContent(XMLement xml) {
            setMenuText(0, "Volgende");
            setMenuIcon(0, AppController.sVolgIcon);
            abortDownload();
            XMLement name = xml.getChildByName("name");
            if (name != null)
                title = name.nodeText;
            mAllMedia = xml.getChildByName("media");
            AppController.sEventLog.WriteLine(mAllMedia.toString());
            mDnlName = "";
            drawNameText();

            mMediaIndex = -1;
            mDownloadIndex = -1;
            mImage.x = 500;

            for (int i = 0; i < 10; i++) dnlFileNames[i] = null;
            if (mAllMedia.getChild(0) != null) {
                doDownloadMedium(0);
            } else {
                setMenuText(0, "Terug");
                setMenuIcon(0, AppController.sTerugIcon);
                return false;
            }
            return true;
        }

        private void reOrient() {
            mNameMess.x = 4;
            
            if (horizontal) {

                mImage.x = 4;
                mImage.y = 56;
                if (mImageBitmap == null) {
                    mImage.size = new Size(240, 180);
                } else {

                    if (mImageBitmap.Width > mImageBitmap.Height)
                        mImage.size = new Size(240, 180);
                    else
                        mImage.size = new Size(135, 180);
                }
                mTextBox.Left = 4;
                mTextBox.Top = 56;
                mTextBox.Size = new Size(280, 116);
                mDnlMess.x = 4;
                mDnlMess.y = 214;
 

            } else {

                mImage.x = 4;
                mImage.y = 54;
                if (mImageBitmap == null) {
                    mImage.size = new Size(180, 240);
                } else {

                    if (mImageBitmap.Width > mImageBitmap.Height)
                        mImage.size = new Size(192, 144);
                    else
                        mImage.size = new Size(180, 240);
                }
                mTextBox.Left = 4;
                mTextBox.Top = 56;
                mTextBox.Size = new Size(200, 196);
                mDnlMess.x = 4;
                mDnlMess.y = 294;
            }
        }


        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            if (mParent != null) {
                mParent.Visible = false;
            }           
            reOrient();
            if (mMediaIndex == -1) {
                AppController.sEventLog.WriteLine("\tDoNext from Onload");
                doNext(0, "");
            }
            mIsInitialized = true;
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
