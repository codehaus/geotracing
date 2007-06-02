// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Boat popup
 *
 * $Id$
 */
BOAT = {
	panel: null,
	boatName: null,
	panelOpen: false,
	initContent: null,

	isCommentPanelOpen: function() {
		return BOAT.panelOpen;
	},

	onPanelClose: function() {
		BOAT.panelOpen = false;
		BOAT.panel.hide();
	},

	show: function(aBoatName) {
		if (BOAT.panel == null) {
			BOAT.panel = new Panel(aBoatName, '#072855', 'white' , null, BOAT.onPanelClose);
			BOAT.panel.setXY(200, 100);
			BOAT.panel.setDimension(400, 300);
			BOAT.initContent = DH.getURL('popup/boot.html');
		}

		// Save target id
		BOAT.boatName = aBoatName;
		BOAT.panel.setTitle(aBoatName);
		BOAT.panel.setContent(BOAT.initContent);
		BOAT.panel.show();
//		BOAT.panel.close = BOAT.onPanelClose;
		BOAT.panelOpen = true;
		BOAT.onShowInfo();
		return false;
	},


	onShowMedia: function() {
		DH.getStyleObject('boatpopupinfo').display = 'none';
		DH.getStyleObject('boatpopupmedia').display = 'block';
	},

	onShowInfo: function() {
		DH.getStyleObject('boatpopupmedia').display = 'none';
		DH.getStyleObject('boatpopupinfo').display = 'block';
		var tracer = GTW.getTracer(BOAT.boatName);
		DH.getObject("tracerid").innerHTML = BOAT.boatName;
	}
}

