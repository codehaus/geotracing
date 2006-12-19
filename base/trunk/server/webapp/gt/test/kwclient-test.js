// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Test KWClientExt.js.
 *
 * $Id$
 */

DH.include('KWClient.js');
DH.include('KWClientExt.js');


var KWCT = {
	cmtId: null,
	cmtTargetId: '68',

	go: function() {
		KWCT.pr('start');
		// KeyWorx client
		KW.init(KWCT.onRsp, KWCT.onNegRsp);
		KW.login('geoapp-user', 'user');
		KWCT.pr('login sent');
	},

	pr: function (s) {
		var elm = document.getElementById('result');
		elm.innerHTML = elm.innerHTML + '<br/>' + s;
	},


/** Add POI to current active Track. */
	addTag: function () {
		KWCT.pr('adding Text...');
	},

/** Send heartbeat. */
	doTests: function() {
		KWCT.pr('start tests..');

		KWCT.pr('add comment..');
		KW.CMT.add(KWCT.onAddCommentRsp, KWCT.cmtTargetId, 'my comment text');
	},

	onAddCommentRsp: function(elm) {
		KWCT.cmtId = elm.getAttribute('id');
		KWCT.pr('comment add id=' + KWCT.cmtId + ', marking as read...'+ elm.tagName);
		KW.CMT.markReadForComment(KWCT.onMarkReadForCommentRsp,  KWCT.cmtId);
		//KW.CMT.markReadForTarget(KWCT.onMarkReadForTargetRsp, KWCT.cmtTargetId);
	},

	onMarkReadForTargetRsp: function(elm) {
		KWCT.pr('onMarkReadForTargetRsp marked read OK, markReadForComment...id=' + elm.getAttribute('ids'));
		KW.CMT.markReadForComment(KWCT.onMarkReadForCommentRsp,  elm.getAttribute('ids'));
	},

	onMarkReadForCommentRsp: function(elm) {
		KWCT.pr('onMarkReadForCommentRsp marked read OK, deleting...');
		KW.CMT.del(KWCT.onDelCommentRsp, KWCT.cmtId);
	},

	onDelCommentRsp: function(elm) {
		KWCT.pr('onDelCommentRsp: '+ elm.tagName);
	},

	uploadMedium: function () {
		KWCT.pr('uploading medium...');
		KW.MEDIA.uploadMedium(KWCT.onMediumUploadRsp, DH.getObject('addmediumform'));
		return false;
	},

	onMediumUploadRsp: function(elm) {
		KWCT.pr('medium upload: ' + elm.tagName);
		var rsp = elm.getElementsByTagname('medium-insert-rsp'); 
	},

/** KWClient positive response handlers. */

	onSuspendRsp: function(elm) {
		KWCT.suspended = true;
		KWCT.pr('track suspended OK');
	},

	onResumeRsp: function(elm) {
		KWCT.suspended = false;
		KWCT.pr('track resumed OK');
	},

	onRsp: function(elm) {
		if (!elm) {
			KWCT.pr('empty response');
			return;
		}
		KWCT.pr('server response ' + elm.tagName);
		if (elm.tagName == 'login-rsp') {
			KW.selectApp('geoapp', 'user');
		} else if (elm.tagName == 'select-app-rsp') {
			KWCT.pr('login OK');
			KWCT.doTests();
		} else if (elm.tagName == 'logout-rsp') {
			KWCT.pr('logout OK');
			window.clearInterval(KWCT.hbTimer);
		} else if (elm.tagName == 't-hb-rsp') {
			KWCT.pr('heartbeat OK');
		} else {
			KWCT.pr('rsp tag=' + elm.tagName + ' ' + elm);
		}
		// alert('onSelect name=' + name + ' value=' + value + ' label=' + label);
	},

/** KWClient negative response handler. */
	onNegRsp: function(errorId, error, details) {
		KWCT.pr('negative resp:' + error + ' details=' + details);
	},

	_checkIFrameRsp: function() {
		var iframe = DH.getObject('uploadFrame');
		if (!iframe) {
			KWCT.pr('cannot get uploadFrame');
			return;
		}

		var iframeDoc = null;

		if (iframe.contentDocument) {
			// For NS6
			iframeDoc = iframe.contentDocument;
		} else if (iframe.contentWindow) {
			// For IE5.5 and IE6
			iframeDoc = iframe.contentWindow.document;
		} else if (iframe.document) {
			// For IE5
			iframeDoc = iframe.document;
		}
		if (iframeDoc == null) {
			KWCT.pr('iframeDoc == null, recheck..');
			setTimeout('KWCT._checkIFrameRsp()', 2000);
		} else {
			KWCT.pr('iframeDoc found !!!');
			// KWCT.onRsp(iframeDoc.documentElement);
			KWCT.pr('iframeDoc.innerHTML=' + iframeDoc.documentElement.innerHTML);
		}
	},

// http://developer.apple.com/internet/webcontent/iframe.html
	_createIFrame: function() {
		if (!document.createElement) {
			return true
		}

		if (KWCT.iframe == null && document.createElement) {
			// create the IFrame and assign a reference to the
			// object to our global variable KWCT.iframe.
			// this will only happen the first time
			// callToServer() is called
			try {
				var tempIFrame = document.createElement('iframe');
				tempIFrame.setAttribute('id', 'rspFrame');
				tempIFrame.style.border = '0px';
				tempIFrame.style.width = '0px';
				tempIFrame.style.height = '0px';
				KWCT.iframe = document.body.appendChild(tempIFrame);

				if (document.frames) {
					// this is for IE5 Mac, because it will only
					// allow access to the document object
					// of the IFrame if we access it through
					// the document.frames array
					KWCT.iframe = document.frames['rspFrame'];
				}
			} catch(exception) {
				// This is for IE5 PC, which does not allow dynamic creation
				// and manipulation of an iframe object. Instead, we'll fake
				// it up by creating our own objects.
				iframeHTML = '\<iframe id="rspFrame" style="';
				iframeHTML += 'border:0px;';
				iframeHTML += 'width:0px;';
				iframeHTML += 'height:0px;';
				iframeHTML += '"><\/iframe>';
				document.body.innerHTML += iframeHTML;
			}
		}
	}

}
DH.onReady = KWCT.go;



