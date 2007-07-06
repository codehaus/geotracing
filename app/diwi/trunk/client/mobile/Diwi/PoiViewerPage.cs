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
        string[] dnlFileNames = new string[10];


        public PoiViewerPage(DiwiPageBase parent)
            : base(parent) {


            mMenu.addItem("Volgende", new DiwiUIMenu.DiwiMenuCallbackHandler(doNext));

            this.Controls.Add(mTextBox);
            mImage = new DiwiScalingImage(this);

            mTextBox.Font = new Font("Arial", 12, FontStyle.Bold);
            mTextBox.Multiline = true;
            mTextBox.ReadOnly = true;
            mTextBox.ScrollBars = ScrollBars.Vertical;
            mTextBox.ForeColor = Color.Black;
            mTextBox.BackColor = Color.Transparent;

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



        void openImage(string fn) {
            if (mIsActive == false) return;
            mImageBitmap = new Bitmap(fn);
            reOrient();
            mImage.bitmap = mImageBitmap;
            mImage.x = 10;
            mTextBox.Visible = false;
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
            mImage.x = 500;
        }

        void openFile(string path) {
            int n = path.IndexOf("Image");
            if (n >= 0) {
                openImage(path);
                return;
            }

            n = path.IndexOf("Sound");
            if (n >= 0) {
                openAudio(path);
                return;
            }

            n = path.IndexOf("Video");
            if (n >= 0) {
                openVideo(path);
                return;
            }

            n = path.IndexOf("Text");
            if (n >= 0) {
                openText(path);
                return;
            }

        
        }


        void dnlDoneT(string path) {

            mMediaDnl = null;

            AppController.sEventLog.WriteLine("\tdownloaded url: {0}", mDnlUrl);

            dnlFileNames[mDownloadIndex] = path;
            if (mDownloadIndex == mMediaIndex) {
                if (InvokeRequired) {
                    this.Invoke(new mediaCallback(this.openFile), new object[] { path });
                } else {
                    openFile(path);
                }
            }

            if (mAllMedia.getChild(1 + mDownloadIndex) != null) {
                mDownloadIndex = mDownloadIndex + 1;
                doDownloadMedium(mDownloadIndex);
            }

        }

        void doDownloadMedium(int index) {
            char[] trimChars = { '\t', ' ', '\n', '\r' };
            XMLement kichUri = mAllMedia.getChild(index);
            mDownloadIndex = index;
            if (kichUri != null) {
                string url = kichUri.nodeText;
                string ext = (url.Substring(url.LastIndexOf('.'))).TrimEnd(trimChars);
                string type = kichUri.getAttributeValue("type");
                switch (type) {
                    case "image":
                        mDnlUrl = url;
                        mMediaDnl = new MediaDownloader(url, "poiImage" + mMediaIndex.ToString() + ext, new AppController.DownloadCallbackHandler(dnlDoneT));
                        break;
                    case "audio":
                        mDnlUrl = url;
                        mMediaDnl = new MediaDownloader(url, "poiSound" + mMediaIndex.ToString() + ext, new AppController.DownloadCallbackHandler(dnlDoneT));
                        break;
                    case "video":
                        mDnlUrl = url;
                        mMediaDnl = new MediaDownloader(url, "poiVideo" + mMediaIndex.ToString() + ext, new AppController.DownloadCallbackHandler(dnlDoneT));
                        break;
                    case "text":
                        mDnlUrl = url;
                        mMediaDnl = new MediaDownloader(url, "poiText" + mMediaIndex.ToString() + ext, new AppController.DownloadCallbackHandler(dnlDoneT));
                        break;
                }
            }
        }

        void doNext(int i, string s) {
            mMediaIndex++;
            XMLement kichUri = mAllMedia.getChild(mMediaIndex);
            if (kichUri != null) {
                mTextBox.Visible = false;
                mImage.x = 500;
                if (dnlFileNames[mMediaIndex] != null) {
                    openFile(dnlFileNames[mMediaIndex]);
                } 
            } else {
                if (mMediaDnl != null)
                    mMediaDnl.abort();
                reset();
                doTerug(0, "");
            }
            if (mAllMedia.getChild(mMediaIndex + 1) == null) {
                setMenuText(0, "Terug");
            }
            draw();
        }

        void reset() {
            mMediaIndex = -1;
            if (mAllMedia.getChild(1) != null) {
                setMenuText(0, "Volgende");
            }
        }
 
        public void setContent(XMLement xml) {
            setMenuText(0, "Volgende");
            abortDownload();
            XMLement name = xml.getChildByName("name");
            if (name != null)
                title = name.nodeText;
            mAllMedia = xml.getChildByName("media");
            xmlString = mAllMedia.toString();
            mMediaIndex = -1;
            mDownloadIndex = -1;
            mImage.x = 500;

            for (int i = 0; i < 10; i++) dnlFileNames[i] = null;
            if (mAllMedia.getChild(0) != null) {
                doDownloadMedium(0);
                //doNext(0, "");
            } else {
                setMenuText(0, "Terug");
            }
        }

        private void reOrient() {
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
                mTextBox.Size = new Size(280, 180);

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
                mTextBox.Size = new Size(200, 260);

            }
        }


        protected override void OnLoad(EventArgs e) {
            base.OnLoad(e);
            if (mParent != null) {
                mParent.Visible = false;
            }
            
            reOrient();

            if (mMediaIndex == -1) doNext(0, "");

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
