// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Test KWClientExt.js.
 *
 * $Id$
 */



var KWCT = {
	cmtId: null,
	cmtTargetId: '68',

	/** Send heartbeat. */
	doTests: function() {
		KWCT.pr('start tests..');

		KWCT.pr('add comment..');
		KW.CMT.add(KWCT.onAddCommentRsp, KWCT.cmtTargetId, 'my comment text');
	},

	go: function() {
		KWCT.pr('start');
		// KeyWorx client
		KW.init(KWCT.onRsp, KWCT.onNegRsp, 1);
		KW.login('geoapp-user', 'user');
		KWCT.pr('login sent');
	},

	pr: function (s) {
		var elm = document.getElementById('result');
		elm.innerHTML = elm.innerHTML + '<br/>' + s;
	},

 /** Send heartbeat. */
	sendHeartbeat: function() {
		KWCT.pr('sending heartbeat..');
		var req = KW.createRequest('t-hb-req');
		KW.utopia(req);
	},

/** Add POI to current active Track. */
	addTag: function () {
		KWCT.pr('adding Text...');
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

	deleteMedium: function (id) {
		KWCT.pr('deleting medium...id=' + id);
		KW.MEDIA.del(KWCT.onDeleteMediumRsp, id);
	},

	onDeleteMediumRsp: function(elm) {
		KWCT.pr('deleted medium OK');
		DH.getObject('uploadedMedium').innerHTML = 'medium deleted';
		DH.getObject('uploadedMediumActions').innerHTML = ' ';
	},

	updateMedium: function (id) {
		KWCT.pr('updateMedium...id=' + id);
		KW.MEDIA.update(KWCT.onUpdateMediumRsp, id, 'new name', 'new description');
	},

	onUpdateMediumRsp: function(elm) {
		KWCT.pr('onUpdateMediumRsp  OK');
		// DH.getObject('uploadedMedium').innerHTML = 'medium updated';
	},

	uploadMedium: function () {
		KWCT.pr('uploading medium...');
		KW.MEDIA.upload(KWCT.onUploadMediumRsp, DH.getObject('addmediumform'));
		return false;
	},

	onUploadMediumRsp: function(elm) {
		// var rsp = elm.getElementsByTagname('medium-insert-rsp');
		KWCT.pr('medium upload: ' + elm.tagName + ' id=' + elm.getAttribute('id'));
	//	if (elm.tagname == 'medium-insert-rsp') {
		var mediumDiv = DH.getObject('uploadedMedium');
		var id = elm.getAttribute('id');
		mediumDiv.innerHTML = '<img src="../media.srv?id=' + id + '&resize=100" border="0" />';
		var mediumActDiv = DH.getObject('uploadedMediumActions');
		mediumActDiv.innerHTML = '[<a href="#" onclick="KWCT.deleteMedium(' + id + '); return false;">delete</a>] ' +
							  '[<a href="#" onclick="KWCT.updateMedium(' + id + '); return false;">update</a>]';

	//	}
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
	}
}
DH.onReady = KWCT.go;



