var APP = {


	init: function() {
	   SRV.init();
	},

	addComment: function(id, content) {
		APP.pr('creating comment');

		var cmtForm = document.getElementById('commentform');

		// Do the comment
		var req = SRV.createXMLReq('cmt-insert-req',
				'target', cmtForm.targetid.value,
				'content', cmtForm.content.value,
				'author', cmtForm.name.value,
				'url', cmtForm.url.value,
				'email', cmtForm.email.value);
		APP.pr('request created: ' + req.documentElement.nodeName);
		SRV.put(req, APP.commentRsp);
		APP.pr('comment posted...');
		return false;
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


