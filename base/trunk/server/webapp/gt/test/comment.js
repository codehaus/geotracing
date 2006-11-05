var APP = {


	init: function() {

	},

	addComment: function(id, content) {
		APP.pr('creating comment');

		var cmtForm = document.getElementById('commentform');

		// Do the comment
		var req = APP.createCommentReq(cmtForm.targetid.value,
				cmtForm.content.value, cmtForm.name.value, cmtForm.url.value, cmtForm.email.value);
		APP.pr('request created: ' + req.documentElement.nodeName);
		DH.postXML(DH.getBaseDir() + '/../srv/put.jsp', req, APP.commentRsp);
		APP.pr('comment posted...');
		return false;
	},

	createCommentReq: function(id, content, name, url, email) {
		// Create put request
		var doc = DH.createXmlDocument();
		var reqElm = doc.createElement('cmt-insert-req');

		APP.addChildTextNode(doc, reqElm, 'target', id);
		APP.addChildTextNode(doc, reqElm, 'content', content);

		if (name) {
			APP.addChildTextNode(doc, reqElm, 'author', name);
		}
		if (url) {
			APP.addChildTextNode(doc, reqElm, 'url', url);
		}
		if (email) {
			APP.addChildTextNode(doc, reqElm, 'email', email);
		}
		doc.appendChild(reqElm);
		return doc;
	},

	addChildTextNode: function(doc, parent, tag, text) {
		var elm = doc.createElement(tag);
		var textNode = doc.createTextNode(text);
		elm.appendChild(textNode);
		parent.appendChild(elm);
	},

	commentRsp: function(rsp) {
		// APP.pr('response received rsp=' + rsp);
 		if (rsp != null) {
			APP.pr('rsp tag=' + rsp.documentElement.nodeName);
		} else {
			APP.pr('NULL response');
		}
	},

	pr: function(s) {
		DH.addHTML('debug', s + '<br/>');
	}
}

DH.onReady = APP.init;


