using System;
using System.Collections;
using System.Text;
using System.Xml;
using Diwi;

namespace Diwi {
    class XMLement {
        private string mTag;
        private string mNodeText;
        Hashtable mAttribs;
        ArrayList mChildren;

        public ArrayList children {
            get {
                return mChildren;
            }
        }
        
        public string tag {
            get {
                return mTag;
            }
            set {
                mTag = value;
            }
        }

        public string nodeText {
            get {
                return mNodeText;
            }
            set {
                mNodeText = value;
            }
        }

        public XMLement() {
            mAttribs = new Hashtable();
            mTag = "";
            mNodeText = null;
            mChildren = new ArrayList();
        }


        public XMLement(string t) {
            mAttribs = new Hashtable();
            mTag = t;
            mNodeText = null;
            mChildren = new ArrayList();
        }


        public void addChild(XMLement xm) {
            mChildren.Add(xm);
        }

        public void addAttribute(string key, string val) {
            mAttribs[key] = val;
        }

        public string getChildValue(string childTag) {
            XMLement child = getChildByName(childTag);
            if (child != null) return child.mNodeText;
            return null;
        }

        public XMLement getChildByName(string key) {
            foreach (XMLement xml in mChildren) {
                if (xml.mTag == key)
                    return xml;
            }
            return null;
        }

        public XMLement getChild(int i) {
            if (i < mChildren.Count) return (XMLement)mChildren[i];
            return null;
        }

        public string getAttributeValue(string key) {
            if (mAttribs.Contains(key))
                return (string)mAttribs[key];
            return null;
        }

        public XMLement firstChild() {
            if(mChildren.Count > 0)
                return (XMLement)mChildren[0];
            return null;
        }

        public string toString() {
            StringBuilder sb = new StringBuilder("<");
            sb.Append(mTag + " ");

            IDictionaryEnumerator en = mAttribs.GetEnumerator();

            while (en.MoveNext()) {
                sb.Append(en.Key + "=\"" + en.Value + "\" ");
            }

            sb.Append(">");

            if (mNodeText != null) {
                sb.Append(mNodeText);
            }

            foreach (XMLement xml in mChildren) {
                sb.Append(xml.toString());
            }

            sb.Append("</" + mTag + ">");

            return sb.ToString();

        }

        private XMLement(XmlNode msNode) {
            mAttribs = new Hashtable();

            if (msNode.Attributes.Count > 0) {
                XmlAttribute[] attrArr;
                attrArr = new XmlAttribute[msNode.Attributes.Count];
                msNode.Attributes.CopyTo(attrArr, 0);
                foreach (XmlAttribute a in attrArr) {
                    mAttribs.Add(a.Name, a.Value);
                }
            }

            mTag = msNode.Name;
            mNodeText = msNode.Value;
            mChildren = new ArrayList();

            if (msNode.HasChildNodes) {
                XmlNodeList children = msNode.ChildNodes;

                foreach (XmlNode x in children) {
                    if (x.GetType() == typeof(System.Xml.XmlText))
                        mNodeText = x.Value;
                    else
                        mChildren.Add(new XMLement(x));
                }

            }
        }

        static public XMLement createFromRawXml(string rawXml) {
            XmlElement msEl = null;
            XmlDocument msXml = new XmlDocument();

            try {
                msXml.LoadXml(rawXml);
                msEl = msXml.DocumentElement;
            } catch (XmlException e) {
                string str = string.Format("Caught XmlException: {0}", e.ToString());
                AppController.sLog.WriteLine(str);
                return null;
            }
            return new XMLement(msEl);
        }
    }
}
