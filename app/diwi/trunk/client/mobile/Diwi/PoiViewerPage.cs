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


        TextBox mTextBox = new TextBox();
        DiwiScalingImage mImage;
        Bitmap mImageBitmap;
        XMLement mAllMedia;
        int mMediaIndex = 0;
        string xmlString;

        public PoiViewerPage(DiwiPageBase parent)
            : base(parent) {


            mMenu.addItem("Next", new DiwiUIMenu.DiwiMenuCallbackHandler(doNext));

            this.Controls.Add(mTextBox);
            mImage = new DiwiScalingImage(this);


            mTextBox.Multiline = true;
            mTextBox.ReadOnly = true;
            mTextBox.ScrollBars = ScrollBars.Vertical;
            mTextBox.ForeColor = Color.Black;
            mTextBox.BackColor = Color.Transparent;

            reOrient();


            addDrawable(mImage);

            title = "";

        }

        void openVideo(string fn) {
            if (mIsActive == false) return;
            Process process = new Process();
            process.StartInfo.FileName = fn;
            process.StartInfo.Verb = "Open";
            process.StartInfo.UseShellExecute = true;
            process.Start();
        }

        void openVideoT(string path) {
            if (InvokeRequired) {
                this.Invoke(new mediaCallback(this.openVideo), new object[] { path });
            } else {
                openVideo(path);
            }
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

        void openImageT(string path) {
            if (InvokeRequired) {
                this.Invoke(new mediaCallback(this.openImage), new object[] { path });
            } else {
                openImage(path);
            }
        }


        void openAudio(string fn) {
            if (mIsActive == false) return;

            Process p = Process.GetCurrentProcess();

            Process process = new Process();
            process.StartInfo.FileName = fn;
            process.StartInfo.Verb = "Open";
            process.StartInfo.UseShellExecute = true;
            process.Start();

            p.Start();
        }

        void openAudioT(string path) {
            if (InvokeRequired) {
                this.Invoke(new mediaCallback(this.openAudio), new object[] { path });
            } else {
                openAudio(path);
            }
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

        void openTextT(string path) {
            if (InvokeRequired) {
                this.Invoke(new mediaCallback(this.openText), new object[] { path });
            } else {
                openText(path);
            }
        }

        
        
        void doNext(int i, string s) {
            mMediaIndex++;
            if (mMediaIndex == -1) {
                openText(@"\DemoNMEA.txt");
                return;
            }
            XMLement kichUri = mAllMedia.getChild(mMediaIndex);
            if (kichUri == null) {
                doTerug(0, "");
            } else {
                string url = kichUri.nodeText;
                string ext = url.Substring(url.LastIndexOf('.'));
                string type = kichUri.getAttributeValue("type");
                mTextBox.Visible = false;
                mImage.x = 500;
                switch (type) {
                    case "image":
                        new MediaDownloader(url, "poiImage" + mMediaIndex.ToString() + ext, new AppController.DownloadCallbackHandler(openImageT));
                        break;
                    case "audio":
                        new MediaDownloader(url, "poiSound" + mMediaIndex.ToString() + ext, new AppController.DownloadCallbackHandler(openAudioT));
                        break;
                    case "video":
                        new MediaDownloader(url, "poiVideo" + mMediaIndex.ToString() + ext, new AppController.DownloadCallbackHandler(openVideoT));
                        break;
                    case "text":
                        new MediaDownloader(url, "poiText" + mMediaIndex.ToString() + ext, new AppController.DownloadCallbackHandler(openTextT));
                        break;
                }
            }
            if ( mAllMedia.getChild(mMediaIndex + 1) == null) {
                setMenuText(0, "Terug");
            }
            draw();
        }

        public void setContent(XMLement xml) {
            setMenuText(0, "Next");
            xml = xml.getChildByName("poi");
            if (xml != null) {
                XMLement name = xml.getChildByName("name");
                if (name != null)
                    title = name.nodeText;
                mAllMedia = xml.getChildByName("media");
                xmlString = mAllMedia.toString();
                mMediaIndex = -1;
                doNext(0, "");
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

            AppController.SysBeep();
            AppController.SysBeep();
            if (mParent != null) {
                mParent.Visible = false;
            }
            
            reOrient();

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
