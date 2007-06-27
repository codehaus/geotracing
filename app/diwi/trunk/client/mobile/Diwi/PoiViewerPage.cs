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

        XMLement mAllMedia;
        int mMediaIndex = 0;
        string xmlString;

        public PoiViewerPage(DiwiPageBase parent)
            : base(parent) {


            mMenu.addItem("Terug", new DiwiUIMenu.DiwiMenuCallbackHandler(doTerug));

            this.Controls.Add(mTextBox);
            mImage = new DiwiScalingImage(this);


            mTextBox.Multiline = true;
            mTextBox.ForeColor = Color.Black;
            mTextBox.BackColor = Color.Transparent;

            reOrient();


            addDrawable(mImage);

            title = "";

        }

        void openVideo(string fn) {
        }

        void openImage(string fn) {
            mImage.bitmap = new Bitmap(fn);
            //mImage.Visible = true;
            draw();
        }

        void openAudio(string fn) {
        }

        void openText(string fn) {
        }

        void doNext(int i, string s) {
            mMediaIndex++;
            XMLement kichUri = mAllMedia.getChild(mMediaIndex);
            if (kichUri == null) {
                doTerug(0, "");
            } else {
                string url = kichUri.nodeText;
                string type = kichUri.getAttributeValue("type");
                mTextBox.Visible = false;
                mImage.Visible = false;
                switch (type) {
                    case "image":
                        new MediaDownloader(url, "poiImage" + mMediaIndex.ToString() + ".jpg", new AppController.DownloadCallbackHandler(openImage));
                        break;
                    case "audio":
                        new MediaDownloader(url, "poiSound" + mMediaIndex.ToString() + ".mp3", new AppController.DownloadCallbackHandler(openAudio));
                        break;
                    case "video":
                        new MediaDownloader(url, "poiVideo" + mMediaIndex.ToString() + ".3gp", new AppController.DownloadCallbackHandler(openVideo));
                        break;
                    case "text":
                        new MediaDownloader(url, "poiText" + mMediaIndex.ToString() + ".txt", new AppController.DownloadCallbackHandler(openText));
                        break;
                }
                // image, video, text, audio
            }
        }

        public void setContent(XMLement xml) {
            int i = 0;
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

            
 
            /*
             * - <poi-get-rsp>
- <poi table="diwi_poi" id="22390">
  <id>22390</id> 
  <kichid>5_5</kichid> 
  <name>poi 5</name> 
  <description>beschrijving poi 5</description> 
  <type>1</type> 
  <state>1</state> 
  <category>italiaans</category> 
  <creationdate>1181907022669</creationdate> 
  <modificationdate>1181907022757</modificationdate> 
- <media>
- <media>
  <kich-uri>http://test.digitalewichelroede.nl/diwi/media.srv?id=22391</kich-uri> 
  <kich-uri>http://test.digitalewichelroede.nl/diwi/media.srv?id=22393</kich-uri> 
  </media>
  </media>
  <extra /> 
  <point>SRID=4326;POINT(5.6222532758092845 52.176457223006295)</point> 
  </poi>
  </poi-get-rsp>
             * 
             * 
             * */



        private void reOrient() {
            if (horizontal) {
                mImage.x = 4;
                mImage.y = 40;
                Size s = new Size(240, 160);
                mImage.size = s;

                mTextBox.Left = 4;
                mTextBox.Top = 40;
                mImage.size = s;

            } else {
                mImage.x = 4;
                mImage.y = 40;
                Size s = new Size(160, 240);
                mImage.size = s;

                mTextBox.Left = 4;
                mTextBox.Top = 40;
                mImage.size = s;
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
