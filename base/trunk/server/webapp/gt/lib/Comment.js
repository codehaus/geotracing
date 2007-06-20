// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Comment handling.
 *
 * $Id$
 */
CMT = {
	commentPanel: null,
	currentTarget: '0',
	panelOpen: false,
	commentList: 'empty',

	addComment: function() {
		var cmtForm = DH.getObject('commentform');

		// Do the comment
		var req = SRV.createXMLReq('cmt-insert-req',
				'target', CMT.currentTarget,
				'content', cmtForm.content.value,
				'author', cmtForm.name.value,
				'url', cmtForm.url.value,
				'email', cmtForm.email.value);
		SRV.put(req, CMT.addCommentRsp);
		return false;
	},

	addCommentRsp: function(aRspDoc) {
		CMT.hideAddCommentForm();

		if (aRspDoc != null) {
			// alert('rsp tag=' + aRspDoc.documentElement.nodeName);
		} else {
			alert('NULL response');
		}
		CMT.displayComments();
	},

	displayComments: function() {
		var cmtList = DH.getObject('commentlist');
		if (cmtList) {
			cmtList.innerHTML = 'loading comments...';
		}
		SRV.get('q-by-example', CMT.displayCommentsRsp, 'table', 'kw_comment', 'target', CMT.currentTarget);
	},

	displayCommentsRsp: function(records) {
		var html = records.length + ' comments for ' + CMT.currentType + ' "' + CMT.currentName + '"<br/>';
		var date;
		var url;
		for (var i = 0; i < records.length; i++) {
			html += '<hr>'
			html = html + '<p>From: ' + records[i].getField('author') + '<br/>';
			time = new Number(records[i].getField('creationdate'));
			date = new Date(time);
			html = html + 'Date: ' + date.format("DDD D MMM YYYY HH:mm:ss") + '<br/>';
			url = records[i].getField('url');
			if (url != null) {
				html = html + 'Website: <a href="' + url + '" target="_new">' + url + '</a></p>';
			}
			html = html + '<p><i>' + records[i].getField('content') + '</i></p>';
		}

		CMT.commentList = html;
		CMT._waitForPanelLoaded();
	},

	isCommentPanelOpen: function() {
		return CMT.panelOpen;
	},

	onPanelClose: function() {
		CMT.panelOpen = false;
	},

	showCommentPanel: function(aTargetId, aTargetType, aTargetName) {
		if (CMT.commentPanel == null) {
			CMT.commentPanel = new Panel('commentpanel', '#000044', 'white', null, CMT.onPanelClose);
			CMT.commentPanel.setDimension(500, 500);
			CMT.commentPanel.setXY(50, 50);
		}


		// Save target id
		CMT.currentTarget = aTargetId + '';
		CMT.currentType = aTargetType;
		CMT.currentName = aTargetName;
		CMT.commentPanel.show();
		CMT.commentPanel.loadContent('content/comment.html');
		CMT.commentPanel.setTitle('Comments for ' + CMT.currentType + '[' + CMT.currentTarget + ']' );
		CMT.displayComments();
		CMT.panelOpen = true;
		return false;
	},


	hideAddCommentForm: function() {
		DH.getStyleObject('commentform').display = 'none';
		DH.show('addcommentlink');
	},

	showAddCommentForm: function() {
		DH.getStyleObject('commentform').display = 'block';
		DH.hide('addcommentlink');
	},

	_waitForPanelLoaded: function(html) {
		var cmtList = DH.getObject('commentlist');
		if (cmtList) {
			cmtList.innerHTML = CMT.commentList;
		} else {
			setTimeout("CMT._waitForPanelLoaded()", 75);
		}
	}


}

